use std::error::Error;

use aws_sdk_ecr::{Client, primitives::DateTime, types::ImageTagMutability};
use aws_sdk_ecr::types::ImageIdentifier;

use super::super::utils::StsAccount;

pub struct EcrRepository {
    client: Client,
    name: String,
}

impl EcrRepository {
    pub async fn connect(profile: &str, name: &str) -> Result<Self, Box<dyn Error>> {
        let config = aws_config::from_env().profile_name(profile).load().await;
        let client = Client::new(&config);
        let account = StsAccount::connect(profile).await?;
        Self::create_if_not_exists(&client, name, account.account_id()).await?;
        Ok(Self {
            client,
            name: name.to_string(),
        })
    }

    async fn create_if_not_exists(
        client: &Client,
        name: &str,
        account: &str,
    ) -> Result<(), Box<dyn Error>> {
        let exists = client
            .describe_repositories()
            .repository_names(name)
            .send()
            .await;
        match exists {
            Ok(_) => {
                log::debug!("Repository {} already exists", name);
                Ok(())
            }
            Err(_) => {
                log::debug!("Creating repository {}", name);
                client
                    .create_repository()
                    .repository_name(name)
                    .set_image_tag_mutability(Some(ImageTagMutability::Immutable))
                    .send()
                    .await?;
                let policy_document = format!(
                    r#"
                    {{
                        "Version": "2012-10-17",
                        "Statement": [
                            {{
                                "Sid": "CrossAccountAccess",
                                "Effect": "Allow",
                                "Principal": {{
                                    "AWS": "arn:aws:iam::{}:root"
                                }},
                                "Action": [
                                    "ecr:BatchGetImage",
                                    "ecr:GetDownloadUrlForLayer",
                                    "ecr:BatchCheckLayerAvailability"
                                ]
                            }}
                        ]
                    }}"#,
                    account
                );
                client
                    .set_repository_policy()
                    .repository_name(name)
                    .policy_text(policy_document.to_string())
                    .send()
                    .await?;
                Ok(())
            }
        }
    }

    pub async fn has_image(&self, image_tag: &str) -> Result<bool, Box<dyn Error>> {
        let resp = self
            .client
            .list_images()
            .repository_name(self.name.clone())
            .send()
            .await?;
        let images = resp.image_ids();
        let tag_exists = images
            .iter()
            .any(|image| image.image_tag().map_or(false, |t| t == image_tag));
        Ok(tag_exists)
    }

    pub async fn get_image(&self, prefix: &str) -> Result<String, Box<dyn Error>> {
        let error_message = "No matching tags found.";
        let rsp = self
            .client
            .list_images()
            .repository_name(self.name.clone())
            .send()
            .await?;
        let mut matching_tags: Vec<ImageIdentifier> = Vec::new();
        rsp.image_ids().iter().for_each(|image| {
            if let Some(tag) = image.image_tag() {
                if tag.starts_with(prefix) {
                    matching_tags.push(image.clone());
                }
            }
        });
        let matching_tags = (!matching_tags.is_empty()).then_some(matching_tags);
        if matching_tags.is_none() {
            return Err(error_message.into());
        }
        let describe_tags = self
            .client
            .describe_images()
            .set_image_ids(matching_tags)
            .repository_name(self.name.clone())
            .send()
            .await?;
        let mut images: Vec<(DateTime, Option<Vec<String>>)> = describe_tags
            .image_details()
            .iter()
            .filter_map(|details| {
                details
                    .image_pushed_at
                    .and_then(|pushed_at| Some((pushed_at, details.image_tags.clone())))
            })
            .collect();
        images.sort_by(|a, b| b.0.cmp(&a.0));
        let tag = images
            .first()
            .map(|(_, tags)| {
                let tags = tags.clone().ok_or(error_message)?;
                let tag = tags.first().ok_or(error_message)?;
                Ok(tag.clone())
            })
            .ok_or("No matching tags found")?;
        tag
    }
}
