import java.io.*;
import java.net.*;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/*
 * Invia richieste contenenti nomeFile 
 * @args[0] = ipServer
 * @args[1] = portaServer
 * @args[2] = nomeFile
 * 
 */
public class Client {
	public static void main(String[] args) {
		System.out.println("C- avviato...");
		DatagramSocket socket = null;
		DatagramPacket packet = null;
		InetAddress addr = null;
		int port = -1;
		String file = null;
		byte[] buf = new byte[256];

		// controllo argomenti
		if (args.length == 3) {
			try {
				addr = InetAddress.getByName(args[0]);
				port = Integer.parseInt(args[1]);
				if (port < 1024 || port > 65535) {
					System.out.println("Errore portaServer");
					System.exit(1);
				}
				file = args[2];
			} catch (NumberFormatException e) {
				System.err.println("Errore conversione porta: ");
				e.printStackTrace();
				System.exit(1);
			} catch (UnknownHostException e) {
				System.err.println("Errore ipServer: ");
				e.printStackTrace();
				System.exit(1);
			}
		} else {
			System.out.println("Errore argomenti: ipServer portaServer nomeFile");
			System.exit(2);
		}

		// creazione socket e packet
		try {
			socket = new DatagramSocket();
			packet = new DatagramPacket(buf, buf.length, addr, port);
			System.out.println("C- creata socket: " + socket.getLocalSocketAddress());
		} catch (SocketException e) {
			System.err.println("Errore creazione socket: ");
			e.printStackTrace();
			System.exit(3);
		}

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); // reader legato alla cosole
			String riga = null;
			ByteArrayOutputStream boStream = null;
			DataOutputStream doStream = null;
			byte[] data;
			ByteArrayInputStream biStream = null;
			DataInputStream diStream = null;
			String messaggio = null;
			StringTokenizer str = null;
			int linea1, linea2;

			// invio richiesta a DiscoveryServer per questo file: mi ritornera la porta da
			// usare o -1(termino)
			try {
				boStream = new ByteArrayOutputStream();
				doStream = new DataOutputStream(boStream);
				doStream.writeUTF(file);
				data = boStream.toByteArray();
				
				packet.setData(data);
				socket.send(packet);	
				System.out.println("C- invio richiesta DS per file: " + file);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(3);
			}
			
			//ricezione risposta Discovery
			try {
				packet.setData(buf);
				socket.receive(packet);
				biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
				diStream = new DataInputStream(biStream);
				port = Integer.parseInt(diStream.readUTF());		//nuova porta con cui dovro comunicare (RowSwapper)
				packet.setPort(port);
				System.out.println("C- ristosta...porta RS: "+port);
				if(port < 0) {
					System.out.println("DiscoveryServer ha restituito una porta negativa: il file non e' presente nel server o altri errori");
					System.exit(4);		//termino il client
				}
			} catch(IOException e) {
				System.err.println("Errore lettura porta DiscoveryServer: ");
				e.printStackTrace();
				System.exit(3);
			}
			

			// ciclo in cui si chiede da terminale di iserire la coppia di righe da
			// scambiare e
			// stampo il risultato ricevuto da RowSwapper
			
			System.out.println("Inserire numero delle 2 righe da scambiare del file: " + file);
			while ((riga = in.readLine()) != null) {
				str = new StringTokenizer(riga);
				try {
					linea1 = Integer.parseInt(str.nextToken().trim());
					linea2 = Integer.parseInt(str.nextToken().trim());
				}catch(NumberFormatException e) {
					System.err.println("Errore conversione linee: ");
					e.printStackTrace();
					continue; 		//continuo a richiedere le linee
				}catch(NoSuchElementException e) {
					System.err.println("Errore conversione linee: ");
					e.printStackTrace();
					continue;
				}
				
				//invio 2 linee
				try {
					messaggio = linea1 + " " + linea2;
					System.out.println("C- Invio messaggio: " + messaggio);
					boStream = new ByteArrayOutputStream();
					doStream = new DataOutputStream(boStream);
					doStream.writeUTF(messaggio);
					data = boStream.toByteArray();
					packet.setData(data);
					socket.send(packet);
					
				} catch(IOException e) {
					System.err.println("Errore invio linee: ");
					e.printStackTrace();
					continue;
				}
				
				//ricezione risposta RowSwapper
				try {
					packet.setData(buf);
					socket.receive(packet);					
				} catch(IOException e) {
					System.err.println("Errore ricezione risposta RowSwapper: ");
					e.printStackTrace();
					continue;
				}
				
				try {
					biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
					diStream = new DataInputStream(biStream);
					messaggio = diStream.readUTF();
					System.out.println("C- risposta da RS: "+ messaggio);
				}catch(IOException e) {
					System.err.println("Errore lettura risosta RowSwapper: ");
					e.printStackTrace();
					continue;
				}
				
				System.out.println("Inserire numero delle 2 righe da scambiare del file: " + file);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		socket.close();
		System.out.println("C- teminazione....");
	}
}