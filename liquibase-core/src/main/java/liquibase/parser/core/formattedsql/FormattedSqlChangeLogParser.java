package liquibase.parser.core.formattedsql;

import liquibase.Labels;
import liquibase.Scope;
import liquibase.change.Change;
import liquibase.change.core.EmptyChange;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.ChangeLogParser;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.precondition.core.SqlPrecondition;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("java:S2583")
public class FormattedSqlChangeLogParser implements ChangeLogParser {


    @Override
    public boolean supports(String changeLogFile, ResourceAccessor resourceAccessor) {
        BufferedReader reader = null;
        try {
            if (supportsExtension(changeLogFile)) {
                InputStream fileStream = openChangeLogFile(changeLogFile, resourceAccessor);
                if (fileStream == null) {
                    return false;
                }
                reader = new BufferedReader(StreamUtil.readStreamWithReader(fileStream, null));

                String firstLine = reader.readLine();

                while (firstLine.trim().isEmpty() && reader.ready()) {
                    firstLine = reader.readLine();
                }
                Pattern firstLinePattern = Pattern.compile("\\-\\-\\s*liquibase formatted.*", Pattern.CASE_INSENSITIVE);
                return (firstLine != null) && firstLinePattern.matcher(firstLine).matches();
            } else {
                return false;
            }
        } catch (IOException e) {
            Scope.getCurrentScope().getLog(getClass()).fine("Exception reading " + changeLogFile, e);
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Scope.getCurrentScope().getLog(getClass()).fine("Exception closing " + changeLogFile, e);
                }
            }
        }
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT + 5;
    }

    @Override
    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {

        DatabaseChangeLog changeLog = new DatabaseChangeLog();
        changeLog.setChangeLogParameters(changeLogParameters);

        changeLog.setPhysicalFilePath(physicalChangeLogLocation);

        try (BufferedReader reader = new BufferedReader(StreamUtil.readStreamWithReader(openChangeLogFile(physicalChangeLogLocation, resourceAccessor), null))) {
            StringBuilder currentSql = new StringBuilder();
            StringBuilder currentRollbackSql = new StringBuilder();

            ChangeSet changeSet = null;
            RawSQLChange change = null;
            Pattern changeLogPattern = Pattern.compile("\\-\\-\\s*liquibase formatted.*", Pattern.CASE_INSENSITIVE);
            Pattern propertyPattern = Pattern.compile("\\s*\\-\\-[\\s]*property\\s+(.*:.*)\\s+(.*:.*).*", Pattern.CASE_INSENSITIVE);
            Pattern altPropertyOneDashPattern = Pattern.compile("\\s*?[-]+\\s*property\\s.*", Pattern.CASE_INSENSITIVE);
            Pattern changeSetPattern = Pattern.compile("\\s*\\-\\-[\\s]*changeset\\s+(\"[^\"]+\"|[^:]+):\\s*(\"[^\"]+\"|\\S+).*", Pattern.CASE_INSENSITIVE);
            Pattern altChangeSetOneDashPattern = Pattern.compile("\\-[\\s]*changeset\\s.*", Pattern.CASE_INSENSITIVE);
            Pattern altChangeSetNoOtherInfoPattern = Pattern.compile("\\s*\\-\\-[\\s]*changeset[\\s]*.*$", Pattern.CASE_INSENSITIVE);
            Pattern rollbackPattern = Pattern.compile("\\s*\\-\\-[\\s]*rollback (.*)", Pattern.CASE_INSENSITIVE);
            Pattern altRollbackOneDashPattern = Pattern.compile("\\s*\\-[\\s]*rollback\\s.*", Pattern.CASE_INSENSITIVE);
            Pattern preconditionsPattern = Pattern.compile("\\s*\\-\\-[\\s]*preconditions(.*)", Pattern.CASE_INSENSITIVE);
            Pattern altPreconditionsOneDashPattern = Pattern.compile("\\s*\\-[\\s]*preconditions\\s.*", Pattern.CASE_INSENSITIVE);
            Pattern preconditionPattern = Pattern.compile("\\s*\\-\\-[\\s]*precondition\\-([a-zA-Z0-9-]+) (.*)", Pattern.CASE_INSENSITIVE);
            Pattern altPreconditionOneDashPattern = Pattern.compile("\\s*\\-[\\s]*precondition(.*)", Pattern.CASE_INSENSITIVE);

            Pattern stripCommentsPattern = Pattern.compile(".*stripComments:(\\w+).*", Pattern.CASE_INSENSITIVE);
            Pattern splitStatementsPattern = Pattern.compile(".*splitStatements:(\\w+).*", Pattern.CASE_INSENSITIVE);
            Pattern rollbackSplitStatementsPattern = Pattern.compile(".*rollbackSplitStatements:(\\w+).*", Pattern.CASE_INSENSITIVE);
            Pattern endDelimiterPattern = Pattern.compile(".*endDelimiter:(\\S*).*", Pattern.CASE_INSENSITIVE);
            Pattern rollbackEndDelimiterPattern = Pattern.compile(".*rollbackEndDelimiter:(\\S*).*", Pattern.CASE_INSENSITIVE);
            Pattern commentPattern = Pattern.compile("\\-\\-[\\s]*comment:? (.*)", Pattern.CASE_INSENSITIVE);
            Pattern altCommentPluralPattern = Pattern.compile("\\-\\-[\\s]*comments:? (.*)", Pattern.CASE_INSENSITIVE);
            Pattern altCommentOneDashPattern = Pattern.compile("\\-[\\s]*comment:? (.*)", Pattern.CASE_INSENSITIVE);
            Pattern validCheckSumPattern = Pattern.compile("\\-\\-[\\s]*validCheckSum:? (.*)", Pattern.CASE_INSENSITIVE);
            Pattern altValidCheckSumOneDashPattern = Pattern.compile("^\\-[\\s]*validCheckSum(.*)$", Pattern.CASE_INSENSITIVE);
            Pattern ignoreLinesPattern = Pattern.compile("\\-\\-[\\s]*ignoreLines:(\\w+)", Pattern.CASE_INSENSITIVE);
            Pattern altIgnoreLinesOneDashPattern = Pattern.compile("\\-[\\s]*?ignoreLines:(\\w+).*$", Pattern.CASE_INSENSITIVE);
            Pattern altIgnorePattern = Pattern.compile("\\-\\-[\\s]*ignore:(\\w+)", Pattern.CASE_INSENSITIVE);
            Pattern runWithPattern = Pattern.compile(".*runWith:([\\w\\$\\{\\}]+).*", Pattern.CASE_INSENSITIVE);

            Pattern runOnChangePattern = Pattern.compile(".*runOnChange:(\\w+).*", Pattern.CASE_INSENSITIVE);
            Pattern runAlwaysPattern = Pattern.compile(".*runAlways:(\\w+).*", Pattern.CASE_INSENSITIVE);
            Pattern contextPattern = Pattern.compile(".*context:(\".*\"|\\S*).*", Pattern.CASE_INSENSITIVE);
            Pattern contextFilterPattern = Pattern.compile(".*contextFilter:(\".*\"|\\S*).*", Pattern.CASE_INSENSITIVE);
            Pattern logicalFilePathPattern = Pattern.compile(".*logicalFilePath:(\\S*).*", Pattern.CASE_INSENSITIVE);
            Pattern changeLogIdPattern = Pattern.compile(".*changeLogId:(\\S*).*", Pattern.CASE_INSENSITIVE);
            Pattern labelsPattern = Pattern.compile(".*labels:(\\S*).*", Pattern.CASE_INSENSITIVE);
            Pattern runInTransactionPattern = Pattern.compile(".*runInTransaction:(\\w+).*", Pattern.CASE_INSENSITIVE);
            Pattern dbmsPattern = Pattern.compile(".*dbms:([^,][\\w!,]+).*", Pattern.CASE_INSENSITIVE);
            Pattern failOnErrorPattern = Pattern.compile(".*failOnError:(\\w+).*", Pattern.CASE_INSENSITIVE);
            Pattern onFailPattern = Pattern.compile(".*onFail:(\\w+).*", Pattern.CASE_INSENSITIVE);
            Pattern onErrorPattern = Pattern.compile(".*onError:(\\w+).*", Pattern.CASE_INSENSITIVE);
            Pattern onUpdateSqlPattern = Pattern.compile(".*onUpdateSQL:(\\w+).*", Pattern.CASE_INSENSITIVE);
            Pattern onSqlOutputPattern = Pattern.compile(".*onSqlOutput:(\\w+).*", Pattern.CASE_INSENSITIVE);

            Pattern rollbackChangeSetIdPattern = Pattern.compile(".*changeSetId:(\\S+).*", Pattern.CASE_INSENSITIVE);
            Pattern rollbackChangeSetAuthorPattern = Pattern.compile(".*changesetAuthor:(\\S+).*", Pattern.CASE_INSENSITIVE);
            Pattern rollbackChangeSetPathPattern = Pattern.compile(".*changesetPath:(\\S+).*", Pattern.CASE_INSENSITIVE);

            Pattern rollbackMultiLineStartPattern = Pattern.compile("\\s*\\/\\*\\s*liquibase\\s*rollback\\s*$", Pattern.CASE_INSENSITIVE);

            Matcher rollbackSplitStatementsPatternMatcher = null;
            boolean rollbackSplitStatements = true;
            String rollbackEndDelimiter = null;

            int count = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                count++;
                Matcher commentMatcher = commentPattern.matcher(line);
                Matcher propertyPatternMatcher = propertyPattern.matcher(line);
                Matcher altPropertyPatternMatcher = altPropertyOneDashPattern.matcher(line);
                if (propertyPatternMatcher.matches()) {
                    handleProperty(changeLogParameters, changeLog, propertyPatternMatcher);
                    continue;
                } else if (altPropertyPatternMatcher.matches()) {
                    String message = String.format("Unexpected formatting at line %d. Formatted SQL changelogs require known formats, such as '--property name=<property name> value=<property value>' and others to be recognized and run. Learn all the options at https://docs.liquibase.com/concepts/changelogs/sql-format.html", count);
                    throw new ChangeLogParseException("\n" + message);
                }
                Matcher changeLogPatterMatcher = changeLogPattern.matcher (line);
                if (changeLogPatterMatcher.matches ()) {
                    Matcher logicalFilePathMatcher = logicalFilePathPattern.matcher (line);
                    changeLog.setLogicalFilePath (parseString(logicalFilePathMatcher));

                    Matcher changeLogIdMatcher = changeLogIdPattern.matcher (line);
                    changeLog.setChangeLogId (parseString(changeLogIdMatcher));
                }

                Matcher ignoreLinesMatcher = ignoreLinesPattern.matcher(line);
                Matcher altIgnoreMatcher = altIgnorePattern.matcher(line);
                Matcher altIgnoreLinesOneDashMatcher = altIgnoreLinesOneDashPattern.matcher(line);
                if (ignoreLinesMatcher.matches ()) {
                    if ("start".equals(ignoreLinesMatcher.group(1))){
                        while ((line = reader.readLine()) != null){
                            altIgnoreLinesOneDashMatcher = altIgnoreLinesOneDashPattern.matcher(line);
                            count++;
                            ignoreLinesMatcher = ignoreLinesPattern.matcher(line);
                            if (ignoreLinesMatcher.matches()) {
                                if ("end".equals(ignoreLinesMatcher.group(1))) {
                                    break;
                                }
                            } else if (altIgnoreLinesOneDashMatcher.matches()) {
                                String message =
                                   String.format("Unexpected formatting at line %d. Formatted SQL changelogs require known formats, such as '--ignoreLines:end' and others to be recognized and run. Learn all the options at https://docs.liquibase.com/concepts/changelogs/sql-format.html", count);
                                throw new ChangeLogParseException("\n" + message);
                            }
                        }
                        continue;
                    } else {
                        try {
                            long ignoreCount = Long.parseLong(ignoreLinesMatcher.group(1));
                            while (ignoreCount > 0 && (line = reader.readLine()) != null) {
                                ignoreCount--;
                                count++;
                            }
                            continue;
                        } catch (NumberFormatException | NullPointerException nfe) {
                            throw new ChangeLogParseException("Unknown ignoreLines syntax");
                        }
                    }
                } else if (altIgnoreLinesOneDashMatcher.matches() || altIgnoreMatcher.matches()) {
                    String message =
                       String.format("Unexpected formatting at line %d. Formatted SQL changelogs require known formats, such as '--ignoreLines:<count|start>' and others to be recognized and run. Learn all the options at https://docs.liquibase.com/concepts/changelogs/sql-format.html", count);
                    throw new ChangeLogParseException("\n" + message);
                }

                Matcher changeSetPatternMatcher = changeSetPattern.matcher(line);
                if (changeSetPatternMatcher.matches()) {
                    String finalCurrentSql = changeLogParameters.expandExpressions(StringUtil.trimToNull(currentSql.toString()), changeLog);
                    if (changeSet != null) {
                        if (finalCurrentSql == null) {
                            throw new ChangeLogParseException("No SQL for changeset " + changeSet.toString(false));
                        }

                        change.setSql(finalCurrentSql);

                        String currentRollBackSqlAsString = currentRollbackSql.toString();
                        if (StringUtil.trimToNull(currentRollBackSqlAsString) != null) {
                            if (currentRollBackSqlAsString.trim().toLowerCase().matches("^not required.*")) {
                                changeSet.addRollbackChange(new EmptyChange());
                            } else if (currentRollBackSqlAsString.trim().toLowerCase().contains("changesetid")) {
                                String rollbackString = currentRollBackSqlAsString.replace("\n", "").replace("\r", "");
                                Matcher authorMatcher = rollbackChangeSetAuthorPattern.matcher(rollbackString);
                                Matcher idMatcher = rollbackChangeSetIdPattern.matcher(rollbackString);
                                Matcher pathMatcher = rollbackChangeSetPathPattern.matcher(rollbackString);

                                String changeSetAuthor = StringUtil.trimToNull(parseString(authorMatcher));
                                String changeSetId = StringUtil.trimToNull(parseString(idMatcher));
                                String changeSetPath = StringUtil.trimToNull(parseString(pathMatcher));

                                if (changeSetId == null) {
                                    throw new ChangeLogParseException("'changesetId' not set in rollback block '"+rollbackString+"'");
                                }

                                if (changeSetAuthor == null) {
                                    throw new ChangeLogParseException("'changesetAuthor' not set in rollback block '"+rollbackString+"'");
                                }

                                if (changeSetPath == null) {
                                    changeSetPath = physicalChangeLogLocation;
                                }

                                ChangeSet rollbackChangeSet = changeLog.getChangeSet(changeSetPath, changeSetAuthor, changeSetId);
                                DatabaseChangeLog parent = changeLog;
                                while ((rollbackChangeSet == null) && (parent != null)) {
                                    parent = parent.getParentChangeLog();
                                    if (parent != null) {
                                        rollbackChangeSet = parent.getChangeSet(changeSetPath, changeSetAuthor, changeSetId);
                                    }
                                }

                                if (rollbackChangeSet == null) {
                                    throw new ChangeLogParseException("Change set " + new ChangeSet(changeSetId, changeSetAuthor, false, false, changeSetPath, null, null, null).toString(false) + " does not exist");
                                }
                                for (Change rollbackChange : rollbackChangeSet.getChanges()) {
                                    changeSet.addRollbackChange(rollbackChange);
                                }
                            } else {
                                RawSQLChange rollbackChange = new RawSQLChange();
                                rollbackChange.setSql(changeLogParameters.expandExpressions(currentRollbackSql.toString(), changeSet.getChangeLog()));
                                if (rollbackSplitStatementsPatternMatcher.matches()) {
                                    rollbackChange.setSplitStatements(rollbackSplitStatements);
                                }
                                if (rollbackEndDelimiter != null) {
                                    rollbackChange.setEndDelimiter(rollbackEndDelimiter);
                                }
                                changeSet.addRollbackChange(rollbackChange);
                            }
                        }
                    }

                    Matcher stripCommentsPatternMatcher = stripCommentsPattern.matcher(line);
                    Matcher splitStatementsPatternMatcher = splitStatementsPattern.matcher(line);
                    Matcher runWithMatcher = runWithPattern.matcher(line);
                    rollbackSplitStatementsPatternMatcher = rollbackSplitStatementsPattern.matcher(line);
                    Matcher endDelimiterPatternMatcher = endDelimiterPattern.matcher(line);
                    Matcher rollbackEndDelimiterPatternMatcher = rollbackEndDelimiterPattern.matcher(line);

                    Matcher logicalFilePathMatcher = logicalFilePathPattern.matcher(line);
                    Matcher runOnChangePatternMatcher = runOnChangePattern.matcher(line);
                    Matcher runAlwaysPatternMatcher = runAlwaysPattern.matcher(line);
                    Matcher contextPatternMatcher = contextPattern.matcher(line);
                    Matcher contextFilterPatternMatcher = contextFilterPattern.matcher(line);
                    Matcher labelsPatternMatcher = labelsPattern.matcher(line);
                    Matcher runInTransactionPatternMatcher = runInTransactionPattern.matcher(line);
                    Matcher dbmsPatternMatcher = dbmsPattern.matcher(line);
                    Matcher failOnErrorPatternMatcher = failOnErrorPattern.matcher(line);

                    boolean stripComments = parseBoolean(stripCommentsPatternMatcher, changeSet, true);
                    boolean splitStatements = parseBoolean(splitStatementsPatternMatcher, changeSet, true);
                    rollbackSplitStatements = parseBoolean(rollbackSplitStatementsPatternMatcher, changeSet, true);
                    boolean runOnChange = parseBoolean(runOnChangePatternMatcher, changeSet, false);
                    boolean runAlways = parseBoolean(runAlwaysPatternMatcher, changeSet, false);
                    boolean runInTransaction = parseBoolean(runInTransactionPatternMatcher, changeSet, true);
                    boolean failOnError = parseBoolean(failOnErrorPatternMatcher, changeSet, true);

                    String runWith = parseString(runWithMatcher);
                    if (runWith != null) {
                        runWith = changeLogParameters.expandExpressions(runWith, changeLog);
                    }
                    String endDelimiter = parseString(endDelimiterPatternMatcher);
                    rollbackEndDelimiter = parseString(rollbackEndDelimiterPatternMatcher);
                    String context = StringUtil.trimToNull(
                        StringUtil.trimToEmpty(parseString(contextFilterPatternMatcher)).replaceFirst("^\"", "").replaceFirst("\"$", "") //remove surrounding quotes if they're in there
                    );
                    if (context == null) {
                        context = StringUtil.trimToNull(
                                StringUtil.trimToEmpty(parseString(contextPatternMatcher)).replaceFirst("^\"", "").replaceFirst("\"$", "") //remove surrounding quotes if they're in there
                        );
                    }

                    if (context != null) {
                        context = changeLogParameters.expandExpressions(context, changeLog);
                    }
                    String labels = parseString(labelsPatternMatcher);
                    if (labels != null) {
                        labels = changeLogParameters.expandExpressions(labels, changeLog);
                    }
                    String logicalFilePath = parseString(logicalFilePathMatcher);
                    if ((logicalFilePath == null) || "".equals(logicalFilePath)) {
                       logicalFilePath = changeLog.getLogicalFilePath();
                    }
                    if (logicalFilePath != null) {
                        logicalFilePath = changeLogParameters.expandExpressions(logicalFilePath, changeLog);
                    }
                    String dbms = parseString(dbmsPatternMatcher);
                    if (dbms != null) {
                        dbms = changeLogParameters.expandExpressions(dbms, changeLog);
                    }

                    //
                    // Make sure that this line matches the --changeset <author>:<id> with no spaces before ID
                    //
                    String idGroup = changeSetPatternMatcher.group(2);
                    String authorGroup = changeSetPatternMatcher.group(1);

                    //
                    // Use Pattern.Quote to escape the meta-characters
                    // <([{\^-=$!|]})?*+.>
                    //
                    Pattern changeSetAuthorIdPattern =
                            Pattern.compile("\\s*\\-\\-[\\s]*changeset\\s+" + Pattern.quote(authorGroup+ ":" + idGroup) + ".*$", Pattern.CASE_INSENSITIVE);
                    Matcher changeSetAuthorIdPatternMatcher = changeSetAuthorIdPattern.matcher(line);
                    if (! changeSetAuthorIdPatternMatcher.matches()) {
                        String message =
                                String.format("Unexpected formatting at line %d. Formatted SQL changelogs require known formats, such as '--changeset <authorname>:<changesetId>' and others to be recognized and run. Learn all the options at https://docs.liquibase.com/concepts/changelogs/sql-format.html", count);
                        throw new ChangeLogParseException("\n" + message);
                    }

                    String changeSetId =
                        changeLogParameters.expandExpressions(StringUtil.stripEnclosingQuotes(idGroup), changeLog);
                    String changeSetAuthor =
                        changeLogParameters.expandExpressions(StringUtil.stripEnclosingQuotes(authorGroup), changeLog);

                    changeSet =
                       new ChangeSet(changeSetId, changeSetAuthor, runAlways, runOnChange, logicalFilePath, context, dbms, runWith, runInTransaction, changeLog.getObjectQuotingStrategy(), changeLog);
                    changeSet.setLabels(new Labels(labels));
                    changeSet.setFailOnError(failOnError);
                    changeLog.addChangeSet(changeSet);

                    change = new RawSQLChange();
                    change.setSql(finalCurrentSql);
                    if (splitStatementsPatternMatcher.matches()) {
                        change.setSplitStatements(splitStatements);
                    }
                    change.setStripComments(stripComments);
                    change.setEndDelimiter(endDelimiter);
                    changeSet.addChange(change);

                    currentSql.setLength(0);
                    currentRollbackSql.setLength(0);
                } else {
                    Matcher altChangeSetOneDashPatternMatcher = altChangeSetOneDashPattern.matcher(line);
                    Matcher altChangeSetNoOtherInfoPatternMatcher = altChangeSetNoOtherInfoPattern.matcher(line);
                    if (altChangeSetOneDashPatternMatcher.matches() || altChangeSetNoOtherInfoPatternMatcher.matches()) {
                        String message = String.format("Unexpected formatting at line %d. Formatted SQL changelogs require known formats, such as '--changeset <authorname>:<changesetId>' and others to be recognized and run. Learn all the options at https://docs.liquibase.com/concepts/changelogs/sql-format.html", count);
                        throw new ChangeLogParseException("\n" + message);
                    }
                    if (changeSet != null) {
                        Matcher altCommentOneDashMatcher = altCommentOneDashPattern.matcher(line);
                        Matcher altCommentPluralMatcher = altCommentPluralPattern.matcher(line);
                        Matcher rollbackMatcher = rollbackPattern.matcher(line);
                        Matcher altRollbackMatcher = altRollbackOneDashPattern.matcher(line);
                        Matcher preconditionsMatcher = preconditionsPattern.matcher(line);
                        Matcher altPreconditionsOneDashMatcher = altPreconditionsOneDashPattern.matcher(line);
                        Matcher preconditionMatcher = preconditionPattern.matcher(line);
                        Matcher altPreconditionOneDashMatcher = altPreconditionOneDashPattern.matcher(line);
                        Matcher validCheckSumMatcher = validCheckSumPattern.matcher(line);
                        Matcher altValidCheckSumOneDashMatcher = altValidCheckSumOneDashPattern.matcher(line);
                        Matcher rollbackMultiLineStartMatcher = rollbackMultiLineStartPattern.matcher(line);

                        if (commentMatcher.matches()) {
                            if (commentMatcher.groupCount() == 0) {
                                String message = String.format("Unexpected formatting at line %d. Formatted SQL changelogs require known formats, such as '--comment <comment>' and others to be recognized and run. Learn all the options at https://docs.liquibase.com/concepts/changelogs/sql-format.html", count);
                                throw new ChangeLogParseException("\n" + message);
                            }
                            if (commentMatcher.groupCount() == 1) {
                                changeSet.setComments(commentMatcher.group(1));
                            }
                        } else if (altCommentOneDashMatcher.matches() || altCommentPluralMatcher.matches()) {
                            String message = String.format("Unexpected formatting at line %d. Formatted SQL changelogs require known formats, such as '--comment <comment>' and others to be recognized and run. Learn all the options at https://docs.liquibase.com/concepts/changelogs/sql-format.html", count);
                            throw new ChangeLogParseException("\n" + message);
                        } else if (validCheckSumMatcher.matches()) {
                            if (validCheckSumMatcher.groupCount() == 0) {
                                String message = String.format("Unexpected formatting at line %d. Formatted SQL changelogs require known formats, such as '--rollback <rollback SQL>' and others to be recognized and run. Learn all the options at https://docs.liquibase.com/concepts/changelogs/sql-format.html", count);
                                throw new ChangeLogParseException("\n" + message);
                            } else if (validCheckSumMatcher.groupCount() == 1) {
                                changeSet.addValidCheckSum(validCheckSumMatcher.group(1));
                            }
                        } else if (altValidCheckSumOneDashMatcher.matches()) {
                            String message = String.format("Unexpected formatting at line %d. Formatted SQL changelogs require known formats, such as '--validChecksum <checksum>' and others to be recognized and run. Learn all the options at https://docs.liquibase.com/concepts/changelogs/sql-format.html", count);
                            throw new ChangeLogParseException("\n" + message);
                        } else if (rollbackMatcher.matches()) {
                            if (rollbackMatcher.groupCount() == 0) {
                                String message = String.format("Unexpected formatting at line %d. Formatted SQL changelogs require known formats, such as '--rollback <rollback SQL>' and others to be recognized and run. Learn all the options at https://docs.liquibase.com/concepts/changelogs/sql-format.html", count);
                                throw new ChangeLogParseException("\n" + message);
                            }
                            currentRollbackSql.append(rollbackMatcher.group(1)).append(System.lineSeparator());
                        } else if (altRollbackMatcher.matches()) {
                            String message = String.format("Unexpected formatting at line %d. Formatted SQL changelogs require known formats, such as '--rollback <rollback SQL>' and others to be recognized and run. Learn all the options at https://docs.liquibase.com/concepts/changelogs/sql-format.html", count);
                            throw new ChangeLogParseException("\n" + message);
                        } else if (rollbackMultiLineStartMatcher.matches()) {
                            if (rollbackMultiLineStartMatcher.groupCount() == 0) {
                                currentRollbackSql.append(extractMultiLineRollBackSQL(reader));
                            }
                        } else if (preconditionsMatcher.matches()) {
                            if (preconditionsMatcher.groupCount() == 0) {
                                String message = String.format("Unexpected formatting at line %d. Formatted SQL changelogs require known formats, such as '--preconditions <onFail>|<onError>|<onUpdate>' and others to be recognized and run. Learn all the options at https://docs.liquibase.com/concepts/changelogs/sql-format.html", count);
                                throw new ChangeLogParseException("\n" + message);
                            }
                            if (preconditionsMatcher.groupCount() == 1) {
                                String body = preconditionsMatcher.group(1);
                                Matcher onFailMatcher = onFailPattern.matcher(body);
                                Matcher onErrorMatcher = onErrorPattern.matcher(body);
                                Matcher onUpdateSqlMatcher = onUpdateSqlPattern.matcher(body);
                                Matcher onSqlOutputMatcher = onSqlOutputPattern.matcher(body);

                                PreconditionContainer pc = new PreconditionContainer();
                                pc.setOnFail(StringUtil.trimToNull(parseString(onFailMatcher)));
                                pc.setOnError(StringUtil.trimToNull(parseString(onErrorMatcher)));

                                if (onSqlOutputMatcher.matches() && onUpdateSqlMatcher.matches()) {
                                    throw new IllegalArgumentException("Please modify the changelog to have preconditions set with either " +
                                            "'onUpdateSql' or 'onSqlOutput', and not with both.");
                                }
                                if (onSqlOutputMatcher.matches()) {
                                    pc.setOnSqlOutput(StringUtil.trimToNull(parseString(onSqlOutputMatcher)));
                                } else {
                                    pc.setOnSqlOutput(StringUtil.trimToNull(parseString(onUpdateSqlMatcher)));
                                }
                                changeSet.setPreconditions(pc);
                            }
                        } else if (altPreconditionsOneDashMatcher.matches()) {
                            String message = String.format("Unexpected formatting at line %d. Formatted SQL changelogs require known formats, such as '--preconditions <onFail>|<onError>|<onUpdate>' and others to be recognized and run. Learn all the options at https://docs.liquibase.com/concepts/changelogs/sql-format.html", count);
                            throw new ChangeLogParseException("\n" + message);
                        } else if (preconditionMatcher.matches()) {
                            if (changeSet.getPreconditions() == null) {
                                // create the defaults
                                changeSet.setPreconditions(new PreconditionContainer());
                            }
                            if (preconditionMatcher.groupCount() == 2) {
                                String name = StringUtil.trimToNull(preconditionMatcher.group(1));
                                if (name != null) {
                                    String body = preconditionMatcher.group(2).trim();
                                    if ("sql-check".equals(name)) {
                                        changeSet.getPreconditions().addNestedPrecondition(parseSqlCheckCondition(changeLogParameters.expandExpressions(StringUtil.trimToNull(body), changeSet.getChangeLog())));
                                    } else {
                                        throw new ChangeLogParseException("The '" + name + "' precondition type is not supported.");
                                    }
                                }
                            }
                        } else if (altPreconditionOneDashMatcher.matches()) {
                            String message =
                                    String.format("Unexpected formatting at line %d. Formatted SQL changelogs require known formats, such as '--precondition-sql-check' and others to be recognized and run. Learn all the options at https://docs.liquibase.com/concepts/changelogs/sql-format.html", count);
                            throw new ChangeLogParseException("\n" + message);
                        } else {
                            currentSql.append(line).append(System.lineSeparator());
                        }
                    } else {
                        if (commentMatcher.matches()) {
                            String message =
                               String.format("Unexpected formatting at line %d. Formatted SQL changelogs do not allow comment lines outside of changesets. Learn all the options at https://docs.liquibase.com/concepts/changelogs/sql-format.html", count);
                            throw new ChangeLogParseException("\n" + message);
                        }
                    }
                }
            }

            if (changeSet != null) {
                change.setSql(changeLogParameters.expandExpressions(StringUtil.trimToNull(currentSql.toString()), changeSet.getChangeLog()));

                if ((change.getEndDelimiter() == null) && StringUtil.trimToEmpty(change.getSql()).endsWith("\n/")) {
                    change.setEndDelimiter("\n/$");
                }

                if (StringUtil.trimToNull(currentRollbackSql.toString()) != null) {
                    if (currentRollbackSql.toString().trim().toLowerCase().matches("^not required.*")) {
                        changeSet.addRollbackChange(new EmptyChange());
                    } else if (currentRollbackSql.toString().trim().toLowerCase().contains("changesetid")) {
                        String rollbackString = currentRollbackSql.toString().replace("\n", "").replace("\r", "");

                        Matcher authorMatcher = rollbackChangeSetAuthorPattern.matcher(rollbackString);
                        Matcher idMatcher = rollbackChangeSetIdPattern.matcher(rollbackString);
                        Matcher pathMatcher = rollbackChangeSetPathPattern.matcher(rollbackString);

                        String changeSetAuthor = StringUtil.trimToNull(parseString(authorMatcher));
                        String changeSetId = StringUtil.trimToNull(parseString(idMatcher));
                        String changeSetPath = StringUtil.trimToNull(parseString(pathMatcher));

                        if (changeSetId == null) {
                            throw new ChangeLogParseException("'changesetId' not set in rollback block '"+rollbackString+"'");
                        }

                        if (changeSetAuthor == null) {
                            throw new ChangeLogParseException("'changesetAuthor' not set in rollback block '"+rollbackString+"'");
                        }

                        if (changeSetPath == null) {
                            changeSetPath = physicalChangeLogLocation;
                        }

                        ChangeSet rollbackChangeSet = changeLog.getChangeSet(changeSetPath, changeSetAuthor, changeSetId);
                        DatabaseChangeLog parent = changeLog;
                        while ((rollbackChangeSet == null) && (parent != null)) {
                            parent = parent.getParentChangeLog();
                            if (parent != null) {
                                rollbackChangeSet = parent.getChangeSet(changeSetPath, changeSetAuthor, changeSetId);
                            }
                        }

                        if (rollbackChangeSet == null) {
                            throw new ChangeLogParseException("Change set " + new ChangeSet(changeSetId, changeSetAuthor, false, false, changeSetPath, null, null, null).toString(false) + " does not exist");
                        }
                        for (Change rollbackChange : rollbackChangeSet.getChanges()) {
                            changeSet.addRollbackChange(rollbackChange);
                        }
                    } else {
                        RawSQLChange rollbackChange = new RawSQLChange();
                        rollbackChange.setSql(changeLogParameters.expandExpressions(currentRollbackSql.toString(), changeSet.getChangeLog()));
                        if (rollbackSplitStatementsPatternMatcher.matches()) {
                            rollbackChange.setSplitStatements(rollbackSplitStatements);
                        }
                        if (rollbackEndDelimiter != null) {
                            rollbackChange.setEndDelimiter(rollbackEndDelimiter);
                        }
                        changeSet.addRollbackChange(rollbackChange);
                    }
                }
            }

        } catch (IOException e) {
            throw new ChangeLogParseException(e);
        }

        return changeLog;
    }

    private void handleProperty(ChangeLogParameters changeLogParameters, DatabaseChangeLog changeLog, Matcher propertyPatternMatcher) {
        String name = null;
        String value = null;
        String context = null;
        String labels = null;
        String dbms = null;
        boolean global = true;
        for (int i = 1; i <= propertyPatternMatcher.groupCount(); i++) {
            String temp = propertyPatternMatcher.group(i);
            String[] parts = temp.split(":");
            String key = parts[0].trim().toLowerCase();
            switch (key) {
                case "name":
                    name = parts[1].trim();
                    break;
                case "value":
                    value = parts[1].trim();
                    break;
                case "context":
                    context = parts[1].trim();
                    break;
                case "labels":
                    labels = parts[1].trim();
                    break;
                case "dbms":
                    dbms = parts[1].trim();
                    break;
                case "global":
                    global = Boolean.parseBoolean(parts[1].trim());
                    break;
            }
        }
        changeLogParameters.set(name, value, context, labels, dbms, global, changeLog);
    }
    private StringBuilder extractMultiLineRollBackSQL(BufferedReader reader) throws IOException, ChangeLogParseException {
        StringBuilder multiLineRollbackSQL = new StringBuilder();
        Pattern rollbackMultiLineEndPattern = Pattern.compile(".*\\s*\\*\\/\\s*$", Pattern.CASE_INSENSITIVE);

        String line;
        if (reader != null) {
            while ((line = reader.readLine()) != null) {
                if (rollbackMultiLineEndPattern.matcher(line).matches()) {
                    String[] lastLineSplit = line.split("\\*\\/\\s*$");
                    if (lastLineSplit.length > 0 && !StringUtil.isWhitespace(lastLineSplit[0])) {
                        multiLineRollbackSQL.append(lastLineSplit[0]);
                    }
                    return multiLineRollbackSQL;
                }
                multiLineRollbackSQL.append(line);
            }
            throw new ChangeLogParseException("Liquibase rollback comment is not closed.");
        }
        return multiLineRollbackSQL;
    }
    protected boolean supportsExtension(String changelogFile){
        return changelogFile.toLowerCase().endsWith(".sql");
    }

    private SqlPrecondition parseSqlCheckCondition(String body) throws ChangeLogParseException{
        Pattern[] patterns = new Pattern[] {
            Pattern.compile("^(?:expectedResult:)?(\\w+) (.*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^(?:expectedResult:)?'([^']+)' (.*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^(?:expectedResult:)?\"([^\"]+)\" (.*)", Pattern.CASE_INSENSITIVE)
        };
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(body);
            if (matcher.matches() && (matcher.groupCount() == 2)) {
                SqlPrecondition p = new SqlPrecondition();
                p.setExpectedResult(matcher.group(1));
                p.setSql(matcher.group(2));
                return p;
            }
        }
        throw new ChangeLogParseException("Could not parse a SqlCheck precondition from '" + body + "'.");
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
                throw new ChangeLogParseException("Cannot parse " + changeSet + " " + matcher.toString().replaceAll("\\.*", "") + " as a boolean");
            }
        }
        return stripComments;
    }

    protected InputStream openChangeLogFile(String physicalChangeLogLocation, ResourceAccessor resourceAccessor) throws IOException {
        return resourceAccessor.getExisting(physicalChangeLogLocation).openInputStream();
    }
}
