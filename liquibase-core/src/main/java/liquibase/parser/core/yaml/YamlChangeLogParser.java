package liquibase.parser.core.yaml;

import liquibase.Contexts;
import liquibase.change.*;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.CreateIndexChange;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.precondition.CustomPreconditionWrapper;
import liquibase.precondition.Precondition;
import liquibase.precondition.PreconditionFactory;
import liquibase.precondition.PreconditionLogic;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.resource.ResourceAccessor;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sql.visitor.SqlVisitorFactory;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceCurrentValueFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.util.ObjectUtil;
import liquibase.util.file.FilenameUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;

public class YamlChangeLogParser implements ChangeLogParser {

    protected Logger log = LogFactory.getLogger();

    @Override
    public boolean supports(String changeLogFile, ResourceAccessor resourceAccessor) {
        return changeLogFile.endsWith("."+ getSupportedFileExtension());
    }

    protected String getSupportedFileExtension() {
        return "yaml";
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        Yaml yaml = new Yaml();

        try {
            InputStream changeLogStream = resourceAccessor.getResourceAsStream(physicalChangeLogLocation);
            if (changeLogStream == null) {
                throw new ChangeLogParseException("Change log file "+physicalChangeLogLocation+" does not exist");
            }

            Map changeLogAsMap = null;
            try {
                changeLogAsMap = yaml.loadAs(changeLogStream, Map.class);
            } catch (Exception e) {
                throw new ChangeLogParseException("Syntax error in "+getSupportedFileExtension()+": " + e.getMessage(), e);
            }

            DatabaseChangeLog changeLog = new DatabaseChangeLog(physicalChangeLogLocation);
            changeLog.setChangeLogParameters(changeLogParameters);
            List rootList = (List) changeLogAsMap.get("databaseChangeLog");
            if (rootList == null) {
                throw new ChangeLogParseException("Could not find root databaseChangeLog node");
            }

            for (Map<String, Object> nestedMap : (List<Map>) rootList) {
                String objectType = nestedMap.keySet().iterator().next();
                if (objectType.equals("changeSet")) {
                    Map<String, Object> changeSetMap = (Map) nestedMap.get("changeSet");
                    if (changeSetMap == null) {
                        throw new ChangeLogParseException("Null changeSet map");
                    }
                    checkRequiredAttribute("changeSet", "id", changeSetMap);
                    checkRequiredAttribute("changeSet", "author", changeSetMap);

                    ChangeSet changeSet = new ChangeSet(getValue(changeSetMap, "id", String.class, changeLogParameters),
                            getValue(changeSetMap, "author", String.class, changeLogParameters),
                            getValue(changeSetMap, "alwaysRun", boolean.class, false, changeLogParameters),
                            getValue(changeSetMap, "runOnChange", boolean.class, false, changeLogParameters),
                            physicalChangeLogLocation,
                            getValue(changeSetMap, "context", String.class, changeLogParameters),
                            getValue(changeSetMap, "dbms", String.class, changeLogParameters),
                            changeLog
                    );

                    try {

                        List<Map<String, Object>> changes = null;
                        try {
                            changes = (List<Map<String, Object>>) getValue(changeSetMap, "changes", List.class, changeLogParameters);
                        } catch (ClassCastException e) {
                            throw new ChangeLogParseException("Invalid 'changes' format in " + changeSet.toString(false));
                        }
                        if (changes != null) {
                            for (Map<String, Object> changeMap : changes) {
                                String changeName = changeMap.keySet().iterator().next();
                                Change change = ChangeFactory.getInstance().create(changeName);
                                change.setResourceAccessor(resourceAccessor);

                                if (changeMap.size() != 1) {
                                    throw new ChangeLogParseException("Invalid format for changeSet " + changeSet.toString(false));
                                }

                                for (Map.Entry<String, Object> param : ((Map<String, Object>) changeMap.get(changeName)).entrySet()) {
                                    ChangeParameterMetaData changeParameterMetaData = ChangeFactory.getInstance().getChangeMetaData(change).getParameters().get(param.getKey());
                                    if (changeParameterMetaData == null) {
                                        throw new ChangeLogParseException("Unexpected parameter " + param.getKey() + " for " + changeName);
                                    }
                                    Object value = param.getValue();
                                    String dataType = changeParameterMetaData.getDataType();
                                    if (dataType.equals("list of columnConfig") || dataType.equals("list of addColumnConfig")) {
                                        List<ColumnConfig> columnList = new ArrayList<ColumnConfig>();
                                        for (Map<String, Map<String, Object>> columnMap : (List<Map<String, Map<String, Object>>>) value ) {
                                            if (columnMap.size() != 1) {
                                                throw new ChangeLogParseException("Invalid 'column' syntax");
                                            }
                                            Map<String, Object> column = columnMap.get("column");
                                            ColumnConfig columnConfig;
                                            if (change instanceof CreateIndexChange || change instanceof AddColumnChange) {
                                                columnConfig = new AddColumnConfig();
                                            } else {
                                                columnConfig = new ColumnConfig();
                                            }

                                            columnConfig.setName(getValue(column, "name", String.class, changeLogParameters));
                                            columnConfig.setType(getValue(column, "type", String.class, changeLogParameters));
                                            columnConfig.setAutoIncrement(getValue(column, "autoIncrement", Boolean.class, changeLogParameters));
                                            columnConfig.setIncrementBy(getValue(column, "incrementBy", BigInteger.class, changeLogParameters));
                                            columnConfig.setRemarks(getValue(column, "remarks", String.class, changeLogParameters));
                                            columnConfig.setStartWith(getValue(column, "startWith", BigInteger.class, changeLogParameters));

                                            columnConfig.setValue(getValue(column, "value", String.class, changeLogParameters));
                                            columnConfig.setValueNumeric(getValue(column, "valueNumeric", String.class, changeLogParameters));
                                            columnConfig.setValueBlobFile(getValue(column, "valueBlobFile", String.class, changeLogParameters));
                                            columnConfig.setValueBoolean(getValue(column, "valueBoolean", String.class, changeLogParameters));
                                            columnConfig.setValueClobFile(getValue(column, "valueClob", String.class, changeLogParameters));
                                            columnConfig.setValueDate(getValue(column, "valueDate", String.class, changeLogParameters));
                                            String valueComputed = getValue(column, "valueComputed", String.class, changeLogParameters);
                                            if (valueComputed != null) {
                                                columnConfig.setValueComputed(new DatabaseFunction(valueComputed));
                                            }
                                            String valueSequenceCurrent = getValue(column, "valueSequenceCurrent", String.class, changeLogParameters);
                                            if (valueSequenceCurrent != null) {
                                                columnConfig.setValueSequenceCurrent(new SequenceCurrentValueFunction(valueSequenceCurrent));
                                            }
                                            String valueSequenceNext = getValue(column, "valueSequenceNext", String.class, changeLogParameters);
                                            if (valueSequenceNext != null) {
                                                columnConfig.setValueSequenceNext(new SequenceNextValueFunction(valueSequenceNext));
                                            }

                                            columnConfig.setDefaultValueNumeric(getValue(column, "defaultValueNumeric", Number.class, changeLogParameters));
                                            columnConfig.setDefaultValueBoolean(getValue(column, "defaultValueBoolean", Boolean.class, changeLogParameters));
                                            columnConfig.setDefaultValueDate(getValue(column, "defaultValueDate", String.class, changeLogParameters));
                                            String defaultValueComputed = getValue(column, "defaultValueComputed", String.class, changeLogParameters);
                                            if (defaultValueComputed != null) {
                                                columnConfig.setDefaultValueComputed(new DatabaseFunction(defaultValueComputed));
                                            }
                                            String defaultValueSequenceNext = getValue(column, "defaultValueSequenceNext", String.class, changeLogParameters);
                                            if (defaultValueSequenceNext != null) {
                                                columnConfig.setDefaultValueSequenceNext(new SequenceNextValueFunction(defaultValueSequenceNext));
                                            }

                                            Map<String, Object> constraintsMap = (Map<String, Object>) column.get("constraints");
                                            if (constraintsMap != null) {
                                                ConstraintsConfig constraints = new ConstraintsConfig();
                                                constraints.setCheckConstraint(getValue(constraintsMap, "checkConstraint", String.class, changeLogParameters));
                                                constraints.setDeferrable(getValue(constraintsMap, "deferrable", Boolean.class, changeLogParameters));
                                                constraints.setInitiallyDeferred(getValue(constraintsMap, "initiallyDeferred", Boolean.class, changeLogParameters));
                                                constraints.setDeleteCascade(getValue(constraintsMap, "deleteCascade", String.class, changeLogParameters));
                                                constraints.setForeignKeyName(getValue(constraintsMap, "foreignKeyName", String.class, changeLogParameters));
                                                constraints.setReferencedColumnNames(getValue(constraintsMap, "referencedColumnNames", String.class, changeLogParameters));
                                                constraints.setReferencedTableName(getValue(constraintsMap, "referencedTableName", String.class, changeLogParameters));
                                                constraints.setReferences(getValue(constraintsMap, "references", String.class, changeLogParameters));
                                                constraints.setNullable(getValue(constraintsMap, "nullable", Boolean.class, changeLogParameters));
                                                constraints.setPrimaryKey(getValue(constraintsMap, "primaryKey", String.class, changeLogParameters));
                                                constraints.setPrimaryKeyTablespace(getValue(constraintsMap, "primaryKeyTablespace", String.class, changeLogParameters));
                                                constraints.setPrimaryKeyName(getValue(constraintsMap, "primaryKeyName", String.class, changeLogParameters));
                                                constraints.setUnique(getValue(constraintsMap, "unique", Boolean.class, changeLogParameters));
                                                constraints.setUniqueConstraintName(getValue(constraintsMap, "uniqueConstraintName", String.class, changeLogParameters));

                                                columnConfig.setConstraints(constraints);
                                            }
                                            columnList.add(columnConfig);

                                        }
                                        value = columnList;

//                                    } else if (!dataType.equals("string")) {
//                                        System.out.println("other");
                                    }
                                    changeParameterMetaData.setValue(change, value);
                                }

                                changeSet.addChange(change);
                            }
                        }

                        List<Map<String, Object>> preconditions = null;
                        try {
                            preconditions = (List<Map<String, Object>>) getValue(changeSetMap, "preConditions", List.class, changeLogParameters);
                        } catch (ClassCastException e) {
                            throw new ChangeLogParseException("Invalid 'preConditions' format in " + changeSet.toString(false));
                        }
                        if (preconditions != null) {
                            changeSet.setPreconditions(new PreconditionContainer());
                            for (Map<String, Object> preconditionMap : preconditions) {
                                String preconditionName = preconditionMap.keySet().iterator().next();
                                if (preconditionName.equals("onFail")) {
                                    changeSet.getPreconditions().setOnFail(getValue(preconditionMap, "onFail", String.class, null, changeLogParameters));
                                    continue;
                                }
                                if(preconditionName.equals("onFailMessage")) {
                                    changeSet.getPreconditions().setOnFailMessage(getValue(preconditionMap, "onFailMessage", String.class, null, changeLogParameters));
                                    continue;
                                }
                                if (preconditionName.equals("onError")) {
                                    changeSet.getPreconditions().setOnError(getValue(preconditionMap, "onError", String.class, null, changeLogParameters));
                                    continue;
                                }
                                if(preconditionName.equals("onErrorMessage")) {
                                    changeSet.getPreconditions().setOnErrorMessage(getValue(preconditionMap, "onErrorMessage", String.class, null, changeLogParameters));
                                    continue;
                                }
                                Precondition precondition = parsePrecondition(preconditionMap);

                                changeSet.getPreconditions().addNestedPrecondition(precondition);
                            }
                        }
                        List<String> validCheckSums = getValue(changeSetMap, "validCheckSums", List.class, changeLogParameters);
                        if (validCheckSums != null) {
                            for (String string : validCheckSums) {
                                changeSet.addValidCheckSum(string);
                            }
                        }

                        List<Map<String, Object>> sqlVisitors = null;
                        try {
                            sqlVisitors = (List<Map<String, Object>>) getValue(changeSetMap, "modifySql", List.class, changeLogParameters);
                        } catch (ClassCastException e) {
                            throw new ChangeLogParseException("Invalid 'modifySql' format in " + changeSet.toString(false));
                        }
                        if (sqlVisitors != null) {
                            for (Map<String, Object> visitor : sqlVisitors) {
                                if (visitor.size() != 1) {
                                    throw new ChangeLogParseException("Invalid modifySql syntax");
                                }
                                String visitorName = visitor.keySet().iterator().next();

                                SqlVisitor sqlVisitor = SqlVisitorFactory.getInstance().create(visitorName);
                                Map<String, Object> visitorParams = (Map<String, Object>) visitor.get(visitorName);
                                for (String param : visitorParams.keySet()) {
                                    if (param.equals("dbms")) {
                                        String value = getValue(visitorParams, param, String.class, changeLogParameters);
                                        String dbms = value.toString().replace(" ", "");
                                        sqlVisitor.setApplicableDbms(new HashSet<String>(Arrays.asList(dbms.split(","))));
                                    } else if (param.equals("context")) {
                                        String value = getValue(visitorParams, param, String.class, changeLogParameters);
                                        String context = value.toString().replace(" ", "");
                                        sqlVisitor.setContexts(new Contexts(context.split(",")));
                                    } else if (param.equals("applyToRollback")) {
                                        Boolean value = getValue(visitorParams, param, Boolean.class, changeLogParameters);
                                        sqlVisitor.setApplyToRollback(value);
                                    } else {
                                        Object value = getValue(visitorParams, param, Object.class, changeLogParameters);
                                        if (value != null) {
                                            value = value.toString();
                                        }
                                        ObjectUtil.setProperty(sqlVisitor, param, (String) value);
                                    }
                                }
                                changeSet.addSqlVisitor(sqlVisitor);
                            }
                        }

                        changeLog.addChangeSet(changeSet);
                    } catch (Throwable e) {
                        throw new ChangeLogParseException(e + " in " + changeSet.toString(false), e);
                    }

                } else if (objectType.equals("property")) {
                    Map<String, Object> propertyMap = (Map) nestedMap.get("property");
                    String name = getValue(propertyMap, "name", String.class, changeLogParameters);
                    Object value = getValue(propertyMap, "value", Object.class, changeLogParameters);
                    String file = getValue(propertyMap, "file", String.class, changeLogParameters);
                    String context = getValue(propertyMap, "context", String.class, changeLogParameters);
                    String dbms = getValue(propertyMap, "dbms", String.class, changeLogParameters);
                    if (name != null) {
                        if (value == null) {
                            throw new ChangeLogParseException("No value for property " + name);
                        }
                        changeLog.getChangeLogParameters().set(name, value.toString(), context, dbms);
                    } else if (file != null) {
                        Properties props = new Properties();
                        InputStream propertiesStream = resourceAccessor.getResourceAsStream(file);
                        if (propertiesStream == null) {
                            log.info("Could not open properties file " + file);
                        } else {
                            props.load(propertiesStream);

                            for (Map.Entry entry : props.entrySet()) {
                                changeLog.getChangeLogParameters().set(entry.getKey().toString(), entry.getValue().toString(), context, dbms);
                            }
                        }
                    }
                } else if (objectType.equals("preConditions")) {
                    List preconditions = (List) nestedMap.get("preConditions");
                    if (preconditions == null) {
                        throw new ChangeLogParseException("Null preConditions map");
                    }

                    PreconditionContainer rootPrecondition = new PreconditionContainer();

                    for (Map<String, Object> preconditionContainerMap : (List<Map>) preconditions) {
                        String preconditionName = preconditionContainerMap.keySet().iterator().next();

                        if(preconditionName.equals("onFail")){
                            rootPrecondition.setOnFail(getValue(preconditionContainerMap, "onFail", String.class, null, changeLogParameters));
                            continue;
                        }
                        if(preconditionName.equals("onError")){
                            rootPrecondition.setOnError(getValue(preconditionContainerMap, "onError", String.class, null, changeLogParameters));
                            continue;
                        }
                        if(preconditionName.equals("onFailMessage")){
                            rootPrecondition.setOnFailMessage(getValue(preconditionContainerMap, "onFailMessage", String.class, null, changeLogParameters));
                            continue;
                        }
                        if(preconditionName.equals("onErrorMessage")){
                            rootPrecondition.setOnErrorMessage(getValue(preconditionContainerMap, "onErrorMessage", String.class, null, changeLogParameters));
                            continue;
                        }

                        Map<String, Object> preconditionMap = (Map) preconditionContainerMap.get(preconditionName);
                        Precondition precondition = PreconditionFactory.getInstance().create(preconditionName);

                        try {
                            for (Map.Entry<String, Object> param : preconditionMap.entrySet()) {
                                ObjectUtil.setProperty(precondition, (String) param.getKey(), param.getValue().toString());
//                                precondition.getChangeMetaData().getParameters().get(param.getKey()).setValue(change, param.getValue());
                            }
                        } catch (Throwable e) {
                            throw new ChangeLogParseException(e);
                        }

                        rootPrecondition.addNestedPrecondition(precondition);
                    }

                    changeLog.setPreconditions(rootPrecondition);
                } else if (objectType.equals("include")) {
                    Map<String, Object> includeMap = (Map) nestedMap.get("include");
                    String file = getValue(includeMap, "file", String.class, changeLogParameters);
                    if (file == null) {
                        throw new ChangeLogParseException("Missing include 'file' attribute");
                    }
                    file = file.replace('\\', '/');
                    boolean isRelativeToChangelogFile = getValue(includeMap, "relativeToChangelogFile", Boolean.class, false, changeLogParameters);
                    handleIncludedChangeLog(file, isRelativeToChangelogFile, physicalChangeLogLocation, changeLog, changeLogParameters, resourceAccessor);

                } else if (objectType.equals("includeAll")) {
                    throw new ChangeLogParseException("includeAll not yet supported in "+getSupportedFileExtension());
//                    Map<String, Object> includeAllMap = (Map) nestedMap.get("includeAll");
//                    String path = getValue(includeAllMap, "path", String.class, changeLogParameters);
//                    if (path == null) {
//                        throw new ChangeLogParseException("Missing includeAll 'file' attribute");
//                    }
//                    path = path.replace('\\', '/');
//                    boolean isRelativeToChangelogFile = getValue(includeAllMap, "relativeToChangelogFile", Boolean.class, false, changeLogParameters);
//                    handleIncludedChangeLog(fileName, isRelativeToChangelogFile, physicalChangeLogLocation);
                } else {
                    throw new ChangeLogParseException("Unexpected databaseChangeLog node: " + objectType);
                }
            }

            return changeLog;
        } catch (Throwable e) {
            throw new ChangeLogParseException(e);
        }
    }

    private Precondition parsePrecondition(Map<String, Object> preconditionMap) throws ChangeLogParseException {
        String preconditionName = preconditionMap.keySet().iterator().next();

        Precondition precondition = PreconditionFactory.getInstance().create(preconditionName);

        try {
            Object children = preconditionMap.get(preconditionName);
            if (children instanceof Map) {
                for (Map.Entry<String, Object> param : ((Map<String, Object>) children).entrySet()) {
                    if (precondition instanceof CustomPreconditionWrapper && param.getKey().equals("params")) {
                        List<Map<String, Object>> params = (List<Map<String, Object>>) param.getValue();
                        for (Map paramMap : params) {
                            paramMap = (Map<String, Object>) paramMap.get("param");
                            ((CustomPreconditionWrapper) precondition).setParam(paramMap.get("name").toString(), paramMap.get("value").toString());
                        }

                    } else {
                        ObjectUtil.setProperty(precondition, param.getKey(), param.getValue().toString());
                    }
                }
            } else {
                for (Map<String, Object> child : (Collection<Map<String, Object>>) children) {
                    ((PreconditionLogic) precondition).addNestedPrecondition(parsePrecondition(child));
                }
            }
        } catch (Throwable e) {
            throw new ChangeLogParseException("Error parsing precondition '"+preconditionName+"'", e);
        }
        return precondition;
    }

    private <T> T getValue(Map<String, Object> map, String key, Class<T> type, T defaultValue, ChangeLogParameters changeLogParameters) {
        if (map.containsKey(key)) {
            Object mapValue = map.get(key);
            if (mapValue == null) {
                return null;
            }
            if (mapValue != null && mapValue instanceof String && ((String) mapValue).startsWith("${")) {
                mapValue = changeLogParameters.expandExpressions((String) mapValue);
            }
            if (type.isAssignableFrom(mapValue.getClass())) {
                return (T) mapValue;
            } else {
                if (type.equals(String.class)) {
                    return (T) mapValue.toString();
                } else if (type.equals(boolean.class) && mapValue.getClass().equals(Boolean.class)) {
                    return (T) mapValue;
                } else {
                    throw new UnexpectedLiquibaseException("Could not convert "+mapValue.getClass().getName()+" '"+mapValue+"' to "+type.getName());
                }
            }
        } else {
            return defaultValue;
        }
    }

    private <T> T getValue(Map<String, Object> map, String key, Class<T> type, ChangeLogParameters changeLogParameters) {
        return getValue(map, key, type, null, changeLogParameters);
    }

    private void checkRequiredAttribute(String outerNode, String key, Map map) throws ChangeLogParseException {
        if (!map.containsKey(key)) {
            throw new ChangeLogParseException(outerNode + " is missing required attribute " + key);
        }
    }

    protected boolean handleIncludedChangeLog(String fileName, boolean isRelativePath, String relativeBaseFileName, DatabaseChangeLog databaseChangeLog, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws LiquibaseException {

        if (fileName.equalsIgnoreCase(".svn") || fileName.equalsIgnoreCase("cvs")) {
            return false;
        }

        ChangeLogParser changeLogParser = ChangeLogParserFactory.getInstance().getParser(fileName, resourceAccessor);
        if (changeLogParser == null) {
            log.warning("included file "+relativeBaseFileName + "/" + fileName + " is not a recognized file type");
            return false;
        }


        if (isRelativePath) {
            // workaround for FilenameUtils.normalize() returning null for relative paths like ../conf/liquibase.xml
            String tempFile = FilenameUtils.concat(FilenameUtils.getFullPath(relativeBaseFileName), fileName);
            if(tempFile != null && new File(tempFile).exists() == true) {
                fileName = tempFile;
            } else {
                fileName = FilenameUtils.getFullPath(relativeBaseFileName) + fileName;
            }
        }
        DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(fileName, resourceAccessor).parse(fileName, changeLogParameters,
                resourceAccessor);
        PreconditionContainer preconditions = changeLog.getPreconditions();
        if (preconditions != null) {
            if (null == databaseChangeLog.getPreconditions()) {
                databaseChangeLog.setPreconditions(new PreconditionContainer());
            }
            databaseChangeLog.getPreconditions().addNestedPrecondition(
                    preconditions);
        }
        for (ChangeSet changeSet : changeLog.getChangeSets()) {
            databaseChangeLog.addChangeSet(changeSet);
        }

        return true;
    }

}
