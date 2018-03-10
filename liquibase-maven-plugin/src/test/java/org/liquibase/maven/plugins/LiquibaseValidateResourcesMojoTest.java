package org.liquibase.maven.plugins;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;

public class LiquibaseValidateResourcesMojoTest extends AbstractLiquibaseMojoTest {

    public void testSucceed() throws Exception {
        File pom = getTestFile("src/test/resources/validateResources/success/plugin_config.xml");

        if (!pom.exists()) {
            fail();
        }
        LiquibaseValidateResourcesMojo update = (LiquibaseValidateResourcesMojo) lookupMojo("validateResources", pom);
        update.execute();
    }

    public void testColumnValueLobFail() throws Exception {
        defaultFailTest(
                "src/test/resources/validateResources/fail/columnValueLob/plugin_config.xml",
                "clob/valClob.txt", "clob/valBlob.txt");
    }

    public void testIncludedFail() throws Exception {
        defaultFailTest(
                "src/test/resources/validateResources/fail/included/plugin_config.xml",
                "testIncluded.xml");
    }

    public void testLoadDataFail() throws Exception {
        defaultFailTest(
                "src/test/resources/validateResources/fail/loadData/plugin_config.xml",
                "testLoadDataFile.txt");
    }

    public void testSqlFileFail() throws Exception {
        defaultFailTest(
                "src/test/resources/validateResources/fail/sqlFile/plugin_config.xml",
                "sqlFiles/testSqlNotRelative.sql", "testSqlRelative.sql");
    }

    private <T extends Exception> void defaultFailTest(String filePath, String... messageSubStrings) throws Exception {
        try {
            File pom = getTestFile(filePath);

            if (!pom.exists()) {
                fail();
            }
            LiquibaseValidateResourcesMojo update = (LiquibaseValidateResourcesMojo) lookupMojo("validateResources", pom);
            update.execute();
        } catch (MojoExecutionException e) {
            testExceptionMessage(e, messageSubStrings);
            return;
        }
        fail();
    }

    private void testExceptionMessage(Exception e, String... messageSubStrings) {
        if (messageSubStrings != null && messageSubStrings.length > 0) {
            for (String messageSubString : messageSubStrings) {
                assertTrue(e.getMessage().contains(messageSubString));
            }
        }
    }

}