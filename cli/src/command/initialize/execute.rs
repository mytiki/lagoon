use std::error::Error;

use super::Cli;
use super::super::super::utils::{EcrRepository, resource_name, S3Bucket, StsAccount};

pub async fn execute(profile: &str, _: &Cli) -> Result<(), Box<dyn Error>> {
    log::info!("Initializing Lagoon storage...");
    let account = StsAccount::connect(profile).await?;
    let name = resource_name::from_account(&account);
    S3Bucket::connect(profile, &name).await?;
    EcrRepository::connect(profile, &name).await?;
    log::info!("Initialization complete: s3://{}", name);
    Ok(())
}
