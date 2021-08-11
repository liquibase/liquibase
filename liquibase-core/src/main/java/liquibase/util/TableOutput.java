package liquibase.util;

import liquibase.Scope;

import java.util.*;

public class TableOutput {
    public static void main(String[] args) {
        /*
         * Table to print in console in 2-dimensional array. Each sub-array is a row.
         */
        String[][] table = new String[][] { { "id", "First Name", "Last Name", "Age", "Profile" },
            { "1", "John", "Johnson", "45", "My name is John Johnson. My id is 1. My age is 45." },
            { "2", "Tom", "", "35", "My name is Tom. My id is 2. My age is 35." },
            { "3", "Rose", "Johnson Johnson Johnson Johnson Johnson Johnson Johnson Johnson Johnson Johnson", "22",
                "My name is Rose Johnson. My id is 3. My age is 22." },
            { "4", "Jimmy", "Kimmel", "", "My name is Jimmy Kimmel. My id is 4. My age is not specified. "
                + "I am the host of the late night show. I am not fan of Matt Damon. " } };
        int[] maxWidths = {30, 30, 30, 30, 30};
        formatOutput(table, maxWidths, true);

        table = new String[][] {{ "id", "First Name", "Last Name", "Age", "Profile" }};
        formatOutput(table, maxWidths, true);
    }

    /**
     *
     * This method outputs the input data in a tabular format with wrapping of lines
     *
     * @param table                    2-dimensional array of data
     * @param maxWidths                Maximum widths of each column to control wrapping
     * @param leftJustifiedRows        If true then add "-" to format string
     *
     */
    public static void formatOutput(String[][] table, int[] maxWidths, boolean leftJustifiedRows) {
        /*
         * Default maximum allowed width. Line will be wrapped beyond this width.
         */
        int defaultMaxWidth = 30;

        if (table[0].length != maxWidths.length) {
            throw new RuntimeException("Table and maximum widths arrays must be the same length");
        } else {
            for (int i=0; i < maxWidths.length; i++) {
                if (maxWidths[i] == 0) {
                    maxWidths[i] = defaultMaxWidth;
                }
            }
        }

        /*
         *
         * Create new table array with wrapped rows
         *
         */
        StringBuilder outputLines = new StringBuilder();
        List<String[]> tableList = new ArrayList<>(Arrays.asList(table));
        List<String[]> finalTableList = new ArrayList<>();
        for (String[] row : tableList) {
            // If any cell data is more than max width, then it will need extra row.
            boolean needExtraRow = false;
            // Count of extra split row.
            int splitRow = 0;
            do {
                needExtraRow = false;
                String[] newRow = new String[row.length];
                for (int i = 0; i < row.length; i++) {
                    // If data is less than max width, use that as it is.
                    if (row[i].length() < maxWidths[i]) {
                        newRow[i] = splitRow == 0 ? row[i] : "";
                    } else if ((row[i].length() > (splitRow * maxWidths[i]))) {
                        // If data is more than max width, then crop data at maxWidths[i].
                        // Remaining cropped data will be part of next row.
                        int end = Math.min(row[i].length(), ((splitRow * maxWidths[i]) + maxWidths[i]));
                        newRow[i] = row[i].substring((splitRow * maxWidths[i]), end);
                        needExtraRow = true;
                    } else {
                        newRow[i] = "";
                    }
                }
                finalTableList.add(newRow);
                if (needExtraRow) {
                    splitRow++;
                }
            } while (needExtraRow);
        }
        String[][] finalTable = new String[finalTableList.size()][finalTableList.get(0).length];
        for (int i = 0; i < finalTable.length; i++) {
            finalTable[i] = finalTableList.get(i);
        }

        /*
         * Calculate appropriate Length of each column by looking at width of data in
         * each column.
         *
         * Map columnLengths is <column_number, column_length>
         */
        Map<Integer, Integer> columnLengths = new HashMap<>();
        Arrays.stream(finalTable).forEach(a -> {
            for (int i=0; i < a.length; i++) {
                columnLengths.putIfAbsent(i, 0);
                if (columnLengths.get(i) < a[i].length()) {
                    columnLengths.put(i, a[i].length());
                }
            }
        });

        /*
         * Prepare format String
         */
        final StringBuilder formatString = new StringBuilder();
        String flag = leftJustifiedRows ? "-" : "";
        columnLengths.forEach((key, value) -> formatString.append("| %" + flag + value + "s "));
        formatString.append("|\n");

        /*
         * Prepare line for top, bottom & below header row.
         */
        String line = columnLengths.entrySet().stream().reduce("", (ln, b) -> {
            StringBuilder templn = new StringBuilder("+-");
            for (int i=0; i < b.getValue(); ++i) {
                templn.append("-");
            }
            templn.append("-");
            return ln + templn;
        }, (a, b) -> a + b);
        line = line + "+\n";

        /*
         * Output table
         */
        outputLines.append(line);
        for (String[] strings : finalTable) {
            outputLines.append(String.format(formatString.toString(), (Object[])strings));
        }
        outputLines.append(line);
        Scope.getCurrentScope().getUI().sendMessage(outputLines.toString());
    }
}
