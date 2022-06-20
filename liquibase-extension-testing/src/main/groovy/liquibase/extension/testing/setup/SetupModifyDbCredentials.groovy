package liquibase.extension.testing.setup

import liquibase.util.FileUtil

class SetupModifyDbCredentials extends TestSetup {

    private final File textFile
    private String originalString
    private String newString

    SetupModifyDbCredentials(File textFile, String originalString, String newString) {
        this.textFile = textFile
        this.originalString = originalString
        this.newString = newString
    }

    @Override
    void setup(TestSetupEnvironment testSetupEnvironment) throws Exception {
        if (this.originalString == "_URL_") {
            this.newString = testSetupEnvironment.url
        } else if (this.originalString == "_USERNAME_") {
            this.newString = testSetupEnvironment.username
        } else if (this.originalString == "_PASSWORD_") {
            this.newString = testSetupEnvironment.password
        }
        String contents = FileUtil.getContents(textFile)
        contents = contents.replaceAll(originalString, newString)
        FileUtil.write(contents, textFile)
    }
}
