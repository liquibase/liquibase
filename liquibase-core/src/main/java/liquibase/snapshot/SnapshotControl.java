package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.DatabaseObjectFactory;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

public class SnapshotControl {

    private Set<Class<? extends DatabaseObject>> types;

    public SnapshotControl(Database database) {
        setTypes(DatabaseObjectFactory.getInstance().getStandardTypes(), database);
    }

    public SnapshotControl(Database database, Class<? extends DatabaseObject>... types) {
        if (types == null || types.length == 0) {
            setTypes(DatabaseObjectFactory.getInstance().getStandardTypes(), database);
        } else {
            setTypes(new HashSet<Class<? extends DatabaseObject>>(Arrays.asList(types)), database);
        }
    }

    public SnapshotControl(Database database, String types) {
        setTypes(DatabaseObjectFactory.getInstance().parseTypes(types), database);
    }

    private void setTypes(Set<Class<? extends DatabaseObject>> types, Database database) {
        this.types = new HashSet<Class<? extends DatabaseObject>>();
        for (Class<? extends DatabaseObject> type : types) {
            this.types.addAll(SnapshotGeneratorFactory.getInstance().getContainerTypes(type, database));
        }
    }

    public Set<Class<? extends DatabaseObject>> getTypesToInclude() {
        return types;
    }

    public boolean shouldInclude(Class<? extends DatabaseObject> type) {
        return type.equals(Catalog.class) || type.equals(Schema.class) || types.contains(type);
    }
}
