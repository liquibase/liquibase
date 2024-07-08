package liquibase.change.core;

import liquibase.ChecksumVersion;
import liquibase.change.*;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateIndexStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Creates an index on an existing column.
 */
@DatabaseChange(name = "createIndex", 
    description = "Creates an index on an existing column or set of columns.",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "index")
public class CreateIndexChange extends AbstractChange implements ChangeWithColumns<AddColumnConfig> {

    @Setter
    private String catalogName;
    @Setter
    private String schemaName;
    @Setter
    private String tableName;
    @Setter
    private String indexName;
    @Setter
    private Boolean unique;
    @Setter
    private String tablespace;
    private List<AddColumnConfig> columns;

    // Contain associations of index
    // for example: foreignKey, primaryKey or uniqueConstraint
    @Setter
    private String associatedWith;
    @Setter
    private Boolean clustered;


    public CreateIndexChange() {
        columns = new ArrayList<>();
    }

    @DatabaseChangeProperty(mustEqualExisting = "index", description = "Name of the index to create")
    public String getIndexName() {
        return indexName;
    }

    @DatabaseChangeProperty(since = "3.0", description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="index.schema", description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "index.table", description = "Name of the table to add the index on",
        exampleValue = "person")
    public String getTableName() {
        return tableName;
    }

    @Override
    @DatabaseChangeProperty(mustEqualExisting = "index.column", description = "Column(s) in the table to add the index on",
        requiredForDatabase = "all")
    public List<AddColumnConfig> getColumns() {
        if (columns == null) {
            return new ArrayList<>();
        }
        return columns;
    }

    @Override
    public void setColumns(List<AddColumnConfig> columns) {
        this.columns = columns;
    }

    @Override
    public void addColumn(AddColumnConfig column) {
        columns.add(column);
    }


    @DatabaseChangeProperty(description = "Tablepace to create the index in. Corresponds to file group in mssql")
    public String getTablespace() {
        return tablespace;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
                new CreateIndexStatement(
                        getIndexName(),
                        getCatalogName(),
                        getSchemaName(),
                        getTableName(),
                        this.isUnique(),
                        getAssociatedWith(),
                        getColumns().toArray(new AddColumnConfig[0]))
                        .setTablespace(getTablespace())
                        .setClustered(getClustered())
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

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            Index example = new Index(getIndexName(), getCatalogName(), getSchemaName(), getTableName());
            if (getColumns() != null) {
                for (ColumnConfig column : getColumns() ) {
                    example.addColumn(new Column(column));
                }
            }

            Index snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
            result.assertComplete(snapshot != null, "Index does not exist");

            if (snapshot != null) {
                if (isUnique() != null) {
                    result.assertCorrect(isUnique().equals(snapshot.isUnique()), "Unique does not match");
                }
            }

            return result;

        } catch (Exception e) {
            return result.unknown(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Index " + getIndexName() + " created";
    }

    @DatabaseChangeProperty(description = "Whether the index is unique (contains no duplicate values)", since = "1.8")
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
    @DatabaseChangeProperty(description = "Index associations. Valid values: primaryKey, foreignKey, uniqueConstriant, none")
    public String getAssociatedWith() {
        return associatedWith;
    }

    @DatabaseChangeProperty(description = "Whether to create a clustered index")
    public Boolean getClustered() {
        return clustered;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        Object value = super.getSerializableFieldValue(field);
        if ((value != null) && "columns".equals(field)) {
            for (ColumnConfig config : (Collection<ColumnConfig>) value) {
                config.setType(null);
                config.setAutoIncrement(null);
                config.setConstraints(null);
                config.setDefaultValue(null);
                config.setValue(null);
                config.setStartWith(null);
                config.setIncrementBy(null);
                config.setEncoding(null);
                config.setRemarks(null);
            }

        }
        return value;
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.addAll(super.validate(database));

        if (columns != null) {
            for (ColumnConfig columnConfig : columns) {
                if (columnConfig.getName() == null) {
                    validationErrors.addError("column 'name' is required for all columns in an index");
                }
            }
        }
        return validationErrors;
    }

    @Override
    public String[] getExcludedFieldFilters(ChecksumVersion version) {
        return new String[]{
                "associatedWith"
        };
    }
}
