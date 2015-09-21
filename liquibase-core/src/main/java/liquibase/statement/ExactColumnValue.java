/**
 * Copyright 2015 SirsiDynix.  All rights reserved.
 */

package liquibase.statement;

/**
 * For storing a value in a Column which should be put directly into the SQL statement
 * without changes 
 * <br>
 * <br>
 * For Example:
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;nextval('xxx')
 * <br>
 * for a sequence generated key.
 * <br>
 * <br>
 * Can't just use a String column - it will get quoted, for example, if Liquibase sql handling
 * thinks the value is a String.
 *
 */
public class ExactColumnValue {

    private String value;

    public ExactColumnValue(String exactColumnValue) {
        this.value = exactColumnValue;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExactColumnValue) {
            return this.toString().equals(obj.toString());
        } else {
            return super.equals(obj);
        }
    }
}
