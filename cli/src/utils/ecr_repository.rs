use std::error::Error;

use aws_config::Region;
use aws_sdk_ecr::{Client, types::ImageTagMutability};

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
}
