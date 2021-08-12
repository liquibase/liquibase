package liquibase.util;

import liquibase.Scope;
import liquibase.exception.LiquibaseException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
        try {
            formatOutput(table, maxWidths, true, new OutputStreamWriter(System.out));
        } catch (LiquibaseException ioe) {
            throw new RuntimeException(ioe);
        }

        try {
            table = new String[][] {{ "id", "First Name", "Last Name", "Age", "Profile" }};
            formatOutput(table, maxWidths, true, new OutputStreamWriter(System.out));
        } catch (LiquibaseException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     *
     * This method outputs the input data in a tabular format with wrapping of lines
     *
     * @param table                    2-dimensional array of data
     * @param maxWidths                Maximum widths of each column to control wrapping
     * @param leftJustifiedRows        If true then add "-" to format string
     * @param writer                   Writer to use for output
     *
     */
    public static void formatOutput(String[][] table, int[] maxWidths, boolean leftJustifiedRows, Writer writer) throws LiquibaseException {
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
            //
            // Fix up the output by padding columns as needed
            // This makes the logic below more straight-forward
            //
            for (int i=0; i < row.length; i++) {
                row[i] = padColumn(row[i], maxWidths[i]);
            }
            //
            // If any cell length is more than max width, then this will
            // be a multi-line output
            //
            boolean isMultiLine = false;
            //
            // Multi-line count
            //
            int multiLine = 0;
            do {
                isMultiLine = false;
                String[] newRow = new String[row.length];
                for (int i = 0; i < row.length; i++) {
                    // If data is less than max width, use that as it is.
                    if (row[i].length() < maxWidths[i]) {
                        newRow[i] = multiLine == 0 ? row[i] : "";
                    } else if ((row[i].length() > (multiLine * maxWidths[i]))) {
                        //
                        // If the cell width is more than max width, then split the data at maxWidths[i].
                        // the rest of the data will go on the next row
                        //
                        int end = Math.min(row[i].length(), ((multiLine * maxWidths[i]) + maxWidths[i]));
                        newRow[i] = row[i].substring((multiLine * maxWidths[i]), end);
                        isMultiLine = true;
                    } else {
                        newRow[i] = "";
                    }
                }
                finalTableList.add(newRow);
                if (isMultiLine) {
                    multiLine++;
                }
            } while (isMultiLine);
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
            StringBuilder tempLine = new StringBuilder("+-");
            for (int i=0; i < b.getValue(); ++i) {
                tempLine.append("-");
            }
            tempLine.append("-");
            return ln + tempLine;
        }, (a, b) -> a + b);
        line = line + "+\n";

        /*
         * Output table
         */
        outputLines.append(line);
        boolean firstLine = true;
        for (String[] strings : finalTable) {
            if (allEmptyStrings(strings)) {
                outputLines.append(line);
            } else {
                outputLines.append(String.format(formatString.toString(), (Object[]) strings));
            }
            if (firstLine) {
                outputLines.append(line);
                firstLine = false;
            }
        }
        try {
            writer.append(outputLines.toString());
            writer.flush();
        } catch (IOException ioe) {
            throw new LiquibaseException(ioe);
        }
    }

    private static boolean allEmptyStrings(String[] strings) {
        return Arrays.stream(strings).allMatch(StringUtil::isEmpty);
    }

    //
    // This method takes as input a space separated String
    // and then pads it in the appropriate places so that it
    // can be used as a multi-line cell in an output table.
    //
    private static String padColumn(String col, int maxWidth) {
        if (col.length() <= maxWidth) {
            return col;
        }
        String[] parts = col.split(" ");
        int runningWidth = 0;
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (runningWidth + part.length() > maxWidth) {
               for (int i=0; i < (maxWidth - runningWidth + 2); i++) {
                   result.append(" ");
               }
               runningWidth = 0;
            }
            if (runningWidth > 0) {
                result.append(" ");
                runningWidth++;
            }
            result.append(part);
            runningWidth += part.length();
        }
        return result.toString();
    }
}
