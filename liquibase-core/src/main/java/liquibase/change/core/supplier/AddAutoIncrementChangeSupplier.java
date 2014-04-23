package liquibase.change.core.supplier;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.AddAutoIncrementChange;
import liquibase.change.core.CreateTableChange;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.exception.DatabaseException;
import liquibase.sdk.supplier.change.AbstractChangeSupplier;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AddAutoIncrementChangeSupplier extends AbstractChangeSupplier<AddAutoIncrementChange>  {

    public AddAutoIncrementChangeSupplier() {
        super(AddAutoIncrementChange.class);
    }

    @Override
    public Change[] prepareDatabase(AddAutoIncrementChange change) throws DatabaseException {
        CreateTableChange createTable = new CreateTableChange();
        createTable.setCatalogName(change.getCatalogName());
        createTable.setSchemaName(change.getSchemaName());
        createTable.setTableName(change.getTableName());
        String dataType = change.getColumnDataType();
        if (dataType == null) {
            dataType = "int";
        }
        createTable.addColumn(new ColumnConfig().setName(change.getColumnName()).setType(dataType).setConstraints(new ConstraintsConfig().setPrimaryKey(true).setNullable(false)));

        return new Change[] {createTable};
    }

    @Override
    public void checkDiffResult(DiffResult diffResult, AddAutoIncrementChange change) {
        Column example = new Column().setName(change.getColumnName()).setRelation(new Table().setName(change.getTableName()).setSchema(new Schema(change.getCatalogName(), change.getSchemaName())));
        ObjectDifferences changes = diffResult.getChangedObject(example);
        assertNotNull(changes);
        assertNull(changes.getDifference("autoIncrementInformation").getReferenceValue());
        assertNotNull(changes.getDifference("autoIncrementInformation").getComparedValue());

        assertNull(changes.getDifference("dataType"));
    }
}
