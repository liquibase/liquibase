package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.core.ForeignKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddForeignKeysAction extends AbstractAction {

    public List<ForeignKey> foreignKeys = new ArrayList<>();

    public AddForeignKeysAction() {
    }

    public AddForeignKeysAction(ForeignKey... foreignKeys) {
        if (foreignKeys != null) {
            this.foreignKeys.addAll(Arrays.asList(foreignKeys));
        }
    }
}