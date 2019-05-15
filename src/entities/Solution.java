package entities;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Solution {

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


    public Solution() {

    }

    // Copy constructor
    public Solution(Solution solution){
        for (SolutionClass aClass : solution.classes) {
            SolutionClass classCopy = new SolutionClass(aClass);
            this.classes.add(classCopy);
        }
    }



    public ArrayList<SolutionClass> classes = new ArrayList<>();


    public String serialize(int numDays, int numWeeks){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<solution name=\"Not implemented yet\">");
        stringBuilder.append("\n");
        for (SolutionClass aClass : classes) {
            stringBuilder.append("\t");
            stringBuilder.append(aClass.serialize(numDays, numWeeks));
            stringBuilder.append("\n");
        }
        stringBuilder.append("</solution>");
        return stringBuilder.toString();
    }

    public void saveToFile(String filename, int numDays, int numWeeks){
        String serialized = this.serialize(numDays, numWeeks);
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
