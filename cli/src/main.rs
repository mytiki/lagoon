use std::error::Error;

use clap::Parser;

use cli::Cli;
use command::{Command, deploy, initialize, pipeline};

mod cli;
mod command;
mod utils;

#[tokio::main]
async fn main() -> Result<(), Box<dyn Error>> {
    env_logger::init();
    let cli = Cli::parse();
    let profile = cli.profile();
    match &cli.command() {
        Command::Initialize(cli) => initialize::execute(profile, cli).await?,
        Command::Deploy(cli) => deploy::execute(profile, cli).await?,
        Command::Pipeline(cli) => pipeline::execute(profile, cli).await?,
    }
    Ok(())
}
