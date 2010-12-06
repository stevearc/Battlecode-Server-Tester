package networking;

/**
 * Types of packets that can be sent to and from Servers and Clients
 * @author stevearc
 *
 */
public enum PacketCmd { 
	// Server -> Client
	RUN, // Sends data for running a match
	STOP, // Tells client to stop running matches
	
	// Client -> Server
	INIT, // Sends initial client data to server
	RUN_REPLY, // Response with match data
}
