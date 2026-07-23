package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.changelog.column.LiquibaseColumn;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawCallStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Common base for the PostgreSQL wire-protocol family (real PostgreSQL plus variants such as
 * Redshift, CockroachDB, EnterpriseDB and YugabyteDB). It holds <strong>only</strong> behavior that
 * is genuinely shared by every member of the family: identifier case-folding and quoting, the
 * reserved-word set, the catalog-less schema model, system-object recognition, and the lowercased
 * changelog table names.
 * <p>
 * This is phase 1 of INT-2139 (the Postgres-family inheritance refactor) and is deliberately
 * <strong>behavior-neutral</strong>: {@link PostgresDatabase} extends this class and no variant is
 * reparented yet, so the runtime type hierarchy is unchanged.
 * <p>
 * <strong>What intentionally stays in {@link PostgresDatabase}</strong> (real-Postgres-specific, and
 * candidates for the explicit-capability work in the next story rather than for moving here):
 * sequences, SERIAL / {@code GENERATED ... AS IDENTITY} auto-increment, tablespaces, initially
 * deferrable columns, changelog-history support, {@code CREATE ... IF NOT EXISTS}, the product
 * name / minimum-version identity check, the default driver and port, and the search-path reset on
 * rollback. These are exactly the features the variants do <em>not</em> share, so they must become
 * opt-in capability methods, not inherited defaults.
 */
public abstract class AbstractPostgresDatabase extends AbstractJdbcDatabase {

    private final Set<String> systemTablesAndViews = new HashSet<>();

    private final Set<String> reservedWords = new HashSet<>();

    protected AbstractPostgresDatabase() {
        // "Reserved" or "reserved (can be function or type)" in PostgreSQL
        // from https://www.postgresql.org/docs/9.6/static/sql-keywords-appendix.html
        reservedWords.addAll(Arrays.asList("ALL", "ANALYSE", "ANALYZE", "AND", "ANY", "ARRAY", "AS", "ASC",
                "ASYMMETRIC", "AUTHORIZATION", "BINARY", "BOTH", "CASE", "CAST", "CHECK", "COLLATE", "COLLATION",
                "COLUMN", "CONCURRENTLY", "CONSTRAINT", "CREATE", "CROSS", "CURRENT_CATALOG", "CURRENT_DATE",
                "CURRENT_ROLE", "CURRENT_SCHEMA", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "DEFAULT",
                "DEFERRABLE", "DESC", "DISTINCT", "DO", "ELSE", "END", "EXCEPT", "FALSE", "FETCH", "FOR", "FOREIGN",
                "FREEZE", "FROM", "FULL", "GRANT", "GROUP", "HAVING", "ILIKE", "IN", "INITIALLY", "INNER", "INTERSECT",
                "INTO", "IS", "ISNULL", "JOIN", "LATERAL", "LEADING", "LEFT", "LIKE", "LIMIT", "LOCALTIME",
                "LOCALTIMESTAMP", "NATURAL", "NOT", "NOTNULL", "NULL", "OFFSET", "ON", "ONLY", "OR", "ORDER", "OUTER",
                "OVERLAPS", "PLACING", "PRIMARY", "REFERENCES", "RETURNING", "RIGHT", "SELECT", "SESSION_USER",
                "SIMILAR", "SOME", "SYMMETRIC", "TABLE", "TABLESAMPLE", "THEN", "TO", "TRAILING", "TRUE", "UNION",
                "UNIQUE", "USER", "USING", "VARIADIC", "VERBOSE", "WHEN", "WHERE", "WINDOW", "WITH"));
        super.unquotedObjectsAreUppercased = false;

        systemTablesAndViews.add("pg_stat_statements");
        systemTablesAndViews.add("pg_stat_statements_info");
    }

    @Override
    public Set<String> getSystemViews() {
        return systemTablesAndViews;
    }

    @Override
    public boolean supportsCatalogInObjectName(Class<? extends DatabaseObject> type) {
        return false;
    }

    @Override
    public String getDatabaseChangeLogTableName() {
        return super.getDatabaseChangeLogTableName().toLowerCase(Locale.US);
    }

    @Override
    public String getDatabaseChangeLogLockTableName() {
        return super.getDatabaseChangeLogLockTableName().toLowerCase(Locale.US);
    }

    @Override
    public String unescapeDataTypeName(String dataTypeName) {
        return dataTypeName.replace("\"", "");
    }

    @Override
    public boolean isSystemObject(DatabaseObject example) {
        // All tables in the schemas pg_catalog and pg_toast are definitely system tables.
        if (example instanceof Table && example.getSchema() != null
                && ("pg_catalog".equals(example.getSchema().getName()) || "pg_toast".equals(example.getSchema().getName()))) {
            return true;
        }

        return super.isSystemObject(example);
    }

    /**
     * This has special case logic to handle NOT quoting column names if they are
     * of type 'LiquibaseColumn' - columns in the DATABASECHANGELOG or DATABASECHANGELOGLOCK
     * tables.
     */
    @Override
    public String escapeObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if ((quotingStrategy == ObjectQuotingStrategy.LEGACY) && hasMixedCase(objectName)) {
            return "\"" + objectName + "\"";
        } else if (objectType != null && LiquibaseColumn.class.isAssignableFrom(objectType)) {
            return (objectName != null && !objectName.isEmpty()) ? objectName.trim() : objectName;
        }

        return super.escapeObjectName(objectName, objectType);
    }

    @Override
    public String correctObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if ((objectName == null) || (quotingStrategy != ObjectQuotingStrategy.LEGACY)) {
            return super.correctObjectName(objectName, objectType);
        }
        //
        // Check preserve case flag for schema
        //
        if (objectType.equals(Schema.class) && Boolean.TRUE.equals(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getCurrentValue())) {
            return objectName;
        }

        if(objectType.equals(Catalog.class) && !StringUtil.hasLowerCase(objectName)) {
            return objectName;
        }

        if (objectName.contains("-")
                || hasMixedCase(objectName)
                || startsWithNumeric(objectName)
                || isReservedWord(objectName)) {
            return objectName;
        } else {
            return objectName.toLowerCase(Locale.US);
        }
    }

    /*
     * Check if given string has case problems according to postgresql documentation.
     * If there are at least one characters with upper case while all other are in lower case (or vice versa) this
     * string should be escaped.
     *
     * Note: This may make postgres support more case sensitive than normally is, but needs to be left in for backwards
     * compatibility.
     * Method is public so a subclass extension can override it to always return false.
     */
    protected boolean hasMixedCase(String tableName) {
        if (tableName == null) {
            return false;
        }
        return StringUtil.hasUpperCase(tableName) && StringUtil.hasLowerCase(tableName);
    }

    @Override
    public boolean isReservedWord(String tableName) {
        return reservedWords.contains(tableName.toUpperCase(Locale.US));
    }

    @Override
    protected SqlStatement getConnectionSchemaNameCallStatement() {
        return new RawCallStatement("select current_schema()");
    }

    @Override
    public CatalogAndSchema.CatalogAndSchemaCase getSchemaAndCatalogCase() {
        return CatalogAndSchema.CatalogAndSchemaCase.LOWER_CASE;
    }

    @Override
    public void setDefaultCatalogName(String defaultCatalogName) {
        if (StringUtils.isNotEmpty(defaultCatalogName)) {
            Scope.getCurrentScope().getUI().sendMessage("WARNING: Postgres does not support catalogs, so the values set in 'defaultCatalogName' and 'referenceDefaultCatalogName' will be ignored.");
        }
        super.setDefaultCatalogName(defaultCatalogName);
    }

    // ---------------------------------------------------------------------------------------------
    // Explicit Postgres-family capabilities (INT-2139).
    //
    // The class-level contract: default to the safe/minimal value here, real PostgresDatabase opts
    // in, and variants that lack the feature opt down. This replaces the fragile
    // "inherit-by-default, override-to-disable" pattern (and the off-classpath short-name checks
    // such as getShortName().equalsIgnoreCase("redshift")) that previously lived in the snapshot
    // generators. These live on the Postgres-family SPI for now; they may be promoted to the core
    // Database interface once the model is proven and generalized to other families (6.1+).
    // ---------------------------------------------------------------------------------------------

    /**
     * Whether PostgreSQL-style enumerated types ({@code CREATE TYPE ... AS ENUM (...)}) on this
     * database can be read by the standard enum-type snapshot generator. Named for
     * <em>snapshot-ability</em>, not raw SQL capability.
     *
     * @return true if enum types on this database are snapshot-able via the standard generator
     */
    public boolean supportsEnumTypeSnapshot() {
        return false;
    }

    /**
     * Whether PostgreSQL-style composite types ({@code CREATE TYPE ... AS (...)}) on this database
     * can be read by the standard composite-type snapshot generator. Named for
     * <em>snapshot-ability</em>, not raw SQL capability: a variant may accept the DDL yet return
     * {@code false} here because the standard generator cannot read its catalog.
     *
     * @return true if composite types on this database are snapshot-able via the standard generator
     */
    public boolean supportsCompositeTypeSnapshot() {
        return false;
    }

    /**
     * Whether check constraints on this database can be read by the standard check-constraint
     * snapshot service. Named for <em>snapshot-ability</em>, not enforcement: e.g. Redshift accepts
     * {@code CHECK} in DDL but never enforces it and exposes no usable catalog for it, so it returns
     * {@code false}. (Cross-family concept; non-Postgres families still gate via {@code instanceof}
     * in {@code StandardCheckConstraintService} until the 6.1+ sweep.)
     *
     * @return true if check constraints on this database are snapshot-able via the standard service
     */
    public boolean supportsCheckConstraintSnapshot() {
        return false;
    }

    /**
     * Whether stored database logic (functions, procedures, packages, triggers) can be snapshotted
     * from this database via the standard stored-logic snapshot generators. (Cross-family concept;
     * non-Postgres families still gate via {@code instanceof} until the 6.1+ sweep.)
     *
     * @return true if stored logic on this database is snapshot-able via the standard generators
     */
    public boolean supportsStoredLogicSnapshot() {
        return false;
    }
}
