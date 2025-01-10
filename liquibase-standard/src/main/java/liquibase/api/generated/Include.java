//
// This file was generated by the Eclipse Implementation of JAXB, v3.0.2 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2025.01.10 at 10:45:55 AM MST 
//


package liquibase.api.generated;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="author" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="file" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="relativeToChangelogFile" type="{http://www.liquibase.org/xml/ns/dbchangelog}booleanExp" /&gt;
 *       &lt;attribute name="errorIfMissing" type="{http://www.liquibase.org/xml/ns/dbchangelog}booleanExp" default="true" /&gt;
 *       &lt;attribute name="context" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="contextFilter" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="labels" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="ignore" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="created" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="logicalFilePath" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "include")
public class Include {

    @XmlAttribute(name = "id")
    protected String id;
    @XmlAttribute(name = "author")
    protected String author;
    @XmlAttribute(name = "file", required = true)
    protected String file;
    @XmlAttribute(name = "relativeToChangelogFile")
    protected String relativeToChangelogFile;
    @XmlAttribute(name = "errorIfMissing")
    protected String errorIfMissing;
    @XmlAttribute(name = "context")
    protected String context;
    @XmlAttribute(name = "contextFilter")
    protected String contextFilter;
    @XmlAttribute(name = "labels")
    protected String labels;
    @XmlAttribute(name = "ignore")
    protected String ignore;
    @XmlAttribute(name = "created")
    protected String created;
    @XmlAttribute(name = "logicalFilePath")
    protected String logicalFilePath;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the author property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the value of the author property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthor(String value) {
        this.author = value;
    }

    /**
     * Gets the value of the file property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the value of the file property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFile(String value) {
        this.file = value;
    }

    /**
     * Gets the value of the relativeToChangelogFile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    /**
     * Sets the value of the relativeToChangelogFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelativeToChangelogFile(String value) {
        this.relativeToChangelogFile = value;
    }

    /**
     * Gets the value of the errorIfMissing property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorIfMissing() {
        if (errorIfMissing == null) {
            return "true";
        } else {
            return errorIfMissing;
        }
    }

    /**
     * Sets the value of the errorIfMissing property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorIfMissing(String value) {
        this.errorIfMissing = value;
    }

    /**
     * Gets the value of the context property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContext() {
        return context;
    }

    /**
     * Sets the value of the context property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContext(String value) {
        this.context = value;
    }

    /**
     * Gets the value of the contextFilter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContextFilter() {
        return contextFilter;
    }

    /**
     * Sets the value of the contextFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContextFilter(String value) {
        this.contextFilter = value;
    }

    /**
     * Gets the value of the labels property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabels() {
        return labels;
    }

    /**
     * Sets the value of the labels property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabels(String value) {
        this.labels = value;
    }

    /**
     * Gets the value of the ignore property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIgnore() {
        return ignore;
    }

    /**
     * Sets the value of the ignore property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIgnore(String value) {
        this.ignore = value;
    }

    /**
     * Gets the value of the created property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreated() {
        return created;
    }

    /**
     * Sets the value of the created property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreated(String value) {
        this.created = value;
    }

    /**
     * Gets the value of the logicalFilePath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogicalFilePath() {
        return logicalFilePath;
    }

    /**
     * Sets the value of the logicalFilePath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogicalFilePath(String value) {
        this.logicalFilePath = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
