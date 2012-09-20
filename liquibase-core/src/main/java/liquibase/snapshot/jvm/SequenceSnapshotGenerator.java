package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;

import java.util.ArrayList;
import java.util.List;

public class SequenceSnapshotGenerator extends JdbcDatabaseObjectSnapshotGenerator<Sequence> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean has(DatabaseObject container, String objectName, Database database) throws DatabaseException {
        return get(container, objectName, database) != null;
    }

    public boolean has(DatabaseObject container, Sequence example, Database database) throws DatabaseException {
        return get(container, example, database) != null;
    }

    public Sequence[] get(DatabaseObject container, Database database) throws DatabaseException {
        if (!(container instanceof Schema)) {
            return new Sequence[0];
        }
        Schema schema = (Schema) container;
        if (!database.supportsSequences()) {
            updateListeners("Sequences not supported for " + database.toString() + " ...");
            return new Sequence[0];
        }

        updateListeners("Reading sequences for " + database.toString() + " ...");

        List<Sequence> returnSequences = new ArrayList<Sequence>();

        //noinspection unchecked
        List<String> sequenceNames = (List<String>) ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement(getSelectSequenceSql(schema, database)), String.class);


        if (sequenceNames != null) {
            for (String sequenceName : sequenceNames) {
                Sequence seq = new Sequence();
                seq.setName(sequenceName.trim());
                seq.setSchema(new Schema(schema.getCatalogName(), schema.getName()));

                returnSequences.add(seq);
            }
        }

        return returnSequences.toArray(new Sequence[returnSequences.size()]);
    }

    public Sequence get(DatabaseObject container, String objectName, Database database) throws DatabaseException {
        Sequence example = new Sequence();
        example.setName(objectName);
        return get(container, example, database);
    }

    public Sequence get(DatabaseObject container, Sequence example, Database database) throws DatabaseException {
        Sequence[] sequences = get(container, database);
        if (sequences == null) {
            return null;
        }
        String objectName = database.correctObjectName(example.getName(), Sequence.class);
        for (Sequence sequence : sequences) {
            if (sequence.getName().equals(objectName)) {
                return sequence;
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
            return "SELECT SEQUENCE_NAME FROM ALL_SEQUENCES WHERE SEQUENCE_OWNER = '" + schema.getName() + "'";
        } else if (database instanceof PostgresDatabase) {
            return "SELECT relname AS SEQUENCE_NAME FROM pg_class, pg_namespace " +
                    "WHERE relkind='S' " +
                    "AND pg_class.relnamespace = pg_namespace.oid " +
                    "AND nspname = '" + schema.getName() + "' " +
                    "AND 'nextval(''" + (schema == null ? "" : schema + ".") + "'||relname||'''::regclass)' not in (select adsrc from pg_attrdef where adsrc is not null) " +
                    "AND 'nextval(''" + (schema == null ? "" : schema + ".") + "\"'||relname||'\"''::regclass)' not in (select adsrc from pg_attrdef where adsrc is not null) " +
                    "AND 'nextval('''||relname||'''::regclass)' not in (select adsrc from pg_attrdef where adsrc is not null)";
        } else {
            throw new UnexpectedLiquibaseException("Don't know how to query for sequences on " + database.getName());
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
