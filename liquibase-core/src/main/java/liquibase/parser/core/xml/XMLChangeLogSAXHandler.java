package liquibase.parser.core.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liquibase.Liquibase;
import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.CreateProcedureChange;
import liquibase.change.core.CreateViewChange;
import liquibase.change.core.DeleteDataChange;
import liquibase.change.core.ExecuteShellCommandChange;
import liquibase.change.core.InsertDataChange;
import liquibase.change.core.LoadDataChange;
import liquibase.change.core.LoadDataColumnConfig;
import liquibase.change.core.RawSQLChange;
import liquibase.change.core.StopChange;
import liquibase.change.core.UpdateDataChange;
import liquibase.change.custom.CustomChangeWrapper;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.CustomChangeException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.MigrationFailedException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.precondition.CustomPreconditionWrapper;
import liquibase.precondition.Precondition;
import liquibase.precondition.PreconditionFactory;
import liquibase.precondition.PreconditionLogic;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.precondition.core.SqlPrecondition;
import liquibase.resource.ResourceAccessor;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sql.visitor.SqlVisitorFactory;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtils;
import liquibase.util.file.FilenameUtils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class XMLChangeLogSAXHandler extends DefaultHandler {

	private static final char LIQUIBASE_FILE_SEPARATOR = '/';

	protected Logger log;

	private DatabaseChangeLog databaseChangeLog;
	private Change change;
	private Stack changeSubObjects = new Stack();
	private StringBuffer text;
	private PreconditionContainer rootPrecondition;
	private Stack<PreconditionLogic> preconditionLogicStack = new Stack<PreconditionLogic>();
	private ChangeSet changeSet;
	private String paramName;
	private ResourceAccessor resourceAccessor;
	private Precondition currentPrecondition;

	private ChangeLogParameters changeLogParameters;
	private boolean inRollback = false;

	private boolean inModifySql = false;
	private Set<String> modifySqlDbmsList;
	private Set<String> modifySqlContexts;
	private boolean modifySqlAppliedOnRollback = false;

	protected XMLChangeLogSAXHandler(String physicalChangeLogLocation,
			ResourceAccessor resourceAccessor,
			ChangeLogParameters changeLogParameters) {
		log = LogFactory.getLogger();
		this.resourceAccessor = resourceAccessor;

		databaseChangeLog = new DatabaseChangeLog();
		databaseChangeLog.setPhysicalFilePath(physicalChangeLogLocation);
		databaseChangeLog.setChangeLogParameters(changeLogParameters);

		this.changeLogParameters = changeLogParameters;
	}

	public DatabaseChangeLog getDatabaseChangeLog() {
		return databaseChangeLog;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes baseAttributes) throws SAXException {
		Attributes atts = new ExpandingAttributes(baseAttributes);
		try {
			if ("comment".equals(qName)) {
				text = new StringBuffer();
			} else if ("validCheckSum".equals(qName)) {
				text = new StringBuffer();
			} else if ("databaseChangeLog".equals(qName)) {
				String schemaLocation = atts.getValue("xsi:schemaLocation");
				if (schemaLocation != null) {
					Matcher matcher = Pattern.compile(
							".*dbchangelog-(\\d+\\.\\d+).xsd").matcher(
							schemaLocation);
					if (matcher.matches()) {
						String version = matcher.group(1);
						if (!version.equals(XMLChangeLogSAXParser
								.getSchemaVersion())) {
							log.info(databaseChangeLog.getPhysicalFilePath()
									+ " is using schema version " + version
									+ " rather than version "
									+ XMLChangeLogSAXParser.getSchemaVersion());
						}
					}
				}
				databaseChangeLog.setLogicalFilePath(atts
						.getValue("logicalFilePath"));
			} else if ("include".equals(qName)) {
				String fileName = atts.getValue("file");
				fileName = fileName.replace('\\', '/');
				boolean isRelativeToChangelogFile = Boolean.parseBoolean(atts
						.getValue("relativeToChangelogFile"));
				handleIncludedChangeLog(fileName, isRelativeToChangelogFile,
						databaseChangeLog.getPhysicalFilePath());
			} else if ("includeAll".equals(qName)) {
				String pathName = atts.getValue("path");
				pathName = pathName.replace('\\', '/');

				if (!(pathName.endsWith("/"))) {
					pathName = pathName + '/';
				}
				log.debug("includeAll for " + pathName);
				log.debug("Using file opener for includeAll: " + resourceAccessor.toString());
				boolean isRelativeToChangelogFile = Boolean.parseBoolean(atts
						.getValue("relativeToChangelogFile"));

				if (isRelativeToChangelogFile) {
					File changeLogFile = new File(databaseChangeLog
							.getPhysicalFilePath());
					File resourceBase = new File(changeLogFile.getParent(),
							pathName);
					if (!resourceBase.exists()) {
						throw new SAXException(
								"Resource directory for includeAll does not exist ["
										+ resourceBase.getPath() + "]");
					}
					pathName = resourceBase.getPath() + '/';
					pathName = pathName.replace('\\', '/');
				}

				Enumeration<URL> resourcesEnum = resourceAccessor.getResources(pathName);
                SortedSet<URL> resources = new TreeSet<URL>(new Comparator<URL>() {
                    public int compare(URL o1, URL o2) {
                        return o1.toString().compareTo(o2.toString());
                    }
                });
                while (resourcesEnum.hasMoreElements()) {
                    resources.add(resourcesEnum.nextElement());
                }

				boolean foundResource = false;

                Set<String> seenPaths = new HashSet<String>();
				for (URL fileUrl : resources) {
					if (!fileUrl.toExternalForm().startsWith("file:")) {
                        if (fileUrl.toExternalForm().startsWith("jar:file:") || fileUrl.toExternalForm().startsWith("wsjar:file:") || fileUrl.toExternalForm().startsWith("zip:")) {
                            File zipFileDir = extractZipFile(fileUrl);
                            fileUrl = new File(zipFileDir, pathName).toURL();
                        } else {
                            log.debug(fileUrl.toExternalForm()
                                    + " is not a file path");
                            continue;
                        }
					}
					File file = new File(fileUrl.toURI());
					log.debug("includeAll using path "
							+ file.getCanonicalPath());
					if (!file.exists()) {
						throw new SAXException("includeAll path " + pathName
								+ " could not be found.  Tried in "
								+ file.toString());
					}
					if (file.isDirectory()) {
						log.debug(file.getCanonicalPath() + " is a directory");
						for (File childFile : new TreeSet<File>(Arrays.asList(file.listFiles()))) {
                            String path = pathName+ childFile.getName();
                            if (!seenPaths.add(path)) {
                                log.debug("already included "+path);
                                continue;
                            }

                            if (handleIncludedChangeLog(path, false, databaseChangeLog.getPhysicalFilePath())) {
								foundResource = true;
							}
						}
					} else {
                        String path = pathName + file.getName();
                        if (!seenPaths.add(path)) {
                            log.debug("already included "+path);
                            continue;
                        }
                        if (handleIncludedChangeLog(path, false, databaseChangeLog.getPhysicalFilePath())) {
							foundResource = true;
						}
					}
				}

				if (!foundResource) {
					throw new SAXException( "Could not find directory or directory was empty for includeAll '" + pathName + "'");
				}
			} else if (changeSet == null && "changeSet".equals(qName)) {
				boolean alwaysRun = false;
				boolean runOnChange = false;
				if ("true".equalsIgnoreCase(atts.getValue("runAlways"))) {
					alwaysRun = true;
				}
				if ("true".equalsIgnoreCase(atts.getValue("runOnChange"))) {
					runOnChange = true;
				}
				String filePath = atts.getValue("logicalFilePath");
				if (filePath == null || "".equals(filePath)) {
					filePath = databaseChangeLog.getFilePath();
				}

				changeSet = new ChangeSet(atts.getValue("id"), atts.getValue("author"), alwaysRun, runOnChange, filePath,
						atts.getValue("context"), atts.getValue("dbms"),
						Boolean.valueOf(atts.getValue("runInTransaction")));
				if (StringUtils.trimToNull(atts.getValue("failOnError")) != null) {
					changeSet.setFailOnError(Boolean.parseBoolean(atts.getValue("failOnError")));
				}
                if (StringUtils.trimToNull(atts.getValue("onValidationFail")) != null) {
                    changeSet.setOnValidationFail(ChangeSet.ValidationFailOption.valueOf(atts.getValue("onValidationFail")));
                }
			} else if (changeSet != null && "rollback".equals(qName)) {
				text = new StringBuffer();
				String id = atts.getValue("changeSetId");
				if (id != null) {
					String path = atts.getValue("changeSetPath");
					if (path == null) {
						path = databaseChangeLog.getFilePath();
					}
					String author = atts.getValue("changeSetAuthor");
					ChangeSet changeSet = databaseChangeLog.getChangeSet(path,
							author, id);
					if (changeSet == null) {
						throw new SAXException(
								"Could not find changeSet to use for rollback: "
										+ path + ":" + author + ":" + id);
					} else {
						for (Change change : changeSet.getChanges()) {
							this.changeSet.addRollbackChange(change);
						}
					}
				}
				inRollback = true;
			} else if ("preConditions".equals(qName)) {
				rootPrecondition = new PreconditionContainer();
				rootPrecondition.setOnFail(StringUtils.trimToNull(atts
						.getValue("onFail")));
				rootPrecondition.setOnError(StringUtils.trimToNull(atts
						.getValue("onError")));
				rootPrecondition.setOnFailMessage(StringUtils.trimToNull(atts
						.getValue("onFailMessage")));
				rootPrecondition.setOnErrorMessage(StringUtils.trimToNull(atts
						.getValue("onErrorMessage")));
				rootPrecondition.setOnSqlOutput(StringUtils.trimToNull(atts
						.getValue("onSqlOutput")));
				preconditionLogicStack.push(rootPrecondition);
			} else if (currentPrecondition != null && currentPrecondition instanceof CustomPreconditionWrapper && qName.equals("param")) {
				((CustomPreconditionWrapper) currentPrecondition).setParam(atts.getValue("name"), atts.getValue("value"));
			} else if (rootPrecondition != null) {
				currentPrecondition = PreconditionFactory.getInstance().create(
						qName);

				for (int i = 0; i < atts.getLength(); i++) {
					String attributeName = atts.getQName(i);
					String attributeValue = atts.getValue(i);
					setProperty(currentPrecondition, attributeName,
							attributeValue);
				}
				preconditionLogicStack.peek().addNestedPrecondition(
						currentPrecondition);

				if (currentPrecondition instanceof PreconditionLogic) {
					preconditionLogicStack
							.push(((PreconditionLogic) currentPrecondition));
				}

				if ("sqlCheck".equals(qName)) {
					text = new StringBuffer();
				}
			} else if ("modifySql".equals(qName)) {
				inModifySql = true;
				if (StringUtils.trimToNull(atts.getValue("dbms")) != null) {
					modifySqlDbmsList = new HashSet<String>(StringUtils
							.splitAndTrim(atts.getValue("dbms"), ","));
				}
				if (StringUtils.trimToNull(atts.getValue("context")) != null) {
					modifySqlContexts = new HashSet<String>(StringUtils
							.splitAndTrim(atts.getValue("context"), ","));
				}
				if (StringUtils.trimToNull(atts.getValue("applyToRollback")) != null) {
					modifySqlAppliedOnRollback = Boolean.valueOf(atts
							.getValue("applyToRollback"));
				}
			} else if (inModifySql) {
				SqlVisitor sqlVisitor = SqlVisitorFactory.getInstance().create(
						qName);
				for (int i = 0; i < atts.getLength(); i++) {
					String attributeName = atts.getQName(i);
					String attributeValue = atts.getValue(i);
					setProperty(sqlVisitor, attributeName, attributeValue);
				}
				sqlVisitor.setApplicableDbms(modifySqlDbmsList);
				sqlVisitor.setApplyToRollback(modifySqlAppliedOnRollback);
				sqlVisitor.setContexts(modifySqlContexts);

				changeSet.addSqlVisitor(sqlVisitor);
			} else if (changeSet != null && change == null) {
				change = ChangeFactory.getInstance().create(localName);
				if (change == null) {
					throw new SAXException("Unknown Liquibase extension: "
							+ localName
							+ ".  Are you missing a jar from your classpath?");
				}
				change.setChangeSet(changeSet);
				text = new StringBuffer();
				if (change == null) {
					throw new MigrationFailedException(changeSet,
							"Unknown change: " + localName);
				}
				change.setResourceAccessor(resourceAccessor);
				if (change instanceof CustomChangeWrapper) {
					((CustomChangeWrapper) change)
							.setClassLoader(resourceAccessor.toClassLoader());
				}
				for (int i = 0; i < atts.getLength(); i++) {
					String attributeName = atts.getLocalName(i);
					String attributeValue = atts.getValue(i);
					setProperty(change, attributeName, attributeValue);
				}
				
				change.setChangeLogParameters(this.changeLogParameters);
				change.init();
			} else if (change != null && "column".equals(qName)) {
				ColumnConfig column;
				if (change instanceof LoadDataChange) {
					column = new LoadDataColumnConfig();
				} else {
					column = new ColumnConfig();
				}
				for (int i = 0; i < atts.getLength(); i++) {
					String attributeName = atts.getQName(i);
					String attributeValue = atts.getValue(i);
					setProperty(column, attributeName, attributeValue);
				}
				if (change instanceof ChangeWithColumns) {
					((ChangeWithColumns) change).addColumn(column);
				} else {
					throw new RuntimeException("Unexpected column tag for "
							+ change.getClass().getName());
				}
			} else if (change != null && "constraints".equals(qName)) {
				ConstraintsConfig constraints = new ConstraintsConfig();
				for (int i = 0; i < atts.getLength(); i++) {
					String attributeName = atts.getQName(i);
					String attributeValue = atts.getValue(i);
					setProperty(constraints, attributeName, attributeValue);
				}
				ColumnConfig lastColumn = null;
                if (change instanceof ChangeWithColumns) {
                    List<ColumnConfig> columns = ((ChangeWithColumns) change).getColumns();
                    if (columns != null && columns.size() > 0) {
                        lastColumn = columns.get(columns.size() - 1);
                    }
                } else {
					throw new RuntimeException("Unexpected change: "
							+ change.getClass().getName());
				}
                if (lastColumn == null) {
                    throw new RuntimeException("Could not determine column to add constraint to");
                }
				lastColumn.setConstraints(constraints);
			} else if ("param".equals(qName)) {
				if (change instanceof CustomChangeWrapper) {
					if (atts.getValue("value") == null) {
						paramName = atts.getValue("name");
						text = new StringBuffer();
					} else {
						((CustomChangeWrapper) change).setParam(atts
								.getValue("name"), atts.getValue("value"));
					}
				} else {
					throw new MigrationFailedException(changeSet,
							"'param' unexpected in " + qName);
				}
			} else if ("where".equals(qName)) {
				text = new StringBuffer();
			} else if ("property".equals(qName)) {
				String context = StringUtils.trimToNull(atts.getValue("context"));
				String dbms = StringUtils.trimToNull(atts.getValue("dbms"));
				if (StringUtils.trimToNull(atts.getValue("file")) == null) {
					this.changeLogParameters.set(atts.getValue("name"), atts.getValue("value"), context, dbms);
				} else {
					Properties props = new Properties();
					InputStream propertiesStream = resourceAccessor.getResourceAsStream(atts.getValue("file"));
					if (propertiesStream == null) {
						log.info("Could not open properties file "
								+ atts.getValue("file"));
					} else {
						props.load(propertiesStream);

						for (Map.Entry entry : props.entrySet()) {
							this.changeLogParameters.set(entry.getKey().toString(), entry.getValue().toString(), context, dbms);
						}
					}
				}
			} else if (change instanceof ExecuteShellCommandChange
					&& "arg".equals(qName)) {
				((ExecuteShellCommandChange) change).addArg(atts
						.getValue("value"));
			} else if (change != null) {
				String creatorMethod = "create"
						+ localName.substring(0, 1).toUpperCase()
						+ localName.substring(1);

				Object objectToCreateFrom;
				if (changeSubObjects.size() == 0) {
					objectToCreateFrom = change;
				} else {
					objectToCreateFrom = changeSubObjects.peek();
				}

				Method method;
				try {
					method = objectToCreateFrom.getClass().getMethod(
							creatorMethod);
				} catch (NoSuchMethodException e) {
					throw new MigrationFailedException(changeSet,
							"Could not find creator method " + creatorMethod
									+ " for tag: " + qName);
				}
				Object subObject = method.invoke(objectToCreateFrom);
				for (int i = 0; i < atts.getLength(); i++) {
					String attributeName = atts.getQName(i);
					String attributeValue = atts.getValue(i);
					setProperty(subObject, attributeName, attributeValue);
				}
				changeSubObjects.push(subObject);

			} else {
				throw new MigrationFailedException(changeSet,
						"Unexpected tag: " + qName);
			}
		} catch (Exception e) {
			log.severe("Error thrown as a SAXException: " + e.getMessage(), e);
			e.printStackTrace();
			throw new SAXException(e);
		}
	}

	protected boolean handleIncludedChangeLog(String fileName,
			boolean isRelativePath, String relativeBaseFileName)
			throws LiquibaseException {
		if (!(fileName.endsWith(".xml") || fileName.endsWith(".sql"))) {
			log.debug(relativeBaseFileName + "/" + fileName
					+ " is not a recognized file type");
			return false;
		}

        if (fileName.equalsIgnoreCase(".svn") || fileName.equalsIgnoreCase("cvs")) {
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
			handleChangeSet(changeSet);
		}

		return true;
	}

	private void setProperty(Object object, String attributeName,
			String attributeValue) throws IllegalAccessException,
			InvocationTargetException, CustomChangeException {
		if (object instanceof CustomChangeWrapper) {
			if (attributeName.equals("class")) {
				((CustomChangeWrapper) object).setClass(changeLogParameters
						.expandExpressions(attributeValue));
			} else {
				((CustomChangeWrapper) object).setParam(attributeName,
						changeLogParameters.expandExpressions(attributeValue));
			}
		} else {
			ObjectUtil.setProperty(object, attributeName, changeLogParameters
					.expandExpressions(attributeValue));
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		String textString = null;
		if (text != null && text.length() > 0) {
			textString = changeLogParameters.expandExpressions(StringUtils
					.trimToNull(text.toString()));
		}

		try {
			if (changeSubObjects.size() > 0) {
				changeSubObjects.pop();
			} else if (rootPrecondition != null) {
				if ("preConditions".equals(qName)) {
					if (changeSet == null) {
						databaseChangeLog.setPreconditions(rootPrecondition);
						handlePreCondition(rootPrecondition);
					} else {
						changeSet.setPreconditions(rootPrecondition);
					}
					rootPrecondition = null;
				} else if ("and".equals(qName)) {
					preconditionLogicStack.pop();
					currentPrecondition = null;
				} else if ("or".equals(qName)) {
					preconditionLogicStack.pop();
					currentPrecondition = null;
				} else if ("not".equals(qName)) {
					preconditionLogicStack.pop();
					currentPrecondition = null;
				} else if (qName.equals("sqlCheck")) {
					((SqlPrecondition) currentPrecondition).setSql(textString);
					currentPrecondition = null;
				} else if (qName.equals("customPrecondition")) {
					((CustomPreconditionWrapper) currentPrecondition).setClassLoader(resourceAccessor.toClassLoader());
                    currentPrecondition = null;
				}

			} else if (changeSet != null && "rollback".equals(qName)) {
				changeSet.addRollBackSQL(textString);
				inRollback = false;
			} else if (change != null && change instanceof RawSQLChange
					&& "comment".equals(qName)) {
				((RawSQLChange) change).setComments(textString);
				text = new StringBuffer();
			} else if (change != null && "where".equals(qName)) {
				if (change instanceof UpdateDataChange) {
					((UpdateDataChange) change).setWhereClause(textString);
				} else if (change instanceof DeleteDataChange) {
					((DeleteDataChange) change).setWhereClause(textString);
				} else {
					throw new RuntimeException("Unexpected change type: "
							+ change.getClass().getName());
				}
				text = new StringBuffer();
			} else if (change != null
					&& change instanceof CreateProcedureChange
					&& "comment".equals(qName)) {
				((CreateProcedureChange) change).setComments(textString);
				text = new StringBuffer();
			} else if (change != null && change instanceof CustomChangeWrapper
					&& paramName != null && "param".equals(qName)) {
				((CustomChangeWrapper) change).setParam(paramName, textString);
				text = new StringBuffer();
				paramName = null;
			} else if (changeSet != null && "comment".equals(qName)) {
				changeSet.setComments(textString);
				text = new StringBuffer();
			} else if (changeSet != null && "changeSet".equals(qName)) {
				handleChangeSet(changeSet);
				changeSet = null;
			} else if (change != null && qName.equals("column")
					&& textString != null) {
				if (change instanceof InsertDataChange) {
					List<ColumnConfig> columns = ((InsertDataChange) change)
							.getColumns();
					columns.get(columns.size() - 1).setValue(textString);
				} else if (change instanceof UpdateDataChange) {
					List<ColumnConfig> columns = ((UpdateDataChange) change)
							.getColumns();
					columns.get(columns.size() - 1).setValue(textString);
				} else {
					throw new RuntimeException("Unexpected column with text: "
							+ textString);
				}
				this.text = new StringBuffer();
			} else if (change != null
					&& localName.equals(change.getChangeMetaData().getName())) {
				if (textString != null) {
					if (change instanceof RawSQLChange) {
						// We've already expanded expressions when we defined 'textString' above. If we enabled
						// escaping, we cannot re-expand; the now-literal variables in the text would get
						// incorrectly expanded. If we haven't enabled escaping, then retain the current behavior.
						String expandedExpression = textString;
						
						if (false == ChangeLogParameters.EnableEscaping) {
							expandedExpression = changeLogParameters.expandExpressions(textString);
						}
						((RawSQLChange) change).setSql(expandedExpression);
					} else if (change instanceof CreateProcedureChange) {
						((CreateProcedureChange) change)
								.setProcedureBody(textString);
						// } else if (change instanceof AlterViewChange) {
						// ((AlterViewChange)
						// change).setSelectQuery(textString);
					} else if (change instanceof CreateViewChange) {
						((CreateViewChange) change).setSelectQuery(textString);
					} else if (change instanceof StopChange) {
						((StopChange) change).setMessage(textString);
					} else {
						throw new RuntimeException("Unexpected text in "
								+ change.getChangeMetaData().getName());
					}
				}
				text = null;
				if (inRollback) {
					changeSet.addRollbackChange(change);
				} else {
					changeSet.addChange(change);
				}
				change = null;
			} else if (changeSet != null && "validCheckSum".equals(qName)) {
				changeSet.addValidCheckSum(text.toString());
				text = null;
			} else if ("modifySql".equals(qName)) {
				inModifySql = false;
				modifySqlDbmsList = null;
				modifySqlContexts = null;
				modifySqlAppliedOnRollback = false;
			}
		} catch (Exception e) {
			log.severe("Error thrown as a SAXException: " + e.getMessage(), e);
			throw new SAXException(databaseChangeLog.getPhysicalFilePath()
					+ ": " + e.getMessage(), e);
		}
	}

	protected void handlePreCondition(
			@SuppressWarnings("unused") Precondition precondition) {
		databaseChangeLog.setPreconditions(rootPrecondition);
	}

	protected void handleChangeSet(ChangeSet changeSet) {
		databaseChangeLog.addChangeSet(changeSet);
	}

	@Override
	public void characters(char ch[], int start, int length)
			throws SAXException {
		if (text != null) {
			text.append(new String(ch, start, length));
		}
	}

	/**
	 * Wrapper for Attributes that expands the value as needed
	 */
	private class ExpandingAttributes implements Attributes {
		private Attributes attributes;

		private ExpandingAttributes(Attributes attributes) {
			this.attributes = attributes;
		}

		public int getLength() {
			return attributes.getLength();
		}

		public String getURI(int index) {
			return attributes.getURI(index);
		}

		public String getLocalName(int index) {
			return attributes.getLocalName(index);
		}

		public String getQName(int index) {
			return attributes.getQName(index);
		}

		public String getType(int index) {
			return attributes.getType(index);
		}

		public String getValue(int index) {
			return attributes.getValue(index);
		}

		public int getIndex(String uri, String localName) {
			return attributes.getIndex(uri, localName);
		}

		public int getIndex(String qName) {
			return attributes.getIndex(qName);
		}

		public String getType(String uri, String localName) {
			return attributes.getType(uri, localName);
		}

		public String getType(String qName) {
			return attributes.getType(qName);
		}

		public String getValue(String uri, String localName) {
			return changeLogParameters.expandExpressions(attributes.getValue(
					uri, localName));
		}

		public String getValue(String qName) {
			return changeLogParameters.expandExpressions(attributes
					.getValue(qName));
		}
	}

    static File extractZipFile(URL resource) throws IOException {
        String file = resource.getFile();
        String path = file.split("!")[0];
        if(path.matches("file:\\/[A-Za-z]:\\/.*")) {
            path = path.replaceFirst("file:\\/", "");
        }else {
            path = path.replaceFirst("file:", "");
        }
        path = URLDecoder.decode(path);
        File zipfile = new File(path);

        File tempDir = File.createTempFile("liquibase-sax", ".dir");
        tempDir.delete();
        tempDir.mkdir();
//        tempDir.deleteOnExit();

        JarFile jarFile = new JarFile(zipfile);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            File entryFile = new File(tempDir, entry.getName());
            entryFile.mkdirs();
        }

        return tempDir;
    }
}
