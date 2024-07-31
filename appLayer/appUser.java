package appLayer;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import dataLayer.DB;

@Entity
public class appUser implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static int defaultUserID = 1;// Administrator
	public static int newUserID = 2;

	private String username;
	private String salt;
	private Timestamp outdated = null;
	public static String emptyPassword = "undefined"; //$NON-NLS-1$
	private String passwordHash;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String hashPassword(String password)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$
		byte[] sha1hash = new byte[40];
		md.reset();
		md.update(salt.getBytes());

		sha1hash = md.digest(md.digest(password.getBytes("UTF-8"))); //$NON-NLS-1$
		return new String(sha1hash);
	}

	public void user() {
	}

	/**
	 * his must be invoked for new users
	 * */
	public void init() {
		salt = new String(utils.randomBytes(2, 4));
		setPassword(emptyPassword);// by default use empty password

	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		if (password.length() == 0) {
			password = appUser.emptyPassword;// as it will be salted this will
												// give a nice checksum
		}
		try {
			this.passwordHash = hashPassword(password);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void save() {
		boolean added = false;
		if (id == -1) {
			added = true;
		}
		DB.getEntityManager().getTransaction().begin();
		DB.getEntityManager().persist(this);
		DB.getEntityManager().getTransaction().commit();

		if (added) {
			application.getUsers().signalAdd(this);
		} else {
			application.getUsers().signalChange(this);
		}

	}
	

	@Override
	public String toString() {
		return getUsername();
	}

	public int getID() {
		return id;
	}

	public void delete() {
		Calendar c = Calendar.getInstance();

		outdated = new Timestamp(c.getTime().getTime());
		save();

	}

	public static appUser getDefaultUser() {
		appUser newUser = new appUser();
		newUser.init();
		newUser.setUsername("Administrator"); //$NON-NLS-1$
		return newUser;
	}

	public static appUser getNewUser() {
		appUser newUser = new appUser();
		newUser.init();
		newUser.setUsername(Messages.getString("user.newUser")); //$NON-NLS-1$
		newUser.setPassword("");// an empty pwd will not be accepted, so nobody can log in as New User. The internal representation of an empty password is the static string emptyPassword(=undefined)  //$NON-NLS-1$
		return newUser;
	}

	public boolean matchesPassword(String password) {
		try {
			String hashToCompare = hashPassword(password);
			return hashToCompare.equals(passwordHash);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

}
