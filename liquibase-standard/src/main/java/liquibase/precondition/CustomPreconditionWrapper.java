package liquibase.precondition;

import liquibase.GlobalConfiguration;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class CustomPreconditionWrapper extends AbstractPrecondition {

    private String className;

    private final SortedSet<String> params = new TreeSet<>();
    private final Map<String, String> paramValues = new LinkedHashMap<>();

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
        // CWE-470 hard gate, paired with the defense-in-depth gate inside check().
        // Hard error so the operator sees a clean pre-execution failure when the
        // embedder has disabled custom-Java elements via
        // liquibase.allowCustomChange=false. Naming-note: the flag is shared with
        // customChange (see GlobalConfiguration.ALLOW_CUSTOM_CHANGE) because both
        // elements expose identical Class.forName(initialize=true) load surfaces.
        if (Boolean.FALSE.equals(GlobalConfiguration.ALLOW_CUSTOM_CHANGE.getCurrentValue())) {
            return new ValidationErrors().addError(
                    "customPrecondition is disabled by configuration " +
                            "(liquibase.allowCustomChange=false). To run customPrecondition (and " +
                            "customChange) elements, set liquibase.allowCustomChange=true (the default). " +
                            "This flag is provided for environments that execute changelogs from " +
                            "less-trusted sources, where loading an arbitrary JVM class by name via " +
                            "changelog is not an acceptable risk.");
        }
        return new ValidationErrors();
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
            throws PreconditionFailedException, PreconditionErrorException {
        // CWE-470 defense-in-depth gate: fire BEFORE any of the two Class.forName
        // call sites below. The audit notes that preconditions are evaluated
        // before changes execute, providing a separate class-load trigger point
        // that fires even if the change body is never reached — e.g. an
        // onFail=MARK_RAN precondition that "always passes" but whose load-time
        // static <clinit> already ran. Throwing PreconditionErrorException
        // (not PreconditionFailedException) bypasses onFail handling — we do NOT
        // want MARK_RAN to silently swallow the embedder's configured-off intent.
        if (Boolean.FALSE.equals(GlobalConfiguration.ALLOW_CUSTOM_CHANGE.getCurrentValue())) {
            throw new PreconditionErrorException(new ErrorPrecondition(
                    new CustomPreconditionErrorException(
                            "customPrecondition is disabled by configuration " +
                                    "(liquibase.allowCustomChange=false). To run customPrecondition (and " +
                                    "customChange) elements, set liquibase.allowCustomChange=true (the " +
                                    "default). This flag is provided for environments that execute " +
                                    "changelogs from less-trusted sources, where loading an arbitrary " +
                                    "JVM class by name via changelog is not an acceptable risk."),
                    changeLog, this));
        }

        CustomPrecondition customPrecondition;
        try {
//            System.out.println(classLoader.toString());
            try {
                customPrecondition = (CustomPrecondition) Class.forName(className, true, Scope.getCurrentScope().getClassLoader()).getConstructor().newInstance();
            } catch (ClassCastException e) { //fails in Ant in particular
                customPrecondition = (CustomPrecondition) Class.forName(className).getConstructor().newInstance();
            }
        } catch (Exception e) {
            throw new PreconditionFailedException("Could not open custom precondition class "+className, changeLog, this, e);
        }

        for (String param : params) {
            try {
                ObjectUtil.setProperty(customPrecondition, param, paramValues.get(param));
            } catch (Exception e) {
                throw new PreconditionFailedException("Error setting parameter "+param+" on custom precondition "+className, changeLog, this, e);
            }
        }

        try {
            customPrecondition.check(database);
        } catch (CustomPreconditionFailedException e) {
            throw new PreconditionFailedException(new FailedPrecondition("Custom Precondition Failed: "+e.getMessage(), changeLog, this), e);
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
