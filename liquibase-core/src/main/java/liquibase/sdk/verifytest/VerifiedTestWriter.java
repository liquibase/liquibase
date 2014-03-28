package liquibase.sdk.verifytest;

import liquibase.util.MD5Util;
import liquibase.util.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class VerifiedTestWriter {

    public void write(VerifiedTest test, Writer out, String group) throws IOException {
        out.append("# Test: ").append(test.getTestClass()).append(" \"").append(test.getTestName()).append("\"");
        if (group != null) {
            out.append(" Group \"").append(group).append("\"");
        }
        out.append(" #\n\n");

        out.append("NOTE: This output is generated when the test is ran. DO NOT EDIT MANUALLY\n\n");

        printNonTablePermutations(test, out, group);
        printTablePermutations(test, out, group);

        out.flush();
    }

    protected void printNonTablePermutations(VerifiedTest test, Writer out, String group) throws IOException {
        List<TestPermutation> permutations = new ArrayList(test.getPermutations());
        Collections.sort(permutations, new Comparator<TestPermutation>() {
            @Override
            public int compare(TestPermutation o1, TestPermutation o2) {
                return o1.getFullKey().compareTo(o2.getFullKey());
            }
        });

        for (TestPermutation permutation : permutations) {
            if (permutation.getRowDescriptionParameter() != null) {
                continue;
            }

            if (!permutation.isValid()) {
                continue;
            }

            if (group != null && !group.equals(permutation.getGroup())) {
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
            message += " "+StringUtils.trimToEmpty(permutation.getNotRanMessage());
        }

        return message;
    }

    protected void printTablePermutations(VerifiedTest test, Writer out, String group) throws IOException {
        SortedMap<String, List<TestPermutation>> permutationsByTable = new TreeMap<String, List<TestPermutation>>();
        for (TestPermutation permutation : test.getPermutations()) {
            if (permutation.getRowDescriptionParameter() == null) {
                continue;
            }

            if (group != null && !group.equals(permutation.getGroup())) {
                continue;
            }

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
            String tableKey = entry.getKey();
            List<TestPermutation> permutations = new ArrayList<TestPermutation>();
            for (TestPermutation permutation : entry.getValue()) {
                if (permutation.isValid()) {
                    permutations.add(permutation);
                }
            }

            if (permutations.size() == 0) {
                continue;
            }

            out.append("## Permutation Group for ").append(permutations.get(0).getRowDescriptionParameter()).append(": ").append(MD5Util.computeMD5(tableKey).substring(0, 16)).append(" ##\n\n");
            for (Map.Entry<String, TestPermutation.Value> descriptionEntry : permutations.get(0).getDescription().entrySet()) {
                appendMapEntry(descriptionEntry, out);
            }


            SortedMap<String, Integer> maxColumnLengths = new TreeMap<String, Integer>();
            int permutationNameColLength = "Permutation".length();
            int verifiedColLength = "Verified".length();

            for (TestPermutation permutation : permutations) {
                if (permutation.getKey().length() > permutationNameColLength) {
                    permutationNameColLength = permutation.getKey().length();
                }

                String verifiedMessage = getVerifiedMessage(permutation);
                if (verifiedMessage.length() > verifiedColLength) {
                    verifiedColLength = verifiedMessage.length();
                }
                Map<String, TestPermutation.Value> columnMap = permutation.getRowDescription();
                for (Map.Entry<String, TestPermutation.Value> columnEntry : columnMap.entrySet()) {
                    Integer oldMax = maxColumnLengths.get(columnEntry.getKey());
                    if (oldMax == null) {
                        oldMax = columnEntry.getKey().length();
                        maxColumnLengths.put(columnEntry.getKey(), oldMax);
                    }
                    String value = columnEntry.getValue().serialize();
                    if (value != null) {
                        if (oldMax < value.length()) {
                            maxColumnLengths.put(columnEntry.getKey(), value.length());
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
            for (TestPermutation permutation : permutations) {
                StringBuilder row = new StringBuilder();
                row.append("| ").append(StringUtils.pad(permutation.getKey(), permutationNameColLength))
                        .append(" | ")
                        .append(StringUtils.pad(getVerifiedMessage(permutation), verifiedColLength))
                        .append(" |");

                String rowKey = "";
                for (Map.Entry<String, Integer> columnAndLength : maxColumnLengths.entrySet()) {
                    TestPermutation.Value cellValue = permutation.getRowDescription().get(columnAndLength.getKey());
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
