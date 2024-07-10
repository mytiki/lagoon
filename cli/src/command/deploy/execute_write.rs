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
    log::info!("Creating `write` assets...");
    let dir = format!("assets/deploy/write/{}", env!("CARGO_PKG_VERSION"));
    bucket
        .upload_dir(&dir, &format!("{}/{}", dist, dir))
        .await?;
    log::info!("`write` assets created.");
    Ok(())
}

async fn deploy_template(
    account: &StsAccount,
    dist: &str,
    bucket: &S3Bucket,
) -> Result<(), Box<dyn Error>> {
    log::info!("Deploying `write` module...");
    let stack = format!("{}-write", STACK_PREFIX);
    CfDeploy::connect(
        account.profile(),
        &stack,
        &format!("{}/templates/write.yml", dist),
    )
    .await?
    .capability("CAPABILITY_AUTO_EXPAND")?
    .capability("CAPABILITY_NAMED_IAM")?
    .parameter("StorageBucket", bucket.name())
    .deploy()
    .await?;
    log::info!("`write` module deployed.");
    Ok(())
}
