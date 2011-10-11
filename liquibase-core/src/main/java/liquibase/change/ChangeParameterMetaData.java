package liquibase.change;

public class ChangeParameterMetaData {
    private String parameterName;
    private String displayName;
    private String type;

    public ChangeParameterMetaData(String parameterName, String displayName, String type) {
        this.parameterName = parameterName;
        this.displayName = displayName;
        this.type = type;
    }
}
