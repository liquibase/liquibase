package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateIndexStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates an index on an existing column.
 */
@DatabaseChange(name="createIndex", description = "Creates an index on an existing column or set of columns.", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "index")
public class CreateIndexChange extends AbstractChange implements ChangeWithColumns<ColumnConfig> {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String indexName;
    private Boolean unique;
    private String tablespace;
    private List<ColumnConfig> columns;

	// Contain associations of index
	// for example: foreignKey, primaryKey or uniqueConstraint
	private String associatedWith;


    public CreateIndexChange() {
        columns = new ArrayList<ColumnConfig>();
    }

    @DatabaseChangeProperty(mustEqualExisting = "index", description = "Name of the index to create")
    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="index.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "index.table", description = "Name of the table to add the index to", exampleValue = "person")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "index.column", description = "Column(s) to add to the index", requiredForDatabase = "all")
    public List<ColumnConfig> getColumns() {
        if (columns == null) {
            return new ArrayList<ColumnConfig>();
        }
        return columns;
    }

    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }


    @DatabaseChangeProperty(description = "Tablepace to create the index in.")
    public String getTablespace() {
        return tablespace;
    }

    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

    public SqlStatement[] generateStatements(Database database) {
        List<String> columns = new ArrayList<String>();
        for (ColumnConfig column : getColumns()) {
            columns.add(column.getName());
        }

	    return new SqlStatement[]{
                new CreateIndexStatement(
					    getIndexName(),
                        getCatalogName(),
					    getSchemaName(),
					    getTableName(),
					    this.isUnique(),
					    getAssociatedWith(),
					    columns.toArray(new String[getColumns().size()]))
					    .setTablespace(getTablespace())
	    };
    }

    @Override
    protected Change[] createInverses() {
        DropIndexChange inverse = new DropIndexChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setIndexName(getIndexName());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Index " + getIndexName() + " created";
    }

    /**
     * @param isUnique the isUnique to set
     */
    public void setUnique(Boolean isUnique) {
        this.unique = isUnique;
    }

    @DatabaseChangeProperty(description = "Unique values index", since = "1.8")
    public Boolean isUnique() {
        return this.unique;
    }

	/**
	 * @return Index associations. Valid values:<br>
	 * <li>primaryKey</li>
	 * <li>foreignKey</li>
	 * <li>uniqueConstraint</li>
	 * <li>none</li>
	 * */
    @DatabaseChangeProperty(isChangeProperty = false)
	public String getAssociatedWith() {
		return associatedWith;
	}

	public void setAssociatedWith(String associatedWith) {
		this.associatedWith = associatedWith;
	}


    @DatabaseChangeProperty(since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }
}
