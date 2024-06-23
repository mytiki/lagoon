use clap::Parser;

use super::module::Module;

#[derive(Parser, Debug)]
pub struct Cli {
    /// Optional. Deploy a submodule of the Lagoon. If not provided, the entire Lagoon will be deployed.
    #[arg(short, long, value_enum)]
    module: Option<Module>,

    /// Path to the dist folder containing the submodule deployment templates.
    #[arg(short, long, default_value = "../dist")]
    dist: String,
}

impl Cli {
    pub fn module(&self) -> Option<Module> {
        self.module
    }
    pub fn dist(&self) -> &str {
        &self.dist
    }
}
