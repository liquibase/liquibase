package liquibase.sqlgenerator.core;

import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.SqlStatement;
import liquibase.util.StringClauses;

public abstract class AbstractStoLoSqlGenerator<StatementType extends SqlStatement> extends AbstractSqlGenerator<StatementType> implements SqlGenerator<StatementType> {

    protected static void replaceCreateByAlterIfNotCreateOrReplaceStatement(StringClauses parsedProcedureDefinition) {
        if (!isCreateOrAlterStatement(parsedProcedureDefinition) && parsedProcedureDefinition.contains("CREATE")) {
            replaceCreateByAlter(parsedProcedureDefinition);
        }
    }

    protected static boolean isCreateOrAlterStatement(StringClauses definition) {
        StringClauses.ClauseIterator procedureClauseIterator = definition.getClauseIterator();
        Object next = "START";
        while (next != null && !(next.toString().equalsIgnoreCase("create") || next.toString().equalsIgnoreCase("alter")) && procedureClauseIterator.hasNext()) {
            next = procedureClauseIterator.nextNonWhitespace();
            if ((procedureClauseIterator.hasNext() && procedureClauseIterator.nextNonWhitespace().toString().equalsIgnoreCase("or"))
                    && (procedureClauseIterator.hasNext() && procedureClauseIterator.nextNonWhitespace().toString().equalsIgnoreCase("alter"))) {
                return true;
            }
        }
        return false;
    }

    protected static void replaceCreateByAlter(StringClauses definition) {
        StringClauses.ClauseIterator clauseIterator = definition.getClauseIterator();
        Object next = "START";
        while (next != null
                && !(next.toString().equalsIgnoreCase("create") || next.toString().equalsIgnoreCase("alter"))
                && clauseIterator.hasNext()) {
            next = clauseIterator.nextNonWhitespace();
        }
        clauseIterator.replace("ALTER");
    }
}
