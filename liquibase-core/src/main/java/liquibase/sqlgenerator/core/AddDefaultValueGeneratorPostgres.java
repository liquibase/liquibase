package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.structure.Column;
import liquibase.database.structure.Schema;
import liquibase.database.structure.Sequence;
import liquibase.database.structure.Table;
import liquibase.datatype.DataTypeFactory;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.SequenceFunction;
import liquibase.statement.core.AddDefaultValueStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Adds functionality for setting the sequence to be owned by the column with the default value
 */
public class AddDefaultValueGeneratorPostgres extends AddDefaultValueGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddDefaultValueStatement statement, Database database) {
        return database instanceof PostgresDatabase;
    }

    @Override
    public Sql[] generateSql(final AddDefaultValueStatement statement, final Database database,
                             final SqlGeneratorChain sqlGeneratorChain) {

        if (!(statement.getDefaultValue() instanceof SequenceFunction)) {
            return super.generateSql(statement, database, sqlGeneratorChain);
        }

        List<Sql> commands = new ArrayList<Sql>(Arrays.asList(super.generateSql(statement, database, sqlGeneratorChain)));
        // for postgres, we need to also set the sequence to be owned by this table for true serial like functionality.
        // this will allow a drop table cascade to remove the sequence as well.
        SequenceFunction sequenceFunction = (SequenceFunction) statement.getDefaultValue();

        Sql alterSequenceOwner = new UnparsedSql("ALTER SEQUENCE " + database.escapeSequenceName(statement.getCatalogName(),
                statement.getSchemaName(), sequenceFunction.getValue()) + " OWNED BY " +
                database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + "."
                + database.escapeDatabaseObject(statement.getColumnName(), Column.class),
                new Column()
                        .setRelation(new Table(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName())))
                        .setName(statement.getColumnName()),
                new Sequence()
                        .setName(sequenceFunction.getValue()));
        commands.add(alterSequenceOwner);
        return commands.toArray(new Sql[commands.size()]);
    }
}
