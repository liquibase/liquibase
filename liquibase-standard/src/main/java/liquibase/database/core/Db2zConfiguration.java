package liquibase.database.core;

import liquibase.configuration.AutoloadedConfigurations;
import liquibase.configuration.ConfigurationDefinition;

/**
 * Configuration for DB2 z/OS specific parameters
 */
public class Db2zConfiguration implements AutoloadedConfigurations {

    public static final String LIQUIBASE_DB2Z_NAMESPACE = "liquibase.db2z";
    
    // DATABASECHANGELOG table configurations
    public static final ConfigurationDefinition<String> DATABASECHANGELOG_DATABASE;
    public static final ConfigurationDefinition<String> DATABASECHANGELOG_TABLESPACE;
    public static final ConfigurationDefinition<String> DATABASECHANGELOG_INDEX;
    
    // DATABASECHANGELOGLOCK table configurations
    public static final ConfigurationDefinition<String> DATABASECHANGELOGLOCK_DATABASE;
    public static final ConfigurationDefinition<String> DATABASECHANGELOGLOCK_TABLESPACE;
    public static final ConfigurationDefinition<String> DATABASECHANGELOGLOCK_INDEX;

    static {
        ConfigurationDefinition.Builder builder = new ConfigurationDefinition.Builder(LIQUIBASE_DB2Z_NAMESPACE);

        // DATABASECHANGELOG configurations
        DATABASECHANGELOG_DATABASE = builder.define("databasechangelog.database", String.class)
                .setDescription("The database name for the DATABASECHANGELOG table on DB2 z/OS")
                .build();
                
        DATABASECHANGELOG_TABLESPACE = builder.define("databasechangelog.tablespace", String.class)
                .setDescription("The tablespace name for the DATABASECHANGELOG table on DB2 z/OS")
                .build();
                
        DATABASECHANGELOG_INDEX = builder.define("databasechangelog.index", String.class)
                .setDescription("The index name for the DATABASECHANGELOG table on DB2 z/OS")
                .build();
                
        // DATABASECHANGELOGLOCK configurations
        DATABASECHANGELOGLOCK_DATABASE = builder.define("databasechangeloglock.database", String.class)
                .setDescription("The database name for the DATABASECHANGELOGLOCK table on DB2 z/OS")
                .build();
                
        DATABASECHANGELOGLOCK_TABLESPACE = builder.define("databasechangeloglock.tablespace", String.class)
                .setDescription("The tablespace name for the DATABASECHANGELOGLOCK table on DB2 z/OS")
                .build();
                
        DATABASECHANGELOGLOCK_INDEX = builder.define("databasechangeloglock.index", String.class)
                .setDescription("The index name for the DATABASECHANGELOGLOCK table on DB2 z/OS")
                .build();
    }
}