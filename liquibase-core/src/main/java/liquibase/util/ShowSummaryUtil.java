package liquibase.util;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.UpdateSummaryEnum;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.ChangeSetStatus;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.StatusVisitor;
import liquibase.exception.LiquibaseException;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 *
 * Methods to show a summary of change set counts after an update
 *
 */
public class ShowSummaryUtil {
    private static final Map<Class<?>, String> filterSummaryLabelMap = new HashMap<>();
    static {
        filterSummaryLabelMap.put(ShouldRunChangeSetFilter.class, "Already ran:             %6d");
        filterSummaryLabelMap.put(DbmsChangeSetFilter.class,      "DBMS mismatch:           %6d");
        filterSummaryLabelMap.put(LabelChangeSetFilter.class,     "Label mismatch:          %6d");
        filterSummaryLabelMap.put(ContextChangeSetFilter.class,   "Context mismatch:        %6d");
        filterSummaryLabelMap.put(CountChangeSetFilter.class,     "After count:             %6d");
        filterSummaryLabelMap.put(UpToTagChangeSetFilter.class,   "After tag:               %6d");
        filterSummaryLabelMap.put(IgnoreChangeSetFilter.class,    "Ignored:                 %6d");
    }

    /**
     *
     * Show a summary of the changesets which were executed
     *
     * @param   changeLog                          The changelog used in this update
     * @param   statusVisitor                      The StatusVisitor used to determine statuses
     * @param   outputStream                       The OutputStream to use for the summary
     * @throws  LiquibaseException                 Thrown by this method
     * @throws  IOException                        Thrown by this method
     *
     */
    public static void showUpdateSummary(DatabaseChangeLog changeLog, StatusVisitor statusVisitor, OutputStream outputStream)
            throws LiquibaseException, IOException {
        //
        // Check the global flag to turn the summary off
        //
        String showSummaryString = Scope.getCurrentScope().get("showSummary", String.class);
        UpdateSummaryEnum showSummary = showSummaryString != null ? UpdateSummaryEnum.valueOf(showSummaryString) : UpdateSummaryEnum.OFF;
        if (showSummary == UpdateSummaryEnum.OFF) {
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
        showSummary(changeLog, statusVisitor, skippedChangeSets, filterDenied, outputStream);
        if (showSummary == UpdateSummaryEnum.SUMMARY || (skippedChangeSets.isEmpty() && denied.isEmpty())) {
            return;
        }

        //
        // Show the details too
        //
        showDetailTable(skippedChangeSets, filterDenied, outputStream);
    }

    //
    // Show the details
    //
    private static void showDetailTable(List<ChangeSet> skippedChangeSets, List<ChangeSetStatus> filterDenied, OutputStream outputStream)
            throws IOException, LiquibaseException {
        //
        // Nothing to do
        //
        if (filterDenied.isEmpty() && skippedChangeSets.isEmpty()) {
            return;
        }
        List<String> columnHeaders = new ArrayList<>();
        columnHeaders.add("Changeset Info");
        columnHeaders.add("Reason Skipped");
        List<List<String>> table = new ArrayList<>();
        table.add(columnHeaders);

        List<ChangeSetStatus> finalList = createFinalStatusList(skippedChangeSets, filterDenied);

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

        List<Integer> widths = new ArrayList<>();
        widths.add(60);
        widths.add(40);

        Writer writer = createOutputWriter(outputStream);
        TableOutput.formatOutput(table, widths, true, writer);
    }

    //
    // Create a final list of changesets to be displayed
    //
    private static List<ChangeSetStatus> createFinalStatusList(List<ChangeSet> skippedChangeSets, List<ChangeSetStatus> filterDenied) {
        //
        // Add skipped during changelog parsing to the final list
        //
        List<ChangeSetStatus> finalList = new ArrayList<>(filterDenied);
        skippedChangeSets.forEach(skippedChangeSet -> {
            String dbmsList = String.format("'%s'", StringUtil.join(skippedChangeSet.getDbmsSet(), ", "));
            String mismatchMessage = String.format("mismatched DBMS value of %s", dbmsList);
            ChangeSetStatus changeSetStatus = new ChangeSetStatus(skippedChangeSet);
            ChangeSetFilterResult filterResult = new ChangeSetFilterResult(false, mismatchMessage, DbmsChangeSetFilter.class);
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
    private static void showSummary(DatabaseChangeLog changeLog,
                                    StatusVisitor statusVisitor,
                                    List<ChangeSet> skippedChangeSets,
                                    List<ChangeSetStatus> filterDenied,
                                    OutputStream outputStream) throws LiquibaseException {
        StringBuilder builder = new StringBuilder();
        builder.append(System.lineSeparator());
        int totalInChangelog = changeLog.getChangeSets().size() + skippedChangeSets.size();
        int skipped = skippedChangeSets.size();
        int filtered = filterDenied.size();
        int totalAccepted = statusVisitor.getChangeSetsToRun().size();
        int totalPreviouslyRun = totalInChangelog - filtered - skipped - totalAccepted;

        String message = "UPDATE SUMMARY";
        Scope.getCurrentScope().getLog(ShowSummaryUtil.class).info(message);
        builder.append(message);
        builder.append(System.lineSeparator());

        message = String.format("Run:                     %6d", totalAccepted);
        Scope.getCurrentScope().getLog(ShowSummaryUtil.class).info(message);
        builder.append(message);
        builder.append(System.lineSeparator());

        message = String.format("Previously run:          %6d", totalPreviouslyRun);
        Scope.getCurrentScope().getLog(ShowSummaryUtil.class).info(message);
        builder.append(message);
        builder.append(System.lineSeparator());

        message = String.format("Filtered out:            %6d", filtered + skipped);
        Scope.getCurrentScope().getLog(ShowSummaryUtil.class).info(message);
        builder.append(message);
        builder.append(System.lineSeparator());

        message = "-------------------------------";
        Scope.getCurrentScope().getLog(ShowSummaryUtil.class).info(message);
        builder.append(message);
        builder.append(System.lineSeparator());

        message = String.format("Total change sets:       %6d%n", totalInChangelog);
        Scope.getCurrentScope().getLog(ShowSummaryUtil.class).info(message);
        builder.append(message);
        builder.append(System.lineSeparator());

        final Map<Class<? extends ChangeSetFilter>, Integer> filterSummaryMap = new LinkedHashMap<>();
        List<ChangeSetStatus> finalList = createFinalStatusList(skippedChangeSets, filterDenied);
        finalList.forEach(status -> {
            status.getFilterResults().forEach(result -> {
                if (! result.isAccepted()) {
                    Class<? extends ChangeSetFilter> clazz = result.getFilter();
                    filterSummaryMap.computeIfAbsent(clazz, count -> {
                        return 0;
                    });
                    filterSummaryMap.put(clazz, filterSummaryMap.get(clazz)+1);
                }
            });
        });

        if (! filterSummaryMap.isEmpty()) {
            message = String.format("%nFILTERED CHANGE SETS SUMMARY%n");
            builder.append(message);
            builder.append(System.lineSeparator());
            Scope.getCurrentScope().getLog(ShowSummaryUtil.class).info(message);
            filterSummaryMap.forEach((filterClass, count) -> {
                String filterSummaryDetailMessage;
                String formatString = (filterSummaryLabelMap.containsKey(filterClass) ? String.valueOf(filterSummaryLabelMap.get(filterClass)) : null);
                if (formatString != null) {
                    filterSummaryDetailMessage = String.format(formatString, count);
                } else {
                    filterSummaryDetailMessage = String.format("%-18s       %6d",
                            filterClass.getSimpleName().replace("ChangeSetFilter", "Filter:"), count);
                }
                Scope.getCurrentScope().getLog(ShowSummaryUtil.class).info(filterSummaryDetailMessage);
                builder.append(filterSummaryDetailMessage);
                builder.append(System.lineSeparator());
            });
            builder.append(System.lineSeparator());
        }

        try {
            Writer writer = createOutputWriter(outputStream);
            writer.append(builder.toString());
            writer.flush();
        } catch (IOException ioe) {
            throw new LiquibaseException(ioe);
        }
    }

    //
    // Create a Writer to display the summary
    //
    private static Writer createOutputWriter(OutputStream outputStream) throws IOException {
        String charsetName = GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue();
        return new OutputStreamWriter(outputStream, charsetName);
    }
}
