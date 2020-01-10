import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/*
 * Agisce da main e lancia tutti gli RowSwapServer come THREAD
 * args[0] = portaDiscoveryServer
 * poi passo sempre come argomenti coppie (nomeFile porta)
 * 
 */
public class DiscoveryServer {

	public static void main(String[] args) {
		System.out.println("S- avviato..");
		DatagramSocket socket = null;
		DatagramPacket packet = null;
		int port = 0;
		byte[] buf = new byte[256];
		Boolean[] validi = new Boolean[(args.length - 1) / 2]; // vettore che indica le coppie file-porta valide

		// controllo argomenti
		if (args.length > 1) {
			try {
				port = Integer.parseInt(args[0]);
				if (port < 1024 || port > 65535) {
					System.err.println(
							"Errore argomenti [potaDiscovery] deve essere un numero compreso fra 1024 e 65535");
					System.exit(1);
				}
			} catch (NumberFormatException e) {
				System.err.println("Errore argomenti [potaDiscovery] deve essere un numero compreso fra 1024 e 65535");
				e.printStackTrace();
				System.exit(1);
			}

		} else {
			System.err.println("Errore argomenti [potaDiscovery]");
			System.exit(1);
		}

		// creazione socket e datagram
		try {
			socket = new DatagramSocket(port, InetAddress.getByName("localhost"));
			packet = new DatagramPacket(buf, buf.length);
			System.out.println("S- Creata la socket: " + socket.getLocalSocketAddress());
		} catch (SocketException e1) {
			System.err.println("Errore creazione socket: ");
			e1.printStackTrace();
			System.exit(1);
		} catch (UnknownHostException e1) {
			System.err.println("Errore risoluzione ip: ");
			e1.printStackTrace();
		}

		// controllo porte diverse e presenza file
		for (int i = 2; i < args.length; i += 2) {
			File f = new File(args[i - 1]);
			int portF = -1;
			if (f.exists()) { // il file passato esiste
				try {
					validi[i / 2 - 1] = true;
					portF = Integer.parseInt(args[i]);
					for (int n = 2; n < args.length; n += 2) {
						if (n != i) { // ovviamente non controllo il mio indice corrente altrimenti fallirei sempre
							try {
								int portA = Integer.parseInt(args[n]);
								if (portF == portA) { // 2 porte uguali
									validi[i / 2 - 1] = false;
									System.out.println("DS- File-porta n. " + (i / 2) + " non valido");
								}
							} catch (NumberFormatException e) {
								continue; // per il momento non mi importa che questa porta non sia un intero
							}
						}
					}
				} catch (NumberFormatException e) {
					validi[i / 2 - 1] = false;
					System.err.println("Errore conversione porta del file n: " + i / 2);
					e.printStackTrace();
					continue; // non faccio crasciare il server solo perche' e' sbagliato un inserimento di tanti
				}
			} else { // file non esiste
				validi[i / 2 - 1] = false;
				System.out.println("S- File-porta n. " + (i / 2) + " non valido");
			}

			// creazione di un RawSwapper per ogni file elencato come argomento

			if (validi[i / 2 - 1]) {
				try {
					RowSwapServer rs = new RowSwapServer(new DatagramSocket(portF), f);
					rs.start(); // attivazione thread RowSwapServer....
					System.out.println("S- attivazione RSS file: " + f.getName() + " porta: " + portF);
				} catch (SocketException e) {
					System.err.println("Errore creazione Socket per porta:" + portF + " e file: " + f.getName());
					e.printStackTrace();
					continue;
				}
			}
		}

		try {
		// attesa richieste e risposta con numero della porta associata al file
		// che il cliente ha richiesto.
		// file non richiesto -> restituisce intero negativo
		ByteArrayInputStream biStream = null;
		DataInputStream diStream = null;
		String nomeFile = null;
		ByteArrayOutputStream boStream = null;
		DataOutputStream doStream = null;
		byte[] data;
		
		while (true) {
			System.out.println("S- attesa richieste.....");

			// ricezione datagramma
			try {
				packet.setData(buf);
				socket.receive(packet);
			} catch (IOException e) {
				System.err.println("Errore riceizone datagramma: ");
				e.printStackTrace();
				continue;
			}

			try {
				biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
				diStream = new DataInputStream(biStream);
				nomeFile = diStream.readUTF();
				System.out.println("S- ricevuta richiesta per file: " + nomeFile + " da cliente: "+ packet.getSocketAddress());
			} catch (IOException e) {
				System.err.println("Errore lettura UTF: ");
				e.printStackTrace();
				continue;
			}

			// preparazine e invio risposta
			int portaRisp = -1; // -1 indica che non e' stata trovata la porta associata al file

			try {
				for (int i = 1; i < args.length - 1 && portaRisp == -1; i += 2) { // controllo se ho il file nella mia lista
					if (nomeFile.equals(args[i]) && validi[(i - 1) / 2]) {
						portaRisp = Integer.parseInt(args[i + 1]);
					}
				}

				boStream = new ByteArrayOutputStream();
				doStream = new DataOutputStream(boStream);
				doStream.writeUTF(String.valueOf(portaRisp));
				data = boStream.toByteArray();
				packet.setData(data);
				socket.send(packet);
				System.out.println("S- inviata risposta: " + portaRisp + " a cliente: " + packet.getSocketAddress());
			} catch (IOException e) {
				System.err.println("Errore scrittura utf: ");
				e.printStackTrace();
				continue;
			}
		}
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		socket.close();
		System.out.println("S- terminazione.....");
		
	}

}
