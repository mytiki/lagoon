use std::error::Error;

use super::{
    Cli,
    module::Module,
    super::super::utils::{CfDeploy, resource_name, S3Bucket, StsAccount},
};

const STACK_PREFIX: &str = "mytiki-lagoon";

pub async fn execute(profile: &str, cli: &Cli) -> Result<(), Box<dyn Error>> {
    let dist = cli.dist();
    let account = StsAccount::connect(profile).await?;
    let name = resource_name::from_account(&account);
    let bucket = S3Bucket::connect(profile, &name).await?;

    if cli.module().is_none() {
        execute_log(profile, dist).await?;
        execute_prepare(profile, dist, &bucket).await?;
    } else {
        match cli.module().unwrap() {
            Module::Log => execute_log(profile, dist).await?,
            Module::Prepare => execute_prepare(profile, dist, &bucket).await?,
        }
    }
    Ok(())
}

async fn execute_log(profile: &str, dist: &str) -> Result<(), Box<dyn Error>> {
    let stack = format!("{}-log", STACK_PREFIX);
    CfDeploy::connect(profile, &stack, &format!("{}/templates/log.yml", dist))
        .await?
        .capability("CAPABILITY_AUTO_EXPAND")?
        .deploy()
        .await?;
    println!("`log` module deployed.");
    Ok(())
}

async fn execute_prepare(
    profile: &str,
    dist: &str,
    bucket: &S3Bucket,
) -> Result<(), Box<dyn Error>> {
    bucket
        .upload_dir("assets/deploy/prepare", "../dist/assets/deploy/prepare")
        .await?;
    let stack = format!("{}-prepare", STACK_PREFIX);
    CfDeploy::connect(profile, &stack, &format!("{}/templates/prepare.yml", dist))
        .await?
        .capability("CAPABILITY_AUTO_EXPAND")?
        .capability("CAPABILITY_NAMED_IAM")?
        .parameter("StorageBucket", &bucket.name())
        .deploy()
        .await?;
    println!("`prepare` module deployed.");
    Ok(())
}
