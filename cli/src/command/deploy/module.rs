use clap::ValueEnum;

#[derive(Copy, Clone, PartialEq, Eq, PartialOrd, Ord, Debug, ValueEnum)]
pub enum Module {
    /// Deploy the `log` module. Responsible for consolidating logs in Iceberg tables.
    Log,
    /// Deploy the `prepare` module. Responsible for transforming and preparing raw data for loading into Iceberg tables.
    Prepare,
    /// Deploy the `pipeline` module. Responsible for orchestrating data preparation workflows.
    Pipeline,
    /// Deploy the `write` module. Responsible for loading data into Iceberg tables.
    Write,
}
