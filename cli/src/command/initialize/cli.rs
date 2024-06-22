use clap::Parser;

#[derive(Parser, Debug)]
pub struct Cli {
    /// The name of the [AWS CLI profile](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-authentication.html) to use.
    #[arg(short, long, default_value = "default")]
    profile: String,
}

impl Cli {
    pub fn profile(&self) -> &str {
        &self.profile
    }
}