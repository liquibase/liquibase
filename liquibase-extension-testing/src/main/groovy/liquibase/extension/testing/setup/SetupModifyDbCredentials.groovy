package liquibase.extension.testing.setup

import liquibase.util.FileUtil

/**
 *
 * This class allows modification of a text file to
 * replace tokens with the actual database credential
 * as specified in the environment
 *
 */
class SetupModifyDbCredentials extends TestSetup {

    private static final String URL = "_URL_"
    private static final String USERNAME = "_USERNAME_"
    private static final String PASSWORD = "_PASSWORD_"
    private final File textFile

    SetupModifyDbCredentials(File textFile) {
        this.textFile = textFile
    }

    @Override
    void setup(TestSetupEnvironment testSetupEnvironment) throws Exception {
        setup(testSetupEnvironment.url, testSetupEnvironment.username, testSetupEnvironment.password)
    }

    void setup(String url, String username, String password) {
        String contents = FileUtil.getContents(textFile)
        contents = contents.replaceAll(URL, url)
        contents = contents.replaceAll(USERNAME, username)
        contents = contents.replaceAll(PASSWORD, password)
        FileUtil.write(contents, textFile)
    }
}