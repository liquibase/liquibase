package liquibase.include;

import static liquibase.changelog.DatabaseChangeLog.CONTEXT;
import static liquibase.changelog.DatabaseChangeLog.CONTEXT_FILTER;
import static liquibase.changelog.DatabaseChangeLog.ERROR_IF_MISSING;
import static liquibase.changelog.DatabaseChangeLog.FILE;
import static liquibase.changelog.DatabaseChangeLog.IGNORE;
import static liquibase.changelog.DatabaseChangeLog.INCLUDE_CHANGELOG;
import static liquibase.changelog.DatabaseChangeLog.LABELS;
import static liquibase.changelog.DatabaseChangeLog.LOGICAL_FILE_PATH;
import static liquibase.changelog.DatabaseChangeLog.PRE_CONDITIONS;
import static liquibase.changelog.DatabaseChangeLog.RELATIVE_TO_CHANGELOG_FILE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import liquibase.Scope;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.lang3.tuple.Pair;

public final class FormattedSqlIncludeUtils {

  private static final String INCLUDE_REGEX = "^\\s*-+\\s*include";
  public static final Pattern INCLUDE_PATTERN = Pattern.compile(INCLUDE_REGEX, Pattern.CASE_INSENSITIVE);
  private static final String FILE_REGEX = "\\s+file:\\S+\\b(.xml|.json|.yml|.yaml|.sql)\\b";
  private static final Pattern FILE_PATTERN = Pattern.compile(FILE_REGEX, Pattern.CASE_INSENSITIVE);
  private static final String RELATIVE_REGEX = "\\s+relativeToChangelogFile:\\S+";
  private static final Pattern RELATIVE_PATTERN = Pattern.compile(RELATIVE_REGEX, Pattern.CASE_INSENSITIVE);
  private static final String ERROR_IF_MISSING_REGEX = "\\s+errorIfMissing:\\S+";
  private static final Pattern ERROR_IF_MISSING_PATTERN = Pattern.compile(ERROR_IF_MISSING_REGEX, Pattern.CASE_INSENSITIVE);
  private static final String IGNORE_REGEX = "\\s+ignore:\\S+";
  private static final Pattern IGNORE_PATTERN = Pattern.compile(IGNORE_REGEX, Pattern.CASE_INSENSITIVE);
  private static final String CONTEXT_FILTER_REGEX = "\\s+contextFilter:\\S+";
  private static final Pattern CONTEXT_FILTER_PATTERN = Pattern.compile(CONTEXT_FILTER_REGEX, Pattern.CASE_INSENSITIVE);
  private static final String CONTEXT_REGEX = "\\s+context:\\S+";
  private static final Pattern CONTEXT_PATTERN = Pattern.compile(CONTEXT_REGEX, Pattern.CASE_INSENSITIVE);
  private static final String LABELS_REGEX = "\\s+labels:\\S+";
  private static final Pattern LABELS_PATTERN = Pattern.compile(LABELS_REGEX, Pattern.CASE_INSENSITIVE);
  private static final String LOGICAL_FILE_PATH_REGEX = "\\s+logicalFilePath:\\S+";
  private static final Pattern LOGICAL_FILE_PATH_PATTERN = Pattern.compile(LOGICAL_FILE_PATH_REGEX, Pattern.CASE_INSENSITIVE);

  private static final String PRECONDITIONS_REGEX = "preconditions\\s+(onError:(?:WARN|HALT|MARK_RAN))\\s+(onFail:(?:WARN|HALT|MARK_RAN)){0,1}|preconditions\\s+(onFail:(?:WARN|HALT|MARK_RAN))\\s+(onError:(?:WARN|HALT|MARK_RAN)){0,1}";
  private static final Pattern PRECONDITIONS_PATTERN = Pattern.compile(PRECONDITIONS_REGEX, Pattern.CASE_INSENSITIVE);

  private static final String TABLE_EXISTS_PRECONDITION_REGEX = "\\b(precondition-table-exists)\\b(?:(?!\\bprecondition-\\b).)*\\b(table:|catalog:|schema:)\\b\\S*";
  private static final Pattern TABLE_EXISTS_PRECONDITION_PATTERN = Pattern.compile(TABLE_EXISTS_PRECONDITION_REGEX, Pattern.CASE_INSENSITIVE);
  private static final String TABLE_REGEX = "(table:)(\\S+)";
  private static final Pattern TABLE_PATTERN = Pattern.compile(TABLE_REGEX, Pattern.CASE_INSENSITIVE);

  private static final String VIEW_EXISTS_PRECONDITION_REGEX = "\\b(precondition-view-exists)\\b(?:(?!\\bprecondition-\\b).)*\\b(view:|catalog:|schema:)\\b\\S*";
  private static final Pattern VIEW_EXISTS_PRECONDITION_PATTERN = Pattern.compile(VIEW_EXISTS_PRECONDITION_REGEX, Pattern.CASE_INSENSITIVE);
  private static final String VIEW_REGEX = "(view:)(\\S+)";
  private static final Pattern VIEW_PATTERN = Pattern.compile(VIEW_REGEX, Pattern.CASE_INSENSITIVE);

  private static final String SQL_CHECK_PRECONDITION_REGEX = "\\b(?:precondition-sql-check)\\b(?:(?!\\bprecondition-\\b).)*";
  private static final Pattern SQL_CHECK_PRECONDITION_PATTERN = Pattern.compile(SQL_CHECK_PRECONDITION_REGEX, Pattern.CASE_INSENSITIVE);
  private static final String EXPECTED_RESULT_REGEX = "(expectedResult:)(\\S+)";
  private static final Pattern EXPECTED_RESULT_PATTERN = Pattern.compile(EXPECTED_RESULT_REGEX, Pattern.CASE_INSENSITIVE);
  private static final String SQL_REGEX = "(?:precondition-sql-check)(.*)";
  private static final Pattern SQL_PATTERN = Pattern.compile(SQL_REGEX, Pattern.CASE_INSENSITIVE);

  private static final String SCHEMA_REGEX = "(schema:)(\\S+)";
  private static final Pattern SCHEMA_PATTERN = Pattern.compile(SCHEMA_REGEX, Pattern.CASE_INSENSITIVE);
  private static final String CATALOG_REGEX = "(catalog:)(\\S+)";
  private static final Pattern CATALOG_PATTERN = Pattern.compile(CATALOG_REGEX, Pattern.CASE_INSENSITIVE);

  private FormattedSqlIncludeUtils() {}

  public static ChangeLogInclude handleInclude(String line, ResourceAccessor resourceAccessor,
                                               DatabaseChangeLog parent, MatchResult matchResult)
      throws ChangeLogParseException {

    try {
      String originalLine = line;

      ParsedNode includeNode = new ParsedNode(null, INCLUDE_CHANGELOG);

      line = cleanUp(line, matchResult);

      line = mapFile(line, includeNode);
      line = mapRelativeToChangeLogFile(line, includeNode);
      line = mapErrorIfMissing(line, includeNode);
      line = mapIgnore(line, includeNode);
      line = mapLogicalFilePath(line, includeNode);
      line = mapLabels(line, includeNode);
      line = mapContextFilter(line, includeNode);
      line = mapContext(line, includeNode);
      line = mapPreconditions(line, includeNode);

      if(!line.trim().isEmpty())
        throw new RuntimeException(String.format(
            "error occurred while parsing liquibase formatted sql '%s' cannot map this part '%s'", originalLine, line.trim()));

      IncludeServiceFactory factory = Scope.getCurrentScope().getSingleton(IncludeServiceFactory.class);
      IncludeService service = factory.getIncludeService();
      return service.createChangelogInclude(includeNode, resourceAccessor, parent, new HashMap<>());
    } catch (Exception e) {
      throw new ChangeLogParseException(e.getMessage());
    }
  }

  private static String mapPreconditions(String line, ParsedNode includeNode) throws ParsedNodeException {

    ParsedNode preconditionsNode = new ParsedNode(null, PRE_CONDITIONS);
    Matcher preconditionsMatcher = PRECONDITIONS_PATTERN.matcher(line);
    Pair<ParsedNode, String> preconditionsPair = findAtMostOnce(line, preconditionsMatcher,
        FormattedSqlIncludeUtils::mapIncludePreconditions);
    if(preconditionsPair != null) {
      line = preconditionsPair.getRight();
      preconditionsNode = preconditionsPair.getLeft();
    }

    List<ParsedNode> preconditions = new ArrayList<>(5);

    line = mapTableExistsPrecondition(line, preconditions);

    line = mapViewExistsPrecondition(line, preconditions);

    line = mapSqlCheckPrecondition(line, preconditions);

    setPreconditions(includeNode, preconditions, preconditionsNode);

    return line;
  }

  private static String mapTableExistsPrecondition(String line, List<ParsedNode> preconditions) {
    Matcher tableExistsPreconditionMatcher = TABLE_EXISTS_PRECONDITION_PATTERN.matcher(line);
    Pair<List<ParsedNode>, String> tableExistsPreconditionPairs = find(line, tableExistsPreconditionMatcher,
        FormattedSqlIncludeUtils::mapTableExistsPrecondition);
    if(tableExistsPreconditionPairs != null) {
      preconditions.addAll(tableExistsPreconditionPairs.getLeft());
      line = tableExistsPreconditionPairs.getRight();
    }
    return line;
  }

  private static String mapViewExistsPrecondition(String line, List<ParsedNode> preconditions) {
    Matcher viewExistsPreconditionMatcher = VIEW_EXISTS_PRECONDITION_PATTERN.matcher(line);
    Pair<List<ParsedNode>, String> viewExistsPreconditionPairs = find(line, viewExistsPreconditionMatcher,
        FormattedSqlIncludeUtils::mapViewExistsPrecondition);
    if(viewExistsPreconditionPairs != null) {
      preconditions.addAll(viewExistsPreconditionPairs.getLeft());
      line = viewExistsPreconditionPairs.getRight();
    }
    return line;
  }

  private static String mapSqlCheckPrecondition(String line, List<ParsedNode> preconditions) {
    Matcher sqlCheckPreconditionMatcher = SQL_CHECK_PRECONDITION_PATTERN.matcher(line);
    Pair<List<ParsedNode>, String> sqlCheckPreconditionPairs = find(line, sqlCheckPreconditionMatcher,
        FormattedSqlIncludeUtils::mapSqlCheckPrecondition);
    if(sqlCheckPreconditionPairs != null) {
      preconditions.addAll(sqlCheckPreconditionPairs.getLeft());
      line = sqlCheckPreconditionPairs.getRight();
    }
    return line;
  }

  private static void setPreconditions(ParsedNode includeNode, List<ParsedNode> preconditions,
                                ParsedNode preconditionsNode) throws ParsedNodeException {
    if(!preconditions.isEmpty()) {
      setPreconditions(preconditionsNode, preconditions);
      includeNode.addChild(preconditionsNode);
    }
  }

  private static String mapContext(String line, ParsedNode includeNode) throws ParsedNodeException {
    Matcher context = CONTEXT_PATTERN.matcher(line);
    Pair<ParsedNode, String> contextPair = findAtMostOnce(line, context,
        FormattedSqlIncludeUtils::mapContext);

    if(contextPair != null) {
      line = contextPair.getRight();
      includeNode.addChild(contextPair.getLeft());
    }
    return line;
  }

  private static String mapContextFilter(String line, ParsedNode includeNode) throws ParsedNodeException {
    Matcher contextFilter = CONTEXT_FILTER_PATTERN.matcher(line);
    Pair<ParsedNode, String> contextFilterPair = findAtMostOnce(line, contextFilter,
        FormattedSqlIncludeUtils::mapContextFilter);

    if(contextFilterPair != null) {
      line = contextFilterPair.getRight();
      includeNode.addChild(contextFilterPair.getLeft());
    }
    return line;
  }

  private static String mapLabels(String line, ParsedNode includeNode) throws ParsedNodeException {
    Matcher labels = LABELS_PATTERN.matcher(line);
    Pair<ParsedNode, String> labelsPair = findAtMostOnce(line, labels,
        FormattedSqlIncludeUtils::mapLabels);

    if(labelsPair != null) {
      line = labelsPair.getRight();
      includeNode.addChild(labelsPair.getLeft());
    }
    return line;
  }

  private static String mapLogicalFilePath(String line, ParsedNode includeNode) throws ParsedNodeException {
    Matcher logicalFilePath = LOGICAL_FILE_PATH_PATTERN.matcher(line);
    Pair<ParsedNode, String> logicalFilePathPair = findAtMostOnce(line, logicalFilePath,
        FormattedSqlIncludeUtils::mapLogicalFilePath);

    if(logicalFilePathPair != null) {
      line = logicalFilePathPair.getRight();
      includeNode.addChild(logicalFilePathPair.getLeft());
    }
    return line;
  }

  private static String mapIgnore(String line, ParsedNode includeNode) throws ParsedNodeException {
    Matcher ignore = IGNORE_PATTERN.matcher(line);
    Pair<ParsedNode, String> ignorePair = findAtMostOnce(line, ignore,
        FormattedSqlIncludeUtils::mapIgnore);

    if(ignorePair != null) {
      line = ignorePair.getRight();
      includeNode.addChild(ignorePair.getLeft());
    }
    return line;
  }

  private static String mapErrorIfMissing(String line, ParsedNode includeNode) throws ParsedNodeException {
    Matcher errorIfMissing = ERROR_IF_MISSING_PATTERN.matcher(line);
    Pair<ParsedNode, String> errorIfMissingPair = findAtMostOnce(line, errorIfMissing,
        FormattedSqlIncludeUtils::mapErrorIfMissing);

    if(errorIfMissingPair != null) {
      line = errorIfMissingPair.getRight();
      includeNode.addChild(errorIfMissingPair.getLeft());
    }
    return line;
  }

  private static String mapRelativeToChangeLogFile(String line, ParsedNode includeNode) throws ParsedNodeException {
    Matcher relativeToChangelogFile = RELATIVE_PATTERN.matcher(line);
    Pair<ParsedNode, String> relativeToChangeLogFilePair = findAtMostOnce(line, relativeToChangelogFile,
        FormattedSqlIncludeUtils::mapRelativeToChangeLogFile);

    if(relativeToChangeLogFilePair != null) {
      line = relativeToChangeLogFilePair.getRight();
      includeNode.addChild(relativeToChangeLogFilePair.getLeft());
    }
    return line;
  }

  private static String mapFile(String line, ParsedNode includeNode) throws ParsedNodeException {
    Matcher file = FILE_PATTERN.matcher(line);
    Pair<ParsedNode, String> filePair = findExactlyOnce(line, file,
        FormattedSqlIncludeUtils::mapFile);

    line = filePair.getRight();
    includeNode.addChild(filePair.getLeft());
    return line;
  }

  private static void setPreconditions(ParsedNode preconditionContainer, List<ParsedNode> preconditions)
      throws ParsedNodeException {
    for(ParsedNode precondition : preconditions) {
      preconditionContainer.addChild(precondition);
    }
  }

  private static Pair<ParsedNode, String> findExactlyOnce(String line, Matcher matcher,
                                                         BiFunction<String, MatchResult, Pair<ParsedNode, String>> fn) {
    Pair<ParsedNode, String> result;
    if(matcher.find()) {
      MatchResult matchResult = matcher.toMatchResult();
      result = fn.apply(line, matchResult);
    } else {
      throw new RuntimeException(String.format("malformed liquibase formatted sql: '%s' no element matching the following regex '%s' found",
          line.trim(), matcher.pattern().pattern()));
    }
    if(matcher.find())
      throw new RuntimeException(String.format("malformed liquibase formatted sql: '%s' more than one element matching the following regex '%s' found",
          line.trim(), matcher.pattern().pattern()));

    return result;
  }

  private static Pair<ParsedNode, String> findAtMostOnce(String line, Matcher matcher,
                                                         BiFunction<String, MatchResult, Pair<ParsedNode, String>> fn) {
    Pair<ParsedNode, String> result = null;
    if(matcher.find()) {
      MatchResult matchResult = matcher.toMatchResult();
      result = fn.apply(line, matchResult);
    }
    if(matcher.find())
      throw new RuntimeException(String.format("malformed liquibase formatted sql: '%s' more than one element matching the following regex '%s' found",
          line.trim(), matcher.pattern().pattern()));

    return result;
  }

  private static Pair<List<ParsedNode>, String> find(String line, Matcher matcher,
                                                              BiFunction<String, MatchResult, ParsedNode> fn) {
    Pair<List<ParsedNode>, String> result = null;
    List<ParsedNode> nodes = new ArrayList<>(5);
    MatchResult matchResult;
    while(matcher.find()) {
      matchResult = matcher.toMatchResult();
      nodes.add(fn.apply(line, matchResult));
      line = cleanUp(line, matcher);
      matcher.reset(line);
    }
    if(!nodes.isEmpty()) result = Pair.of(nodes, line);
    return result;
  }

  private static Pair<ParsedNode, String> mapFile(String line, MatchResult matchResult) {
    try {
      ParsedNode parsedNode = new ParsedNode(null, FILE);
      parsedNode.setValue(line.substring(matchResult.start(), matchResult.end()).split(":")[1]);
      return Pair.of(parsedNode, cleanUp(line, matchResult));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Pair<ParsedNode, String> mapRelativeToChangeLogFile(String line, MatchResult matchResult) {
    try {
      ParsedNode parsedNode = new ParsedNode(null, RELATIVE_TO_CHANGELOG_FILE);
      parsedNode.setValue(line.substring(matchResult.start(), matchResult.end()).split(":")[1]);
      return Pair.of(parsedNode, cleanUp(line, matchResult));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Pair<ParsedNode, String> mapErrorIfMissing(String line, MatchResult matchResult) {
    try {
      ParsedNode parsedNode = new ParsedNode(null, ERROR_IF_MISSING);
      parsedNode.setValue(line.substring(matchResult.start(), matchResult.end()).split(":")[1]);
      return Pair.of(parsedNode, cleanUp(line, matchResult));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Pair<ParsedNode, String> mapIgnore(String line, MatchResult matchResult) {
    try {
      ParsedNode parsedNode = new ParsedNode(null, IGNORE);
      parsedNode.setValue(line.substring(matchResult.start(), matchResult.end()).split(":")[1]);
      return Pair.of(parsedNode, cleanUp(line, matchResult));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Pair<ParsedNode, String> mapLogicalFilePath(String line, MatchResult matchResult) {
    try {
      ParsedNode parsedNode = new ParsedNode(null, LOGICAL_FILE_PATH);
      parsedNode.setValue(line.substring(matchResult.start(), matchResult.end()).split(":")[1]);
      return Pair.of(parsedNode, cleanUp(line, matchResult));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Pair<ParsedNode, String> mapLabels(String line, MatchResult matchResult) {
    try {
      ParsedNode parsedNode = new ParsedNode(null, LABELS);
      parsedNode.setValue(line.substring(matchResult.start(), matchResult.end()).split(":")[1]);
      return Pair.of(parsedNode, cleanUp(line, matchResult));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Pair<ParsedNode, String> mapContextFilter(String line, MatchResult matchResult) {
    try {
      ParsedNode parsedNode = new ParsedNode(null, CONTEXT_FILTER);
      parsedNode.setValue(line.substring(matchResult.start(), matchResult.end()).split(":")[1]);
      return Pair.of(parsedNode, cleanUp(line, matchResult));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Pair<ParsedNode, String> mapContext(String line, MatchResult matchResult) {
    try {
      ParsedNode parsedNode = new ParsedNode(null, CONTEXT);
      parsedNode.setValue(line.substring(matchResult.start(), matchResult.end()).split(":")[1]);
      return Pair.of(parsedNode, cleanUp(line, matchResult));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  private static Pair<ParsedNode, String> mapIncludePreconditions(String line, MatchResult matchResult) {
    try {
      ParsedNode parsedNode = new ParsedNode(null, PRE_CONDITIONS);
      String group1 = matchResult.group(1);
      String group3 = matchResult.group(3);
      ParsedNode onError = new ParsedNode(null, "onError");
      ParsedNode onFail = new ParsedNode(null, "onFail");
      if(group1 != null) {
        onError.setValue(PreconditionContainer.ErrorOption.valueOf(group1.split(":")[1].toUpperCase()));
        parsedNode.addChild(onError);
        String group2 = matchResult.group(2);
        if(group2 != null) {
          onFail.setValue((PreconditionContainer.FailOption.valueOf(group2.split(":")[1].toUpperCase())));
          parsedNode.addChild(onFail);
        }
      }
      if(group3 != null) {
        onFail.setValue((PreconditionContainer.FailOption.valueOf(group3.split(":")[1].toUpperCase())));
        parsedNode.addChild(onFail);
        String group4 = matchResult.group(4);
        if(group4 != null) {
          onError.setValue(PreconditionContainer.ErrorOption.valueOf(group4.split(":")[1].toUpperCase()));
          parsedNode.addChild(onError);
        }
      }
      return Pair.of(parsedNode, cleanUp(line, matchResult));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static ParsedNode mapTableExistsPrecondition(String line, MatchResult matchResult) {
    try {
      ParsedNode parsedNode = new ParsedNode(null, "tableExists");
      ParsedNode tableNameNode = new ParsedNode(null, "tableName");
      String precondition = line.substring(matchResult.start(), matchResult.end());
      Matcher tableMatcher =  TABLE_PATTERN.matcher(precondition);

      if(!tableMatcher.find() || tableMatcher.group(2) == null)
        throw new RuntimeException(String.format("malformed liquibase formatted sql: tableName attribute not found for precondition %s", precondition));
      tableNameNode.setValue(tableMatcher.group(2));
      parsedNode.addChild(tableNameNode);

      for(ParsedNode node : getCatalogAndSchemaName(precondition))
        parsedNode.addChild(node);

      return parsedNode;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static ParsedNode mapViewExistsPrecondition(String line, MatchResult matchResult) {
    try {
      ParsedNode parsedNode = new ParsedNode(null, "viewExists");
      ParsedNode viewNameNode = new ParsedNode(null, "viewName");
      String precondition = line.substring(matchResult.start(), matchResult.end());
      Matcher viewMatcher =  VIEW_PATTERN.matcher(precondition);

      if(!viewMatcher.find() || viewMatcher.group(2) == null)
        throw new RuntimeException(String.format("malformed liquibase formatted sql: viewName attribute not found for precondition %s", precondition));
      viewNameNode.setValue(viewMatcher.group(2));
      parsedNode.addChild(viewNameNode);

      for(ParsedNode node : getCatalogAndSchemaName(precondition))
        parsedNode.addChild(node);
      return parsedNode;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static ParsedNode mapSqlCheckPrecondition(String line, MatchResult matchResult) {
    try {
      ParsedNode parsedNode = new ParsedNode(null, "sqlCheck");
      ParsedNode expectedResultNode = new ParsedNode(null, "expectedResult");
      ParsedNode sqlNode = new ParsedNode(null, "sql");
      String precondition = line.substring(matchResult.start(), matchResult.end());
      Matcher expectedResultMatcher =  EXPECTED_RESULT_PATTERN.matcher(precondition);

      if(!expectedResultMatcher.find() || expectedResultMatcher.group(2) == null)
        throw new RuntimeException(String.format("malformed liquibase formatted sql: expectedResult attribute not found for precondition %s", precondition));
      expectedResultNode.setValue(expectedResultMatcher.group(2));
      parsedNode.addChild(expectedResultNode);
      String sql = cleanUp(precondition, expectedResultMatcher);
      Matcher sqlMatcher = SQL_PATTERN.matcher(sql);

      if(!sqlMatcher.find() || sqlMatcher.group(1) == null)
        throw new RuntimeException(String.format("malformed liquibase formatted sql: sql attribute not found for precondition %s", precondition));
      sqlNode.setValue(sqlMatcher.group(1).trim());
      parsedNode.addChild(sqlNode);

      return parsedNode;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static List<ParsedNode> getCatalogAndSchemaName(String precondition)
      throws ParsedNodeException {
    List<ParsedNode> result = new ArrayList<>(2);
    Matcher schemaMatcher =  SCHEMA_PATTERN.matcher(precondition);
    Matcher catalogMatcher =  CATALOG_PATTERN.matcher(precondition);
    if(schemaMatcher.find() && schemaMatcher.group(2) != null) {
      ParsedNode schemaNameNode = new ParsedNode(null, "schemaName");
      schemaNameNode.setValue(schemaMatcher.group(2));
      result.add(schemaNameNode);
    }
    if(schemaMatcher.find())
      throw new RuntimeException(String.format("multiple schemas found for precondition %s", precondition));

    if(catalogMatcher.find() && catalogMatcher.group(2) != null) {
      ParsedNode catalogNameNode = new ParsedNode(null, "catalogName");
      catalogNameNode.setValue(catalogMatcher.group(2));
      result.add(catalogNameNode);
    }
    if(catalogMatcher.find())
      throw new RuntimeException(String.format("multiple catalogs found for precondition %s", precondition));
    return result;
  }

  private static String cleanUp(String line, MatchResult result) {
    int start = result.start(), end = result.end() -1;
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < line.length(); i++) {
      if((i < start || i > end) || Character.isSpaceChar(line.charAt(i))) {
        sb.append(line.charAt(i));
      }
    }
    return sb.toString();
  }
}