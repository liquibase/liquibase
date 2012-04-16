package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.SybaseDatabase;
import liquibase.database.typeconversion.TypeConverter;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.statement.core.CreateTableStatement;

public class CreateDatabaseChangeLogTableGenerator extends AbstractSqlGenerator<CreateDatabaseChangeLogTableStatement> {

    @Override
    public boolean supports(CreateDatabaseChangeLogTableStatement statement, Database database) {
        return (!(database instanceof SybaseDatabase));
    }

    public ValidationErrors validate(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        TypeConverter typeConverter = TypeConverterFactory.getInstance().findTypeConverter(database);
        CreateTableStatement createTableStatement = new CreateTableStatement(database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                .addPrimaryKeyColumn("ID", typeConverter.getDataType("VARCHAR("+getIdColumnSize()+")", false), null, null, null,new NotNullConstraint())
                .addPrimaryKeyColumn("AUTHOR", typeConverter.getDataType("VARCHAR("+getAuthorColumnSize()+")", false), null, null, null,new NotNullConstraint())
                .addPrimaryKeyColumn("FILENAME", typeConverter.getDataType("VARCHAR("+getFilenameColumnSize()+")", false), null, null, null,new NotNullConstraint())
                .addColumn("DATEEXECUTED", typeConverter.getDateTimeType(), null, new NotNullConstraint())
                .addColumn("ORDEREXECUTED", typeConverter.getDataType("INT", false), new NotNullConstraint())
                .addColumn("EXECTYPE", typeConverter.getDataType("VARCHAR(10)", false), new NotNullConstraint())
                .addColumn("MD5SUM", typeConverter.getDataType("VARCHAR(35)", false))
                .addColumn("DESCRIPTION", typeConverter.getDataType("VARCHAR(255)", false))
                .addColumn("COMMENTS", typeConverter.getDataType("VARCHAR(255)", false))
                .addColumn("TAG", typeConverter.getDataType("VARCHAR(255)", false))
                .addColumn("LIQUIBASE", typeConverter.getDataType("VARCHAR(20)", false));

        return SqlGeneratorFactory.getInstance().generateSql(createTableStatement, database);
    }

    protected String getIdColumnSize() {
        return "63";
    }

    protected String getAuthorColumnSize() {
        return "63";
    }

    protected String getFilenameColumnSize() {
        return "200";
    }
}
