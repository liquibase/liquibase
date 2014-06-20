package liquibase.snapshot.core;

import liquibase.RuntimeEnvironment;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.snapshot.AbstractSnapshotGenerator;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.FetchObjectsStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectCollection;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

public class ColumnSnapshotGenerator extends AbstractSnapshotGenerator<Column> {

    @Override
    public SqlStatement[] generateAddToStatements(DatabaseObject example, RuntimeEnvironment runtimeEnvironment, ActionGeneratorChain chain) {
        if (example instanceof Table) {
            return new SqlStatement[] {
                    new FetchObjectsStatement(new Column(Table.class, getCatalogName(example.getSchema()), getSchemaName(example.getSchema()), example.getName(), null)),
            };
        }
        return null;
    }


    @Override
    public void addTo(DatabaseObject object, DatabaseObjectCollection collection, RuntimeEnvironment runtimeEnvironment, ActionGeneratorChain chain) {
        if (object instanceof Column) {
            ((Column) object).setRelation(collection.get(((Column) object).getRelation()));
        } else if (object instanceof Table) {
            Table table = (Table) object;
            for (Column column : collection.get(Column.class)) {
                table.getColumns().add(column);
            }
        }
    }
}
