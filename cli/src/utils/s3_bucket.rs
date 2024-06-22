use std::error::Error;

use aws_config::Region;
use aws_sdk_s3::{
    Client,
    types::{
        BucketLocationConstraint, CreateBucketConfiguration, ServerSideEncryption,
        ServerSideEncryptionByDefault, ServerSideEncryptionConfiguration, ServerSideEncryptionRule,
    },
};

pub struct S3Bucket {
    client: Client,
    name: String,
}

impl S3Bucket {
    pub async fn connect(profile: &str, name: &str) -> Result<Self, Box<dyn Error>> {
        let config = aws_config::from_env().profile_name(profile).load().await;
        let client = Client::new(&config);
        Self::create_if_not_exists(
            &client,
            name,
            config.region().ok_or("Region required for profile")?,
        )
        .await?;
        Ok(Self {
            client,
            name: name.to_string(),
        })
    }

    async fn create_if_not_exists(
        client: &Client,
        name: &str,
        region: &Region,
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
                Ok(())
            }
        }
    }
}
