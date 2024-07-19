use clap::Parser;

use crate::command::Command;

#[derive(Parser, Debug)]
#[command(name = "MyApp")]
#[command(version, about, long_about = None)]
pub struct Cli {
    #[command(subcommand)]
    command: Command,

    /// The name of the [AWS CLI profile](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-authentication.html) to use.
    #[arg(long, default_value = "default")]
    profile: String,
}

impl Cli {
    pub fn command(&self) -> &Command {
        &self.command
    }
    pub fn profile(&self) -> &str {
        &self.profile
    }
}
