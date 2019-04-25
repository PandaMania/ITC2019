package Parsing;

import entities.Student;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class StudentParser {

    public Student parse(XMLEventReader reader, StartElement el) throws XMLStreamException {
        // Create course
        Student student = new Student();
        // Read ID
        Attribute id = el.getAttributeByName(QName.valueOf("id"));
        student.id = Integer.parseInt(id.getValue());
        // Use reader to expand until we hit the EndElement of the course

        // We don't have to initialize these, as the order
        // in which we encounter them guarantees that they will be assigned

        XMLEvent event = reader.nextEvent();
        while(!(event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("student"))) {
            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                String name = startElement.getName().getLocalPart();
                if (name.equals("course")){
                    Attribute idAttr = startElement.getAttributeByName(QName.valueOf("id"));
                    int courseId = Integer.parseInt(idAttr.getValue());
                    student.courses.add(courseId);
                }
            }
            event = reader.nextEvent();
        }
        return student;
    }
}
