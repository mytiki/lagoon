use std::str;

use clap::Subcommand;

pub mod deploy;
pub mod initialize;
pub mod pipeline;

#[derive(Subcommand, Debug)]
pub enum Command {
    Initialize(initialize::Cli),
    Deploy(deploy::Cli),
    Pipeline(pipeline::Cli),
}
