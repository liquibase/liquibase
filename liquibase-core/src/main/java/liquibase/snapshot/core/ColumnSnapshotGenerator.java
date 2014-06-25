package liquibase.snapshot.core;

import liquibase.ExecutionEnvironment;
import liquibase.statement.Statement;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.snapshot.AbstractSnapshotGenerator;
import liquibase.statement.core.MetaDataQueryStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectCollection;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

public class ColumnSnapshotGenerator extends AbstractSnapshotGenerator<Column> {

    @Override
    public Statement[] generateAddToStatements(DatabaseObject example, ExecutionEnvironment env, StatementLogicChain chain) {
        if (example instanceof Table) {
            return new Statement[] {
                    new MetaDataQueryStatement(new Column(Table.class, getCatalogName(example.getSchema()), getSchemaName(example.getSchema()), example.getName(), null)),
            };
        }
        return null;
    }


    @Override
    public void addTo(DatabaseObject object, DatabaseObjectCollection collection, ExecutionEnvironment env, StatementLogicChain chain) {
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
