package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotIdService;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * Snapshot generator for a SEQUENCE object in a JDBC-accessible database
 */
public class SequenceSnapshotGenerator extends JdbcSnapshotGenerator {

    public SequenceSnapshotGenerator() {
        super(Sequence.class, new Class[]{Schema.class});
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
            List<Map<String, ?>> sequences = ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement(getSelectSequenceSql(schema, database)));

            if (sequences != null) {
                for (Map<String, ?> sequence : sequences) {
                    schema.addDatabaseObject(mapToSequence(sequence, (Schema) foundObject, database));
                }
            }
        }
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException {
        if (example.getSnapshotId() != null) {
            return example;
        }
        Database database = snapshot.getDatabase();
        List<Map<String, ?>> sequences;
        if (database instanceof Db2zDatabase) {
            sequences = ExecutorService.getInstance()
                    .getExecutor(database)
                    .queryForList(new RawSqlStatement(getSelectSequenceSql(example.getSchema(), database)));
            return getSequences(example, database, sequences);
        } else {
            if (example.getAttribute("liquibase-complete", false)) { //need to go through "snapshotting" the object even if it was previously populated in addTo. Use the "liquibase-complete" attribute to track that it doesn't need to be fully snapshotted
                example.setSnapshotId(SnapshotIdService.getInstance().generateId());
                example.setAttribute("liquibase-complete", null);
                return example;
            }

            if (!database.supportsSequences()) {
                return null;
            }

            sequences = ExecutorService.getInstance()
                    .getExecutor(database)
                    .queryForList(new RawSqlStatement(getSelectSequenceSql(example.getSchema(), database)));
            DatabaseObject sequenceRow = getSequences(example, database, sequences);
            if (sequenceRow != null) return sequenceRow;
        }
        return null;
    }

    private DatabaseObject getSequences(DatabaseObject example, Database database, List<Map<String, ?>> sequences) {
        for (Map<String, ?> sequenceRow : sequences) {
            String name = cleanNameFromDatabase((String) sequenceRow.get("SEQUENCE_NAME"), database);
            if (((database.isCaseSensitive() && name.equals(example.getName())) || (!database.isCaseSensitive() &&
                name.equalsIgnoreCase(example.getName())))) {
                return mapToSequence(sequenceRow, example.getSchema(), database);
            }
        }
        return null;
    }

    private Sequence mapToSequence(Map<String, ?> sequenceRow, Schema schema, Database database) {
        String name = cleanNameFromDatabase((String) sequenceRow.get("SEQUENCE_NAME"), database);
        Sequence seq = new Sequence();
        seq.setName(name);
        seq.setSchema(schema);
        seq.setStartValue(toBigInteger(sequenceRow.get("START_VALUE"), database));
        seq.setMinValue(toBigInteger(sequenceRow.get("MIN_VALUE"), database));
        seq.setMaxValue(toBigInteger(sequenceRow.get("MAX_VALUE"), database));
        seq.setCacheSize(toBigInteger(sequenceRow.get("CACHE_SIZE"), database));
        seq.setIncrementBy(toBigInteger(sequenceRow.get("INCREMENT_BY"), database));
        seq.setWillCycle(toBoolean(sequenceRow.get("WILL_CYCLE"), database));
        seq.setOrdered(toBoolean(sequenceRow.get("IS_ORDERED"), database));
        seq.setAttribute("liquibase-complete", true);

        return seq;
    }

    protected Boolean toBoolean(Object value, Database database) {
        if (value == null) {
            return null;
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        String valueAsString = value.toString();
        valueAsString = valueAsString.replace("'", "");
        if ("true".equalsIgnoreCase(valueAsString)
                || "'true'".equalsIgnoreCase(valueAsString)
                || "y".equalsIgnoreCase(valueAsString)
                || "1".equalsIgnoreCase(valueAsString)
                || "t".equalsIgnoreCase(valueAsString)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    protected BigInteger toBigInteger(Object value, Database database) {
        if (value == null) {
            return null;
        }

        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }

        return new BigInteger(value.toString());
    }

    protected String getSelectSequenceSql(Schema schema, Database database) {
        if (database instanceof DB2Database) {
            if (database.getDatabaseProductName().startsWith("DB2 UDB for AS/400")) {
                return "SELECT SEQNAME AS SEQUENCE_NAME FROM QSYS2.SYSSEQUENCES WHERE SEQSCHEMA = '" + schema.getCatalogName() + "'";
            }
            return "SELECT SEQNAME AS SEQUENCE_NAME FROM SYSCAT.SEQUENCES WHERE SEQTYPE='S' AND SEQSCHEMA = '" + schema.getCatalogName() + "'";
        } else if (database instanceof Db2zDatabase) {
            return "SELECT NAME AS SEQUENCE_NAME, " +
                    "START AS START_VALUE, " +
                    "MINVALUE AS MIN_VALUE, " +
                    "MAXVALUE AS MAX_VALUE, " +
                    "CACHE AS CACHE_SIZE, " +
                    "INCREMENT AS INCREMENT_BY, " +
                    "CYCLE AS WILL_CYCLE, " +
                    "ORDER AS IS_ORDERED " +
                    "FROM SYSIBM.SYSSEQUENCES WHERE SEQTYPE = 'S' AND SCHEMA = '" + schema.getCatalogName() + "'";
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
            return "SELECT TRIM(RDB$GENERATOR_NAME) AS SEQUENCE_NAME FROM RDB$GENERATORS WHERE RDB$SYSTEM_FLAG IS NULL OR RDB$SYSTEM_FLAG = 0";
        } else if (database instanceof H2Database) {
            return "SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA = '" + schema.getName() + "' AND IS_GENERATED=FALSE";
        } else if (database instanceof HsqlDatabase) {
            return "SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_SCHEMA = '" + schema.getName() + "'";
        } else if (database instanceof InformixDatabase) {
            return "SELECT tabname AS SEQUENCE_NAME FROM systables t, syssequences s WHERE s.tabid = t.tabid AND t.owner = '" + schema.getName() + "'";
        } else if (database instanceof OracleDatabase) {
            /*
             * Return an SQL statement that only returns the non-default values so the output changeLog is cleaner
             * and less polluted with unnecessary values.
             * The the following pages for the defaults (consistent for all supported releases ATM):
             * 12cR2: http://docs.oracle.com/database/122/SQLRF/CREATE-SEQUENCE.htm
             * 12cR1: http://docs.oracle.com/database/121/SQLRF/statements_6017.htm
             * 11gR2: http://docs.oracle.com/cd/E11882_01/server.112/e41084/statements_6015.htm
             */
            return "SELECT sequence_name, \n" +
                    "CASE WHEN increment_by > 0 \n" +
                    "     THEN CASE WHEN min_value=1 THEN NULL ELSE min_value END\n" +
                    "     ELSE CASE WHEN min_value=(-999999999999999999999999999) THEN NULL else min_value END\n" +
                    "END AS min_value, \n" +
                    "CASE WHEN increment_by > 0 \n" +
                    "     THEN CASE WHEN max_value=999999999999999999999999999 THEN NULL ELSE max_value END\n" +
                    "     ELSE CASE WHEN max_value=last_number THEN NULL else max_value END \n" +
                    "END  AS max_value, \n" +
                    "CASE WHEN increment_by = 1 THEN NULL ELSE increment_by END AS increment_by, \n" +
                    "CASE WHEN cycle_flag = 'N' THEN NULL ELSE cycle_flag END AS will_cycle, \n" +
                    "CASE WHEN order_flag = 'N' THEN NULL ELSE order_flag END AS is_ordered, \n" +
                    "LAST_NUMBER as START_VALUE, \n" +
                    "CASE WHEN cache_size = 20 THEN NULL ELSE cache_size END AS cache_size \n" +
                    "FROM ALL_SEQUENCES WHERE SEQUENCE_OWNER = '" + schema.getCatalogName() + "'";
        } else if (database instanceof PostgresDatabase) {
            return "SELECT c.relname AS SEQUENCE_NAME FROM pg_class c " +
                    "join pg_namespace on c.relnamespace = pg_namespace.oid " +
                    "WHERE c.relkind='S' " +
                    "AND nspname = '" + schema.getName() + "' " +
                    "AND c.oid not in (select d.objid FROM pg_depend d where d.refobjsubid > 0)"
                    ;


//        select c.relname FROM pg_class c, pg_user u
//            WHERE c.relowner = u.usesysid and c.relkind = 'S'
//            AND relnamespace IN (
//                    SELECT oid
//                    FROM pg_namespace
//                    WHERE nspname ='public'
//            ) and c.oid not in (SELECT d.objid
//                    FROM   pg_depend    d
//                    JOIN   pg_attribute a ON a.attrelid = d.refobjid AND a.attnum = d.refobjsubid
//                    WHERE  d.refobjsubid > 0
//            );
        } else if (database instanceof MSSQLDatabase) {
            return "SELECT SEQUENCE_NAME, " +
                    "cast(START_VALUE AS BIGINT) AS START_VALUE, " +
                    "cast(MINIMUM_VALUE AS BIGINT) AS MIN_VALUE, " +
                    "cast(MAXIMUM_VALUE AS BIGINT) AS MAX_VALUE, " +
                    "CAST(INCREMENT AS BIGINT) AS INCREMENT_BY, " +
                    "CYCLE_OPTION AS WILL_CYCLE " +
                    "FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA = '" + schema.getName() + "'";
        } else if (database instanceof SybaseASADatabase) {
        	return "SELECT SEQUENCE_NAME, " +
                    "START_WITH AS START_VALUE, " +
                    "MIN_VALUE, " +
                    "MAX_VALUE, " +
                    "INCREMENT_BY, " +
                    "CYCLE AS WILL_CYCLE " +
                    "FROM SYS.SYSSEQUENCE s " +
                    "JOIN SYS.SYSUSER u ON s.OWNER = u.USER_ID "+
                    "WHERE u.USER_NAME = '" + schema.getName() + "'";
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
