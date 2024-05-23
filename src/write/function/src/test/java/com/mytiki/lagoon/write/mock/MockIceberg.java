/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.lagoon.write.mock;

import com.mytiki.lagoon.write.utils.Iceberg;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Table;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.catalog.Namespace;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class MockIceberg {
    private final Iceberg iceberg;
    private final String name;

    public MockIceberg() {
        try (InputStream input = Iceberg.class.getClassLoader().getResourceAsStream("iceberg.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            name = prop.getProperty("database-name");
            iceberg = Mockito.mock(Iceberg.class);

            Table table = Mockito.mock(Table.class);
            Transaction transaction = Mockito.mock(Transaction.class);
            AppendFiles appendFiles = Mockito.mock(AppendFiles.class);

            PartitionSpec spec = Mockito.mock(PartitionSpec.class);
            Mockito.lenient().doNothing().when(iceberg).close();
            Mockito.lenient().doReturn(List.of()).when(spec).fields();
            Mockito.lenient().doReturn(spec).when(table).spec();
            Mockito.lenient().doReturn(table).when(iceberg).loadTable(Mockito.any());
            Mockito.lenient().doReturn(transaction).when(table).newTransaction();
            Mockito.lenient().doReturn(appendFiles).when(transaction).newAppend();
            Mockito.lenient().doNothing().when(transaction).commitTransaction();
            Mockito.lenient().doNothing().when(appendFiles).commit();
            Mockito.lenient().doReturn(appendFiles).when(appendFiles).appendFile(Mockito.any());
            Mockito.lenient().doReturn(Namespace.of(name)).when(iceberg).getDatabase();
        }catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Iceberg iceberg() {
        return iceberg;
    }

    public String name() {
        return name;
    }
}
