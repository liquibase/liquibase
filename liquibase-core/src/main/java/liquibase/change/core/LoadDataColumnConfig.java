package liquibase.change.core;

import liquibase.change.ColumnConfig;

public class LoadDataColumnConfig extends ColumnConfig {

    private Integer index;
    private String header;

   public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

}