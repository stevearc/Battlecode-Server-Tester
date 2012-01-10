package networking;

import java.io.Serializable;

public class DependencyHashes implements Serializable {
	private static final long serialVersionUID = 1462085285087236859L;
	public final String battlecodeServerHash;
	public final String allowedPackagesHash;
	public final String disallowedClassesHash;
	public final String methodCostsHash;

	public DependencyHashes(String battlecodeServerHash, String allowedPackagesHash, String disallowedClassesHash, String methodCostsHash) {
		this.battlecodeServerHash = battlecodeServerHash;
		this.allowedPackagesHash = allowedPackagesHash;
		this.disallowedClassesHash = disallowedClassesHash;
		this.methodCostsHash = methodCostsHash;
	}
}
