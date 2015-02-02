package liquibase.actionlogic;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Instead of writing {@link liquibase.actionlogic.ActionLogic} implementations for classes that simply rewrite Actions as a text-based commands (e.g. SQL),
 * this class consumes a template/DSL that defines the rewrote text. Liquibase will scan the classpath looking for files that end in ".logic" and auto-create instances based on those files.
 *
 * The template language is a simple subset of <a href="https://velocity.apache.org"Apache Velocity</a> which only supports:
 * <ul>
 *     <li>$variable output</li>
 *     <li>#if #else #end (no nesting of #if statements supported)</li>
 *     <li>Single line comments on lines starting with '##'</li>
 * </ul>
 *
 * Plus only these functions:
 * <ul>
 *     <li>#escapeName($catalogName, $schemaName, $objectName, objectType.class)</li>
 *     <li>#escapeName($objectName, objectType.class)</li>
 * </ul>
 *
 * Additional processing logic:
 * <ul>
 *     <li>All line endings are stripped to make a single line</li>
 *     <li>All spaces are collapsed to a single space in the original template. Spaces within variable values are preserved.</li>
 * </ul>
 *
 * In addition to the result template itself, the logic file begins with control/metadata settings using lines starting with ">". The following control settings are supported:
 * <ul>
 *     <li><b>priority (required)</b> sets the {@link liquibase.actionlogic.ActionLogic#getPriority(liquibase.action.Action, liquibase.Scope)} value. Example: "&gt;priority: 10"</li>
 *     <li><b>action (required)</b> sets the {@link liquibase.action.Action} class this logic file supports. Example: "&gt;action: liquibase.action.core.CreateTableAction"</li>
 *     <li><b>required</b> defines a comma separated list of fields on the Action instance that must be set to a non-null value. Example: "&gt;required: tableName, type"</li>
 *     <li><b>unsupported</b> defines a comma separated list of fields on the Action instance that cannot be set to a non-null value. Example: "&gt;unsupported: cacheSize, cycle"</li>
 * </ul>
 */
public class TemplateActionLogic extends AbstractActionLogic {

    private Integer priority;
    private String template;
    private final Pattern CONTROL_PATTERN = Pattern.compile("^>\\s*([a-zA-Z]+)[:\\s]+(.*)");
    private final Pattern VARIABLE_PATTERN = Pattern.compile("\\$([a-zA-Z]+)");
    private final Pattern FUNCTION_PATTERN = Pattern.compile("\\#([a-zA-Z]+)\\((.*?)\\)", Pattern.MULTILINE | Pattern.DOTALL);
    private final Pattern IF_PATTERN = Pattern.compile("#if\\s*\\((.*?)\\)(.*?)#end", Pattern.MULTILINE | Pattern.DOTALL);

    private Class<? extends Action> supportedAction;
    private List<String> requiredAttributes;
    private List<String> unsupportedAttributes;

    public TemplateActionLogic(String template) throws ParseException {
        parseTemplate(template);
    }

    protected void parseTemplate(String spec) throws ParseException {
        if (spec == null) {
            throw new ParseException("Null specification");
        }
        List<String> lines = new ArrayList<String>(Arrays.asList(spec.split("\\r?\\n")));

        ListIterator<String> itr = lines.listIterator();
        while (itr.hasNext()) {
            String line = itr.next();
            if (line.startsWith(">")) {
                Matcher matcher = CONTROL_PATTERN.matcher(line);
                if (matcher.matches()) {
                    readControlLine(matcher.group(1), matcher.group(2));
                    itr.remove();
                }
            } else if (line.startsWith("##")) {
                itr.remove();
            }
        }
        this.template = StringUtils.join(lines, "\n");

        verify();
    }

    protected void verify() throws ParseException {
        if (priority == null) {
            throw new ParseException("Missing '>priority: ##' configuration");
        }
        if (supportedAction == null) {
            throw new ParseException("Missing '>action: com.example.ActionClass' configuration");
        }

    }

    protected void readControlLine(String command, String value) throws ParseException {
        value = StringUtils.trimToNull(value);
        if (value == null) {
            return;
        }
        if (command.equalsIgnoreCase("priority")) {
            if (this.priority != null) {
                throw new ParseException(">priority header set multiple times");
            }

            this.priority = Integer.valueOf(value);

            if (this.priority <= 0) {
                throw new ParseException("Priority '>priority: "+this.priority+"' must be greater than zero");
            }
        } else if (command.equalsIgnoreCase("action")) {
            try {
                if (this.supportedAction != null) {
                    throw new ParseException(">action header set multiple times");
                }

                this.supportedAction = (Class<? extends Action>) Class.forName(value);

                if (!Action.class.isAssignableFrom(supportedAction)) {
                    throw new ParseException("Class in '>action: " + value + "' must implement liquibase.action.Action");
                }
            } catch (ClassNotFoundException e) {
                throw new ParseException("Cannot find action class " + value, e);
            }
        } else if (command.equalsIgnoreCase("required")) {
            if (this.requiredAttributes != null) {
                throw new ParseException(">required header set multiple times");
            }
            this.requiredAttributes = Arrays.asList(value.split("\\s*,\\s*"));
        } else if (command.equalsIgnoreCase("unsupported")) {
            if (this.unsupportedAttributes != null) {
                throw new ParseException(">unsupported header set multiple times");
            }
            this.unsupportedAttributes = Arrays.asList(value.split("\\s*,\\s*"));
        } else {
            throw new ParseException("Unknown control command: "+command);
        }
    }

    @Override
    public int getPriority(Action action, Scope scope) {
        if (supportedAction.isAssignableFrom(action.getClass())) {
            return priority;
        }
        return PRIORITY_NOT_APPLICABLE;
    }

    protected String getTemplate() {
        return template;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors validate = super.validate(action, scope);
        if (requiredAttributes != null) {
            for (String attr : requiredAttributes) {
                validate.checkRequiredField(attr, action.get(attr, Object.class));
            }
        }

        if (unsupportedAttributes != null) {
            for (String attr : unsupportedAttributes) {
                validate.checkDisallowedField(attr, action.get(attr, Object.class), scope.get(Scope.Attr.database, Database.class));
            }
        }
        return validate;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        String finalString;
        try {
            finalString = fillTemplate(action, scope);
        } catch (ParseException e) {
            throw new ActionPerformException(e);
        }

        return new DelegateResult(createRewriteAction(finalString));
    }

    protected ExecuteSqlAction createRewriteAction(String finalSql) {
        return new ExecuteSqlAction(finalSql);
    }

    protected String fillTemplate(Action action, Scope scope) throws ParseException {
        String template = StringUtils.trimToNull(getTemplate());
        if (template == null) {
            return "";
        }

        template = evaluateIfStatements(template, action, scope);

        template = template.replaceAll("[\\s\r\n]+", " ").trim();

        template = callFunctions(template, action, scope);
        template = replaceVariables(template, action, scope);

        return template;
    }

    protected String evaluateIfStatements(String template, Action action, Scope scope) throws ParseException {
        Matcher matcher = IF_PATTERN.matcher(template);
        while(matcher.find()) {
            String expression = matcher.group(1);
            String body = matcher.group(2);

            if (body.contains("#if")) {
                throw new ParseException("Cannot nest #if statements");
            }

            String[] ifElse = body.split("\\s*#else\\s*");

            if (ifElse.length == 1) {
                ifElse = new String[] {
                        ifElse[0],
                        ""
                };
            } else if (ifElse.length > 2) {
                throw new ParseException("Cannot include multiple #else clauses in an #if statement");
            }

            String replaceValue;
            if (evaluateExpression(expression, action, scope)) {
                replaceValue = ifElse[0];
            } else {
                replaceValue = ifElse[1];
            }
            template = template.replace(matcher.group(), (" "+ replaceValue.trim() +" "));
        }
        return template;
    }

    protected boolean evaluateExpression(String expression, Action action, Scope scope) throws ParseException {
        expression = expression.trim();
        if (VARIABLE_PATTERN.matcher(expression).matches()) {
            expression = expression.substring(1);
            Object value = action.get(expression, Object.class);
            if (value == null) {
                value = scope.get(expression, Object.class);
            }
            if (value==null) {
                return false;
            }
            if (value instanceof Boolean && !((Boolean) value)) {
                return false;
            }
            return true;
        }
        throw new ParseException("Invalid expression: '"+expression+"'");
    }

    protected String callFunctions(String template, Action action, Scope scope) throws ParseException {
        Matcher matcher = FUNCTION_PATTERN.matcher(template);
        while (matcher.find()) {
            String function = matcher.group(1);
            String[] args = matcher.group(2).split("\\s*,\\s*");
            args[0] = args[0].trim();
            args[args.length-1] = args[args.length-1].trim();
            String functionResult = callFunction(function, args, action, scope);
            if (functionResult != null) {
                template = template.replace(matcher.group(), functionResult);
            }
        }
        return template;
    }

    protected String callFunction(String function, String[] args, Action action, Scope scope) throws ParseException {
        if (function.equals("escapeName")) {
            return escapeName(args, action, scope);
        } else {
            return null;
        }
    }

    protected String escapeName(String[] args, Action action, Scope scope) throws ParseException {
        Database database = scope.get(Scope.Attr.database, Database.class);

        String className;
        if (args.length == 2) {
            className = args[1];
        } else if (args.length == 4) {
            className = args[3];
        } else {
            throw new ParseException("Unsupported number of operations for escapeName: "+args.length);
        }

        className = className.replaceFirst("\\.class$", "");
        if (!className.contains(".")) {
            className = "liquibase.structure.core."+className;
        }

        Class<? extends DatabaseObject> objectType;
        try {
            objectType = (Class<? extends DatabaseObject>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ParseException("Cannot find class of type to escape: "+className, e);
        }

        if (args.length == 2) {
            return database.escapeObjectName(args[0], objectType);
        } else {
            return database.escapeObjectName(args[0], args[1], args[2], objectType);
        }
    }

    protected String replaceVariables(String template, Action action, Scope scope) {
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        while (matcher.find()) {
            String variable = matcher.group(1);
            String value = action.get(variable, String.class);

            if (value == null) {
                value = scope.get(variable, String.class);
            }

            if (value != null) {
                template = template.replace("$"+variable, value);
            }
        }
        return template;
    }

    public static final class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }

        public ParseException(String message, Throwable cause) {
            super(message, cause);
        }

        public ParseException(Throwable cause) {
            super(cause);
        }
    }
}
