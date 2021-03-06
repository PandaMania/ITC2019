package Parsing;

import entities.distribution.Distribution;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class DistributionParser {

    public Distribution parse(XMLEventReader reader, StartElement el) throws XMLStreamException {
        // Create distribution
        // Read ID
        Attribute typeAttr = el.getAttributeByName(QName.valueOf("type"));
        String type = String.valueOf(typeAttr.getValue());

        Distribution distribution = Distribution.get(type);

        distribution.type = type;
        // Use reader to expand until we hit the EndElement of the course
        Attribute required = el.getAttributeByName(QName.valueOf("required"));
        Attribute penalty = el.getAttributeByName(QName.valueOf("penalty"));

        if (required != null) {
            distribution.required = Boolean.parseBoolean(required.getValue());
        } else if (penalty != null) {
            distribution.setPenalty( Integer.parseInt(penalty.getValue()));
        } else {
            throw new IllegalArgumentException("either penalty or required should be specified");
        }
        XMLEvent event = reader.nextEvent();
        while (!(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("distribution"))) {
            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                String name = startElement.getName().getLocalPart();
                if (name.equals("class")) {
                    Attribute idAttribute = startElement.getAttributeByName(QName.valueOf("id"));
                    distribution.idInDistribution.add(Integer.parseInt(idAttribute.getValue()));
                }else {
                    throw new IllegalArgumentException("Unexpected element encountered");

                }

            }
            event = reader.nextEvent();
        }

        return distribution;
    }
}

