import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

public class Server {
	// le porte 'libere' vanno da 1024 a 65535
	private static final int PORT = 19000;

	public static void main(String[] args) {
		DatagramSocket socket = null;

		// controllo argomenti
		InetAddress add = null;
		try {
			add = InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int port = -1;

		if (args.length == 0) { // porta standard
			port = PORT;
		} else if (args.length == 1) { // setto la porta
			try {
				port = Integer.parseInt(args[0]);
				if (port < 1024 || port > 65535) {
					System.out.println("Errore numero porta");
					System.exit(1);
				}
			} catch (NumberFormatException e) {
				System.out.println("Errore numero porta");
				System.exit(1);
			}
		} else {
			System.out.println("Errore paramentri: [porta]");
			System.exit(1);
		}

		DatagramPacket packet = null;
		byte[] buf = new byte[256];

		try {
			socket = new DatagramSocket(port, add);
			packet = new DatagramPacket(buf, buf.length);
			System.out.println("S- creata socket " + socket);
		} catch (SocketException e) {
			System.out.println("S- errore creazione socket");
			e.printStackTrace();
		}

		// ciclo infinito server
		ByteArrayInputStream biStream = null;
		DataInputStream diStream = null;
		ByteArrayOutputStream boStream = null;
		DataOutputStream doStream = null;
		String nomeFile = null, ricevuto, messaggio;
		int linea = 0;
		StringTokenizer st = null;

		try {
			while (true) {
				System.out.println("S- attesa file..");

				// ricezione messaggio
				packet.setData(buf);
				try {
					socket.receive(packet);
				} catch (IOException e) {
					System.out.println("S- errore ricezione messaggio");
					e.printStackTrace();
					continue;
				}

				// lettura utf
				biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
				diStream = new DataInputStream(biStream);
				try {
					ricevuto = diStream.readUTF(); // lettura utf
					st = new StringTokenizer(ricevuto);
					nomeFile = st.nextToken();
					linea = Integer.parseInt(st.nextToken());
					System.out.println("S- ricevuta richiesta.. file: " + nomeFile + " linea: " + linea);

				} catch (IOException e) {
					System.out.println("S- errore lettura utf");
					e.printStackTrace();
					continue;
				} catch (NumberFormatException e1) {
					System.out.println("S- errore lettura formato num.linea");
					e1.printStackTrace();
					continue;
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}

				// invio risposta
				// ora uso LineUtility per trovare la linea nel file
				messaggio = LineUtility.getLine(nomeFile, linea);

				// invioi del messaggio

				try {
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
					doStream.writeUTF(messaggio);
					buf = boStream.toByteArray(); // ho scritto lo stream di byte sul buffer
					packet.setData(buf);
					socket.send(packet);
					System.out.println("S- inviata risposta: " + messaggio);

				} catch (IOException e) {
					System.out.println("S- Errore invio risposta");
					e.printStackTrace();
					continue;
				}

			}
			//eccezioni non catturate all'interno del while(true)
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("S- chiusura...");
		socket.close();
		
		
	}
}