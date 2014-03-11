package liquibase.sdk.verifytest;

import liquibase.util.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class VerifiedTestWriter {

    public void write(VerifiedTest test, Writer out) throws IOException {
        out.append("# Test: ").append(test.getTestClass()).append(" \"").append(test.getTestName()).append("\" #\n\n");
        out.append("NOTE: This output is generated when the test is ran. DO NOT EDIT MANUALLY\n\n");

        List<TestPermutation> permutations = new ArrayList(test.getPermutations());
        Collections.sort(permutations, new Comparator<TestPermutation>() {
            @Override
            public int compare(TestPermutation o1, TestPermutation o2) {
                return o1.getLongKey().compareTo(o2.getLongKey());
            }
        });
        for (TestPermutation permutation : permutations) {
            out.append("## Permutation: ").append(permutation.getKey()).append(" ##\n\n");
            out.append("- _VERIFIED:_ ").append(String.valueOf(permutation.getVerified()));
            if (!permutation.getVerified() && StringUtils.trimToNull(permutation.getNotVerifiedMessage()) != null) {
                out.append(" ").append(StringUtils.trimToEmpty(permutation.getNotVerifiedMessage()));
            }
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

        out.flush();
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
