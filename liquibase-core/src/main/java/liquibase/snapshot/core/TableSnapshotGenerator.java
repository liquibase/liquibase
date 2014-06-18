package liquibase.snapshot.core;

import liquibase.RuntimeEnvironment;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.snapshot.AbstractSnapshotGenerator;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.FetchObjectsStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectCollection;
import liquibase.structure.core.*;

import java.util.Set;

public class TableSnapshotGenerator extends AbstractSnapshotGenerator<Table> {

    @Override
    public SqlStatement[] generateAddToStatements(DatabaseObject example, RuntimeEnvironment runtimeEnvironment, ActionGeneratorChain chain) {
        if (example instanceof Schema) {
            return new SqlStatement[]{
                    new FetchObjectsStatement(new Table(getCatalogName((Schema) example), getSchemaName(((Schema) example)), null)),
            };
        }
        return null;
    }

    @Override
    public void addTo(DatabaseObject object, DatabaseObjectCollection collection, RuntimeEnvironment runtimeEnvironment, ActionGeneratorChain chain) {
        if (object instanceof Schema) {
            for (Table table : collection.get(Table.class)) {
                if (DatabaseObjectComparatorFactory.getInstance().isSameObject(table.getSchema(), ((Schema) object), runtimeEnvironment.getTargetDatabase())) {
                    ((Schema) object).addDatabaseObject(table);
                }
            }
        }
    }
}
