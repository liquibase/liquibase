package liquibase.sdk.verifytest;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerifiedTestReader {

    private enum Section {
        DEFINITION,
        NOTES,
        DATA
    }

    public VerifiedTest read(Reader reader) throws IOException {
        VerifiedTest results = null;
        BufferedReader bufferedReader = new BufferedReader(reader);


        Pattern permutationStartPattern = Pattern.compile("## Permutation: (.*) ##");
        Pattern internalKeyValuePattern = Pattern.compile("\\- _(.+):_ (.+)");
        Pattern keyValuePattern = Pattern.compile("\\- \\*\\*(.+):\\*\\* (.*)");
        Pattern multiLineKeyValuePattern = Pattern.compile("\\- \\*\\*(.+) =>\\*\\*");

        TestPermutation currentPermutation = null;

        String line;
        int lineNumber = 0;
        Section section = null;
        String multiLineKey = null;
        String multiLineValue = null;
        while ((line = bufferedReader.readLine()) != null) {
            lineNumber++;

            if (lineNumber == 1) {
                Matcher matcher = Pattern.compile("# Test: (\\S*) \"(.*)\" #").matcher(line);
                if (!matcher.matches()) {
                    throw new IOException("Invalid header: "+line);
                } else {
                    String testClass = matcher.group(1);
                    String testName = matcher.group(2);

                    results = new VerifiedTest(testClass, testName);
                }

                continue;
            }

            if (multiLineKey != null) {
                if (line.equals("") || line.startsWith("    ")) {
                    multiLineValue += line.replaceFirst("    ", "")+"\n";
                    continue;
                } else {
                    multiLineValue = multiLineValue.trim();
                    if (section.equals(Section.DEFINITION)) {
                        currentPermutation.describe(multiLineKey, multiLineValue, OutputFormat.FromFile);
                    } else if (section.equals(Section.NOTES)) {
                        currentPermutation.note(multiLineKey, multiLineValue, OutputFormat.FromFile);
                    } else if (section.equals(Section.DATA)) {
                        currentPermutation.data(multiLineKey, multiLineValue, OutputFormat.FromFile);
                    } else {
                        throw new UnexpectedLiquibaseException("Unknown multiline section on line "+lineNumber+": "+section);
                    }
                    multiLineKey = null;
                    multiLineValue = null;
                }
            }

            if (StringUtils.trimToEmpty(line).equals("")) {
                continue;
            }

            if (line.equals("#### Notes ####")) {
                section = Section.NOTES;
                continue;
            } else if (line.equals("#### Data ####")) {
                section = Section.DATA;
                continue;
            }

            Matcher permutationStartMatcher = permutationStartPattern.matcher(line);
            if (permutationStartMatcher.matches()) {
                currentPermutation = new TestPermutation(results);
                section = Section.DEFINITION;
                continue;
            }

            Matcher internalKeyValueMatcher = internalKeyValuePattern.matcher(line);
            if (internalKeyValueMatcher.matches()) {
                String key = internalKeyValueMatcher.group(1);
                String value = internalKeyValueMatcher.group(2);
                if (key.equals("VERIFIED")) {
                    String[] splitValue = value.split("\\s+", 2);
                    currentPermutation.setVerified(Boolean.valueOf(splitValue[0]));
                    if (splitValue.length > 1) {
                        currentPermutation.setNotVerifiedMessage(splitValue[1]);
                    }
                } else {
                    throw new UnexpectedLiquibaseException("Unknown internal parameter "+ key);
                }
                continue;
            }

            Matcher keyValueMatcher = keyValuePattern.matcher(line);
            if (keyValueMatcher.matches()) {
                String key = keyValueMatcher.group(1);
                String value = keyValueMatcher.group(2);

                if (section.equals(Section.DEFINITION)) {
                    currentPermutation.describe(key, value, OutputFormat.FromFile);
                } else if (section.equals(Section.NOTES)) {
                    currentPermutation.note(key, value, OutputFormat.FromFile);
                } else if (section.equals(Section.DATA)) {
                    currentPermutation.data(key, value, OutputFormat.FromFile);
                } else {
                    throw new UnexpectedLiquibaseException("Unknown section "+section);
                }
                continue;
            }

            Matcher multiLineKeyValueMatcher = multiLineKeyValuePattern.matcher(line);
            if (multiLineKeyValueMatcher.matches()) {
                multiLineKey = multiLineKeyValueMatcher.group(1);
                multiLineValue = "";
                continue;
            }

            if (currentPermutation == null) {
                //in the header section describing what the file is for
            } else {
                throw new UnexpectedLiquibaseException("Could not parse line "+lineNumber+": "+line);
            }
        }

        return results;
    }
}
