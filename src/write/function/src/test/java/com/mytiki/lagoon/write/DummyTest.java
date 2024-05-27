package com.mytiki.lagoon.write;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.Schema;
import org.apache.iceberg.avro.AvroSchemaUtil;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.avro.AvroSchemaConverter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.iceberg.util.SerializableSupplier;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.io.LocalOutputFile;
import org.apache.parquet.schema.MessageType;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;

public class DummyTest {
//    @Test
//    public void CreateTestAvro() throws IOException {
//        String avroSchema = "{\"namespace\": \"example.avro\",\n" +
//                " \"type\": \"record\",\n" +
//                " \"name\": \"User\",\n" +
//                " \"fields\": [\n" +
//                "     {\"name\": \"name\", \"type\": \"string\"},\n" +
//                "     {\"name\": \"favorite_number\",  \"type\": [\"int\", \"null\"]},\n" +
//                "     {\"name\": \"favorite_color\", \"type\": [\"string\", \"null\"]}\n" +
//                " ]\n" +
//                "}";
//        Schema schema = new Schema.Parser().parse(avroSchema);
//        GenericRecord user = new GenericData.Record(schema);
//        user.put("name", "Alyssa");
//        user.put("favorite_number", 256);
//
//        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>();
//        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
//        dataFileWriter.create(schema, new File("users.avro"));
//        dataFileWriter.append(user);
//        dataFileWriter.close();
//    }
//
//    @Test
//    public void ReadTestAvroToIceberg() throws IOException {
//        File file = new File("users.avro");
//
//        //DatumReader datumReader = new GenericAvroReader<>();
//
//        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>();
//        DataFileReader<GenericRecord> dataFileReader = new DataFileReader<GenericRecord>(file, datumReader);
//
//        Schema schema = dataFileReader.getSchema();
//        System.out.println(schema);
//
//        org.apache.iceberg.Schema iceberg = AvroSchemaUtil.toIceberg(schema);
//        System.out.println(iceberg);
//
//        GenericRecord user = null;
//        while (dataFileReader.hasNext()) {
//            user = dataFileReader.next(user);
//            System.out.println(user);
//        }
//    }

    @Test
    public void createParquet() throws IOException {
        File file = new File("users.avro");
        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>();
        DataFileReader<GenericRecord> dataFileReader = new DataFileReader<GenericRecord>(file, datumReader);

        GenericRecord user = new GenericData.Record(dataFileReader.getSchema());
        user.put("name", "Alyssa");
        user.put("favorite_number", 256);

        LocalOutputFile outputPath = new LocalOutputFile(Path.of("users.parquet"));
        ParquetWriter<GenericRecord> writer = AvroParquetWriter
                .<GenericRecord>builder(outputPath)
                .withSchema(dataFileReader.getSchema())
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .build();

        writer.write(user);
        writer.close();
    }

    @Test
    public void move() throws IOException {
//        File file = new File("users.avro");
//        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>();
//        DataFileReader<GenericRecord> dataFileReader = new DataFileReader<GenericRecord>(file, datumReader);
//
//        Schema schema = AvroSchemaUtil.toIceberg(dataFileReader.getSchema());
//        PartitionSpec spec = PartitionSpec.builderFor(schema).build();



        DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.builder().build();
        S3Client s3Client = S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(DefaultAwsRegionProviderChain.builder().build().getRegion())
                .build();

        SerializableSupplier<S3Client> s3ClientSupplier = (SerializableSupplier<S3Client> & Serializable) () -> s3Client;

        Configuration conf = new Configuration();
        AwsSessionCredentials creds = (AwsSessionCredentials) credentialsProvider.resolveCredentials();
        conf.set("fs.s3a.access.key", creds.accessKeyId());
        conf.set("fs.s3a.secret.key", creds.secretAccessKey());
        conf.set("fs.s3a.session.token", creds.sessionToken());
        conf.set("fs.s3a.endpoint", "s3.amazonaws.com");

        org.apache.hadoop.fs.Path s3Path = new org.apache.hadoop.fs.Path("s3a://mytiki-lagoon-590184107666-us-east-2/load/example/users.parquet");
        HadoopInputFile inputFile = HadoopInputFile.fromPath(s3Path, conf);

        ParquetFileReader reader = ParquetFileReader.open(inputFile);
        ParquetMetadata footer = reader.getFooter();
        MessageType parquetSchema = footer.getFileMetaData().getSchema();

        AvroSchemaConverter converter = new AvroSchemaConverter();
        Schema schema = AvroSchemaUtil.toIceberg(converter.convert(parquetSchema));

        System.out.println(schema);



//      normally you get the table schema if it exists, but we want to create a new table so we need to load a record.

//        File file = new File(inputFile.location());
//        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>();
//        DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(file, datumReader);
//        Schema schema = AvroSchemaUtil.toIceberg(dataFileReader.getSchema());
//        PartitionSpec spec = PartitionSpec.builderFor(schema).build();

//        DataFile src = DataFiles.builder(spec)
//                .withInputFile(inputFile)
//                .build();
//        DataFile dest = DataFiles.builder(spec)
//                .copy(src)
//                .withPath("s3://mytiki-lagoon-590184107666-us-east-2/stg/users.avro")
//                .build();
    }
}
