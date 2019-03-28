package Parsing;

import entities.Course;
import entities.Student;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class InstanceParser {

    private Map<String, BiConsumer<XMLEventReader, StartElement>> tagFunctions = new HashMap<>();

    private List<Course> courses = new ArrayList<>();
    private List<Student> students = new ArrayList<>();
//    private List<Room> rooms = new ArrayList<>();
//    private List<Distribution> distributions = new ArrayList<>();

    private void init(){
        tagFunctions.put("course", this::handleCourse);
        tagFunctions.put("student", this::handleStudent);
    }



    public InstanceParser() {
        init();
    }

    public void parse(String filename) throws FileNotFoundException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream(filename));
            while (xmlEventReader.hasNext()) {
                XMLEvent ev = xmlEventReader.nextEvent();
                if(ev.isStartElement()){
                    StartElement startElement = ev.asStartElement();
                    String tag = startElement.getName().getLocalPart();
                    BiConsumer<XMLEventReader, StartElement> f = tagFunctions.get(tag);
                    if(f != null){
                        f.accept(xmlEventReader, startElement);
                    }
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private void handleCourse(XMLEventReader reader, StartElement el) {

        CourseParser courseParser = new CourseParser();
        Course course;
        try {
            course = courseParser.parse(reader, el);
            courses.add(course);
        } catch (XMLStreamException e) {
            System.out.println("Error while handling course");
            e.printStackTrace();
        }
    }

    private void handleStudent(XMLEventReader reader, StartElement el){
        StudentParser parser = new StudentParser();
        Student student;
        try {
            student = parser.parse(reader, el);
            students.add(student);
        } catch (XMLStreamException e) {
            System.out.println("Error while handling course");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        InstanceParser p = new InstanceParser();
        try {
            p.parse("lums-sum17.xml");
//            p.parse("pu-cs-fal07.xml");
//            p.parse("pu-c8-spr07.xml");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}