package Parsing;

import entities.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class RoomParser {

    public Room parse(XMLEventReader reader, StartElement el) throws XMLStreamException {
        // Create Room
        Room room = new Room();
        // Read ID
        Attribute capacity =  el.getAttributeByName(QName.valueOf("room capacity"));
        room.capacity = Integer.parseInt(capacity.getValue());

        Attribute id = el.getAttributeByName(QName.valueOf("id"));
        room.id = Integer.parseInt(id.getValue());

        XMLEvent event = reader.nextEvent();
        while(!(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("room"))) {
        if(event.isStartElement()){
            StartElement startElement = event.asStartElement();
            String name = startElement.getName().getLocalPart();
            if(name.equals("travel")){
                Attribute roomidAttr= startElement.getAttributeByName(QName.valueOf("room"));
                Integer roomid= Integer.parseInt(roomidAttr.getValue());

                Attribute distAttr = startElement.getAttributeByName(QName.valueOf("value"));
                Integer distance = Integer.parseInt(distAttr.getValue());
                room.distanceToRooms.put(roomid,distance);
            }
        }
        else{
            throw new IllegalArgumentException("Unexpected element encountered");
        }

        }
        return room;
    }

}
