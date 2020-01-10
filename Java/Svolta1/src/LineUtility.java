import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

//utility per leggere una linea da un file
public class LineUtility {
	public static String getLine(String nomeFile, int numLinea) {
		String linea = null;
		
		if(numLinea <= 0) {
			linea = "Errore numero linea <= 0";
		}else {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(nomeFile));
			} catch (FileNotFoundException e) {
				System.out.println("S- File non trovato");
				e.printStackTrace();
				return linea = "File non trovato";				
			}
			
			for(int i = 1; i <= numLinea; i++) {
				try {
					linea = br.readLine();
					if(linea == null) {		//sono alla fine del file
						br.close();
						linea = "linea non trovata";
						return linea;
					}
				} catch (IOException e) {
					System.out.println("S- Errore lettura linea file");
					e.printStackTrace();
				}
			}
			
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return linea;
	}
}
