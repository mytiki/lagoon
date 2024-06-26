use std::error::Error;

use super::{
    execute::STACK_PREFIX,
    super::super::utils::{CfDeploy, StsAccount},
};

pub async fn execute(account: &StsAccount, dist: &str) -> Result<(), Box<dyn Error>> {
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
