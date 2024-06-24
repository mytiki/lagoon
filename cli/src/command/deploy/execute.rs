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
        execute_pipeline(profile, dist, bucket.name()).await?;
        execute_write(profile, dist, &bucket).await?;
    } else {
        match cli.module().unwrap() {
            Module::Log => execute_log(profile, dist).await?,
            Module::Prepare => execute_prepare(profile, dist, &bucket).await?,
            Module::Pipeline => execute_pipeline(profile, dist, bucket.name()).await?,
            Module::Write => execute_write(profile, dist, &bucket).await?,
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

async fn execute_pipeline(
    profile: &str,
    dist: &str,
    bucket_name: &str,
) -> Result<(), Box<dyn Error>> {
    let stack = format!("{}-pipeline", STACK_PREFIX);
    CfDeploy::connect(profile, &stack, &format!("{}/templates/pipeline.yml", dist))
        .await?
        .capability("CAPABILITY_AUTO_EXPAND")?
        .parameter("StorageBucket", bucket_name)
        .deploy()
        .await?;
    println!("`pipeline` module deployed.");
    Ok(())
}

async fn execute_write(profile: &str, dist: &str, bucket: &S3Bucket) -> Result<(), Box<dyn Error>> {
    bucket
        .upload_dir("assets/deploy/write", "../dist/assets/deploy/write")
        .await?;
    let stack = format!("{}-write", STACK_PREFIX);
    CfDeploy::connect(profile, &stack, &format!("{}/templates/write.yml", dist))
        .await?
        .capability("CAPABILITY_AUTO_EXPAND")?
        .capability("CAPABILITY_NAMED_IAM")?
        .parameter("StorageBucket", bucket.name())
        .deploy()
        .await?;
    println!("`write` module deployed.");
    Ok(())
}
