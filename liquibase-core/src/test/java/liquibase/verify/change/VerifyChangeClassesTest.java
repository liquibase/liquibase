package liquibase.verify.change;

import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeParameterMetaData;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.core.string.StringChangeLogSerializer;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.test.JUnitResourceAccessor;
import liquibase.util.StringUtils;
import liquibase.verify.AbstractVerifyTest;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class VerifyChangeClassesTest extends AbstractVerifyTest {

    @Test
    public void minimumRequiredIsValidSql() throws Exception {
        ChangeFactory changeFactory = ChangeFactory.getInstance();
        for (String changeName : changeFactory.getDefinedChanges()) {
            for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
                if (database.getShortName() == null) {
                    continue;
                }

                TestState state = new TestState(name.getMethodName(), changeName, database.getShortName(), TestState.Type.SQL);
                Change change = changeFactory.create(changeName);
                if (!change.supports(database)) {
                    continue;
                }
                if (change.generateStatementsVolatile(database)) {
                    continue;
                }
                ChangeMetaData changeMetaData = change.getChangeMetaData();

                change.setResourceAccessor(new JUnitResourceAccessor());

                for (String paramName : new TreeSet<String>(changeMetaData.getRequiredParameters(database).keySet())) {
                    ChangeParameterMetaData param = changeMetaData.getParameters().get(paramName);
                    Object paramValue = param.getExampleValue();
                    String serializedValue;
                    serializedValue = formatParameter(paramValue);
                    state.addComment("Database: "+database.getShortName());
                    state.addComment("Change Parameter: "+ param.getParameterName()+"="+ serializedValue);
                    param.setValue(change, paramValue);
                }

                ValidationErrors errors = change.validate(database);
                assertFalse("Validation errors for " + changeMetaData.getName() + " on "+database.getShortName()+": " +errors.toString(), errors.hasErrors());

                SqlStatement[] sqlStatements = change.generateStatements(database);
                for (SqlStatement statement : sqlStatements) {
                    Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, database);
                    if (sql == null) {
                        System.out.println("Null sql for "+statement+" on "+database.getShortName());
                    } else {
                        for (Sql line : sql) {
                            state.addValue(line.toSql()+";");
                        }
                    }
                }
                state.test();
            }
        }
    }

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
                ChangeMetaData changeMetaData = change.getChangeMetaData();

                change.setResourceAccessor(new JUnitResourceAccessor());

                ArrayList<String> requiredParams = new ArrayList<String>(changeMetaData.getRequiredParameters(database).keySet());
                for (String paramName : requiredParams) {
                    ChangeParameterMetaData param = changeMetaData.getParameters().get(paramName);
                    Object paramValue = param.getExampleValue();
                    param.setValue(change, paramValue);
                }

                for (int i=0; i<requiredParams.size(); i++) {
                    String paramToRemove = requiredParams.get(i);
                    ChangeParameterMetaData paramToRemoveMetadata = changeMetaData.getParameters().get(paramToRemove);
                    Object currentValue = paramToRemoveMetadata.getCurrentValue(change);
                    paramToRemoveMetadata.setValue(change, null);

                    assertTrue(change.validate(database).hasErrors());
                    paramToRemoveMetadata.setValue(change, currentValue);
                }
          }
        }
    }

    private String formatParameter(Object paramValue) {
        String serializedValue;
        if (paramValue instanceof Collection) {
            serializedValue = "[";
            for (Object obj : (Collection) paramValue) {
                serializedValue += formatParameter(obj)+", ";
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
