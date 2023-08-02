package liquibase.util;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.UpdateSummaryEnum;
import liquibase.UpdateSummaryOutputEnum;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.ChangeSetStatus;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.StatusVisitor;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.Logger;
import liquibase.logging.core.AbstractLogger;
import liquibase.logging.core.CompositeLogger;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcObject;
import liquibase.logging.mdc.customobjects.UpdateSummary;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * Methods to show a summary of change set counts after an update
 *
 */
public class ShowSummaryUtil {

    /**
     *
     * Show a summary of the changesets which were executed
     *
     * @param   changeLog                          The changelog used in this update
     * @param   showSummary                        Flag to control whether or not we show the summary
     * @param   showSummaryOutput                  Flag to control where we show the summary
     * @param   statusVisitor                      The StatusVisitor used to determine statuses
     * @param   outputStream                       The OutputStream to use for the summary
     * @throws  LiquibaseException                 Thrown by this method
     * @throws  IOException                        Thrown by this method
     *
     */
    public static void showUpdateSummary(DatabaseChangeLog changeLog, UpdateSummaryEnum showSummary, UpdateSummaryOutputEnum showSummaryOutput, StatusVisitor statusVisitor, OutputStream outputStream)
            throws LiquibaseException, IOException {
        //
        // Check the global flag to turn the summary off
        //
        if (showSummary == null || showSummary == UpdateSummaryEnum.OFF) {
            return;
        }

        //
        // Obtain two lists:  the list of filtered change sets that
        // The StatusVisitor discovered, and also any change sets which
        // were skipped during parsing, i.e. they had mismatched DBMS values
        //
        List<ChangeSetStatus> denied = statusVisitor.getChangeSetsToSkip();
        List<ChangeSet> skippedChangeSets = changeLog.getSkippedChangeSets();

        //
        // Filter the skipped list to remove changes which were:
        // Previously run
        // After the tag
        // After the count value
        //
        List<ChangeSetStatus> filterDenied =
                denied.stream()
                        .filter(status -> status.getFilterResults()
                                .stream().anyMatch(result ->  result.getFilter() != ShouldRunChangeSetFilter.class))
                        .collect(Collectors.toList());

        //
        // Only show the summary
        //
        UpdateSummary updateSummaryMdc = showSummary(changeLog, statusVisitor, skippedChangeSets, filterDenied, outputStream, showSummaryOutput);
        updateSummaryMdc.setValue(showSummary.toString());
        boolean shouldPrintDetailTable = showSummary != UpdateSummaryEnum.SUMMARY && (!skippedChangeSets.isEmpty() || !denied.isEmpty());

        //
        // Show the details too
        //
        SortedMap<String, Integer> skippedMdc = showDetailTable(skippedChangeSets, filterDenied, outputStream, shouldPrintDetailTable);
        updateSummaryMdc.setSkipped(skippedMdc);
        try(MdcObject updateSummaryMdcObject = Scope.getCurrentScope().addMdcValue(MdcKey.UPDATE_SUMMARY, updateSummaryMdc)) {
            Scope.getCurrentScope().getLog(ShowSummaryUtil.class).info("Update summary generated");
        }
    }

    //
    // Show the details
    //
    private static SortedMap<String, Integer> showDetailTable(List<ChangeSet> skippedChangeSets, List<ChangeSetStatus> filterDenied, OutputStream outputStream, boolean shouldPrintDetailTable)
            throws IOException, LiquibaseException {
        String totalSkippedMdcKey = "totalSkipped";
        //
        // Nothing to do
        //
        if (filterDenied.isEmpty() && skippedChangeSets.isEmpty()) {
            return new TreeMap<>(Collections.singletonMap(totalSkippedMdcKey, 0));
        }
        List<String> columnHeaders = new ArrayList<>();
        columnHeaders.add("Changeset Info");
        columnHeaders.add("Reason Skipped");
        List<List<String>> table = new ArrayList<>();
        table.add(columnHeaders);
        SortedMap<String, Integer> mdcSkipCounts = new TreeMap<>();
        mdcSkipCounts.put(totalSkippedMdcKey, skippedChangeSets.size() + filterDenied.size());

        List<ChangeSetStatus> finalList = createFinalStatusList(skippedChangeSets, filterDenied, mdcSkipCounts);

        finalList.sort(new Comparator<ChangeSetStatus>() {
            @Override
            public int compare(ChangeSetStatus o1, ChangeSetStatus o2) {
                ChangeSet c1 = o1.getChangeSet();
                ChangeSet c2 = o2.getChangeSet();
                int order1 = determineOrderInChangelog(c1);
                int order2 = determineOrderInChangelog(c2);
                if (order1 == -1 || order2 == -1) {
                    return -1;
                }
                return Integer.compare(order1, order2);
            }
        });

        //
        // Filtered because of labels or context
        //
        for (ChangeSetStatus st : finalList) {
            AtomicBoolean flag = new AtomicBoolean(true);
            StringBuilder builder = new StringBuilder();
            st.getFilterResults().forEach(consumer -> {
                if (consumer.getFilter() != null) {
                    String displayName = consumer.getMdcName();
                    mdcSkipCounts.merge(displayName, 1, Integer::sum);
                }
                String skippedMessage = String.format("   '%s' : %s", st.getChangeSet().toString(), consumer.getMessage());
                Scope.getCurrentScope().getLog(ShowSummaryUtil.class).info(skippedMessage);
                if (! flag.get()) {
                    builder.append(System.lineSeparator());
                }
                builder.append(consumer.getMessage());
                flag.set(false);
            });
            List<String> outputRow = new ArrayList<>();
            outputRow.add(st.getChangeSet().toString());
            outputRow.add(builder.toString());
            table.add(outputRow);
        }

        if (shouldPrintDetailTable) {
            List<Integer> widths = new ArrayList<>();
            widths.add(60);
            widths.add(40);

            Writer writer = createOutputWriter(outputStream);
            TableOutput.formatOutput(table, widths, true, writer);
        }
        return mdcSkipCounts;
    }

    //
    // Create a final list of changesets to be displayed
    //
    private static List<ChangeSetStatus> createFinalStatusList(List<ChangeSet> skippedChangeSets, List<ChangeSetStatus> filterDenied, SortedMap<String, Integer> mdcSkipCounts) {
        //
        // Add skipped during changelog parsing to the final list
        //
        List<ChangeSetStatus> finalList = new ArrayList<>(filterDenied);
        skippedChangeSets.forEach(skippedChangeSet -> {
            String dbmsList = String.format("'%s'", StringUtil.join(skippedChangeSet.getDbmsSet(), ", "));
            String mismatchMessage = String.format("mismatched DBMS value of %s", dbmsList);
            ChangeSetStatus changeSetStatus = new ChangeSetStatus(skippedChangeSet);
            ChangeSetFilterResult filterResult = new ChangeSetFilterResult(false, mismatchMessage, DbmsChangeSetFilter.class, DbmsChangeSetFilter.MDC_NAME, DbmsChangeSetFilter.DISPLAY_NAME);
            changeSetStatus.setFilterResults(Collections.singleton(filterResult));
            finalList.add(changeSetStatus);
        });
        return finalList;
    }

    //
    // Determine the change set's order in the changelog
    //
    private static int determineOrderInChangelog(ChangeSet changeSetToMatch) {
        DatabaseChangeLog changeLog = changeSetToMatch.getChangeLog();
        int order = 0;
        for (ChangeSet changeSet : changeLog.getChangeSets()) {
            if (changeSet == changeSetToMatch) {
                return order;
            }
            order++;
        }
        return -1;
    }

    //
    // Show the summary list
    //
    private static UpdateSummary showSummary(DatabaseChangeLog changeLog,
                                    StatusVisitor statusVisitor,
                                    List<ChangeSet> skippedChangeSets,
                                    List<ChangeSetStatus> filterDenied,
                                    OutputStream outputStream,
                                    UpdateSummaryOutputEnum showSummaryOutput) throws LiquibaseException {
        StringBuilder builder = new StringBuilder();
        builder.append(System.lineSeparator());
        int totalInChangelog = changeLog.getChangeSets().size() + skippedChangeSets.size();
        int skipped = skippedChangeSets.size();
        int filtered = filterDenied.size();
        int totalAccepted = statusVisitor.getChangeSetsToRun().size();
        int totalPreviouslyRun = totalInChangelog - filtered - skipped - totalAccepted;
        UpdateSummary updateSummaryMdc = new UpdateSummary(null, totalAccepted, totalPreviouslyRun, null, totalInChangelog);

        String message = "UPDATE SUMMARY";
        builder.append(message);
        builder.append(System.lineSeparator());

        message = String.format("Run:                     %6d", totalAccepted);
        builder.append(message);
        builder.append(System.lineSeparator());

        message = String.format("Previously run:          %6d", totalPreviouslyRun);
        builder.append(message);
        builder.append(System.lineSeparator());

        message = String.format("Filtered out:            %6d", filtered + skipped);
        builder.append(message);
        builder.append(System.lineSeparator());

        message = "-------------------------------";
        builder.append(message);
        builder.append(System.lineSeparator());

        message = String.format("Total change sets:       %6d", totalInChangelog);
        builder.append(message);
        builder.append(System.lineSeparator());
        builder.append(System.lineSeparator());

        final Map<String, Integer> filterSummaryMap = new LinkedHashMap<>();
        List<ChangeSetStatus> finalList = createFinalStatusList(skippedChangeSets, filterDenied, null);
        finalList.forEach(status -> {
            status.getFilterResults().forEach(result -> {
                if (! result.isAccepted()) {
                    String displayName = result.getDisplayName();
                    filterSummaryMap.merge(displayName, 1, Integer::sum);
                }
            });
        });

        if (! filterSummaryMap.isEmpty()) {
            message = "FILTERED CHANGE SETS SUMMARY";
            builder.append(System.lineSeparator());
            builder.append(message);
            builder.append(System.lineSeparator());
            filterSummaryMap.forEach((filterDisplayName, count) -> {
                String filterSummaryDetailMessage = String.format("%-18s       %6d",
                        filterDisplayName + ":", count);
                builder.append(filterSummaryDetailMessage);
                builder.append(System.lineSeparator());
            });
            builder.append(System.lineSeparator());
        }

        writeMessage(builder.toString(), showSummaryOutput, outputStream);

        return updateSummaryMdc;
    }

    //
    // Create a Writer to display the summary
    //
    private static Writer createOutputWriter(OutputStream outputStream) throws IOException {
        String charsetName = GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue();
        return new OutputStreamWriter(outputStream, charsetName);
    }

    private static void writeMessage(String message, UpdateSummaryOutputEnum showSummaryOutput, OutputStream outputStream) throws LiquibaseException {
        switch (showSummaryOutput) {
            case CONSOLE:
                writeToOutput(outputStream, message);
                break;
            case LOG:
                writeToLog(message);
                break;
            default:
                writeToOutput(outputStream, message);
                writeToLog(message);
        }
    }

    private static void writeToOutput(OutputStream outputStream, String message) throws LiquibaseException {
        try {
            Writer writer = createOutputWriter(outputStream);
            writer.append(message);
            writer.flush();
        } catch (IOException ioe) {
            throw new LiquibaseException(ioe);
        }
    }

    private static void writeToLog(String message) {
        Stream.of(message.split(System.lineSeparator()))
                .filter(s -> !StringUtil.isWhitespace(s))
                .forEach(Scope.getCurrentScope().getLog(ShowSummaryUtil.class)::info);
    }
}
