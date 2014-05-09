package liquibase.changelog;

import liquibase.Contexts;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.visitor.ValidatingVisitor;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.*;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.xml.IncludeAllFilter;
import liquibase.precondition.Conditional;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.resource.ResourceAccessor;
import liquibase.util.FileUtil;
import liquibase.util.file.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

/**
 * Encapsulates the information stored in the change log XML file.
 */
public class DatabaseChangeLog implements Comparable<DatabaseChangeLog>, Conditional {
    private PreconditionContainer preconditionContainer = new PreconditionContainer();
    private String physicalFilePath;
    private String logicalFilePath;
    private ObjectQuotingStrategy objectQuotingStrategy;

    private List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
    private ChangeLogParameters changeLogParameters;

    public DatabaseChangeLog() {
    }

    public DatabaseChangeLog(String physicalFilePath) {
        this.physicalFilePath = physicalFilePath;
    }

    @Override
    public PreconditionContainer getPreconditions() {
        return preconditionContainer;
    }

    @Override
    public void setPreconditions(PreconditionContainer precond) {
        if (precond == null) {
            this.preconditionContainer = new PreconditionContainer();
        } else {
            preconditionContainer = precond;
        }
    }


    public ChangeLogParameters getChangeLogParameters() {
        return changeLogParameters;
    }

    public void setChangeLogParameters(ChangeLogParameters changeLogParameters) {
        this.changeLogParameters = changeLogParameters;
    }

    public String getPhysicalFilePath() {
        return physicalFilePath;
    }

    public void setPhysicalFilePath(String physicalFilePath) {
        this.physicalFilePath = physicalFilePath;
    }

    public String getLogicalFilePath() {
        String returnPath = logicalFilePath;
        if (logicalFilePath == null) {
            returnPath = physicalFilePath;
        }
        return returnPath.replaceAll("\\\\", "/");
    }

    public void setLogicalFilePath(String logicalFilePath) {
        this.logicalFilePath = logicalFilePath;
    }

    public String getFilePath() {
        if (logicalFilePath == null) {
            return physicalFilePath;
        } else {
            return logicalFilePath;
        }
    }

    public ObjectQuotingStrategy getObjectQuotingStrategy() {
        return objectQuotingStrategy;
    }

    public void setObjectQuotingStrategy(ObjectQuotingStrategy objectQuotingStrategy) {
        this.objectQuotingStrategy = objectQuotingStrategy;
    }

    @Override
    public String toString() {
        return getFilePath();
    }

    @Override
    public int compareTo(DatabaseChangeLog o) {
        return getFilePath().compareTo(o.getFilePath());
    }


    public ChangeSet getChangeSet(String path, String author, String id) {
        for (ChangeSet changeSet : changeSets) {
            if (changeSet.getFilePath().equalsIgnoreCase(path)
                    && changeSet.getAuthor().equalsIgnoreCase(author)
                    && changeSet.getId().equalsIgnoreCase(id)
                    && (changeSet.getDbmsSet() == null
                    || changeLogParameters == null
                    || changeLogParameters.getValue("database.typeName") == null
                    || changeSet.getDbmsSet().isEmpty()
                    || changeSet.getDbmsSet().contains(changeLogParameters.getValue("database.typeName").toString()))) {
                return changeSet;
            }
        }

        return null;
    }

    public List<ChangeSet> getChangeSets() {
        return changeSets;
    }

    public void addChangeSet(ChangeSet changeSet) {
        this.changeSets.add(changeSet);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseChangeLog that = (DatabaseChangeLog) o;

        return getFilePath().equals(that.getFilePath());

    }

    @Override
    public int hashCode() {
        return getFilePath().hashCode();
    }

    public void validate(Database database, String... contexts) throws LiquibaseException {
        this.validate(database, new Contexts(contexts));
    }

    public void validate(Database database, Contexts contexts) throws LiquibaseException {

        ChangeLogIterator logIterator = new ChangeLogIterator(this, new DbmsChangeSetFilter(database), new ContextChangeSetFilter(contexts));

        ValidatingVisitor validatingVisitor = new ValidatingVisitor(database.getRanChangeSetList());
        validatingVisitor.validate(database, this);
        logIterator.run(validatingVisitor, database);

        for (String message : validatingVisitor.getWarnings().getMessages()) {
            LogFactory.getLogger().warning(message);
        }

        if (!validatingVisitor.validationPassed()) {
            throw new ValidationFailedException(validatingVisitor);
        }
    }

    public ChangeSet getChangeSet(RanChangeSet ranChangeSet) {
        return getChangeSet(ranChangeSet.getChangeLog(), ranChangeSet.getAuthor(), ranChangeSet.getId());
    }

    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParseException, SetupException {
        setLogicalFilePath(parsedNode.getChildValue(null, "logicalFilePath", String.class));

        Object value = parsedNode.getValue();
        if (value != null) {
            if (value instanceof ParsedNode) {
                handleChildNode(((ParsedNode) value), resourceAccessor);
            } else if (value instanceof Collection) {
                for (Object childValue : ((Collection) value)) {
                    if (childValue instanceof ParsedNode) {
                        handleChildNode((ParsedNode) childValue, resourceAccessor);
                    }
                }
            }
        }

        for (ParsedNode childNode : parsedNode.getChildren()) {
            handleChildNode(childNode, resourceAccessor);
        }
    }

    protected void handleChildNode(ParsedNode node, ResourceAccessor resourceAccessor) throws ParseException, SetupException {
        String nodeName = node.getNodeName();
        if (nodeName.equals("changeSet")) {
            this.addChangeSet(createChangeSet(node, resourceAccessor));
        } else if (nodeName.equals("include")) {
            String path = node.getChildValue(null, "file", String.class);
            path = path.replace('\\', '/');
            try {
                include(path, node.getChildValue(null, "relativeToChangelogFile", false), resourceAccessor);
            } catch (LiquibaseException e) {
                throw new SetupException(e);
            }
        } else if (nodeName.equals("includeAll")) {
            String path = node.getChildValue(null, "path", String.class);
            String resourceFilterDef = node.getChildValue(null, "resourceFilter", String.class);
            IncludeAllFilter resourceFilter = null;
            if (resourceFilterDef != null) {
                try {
                    resourceFilter = (IncludeAllFilter) Class.forName(resourceFilterDef).newInstance();
                } catch (Exception e) {
                    throw new SetupException(e);
                }
            }

            includeAll(path, node.getChildValue(null, "relativeToChangelogFile", false), resourceFilter, resourceAccessor);
        } else if (nodeName.equals("preConditions")) {
            this.preconditionContainer = new PreconditionContainer();
            this.preconditionContainer.load(node, resourceAccessor);
        }
    }

    public void includeAll(String pathName, boolean isRelativeToChangelogFile, IncludeAllFilter resourceFilter, ResourceAccessor resourceAccessor) throws SetupException {
        try {
            pathName = pathName.replace('\\', '/');

            if (!(pathName.endsWith("/"))) {
                pathName = pathName + '/';
            }
            Logger log = LogFactory.getInstance().getLog();
            log.debug("includeAll for " + pathName);
            log.debug("Using file opener for includeAll: " + resourceAccessor.toString());

            if (isRelativeToChangelogFile) {
                File changeLogFile = null;

                Enumeration<URL> resources = resourceAccessor.getResources(this.getPhysicalFilePath());
                while (resources.hasMoreElements()) {
                    try {
                        changeLogFile = new File(resources.nextElement().toURI());
                    } catch (URISyntaxException e) {
                        continue; //ignore error, probably a URL or something like that
                    }
                    if (changeLogFile.exists()) {
                        break;
                    } else {
                        changeLogFile = null;
                    }
                }

                if (changeLogFile == null) {
                    throw new SetupException("Cannot determine physical location of " + this.getPhysicalFilePath());
                }

                File resourceBase = new File(changeLogFile.getParentFile(), pathName);

                if (!resourceBase.exists()) {
                    throw new SetupException("Resource directory for includeAll does not exist [" + resourceBase.getAbsolutePath() + "]");
                }

                pathName = resourceBase.getPath();
                pathName = pathName.replaceFirst("^\\Q" + changeLogFile.getParentFile().getAbsolutePath() + "\\E", "");
                pathName = this.getFilePath().replaceFirst("/[^/]*$", "") + pathName;
                pathName = pathName.replace('\\', '/');
                if (!pathName.endsWith("/")) {
                    pathName = pathName + "/";
                }

                while (pathName.matches(".*/\\.\\./.*")) {
                    pathName = pathName.replaceFirst("/[^/]+/\\.\\./", "/");
                }
            }

            Enumeration<URL> resourcesEnum = resourceAccessor.getResources(pathName);
            SortedSet<URL> resources = new TreeSet<URL>(new Comparator<URL>() {
                @Override
                public int compare(URL o1, URL o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });
            while (resourcesEnum.hasMoreElements()) {
                resources.add(resourcesEnum.nextElement());
            }

            boolean foundResource = false;

            Set<String> seenPaths = new HashSet<String>();
            List<String> includedChangeLogs = new LinkedList<String>();
            for (URL fileUrl : resources) {
                if (!fileUrl.toExternalForm().startsWith("file:")) {
                    if (fileUrl.toExternalForm().startsWith("jar:file:") || fileUrl.toExternalForm().startsWith("wsjar:file:")
                            || fileUrl.toExternalForm().startsWith("zip:")) {
                        File zipFileDir = FileUtil.extractZipFile(fileUrl);
                        if (pathName.startsWith("classpath:")) {
                            log.debug("replace classpath");
                            pathName = pathName.replaceFirst("classpath:", "");
                        }
                        if (pathName.startsWith("classpath*:")) {
                            log.debug("replace classpath*");
                            pathName = pathName.replaceFirst("classpath\\*:", "");
                        }
                        URI fileUri = new File(zipFileDir, pathName).toURI();
                        fileUrl = fileUri.toURL();
                    } else {
                        log.debug(fileUrl.toExternalForm() + " is not a file path");
                        continue;
                    }
                }
                File file = new File(fileUrl.toURI());
                log.debug("includeAll using path " + file.getCanonicalPath());
                if (!file.exists()) {
                    throw new SetupException("includeAll path " + pathName + " could not be found.  Tried in " + file.toString());
                }
                if (file.isDirectory()) {
                    log.debug(file.getCanonicalPath() + " is a directory");
                    for (File childFile : new TreeSet<File>(Arrays.asList(file.listFiles()))) {
                        String path = pathName + childFile.getName();
                        if (!seenPaths.add(path)) {
                            log.debug("already included " + path);
                            continue;
                        }

                        includedChangeLogs.add(path);
                    }
                } else {
                    String path = pathName + file.getName();
                    if (!seenPaths.add(path)) {
                        log.debug("already included " + path);
                        continue;
                    }
                    includedChangeLogs.add(path);
                }
            }
            if (resourceFilter != null) {
                includedChangeLogs = resourceFilter.filter(includedChangeLogs);
            }

            for (String path : includedChangeLogs) {
                if (include(path, false, resourceAccessor)) {
                    foundResource = true;
                }
            }

            if (!foundResource) {
                throw new SetupException("Could not find directory or directory was empty for includeAll '" + pathName + "'");
            }
        } catch (Exception e) {
            throw new SetupException(e);
        }
    }

    protected boolean include(String fileName, boolean isRelativePath, ResourceAccessor resourceAccessor) throws LiquibaseException {

        if (fileName.equalsIgnoreCase(".svn") || fileName.equalsIgnoreCase("cvs")) {
            return false;
        }

        String relativeBaseFileName = this.getPhysicalFilePath();
        if (isRelativePath) {
            // workaround for FilenameUtils.normalize() returning null for relative paths like ../conf/liquibase.xml
            String tempFile = FilenameUtils.concat(FilenameUtils.getFullPath(relativeBaseFileName), fileName);
            if (tempFile != null && new File(tempFile).exists() == true) {
                fileName = tempFile;
            } else {
                fileName = FilenameUtils.getFullPath(relativeBaseFileName) + fileName;
            }
        }
        DatabaseChangeLog changeLog;
        try {
            changeLog = ChangeLogParserFactory.getInstance().getParser(fileName, resourceAccessor).parse(fileName, changeLogParameters, resourceAccessor);
        } catch (UnknownChangelogFormatException e) {
            LogFactory.getInstance().getLog().warning("included file " + relativeBaseFileName + "/" + fileName + " is not a recognized file type");
            return false;
        }
        PreconditionContainer preconditions = changeLog.getPreconditions();
        if (preconditions != null) {
            if (null == this.getPreconditions()) {
                this.setPreconditions(new PreconditionContainer());
            }
            this.getPreconditions().addNestedPrecondition(preconditions);
        }
        for (ChangeSet changeSet : changeLog.getChangeSets()) {
            this.changeSets.add(changeSet);
        }

        return true;
    }

    protected ChangeSet createChangeSet(ParsedNode node, ResourceAccessor resourceAccessor) throws ParseException, SetupException {
        ChangeSet changeSet = new ChangeSet(this);
        changeSet.load(node, resourceAccessor);
        return changeSet;
    }


}
