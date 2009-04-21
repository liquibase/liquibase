package liquibase.change;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

    public Element createNode(Document document) {
        Element element = super.createNode(document);
        if (getIndex() != null) {
            element.setAttribute("index", getIndex().toString());
        }

        if (getHeader() != null) {
            element.setAttribute("header", getHeader());
        }
        return element;
    }
}