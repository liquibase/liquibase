package liquibase.snapshot.core;

import liquibase.Scope;
import liquibase.snapshot.Snapshot;
import liquibase.snapshot.SnapshotRelateLogic;
import liquibase.structure.core.Column;
import liquibase.structure.core.Relation;

public class ColumnRelateLogic implements SnapshotRelateLogic {

    @Override
    public boolean supports(Scope scope) {
        return scope.getDatabase() != null;
    }

    @Override
    public void relate(Snapshot snapshot) {
        for (Column column : snapshot.get(Column.class)) {
            Relation relation = column.getRelation();
            if (relation != null) {
                Relation realRelation = snapshot.get(relation);
                column.setRelation(realRelation);
                realRelation.add(Relation.Attr.columns, column);
            }
        }
    }
}
