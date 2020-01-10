import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Server {
	public static void main(String[] args) {
		InetAddress add = null;
		try {
			add = InetAddress.getByName("224.1.1.1");		//indirizzo di classe D a cui associarsi
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int port = 19000;
		
		try (MulticastSocket socket = new MulticastSocket(port)){				//serve una multicast socket per ricevere da chiunque scriva nella
			System.out.println("S: avviato- " + socket.getLocalSocketAddress());
			socket.joinGroup(add);		//faccio il join del gruppo
			
			byte[] buf = new byte[10];
			
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			
			socket.receive(packet);
			
			String s = new String(packet.getData());
			
			System.out.println("S: ricevuto- " + s + " da ip: " 
						+ packet.getAddress() + " port: " + packet.getPort());	

/*			buf[0] = 'y';
			socket.send(packet);
*/
			socket.leaveGroup(add);
			System.out.println("Fine");
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
