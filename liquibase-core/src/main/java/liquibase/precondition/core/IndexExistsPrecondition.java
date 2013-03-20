package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Schema;
import liquibase.exception.*;
import liquibase.precondition.Precondition;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

public class IndexExistsPrecondition implements Precondition {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnNames;
    private String indexName;

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    public Warnings warn(Database database) {
        return new Warnings();
    }

    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (getIndexName() == null && getTableName() == null && getColumnNames() == null) {
            validationErrors.addError("indexName OR tableName and columnNames is required");
        }
        return validationErrors;
    }

    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
    	try {
            Schema schema = new Schema(getCatalogName(), getSchemaName());
            Index example = new Index();
            example.setTable(new Table());
            if (StringUtils.trimToNull(getTableName()) != null) {
                example.getTable().setName(database.correctObjectName(getTableName(), Table.class));
            }
            example.getTable().setSchema(schema);
            example.setName(database.correctObjectName(getIndexName(), Index.class));
            if (StringUtils.trimToNull(getColumnNames()) != null) {
                for (String column : getColumnNames().split("\\s*,\\s*")) {
                    example.getColumns().add(database.correctObjectName(column, Column.class));
                }
            }
            if (!SnapshotGeneratorFactory.getInstance().has(example, database)) {
                String name = "";

                if (getIndexName() != null) {
                    name += database.escapeStringForDatabase(getIndexName());
                }

                if (StringUtils.trimToNull(getTableName()) != null) {
                    name += " on "+database.escapeStringForDatabase(getTableName());

                    if (StringUtils.trimToNull(getColumnNames()) != null) {
                        name += " columns "+getColumnNames();
                    }
                }
                throw new PreconditionFailedException("Index "+ name +" does not exist", changeLog, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    public String getName() {
        return "indexExists";
    }
}