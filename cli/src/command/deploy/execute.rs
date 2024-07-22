use std::error::Error;

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
    let bucket = S3Bucket::connect(profile, &name).await?;

    if cli.module().is_none() {
        execute_load::execute(&account, dist, &bucket).await?;
        execute_pipeline::execute(&account, cli, bucket.name()).await?;
    } else {
        match cli.module().unwrap() {
            Module::Load => execute_load::execute(&account, dist, &bucket).await?,
            Module::Pipeline => execute_pipeline::execute(&account, cli, bucket.name()).await?,
        }
    }
    log::info!("Deployment complete.");
    Ok(())
}
