package networking;

/**
 * Types of packets that can be sent to and from Masters and Workers
 * @author stevearc
 *
 */
public enum PacketCmd { 
	// Master -> Worker
	RUN, // Sends data for running a match
	STOP, // Tells worker to stop running matches
	
	// Worker -> Master
	INIT, // Sends initial worker data to master
	RUN_REPLY, // Response with match data
	REQUEST, // Requests a map and/or player from the master
}
