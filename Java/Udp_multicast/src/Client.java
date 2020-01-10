import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {
	public static void main(String[] args) {
		try (DatagramSocket socket = new DatagramSocket()){
			InetAddress gruppo = InetAddress.getByName("224.1.1.1");		//indirizzo multicast: classe D
			int port = 19000;
			byte[] buf = new String("ciao").getBytes();
			
			DatagramPacket packet = new DatagramPacket(buf, buf.length, gruppo, port);
			
			
			System.out.println("C: add: " + socket.getLocalAddress() + " porta: "  + socket.getLocalPort());
			
			socket.send(packet);
			System.out.println("C: inviato- " + new String(buf));
			
/*			socket.receive(packet);
			
			String s = new String(packet.getData());
			System.out.println("C: ricevuto- " + s);
			*/
		} catch (SocketException e) {
			// TODO Auto-generated catch block
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
