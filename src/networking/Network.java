package networking;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

/**
 * Sends and receives packets to and from one other Network at a remote location
 * @author stevearc
 *
 */
public class Network implements Runnable{
	protected static Logger _log = Logger.getLogger(Network.class);
	protected Socket socket;
	protected boolean finish = false;
	protected Controller controller;

	public Network (Controller controller, Socket socket) throws IOException{
		this.socket = socket;
		this.controller = controller;
	}

	/**
	 * Transmits a packet to the connected Network
	 * @param packet The Packet to be transmitted
	 * @throws IOException 
	 */
	public void send(Packet packet) {
		if (!isConnected())
			return;
		try {
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(packet);
			oos.flush();
		} catch (IOException e) {
			_log.error("Error serializing packet:\n" + packet, e);
		}
	}

	/**
	 * Close the current connection.
	 *
	 */
	public void close(){
		finish = true;
		if (!socket.isClosed()) {
			try {
				socket.close();
			} catch (IOException e){
				_log.warn("Cannot close socket");
			}
		}
	}

	/**
	 * Detect if the Network has connected to another Network.
	 * @return True or False.
	 */
	public boolean isConnected(){
		return !finish;
	}

	@Override
	public void run() {
		while (!finish){
			try {
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				Packet packet;
				try {
					packet = (Packet) ois.readObject();
				} catch (ClassNotFoundException e) {
					_log.warn("Could not find class during packet deserialization", e);
					return;
				}

				controller.addPacket(packet);
			} 
			catch (EOFException e) {
				break;
			}
			catch (IOException e){
				_log.error("Error running network", e);
				break;
			}
		}
		close();
		controller.onDisconnect();
	}
	
	@Override
	public String toString() {
		if (socket != null) {
			return socket.getInetAddress().toString();
		} else
			return "Null Network";
	}

}
