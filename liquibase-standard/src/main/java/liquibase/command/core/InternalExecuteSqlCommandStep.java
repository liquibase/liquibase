package liquibase.command.core;

import liquibase.Scope;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.resource.PathHandlerFactory;
import liquibase.resource.Resource;
import liquibase.statement.core.RawSqlStatement;
import liquibase.util.FileUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtil;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class InternalExecuteSqlCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"internalExecuteSql"};

    public static final CommandArgumentDefinition<Database> DATABASE_ARG;
    public static final CommandArgumentDefinition<String> SQL_ARG;
    public static final CommandArgumentDefinition<String> SQLFILE_ARG;
    public static final CommandArgumentDefinition<String> DELIMITER_ARG;

    static {
        final CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        DATABASE_ARG = builder.argument("database", Database.class).required().build();
        SQL_ARG = builder.argument("sql", String.class).build();
        SQLFILE_ARG = builder.argument("sqlFile", String.class).build();
        DELIMITER_ARG = builder.argument("delimiter", String.class).defaultValue(";").build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        super.adjustCommandDefinition(commandDefinition);
        commandDefinition.setInternal(true);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        Database database = commandScope.getArgumentValue(DATABASE_ARG);
        String sql = commandScope.getArgumentValue(SQL_ARG);
        String sqlFile = commandScope.getArgumentValue(SQLFILE_ARG);

        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        String sqlText;
        if (sqlFile == null) {
            sqlText = sql;
        } else {
            final PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);
            Resource resource = pathHandlerFactory.getResource(sqlFile);
            if (!resource.exists()){
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
        resultsBuilder.addResult("output", out.toString().trim());
        // Scope.getCurrentScope().getUI().sendMessage(out.trim());
    }

}
