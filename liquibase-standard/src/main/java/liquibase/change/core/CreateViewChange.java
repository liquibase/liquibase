package liquibase.change.core;

import liquibase.ChecksumVersion;
import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateViewStatement;
import liquibase.statement.core.DropViewStatement;
import liquibase.statement.core.SetViewRemarksStatement;
import liquibase.structure.core.View;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static liquibase.change.core.CreateViewChange.changeName;
import static liquibase.statement.SqlStatement.EMPTY_SQL_STATEMENT;
import static org.apache.commons.lang3.StringUtils.trimToNull;

/**
 * Creates a new view.
 */
@DatabaseChange(name = changeName, description = "Create a new database view", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class CreateViewChange extends AbstractSQLAndFileChange {
    public static final String changeName = "createView";

    @Setter
    private String viewName;

    public void setSelectQuery(String selectQuery) {
        sql(selectQuery);
    }

    @Setter
    private Boolean fullDefinition;

    @Setter
    private String remarks;

    @DatabaseChangeProperty(description = "Name of the view to create")
    public String getViewName() {
        return viewName;
    }

    @DatabaseChangeProperty(serializationType = SerializationType.DIRECT_VALUE, description = "SQL for generating the view",
        exampleValue = "SELECT id, name FROM person WHERE id > 10")
    public String getSelectQuery() {
        return sql();
    }

    @DatabaseChangeProperty(description = "Use 'CREATE OR REPLACE' syntax", since = "1.5")
    public Boolean getReplaceIfExists() {
        return replaceIfExists;
    }

    @DatabaseChangeProperty(since = "3.3",
        description = "Set to true if selectQuery is the entire view definition. Set to false if the CREATE VIEW header should be added")
    public Boolean getFullDefinition() {
        return fullDefinition;
    }

    @DatabaseChangeProperty(description = "Path to the file containing the view definition. Specifying 'path' is an alternative to selectQuery.", since = "3.6")
    public String getPath() {
        return file();
    }

    @DatabaseChangeProperty(description = "A brief descriptive comment stored in the view metadata")
    public String getRemarks() {
        return remarks;
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validate = super.validate(database);
        if (!validate.hasErrors()) {
            super.validate(database, validate);
        }
        return validate;
    }

    @Override
    public String[] getExcludedFieldFilters(ChecksumVersion version) {
        if (version.lowerOrEqualThan(ChecksumVersion.V8)) {
            return new String[0];
        }
        return new String[] {
                "path",
                "relativeToChangelogFile",
                "selectQuery",
                "encoding"
        };
    }


    @Override
    public SqlStatement[] generateStatements(Database database) {
        List<SqlStatement> statements = new ArrayList<>();

        boolean replaceIfExists = (getReplaceIfExists() != null) && getReplaceIfExists();

        boolean fullDefinition = false;
        if (this.fullDefinition != null) {
            fullDefinition = this.fullDefinition;
        }

        String selectQuery = getSqlText();

        if (!supportsReplaceIfExistsOption(database) && replaceIfExists) {
            statements.add(new DropViewStatement(getCatalogName(), getSchemaName(), getViewName()));
            statements.add(createViewStatement(getCatalogName(), getSchemaName(), getViewName(), selectQuery, false)
                    .setFullDefinition(fullDefinition));
        } else {
            statements.add(createViewStatement(getCatalogName(), getSchemaName(), getViewName(), selectQuery, replaceIfExists)
                    .setFullDefinition(fullDefinition));
        }

        List<Class<?>> databaseSupportsViewComments = Arrays.asList(OracleDatabase.class, PostgresDatabase.class, MSSQLDatabase.class, DB2Database.class,
                SybaseASADatabase.class);
        boolean supportsViewComments = databaseSupportsViewComments.stream().anyMatch(clazz -> clazz.isInstance(database));

        if (supportsViewComments && (trimToNull(remarks) != null)) {
            SetViewRemarksStatement remarksStatement = new SetViewRemarksStatement(catalogName, schemaName, viewName, remarks);
            if (SqlGeneratorFactory.getInstance().supports(remarksStatement, database)) {
                statements.add(remarksStatement);
            }
        }

        return statements.toArray(EMPTY_SQL_STATEMENT);
    }

    protected CreateViewStatement createViewStatement(String catalogName, String schemaName, String viewName, String selectQuery, boolean replaceIfExists) {
        return new CreateViewStatement(catalogName, schemaName, viewName, selectQuery, replaceIfExists);
    }

    @Override
    public String getConfirmationMessage() {
        return "View " + getViewName() + " created";
    }

    @Override
    protected Change[] createInverses() {
        DropViewChange inverse = new DropViewChange();
        inverse.setViewName(getViewName());
        inverse.setSchemaName(getSchemaName());

        return new Change[] { inverse };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            View example = new View(getCatalogName(), getSchemaName(), getViewName());

            View snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
            result.assertComplete(snapshot != null, "View does not exist");

            return result;

        } catch (Exception e) {
            return result.unknown(e);
        }
    }

    private boolean supportsReplaceIfExistsOption(Database database) {
        return !(database instanceof SQLiteDatabase);
    }

    @Override
    protected String sqlFieldName() {
        return "selectQuery";
    }
}
