package liquibase.xml;

import org.w3c.dom.Document;

import java.io.OutputStream;
import java.io.IOException;

import liquibase.DatabaseChangeLog;

public interface XmlWriter {
    public void write(Document doc, OutputStream outputStream) throws IOException;
}
