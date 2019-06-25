package entities;

import entities.course.CourseClass;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Solution {
    private Instance instance;

/*Format:
<solution name="unique-instance-name"
          runtime="12.3" cores="4" technique="Local Search"
          author="Pavel Novak" institution="Masaryk University" country="Czech Republic">
    <class id="1" days="1010100" start="90" weeks="1111111111111" room="1">
		<student id="1"/>
		<student id="3"/>
	</class>
</solution>
	*/

    public Solution(Instance instance) {
        this.instance = instance;
    }

    // Copy constructor
    public Solution(Solution solution, Instance instance){
        this.instance = instance;
        for (SolutionClass aClass : solution.classes) {
            SolutionClass classCopy = new SolutionClass(aClass);
            this.classes.add(classCopy);
        }
    }



    public ArrayList<SolutionClass> classes = new ArrayList<>();


    public String serialize(){
        int numDays = instance.days;
        int numWeeks = instance.weeks;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("<solution name=\"%s\">", instance.name));
        stringBuilder.append("\n");
//        for (CourseClass courseClass : instance.getClasses().collect(Collectors.toList())) {
        for (SolutionClass aClass : classes) {
//            SolutionClass aClass = getClassForId(Integer.parseInt(courseClass.id));
//            stringBuilder.append("\t");
//            if(aClass!= null){
//            }else
//            {
//            stringBuilder.append(SolutionClass.serializeOther(courseClass, numDays, numWeeks));
//            }
                stringBuilder.append(aClass.serialize(numDays, numWeeks));
            stringBuilder.append("\n");
        }
        stringBuilder.append("</solution>");
        return stringBuilder.toString();
    }

    public SolutionClass getClassForId(int id) {
        for (SolutionClass aClass : classes) {
            if(aClass.classId == id){
                return aClass;
            }
        }
        throw new IllegalArgumentException(String.format("No class for id %d", id));
    }

    public void saveToFile(String filename){
        String serialized = this.serialize();
        try{
            FileWriter writer = new FileWriter(filename);
            writer.write(serialized);
            writer.close();
        }
        catch (IOException e){
            System.out.println("Error while writing solution to file");
            e.printStackTrace();
            System.out.println("Trying to write:");
            System.out.println(serialized);
        }

    }
}
