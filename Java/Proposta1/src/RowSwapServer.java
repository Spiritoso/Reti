import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/*
 *riceve dal client la richiesta di scambiare 2 righe di un file (attesa messaggio udp)
 *scambia le di righe
 *ritorna come risultato 1 se tutto ok o -1 se problema
 */

public class RowSwapServer extends Thread {
	DatagramSocket socket;
	File file;

	public RowSwapServer(DatagramSocket socket, File file) {
		this.socket = socket;
		this.file = file;
	}

	@Override
	public void run() {
		System.out.println("RSS" + socket.getLocalPort() + "- avvio per file: " + file.getName() + " su: "
				+ socket.getLocalSocketAddress());

		try {
			int linea1, linea2, result;
			StringTokenizer str = null;
			ByteArrayInputStream biStream = null;
			DataInputStream diStream = null;
			byte[] buf = new byte[256];
			String messaggio;
			ByteArrayOutputStream boStream = null;
			DataOutputStream doStream = null;
			byte[] data;

			DatagramPacket packet = new DatagramPacket(buf, buf.length);

			while (true) {
				System.out.println("RSS" + socket.getLocalPort() + "- attesa richieste linee...");

				// ricezione datagramma
				try {
					packet.setData(buf);
					socket.receive(packet);
				} catch (IOException e) {
					System.err.println("Errore ricezione datagramma: ");
					e.printStackTrace();
					continue;
				}

				// lettura messaggio e preparazione linee
				try {
					biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					diStream = new DataInputStream(biStream);
					messaggio = diStream.readUTF();
					str = new StringTokenizer(messaggio);
					linea1 = Integer.parseInt(str.nextToken().trim());
					linea2 = Integer.parseInt(str.nextToken().trim());
					System.out.println("RSS" + socket.getLocalPort() + "- ricevuta richiesta scambio: " + messaggio);

					if (linea1 > linea2) { // voglio i valori di linea odinati per effetuare uno scambio piu veloce
						int tmp = linea1;
						linea1 = linea2;
						linea2 = tmp;
					}
				} catch (IOException e) {
					System.err.println("Errore lettura datagramma: ");
					e.printStackTrace();
					continue;
				} catch (NoSuchElementException e) {
					System.err.println("Errore lettura datagramma tokenizer: ");
					e.printStackTrace();
					continue;
				} catch (NumberFormatException e) {
					System.err.println("Errore lettura datagramma; le linee non sono interi ");
					e.printStackTrace();
					continue;
				}

				// sostituzione e invio risposta
				try {
					result = scambia(linea1, linea2);
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
					doStream.writeUTF(String.valueOf(result));
					data = boStream.toByteArray();
					packet.setData(data);
					socket.send(packet);
					System.out.println("RSS" + socket.getLocalPort() + " invio risultato: " + result);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		socket.close();
	}

	private int scambia(int linea1, int linea2) {
		// creo il file disupporto per modificare il file
		File file2 = new File(file.getName() + "Temp");
		if (file2.exists())
			file2.delete();
		try {
			file2.createNewFile();
		} catch (IOException e) {
			System.err.println("Errore creazione file Temp");
			e.printStackTrace();
			return -1; // messaggio di errore per client
		}

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null, tmp = null, tmp2 = null;
			int i = 1;

			// trovo la linea piu lontana
			while (i <= linea2 && (line = br.readLine()) != null) {
				if (i == linea2) {
					tmp = line; // mi salvo la linea piu alta per poi scambiarla
				}

				i++;
			}
			if (line == null) { // linea2 non e' contenuta nel file
				System.out.println("RSS" + socket.getLocalPort() + " linea2 non contenuta nel file");
				br.close();
				return -1;
			}
			
			br.close();
			br = new BufferedReader(new FileReader(file)); // riparto a leggere dall'inizio
			PrintWriter pw = new PrintWriter(file2);
			i = 1;
			while ((line = br.readLine()) != null) {
				if (i == linea1) {
					tmp2 = new String(line); // salvo la linea1 in tmp
					line = tmp; // mi preparo a scrivere la linea2 che avevo trovato prima
				} else if (i == linea2) {
					line = tmp2;
				}

				pw.println(line); // scrivo il nuovo file modificato
				i++;
			}
			
			br.close();
			pw.close();
			file.delete();
			if (file2.renameTo(file)) {
				System.out.println("RSS" + socket.getLocalPort() + " sostituzione avvenuta con suc");
			} else {
				System.out.println("RSS" + socket.getLocalPort() + " sostituzione fallita");
				return -1;
			}
		} catch (FileNotFoundException e) {
			System.err.println("Errore creazione file tmp: ");
			e.printStackTrace();
			return -1; // messaggio di errore per client
		} catch (IOException e) {
			System.err.println("Errore lettura file: ");
			e.printStackTrace();
			return -1; // messaggio di errore per client
		}

		return 1;
	}

}
