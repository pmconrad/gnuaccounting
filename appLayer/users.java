package appLayer;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import dataLayer.DB;

public class users implements IStructuredContentProvider {
	private Vector<appUser> usersList = new Vector<appUser>();

	private appUser activeUser = null;

	public void getUsersFromDB() {
		usersList.clear();

		List retrievals = DB
				.getEntityManager()
				.createQuery("SELECT u FROM appUser u WHERE u.outdated IS NULL").getResultList(); //$NON-NLS-1$   
		for (Iterator iter = retrievals.iterator(); iter.hasNext();) {
			appUser currentlyRetrieved = (appUser) iter.next();
			usersList.add(currentlyRetrieved);
		}

	}

	public appUser getActiveUser() {
		return activeUser;
	}
	
	public void permamentlyRemoveAll() {
		DB.getEntityManager().getTransaction().begin();
		for (appUser currentUser : usersList) {
			DB.getEntityManager().remove(this);
	
		}
		DB.getEntityManager().getTransaction().commit();
		usersList.clear();
	}

	public boolean authenticate(String username, String password) {
		if ((username.length() == 0) || (password.length() == 0)) {
			return false;
		}
		if (usersList.size() == 0) {
			getUsersFromDB();
		}
		appUser u = getUserForUsername(username);
		if (u.matchesPassword(password)) {
			activeUser = u;
			return true;
		}
		return false;
	}

	public int getCount() {
		return usersList.size();
	}

	@Override
	public Object[] getElements(Object arg0) {
		/*
		 * String[] res=new String[users.size()]; int userIndex=0; for (user
		 * currentUser : users) { res[userIndex]=currentUser.getUsername();
		 * userIndex++; }
		 * 
		 * return res;
		 */
		return usersList.toArray();
	}

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {

	}

	public void signalAdd(appUser theUser) {

		usersList.add(theUser);

	}

	public void signalChange(appUser user) {
		getUsersFromDB();

	}

	public boolean emptyAdminPassword() {
		appUser admin = getUserForUsername("Administrator"); //$NON-NLS-1$
		return admin.matchesPassword(appUser.emptyPassword);
	}

	public appUser getUserForUsername(String username) {
		for (appUser currentUser : usersList) {
			if (currentUser.getUsername().equalsIgnoreCase(username)) {
				return currentUser;
			}
		}
		return null;
	}

}
