import Parsing.*;
import entities.*;
import java.io.*;

public class SimulatedAnnealing {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		InstanceParser p;
        try {
            p = new InstanceParser("iku-fal17.xml");
            Instance x = p.parse();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        
	}

}
