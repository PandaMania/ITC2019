package Parsing;

import entities.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.BitSet;

public class RoomParser {

    public Room parse(XMLEventReader reader, StartElement el) throws XMLStreamException {
        // Create Room
        Room room = new Room();
        // Read ID
        Attribute capacity =  el.getAttributeByName(QName.valueOf("capacity"));
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
            }else if(name.equals("unavailable")){
                Attribute weeksAttr= startElement.getAttributeByName(QName.valueOf("weeks"));
                String weeks= weeksAttr.getValue();

                Attribute lengthAttr= startElement.getAttributeByName(QName.valueOf("length"));
                String   length= lengthAttr.getValue();

                Attribute startAttr= startElement.getAttributeByName(QName.valueOf("start"));
                String   start= startAttr.getValue();

                Attribute daysAttr= startElement.getAttributeByName(QName.valueOf("days"));
                String   days= daysAttr.getValue();

                room.unaivailableweeks.add(new Unavailability(BitSet.valueOf(weeks.getBytes()),BitSet.valueOf(days.getBytes()), Integer.parseInt(length),Integer.parseInt(start)));

            } else{
                throw new IllegalArgumentException("Unexpected element encountered");
            }
        }
            event = reader.nextEvent();

        }
        return room;
    }

}
