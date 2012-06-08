package liquibase.change.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.AbstractChange;
import liquibase.change.Change;
import liquibase.change.ChangeClass;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeProperty;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateIndexStatement;

/**
 * Creates an index on an existing column.
 */
@ChangeClass(name="createIndex", description = "Create Index", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "index")
public class CreateIndexChange extends AbstractChange implements ChangeWithColumns<ColumnConfig> {

    private String schemaName;
    private String tableName;
    private String indexName;
    private Boolean unique;
    private String tablespace;
    private List<ColumnConfig> columns;
	// Contain associations of index
	// for example: foreignKey, primaryKey or uniqueConstraint
    @ChangeProperty(includeInSerialization = false)
	private String associatedWith;
    private String catalogName;


    public CreateIndexChange() {
        columns = new ArrayList<ColumnConfig>();
    }

    @ChangeProperty(requiredForDatabase = "all", mustApplyTo = "index")
    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }



    @ChangeProperty(mustApplyTo ="index.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @ChangeProperty(requiredForDatabase = "all", mustApplyTo = "index.table")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @ChangeProperty(requiredForDatabase = "all", mustApplyTo = "index.column")
    public List<ColumnConfig> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    public void addColumn(ColumnConfig column) {
        columns.add(column);
    }


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

    /**
     * @return the isUnique
     */
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
	public String getAssociatedWith() {
		return associatedWith;
	}

	public void setAssociatedWith(String associatedWith) {
		this.associatedWith = associatedWith;
	}


    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }
}
