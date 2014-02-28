package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.CreateIndexChange;
import liquibase.change.core.CreateTableChange;
import liquibase.diff.DiffResult;
import liquibase.exception.DatabaseException;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.Index;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;

public class CreateIndexChangeSupplier extends AbstractChangeSupplier<CreateIndexChange>  {
    @Override
    public Change[]  prepareDatabase(CreateIndexChange change) throws DatabaseException {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName(change.getTableName());
        for (ColumnConfig column : change.getColumns()) {
            createTableChange.addColumn(column);
        }

        return new Change[] {createTableChange };
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, CreateIndexChange change) {
        Index example = new Index(change.getIndexName(), change.getCatalogName(), change.getSchemaName(), change.getTableName());

        List<String> columns = null;
        if (change.getColumns() != null) {
            columns = new ArrayList<String>();
            for (ColumnConfig col : change.getColumns()) {
                columns.add(col.getName());
            }
            example.setColumns(StringUtils.join(columns, ","));
        }

        assertNotNull(diffResult.getUnexpectedObject(example));
    }
}
