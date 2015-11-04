package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.SnapshotDatabaseObjectsAction;
import liquibase.actionlogic.ActionLogic;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;
import liquibase.structure.core.*;
import liquibase.util.CollectionUtil;

public class SnapshotTablesLogicOffline extends AbstractSnapshotDatabaseObjectsLogicOffline implements ActionLogic.InteractsExternally {

    @Override
    protected Class<? extends DatabaseObject> getTypeToSnapshot() {
        return Table.class;
    }

    @Override
    protected Class<? extends DatabaseObject>[] getSupportedRelatedTypes() {
        return new Class[]{
                Schema.class,
                Catalog.class,
                Table.class
        };
    }

    @Override
    public boolean interactsExternally(Action action, Scope scope) {
        return true;
    }

    @Override
    protected CollectionUtil.CollectionFilter<? extends DatabaseObject> getDatabaseObjectFilter(SnapshotDatabaseObjectsAction action, Scope scope) {
        final DatabaseObject relatedTo = action.relatedTo;

        return new CollectionUtil.CollectionFilter<Relation>() {
            @Override
            public boolean include(Relation relation) {
                if (relatedTo instanceof Relation) {
                    return relation.name.equals(relatedTo);
                } else if (relatedTo instanceof Schema) {
                    return relation.name.container.equals(relatedTo);
                } else if (relatedTo instanceof Catalog) {
                    ObjectName schemaName = relation.name.container;

                    return schemaName.container != null && schemaName.container.equals(relatedTo);
                } else {
                    throw new UnexpectedLiquibaseException("Unexpected relatedTo type: "+relatedTo.getClass().getName());
                }
            }
        };

    }
}
