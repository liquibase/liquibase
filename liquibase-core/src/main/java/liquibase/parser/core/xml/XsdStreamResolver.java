package liquibase.parser.core.xml;

import java.io.InputStream;

public abstract class XsdStreamResolver {

	protected XsdStreamResolver successor;

	public abstract InputStream getResourceAsStream(String xsdFile);

	public void setSuccessor(XsdStreamResolver successor) {
		this.successor = successor;
	}

	public InputStream getSuccessorValue(String xsdFile){
		if(successor != null){
			return successor.getResourceAsStream(xsdFile);
		}
		return null;
	}
}
