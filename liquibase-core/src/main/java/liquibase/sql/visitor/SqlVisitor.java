package liquibase.sql.visitor;

import java.util.Set;

import liquibase.Contexts;
import liquibase.change.CheckSum;
import liquibase.database.Database;
import liquibase.serializer.LiquibaseSerializable;

public interface SqlVisitor extends LiquibaseSerializable {

    String modifySql(String sql, Database database);

    String getName();

    Set<String> getApplicableDbms();

    void setApplicableDbms(Set<String> modifySqlDbmsList);

    void setApplyToRollback(boolean applyOnRollback);

    boolean isApplyToRollback();

    Contexts getContexts();

    void setContexts(Contexts contexts);

    CheckSum generateCheckSum();

}
