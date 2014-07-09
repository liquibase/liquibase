package liquibase.snapshot.core;

import liquibase.ExecutionEnvironment;
import liquibase.statement.Statement;
import liquibase.statement.core.SelectMetaDataStatement;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.snapshot.AbstractSnapshotGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectCollection;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

public class TableSnapshotGenerator extends AbstractSnapshotGenerator<Table> {

    @Override
    public Statement[] generateAddToStatements(DatabaseObject example, ExecutionEnvironment env, StatementLogicChain chain) {
        if (example instanceof Schema) {
            return new Statement[]{
                    new SelectMetaDataStatement(new Table(getCatalogName((Schema) example), getSchemaName(((Schema) example)), null)),
            };
        }
        return null;
    }

    @Override
    public void addTo(DatabaseObject object, DatabaseObjectCollection collection, ExecutionEnvironment env, StatementLogicChain chain) {
        if (object instanceof Schema) {
            for (Table table : collection.get(Table.class)) {
                if (DatabaseObjectComparatorFactory.getInstance().isSameObject(table.getSchema(), ((Schema) object), env.getTargetDatabase())) {
                    ((Schema) object).addDatabaseObject(table);
                }
            }
        }
    }
}
