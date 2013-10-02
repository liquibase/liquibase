package liquibase.datatype;

import liquibase.util.StringUtils;

public class DatabaseDataType {

    private String type;

    public DatabaseDataType(String type) {
        this.type = type;
    }

    public void addAdditionalInformation(String additionalInformation) {
        if (additionalInformation != null) {
            this.type += " "+additionalInformation;
        }
    }
    
    public DatabaseDataType(String name, Object... parameters) {
        this.type = name;

        String[] stringParams = new String[parameters.length];
        if (parameters.length > 0) {
            for (int i=0; i<parameters.length; i++){
                if (parameters[i] == null) {
                    stringParams[i] = "NULL";
                } else {
                    stringParams[i] = parameters[i].toString();
                }
            }
            type += "("+ StringUtils.join(stringParams, ", ")+")";
        }
    }

    /**
     * Mainly for postgres, check if the column is a serial data type.
     * @return Whether the type is serial
     */
    public boolean isAutoIncrement() {
        return type.equalsIgnoreCase("serial") || type.equalsIgnoreCase("bigserial");
    }

    public String toSql() {
        return toString();
    }
    
    @Override
    public String toString() {
        return type;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

}
