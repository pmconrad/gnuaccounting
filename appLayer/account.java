package appLayer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Vector;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import dataLayer.DB;

class typeNotFoundException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public typeNotFoundException(String message) {
		super(message);
	}

}

@Entity
public class account implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int accountID = -1;
	String code;
	String description;
	int subAccounts;
	int type;
	/***
	 * Some SKR accounts automatically include VAT
	 */
	protected boolean isAutoVATAccount=false;
	Integer refersTo = null;
	Integer taxField = null;
	boolean isPlaceholderForNewAccount = false;

	@Transient
	private BigDecimal openingBalance = new BigDecimal(0);
	@Transient
	private BigDecimal accountBalanceDebit = new BigDecimal(0);
	@Transient
	private BigDecimal accountBalanceCredit = new BigDecimal(0);

	private Timestamp outdated;
	private boolean assetsDeductable = false;
	private accountChart parent;

	static final String[] accountTypes = { "unknown", //$NON-NLS-1$
			// inventory account types =Bestandskonten
			"asset", // asset account=aktivkonto  //$NON-NLS-1$
			"liability",//liability account=passivkonto  //$NON-NLS-1$
			// nominal account types =Erfolgskonten
			"expense", //expense account=aufwandskonto //$NON-NLS-1$
			"income", // revenue account=ertragskonto/erlöskonto. //$NON-NLS-1$
			"empty", // for the "please select" entry  //$NON-NLS-1$
			"payable", //$NON-NLS-1$
			"equity", //$NON-NLS-1$
			"placeholder", //$NON-NLS-1$
			"currency", //$NON-NLS-1$
			"bank", //$NON-NLS-1$
			"cash" //$NON-NLS-1$
	};

	// not localizable because its in file SKR03
	static final String[] subAccountTypes = { "nothing", //$NON-NLS-1$
			"contacts", //$NON-NLS-1$
			"customers", //$NON-NLS-1$
			"suppliers", //$NON-NLS-1$
			"partners" //$NON-NLS-1$
	};

	public account() {
	}

	public account(accountChart parent, String code, String description,
			int type, int subAccounts) {
		// this.accountID=id;
		this.parent = parent;
		update(code, description, type, subAccounts);
	}

	public boolean isEmpty() {
		boolean res = true;
		try {
			res = (getType() == getTypeIDForString("empty"));//$NON-NLS-1$
		} catch (typeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	public account(int id, String code, String description, int type,
			int subAccounts) {
		// this.accountID=id;
		update(code, description, type, subAccounts);
	}

	public void setParent(accountChart parent) {
		this.parent = parent;
	}

	public void update(String code, String description, int type,
			int subAccounts) {

		this.code = code;
		this.description = description;
		this.type = type;
		this.subAccounts = subAccounts;
	}

	public void startSession() {
		DB.getEntityManager().getTransaction().begin();
	}

	public void saveInSession() {
		DB.getEntityManager().persist(this);

	}

	public void endSession() {
		DB.getEntityManager().getTransaction().commit();
		if (parent != null) {
			parent.signalChange(this);
		}

	}

	/**
	 * this will save the account definition to the database. If a existing
	 * account has been updated using the update function the according DB entry
	 * is updated, otherwise, if a new account has been created with a
	 * constructor that did not specifiy a database ID, a database entry is
	 * inserted
	 * */
	public void save() {
		startSession();
		saveInSession();
		endSession();
	}

	/**
	 * deletes this account logically from the db by setting the outdated
	 * attribute
	 * */
	public void delete() {
		Calendar c = Calendar.getInstance();

		outdated = new Timestamp(c.getTime().getTime());
		save();

		parent.signalChange(this);
	}

	public static int getTypeIDForString(String type)
			throws typeNotFoundException {
		int index = 0;
		for (String currentType : accountTypes) {
			if (currentType.equals(type)) {
				return index;
			}
			index++;
		}
		throw new typeNotFoundException(
				Messages.getString("account.type") + type + Messages.getString("account.notfoundtype")); //$NON-NLS-1$ //$NON-NLS-2$

	}

	public void setTaxField(Integer field) {
		this.taxField = field;
	}

	public Integer getTaxField() {
		return taxField;
	}

	public boolean isInventoryAccount() {

		try {
			return (!getCode().equals(
					client.getAccounts().getYearClosingAccount().getCode()) && (accountTypes[type]
					.equals("asset") || accountTypes[type].equals("liability"))); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (AccountNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public boolean isProfitLossAccount() {
		return ((accountTypes[type].equals("income") || accountTypes[type].equals("expense"))); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public boolean isIncomeAccount() {
		return accountTypes[type].equals("income"); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
	}

	public boolean isExpenseAccount() {
		return accountTypes[type].equals("expense"); //$NON-NLS-1$ 
	}

	public boolean isLiabilityAccount() {
		return accountTypes[type].equals("liability"); //$NON-NLS-1$ 
	}

	public boolean isBankAccount() {
		return accountTypes[type].equals("bank"); //$NON-NLS-1$ 
	}

	public boolean isCashAccount() {
		return accountTypes[type].equals("cash"); //$NON-NLS-1$ 
	}

	public boolean isAssetAccount() {
		return accountTypes[type].equals("asset"); //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
	}

	
	public void setType(String t) {
		try {
			type = getTypeIDForString(t);
		} catch (typeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getType() {
		return type;
	}

	public String getTypeString() {
		return accountTypes[type];
	}

	public void setType(int typeCode) throws typeNotFoundException {
		if ((typeCode < 0) || (typeCode > accountTypes.length)) {
			throw new typeNotFoundException(
					Messages.getString("account.typecode") + Integer.toString(typeCode) + Messages.getString("account.notfoundtypecode")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		type = typeCode;
	}

	public String getAsString() {
		return code + " " + description; //$NON-NLS-1$
	}

	public String getDescription() {
		return description;
	}

	public int getID() {
		return accountID;
	}

	public String getCode() {
		return code;
	}

	public String toString() {
		return getAsString();
	}

	static public String[] getTypes() {
		return accountTypes;
	}

	public boolean isAssetsDeductable() {
		return assetsDeductable;
	}

	public void setAssetsDeductable(boolean assetsDeductable) {
		this.assetsDeductable = assetsDeductable;
	}

	public int getSubAccountTypesCode() {
		return subAccounts;
	}

	public void setReferTo(int contactID) throws accountException {
		if (subAccounts == 0) {
			throw new accountException(
					Messages.getString("account.account") + getAsString() + Messages.getString("account.maynotrefercontacts")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		refersTo = new Integer(contactID);

	}

	public boolean balanceIncreasesInDebit() {

		try {
			return (getType() == getTypeIDForString("asset")) || (getType() == getTypeIDForString("expense")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (typeNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;// this should never be reached

	}

	public String getSubAccountSQLString() {
		if (refersTo == null) {
			return "NULL"; //$NON-NLS-1$
		} else {
			return refersTo.toString();
		}

	}

	public static int getSubAccountsTypeIDForString(String type)
			throws typeNotFoundException {
		int index = 0;
		for (String currentType : subAccountTypes) {
			if (currentType.equals(type)) {
				return index;
			}
			index++;
		}
		throw new typeNotFoundException(
				Messages.getString("account.type") + type + Messages.getString("account.notallowedsubaccount")); //$NON-NLS-1$ //$NON-NLS-2$

	}

	static public String[] getSubAccounts() {
		return subAccountTypes;
	}

	public static account getNewAccount(accountChart al) {
		account newA = null;
		try {
			newA = new account(
					al,
					"0000", Messages.getString("account.newAccount"), account.getTypeIDForString("empty"), 0); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} catch (typeNotFoundException e) {
			e.printStackTrace();
		}
		return newA;
	}

	/**
	 * returns all entries of a single account in the period selected by
	 * setPeriod
	 * 
	 * openingbalance is calculated here and in getJournal
	 */
	public Vector<entry> getJournalForAccount(boolean restrictToPeriod) {
		Vector<entry> res = new Vector<entry>();

		for (entry currentEntry : client.getEntries().getJournal(
				restrictToPeriod)) {
			if ((currentEntry.getCreditAccount().getID() == getID() || currentEntry
					.getDebitAccount().getID() == getID())) {
				res.add(currentEntry);

			}

		}
		if (openingBalance.compareTo(new BigDecimal(0)) != 0) {
			entry opening = null;
			try {
				if (openingBalance.compareTo(new BigDecimal(0)) < 0) {
					opening = new entry(
							client.getEntries().getStart(),
							Messages.getString("account.openingEntryText"), this, client.getAccounts().getYearClosingAccount(), "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				} else {
					opening = new entry(
							client.getEntries().getStart(),
							Messages.getString("account.openingEntryText"), client.getAccounts().getYearClosingAccount(), this, "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

				}
				opening.setNumber(0);
				opening.setValue(openingBalance.abs());
				res.add(0, opening);
			} catch (AccountNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return res;
	}

	@Override
	public account clone() throws CloneNotSupportedException {

		return (account) super.clone();
	}

	public void setPlaceholderForNewAccount(boolean isOne) {
		isPlaceholderForNewAccount = isOne;
	}

	public boolean isPlaceholderForNewAccount() {
		return isPlaceholderForNewAccount;
	}
	
	
	/**
	 * only set after getJournalForAccount has been executed
	 * @return
	 */
	public BigDecimal getOpeningBalance() {
		return openingBalance;
	}

	/**
	 * only set after getJournalForAccount has been executed
	 * @return
	 */
	public BigDecimal getBalanceTotal() {
		return accountBalanceDebit.subtract(accountBalanceCredit);
	}

	public BigDecimal getBalanceDebit() {
		return accountBalanceDebit;
	}
	
	public BigDecimal getBalanceCredit() {
		return accountBalanceCredit;
	}
	
	public void resetBalance() {
		openingBalance = new BigDecimal(0);
		accountBalanceCredit = new BigDecimal(0);
		accountBalanceDebit = new BigDecimal(0);

	}

	public void addAccountBalance(BigDecimal value, boolean onDebitSide) {

		if (onDebitSide) {
			accountBalanceDebit = accountBalanceDebit.add(value);
		} else {
			accountBalanceCredit = accountBalanceCredit.add(value);
		}

	}

	public void addOpeningBalance(BigDecimal value, boolean onDebitSide) {
		if ((onDebitSide) && (balanceIncreasesInDebit())) {
			openingBalance = openingBalance.add(value);
		} else {
			openingBalance = openingBalance.subtract(value);
		}

	}
	
	public boolean isDeleted() {
		return (outdated!=null);
	}

	public boolean isAutoVAT() {
		return this.isAutoVATAccount;
	}

	public void setAutoVAT(boolean b) {
		this.isAutoVATAccount = b;
	}

}
