package liquibase.sql.visitor;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.change.CheckSum;
import liquibase.database.Database;
import liquibase.serializer.LiquibaseSerializable;

import java.util.Set;

public interface SqlVisitor extends LiquibaseSerializable {

    String modifySql(String sql, Database database);

    String getName();

    Set<String> getApplicableDbms();

    void setApplicableDbms(Set<String> modifySqlDbmsList);

    void setApplyToRollback(boolean applyOnRollback);

    boolean isApplyToRollback();

    ContextExpression getContexts();

    void setContexts(ContextExpression contexts);

    Labels getLabels();
    void setLabels(Labels labels);

    CheckSum generateCheckSum();

}
