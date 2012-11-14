package liquibase.diff.output.changelog;

import liquibase.CatalogAndSchema;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.*;
import liquibase.changelog.ChangeSet;
import liquibase.datatype.DataTypeFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.ChangeLogSerializerFactory;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.util.StringUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class DiffToChangeLog {

    private String idRoot = String.valueOf(new Date().getTime());
    private int changeNumber = 1;

    private String changeSetContext;
    private String changeSetAuthor;
    private DiffResult diffResult;
    private DiffOutputControl diffOutputControl;

    public DiffToChangeLog(DiffResult diffResult, DiffOutputControl diffOutputControl) {
        this.diffResult = diffResult;
        this.diffOutputControl = diffOutputControl;
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

        ChangeGeneratorFactory changeGeneratorFactory = ChangeGeneratorFactory.getInstance();

        for (DatabaseObject object : diffResult.getMissingObjects()) {
            Change[] changes = changeGeneratorFactory.fixMissing(object, diffOutputControl, diffResult.getReferenceSnapshot().getDatabase(), diffResult.getComparisonSnapshot().getDatabase());
            if (changes != null) {
                for (Change change : changes) {
                    changeSets.add(generateChangeSet(change));
                }
            }
        }

        for (DatabaseObject object : diffResult.getUnexpectedObjects()) {
            Change[] changes = changeGeneratorFactory.fixUnexpected(object, diffOutputControl, diffResult.getReferenceSnapshot().getDatabase(), diffResult.getComparisonSnapshot().getDatabase());
            if (changes != null) {
                for (Change change : changes) {
                    changeSets.add(generateChangeSet(change));
                }
            }
        }

        for (Map.Entry<DatabaseObject, ObjectDifferences> entry : diffResult.getChangedObjects().entrySet()) {
            Change[] changes = changeGeneratorFactory.fixChanged(entry.getKey(), entry.getValue(), diffOutputControl, diffResult.getReferenceSnapshot().getDatabase(), diffResult.getComparisonSnapshot().getDatabase());
            if (changes != null) {
                for (Change change : changes) {
                    changeSets.add(generateChangeSet(change));
                }
            }
        }

//        addMissingTableChanges(changeSets);
//        addMissingColumnChanges(changeSets);
//        addChangedColumnChanges(changeSets);
//        addMissingPrimaryKeyChanges(changeSets);
//        addUnexpectedPrimaryKeyChanges(changeSets);
//        addUnexpectedForeignKeyChanges(changeSets);
//        addMissingUniqueConstraintChanges(changeSets);
//        addUnexpectedUniqueConstraintChanges(changeSets);
//
////todo        if (diffResult.getData().wasCompared()) {
////            addInsertDataChanges(changeSets, diffResult.getCompareControl().getDataDir());
////        }
//
//        addMissingForeignKeyChanges(changeSets);
//        addUnexpectedIndexChanges(changeSets);
//        addMissingIndexChanges(changeSets);
//        addUnexpectedColumnChanges(changeSets);
//        addMissingSequenceChanges(changeSets);
//        addUnexpectedSequenceChanges(changeSets);
//        addMissingViewChanges(changeSets);
//        addUnexpectedViewChanges(changeSets);
//        addChangedViewChanges(changeSets);
//        addUnexpectedTableChanges(changeSets);

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

    protected boolean shouldModifyColumn(Column column) {
        CatalogAndSchema schema = column.getRelation().getSchema().toCatalogAndSchema();
        return !diffResult.getReferenceSnapshot().getDatabase().isLiquibaseTable(schema, column.getRelation().getName());

    }

    protected void addInsertDataChanges(List<ChangeSet> changeSets, String dataDir) throws DatabaseException, IOException {
//TODO        try {
//            for (Schema schema : diffResult.getReferenceSnapshot().getSchemas()) {
//                for (Table table : diffResult.getReferenceSnapshot().getDatabaseObjects(schema, Table.class)) {
//                    if (diffResult.getReferenceSnapshot().getDatabase().isLiquibaseTable(schema, table.getName())) {
//                        continue;
//                    }
//                    List<Change> changes = new ArrayList<Change>();
//                    List<Map> rs = ExecutorService.getInstance().getExecutor(diffResult.getReferenceSnapshot().getDatabase()).queryForList(new RawSqlStatement("SELECT * FROM " + diffResult.getReferenceSnapshot().getDatabase().escapeTableName(schema.getCatalog().getName(), schema.getName(), table.getName())));
//
//                    if (rs.size() == 0) {
//                        continue;
//                    }
//
//                    List<String> columnNames = new ArrayList<String>();
//                    for (Column column : table.getColumns()) {
//                        columnNames.add(column.getName());
//                    }
//
//                    // if dataDir is not null, print out a csv file and use loadData
//                    // tag
//                    if (dataDir != null) {
//                        String fileName = table.getName().toLowerCase() + ".csv";
//                        if (dataDir != null) {
//                            fileName = dataDir + "/" + fileName;
//                        }
//
//                        File parentDir = new File(dataDir);
//                        if (!parentDir.exists()) {
//                            parentDir.mkdirs();
//                        }
//                        if (!parentDir.isDirectory()) {
//                            throw new RuntimeException(parentDir
//                                    + " is not a directory");
//                        }
//
//                        CSVWriter outputFile = new CSVWriter(new BufferedWriter(new FileWriter(fileName)));
//                        String[] dataTypes = new String[columnNames.size()];
//                        String[] line = new String[columnNames.size()];
//                        for (int i = 0; i < columnNames.size(); i++) {
//                            line[i] = columnNames.get(i);
//                        }
//                        outputFile.writeNext(line);
//
//                        for (Map row : rs) {
//                            line = new String[columnNames.size()];
//
//                            for (int i = 0; i < columnNames.size(); i++) {
//                                Object value = row.get(columnNames.get(i).toUpperCase());
//                                if (dataTypes[i] == null && value != null) {
//                                    if (value instanceof Number) {
//                                        dataTypes[i] = "NUMERIC";
//                                    } else if (value instanceof Boolean) {
//                                        dataTypes[i] = "BOOLEAN";
//                                    } else if (value instanceof Date) {
//                                        dataTypes[i] = "DATE";
//                                    } else {
//                                        dataTypes[i] = "STRING";
//                                    }
//                                }
//                                if (value == null) {
//                                    line[i] = "NULL";
//                                } else {
//                                    if (value instanceof Date) {
//                                        line[i] = new ISODateFormat().format(((Date) value));
//                                    } else {
//                                        line[i] = value.toString();
//                                    }
//                                }
//                            }
//                            outputFile.writeNext(line);
//                        }
//                        outputFile.flush();
//                        outputFile.close();
//
//                        LoadDataChange change = new LoadDataChange();
//                        change.setFile(fileName);
//                        change.setEncoding("UTF-8");
//                        if (diffOutputConfig.isIncludeCatalog()) {
//                            change.setCatalogName(schema.getCatalog().getName());
//                        }
//                        if (diffOutputConfig.isIncludeSchema()) {
//                            change.setSchemaName(schema.getName());
//                        }
//                        change.setTableName(table.getName());
//
//                        for (int i = 0; i < columnNames.size(); i++) {
//                            String colName = columnNames.get(i);
//                            LoadDataColumnConfig columnConfig = new LoadDataColumnConfig();
//                            columnConfig.setHeader(colName);
//                            columnConfig.setName(colName);
//                            columnConfig.setType(dataTypes[i]);
//
//                            change.addColumn(columnConfig);
//                        }
//
//                        changes.add(change);
//                    } else { // if dataDir is null, build and use insert tags
//                        for (Map row : rs) {
//                            InsertDataChange change = new InsertDataChange();
//                            if (diffOutputConfig.isIncludeCatalog()) {
//                                change.setCatalogName(schema.getCatalog().getName());
//                            }
//                            if (diffOutputConfig.isIncludeSchema()) {
//                                change.setSchemaName(schema.getName());
//                            }
//                            change.setTableName(table.getName());
//
//                            // loop over all columns for this row
//                            for (int i = 0; i < columnNames.size(); i++) {
//                                ColumnConfig column = new ColumnConfig();
//                                column.setName(columnNames.get(i));
//
//                                Object value = row.get(columnNames.get(i).toUpperCase());
//                                if (value == null) {
//                                    column.setValue(null);
//                                } else if (value instanceof Number) {
//                                    column.setValueNumeric((Number) value);
//                                } else if (value instanceof Boolean) {
//                                    column.setValueBoolean((Boolean) value);
//                                } else if (value instanceof Date) {
//                                    column.setValueDate((Date) value);
//                                } else { // string
//                                    column.setValue(value.toString().replace("\\", "\\\\"));
//                                }
//
//                                change.addColumn(column);
//
//                            }
//
//                            // for each row, add a new change
//                            // (there will be one group per table)
//                            changes.add(change);
//                        }
//
//                    }
//                    if (changes.size() > 0) {
//                        ChangeSet changeSet = generateChangeSet();
//                        for (Change change : changes) {
//                            changeSet.addChange(change);
//                        }
//                        changeSets.add(changeSet);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

    }
}
