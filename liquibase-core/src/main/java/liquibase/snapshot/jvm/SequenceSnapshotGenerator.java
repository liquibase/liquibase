package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.UnsupportedException;
import liquibase.executor.ExecutorService;
import liquibase.executor.Row;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;

import java.math.BigInteger;
import java.util.List;

public class SequenceSnapshotGenerator extends JdbcSnapshotGenerator {

    public SequenceSnapshotGenerator() {
        super(Sequence.class, new Class[] { Schema.class});
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException, UnsupportedException {
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
            List<Row> sequenceNames = ExecutorService.getInstance().getExecutor(database).query(new RawSqlStatement(getSelectSequenceSql(schema, database))).toList();

            if (sequenceNames != null) {
                for (Row sequence : sequenceNames) {
                    schema.addDatabaseObject(new Sequence().setName(cleanNameFromDatabase(sequence.get("SEQUENCE_NAME", String.class), database)).setSchema(schema));
                }
            }
        }
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException, UnsupportedException {
        Database database = snapshot.getDatabase();
        if (!database.supportsSequences()) {
            return null;
        }

        List<Row> sequences = ExecutorService.getInstance().getExecutor(database).query(new RawSqlStatement(getSelectSequenceSql(example.getSchema(), database))).toList();
        for (Row sequenceRow : sequences) {
            String name = cleanNameFromDatabase(sequenceRow.get("SEQUENCE_NAME", String.class), database);
            if ((database.isCaseSensitive() && name.equals(example.getName()) || (!database.isCaseSensitive() && name.equalsIgnoreCase(example.getName())))) {
                Sequence seq = new Sequence();
                seq.setName(name);
                seq.setSchema(example.getSchema());
                seq.setStartValue(sequenceRow.get("START_VALUE", BigInteger.class));
                seq.setMinValue(sequenceRow.get("MIN_VALUE", BigInteger.class));
                seq.setMaxValue(sequenceRow.get("MAX_VALUE", BigInteger.class));
                seq.setCacheSize(sequenceRow.get("CACHE_SIZE", BigInteger.class));
                seq.setIncrementBy(sequenceRow.get("INCREMENT_BY", BigInteger.class));
                seq.setWillCycle(sequenceRow.get("WILL_CYCLE", Boolean.class));
                seq.setOrdered(sequenceRow.get("IS_ORDERED", Boolean.class));


                return seq;

            }
        }

        return null;
    }

    protected String getSelectSequenceSql(Schema schema, Database database) {
        if (database instanceof DB2Database) {
            return "SELECT SEQNAME AS SEQUENCE_NAME FROM SYSCAT.SEQUENCES WHERE SEQTYPE='S' AND SEQSCHEMA = '" + schema.getCatalogName() + "'";
        } else if (database instanceof DerbyDatabase) {
            return "SELECT " +
                    "  seq.SEQUENCENAME AS SEQUENCE_NAME " +
                    "FROM " +
                    "  SYS.SYSSEQUENCES seq, " +
                    "  SYS.SYSSCHEMAS sch " +
                    "WHERE " +
                    "  sch.SCHEMANAME = '" + new CatalogAndSchema(null, schema.getName()).customize(database).getSchemaName() + "' AND " +
                    "  sch.SCHEMAID = seq.SCHEMAID";
        } else if (database instanceof FirebirdDatabase) {
            return "SELECT RDB$GENERATOR_NAME AS SEQUENCE_NAME FROM RDB$GENERATORS WHERE RDB$SYSTEM_FLAG IS NULL OR RDB$SYSTEM_FLAG = 0";
        } else if (database instanceof H2Database) {
            return "SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA = '" + schema.getName() + "' AND IS_GENERATED=FALSE";
        } else if (database instanceof HsqlDatabase) {
            return "SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_SCHEMA = '" + schema.getName() + "'";
        } else if (database instanceof InformixDatabase) {
            return "SELECT tabname AS SEQUENCE_NAME FROM systables t, syssequences s WHERE s.tabid = t.tabid AND t.owner = '" + schema.getName() + "'";
        } else if (database instanceof OracleDatabase) {
            return "SELECT SEQUENCE_NAME AS SEQUENCE_NAME, MIN_VALUE, MAX_VALUE, INCREMENT_BY, CYCLE_FLAG AS WILL_CYCLE, ORDER_FLAG AS IS_ORDERED, LAST_NUMBER as START_VALUE, CACHE_SIZE FROM ALL_SEQUENCES WHERE SEQUENCE_OWNER = '" + schema.getCatalogName() + "'";
        } else if (database instanceof PostgresDatabase) {
            return "SELECT relname AS SEQUENCE_NAME FROM pg_class, pg_namespace " +
                    "WHERE relkind='S' " +
                    "AND pg_class.relnamespace = pg_namespace.oid " +
                    "AND nspname = '" + schema.getName() + "' " +
                    "AND 'nextval(''" + schema.getName() + "." + "'||relname||'''::regclass)' not in (select adsrc from pg_attrdef where adsrc is not null) " +
                    "AND 'nextval(''" + schema.getName() + "." + "\"'||relname||'\"''::regclass)' not in (select adsrc from pg_attrdef where adsrc is not null) " +
                    "AND 'nextval('''||relname||'''::regclass)' not in (select adsrc from pg_attrdef where adsrc is not null)" +
                    "AND 'nextval(''\"'||relname||'\"''::regclass)' not in (select adsrc from pg_attrdef where adsrc is not null)";
        } else if (database instanceof MSSQLDatabase) {
                return "SELECT SEQUENCE_NAME, " +
                        "cast(START_VALUE AS BIGINT) AS START_VALUE, " +
                        "cast(MINIMUM_VALUE AS BIGINT) AS MIN_VALUE, " +
                        "cast(MAXIMUM_VALUE AS BIGINT) AS MAX_VALUE, " +
                        "CAST(INCREMENT AS BIGINT) AS INCREMENT_BY, " +
                        "CYCLE_OPTION AS WILL_CYCLE " +
                        "FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA = '" + schema.getName() +"'";
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
