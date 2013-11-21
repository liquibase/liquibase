package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGeneratorChain;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;

import java.util.ArrayList;
import java.util.List;

public class SequenceSnapshotGenerator extends JdbcSnapshotGenerator {

    public SequenceSnapshotGenerator() {
        super(Sequence.class, new Class[] { Schema.class});
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getDatabase().supportsSequences()) {
            return;
        }
        if (foundObject instanceof Schema) {
            Schema schema = (Schema) foundObject;
            Database database = snapshot.getDatabase();
            if (!database.supportsSequences()) {
                updateListeners("Sequences not supported for " + database.toString() + " ...");
            }

            //noinspection unchecked
            List<String> sequenceNames = (List<String>) ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement(getSelectSequenceSql(schema, database)), String.class);

            if (sequenceNames != null) {
                for (String sequenceName : sequenceNames) {
                    schema.addDatabaseObject(new Sequence().setName(cleanNameFromDatabase(sequenceName, database)).setSchema(schema));
                }
            }
        }
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException {
        Database database = snapshot.getDatabase();
        if (!database.supportsSequences()) {
            return null;
        }

        List<String> sequenceNames = (List<String>) ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement(getSelectSequenceSql(example.getSchema(), database)), String.class);
        for (String name : sequenceNames) {
            name = cleanNameFromDatabase(name, database);
            if ((database.isCaseSensitive() && name.equals(example.getName())
                    || (!database.isCaseSensitive() && name.equalsIgnoreCase(example.getName())))) {
                Sequence seq = new Sequence();
                seq.setName(name);
                seq.setSchema(example.getSchema());

                return seq;

            }
        }

        return null;
    }

    protected String getSelectSequenceSql(Schema schema, Database database) {
        if (database instanceof DB2iDatabase) {
            return "SELECT SEQUENCE_NAME FROM QSYS2.SYSSEQUENCES WHERE SEQUENCE_SCHEMA = '" + schema.getCatalogName() + "'";
        } else if (database instanceof DB2Database) {
            return "SELECT SEQNAME AS SEQUENCE_NAME FROM SYSCAT.SEQUENCES WHERE SEQTYPE='S' AND SEQSCHEMA = '" + schema.getCatalogName() + "'";
        } else if (database instanceof DerbyDatabase) {
            return "SELECT " +
                    "  seq.SEQUENCENAME AS SEQUENCE_NAME " +
                    "FROM " +
                    "  SYS.SYSSEQUENCES seq, " +
                    "  SYS.SYSSCHEMAS sch " +
                    "WHERE " +
                    "  sch.SCHEMANAME = '" + schema.getName() + "' AND " +
                    "  sch.SCHEMAID = seq.SCHEMAID";
        } else if (database instanceof FirebirdDatabase) {
            return "SELECT RDB$GENERATOR_NAME FROM RDB$GENERATORS WHERE RDB$SYSTEM_FLAG IS NULL OR RDB$SYSTEM_FLAG = 0";
        } else if (database instanceof H2Database) {
            return "SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA = '" + schema.getName() + "' AND IS_GENERATED=FALSE";
        } else if (database instanceof HsqlDatabase) {
            return "SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_SCHEMA = '" + schema.getName() + "'";
        } else if (database instanceof InformixDatabase) {
            return "SELECT tabname FROM systables t, syssequences s WHERE s.tabid = t.tabid AND t.owner = '" + schema.getName() + "'";
        } else if (database instanceof MaxDBDatabase) {
            return "SELECT SEQUENCE_NAME FROM DOMAIN.SEQUENCES WHERE OWNER = '" + schema.getName() + "'";
        } else if (database instanceof OracleDatabase) {
            return "SELECT SEQUENCE_NAME FROM ALL_SEQUENCES WHERE SEQUENCE_OWNER = '" + schema.getCatalogName() + "'";
        } else if (database instanceof PostgresDatabase) {
            return "SELECT relname AS SEQUENCE_NAME FROM pg_class, pg_namespace " +
                    "WHERE relkind='S' " +
                    "AND pg_class.relnamespace = pg_namespace.oid " +
                    "AND nspname = '" + schema.getName() + "' " +
                    "AND 'nextval(''" + schema.getName() + "." + "'||relname||'''::regclass)' not in (select adsrc from pg_attrdef where adsrc is not null) " +
                    "AND 'nextval(''" + schema.getName() + "." + "\"'||relname||'\"''::regclass)' not in (select adsrc from pg_attrdef where adsrc is not null) " +
                    "AND 'nextval('''||relname||'''::regclass)' not in (select adsrc from pg_attrdef where adsrc is not null)" +
                    "AND 'nextval(''\"'||relname||'\"''::regclass)' not in (select adsrc from pg_attrdef where adsrc is not null)";
        } else {
            throw new UnexpectedLiquibaseException("Don't know how to query for sequences on " + database);
        }

    }

    //from SQLiteDatbaseSnapshotGenerator
    //    protected void readSequences(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws DatabaseException {
//        Database database = snapshot.getDatabase();
//        updateListeners("Reading sequences for " + database.toString() + " ...");
//
//        String convertedSchemaName = database.convertRequestedSchemaToSchema(schema);
//
//        if (database.supportsSequences()) {
//            //noinspection unchecked
//            List<String> sequenceNamess = (List<String>) ExecutorService.getInstance().getExecutor(database).queryForList(new SelectSequencesStatement(schema), String.class);
//
//
//            for (String sequenceName : sequenceNamess) {
//                Sequence seq = new Sequence();
//                seq.setName(sequenceName.trim());
//                seq.setName(convertedSchemaName);
//
//                snapshot.getSequences().add(seq);
//            }
//        }
//    }

}
