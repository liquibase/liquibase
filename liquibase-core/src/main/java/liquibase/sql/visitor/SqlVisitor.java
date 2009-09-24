package liquibase.sql.visitor;

import liquibase.database.Database;

import java.util.Collection;
import java.util.Set;

public interface SqlVisitor {

    String modifySql(String sql, Database database);

    String getTagName();

    Set<String> getApplicableDbms();

    void setApplicableDbms(Set<String> modifySqlDbmsList);

    void setApplyToRollback(boolean applyOnRollback);

    boolean isApplyToRollback();

    Set<String> getContexts();

    void setContexts(Set<String> contexts);

}
