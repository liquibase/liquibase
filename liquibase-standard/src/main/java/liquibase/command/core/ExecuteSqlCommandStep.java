package liquibase.command.core;

import liquibase.Scope;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.lockservice.LockService;
import liquibase.resource.PathHandlerFactory;
import liquibase.resource.Resource;
import liquibase.statement.core.RawSqlStatement;
import liquibase.util.FileUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtil;

import java.io.Writer;
import java.util.*;

public class ExecuteSqlCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"executeSql"};
    public static final CommandArgumentDefinition<String> SQL_ARG;
    public static final CommandArgumentDefinition<String> SQLFILE_ARG;
    public static final CommandArgumentDefinition<String> DELIMITER_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        SQL_ARG = builder.argument("sql", String.class)
                .description("SQL string to execute").build();
        SQLFILE_ARG = builder.argument("sqlFile", String.class)
                .description("SQL script to execute").build();
        DELIMITER_ARG = builder.argument("delimiter", String.class)
                .description("Delimiter to use when executing SQL script").build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Execute a SQL string or file");
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class, LockService.class, DatabaseChangeLog.class, Writer.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        final CommandScope commandScope = resultsBuilder.getCommandScope();
        final Database database = (Database) commandScope.getDependency(Database.class);
        final String sql = commandScope.getArgumentValue(SQL_ARG);
        final String sqlFile = commandScope.getArgumentValue(SQLFILE_ARG);
        final Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);

        String sqlText;
        if (sqlFile == null) {
            sqlText = sql;
        } else {
            final PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);
            Resource resource = pathHandlerFactory.getResource(sqlFile);
            if (!resource.exists()) {
                throw new LiquibaseException(FileUtil.getFileNotFoundMessage(sqlFile));
            }
            sqlText = StreamUtil.readStreamAsString(resource.openInputStream());
        }

        final StringBuilder out = new StringBuilder();
        String[] sqlStrings = StringUtil.processMultiLineSQL(sqlText, true, true, commandScope.getArgumentValue(DELIMITER_ARG));
        for (String sqlString : sqlStrings) {
            if (sqlString.toLowerCase().matches("\\s*select .*")) {
                List<Map<String, ?>> rows = executor.queryForList(new RawSqlStatement(sqlString));
                out.append("Output of ").append(sqlString).append(":\n");
                if (rows.isEmpty()) {
                    out.append("-- Empty Resultset --\n");
                } else {
                    SortedSet<String> keys = new TreeSet<>();
                    for (Map<String, ?> row : rows) {
                        keys.addAll(row.keySet());
                    }
                    out.append(StringUtil.join(keys, " | ")).append(" |\n");

                    for (Map<String, ?> row : rows) {
                        for (String key : keys) {
                            out.append(row.get(key)).append(" | ");
                        }
                        out.append("\n");
                    }
                }
            } else {
                executor.execute(new RawSqlStatement(sqlString));
                out.append("Successfully Executed: ").append(sqlString).append("\n");
            }
            out.append("\n");
        }
        database.commit();
        Writer outputWriter = (Writer) commandScope.getDependency(Writer.class);
        outputWriter.write(out.toString());
        outputWriter.flush();
        resultsBuilder.addResult("output", out.toString());
    }
}
