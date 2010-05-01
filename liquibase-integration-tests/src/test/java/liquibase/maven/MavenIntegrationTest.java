package liquibase.maven;

import java.io.File;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.test.DatabaseTestContext;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 *
 * @author lujop
 */
public class MavenIntegrationTest {
    //TODO: Not hardcoded here
    private static final String URL="jdbc:derby:liquibase;create=true";


    @Before
    public void cleanDatabase() throws Exception {
         DatabaseConnection connection = DatabaseTestContext.getInstance().getConnection(URL);
         assertNotNull(connection);
         Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
         database.dropDatabaseObjects("liquibase");
         database.close();
         DatabaseFactory.reset();
    }

    @Test
    public void testUpdate() throws Exception{
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/maven" );

        Verifier verifier;

        //Clear any artifact created by the test project to avoid unstable test results
        verifier = new Verifier( testDir.getAbsolutePath() );
        //TODO: Remove hardcoded version
        verifier.deleteArtifact( "org.liquibase", "liquibase-maven-integration-tests", "1.0-SNAPSHOT", "jar" );


        //If needed options:
        //List cliOptions = new ArrayList();
        //cliOptions.add( "-N" );
        //verifier.setCliOptions(cliOptions);

        verifier.executeGoal( "clean" );
        verifier.executeGoal( "install" );

        //Verify everithing has gone well.
        verifier.verifyErrorFreeLog();

        //Reset the streams before executing the verifier
        verifier.resetStreams();
    }
}
