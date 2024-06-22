use clap::Subcommand;

pub mod initialize;

#[derive(Subcommand, Debug)]
pub enum Command {
    Initialize(initialize::Cli),
}
