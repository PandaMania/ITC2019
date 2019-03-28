package Parsing;

import entities.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class CourseParser {


    public Course parse(XMLEventReader reader, StartElement el) throws XMLStreamException {
        // Create course
        Course course = new Course();
        // Read ID
        Attribute id = el.getAttributeByName(QName.valueOf("id"));
        course.id = Integer.parseInt(id.getValue());
        // Use reader to expand until we hit the EndElement of the course

        // We don't have to initialize these, as the order
        // in which we encounter them guarantees that they will be assigned
        CourseConfiguration currentConfig = null;
        SubPart currentSubPart = null;
        CourseClass currentClass = null;

        XMLEvent event = reader.nextEvent();
        while(!(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("course"))){
            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                String name = startElement.getName().getLocalPart();
                if(name.equals("config")){
                    CourseConfiguration config = new CourseConfiguration();
                    Attribute idAttribute = startElement.getAttributeByName(QName.valueOf("id"));
                    config.id = Integer.parseInt(idAttribute.getValue());
                    currentConfig = config;
                    course.configs.add(config);
                }
                else if(name.equals("subpart")){
                    SubPart subPart = new SubPart();
                    Attribute idAttribute = startElement.getAttributeByName(QName.valueOf("id"));
                    subPart.id = Integer.parseInt(idAttribute.getValue());
                    currentSubPart = subPart;
                    currentConfig.subparts.add(subPart);
                }
                else if(name.equals("class")){
                    CourseClass cclass = new CourseClass();
                    Attribute idAttribute = startElement.getAttributeByName(QName.valueOf("id"));
                    cclass.id = idAttribute.getValue();
                    Attribute limitAttr = startElement.getAttributeByName(QName.valueOf("limit"));
                    cclass.limit = Integer.parseInt(limitAttr.getValue());
                    Attribute roomAttr = startElement.getAttributeByName(QName.valueOf("room"));
                    cclass.roomNeeded = roomAttr == null || Boolean.getBoolean(roomAttr.getValue());
                    Attribute parentAttr = startElement.getAttributeByName(QName.valueOf("parent"));
                    cclass.parentId = parentAttr == null? null: parentAttr.getValue();
                    currentClass = cclass;
                    currentSubPart.classes.add(cclass);
                }
                else if(name.equals("room")){
                    Attribute roomidAttr = startElement.getAttributeByName(QName.valueOf("id"));
                    Integer roomId = Integer.parseInt(roomidAttr.getValue());
                    Attribute penaltyAttr = startElement.getAttributeByName(QName.valueOf("penalty"));
                    Integer penalty = Integer.parseInt(penaltyAttr.getValue());
                    currentClass.roomPenalties.put(roomId, penalty);
                }
                else if(name.equals("time")){
                    //<time days="1111000" start="198" length="22" weeks="111111111" penalty="12"/>
                    CourseTime time = new CourseTime();
                    Attribute daysAttr = startElement.getAttributeByName(QName.valueOf("days"));
                    time.days = daysAttr.getValue();
                    Attribute weeksAttr = startElement.getAttributeByName(QName.valueOf("weeks"));
                    time.weeks = weeksAttr.getValue();

                    Attribute lengthAttr = startElement.getAttributeByName(QName.valueOf("length"));
                    time.length = Integer.parseInt(lengthAttr.getValue());
                    Attribute startAttr = startElement.getAttributeByName(QName.valueOf("start"));
                    time.start = Integer.parseInt(startAttr.getValue());
                    Attribute penaltyAttr = startElement.getAttributeByName(QName.valueOf("penalty"));
                    time.penalty = Integer.parseInt(penaltyAttr.getValue());

                    currentClass.times.add(time);
                }
                else{
                    throw new IllegalArgumentException("Unexpected element encountered");
                }
            }
//            else if(event.isEndElement()){
//                throw new IllegalArgumentException("Unexpected end element");
//            }

            event = reader.nextEvent();

        }
        return course;
    }



}
