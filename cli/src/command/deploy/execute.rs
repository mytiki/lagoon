use std::error::Error;

use crate::utils::KmsKey;

use super::{
    Cli,
    execute_load, execute_pipeline,
    module::Module,
    super::super::utils::{resource_name, S3Bucket, StsAccount},
};

pub const STACK_PREFIX: &str = "mytiki-lagoon";

pub async fn execute(profile: &str, cli: &Cli) -> Result<(), Box<dyn Error>> {
    log::info!("Deploying mytiki-lagoon modules...");
    let dist = cli.dist();
    let account = StsAccount::connect(profile).await?;
    let name = resource_name::from_account(&account);
    let key = KmsKey::connect(&account, &format!("alias/{}", STACK_PREFIX))
        .await?
        .get_key()
        .await?;
    let bucket = S3Bucket::connect(profile, &name, &key).await?;

    if cli.module().is_none() {
        execute_load::execute(&account, dist, &bucket, &key).await?;
        execute_pipeline::execute(&account, cli, bucket.name(), &key).await?;
    } else {
        match cli.module().unwrap() {
            Module::Load => execute_load::execute(&account, dist, &bucket, &key).await?,
            Module::Pipeline => {
                execute_pipeline::execute(&account, cli, bucket.name(), &key).await?
            }
        }
    }
    log::info!("Deployment complete.");
    Ok(())
}
