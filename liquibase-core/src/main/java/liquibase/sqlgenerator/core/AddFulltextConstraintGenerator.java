package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddFulltextConstraintStatement;
import liquibase.structure.core.Table;
import liquibase.structure.core.FulltextConstraint;
import liquibase.util.StringUtils;

public class AddFulltextConstraintGenerator extends AbstractSqlGenerator<AddFulltextConstraintStatement> {

    @Override
    public boolean supports(AddFulltextConstraintStatement statement, Database database) {
        return database instanceof MySQLDatabase;
    }

    @Override
    public ValidationErrors validate(AddFulltextConstraintStatement addFulltextConstraintStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        if( database instanceof MySQLDatabase ){
            validationErrors.checkRequiredField("columnNames", addFulltextConstraintStatement.getColumnNames());
            validationErrors.checkRequiredField("tableName", addFulltextConstraintStatement.getTableName());
        }
        
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(AddFulltextConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        
        String sql = null;

        if( database instanceof MySQLDatabase ){
            if (statement.getConstraintName() == null) {
                    sql = String.format("ALTER TABLE %s ADD FULLTEXT INDEX (%s)"
                                    , database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
                                    , database.escapeColumnNameList(statement.getColumnNames())
                    );
            } else {
                    sql = String.format("ALTER TABLE %s ADD FULLTEXT INDEX %s (%s)"
                                    , database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
                                    , database.escapeConstraintName(statement.getConstraintName())
                                    , database.escapeColumnNameList(statement.getColumnNames())
                    );
            }
        }

        return new Sql[] {
                new UnparsedSql(sql, getAffectedFulltextConstraint(statement))
        };

    }

    protected FulltextConstraint getAffectedFulltextConstraint(AddFulltextConstraintStatement statement) {
        FulltextConstraint fulltextConstraint = new FulltextConstraint()
                .setName(statement.getConstraintName())
                .setTable((Table) new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName()));
        int i = 0;
        for (String column : StringUtils.splitAndTrim(statement.getColumnNames(), ",")) {
            fulltextConstraint.addColumn(i++, column);
        }
        return fulltextConstraint;
    }
}
