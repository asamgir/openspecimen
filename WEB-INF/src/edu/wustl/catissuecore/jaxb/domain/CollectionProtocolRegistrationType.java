//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.06.03 at 06:02:46 PM IST 
//


package edu.wustl.catissuecore.jaxb.domain;

import javax.xml.bind.annotation.AccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CollectionProtocolRegistrationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CollectionProtocolRegistrationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CollectionProtocol" type="{}CollectionProtocolType"/>
 *         &lt;element name="SCGCollection" type="{}SCGCollectionType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(AccessType.FIELD)
@XmlType(name = "CollectionProtocolRegistrationType", propOrder = {
    "collectionProtocol",
    "scgCollection"
})
public class CollectionProtocolRegistrationType {

    @XmlElement(name = "CollectionProtocol")
    protected CollectionProtocolType collectionProtocol;
    @XmlElement(name = "SCGCollection")
    protected SCGCollectionType scgCollection;

    /**
     * Gets the value of the collectionProtocol property.
     * 
     * @return
     *     possible object is
     *     {@link CollectionProtocolType }
     *     
     */
    public CollectionProtocolType getCollectionProtocol() {
        return collectionProtocol;
    }

    /**
     * Sets the value of the collectionProtocol property.
     * 
     * @param value
     *     allowed object is
     *     {@link CollectionProtocolType }
     *     
     */
    public void setCollectionProtocol(CollectionProtocolType value) {
        this.collectionProtocol = value;
    }

    /**
     * Gets the value of the scgCollection property.
     * 
     * @return
     *     possible object is
     *     {@link SCGCollectionType }
     *     
     */
    public SCGCollectionType getSCGCollection() {
        return scgCollection;
    }

    /**
     * Sets the value of the scgCollection property.
     * 
     * @param value
     *     allowed object is
     *     {@link SCGCollectionType }
     *     
     */
    public void setSCGCollection(SCGCollectionType value) {
        this.scgCollection = value;
    }

}