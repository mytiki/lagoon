use std::{error::Error, fs};

use crate::utils::KmsKey;

use super::{
    Cli,
    super::super::utils::{resource_name, S3Bucket, StsAccount, zip_dir},
};

pub async fn execute(profile: &str, cli: &Cli) -> Result<(), Box<dyn Error>> {
    log::info!("Preparing pipeline deployment...");
    let account = StsAccount::connect(profile).await?;
    let name = resource_name::from_account(&account);
    let key = KmsKey::connect(&account, &name).await?.get_key().await?;
    let bucket = S3Bucket::connect(profile, &name, &key).await?;
    zip_dir::new(cli.project(), "dbt.zip", zip::CompressionMethod::Stored)?;
    bucket
        .upload_file("assets/deploy/pipeline/dbt.zip", "dbt.zip")
        .await?;
    fs::remove_file("dbt.zip")?;
    log::info!("Pipeline deployment submitted. Visit your dashboard for deployment status.");
    Ok(())
}
