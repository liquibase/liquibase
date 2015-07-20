package liquibase.command;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.serializer.SnapshotSerializerFactory;
import liquibase.snapshot.*;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class OldSnapshotCommand extends AbstractCommand<OldSnapshotCommand.SnapshotCommandResult> {

    private Database database;
    private CatalogAndSchema[] schemas;
    private String serializerFormat;
    private SnapshotListener snapshotListener;

    @Override
    public String getName() {
        return "snapshot";
    }


    public void setDatabase(Database database) {
        this.database = database;
    }

    public Database getDatabase() {
        return database;
    }

    public OldSnapshotCommand setSchemas(CatalogAndSchema... catalogAndSchema) {
        schemas = catalogAndSchema;
        return this;
    }

    public OldSnapshotCommand setSchemas(String... schemas) {
        if (schemas == null || schemas.length == 0 || schemas[0] == null) {
            this.schemas = null;
            return this;
        }

        schemas = StringUtils.join(schemas, ",").split("\\s*,\\s*");
        List<CatalogAndSchema> finalList = new ArrayList<CatalogAndSchema>();
        for (String schema : schemas) {
            finalList.add(new CatalogAndSchema(null, schema).customize(database));
        }

        this.schemas = finalList.toArray(new CatalogAndSchema[finalList.size()]);


        return this;
    }


    public String getSerializerFormat() {
        return serializerFormat;
    }

    public OldSnapshotCommand setSerializerFormat(String serializerFormat) {
        this.serializerFormat = serializerFormat;
        return this;
    }

    public SnapshotListener getSnapshotListener() {
        return snapshotListener;
    }

    public void setSnapshotListener(SnapshotListener snapshotListener) {
        this.snapshotListener = snapshotListener;
    }

    @Override
    protected SnapshotCommandResult run(Scope scope) throws Exception {

        SnapshotControl snapshotControl = new SnapshotControl(database);
        snapshotControl.setSnapshotListener(snapshotListener);

        CatalogAndSchema[] schemas = this.schemas;
        if (schemas == null) {
            schemas = new CatalogAndSchema[]{database.getDefaultSchema()};
        }
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(schemas, database, snapshotControl);

        String format = getSerializerFormat();
        if (format == null) {
            format = "txt";
        }
        return new SnapshotCommandResult(snapshot, SnapshotSerializerFactory.getInstance().getSerializer(format).serialize(snapshot, true));
    }

    @Override
    public CommandValidationErrors validate() {
        return new CommandValidationErrors(this);
    }

    public static class SnapshotCommandResult extends CommandResult {

        public String formattedSnapshot;
        public DatabaseSnapshot snapshot;

        public SnapshotCommandResult() {
        }

        public SnapshotCommandResult(DatabaseSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        public SnapshotCommandResult(DatabaseSnapshot snapshot, String formattedSnapshot) {
            this(snapshot);
            this.formattedSnapshot = formattedSnapshot;
        }
    }
}
