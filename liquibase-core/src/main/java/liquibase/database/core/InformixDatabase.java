package liquibase.database.core;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import liquibase.CatalogAndSchema;
import liquibase.Contexts;
import liquibase.change.CheckSum;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.statement.core.ModifyDataTypeStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.statement.core.SelectFromDatabaseChangeLogStatement;
import liquibase.statement.core.SetNullableStatement;
import liquibase.statement.core.UpdateChangeSetChecksumStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

public class InformixDatabase extends AbstractJdbcDatabase {

	private static final String PRODUCT_NAME = "Informix Dynamic Server";
    private static final String INTERVAL_FIELD_QUALIFIER = "HOUR TO FRACTION(5)";
    private static final String DATETIME_FIELD_QUALIFIER = "YEAR TO FRACTION(5)";

	private final Set<String> systemTablesAndViews = new HashSet<String>();

    private static final Pattern CREATE_VIEW_AS_PATTERN = Pattern.compile("^CREATE\\s+.*?VIEW\\s+.*?AS\\s+",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	public InformixDatabase() {
        super.setCurrentDateTimeFunction("CURRENT " + DATETIME_FIELD_QUALIFIER);
        super.sequenceNextValueFunction = "%s.NEXTVAL";
        systemTablesAndViews.add("systables");
		systemTablesAndViews.add("syscolumns");
		systemTablesAndViews.add("sysindices");
		systemTablesAndViews.add("systabauth");
		systemTablesAndViews.add("syscolauth");
		systemTablesAndViews.add("sysviews");
		systemTablesAndViews.add("sysusers");
		systemTablesAndViews.add("sysdepend");
		systemTablesAndViews.add("syssynonyms");
		systemTablesAndViews.add("syssyntable");
		systemTablesAndViews.add("sysconstraints");
		systemTablesAndViews.add("sysreferences");
		systemTablesAndViews.add("syschecks");
		systemTablesAndViews.add("sysdefaults");
		systemTablesAndViews.add("syscoldepend");
		systemTablesAndViews.add("sysprocedures");
		systemTablesAndViews.add("sysprocbody");
		systemTablesAndViews.add("sysprocplan");
		systemTablesAndViews.add("sysprocauth");
		systemTablesAndViews.add("sysblobs");
		systemTablesAndViews.add("sysopclstr");
		systemTablesAndViews.add("systriggers");
		systemTablesAndViews.add("systrigbody");
		systemTablesAndViews.add("sysdistrib");
		systemTablesAndViews.add("sysfragments");
		systemTablesAndViews.add("sysobjstate");
		systemTablesAndViews.add("sysviolations");
		systemTablesAndViews.add("sysfragauth");
		systemTablesAndViews.add("sysroleauth");
		systemTablesAndViews.add("sysxtdtypes");
		systemTablesAndViews.add("sysattrtypes");
		systemTablesAndViews.add("sysxtddesc");
		systemTablesAndViews.add("sysinherits");
		systemTablesAndViews.add("syscolattribs");
		systemTablesAndViews.add("syslogmap");
		systemTablesAndViews.add("syscasts");
		systemTablesAndViews.add("sysxtdtypeauth");
		systemTablesAndViews.add("sysroutinelangs");
		systemTablesAndViews.add("syslangauth");
		systemTablesAndViews.add("sysams");
		systemTablesAndViews.add("systabamdata");
		systemTablesAndViews.add("sysopclasses");
		systemTablesAndViews.add("syserrors");
		systemTablesAndViews.add("systraceclasses");
		systemTablesAndViews.add("systracemsgs");
		systemTablesAndViews.add("sysaggregates");
		systemTablesAndViews.add("syssequences");
		systemTablesAndViews.add("sysdirectives");
		systemTablesAndViews.add("sysxasourcetypes");
		systemTablesAndViews.add("sysxadatasources");
		systemTablesAndViews.add("sysseclabelcomponents");
		systemTablesAndViews.add("sysseclabelcomponentelements");
		systemTablesAndViews.add("syssecpolicies");
		systemTablesAndViews.add("syssecpolicycomponents");
		systemTablesAndViews.add("syssecpolicyexemptions");
		systemTablesAndViews.add("sysseclabels");
		systemTablesAndViews.add("sysseclabelnames");
		systemTablesAndViews.add("sysseclabelauth");
		systemTablesAndViews.add("syssurrogateauth");
		systemTablesAndViews.add("sysproccolumns");

		systemTablesAndViews.add("sysdomains");
		systemTablesAndViews.add("sysindexes");
        super.sequenceNextValueFunction = "%s.NEXTVAL";
        super.sequenceCurrentValueFunction = "%s.CURRVAL";
	}

	@Override
	protected Set<String> getSystemViews() {
		return systemTablesAndViews;
	}

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "Informix";
    }

    @Override
    public Integer getDefaultPort() {
        return 1526;
    }

    @Override
    public void setConnection(final DatabaseConnection connection) {
        super.setConnection(connection);
        try {
        	/*
        	 * TODO Maybe there is a better place for this.
        	 * For each session this statement has to be executed,
        	 * to allow newlines in quoted strings
        	 */
			ExecutorService.getInstance().getExecutor(this).execute(new RawSqlStatement("EXECUTE PROCEDURE IFX_ALLOW_NEWLINE('T');"));
		} catch (Exception e) {
			throw new UnexpectedLiquibaseException("Could not allow newline characters in quoted strings with IFX_ALLOW_NEWLINE", e);
		}
    }

	@Override
    public String getDefaultDriver(final String url) {
		if (url.startsWith("jdbc:informix-sqli")) {
			return "com.informix.jdbc.IfxDriver";
		}
		return null;
	}

	@Override
    public String getShortName() {
		return "informix";
	}

	@Override
    public boolean isCorrectDatabaseImplementation(final DatabaseConnection conn)
			throws DatabaseException {
		return PRODUCT_NAME.equals(conn.getDatabaseProductName());
	}

	@Override
    public boolean supportsInitiallyDeferrableColumns() {
		return false;
	}

	/*
	 * Informix calls them Dbspaces
	 */
	@Override
    public boolean supportsTablespaces() {
		return true;
	}

	@Override
	public void checkDatabaseChangeLogTable(final boolean updateExistingNullChecksums, final DatabaseChangeLog databaseChangeLog, final Contexts contexts) throws DatabaseException {
        if (updateExistingNullChecksums && databaseChangeLog == null) {
            throw new DatabaseException("changeLog parameter is required if updating existing checksums");
        }

        Executor executor = ExecutorService.getInstance().getExecutor(this);

        Table changeLogTable = SnapshotGeneratorFactory.getInstance().getDatabaseChangeLogTable(new SnapshotControl(this, Table.class, Column.class), this);

        List<SqlStatement> statementsToExecute = new ArrayList<SqlStatement>();

        boolean changeLogCreateAttempted = false;
        if (changeLogTable != null) {
            boolean hasDescription = changeLogTable.getColumn("DESCRIPTION") != null;
            boolean hasComments = changeLogTable.getColumn("COMMENTS") != null;
            boolean hasTag = changeLogTable.getColumn("TAG") != null;
            boolean hasLiquibase = changeLogTable.getColumn("LIQUIBASE") != null;
            boolean liquibaseColumnNotRightSize = false;
            if (!getConnection().getDatabaseProductName().equals("SQLite")) {
                liquibaseColumnNotRightSize = changeLogTable.getColumn("LIQUIBASE").getType().getColumnSize() != 20;
            }
            boolean hasOrderExecuted = changeLogTable.getColumn("ORDEREXECUTED") != null;
            boolean checksumNotRightSize = false;
            boolean hasExecTypeColumn = changeLogTable.getColumn("EXECTYPE") != null;

            if (!hasDescription) {
                executor.comment("Adding missing databasechangelog.description column");
                statementsToExecute.add(
                		new AddColumnStatement(
                				getLiquibaseCatalogName(),
                				getLiquibaseSchemaName(),
                				getDatabaseChangeLogTableName(),
                				"DESCRIPTION",
                				"VARCHAR(255)",
                				null));
            }
            if (!hasTag) {
                executor.comment("Adding missing databasechangelog.tag column");
                statementsToExecute.add(
                		new AddColumnStatement(
                				getLiquibaseCatalogName(),
                				getLiquibaseSchemaName(),
                				getDatabaseChangeLogTableName(),
                				"TAG",
                				"VARCHAR(255)", null));
            }
            if (!hasComments) {
                executor.comment("Adding missing databasechangelog.comments column");
                statementsToExecute.add(
                		new AddColumnStatement(
                				getLiquibaseCatalogName(),
                				getLiquibaseSchemaName(),
                				getDatabaseChangeLogTableName(),
                				"COMMENTS",
                				"VARCHAR(255)",
                				null));
            }
            if (!hasLiquibase) {
                executor.comment("Adding missing databasechangelog.liquibase column");
                statementsToExecute.add(
                		new AddColumnStatement(
                				getLiquibaseCatalogName(),
                				getLiquibaseSchemaName(),
                				getDatabaseChangeLogTableName(),
                				"LIQUIBASE",
                				"VARCHAR(255)",
                				null));
            }
            if (!hasOrderExecuted) {
                executor.comment("Adding missing databasechangelog.orderexecuted column");
                statementsToExecute.add(
                		new AddColumnStatement(
                				getLiquibaseCatalogName(),
                				getLiquibaseSchemaName(),
                				getDatabaseChangeLogTableName(),
                				"ORDEREXECUTED",
                				"INT",
                				null));
                statementsToExecute.add(
                		new UpdateStatement(
                				getLiquibaseCatalogName(),
                				getLiquibaseSchemaName(),
                				getDatabaseChangeLogTableName())
                			.addNewColumnValue("ORDEREXECUTED", -1));
                statementsToExecute.add(
                		new SetNullableStatement(
                				getLiquibaseCatalogName(),
                				getLiquibaseSchemaName(),
                				getDatabaseChangeLogTableName(),
                				"ORDEREXECUTED",
                				"INT",
                				false));
            }
            if (checksumNotRightSize) {
                executor.comment("Modifying size of databasechangelog.md5sum column");

                statementsToExecute.add(
                		new ModifyDataTypeStatement(
                				getLiquibaseCatalogName(),
                				getLiquibaseSchemaName(),
                				getDatabaseChangeLogTableName(),
                				"MD5SUM",
                				"VARCHAR(35)"));
            }
            if (liquibaseColumnNotRightSize) {
                executor.comment("Modifying size of databasechangelog.liquibase column");

                statementsToExecute.add(new ModifyDataTypeStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "LIQUIBASE", "VARCHAR(20)"));
            }
            if (!hasExecTypeColumn) {
                executor.comment("Adding missing databasechangelog.exectype column");
                statementsToExecute.add(
                		new AddColumnStatement(
                				getLiquibaseCatalogName(),
                				getLiquibaseSchemaName(),
                				getDatabaseChangeLogTableName(),
                				"EXECTYPE",
                				"VARCHAR(10)",
                				null));
                statementsToExecute.add(
                		new UpdateStatement(
                				getLiquibaseCatalogName(),
                				getLiquibaseSchemaName(),
                				getDatabaseChangeLogTableName())
                			.addNewColumnValue("EXECTYPE", "EXECUTED"));
                statementsToExecute.add(
                		new SetNullableStatement(
                				getLiquibaseCatalogName(),
                				getLiquibaseSchemaName(),
                				getDatabaseChangeLogTableName(),
                				"EXECTYPE",
                				"VARCHAR(10)",
                				false));
            }

            List<Map<String, ?>> md5sumRS = ExecutorService.getInstance().getExecutor(this).queryForList(
            		new SelectFromDatabaseChangeLogStatement(
            				new SelectFromDatabaseChangeLogStatement.ByNotNullCheckSum(), "MD5SUM"));
            if (md5sumRS.size() > 0) {
                String md5sum = md5sumRS.get(0).get("MD5SUM").toString();
                if (!md5sum.startsWith(CheckSum.getCurrentVersion() + ":")) {
                    executor.comment("DatabaseChangeLog checksums are an incompatible version.  Setting them to null so they will be updated on next database update");
                    statementsToExecute.add(new RawSqlStatement("UPDATE " + escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName()) + " SET MD5SUM=null"));
                }
            }

        } else if (!changeLogCreateAttempted) {
            executor.comment("Create Database Change Log Table");
            SqlStatement createTableStatement = new CreateDatabaseChangeLogTableStatement();
            if (!canCreateChangeLogTable()) {
                throw new DatabaseException("Cannot create " + escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName()) + " table for your database.\n\n" +
                        "Please construct it manually using the following SQL as a base and re-run Liquibase:\n\n" +
                        createTableStatement);
            }
            // If there is no table in the database for recording change history create one.
            statementsToExecute.add(createTableStatement);
            LogFactory.getLogger().info("Creating database history table with name: " + escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName()));
//                }
        }

        for (SqlStatement sql : statementsToExecute) {
            executor.execute(sql);
            this.commit();
        }

        if (updateExistingNullChecksums) {
            for (RanChangeSet ranChangeSet : this.getRanChangeSetList()) {
                if (ranChangeSet.getLastCheckSum() == null) {
                    ChangeSet changeSet = databaseChangeLog.getChangeSet(ranChangeSet);
                    if (changeSet != null && new ContextChangeSetFilter(contexts).accepts(changeSet) && new DbmsChangeSetFilter(this).accepts(changeSet)) {
                        LogFactory.getLogger().info("Updating null or out of date checksum on changeSet " + changeSet + " to correct value");
                        executor.execute(new UpdateChangeSetChecksumStatement(changeSet));
                    }
                }
            }
            commit();
            resetRanChangeSetList();
        }
    }


	@Override
	public String getViewDefinition(CatalogAndSchema schema, final String viewName) throws DatabaseException {
        schema = correctSchema(schema);
		List<Map<String, ?>> retList = ExecutorService.getInstance().getExecutor(this).queryForList(new GetViewDefinitionStatement(schema.getCatalogName(), schema.getSchemaName(), viewName));
		// building the view definition from the multiple rows
		StringBuilder sb = new StringBuilder();
		for (Map rowMap : retList) {
			String s = (String) rowMap.get("VIEWTEXT");
			sb.append(s);
		}
		return CREATE_VIEW_AS_PATTERN.matcher(sb.toString()).replaceFirst("");
	}

	@Override
	public String getAutoIncrementClause(final BigInteger startWith, final BigInteger incrementBy) {
		return "";
	}


	@Override
    public String getDateLiteral(final String isoDate) {
        if (isTimeOnly(isoDate)) {
            return "INTERVAL (" + super.getDateLiteral(isoDate).replaceAll("'", "") + ") " + INTERVAL_FIELD_QUALIFIER;
        } else if (isDateOnly(isoDate)){
        	return super.getDateLiteral(isoDate);
        } else {
            return "DATETIME (" + super.getDateLiteral(isoDate).replaceAll("'", "") + ") " + DATETIME_FIELD_QUALIFIER;
        }
    }

	@Override
	public boolean supportsRestrictForeignKeys() {
		// TODO dont know if this correct
		return false;
	}


    @Override
    public String escapeObjectName(final String catalogName, final String schemaName, final String objectName, final Class<? extends DatabaseObject> objectType) {
        String name = super.escapeObjectName(catalogName, schemaName, objectName, objectType);
        if (name == null) {
            return null;
        }
        if (name.matches(".*\\..*\\..*")) {
            name = name.replaceFirst("\\.", ":"); //informix uses : to separate catalog and schema. Like "catalog:schema.table"
        }
        return name;
    }

    @Override
	public String getSystemSchema(){
    	return "informix";
    }
}
