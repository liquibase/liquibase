package liquibase.parser.visitor;

import liquibase.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;

public class UpdateSqlVisitor extends UpdateVisitor {

    public UpdateSqlVisitor(Database database) {
        super(database);
    }

    public void visit(ChangeSet changeSet) throws LiquibaseException {
        super.visit(changeSet);
    }
}