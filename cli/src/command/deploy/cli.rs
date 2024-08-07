use clap::Parser;

use super::module::Module;

#[derive(Parser, Debug)]
pub struct Cli {
    /// Optional. Deploy a submodule of the Lagoon. If not provided, the entire Lagoon will be deployed.
    #[arg(long, value_enum)]
    module: Option<Module>,

    /// Path to the dist folder containing the submodule deployment templates.
    #[arg(long, default_value = ".")]
    dist: String,

    /// The ARN of the [AWS SSL certificate](https://aws.amazon.com/certificate-manager/) to
    /// use for the Dagster endpoint. Required by the `pipeline` module.
    #[arg(long)]
    ssl: String,

    /// A password to use for communication between resources. Auto-generated on each run
    /// if not provided.
    #[arg(long)]
    password: Option<String>,

    /// The full URI:tag for the docker image hosted in your Lagoon ECR repository.
    /// If not provided, the most recent image will be used. If not found, a default example
    /// image will be used.
    #[arg(long)]
    dbt: Option<String>,
}

impl Cli {
    pub fn module(&self) -> Option<Module> {
        self.module
    }
    pub fn dist(&self) -> &str {
        &self.dist
    }
    pub fn ssl(&self) -> &str {
        &self.ssl
    }
    pub fn password(&self) -> &Option<String> {
        &self.password
    }
    pub fn dbt(&self) -> &Option<String> {
        &self.dbt
    }
}
