package liquibase.util;

import liquibase.*;
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.ChangeSetStatus;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.filter.ShouldRunChangeSetFilter;
import liquibase.changelog.visitor.StatusVisitor;
import liquibase.exception.LiquibaseException;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcObject;
import liquibase.logging.mdc.customobjects.UpdateSummary;
import liquibase.report.ShowSummaryGenerator;
import liquibase.report.ShowSummaryGeneratorFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Methods to show a summary of change set counts after an update
 */
public class ShowSummaryUtil {

    /**
     * Show a summary of the changesets which were executed
     *
     * @param changeLog         The changelog used in this update
     * @param showSummary       Flag to control whether or not we show the summary
     * @param showSummaryOutput Flag to control where we show the summary
     * @param statusVisitor     The StatusVisitor used to determine statuses
     * @param outputStream      The OutputStream to use for the summary
     * @throws LiquibaseException Thrown by this method
     * @throws IOException        Thrown by this method
     */
    @Deprecated
    public static void showUpdateSummary(DatabaseChangeLog changeLog, UpdateSummaryEnum showSummary, UpdateSummaryOutputEnum showSummaryOutput, StatusVisitor statusVisitor, OutputStream outputStream)
            throws LiquibaseException, IOException {
        showUpdateSummary(changeLog, showSummary, showSummaryOutput, statusVisitor, outputStream, null);
    }

    /**
     * Show a summary of the changesets which were executed
     *
     * @param changeLog         The changelog used in this update
     * @param showSummary       Flag to control whether or not we show the summary
     * @param showSummaryOutput Flag to control where we show the summary
     * @param statusVisitor     The StatusVisitor used to determine statuses
     * @param outputStream      The OutputStream to use for the summary
     * @throws LiquibaseException Thrown by this method
     * @throws IOException        Thrown by this method
     */
    public static void showUpdateSummary(DatabaseChangeLog changeLog, UpdateSummaryEnum showSummary, UpdateSummaryOutputEnum showSummaryOutput, StatusVisitor statusVisitor, OutputStream outputStream, ChangeLogIterator runChangeLogIterator)
            throws LiquibaseException, IOException {
        buildSummaryDetails(changeLog, showSummary, showSummaryOutput, statusVisitor, outputStream, runChangeLogIterator);
    }

    /**
     * Show a summary of the changesets which were executed AND return an object with the records of what has happened.
     *
     * @param changeLog         The changelog used in this update
     * @param showSummary       Flag to control whether we show the summary
     * @param showSummaryOutput Flag to control where we show the summary
     * @param statusVisitor     The StatusVisitor used to determine statuses
     * @param outputStream      The OutputStream to use for the summary
     * @return the details of the update summary
     * @throws LiquibaseException Thrown by this method
     * @throws IOException        Thrown by this method
     */
    public static UpdateSummaryDetails buildSummaryDetails(DatabaseChangeLog changeLog, UpdateSummaryEnum showSummary, UpdateSummaryOutputEnum showSummaryOutput, StatusVisitor statusVisitor, OutputStream outputStream, ChangeLogIterator runChangeLogIterator)
            throws LiquibaseException, IOException {
        //
        // Check the global flag to turn the summary off
        //
        if (showSummary == null || showSummary == UpdateSummaryEnum.OFF) {
            return null;
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
                                .stream().anyMatch(result -> result.getFilter() != ShouldRunChangeSetFilter.class))
                        .collect(Collectors.toList());

        ShowSummaryGeneratorFactory showSummaryGeneratorFactory = Scope.getCurrentScope().getSingleton(ShowSummaryGeneratorFactory.class);
        ShowSummaryGenerator showSummaryGenerator = showSummaryGeneratorFactory.getShowSummaryGenerator();
        List<ChangeSetStatus> additionalChangeSetStatus = showSummaryGenerator.getAllAdditionalChangeSetStatus(runChangeLogIterator);

        //
        // Only show the summary
        //
        UpdateSummaryDetails summaryDetails = showSummary(changeLog, statusVisitor, skippedChangeSets, filterDenied, outputStream, showSummaryOutput, runChangeLogIterator);
        summaryDetails.getSummary().setValue(showSummary.toString());
        boolean shouldPrintDetailTable = showSummary != UpdateSummaryEnum.SUMMARY && (!skippedChangeSets.isEmpty() || !denied.isEmpty() || !additionalChangeSetStatus.isEmpty());

        //
        // Show the details too
        //
        SortedMap<String, Integer> skippedMdc = showDetailTable(skippedChangeSets, filterDenied, outputStream, shouldPrintDetailTable, showSummaryOutput, additionalChangeSetStatus, runChangeLogIterator);
        summaryDetails.getSummary().setSkipped(skippedMdc);
        try (MdcObject updateSummaryMdcObject = Scope.getCurrentScope().addMdcValue(MdcKey.UPDATE_SUMMARY, summaryDetails.getSummary())) {
            Scope.getCurrentScope().getLog(ShowSummaryUtil.class).info("Update summary generated");
        }
        return summaryDetails;
    }

    //
    // Show the details
    //
    private static SortedMap<String, Integer> showDetailTable(List<ChangeSet> skippedChangeSets,
                                                              List<ChangeSetStatus> filterDenied,
                                                              OutputStream outputStream,
                                                              boolean shouldPrintDetailTable,
                                                              UpdateSummaryOutputEnum showSummaryOutput,
                                                              List<ChangeSetStatus> additionalChangesets,
                                                              ChangeLogIterator runChangeLogIterator)
            throws IOException, LiquibaseException {
        String totalSkippedMdcKey = "totalSkipped";
        //
        // Nothing to do
        //
        if (filterDenied.isEmpty() && skippedChangeSets.isEmpty() && additionalChangesets.isEmpty()) {
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
        ShowSummaryGeneratorFactory showSummaryGeneratorFactory = Scope.getCurrentScope().getSingleton(ShowSummaryGeneratorFactory.class);
        ShowSummaryGenerator showSummaryGenerator = showSummaryGeneratorFactory.getShowSummaryGenerator();
        finalList.addAll(showSummaryGenerator.getAllAdditionalChangeSetStatus(runChangeLogIterator));

        finalList.sort(new Comparator<ChangeSetStatus>() {
            @Override
            public int compare(ChangeSetStatus o1, ChangeSetStatus o2) {
                ChangeSet c1 = o1.getChangeSet();
                ChangeSet c2 = o2.getChangeSet();
                int order1 = determineOrderInChangelog(c1);
                int order2 = determineOrderInChangelog(c2);
                return Integer.compare(order1, order2);
            }
        });

        //
        // Filtered because of labels or context
        //
        List<String> skippedMessages = new ArrayList<>();
        for (ChangeSetStatus st : finalList) {
            AtomicBoolean flag = new AtomicBoolean(true);
            StringBuilder builder = new StringBuilder();
            st.getFilterResults().forEach(consumer -> {
                if (consumer.getFilter() != null && !consumer.getFilter().isAssignableFrom(ShouldNotCountAsSkipChangesetFilter.class)) {
                    String displayName = consumer.getMdcName();
                    mdcSkipCounts.merge(displayName, 1, Integer::sum);
                }
                String skippedMessage = String.format("   '%s' : %s", st.getChangeSet().toString(), consumer.getMessage());
                skippedMessages.add(skippedMessage);
                if (!flag.get()) {
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
            switch (showSummaryOutput) {
                case CONSOLE:
                    printDetailTable(table, outputStream);
                    break;
                case LOG:
                    skippedMessages.forEach(ShowSummaryUtil::writeToLog);
                    break;
                default:
                    printDetailTable(table, outputStream);
                    skippedMessages.forEach(ShowSummaryUtil::writeToLog);
            }
        }
        return mdcSkipCounts;
    }


    /**
     * Internal use only filter.
     */
    public interface ShouldNotCountAsSkipChangesetFilter extends ChangeSetFilter {

    }

    private static void printDetailTable(List<List<String>> table, OutputStream outputStream) throws IOException, LiquibaseException {
        List<Integer> widths = new ArrayList<>();
        widths.add(60);
        widths.add(40);
        Writer writer = createOutputWriter(outputStream);
        TableOutput.formatOutput(table, widths, true, writer);
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
    private static UpdateSummaryDetails showSummary(DatabaseChangeLog changeLog,
                                                    StatusVisitor statusVisitor,
                                                    List<ChangeSet> skippedChangeSets,
                                                    List<ChangeSetStatus> filterDenied,
                                                    OutputStream outputStream,
                                                    UpdateSummaryOutputEnum showSummaryOutput,
                                                    ChangeLogIterator runChangeLogIterator) throws LiquibaseException {
        StringBuilder builder = new StringBuilder();
        builder.append(System.lineSeparator());
        int totalInChangelog = changeLog.getChangeSets().size() + skippedChangeSets.size();
        int skipped = skippedChangeSets.size();
        int filtered = filterDenied.size();
        ShowSummaryGeneratorFactory showSummaryGeneratorFactory = Scope.getCurrentScope().getSingleton(ShowSummaryGeneratorFactory.class);
        ShowSummaryGenerator showSummaryGenerator = showSummaryGeneratorFactory.getShowSummaryGenerator();
        int additional = showSummaryGenerator.getAllAdditionalChangeSetStatus(runChangeLogIterator).size();
        int totalAccepted = statusVisitor.getChangeSetsToRun().size() - additional;
        int totalPreviouslyRun = totalInChangelog - filtered - skipped - totalAccepted - additional;
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

        showSummaryGenerator.appendAdditionalSummaryMessages(builder, runChangeLogIterator);

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
                if (!result.isAccepted()) {
                    String displayName = result.getDisplayName();
                    filterSummaryMap.merge(displayName, 1, Integer::sum);
                }
            });
        });

        if (!filterSummaryMap.isEmpty()) {
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

        String outputMessage = builder.toString();
        writeMessage(outputMessage, showSummaryOutput, outputStream);
        UpdateSummaryDetails updateSummaryDetails = new UpdateSummaryDetails();
        updateSummaryDetails.setSummary(updateSummaryMdc);
        updateSummaryDetails.setOutput(outputMessage);
        return updateSummaryDetails;
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
