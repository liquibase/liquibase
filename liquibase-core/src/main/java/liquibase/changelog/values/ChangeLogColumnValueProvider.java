package liquibase.changelog.values;

import liquibase.ContextExpression;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.sqlgenerator.core.MarkChangeSetRanGenerator;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.core.MarkChangeSetRanStatement;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StringUtils;

public interface ChangeLogColumnValueProvider {
    Object getValue(MarkChangeSetRanStatement statement, Database database) throws LiquibaseException;

    class IdProvider implements ChangeLogColumnValueProvider {
        @Override
        public Object getValue(MarkChangeSetRanStatement statement, Database database) {
            return statement.getChangeSet().getId();
        }
    }

    class AuthorProvider implements ChangeLogColumnValueProvider {
        @Override
        public Object getValue(MarkChangeSetRanStatement statement, Database database) {
            return statement.getChangeSet().getAuthor();
        }
    }

    class FileNameProvider implements ChangeLogColumnValueProvider {
        @Override
        public Object getValue(MarkChangeSetRanStatement statement, Database database) {
            return statement.getChangeSet().getFilePath();
        }
    }

    class DateExecutedProvider implements ChangeLogColumnValueProvider {
        @Override
        public Object getValue(MarkChangeSetRanStatement statement, Database database) {
            String dateValue = database.getCurrentDateTimeFunction();
            return new DatabaseFunction(dateValue);
        }
    }

    class OrderExecutedProvider implements ChangeLogColumnValueProvider {
        @Override
        public Object getValue(MarkChangeSetRanStatement statement, Database database) throws LiquibaseException {
            return ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).getNextSequenceValue();
        }
    }

    class MD5SUMProvider implements ChangeLogColumnValueProvider {
        @Override
        public Object getValue(MarkChangeSetRanStatement statement, Database database) throws LiquibaseException {
            return statement.getChangeSet().generateCheckSum().toString();
        }
    }

    class DescriptionProvider implements ChangeLogColumnValueProvider {
        @Override
        public Object getValue(MarkChangeSetRanStatement statement, Database database) throws LiquibaseException {
            return limitSize(statement.getChangeSet().getDescription());
        }


        private String limitSize(String string) {
            int maxLength = 250;
            if (string.length() > maxLength) {
                return string.substring(0, maxLength - 3) + "...";
            }
            return string;
        }
    }

    class CommentsProvider implements ChangeLogColumnValueProvider {
        @Override
        public Object getValue(MarkChangeSetRanStatement statement, Database database) throws LiquibaseException {
            return limitSize(StringUtils.trimToEmpty(statement.getChangeSet().getComments()));
        }


        private String limitSize(String string) {
            int maxLength = 250;
            if (string.length() > maxLength) {
                return string.substring(0, maxLength - 3) + "...";
            }
            return string;
        }
    }

    class ExecTypeProvider implements ChangeLogColumnValueProvider {
        @Override
        public Object getValue(MarkChangeSetRanStatement statement, Database database) throws LiquibaseException {
            return statement.getExecType().value;
        }
    }

    class ContextsProvider implements ChangeLogColumnValueProvider {
        @Override
        public Object getValue(MarkChangeSetRanStatement statement, Database database) throws LiquibaseException {
            ChangeSet changeSet = statement.getChangeSet();
            return ((changeSet.getContexts() == null) || changeSet.getContexts().isEmpty()) ? null : buildFullContext(changeSet);
        }

        private String buildFullContext(ChangeSet changeSet) {
            StringBuilder contextExpression = new StringBuilder();
            boolean notFirstContext = false;
            for (ContextExpression inheritableContext : changeSet.getInheritableContexts()) {
                appendContext(contextExpression, inheritableContext.toString(), notFirstContext);
                notFirstContext = true;
            }
            ContextExpression changeSetContext = changeSet.getContexts();
            if ((changeSetContext != null) && !changeSetContext.isEmpty()) {
                appendContext(contextExpression, changeSetContext.toString(), notFirstContext);
            }
            return contextExpression.toString();
        }

        private void appendContext(StringBuilder contextExpression, String contextToAppend, boolean notFirstContext) {
            boolean complexExpression = contextToAppend.contains(MarkChangeSetRanGenerator.COMMA) || contextToAppend.contains(MarkChangeSetRanGenerator.WHITESPACE);
            if (notFirstContext) {
                contextExpression.append(MarkChangeSetRanGenerator.AND);
            }
            if (complexExpression) {
                contextExpression.append(MarkChangeSetRanGenerator.OPEN_BRACKET);
            }
            contextExpression.append(contextToAppend);
            if (complexExpression) {
                contextExpression.append(MarkChangeSetRanGenerator.CLOSE_BRACKET);
            }
        }
    }

    class LabelsProvider implements ChangeLogColumnValueProvider {
        @Override
        public Object getValue(MarkChangeSetRanStatement statement, Database database) throws LiquibaseException {
            ChangeSet changeSet = statement.getChangeSet();

            return ((changeSet.getLabels() == null) || changeSet.getLabels().isEmpty() ) ? null : changeSet.getLabels().toString();
        }
    }

    class LiquibaseVersionProvider implements ChangeLogColumnValueProvider {
        @Override
        public Object getValue(MarkChangeSetRanStatement statement, Database database) throws LiquibaseException {
           return LiquibaseUtil.getBuildVersion().replaceAll("SNAPSHOT", "SNP");
        }
    }

    class DeploymentIdProvider implements ChangeLogColumnValueProvider {
        @Override
        public Object getValue(MarkChangeSetRanStatement statement, Database database) throws LiquibaseException {
           return ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).getDeploymentId();
        }
    }
}
