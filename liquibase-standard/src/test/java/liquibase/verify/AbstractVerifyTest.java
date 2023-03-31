package liquibase.verify;

import liquibase.util.StringUtil;
import org.junit.ComparisonFailure;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.*;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

public class AbstractVerifyTest {

    @Rule
    public TestName name = new TestName();

    protected static class TestState {
        private File stateFile;
        private File savedStateDir;
        private String testName;
        private String testGroup;
        private String stateName;
        private Type type;

        private StringBuilder stateContent = new StringBuilder();

        public TestState(String testName, String testGroup, String stateName, Type type) {
            this.type = type;
            this.testName = testName;
            this.testGroup = testGroup;
            this.stateName = stateName;

            File liquibaseRootDir = new File("");
            if (liquibaseRootDir.getAbsolutePath().endsWith("liquibase-core")) { //sometimes running in liquibase-core, especially in maven
                liquibaseRootDir = liquibaseRootDir.getAbsoluteFile().getParentFile();
            }

            this.savedStateDir = new File(liquibaseRootDir.getAbsoluteFile(), "liquibase-core/src/test/java/liquibase/verify/saved_state/"+testName+"/"+testGroup);
            this.stateFile = new File(savedStateDir, stateName+"."+type.name().toLowerCase());

        }

        public void addComment(String comment) {
            stateContent.append(Pattern.compile("^", Pattern.MULTILINE).matcher(comment).replaceAll("-- ")).append("\n");
        }

        public void addValue(String value) {
            stateContent.append(value).append("\n");
        }

        public void save() throws Exception{
            savedStateDir.mkdirs();

            BufferedWriter outputStream = new BufferedWriter(new FileWriter(stateFile));
            outputStream.write(stateContent.toString());
            outputStream.flush();
            outputStream.close();
        }

        public void test() throws Exception {
            String existingContent = readExistingValue();
            if ("".equals(existingContent) && (StringUtil.trimToNull(stateContent.toString()) != null)) {
                save();
            } else {
                try {
                    String stateContentString = stateContent.toString();
                    /* For the purpose of comparison, normalise all line endings to UNIX style (\n)
                     * instead of Windows (\r\n) */
                    stateContentString = stateContentString.replaceAll("\r\n", "\n").trim();
                    existingContent = existingContent.replaceAll("\r\n", "\n").trim();
                    boolean contentsAreEqual = stateContentString.equals(existingContent);
                    assertTrue(String.format("Unexpected difference in %s\nOriginal:\n[%s]\nNew state:\n[%s]\n",
                        stateFile.getAbsolutePath(),
                        existingContent,
                        stateContentString),
                        contentsAreEqual);
                } catch (ComparisonFailure e) {
                    if ("overwrite".equals(System.getProperty("liquibase.verify.mode"))) {
                        save();
                    } else {
                        throw e;
                    }
                }
            }
        }

        private String readExistingValue() throws IOException {
            StringBuilder content = new StringBuilder();
            if (stateFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(stateFile));

                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                reader.close();
            }

            return content.toString();
        }

        public enum Type {
            SQL
        }
    }
}
