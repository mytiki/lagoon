use std::error::Error;

use clap::Parser;

use command::{Command, initialize};

mod command;
mod utils;

#[derive(Parser, Debug)]
#[command(name = "MyApp")]
#[command(version, about, long_about = None)]
pub struct Cli {
    #[command(subcommand)]
    pub command: Command,
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn Error>> {
    let cli = Cli::parse();

    match &cli.command {
        Command::Initialize(cli) => {
            initialize::execute(cli).await?;
        }
    }

    Ok(())
}
