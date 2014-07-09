package liquibase.sdk.verifytest;

import liquibase.util.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class VerifiedTestWriter {

    public void write(String testClass, String testName, Collection<TestPermutation> permutations, Writer out) throws IOException {
        out.append("# Test: ").append(testClass).append(" \"").append(testName).append("\"");
        out.append(" #\n\n");

        out.append("NOTE: This output is generated when the test is ran. DO NOT EDIT MANUALLY\n\n");


        if (shouldPrintTables(permutations)) {
            printWithTables(permutations, out);
        } else {
            printWithoutTables(permutations, out);
        }

        out.flush();
    }

    protected boolean shouldPrintTables(Collection<TestPermutation> permutations) {
        if (permutations == null || permutations.size() == 0) {
            return false;
        }

        TestPermutation permutation = permutations.iterator().next();
        return permutation.getTableParameters() != null && permutation.getTableParameters().size() > 0;

    }

    protected void printWithoutTables(Collection<TestPermutation> passedPermutations, Writer out) throws IOException {
        List<TestPermutation> permutations = new ArrayList(passedPermutations);
        Collections.sort(permutations);

        for (TestPermutation permutation : permutations) {
            if (!permutation.isValid()) {
                continue;
            }

            out.append("## Permutation: ").append(permutation.getKey()).append(" ##\n\n");
            out.append("- _VERIFIED:_ ").append(getVerifiedMessage(permutation));
            out.append("\n");

            for (Map.Entry<String, TestPermutation.Value> entry : permutation.getDescription().entrySet()) {
                appendMapEntry(entry, out);
            }

            if (permutation.getNotes().size() > 0) {
                out.append("\n");
                out.append("#### Notes ####\n");
                out.append("\n");

                for (Map.Entry<String, TestPermutation.Value> entry : permutation.getNotes().entrySet()) {
                    appendMapEntry(entry, out);
                }
            }

            if (permutation.getData().entrySet().size() > 0) {
                out.append("\n");
                out.append("#### Data ####\n");
                out.append("\n");

                for (Map.Entry<String, TestPermutation.Value> entry : permutation.getData().entrySet()) {
                    appendMapEntry(entry, out);
                }
            }

            out.append("\n\n");
        }
    }

    private String getVerifiedMessage(TestPermutation permutation) {
        String message = String.valueOf(permutation.getVerified());

        if (!permutation.getVerified() && StringUtils.trimToNull(permutation.getNotRanMessage()) != null) {
            message = StringUtils.trimToEmpty(permutation.getNotRanMessage());
        }

        return message;
    }

    protected void printWithTables(Collection<TestPermutation> passedPermutations, Writer out) throws IOException {
        List<TestPermutation> permutations = new ArrayList(passedPermutations);
        Collections.sort(permutations);

        SortedMap<String, List<TestPermutation>> permutationsByTable = new TreeMap<String, List<TestPermutation>>();
        for (TestPermutation permutation : passedPermutations) {
            String tableKey = permutation.getTableKey();
            if (!permutationsByTable.containsKey(tableKey)) {
                permutationsByTable.put(tableKey, new ArrayList<TestPermutation>());
            }
            permutationsByTable.get(tableKey).add(permutation);
        }

        if (permutationsByTable.isEmpty()) {
            return;
        }

        for (Map.Entry<String, List<TestPermutation>> entry : permutationsByTable.entrySet()) {
            List<TestPermutation> tablePermutations = new ArrayList<TestPermutation>();
            for (TestPermutation permutation : entry.getValue()) {
                if (permutation.isValid()) {
                    tablePermutations.add(permutation);
                }
            }

            if (tablePermutations.size() == 0) {
                continue;
            }

            out.append("## Permutations ##\n\n");
            for (Map.Entry<String, TestPermutation.Value> descriptionEntry : tablePermutations.get(0).getDescription().entrySet()) {
                if (!tablePermutations.get(0).getTableParameters().contains(descriptionEntry.getKey())) {
                    appendMapEntry(descriptionEntry, out);
                }
            }


            SortedMap<String, Integer> maxColumnLengths = new TreeMap<String, Integer>();
            int permutationNameColLength = "Permutation".length();
            int verifiedColLength = "Verified".length();

            for (TestPermutation permutation : tablePermutations) {
                if (permutation.getKey().length() > permutationNameColLength) {
                    permutationNameColLength = permutation.getKey().length();
                }

                String verifiedMessage = getVerifiedMessage(permutation);
                if (verifiedMessage.length() > verifiedColLength) {
                    verifiedColLength = verifiedMessage.length();
                }
                for (String columnName : permutation.getTableParameters()) {
                    Integer oldMax = maxColumnLengths.get(columnName);
                    if (oldMax == null) {
                        oldMax = columnName.length();
                        maxColumnLengths.put(columnName, oldMax);
                    }
                    TestPermutation.Value storedValue = permutation.getDescription().get(columnName);
                    if (storedValue != null) {
                        String value = storedValue.serialize();
                        if (value != null) {
                            if (oldMax < value.length()) {
                                maxColumnLengths.put(columnName, value.length());
                            }
                        }
                    }
                }
            }

            out.append("\n");
            out.append("| ").append(StringUtils.pad("Permutation", permutationNameColLength)).append(" | ")
                    .append(StringUtils.pad("Verified", verifiedColLength)).append(" |");
            for (Map.Entry<String, Integer> columnEntry : maxColumnLengths.entrySet()) {
                out.append(" ").append(StringUtils.pad(columnEntry.getKey(), columnEntry.getValue())).append(" |");
            }
            out.append(" DETAILS\n");

            SortedMap<String, String> permutationRows = new TreeMap<String, String>();
            for (TestPermutation permutation : tablePermutations) {
                StringBuilder row = new StringBuilder();
                row.append("| ").append(StringUtils.pad(permutation.getKey(), permutationNameColLength))
                        .append(" | ")
                        .append(StringUtils.pad(getVerifiedMessage(permutation), verifiedColLength))
                        .append(" |");

                String rowKey = "";
                for (Map.Entry<String, Integer> columnAndLength : maxColumnLengths.entrySet()) {
                    TestPermutation.Value cellValue = permutation.getDescription().get(columnAndLength.getKey());
                    String cellString;
                    if (cellValue == null) {
                        cellString = "";
                    } else {
                        cellString = clean(cellValue.serialize());
                    }

                    rowKey += " "+StringUtils.pad(cellString, columnAndLength.getValue())+ " |";
                }
                row.append(rowKey);

                List<String> details = new ArrayList<String>();
                for (Map.Entry<String, TestPermutation.Value> notesEntry : permutation.getNotes().entrySet()) {
                    details.add(" __"+notesEntry.getKey()+"__: "+clean(notesEntry.getValue().serialize()));
                }
                for (Map.Entry<String, TestPermutation.Value> dataEntry : permutation.getData().entrySet()) {
                    details.add(" **" + dataEntry.getKey() + "**: " + clean(dataEntry.getValue().serialize()));
                }

                for (int i=0; i<details.size(); i++) {
                    if (i > 0) {
                        row.append("| ").append(StringUtils.pad("", permutationNameColLength)).append(" | ")
                                .append(StringUtils.pad("", verifiedColLength)).append(" |");
                        for (Map.Entry<String, Integer> nameAndLength : maxColumnLengths.entrySet()) {
                            row.append(" ").append(StringUtils.pad("", nameAndLength.getValue())).append(" |");
                        }

                    }
                    row.append(details.get(i)).append("\n");
                }

                permutationRows.put(rowKey, row.toString());
            }
            out.append(StringUtils.join(permutationRows.values(), ""));
//
//            if (permutation.getTableDescriptionKey() != null) {
//                continue;
//            }
//
//            if (!permutation.isValid()) {
//                continue;
//            }
//
//            out.append("## Permutation: ").append(permutation.getKey()).append(" ##\n\n");
//            out.append("- _VERIFIED:_ ").append(String.valueOf(permutation.getVerified()));
//            if (!permutation.getVerified() && StringUtils.trimToNull(permutation.getNotRanMessage()) != null) {
//                out.append(" ").append(StringUtils.trimToEmpty(permutation.getNotRanMessage()));
//            }
//            out.append("\n");
//
//            for (Map.Entry<String, TestPermutation.Value> entry : permutation.getDescription().entrySet()) {
//                appendMapEntry(entry, out);
//            }
//
//            if (permutation.getNotes().size() > 0) {
//                out.append("\n");
//                out.append("#### Notes ####\n");
//                out.append("\n");
//
//                for (Map.Entry<String, TestPermutation.Value> entry : permutation.getNotes().entrySet()) {
//                    appendMapEntry(entry, out);
//                }
//            }
//
//            if (permutation.getData().entrySet().size() > 0) {
//                out.append("\n");
//                out.append("#### Data ####\n");
//                out.append("\n");
//
//                for (Map.Entry<String, TestPermutation.Value> entry : permutation.getData().entrySet()) {
//                    appendMapEntry(entry, out);
//                }
//            }
//
            out.append("\n\n");
        }
    }

    private String clean(String string) {
        return string.replace("\r\n", "\n").replace("\n", "<br>").replace("|", "\\|");
    }

    private void appendMapEntry(Map.Entry<String, TestPermutation.Value> entry, Writer out) throws IOException {
        String value = entry.getValue().serialize();
        value  = value.replace("\r\n", "\n");

        boolean multiLine = value.contains("\n");

        out.append("- **").append(entry.getKey());

        if (multiLine) {
            out.append(" =>**\n");
        } else {
            out.append(":** ");
        }
        if (multiLine) {
            out.append(StringUtils.indent(value, 4));
        } else {
            out.append(value);
        }

        out.append("\n");
    }

}
