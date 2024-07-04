use std::error::Error;

use super::{
    Cli,
    execute::STACK_PREFIX,
    super::super::utils::{CfDeploy, Docker, EcrRepository, StsAccount},
};

pub async fn execute(account: &StsAccount, cli: &Cli, name: &str) -> Result<(), Box<dyn Error>> {
    deploy_images(account, name).await?;
    deploy_template(account, cli, name).await?;
    Ok(())
}

async fn deploy_images(account: &StsAccount, name: &str) -> Result<(), Box<dyn Error>> {
    log::info!("Building `pipeline` images...");
    let ecr = EcrRepository::connect(account.profile(), name).await?;

    let image_tag = format!("dagster-{}", "1.7.12");
    if !ecr.has_image(&image_tag).await? {
        Docker::new(account.account_id(), &account.region().to_string(), name)
            .with_context("../dist/assets/deploy/pipeline/dagster")
            .with_tag(&image_tag)
            .with_target("webserver")
            .with_platform("linux/x86_64")
            .auth(account.profile())?
            .build()?
            .push()?;
    }

    let image_tag = format!("daemon-{}", "1.7.12");
    if !ecr.has_image(&image_tag).await? {
        Docker::new(account.account_id(), &account.region().to_string(), name)
            .with_context("../dist/assets/deploy/pipeline/dagster")
            .with_tag(&image_tag)
            .with_target("daemon")
            .with_platform("linux/x86_64")
            .auth(account.profile())?
            .build()?
            .push()?;
    }

    if !ecr.has_image("dbt-example").await? {
        Docker::new(account.account_id(), &account.region().to_string(), name)
            .with_context("../dist/assets/deploy/pipeline/dbt")
            .with_tag("dbt-example")
            .with_platform("linux/x86_64")
            .auth(account.profile())?
            .build()?
            .push()?;
    }

    log::info!("`pipeline` images created.");
    Ok(())
}

async fn deploy_template(
    account: &StsAccount,
    cli: &Cli,
    name: &str,
) -> Result<(), Box<dyn Error>> {
    log::info!("Deploying `pipeline` module...");
    let stack = format!("{}-pipeline", STACK_PREFIX);
    CfDeploy::connect(
        account.profile(),
        &stack,
        &format!("{}/templates/pipeline.yml", cli.dist()),
    )
    .await?
    .capability("CAPABILITY_AUTO_EXPAND")?
    .capability("CAPABILITY_NAMED_IAM")?
    .parameter("StorageBucket", name)
    .parameter("SSLCertificate", cli.ssl())
    .parameter("Password", cli.password())
    .deploy()
    .await?;
    log::info!("`pipeline` module deployed.");
    Ok(())
}
