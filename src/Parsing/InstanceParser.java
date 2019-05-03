package Parsing;

import entities.*;
import entities.course.Course;
import entities.distribution.Distribution;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.BiConsumer;

public class InstanceParser {

    private Map<String, BiConsumer<XMLEventReader, StartElement>> tagFunctions = new HashMap<>();

    private Instance instance = new Instance();

    private FileInputStream file;

    private void init() {       //            p.parse("pu-cs-fal07.xml");
//            p.parse("pu-c8-spr07.xml");

        tagFunctions.put("course", this::handleCourse);
        tagFunctions.put("student", this::handleStudent);
        tagFunctions.put("room", this::handleRoom);
        tagFunctions.put("distribution", this::handledistribution);
    }


    public InstanceParser(String filename) throws FileNotFoundException {
        file = new FileInputStream(filename);
        init();
    }

    public Instance parse() {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(file);
            while (xmlEventReader.hasNext()) {
                XMLEvent ev = xmlEventReader.nextEvent();
                if (ev.isStartElement()) {
                    StartElement startElement = ev.asStartElement();
                    String tag = startElement.getName().getLocalPart();
                    BiConsumer<XMLEventReader, StartElement> f = tagFunctions.get(tag);
                    if (f != null) {
                        f.accept(xmlEventReader, startElement);
                    }
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return instance;
    }

    private void handleRoom(XMLEventReader reader, StartElement el) {

        RoomParser roomParser = new RoomParser();
        Room room;

        try {
            room = roomParser.parse(reader, el);
            instance.rooms.add(room);
//            for (Map.Entry<Integer, Integer> entry : room.distanceToRooms.entrySet()) {
//                HashSet<Integer> set = new HashSet<>();
//                set.
//            }
//
//            instance.distances.put()
        } catch (XMLStreamException e) {
            System.out.println("Error while handling room");
            e.printStackTrace();
        }
    }

    private void handledistribution(XMLEventReader reader, StartElement el) {

        DistributionParser distributionParser = new DistributionParser();
        Distribution distribution;
        try {
            distribution = distributionParser.parse(reader, el);
            instance.distributions.add(distribution);
        } catch (XMLStreamException e) {
            System.out.println("Error while handling room");
            e.printStackTrace();
        }
    }


    private void handleCourse(XMLEventReader reader, StartElement el) {

        CourseParser courseParser = new CourseParser();
        Course course;
        try {
            course = courseParser.parse(reader, el);
            instance.courses.add(course);
        } catch (XMLStreamException e) {
            System.out.println("Error while handling course");
            e.printStackTrace();
        }
    }

    private void handleStudent(XMLEventReader reader, StartElement el) {
        StudentParser parser = new StudentParser();
        Student student;
        try {
            student = parser.parse(reader, el);
            instance.students.add(student);
        } catch (XMLStreamException e) {
            System.out.println("Error while handling course");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        InstanceParser p;
        try {
            p = new InstanceParser(//"lums-sum17.xml");
            //            p.parse("pu-cs-fal07.xml");
                        "iku-fal17.xml");
            Instance x = p.parse();
            System.out.println(x);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }



    }
}