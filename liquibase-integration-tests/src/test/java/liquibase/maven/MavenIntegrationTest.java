package liquibase.maven;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Maven integration test. Run an update executing maven as if it was ran by the user
 * @author lujop
 */
public class MavenIntegrationTest {
    private static final String URL="jdbc:hsqldb:file:target/test-classes/maven/liquibase;shutdown=true";

    @Test
    public void nothing() {
        //tests fail when not running a maven based build. need to figure out how to determine that
    }

//    @Before
//    public void cleanDatabase() throws Exception {
//         DatabaseConnection connection = DatabaseTestContext.getInstance().getConnection(URL);
//         assertNotNull(connection);
//         Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
//         database.dropDatabaseObjects(CatalogAndSchema.DEFAULT);
//         database.close();
//         DatabaseFactory.reset();
//    }
//
//    @Test
//    public void testUpdate() throws Exception{
//        Verifier verifier=createVerifier();
//
//        verifier.executeGoal( "clean" );
//        verifier.executeGoal( "install" );
//
//        //Verify everithing has gone well.
//        verifier.verifyErrorFreeLog();
//
//        //Reset the streams before executing the verifier
//        verifier.resetStreams();
//    }
//
//    @Test
//    public void testRollbackTag() throws Exception {
//        Verifier verifier= createVerifier();
//
//
//        verifier.executeGoal("clean");
//        verifier.executeGoal("liquibase:tag");
//        verifier.executeGoal("package"); //runs update that is bound to test phase
//        verifier.executeGoal("liquibase:rollback");
//        //If we can reupdate rollback has succeded
//        verifier.executeGoal("liquibase:update");
//
//        //Verify everithing has gone well.
//        verifier.verifyErrorFreeLog();
//
//        //Reset the streams before executing the verifier
//        verifier.resetStreams();
//    }

   private Verifier createVerifier() throws IOException, VerificationException {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/maven" );

        //Clear any artifact created by the test project to avoid unstable test results
        Verifier verifier = new Verifier(testDir.getAbsolutePath());
        verifier.setAutoclean(false); //Don't do clean automatically in each executeGoal
       verifier.deleteArtifact("org.liquibase", "liquibase-maven-integration-tests", "1.0-SNAPSHOT", "jar");
        return verifier;
    }
    
}
