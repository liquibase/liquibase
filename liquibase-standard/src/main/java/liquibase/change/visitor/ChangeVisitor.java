package liquibase.change.visitor;

import liquibase.changelog.ChangeLogChild;

import java.util.Set;

public interface ChangeVisitor extends ChangeLogChild {

    String getName();

    String getChange();

    Set<String> getDbms();

}
