use std::error::Error;

use super::StsAccount;

const PREFIX: &str = "mytiki-lagoon";

pub fn full_name(account_id: &str, region: &str) -> String {
    format!("{}-{}-{}", PREFIX, account_id, region)
}

pub async fn from_profile(profile: &str) -> Result<String, Box<dyn Error>> {
    let account = StsAccount::connect(profile).await?;
    Ok(full_name(
        account.account_id(),
        &account.region().to_string(),
    ))
}

pub fn from_account(account: &StsAccount) -> String {
    full_name(account.account_id(), &account.region().to_string())
}
