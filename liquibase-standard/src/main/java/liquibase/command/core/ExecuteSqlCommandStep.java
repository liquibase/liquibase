package liquibase.command.core;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.changeset.ChangeSetService;
import liquibase.changeset.ChangeSetServiceFactory;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.lockservice.LockService;
import liquibase.resource.PathHandlerFactory;
import liquibase.resource.Resource;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.util.FileUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtil;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

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
        return Arrays.asList(Database.class, LockService.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        final CommandScope commandScope = resultsBuilder.getCommandScope();
        final Database database = (Database) commandScope.getDependency(Database.class);
        final String sql = commandScope.getArgumentValue(SQL_ARG);
        final String sqlFile = commandScope.getArgumentValue(SQLFILE_ARG);
        final Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        final String sqlText = getSqlScript(sql, sqlFile);
        final StringBuilder out = new StringBuilder();
        final String[] sqlStrings = StringUtil.processMultiLineSQL(sqlText, true, true, determineEndDelimiter(commandScope), null);

        for (String sqlString : sqlStrings) {
            if (sqlString.toLowerCase().matches("\\s*select .*")) {
                out.append(handleSelect(sqlString, executor));
            } else {
                executor.execute(new RawParameterizedSqlStatement(sqlString));
                out.append("Successfully Executed: ").append(sqlString).append("\n");
            }
            out.append("\n");
        }

        database.commit();
        handleOutput(resultsBuilder, out.toString());
        resultsBuilder.addResult("output", out.toString());
    }

    protected static String determineEndDelimiter(CommandScope commandScope) {
        String delimiter = commandScope.getArgumentValue(DELIMITER_ARG);
        if (delimiter == null) {
            ChangeSetService service = ChangeSetServiceFactory.getInstance().createChangeSetService();
            delimiter = service.getEndDelimiter(null);
        }
        return delimiter;
    }

    protected void handleOutput(CommandResultsBuilder resultsBuilder, String output) throws IOException {
        String charsetName = GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue();
        Writer outputWriter = new OutputStreamWriter(resultsBuilder.getOutputStream(), charsetName);
        outputWriter.write(output);
        outputWriter.flush();
    }

    protected String getSqlScript(String sql, String sqlFile) throws IOException, LiquibaseException {
        if (sqlFile == null) {
            return sql;
        } else {
            final PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);
            Resource resource = pathHandlerFactory.getResource(sqlFile);
            if (!resource.exists()) {
                throw new LiquibaseException(FileUtil.getFileNotFoundMessage(sqlFile));
            }
            return StreamUtil.readStreamAsString(resource.openInputStream());
        }
    }

    private String handleSelect(String sqlString, Executor executor) throws DatabaseException {
        StringBuilder out = new StringBuilder();
        List<Map<String, ?>> rows = executor.queryForList(new RawParameterizedSqlStatement(sqlString));
        out.append("Output of ").append(sqlString).append(":\n");
        if (rows.isEmpty()) {
            out.append("-- Empty Resultset --\n");
        } else {
            LinkedHashSet<String> keys = new LinkedHashSet<>();
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
        return out.toString();
    }
}
