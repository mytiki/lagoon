use std::error::Error;

use super::{
    execute::STACK_PREFIX,
    super::super::utils::{CfDeploy, S3Bucket, StsAccount},
};

pub async fn execute(
    account: &StsAccount,
    dist: &str,
    bucket: &S3Bucket,
) -> Result<(), Box<dyn Error>> {
    deploy_assets(bucket).await?;
    deploy_template(account, dist, bucket).await?;
    Ok(())
}

async fn deploy_assets(bucket: &S3Bucket) -> Result<(), Box<dyn Error>> {
    log::info!("Creating `prepare` assets...");
    bucket
        .upload_dir("assets/deploy/prepare", "../dist/assets/deploy/prepare")
        .await?;
    log::info!("`prepare` assets created.");
    Ok(())
}

async fn deploy_template(
    account: &StsAccount,
    dist: &str,
    bucket: &S3Bucket,
) -> Result<(), Box<dyn Error>> {
    log::info!("Deploying `prepare` module...");
    let stack = format!("{}-prepare", STACK_PREFIX);
    CfDeploy::connect(
        account.profile(),
        &stack,
        &format!("{}/templates/prepare.yml", dist),
    )
    .await?
    .capability("CAPABILITY_AUTO_EXPAND")?
    .capability("CAPABILITY_NAMED_IAM")?
    .parameter("StorageBucket", &bucket.name())
    .deploy()
    .await?;
    log::info!("`prepare` module deployed.");
    Ok(())
}
