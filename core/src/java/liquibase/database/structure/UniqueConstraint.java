package liquibase.database.structure;

import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UniqueConstraint implements DatabaseObject, Comparable<UniqueConstraint>
{
  private String       name;
  private Table        table;
  private List<String> columns = new ArrayList<String>();


    public DatabaseObject[] getContainingObjects() {
        List<DatabaseObject> columns = new ArrayList<DatabaseObject>();
        for (String column : this.columns) {
            columns.add(new Column().setName(column).setTable(table));
        }

        return columns.toArray(new DatabaseObject[columns.size()]);
    }

    public String getName () {
    return name;
  }

  public void setName (String constraintName) {
    this.name = constraintName;
  }

  public Table getTable () {
    return table;
  }

  public void setTable (Table table) {
    this.table = table;
  }

  public List<String> getColumns () {
    return columns;
  }

  public String getColumnNames () {
    return StringUtils.join(columns, ", ");
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo (UniqueConstraint o) {
    int returnValue = this.getTable().getName().compareTo(o.getTable().getName());
    if (returnValue == 0) {
      returnValue = this.getName().compareTo(o.getName());
    }
    if (returnValue == 0) {
      returnValue = this.getColumnNames().compareTo(o.getColumnNames());
    }
    return returnValue;
  }

  @Override
  public int hashCode () {
    int result = 0;
    if (this.table != null) {
      result = this.table.hashCode();
    }
    if (this.name != null) {
      result = 31 * result + this.name.toUpperCase().hashCode();
    }
    if (getColumnNames() != null) {
      result = 31 * result + getColumnNames().hashCode();
    }
    return result;
  }

}
