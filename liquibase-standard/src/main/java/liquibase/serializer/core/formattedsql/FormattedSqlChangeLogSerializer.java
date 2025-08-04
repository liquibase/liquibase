package liquibase.serializer.core.formattedsql;

import liquibase.*;
import liquibase.change.AbstractChange;
import liquibase.change.AbstractSQLChange;
import liquibase.ContextExpression;
import liquibase.GlobalConfiguration;
import liquibase.Labels;
import liquibase.Scope;
import liquibase.change.Change;
import liquibase.changelog.ChangeLogChild;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.OracleDatabase;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import org.apache.tools.ant.taskdefs.condition.Or;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormattedSqlChangeLogSerializer  implements ChangeLogSerializer {

    private static final String SQL_FILE_NAME_REGEX = ".*\\.(\\w+)\\.sql";
    private static final Pattern SQL_FILE_NAME_PATTERN = Pattern.compile(SQL_FILE_NAME_REGEX);


    @Override
    public String[] getValidFileExtensions() {
        return new String[] {
                "sql"
        };
    }

    @Override
    public String serialize(LiquibaseSerializable object, boolean pretty) {
        if (object instanceof ChangeSet) {
            //
            // If there is a Database object in the current scope, then use it for serialization
            //
            ChangeSet changeSet = (ChangeSet) object;
            Database database = Scope.getCurrentScope().get(DiffToChangeLog.DIFF_SNAPSHOT_DATABASE, Database.class);
            if (database == null) {
                database = getTargetDatabase(changeSet);
            }

            StringBuilder builder = new StringBuilder();
            createChangeSetInfo(changeSet, builder);
            for (Change change : changeSet.getChanges()) {
                Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(change.generateStatements(database), database);
                if (sqls != null) {
                    if (sqls.length > 1) {
                        builder = new StringBuilder(builder.toString().replace(" splitStatements:false", " splitStatements:true"));
                    } else if (database instanceof OracleDatabase) {
                        //
                        // Handle Oracle differently because setting splitStatements:true on a statement
                        // that has an endDelimiter will cause invalid syntax
                        //
                        builder = new StringBuilder(builder.toString().replace(" splitStatements:false", ""));
                    }
                    for (Sql sql : sqls) {
                        builder.append(sql.toSql().endsWith(sql.getEndDelimiter()) ? sql.toSql() : sql.toSql() + sql.getEndDelimiter()).append("\n");
                    }
                }
            }

            return builder.toString();
        } else {
            throw new UnexpectedLiquibaseException("Cannot serialize object type: "+object.getClass().getName());
        }
    }

    /**
     *
     * Create the changeSet header information and add it to the StringBuilder
     *
     * @param  changeSet    The ChangeSet we are emitting
     * @param  builder      The current StringBuilder we will add to
     *
     */
    public void createChangeSetInfo(ChangeSet changeSet, StringBuilder builder) {
        String author = (changeSet.getAuthor()).replaceAll("\\s+", "_");
        author = author.replace("_(generated)", "");
        builder.append("-- changeset ").append(author).append(":").append(changeSet.getId());
        Labels labels = changeSet.getLabels();
        if (labels != null && ! labels.isEmpty()) {
            String outputLabels = labels.toString();
            builder.append(" labels:\"");
            builder.append(outputLabels);
            builder.append("\"");
        }
        ContextExpression contexts = changeSet.getContextFilter();
        if (contexts != null && ! contexts.isEmpty()) {
            String outputContexts = contexts.toString();
            builder.append(" contextFilter:\"");
            builder.append(outputContexts);
            builder.append("\"");
        }
        String logicalFilePath = changeSet.getLogicalFilePath();
        if (logicalFilePath != null && ! logicalFilePath.isEmpty()) {
            builder.append(" logicalFilePath:\"");
            builder.append(logicalFilePath);
            builder.append("\"");
        }
        builder.append(" splitStatements:" + getSplitStatementsValue(changeSet));
        builder.append("\n");
    }

    public boolean getSplitStatementsValue(ChangeSet changeSet) {
        return false;
    }

    protected Database getTargetDatabase(ChangeSet changeSet) {
        final String shortName = getShortName(changeSet);

        Database database = DatabaseFactory.getInstance().getDatabase(shortName);

        if (database == null) {
            List<Database> databases = DatabaseFactory.getInstance().getImplementedDatabases();
            StringBuilder availableDbs = new StringBuilder();
            availableDbs.append("Available database short names for serialization:\n");
            for (Database db : databases) {
                availableDbs.append("  ").append(db.getShortName()).append("\n");
            }
            throw new UnexpectedLiquibaseException("Serializing changelog as sql requires a file name in the format *.databaseType.sql. Example: changelog.h2.sql. Unknown database type: "+shortName +"\n" + availableDbs);
        }

        return database;
    }

    private static String getShortName(ChangeSet changeSet) {
        String filePath = changeSet.getFilePath();
        if (filePath == null) {
            throw new UnexpectedLiquibaseException("You must specify the changelog file name as filename.DB_TYPE.sql. Example: changelog.mysql.sql");
        }
        Matcher matcher = SQL_FILE_NAME_PATTERN.matcher(filePath);
        if (!matcher.matches()) {
            throw new UnexpectedLiquibaseException("Serializing changelog as sql requires a file name in the format *.databaseType.sql. Example: changelog.h2.sql. Passed: "+filePath);
        }
        return matcher.replaceFirst("$1");
    }

    @Override
    public <T extends ChangeLogChild> void write(List<T> children, OutputStream out) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("-- liquibase formatted sql\n\n");

        for (T child : children) {
            builder.append(serialize(child, true));
            builder.append("\n");
        }

        out.write(builder.toString().getBytes(GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()));

    }

    @Override
    public void append(ChangeSet changeSet, File changeLogFile) throws IOException {

    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
}
