package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            List<Map<String, ?>> sequenceNames = ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement(getSelectSequenceSql(schema, database)));

            if (sequenceNames != null) {
                for (Map<String, ?> sequence : sequenceNames) {
                    schema.addDatabaseObject(new Sequence().setName(cleanNameFromDatabase((String) sequence.get("SEQUENCE_NAME"), database)).setSchema(schema));
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

        List<Map<String, ?>> sequences = ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement(getSelectSequenceSql(example.getSchema(), database)));
        for (Map<String, ?> sequenceRow : sequences) {
            String name = cleanNameFromDatabase((String) sequenceRow.get("SEQUENCE_NAME"), database);
            if ((database.isCaseSensitive() && name.equals(example.getName()) || (!database.isCaseSensitive() && name.equalsIgnoreCase(example.getName())))) {
                Sequence seq = new Sequence();
                seq.setName(name);
                seq.setSchema(example.getSchema());
                seq.setStartValue(toBigInteger(sequenceRow.get("START_VALUE"), database));
                seq.setMinValue(toBigInteger(sequenceRow.get("MIN_VALUE"), database));
                seq.setMaxValue(toBigInteger(sequenceRow.get("MAX_VALUE"), database));
                seq.setCacheSize(toBigInteger(sequenceRow.get("CACHE_SIZE"), database));
                seq.setIncrementBy(toBigInteger(sequenceRow.get("INCREMENT_BY"), database));
                seq.setWillCycle(toBoolean(sequenceRow.get("WILL_CYCLE"), database));
                seq.setOrdered(toBoolean(sequenceRow.get("IS_ORDERED"), database));


                return seq;

            }
        }

        return null;
    }

    protected Boolean toBoolean(Object value, Database database) {
        if (value == null) {
            return null;
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        String valueAsString = value.toString();
        valueAsString = valueAsString.replace("'","");
        if (valueAsString.equalsIgnoreCase("true")
                || valueAsString.equalsIgnoreCase("'true'")
                || valueAsString.equalsIgnoreCase("y")
                || valueAsString.equalsIgnoreCase("1")
                || valueAsString.equalsIgnoreCase("t")) {
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
            if(database.getDatabaseProductName().startsWith("DB2 UDB for AS/400")){
                 return "SELECT SEQNAME AS SEQUENCE_NAME FROM QSYS2.SYSSEQUENCES WHERE SEQSCHEMA = '" + schema.getCatalogName() + "'";
            }
            else{
                return "SELECT SEQNAME AS SEQUENCE_NAME FROM SYSCAT.SEQUENCES WHERE SEQTYPE='S' AND SEQSCHEMA = '" + schema.getCatalogName() + "'";
            }
            
            //return "SELECT SEQNAME AS SEQUENCE_NAME FROM SYSCAT.SEQUENCES WHERE SEQTYPE='S' AND SEQSCHEMA = '" + schema.getCatalogName() + "'";
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
