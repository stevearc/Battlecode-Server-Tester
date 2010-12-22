package networking;

/**
 * Types of packets that can be sent to and from Servers and Workers
 * @author stevearc
 *
 */
public enum PacketCmd { 
	// Server -> Worker
	RUN, // Sends data for running a match
	STOP, // Tells worker to stop running matches
	
	// Worker -> Server
	INIT, // Sends initial worker data to server
	RUN_REPLY, // Response with match data
}
