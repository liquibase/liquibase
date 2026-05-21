package liquibase.integration.ant.type;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.DatabaseException;
import liquibase.resource.ResourceAccessor;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

import java.util.Properties;

public class DatabaseType extends DataType {
    private static final String USER_PROPERTY_NAME = "user";
    // SONAR thinks this is a hard-coded password, but actually it is only the
    // property name for the password
    @SuppressWarnings("squid:S2068")
    private static final String PASSWORD_PROPERTY_NAME = "password";

    private String driver;
    private String url;
    private String user;
    private String password;
    private ConnectionProperties connectionProperties;
    private String defaultSchemaName;
    private String defaultCatalogName;
    private String currentDateTimeFunction;
    private boolean outputDefaultSchema = true;
    private boolean outputDefaultCatalog = true;
    private String liquibaseSchemaName;
    private String liquibaseCatalogName;
    private String databaseClass;
    private String databaseChangeLogTableName;
    private String databaseChangeLogLockTableName;
    private String liquibaseTablespaceName;

    private boolean credentialCleanupRegistered = false;

    public DatabaseType(Project project) {
        setProject(project);
    }

    public Database createDatabase(ResourceAccessor resourceAccessor) {
        registerCredentialCleanup();
        logParameters();
        validateParameters();
        try {
            DatabaseFactory databaseFactory = DatabaseFactory.getInstance();

            Properties connectionProps = new Properties();
            String connectionUserName = getUser();
            if((connectionUserName != null) && !connectionUserName.isEmpty()) {
                connectionProps.setProperty(USER_PROPERTY_NAME, connectionUserName);
            }
            String connectionPassword = getPassword();
            if((connectionPassword != null) && !connectionPassword.isEmpty()) {
                connectionProps.setProperty(PASSWORD_PROPERTY_NAME, connectionPassword);
            }
            ConnectionProperties dbConnectionProperties = getConnectionProperties();
            if(dbConnectionProperties != null) {
                connectionProps.putAll(dbConnectionProperties.buildProperties());
            }

            Database database = databaseFactory.openDatabase(
                    getUrl(),
                    getUser(),
                    getDriver(),
                    getDatabaseClass(),
                    connectionProps,
                    resourceAccessor);

            String schemaName = getDefaultSchemaName();
            if (schemaName != null) {
                database.setDefaultSchemaName(schemaName);
            }
            String catalogName = getDefaultCatalogName();
            if (catalogName != null) {
                database.setDefaultCatalogName(catalogName);
            }
            String dbmsCurrentDateTimeFunction = getCurrentDateTimeFunction();
            if(dbmsCurrentDateTimeFunction != null) {
                database.setCurrentDateTimeFunction(dbmsCurrentDateTimeFunction);
            }

            database.setOutputDefaultSchema(isOutputDefaultSchema());
            database.setOutputDefaultCatalog(isOutputDefaultCatalog());

            String connLiquibaseSchemaName = getLiquibaseSchemaName();
            if (liquibaseSchemaName != null) {
                database.setLiquibaseSchemaName(connLiquibaseSchemaName);
            }
            
            String connLiquibaseCatalogName = getLiquibaseCatalogName();
            if(connLiquibaseCatalogName != null) {
                database.setLiquibaseCatalogName(connLiquibaseCatalogName);
            }

            String connDatabaseChangeLogTableName = getDatabaseChangeLogTableName();
            if(connDatabaseChangeLogTableName != null) {
                database.setDatabaseChangeLogTableName(connDatabaseChangeLogTableName);
            }
            
            String connDatabaseChangeLogLockTableName = getDatabaseChangeLogLockTableName();
            if(connDatabaseChangeLogLockTableName != null) {
                database.setDatabaseChangeLogLockTableName(connDatabaseChangeLogLockTableName);
            }
            
            String connLiquibaseTablespaceName = getLiquibaseTablespaceName();
            if(connLiquibaseTablespaceName != null) {
                database.setLiquibaseTablespaceName(connLiquibaseTablespaceName);
            }

            return database;
        } catch (DatabaseException e) {
            throw new BuildException("Unable to create Liquibase database instance. " + e, e);
        }
    }

    private void validateParameters() {
        if(getUrl() == null) {
            throw new BuildException("JDBC URL is required.");
        }
    }

    private void logParameters() {
        log("Creating Liquibase Database", Project.MSG_DEBUG);
        log("JDBC driver: " + driver, Project.MSG_DEBUG);
        log("JDBC URL: " + url, Project.MSG_DEBUG);
        log("JDBC username: " + user, Project.MSG_DEBUG);
        log("Default catalog name: " + defaultCatalogName, Project.MSG_DEBUG);
        log("Default schema name: " + defaultSchemaName, Project.MSG_DEBUG);
        log("Liquibase catalog name: " + liquibaseCatalogName, Project.MSG_DEBUG);
        log("Liquibase schema name: " + liquibaseSchemaName, Project.MSG_DEBUG);
        log("Liquibase tablespace name: " + liquibaseTablespaceName, Project.MSG_DEBUG);
        log("Database changelog table name: " + databaseChangeLogTableName, Project.MSG_DEBUG);
        log("Database changelog lock table name: " + databaseChangeLogLockTableName, Project.MSG_DEBUG);
        log("Output default catalog: " + outputDefaultCatalog, Project.MSG_DEBUG);
        log("Output default schema: " + outputDefaultSchema, Project.MSG_DEBUG);
        log("Current date/time function: " + currentDateTimeFunction, Project.MSG_DEBUG);
        log("Database class: " + databaseClass, Project.MSG_DEBUG);
    }

    @Override
    public void setRefid(Reference ref) {
        if((driver != null) || (url != null) || (user != null) || (password != null) || (defaultSchemaName != null)
            || (defaultCatalogName != null) || (currentDateTimeFunction != null) || (databaseClass != null) ||
            (liquibaseSchemaName != null) || (liquibaseCatalogName != null) || (databaseChangeLogTableName != null)
            || (databaseChangeLogLockTableName != null) || (liquibaseTablespaceName != null)) {
            throw tooManyAttributes();
        }
        super.setRefid(ref);
    }

    public String getDriver() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getDriver() : driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getUrl() : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getUser() : user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getPassword() : password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ConnectionProperties getConnectionProperties() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getConnectionProperties() : connectionProperties;
    }

    public void addConnectionProperties(ConnectionProperties connectionProperties) {
        if(this.connectionProperties != null) {
            throw new BuildException("Only one <connectionProperties> element is allowed.");
        }
        this.connectionProperties = connectionProperties;
    }

    public String getDefaultSchemaName() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getDefaultSchemaName() : defaultSchemaName;
    }

    public void setDefaultSchemaName(String defaultSchemaName) {
        this.defaultSchemaName = defaultSchemaName;
    }

    public String getDefaultCatalogName() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getDefaultCatalogName() : defaultCatalogName;
    }

    public void setDefaultCatalogName(String defaultCatalogName) {
        this.defaultCatalogName = defaultCatalogName;
    }

    public String getCurrentDateTimeFunction() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getCurrentDateTimeFunction() : currentDateTimeFunction;
    }

    public void setCurrentDateTimeFunction(String currentDateTimeFunction) {
        this.currentDateTimeFunction = currentDateTimeFunction;
    }

    public boolean isOutputDefaultSchema() {
        return isReference() ? ((DatabaseType) getCheckedRef()).isOutputDefaultSchema() : outputDefaultSchema;
    }

    public void setOutputDefaultSchema(boolean outputDefaultSchema) {
        this.outputDefaultSchema = outputDefaultSchema;
    }

    public boolean isOutputDefaultCatalog() {
        return isReference() ? ((DatabaseType) getCheckedRef()).isOutputDefaultCatalog() : outputDefaultCatalog;
    }

    public void setOutputDefaultCatalog(boolean outputDefaultCatalog) {
        this.outputDefaultCatalog = outputDefaultCatalog;
    }

    public String getDatabaseClass() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getDatabaseClass() : databaseClass;
    }

    public void setDatabaseClass(String databaseClass) {
        this.databaseClass = databaseClass;
    }

    public String getLiquibaseSchemaName() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getLiquibaseSchemaName() : liquibaseSchemaName;
    }

    public void setLiquibaseSchemaName(String liquibaseSchemaName) {
        this.liquibaseSchemaName = liquibaseSchemaName;
    }

    public String getLiquibaseCatalogName() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getLiquibaseCatalogName() : liquibaseCatalogName;
    }

    public void setLiquibaseCatalogName(String liquibaseCatalogName) {
        this.liquibaseCatalogName = liquibaseCatalogName;
    }

    public String getDatabaseChangeLogTableName() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getDatabaseChangeLogTableName() : databaseChangeLogTableName;
    }

    public void setDatabaseChangeLogTableName(String databaseChangeLogTableName) {
        this.databaseChangeLogTableName = databaseChangeLogTableName;
    }

    public String getDatabaseChangeLogLockTableName() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getDatabaseChangeLogLockTableName() : databaseChangeLogLockTableName;
    }

    public void setDatabaseChangeLogLockTableName(String databaseChangeLogLockTableName) {
        this.databaseChangeLogLockTableName = databaseChangeLogLockTableName;
    }

    public String getLiquibaseTablespaceName() {
        return isReference() ? ((DatabaseType) getCheckedRef()).getLiquibaseTablespaceName() : liquibaseTablespaceName;
    }

    public void setLiquibaseTablespaceName(String liquibaseTablespaceName) {
        this.liquibaseTablespaceName = liquibaseTablespaceName;
    }

    /**
     * Register a one-shot Project BuildListener that nulls this DatabaseType's
     * credential fields when the Ant build finishes (CWE-316: Cleartext Storage
     * of Sensitive Information in Memory). Idempotent — only the first call per
     * instance registers the listener.
     * <p>
     * <b>Why <code>buildFinished</code> and not at task end?</b> Ant's
     * <code>&lt;typedef&gt;</code> + <code>databaseRef</code> pattern is
     * specifically designed to let multiple tasks share a single
     * <code>&lt;database id="…"&gt;</code> definition (e.g. an update followed
     * by a rollback followed by a status check, all pointing at the same DB).
     * Clearing credentials at task end would break the second and third tasks.
     * <code>buildFinished</code> fires after the very last task on the project,
     * so credentials are gone before the JVM idles but never while another task
     * still needs them.
     * <p>
     * <b>Why the listener deregisters itself.</b> In a one-shot Ant CLI build the
     * Project is GC'd after the build and the listener with it. But the audit
     * threat model specifically covers long-running embedded hosts (Ant embedded
     * in a build daemon, IDE plugin, MPS-style tool) that reuse one Project
     * across many builds. Without explicit deregistration, every new DatabaseType
     * would permanently add a listener; each listener holds a strong reference to
     * its closed-over DatabaseType, accumulating empty (post-clear) shells for
     * the host's lifetime — exactly the residency-window problem the rest of this
     * audit slice is narrowing. {@code event.getProject().removeBuildListener(this)}
     * inside {@code buildFinished} makes the listener truly one-shot.
     * <p>
     * Called automatically from {@link #createDatabase(ResourceAccessor)} so any
     * Ant task that consumes this type opts in implicitly; package-private
     * accessibility allows direct testing.
     */
    void registerCredentialCleanup() {
        if (credentialCleanupRegistered) {
            return;
        }
        final Project project = getProject();
        if (project == null) {
            return;
        }
        credentialCleanupRegistered = true;
        project.addBuildListener(new BuildListener() {
            @Override public void buildStarted(BuildEvent event) {}
            @Override public void buildFinished(BuildEvent event) {
                try {
                    clearCredentials();
                } finally {
                    // One-shot: detach the listener so it (and the DatabaseType
                    // it closes over) can be GC'd in long-lived embedded Ant
                    // scenarios where the Project survives across many builds.
                    // Use the captured `project` instead of event.getProject()
                    // so the deregister target is the same Project we attached
                    // to, regardless of which Project fired the event.
                    project.removeBuildListener(this);
                    // Reset the idempotency flag so a SUBSEQUENT build on the
                    // same DatabaseType instance — possible in long-lived
                    // embedded hosts that reuse DataType instances across
                    // builds — can register a fresh listener. Without this
                    // reset, build 1's credentials would be cleared but build
                    // 2 and onward would silently skip re-registration (per
                    // @coderabbitai's review on #7743).
                    credentialCleanupRegistered = false;
                }
            }
            @Override public void targetStarted(BuildEvent event) {}
            @Override public void targetFinished(BuildEvent event) {}
            @Override public void taskStarted(BuildEvent event) {}
            @Override public void taskFinished(BuildEvent event) {}
            @Override public void messageLogged(BuildEvent event) {}
        });
    }

    /**
     * Null the credential-bearing fields. Strings are immutable in Java and
     * cannot be wiped in place; nulling the field is the closest equivalent and
     * makes the String GC-eligible if no other references hold it.
     * <p>
     * For refid-based shells whose getters delegate to a referenced
     * <code>DatabaseType</code> (the typedef+databaseRef pattern), also clear
     * the referenced instance's credentials so the actual shared field is
     * wiped. Safe to call multiple times on the same referenced instance —
     * subsequent calls are no-ops because the field is already null.
     */
    void clearCredentials() {
        this.password = null;
        // Also wipe credential-bearing <connectionProperty> values — per
        // @filipelautert's review on #7743, a build that uses
        //   <connectionProperty name="password" value="hunter2"/>
        // instead of (or in addition to) <password> would otherwise leave the
        // raw value sitting in the Property list past buildFinished. Walk only
        // the local connectionProperties — for refid shells, setRefid() rejects
        // any locally-set attribute, so connectionProperties is always null on
        // the shell; the referenced DatabaseType's connectionProperties get
        // cleared via the refid traversal below.
        if (this.connectionProperties != null) {
            this.connectionProperties.clearCredentialValues();
        }
        if (isReference()) {
            try {
                // Type-safe Ant API (since 1.8) — the zero-arg getCheckedRef() form is
                // deprecated. getCheckedRef(Class, String) returns the dereferenced
                // value already cast, or throws BuildException if the refid does not
                // resolve to a DatabaseType (which is the only valid use here).
                DatabaseType ref = getCheckedRef(DatabaseType.class, "database");
                ref.clearCredentials();
            } catch (BuildException ignored) {
                // The referenced object may no longer be resolvable at buildFinished
                // time (project tear-down), or the refid may point to a non-DatabaseType.
                // The local shell's field is already null.
            }
        }
    }
}
