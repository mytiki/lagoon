use std::error::Error;
use std::fs;
use std::path::PathBuf;

use aws_config::Region;
use aws_sdk_s3::{
    Client,
    primitives::ByteStream,
    types::{
        BucketLocationConstraint, CreateBucketConfiguration, EventBridgeConfiguration,
        NotificationConfiguration, ServerSideEncryption, ServerSideEncryptionByDefault,
        ServerSideEncryptionConfiguration, ServerSideEncryptionRule,
    },
};

pub struct S3Bucket {
    client: Client,
    name: String,
}

impl S3Bucket {
    pub async fn connect(profile: &str, name: &str, key_arn: &str) -> Result<Self, Box<dyn Error>> {
        let config = aws_config::from_env().profile_name(profile).load().await;
        let client = Client::new(&config);
        Self::create_if_not_exists(
            &client,
            name,
            config.region().ok_or("Region required for profile")?,
            key_arn,
        )
        .await?;
        Ok(Self {
            client,
            name: name.to_string(),
        })
    }

    pub async fn upload_bytes(&self, key: &str, body: ByteStream) -> Result<(), Box<dyn Error>> {
        self.client
            .put_object()
            .bucket(&self.name)
            .key(key)
            .body(body)
            .send()
            .await?;
        Ok(())
    }

    pub async fn upload_file(&self, key: &str, path: &str) -> Result<(), Box<dyn Error>> {
        let body = ByteStream::from(fs::read(path)?);
        self.upload_bytes(key, body).await
    }

    pub async fn upload_dir(&self, prefix: &str, dir: &str) -> Result<(), Box<dyn Error>> {
        let files = Self::list_files(dir)?;
        for file in files {
            let key = format!("{}{}", prefix, file.replace(dir, ""));
            self.upload_file(&key, &file).await?;
        }
        Ok(())
    }

    pub fn name(&self) -> &str {
        &self.name
    }

    async fn create_if_not_exists(
        client: &Client,
        name: &str,
        region: &Region,
        key_arn: &str,
    ) -> Result<(), Box<dyn Error>> {
        let exists = client.head_bucket().bucket(name).send().await;
        match exists {
            Ok(_) => {
                log::debug!("Bucket {} already exists", name);
                Ok(())
            }
            Err(_) => {
                log::debug!("Creating bucket {}", name);
                client
                    .create_bucket()
                    .bucket(name)
                    .create_bucket_configuration(
                        CreateBucketConfiguration::builder()
                            .location_constraint(BucketLocationConstraint::from(
                                region.to_string().as_str(),
                            ))
                            .build(),
                    )
                    .send()
                    .await?;
                let encryption_config = ServerSideEncryptionConfiguration::builder()
                    .rules(
                        ServerSideEncryptionRule::builder()
                            .apply_server_side_encryption_by_default(
                                ServerSideEncryptionByDefault::builder()
                                    .kms_master_key_id(key_arn)
                                    .sse_algorithm(ServerSideEncryption::AwsKms)
                                    .build()?,
                            )
                            .bucket_key_enabled(true)
                            .build(),
                    )
                    .build()?;
                client
                    .put_bucket_encryption()
                    .bucket(name)
                    .server_side_encryption_configuration(encryption_config)
                    .send()
                    .await?;
                let notification_config = NotificationConfiguration::builder()
                    .event_bridge_configuration(EventBridgeConfiguration::builder().build())
                    .build();
                client
                    .put_bucket_notification_configuration()
                    .bucket(name)
                    .notification_configuration(notification_config)
                    .send()
                    .await?;
                Ok(())
            }
        }
    }

    fn list_files(dir: &str) -> Result<Vec<String>, Box<dyn Error>> {
        let dir = PathBuf::from(dir);
        let mut files: Vec<String> = Vec::new();
        if dir.is_dir() {
            for entry_result in fs::read_dir(dir)? {
                let entry = entry_result?;
                let path = entry.path();
                let path_str = path.to_str().ok_or("Invalid path")?;
                if path.is_file() {
                    files.push(path_str.to_string());
                } else if path.is_dir() {
                    let mut sub_files = Self::list_files(path_str)?;
                    files.append(&mut sub_files);
                }
            }
        }
        Ok(files)
    }
}
