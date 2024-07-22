import sys

import boto3
from awsglue.context import GlueContext
from awsglue.dynamicframe import DynamicFrame
from awsglue.job import Job
from awsglue.transforms import *
from awsglue.utils import getResolvedOptions
from pyspark.context import SparkContext


def get_partition(bucket, key):
    s3 = boto3.client('s3')
    response = s3.head_object(Bucket=bucket, Key=key)
    file_size = response['ContentLength'] / 1048576
    return max(1, int(file_size / 128))


def process_key(key):
    if key.startswith('/'):
        key = key[1:]
    parts = key.split('/')
    parts.pop(0)
    db_table = '/'.join(parts[:-1])
    parts = key.split('.')
    return db_table, parts[1]


args = getResolvedOptions(sys.argv, ['JOB_NAME', 'S3_BUCKET', 'S3_KEY'])
sc = SparkContext()
glueContext = GlueContext(sc)
spark = glueContext.spark_session
job = Job(glueContext)
job.init(args['JOB_NAME'], args)

db_table, file_type = process_key(args['S3_KEY'])

inputFrame = glueContext.create_dynamic_frame.from_options(
    format_options={"quoteChar": "\"", "withHeader": True, "separator": ",", "optimizePerformance": True},
    connection_type="s3", format=file_type,
    connection_options={"paths": [f"s3://{args['S3_BUCKET']}/{args['S3_KEY']}"], "recurse": True},
    transformation_ctx="inputFrame")

num_partitions = get_partition(args['S3_BUCKET'], args['S3_KEY'])
part_df = inputFrame.toDF().repartition(num_partitions)
part_frame = DynamicFrame.fromDF(part_df, glueContext, "part_frame")

outputFrame = glueContext.write_dynamic_frame.from_options(
    frame=part_frame,
    connection_type="s3", format="glueparquet",
    connection_options={"path": f"s3://{args['S3_BUCKET']}/load/{db_table}", "partitionKeys": []},
    format_options={
        "compression": "uncompressed",
        "useGlueParquetWriter": True,
        "blockSize": 134217728,
        "pageSize": 1048576
    },
    transformation_ctx="outputFrame")

job.commit()
