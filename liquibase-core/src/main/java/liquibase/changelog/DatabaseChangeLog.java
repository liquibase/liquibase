package liquibase.changelog;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.RuntimeEnvironment;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.filter.LabelChangeSetFilter;
import liquibase.changelog.visitor.ValidatingVisitor;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.*;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.precondition.Conditional;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.resource.ResourceAccessor;
import liquibase.util.file.FilenameUtils;

import java.io.File;
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

    private RuntimeEnvironment runtimeEnvironment;

    public DatabaseChangeLog() {
    }

    public DatabaseChangeLog(String physicalFilePath) {
        this.physicalFilePath = physicalFilePath;
    }

    public RuntimeEnvironment getRuntimeEnvironment() {
        return runtimeEnvironment;
    }

    public void setRuntimeEnvironment(RuntimeEnvironment runtimeEnvironment) {
        this.runtimeEnvironment = runtimeEnvironment;
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
        this.validate(database, new Contexts(contexts), new LabelExpression());
    }

    /**
     * @deprecated Use LabelExpression version
     */
    public void validate(Database database, Contexts contexts) throws LiquibaseException {
        this.validate(database, contexts, new LabelExpression());
    }

    public void validate(Database database, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {

        ChangeLogIterator logIterator = new ChangeLogIterator(this, new DbmsChangeSetFilter(database), new ContextChangeSetFilter(contexts), new LabelChangeSetFilter(labelExpression));

        ValidatingVisitor validatingVisitor = new ValidatingVisitor(database.getRanChangeSetList());
        validatingVisitor.validate(database, this);
        logIterator.run(validatingVisitor, new RuntimeEnvironment(database, contexts, labelExpression));

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

    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException, SetupException {
        setLogicalFilePath(parsedNode.getChildValue(null, "logicalFilePath", String.class));

        for (ParsedNode childNode : parsedNode.getChildren()) {
            handleChildNode(childNode, resourceAccessor);
        }
    }

    protected void handleChildNode(ParsedNode node, ResourceAccessor resourceAccessor) throws ParsedNodeException, SetupException {
        String nodeName = node.getName();
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

            includeAll(path, node.getChildValue(null, "relativeToChangelogFile", false), resourceFilter, getStandardChangeLogComparator(), resourceAccessor);
        } else if (nodeName.equals("preConditions")) {
            this.preconditionContainer = new PreconditionContainer();
            try {
                this.preconditionContainer.load(node, resourceAccessor);
            } catch (ParsedNodeException e) {
                e.printStackTrace();
            }
        }
    }

    public void includeAll(String pathName, boolean isRelativeToChangelogFile, IncludeAllFilter resourceFilter, Comparator<String> resourceComparator, ResourceAccessor resourceAccessor) throws SetupException {
        try {
            pathName = pathName.replace('\\', '/');

            if (!(pathName.endsWith("/"))) {
                pathName = pathName + '/';
            }
            Logger log = LogFactory.getInstance().getLog();
            log.debug("includeAll for " + pathName);
            log.debug("Using file opener for includeAll: " + resourceAccessor.toString());

            String relativeTo = null;
            if (isRelativeToChangelogFile) {
                relativeTo = this.getPhysicalFilePath();
            }

            Set<String> unsortedResources = resourceAccessor.list(relativeTo, pathName, true, false, true);
            SortedSet<String> resources = new TreeSet<String>(resourceComparator);
            if (unsortedResources != null) {
                for (String resourcePath : unsortedResources) {
                    if (resourceFilter == null || resourceFilter.include(resourcePath)) {
                        resources.add(resourcePath);
                    }
                }
            }

            if (resources.size() == 0) {
                throw new SetupException("Could not find directory or directory was empty for includeAll '" + pathName + "'");
            }

            for (String path : resources) {
                include(path, false, resourceAccessor);
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

    protected ChangeSet createChangeSet(ParsedNode node, ResourceAccessor resourceAccessor) throws ParsedNodeException, SetupException {
        ChangeSet changeSet = new ChangeSet(this);
        changeSet.setChangeLogParameters(this.getChangeLogParameters());
        try {
            changeSet.load(node, resourceAccessor);
        } catch (ParsedNodeException e) {
            e.printStackTrace();
        }
        return changeSet;
    }

    protected Comparator<String> getStandardChangeLogComparator() {
        return new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1. compareTo(o2);
            }
        };
    }

}
