package liquibase.precondition;

import liquibase.Scope;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.*;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.util.ObjectUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class CustomPreconditionWrapper extends AbstractPrecondition {

    private String className;

    private SortedSet<String> params = new TreeSet<>();
    private Map<String, String> paramValues = new LinkedHashMap<>();

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setClass(String className) {
        this.className = className;
    }

    public String getParamValue(String key) {
        return paramValues.get(key);
    }

    public void setParam(String name, String value) {
        this.params.add(name);
        this.paramValues.put(name, value);
    }

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
    
    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
            throws PreconditionFailedException, PreconditionErrorException {
        CustomPrecondition customPrecondition;
        try {
//            System.out.println(classLoader.toString());
            try {
                customPrecondition = (CustomPrecondition) Class.forName(className, true, Scope.getCurrentScope().getClassLoader()).getConstructor().newInstance();
            } catch (ClassCastException e) { //fails in Ant in particular
                customPrecondition = (CustomPrecondition) Class.forName(className).getConstructor().newInstance();
            }
        } catch (Exception e) {
            throw new PreconditionFailedException("Could not open custom precondition class "+className, changeLog, this);
        }

        for (String param : params) {
            try {
                ObjectUtil.setProperty(customPrecondition, param, paramValues.get(param));
            } catch (Exception e) {
                throw new PreconditionFailedException("Error setting parameter "+param+" on custom precondition "+className, changeLog, this);
            }
        }

        try {
            customPrecondition.check(database);
        } catch (CustomPreconditionFailedException e) {
            throw new PreconditionFailedException(new FailedPrecondition("Custom Precondition Failed: "+e.getMessage(), changeLog, this));
        } catch (CustomPreconditionErrorException e) {
            throw new PreconditionErrorException(new ErrorPrecondition(e, changeLog, this));
        }
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public Set<String> getSerializableFields() {
        return new LinkedHashSet<>(Arrays.asList("className", "param"));
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        return field.equals("param") ? paramValues
                                     : super.getSerializableFieldValue(field);
    }

    @Override
    public String getName() {
        return "customPrecondition";
    }

    @Override
    protected boolean shouldAutoLoad(ParsedNode node) {
        if ("params".equals(node.getName()) || "param".equals(node.getName())) {
            return false;
        }
        return super.shouldAutoLoad(node);
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        setClassName(parsedNode.getChildValue(null, "className", String.class));

        ParsedNode paramsNode = parsedNode.getChild(null, "params");
        if (paramsNode == null) {
            paramsNode = parsedNode;
        }

        for (ParsedNode child : paramsNode.getChildren(null, "param")) {
            Object value = child.getValue();
            if (value == null) {
                value = child.getChildValue(null, "value");
            }
            if (value != null) {
                value = value.toString();
            }
            this.setParam(child.getChildValue(null, "name", String.class), (String) value);
        }
        super.load(parsedNode, resourceAccessor);

    }
}
