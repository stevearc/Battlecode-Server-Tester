package networking;

public enum PacketCmd { 
	// Server -> Client
	RUN, 
	STOP,
	AUTH_REPLY,
	
	// Client -> Server
	AUTH,
	RUN_REPLY,
}
