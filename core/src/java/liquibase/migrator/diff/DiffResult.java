package liquibase.migrator.diff;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import liquibase.database.Database;
import liquibase.database.structure.*;
import liquibase.migrator.change.*;
import liquibase.migrator.exception.JDBCException;
import liquibase.migrator.parser.MigratorSchemaResolver;
import liquibase.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class DiffResult {

    private Long baseId = new Date().getTime();
    private int changeNumber = 1;

    private Database baseDatabase;
    private Database targetDatabase;

    private DiffComparison productName;
    private DiffComparison productVersion;

    private SortedSet<Table> missingTables = new TreeSet<Table>();
    private SortedSet<Table> unexpectedTables = new TreeSet<Table>();

    private SortedSet<View> missingViews = new TreeSet<View>();
    private SortedSet<View> unexpectedViews = new TreeSet<View>();

    private SortedSet<Column> missingColumns = new TreeSet<Column>();
    private SortedSet<Column> unexpectedColumns = new TreeSet<Column>();

    private SortedSet<ForeignKey> missingForeignKeys = new TreeSet<ForeignKey>();
    private SortedSet<ForeignKey> unexpectedForeignKeys = new TreeSet<ForeignKey>();

    private SortedSet<Index> missingIndexes = new TreeSet<Index>();
    private SortedSet<Index> unexpectedIndexes = new TreeSet<Index>();

    private SortedSet<PrimaryKey> missingPrimaryKeys = new TreeSet<PrimaryKey>();
    private SortedSet<PrimaryKey> unexpectedPrimaryKeys = new TreeSet<PrimaryKey>();

    private SortedSet<Sequence> missingSequences = new TreeSet<Sequence>();
    private SortedSet<Sequence> unexpectedSequences = new TreeSet<Sequence>();

    public DiffResult(Database baseDatabase, Database targetDatabase) {
        this.baseDatabase = baseDatabase;
        this.targetDatabase = targetDatabase;
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

    public void printResult(PrintStream out) throws JDBCException {
        out.println("Base Database: " + baseDatabase.getConnectionUsername() + " " + baseDatabase.getConnectionURL());
        out.println("Target Database: " + targetDatabase.getConnectionUsername() + " " + targetDatabase.getConnectionURL());

        printComparision("Product Name", productName, out);
        printComparision("Product Version", productVersion, out);
        printSetComparison("Missing Tables", getMissingTables(), out);
        printSetComparison("Unexpected Tables", getUnexpectedTables(), out);
        printSetComparison("Missing Views", getMissingViews(), out);
        printSetComparison("Unexpected Views", getUnexpectedViews(), out);
        printSetComparison("Missing Columns", getMissingColumns(), out);
        printSetComparison("Unexpected Columns", getUnexpectedColumns(), out);
        printSetComparison("Missing Foreign Keys", getMissingForeignKeys(), out);
        printSetComparison("Unexpected Foreign Keys", getUnexpectedForeignKeys(), out);
        printSetComparison("Missing Primary Keys", getMissingPrimaryKeys(), out);
        printSetComparison("Unexpected Primary Keys", getUnexpectedPrimaryKeys(), out);
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

    /**
     * Prints changeLog that would bring the base database to be the same as the target database
     */
    public void printChangeLog(PrintStream out, Database targetDatabase) throws ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        documentBuilder.setEntityResolver(new MigratorSchemaResolver());

        Document doc = documentBuilder.newDocument();

        Element changeLogElement = doc.createElement("databaseChangeLog");
        changeLogElement.setAttribute("xmlns", "http://www.liquibase.org/xml/ns/dbchangelog/1.3");
        changeLogElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        changeLogElement.setAttribute("xsi:schemaLocation", "http://www.liquibase.org/xml/ns/dbchangelog/1.3 http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-1.3.xsd");

        doc.appendChild(changeLogElement);

        List<Change> changes = new ArrayList<Change>();
        addMissingTableChanges(changes, targetDatabase);
        addUnexpectedTableChanges(changes);
        addMissingColumnChanges(changes, targetDatabase);
        addUnexpectedColumnChanges(changes);
        addMissingPrimaryKeyChanges(changes);
        addUnexpectedPrimaryKeyChanges(changes);
        addMissingIndexChanges(changes);
        addUnexpectedIndexChanges(changes);
        addMissingForeignKeyChanges(changes);
        addUnexpectedForeignKeyChanges(changes);
        addMissingSequenceChanges(changes);
        addUnexpectedSequenceChanges(changes);

        for (Change change : changes) {
            Element changeSet = doc.createElement("changeSet");
            changeSet.setAttribute("author", "diff-generated");
            changeSet.setAttribute("id", generateId());

            changeSet.appendChild(change.createNode(doc));
            doc.getDocumentElement().appendChild(changeSet);
        }


        OutputFormat format = new OutputFormat(doc);
        format.setIndenting(true);
        XMLSerializer serializer = new XMLSerializer(out, format);
        serializer.asDOMSerializer();
        serializer.serialize(doc);

        out.flush();
    }

    private String generateId() {
        return baseId.toString()+"-"+changeNumber++;
    }

    private void addUnexpectedIndexChanges(List<Change> changes) {
        for (Index index : getUnexpectedIndexes()) {

            DropIndexChange change = new DropIndexChange();
            change.setTableName(index.getTableName());
            change.setIndexName(index.getName());

            changes.add(change);
        }
    }

    private void addMissingIndexChanges(List<Change> changes) {
        for (Index index : getMissingIndexes()) {

            CreateIndexChange change = new CreateIndexChange();
            change.setTableName(index.getTableName());
            change.setIndexName(index.getName());
            
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

            DropPrimaryKeyChange change = new DropPrimaryKeyChange();
            change.setTableName(pk.getTableName());
            change.setConstraintName(pk.getName());

            changes.add(change);
        }
    }

    private void addMissingPrimaryKeyChanges(List<Change> changes) {
        for (PrimaryKey pk : getMissingPrimaryKeys()) {

            AddPrimaryKeyChange change = new AddPrimaryKeyChange();
            change.setTableName(pk.getTableName());
            change.setConstraintName(pk.getName());
            change.setColumnNames(pk.getColumnNames());

            changes.add(change);
        }
    }

    private void addUnexpectedForeignKeyChanges(List<Change> changes) {
        for (ForeignKey fk : getUnexpectedForeignKeys()) {

            DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
            change.setConstraintName(fk.getName());
            change.setBaseTableName(fk.getPrimaryKeyTable().getName());

            changes.add(change);
        }
    }

    private void addMissingForeignKeyChanges(List<Change> changes) {
        for (ForeignKey fk : getMissingForeignKeys()) {

            AddForeignKeyConstraintChange change = new AddForeignKeyConstraintChange();
            change.setConstraintName(fk.getName());

            change.setReferencedTableName(fk.getPrimaryKeyTable().getName());
            change.setReferencedColumnNames(fk.getPrimaryKeyColumn());

            change.setBaseTableName(fk.getForeignKeyTable().getName());
            change.setBaseColumnNames(fk.getForeignKeyColumn());

            change.setDeferrable(fk.isDeferrable());
            change.setInitiallyDeferred(fk.isInitiallyDeferred());

            changes.add(change);
        }
    }

    private void addUnexpectedSequenceChanges(List<Change> changes) {
        for (Sequence sequence : getUnexpectedSequences()) {

            DropSequenceChange change = new DropSequenceChange();
            change.setSequenceName(sequence.getName());

            changes.add(change);
        }
    }

    private void addMissingSequenceChanges(List<Change> changes) {
        for (Sequence sequence : getMissingSequences()) {

            CreateSequenceChange change = new CreateSequenceChange();
            change.setSequenceName(sequence.getName());

            changes.add(change);
        }
    }

    private void addUnexpectedColumnChanges(List<Change> changes) {
        for (Column column : getUnexpectedColumns()) {
            if (baseDatabase.isLiquibaseTable(column.getTable().getName())) {
                continue;
            }

            DropColumnChange change = new DropColumnChange();
            change.setTableName(column.getTable().getName());
            change.setColumnName(column.getName());

            changes.add(change);
        }
    }

    private void addMissingColumnChanges(List<Change> changes, Database database) {
        for (Column column : getMissingColumns()) {
            if (baseDatabase.isLiquibaseTable(column.getTable().getName())) {
                continue;
            }

            AddColumnChange change = new AddColumnChange();
            change.setTableName(column.getTable().getName());

            ColumnConfig columnConfig = new ColumnConfig();
            columnConfig.setName(column.getName());


            String dataType = column.getDataTypeString(database);

            columnConfig.setType(dataType);

            columnConfig.setDefaultValue(StringUtils.trimToNull(column.getDefaultValue()));

            change.setColumn(columnConfig);

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

            for (Column column : missingTable.getColumns()) {
                ColumnConfig columnConfig = new ColumnConfig();
                columnConfig.setName(column.getName());
                columnConfig.setType(column.getDataTypeString(database));

                if (column.isNullable() != null && !column.isNullable()) {
                    ConstraintsConfig constraintsConfig = new ConstraintsConfig();
                    constraintsConfig.setNullable(false);
                    columnConfig.setConstraints(constraintsConfig);
                }

                if (column.isNumeric()) {
                    columnConfig.setDefaultValueNumeric(StringUtils.trimToNull(column.getDefaultValue()));
                } else {
                    columnConfig.setDefaultValue(StringUtils.trimToNull(translateDefaultValue(column.getDefaultValue())));
                }

                change.addColumn(columnConfig);
            }

            changes.add(change);
        }
    }

    private String translateDefaultValue(String defaultValue) {
        if (defaultValue != null) {
            defaultValue = defaultValue.replaceFirst("^'", "").replaceFirst("'$", "");
            defaultValue = defaultValue.replaceFirst("'\\:\\:[a-zA-Z0-9 ]+$", "");
        }
        return defaultValue;
    }

    private void addUnexpectedTableChanges(List<Change> changes) {
        for (Table unexpectedTable : getUnexpectedTables()) {
            DropTableChange change = new DropTableChange();
            change.setTableName(unexpectedTable.getName());

            changes.add(change);
        }
    }

}
