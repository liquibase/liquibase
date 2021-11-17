package liquibase.command.core;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.command.*;
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

public class InternalSnapshotCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"internalSnapshot"};

    public static final CommandArgumentDefinition<Database> DATABASE_ARG;
    public static final CommandArgumentDefinition<CatalogAndSchema[]> SCHEMAS_ARG;
    public static final CommandArgumentDefinition<String> SERIALIZER_FORMAT_ARG;
    public static final CommandArgumentDefinition<SnapshotListener> SNAPSHOT_LISTENER_ARG;

    private Map<String, Object> snapshotMetadata;

    static {
        final CommandBuilder builder = new CommandBuilder(COMMAND_NAME);

        DATABASE_ARG = builder.argument("database", Database.class).required().build();
        SCHEMAS_ARG = builder.argument("schemas", CatalogAndSchema[].class).build();
        SERIALIZER_FORMAT_ARG = builder.argument("serializerFormat", String.class).build();
        SNAPSHOT_LISTENER_ARG = builder.argument("snapshotListener", SnapshotListener.class).build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        super.adjustCommandDefinition(commandDefinition);
        commandDefinition.setInternal(true);
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
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        Database database = commandScope.getArgumentValue(DATABASE_ARG);
        SnapshotListener snapshotListener = commandScope.getArgumentValue(SNAPSHOT_LISTENER_ARG);
        CatalogAndSchema[] schemas = commandScope.getArgumentValue(SCHEMAS_ARG);

        InternalSnapshotCommandStep.logUnsupportedDatabase(database, this.getClass());
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

        resultsBuilder.addResult("snapshot", snapshot);
    }

    public static String printSnapshot(CommandScope commandScope, CommandResults snapshotResults) throws LiquibaseException {
        String format = commandScope.getArgumentValue(InternalSnapshotCommandStep.SERIALIZER_FORMAT_ARG);
        if (format == null) {
            format = "txt";
        }

        return SnapshotSerializerFactory.getInstance().getSerializer(format.toLowerCase(Locale.US)).serialize((DatabaseSnapshot) snapshotResults.getResult("snapshot"), true);
    }
//
//        public void merge(SnapshotCommandResult resultToMerge) {
//            this.snapshot.merge(resultToMerge.snapshot);
//        }
//    }

    public static void logUnsupportedDatabase(Database database, Class callingClass) {
        if (LicenseServiceUtils.isProLicenseValid()) {
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
