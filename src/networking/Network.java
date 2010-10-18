package networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import common.Config;

/**
 * Sends and recieves packets to and from one other Network at a remote location
 * @author steven
 *
 */
public class Network implements Runnable{
	private Logger _log;
	private Socket socket;
	private boolean finish = false;
	private Controller controller;
	private Config config;

	public Network (Controller controller, Socket socket) throws IOException{
		config = Config.getConfig();
		_log = config.getLogger();
		this.socket = socket;
		this.controller = controller;
	}

	private void getPacket() {
		try {

			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			Packet packet;
			try {
				packet = (Packet) ois.readObject();
			} catch (ClassNotFoundException e) {
				_log.log(Level.SEVERE, "Could not find class during packet deserialization", e);
				return;
			}

			if (packet.validate()) {
				controller.addPacket(this, packet);
			} else {
				_log.warning("Received invalid packet:\n" + packet);
			}

		} 
		catch (IOException e){
			_log.log(Level.WARNING, "Could not read from connection", e);
			stop();
		}
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
			if (packet.validate()) {
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				oos.writeObject(packet);
				oos.flush();
			} else {
				_log.severe("Trying to send invalid packet:\n" + packet);
			}
		} catch (IOException e) {
			_log.log(Level.SEVERE, "Error serializing packet:\n" + packet, e);
		}
	}

	/**
	 * Close the current connection.
	 *
	 */
	private void close(){
		finish = true;
		if (!socket.isClosed()) {
			try {
				socket.close();
			} catch (IOException e){
				_log.warning("Cannot close socket");
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
			getPacket();
		}
		close();
	}

	/**
	 * Cleanly stop transmissions and close the connection.
	 */
	public void stop() {
		_log.info("Stopping connection " + socket.toString());
		finish = true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (socket != null) {
			sb.append(socket.getInetAddress().toString());
			return sb.toString();
		} else
			return "Null Network";
	}

	/**
	 * Format the Network for the MYSQL database
	 * @return
	 */
	public String toSQL() {
		return socket.getInetAddress().toString();
	}
}
