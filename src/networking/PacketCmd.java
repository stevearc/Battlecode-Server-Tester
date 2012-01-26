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
	RESTART, // Tell a worker to restart
	
	// Worker -> Master
	REQUEST_MATCH, // Requests that the master send the worker a match
	RUN_REPLY, // Response with match data
	REQUEST_DEPENDENCIES, // Requests files from the master
	ANALYZE_REPLY, // The response with the analyzed scrimmage match
	INIT, // DEPRECATED
}
