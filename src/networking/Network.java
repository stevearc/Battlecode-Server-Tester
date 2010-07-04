package networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import common.Config;

/**
 * Sends and recieves packets to and from one other Network at a remote location
 * @author steven
 *
 */
public class Network implements Runnable{
	private static final String startSequence = "<?xml>";
	private static final String endPacket = "</xml>";
	private static final String stayAlive = "l;kj209jakndf93n23lknf09830[jfa;kasd;lkj9j9h";
	/** How frequently (in milliseconds) to send a "stay alive" message */
	public static final int STAY_ALIVE_INTERVAL = 1000;
	private Logger _log;
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private XStream xstream;
	private boolean finish = false;
	private Controller controller;
	private Config config;
	private Date lastHeardPacket;

	public Network (Controller controller, Config config, Socket socket, Logger _log) throws IOException{
		xstream = new XStream(new DomDriver());
		xstream.alias("NetworkPacket", NetworkPacket.class);
		this._log = _log;
		this.config = config;
		this.socket = socket;
		this.controller = controller;
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new PrintWriter(socket.getOutputStream(), true);
		socket.setSoTimeout(10000);
		new Thread(new StayAlive(this, config.DEBUG, _log)).start();
	}

	private void getPacket() {
		try {
			String line;
			for (line = reader.readLine(); line != null && !line.equals(startSequence); line = reader.readLine())
				lastHeardPacket = Calendar.getInstance().getTime();

			if (line == null) {
				stop();
				return;
			}

			line = reader.readLine();
			StringBuilder sb = new StringBuilder();
			while (line != null && !line.equals(endPacket)) {
				if (line.equals(stayAlive)) {
					_log.warning("Packet ended without proper tail sequence");
					return;
				}
				sb.append(line);
				line = reader.readLine();
			}

			if (line == null) {
				_log.warning("Malformed packet: Fragment");
				return;
			}

			NetworkPacket newPacket = (NetworkPacket) xstream.fromXML(sb.toString());
			if (!newPacket.validate()) {
				_log.warning("Received invalid packet:\n" + newPacket);
				return;
			}
			byte[] matchFile = null;
			if (newPacket.matchLength > 0) {
				matchFile = new byte[newPacket.matchLength];
				for (int i = 0; i < newPacket.matchLength; i++) {
					matchFile[i] = (byte) reader.read();
				}
			}
			Packet packet = new Packet(newPacket, matchFile);
			//			_log.info(socket.toString() + " Receiving packet: \n" + packet);
			if (packet.validate()) {
				controller.addPacket(this, packet);
			} else {
				_log.warning("Received invalid packet:\n" + packet);
			}

		} 
		catch (SocketTimeoutException e1) {
			_log.warning("Connection timed out");
			stop();
		}
		catch (IOException e){
			_log.severe("Could not read from connection");
			if (config.DEBUG)
				e.printStackTrace();
			stop();
		}
	}

	/**
	 * Transmits a packet to the connected Network
	 * @param packet The Packet to be transmitted
	 */
	public synchronized void send(Packet packet){
		if (!isConnected())
			return;
		if (packet.validate()) {
			NetworkPacket sendPacket = new NetworkPacket(packet);
			writer.append("\n");
			writer.append(startSequence);
			writer.append("\n");
			xstream.toXML(sendPacket, writer);
			writer.append("\n");
			writer.append(endPacket);
			writer.append("\n");
			if (packet.matchLength > 0) {
				for (byte b: packet.match)
					writer.append((char)b);
			}
			writer.flush();
		} else {
			_log.warning("Trying to send invalid packet:\n" + packet);
		}
	}

	/**
	 * Sends a unique "stay alive" sequence to the connected Network.
	 * A Thread does this action in the background so we can detect if a connection times out.
	 */
	public synchronized void sendStayAlive() {
		writer.append("\n");
		writer.append(stayAlive);
		writer.append("\n");
		writer.flush();
	}

	/**
	 * Close the current connection.
	 *
	 */
	private void close(){
		finish = true;
		if (!socket.isClosed()) {
			try {
				reader.close();
				writer.close();
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
			//sb.append(socket.toString());
			//if (lastHeardPacket != null) {
			//	sb.append(" at ");
			//	sb.append(lastHeardPacket.toString());
			//}
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

/**
 * Periodically sends the "stay alive" message through the Network.
 * @author steven
 *
 */
class StayAlive implements Runnable{
	private Network network;
	private boolean DEBUG;
	private Logger _log;

	public StayAlive (Network network, boolean debug, Logger _log) {
		this.network = network;
		this.DEBUG = debug;
		this._log = _log;
	}

	@Override
	public void run() {
		while (network.isConnected()){
			network.sendStayAlive();
			try {
				Thread.sleep(Network.STAY_ALIVE_INTERVAL);
			} catch (InterruptedException e) {
				if (DEBUG)
					_log.severe(e.toString());
			}
		}

	}

}
