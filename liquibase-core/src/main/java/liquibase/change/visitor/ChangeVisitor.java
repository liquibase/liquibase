package liquibase.change.visitor;

import liquibase.changelog.ChangeLogChild;

public interface ChangeVisitor extends ChangeLogChild {

    String getName();

    String getChange();

    String getDbms();

}
