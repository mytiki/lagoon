use std::error::Error;

use aws_sdk_sts::Client as StsClient;

pub struct StsAccount {
    client: StsClient,
    account_id: String,
}

impl StsAccount {
    pub async fn connect(profile: &str) -> Result<Self, Box<dyn Error>> {
        let config = aws_config::from_env().profile_name(profile).load().await;
        let client = StsClient::new(&config);
        let account_id = client
            .get_caller_identity()
            .send()
            .await?
            .account
            .ok_or("No account ID")?;
        Ok(Self { client, account_id })
    }

    pub fn account_id(&self) -> &str {
        &self.account_id
    }
}
