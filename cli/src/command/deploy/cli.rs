use clap::Parser;

use super::module::Module;

#[derive(Parser, Debug)]
pub struct Cli {
    /// Optional. Deploy a submodule of the Lagoon. If not provided, the entire Lagoon will be deployed.
    #[arg(short, long, value_enum)]
    module: Option<Module>,

    /// Path to the dist folder containing the submodule deployment templates.
    #[arg(short, long, default_value = ".")]
    dist: String,

    /// The ARN of the [AWS SSL certificate](https://aws.amazon.com/certificate-manager/) to
    /// use for the Dagster endpoint. Required by the `pipeline` module.
    #[arg(short, long)]
    ssl: String,

    /// A password to use for communication between resources. Required by the `pipeline` module.
    /// Does not need to be memorable; you should randomly generate a strong password.
    #[arg(short, long)]
    password: String,
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
    pub fn password(&self) -> &str {
        &self.password
    }
}
