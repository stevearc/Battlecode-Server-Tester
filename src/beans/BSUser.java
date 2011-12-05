package beans;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class BSUser {
	public static enum PRIVS {PENDING, USER, ADMIN}
	private Long id;
	private String username;
	private String hashedPassword;
	private String salt;
	private String session;
	private PRIVS privs;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public Long getId() {
		return id;
	}
	@Column(nullable=false,length=20,updatable=false,unique=true)
	public String getUsername() {
		return username;
	}
	@Column(nullable=false,length=40,updatable=false)
	public String getHashedPassword() {
		return hashedPassword;
	}
	@Column(nullable=false,length=40,updatable=false)
	public String getSalt() {
		return salt;
	}
	@Column(length=40)
	public String getSession() {
		return session;
	}
	@Enumerated(EnumType.STRING)
	public PRIVS getPrivs() {
		return privs;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setHashedPassword(String password) {
		this.hashedPassword = password;
	}
	public void setSalt(String salt) {
		this.salt = salt;
	}
	public void setSession(String session) {
		this.session = session;
	}
	public void setPrivs(PRIVS privs) {
		this.privs = privs;
	}

}
