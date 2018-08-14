package liquibase.diff.output.changelog;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.OfflineConnection;
import liquibase.database.core.*;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.ChangeLogSerializerFactory;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectComparator;
import liquibase.structure.core.Column;
import liquibase.util.DependencyUtil;
import liquibase.util.StringUtil;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class DiffToChangeLog {

    public static final String ORDER_ATTRIBUTE = "order";
    public static final String DATABASE_CHANGE_LOG_CLOSING_XML_TAG = "</databaseChangeLog>";

    private String idRoot = String.valueOf(new Date().getTime());
    private boolean overriddenIdRoot;

    private int changeNumber = 1;

    private String changeSetContext;
    private String changeSetAuthor;
    private String changeSetPath;
    private DiffResult diffResult;
    private DiffOutputControl diffOutputControl;
    private boolean tryDbaDependencies=true;

    private static Set<Class> loggedOrderFor = new HashSet<>();

    public DiffToChangeLog(DiffResult diffResult, DiffOutputControl diffOutputControl) {
        this.diffResult = diffResult;
        this.diffOutputControl = diffOutputControl;
    }

    public DiffToChangeLog(DiffOutputControl diffOutputControl) {
        this.diffOutputControl = diffOutputControl;
    }

    public void setDiffResult(DiffResult diffResult) {
        this.diffResult = diffResult;
    }

    public void setChangeSetContext(String changeSetContext) {
        this.changeSetContext = changeSetContext;
    }

    public void print(String changeLogFile) throws ParserConfigurationException, IOException, DatabaseException {
        this.changeSetPath = changeLogFile;
        ChangeLogSerializer changeLogSerializer = ChangeLogSerializerFactory.getInstance().getSerializer(changeLogFile);
        this.print(changeLogFile, changeLogSerializer);
    }

    public void print(PrintStream out) throws ParserConfigurationException, IOException, DatabaseException {
        this.print(out, ChangeLogSerializerFactory.getInstance().getSerializer("xml"));
    }

    public void print(String changeLogFile, ChangeLogSerializer changeLogSerializer) throws ParserConfigurationException, IOException, DatabaseException {
        this.changeSetPath = changeLogFile;
        File file = new File(changeLogFile);
        if (!file.exists()) {
            LogService.getLog(getClass()).info(LogType.LOG, file + " does not exist, creating");
            FileOutputStream stream = new FileOutputStream(file);
            print(new PrintStream(stream, true, LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding()), changeLogSerializer);
            stream.close();
        } else {
            LogService.getLog(getClass()).info(LogType.LOG, file + " exists, appending");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            print(new PrintStream(out, true, LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding()), changeLogSerializer);

            String xml = new String(out.toByteArray(), LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding());
            String innerXml = xml.replaceFirst("(?ms).*<databaseChangeLog[^>]*>", "");

            innerXml = innerXml.replaceFirst(DATABASE_CHANGE_LOG_CLOSING_XML_TAG, "");
            innerXml = innerXml.trim();
            if ("".equals(innerXml)) {
                LogService.getLog(getClass()).info(LogType.LOG, "No changes found, nothing to do");
                return;
            }

            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {

                String line;
                long offset = 0;
                boolean foundEndTag = false;
                while ((line = randomAccessFile.readLine()) != null) {
                    int index = line.indexOf(DATABASE_CHANGE_LOG_CLOSING_XML_TAG);
                    if (index >= 0) {
                        foundEndTag = true;
                        break;
                    } else {
                        offset = randomAccessFile.getFilePointer();
                    }
                }

                String lineSeparator = LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration
                .class).getOutputLineSeparator();

                if (foundEndTag) {
                    randomAccessFile.seek(offset);
                    randomAccessFile.writeBytes("    ");
                    randomAccessFile.write(innerXml.getBytes(LiquibaseConfiguration.getInstance().getConfiguration
                    (GlobalConfiguration.class).getOutputEncoding()));
                    randomAccessFile.writeBytes(lineSeparator);
                    randomAccessFile.writeBytes(DATABASE_CHANGE_LOG_CLOSING_XML_TAG + lineSeparator);
                } else {
                    randomAccessFile.seek(0);
                    randomAccessFile.write(xml.getBytes(LiquibaseConfiguration.getInstance().getConfiguration
                    (GlobalConfiguration.class).getOutputEncoding()));
                }
                randomAccessFile.close();
            }

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
        DatabaseObjectComparator comparator = new DatabaseObjectComparator();

        String created = null;
        if (LiquibaseConfiguration.getInstance().getProperty(GlobalConfiguration.class, GlobalConfiguration.GENERATE_CHANGESET_CREATED_VALUES).getValue(Boolean.class)) {
            created = new SimpleDateFormat("yyyy-MM-dd HH:mmZ").format(new Date());
        }

        List<Class<? extends DatabaseObject>> types = getOrderedOutputTypes(ChangedObjectChangeGenerator.class);
        List<ChangeSet> updateChangeSets = new ArrayList<ChangeSet>();

        for (Class<? extends DatabaseObject> type : types) {
            ObjectQuotingStrategy quotingStrategy = diffOutputControl.getObjectQuotingStrategy();
            for (Map.Entry<? extends DatabaseObject, ObjectDifferences> entry : diffResult.getChangedObjects(type, comparator).entrySet()) {
                if (!diffResult.getReferenceSnapshot().getDatabase().isLiquibaseObject(entry.getKey()) && !diffResult.getReferenceSnapshot().getDatabase().isSystemObject(entry.getKey())) {
                    Change[] changes = changeGeneratorFactory.fixChanged(entry.getKey(), entry.getValue(), diffOutputControl, diffResult.getReferenceSnapshot().getDatabase(), diffResult.getComparisonSnapshot().getDatabase());
                    addToChangeSets(changes, updateChangeSets, quotingStrategy, created);
                }
            }
        }

        types = getOrderedOutputTypes(MissingObjectChangeGenerator.class);
        List<DatabaseObject> missingObjects = new ArrayList<DatabaseObject>();
        for (Class<? extends DatabaseObject> type : types) {
            for (DatabaseObject object : diffResult.getMissingObjects(type, new DatabaseObjectComparator() {
                @Override
                public int compare(DatabaseObject o1, DatabaseObject o2) {
                    if ((o1 instanceof Column) && (o1.getAttribute(ORDER_ATTRIBUTE, Integer.class) != null) &&
                        (o2.getAttribute(ORDER_ATTRIBUTE, Integer.class) != null)) {
                        int i = o1.getAttribute(ORDER_ATTRIBUTE, Integer.class).compareTo(o2.getAttribute(ORDER_ATTRIBUTE, Integer.class));
                        if (i != 0) {
                            return i;
                        }
                    }
                    return super.compare(o1, o2);

                }
            })) {
                if (object == null) {
                    continue;
                }
                if (!diffResult.getReferenceSnapshot().getDatabase().isLiquibaseObject(object) && !diffResult.getReferenceSnapshot().getDatabase().isSystemObject(object)) {
                    missingObjects.add(object);
                }
            }
        }

        List<ChangeSet> createChangeSets = new ArrayList<ChangeSet>();

        for (DatabaseObject object : sortMissingObjects(missingObjects, diffResult.getReferenceSnapshot().getDatabase())) {
            ObjectQuotingStrategy quotingStrategy = diffOutputControl.getObjectQuotingStrategy();

            Change[] changes = changeGeneratorFactory.fixMissing(object, diffOutputControl, diffResult.getReferenceSnapshot().getDatabase(), diffResult.getComparisonSnapshot().getDatabase());
            addToChangeSets(changes, createChangeSets, quotingStrategy, created);
        }

        List<ChangeSet> deleteChangeSets = new ArrayList<ChangeSet>();

        types = getOrderedOutputTypes(UnexpectedObjectChangeGenerator.class);
        for (Class<? extends DatabaseObject> type : types) {
            ObjectQuotingStrategy quotingStrategy = diffOutputControl.getObjectQuotingStrategy();
            for (DatabaseObject object : sortUnexpectedObjects(diffResult.getUnexpectedObjects(type, comparator), diffResult.getReferenceSnapshot().getDatabase())) {
                if (!diffResult.getComparisonSnapshot().getDatabase().isLiquibaseObject(object) && !diffResult.getComparisonSnapshot().getDatabase().isSystemObject(object)) {
                    Change[] changes = changeGeneratorFactory.fixUnexpected(object, diffOutputControl, diffResult.getReferenceSnapshot().getDatabase(), diffResult.getComparisonSnapshot().getDatabase());
                    addToChangeSets(changes, deleteChangeSets, quotingStrategy, created);
                }
            }
        }

        List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
        changeSets.addAll(createChangeSets);
        changeSets.addAll(deleteChangeSets);
        changeSets.addAll(updateChangeSets);
        return changeSets;
    }

    private List<DatabaseObject> sortUnexpectedObjects(Collection<? extends DatabaseObject> unexpectedObjects, Database database) {
        return sortObjects("unexpected", (Collection<DatabaseObject>) unexpectedObjects, database);
    }

    private List<DatabaseObject> sortMissingObjects(Collection<DatabaseObject> missingObjects, Database database) {
        return sortObjects("missing", (Collection<DatabaseObject>) missingObjects, database);
    }

    private List<DatabaseObject> sortObjects(final String type, Collection<DatabaseObject> objects, Database database) {

        if ((diffOutputControl.getSchemaComparisons() != null) && !objects.isEmpty() && supportsSortingObjects
            (database) && (database.getConnection() != null) && !(database.getConnection() instanceof OfflineConnection)) {
            List<String> schemas = new ArrayList<>();
            CompareControl.SchemaComparison[] schemaComparisons = this.diffOutputControl.getSchemaComparisons();
            if (schemaComparisons != null) {
                for (CompareControl.SchemaComparison comparison : schemaComparisons) {
                    String schemaName = comparison.getReferenceSchema().getSchemaName();
                    if (schemaName == null) {
                        schemaName = database.getDefaultSchemaName();
                    }
                    schemas.add(schemaName);
                }
            }

            if (schemas.isEmpty()) {
                schemas.add(database.getDefaultSchemaName());
            }

            try {
                final List<String> dependencyOrder = new ArrayList<>();
                DependencyUtil.NodeValueListener<String> nameListener = new DependencyUtil.NodeValueListener<String>() {
                    @Override
                    public void evaluating(String nodeValue) {
                        dependencyOrder.add(nodeValue);
                    }
                };

                DependencyUtil.DependencyGraph graph = new DependencyUtil.DependencyGraph(nameListener);
                addDependencies(graph, schemas, database);
                graph.computeDependencies();

                if (!dependencyOrder.isEmpty()) {

                    final List<DatabaseObject> toSort = new ArrayList<>();
                    final List<DatabaseObject> toNotSort = new ArrayList<>();

                    for (DatabaseObject obj : objects) {
                        if (!(obj instanceof Column)) {
                            String schemaName = null;
                            if (obj.getSchema() != null) {
                                schemaName = obj.getSchema().getName();
                            }

                            String name = schemaName + "." + obj.getName();
                            if (dependencyOrder.contains(name)) {
                                toSort.add(obj);
                            } else {
                                toNotSort.add(obj);
                            }
                        } else {
                            toNotSort.add(obj);
                        }
                    }

                    Collections.sort(toSort, new Comparator<DatabaseObject>() {
                        @Override
                        public int compare(DatabaseObject o1, DatabaseObject o2) {
                            String o1Schema = null;
                            if (o1.getSchema() != null) {
                                o1Schema = o1.getSchema().getName();
                            }

                            String o2Schema = null;
                            if (o2.getSchema() != null) {
                                o2Schema = o2.getSchema().getName();
                            }

                            Integer o1Order = dependencyOrder.indexOf(o1Schema + "." + o1.getName());
                            int o2Order = dependencyOrder.indexOf(o2Schema + "." + o2.getName());

                            int order = o1Order.compareTo(o2Order);
                            if ("unexpected".equals(type)) {
                                order = order * -1;
                            }
                            return order;
                        }
                    });

                    toSort.addAll(toNotSort);
                    return toSort;
                }
            } catch (DatabaseException e) {
                LogService.getLog(getClass()).debug(LogType.LOG, "Cannot get object dependencies: " + e.getMessage());
            }
        }

        return new ArrayList<>(objects);
    }

    private List<Map<String, ?>> queryForDependencies(Executor executor, List<String> schemas)
        throws DatabaseException {
        List<Map<String, ?>> rs = null;
        try {
            if (tryDbaDependencies) {
                rs = executor.queryForList(new RawSqlStatement("select OWNER, NAME, REFERENCED_OWNER, REFERENCED_NAME from DBA_DEPENDENCIES where REFERENCED_OWNER != 'SYS' AND NOT(NAME LIKE 'BIN$%') AND NOT(OWNER = REFERENCED_OWNER AND NAME = REFERENCED_NAME) AND (" + StringUtil.join(schemas, " OR ", new StringUtil.StringUtilFormatter<String>() {
                            @Override
                            public String toString(String obj) {
                                return "OWNER='" + obj + "'";
                            }
                        }
                    ) + ")"));
            }
            else {
                rs = executor.queryForList(new RawSqlStatement("select NAME, REFERENCED_OWNER, REFERENCED_NAME from USER_DEPENDENCIES where REFERENCED_OWNER != 'SYS' AND NOT(NAME LIKE 'BIN$%') AND NOT(NAME = REFERENCED_NAME) AND (" + StringUtil.join(schemas, " OR ", new StringUtil.StringUtilFormatter<String>() {
                                @Override
                                public String toString(String obj) {
                                    return "REFERENCED_OWNER='" + obj + "'";
                                }
                            }
                ) + ")"));
            }
        }
        catch (DatabaseException dbe) {
            //
            // If our exception is for something other than a missing table/view
            // then we just re-throw the exception
            // else if we can't see USER_DEPENDENCIES then we also re-throw
            //   to stop the recursion
            //
            String message = dbe.getMessage();
            if (! message.contains("ORA-00942: table or view does not exist")) {
              throw new DatabaseException(dbe);
            }
            else if (! tryDbaDependencies) {
              throw new DatabaseException(dbe);
            }
            LogService.getLog(getClass()).warning("Unable to query DBA_DEPENDENCIES table. Switching to USER_DEPENDENCIES");
            tryDbaDependencies = false;
            return queryForDependencies(executor, schemas);
        }
        return rs;
    }

    /**
     * Used by {@link #sortMissingObjects(Collection, Database)} to determine whether to go into the sorting logic.
     */
    protected boolean supportsSortingObjects(Database database) {
        return (database instanceof AbstractDb2Database) || (database instanceof MSSQLDatabase) || (database instanceof
            OracleDatabase);
    }

    /**
     * Adds dependencies to the graph as schema.object_name.
     */
    protected void addDependencies(DependencyUtil.DependencyGraph<String> graph, List<String> schemas, Database database) throws DatabaseException {
        if (database instanceof DB2Database) {
            Executor executor = ExecutorService.getInstance().getExecutor(database);
            List<Map<String, ?>> rs = executor.queryForList(new RawSqlStatement("select TABSCHEMA, TABNAME, BSCHEMA, BNAME from syscat.tabdep where (" + StringUtil.join(schemas, " OR ", new StringUtil.StringUtilFormatter<String>() {
                        @Override
                        public String toString(String obj) {
                            return "TABSCHEMA='" + obj + "'";
                        }
                    }
            ) + ")"));
            for (Map<String, ?> row : rs) {
                String tabName = StringUtil.trimToNull((String) row.get("TABSCHEMA")) + "." + StringUtil.trimToNull((String) row.get("TABNAME"));
                String bName = StringUtil.trimToNull((String) row.get("BSCHEMA")) + "." + StringUtil.trimToNull((String) row.get("BNAME"));

                graph.add(bName, tabName);
            }
        } else if (database instanceof Db2zDatabase) {
            Executor executor = ExecutorService.getInstance().getExecutor(database);
            String db2ZosSql = "SELECT DSCHEMA AS TABSCHEMA, DNAME AS TABNAME, BSCHEMA, BNAME FROM SYSIBM.SYSDEPENDENCIES WHERE (" + StringUtil.join(schemas, " OR ", new StringUtil.StringUtilFormatter<String>() {
                        @Override
                        public String toString(String obj) {
                            return "DSCHEMA='" + obj + "'";
                        }
                    }
            ) + ")";
            List<Map<String, ?>> rs = executor.queryForList(new RawSqlStatement(db2ZosSql));
            for (Map<String, ?> row : rs) {
                String tabName = StringUtil.trimToNull((String) row.get("TABSCHEMA")) + "." + StringUtil.trimToNull((String) row.get("TABNAME"));
                String bName = StringUtil.trimToNull((String) row.get("BSCHEMA")) + "." + StringUtil.trimToNull((String) row.get("BNAME"));

                graph.add(bName, tabName);
            }
        } else if (database instanceof OracleDatabase) {
            Executor executor = ExecutorService.getInstance().getExecutor(database);
            List<Map<String, ?>> rs = queryForDependencies(executor, schemas);
            for (Map<String, ?> row : rs) {
                String tabName = null;
                if (tryDbaDependencies) {
                    tabName =
                        StringUtil.trimToNull((String) row.get("OWNER")) + "." +
                        StringUtil.trimToNull((String) row.get("NAME"));
                }
                else {
                    tabName =
                        StringUtil.trimToNull((String) row.get("REFERENCED_OWNER")) + "." +
                        StringUtil.trimToNull((String) row.get("NAME"));
                }
                String bName =
                    StringUtil.trimToNull((String) row.get("REFERENCED_OWNER")) + "." +
                    StringUtil.trimToNull((String) row.get("REFERENCED_NAME"));

                graph.add(bName, tabName);
            }
        } else if (database instanceof MSSQLDatabase) {
            Executor executor = ExecutorService.getInstance().getExecutor(database);
            String sql = "select object_schema_name(referencing_id) as referencing_schema_name, object_name(referencing_id) as referencing_name, object_name(referenced_id) as referenced_name, object_schema_name(referenced_id) as referenced_schema_name  from sys.sql_expression_dependencies depz where (" + StringUtil.join(schemas, " OR ", new StringUtil.StringUtilFormatter<String>() {
                        @Override
                        public String toString(String obj) {
                            return "object_schema_name(referenced_id)='" + obj + "'";
                        }
                    }
            ) + ")";
            sql += " UNION select object_schema_name(object_id) as referencing_schema_name, object_name(object_id) as referencing_name, object_name(parent_object_id) as referenced_name, object_schema_name(parent_object_id) as referenced_schema_name " +
                    "from sys.objects " +
                    "where parent_object_id > 0 " +
                    "and is_ms_shipped=0 " +
                    "and (" + StringUtil.join(schemas, " OR ", new StringUtil.StringUtilFormatter<String>() {
                        @Override
                        public String toString(String obj) {
                            return "object_schema_name(object_id)='" + obj + "'";
                        }
                    }
            ) + ")";

            sql += " UNION select object_schema_name(fk.object_id) as referencing_schema_name, fk.name as referencing_name, i.name as referenced_name, object_schema_name(i.object_id) as referenced_schema_name " +
                    "from sys.foreign_keys fk " +
                    "join sys.indexes i on fk.referenced_object_id=i.object_id and fk.key_index_id=i.index_id " +
                    "where fk.is_ms_shipped=0 " +
                    "and (" + StringUtil.join(schemas, " OR ", new StringUtil.StringUtilFormatter<String>() {
                        @Override
                        public String toString(String obj) {
                            return "object_schema_name(fk.object_id)='" + obj + "'";
                        }
                    }
            ) + ")";

            sql += " UNION select object_schema_name(i.object_id) as referencing_schema_name, object_name(i.object_id) as referencing_name, s.name as referenced_name, null as referenced_schema_name " +
                    "from sys.indexes i " +
                    "join sys.partition_schemes s on i.data_space_id = s.data_space_id";

            sql += " UNION select null as referencing_schema_name, s.name as referencing_name, f.name as referenced_name, null as referenced_schema_name from sys.partition_functions f " +
                    "join sys.partition_schemes s on s.function_id=f.function_id";

            sql += " UNION select null as referencing_schema_name, s.name as referencing_name, fg.name as referenced_name, null as referenced_schema_name from sys.partition_schemes s " +
                    "join sys.destination_data_spaces ds on s.data_space_id=ds.partition_scheme_id " +
                    "join sys.filegroups fg on ds.data_space_id=fg.data_space_id";

            //get data file -> filegroup dependencies
            sql += " UNION select distinct null as referencing_schema_name, f.name as referencing_name, ds.name as referenced_name, null as referenced_schema_name from sys.database_files f " +
                    "join sys.data_spaces ds on f.data_space_id=ds.data_space_id " +
                    "where f.data_space_id > 1";

            //get table -> filestream dependencies
            sql += " UNION select object_schema_name(t.object_id) as referencing_schema_name, t.name as referencing_name, ds.name as referenced_name, null as referenced_schema_name from sys.tables t " +
                    "join sys.data_spaces ds on t.filestream_data_space_id=ds.data_space_id " +
                    "where t.filestream_data_space_id > 1";

            //get table -> filestream dependencies
            sql += " UNION select object_schema_name(t.object_id) as referencing_schema_name, t.name as referencing_name, ds.name as referenced_name, null as referenced_schema_name from sys.tables t " +
                    "join sys.data_spaces ds on t.lob_data_space_id=ds.data_space_id " +
                    "where t.lob_data_space_id > 1";

            //get index -> filegroup dependencies
            sql += " UNION select object_schema_name(i.object_id) as referencing_schema_name, i.name as referencing_name, ds.name as referenced_name, null as referenced_schema_name from sys.indexes i " +
                    "join sys.data_spaces ds on i.data_space_id=ds.data_space_id " +
                    "where i.data_space_id > 1";

            //get index -> table dependencies
            sql += " UNION select object_schema_name(i.object_id) as referencing_schema_name, i.name as referencing_name, object_name(i.object_id) as referenced_name, object_schema_name(i.object_id) as referenced_schema_name from sys.indexes i " +
                    "where " + StringUtil.join(schemas, " OR ", new StringUtil.StringUtilFormatter<String>() {
                @Override
                public String toString(String obj) {
                    return "object_schema_name(i.object_id)='" + obj + "'";
                }
            });

            //get schema -> base object dependencies
            sql += " UNION SELECT SCHEMA_NAME(SCHEMA_ID) as referencing_schema_name, name as referencing_name, PARSENAME(BASE_OBJECT_NAME,1) AS referenced_name, (CASE WHEN PARSENAME(BASE_OBJECT_NAME,2) IS NULL THEN schema_name(schema_id) else PARSENAME(BASE_OBJECT_NAME,2) END) AS referenced_schema_name FROM SYS.SYNONYMS WHERE is_ms_shipped='false' AND " + StringUtil.join(schemas, " OR ", new StringUtil.StringUtilFormatter<String>() {
                @Override
                public String toString(String obj) {
                    return "SCHEMA_NAME(SCHEMA_ID)='" + obj + "'";
                }
            });

            //get non-clustered indexes -> unique clustered indexes on views dependencies
            sql += " UNION select object_schema_name(c.object_id) as referencing_schema_name, c.name as referencing_name, object_schema_name(nc.object_id) as referenced_schema_name, nc.name as referenced_name from sys.indexes c join sys.indexes nc on c.object_id=nc.object_id JOIN sys.objects o ON c.object_id = o.object_id where  c.index_id != nc.index_id and c.type_desc='CLUSTERED' and c.is_unique='true' and (not(nc.type_desc='CLUSTERED') OR nc.is_unique='false') AND o.type_desc='VIEW' AND o.name='AR_DETAIL_OPEN'";

            List<Map<String, ?>> rs = executor.queryForList(new RawSqlStatement(sql));
            if (!rs.isEmpty()) {
                for (Map<String, ?> row : rs) {
                    String bName = StringUtil.trimToNull((String) row.get("REFERENCED_SCHEMA_NAME")) + "." + StringUtil.trimToNull((String) row.get("REFERENCED_NAME"));
                    String tabName = StringUtil.trimToNull((String) row.get("REFERENCING_SCHEMA_NAME")) + "." + StringUtil.trimToNull((String) row.get("REFERENCING_NAME"));

                    if (!bName.equals(tabName)) {
                        graph.add(bName, tabName);
                    }
                }
            }
        }
    }

    protected List<Class<? extends DatabaseObject>> getOrderedOutputTypes(Class<? extends ChangeGenerator> generatorType) {

        Database comparisonDatabase = diffResult.getComparisonSnapshot().getDatabase();
        DependencyGraph graph = new DependencyGraph();
        for (Class<? extends DatabaseObject> type : diffResult.getReferenceSnapshot().getSnapshotControl().getTypesToInclude()) {
            graph.addType(type);
        }
        List<Class<? extends DatabaseObject>> types = graph.sort(comparisonDatabase, generatorType);

        if (!loggedOrderFor.contains(generatorType)) {
            String log = generatorType.getSimpleName() + " type order: ";
            for (Class<? extends DatabaseObject> type : types) {
                log += "    " + type.getName();
            }
            LogService.getLog(getClass()).debug(LogType.LOG, log);
            loggedOrderFor.add(generatorType);
        }

        return types;
    }

    private void addToChangeSets(Change[] changes, List<ChangeSet> changeSets, ObjectQuotingStrategy quotingStrategy, String created) {
        if (changes != null) {
            String csContext = this.changeSetContext;

            if (diffOutputControl.getContext() != null) {
                csContext = diffOutputControl.getContext().toString().replaceFirst("^\\(", "")
                .replaceFirst("\\)$", "");
            }
            ChangeSet changeSet = new ChangeSet(generateId(changes), getChangeSetAuthor(), false, false, this.changeSetPath, csContext,
                    null, false, quotingStrategy, null);
            changeSet.setCreated(created);
            if (diffOutputControl.getLabels() != null) {
                changeSet.setLabels(diffOutputControl.getLabels());
            }
            for (Change change : changes) {
                changeSet.addChange(change);
            }
            changeSets.add(changeSet);
        }
    }

    protected String getChangeSetAuthor() {
        if (changeSetAuthor != null) {
            return changeSetAuthor;
        }
        String author = System.getProperty("user.name");
        if (StringUtil.trimToNull(author) == null) {
            return "diff-generated";
        } else {
            return author + " (generated)";
        }
    }

    public void setChangeSetAuthor(String changeSetAuthor) {
        this.changeSetAuthor = changeSetAuthor;
    }

    public String getChangeSetPath() {
        return changeSetPath;
    }

    public void setChangeSetPath(String changeSetPath) {
        this.changeSetPath = changeSetPath;
    }

    public void setIdRoot(String idRoot) {
        this.idRoot = idRoot;
        this.overriddenIdRoot = true;
    }

    protected String generateId(Change[] changes) {
        String desc = "";

        if (LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getGeneratedChangeSetIdsContainDescription()) {
            if (!overriddenIdRoot) { //switch timestamp to a shorter string (last 4 digits in base 36 format). Still mostly unique, but shorter since we also now have mostly-unique descriptions of the changes
                this.idRoot = Long.toString(Long.decode(idRoot), 36);
                idRoot = idRoot.substring(idRoot.length() - 4);
                this.overriddenIdRoot = true;
            }

             if ((changes != null) && (changes.length > 0)) {
                 desc = " ("+ StringUtil.join(changes, " :: ", new StringUtil.StringUtilFormatter<Change>() {
                     @Override
                     public String toString(Change obj) {
                         return obj.getDescription();
                     }
                 })+")";
             }

            if (desc.length() > 150) {
                desc = desc.substring(0, 146) + "...)";
            }
        }

        return idRoot + "-" + changeNumber++ + desc;
    }

    private static class DependencyGraph {

        private Map<Class<? extends DatabaseObject>, Node> allNodes = new HashMap<>();

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


            ArrayList<Node> returnNodes = new ArrayList<>();

            SortedSet<Node> nodesWithNoIncomingEdges = new TreeSet<>(new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    return o1.type.getName().compareTo(o2.type.getName());
                }
            });
            for (Node n : allNodes.values()) {
                if (n.inEdges.isEmpty()) {
                    nodesWithNoIncomingEdges.add(n);
                }
            }

            while (!nodesWithNoIncomingEdges.isEmpty()) {
                Node node = nodesWithNoIncomingEdges.iterator().next();
                nodesWithNoIncomingEdges.remove(node);

                returnNodes.add(node);

                for (Iterator<Edge> it = node.outEdges.iterator(); it.hasNext(); ) {
                    //remove edge e from the graph
                    Edge edge = it.next();
                    Node nodePointedTo = edge.to;
                    it.remove();//Remove edge from node
                    nodePointedTo.inEdges.remove(edge);//Remove edge from nodePointedTo

                    //if nodePointedTo has no other incoming edges then insert nodePointedTo into nodesWithNoIncomingEdges
                    if (nodePointedTo.inEdges.isEmpty()) {
                        nodesWithNoIncomingEdges.add(nodePointedTo);
                    }
                }
            }
            checkForCycleInDependencies(generatorType);


            List<Class<? extends DatabaseObject>> returnList = new ArrayList<>();
            for (Node node : returnNodes) {
                returnList.add(node.type);
            }
            return returnList;
        }

        private void checkForCycleInDependencies(Class<? extends ChangeGenerator> generatorType) {
            //Check to see if all edges are removed
            for (Node n : allNodes.values()) {
                if (!n.inEdges.isEmpty()) {
                    String message = "Could not resolve " + generatorType.getSimpleName() + " dependencies due " +
                     "to dependency cycle. Dependencies: \n";

                    for (Node node : allNodes.values()) {
                        SortedSet<String> fromTypes = new TreeSet<>();
                        SortedSet<String> toTypes = new TreeSet<>();
                        for (Edge edge : node.inEdges) {
                            fromTypes.add(edge.from.type.getSimpleName());
                        }
                        for (Edge edge : node.outEdges) {
                            toTypes.add(edge.to.type.getSimpleName());
                        }
                        String from = StringUtil.join(fromTypes, ",");
                        String to = StringUtil.join(toTypes, ",");
                        message += "    [" + from + "] -> " + node.type.getSimpleName() + " -> [" + to + "]\n";
                    }

                    throw new UnexpectedLiquibaseException(message);
                }
            }
        }


        private Node getNode(Class<? extends DatabaseObject> type) {
            Node node = allNodes.get(type);
            if (node == null) {
                node = new Node(type);
            }
            return node;
        }


        static class Node {
            public final Class<? extends DatabaseObject> type;
            public final HashSet<Edge> inEdges;
            public final HashSet<Edge> outEdges;

            public Node(Class<? extends DatabaseObject> type) {
                this.type = type;
                inEdges = new HashSet<>();
                outEdges = new HashSet<>();
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
                if (obj == null) {
                    return false;
                }
                if (!(obj instanceof Edge)) {
                    return false;
                }
                Edge e = (Edge) obj;
                return (e.from == from) && (e.to == to);
            }

            @Override
            public int hashCode() {
                return (this.from.toString() + "." + this.to.toString()).hashCode();
            }
        }
    }
}
