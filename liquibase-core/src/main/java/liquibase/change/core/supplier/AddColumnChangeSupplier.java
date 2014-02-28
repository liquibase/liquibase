package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.CreateTableChange;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

import java.util.SortedSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AddColumnChangeSupplier extends AbstractChangeSupplier<AddColumnChange>  {
    @Override
    public Change[] prepareDatabase(AddColumnChange change) throws Exception {
        CreateTableChange createTableChange = new CreateTableChange();
        createTableChange.setCatalogName(change.getCatalogName());
        createTableChange.setSchemaName(change.getSchemaName());
        createTableChange.setTableName(change.getTableName());
        createTableChange.addColumn(new ColumnConfig().setName("other_col").setType("int"));

        return new Change[] {createTableChange};
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, AddColumnChange change) {
        for (ColumnConfig column : change.getColumns()) {
            Column example = new Column().setName(column.getName()).setRelation(new Table().setName(change.getTableName()).setSchema(new Schema(change.getCatalogName(), change.getSchemaName())));
            Column snapshot = diffResult.getUnexpectedObject(example);
            assertNotNull(snapshot);
            //todo assertEquals(column.getType(), snapshot.getType().toString());
        }
    }
}
