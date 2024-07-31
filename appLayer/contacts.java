package appLayer;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import dataLayer.DB;

public class contacts implements Cloneable {
	protected boolean emptyContact = false;
	protected Vector<contact> contactsList = new Vector<contact>();

	static String[] roles = { Messages.getString("contacts.RoleCustomer"), //$NON-NLS-1$
			Messages.getString("contacts.RoleSupplier"), //$NON-NLS-1$
			Messages.getString("contacts.RoleCustomerAndSupplier"), //$NON-NLS-1$
			Messages.getString("contacts.RolePartner"), //$NON-NLS-1$
			Messages.getString("contacts.RoleMember") //$NON-NLS-1$

	};

	static String[] paymentMethods = {
			Messages.getString("contacts.paymentMethodInvoice"), //$NON-NLS-1$
			Messages.getString("contacts.paymentMethodDebit") //$NON-NLS-1$
	};

	public static String[] getRoles() {
		return roles;
	}

	public static int getRoleIDForString(String role)
			throws RoleNotFoundException {
		int currentIndex = 0;
		for (String currentRole : roles) {
			if (currentRole.equals(role)) {
				return currentIndex;
			}
			currentIndex++;
		}
		throw new RoleNotFoundException(
				Messages.getString("contacts.role") + role + Messages.getString("contacts.notfound")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String[] getPaymentMethods() {
		return paymentMethods;
	}

	public static int getPaymentIDForString(String payment)
			throws PaymentMethodNotFoundException {
		int currentIndex = 0;
		for (String currentMethod : paymentMethods) {
			if (currentMethod.equals(payment)) {
				return currentIndex;
			}
			currentIndex++;
		}
		throw new PaymentMethodNotFoundException(
				Messages.getString("contacts.paymentMethodNotFoundFirstPart") + payment + Messages.getString("contacts.paymentMethodNotFoundSecondPart")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void removeAll() {
		contactsList.clear();

	}

	public void getContactsFromDB() {
		removeAll();
		javax.persistence.Query q = DB.getEntityManager().createQuery(
				"SELECT c FROM contact c WHERE c.outdated IS NULL"); //$NON-NLS-1$
		List<contact> contactList = q.getResultList();
		for (contact currentContact : contactList) {
			contactsList.add(currentContact);
		}

		sort();

	}

	/**
	 * sort list (new customer at top) and update string array
	 * */
	private void sort() {
		if (contactsList.size() != 0) {
			Collections.sort(contactsList, new Comparator<contact>() {
				public int compare(contact s1, contact s2) {
					// "new product" always at top

					if (s1.getID() == contact.newContactID)
						return -1;
					if (s2.getID() == contact.newContactID)
						return +1;
					// otherwise sort by name

					return s1.getName().compareToIgnoreCase(s2.getName());
				}
			});
		}
	}

	public void signalChange(contact changed) {
		/**
		 * @todo: do sth. more clever here (no need to reload /rebuild the
		 *        complete list)
		 */
		getContactsFromDB();
		sort();
	}

	public void signalAdd(contact theContact) {
		contactsList.add(theContact);
		sort();
	}

	public Vector<contact> getContacts() {
		return contactsList;
	}

	public contact getContactForID(int id) {
		for (contact currentContact : contactsList) {
			if (currentContact.getID() == id) {
				return currentContact;
			}
		}
		return null;
	}

	public contact getContactByName(String name) {
		for (contact currentContact : contactsList) {
			if (currentContact.getName().equals(name)) {
				return currentContact;
			}
		}
		return null;
	}

	public contact getContactByBankDetails(String accountCode, String bankCode,
			String accountHolder, String organisationName) {
		if ((accountCode == null) || (accountCode.length() == 0)) {
			accountCode = ""; //$NON-NLS-1$
		}
		if ((bankCode == null) || (bankCode.length() == 0)) {
			bankCode = ""; //$NON-NLS-1$
		}
		if ((organisationName == null) || (organisationName.length() == 0)) {
			return null;
		}

		for (contact currentContact : contactsList) {
			if ((currentContact.getBankAccount().equals(accountCode))
					&& (accountCode.length() > 0)
					&& (currentContact.getBankCode().equals(bankCode))
					&& (bankCode.length() > 0)) {
				return currentContact;
			}
		}// no matching bank details found (or no bank details available?),
			// match by name only
		for (contact currentContact : contactsList) {
			if (currentContact.getName().equals(organisationName)) {
				return currentContact;
			}
		}
		if (accountHolder == null) {
			accountHolder = ""; //$NON-NLS-1$
		}
		contact newContact = new contact(organisationName, accountHolder,
				""/* street */, ""/* add addr */, ""/* zip */, ""/* location */, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				""/* country */, ""/* email */, ""/* phone */, ""/* fax */, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				""/* Name */, 0/* role */, accountCode, bankCode, accountHolder, "", "", 0 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		); 
		newContact.save();

		return newContact;
	}

	public boolean containsInstallationDefault() {
		for (contact currentContact : contactsList) {
			if (currentContact.getName().equals(
					contact.getInstallationDefault().getName())) {
				return true;
			}
		}
		return false;
	}
	
	public String[] getStringArray(boolean includeNewContact) {
		
		String[] contactStringArray = new String[contactsList.size()];
		for (int contactIndex = 0; contactIndex < contactsList.size(); contactIndex++) {
			contactStringArray[contactIndex] = contactsList.get(contactIndex)
					.getName();
		}
		return contactStringArray;
	}

	@Override
	public contacts clone() throws CloneNotSupportedException {
		return (contacts) super.clone();
	}

	public int getCount() {
		return contactsList.size();
	}
/*
	public contact getContactByDetails(String name, String co, String street,
			String additionalAddress, String zip, String location,
			String country, String email, String phone, String fax,
			String VATID, int role, String bankaccount, String bankcode,
			String accountholder, String bic, String iban, int paymentmethod) {
		if ((name == null) || (name.length() == 0)) {
			return null;
		}

		for (contact currentContact : contactsList) {
			if ((!currentContact.getIsDeleted())
					&& (currentContact.getStreet().equalsIgnoreCase(street))
					&& (currentContact.getZIP().equalsIgnoreCase(zip))
					&& (currentContact.getLocation().equalsIgnoreCase(location))) {
				return currentContact;
			}
		}
		contact newContact = new contact(name, co, street, additionalAddress,
				zip, location, country, email, phone, fax, VATID, role,
				bankaccount, bankcode, accountholder, bic, iban, paymentmethod);

		newContact.save();

		return newContact;
	}*/

	public Vector<contact> getExistingContacts() {
		return new Vector<contact>(contactsList.subList(1, contactsList.size()));
	}

	public contact getContactByVCFID(String ID) {
		for (contact currentContact : contactsList) {
			if (ID.equals(currentContact.getVcfID())) {
				return currentContact;
			}
		}
		return null;

	}

}
