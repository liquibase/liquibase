package liquibase.snapshot.jvm;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.CachedRow;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class UniqueConstraintSnapshotGeneratorSnowflake extends UniqueConstraintSnapshotGenerator {


    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof SnowflakeDatabase) {
            return PRIORITY_DATABASE;
        } else {
            return PRIORITY_NONE;
        }
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return new Class[] { UniqueConstraintSnapshotGenerator.class };
    }

    @Override
    protected List<CachedRow> listConstraints(Table table, DatabaseSnapshot snapshot, Schema schema)
            throws DatabaseException, SQLException {
        return new SnowflakeResultSetConstraintsExtractor(snapshot, schema.getCatalogName(), schema.getName(), table.getName())
                .fastFetch();
    }

    @Override
    protected List<Map<String, ?>> listColumns(UniqueConstraint example, Database database, DatabaseSnapshot snapshot)
            throws DatabaseException {
        Relation table = example.getRelation();
        String name = example.getName();
        String tableName = database.correctObjectName(table.getName(), Table.class);
        String constraintName = database.correctObjectName(name, UniqueConstraint.class);

        String showSql = String.format("SHOW UNIQUE KEYS IN %s", database.escapeObjectName(table.getSchema().getCatalogName(), table.getSchema().getName(), tableName, Table.class));
        String sql = "SELECT \"column_name\" AS COLUMN_NAME FROM TABLE(result_scan(last_query_id())) WHERE \"constraint_name\"= ?";

        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database)
                .queryForList(new RawParameterizedSqlStatement(showSql));

        return Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database)
                .queryForList(new RawParameterizedSqlStatement(sql, constraintName));
    }
}
