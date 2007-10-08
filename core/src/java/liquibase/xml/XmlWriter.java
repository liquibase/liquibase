package liquibase.xml;

import org.w3c.dom.Document;

import java.io.OutputStream;
import java.io.IOException;

public interface XmlWriter {
    public void write(Document doc, OutputStream outputStream) throws IOException;
}
