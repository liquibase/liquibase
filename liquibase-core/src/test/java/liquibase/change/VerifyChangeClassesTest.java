package liquibase.change;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.UnsupportedChangeException;
import liquibase.resource.ResourceAccessor;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.test.JUnitResourceAccessor;
import liquibase.util.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeSet;

public class VerifyChangeClassesTest {

    @Test
    public void test_minimumRequired() throws Exception {
        ChangeFactory changeFactory = ChangeFactory.getInstance();
        for (String changeName : changeFactory.getDefinedChanges()) {
            for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
                Change change = changeFactory.create(changeName);
                if (!change.supports(database)) {
                    continue;
                }
                if (change.generateStatementsVolatile(database)) {
                    System.out.println("Cannot test "+changeName+" on "+database);
                    continue;
                }
                ChangeMetaData changeMetaData = change.getChangeMetaData();
                System.out.println("Change: "+changeName);
                System.out.println("Database: "+database.getShortName());
                System.out.println("Parameters: "+ StringUtils.join(new TreeSet<String>(changeMetaData.getRequiredParameters(database).keySet()), ","));

                change.setResourceAccessor(new JUnitResourceAccessor());

                for (ChangeParameterMetaData param : changeMetaData.getRequiredParameters(database).values()) {
                    param.setValue(change, param.getExampleValue());
                }

                SqlStatement[] sqlStatements = change.generateStatements(database);
                for (SqlStatement statement : sqlStatements) {
                    Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, database);
                    if (sql == null) {
                        System.out.println("Null sql for "+statement);
                    } else {
                        for (Sql line : sql) {
                            System.out.println(line.toSql());
                        }
                    }
                }
            }
        }
    }


    @Test
    public void volitileIsCorrect() {

    }

    @Test
    public void unsupportedIsCorrect() throws UnsupportedChangeException {
        ChangeFactory changeFactory = ChangeFactory.getInstance();
        for (String changeName : changeFactory.getDefinedChanges()) {
            for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
                Change change = changeFactory.create(changeName);
                if (change.supports(database)) {
                    continue;
                }
                ChangeMetaData changeMetaData = change.getChangeMetaData();
                System.out.println("Change: "+changeName);
                System.out.println("Database: "+database.getShortName());
                System.out.println("Parameters: "+ StringUtils.join(new TreeSet<String>(changeMetaData.getRequiredParameters(database).keySet()), ","));
                for (ChangeParameterMetaData param : changeMetaData.getRequiredParameters(database).values()) {
                    param.setValue(change, param.getExampleValue());
                }

                SqlStatement[] sqlStatements = change.generateStatements(database);
                for (SqlStatement statement : sqlStatements) {
                    Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, database);
                    for (Sql line : sql) {
                        System.out.println(line.toSql());
                    }
                }
            }
        }
    }

}
