package liquibase.extension.testing.setup

import liquibase.util.FileUtil

class SetupModifyTextFile extends TestSetup {

    private final File textFile
    private final String originalString
    private final String newString

    SetupModifyTextFile(File textFile, String originalString, String newString) {
        this.textFile = textFile
        this.originalString = originalString
        this.newString = newString
    }

    @Override
    void setup(TestSetupEnvironment testSetupEnvironment) throws Exception {
        String contents = FileUtil.getContents(textFile)
        contents = contents.replaceAll(originalString, newString)
        FileUtil.write(contents, textFile)
    }
}
