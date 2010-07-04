package networking;

/**
 * Types of messages that can be sent over the network.
 * @author steven
 *
 */

public enum PacketType {
	/** Server -> Client: Gives the Client the information necessary to run a match */
SEND_MAP, 
	/** Client -> Server: Gives the Server the results of a match */
MAP_RESULT, 
	/** Client -> Server: Tells the Server that the Client can run a match */
REQUEST_MAP, 
	/** Server -> Client: Tells Client to kill all currently running matches */
RESET_RUNS
}
