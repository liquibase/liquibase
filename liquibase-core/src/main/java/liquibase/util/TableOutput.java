package liquibase.util;

import liquibase.Scope;
import liquibase.exception.LiquibaseException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

public class TableOutput {
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
    public static void formatOutput(List<List<String>> table, int[] maxWidths, boolean leftJustifiedRows, Writer writer) throws LiquibaseException {
        formatOutput(table.stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new), maxWidths, leftJustifiedRows, writer);
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
                    if (row[i] == null || row[i].length() < maxWidths[i]) {
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
                if (a[i] != null && columnLengths.get(i) < a[i].length()) {
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
        if (col == null || col.length() <= maxWidth) {
            return col;
        }
        String[] parts = col.split(" ");
        int runningWidth = 0;
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            // if the string contains a line separator, then write the values onto separate lines in the table
            String[] lineSplitParts = part.split(System.lineSeparator());
            if (lineSplitParts.length > 1) {
                for (int i = 0; i < lineSplitParts.length; i++) {
                    String lineSplitPart = lineSplitParts[i];
                    runningWidth = doAppend(runningWidth, lineSplitPart, maxWidth, result);
                    // append spaces to push onto a new line if this is not the last entry in the array
                    if (i != lineSplitParts.length -1) {
                        runningWidth = fillLineWithSpaces(runningWidth, maxWidth, result);
                    }
                }
            } else {
                runningWidth = doAppend(runningWidth, part, maxWidth, result);
            }

        }
        return result.toString();
    }

    /**
     * Append the specified part of a string to the string builder and add spaces where needed.
     * @param runningWidth the current running width
     * @param part the part to append
     * @param maxWidth the maxwidth for this column
     * @param result the string builder result to append to
     * @return the new current running width
     */
    private static int doAppend(int runningWidth, String part, int maxWidth, StringBuilder result) {
        int spaceWidth = runningWidth > 0 ? 1 : 0;
        if (runningWidth + (part.length() + spaceWidth) > maxWidth) {
            runningWidth = fillLineWithSpaces(runningWidth, maxWidth, result);
        }
        if (runningWidth > 0) {
            result.append(" ");
            runningWidth++;
        }
        result.append(part);
        runningWidth += part.length();
        return runningWidth;
    }

    /**
     * Given a current running width and a max width, fill the remaining space on the current line with spaces.
     * @param runningWidth the current running width
     * @param maxWidth the max width for the column
     * @param result the result to append spaces to
     * @return the new current running width (which is always 0, since the line has been filled to the end with spaces)
     */
    private static int fillLineWithSpaces(int runningWidth, int maxWidth, StringBuilder result) {
        for (int i=0; i < (maxWidth - runningWidth); i++) {
            result.append(" ");
        }
        return 0;
    }
}
