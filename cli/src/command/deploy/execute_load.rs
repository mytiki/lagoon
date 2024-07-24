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
    deploy_assets(bucket, dist).await?;
    deploy_template(account, dist, bucket).await?;
    Ok(())
}

async fn deploy_assets(bucket: &S3Bucket, dist: &str) -> Result<(), Box<dyn Error>> {
    log::info!("Creating `load` assets...");
    let dir = format!("assets/deploy/load/{}", env!("CARGO_PKG_VERSION"));
    bucket
        .upload_dir(&dir, &format!("{}/{}", dist, dir))
        .await?;
    log::info!("`load` assets created.");
    Ok(())
}

async fn deploy_template(
    account: &StsAccount,
    dist: &str,
    bucket: &S3Bucket,
) -> Result<(), Box<dyn Error>> {
    log::info!("Deploying `load` module...");
    let stack = format!("{}-load", STACK_PREFIX);
    CfDeploy::connect(
        account.profile(),
        &stack,
        &format!("{}/templates/load.yml", dist),
    )
    .await?
    .capability("CAPABILITY_AUTO_EXPAND")?
    .capability("CAPABILITY_NAMED_IAM")?
    .parameter("StorageBucket", &bucket.name())
    .deploy()
    .await?;
    log::info!("`load` module deployed.");
    Ok(())
}
