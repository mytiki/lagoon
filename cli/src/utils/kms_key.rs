use std::error::Error;

use aws_sdk_kms::{
    Client,
    types::{KeySpec, KeyUsageType},
};

use super::StsAccount;

pub struct KmsKey {
    client: Client,
    alias: String,
    account: StsAccount,
}

impl KmsKey {
    pub async fn connect(account: &StsAccount, alias: &str) -> Result<Self, Box<dyn Error>> {
        let config = aws_config::from_env()
            .profile_name(account.profile())
            .load()
            .await;
        let client = Client::new(&config);
        Self::create_if_not_exists(&client, account.account_id(), alias).await?;
        Ok(Self {
            client,
            alias: alias.to_string(),
            account: account.clone(),
        })
    }

    async fn create_if_not_exists(
        client: &Client,
        account_id: &str,
        alias: &str,
    ) -> Result<(), Box<dyn std::error::Error>> {
        let exists = client
            .list_aliases()
            .send()
            .await?
            .aliases
            .unwrap_or_default()
            .iter()
            .any(|entry| entry.clone().alias_name.unwrap_or_default() == alias);
        if exists {
            log::debug!("Key alias {} already exists", alias);
            Ok(())
        } else {
            log::debug!("Creating key alias {}", alias);
            let rsp = client
                .create_key()
                .key_spec(KeySpec::SymmetricDefault)
                .key_usage(KeyUsageType::EncryptDecrypt)
                .policy(format!(
                    r#"
                {{
                    "Version": "2012-10-17",
                    "Statement": [
                        {{
                            "Effect": "Allow",
                            "Principal": {{"AWS": "arn:aws:iam::{}:root"}},
                            "Action": ["kms:*"],
                            "Resource": "*"
                        }}
                    ]
                  }}"#,
                    account_id
                ))
                .send()
                .await?;
            client
                .create_alias()
                .alias_name(alias)
                .target_key_id(rsp.key_metadata.ok_or("Failed to create alias")?.key_id)
                .send()
                .await?;
            Ok(())
        }
    }

    pub async fn get_key(&self) -> Result<String, Box<dyn Error>> {
        let rsp = self.client.list_aliases().send().await?;
        let list = rsp.aliases.ok_or("Failed to list aliases")?;
        let alias = list
            .iter()
            .find(|alias| alias.alias_name.as_ref().unwrap() == &self.alias)
            .ok_or("Alias not found")?;
        let arn = format!(
            "arn:aws:kms:{}:{}:key/{}",
            self.account.region().to_string(),
            self.account.account_id(),
            alias.target_key_id.clone().ok_or("Key ID not found")?
        );
        Ok(arn)
    }
}
