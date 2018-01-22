package liquibase.verify.change;

import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeParameterMetaData;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.Db2zDatabase;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.core.string.StringChangeLogSerializer;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.test.JUnitResourceAccessor;
import liquibase.util.StringUtils;
import liquibase.verify.AbstractVerifyTest;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VerifyChangeClassesTest extends AbstractVerifyTest {

    @Test
    public void compareGeneratedSqlWithExpectedSqlForMinimalChangesets() throws Exception {
        ChangeFactory changeFactory = ChangeFactory.getInstance();
        for (String changeName : changeFactory.getDefinedChanges()) {
            if ("addDefaultValue".equals(changeName)) {
                continue; //need to better handle strange "one of defaultValue* is required" logic
            }
            if ("changeWithNestedTags".equals(changeName) || "sampleChange".equals(changeName)
                || "output".equals(changeName) || "tagDatabase".equals(changeName)){
                continue; //not a real change
            }
            for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
                if (database.getShortName() == null) {
                    continue;
                }

                TestState state = new TestState(name.getMethodName(), changeName, database.getShortName(), TestState.Type.SQL);
                state.addComment("Database: " + database.getShortName());

                Change change = changeFactory.create(changeName);
                if (!change.supports(database)) {
                    continue;
                }
                if (change.generateStatementsVolatile(database)) {
                    continue;
                }
                ChangeMetaData changeMetaData = ChangeFactory.getInstance().getChangeMetaData(change);

                change.setResourceAccessor(new JUnitResourceAccessor());

                // Prepare a list of required parameters, plus a few extra for complicated cases (e.g. where at least
                // one of two parameters in a group is required.
                TreeSet<String> requiredParams = new TreeSet<String>(changeMetaData.getRequiredParameters(database).keySet());
                /* dropColumn allows either one column or a list of columns; we choose to test with a single column. */
                if ("dropColumn".equalsIgnoreCase(changeName)) requiredParams.add("columnName");
                /* When testing table and column remarks, do not test with an empty remark. */
                if ("setColumnRemarks".equalsIgnoreCase(changeName)) requiredParams.add("remarks");
                if ("setTableRemarks".equalsIgnoreCase(changeName)) requiredParams.add("remarks");
                if ("createView".equalsIgnoreCase(changeName)) requiredParams.add("selectQuery");
                /* ALTER SEQUENCE should change at least one property
                 * hsqldb/h2 do not support incrementBy. */
                if ("alterSequence".equalsIgnoreCase(changeName)) {
                    if ("h2".equalsIgnoreCase(database.getShortName())) {
                        requiredParams.add("ordered");
                    } else if ("hsqldb".equalsIgnoreCase(database.getShortName())) {
                        requiredParams.add("minValue");
                    } else {
                        requiredParams.add("incrementBy");
                    }
                }

                if (database instanceof Db2zDatabase && "addLookupTable".equalsIgnoreCase(changeName)) {
                    requiredParams.add("newColumnDataType");
                }


                // For every required parameter of the change, fetch an example value.
                for (String paramName : requiredParams) {
                    ChangeParameterMetaData param = changeMetaData.getParameters().get(paramName);
                    Object paramValue = param.getExampleValue(database);
                    String serializedValue;
                    serializedValue = formatParameter(paramValue);
                    state.addComment("Change Parameter: " + param.getParameterName() + "=" + serializedValue);
                    param.setValue(change, paramValue);
                }

                ValidationErrors errors = change.validate(database);
                assertFalse("Validation errors for " + changeMetaData.getName() + " on "
                        + database.getShortName() + ": " + errors.toString(), errors.hasErrors());

                SqlStatement[] sqlStatements = {};
                try {
                    sqlStatements = change.generateStatements(database);
                } catch (UnexpectedLiquibaseException ex) {
                    if (ex.getCause() instanceof IOException) {
                        // Do nothing. I/O exceptions at this point come from the example value "my/path/file.sql"
                        // for the <sql> type change and can be safely ignored.
                    }
                }
                for (SqlStatement statement : sqlStatements) {
                    Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, database);
                    if (sql == null) {
                        LogService.getLog(getClass()).severe(LogType.LOG, "Null sql for " + statement + " on " + database.getShortName());
                    } else {
                        for (Sql line : sql) {
                            String sqlLine = line.toSql();
                            assertFalse("Change "+changeMetaData.getName()+" contains 'null' for "+database.getShortName()+": "+sqlLine, sqlLine.contains(" null "));

                            state.addValue(sqlLine + ";");
                        }
                    }
                }
                state.test();
            }
        }
    }

    /**
     * For every combination of database (e.g. Oracle) and change (e.g. DropForeignKeyConstraint),
     * construct a change object with some example values. For every required parameter, verify that
     * removing it causes the change object to change its state to "hasErrors".
     * @throws Exception
     */
    @Test
    public void lessThanMinimumFails() throws Exception {
        ChangeFactory changeFactory = ChangeFactory.getInstance();
        for (String changeName : changeFactory.getDefinedChanges()) {
            for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
                if (database.getShortName() == null) {
                    continue;
                }

                Change change = changeFactory.create(changeName);
                if (!change.supports(database)) {
                    continue;
                }
                if (change.generateStatementsVolatile(database)) {
                    continue;
                }
                ChangeMetaData changeMetaData = ChangeFactory.getInstance().getChangeMetaData(change);

                change.setResourceAccessor(new JUnitResourceAccessor());

                ArrayList<String> requiredParams = new ArrayList<String>(changeMetaData.getRequiredParameters(database).keySet());
                for (String paramName : requiredParams) {
                    ChangeParameterMetaData param = changeMetaData.getParameters().get(paramName);
                    Object paramValue = param.getExampleValue(database);
                    param.setValue(change, paramValue);
                }

                for (int i = 0; i < requiredParams.size(); i++) {
                    String paramToRemove = requiredParams.get(i);
                    ChangeParameterMetaData paramToRemoveMetadata = changeMetaData.getParameters().get(paramToRemove);
                    Object currentValue = paramToRemoveMetadata.getCurrentValue(change);
                    paramToRemoveMetadata.setValue(change, null);

                    assertTrue("No errors even with "+changeMetaData.getName()+" with a null "+paramToRemove+" on "+database.getShortName(), change.validate(database).hasErrors());
                    paramToRemoveMetadata.setValue(change, currentValue);
                }
            }
        }
    }

    @Ignore
    @Test
    public void extraParamsIsValidSql() throws Exception {
        ChangeFactory changeFactory = ChangeFactory.getInstance();
        for (String changeName : changeFactory.getDefinedChanges()) {
            if ("addDefaultValue".equals(changeName)) {
                continue; //need to better handle strange "one of defaultValue* is required" logic
            }

            if ("createProcedure".equals(changeName)) {
                continue; //need to better handle strange "one of path or body is required" logic
            }

            for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
                if (database.getShortName() == null) {
                    continue;
                }

                TestState state = new TestState(name.getMethodName(), changeName, database.getShortName(), TestState.Type.SQL);
                state.addComment("Database: " + database.getShortName());

                Change baseChange = changeFactory.create(changeName);
                if (!baseChange.supports(database)) {
                    continue;
                }
                if (baseChange.generateStatementsVolatile(database)) {
                    continue;
                }
                ChangeMetaData changeMetaData = ChangeFactory.getInstance().getChangeMetaData(baseChange);
                ArrayList<String> optionalParameters = new ArrayList<String>(changeMetaData.getOptionalParameters(database).keySet());
                Collections.sort(optionalParameters);

                List<List<String>> paramLists = powerSet(optionalParameters);
                Collections.sort(paramLists, new Comparator<List<String>>() {
                    @Override
                    public int compare(List<String> o1, List<String> o2) {
                        int comp = Integer.valueOf(o1.size()).compareTo(o2.size());
                        if (comp == 0) {
                            comp =  StringUtils.join(o1,",").compareTo(StringUtils.join(o2, ","));
                        }
                        return comp;
                    }
                });
                for (List<String> permutation : paramLists) {
                    Change change = changeFactory.create(changeName);
                    change.setResourceAccessor(new JUnitResourceAccessor());
//
                    for (String paramName : new TreeSet<String>(changeMetaData.getRequiredParameters(database).keySet())) {
                        ChangeParameterMetaData param = changeMetaData.getParameters().get(paramName);
                        Object paramValue = param.getExampleValue(database);
                        String serializedValue;
                        serializedValue = formatParameter(paramValue);
                        state.addComment("Required Change Parameter: "+ param.getParameterName()+"="+ serializedValue);
                        param.setValue(change, paramValue);
                    }

                    for (String paramName : permutation) {
                        ChangeParameterMetaData param = changeMetaData.getParameters().get(paramName);
                        if (!param.supports(database)) {
                            continue;
                        }
                        Object paramValue = param.getExampleValue(database);
                        String serializedValue;
                        serializedValue = formatParameter(paramValue);
                        state.addComment("Optional Change Parameter: "+ param.getParameterName()+"="+ serializedValue);
                        param.setValue(change, paramValue);

                    }

                    ValidationErrors errors = change.validate(database);
                    assertFalse("Validation errors for " + changeMetaData.getName() + " on "+database.getShortName()+": " +errors.toString(), errors.hasErrors());
//
//                    SqlStatement[] sqlStatements = change.generateStatements(database);
//                    for (SqlStatement statement : sqlStatements) {
//                        Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, database);
//                        if (sql == null) {
//                            System.out.println("Null sql for "+statement+" on "+database.getShortName());
//                        } else {
//                            for (Sql line : sql) {
//                                state.addValue(line.toSql()+";");
//                            }
//                        }
//                    }
//                    state.test();
                }
            }
        }
    }

    private List<List<String>> powerSet(List<String> baseSet) {
        List<List<String>> returnList = new LinkedList<List<String>>();

        if (baseSet.isEmpty()) {
            returnList.add(new ArrayList<String>());
            return returnList;
        }
        List<String> list = new ArrayList<String>(baseSet);
        String head = list.get(0);
        List<String> rest = new ArrayList<String>(list.subList(1, list.size()));
        for (List<String> set : powerSet(rest)) {
            List<String> newSet = new ArrayList<String>();
            newSet.add(head);
            newSet.addAll(set);
            returnList.add(newSet);
            returnList.add(set);
        }
        return returnList;


    }

    private String formatParameter(Object paramValue) {
        String serializedValue;
        if (paramValue instanceof Collection) {
            serializedValue = "[";
            for (Object obj : (Collection) paramValue) {
                serializedValue += formatParameter(obj) + ", ";
            }
            serializedValue += "]";
        } else if (paramValue instanceof LiquibaseSerializable) {
            serializedValue = new StringChangeLogSerializer().serialize(((LiquibaseSerializable) paramValue), true);
        } else {
            serializedValue = paramValue.toString();
        }
        return serializedValue;
    }

//    @Test
//    public void volitileIsCorrect() {
//
//    }

}
