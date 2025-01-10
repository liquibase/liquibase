//
// This file was generated by the Eclipse Implementation of JAXB, v3.0.2 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2025.01.10 at 10:45:55 AM MST 
//


package liquibase.api.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlMixed;
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
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.liquibase.org/xml/ns/dbchangelog}comment" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attGroup ref="{http://www.liquibase.org/xml/ns/dbchangelog}changeAttributes"/&gt;
 *       &lt;attribute name="catalogName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="schemaName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="procedureName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="path" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="encoding" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="relativeToChangelogFile" type="{http://www.liquibase.org/xml/ns/dbchangelog}booleanExp" /&gt;
 *       &lt;attribute name="dbms" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="replaceIfExists" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "content"
})
@XmlRootElement(name = "createProcedure")
public class CreateProcedure {

    @XmlElementRef(name = "comment", namespace = "http://www.liquibase.org/xml/ns/dbchangelog", type = JAXBElement.class, required = false)
    @XmlMixed
    protected List<Serializable> content;
    @XmlAttribute(name = "catalogName")
    protected String catalogName;
    @XmlAttribute(name = "schemaName")
    protected String schemaName;
    @XmlAttribute(name = "procedureName")
    protected String procedureName;
    @XmlAttribute(name = "path")
    protected String path;
    @XmlAttribute(name = "encoding")
    protected String encoding;
    @XmlAttribute(name = "relativeToChangelogFile")
    protected String relativeToChangelogFile;
    @XmlAttribute(name = "dbms")
    protected String dbms;
    @XmlAttribute(name = "replaceIfExists")
    protected Boolean replaceIfExists;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the content property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link String }
     * 
     * 
     */
    public List<Serializable> getContent() {
        if (content == null) {
            content = new ArrayList<Serializable>();
        }
        return this.content;
    }

    /**
     * Gets the value of the catalogName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCatalogName() {
        return catalogName;
    }

    /**
     * Sets the value of the catalogName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCatalogName(String value) {
        this.catalogName = value;
    }

    /**
     * Gets the value of the schemaName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchemaName() {
        return schemaName;
    }

    /**
     * Sets the value of the schemaName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchemaName(String value) {
        this.schemaName = value;
    }

    /**
     * Gets the value of the procedureName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcedureName() {
        return procedureName;
    }

    /**
     * Sets the value of the procedureName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcedureName(String value) {
        this.procedureName = value;
    }

    /**
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPath(String value) {
        this.path = value;
    }

    /**
     * Gets the value of the encoding property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the value of the encoding property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEncoding(String value) {
        this.encoding = value;
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
     * Gets the value of the dbms property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDbms() {
        return dbms;
    }

    /**
     * Sets the value of the dbms property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDbms(String value) {
        this.dbms = value;
    }

    /**
     * Gets the value of the replaceIfExists property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isReplaceIfExists() {
        return replaceIfExists;
    }

    /**
     * Sets the value of the replaceIfExists property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setReplaceIfExists(Boolean value) {
        this.replaceIfExists = value;
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
