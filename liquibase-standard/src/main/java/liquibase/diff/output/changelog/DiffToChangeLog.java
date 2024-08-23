package liquibase.diff.output.changelog;

import liquibase.GlobalConfiguration;
import liquibase.Labels;
import liquibase.Scope;
import liquibase.change.Change;
import liquibase.change.ReplaceIfExists;
import liquibase.change.core.*;
import liquibase.changelog.ChangeSet;
import liquibase.changeset.ChangeSetService;
import liquibase.changeset.ChangeSetServiceFactory;
import liquibase.command.core.helpers.AbstractChangelogCommandStep;
import liquibase.configuration.core.DeprecatedConfigurationValueProvider;
import liquibase.database.*;
import liquibase.database.core.*;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectCollectionComparator;
import liquibase.diff.output.DiffOutputControl;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.resource.OpenOptions;
import liquibase.resource.PathHandlerFactory;
import liquibase.resource.Resource;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.ChangeLogSerializerFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.EmptyDatabaseSnapshot;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.StoredDatabaseLogic;
import liquibase.structure.core.Table;
import liquibase.util.DependencyUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtil;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DiffToChangeLog {

    public static final String ORDER_ATTRIBUTE = "order";
    public static final String DATABASE_CHANGE_LOG_CLOSING_XML_TAG = "</databaseChangeLog>";
    public static final String EXTERNAL_FILE_DIR_SCOPE_KEY = "DiffToChangeLog.externalFilesDir";
    public static final String DIFF_OUTPUT_CONTROL_SCOPE_KEY = "diffOutputControl";
    public static final String DIFF_SNAPSHOT_DATABASE = "snapshotDatabase";

    private String idRoot = String.valueOf(new Date().getTime());
    private boolean overriddenIdRoot;

    private int changeNumber = 1;

    private String changeSetContext;
    private String changeSetLabels;
    private String changeSetAuthor;
    private String changeSetPath;
    private String[] changeSetRunOnChangeTypes;
    private String[] changeReplaceIfExistsTypes;
    private DiffResult diffResult;
    private final DiffOutputControl diffOutputControl;
    private boolean tryDbaDependencies = true;

    private boolean skipObjectSorting = false;

    private static final Set<Class> loggedOrderFor = new HashSet<>();

    /**
     * Creates a new DiffToChangeLog with the given DiffResult and default DiffOutputControl
     * @param diffResult the DiffResult to convert to a ChangeLog
     * @param diffOutputControl the DiffOutputControl to use to control the output
     * @param skipObjectSorting if true, will skip dependency object sorting. This can be useful on databases that have a lot of packages/procedures that are linked to each other
     */
    public DiffToChangeLog(DiffResult diffResult, DiffOutputControl diffOutputControl, boolean skipObjectSorting) {
        this(diffResult, diffOutputControl);
        this.skipObjectSorting = skipObjectSorting;
    }

    public DiffToChangeLog(DiffResult diffResult, DiffOutputControl diffOutputControl) {
        this.diffResult = diffResult;
        this.diffOutputControl = diffOutputControl;
        respectSchemaAndCatalogCaseIfNeeded(diffOutputControl);
    }

    public DiffToChangeLog(DiffOutputControl diffOutputControl) {
        this.diffOutputControl = diffOutputControl;
    }

    private void respectSchemaAndCatalogCaseIfNeeded(DiffOutputControl diffOutputControl) {
        if (this.diffResult.getComparisonSnapshot().getDatabase() instanceof AbstractDb2Database) {
            diffOutputControl.setRespectSchemaAndCatalogCase(true);
        }
    }

    public void setDiffResult(DiffResult diffResult) {
        this.diffResult = diffResult;
    }

    public void setChangeSetContext(String changeSetContext) {
        this.changeSetContext = changeSetContext;
    }

    public void setChangeSetLabels(String changeSetLabels) {
        this.changeSetLabels = changeSetLabels;
    }

    public void print(String changeLogFile) throws ParserConfigurationException, IOException, DatabaseException {
        this.print(changeLogFile, false);
    }

    public void print(String changeLogFile, Boolean overwriteOutputFile) throws ParserConfigurationException, IOException, DatabaseException {
        this.changeSetPath = changeLogFile;
        ChangeLogSerializer changeLogSerializer = ChangeLogSerializerFactory.getInstance().getSerializer(changeLogFile);
        this.print(changeLogFile, changeLogSerializer, overwriteOutputFile);
    }

    public void print(PrintStream out) throws ParserConfigurationException, IOException, DatabaseException {
        this.print(out, ChangeLogSerializerFactory.getInstance().getSerializer("xml"));
    }

    public void print(String changeLogFile, ChangeLogSerializer changeLogSerializer) throws ParserConfigurationException, IOException, DatabaseException {
        this.print(changeLogFile, changeLogSerializer, false);
    }

    public void print(String changeLogFile, ChangeLogSerializer changeLogSerializer, Boolean overwriteOutputFile) throws ParserConfigurationException, IOException, DatabaseException {
        this.changeSetPath = changeLogFile;
        final PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);
        Resource file = pathHandlerFactory.getResource(changeLogFile);

        final Map<String, Object> newScopeObjects = new HashMap<>();

        Resource objectsDir = null;
        if (changeLogFile.toLowerCase().endsWith("sql")) {
            DeprecatedConfigurationValueProvider.setData("liquibase.pro.sql.inline", "true");
        } else if (this.diffResult.getComparisonSnapshot() instanceof EmptyDatabaseSnapshot) {
            objectsDir = file.resolveSibling("objects");
        } else {
            objectsDir = file.resolveSibling("objects-" + new Date().getTime());
        }

        if (objectsDir != null) {
            if (objectsDir.exists()) {
                throw new UnexpectedLiquibaseException("The generatechangelog command would overwrite your existing stored logic files. To run this command please remove or rename the '"+objectsDir+"' dir");
            }
            newScopeObjects.put(EXTERNAL_FILE_DIR_SCOPE_KEY, objectsDir);
        }


        newScopeObjects.put(DIFF_OUTPUT_CONTROL_SCOPE_KEY, diffOutputControl);

        try {
            //
            // Get a Database instance and save it in the scope for later use
            //
            Database database = determineDatabase(diffResult.getReferenceSnapshot());
            if (database == null) {
                database = determineDatabase(diffResult.getComparisonSnapshot());
            }
            newScopeObjects.put(DIFF_SNAPSHOT_DATABASE, database);
            Scope.child(newScopeObjects, new Scope.ScopedRunner() {
                @Override
                public void run() {
                    try {
                        if (!file.exists()) {
                            //print changeLog only if there are available changeSets to print instead of printing it always
                            printNew(changeLogSerializer, file);
                        } else {
                            StringBuilder fileContents = new StringBuilder();
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            print(new PrintStream(out, true, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()), changeLogSerializer);

                            String xml = new String(out.toByteArray(), GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue());
                            if (overwriteOutputFile) {
                                // write xml contents to file
                                Scope.getCurrentScope().getLog(getClass()).info(file.getUri() + " exists, overwriting");
                                fileContents.append(xml);
                            } else {
                                // read existing file
                                Scope.getCurrentScope().getLog(getClass()).info(file.getUri() + " exists, appending");
                                fileContents = new StringBuilder(StreamUtil.readStreamAsString(file.openInputStream()));

                                String innerXml = xml.replaceFirst("(?ms).*<databaseChangeLog[^>]*>", "");

                                innerXml = innerXml.replaceFirst(DATABASE_CHANGE_LOG_CLOSING_XML_TAG, "");
                                innerXml = innerXml.trim();
                                if ("".equals(innerXml)) {
                                    Scope.getCurrentScope().getLog(getClass()).info("No changes found, nothing to do");
                                    return;
                                }

                                // insert new XML
                                int endTagIndex = fileContents.indexOf(DATABASE_CHANGE_LOG_CLOSING_XML_TAG);
                                if (endTagIndex == -1) {
                                    fileContents.append(xml);
                                } else {
                                    String lineSeparator = GlobalConfiguration.OUTPUT_LINE_SEPARATOR.getCurrentValue();
                                    String toInsert = "    " + innerXml + lineSeparator;
                                    fileContents.insert(endTagIndex, toInsert);
                                }
                            }

                            try (OutputStream outputStream = file.openOutputStream(new OpenOptions())) {
                                outputStream.write(fileContents.toString().getBytes());
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (Exception e) {
            //rethrow known exceptions. TODO: Fix this up with final Scope API
            final Throwable cause = e.getCause();
            if (cause instanceof ParserConfigurationException) {
                throw (ParserConfigurationException) cause;
            }
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            if (cause instanceof DatabaseException) {
                throw (DatabaseException) cause;
            }

            throw new RuntimeException(e);
        }
    }

    //
    // Return the Database from this snapshot
    // if it is not offline
    //
    private Database determineDatabase(DatabaseSnapshot snapshot) {
        Database database = snapshot.getDatabase();
        DatabaseConnection connection = database.getConnection();
        if (! (connection instanceof OfflineConnection) && database instanceof PostgresDatabase) {
            return database;
        }
        return null;
    }

    /**
     * Prints changeLog that would bring the target database to be the same as
     * the reference database
     */
    public void printNew(ChangeLogSerializer changeLogSerializer, Resource file) throws ParserConfigurationException, IOException, DatabaseException {

        List<ChangeSet> changeSets = generateChangeSets();

        Scope.getCurrentScope().getLog(getClass()).info("changeSets count: " + changeSets.size());
        if (changeSets.isEmpty()) {
            Scope.getCurrentScope().getLog(getClass()).info("No changesets to add to the changelog output.");
        } else {
            Scope.getCurrentScope().getLog(getClass()).info(file + " does not exist, creating and adding " + changeSets.size() + " changesets.");
            try (OutputStream stream = file.openOutputStream(new OpenOptions());
                 PrintStream out = new PrintStream(stream, true, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue())) {
                 changeLogSerializer.write(changeSets, out);
            }
        }
    }

    /**
     * Prints changeLog that would bring the target database to be the same as
     * the reference database
     */
    public void print(final PrintStream out, final ChangeLogSerializer changeLogSerializer) throws ParserConfigurationException, IOException, DatabaseException {
        List<ChangeSet> changeSets = generateChangeSets();

        changeLogSerializer.write(changeSets, out);

        out.flush();
    }

    public List<ChangeSet> generateChangeSets() {
        final ChangeGeneratorFactory changeGeneratorFactory = ChangeGeneratorFactory.getInstance();
        DatabaseObjectCollectionComparator comparator = new DatabaseObjectCollectionComparator();

        String created = null;
        if (GlobalConfiguration.GENERATE_CHANGESET_CREATED_VALUES.getCurrentValue()) {
            created = new SimpleDateFormat("yyyy-MM-dd HH:mmZ").format(new Date());
        }

        List<Class<? extends DatabaseObject>> types = getOrderedOutputTypes(ChangedObjectChangeGenerator.class);
        List<ChangeSet> updateChangeSets = new ArrayList<>();

        // Keep a reference to DiffResult in the comparision database so that it can be retrieved later
        // This is to avoid changing the MissingObjectChangeGenerator API and still be able to pass the
        // initial DiffResult Object which can be used to check for the objects available in the database
        // without doing any expensive db calls. Example usage is in MissingUniqueConstraintChangeGenerator#alreadyExists()
        Database comparisonDatabase = diffResult.getComparisonSnapshot().getDatabase();
        if (comparisonDatabase instanceof AbstractJdbcDatabase) {
            ((AbstractJdbcDatabase) comparisonDatabase).set("diffResult", diffResult);
        }

        for (Class<? extends DatabaseObject> type : types) {
            ObjectQuotingStrategy quotingStrategy = diffOutputControl.getObjectQuotingStrategy();
            for (Map.Entry<? extends DatabaseObject, ObjectDifferences> entry : diffResult.getChangedObjects(type, comparator).entrySet()) {
                if (!diffResult.getReferenceSnapshot().getDatabase().isLiquibaseObject(entry.getKey()) && !diffResult.getReferenceSnapshot().getDatabase().isSystemObject(entry.getKey())) {
                    Change[] changes = changeGeneratorFactory.fixChanged(entry.getKey(), entry.getValue(), diffOutputControl, diffResult.getReferenceSnapshot().getDatabase(), diffResult.getComparisonSnapshot().getDatabase());
                    setReplaceIfExistsTrueIfApplicable(changes);
                    addToChangeSets(changes, updateChangeSets, quotingStrategy, created);
                }
            }
        }

        types = getOrderedOutputTypes(MissingObjectChangeGenerator.class);
        List<DatabaseObject> missingObjects = new ArrayList<>();
        for (Class<? extends DatabaseObject> type : types) {
            for (DatabaseObject object : diffResult.getMissingObjects(type, getDatabaseObjectCollectionComparator())) {
                if (object == null) {
                    continue;
                }
                if (!diffResult.getReferenceSnapshot().getDatabase().isLiquibaseObject(object) && !diffResult.getReferenceSnapshot().getDatabase().isSystemObject(object)) {
                    missingObjects.add(object);
                }
            }
        }

        List<ChangeSet> createChangeSets = new ArrayList<>();

        for (DatabaseObject object : sortMissingObjects(missingObjects, diffResult.getReferenceSnapshot().getDatabase())) {
            ObjectQuotingStrategy quotingStrategy = diffOutputControl.getObjectQuotingStrategy();

            Change[] changes = changeGeneratorFactory.fixMissing(object, diffOutputControl, diffResult.getReferenceSnapshot().getDatabase(), diffResult.getComparisonSnapshot().getDatabase());
            setReplaceIfExistsTrueIfApplicable(changes);
            addToChangeSets(changes, createChangeSets, quotingStrategy, created);
        }

        List<ChangeSet> deleteChangeSets = new ArrayList<>();

        types = getOrderedOutputTypes(UnexpectedObjectChangeGenerator.class);
        for (Class<? extends DatabaseObject> type : types) {
            ObjectQuotingStrategy quotingStrategy = diffOutputControl.getObjectQuotingStrategy();
            for (DatabaseObject object : sortUnexpectedObjects(diffResult.getUnexpectedObjects(type, comparator), diffResult.getReferenceSnapshot().getDatabase())) {
                if (!diffResult.getComparisonSnapshot().getDatabase().isLiquibaseObject(object) && !diffResult.getComparisonSnapshot().getDatabase().isSystemObject(object)) {
                    Change[] changes = changeGeneratorFactory.fixUnexpected(object, diffOutputControl, diffResult.getReferenceSnapshot().getDatabase(), diffResult.getComparisonSnapshot().getDatabase());
                    setReplaceIfExistsTrueIfApplicable(changes);
                    addToChangeSets(changes, deleteChangeSets, quotingStrategy, created);
                }
            }
        }
        // remove the diffResult from the database object
        if (comparisonDatabase instanceof AbstractJdbcDatabase) {
            ((AbstractJdbcDatabase) comparisonDatabase).set("diffResult", null);
        }


        List<ChangeSet> changeSets = new ArrayList<>();
        changeSets.addAll(createChangeSets);
        changeSets.addAll(deleteChangeSets);
        changeSets.addAll(updateChangeSets);
        changeSets = bringDropFKToTop(changeSets);
        return changeSets;
    }

    private void setReplaceIfExistsTrueIfApplicable(Change[] changes) {
        if (changes !=null && diffOutputControl.isReplaceIfExistsSet()) {
            for (Change change : changes) {
                if (change instanceof ReplaceIfExists) {
                    ((ReplaceIfExists) change).setReplaceIfExists(true);
                }
            }
        }
    }

    //
    // Because the generated changeset list can contain both add and drop
    // FK changes with the same constraint name, we make sure that the
    // drop FK goes first
    //
    private List<ChangeSet> bringDropFKToTop(List<ChangeSet> changeSets) {
        List<ChangeSet> dropFk = changeSets.stream().filter(cs ->
            cs.getChanges().stream().anyMatch(DropForeignKeyConstraintChange.class::isInstance)
        ).collect(Collectors.toList());
        if (dropFk.isEmpty()) {
            return changeSets;
        }
        List<ChangeSet> returnList = new ArrayList<>();
        changeSets.stream().forEach(cs -> {
            if (dropFk.contains(cs)) {
                returnList.add(cs);
            }
        });
        changeSets.stream().forEach(cs -> {
            if (! dropFk.contains(cs)) {
                returnList.add(cs);
            }
        });
        return returnList;
    }

    private DatabaseObjectCollectionComparator getDatabaseObjectCollectionComparator() {
        return new DatabaseObjectCollectionComparator() {
            @Override
            public int compare(DatabaseObject o1, DatabaseObject o2) {
                if (o1 instanceof Column && o1.getAttribute(ORDER_ATTRIBUTE, Integer.class) != null && o2.getAttribute(ORDER_ATTRIBUTE, Integer.class) != null) {
                    int i = o1.getAttribute(ORDER_ATTRIBUTE, Integer.class).compareTo(o2.getAttribute(ORDER_ATTRIBUTE, Integer.class));
                    if (i != 0) {
                        return i;
                    }
                } else if (o1 instanceof StoredDatabaseLogic) {
                    if (o1.getAttribute(ORDER_ATTRIBUTE, Integer.class) != null && o2.getAttribute(ORDER_ATTRIBUTE, Integer.class) != null) {
                        int order = o1.getAttribute(ORDER_ATTRIBUTE, Long.class).compareTo(o2.getAttribute(ORDER_ATTRIBUTE, Long.class));
                        if (order != 0) {
                            return order;
                        }
                    }
                }
                return super.compare(o1, o2);
            }
        };
    }

    private List<DatabaseObject> sortUnexpectedObjects(Collection<? extends DatabaseObject> unexpectedObjects, Database database) {
        return sortObjects("unexpected", (Collection<DatabaseObject>) unexpectedObjects, database);
    }

    private List<DatabaseObject> sortMissingObjects(Collection<DatabaseObject> missingObjects, Database database) {
        return sortObjects("missing", missingObjects, database);
    }

    private List<DatabaseObject> sortObjects(final String type, Collection<DatabaseObject> objects, Database database) {

        if (!objects.isEmpty() && supportsSortingObjects(database) && (database.getConnection() != null) && !(database.getConnection() instanceof OfflineConnection)) {
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
                DependencyUtil.NodeValueListener<String> nameListener = dependencyOrder::add;

                DependencyUtil.DependencyGraph<String> graph = new DependencyUtil.DependencyGraph<>(nameListener);
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

                            String objectName = obj.getName();
                            String name = schemaName + "." + objectName;
                            if (dependencyOrder.contains(name) ||
                                dependencyOrder.contains(convertStoredLogicObjectName(schemaName, objectName, database))) {
                                toSort.add(obj);
                            } else {
                                toNotSort.add(obj);
                            }
                        } else {
                            toNotSort.add(obj);
                        }
                    }

                    toSort.sort((o1, o2) -> {
                        //
                        // For Postgres, make tables appear before stored logic
                        //
                        if (database instanceof PostgresDatabase) {
                            Integer x = determineOrderingForTablesAndStoredLogic(o1, o2);
                            if (x != null) {
                                return x;
                            }
                        }
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
                    });

                    toSort.addAll(toNotSort);
                    return toSort;
                }
            } catch (DatabaseException e) {
                Scope.getCurrentScope().getLog(getClass()).fine("Cannot get object dependencies: " + e.getMessage());
            } catch (StackOverflowError e) {
                Scope.getCurrentScope().getLog(getClass()).warning("You have too many or recursive database object dependencies! " +
                        "Liquibase is going to ignore dependency sorting and resume processing. To skip this message " +
                        "(and save a lot of processing time) use flag " + AbstractChangelogCommandStep.SKIP_OBJECT_SORTING.getName(), e);
            }
        }
        return new ArrayList<>(objects);
    }

    private static Integer determineOrderingForTablesAndStoredLogic(DatabaseObject o1, DatabaseObject o2) {
        if (o1 instanceof Table && o2 instanceof StoredDatabaseLogic) {
            return -1;
        }
        if (o1 instanceof StoredDatabaseLogic && o2 instanceof Table) {
            return 1;
        }
        return null;
    }

    /**
     *
     * POSTGRES ONLY:
     *
     * If we have a stored logic object then we edit the name
     * to replace the parameter list with a list of just the types.
     * This is the format that the dependency computation puts out.
     *
     * Example:  calculate_bonus(emp_salary numeric, emp_name character varying) becomes
     *           calculate_bonus(numeric, character varying)
     *
     * @param  objectName     The input object name to work on
     * @return String
     *
     */
    private static String convertStoredLogicObjectName(String schemaName, String objectName, Database database) {
        String name = schemaName + "." + objectName;
        if (! (database instanceof PostgresDatabase) || ! (objectName.contains("(") && objectName.contains(")"))) {
            return name;
        }
        Pattern p = Pattern.compile(".*?[(]+(.*)?[)]+[\\s]*?$");
        Matcher m = p.matcher(objectName);
        if (m.matches()) {
            String originalParameters = m.group(1);
            String editedParameters = originalParameters;
            String[] parameters = m.group(1).split(",");
            for (String parameter : parameters) {
                parameter = parameter.trim();
                String[] parts = parameter.split(" ");
                String[] rest = Arrays.copyOfRange(parts, 1, parts.length);
                String part = StringUtil.join(rest, " ");
                editedParameters = editedParameters.replace(parameter, part);
            }
            name = schemaName + "." + objectName.replace(originalParameters, editedParameters)
                                                .replace(", ",",");
        }
        return name;
    }

    private List<Map<String, ?>> queryForDependenciesOracle(Executor executor, List<String> schemas)
            throws DatabaseException {
        List<Map<String, ?>> rs = null;
        try {
            if (tryDbaDependencies) {
                rs = executor.queryForList(new RawParameterizedSqlStatement("select OWNER, NAME, REFERENCED_OWNER, REFERENCED_NAME from DBA_DEPENDENCIES where REFERENCED_OWNER != 'SYS' AND NOT(NAME LIKE 'BIN$%') AND NOT(OWNER = REFERENCED_OWNER AND NAME = REFERENCED_NAME) AND (" + StringUtil.join(schemas, " OR ", (StringUtil.StringUtilFormatter<String>) obj -> "OWNER='" + obj + "'"
                ) + ")"));
            } else {
                rs = executor.queryForList(new RawParameterizedSqlStatement("select NAME, REFERENCED_OWNER, REFERENCED_NAME from USER_DEPENDENCIES where REFERENCED_OWNER != 'SYS' AND NOT(NAME LIKE 'BIN$%') AND NOT(NAME = REFERENCED_NAME) AND (" + StringUtil.join(schemas, " OR ", (StringUtil.StringUtilFormatter<String>) obj -> "REFERENCED_OWNER='" + obj + "'"
                ) + ")"));
            }
        } catch (DatabaseException dbe) {
            //
            // If our exception is for something other than a missing table/view
            // then we just re-throw the exception
            // else if we can't see USER_DEPENDENCIES then we also re-throw
            //   to stop the recursion
            //
            String message = dbe.getMessage();
            if (!message.contains("ORA-00942") || !tryDbaDependencies) {
                throw new DatabaseException(dbe);
            }
            Scope.getCurrentScope().getLog(getClass()).warning("Unable to query DBA_DEPENDENCIES table. Switching to USER_DEPENDENCIES");
            tryDbaDependencies = false;
            return queryForDependenciesOracle(executor, schemas);
        }
        return rs;
    }

    /**
     * Used by {@link #sortMissingObjects(Collection, Database)} to determine whether to go into the sorting logic.
     */
    protected boolean supportsSortingObjects(Database database) {
        if (this.skipObjectSorting) {
            return false;
        }
        return (database instanceof AbstractDb2Database) || (database instanceof MSSQLDatabase) || (database instanceof
                OracleDatabase) || database instanceof PostgresDatabase;
    }

    /**
     * Adds dependencies to the graph as schema.object_name.
     */
    protected void addDependencies(DependencyUtil.DependencyGraph<String> graph, List<String> schemas, Database database) throws DatabaseException {
        if (database instanceof DB2Database) {
            Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
            String sql = "select TABSCHEMA, TABNAME, BSCHEMA, BNAME from syscat.tabdep where TABSCHEMA in (" + StringUtil.join(schemas, ", ", obj -> "?") + ")";
            List<Map<String, ?>> rs = executor.queryForList(new RawParameterizedSqlStatement(sql, schemas.toArray()));
            for (Map<String, ?> row : rs) {
                String tabName = StringUtil.trimToNull((String) row.get("TABSCHEMA")) + "." + StringUtil.trimToNull((String) row.get("TABNAME"));
                String bName = StringUtil.trimToNull((String) row.get("BSCHEMA")) + "." + StringUtil.trimToNull((String) row.get("BNAME"));

                graph.add(bName, tabName);
            }
        } else if (database instanceof Db2zDatabase) {
            Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
            String sql = "SELECT DSCHEMA AS TABSCHEMA, DNAME AS TABNAME, BSCHEMA, BNAME FROM SYSIBM.SYSDEPENDENCIES WHERE DSCHEMA IN (" + StringUtil.join(schemas, ", ", obj -> "?") + ")";
            List<Map<String, ?>> rs = executor.queryForList(new RawParameterizedSqlStatement(sql, schemas.toArray()));
            for (Map<String, ?> row : rs) {
                String tabName = StringUtil.trimToNull((String) row.get("TABSCHEMA")) + "." + StringUtil.trimToNull((String) row.get("TABNAME"));
                String bName = StringUtil.trimToNull((String) row.get("BSCHEMA")) + "." + StringUtil.trimToNull((String) row.get("BNAME"));

                graph.add(bName, tabName);
            }
        } else if (database instanceof OracleDatabase) {
            Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
            List<Map<String, ?>> rs = queryForDependenciesOracle(executor, schemas);
            for (Map<String, ?> row : rs) {
                String tabName = null;
                if (tryDbaDependencies) {
                    tabName =
                            StringUtil.trimToNull((String) row.get("OWNER")) + "." +
                                    StringUtil.trimToNull((String) row.get("NAME"));
                } else {
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
            Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
            String sql = "select object_schema_name(referencing_id) as referencing_schema_name, object_name(referencing_id) as referencing_name, object_name(referenced_id) as referenced_name, object_schema_name(referenced_id) as referenced_schema_name  from sys.sql_expression_dependencies depz where (" + StringUtil.join(schemas, " OR ", (StringUtil.StringUtilFormatter<String>) obj -> "object_schema_name(referenced_id)='" + obj + "'"
            ) + ")";
            sql += " UNION select object_schema_name(object_id) as referencing_schema_name, object_name(object_id) as referencing_name, object_name(parent_object_id) as referenced_name, object_schema_name(parent_object_id) as referenced_schema_name " +
                    "from sys.objects " +
                    "where parent_object_id > 0 " +
                    "and is_ms_shipped=0 " +
                    "and (" + StringUtil.join(schemas, " OR ", (StringUtil.StringUtilFormatter<String>) obj -> "object_schema_name(object_id)='" + obj + "'"
            ) + ")";

            sql += " UNION select object_schema_name(fk.object_id) as referencing_schema_name, fk.name as referencing_name, i.name as referenced_name, object_schema_name(i.object_id) as referenced_schema_name " +
                    "from sys.foreign_keys fk " +
                    "join sys.indexes i on fk.referenced_object_id=i.object_id and fk.key_index_id=i.index_id " +
                    "where fk.is_ms_shipped=0 " +
                    "and (" + StringUtil.join(schemas, " OR ", (StringUtil.StringUtilFormatter<String>) obj -> "object_schema_name(fk.object_id)='" + obj + "'"
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
                    "where " + StringUtil.join(schemas, " OR ", (StringUtil.StringUtilFormatter<String>) obj -> "object_schema_name(i.object_id)='" + obj + "'");

            //get schema -> base object dependencies
            sql += " UNION SELECT SCHEMA_NAME(SCHEMA_ID) as referencing_schema_name, name as referencing_name, PARSENAME(BASE_OBJECT_NAME,1) AS referenced_name, (CASE WHEN PARSENAME(BASE_OBJECT_NAME,2) IS NULL THEN schema_name(schema_id) else PARSENAME(BASE_OBJECT_NAME,2) END) AS referenced_schema_name FROM sys.synonyms WHERE is_ms_shipped='false' AND " + StringUtil.join(schemas, " OR ", (StringUtil.StringUtilFormatter<String>) obj -> "SCHEMA_NAME(SCHEMA_ID)='" + obj + "'");

            //get non-clustered indexes -> unique clustered indexes on views dependencies
            sql += " UNION select object_schema_name(c.object_id) as referencing_schema_name, c.name as referencing_name, object_schema_name(nc.object_id) as referenced_schema_name, nc.name as referenced_name from sys.indexes c join sys.indexes nc on c.object_id=nc.object_id JOIN sys.objects o ON c.object_id = o.object_id where  c.index_id != nc.index_id and c.type_desc='CLUSTERED' and c.is_unique='true' and (not(nc.type_desc='CLUSTERED') OR nc.is_unique='false') AND o.type_desc='VIEW' AND o.name='AR_DETAIL_OPEN'";

            List<Map<String, ?>> rs = executor.queryForList(new RawParameterizedSqlStatement(sql));
            if (!rs.isEmpty()) {
                for (Map<String, ?> row : rs) {
                    String bName = StringUtil.trimToNull((String) row.get("REFERENCED_SCHEMA_NAME")) + "." + StringUtil.trimToNull((String) row.get("REFERENCED_NAME"));
                    String tabName = StringUtil.trimToNull((String) row.get("REFERENCING_SCHEMA_NAME")) + "." + StringUtil.trimToNull((String) row.get("REFERENCING_NAME"));

                    if (!bName.equals(tabName)) {
                        graph.add(bName, tabName);
                    }
                }
            }
        } else if (database instanceof PostgresDatabase) {
            final String sql = queryForDependenciesPostgreSql(schemas);
            final Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
            final List<Map<String, ?>> queryForListResult = executor.queryForList(new RawParameterizedSqlStatement(sql));

            for (Map<String, ?> row : queryForListResult) {
                String bName = StringUtil.trimToEmpty((String) row.get("REFERENCING_SCHEMA_NAME")) +
                        "." + StringUtil.trimToEmpty((String)row.get("REFERENCING_NAME"));
                String tabName = StringUtil.trimToEmpty((String)row.get("REFERENCED_SCHEMA_NAME")) +
                        "." + StringUtil.trimToEmpty((String)row.get("REFERENCED_NAME"));

                if (!(tabName.isEmpty() || bName.isEmpty())) {
                    graph.add(bName.replace("\"", ""), tabName.replace("\"", ""));
                    graph.add(bName.replace("\"", "").replaceAll("\\s*\\([^)]*\\)\\s*",""),
                            tabName.replace("\"", "").replaceAll("\\s*\\([^)]*\\)\\s*", ""));
                }
            }
        }
    }

    private String queryForDependenciesPostgreSql(List<String> schemas) {
        //SQL adapted from https://wiki.postgresql.org/wiki/Pg_depend_display
        //We filter out PK and FK in this query so that they are not added to the dependency graph
        //they get added later in the flow, after the table is guaranteed to have been created
        return "WITH RECURSIVE preference AS (\n" +
                "    SELECT 10 AS max_depth  -- The deeper the recursion goes, the slower it performs.\n" +
                "         , 16384 AS min_oid -- user objects only\n" +
                "         , '^(londiste|pgq|pg_toast)'::text AS schema_exclusion\n" +
                "         , '^pg_(conversion|language|ts_(dict|template))'::text AS class_exclusion\n" +
                "         , '{\"SCHEMA\":\"00\", \"TABLE\":\"01\", \"CONSTRAINT\":\"02\", \"DEFAULT\":\"03\",\n" +
                "      \"INDEX\":\"05\", \"SEQUENCE\":\"06\", \"TRIGGER\":\"07\", \"FUNCTION\":\"08\",\n" +
                "      \"VIEW\":\"10\", \"MVIEW\":\"11\", \"FOREIGN\":\"12\"}'::json AS type_ranks),\n" +
                "               dependency_pair AS (\n" +
                "                   WITH relation_object AS ( SELECT oid, oid::regclass::text AS object_name  FROM pg_class )\n" +
                "                   SELECT DISTINCT " +
                "                         substring(pg_identify_object(classid, objid, 0)::text, E'(\\\\w+?)\\\\.') as referenced_schema_name, " +
                "                         CASE classid\n" +
                "                              WHEN 'pg_constraint'::regclass THEN (SELECT CONTYPE::text FROM pg_constraint WHERE oid = objid)\n" +
                "                              ELSE objid::text\n" +
                "                              END AS CONTYPE,\n" +
                "                         CASE classid\n" +
                "                              WHEN 'pg_attrdef'::regclass THEN (SELECT attname FROM pg_attrdef d JOIN pg_attribute c ON (c.attrelid,c.attnum)=(d.adrelid,d.adnum) WHERE d.oid = objid)\n" +
                "                              WHEN 'pg_cast'::regclass THEN (SELECT concat(castsource::regtype::text, ' AS ', casttarget::regtype::text,' WITH ', castfunc::regprocedure::text) FROM pg_cast WHERE oid = objid)\n" +
                "                              WHEN 'pg_class'::regclass THEN rel.object_name\n" +
                "                              WHEN 'pg_constraint'::regclass THEN (SELECT conname FROM pg_constraint WHERE oid = objid)\n" +
                "                              WHEN 'pg_extension'::regclass THEN (SELECT extname FROM pg_extension WHERE oid = objid)\n" +
                "                              WHEN 'pg_namespace'::regclass THEN (SELECT nspname FROM pg_namespace WHERE oid = objid)\n" +
                "                              WHEN 'pg_opclass'::regclass THEN (SELECT opcname FROM pg_opclass WHERE oid = objid)\n" +
                "                              WHEN 'pg_operator'::regclass THEN (SELECT oprname FROM pg_operator WHERE oid = objid)\n" +
                "                              WHEN 'pg_opfamily'::regclass THEN (SELECT opfname FROM pg_opfamily WHERE oid = objid)\n" +
                "                              WHEN 'pg_proc'::regclass THEN objid::regprocedure::text\n" +
                "                              WHEN 'pg_rewrite'::regclass THEN (SELECT ev_class::regclass::text FROM pg_rewrite WHERE oid = objid)\n" +
                "                              WHEN 'pg_trigger'::regclass THEN (SELECT tgname FROM pg_trigger WHERE oid = objid)\n" +
                "                              WHEN 'pg_type'::regclass THEN objid::regtype::text\n" +
                "                              ELSE objid::text\n" +
                "                              END AS REFERENCED_NAME,\n" +
                "                          substring(pg_identify_object(refclassid, refobjid, 0)::text, E'(\\\\w+?)\\\\.') as referencing_schema_name, " +
                "                          CASE refclassid\n" +
                "                              WHEN 'pg_namespace'::regclass THEN (SELECT nspname FROM pg_namespace WHERE oid = refobjid)\n" +
                "                              WHEN 'pg_class'::regclass THEN rrel.object_name\n" +
                "                              WHEN 'pg_opfamily'::regclass THEN (SELECT opfname FROM pg_opfamily WHERE oid = refobjid)\n" +
                "                              WHEN 'pg_proc'::regclass THEN refobjid::regprocedure::text\n" +
                "                              WHEN 'pg_type'::regclass THEN refobjid::regtype::text\n" +
                "                              ELSE refobjid::text\n" +
                "                              END AS REFERENCING_NAME\n" +
                "                   FROM pg_depend dep\n" +
                "                            LEFT JOIN relation_object rel ON rel.oid = dep.objid\n" +
                "                            LEFT JOIN relation_object rrel ON rrel.oid = dep.refobjid, preference\n" +
                "                   WHERE deptype = ANY('{n,a}')\n" +
                "                     AND objid >= preference.min_oid\n" +
                "                     AND (refobjid >= preference.min_oid OR refobjid = 2200) -- need public schema as root node\n" +
                "                     AND classid::regclass::text !~ preference.class_exclusion\n" +
                "                     AND refclassid::regclass::text !~ preference.class_exclusion\n" +
                "                     AND COALESCE(SUBSTRING(objid::regclass::text, E'^(\\\\\\\\w+)\\\\\\\\.'),'') !~ preference.schema_exclusion\n" +
                "                     AND COALESCE(SUBSTRING(refobjid::regclass::text, E'^(\\\\\\\\w+)\\\\\\\\.'),'') !~ preference.schema_exclusion\n" +
                "                   GROUP BY classid, objid, refclassid, refobjid, deptype, rel.object_name, rrel.object_name\n" +
                "               )\n" +
                " select referenced_schema_name,\n" +
                "    (CASE\n" +
                "      WHEN position('.' in referenced_name) >0 THEN substring(referenced_name from position('.' in referenced_name)+1 for length(referenced_name))\n" +
                "      ELSE referenced_name\n" +
                "    END)  AS referenced_name, \n" +
                "   referencing_schema_name,\n" +
                "   (CASE\n" +
                "      WHEN position('.' in referencing_name) >0 THEN substring(referencing_name from position('.' in referencing_name)+1 for length(referencing_name))\n" +
                "      ELSE referencing_name\n" +
                "    END)  AS referencing_name from dependency_pair where REFERENCED_NAME != REFERENCING_NAME " +
                " AND (" +
                StringUtil.join(schemas, " OR ", (StringUtil.StringUtilFormatter<String>) obj -> " REFERENCED_NAME like '" + obj + ".%' OR REFERENCED_NAME NOT LIKE '%.%'") + ")\n" +
                " AND (CONTYPE::text != 'p' AND CONTYPE::text != 'f')\n" +
                " AND referencing_schema_name is not null and referencing_name is not null";
    }

    protected List<Class<? extends DatabaseObject>> getOrderedOutputTypes(Class<? extends ChangeGenerator> generatorType) {

        Database comparisonDatabase = diffResult.getComparisonSnapshot().getDatabase();
        DependencyGraph graph = new DependencyGraph();
        for (Class<? extends DatabaseObject> type : diffResult.getReferenceSnapshot().getSnapshotControl().getTypesToInclude()) {
            graph.addType(type);
        }
        List<Class<? extends DatabaseObject>> types = graph.sort(comparisonDatabase, generatorType);
        if (!loggedOrderFor.contains(generatorType)) {
            final StringBuilder log = new StringBuilder(generatorType.getSimpleName() + " type order: ");
            for (Class<? extends DatabaseObject> type : types) {
                log.append("    ").append(type.getName());
            }
            Scope.getCurrentScope().getLog(getClass()).fine(log.toString());
            loggedOrderFor.add(generatorType);
        }

        return types;
    }

    private void addToChangeSets(Change[] changes, List<ChangeSet> changeSets, ObjectQuotingStrategy quotingStrategy, String created) {
        if (changes != null) {
            ChangeSetService service = ChangeSetServiceFactory.getInstance().createChangeSetService();
            if (useSeparateChangeSets(changes)) {
                for (Change change : changes) {
                    final boolean runOnChange = isContainedInRunOnChangeTypes(change);
                    ChangeSet changeSet =
                            service.createChangeSet(generateId(changes), getChangeSetAuthor(), false, runOnChange, this.changeSetPath, changeSetContext,
                                    null, null, null, true, quotingStrategy, null);
                    changeSet.setCreated(created);
                    if (diffOutputControl.getLabels() != null) {
                        changeSet.setLabels(diffOutputControl.getLabels());
                    } else {
                        changeSet.setLabels(new Labels(this.changeSetLabels));
                    }
                    if (change instanceof ReplaceIfExists && isContainedInReplaceIfExistsTypes(change)) {
                        ((ReplaceIfExists) change).setReplaceIfExists(true);
                    }
                    changeSet.addChange(change);
                    changeSets.add(changeSet);
                }
            } else {
                final boolean runOnChange = Arrays.asList(changes).stream().allMatch(this::isContainedInRunOnChangeTypes);
                ChangeSet changeSet = service.createChangeSet(generateId(changes), getChangeSetAuthor(), false, runOnChange, this.changeSetPath, changeSetContext,
                                        null, null, null, true, quotingStrategy, null);
                changeSet.setCreated(created);
                if (diffOutputControl.getLabels() != null) {
                    changeSet.setLabels(diffOutputControl.getLabels());
                } else {
                    changeSet.setLabels(new Labels(this.changeSetLabels));
                }
                for (Change change : changes) {
                    if (change instanceof ReplaceIfExists && isContainedInReplaceIfExistsTypes(change)) {
                        ((ReplaceIfExists) change).setReplaceIfExists(true);
                    }
                    changeSet.addChange(change);
                }
                changeSets.add(changeSet);

            }
        }
    }

    protected boolean useSeparateChangeSets(Change[] changes) {
        boolean sawAutocommitBefore = false;

        for (Change change : changes) {
            boolean thisStatementAutocommits = !(change instanceof InsertDataChange)
                    && !(change instanceof DeleteDataChange)
                    && !(change instanceof UpdateDataChange)
                    && !(change instanceof LoadDataChange);

            if (change instanceof RawSQLChange) {
                if (((RawSQLChange) change).getSql().trim().matches("SET\\s+\\w+\\s+\\w+")) {
                    //don't separate out when there is a `SET X Y` statement
                    thisStatementAutocommits = false;
                }
            }

            if (thisStatementAutocommits) {
                if (sawAutocommitBefore) {
                    return true;
                } else {
                    sawAutocommitBefore = true;
                }
            }
        }

        return false;
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

    public void setChangeSetRunOnChangeTypes(final String[] runOnChangeTypes) {
        changeSetRunOnChangeTypes = runOnChangeTypes;
    }

    protected String[] getChangeSetRunOnChangeTypes() {
        return changeSetRunOnChangeTypes;
    }

    private boolean isContainedInRunOnChangeTypes(final Change change) {
        return getChangeSetRunOnChangeTypes() != null && Arrays.asList(getChangeSetRunOnChangeTypes()).contains(change.getSerializedObjectName());
    }

    public void setChangeReplaceIfExistsTypes(final String[] replaceIfExistsTypes) {
        changeReplaceIfExistsTypes = replaceIfExistsTypes;
    }

    protected String[] getChangeReplaceIfExistsTypes() {
        return changeReplaceIfExistsTypes;
    }

    private boolean isContainedInReplaceIfExistsTypes(final Change change) {
        return getChangeReplaceIfExistsTypes() != null && Arrays.asList(getChangeReplaceIfExistsTypes()).contains(change.getSerializedObjectName());
    }

    public void setIdRoot(String idRoot) {
        this.idRoot = idRoot;
        this.overriddenIdRoot = true;
    }

    protected String generateId(Change[] changes) {
        String desc = "";

        if (GlobalConfiguration.GENERATED_CHANGESET_IDS_INCLUDE_DESCRIPTION.getCurrentValue()) {
            if (!overriddenIdRoot) { //switch timestamp to a shorter string (last 4 digits in base 36 format). Still mostly unique, but shorter since we also now have mostly-unique descriptions of the changes
                this.idRoot = Long.toString(Long.decode(idRoot), 36);
                idRoot = idRoot.substring(idRoot.length() - 4);
                this.overriddenIdRoot = true;
            }

            if ((changes != null) && (changes.length > 0)) {
                desc = " ("+ StringUtil.join(changes, " :: ", (StringUtil.StringUtilFormatter<Change>) Change::getDescription) + ")";
            }

            if (desc.length() > 150) {
                desc = desc.substring(0, 146) + "...)";
            }
        }

        return idRoot + "-" + changeNumber++ + desc;
    }

    private static class DependencyGraph {

        private final Map<Class<? extends DatabaseObject>, Node> allNodes = new HashMap<>();

        private void addType(Class<? extends DatabaseObject> type) {
            allNodes.put(type, new Node(type));
        }

        public List<Class<? extends DatabaseObject>> sort(Database database, Class<? extends ChangeGenerator> generatorType) {
            Map<Class<? extends DatabaseObject>, Node> newNodes = new HashMap<>();
            ChangeGeneratorFactory changeGeneratorFactory = ChangeGeneratorFactory.getInstance();
            for (Class<? extends DatabaseObject> type : allNodes.keySet()) {
                //
                // For both run* types
                // make sure that if the Node does not exist
                // it gets created and saved in the newNodes map
                //
                for (Class<? extends DatabaseObject> afterType : changeGeneratorFactory.runBeforeTypes(type, database, generatorType)) {
                    Node typeNode = retrieveOrCreateNode(newNodes, type);
                    Node afterTypeNode = retrieveOrCreateNode(newNodes, afterType);
                    typeNode.addEdge(afterTypeNode);
                }

                for (Class<? extends DatabaseObject> beforeType : changeGeneratorFactory.runAfterTypes(type, database, generatorType)) {
                    Node beforeTypeNode = retrieveOrCreateNode(newNodes, beforeType);
                    Node typeNode = retrieveOrCreateNode(newNodes, type);
                    beforeTypeNode.addEdge(typeNode);
                }
            }

            //
            // Add any newly created Node objects to the allNodes map
            //
            for (Node newNode : newNodes.values()) {
                if (! allNodes.containsKey(newNode.type)) {
                    allNodes.put(newNode.type, newNode);
                }
            }

            ArrayList<Node> returnNodes = new ArrayList<>();

            SortedSet<Node> nodesWithNoIncomingEdges = new TreeSet<>(Comparator.comparing(o -> o.type.getName()));
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

        //
        // If the Node for this type already exists then return it
        // else look in the newNodes map for one
        // else create a new Node and put it in the newNodes map
        //
        private Node retrieveOrCreateNode(Map<Class<? extends DatabaseObject>, Node> newNodes, Class<? extends DatabaseObject> type) {
            Node node;
            if (allNodes.containsKey(type)) {
                node = allNodes.get(type);
            } else if (newNodes.containsKey(type)) {
                node = newNodes.get(type);
            }
            else {
                node = new Node(type);
                newNodes.put(type, node);
            }
            return node;
        }

        private void checkForCycleInDependencies(Class<? extends ChangeGenerator> generatorType) {
            //Check to see if all edges are removed
            for (Node n : allNodes.values()) {
                if (!n.inEdges.isEmpty()) {
                    StringBuilder message = new StringBuilder("Could not resolve " + generatorType.getSimpleName() + " dependencies due " +
                            "to dependency cycle. Dependencies: \n");

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
                        message.append("    [").append(from).append("] -> ").append(node.type.getSimpleName()).append(" -> [").append(to).append("]\n");
                    }

                    throw new UnexpectedLiquibaseException(message.toString());
                }
            }
        }

        static class Node {
            public final Class<? extends DatabaseObject> type;
            public final Set<Edge> inEdges;
            public final Set<Edge> outEdges;

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
