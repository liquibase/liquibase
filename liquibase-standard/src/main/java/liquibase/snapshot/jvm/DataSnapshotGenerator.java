package liquibase.snapshot.jvm;

import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Data;
import liquibase.structure.core.Table;

public class DataSnapshotGenerator extends JdbcSnapshotGenerator {

    public DataSnapshotGenerator() {
        super(Data.class, new Class[]{Table.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        return example;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(Data.class)) {
            return;
        }
        if (foundObject instanceof Table) {
            Table table = (Table) foundObject;
            try {

                Data exampleData = new Data().setTable(table);
                table.setAttribute("data", exampleData);
            } catch (Exception e) {
                throw new DatabaseException(e);
            }
        }
    }
}
