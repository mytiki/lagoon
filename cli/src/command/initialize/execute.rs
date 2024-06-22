use std::error::Error;

use super::{
    Cli,
    super::super::utils::{EcrRepository, S3Bucket, StsAccount},
};

pub async fn execute(cli: &Cli) -> Result<(), Box<dyn Error>> {
    let account = StsAccount::connect(cli.profile()).await?;
    let bucket = format!("mytiki-lagoon-rust-cli-test-{}", account.account_id());
    let bucket = S3Bucket::connect(cli.profile(), &bucket).await?;
    let repository = "mytiki-lagoon-rust-cli-test";
    let repository = EcrRepository::connect(cli.profile(), repository).await?;
    Ok(())
}
