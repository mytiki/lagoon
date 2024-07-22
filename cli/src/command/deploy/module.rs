use clap::ValueEnum;

#[derive(Copy, Clone, PartialEq, Eq, PartialOrd, Ord, Debug, ValueEnum)]
pub enum Module {
    /// Deploy the `load` module. Responsible for transforming and loading raw data into Iceberg tables.
    Load,
    /// Deploy the `pipeline` module. Responsible for orchestrating data preparation workflows.
    Pipeline,
}
