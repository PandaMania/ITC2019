package entities;

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


    public String serialize(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<solution name=\"Not implemented yet\">");
        for (SolutionClass aClass : classes) {
            stringBuilder.append("\t");
            stringBuilder.append(aClass.serialize());
            stringBuilder.append("\n");
        }
        stringBuilder.append("</solution>");
        return stringBuilder.toString();
    }
}
