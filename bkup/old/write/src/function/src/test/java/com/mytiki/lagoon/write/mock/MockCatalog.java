/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.lagoon.write.mock;

import com.mytiki.lagoon.write.Catalog;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Table;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.catalog.Namespace;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

public class MockCatalog {
    private final Catalog catalog;

    public MockCatalog() {
        catalog = Mockito.mock(Catalog.class);

        Table table = Mockito.mock(Table.class);
        Transaction transaction = Mockito.mock(Transaction.class);
        AppendFiles appendFiles = Mockito.mock(AppendFiles.class);

        PartitionSpec spec = Mockito.mock(PartitionSpec.class);
        Mockito.lenient().doNothing().when(catalog).close();
        Mockito.lenient().doReturn(List.of()).when(spec).fields();
        Mockito.lenient().doReturn(spec).when(table).spec();
        Mockito.lenient().doReturn(table).when(catalog).loadTable(Mockito.any());
        Mockito.lenient().doReturn(table).when(catalog).createTable(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.lenient().doReturn(transaction).when(table).newTransaction();
        Mockito.lenient().doReturn(appendFiles).when(transaction).newAppend();
        Mockito.lenient().doNothing().when(transaction).commitTransaction();
        Mockito.lenient().doNothing().when(appendFiles).commit();
        Mockito.lenient().doReturn(appendFiles).when(appendFiles).appendFile(Mockito.any());

        Mockito.lenient().doReturn(catalog).when(catalog).initialize();
        Mockito.lenient().doReturn(Namespace.of("dummydb")).when(catalog).getDatabase(Mockito.any());
        Mockito.lenient().doReturn(Namespace.of("dummydb")).when(catalog).getDatabaseSafe(Mockito.any());

        try (MockedStatic<Catalog> dfltMock = Mockito.mockStatic(Catalog.class)) {
            dfltMock.when(Catalog::load).thenReturn(catalog);
        }
    }

    public Catalog iceberg() {
        return catalog;
    }
}
