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
	ANALYZE, // Tell a worker to analyze a scrimmage match
	DEPENDENCIES, // Response with up-to-date dependency files
	
	// Worker -> Master
	INIT, // Sends initial worker data to master
	RUN_REPLY, // Response with match data
	REQUEST_DEPENDENCIES, // Requests files from the master
	ANALYZE_REPLY, // The response with the analyzed scrimmage match
}
