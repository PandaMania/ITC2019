import Parsing.*;
import entities.*;
import java.io.*;

public class SimulatedAnnealing {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		InstanceParser p;
        Instance x;
        try {
            p = new InstanceParser("iku-fal17.xml");
            x = p.parse();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        
	}

}
