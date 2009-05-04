package liquibase.diff;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import liquibase.change.*;

import liquibase.csv.CSVWriter;
import liquibase.database.Database;
import liquibase.database.structure.*;
import liquibase.exception.JDBCException;
import liquibase.log.LogFactory;
import liquibase.parser.LiquibaseSchemaResolver;
import liquibase.parser.ChangeLogSerializer;
import liquibase.parser.xml.XMLChangeLogSerializer;
import liquibase.parser.xml.XMLChangeLogParser;
import liquibase.util.SqlUtil;
import liquibase.util.StringUtils;
import liquibase.xml.DefaultXmlWriter;
import liquibase.xml.XmlWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DiffResult {

    private Long baseId = new Date().getTime();
    private int changeNumber = 1;

    private Database baseDatabase;
    private Database targetDatabase;

    private DatabaseSnapshot baseSnapshot;
    private DatabaseSnapshot targetSnapshot;

    private DiffComparison productName;
    private DiffComparison productVersion;

    private SortedSet<Table> missingTables = new TreeSet<Table>();
    private SortedSet<Table> unexpectedTables = new TreeSet<Table>();

    private SortedSet<View> missingViews = new TreeSet<View>();
    private SortedSet<View> unexpectedViews = new TreeSet<View>();

    private SortedSet<Column> missingColumns = new TreeSet<Column>();
    private SortedSet<Column> unexpectedColumns = new TreeSet<Column>();
    private SortedSet<Column> changedColumns = new TreeSet<Column>();

    private SortedSet<ForeignKey> missingForeignKeys = new TreeSet<ForeignKey>();
    private SortedSet<ForeignKey> unexpectedForeignKeys = new TreeSet<ForeignKey>();

    private SortedSet<Index> missingIndexes = new TreeSet<Index>();
    private SortedSet<Index> unexpectedIndexes = new TreeSet<Index>();

    private SortedSet<PrimaryKey> missingPrimaryKeys = new TreeSet<PrimaryKey>();
    private SortedSet<PrimaryKey> unexpectedPrimaryKeys = new TreeSet<PrimaryKey>();

    private SortedSet<UniqueConstraint> missingUniqueConstraints = new TreeSet<UniqueConstraint>();
    private SortedSet<UniqueConstraint> unexpectedUniqueConstraints = new TreeSet<UniqueConstraint>();

    private SortedSet<Sequence> missingSequences = new TreeSet<Sequence>();
    private SortedSet<Sequence> unexpectedSequences = new TreeSet<Sequence>();

    private boolean diffData = false;
    private String dataDir = null;
    private String changeSetContext;
    private String changeSetAuthor;

    public DiffResult(DatabaseSnapshot baseDatabase, DatabaseSnapshot targetDatabase) {
        this.baseDatabase = baseDatabase.getDatabase();
        this.targetDatabase = targetDatabase.getDatabase();

        this.baseSnapshot = baseDatabase;
        this.targetSnapshot = targetDatabase;
    }

    public DiffComparison getProductName() {
        return productName;
    }

    public void setProductName(DiffComparison productName) {
        this.productName = productName;
    }

    public DiffComparison getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(DiffComparison product) {
        this.productVersion = product;
    }

    public void addMissingTable(Table table) {
        missingTables.add(table);
    }

    public SortedSet<Table> getMissingTables() {
        return missingTables;
    }

    public void addUnexpectedTable(Table table) {
        unexpectedTables.add(table);
    }

    public SortedSet<Table> getUnexpectedTables() {
        return unexpectedTables;
    }

    public void addMissingView(View viewName) {
        missingViews.add(viewName);
    }

    public SortedSet<View> getMissingViews() {
        return missingViews;
    }

    public void addUnexpectedView(View viewName) {
        unexpectedViews.add(viewName);
    }

    public SortedSet<View> getUnexpectedViews() {
        return unexpectedViews;
    }

    public void addMissingColumn(Column columnName) {
        missingColumns.add(columnName);
    }

    public SortedSet<Column> getMissingColumns() {
        return missingColumns;
    }

    public void addUnexpectedColumn(Column columnName) {
        unexpectedColumns.add(columnName);
    }

    public SortedSet<Column> getUnexpectedColumns() {
        return unexpectedColumns;
    }

    public void addChangedColumn(Column columnName) {
        changedColumns.add(columnName);
    }

    public SortedSet<Column> getChangedColumns() {
        return changedColumns;
    }

    public void addMissingForeignKey(ForeignKey fkName) {
        missingForeignKeys.add(fkName);
    }

    public SortedSet<ForeignKey> getMissingForeignKeys() {
        return missingForeignKeys;
    }

    public void addUnexpectedForeignKey(ForeignKey fkName) {
        unexpectedForeignKeys.add(fkName);
    }

    public SortedSet<ForeignKey> getUnexpectedForeignKeys() {
        return unexpectedForeignKeys;
    }

    public void addMissingIndex(Index fkName) {
        missingIndexes.add(fkName);
    }

    public SortedSet<Index> getMissingIndexes() {
        return missingIndexes;
    }

    public void addUnexpectedIndex(Index fkName) {
        unexpectedIndexes.add(fkName);
    }

    public SortedSet<Index> getUnexpectedIndexes() {
        return unexpectedIndexes;
    }

    public void addMissingPrimaryKey(PrimaryKey primaryKey) {
        missingPrimaryKeys.add(primaryKey);
    }

    public SortedSet<PrimaryKey> getMissingPrimaryKeys() {
        return missingPrimaryKeys;
    }

    public void addUnexpectedPrimaryKey(PrimaryKey primaryKey) {
        unexpectedPrimaryKeys.add(primaryKey);
    }

    public SortedSet<PrimaryKey> getUnexpectedPrimaryKeys() {
        return unexpectedPrimaryKeys;
    }

    public void addMissingSequence(Sequence sequence) {
        missingSequences.add(sequence);
    }

    public SortedSet<Sequence> getMissingSequences() {
        return missingSequences;
    }

    public void addUnexpectedSequence(Sequence sequence) {
        unexpectedSequences.add(sequence);
    }

    public SortedSet<Sequence> getUnexpectedSequences() {
        return unexpectedSequences;
    }

    public void addMissingUniqueConstraint(UniqueConstraint uniqueConstraint) {
        missingUniqueConstraints.add(uniqueConstraint);
    }

    public SortedSet<UniqueConstraint> getMissingUniqueConstraints() {
        return this.missingUniqueConstraints;
    }

    public void addUnexpectedUniqueConstraint(UniqueConstraint uniqueConstraint) {
        unexpectedUniqueConstraints.add(uniqueConstraint);
    }

    public SortedSet<UniqueConstraint> getUnexpectedUniqueConstraints() {
        return unexpectedUniqueConstraints;
    }

    public boolean shouldDiffData() {
        return diffData;
    }

    public void setDiffData(boolean diffData) {
        this.diffData = diffData;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String getChangeSetContext() {
        return changeSetContext;
    }

    public void setChangeSetContext(String changeSetContext) {
        this.changeSetContext = changeSetContext;
    }

    public void printResult(PrintStream out) throws JDBCException {
        out.println("Base Database: " + targetDatabase.getConnectionUsername() + " " + targetDatabase.getConnectionURL());
        out.println("Target Database: " + baseDatabase.getConnectionUsername() + " " + baseDatabase.getConnectionURL());

        printComparision("Product Name", productName, out);
        printComparision("Product Version", productVersion, out);
        printSetComparison("Missing Tables", getMissingTables(), out);
        printSetComparison("Unexpected Tables", getUnexpectedTables(), out);
        printSetComparison("Missing Views", getMissingViews(), out);
        printSetComparison("Unexpected Views", getUnexpectedViews(), out);
        printSetComparison("Missing Columns", getMissingColumns(), out);
        printSetComparison("Unexpected Columns", getUnexpectedColumns(), out);
        printColumnComparison(getChangedColumns(), out);
        printSetComparison("Missing Foreign Keys", getMissingForeignKeys(), out);
        printSetComparison("Unexpected Foreign Keys", getUnexpectedForeignKeys(), out);
        printSetComparison("Missing Primary Keys", getMissingPrimaryKeys(), out);
        printSetComparison("Unexpected Primary Keys", getUnexpectedPrimaryKeys(), out);
        printSetComparison("Missing Unique Constraints", getMissingUniqueConstraints(), out);
        printSetComparison("Unexpected Unique Constraints", getUnexpectedUniqueConstraints(), out);
        printSetComparison("Missing Indexes", getMissingIndexes(), out);
        printSetComparison("Unexpected Indexes", getUnexpectedIndexes(), out);
        printSetComparison("Missing Sequences", getMissingSequences(), out);
        printSetComparison("Unexpected Sequences", getUnexpectedSequences(), out);
    }

    private void printSetComparison(String title, SortedSet<?> objects, PrintStream out) {
        out.print(title + ": ");
        if (objects.size() == 0) {
            out.println("NONE");
        } else {
            out.println();
            for (Object object : objects) {
                out.println("     " + object);
            }
        }
    }

    private void printColumnComparison(SortedSet<Column> changedColumns, PrintStream out) {
        out.print("Changed Columns: ");
        if (changedColumns.size() == 0) {
            out.println("NONE");
        } else {
            out.println();
            for (Column column : changedColumns) {
                out.println("     " + column);
                Column baseColumn = baseSnapshot.getColumn(column);
                if (baseColumn != null) {
                    if (baseColumn.isDataTypeDifferent(column)) {
                        out.println("           from " + baseColumn.getDataTypeString(baseDatabase) + " to " + targetSnapshot.getColumn(column).getDataTypeString(targetDatabase));
                    }
                    if (baseColumn.isNullabilityDifferent(column)) {
                        Boolean nowNullable = targetSnapshot.getColumn(column).isNullable();
                        if (nowNullable == null) {
                            nowNullable = Boolean.TRUE;
                        }
                        if (nowNullable) {
                            out.println("           now nullable");
                        } else {
                            out.println("           now not null");
                        }
                    }
                }
            }
        }
    }

    private void printComparision(String title, DiffComparison comparison, PrintStream out) {
        out.print(title + ":");
        if (comparison.areTheSame()) {
            out.println(" EQUAL");
        } else {
            out.println();
            out.println("     Base:   '" + comparison.getBaseVersion() + "'");
            out.println("     Target: '" + comparison.getTargetVersion() + "'");
        }

    }

    public void printChangeLog(String changeLogFile, Database targetDatabase) throws ParserConfigurationException, IOException, JDBCException {
        this.printChangeLog(changeLogFile, targetDatabase, new DefaultXmlWriter());
    }

    public void printChangeLog(PrintStream out, Database targetDatabase) throws ParserConfigurationException, IOException, JDBCException {
        this.printChangeLog(out, targetDatabase, new DefaultXmlWriter());
    }

    public void printChangeLog(String changeLogFile, Database targetDatabase, XmlWriter xmlWriter) throws ParserConfigurationException, IOException, JDBCException {
        File file = new File(changeLogFile);
        if (!file.exists()) {
            LogFactory.getLogger().info(file + " does not exist, creating");
            FileOutputStream stream = new FileOutputStream(file);
            printChangeLog(new PrintStream(stream), targetDatabase, xmlWriter);
            stream.close();
        } else {
            LogFactory.getLogger().info(file + " exists, appending");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            printChangeLog(new PrintStream(out), targetDatabase, xmlWriter);

            String xml = new String(out.toByteArray());
            xml = xml.replaceFirst("(?ms).*<databaseChangeLog[^>]*>", "");
            xml = xml.replaceFirst("</databaseChangeLog>", "");
            xml = xml.trim();

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

//            System.out.println("resulting XML: " + xml.trim());

            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(offset);
            randomAccessFile.writeBytes("    " + xml + lineSeparator);
            randomAccessFile.writeBytes("</databaseChangeLog>" + lineSeparator);
            randomAccessFile.close();

//            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
//            fileWriter.append(xml);
//            fileWriter.close();
        }
    }

    /**
     * Prints changeLog that would bring the base database to be the same as the target database
     */
    public void printChangeLog(PrintStream out, Database targetDatabase, XmlWriter xmlWriter) throws ParserConfigurationException, IOException, JDBCException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        documentBuilder.setEntityResolver(new LiquibaseSchemaResolver());

        Document doc = documentBuilder.newDocument();

        Element changeLogElement = doc.createElement("databaseChangeLog");
        changeLogElement.setAttribute("xmlns", "http://www.liquibase.org/xml/ns/dbchangelog/" + XMLChangeLogParser.getSchemaVersion());
        changeLogElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        changeLogElement.setAttribute("xsi:schemaLocation", "http://www.liquibase.org/xml/ns/dbchangelog/" + XMLChangeLogParser.getSchemaVersion() + " http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-" + XMLChangeLogParser.getSchemaVersion() + ".xsd");

        doc.appendChild(changeLogElement);

        List<Change> changes = new ArrayList<Change>();
        addUnexpectedViewChanges(changes);
        addMissingTableChanges(changes, targetDatabase);
        addMissingColumnChanges(changes, targetDatabase);
        addChangedColumnChanges(changes);
        addMissingPrimaryKeyChanges(changes);
        addUnexpectedPrimaryKeyChanges(changes);
        addMissingUniqueConstraintChanges(changes);
        addUnexpectedUniqueConstraintChanges(changes);
        addMissingIndexChanges(changes);
        addUnexpectedIndexChanges(changes);

        if (diffData) {
            addInsertDataChanges(changes, dataDir);
        }

        addMissingForeignKeyChanges(changes);
        addUnexpectedForeignKeyChanges(changes);
        addUnexpectedColumnChanges(changes);
        addMissingSequenceChanges(changes);
        addUnexpectedSequenceChanges(changes);
        addMissingViewChanges(changes);
        addUnexpectedTableChanges(changes);

        XMLChangeLogSerializer changeLogSerializer = new XMLChangeLogSerializer(doc);
        for (Change change : changes) {
            Element changeSet = doc.createElement("changeSet");
            changeSet.setAttribute("author", getChangeSetAuthor());
            changeSet.setAttribute("id", generateId());
            if (getChangeSetContext() != null) {
                changeSet.setAttribute("context", getChangeSetContext());
            }

            changeSet.appendChild(changeLogSerializer.createNode(change));
            doc.getDocumentElement().appendChild(changeSet);
        }


        xmlWriter.write(doc, out);

        out.flush();
    }

    private String getChangeSetAuthor() {
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

    private String generateId() {
        return baseId.toString() + "-" + changeNumber++;
    }

    private void addUnexpectedIndexChanges(List<Change> changes) {
        for (Index index : getUnexpectedIndexes()) {

            DropIndexChange change = new DropIndexChange();
            change.setTableName(index.getTable().getName());
            change.setSchemaName(index.getTable().getSchema());
            change.setIndexName(index.getName());

            changes.add(change);
        }
    }

    private void addMissingIndexChanges(List<Change> changes) {
        for (Index index : getMissingIndexes()) {

            CreateIndexChange change = new CreateIndexChange();
            change.setTableName(index.getTable().getName());
            change.setSchemaName(index.getTable().getSchema());
            change.setIndexName(index.getName());
            change.setUnique(index.isUnique());

            for (String columnName : index.getColumns()) {
                ColumnConfig column = new ColumnConfig();
                column.setName(columnName);
                change.addColumn(column);
            }
            changes.add(change);
        }
    }

    private void addUnexpectedPrimaryKeyChanges(List<Change> changes) {
        for (PrimaryKey pk : getUnexpectedPrimaryKeys()) {

            if (!getUnexpectedTables().contains(pk.getTable())) {
                DropPrimaryKeyChange change = new DropPrimaryKeyChange();
                change.setTableName(pk.getTable().getName());
                change.setSchemaName(pk.getTable().getSchema());
                change.setConstraintName(pk.getName());

                changes.add(change);
            }
        }
    }

    private void addMissingPrimaryKeyChanges(List<Change> changes) {
        for (PrimaryKey pk : getMissingPrimaryKeys()) {

            AddPrimaryKeyChange change = new AddPrimaryKeyChange();
            change.setTableName(pk.getTable().getName());
            change.setSchemaName(pk.getTable().getSchema());
            change.setConstraintName(pk.getName());
            change.setColumnNames(pk.getColumnNames());

            changes.add(change);
        }
    }

    private void addUnexpectedUniqueConstraintChanges(List<Change> changes) {
        for (UniqueConstraint uc : getUnexpectedUniqueConstraints()) {

            if (!getUnexpectedTables().contains(uc.getTable())) {
                DropUniqueConstraintChange change = new DropUniqueConstraintChange();
                change.setTableName(uc.getTable().getName());
                change.setSchemaName(uc.getTable().getSchema());
                change.setConstraintName(uc.getName());

                changes.add(change);
            }
        }
    }

    private void addMissingUniqueConstraintChanges(List<Change> changes) {
        for (UniqueConstraint uc : getMissingUniqueConstraints()) {

            AddUniqueConstraintChange change = new AddUniqueConstraintChange();
            change.setTableName(uc.getTable().getName());
            change.setSchemaName(uc.getTable().getSchema());
            change.setConstraintName(uc.getName());
            change.setColumnNames(uc.getColumnNames());

            changes.add(change);
        }
    }

    private void addUnexpectedForeignKeyChanges(List<Change> changes) {
        for (ForeignKey fk : getUnexpectedForeignKeys()) {

            DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
            change.setConstraintName(fk.getName());
            change.setBaseTableName(fk.getForeignKeyTable().getName());
            change.setBaseTableSchemaName(fk.getForeignKeyTable().getSchema());

            changes.add(change);
        }
    }

    private void addMissingForeignKeyChanges(List<Change> changes) {
        for (ForeignKey fk : getMissingForeignKeys()) {

            AddForeignKeyConstraintChange change = new AddForeignKeyConstraintChange();
            change.setConstraintName(fk.getName());

            change.setReferencedTableName(fk.getPrimaryKeyTable().getName());
            change.setReferencedTableSchemaName(fk.getPrimaryKeyTable().getSchema());
            change.setReferencedColumnNames(fk.getPrimaryKeyColumns());

            change.setBaseTableName(fk.getForeignKeyTable().getName());
            change.setBaseTableSchemaName(fk.getForeignKeyTable().getSchema());
            change.setBaseColumnNames(fk.getForeignKeyColumns());

            change.setDeferrable(fk.isDeferrable());
            change.setInitiallyDeferred(fk.isInitiallyDeferred());
            change.setUpdateRule(fk.getUpdateRule());
            change.setDeleteRule(fk.getDeleteRule());

            changes.add(change);
        }
    }

    private void addUnexpectedSequenceChanges(List<Change> changes) {
        for (Sequence sequence : getUnexpectedSequences()) {

            DropSequenceChange change = new DropSequenceChange();
            change.setSequenceName(sequence.getName());
            change.setSchemaName(sequence.getSchema());
            
            changes.add(change);
        }
    }

    private void addMissingSequenceChanges(List<Change> changes) {
        for (Sequence sequence : getMissingSequences()) {

            CreateSequenceChange change = new CreateSequenceChange();
            change.setSequenceName(sequence.getName());
            change.setSchemaName(sequence.getSchema());
            
            changes.add(change);
        }
    }

    private void addUnexpectedColumnChanges(List<Change> changes) {
        for (Column column : getUnexpectedColumns()) {
            if (!shouldModifyColumn(column)) {
                continue;
            }

            DropColumnChange change = new DropColumnChange();
            change.setTableName(column.getTable().getName());
            change.setSchemaName(column.getTable().getSchema());
            change.setColumnName(column.getName());

            changes.add(change);
        }
    }

    private void addMissingViewChanges(List<Change> changes) {
        for (View view : getMissingViews()) {

            CreateViewChange change = new CreateViewChange();
            change.setViewName(view.getName());
            change.setSchemaName(view.getSchema());
            String selectQuery = view.getDefinition();
            if (selectQuery == null) {
                selectQuery = "COULD NOT DETERMINE VIEW QUERY";
            }
            change.setSelectQuery(selectQuery);

            changes.add(change);
        }
    }

    private void addChangedColumnChanges(List<Change> changes) {
        for (Column column : getChangedColumns()) {
            if (!shouldModifyColumn(column)) {
                continue;
            }

            boolean foundDifference = false;
            Column baseColumn = baseSnapshot.getColumn(column);
            if (column.isDataTypeDifferent(baseColumn)) {
                ColumnConfig columnConfig = new ColumnConfig();
                columnConfig.setName(column.getName());
                columnConfig.setType(baseColumn.getDataTypeString(targetDatabase));

                ModifyColumnChange change = new ModifyColumnChange();
                change.setTableName(column.getTable().getName());
                change.setSchemaName(column.getTable().getSchema());
                change.addColumn(columnConfig);

                changes.add(change);
                foundDifference = true;
            }
            if (column.isNullabilityDifferent(baseColumn)) {
                if (baseColumn.isNullable() == null || baseColumn.isNullable()) {
                    DropNotNullConstraintChange change = new DropNotNullConstraintChange();
                    change.setTableName(column.getTable().getName());
                    change.setSchemaName(column.getTable().getSchema());
                    change.setColumnName(column.getName());
                    change.setColumnDataType(baseColumn.getDataTypeString(targetDatabase));

                    changes.add(change);
                    foundDifference = true;
                } else {
                    AddNotNullConstraintChange change = new AddNotNullConstraintChange();
                    change.setTableName(column.getTable().getName());
                    change.setSchemaName(column.getTable().getSchema());
                    change.setColumnName(column.getName());
                    change.setColumnDataType(baseColumn.getDataTypeString(targetDatabase));

                    changes.add(change);
                    foundDifference = true;
                }

            }
            if (!foundDifference) {
                throw new RuntimeException("Unknown difference");
            }
        }
    }

    private boolean shouldModifyColumn(Column column) {
        return column.getView() == null
                && !baseDatabase.isLiquibaseTable(column.getTable().getName());

    }

    private void addUnexpectedViewChanges(List<Change> changes) {
        for (View view : getUnexpectedViews()) {

            DropViewChange change = new DropViewChange();
            change.setViewName(view.getName());
            change.setSchemaName(view.getSchema());

            changes.add(change);
        }
    }


    private void addMissingColumnChanges(List<Change> changes, Database database) {
        for (Column column : getMissingColumns()) {
            if (!shouldModifyColumn(column)) {
                continue;
            }

            AddColumnChange change = new AddColumnChange();
            change.setTableName(column.getTable().getName());
            change.setSchemaName(column.getTable().getSchema());

            ColumnConfig columnConfig = new ColumnConfig();
            columnConfig.setName(column.getName());


            String dataType = column.getDataTypeString(database);

            columnConfig.setType(dataType);

            String defaultValueString = database.convertJavaObjectToString(column.getDefaultValue());
            if(defaultValueString !=null) {
              defaultValueString  = defaultValueString.replaceFirst("'","").replaceAll("'$", "");
            }
            columnConfig.setDefaultValue(defaultValueString);

            if (column.getRemarks() != null) {
                columnConfig.setRemarks(column.getRemarks());
            }
            if (column.isNullable() != null && !column.isNullable()) {
                ConstraintsConfig constraintsConfig = columnConfig.getConstraints();
                if (constraintsConfig == null) {
                    constraintsConfig = new ConstraintsConfig();
                    columnConfig.setConstraints(constraintsConfig);
                }
                constraintsConfig.setNullable(false);
            }

            change.addColumn(columnConfig);

            changes.add(change);
        }
    }

    private void addMissingTableChanges(List<Change> changes, Database database) {
        for (Table missingTable : getMissingTables()) {
            if (baseDatabase.isLiquibaseTable(missingTable.getName())) {
                continue;
            }

            CreateTableChange change = new CreateTableChange();
            change.setTableName(missingTable.getName());
            change.setSchemaName(missingTable.getSchema());
            if (missingTable.getRemarks() != null) {
                change.setRemarks(missingTable.getRemarks());
            }

            for (Column column : missingTable.getColumns()) {
                ColumnConfig columnConfig = new ColumnConfig();
                columnConfig.setName(column.getName());
                columnConfig.setType(column.getDataTypeString(database));

                ConstraintsConfig constraintsConfig = null;
                if (column.isPrimaryKey()) {
                    PrimaryKey primaryKey = null;
                    for (PrimaryKey pk : getMissingPrimaryKeys()) {
                        if (pk.getTable().getName().equalsIgnoreCase(missingTable.getName())) {
                            primaryKey = pk;
                        }
                    }

                    if (primaryKey == null || primaryKey.getColumnNamesAsList().size() == 1) {
                        constraintsConfig = new ConstraintsConfig();
                        constraintsConfig.setPrimaryKey(true);

                        if (primaryKey != null) {
                            constraintsConfig.setPrimaryKeyName(primaryKey.getName());
                            getMissingPrimaryKeys().remove(primaryKey);
                        }
                    }
                }

                if (column.isAutoIncrement()) {
                    columnConfig.setAutoIncrement(true);
                }

                if (column.isNullable() != null && !column.isNullable()) {
                    if (constraintsConfig == null) {
                        constraintsConfig = new ConstraintsConfig();
                    }

                    constraintsConfig.setNullable(false);
                }
                if (constraintsConfig != null) {
                    columnConfig.setConstraints(constraintsConfig);
                }

                Object defaultValue = column.getDefaultValue();
                if (defaultValue == null) {
                    //do nothing
                } else if (column.isAutoIncrement()) {
                    //do nothing
                } else if (defaultValue instanceof Date) {
                    columnConfig.setDefaultValueDate((Date) defaultValue);
                } else if (defaultValue instanceof Boolean) {
                    columnConfig.setDefaultValueBoolean(((Boolean) defaultValue));
                } else if (defaultValue instanceof Number) {
                    columnConfig.setDefaultValueNumeric(((Number) defaultValue));
                } else {
                    columnConfig.setDefaultValue(defaultValue.toString());
                }

                if (column.getRemarks() != null) {
                    columnConfig.setRemarks(column.getRemarks());
                }

                change.addColumn(columnConfig);
            }

            changes.add(change);
        }
    }

    private void addUnexpectedTableChanges(List<Change> changes) {
        for (Table unexpectedTable : getUnexpectedTables()) {
            DropTableChange change = new DropTableChange();
            change.setTableName(unexpectedTable.getName());
            change.setSchemaName(unexpectedTable.getSchema());

            changes.add(change);
        }
    }

    private void addInsertDataChanges(List<Change> changes, String dataDir) throws JDBCException, IOException {
        try {
            String schema = baseSnapshot.getSchema();
            Statement stmt = baseSnapshot.getDatabase().getConnection().createStatement();
            for (Table table : baseSnapshot.getTables()) {
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + baseSnapshot.getDatabase().escapeTableName(schema, table.getName()));

                ResultSetMetaData columnData = rs.getMetaData();
                int columnCount = columnData.getColumnCount();

                // if dataDir is not null, print out a csv file and use loadData tag
                if (dataDir != null) {
                    String fileName = table.getName() + ".csv";
                    if (dataDir != null) {
                        fileName = dataDir + "/" + fileName;
                    }

                    File parentDir = new File(dataDir);
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    if (!parentDir.isDirectory()) {
                        throw new RuntimeException(parentDir + " is not a directory");
                    }

                    CSVWriter outputFile = new CSVWriter(new FileWriter(fileName));
                    outputFile.writeAll(rs, true);
                    outputFile.flush();
                    outputFile.close();

                    LoadDataChange change = new LoadDataChange();
                    change.setFile(fileName);
                    change.setEncoding("UTF-8");
                    change.setSchemaName(schema);
                    change.setTableName(table.getName());

                    for (int col = 1; col <= columnCount; col++) {
                        String colName = columnData.getColumnName(col);
                        int dataType = columnData.getColumnType(col);
                        String typeString = "STRING";
                        if (SqlUtil.isNumeric(dataType)) {
                            typeString = "NUMERIC";
                        } else if (SqlUtil.isBoolean(dataType)) {
                            typeString = "BOOLEAN";
                        } else if (SqlUtil.isDate(dataType)) {
                            typeString = "DATE";
                        }

                        LoadDataColumnConfig columnConfig = new LoadDataColumnConfig();
                        columnConfig.setHeader(colName);
                        columnConfig.setType(typeString);

                        change.addColumn(columnConfig);
                    }

                    changes.add(change);
                } else { // if dataDir is null, build and use insert tags


                    // loop over all rows
                    while (rs.next()) {
                        InsertDataChange change = new InsertDataChange();
                        change.setSchemaName(schema);
                        change.setTableName(table.getName());

                        // loop over all columns for this row
                        for (int col = 1; col <= columnCount; col++) {
                            ColumnConfig column = new ColumnConfig();
                            column.setName(columnData.getColumnName(col));

                            // set the value for this column
                            int dataType = columnData.getColumnType(col);
                            if (SqlUtil.isNumeric(dataType)) {
                                String columnValue = rs.getString(col);
                                if (columnValue == null) {
                                    column.setValueNumeric((Number) null);
                                } else {
                                    // its some sort of non-null number
                                    if (dataType == Types.DOUBLE ||
                                            dataType == Types.NUMERIC ||
                                            dataType == Types.DECIMAL) {
                                        column.setValueNumeric(new Double(columnValue));
                                    } else if (dataType == Types.FLOAT ||
                                            dataType == Types.REAL) {
                                        column.setValueNumeric(new Float(columnValue));
                                    } else {
                                        // its an integer type of column
                                        column.setValueNumeric(new Integer(columnValue));
                                    }

                                }

                            } else if (SqlUtil.isBoolean(dataType)) {
                                column.setValueBoolean(rs.getBoolean(col));
                            } else if (SqlUtil.isDate(dataType)) {
                                column.setValueDate(rs.getDate(col));
                            } else { //string
                                column.setValue(rs.getString(col));
                            }

                            change.addColumn(column);

                        }

                        // for each row, add a new change
                        // (there will be one group per table)
                        changes.add(change);
                    }

                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
