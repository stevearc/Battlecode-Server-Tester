package model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class BSMetadata {
	private Long id;
	private String version;
	private int hashVersion;
	
	@Id
	public Long getId() {
		return id;
	}

	public String getVersion() {
		return version;
	}
	
	public int getHashVersion() {
		return hashVersion;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public void setHashVersion(int hashVersion) {
		this.hashVersion = hashVersion;
	}

}
