package liquibase.util;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.UpdateSummaryEnum;
import liquibase.UpdateSummaryOutputEnum;
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.ChangeSetStatus;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.filter.ShouldRunChangeSetFilter;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.changelog.visitor.DefaultChangeExecListener;
import liquibase.changelog.visitor.StatusVisitor;
import liquibase.exception.LiquibaseException;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcObject;
import liquibase.logging.mdc.customobjects.UpdateSummary;
import liquibase.report.ShowSummaryGenerator;
import liquibase.report.ShowSummaryGeneratorFactory;
import lombok.Data;

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
        buildSummaryDetails(changeLog, showSummary, showSummaryOutput, statusVisitor, outputStream, runChangeLogIterator, null);
    }

    /**
     * Show a summary of the changesets which were executed AND return an object with the records of what has happened.
     *
     * @param changeLog          The changelog used in this update
     * @param showSummary        Flag to control whether we show the summary
     * @param showSummaryOutput  Flag to control where we show the summary
     * @param statusVisitor      The StatusVisitor used to determine statuses
     * @param outputStream       The OutputStream to use for the summary
     * @return the details of the update summary
     * @throws LiquibaseException Thrown by this method
     * @throws IOException        Thrown by this method
     * @deprecated use {@link ShowSummaryUtil#buildSummaryDetails(DatabaseChangeLog, UpdateSummaryEnum, UpdateSummaryOutputEnum, StatusVisitor, OutputStream, ChangeLogIterator, ChangeExecListener)} instead.
     */
    @Deprecated
    public static UpdateSummaryDetails buildSummaryDetails(DatabaseChangeLog changeLog, UpdateSummaryEnum showSummary, UpdateSummaryOutputEnum showSummaryOutput, StatusVisitor statusVisitor, OutputStream outputStream, ChangeLogIterator runChangeLogIterator) throws LiquibaseException, IOException {
        return buildSummaryDetails(changeLog, showSummary, showSummaryOutput, statusVisitor,outputStream, runChangeLogIterator, null);
    }

    /**
     * Show a summary of the changesets which were executed AND return an object with the records of what has happened.
     *
     * @param changeLog          The changelog used in this update
     * @param showSummary        Flag to control whether we show the summary
     * @param showSummaryOutput  Flag to control where we show the summary
     * @param statusVisitor      The StatusVisitor used to determine statuses
     * @param outputStream       The OutputStream to use for the summary
     * @param changeExecListener
     * @return the details of the update summary
     * @throws LiquibaseException Thrown by this method
     * @throws IOException        Thrown by this method
     */
    public static UpdateSummaryDetails buildSummaryDetails(DatabaseChangeLog changeLog, UpdateSummaryEnum showSummary, UpdateSummaryOutputEnum showSummaryOutput, StatusVisitor statusVisitor, OutputStream outputStream, ChangeLogIterator runChangeLogIterator, ChangeExecListener changeExecListener)
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
        UpdateSummaryDetails summaryDetails = showSummary(changeLog, statusVisitor, skippedChangeSets, filterDenied, outputStream, showSummaryOutput, runChangeLogIterator, changeExecListener);
        summaryDetails.getSummary().setValue(showSummary.toString());
        boolean shouldPrintDetailTable = showSummary != UpdateSummaryEnum.SUMMARY && (!skippedChangeSets.isEmpty() || !denied.isEmpty() || !additionalChangeSetStatus.isEmpty());

        // Show the details too
        FilteredChanges filteredChanges = showDetailTable(skippedChangeSets, filterDenied, outputStream, shouldPrintDetailTable, showSummaryOutput, additionalChangeSetStatus, runChangeLogIterator);
        summaryDetails.getSummary().setSkipped(filteredChanges.getMdcSkipCounts());
        summaryDetails.setSkipped(filteredChanges.getSkippedChangesetsMessage());
        try (MdcObject updateSummaryMdcObject = Scope.getCurrentScope().addMdcValue(MdcKey.UPDATE_SUMMARY, summaryDetails.getSummary())) {
            Scope.getCurrentScope().getLog(ShowSummaryUtil.class).info("Update summary generated");
        }
        return summaryDetails;
    }

    //
    // Show the details
    //
    private static FilteredChanges showDetailTable(List<ChangeSet> skippedChangeSets,
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
            return new FilteredChanges(new TreeMap<>(Collections.singletonMap(totalSkippedMdcKey, 0)), new LinkedHashMap<>());
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

        finalList.sort((o1, o2) -> {
            ChangeSet c1 = o1.getChangeSet();
            ChangeSet c2 = o2.getChangeSet();
            int order1 = determineOrderInChangelog(c1);
            int order2 = determineOrderInChangelog(c2);
            return Integer.compare(order1, order2);
        });

        // Filtered because of labels or context
        Map<ChangeSet, String> filteredChangesets = new LinkedHashMap<>();
        List<String> skippedMessages = new ArrayList<>();
        for (ChangeSetStatus changeSetStatus : finalList) {
            AtomicBoolean flag = new AtomicBoolean(true);
            StringBuilder builder = new StringBuilder();
            changeSetStatus.getFilterResults().forEach(filterResult -> {
                if (filterResult.getFilter() != null && !filterResult.getFilter().isAssignableFrom(ShouldNotCountAsSkipChangesetFilter.class)) {
                    String displayName = filterResult.getMdcName();
                    mdcSkipCounts.merge(displayName, 1, Integer::sum);
                }
                String skippedMessage = String.format("   '%s' : %s", changeSetStatus.getChangeSet().toString(), filterResult.getMessage());
                skippedMessages.add(skippedMessage);
                if (!flag.get()) {
                    builder.append(System.lineSeparator());
                }
                builder.append(filterResult.getMessage());
                flag.set(false);
            });
            List<String> outputRow = new ArrayList<>();
            outputRow.add(changeSetStatus.getChangeSet().toString());
            outputRow.add(builder.toString());
            filteredChangesets.put(changeSetStatus.getChangeSet(), builder.toString());
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
        return new FilteredChanges(mdcSkipCounts, filteredChangesets);
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
                                                    ChangeLogIterator runChangeLogIterator,
                                                    ChangeExecListener changeExecListener) throws LiquibaseException {
        StringBuilder builder = new StringBuilder();
        builder.append(System.lineSeparator());
        int skipped = skippedChangeSets.size();
        int filtered = filterDenied.size();
        int totalAccepted = calculateAccepted(statusVisitor, changeExecListener);
        int totalPreviouslyRun = calculatePreviouslyRun(statusVisitor);
        int totalInChangelog = CollectionUtil.createIfNull(changeLog.getChangeSets()).size() + CollectionUtil.createIfNull(changeLog.getSkippedChangeSets()).size();
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

        ShowSummaryGeneratorFactory showSummaryGeneratorFactory = Scope.getCurrentScope().getSingleton(ShowSummaryGeneratorFactory.class);
        ShowSummaryGenerator showSummaryGenerator = showSummaryGeneratorFactory.getShowSummaryGenerator();
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

    private static int calculatePreviouslyRun(StatusVisitor statusVisitor) {
        return (int) statusVisitor.getStatuses().stream().filter(
                s -> s.getFilterResults().stream().anyMatch(
                        fr -> fr.getFilter().isAssignableFrom(ShouldRunChangeSetFilter.class) && !fr.isAccepted() && fr.getMessage().equals(ShouldRunChangeSetFilter.CHANGESET_ALREADY_RAN_MESSAGE)
                )
        ).count();
    }

    /**
     * Calculate the accepted number of changesets.
     * The status visitor provides a list of changesets that are expected to execute, not the actual list of
     * executed changesets. We retain this code despite its inaccuracy for backwards compatibility, in case
     * a change exec listener is not provided.
     */
    private static int calculateAccepted(StatusVisitor statusVisitor, ChangeExecListener changeExecListener) {
        int ran = statusVisitor.getChangeSetsToRun().size();
        if (changeExecListener instanceof DefaultChangeExecListener) {
            ran = ((DefaultChangeExecListener) changeExecListener).getDeployedChangeSets().size();
        }
        return ran;
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

    @Data
    private static class FilteredChanges {
        private final SortedMap<String, Integer> mdcSkipCounts;
        private final Map<ChangeSet, String> skippedChangesetsMessage;
    }
}
