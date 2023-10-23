package liquibase.util;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Various utility methods for working with XML.
 */
public abstract class XMLUtil {
    /**
     * Extracts the text from the given element.
     * {@link Node#getTextContent()} returns the text from ALL children, this returns the text only for this element.
     */
    public static String getTextContent(Node element) {
        StringBuilder text = new StringBuilder();
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child instanceof Text) {
                text.append(child.getNodeValue());
            }
        }

        return text.toString();
    }
}
