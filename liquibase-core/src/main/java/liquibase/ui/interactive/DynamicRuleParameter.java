package liquibase.ui.interactive;


import java.util.Objects;


public class DynamicRuleParameter {

    /**
     * The actual underlying DynamicRuleParameterEnum that corresponds to this value. We store the parameter as a string,
     * because different versions of Liquibase might not contain the enum that the rule requires. We want to be able to
     * parse newer conf files with older versions of Liquibase, without a SnakeYaml error that it cannot find the enum
     * value.
     */
    private String parameter;
    private Object value;

    /**
     * Constructor for SnakeYaml
     */
    public DynamicRuleParameter() {
    }

    public DynamicRuleParameter(IInteractivelyPromptableEnum parameter, Object value) {
        Objects.requireNonNull(parameter);
        this.parameter = parameter.toString();
        this.value = value;
    }

    public DynamicRuleParameter(String parameter, Object value) {
        Objects.requireNonNull(parameter);
        this.parameter = parameter;
        this.value = value;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamicRuleParameter that = (DynamicRuleParameter) o;
        return Objects.equals(parameter, that.parameter) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameter, value);
    }
}

