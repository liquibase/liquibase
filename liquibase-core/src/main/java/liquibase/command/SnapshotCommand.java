package liquibase.command;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.serializer.SnapshotSerializerFactory;
import liquibase.snapshot.*;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SnapshotCommand extends AbstractCommand {

    private Database database;
    private CatalogAndSchema[] schemas;
    private String serializerFormat = "txt";
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

    public SnapshotCommand setSchemas(CatalogAndSchema... catalogAndSchema) {
        schemas = catalogAndSchema;
        return this;
    }

    public SnapshotCommand setSchemas(String... schemas) {
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

    public SnapshotCommand setSerializerFormat(String serializerFormat) {
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
    protected Object run() throws Exception {
        SnapshotControl snapshotControl = new SnapshotControl(database);
        snapshotControl.setSnapshotListener(snapshotListener);

        CatalogAndSchema[] schemas = this.schemas;
        if (schemas == null) {
            schemas = new CatalogAndSchema[]{database.getDefaultSchema()};
        }
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(schemas, database, snapshotControl);

        return SnapshotSerializerFactory.getInstance().getSerializer(getSerializerFormat()).serialize(snapshot, true);
    }

    @Override
    public CommandValidationErrors validate() {
        return new CommandValidationErrors(this);
    }
}
