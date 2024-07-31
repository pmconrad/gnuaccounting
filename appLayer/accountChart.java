package appLayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import dataLayer.DB;

@Entity
public class accountChart implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int id = -1;
	String name, description, vatIn, vatOut, vatOnHold;

	@OneToMany(mappedBy = "parent")
	@OrderBy("code")
	List<account> accounts = new ArrayList<account>();

	public accountChart() {

	}

	public accountChart(String name, String description, String vatIn,
			String vatOut, String vatOnHold) {
		this.name = name;
		this.description = description;
		this.vatIn = vatIn;
		this.vatOut = vatOut;
		this.vatOnHold = vatOnHold;
	}

	public void addAccount(account a) {
		accounts.add(a);
	}

	public void removeAccount(account a) {
		accounts.remove(a);
	}

	public void save() {
		// persistence1 Transaction tx = DB.getSession().beginTransaction();
		DB.getEntityManager().getTransaction().begin();
		DB.getEntityManager().persist(this);
		DB.getEntityManager().getTransaction().commit();
	}

	public String getName() {
		return name;
	}

	public List<account> getAccounts() {
		return accounts;
	}

	public void signalChange(account changed) {
		client.getAccounts().getAccountsFromDatabase();

	}

	public account getAccountForID(int ID) throws AccountNotFoundException {
		for (int accountIndex = 0; accountIndex < getAccounts().size(); accountIndex++) {
			account currentAccount = (account) getAccounts().get(accountIndex);
			// if (currentAccount.getID() == ID + 1) {
			// ID+1 because the database counts from 0, hibernate starts
			// with 1
			if (currentAccount.getID() == ID) {

				return currentAccount;
			}

		}
		throw new AccountNotFoundException(
				Messages.getString("accountsList.accountid") + Integer.toString(ID) + Messages.getString("accountsList.accountnotfound")); //$NON-NLS-1$ //$NON-NLS-2$

	}

	public account getAccountForCode(String code)
			throws AccountNotFoundException {

		for (int accountIndex = 0; accountIndex < getAccounts().size(); accountIndex++) {
			account currentAccount = (account) getAccounts().get(accountIndex);
			if (currentAccount.getCode().equals(code)) {
				return currentAccount;
			}

		}
		throw new AccountNotFoundException(
				Messages.getString("accountsList.accountcode") + code + Messages.getString("accountsList.codenotfound")); //$NON-NLS-1$ //$NON-NLS-2$

	}

	public List<account> getAccounts(boolean includeNewAccount) {

		if (includeNewAccount) {

			return getAccounts();
		} else {
			// we need to copy (=clone), if we dont we pass by reference and
			// always remove the current first element from the accounting chart
			// when invoked
			List<account> dest = new ArrayList<account>();

			List src = getAccounts();
			dest.addAll(src);
			dest.remove(0);

			return dest;
		}
	}

	public void removeDeletedAccounts() {

		for (int i=0; i<accounts.size(); i++) {
			if (accounts.get(i).isDeleted()) {
				accounts.remove(i);
			}
		}
		
	}

	/*
	public void sort() {
		Collections.sort(accounts,
				new Comparator<account>() {
					public int compare(account a1, account a2) {
						// "new product" always at top

						if (a1.isPlaceholderForNewAccount())
							return -1;
						if (a2.isPlaceholderForNewAccount())
							return +1;
							
						// otherwise sort by code

						return a1.getCode().compareToIgnoreCase(
								a2.getCode());
					}
				});
		
	}*/

}
