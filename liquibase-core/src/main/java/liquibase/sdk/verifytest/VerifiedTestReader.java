package liquibase.sdk.verifytest;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerifiedTestReader {

    private enum Section {
        GROUP_DEFINITION,
        DEFINITION,
        NOTES,
        DATA
    }

    public VerifiedTest read(Reader... readers) throws IOException {
        VerifiedTest results = null;

        Pattern permutationStartPattern = Pattern.compile("## Permutation: (.*) ##");
        Pattern permutationGroupStartPattern = Pattern.compile("## Permutation Group for (.*?): (.*) ##");
        Pattern internalKeyValuePattern = Pattern.compile("\\- _(.+):_ (.+)");
        Pattern keyValuePattern = Pattern.compile("\\- \\*\\*(.+):\\*\\* (.*)");
        Pattern multiLineKeyValuePattern = Pattern.compile("\\- \\*\\*(.+) =>\\*\\*");
        Pattern dataDetailsMatcher = Pattern.compile("\\*\\*(.*?)\\*\\*: (.*)");
        Pattern notesDetailsMatcher = Pattern.compile("__(.*?)__: (.*)");

        for (Reader reader : readers) {
            BufferedReader bufferedReader = new BufferedReader(reader);

            TestPermutation currentPermutation = null;
            Map<String, String> currentPermutationGroup = null;
            List<String> permutationColumns = null;
            String permutationDefinitionKey = null;

            String line;
            int lineNumber = 0;
            Section section = null;
            String multiLineKey = null;
            String multiLineValue = null;

            while ((line = bufferedReader.readLine()) != null) {
                lineNumber++;

                if (lineNumber == 1) {
                    Matcher groupMatcher = Pattern.compile("# Test: (\\S*) \"(.*)\" Group \"(.*)\" #").matcher(line);
                    Matcher nonGroupMatcher = Pattern.compile("# Test: (\\S*) \"(.*)\" #").matcher(line);
                    if (groupMatcher.matches()) {
                        if (results == null) {
                            String testClass = groupMatcher.group(1);
                            String testName = groupMatcher.group(2);

                            results = new VerifiedTest(testClass, testName);
                        }
                    } else if (nonGroupMatcher.matches()) {
                        if (results == null) {
                            String testClass = nonGroupMatcher.group(1);
                            String testName = nonGroupMatcher.group(2);

                            results = new VerifiedTest(testClass, testName);
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
                            currentPermutation.describe(multiLineKey, multiLineValue, OutputFormat.FromFile);
                        } else if (section.equals(Section.GROUP_DEFINITION)) {
                            currentPermutationGroup.put(multiLineKey, multiLineValue);
                        } else if (section.equals(Section.NOTES)) {
                            currentPermutation.note(multiLineKey, multiLineValue, OutputFormat.FromFile);
                        } else if (section.equals(Section.DATA)) {
                            currentPermutation.data(multiLineKey, multiLineValue, OutputFormat.FromFile);
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

                Matcher permutationStartMatcher = permutationStartPattern.matcher(line);
                if (permutationStartMatcher.matches()) {
                    currentPermutation = new TestPermutation(results);
                    section = Section.DEFINITION;
                    continue;
                }

                Matcher permutationGroupStartMatcher = permutationGroupStartPattern.matcher(line);
                if (permutationGroupStartMatcher.matches()) {
                    currentPermutation = null;
                    currentPermutationGroup = new HashMap<String, String>();
                    permutationDefinitionKey = permutationGroupStartMatcher.group(1);
                    permutationColumns = new ArrayList<String>();
                    section = Section.GROUP_DEFINITION;
                    continue;
                }


                Matcher internalKeyValueMatcher = internalKeyValuePattern.matcher(line);
                if (internalKeyValueMatcher.matches()) {
                    String key = internalKeyValueMatcher.group(1);
                    String value = internalKeyValueMatcher.group(2);
                    if (key.equals("VERIFIED")) {
                        setVerifiedFromString(currentPermutation, value);
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
                        currentPermutation.describe(key, value, OutputFormat.FromFile);
                    } else if (section.equals(Section.GROUP_DEFINITION)) {
                        currentPermutationGroup.put(key, value);
                    } else if (section.equals(Section.NOTES)) {
                        currentPermutation.note(key, value, OutputFormat.FromFile);
                    } else if (section.equals(Section.DATA)) {
                        currentPermutation.data(key, value, OutputFormat.FromFile);
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
                        for (int i = 3; i < values.length - 1; i++) { //ignoring first value that is an empty string and last value that is DETAILS
                            permutationColumns.add(values[i]);
                        }
                    } else {
                        if (values[1].equals("")) {
                            ; //continuing row
                        } else {
                            currentPermutation = new TestPermutation(results);
                            for (Map.Entry<String, String> entry : currentPermutationGroup.entrySet()) {
                                currentPermutation.describe(entry.getKey(), entry.getValue(), OutputFormat.FromFile);
                            }
                            setVerifiedFromString(currentPermutation, values[2]);
                            int columnNum = 0;
                            Map<String, TestPermutation.Value> valueDescription = new HashMap<String, TestPermutation.Value>();
                            try {
                                for (int i = 3; i < values.length - 1; i++) {
                                    if (!values[i].equals("")) {
                                        valueDescription.put(permutationColumns.get(columnNum), new TestPermutation.Value(decode(values[i]), OutputFormat.FromFile));
                                    }
                                    columnNum++;
                                }
                            } catch (Throwable e) {
                                throw new UnexpectedLiquibaseException("Error parsing line " + line);
                            }
                            currentPermutation.describeAsTable(permutationDefinitionKey, valueDescription);
                        }
                        String details = values[values.length - 1];
                        Matcher dataMatcher = dataDetailsMatcher.matcher(details);
                        Matcher notesMatcher = notesDetailsMatcher.matcher(details);
                        if (dataMatcher.matches()) {
                            currentPermutation.data(dataMatcher.group(1), decode(dataMatcher.group(2)), OutputFormat.FromFile);
                        } else if (notesMatcher.matches()) {
                            currentPermutation.note(notesMatcher.group(1), decode(notesMatcher.group(2)), OutputFormat.FromFile);
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

                if (currentPermutation == null) {
                    //in the header section describing what the file is for
                } else {
                    throw new UnexpectedLiquibaseException("Could not parse line " + lineNumber + ": " + line);
                }
            }
        }

        return results;
    }

    private String decode(String string) {
        return string.replace("<br>", "\n").replace("\\|", "|");
    }

    protected void setVerifiedFromString(TestPermutation currentPermutation, String value) {
        String[] splitValue = value.split("\\s+", 2);
        currentPermutation.setVerified(Boolean.valueOf(splitValue[0]));
        if (splitValue.length > 1) {
            currentPermutation.setNotRanMessage(splitValue[1]);
        }
    }
}
