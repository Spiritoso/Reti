import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {

	public static void main(String[] args) {
		try (DatagramSocket socket = new DatagramSocket()){
			
			//byte[] buf = {'C', 'i', 'a', 'o'};		//byte da mandare
			String s = new String("Gianni e pinotto");
			byte[] b = s.getBytes();			//prendo i byte della stringa: sono esattamente i caratteri sopra (non c'e' il terminatore)
				
			
			//InetAddress addr = InetAddress.getByName("www.google.it");
			InetAddress addr = InetAddress.getLocalHost();
			int port = 19900;
			System.out.println("C: ip- " + socket.getLocalPort() + " porta- " + socket.getLocalAddress());
			
			
			DatagramPacket packet = new DatagramPacket(b, b.length, addr, port);
			
			socket.send(packet);		//invio del DatagramPacket
			System.out.println("C: Invio del paccketto. dati: " + new String(b));		//converto al volo l'array di byte in stringa. Quando trova uno \0 termina la conversione
			
			//ricezione risposta
			socket.receive(packet);
			
			System.out.println("C: ricevuto datagram da: porta: " 
					+ packet.getPort() + " address: " + packet.getAddress() 
					+ " dati: " + (char)packet.getData()[0]);	//se volessi la stringa dovrei fare la conversione al volo come ho fatto prima
			
			System.out.println("Fine");
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}

}
