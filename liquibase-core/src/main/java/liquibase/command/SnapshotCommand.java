package liquibase.command;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.serializer.SnapshotSerializerFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.DatabaseObject;

import java.util.ArrayList;
import java.util.List;

public class SnapshotCommand extends AbstractCommand {

    private Database database;
    private List<DatabaseObject> examples = new ArrayList<DatabaseObject>();
    private List<CatalogAndSchema> catalogs = new ArrayList<CatalogAndSchema>();
    private String serializerFormat = "txt";

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

    public void addExample(CatalogAndSchema catalogAndSchema) {
        catalogs.add(catalogAndSchema);
    }

    public void addExample(DatabaseObject example) {
        examples.add(example);
    }

    public String getSerializerFormat() {
        return serializerFormat;
    }

    public void setSerializerFormat(String serializerFormat) {
        this.serializerFormat = serializerFormat;
    }


    @Override
    protected Object run() throws Exception {
        SnapshotControl snapshotControl = new SnapshotControl(database);

        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(catalogs.toArray(new CatalogAndSchema[catalogs.size()]), database, snapshotControl);

        return SnapshotSerializerFactory.getInstance().getSerializer(getSerializerFormat()).serialize(snapshot, true);
    }

    @Override
    public CommandValidationErrors validate() {
        return new CommandValidationErrors(this);
    }
}
