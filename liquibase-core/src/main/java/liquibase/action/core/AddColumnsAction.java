package liquibase.action.core;

import liquibase.Scope;
import liquibase.action.AbstractAction;
import liquibase.action.ActionStatus;
import liquibase.exception.ActionPerformException;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotFactory;
import liquibase.structure.ObjectName;
import liquibase.structure.core.*;
import liquibase.util.ObjectUtil;

import java.util.ArrayList;
import java.util.List;

public class AddColumnsAction extends AbstractAction {

    public List<Column> columns = new ArrayList<>();
    public PrimaryKey primaryKey;
    public List<UniqueConstraint> uniqueConstraints = new ArrayList<>();
    public List<ForeignKey> foreignKeys = new ArrayList<>();

}
