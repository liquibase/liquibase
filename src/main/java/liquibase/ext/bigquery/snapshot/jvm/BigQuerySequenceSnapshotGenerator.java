package liquibase.ext.bigquery.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.ext.bigquery.database.BigqueryDatabase;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.snapshot.jvm.SequenceSnapshotGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;

public class BigQuerySequenceSnapshotGenerator extends SequenceSnapshotGenerator {

        @Override
        public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
            int priority = super.getPriority(objectType, database);
            if (priority > PRIORITY_NONE && database instanceof BigqueryDatabase) {
                priority += PRIORITY_DATABASE;
            }
            return priority;
        }

        @Override
        public Class<? extends SnapshotGenerator>[] replaces() {
            return new Class[]{SequenceSnapshotGenerator.class};
        }

        @Override
        protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException {
            Scope.getCurrentScope().getLog(this.getClass()).info("Sequences are not supported by BigQuery");
            return null;
        }

        @Override
        protected String getSelectSequenceSql(Schema schema, Database database) {
            if (database instanceof BigqueryDatabase) {
                // BigQuery does not support sequences
                //String catalog = database.getDefaultCatalogName();
                CatalogAndSchema catalogAndSchema = (new CatalogAndSchema(schema.getCatalogName(), schema.getName())).customize(database);

                String jdbcSchemaName = database.correctObjectName(((AbstractJdbcDatabase)database).getJdbcSchemaName(catalogAndSchema), Schema.class);

                return "SELECT NULL AS SEQUENCE_NAME, NULL AS START_VALUE, NULL AS AS MIN_VALUE, NULL AS MAX_VALUE, " +
                        "NULL AS INCREMENT_BY, " +
                        "NULL AS WILL_CYCLE "+
                        jdbcSchemaName+"."+database.getSystemSchema().toUpperCase() + ".COLUMNS WHERE 1=0";
            }
            return super.getSelectSequenceSql(schema, database);
        }
    }
