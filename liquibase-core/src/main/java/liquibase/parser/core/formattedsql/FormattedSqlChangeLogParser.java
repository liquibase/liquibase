package liquibase.parser.core.formattedsql;

import liquibase.change.core.EmptyChange;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.logging.LogFactory;
import liquibase.parser.ChangeLogParser;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtils;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormattedSqlChangeLogParser implements ChangeLogParser {

    public boolean supports(String changeLogFile, ResourceAccessor resourceAccessor) {
        BufferedReader reader = null;
        try {
            if (changeLogFile.endsWith(".sql")) {
                reader = new BufferedReader(new InputStreamReader(openChangeLogFile(changeLogFile, resourceAccessor)));

                return reader.readLine().matches("\\-\\-\\s*liquibase formatted.*");
            } else {
                return false;
            }
        } catch (IOException e) {
            LogFactory.getLogger().debug("Exception reading " + changeLogFile, e);
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LogFactory.getLogger().debug("Exception closing " + changeLogFile, e);
                }
            }
        }
    }

    public int getPriority() {
        return PRIORITY_DEFAULT + 5;
    }

    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {

        DatabaseChangeLog changeLog = new DatabaseChangeLog();
        changeLog.setPhysicalFilePath(physicalChangeLogLocation);

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(openChangeLogFile(physicalChangeLogLocation, resourceAccessor)));
            StringBuffer currentSql = new StringBuffer();
            StringBuffer currentRollbackSql = new StringBuffer();

            ChangeSet changeSet = null;
            RawSQLChange change = null;
            Pattern changeSetPattern = Pattern.compile("\\-\\-[\\s]*changeset (\\S+):(\\S+).*", Pattern.CASE_INSENSITIVE);
            Pattern rollbackPattern = Pattern.compile("\\s*\\-\\-[\\s]*rollback (.*)", Pattern.CASE_INSENSITIVE);
            Pattern stripCommentsPattern = Pattern.compile(".*stripComments:(\\w+).*", Pattern.CASE_INSENSITIVE);
            Pattern splitStatementsPattern = Pattern.compile(".*splitStatements:(\\w+).*", Pattern.CASE_INSENSITIVE);
            Pattern endDelimiterPattern = Pattern.compile(".*endDelimiter:(\\w+).*", Pattern.CASE_INSENSITIVE);

            Pattern runOnChangePattern = Pattern.compile(".*runOnChange:(\\w+).*", Pattern.CASE_INSENSITIVE);
            Pattern runAlwaysPattern = Pattern.compile(".*runAlways:(\\w+).*", Pattern.CASE_INSENSITIVE);
            Pattern contextPattern = Pattern.compile(".*context:(\\S+).*", Pattern.CASE_INSENSITIVE);
            Pattern runInTransactionPattern = Pattern.compile(".*runInTransaction:(\\w+).*", Pattern.CASE_INSENSITIVE);
            Pattern dbmsPattern = Pattern.compile(".*dbms:(\\w+).*", Pattern.CASE_INSENSITIVE);
            Pattern failOnErrorPattern = Pattern.compile(".*failOnError:(\\w+).*", Pattern.CASE_INSENSITIVE);

            String line;
            while ((line = reader.readLine()) != null) {
                Matcher changeSetPatternMatcher = changeSetPattern.matcher(line);
                if (changeSetPatternMatcher.matches()) {
                    String finalCurrentSql = changeLogParameters.expandExpressions(StringUtils.trimToNull(currentSql.toString()));
                    if (changeSet != null) {

                        if (finalCurrentSql == null) {
                            throw new ChangeLogParseException("No SQL for changeset " + changeSet.toString(false));
                        }

                        change.setSql(finalCurrentSql);

                        if (StringUtils.trimToNull(currentRollbackSql.toString()) != null) {
                            try {
                                if (currentRollbackSql.toString().trim().toLowerCase().matches("^not required.*")) {
                                    changeSet.addRollbackChange(new EmptyChange());
                                } else {
                                    RawSQLChange rollbackChange = new RawSQLChange();
                                    rollbackChange.setSql(changeLogParameters.expandExpressions(currentRollbackSql.toString()));
                                    changeSet.addRollbackChange(rollbackChange);
                                }
                            } catch (UnsupportedChangeException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    Matcher stripCommentsPatternMatcher = stripCommentsPattern.matcher(line);
                    Matcher splitStatementsPatternMatcher = splitStatementsPattern.matcher(line);
                    Matcher endDelimiterPatternMatcher = endDelimiterPattern.matcher(line);

                    Matcher runOnChangePatternMatcher = runOnChangePattern.matcher(line);
                    Matcher runAlwaysPatternMatcher = runAlwaysPattern.matcher(line);
                    Matcher contextPatternMatcher = contextPattern.matcher(line);
                    Matcher runInTransactionPatternMatcher = runInTransactionPattern.matcher(line);
                    Matcher dbmsPatternMatcher = dbmsPattern.matcher(line);
                    Matcher failOnErrorPatternMatcher = failOnErrorPattern.matcher(line);

                    boolean stripComments = parseBoolean(stripCommentsPatternMatcher, changeSet, true);
                    boolean splitStatements = parseBoolean(splitStatementsPatternMatcher, changeSet, true);
                    boolean runOnChange = parseBoolean(runOnChangePatternMatcher, changeSet, false);
                    boolean runAlways = parseBoolean(runAlwaysPatternMatcher, changeSet, false);
                    boolean runInTransaction = parseBoolean(runInTransactionPatternMatcher, changeSet, true);
                    boolean failOnError = parseBoolean(failOnErrorPatternMatcher, changeSet, true);

                    String endDelimiter = parseString(endDelimiterPatternMatcher);
                    String context = parseString(contextPatternMatcher);
                    String dbms = parseString(dbmsPatternMatcher);

                    changeSet = new ChangeSet(changeSetPatternMatcher.group(2), changeSetPatternMatcher.group(1), runAlways, runOnChange, physicalChangeLogLocation, context, dbms, runInTransaction);
                    changeSet.setFailOnError(failOnError);
                    changeLog.addChangeSet(changeSet);

                    change = new RawSQLChange();
                    change.setSql(finalCurrentSql);
                    change.setResourceAccessor(resourceAccessor);
                    change.setSplitStatements(splitStatements);
                    change.setStripComments(stripComments);
                    change.setEndDelimiter(endDelimiter);
                    changeSet.addChange(change);

                    currentSql = new StringBuffer();
                    currentRollbackSql = new StringBuffer();
                } else {
                    if (changeSet != null) {
                        Matcher rollbackMatcher = rollbackPattern.matcher(line);
                        if (rollbackMatcher.matches()) {
                            if (rollbackMatcher.groupCount() == 1) {
                                currentRollbackSql.append(rollbackMatcher.group(1)).append("\n");
                            }
                        } else {
                            currentSql.append(line).append("\n");
                        }
                    }
                }
            }

            if (changeSet != null) {
                change.setSql(changeLogParameters.expandExpressions(StringUtils.trimToNull(currentSql.toString())));

                if (StringUtils.trimToNull(currentRollbackSql.toString()) != null) {
                    try {
                        if (currentRollbackSql.toString().trim().toLowerCase().matches("^not required.*")) {
                            changeSet.addRollbackChange(new EmptyChange());
                        } else {
                            RawSQLChange rollbackChange = new RawSQLChange();
                            rollbackChange.setSql(changeLogParameters.expandExpressions(currentRollbackSql.toString()));
                            changeSet.addRollbackChange(rollbackChange);
                        }
                    } catch (UnsupportedChangeException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        } catch (IOException e) {
            throw new ChangeLogParseException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) { }
            }
        }

        return changeLog;
    }

    private String parseString(Matcher matcher) {
        String endDelimiter = null;
        if (matcher.matches()) {
            endDelimiter = matcher.group(1);
        }
        return endDelimiter;
    }

    private boolean parseBoolean(Matcher matcher, ChangeSet changeSet, boolean defaultValue) throws ChangeLogParseException {
        boolean stripComments = defaultValue;
        if (matcher.matches()) {
            try {
                stripComments = Boolean.parseBoolean(matcher.group(1));
            } catch (Exception e) {
                throw new ChangeLogParseException("Cannot parse "+changeSet+" "+matcher.toString().replaceAll("\\.*","")+" as a boolean");
            }
        }
        return stripComments;
    }

    protected InputStream openChangeLogFile(String physicalChangeLogLocation, ResourceAccessor resourceAccessor) throws IOException {
        return resourceAccessor.getResourceAsStream(physicalChangeLogLocation);
    }
}
