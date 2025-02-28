package liquibase.util;

import liquibase.exception.LiquibaseException;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TableOutput {
    /**
     *
     * This method outputs the input data in a tabular format *without* wrapping of lines
     *
     * @param table                    2-dimensional array of data
     * @param leftJustifiedRows        If true then add "-" to format string
     * @param writer                   Writer to use for output
     *
     */
    public static void formatUnwrappedOutput(List<List<String>> table, boolean leftJustifiedRows, Writer writer) throws LiquibaseException {
        formatOutput(table, computeMaxWidths(table), leftJustifiedRows, writer);
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
    public static void formatOutput(List<List<String>> table, List<Integer> maxWidths, boolean leftJustifiedRows, Writer writer) throws LiquibaseException {
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
        formatOutput(table, IntStream.of(maxWidths).boxed().collect(Collectors.toList()), leftJustifiedRows, writer);
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
    public static void formatOutput(String[][] table, List<Integer> maxWidths, boolean leftJustifiedRows, Writer writer) throws LiquibaseException {
        /*
         * Default maximum allowed width. Line will be wrapped beyond this width.
         */
        int defaultMaxWidth = 30;

        if (table[0].length != maxWidths.size()) {
            throw new RuntimeException("Table and maximum widths arrays must be the same length");
        } else {
            for (int i=0; i < maxWidths.size(); i++) {
                if (maxWidths.get(i) == 0) {
                    maxWidths.set(i, defaultMaxWidth);
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
                row[i] = padColumn(row[i], maxWidths.get(i));
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
            // In cases where a row contains content for a single line only, separation line doesn't appear
            // This verifies all the row values have empty string at the end which will eventually be switched with
            // the table line separator
            boolean endLineAdded = false;
            do {
                isMultiLine = false;
                String[] newRow = new String[row.length];
                for (int i = 0; i < row.length; i++) {
                    // If data is less than max width, use that as it is.
                    if (row[i] == null || row[i].length() < maxWidths.get(i)) {
                        if (multiLine == 0) {
                            newRow[i] = row[i];
                        } else {
                            newRow[i] = "";
                            endLineAdded = true;
                        }
                    } else if ((row[i].length() > (multiLine * maxWidths.get(i)))) {
                        //
                        // If the cell width is more than max width, then split the data at maxWidths.get(i).
                        // the rest of the data will go on the next row
                        //
                        int end = Math.min(row[i].length(), ((multiLine * maxWidths.get(i)) + maxWidths.get(i)));
                        newRow[i] = row[i].substring((multiLine * maxWidths.get(i)), end);
                        isMultiLine = true;
                    } else {
                        newRow[i] = "";
                        endLineAdded = true;
                    }
                }
                finalTableList.add(newRow);
                if (isMultiLine) {
                    multiLine++;
                }
            } while (isMultiLine);
            if (!endLineAdded) {
                String[] emptyCells = Collections.nCopies(row.length, "").toArray(new String[row.length]);
                finalTableList.add(emptyCells);
            }
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
        List<Integer> columnLengths = computeMaxWidths(finalTable);

        /*
         * Prepare format String
         */
        final StringBuilder formatString = new StringBuilder();
        String flag = leftJustifiedRows ? "-" : "";
        columnLengths.forEach((value) -> formatString.append("| %" + flag + value + "s "));
        formatString.append("|\n");

        /*
         * Prepare line for top, bottom & below header row.
         */
        StringBuilder builder = new StringBuilder();
        for (Integer columnLength : columnLengths) {
            builder.append("+-");
            builder.append(String.join("", Collections.nCopies(columnLength, "-")));
            builder.append("-");
        }
        String line = builder.append("+\n").toString();

        /*
         * Output table
         */
        outputLines.append(line);
        for (String[] strings : finalTable) {
            if (allEmptyStrings(strings)) {
                outputLines.append(line);
            } else {
                outputLines.append(String.format(formatString.toString(), (Object[]) strings));
            }
        }
        try {
            writer.append(outputLines.toString());
            writer.flush();
        } catch (IOException ioe) {
            throw new LiquibaseException(ioe);
        }
    }

    /**
     * Compute the size of the largest string of each column of the provided table
     * @param rows the provided table to compute widths from
     * @return an empty immutable list if the provided table is empty
     * @throws RuntimeException if rows is null or the column count is not the same for every row
     */
    public static List<Integer> computeMaxWidths(List<List<String>> rows) {
        return computeMaxWidths(rows.stream().map(row -> row.toArray(new String[0])).toArray(String[][]::new));
    }

    /**
     * Compute the size of the largest string of each column of the provided table
     * @param rows the provided table to compute widths from
     * @return an empty immutable list if the provided table is empty
     * @throws RuntimeException if rows is null or the column count is not the same for every row
     */
    public static List<Integer> computeMaxWidths(String[][] rows) {
        if (rows.length == 0) {
            return Collections.emptyList();
        }
        int columnCount = rows[0].length;
        List<Integer> widths = new ArrayList<>(Collections.nCopies(columnCount, 0));
        for (String[] row : rows) {
            if (row.length != columnCount) {
                throw new RuntimeException(
                        String.format("could not compute table width: heterogeneous tables are not supported. " +
                                        "Expected each row to have %d column(s), found %d",
                                columnCount,
                                row.length)
                );
            }
            for (int i = 0; i < row.length; i++) {
                String column = row[i];
                if (column != null && column.length() > widths.get(i)) {
                    widths.set(i, column.length());
                }
            }
        }
        return widths;
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
        // If the column is null or shorter than the maxWidth AND the column does not contain a line separator,
        // return it unmodified. If it contains a line separator, then it does not matter if it is shorter than the max
        // width, because it must be split on the line separator to flow into multiple lines.
        if (col == null || (col.length() <= maxWidth && !col.contains(System.lineSeparator()))) {
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
        // If a word that is longer than the maxWidth is appended before this method is called, it will spill onto
        // multiple lines, and we only care about the runningWidth of the last line.
        if (runningWidth > maxWidth) {
            runningWidth = runningWidth % maxWidth;
        }
        int spaceWidth = runningWidth > 0 ? 1 : 0;

        boolean lineNotFilled = runningWidth != maxWidth;
        if (runningWidth + (part.length() + spaceWidth) > maxWidth
                && runningWidth > 0 // If runningWidth is 0, then no need to add a space before the part because it is the first part of the line.
                && lineNotFilled) { // If runningWidth is not equal to maxWidth, then no need to add a space before the part because the next part will be written to the next line.
            runningWidth = fillLineWithSpaces(runningWidth, maxWidth, result);
        }
        if (runningWidth > 0 && lineNotFilled) {
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
        for (int i=0; i < (maxWidth - (runningWidth % maxWidth)); i++) {
            result.append(" ");
        }
        return 0;
    }
}
