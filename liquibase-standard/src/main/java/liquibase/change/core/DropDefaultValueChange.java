package liquibase.change.core;

import static liquibase.change.ChangeParameterMetaData.ALL;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropDefaultValueStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import lombok.Setter;

/**
 * Removes the default value from an existing column.
 */
@DatabaseChange(name = "dropDefaultValue", description = "Removes the database default value for a column",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "column")
@Setter
public class DropDefaultValueChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnDataType;

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.catalog", since = "3.0", description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.schema", description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation", description = "Name of the table containing the column")
    public String getTableName() {
        return tableName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column", description = "Name of the column to drop the default value from")
    public String getColumnName() {
        return columnName;
    }

    @DatabaseChangeProperty(supportsDatabase = ALL, description = "Data type of the column")
    public String getColumnDataType() {
		return columnDataType;
	}

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
                new DropDefaultValueStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName(), getColumnDataType()),
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            Column snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(new Column(Table.class, getCatalogName(), getSchemaName(), getTableName(), getColumnName()), database);
            return new ChangeStatus().assertComplete(snapshot.getDefaultValue() == null, "Column has a default value");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }

    }

    @Override
    public String getConfirmationMessage() {
        return "Default value dropped from "+getTableName()+"."+getColumnName();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
