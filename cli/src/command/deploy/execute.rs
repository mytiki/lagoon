use std::error::Error;

use super::{
    Cli,
    execute_log, execute_pipeline, execute_prepare, execute_write,
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
        execute_log::execute(&account, dist).await?;
        execute_prepare::execute(&account, dist, &bucket).await?;
        execute_pipeline::execute(&account, cli, bucket.name()).await?;
        execute_write::execute(&account, dist, &bucket).await?;
    } else {
        match cli.module().unwrap() {
            Module::Log => execute_log::execute(&account, dist).await?,
            Module::Prepare => execute_prepare::execute(&account, dist, &bucket).await?,
            Module::Pipeline => execute_pipeline::execute(&account, cli, bucket.name()).await?,
            Module::Write => execute_write::execute(&account, dist, &bucket).await?,
        }
    }
    log::info!("Deployment complete.");
    Ok(())
}
