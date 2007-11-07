package liquibase.xml;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.OutputStream;

public class DefaultXmlWriter implements XmlWriter {

    public void write(Document doc, OutputStream outputStream) throws IOException {
        OutputFormat format = new OutputFormat(doc);
        format.setIndenting(true);
        XMLSerializer serializer = new XMLSerializer(outputStream, format);
        serializer.asDOMSerializer();
        serializer.serialize(doc);
    }
}
