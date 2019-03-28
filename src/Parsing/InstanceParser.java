package Parsing;

import entities.Course;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class InstanceParser {

    private Map<String, BiConsumer<XMLEventReader, StartElement>> tagFunctions = new HashMap<>();

    private List<Course> courses = new ArrayList<>();
//    private List<Constraint> constraints = new ArrayList<>();
//    private List<Room> rooms = new ArrayList<>();
//    private List<Distribution> distributions = new ArrayList<>();

    private void init(){
        tagFunctions.put("course", this::handleCourse);
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
        Course course = null;
        try {
            course = courseParser.parse(reader, el);
            courses.add(course);
        } catch (XMLStreamException e) {
            System.out.println("Error while handling course");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        InstanceParser p = new InstanceParser();
        try {
            p.parse("lums-sum17.xml");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}