package liquibase.action.core;

import liquibase.action.AbstractAction;

public class AddForeignKeyConstraintAction extends AbstractAction {

    public static enum Attr {
        baseTableCatalogName,
        baseTableSchemaName,
        baseTableName,
        baseColumnNames,

        referencedTableCatalogName,
        referencedTableSchemaName,
        referencedTableName,
        referencedColumnNames,

        constraintName,

        deferrable,
        initiallyDeferred,

        onDelete,
        onUpdate,
    }

    public AddForeignKeyConstraintAction() {
    }

    public AddForeignKeyConstraintAction(String constraintName, String baseTableCatalogName, String baseTableSchemaName, String baseTableName, String[] baseColumnNames, String referencedTableCatalogName, String referencedTableSchemaName, String referencedTableName, String[] referencedColumnNames) {
        set(Attr.constraintName, constraintName);
        set(Attr.baseTableCatalogName, baseTableCatalogName);
        set(Attr.baseTableSchemaName, baseTableSchemaName);
        set(Attr.baseTableName, baseTableName);
        set(Attr.baseColumnNames, baseColumnNames);
        set(Attr.referencedTableCatalogName, referencedTableCatalogName);
        set(Attr.referencedTableSchemaName, referencedTableSchemaName);
        set(Attr.referencedTableName, referencedTableName);
        set(Attr.referencedColumnNames, referencedColumnNames);


    }
}