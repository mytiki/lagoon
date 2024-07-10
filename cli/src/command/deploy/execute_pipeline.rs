use std::error::Error;

use rand::{distributions::Alphanumeric, Rng};

use super::{
    Cli,
    execute::STACK_PREFIX,
    super::super::utils::{CfDeploy, EcrRepository, StsAccount},
};

pub async fn execute(account: &StsAccount, cli: &Cli, name: &str) -> Result<(), Box<dyn Error>> {
    deploy_template(account, cli, name).await?;
    Ok(())
}

async fn deploy_template(
    account: &StsAccount,
    cli: &Cli,
    name: &str,
) -> Result<(), Box<dyn Error>> {
    log::info!("Deploying `pipeline` module...");
    let stack = format!("{}-pipeline", STACK_PREFIX);
    let password = cli.password().clone().unwrap_or(random_password());
    let dbt = match cli.dbt().clone() {
        Some(dbt) => dbt,
        None => {
            let ecr = EcrRepository::connect(account.profile(), name).await?;
            let version = env!("CARGO_PKG_VERSION");
            let tag = ecr.get_image("dbt-").await;
            match tag {
                Ok(tag) => format!(
                    "{}.dkr.ecr.{}.amazonaws.com/{}:{}",
                    account.account_id(),
                    account.region(),
                    name,
                    tag
                ),
                Err(_) => format!(
                    "public.ecr.aws/mytiki/mytiki-lagoon:dbt-example-{}",
                    version
                ),
            }
        }
    };
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
    .parameter("Password", &password)
    .parameter("Dbt", &dbt)
    .deploy()
    .await?;
    log::info!("`pipeline` module deployed.");
    Ok(())
}

fn random_password() -> String {
    let password = rand::thread_rng()
        .sample_iter(&Alphanumeric)
        .take(24)
        .map(char::from)
        .collect();
    log::info!("Generated password: {}", password);
    password
}
