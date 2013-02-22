package liquibase.diff.output.changelog;

import liquibase.CatalogAndSchema;
import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.ChangeLogSerializerFactory;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
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


    private static Set<Class> loggedOrderFor = new HashSet<Class>();

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

        List<ChangeSet> changeSets = generateChangeSets();

        changeLogSerializer.write(changeSets, out);

        out.flush();
    }

    public List<ChangeSet> generateChangeSets() {
        final ChangeGeneratorFactory changeGeneratorFactory = ChangeGeneratorFactory.getInstance();

        List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
        List<Class<? extends DatabaseObject>> types = getOrderedOutputTypes(MissingObjectChangeGenerator.class);
        for (Class<? extends DatabaseObject> type : types) {
            ObjectQuotingStrategy quotingStrategy = ObjectQuotingStrategy.LEGACY;
            for (DatabaseObject object : diffResult.getMissingObjects(type)) {
                if (object == null) {
                    continue;
                }
                String objectName = object.getName();
                Database targetDatabase = diffResult.getReferenceSnapshot().getDatabase();
                if (!objectName.equals(targetDatabase.correctObjectName(objectName, object.getClass()))) {
                    quotingStrategy = ObjectQuotingStrategy.QUOTE_ALL_OBJECTS;
                }
                Change[] changes = changeGeneratorFactory.fixMissing(object, diffOutputControl, diffResult.getReferenceSnapshot().getDatabase(), diffResult.getComparisonSnapshot().getDatabase());
                if (!diffResult.getReferenceSnapshot().getDatabase().isLiquibaseObject(object) && !diffResult.getReferenceSnapshot().getDatabase().isSystemObject(object)) {
                    addToChangeSets(changes, changeSets, quotingStrategy);
                }
            }
        }

        types = getOrderedOutputTypes(UnexpectedObjectChangeGenerator.class);
        for (Class<? extends DatabaseObject> type : types) {
            ObjectQuotingStrategy quotingStrategy = ObjectQuotingStrategy.LEGACY;
            for (DatabaseObject object : diffResult.getUnexpectedObjects(type)) {
                Change[] changes = changeGeneratorFactory.fixUnexpected(object, diffOutputControl, diffResult.getReferenceSnapshot().getDatabase(), diffResult.getComparisonSnapshot().getDatabase());
                if (!diffResult.getComparisonSnapshot().getDatabase().isLiquibaseObject(object) && !diffResult.getComparisonSnapshot().getDatabase().isSystemObject(object)) {
                    String objectName = object.getName();
                    Database targetDatabase = diffResult.getReferenceSnapshot().getDatabase();
                    if (!objectName.equals(targetDatabase.correctObjectName(objectName, object.getClass()))) {
                        quotingStrategy = ObjectQuotingStrategy.QUOTE_ALL_OBJECTS;
                    }
                    addToChangeSets(changes, changeSets, quotingStrategy);
                }
            }
        }

        types = getOrderedOutputTypes(ChangedObjectChangeGenerator.class);
        for (Class<? extends DatabaseObject> type : types) {
            ObjectQuotingStrategy quotingStrategy = ObjectQuotingStrategy.LEGACY;
            for (Map.Entry<DatabaseObject, ObjectDifferences> entry : diffResult.getChangedObjects(type).entrySet()) {
                Change[] changes = changeGeneratorFactory.fixChanged(entry.getKey(), entry.getValue(), diffOutputControl, diffResult.getReferenceSnapshot().getDatabase(), diffResult.getComparisonSnapshot().getDatabase());
                if (!diffResult.getReferenceSnapshot().getDatabase().isLiquibaseObject(entry.getKey()) && !diffResult.getReferenceSnapshot().getDatabase().isSystemObject(entry.getKey())) {
                    String objectName = entry.getKey().getName();
                    Database targetDatabase = diffResult.getReferenceSnapshot().getDatabase();
                    if (!objectName.equals(targetDatabase.correctObjectName(objectName, entry.getKey().getClass()))) {
                        quotingStrategy = ObjectQuotingStrategy.QUOTE_ALL_OBJECTS;
                    }
                    addToChangeSets(changes, changeSets, quotingStrategy);
                }
            }
        }
        return changeSets;
    }

    private List<Class<? extends DatabaseObject>> getOrderedOutputTypes(Class<? extends ChangeGenerator> generatorType) {

        Database comparisonDatabase = diffResult.getComparisonSnapshot().getDatabase();
        DependencyGraph graph = new DependencyGraph();
        for (Class<? extends DatabaseObject> type : diffResult.getReferenceSnapshot().getSnapshotControl().getTypesToInclude()) {
            graph.addType(type);
        }
        List<Class<? extends DatabaseObject>> types = graph.sort(comparisonDatabase, generatorType);

        if (!loggedOrderFor.contains(generatorType)) {
            String log = generatorType.getSimpleName()+" type order: ";
            for (Class<? extends DatabaseObject> type : types) {
                log += "    " + type.getName();
            }
            LogFactory.getLogger().debug(log);
            loggedOrderFor.add(generatorType);
        }

        return types;
    }

    private void addToChangeSets(Change[] changes, List<ChangeSet> changeSets, ObjectQuotingStrategy quotingStrategy) {
        if (changes != null) {
            for (Change change : changes) {
                changeSets.add(generateChangeSet(change, quotingStrategy));
            }
        }
    }

    protected ChangeSet generateChangeSet(Change change, ObjectQuotingStrategy quotingStrategy) {
        ChangeSet changeSet = new ChangeSet(generateId(), getChangeSetAuthor(), false, false,
                null, changeSetContext, null, quotingStrategy);
        changeSet.addChange(change);

        return changeSet;
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


    private static class DependencyGraph {

        private Map<Class<? extends DatabaseObject>, Node> allNodes = new HashMap<Class<? extends DatabaseObject>, Node>();

        private void addType(Class<? extends DatabaseObject> type) {
            allNodes.put(type, new Node(type));
        }

        public List<Class<? extends DatabaseObject>> sort(Database database, Class<? extends ChangeGenerator> generatorType) {
            ChangeGeneratorFactory changeGeneratorFactory = ChangeGeneratorFactory.getInstance();
            for (Class<? extends DatabaseObject> type : allNodes.keySet()) {
                for (Class<? extends DatabaseObject> afterType : changeGeneratorFactory.runBeforeTypes(type, database, generatorType)) {
                    getNode(type).addEdge(getNode(afterType));
                }

                for (Class<? extends DatabaseObject> beforeType : changeGeneratorFactory.runAfterTypes(type, database, generatorType)) {
                    getNode(beforeType).addEdge(getNode(type));
                }
            }


            //L <- Empty list that will contain the sorted elements
            ArrayList<Node> L = new ArrayList<Node>();

            //S <- Set of all nodes with no incoming edges
            HashSet<Node> S = new HashSet<Node>();
            for (Node n : allNodes.values()) {
                if (n.inEdges.size() == 0) {
                    S.add(n);
                }
            }

            //while S is non-empty do
            while (!S.isEmpty()) {
                //remove a node n from S
                Node n = S.iterator().next();
                S.remove(n);

                //insert n into L
                L.add(n);

                //for each node m with an edge e from n to m do
                for (Iterator<Edge> it = n.outEdges.iterator(); it.hasNext(); ) {
                    //remove edge e from the graph
                    Edge e = it.next();
                    Node m = e.to;
                    it.remove();//Remove edge from n
                    m.inEdges.remove(e);//Remove edge from m

                    //if m has no other incoming edges then insert m into S
                    if (m.inEdges.isEmpty()) {
                        S.add(m);
                    }
                }
            }
            //Check to see if all edges are removed
            for (Node n : allNodes.values()) {
                if (!n.inEdges.isEmpty()) {
                    String message = "Could not resolve " + generatorType.getSimpleName() + " dependencies due to dependency cycle. Dependencies: \n";
                    for (Node node : allNodes.values()) {
                        SortedSet<String> fromTypes = new TreeSet<String>();
                        SortedSet<String> toTypes = new TreeSet<String>();
                        for (Edge edge : node.inEdges) {
                            fromTypes.add(edge.from.type.getSimpleName());
                        }
                        for (Edge edge : node.outEdges) {
                            toTypes.add(edge.to.type.getSimpleName());
                        }
                        String from = StringUtils.join(fromTypes, ",");
                        String to = StringUtils.join(toTypes, ",");
                        message += "    ["+ from +"] -> "+ node.type.getSimpleName()+" -> [" + to +"]\n";
                    }

                    throw new UnexpectedLiquibaseException(message);
                }
            }
            List<Class<? extends DatabaseObject>> returnList = new ArrayList<Class<? extends DatabaseObject>>();
            for (Node node : L) {
                returnList.add(node.type);
            }
            return returnList;
        }


        private Node getNode(Class<? extends DatabaseObject> type) {
            return allNodes.get(type);
        }


        static class Node {
            public final Class<? extends DatabaseObject> type;
            public final HashSet<Edge> inEdges;
            public final HashSet<Edge> outEdges;

            public Node(Class<? extends DatabaseObject> type) {
                this.type = type;
                inEdges = new HashSet<Edge>();
                outEdges = new HashSet<Edge>();
            }

            public Node addEdge(Node node) {
                Edge e = new Edge(this, node);
                outEdges.add(e);
                node.inEdges.add(e);
                return this;
            }

            @Override
            public String toString() {
                return type.getName();
            }
        }

        static class Edge {
            public final Node from;
            public final Node to;

            public Edge(Node from, Node to) {
                this.from = from;
                this.to = to;
            }

            @Override
            public boolean equals(Object obj) {
                Edge e = (Edge) obj;
                return e.from == from && e.to == to;
            }
        }
    }
}
