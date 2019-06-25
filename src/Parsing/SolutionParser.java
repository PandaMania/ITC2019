package Parsing;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;
import entities.SolutionStudent;
import entities.course.CourseClass;
import util.BitSets;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.BitSet;

public class SolutionParser {

    private final FileInputStream file;
    private final Instance instance;

    public SolutionParser(String path, Instance instance) throws FileNotFoundException {
        file = new FileInputStream(path);
        this.instance = instance;

    }

    public Solution parse() {
        Solution sol = new Solution(instance);
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(file);
            SolutionClass currentClass = null;
            while (xmlEventReader.hasNext()) {
                XMLEvent ev = xmlEventReader.nextEvent();
                if (ev.isStartElement()) {
                    StartElement startElement = ev.asStartElement();
                    String tag = startElement.getName().getLocalPart();

                    if (tag.equals("class")) {
                        Attribute idAttr = startElement.getAttributeByName(QName.valueOf("id"));
                        int id = Integer.parseInt(idAttr.getValue());
                        Attribute daysAttr = startElement.getAttributeByName(QName.valueOf("days"));
                        BitSet days = BitSets.fromString(daysAttr.getValue());
                        Attribute weeksAttr = startElement.getAttributeByName(QName.valueOf("weeks"));
                        BitSet weeks = BitSets.fromString(weeksAttr.getValue());
                        Attribute startAttr = startElement.getAttributeByName(QName.valueOf("start"));
                        int start = Integer.parseInt(startAttr.getValue());
                        Attribute roomAttr = startElement.getAttributeByName(QName.valueOf("room"));
                        int room;
                        try {
                            room = Integer.parseInt(roomAttr.getValue());
                        } catch (NullPointerException e) {
                            room = -1;
                        }

                        SolutionClass c = new SolutionClass();
                        c.classId = id;
                        c.days = days;
                        c.start = start;
                        c.roomId = room;
                        c.weeks = weeks;
                        sol.classes.add(c);
                        CourseClass courseClass = instance.getClassForId(id);
                        c.length = courseClass.times.stream()
                                .filter(t -> t.start == start && BitSets.and(t.days, days).cardinality() > 0 && BitSets.and(t.weeks, weeks).cardinality() > 0)
                                .findFirst().get().length;
                        c.limit = courseClass.limit;
                        currentClass = c;
                    } else if (tag.equals("student")) {
                        Attribute idAttr = startElement.getAttributeByName(QName.valueOf("id"));
                        int id = Integer.parseInt(idAttr.getValue());

                        SolutionStudent s = new SolutionStudent();
                        s.id = id;
                        if (currentClass != null) {
                            currentClass.students.add(s);
                        } else {
                            throw new IllegalStateException("Encountered student tag without a preceding class tag.");
                        }
                    }
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return sol;
    }

    public static void main(String[] args) throws FileNotFoundException {
//        InstanceParser ip = new InstanceParser("lums-sum17.xml");
        InstanceParser ip = new InstanceParser("bet-sum18.xml");
        Instance instance = ip.parse();
        SolutionParser p = new SolutionParser("solution-1560887743789.xml", instance);
        Solution parse = p.parse();
        System.out.println(parse.serialize());
        parse.saveToFile("bet-sum_solution.xml");
    }
}
