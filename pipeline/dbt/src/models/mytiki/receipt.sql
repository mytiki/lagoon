{{
    config(
        table_type='iceberg',
        materialized='table',
        incremental_strategy='append',
        s3_data_naming='schema_table_unique',
        on_schema_change='append_new_columns',
        partition_by='day(receipt_date)',
        post_hook = [
            'VACUUM {{ this }};'
        ]
    )
}}

SELECT *
FROM {{ source('mytiki', 'test') }}
