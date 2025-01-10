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
 *       &lt;attGroup ref="{http://www.liquibase.org/xml/ns/dbchangelog}changeAttributes"/&gt;
 *       &lt;attGroup ref="{http://www.liquibase.org/xml/ns/dbchangelog}dropAllForeignKeyConstraintsAttrib"/&gt;
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
@XmlRootElement(name = "dropAllForeignKeyConstraints")
public class DropAllForeignKeyConstraints {

    @XmlAttribute(name = "baseTableCatalogName")
    protected String baseTableCatalogName;
    @XmlAttribute(name = "baseTableSchemaName")
    protected String baseTableSchemaName;
    @XmlAttribute(name = "baseTableName", required = true)
    protected String baseTableName;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the baseTableCatalogName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBaseTableCatalogName() {
        return baseTableCatalogName;
    }

    /**
     * Sets the value of the baseTableCatalogName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBaseTableCatalogName(String value) {
        this.baseTableCatalogName = value;
    }

    /**
     * Gets the value of the baseTableSchemaName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBaseTableSchemaName() {
        return baseTableSchemaName;
    }

    /**
     * Sets the value of the baseTableSchemaName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBaseTableSchemaName(String value) {
        this.baseTableSchemaName = value;
    }

    /**
     * Gets the value of the baseTableName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBaseTableName() {
        return baseTableName;
    }

    /**
     * Sets the value of the baseTableName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBaseTableName(String value) {
        this.baseTableName = value;
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
