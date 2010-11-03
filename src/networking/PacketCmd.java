package networking;

public enum PacketCmd { 
	// Server -> Client
	RUN, 
	STOP,
	
	// Client -> Server
	INIT,
	RUN_REPLY,
}
