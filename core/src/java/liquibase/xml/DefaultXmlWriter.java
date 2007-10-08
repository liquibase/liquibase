package liquibase.xml;

import org.w3c.dom.Document;

import java.io.OutputStream;
import java.io.IOException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class DefaultXmlWriter implements XmlWriter {

    public void write(Document doc, OutputStream outputStream) throws IOException {
        OutputFormat format = new OutputFormat(doc);
        format.setIndenting(true);
        XMLSerializer serializer = new XMLSerializer(outputStream, format);
        serializer.asDOMSerializer();
        serializer.serialize(doc);
    }
}
