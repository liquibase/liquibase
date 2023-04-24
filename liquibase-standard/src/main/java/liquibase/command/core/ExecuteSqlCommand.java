package liquibase.command.core;

import liquibase.command.*;
import liquibase.database.Database;

/**
 * @deprecated Implement commands with {@link liquibase.command.CommandStep} and call them with {@link liquibase.command.CommandFactory#getCommandDefinition(String...)}.
 */
public class ExecuteSqlCommand extends AbstractCommand {

    private Database database;
    private String sql;
    private String sqlFile;
    private String delimiter = ";";

    @Override
    public String getName() {
        return "executeSql";
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getSqlFile() {
        return sqlFile;
    }

    public void setSqlFile(String sqlFile) {
        this.sqlFile = sqlFile;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public CommandValidationErrors validate() {
        return new CommandValidationErrors(this);
    }

    @Override
    public CommandResult run() throws Exception {
        final CommandScope commandScope = new CommandScope("internalExecuteSql");
        commandScope.addArgumentValue(InternalExecuteSqlCommandStep.DATABASE_ARG, this.getDatabase());
        commandScope.addArgumentValue(InternalExecuteSqlCommandStep.SQL_ARG, getSql());
        commandScope.addArgumentValue(InternalExecuteSqlCommandStep.SQLFILE_ARG, getSqlFile());
        commandScope.addArgumentValue(InternalExecuteSqlCommandStep.DELIMITER_ARG, this.delimiter);

        final CommandResults results = commandScope.execute();

        return new CommandResult((String) results.getResult("output"));
    }

}
