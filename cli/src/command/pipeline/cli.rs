use clap::Parser;

#[derive(Parser, Debug)]
pub struct Cli {
    /// Path to the folder containing the dagster+dbt project to deploy.
    #[arg(short, long)]
    project: String,
}

impl Cli {
    pub fn project(&self) -> &str {
        &self.project
    }
}
