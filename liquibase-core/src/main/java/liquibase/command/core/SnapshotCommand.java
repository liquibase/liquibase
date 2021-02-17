package liquibase.command.core;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.command.AbstractCommand;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandScope;
import liquibase.command.CommandValidationErrors;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.core.*;
import liquibase.exception.LiquibaseException;
import liquibase.license.LicenseServiceUtils;
import liquibase.serializer.SnapshotSerializerFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.snapshot.SnapshotListener;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SnapshotCommand extends AbstractCommand {

    public static final CommandArgumentDefinition<Database> DATABASE_ARG;
    public static final CommandArgumentDefinition<CatalogAndSchema[]> SCHEMAS_ARG;
    public static final CommandArgumentDefinition<String> SERIALIZER_FORMAT_ARG;
    public static final CommandArgumentDefinition<SnapshotListener> SNAPSHOT_LISTENER_ARG;

    private Map<String, Object> snapshotMetadata;

    static {
        final CommandArgumentDefinition.Builder builder = new CommandArgumentDefinition.Builder();

        DATABASE_ARG = builder.define("database", Database.class).required().build();
        SCHEMAS_ARG = builder.define("schemas", CatalogAndSchema[].class).required().build();
        SERIALIZER_FORMAT_ARG = builder.define("serializerFormat", String.class).required().build();
        SNAPSHOT_LISTENER_ARG = builder.define("snapshotListener", SnapshotListener.class).required().build();
    }

    @Override
    public String[] getName() {
        return new String[]{"snapshot"};
    }


    public static CatalogAndSchema[] parseSchemas(Database database, String... schemas) {
        if ((schemas == null) || (schemas.length == 0) || (schemas[0] == null)) {
            return null;
        }

        schemas = StringUtil.join(schemas, ",").split("\\s*,\\s*");
        List<CatalogAndSchema> finalList = new ArrayList<>();
        for (String schema : schemas) {
            finalList.add(new CatalogAndSchema(null, schema).customize(database));
        }

        return finalList.toArray(new CatalogAndSchema[finalList.size()]);
    }

    public Map<String, Object> getSnapshotMetadata() {
        return snapshotMetadata;
    }

    public void setSnapshotMetadata(Map<String, Object> snapshotMetadata) {
        this.snapshotMetadata = snapshotMetadata;
    }

    @Override
    public void run(CommandScope commandScope) throws Exception {
        Database database = DATABASE_ARG.getValue(commandScope);
        SnapshotListener snapshotListener = SNAPSHOT_LISTENER_ARG.getValue(commandScope);
        CatalogAndSchema[] schemas = SCHEMAS_ARG.getValue(commandScope);

        SnapshotCommand.logUnsupportedDatabase(database, this.getClass());
        SnapshotControl snapshotControl = new SnapshotControl(database);
        snapshotControl.setSnapshotListener(snapshotListener);

        if (schemas == null) {
            schemas = new CatalogAndSchema[]{database.getDefaultSchema()};
        }

        ObjectQuotingStrategy originalQuotingStrategy = database.getObjectQuotingStrategy();

        database.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
        DatabaseSnapshot snapshot;
        try {
            snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(schemas, database, snapshotControl);
        } finally {
            database.setObjectQuotingStrategy(originalQuotingStrategy);
        }

        snapshot.setMetadata(this.getSnapshotMetadata());

        commandScope.addResult("snapshot", snapshot);
    }

    @Override
    public CommandValidationErrors validate() {
        return new CommandValidationErrors(this);
    }

    public static String printSnapshot(CommandScope commandScope) throws LiquibaseException {
        String format = SnapshotCommand.SERIALIZER_FORMAT_ARG.getValue(commandScope);
        if (format == null) {
            format = "txt";
        }

        return SnapshotSerializerFactory.getInstance().getSerializer(format.toLowerCase(Locale.US)).serialize((DatabaseSnapshot) commandScope.getResult("snapshot"), true);
    }
//
//        public void merge(SnapshotCommandResult resultToMerge) {
//            this.snapshot.merge(resultToMerge.snapshot);
//        }
//    }

    public static void logUnsupportedDatabase(Database database, Class callingClass) {
        if (LicenseServiceUtils.checkForValidLicense("Liquibase Pro")) {
            if (!(database instanceof MSSQLDatabase
                    || database instanceof OracleDatabase
                    || database instanceof MySQLDatabase
                    || database instanceof DB2Database
                    || database instanceof PostgresDatabase)) {
                Scope.getCurrentScope().getUI().sendMessage("INFO This command might not yet capture Liquibase Pro additional object types on " + database.getShortName());
            }
        }
    }

}
