package appLayer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import appLayer.transactionRelated.appTransaction;
import dataLayer.DB;

/**
 * @todo have the entry deliver the right table editors
 * */
/**
 * an account entry along with it's attributes like value, date, credit account,
 * debit account etc. DB table: account_entries -- but this is not read here.
 * For perfomance reasons, accounftsList will fill the according account objects
 * with it's entries if needed
 * */
@Entity
public class entry implements Cloneable, Serializable, Comparable<entry> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	class undefinedNumberException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	}

	// ////////////////////END OF PRIVATE CLASSES...
	// public class entry

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int entryID;
	static int numberUndefined = -1;
	private int number = entry.numberUndefined;
	Timestamp outdated = null;
	// mode with set ids
	private appTransaction referredTransaction = null;

	private boolean isBooked = false;
	private contact other_party;
	private account creditAccount;
	private account debitAccount;
	@Temporal(value = TemporalType.DATE)
	private Date issueDate;
	private String description = "", reference = "", comment = ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	@Column(precision = 16, scale = 6)
	private BigDecimal value;

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal theValue) {
		this.value = theValue;

	}

	public entry() {
		prepare();
	}

	public entry(Date date, String description, BigDecimal value,
			account theAccount, account contraAccount, String reference,
			String comment) {
		prepare();
		setDate(date);
		setDescription(description);
		setCreditAccount(theAccount);
		setDebitAccount(contraAccount);
		setReference(reference);
		setComment(comment);
		setValue(value);
	}

	public entry(Date date, String description, account theAccount,
			account contraAccount, String reference, String comment) {
		prepare();
		assignBasicValues(date, description, theAccount, contraAccount,
				reference, comment);
	}

	public entry(int number, Date date, String description, BigDecimal value,
			account theAccount, account contraAccount, String reference,
			String comment) {
		prepare();
		setNumber(number);
		assignBasicValues(date, description, theAccount, contraAccount,
				reference, comment);
		setValue(value);

	}

	// this is the real common constructor, invoked by every constructor no
	// matter which arguments
	private void prepare() {
		creditAccount = null;
		debitAccount = null;
		setReference(""); //$NON-NLS-1$
		setComment(""); //$NON-NLS-1$
		setNumber(client.getEntries().getHighestNumber() + 1);
	}

	private void assignBasicValues(Date date, String description,
			account theAccount, account contraAccount, String reference,
			String comment) {
		setDate(date); //$NON-NLS-1$
		setDescription(description); //$NON-NLS-1$
		setCreditAccount(theAccount);
		setDebitAccount(contraAccount);
		setReference(reference); //$NON-NLS-1$
		setComment(comment); //$NON-NLS-1$
	}

	/**
	 * the number is the running index of an entry, in a single-client version
	 * identical with the auto-incremented ID
	 * */
	public int getID() {
		return entryID;
	}

	public void setID(int ID) {
		entryID = ID;
	}

	public boolean isCreditSide(account inAccount)
			throws entryNotInThisAccountException {
		boolean res;
		if (inAccount.equals(creditAccount))
			res = true;
		else //$NON-NLS-1$
		if (inAccount.equals(debitAccount))
			res = false;
		else
			//$NON-NLS-1$
			throw new entryNotInThisAccountException();

		return res;
	}

	public boolean increasesBalance(account inAccount)
			throws entryNotInThisAccountException {
		boolean res = !isCreditSide(inAccount);

		if (!inAccount.balanceIncreasesInDebit()) { //$NON-NLS-1$ //$NON-NLS-2$
			// asset and expenses grow in debit side, revenues and
			// liabilities in credit
			res = !res;
		}
		return res;
	}

	@Override
	public String toString() {
		String res = null;
		res = getDate().toString();
		return res;
	}

	public account getCreditAccount() {
		return creditAccount;
	}

	public account getDebitAccount() {
		return debitAccount;
	}

	public void setCreditAccount(account theAccount) {
		creditAccount = theAccount;
	}

	public void setDebitAccount(account theAccount) {
		debitAccount = theAccount;
	}

	public Date getDate() {
		return issueDate;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String s) {
		reference = s;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String s) {
		description = s;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String s) {
		comment = s;
	}

	public void save() {
		DB.getEntityManager().getTransaction().begin();
		DB.getEntityManager().persist(this);
		DB.getEntityManager().getTransaction().commit();
		// DB.getEntityManager().flush();
		// DB.getEntityManager().refresh(this);
		client.getEntries().updateEntryInJournal(this);

	}

	/**
	 * performs the insertion into the db. Method is protected, please use book
	 * from outside of this class. This function is fired once per booking and
	 * once per splitbooking. All columns will be inserted with their respective
	 * values -- except "value" which will take the local variable
	 * "insertValue", otherwise split bookings with different amounts would get
	 * tricky
	 * 
	 * @return the ID of the inserted DB entry
	 * */
	/*
	 * protected int insert() { save(); return this.id; }
	 */

	public void book() {
		appTransaction currentTrans = null;
		String ref = getReference();
		if ((ref.length() > 0) && (referredTransaction == null)) {
			// when booking to a document which we know belong to one of our
			// transactions, wire it up with the transaction
			currentTrans = client.getTransactions().getForNumber(ref);
			if (currentTrans != null) {
				setReferredTransaction(currentTrans);
			}

		}
		if (referredTransaction != null) {
			currentTrans = referredTransaction;
		}

		setCreditAccount(creditAccount);
		setDebitAccount(debitAccount);

		isBooked = true;
		// entry has not yet been saved. save it to get ID.
		save();

	}

	public void setReferredTransaction(appTransaction transaction) {
		this.referredTransaction = transaction;
	}

	public void setDate(Date date) {
		issueDate = date;
	}

	public boolean isBooked() {
		return isBooked;
	}

	public boolean refersATransaction() {
		return (referredTransaction != null);
	}

	public appTransaction getReferredTransaction() {
		return referredTransaction;
	}

	public void delete() {
		Calendar c = Calendar.getInstance();

		outdated = new Timestamp(c.getTime().getTime());
		save();

	}

	@Override
	public entry clone() throws CloneNotSupportedException {
		return (entry) super.clone();
	}

	public int compareTo(entry o) {
		return getDate().compareTo(o.getDate());
	}

	/**
	 * the ID of an entry is stored in the DB, is based on date&time of
	 * insertion and will never change. The number of an entry is a sequential
	 * number based on the effective date of the entry. after e.g. a previous
	 * entry has been deleted the number of all subsequent entries will updated
	 * (decrease by one).
	 * 
	 * The number is assigned on runtime by getAllEntriesFromDB
	 * */
	public int getNumber() throws undefinedNumberException {
		if (number == numberUndefined) {
			throw new undefinedNumberException();
		}
		return number;
	}

	public void setOutdated(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);

		outdated = new Timestamp(c.getTime().getTime());

	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getColumnString(int arg1) {
		switch (arg1) {
		case 0:
			SimpleDateFormat sdf = new SimpleDateFormat(
					Messages.getString("entry.columnDateFormat")); //$NON-NLS-1$
			return sdf.format(getDate());
		case 1:
			return getDescription();
		case 2:
			return getValue().toString();
		case 3:
			return getDebitAccount().getAsString();
		case 4:
			return getCreditAccount().getAsString();
		case 5:
			return getReference();
		case 6:
			return getComment();

		}
		return null;
	}

	public Object getColumnObject(int arg1) {
		switch (arg1) {
		case 0:
			return getDate();
		case 1:
			return getDescription();
		case 2:
			return getValue().toString();// as we're using a textcelleditor we
											// shall not return the bigint here
		case 3:
			return getDebitAccount();
		case 4:
			return getCreditAccount();
		case 5:
			return getReference();
		case 6:
			return getComment();

		}
		return null;
	}

	public void setColumnObject(String column, Object value) {
		int idx = -1;
		try {
			idx = utils
					.findIndexOfStringInStringArray(getColumnNames(), column);
		} catch (elementNotFoundException e) {
			e.printStackTrace();
		}
		switch (idx) {
		case 0:
			setDate((Date) value);
			break;
		case 1:
			setDescription((String) value);
			break;
		case 2:
			setValue(new BigDecimal(((String) value).replace(',', '.')));
			break;
		case 3:
			setDebitAccount((account) value);
			break;
		case 4:
			setCreditAccount((account) value);
			break;
		case 5:
			setReference((String) value);
			break;
		case 6:
			setComment((String) value);
			break;

		}

	}

	public Object getColumnObject(String colName) {
		int idx;
		try {
			idx = utils.findIndexOfStringInStringArray(getColumnNames(),
					colName);
		} catch (elementNotFoundException e) {
			return null;
		}
		return getColumnObject(idx);
	}

	public static String[] getColumnNames() {

		String[] res = {
				Messages.getString("entry.dateColHeader"), Messages.getString("entry.descriptionColHeader"), Messages.getString("entry.valueColHeader"), Messages.getString("entry.debitColHeader"), Messages.getString("entry.creditColHeader"), Messages.getString("entry.referenceColHeader"), Messages.getString("entry.commentColHeader") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
		return res;
	}

	public void setContact(contact c) {
		other_party = c;
	}

	public contact getContact() {
		return other_party;
	}

}
