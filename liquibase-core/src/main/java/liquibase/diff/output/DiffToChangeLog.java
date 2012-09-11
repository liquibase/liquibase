package liquibase.diff.output;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.*;
import liquibase.changelog.ChangeSet;
import liquibase.datatype.DataTypeFactory;
import liquibase.diff.DiffResult;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.ChangeLogSerializerFactory;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.core.*;
import liquibase.util.ISODateFormat;
import liquibase.util.StringUtils;
import liquibase.util.csv.CSVWriter;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DiffToChangeLog {

    private String idRoot = String.valueOf(new Date().getTime());
    private int changeNumber = 1;

    private String changeSetContext;
    private String changeSetAuthor;
    private DiffResult diffResult;
    private DiffOutputConfig diffOutputConfig;

    public DiffToChangeLog(DiffResult diffResult, DiffOutputConfig diffOutputConfig) {
        this.diffResult = diffResult;
        this.diffOutputConfig = diffOutputConfig;
    }

    public void setChangeSetContext(String changeSetContext) {
        this.changeSetContext = changeSetContext;
    }

    public void print(String changeLogFile) throws ParserConfigurationException, IOException, DatabaseException {
        ChangeLogSerializer changeLogSerializer = ChangeLogSerializerFactory.getInstance().getSerializer(changeLogFile);
        this.print(changeLogFile, changeLogSerializer);
    }

    public void print(PrintStream out) throws ParserConfigurationException, IOException, DatabaseException {
        this.print(out, new XMLChangeLogSerializer());
    }

    public void print(String changeLogFile, ChangeLogSerializer changeLogSerializer) throws ParserConfigurationException, IOException, DatabaseException {
        File file = new File(changeLogFile);
        if (!file.exists()) {
            LogFactory.getLogger().info(file + " does not exist, creating");
            FileOutputStream stream = new FileOutputStream(file);
            print(new PrintStream(stream), changeLogSerializer);
            stream.close();
        } else {
            LogFactory.getLogger().info(file + " exists, appending");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            print(new PrintStream(out), changeLogSerializer);

            String xml = new String(out.toByteArray());
            xml = xml.replaceFirst("(?ms).*<databaseChangeLog[^>]*>", "");
            xml = xml.replaceFirst("</databaseChangeLog>", "");
            xml = xml.trim();
            if ("".equals(xml)) {
                LogFactory.getLogger().info("No changes found, nothing to do");
                return;
            }

            String lineSeparator = System.getProperty("line.separator");
            BufferedReader fileReader = new BufferedReader(new FileReader(file));
            String line;
            long offset = 0;
            while ((line = fileReader.readLine()) != null) {
                int index = line.indexOf("</databaseChangeLog>");
                if (index >= 0) {
                    offset += index;
                } else {
                    offset += line.getBytes().length;
                    offset += lineSeparator.getBytes().length;
                }
            }
            fileReader.close();

            fileReader = new BufferedReader(new FileReader(file));
            fileReader.skip(offset);

            fileReader.close();

            // System.out.println("resulting XML: " + xml.trim());

            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(offset);
            randomAccessFile.writeBytes("    ");
            randomAccessFile.write(xml.getBytes());
            randomAccessFile.writeBytes(lineSeparator);
            randomAccessFile.writeBytes("</databaseChangeLog>" + lineSeparator);
            randomAccessFile.close();

            // BufferedWriter fileWriter = new BufferedWriter(new
            // FileWriter(file));
            // fileWriter.append(xml);
            // fileWriter.close();
        }
    }

    /**
     * Prints changeLog that would bring the target database to be the same as
     * the reference database
     */
    public void print(PrintStream out, ChangeLogSerializer changeLogSerializer) throws ParserConfigurationException, IOException, DatabaseException {
        List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
        addMissingTableChanges(changeSets);
        addMissingColumnChanges(changeSets);
        addChangedColumnChanges(changeSets);
        addMissingPrimaryKeyChanges(changeSets);
        addUnexpectedPrimaryKeyChanges(changeSets);
        addUnexpectedForeignKeyChanges(changeSets);
        addMissingUniqueConstraintChanges(changeSets);
        addUnexpectedUniqueConstraintChanges(changeSets);

        if (diffResult.getData().wasCompared()) {
            addInsertDataChanges(changeSets, diffResult.getDiffControl().getDataDir());
        }

        addMissingForeignKeyChanges(changeSets);
        addUnexpectedIndexChanges(changeSets);
        addMissingIndexChanges(changeSets);
        addUnexpectedColumnChanges(changeSets);
        addMissingSequenceChanges(changeSets);
        addUnexpectedSequenceChanges(changeSets);
        addMissingViewChanges(changeSets);
        addUnexpectedViewChanges(changeSets);
        addChangedViewChanges(changeSets);
        addUnexpectedTableChanges(changeSets);

        changeLogSerializer.write(changeSets, out);

        out.flush();
    }

    protected ChangeSet generateChangeSet(Change change) {
        ChangeSet changeSet = generateChangeSet();
        changeSet.addChange(change);

        return changeSet;
    }

    protected ChangeSet generateChangeSet() {
        return new ChangeSet(generateId(), getChangeSetAuthor(), false, false,
                null, changeSetContext, null);
    }

    protected String getChangeSetAuthor() {
        if (changeSetAuthor != null) {
            return changeSetAuthor;
        }
        String author = System.getProperty("user.name");
        if (StringUtils.trimToNull(author) == null) {
            return "diff-generated";
        } else {
            return author + " (generated)";
        }
    }

    public void setChangeSetAuthor(String changeSetAuthor) {
        this.changeSetAuthor = changeSetAuthor;
    }

    public void setIdRoot(String idRoot) {
        this.idRoot = idRoot;
    }

    protected String generateId() {
        return idRoot + "-" + changeNumber++;
    }

    protected void addUnexpectedIndexChanges(List<ChangeSet> changes) {
        for (Index index : diffResult.getObjectDiff(Index.class).getUnexpected()) {

            if (index.getAssociatedWith().contains(Index.MARK_PRIMARY_KEY) || index.getAssociatedWith().contains(Index.MARK_FOREIGN_KEY) || index.getAssociatedWith().contains(Index.MARK_UNIQUE_CONSTRAINT)) {
                continue;
            }

            DropIndexChange change = new DropIndexChange();
            change.setTableName(index.getTable().getName());
            if (diffOutputConfig.isIncludeCatalog()) {
                change.setCatalogName(index.getTable().getSchema().getCatalog().getName());
            }
            if (diffOutputConfig.isIncludeSchema()) {
                change.setSchemaName(index.getTable().getSchema().getName());
            }
            change.setIndexName(index.getName());
            change.setAssociatedWith(index.getAssociatedWithAsString());

            changes.add(generateChangeSet(change));
        }
    }

    protected void addMissingIndexChanges(List<ChangeSet> changes) {
        for (Index index : diffResult.getObjectDiff(Index.class).getMissing()) {

            CreateIndexChange change = new CreateIndexChange();
            change.setTableName(index.getTable().getName());
            if (diffOutputConfig.isIncludeTablespace()) {
                change.setTablespace(index.getTablespace());
            }
            if (diffOutputConfig.isIncludeCatalog()) {
                change.setCatalogName(index.getTable().getSchema().getCatalog().getName());
            }
            if (diffOutputConfig.isIncludeSchema()) {
                change.setSchemaName(index.getTable().getSchema().getName());
            }
            change.setIndexName(index.getName());
            change.setUnique(index.isUnique());
            change.setAssociatedWith(index.getAssociatedWithAsString());

            if (index.getAssociatedWith().contains(Index.MARK_PRIMARY_KEY) || index.getAssociatedWith().contains(Index.MARK_FOREIGN_KEY) || index.getAssociatedWith().contains(Index.MARK_UNIQUE_CONSTRAINT)) {
                continue;
            }

            for (String columnName : index.getColumns()) {
                ColumnConfig column = new ColumnConfig();
                column.setName(columnName);
                change.addColumn(column);
            }
            changes.add(generateChangeSet(change));
        }
    }

    protected void addUnexpectedPrimaryKeyChanges(List<ChangeSet> changes) {
        for (PrimaryKey pk : diffResult.getObjectDiff(PrimaryKey.class).getUnexpected()) {

            if (!diffResult.getObjectDiff(Table.class).getUnexpected().contains(pk.getTable())) {
                DropPrimaryKeyChange change = new DropPrimaryKeyChange();
                change.setTableName(pk.getTable().getName());
                if (diffOutputConfig.isIncludeCatalog()) {
                    change.setCatalogName(pk.getTable().getSchema().getCatalog().getName());
                }
                if (diffOutputConfig.isIncludeSchema()) {
                    change.setSchemaName(pk.getTable().getSchema().getName());
                }
                change.setConstraintName(pk.getName());

                changes.add(generateChangeSet(change));
            }
        }
    }

    protected void addMissingPrimaryKeyChanges(List<ChangeSet> changes) {
        for (PrimaryKey pk : diffResult.getObjectDiff(PrimaryKey.class).getMissing()) {

            AddPrimaryKeyChange change = new AddPrimaryKeyChange();
            change.setTableName(pk.getTable().getName());
            if (diffOutputConfig.isIncludeCatalog()) {
                change.setCatalogName(pk.getTable().getSchema().getCatalog().getName());
            }
            if (diffOutputConfig.isIncludeSchema()) {
                change.setSchemaName(pk.getTable().getSchema().getName());
            }
            change.setConstraintName(pk.getName());
            change.setColumnNames(pk.getColumnNames());
            if (diffOutputConfig.isIncludeTablespace()) {
                change.setTablespace(pk.getTablespace());
            }

            changes.add(generateChangeSet(change));
        }
    }

    protected void addUnexpectedUniqueConstraintChanges(List<ChangeSet> changes) {
        for (UniqueConstraint uc : diffResult.getObjectDiff(UniqueConstraint.class).getUnexpected()) {
            // Need check for nulls here due to NullPointerException using Postgres
            if (null != uc) {
                if (null != uc.getTable()) {
                    DropUniqueConstraintChange change = new DropUniqueConstraintChange();
                    change.setTableName(uc.getTable().getName());
                    if (diffOutputConfig.isIncludeCatalog()) {
                        change.setCatalogName(uc.getTable().getSchema().getCatalog().getName());
                    }
                    if (diffOutputConfig.isIncludeSchema()) {
                        change.setSchemaName(uc.getTable().getSchema().getName());
                    }
                    change.setConstraintName(uc.getName());

                    changes.add(generateChangeSet(change));
                }
            }
        }
    }

    protected void addMissingUniqueConstraintChanges(List<ChangeSet> changes) {
        for (UniqueConstraint uc : diffResult.getObjectDiff(UniqueConstraint.class).getMissing()) {
            // Need check for nulls here due to NullPointerException using Postgres
            if (null != uc)
                if (null != uc.getTable()) {
                    AddUniqueConstraintChange change = new AddUniqueConstraintChange();
                    change.setTableName(uc.getTable().getName());
                    if (uc.getBackingIndex() != null && diffOutputConfig.isIncludeTablespace()) {
                        change.setTablespace(uc.getBackingIndex().getTablespace());
                    }
                    if (diffOutputConfig.isIncludeCatalog()) {
                        change.setCatalogName(uc.getTable().getSchema().getCatalog().getName());
                    }
                    if (diffOutputConfig.isIncludeSchema()) {
                        change.setSchemaName(uc.getTable().getSchema().getName());
                    }
                    change.setConstraintName(uc.getName());
                    change.setColumnNames(uc.getColumnNames());
                    change.setDeferrable(uc.isDeferrable());
                    change.setInitiallyDeferred(uc.isInitiallyDeferred());
                    change.setDisabled(uc.isDisabled());
                    changes.add(generateChangeSet(change));
                }
        }
    }

    protected void addUnexpectedForeignKeyChanges(List<ChangeSet> changes) {
        for (ForeignKey fk : diffResult.getObjectDiff(ForeignKey.class).getUnexpected()) {

            DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
            change.setConstraintName(fk.getName());
            change.setBaseTableName(fk.getForeignKeyTable().getName());
            if (diffOutputConfig.isIncludeCatalog()) {
                change.setBaseTableCatalogName(fk.getForeignKeyTable().getSchema().getCatalogName());
            }
            if (diffOutputConfig.isIncludeSchema()) {
                change.setBaseTableSchemaName(fk.getForeignKeyTable().getSchema().getName());
            }

            changes.add(generateChangeSet(change));
        }
    }

    protected void addMissingForeignKeyChanges(List<ChangeSet> changes) {
        for (ForeignKey fk : diffResult.getObjectDiff(ForeignKey.class).getMissing()) {

            AddForeignKeyConstraintChange change = new AddForeignKeyConstraintChange();
            change.setConstraintName(fk.getName());

            change.setReferencedTableName(fk.getPrimaryKeyTable().getName());
            if (diffOutputConfig.isIncludeCatalog()) {
                change.setReferencedTableCatalogName(fk.getPrimaryKeyTable().getSchema().getCatalogName());
            }
            if (diffOutputConfig.isIncludeSchema()) {
                change.setReferencedTableSchemaName(fk.getPrimaryKeyTable().getSchema().getName());
            }
            change.setReferencedColumnNames(fk.getPrimaryKeyColumns());

            change.setBaseTableName(fk.getForeignKeyTable().getName());
            if (diffOutputConfig.isIncludeCatalog()) {
                change.setBaseTableCatalogName(fk.getForeignKeyTable().getSchema().getCatalogName());
            }
            if (diffOutputConfig.isIncludeSchema()) {
                change.setBaseTableSchemaName(fk.getForeignKeyTable().getSchema().getName());
            }
            change.setBaseColumnNames(fk.getForeignKeyColumns());

            change.setDeferrable(fk.isDeferrable());
            change.setInitiallyDeferred(fk.isInitiallyDeferred());
            change.setOnUpdate(fk.getUpdateRule());
            change.setOnDelete(fk.getDeleteRule());

            change.setReferencesUniqueColumn(fk.getReferencesUniqueColumn());

            changes.add(generateChangeSet(change));
        }
    }

    protected void addUnexpectedSequenceChanges(List<ChangeSet> changes) {
        for (Sequence sequence : diffResult.getObjectDiff(Sequence.class).getUnexpected()) {

            DropSequenceChange change = new DropSequenceChange();
            change.setSequenceName(sequence.getName());
            if (diffOutputConfig.isIncludeCatalog()) {
                change.setCatalogName(sequence.getSchema().getCatalog().getName());
            }
            if (diffOutputConfig.isIncludeSchema()) {
                change.setSchemaName(sequence.getSchema().getName());
            }

            changes.add(generateChangeSet(change));
        }
    }

    protected void addMissingSequenceChanges(List<ChangeSet> changes) {
        for (Sequence sequence : diffResult.getObjectDiff(Sequence.class).getMissing()) {

            CreateSequenceChange change = new CreateSequenceChange();
            change.setSequenceName(sequence.getName());
            if (diffOutputConfig.isIncludeCatalog()) {
                change.setCatalogName(sequence.getSchema().getCatalog().getName());
            }
            if (diffOutputConfig.isIncludeSchema()) {
                change.setSchemaName(sequence.getSchema().getName());
            }

            changes.add(generateChangeSet(change));
        }
    }

    protected void addUnexpectedColumnChanges(List<ChangeSet> changes) {
        for (Column column : diffResult.getObjectDiff(Column.class).getUnexpected()) {
            if (!shouldModifyColumn(column)) {
                continue;
            }

            DropColumnChange change = new DropColumnChange();
            change.setTableName(column.getRelation().getName());
            if (diffOutputConfig.isIncludeCatalog()) {
                change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
            }
            if (diffOutputConfig.isIncludeSchema()) {
                change.setSchemaName(column.getRelation().getSchema().getName());
            }
            change.setColumnName(column.getName());

            changes.add(generateChangeSet(change));
        }
    }

    protected void addMissingViewChanges(List<ChangeSet> changes) {
        for (View view : diffResult.getObjectDiff(View.class).getMissing()) {

            CreateViewChange change = new CreateViewChange();
            change.setViewName(view.getName());
            if (diffOutputConfig.isIncludeCatalog()) {
                change.setCatalogName(view.getSchema().getCatalog().getName());
            }
            if (diffOutputConfig.isIncludeSchema()) {
                change.setSchemaName(view.getSchema().getName());
            }
            String selectQuery = view.getDefinition();
            if (selectQuery == null) {
                selectQuery = "COULD NOT DETERMINE VIEW QUERY";
            }
            change.setSelectQuery(selectQuery);

            changes.add(generateChangeSet(change));
        }
    }

    protected void addChangedViewChanges(List<ChangeSet> changes) {
        for (View view : diffResult.getObjectDiff(View.class).getChanged()) {

            CreateViewChange change = new CreateViewChange();
            change.setViewName(view.getName());
            if (diffOutputConfig.isIncludeCatalog()) {
                change.setCatalogName(view.getSchema().getCatalog().getName());
            }
            if (diffOutputConfig.isIncludeSchema()) {
                change.setSchemaName(view.getSchema().getName());
            }
            String selectQuery = view.getDefinition();
            if (selectQuery == null) {
                selectQuery = "COULD NOT DETERMINE VIEW QUERY";
            }
            change.setSelectQuery(selectQuery);
            change.setReplaceIfExists(true);

            changes.add(generateChangeSet(change));
        }
    }

    protected void addChangedColumnChanges(List<ChangeSet> changes) {
        for (Column column : diffResult.getObjectDiff(Column.class).getChanged()) {
            if (!shouldModifyColumn(column)) {
                continue;
            }

            boolean foundDifference = false;
            Column referenceColumn = diffResult.getReferenceSnapshot().getColumn(column.getRelation().getSchema(), column.getRelation().getName(), column.getName());
            if (column.isDataTypeDifferent(referenceColumn)) {
                ModifyDataTypeChange change = new ModifyDataTypeChange();
                change.setTableName(column.getRelation().getName());
                if (diffOutputConfig.isIncludeCatalog()) {
                    change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
                }
                if (diffOutputConfig.isIncludeSchema()) {
                    change.setSchemaName(column.getRelation().getSchema().getName());
                }
                change.setColumnName(column.getName());
                change.setNewDataType(referenceColumn.getType().toString());
                changes.add(generateChangeSet(change));
                foundDifference = true;
            }
            if (column.isNullabilityDifferent(referenceColumn)) {
                if (referenceColumn.isNullable() == null
                        || referenceColumn.isNullable()) {
                    DropNotNullConstraintChange change = new DropNotNullConstraintChange();
                    change.setTableName(column.getRelation().getName());
                    if (diffOutputConfig.isIncludeCatalog()) {
                        change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
                    }
                    if (diffOutputConfig.isIncludeSchema()) {
                        change.setSchemaName(column.getRelation().getSchema().getName());
                    }
                    change.setColumnName(column.getName());
                    change.setColumnDataType(referenceColumn.getType().toString());

                    changes.add(generateChangeSet(change));
                    foundDifference = true;
                } else {
                    AddNotNullConstraintChange change = new AddNotNullConstraintChange();
                    change.setTableName(column.getRelation().getName());
                    if (diffOutputConfig.isIncludeCatalog()) {
                        change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
                    }
                    if (diffOutputConfig.isIncludeSchema()) {
                        change.setSchemaName(column.getRelation().getSchema().getName());
                    }
                    change.setColumnName(column.getName());
                    change.setColumnDataType(referenceColumn.getType().toString());

                    Object defaultValue = column.getDefaultValue();
                    String defaultValueString;
                    if (defaultValue != null) {
                        defaultValueString = DataTypeFactory.getInstance().from(column.getType()).objectToSql(defaultValue, diffResult.getComparisonSnapshot().getDatabase());

                        if (defaultValueString != null) {
                            change.setDefaultNullValue(defaultValueString);
                        }
                    }


                    changes.add(generateChangeSet(change));
                    foundDifference = true;
                }

            }
            if (!foundDifference) {
                throw new RuntimeException("Unknown difference");
            }
        }
    }

    protected boolean shouldModifyColumn(Column column) {
        return !diffResult.getReferenceSnapshot().getDatabase().isLiquibaseTable(column.getRelation().getSchema(), column.getRelation().getName());

    }

    protected void addUnexpectedViewChanges(List<ChangeSet> changes) {
        for (View view : diffResult.getObjectDiff(View.class).getUnexpected()) {

            DropViewChange change = new DropViewChange();
            change.setViewName(view.getName());
            if (diffOutputConfig.isIncludeCatalog()) {
                change.setCatalogName(view.getSchema().getCatalog().getName());
            }
            if (diffOutputConfig.isIncludeSchema()) {
                change.setSchemaName(view.getSchema().getName());
            }

            changes.add(generateChangeSet(change));
        }
    }

    protected void addMissingColumnChanges(List<ChangeSet> changes) {
        for (Column column : diffResult.getObjectDiff(Column.class).getMissing()) {
            if (!shouldModifyColumn(column)) {
                continue;
            }

            AddColumnChange change = new AddColumnChange();
            change.setTableName(column.getRelation().getName());
            if (diffOutputConfig.isIncludeCatalog()) {
                change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
            }
            if (diffOutputConfig.isIncludeSchema()) {
                change.setSchemaName(column.getRelation().getSchema().getName());
            }

            ColumnConfig columnConfig = new ColumnConfig();
            columnConfig.setName(column.getName());

            String dataType = column.getType().toString();

            columnConfig.setType(dataType);

            Object defaultValue = column.getDefaultValue();
            if (defaultValue != null) {
                String defaultValueString = DataTypeFactory.getInstance().from(column.getType()).objectToSql(defaultValue, diffResult.getReferenceSnapshot().getDatabase());
                if (defaultValueString != null) {
                    defaultValueString = defaultValueString.replaceFirst("'",
                            "").replaceAll("'$", "");
                }
                columnConfig.setDefaultValue(defaultValueString);
            }

            if (column.getRemarks() != null) {
                columnConfig.setRemarks(column.getRemarks());
            }
            ConstraintsConfig constraintsConfig = columnConfig.getConstraints();
            if (column.isNullable() != null && !column.isNullable()) {
                if (constraintsConfig == null) {
                    constraintsConfig = new ConstraintsConfig();
                }
                constraintsConfig.setNullable(false);
            }
            if (column.isUnique()) {
                if (constraintsConfig == null) {
                    constraintsConfig = new ConstraintsConfig();
                }
                constraintsConfig.setUnique(true);
            }
            if (constraintsConfig != null) {
                columnConfig.setConstraints(constraintsConfig);
            }

            change.addColumn(columnConfig);

            changes.add(generateChangeSet(change));
        }
    }

    protected void addMissingTableChanges(List<ChangeSet> changes) {
        for (Table missingTable : diffResult.getObjectDiff(Table.class).getMissing()) {
            if (diffResult.getReferenceSnapshot().getDatabase().isLiquibaseTable(missingTable.getSchema(), missingTable.getName())) {
                continue;
            }

            CreateTableChange change = new CreateTableChange();
            change.setTableName(missingTable.getName());
            if (diffOutputConfig.isIncludeCatalog()) {
                change.setCatalogName(missingTable.getSchema().getCatalog().getName());
            }
            if (diffOutputConfig.isIncludeSchema()) {
                change.setSchemaName(missingTable.getSchema().getName());
            }
            if (missingTable.getRemarks() != null) {
                change.setRemarks(missingTable.getRemarks());
            }

            for (Column column : missingTable.getColumns()) {
                ColumnConfig columnConfig = new ColumnConfig();
                columnConfig.setName(column.getName());
                columnConfig.setType(column.getType().toString());

                ConstraintsConfig constraintsConfig = null;
                if (column.isPrimaryKey()) {
                    PrimaryKey primaryKey = null;
                    for (PrimaryKey pk : diffResult.getObjectDiff(PrimaryKey.class).getMissing()) {
                        if (pk.getTable().equals(missingTable.getName(), diffResult.getComparisonSnapshot().getDatabase())) {
                            primaryKey = pk;
                        }
                    }

                    if (primaryKey == null || primaryKey.getColumnNamesAsList().size() == 1) {
                        constraintsConfig = new ConstraintsConfig();
                        constraintsConfig.setPrimaryKey(true);
                        constraintsConfig.setPrimaryKeyTablespace(column.getTablespace());

                        if (primaryKey != null) {
                            constraintsConfig.setPrimaryKeyName(primaryKey.getName());
                            diffResult.getObjectDiff(PrimaryKey.class).getMissing().remove(primaryKey);
                        }
                    }
                }

                if (column.getType().isAutoIncrement()) {
                    columnConfig.setAutoIncrement(true);
                }

                if (column.isNullable() != null && !column.isNullable()) {
                    if (constraintsConfig == null) {
                        constraintsConfig = new ConstraintsConfig();
                    }

                    constraintsConfig.setNullable(false);
                }
//                if (column.isUnique()) {
//					if (constraintsConfig == null) {
//						constraintsConfig = new ConstraintsConfig();
//					}
//					constraintsConfig.setUnique(true);
//				}
                if (constraintsConfig != null) {
                    columnConfig.setConstraints(constraintsConfig);
                }

                Object defaultValue = column.getDefaultValue();
                if (defaultValue == null) {
                    // do nothing
                } else if (column.getType().isAutoIncrement()) {
                    // do nothing
                } else if (defaultValue instanceof Date) {
                    columnConfig.setDefaultValueDate((Date) defaultValue);
                } else if (defaultValue instanceof Boolean) {
                    columnConfig.setDefaultValueBoolean(((Boolean) defaultValue));
                } else if (defaultValue instanceof Number) {
                    columnConfig.setDefaultValueNumeric(((Number) defaultValue));
                } else if (defaultValue instanceof DatabaseFunction) {
                    columnConfig.setDefaultValueComputed((DatabaseFunction) defaultValue);
                } else {
                    columnConfig.setDefaultValue(defaultValue.toString());
                }

                if (column.getRemarks() != null) {
                    columnConfig.setRemarks(column.getRemarks());
                }

                change.addColumn(columnConfig);
            }

            changes.add(generateChangeSet(change));
        }
    }

    protected void addUnexpectedTableChanges(List<ChangeSet> changes) {
        for (Table unexpectedTable : diffResult.getObjectDiff(Table.class).getUnexpected()) {
            DropTableChange change = new DropTableChange();
            change.setTableName(unexpectedTable.getName());
            if (diffOutputConfig.isIncludeCatalog()) {
                change.setCatalogName(unexpectedTable.getSchema().getCatalog().getName());
            }
            if (diffOutputConfig.isIncludeSchema()) {
                change.setSchemaName(unexpectedTable.getSchema().getName());
            }

            changes.add(generateChangeSet(change));
        }
    }

    protected void addInsertDataChanges(List<ChangeSet> changeSets, String dataDir) throws DatabaseException, IOException {
        try {
            for (Schema schema : diffResult.getReferenceSnapshot().getSchemas()) {
                for (Table table : diffResult.getReferenceSnapshot().getDatabaseObjects(schema, Table.class)) {
                    if (diffResult.getReferenceSnapshot().getDatabase().isLiquibaseTable(schema, table.getName())) {
                        continue;
                    }
                    List<Change> changes = new ArrayList<Change>();
                    List<Map> rs = ExecutorService.getInstance().getExecutor(diffResult.getReferenceSnapshot().getDatabase()).queryForList(new RawSqlStatement("SELECT * FROM " + diffResult.getReferenceSnapshot().getDatabase().escapeTableName(schema.getCatalog().getName(), schema.getName(), table.getName())));

                    if (rs.size() == 0) {
                        continue;
                    }

                    List<String> columnNames = new ArrayList<String>();
                    for (Column column : table.getColumns()) {
                        columnNames.add(column.getName());
                    }

                    // if dataDir is not null, print out a csv file and use loadData
                    // tag
                    if (dataDir != null) {
                        String fileName = table.getName().toLowerCase() + ".csv";
                        if (dataDir != null) {
                            fileName = dataDir + "/" + fileName;
                        }

                        File parentDir = new File(dataDir);
                        if (!parentDir.exists()) {
                            parentDir.mkdirs();
                        }
                        if (!parentDir.isDirectory()) {
                            throw new RuntimeException(parentDir
                                    + " is not a directory");
                        }

                        CSVWriter outputFile = new CSVWriter(new BufferedWriter(new FileWriter(fileName)));
                        String[] dataTypes = new String[columnNames.size()];
                        String[] line = new String[columnNames.size()];
                        for (int i = 0; i < columnNames.size(); i++) {
                            line[i] = columnNames.get(i);
                        }
                        outputFile.writeNext(line);

                        for (Map row : rs) {
                            line = new String[columnNames.size()];

                            for (int i = 0; i < columnNames.size(); i++) {
                                Object value = row.get(columnNames.get(i).toUpperCase());
                                if (dataTypes[i] == null && value != null) {
                                    if (value instanceof Number) {
                                        dataTypes[i] = "NUMERIC";
                                    } else if (value instanceof Boolean) {
                                        dataTypes[i] = "BOOLEAN";
                                    } else if (value instanceof Date) {
                                        dataTypes[i] = "DATE";
                                    } else {
                                        dataTypes[i] = "STRING";
                                    }
                                }
                                if (value == null) {
                                    line[i] = "NULL";
                                } else {
                                    if (value instanceof Date) {
                                        line[i] = new ISODateFormat().format(((Date) value));
                                    } else {
                                        line[i] = value.toString();
                                    }
                                }
                            }
                            outputFile.writeNext(line);
                        }
                        outputFile.flush();
                        outputFile.close();

                        LoadDataChange change = new LoadDataChange();
                        change.setFile(fileName);
                        change.setEncoding("UTF-8");
                        if (diffOutputConfig.isIncludeCatalog()) {
                            change.setCatalogName(schema.getCatalog().getName());
                        }
                        if (diffOutputConfig.isIncludeSchema()) {
                            change.setSchemaName(schema.getName());
                        }
                        change.setTableName(table.getName());

                        for (int i = 0; i < columnNames.size(); i++) {
                            String colName = columnNames.get(i);
                            LoadDataColumnConfig columnConfig = new LoadDataColumnConfig();
                            columnConfig.setHeader(colName);
                            columnConfig.setName(colName);
                            columnConfig.setType(dataTypes[i]);

                            change.addColumn(columnConfig);
                        }

                        changes.add(change);
                    } else { // if dataDir is null, build and use insert tags
                        for (Map row : rs) {
                            InsertDataChange change = new InsertDataChange();
                            if (diffOutputConfig.isIncludeCatalog()) {
                                change.setCatalogName(schema.getCatalog().getName());
                            }
                            if (diffOutputConfig.isIncludeSchema()) {
                                change.setSchemaName(schema.getName());
                            }
                            change.setTableName(table.getName());

                            // loop over all columns for this row
                            for (int i = 0; i < columnNames.size(); i++) {
                                ColumnConfig column = new ColumnConfig();
                                column.setName(columnNames.get(i));

                                Object value = row.get(columnNames.get(i).toUpperCase());
                                if (value == null) {
                                    column.setValue(null);
                                } else if (value instanceof Number) {
                                    column.setValueNumeric((Number) value);
                                } else if (value instanceof Boolean) {
                                    column.setValueBoolean((Boolean) value);
                                } else if (value instanceof Date) {
                                    column.setValueDate((Date) value);
                                } else { // string
                                    column.setValue(value.toString().replace("\\", "\\\\"));
                                }

                                change.addColumn(column);

                            }

                            // for each row, add a new change
                            // (there will be one group per table)
                            changes.add(change);
                        }

                    }
                    if (changes.size() > 0) {
                        ChangeSet changeSet = generateChangeSet();
                        for (Change change : changes) {
                            changeSet.addChange(change);
                        }
                        changeSets.add(changeSet);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
