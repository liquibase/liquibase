package liquibase.snapshot.core;

import liquibase.Scope;
import liquibase.snapshot.Snapshot;
import liquibase.snapshot.SnapshotRelateLogic;
import liquibase.structure.core.Column;
import liquibase.structure.core.Relation;
import liquibase.util.CollectionUtil;

public class ColumnRelateLogic implements SnapshotRelateLogic {

    @Override
    public boolean supports(Scope scope) {
        return scope.getDatabase() != null;
    }

    @Override
    public void relate(Snapshot snapshot) {
        for (Column column : snapshot.get(Column.class)) {
            Relation relation = column.relation;
            if (relation != null) {
                Relation realRelation = snapshot.get(relation);
                column.relation = realRelation;
                realRelation.columns = CollectionUtil.createIfNull(realRelation.columns);
                realRelation.columns.add(column);
            }
        }
    }
}
