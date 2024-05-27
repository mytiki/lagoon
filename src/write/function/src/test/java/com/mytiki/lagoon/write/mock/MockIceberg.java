/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.lagoon.write.mock;

import com.mytiki.lagoon.write.utils.IcebergFacade;
import com.mytiki.lagoon.write.utils.StorageFacade;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Table;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.catalog.Namespace;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class MockIceberg {
    private final IcebergFacade iceberg;

    public MockIceberg() {
        iceberg = Mockito.mock(IcebergFacade.class);

        Table table = Mockito.mock(Table.class);
        Transaction transaction = Mockito.mock(Transaction.class);
        AppendFiles appendFiles = Mockito.mock(AppendFiles.class);

        PartitionSpec spec = Mockito.mock(PartitionSpec.class);
        Mockito.lenient().doNothing().when(iceberg).close();
        Mockito.lenient().doReturn(List.of()).when(spec).fields();
        Mockito.lenient().doReturn(spec).when(table).spec();
        Mockito.lenient().doReturn(table).when(iceberg).loadTable(Mockito.any());
        Mockito.lenient().doReturn(table).when(iceberg).createTable(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.lenient().doReturn(transaction).when(table).newTransaction();
        Mockito.lenient().doReturn(appendFiles).when(transaction).newAppend();
        Mockito.lenient().doNothing().when(transaction).commitTransaction();
        Mockito.lenient().doNothing().when(appendFiles).commit();
        Mockito.lenient().doReturn(appendFiles).when(appendFiles).appendFile(Mockito.any());

        Mockito.lenient().doReturn(iceberg).when(iceberg).initialize();
        Mockito.lenient().doReturn(Namespace.of("dummydb")).when(iceberg).getDatabase(Mockito.any());
        Mockito.lenient().doReturn(Namespace.of("dummydb")).when(iceberg).getDatabaseSafe(Mockito.any());

        try(MockedStatic<IcebergFacade> dfltMock = Mockito.mockStatic(IcebergFacade.class)) {
            dfltMock.when(IcebergFacade::load).thenReturn(iceberg);
        }
    }

    public IcebergFacade iceberg() {
        return iceberg;
    }
}
