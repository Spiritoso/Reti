import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Scrittura {

	public static void main(String[] args) {	
		try {
			PrintWriter pw = new PrintWriter("prova.txt");
			pw.println("gianni e pinotto");
			pw.println("teo e gigi");
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
