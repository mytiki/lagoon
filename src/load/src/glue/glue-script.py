import sys
from typing import Any, List, Tuple

import boto3
from awsglue.context import GlueContext, DynamicFrame
from awsglue.job import Job
from awsglue.utils import getResolvedOptions
from botocore.exceptions import ClientError
from pyspark.context import SparkContext
from pyspark.sql import SparkSession, DataFrame
from pyspark.sql.functions import current_timestamp, col, hours


def process_key(key: str):
    if key.startswith("/"):
        key = key[1:]
    parts: List[str] = key.split("/")
    parts.pop(0)
    return parts[0], parts[1], key.split(".")[1]


def create_database(client, bucket: str, name: str):
    try:
        client.get_database(Name=name)
    except ClientError as e:
        if e.response['Error']['Code'] == 'EntityNotFoundException':
            print(f"Create database: '{name}'")
            client.create_database(DatabaseInput={'Name': name, 'LocationUri': f"s3://{bucket}/stg/{name}.db"})
        else:
            raise


def table_exists(client, database_name, table_name):
    try:
        client.get_table(DatabaseName=database_name, Name=table_name)
        return True
    except ClientError as e:
        return False


def append_table(df: DataFrame, table: str, database: str):
    print(f"Append table: {database}.{table}")
    df.writeTo(f"glue_catalog.{database}.{table}") \
        .tableProperty("format-version", "2") \
        .option("mergeSchema", "true") \
        .append()


def create_table(df: DataFrame, table: str, database: str):
    try:
        print(f"Create table: {database}.{table}")
        df.writeTo(f"glue_catalog.{database}.{table}") \
            .partitionedBy(hours(col(ETL_COL))) \
            .tableProperty("format-version", "2") \
            .tableProperty("write.spark.accept-any-schema", "true") \
            .create()
    except Exception as e:
        print(f"Failed to create table. Trying append: {e}")
        append_table(df, table, database)


ETL_COL: str = "_etl_loaded_at"
args: dict[str, Any] = getResolvedOptions(sys.argv, ["JOB_NAME", "S3_BUCKET", "S3_KEY"])
glue_context: GlueContext = GlueContext(SparkContext())
job: Job = Job(glue_context)
glue_client = boto3.client('glue')

job.init(args["JOB_NAME"], args)
spark: SparkSession = glue_context.spark_session.builder \
    .appName("iceberg") \
    .config("spark.sql.defaultCatalog", "glue_catalog") \
    .config("spark.sql.extensions", "org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions") \
    .config("spark.sql.catalog.glue_catalog", "org.apache.iceberg.spark.SparkCatalog") \
    .config("spark.sql.catalog.glue_catalog.warehouse", f"s3://{args['S3_BUCKET']}/stg") \
    .config("spark.sql.catalog.glue_catalog.catalog-impl", "org.apache.iceberg.aws.glue.GlueCatalog") \
    .config("spark.sql.catalog.glue_catalog.io-impl", "org.apache.iceberg.aws.s3.S3FileIO") \
    .getOrCreate()

key_tuple: Tuple[str, str, str] = process_key(args["S3_KEY"])
database: str = key_tuple[0]
table: str = key_tuple[1]
file_type: str = key_tuple[2]

create_database(glue_client, args["S3_BUCKET"], database)

inputFrame: DynamicFrame = glue_context.create_dynamic_frame.from_options(
    format_options={"quoteChar": "\"", "withHeader": True, "separator": ",", "optimizePerformance": True},
    connection_type="s3", format=file_type,
    connection_options={"paths": [f"s3://{args['S3_BUCKET']}/{args['S3_KEY']}"], "recurse": True},
    transformation_ctx="inputFrame")
df: DataFrame = inputFrame.toDF().withColumn(ETL_COL, current_timestamp())

if table_exists(glue_client, database, table):
    append_table(df, table, database)
else:
    create_table(df, table, database)

print(f"Complete: {database}.{table}")
job.commit()
