package liquibase.change;

public interface ChangeWithTablespace {

    String getTablespace();

    void setTablespace(String tablespace);

}
