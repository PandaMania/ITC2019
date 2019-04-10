package Parsing;

import entities.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class DistributionParser {

    public Distribution parse(XMLEventReader reader, StartElement el) throws XMLStreamException {
        // Create distribution
        Distribution distribution = new Distribution();
        // Read ID
        Attribute type = el.getAttributeByName(QName.valueOf("type"));
        distribution.type = String.valueOf(type.getValue());
        // Use reader to expand until we hit the EndElement of the course
        Attribute required= el.getAttributeByName(QName.valueOf("required"));
        distribution.required= Boolean.parseBoolean(required.getValue());


        Attribute penalty= el.getAttributeByName(QName.valueOf("penalty"));
        distribution.penalty= Integer.parseInt(required.getValue());

        XMLEvent event = reader.nextEvent();
        while(!(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("distribution"))) {
            if (event.isStartElement()) {
            StartElement startElement = event.asStartElement();
            String name = startElement.getName().getLocalPart();
            if(name.equals("id")){
                Attribute idAttribute=startElement.getAttributeByName(QName.valueOf("id"));
                distribution.idInDistribution.add(Integer.parseInt(idAttribute.getValue()));
            }

            }
            else{
                throw new IllegalArgumentException("Unexpected element encountered");
            }
        }
        return distribution;
    }
}
