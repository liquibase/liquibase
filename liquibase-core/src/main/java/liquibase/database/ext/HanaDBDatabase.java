package liquibase.database.ext;


import liquibase.database.AbstractDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;

import java.util.HashSet;
import java.util.Set;


public class HanaDBDatabase extends AbstractDatabase {

    public static final String PRODUCT_NAME = "HDB";
    protected Set<String> systemTablesAndViews = new HashSet<String>();

    public HanaDBDatabase() {
        super();
        systemTablesAndViews.add("---");

        systemTablesAndViews.add("AUDIT_POLICIES");
        systemTablesAndViews.add("AUTHORIZATION_GRAPH");
        systemTablesAndViews.add("CONSTRAINTS");
        systemTablesAndViews.add("CS_BO_VIEWS");
        systemTablesAndViews.add("CS_FREESTYLE_COLUMNS");
        systemTablesAndViews.add("CS_JOIN_CONDITIONS");
        systemTablesAndViews.add("CS_JOIN_CONSTRAINTS");
        systemTablesAndViews.add("CS_JOIN_PATHS");
        systemTablesAndViews.add("CS_JOIN_TABLES");
        systemTablesAndViews.add("CS_KEY_FIGURES");
        systemTablesAndViews.add("CS_VIEW_COLUMNS");
        systemTablesAndViews.add("DATA_TYPES");
        systemTablesAndViews.add("EFFECTIVE_PRIVILEGES");
        systemTablesAndViews.add("EXPLAIN_PLAN_TABLE");
        systemTablesAndViews.add("FULLTEXT_INDEXES");
        systemTablesAndViews.add("FUNCTIONS");
        systemTablesAndViews.add("FUNCTION_PARAMETERS");
        systemTablesAndViews.add("GRANTED_PRIVILEGES");
        systemTablesAndViews.add("GRANTED_ROLES");
        systemTablesAndViews.add("INDEXES");
        systemTablesAndViews.add("INDEX_COLUMNS");
        systemTablesAndViews.add("INVALID_CONNECT_ATTEMPTS");
        systemTablesAndViews.add("M_ATTACHED_STORAGES");
        systemTablesAndViews.add("M_BACKUP_CATALOG");
        systemTablesAndViews.add("M_BACKUP_CATALOG_FILES");
        systemTablesAndViews.add("M_BACKUP_CONFIGURATION");
        systemTablesAndViews.add("M_BLOCKED_TRANSACTIONS");
        systemTablesAndViews.add("M_CACHES");
        systemTablesAndViews.add("M_CACHES_RESET");
        systemTablesAndViews.add("M_CACHE_ENTRIES");
        systemTablesAndViews.add("M_CATALOG_MEMORY");
        systemTablesAndViews.add("M_CE_CALCSCENARIOS");
        systemTablesAndViews.add("M_CE_CALCVIEW_DEPENDENCIES");
        systemTablesAndViews.add("M_CE_DEBUG_INFOS");
        systemTablesAndViews.add("M_CE_DEBUG_JSONS");
        systemTablesAndViews.add("M_CE_DEBUG_NODE_MAPPING");
        systemTablesAndViews.add("M_CE_PLE_CALCSCENARIOS");
        systemTablesAndViews.add("M_CLIENT_VERSIONS");
        systemTablesAndViews.add("M_COMPACTION_THREAD");
        systemTablesAndViews.add("M_CONDITIONAL_VARIABLES");
        systemTablesAndViews.add("M_CONDITIONAL_VARIABLES_RESET");
        systemTablesAndViews.add("M_CONFIGURATION");
        systemTablesAndViews.add("M_CONNECTIONS");
        systemTablesAndViews.add("M_CONNECTION_STATISTICS");
        systemTablesAndViews.add("M_CONTAINER_DIRECTORY");
        systemTablesAndViews.add("M_CONTAINER_NAME_DIRECTORY");
        systemTablesAndViews.add("M_CONTEXT_MEMORY");
        systemTablesAndViews.add("M_CONTEXT_MEMORY_RESET");
        systemTablesAndViews.add("M_CONVERTER_STATISTICS");
        systemTablesAndViews.add("M_CONVERTER_STATISTICS_RESET");
        systemTablesAndViews.add("M_CS_ALL_COLUMNS");
        systemTablesAndViews.add("M_CS_COLUMNS");
        systemTablesAndViews.add("M_CS_PARTITIONS");
        systemTablesAndViews.add("M_CS_TABLES");
        systemTablesAndViews.add("M_CS_UNLOADS");
        systemTablesAndViews.add("M_DATABASE");
        systemTablesAndViews.add("M_DATABASE_HISTORY");
        systemTablesAndViews.add("M_DATA_VOLUMES");
        systemTablesAndViews.add("M_DATA_VOLUME_PAGE_STATISTICS");
        systemTablesAndViews.add("M_DATA_VOLUME_PAGE_STATISTICS_RESET");
        systemTablesAndViews.add("M_DATA_VOLUME_SUPERBLOCK_STATISTICS");
        systemTablesAndViews.add("M_DELTA_MERGE_STATISTICS");
        systemTablesAndViews.add("M_DISKS");
        systemTablesAndViews.add("M_ERROR_CODES");
        systemTablesAndViews.add("M_EVENTS");
        systemTablesAndViews.add("M_EXPENSIVE_STATEMENTS");
        systemTablesAndViews.add("M_EXPORT_BINARY_STATUS");
        systemTablesAndViews.add("M_EXTRACTORS");
        systemTablesAndViews.add("M_FEATURES");
        systemTablesAndViews.add("M_FULLTEXT_QUEUES");
        systemTablesAndViews.add("M_GARBAGE_COLLECTION_STATISTICS");
        systemTablesAndViews.add("M_GARBAGE_COLLECTION_STATISTICS_RESET");
        systemTablesAndViews.add("M_HEAP_MEMORY");
        systemTablesAndViews.add("M_HEAP_MEMORY_RESET");
        systemTablesAndViews.add("M_HISTORY_INDEX_LAST_COMMIT_ID");
        systemTablesAndViews.add("M_HOST_INFORMATION");
        systemTablesAndViews.add("M_HOST_RESOURCE_UTILIZATION");
        systemTablesAndViews.add("M_IMPORT_BINARY_STATUS");
        systemTablesAndViews.add("M_INIFILES");
        systemTablesAndViews.add("M_INIFILE_CONTENTS");
        systemTablesAndViews.add("M_JOB_PROGRESS");
        systemTablesAndViews.add("M_LANDSCAPE_HOST_CONFIGURATION");
        systemTablesAndViews.add("M_LICENSE");
        systemTablesAndViews.add("M_LICENSE_USAGE_HISTORY");
        systemTablesAndViews.add("M_LIVECACHE_CONTAINER_STATISTICS");
        systemTablesAndViews.add("M_LIVECACHE_CONTAINER_STATISTICS_RESET");
        systemTablesAndViews.add("M_LIVECACHE_LOCKS");
        systemTablesAndViews.add("M_LIVECACHE_LOCK_STATISTICS");
        systemTablesAndViews.add("M_LIVECACHE_LOCK_STATISTICS_RESET");
        systemTablesAndViews.add("M_LIVECACHE_OMS_VERSIONS");
        systemTablesAndViews.add("M_LIVECACHE_PROCEDURE_STATISTICS");
        systemTablesAndViews.add("M_LIVECACHE_PROCEDURE_STATISTICS_RESET");
        systemTablesAndViews.add("M_LIVECACHE_SCHEMA_STATISTICS");
        systemTablesAndViews.add("M_LIVECACHE_SCHEMA_STATISTICS_RESET");
        systemTablesAndViews.add("M_LOCK_WAITS_STATISTICS");
        systemTablesAndViews.add("M_LOG_BUFFERS");
        systemTablesAndViews.add("M_LOG_BUFFERS_RESET");
        systemTablesAndViews.add("M_LOG_PARTITIONS");
        systemTablesAndViews.add("M_LOG_PARTITIONS_RESET");
        systemTablesAndViews.add("M_LOG_SEGMENTS");
        systemTablesAndViews.add("M_LOG_SEGMENTS_RESET");
        systemTablesAndViews.add("M_MEMORY_OBJECTS");
        systemTablesAndViews.add("M_MEMORY_OBJECTS_RESET");
        systemTablesAndViews.add("M_MEMORY_OBJECT_DISPOSITIONS");
        systemTablesAndViews.add("M_MERGED_TRACES");
        systemTablesAndViews.add("M_MONITORS");
        systemTablesAndViews.add("M_MONITOR_COLUMNS");
        systemTablesAndViews.add("M_MUTEXES");
        systemTablesAndViews.add("M_MUTEXES_RESET");
        systemTablesAndViews.add("M_MVCC_TABLES");
        systemTablesAndViews.add("M_OBJECT_LOCKS");
        systemTablesAndViews.add("M_OBJECT_LOCK_STATISTICS");
        systemTablesAndViews.add("M_OBJECT_LOCK_STATISTICS_RESET");
        systemTablesAndViews.add("M_PAGEACCESS_STATISTICS");
        systemTablesAndViews.add("M_PAGEACCESS_STATISTICS_RESET");
        systemTablesAndViews.add("M_PASSWORD_POLICY");
        systemTablesAndViews.add("M_PERFTRACE");
        systemTablesAndViews.add("M_PERSISTENCE_MANAGERS");
        systemTablesAndViews.add("M_PERSISTENCE_MANAGERS_RESET");
        systemTablesAndViews.add("M_PREPARED_STATEMENTS");
        systemTablesAndViews.add("M_READWRITELOCKS");
        systemTablesAndViews.add("M_READWRITELOCKS_RESET");
        systemTablesAndViews.add("M_RECORD_LOCKS");
        systemTablesAndViews.add("M_REORG_ALGORITHMS");
        systemTablesAndViews.add("M_REPO_TRANSPORT_FILES");
        systemTablesAndViews.add("M_RS_INDEXES");
        systemTablesAndViews.add("M_RS_TABLES");
        systemTablesAndViews.add("M_RS_TABLE_VERSION_STATISTICS");
        systemTablesAndViews.add("M_SAVEPOINTS");
        systemTablesAndViews.add("M_SAVEPOINT_STATISTICS");
        systemTablesAndViews.add("M_SAVEPOINT_STATISTICS_RESET");
        systemTablesAndViews.add("M_SEMAPHORES");
        systemTablesAndViews.add("M_SEMAPHORES_RESET");
        systemTablesAndViews.add("M_SERVICES");
        systemTablesAndViews.add("M_SERVICE_COMPONENT_MEMORY");
        systemTablesAndViews.add("M_SERVICE_MEMORY");
        systemTablesAndViews.add("M_SERVICE_NETWORK_IO");
        systemTablesAndViews.add("M_SERVICE_REPLICATION");
        systemTablesAndViews.add("M_SERVICE_STATISTICS");
        systemTablesAndViews.add("M_SERVICE_THREADS");
        systemTablesAndViews.add("M_SERVICE_THREAD_CALLSTACKS");
        systemTablesAndViews.add("M_SERVICE_TRACES");
        systemTablesAndViews.add("M_SERVICE_TYPES");
        systemTablesAndViews.add("M_SESSION_CONTEXT");
        systemTablesAndViews.add("M_SHARED_MEMORY");
        systemTablesAndViews.add("M_SNAPSHOTS");
        systemTablesAndViews.add("M_SQL_PLAN_CACHE");
        systemTablesAndViews.add("M_SQL_PLAN_CACHE_OVERVIEW");
        systemTablesAndViews.add("M_SQL_PLAN_CACHE_RESET");
        systemTablesAndViews.add("M_SYSTEM_INFORMATION_STATEMENTS");
        systemTablesAndViews.add("M_SYSTEM_LIMITS");
        systemTablesAndViews.add("M_SYSTEM_OVERVIEW");
        systemTablesAndViews.add("M_TABLES");
        systemTablesAndViews.add("M_TABLE_LOB_FILES");
        systemTablesAndViews.add("M_TABLE_LOCATIONS");
        systemTablesAndViews.add("M_TABLE_PERSISTENCE_LOCATIONS");
        systemTablesAndViews.add("M_TABLE_PERSISTENCE_STATISTICS");
        systemTablesAndViews.add("M_TABLE_VIRTUAL_FILES");
        systemTablesAndViews.add("M_TEMPORARY_TABLES");
        systemTablesAndViews.add("M_TEMPORARY_TABLE_COLUMNS");
        systemTablesAndViews.add("M_TEMPORARY_VIEWS");
        systemTablesAndViews.add("M_TEMPORARY_VIEW_COLUMNS");
        systemTablesAndViews.add("M_TENANTS");
        systemTablesAndViews.add("M_TEXT_ANALYSIS_LANGUAGES");
        systemTablesAndViews.add("M_TEXT_ANALYSIS_MIME_TYPES");
        systemTablesAndViews.add("M_TOPOLOGY_TREE");
        systemTablesAndViews.add("M_TRACEFILES");
        systemTablesAndViews.add("M_TRACEFILE_CONTENTS");
        systemTablesAndViews.add("M_TRANSACTIONS");
        systemTablesAndViews.add("M_UNDO_CLEANUP_FILES");
        systemTablesAndViews.add("M_VERSION_MEMORY");
        systemTablesAndViews.add("M_VOLUMES");
        systemTablesAndViews.add("M_VOLUME_FILES");
        systemTablesAndViews.add("M_VOLUME_IO_PERFORMANCE_STATISTICS");
        systemTablesAndViews.add("M_VOLUME_IO_PERFORMANCE_STATISTICS_RESET");
        systemTablesAndViews.add("M_VOLUME_IO_STATISTICS");
        systemTablesAndViews.add("M_VOLUME_IO_STATISTICS_RESET");
        systemTablesAndViews.add("M_VOLUME_SIZES");
        systemTablesAndViews.add("M_WORKLOAD");
        systemTablesAndViews.add("M_XS_APPLICATIONS");
        systemTablesAndViews.add("M_XS_APPLICATION_ISSUES");
        systemTablesAndViews.add("OBJECTS");
        systemTablesAndViews.add("OBJECT_DEPENDENCIES");
        systemTablesAndViews.add("OWNERSHIP");
        systemTablesAndViews.add("PRIVILEGES");
        systemTablesAndViews.add("PROCEDURES");
        systemTablesAndViews.add("PROCEDURE_OBJECTS");
        systemTablesAndViews.add("PROCEDURE_PARAMETERS");
        systemTablesAndViews.add("QUERY_PLANS");
        systemTablesAndViews.add("REFERENTIAL_CONSTRAINTS");
        systemTablesAndViews.add("REORG_OVERVIEW");
        systemTablesAndViews.add("REORG_PLAN");
        systemTablesAndViews.add("REORG_PLAN_INFOS");
        systemTablesAndViews.add("REORG_STEPS");
        systemTablesAndViews.add("ROLES");
        systemTablesAndViews.add("SAML_PROVIDERS");
        systemTablesAndViews.add("SAML_USER_MAPPINGS");
        systemTablesAndViews.add("SCHEMAS");
        systemTablesAndViews.add("SEQUENCES");
        systemTablesAndViews.add("SQLSCRIPT_TRACE");
        systemTablesAndViews.add("STATISTICS");
        systemTablesAndViews.add("STRUCTURED_PRIVILEGES");
        systemTablesAndViews.add("SYNONYMS");
        systemTablesAndViews.add("TABLES");
        systemTablesAndViews.add("TABLE_COLUMNS");
        systemTablesAndViews.add("TABLE_COLUMNS_ODBC");
        systemTablesAndViews.add("TABLE_GROUPS");
        systemTablesAndViews.add("TRANSACTION_HISTORY");
        systemTablesAndViews.add("TRIGGERS");
        systemTablesAndViews.add("USERS");
        systemTablesAndViews.add("USER_PARAMETERS");
        systemTablesAndViews.add("VIEWS");
        systemTablesAndViews.add("VIEW_COLUMNS");

        systemTablesAndViews.add("GLOBAL_COLUMN_TABLES_SIZE");
        systemTablesAndViews.add("GLOBAL_CPU_STATISTICS");
        systemTablesAndViews.add("GLOBAL_INTERNAL_DISKFULL_EVENTS");
        systemTablesAndViews.add("GLOBAL_INTERNAL_EVENTS");
        systemTablesAndViews.add("GLOBAL_MEMORY_STATISTICS");
        systemTablesAndViews.add("GLOBAL_PERSISTENCE_STATISTICS");
        systemTablesAndViews.add("GLOBAL_TABLES_SIZE");
        systemTablesAndViews.add("HOST_BLOCKED_TRANSACTIONS");
        systemTablesAndViews.add("HOST_COLUMN_TABLES_PART_SIZE");
        systemTablesAndViews.add("HOST_DATA_VOLUME_PAGE_STATISTICS");
        systemTablesAndViews.add("HOST_DATA_VOLUME_SUPERBLOCK_STATISTICS");
        systemTablesAndViews.add("HOST_DELTA_MERGE_STATISTICS");
        systemTablesAndViews.add("HOST_HEAP_ALLOCATORS");
        systemTablesAndViews.add("HOST_LONG_RUNNING_STATEMENTS");
        systemTablesAndViews.add("HOST_MEMORY_STATISTICS");
        systemTablesAndViews.add("HOST_ONE_DAY_FILE_COUNT");
        systemTablesAndViews.add("HOST_RESOURCE_UTILIZATION_STATISTICS");
        systemTablesAndViews.add("HOST_SERVICE_MEMORY");
        systemTablesAndViews.add("HOST_SERVICE_STATISTICS");
        systemTablesAndViews.add("HOST_TABLE_VIRTUAL_FILES");
        systemTablesAndViews.add("HOST_VIRTUAL_FILES");
        systemTablesAndViews.add("HOST_VOLUME_FILES");
        systemTablesAndViews.add("HOST_VOLUME_IO_PERFORMANCE_STATISTICS");
        systemTablesAndViews.add("HOST_VOLUME_IO_STATISTICS");
        systemTablesAndViews.add("STATISTICS_ALERTS");
        systemTablesAndViews.add("STATISTICS_ALERT_INFORMATION");
        systemTablesAndViews.add("STATISTICS_ALERT_LAST_CHECK_INFORMATION");
        systemTablesAndViews.add("STATISTICS_INTERVAL_INFORMATION");
        systemTablesAndViews.add("STATISTICS_LASTVALUES");
        systemTablesAndViews.add("STATISTICS_STATE");
        systemTablesAndViews.add("STATISTICS_VERSION");
        systemTablesAndViews.add("STATISTICS_ALERTS");
    }

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public Set<String> getSystemTablesAndViews() {
        return systemTablesAndViews;
    }

    public String getTypeName() {
        return "hanadb";
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsSequences() {
        return true;
    }

    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:sapdb")) {
            return "com.sap.db.jdbc.Driver";
        }
        return null;
    }

    public String getCurrentDateTimeFunction() {
        if (currentDateTimeFunction != null) {
            return currentDateTimeFunction;
        }
        
        return "CURRENT_TIMESTAMP";
    }

    @Override
    protected String getDefaultDatabaseSchemaName() throws DatabaseException {//NOPMD
        return super.getDefaultDatabaseSchemaName().toUpperCase();
    }

    @Override
    public boolean isSystemTable(String catalogName, String schemaName, String tableName) {
        if (super.isSystemTable(catalogName, schemaName, tableName)) {
            return true;
        } else if ("_SYS_SECURITY".equalsIgnoreCase(schemaName)) {
            return true;
        } else if ("_SYS_REPO".equalsIgnoreCase(schemaName)) {
            return true;
        } else if ("_SYS_STATISTICS".equalsIgnoreCase(schemaName)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isSystemView(String catalogName, String schemaName, String tableName) {
        if (super.isSystemView(catalogName, schemaName, tableName)) {
            return true;
        } else if ("_SYS_SECURITY".equalsIgnoreCase(schemaName)) {
            return true;
        } else if ("_SYS_REPO".equalsIgnoreCase(schemaName)) {
            return true;
        } else if ("_SYS_STATISTICS".equalsIgnoreCase(schemaName)) {
            return true;
        }
        return false;
    }

    public boolean supportsTablespaces() {
        return true;
    }

    @Override
    public boolean supportsAutoIncrement() {
        return false;
    }
}
