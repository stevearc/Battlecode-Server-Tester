package networking;

import java.io.Serializable;

public class DependencyHashes implements Serializable {
	private static final long serialVersionUID = 1462085285087236859L;
	public final String battlecodeServerHash;
	public final String idataHash;

	public DependencyHashes(String battlecodeServerHash, String idataHash) {
		this.battlecodeServerHash = battlecodeServerHash;
		this.idataHash = idataHash;
	}
}
