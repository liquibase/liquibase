package liquibase.command.core;

import liquibase.Scope;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawSqlStatement;
import liquibase.util.FileUtil;
import liquibase.util.StringUtil;

import java.io.File;
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
    public String[] getName() {
        return COMMAND_NAME;
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
            File file = new File(sqlFile);
            if (! file.exists()){
              throw new LiquibaseException(String.format("The file '%s' does not exist", file.getCanonicalPath()));
            }
            sqlText = FileUtil.getContents(file);
        }

        String out = "";
        String[] sqlStrings = StringUtil.processMutliLineSQL(sqlText, true, true, commandScope.getArgumentValue(DELIMITER_ARG));
        for (String sqlString : sqlStrings) {
            if (sqlString.toLowerCase().matches("\\s*select .*")) {
                List<Map<String, ?>> rows = executor.queryForList(new RawSqlStatement(sqlString));
                out += "Output of "+sqlString+":\n";
                if (rows.isEmpty()) {
                    out += "-- Empty Resultset --\n";
                } else {
                    SortedSet<String> keys = new TreeSet<>();
                    for (Map<String, ?> row : rows) {
                        keys.addAll(row.keySet());
                    }
                    out += StringUtil.join(keys, " | ")+" |\n";

                    for (Map<String, ?> row : rows) {
                        for (String key : keys) {
                            out += row.get(key)+" | ";
                        }
                        out += "\n";
                    }
                }
            } else {
                executor.execute(new RawSqlStatement(sqlString));
                out += "Successfully Executed: "+ sqlString+"\n";
            }
            out += "\n";
        }
        database.commit();
        resultsBuilder.addResult("output", out.trim());
        // Scope.getCurrentScope().getUI().sendMessage(out.trim());
    }

}
