package liquibase.sdk.verifytest;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sdk.exception.UnexpectedLiquibaseSdkException;
import liquibase.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerifiedTestReader {

    private enum Section {
        DEFINITION,
        NOTES,
        DATA
    }

    public List<TestPermutation> read(Reader... readers) throws IOException {
        List<TestPermutation> results = new ArrayList<TestPermutation>();

        String testClass = null;
        String testName = null;

        Pattern headerPattern = Pattern.compile("# Test: (\\S*) \"(.*)\" #");
        Pattern permutationStartPattern = Pattern.compile("## Permutation: (.*) ##");
        Pattern permutationsStartPattern = Pattern.compile("## Permutations ##");
        Pattern internalKeyValuePattern = Pattern.compile("\\- _(.+):_ (.+)");
        Pattern keyValuePattern = Pattern.compile("\\- \\*\\*(.+):\\*\\* (.*)");
        Pattern multiLineKeyValuePattern = Pattern.compile("\\- \\*\\*(.+) =>\\*\\*");
        Pattern dataDetailsMatcher = Pattern.compile("\\*\\*(.*?)\\*\\*: (.*)");
        Pattern notesDetailsMatcher = Pattern.compile("__(.*?)__: (.*)");

        Set<String> tableColumns = new HashSet<String>();
        List<String> thisTableColumns = null;
        for (Reader reader : readers) {
            BufferedReader bufferedReader = new BufferedReader(reader);

            CurrentDetails currentDetails = null;
            Map<String, Object> commonDetails = new HashMap<String, Object>();

            String line;
            int lineNumber = 0;
            Section section = null;
            String multiLineKey = null;
            String multiLineValue = null;
            boolean firstTableRow = true;

            while ((line = bufferedReader.readLine()) != null) {
                lineNumber++;

                if (lineNumber == 1) {
                    Matcher headerMatcher = headerPattern.matcher(line);
                    if (headerMatcher.matches()) {
                        if (results == null) {
                            testClass = headerMatcher.group(1);
                            testName = headerMatcher.group(2);
                        }
                    } else {
                        throw new IOException("Invalid header: " + line);
                    }

                    continue;
                }

                if (multiLineKey != null) {
                    if (line.equals("") || line.startsWith("    ")) {
                        multiLineValue += line.replaceFirst("    ", "") + "\n";
                        continue;
                    } else {
                        multiLineValue = multiLineValue.trim();
                        if (section.equals(Section.DEFINITION)) {
                            currentDetails.definition.put(multiLineKey, new TestPermutation.Value(multiLineValue, OutputFormat.FromFile));
                            commonDetails.put(multiLineKey, new TestPermutation.Value(multiLineValue, OutputFormat.FromFile));
                        } else if (section.equals(Section.NOTES)) {
                            currentDetails.notes.put(multiLineKey, new TestPermutation.Value(multiLineValue, OutputFormat.FromFile));
                        } else if (section.equals(Section.DATA)) {
                            currentDetails.data.put(multiLineKey, new TestPermutation.Value(multiLineValue, OutputFormat.FromFile));
                        } else {
                            throw new UnexpectedLiquibaseException("Unknown multiline section on line " + lineNumber + ": " + section);
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

                if (permutationStartPattern.matcher(line).matches() || permutationsStartPattern.matcher(line).matches()) {
                    saveLastPermutation(currentDetails, results);
                    currentDetails = new CurrentDetails(testClass, testName);
                    commonDetails = new HashMap<String, Object>();
                    firstTableRow = true;
                    section = Section.DEFINITION;
                    continue;
                }

                Matcher internalKeyValueMatcher = internalKeyValuePattern.matcher(line);
                if (internalKeyValueMatcher.matches()) {
                    String key = internalKeyValueMatcher.group(1);
                    String value = internalKeyValueMatcher.group(2);
                    if (key.equals("VERIFIED")) {
                        currentDetails.setVerified(value);
                    } else {
                        throw new UnexpectedLiquibaseException("Unknown internal parameter " + key);
                    }
                    continue;
                }

                Matcher keyValueMatcher = keyValuePattern.matcher(line);
                if (keyValueMatcher.matches()) {
                    String key = keyValueMatcher.group(1);
                    String value = keyValueMatcher.group(2);

                    if (section.equals(Section.DEFINITION)) {
                        currentDetails.definition.put(key, new TestPermutation.Value(value, OutputFormat.FromFile));
                        commonDetails.put(key, new TestPermutation.Value(value, OutputFormat.FromFile));
                    } else if (section.equals(Section.NOTES)) {
                        currentDetails.notes.put(key, new TestPermutation.Value(value, OutputFormat.FromFile));
                    } else if (section.equals(Section.DATA)) {
                        currentDetails.data.put(key, new TestPermutation.Value(value, OutputFormat.FromFile));
                    } else {
                        throw new UnexpectedLiquibaseException("Unknown section " + section);
                    }
                    continue;
                }

                if (line.startsWith("|")) {
                    String unlikelyStringForSplit = "OIPUGAKJNGAOIUWDEGKJASDG";
                    String lineToSplit = line.replaceFirst("\\|", unlikelyStringForSplit).replaceAll("([^\\\\])\\|", "$1" + unlikelyStringForSplit);
                    String[] values = lineToSplit.split("\\s*" + unlikelyStringForSplit + "\\s*");
                    if (line.startsWith("| Permutation ")) {
                        thisTableColumns = new ArrayList<String>();

                        for (int i = 3; i < values.length - 1; i++) { //ignoring first value that is an empty string and last value that is DETAILS
                            tableColumns.add(values[i]);
                            thisTableColumns.add(values[i]);
                        }
                    } else {
                        if (!firstTableRow) {
                            saveLastPermutation(currentDetails, results);
                        }
                        firstTableRow = false;
                        if (values[1].equals("")) {
                            ; //continuing row
                        } else {
                            for (Map.Entry<String, Object> entry : commonDetails.entrySet()) {
                                currentDetails.definition.put(entry.getKey(), new TestPermutation.Value(entry.getValue(), OutputFormat.FromFile));
                            }
                            currentDetails.setVerified(values[2]);

                            int columnNum = 0;
                            try {
                                for (int i = 3; i < values.length - 1; i++) {
                                    if (!values[i].equals("")) {
                                        currentDetails.definition.put(thisTableColumns.get(columnNum), new TestPermutation.Value(decode(values[i]), OutputFormat.FromFile));
                                    }
                                    columnNum++;
                                }
                            } catch (Throwable e) {
                                throw new UnexpectedLiquibaseSdkException("Error parsing line " + line, e);
                            }
                        }
                        String details = values[values.length - 1];
                        Matcher dataMatcher = dataDetailsMatcher.matcher(details);
                        Matcher notesMatcher = notesDetailsMatcher.matcher(details);
                        if (dataMatcher.matches()) {
                            currentDetails.data.put(dataMatcher.group(1), new TestPermutation.Value(decode(dataMatcher.group(2)), OutputFormat.FromFile));
                        } else if (notesMatcher.matches()) {
                            currentDetails.notes.put(notesMatcher.group(1), new TestPermutation.Value(decode(notesMatcher.group(2)), OutputFormat.FromFile));
                        } else {
                            throw new RuntimeException("Unknown details column format: " + details);
                        }
                    }
                    continue;
                }

                Matcher multiLineKeyValueMatcher = multiLineKeyValuePattern.matcher(line);
                if (multiLineKeyValueMatcher.matches()) {
                    multiLineKey = multiLineKeyValueMatcher.group(1);
                    multiLineValue = "";
                    continue;
                }

                if (currentDetails == null) {
                    //in the header section describing what the file is for
                } else {
                    throw new UnexpectedLiquibaseException("Could not parse line " + lineNumber + ": " + line);
                }
            }
            saveLastPermutation(currentDetails, results);
        }

        for (TestPermutation permutation : results) {
            permutation.asTable(tableColumns);
        }
        return results;
    }

    private void saveLastPermutation(CurrentDetails currentDetails, List<TestPermutation> results) {
        if (currentDetails == null) {
            return;
        }
        TestPermutation permutation = new TestPermutation(currentDetails.definition);
        for (Map.Entry<String, Object> entity : currentDetails.notes.entrySet()) {
            permutation.note(entity.getKey(), entity.getValue());
        }
        for (Map.Entry<String, Object> entity : currentDetails.data.entrySet()) {
            permutation.data(entity.getKey(), entity.getValue());
        }
        if (currentDetails.verified != null) {
            permutation.setVerified(currentDetails.verified);
        }
        permutation.setNotRanMessage(currentDetails.notRanMessage);

        results.add(permutation);
    }

    private String decode(String string) {
        return string.replace("<br>", "\n").replace("\\|", "|");
    }

    private static class CurrentDetails {
        private final String testClass;
        private final String testName;
        private Map<String, Object> definition = new HashMap<String, Object>();
        private Map<String, Object> notes = new HashMap<String, Object>();
        private Map<String, Object> data = new HashMap<String, Object>();
        private Boolean verified;
        private String notRanMessage;

        public CurrentDetails(String testClass, String testName) {
            this.testClass = testClass;
            this.testName = testName;
        }

        protected void setVerified(String value) {
            String[] splitValue = value.split("\\s+", 2);
            verified = Boolean.valueOf(splitValue[0]);
            if (splitValue.length > 1) {
                notRanMessage = splitValue[1];
            }
        }

    }
}
