package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.SnapshotDatabaseObjectsAction;
import liquibase.actionlogic.ActionLogic;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Schema;
import liquibase.util.CollectionUtil;

public class SnapshotPrimaryKeysLogicOffline extends AbstractSnapshotDatabaseObjectsLogicOffline implements ActionLogic.InteractsExternally {

    @Override
    protected Class<? extends DatabaseObject> getTypeToSnapshot() {
        return PrimaryKey.class;
    }

    @Override
    protected Class<? extends DatabaseObject>[] getSupportedRelatedTypes() {
        return new Class[]{
                PrimaryKey.class,
                Relation.class,
                Schema.class,
                Catalog.class
        };
    }

    @Override
    public boolean interactsExternally(Action action, Scope scope) {
        return true;
    }

    protected CollectionUtil.CollectionFilter<? extends DatabaseObject> getDatabaseObjectFilter(SnapshotDatabaseObjectsAction action, Scope scope) {
        final ObjectReference relatedTo = action.relatedTo;

        return new CollectionUtil.CollectionFilter<PrimaryKey>() {
            @Override
            public boolean include(PrimaryKey primarykey) {
                if (relatedTo.instanceOf(PrimaryKey.class)) {
                    return primarykey.getName().equals(relatedTo.name);
                } else if (relatedTo.instanceOf(Relation.class)) {
                    ObjectReference tableName = primarykey.getTableName();
                    return tableName != null && tableName.equals((relatedTo));
                } else if (relatedTo.instanceOf(Schema.class)) {
                    ObjectReference tableName = primarykey.container;
                    return tableName != null && tableName.container != null && tableName.container.equals((relatedTo.container));
                } else if (relatedTo.instanceOf(Catalog.class)) {
                    ObjectReference tableName = primarykey.container;
                    if (tableName == null || tableName.container == null) {
                        return false;
                    }
                    ObjectReference schemaName = tableName.container;

                    return schemaName.container != null && schemaName.container.equals((relatedTo.container));
                } else {
                    throw new UnexpectedLiquibaseException("Unexpected relatedTo type: "+relatedTo.getClass().getName());
                }
            }
        };

    }
}