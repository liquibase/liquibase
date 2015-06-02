package liquibase.action.core;

import liquibase.action.AbstractAction;

public class DropForeignKeyConstraintAction extends AbstractAction {
    
    public static enum Attr {
        baseTableName,
        constraintName,
    }
}
