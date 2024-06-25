use std::error::Error;

use super::{
    Cli,
    module::Module,
    super::super::utils::{CfDeploy, Docker, resource_name, S3Bucket, StsAccount},
};

const STACK_PREFIX: &str = "mytiki-lagoon";

pub async fn execute(profile: &str, cli: &Cli) -> Result<(), Box<dyn Error>> {
    log::info!("Deploying mytiki-lagoon modules...");
    let dist = cli.dist();
    let account = StsAccount::connect(profile).await?;
    let name = resource_name::from_account(&account);
    let bucket = S3Bucket::connect(profile, &name).await?;

    if cli.module().is_none() {
        execute_log(&account, dist).await?;
        execute_prepare(&account, dist, &bucket).await?;
        execute_pipeline(&account, cli, bucket.name()).await?;
        execute_write(&account, dist, &bucket).await?;
    } else {
        match cli.module().unwrap() {
            Module::Log => execute_log(&account, dist).await?,
            Module::Prepare => execute_prepare(&account, dist, &bucket).await?,
            Module::Pipeline => execute_pipeline(&account, cli, bucket.name()).await?,
            Module::Write => execute_write(&account, dist, &bucket).await?,
        }
    }
    log::info!("Deployment complete.");
    Ok(())
}

async fn execute_log(account: &StsAccount, dist: &str) -> Result<(), Box<dyn Error>> {
    log::info!("Deploying `log` module...");
    let stack = format!("{}-log", STACK_PREFIX);
    CfDeploy::connect(
        account.profile(),
        &stack,
        &format!("{}/templates/log.yml", dist),
    )
    .await?
    .capability("CAPABILITY_AUTO_EXPAND")?
    .deploy()
    .await?;
    log::info!("`log` module deployed.");
    Ok(())
}

async fn execute_prepare(
    account: &StsAccount,
    dist: &str,
    bucket: &S3Bucket,
) -> Result<(), Box<dyn Error>> {
    log::info!("Deploying `prepare` module...");
    bucket
        .upload_dir("assets/deploy/prepare", "../dist/assets/deploy/prepare")
        .await?;
    log::info!("`prepare` assets created.");
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

async fn execute_pipeline(
    account: &StsAccount,
    cli: &Cli,
    bucket_name: &str,
) -> Result<(), Box<dyn Error>> {
    log::info!("Deploying `pipeline` module...");

    Docker::new(account.account_id(), &account.region().to_string())
        .with_context("../dist/assets/deploy/pipeline/dagster")
        .with_tag("dagster-latest")
        .with_arg("DAGSTER_PG_PASSWORD", "IDFK")
        .with_arg("DAGSTER_PG_HOST", "IDFK")
        .with_arg("DAGSTER_CURRENT_IMAGE", "IDFK")
        .with_arg("DAGSTER_GRPC_SERVER_HOST", "IDFK")
        .auth()?
        .build()?;

    //TODO we need to deploy resources in 2 steps (db + nlb), then we use the cf api to get the export values to put in the containers
    //also all of these funcs should be split into their own files
    //we need to build the dbt image as well (the DAGSTER_GRPC_SERVER_HOST env var may not work)

    //.push()?;

    // let stack = format!("{}-pipeline", STACK_PREFIX);
    // CfDeploy::connect(
    //     account.profile(),
    //     &stack,
    //     &format!("{}/templates/pipeline.yml", cli.dist()),
    // )
    // .await?
    // .capability("CAPABILITY_AUTO_EXPAND")?
    // .parameter("StorageBucket", bucket_name)
    // .parameter("SSLCertificate", cli.ssl())
    // .parameter("Password", "IDFK")
    // .deploy()
    // .await?;
    log::info!("`pipeline` module deployed.");
    Ok(())
}

async fn execute_write(
    account: &StsAccount,
    dist: &str,
    bucket: &S3Bucket,
) -> Result<(), Box<dyn Error>> {
    log::info!("Deploying `write` module...");
    bucket
        .upload_dir("assets/deploy/write", "../dist/assets/deploy/write")
        .await?;
    log::info!("`write` assets created.");
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
