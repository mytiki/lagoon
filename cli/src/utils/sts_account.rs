use std::error::Error;

use aws_config::Region;
use aws_sdk_sts::Client as StsClient;

#[derive(Clone)]
pub struct StsAccount {
    client: StsClient,
    account_id: String,
    region: Region,
    profile: String,
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
        let region = config.region().ok_or("No region configured for profile")?;
        Ok(Self {
            client,
            account_id,
            region: region.clone(),
            profile: profile.to_string(),
        })
    }

    pub fn account_id(&self) -> &str {
        &self.account_id
    }

    pub fn region(&self) -> &Region {
        &self.region
    }

    pub fn profile(&self) -> &str {
        &self.profile
    }
}
