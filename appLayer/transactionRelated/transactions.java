package appLayer.transactionRelated;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import GUILayer.todoWindow;
import appLayer.Messages;
import appLayer.client;
import dataLayer.DB;

public class transactions {
	private appTransaction[] instances = new appTransaction[10];
	private Vector<transactionType> allTypes = null;
	private Vector<appTransaction> allTransactions = null;
	private int currentTransactionListIndex = 0;

	public transactions() {
		allTypes = new Vector<transactionType>();
		// allTransactions to remain null because later check in getTransactions
		// will invoke getFromDB if this is the case
		init();
	}

	public void init() {
		instances[0] = new invoice(this);
		instances[1] = new creditnote(this);
		instances[2] = new cancelation(this);
		instances[3] = new reminder(this);
		instances[4] = new offer(this);
		instances[5] = new personalDrawing(this);
		instances[6] = new receipt(this);// outgoing receipt
		instances[7] = new shippingTicket(this);
		instances[8] = new receiptIncoming(this);// incoming receipt
		instances[9] = new salarySlip(this);

		getTransactionTypesFromDB();
	}

	/**
	 * takes a type id and returns its index in the instances array which is the
	 * same as in the types array (the passed argument -1)
	 * */
	public int getInstanceIndexForTypeID(int typeID) {
		return typeID - 1;
	}

	public appTransaction getInstanceByTypeIndex(int typeIndex) {
		return instances[typeIndex];
	}

	public appTransaction getInstanceByTypeID(int typeID) {
		return instances[getInstanceIndexForTypeID(typeID)];
	}

	public void setInstanceByTypeID(int typeID, appTransaction inst) {
		instances[getInstanceIndexForTypeID(typeID)] = inst;
	}

	public appTransaction getByID(int ID) {
		if (allTransactions == null) {
			getTransactionsFromDB();
		}
		for (appTransaction current : allTransactions) {
			if (current.getID() == ID) {
				return current;
			}
		}
		return null;
	}

	public transactionType[] getAllTypes() {
		transactionType[] types = new transactionType[allTypes.size()];
		int transactionIndex = 0;
		for (transactionType currentType : allTypes) {
			currentType.setListIndex(transactionIndex);
			types[transactionIndex] = currentType;
			transactionIndex++;
		}
		return types;
	}

	/*
	 * returns types that can be selected for a new transaction at that moment,
	 * i.e. no cancellation if no transaction had previously been selected
	 */
	public transactionType[] getAllSelectableTypes(boolean showUnreferenced) {
		int countIndex = 0;
		for (transactionType currentType : allTypes) {
			if (showUnreferenced || currentType.isAllowUnreferencedCreation()) {
				countIndex++;
			}
		}
		transactionType[] types = new transactionType[countIndex];
		int transactionIndex = 0;
		int listIndex = 0;
		for (transactionType currentType : allTypes) {
			currentType.setListIndex(transactionIndex);
			if (showUnreferenced || currentType.isAllowUnreferencedCreation()) {
				types[listIndex] = currentType;
				listIndex++;
			}
			transactionIndex++;
		}
		return types;
	}

	/**
	 * usually a stock transaction should be used (getTransactionByName) but
	 * when creating new transactions, e.g. to import entries from the bank
	 * statement, we need to return a type for a nema
	 * */
	public transactionType getTypeByTypeID(int id) {
		return allTypes.get(getInstanceIndexForTypeID(id));
	}

	public void setCurrentTransactionByTypeID(int typeID) {
		for (int currentIdx = 0; currentIdx < instances.length; currentIdx++) {
			appTransaction current = instances[currentIdx];
			if (current.getTypeID() == typeID) {
				currentTransactionListIndex = currentIdx;
			}
		}

	}

	public void setTransactionListIndex(int newTypeIndex) {
		appTransaction newTransaction = instances[newTypeIndex];
		newTransaction.cloneFrom(getCurrentTransaction());
		/*
		 * because the cloned transaction also has the number cloned (which
		 * depends on the type of transaction), make sure a new number is
		 * obtained at next opportunity
		 */
		currentTransactionListIndex = newTypeIndex;

	}

	public appTransaction getCurrentTransaction() {
		return instances[currentTransactionListIndex];
	}

	public appTransaction getDefaultTransaction() {
		return getInstanceByTypeIndex(0);
	}

	public transactionType getDefaultTransactionType() {
		return allTypes.firstElement();
	}

	/**
	 * loads the number formats, the current transaction numbers etc. for all
	 * transaction types from the db without this, a transaction is not
	 * completely ready, e.g. the mentioned attributes will be null
	 * */
	public void getTransactionTypesFromDB() {

		allTypes.clear();
		List retrievals = DB.getEntityManager()
				.createQuery("SELECT t FROM transactionType t").getResultList(); //$NON-NLS-1$
		int currentTransaction = 0;
		for (Iterator iter = retrievals.iterator(); iter.hasNext();) {
			transactionType currentlyRetrieved = (transactionType) iter.next();
			getInstanceByTypeIndex(currentTransaction).setType(
					currentlyRetrieved);
			allTypes.add(currentlyRetrieved);
			currentTransaction++;
		}
	}

	/**
	 * loads all performed transactions
	 * */
	private void getTransactionsFromDB() {
		if (allTransactions == null) {
			allTransactions = new Vector<appTransaction>();
		} else {
			allTransactions.clear();

		}

		List retrievals = DB.getEntityManager()
				.createQuery("SELECT t FROM appTransaction t").getResultList(); //$NON-NLS-1$
		int currentTransaction = 0;
		for (Iterator iter = retrievals.iterator(); iter.hasNext();) {
			appTransaction currentlyRetrieved = (appTransaction) iter.next();
			allTransactions.add(currentlyRetrieved);
			currentTransaction++;
		}
	}

	public Vector<appTransaction> getTransactions() {
		if (allTransactions == null) {
			getTransactionsFromDB();
		}
		return allTransactions;

	}

	/**
	 * when the user has selected transaction type and items and clicks finish
	 * transaction, it needs to be booked and the current transaction needs to
	 * be replaced by a blank one
	 * 
	 * @param balanced
	 * */
	public void finishCurrentTransaction(boolean balanced) {
		appTransaction current = getCurrentTransaction();
		current.setBalanced(balanced);
		current.book();
		init();
		todoWindow.refreshToDoList();
	}

	/**
	 * 0 when the creation process of a transaction has been cancelled e.g. by
	 * the user clicking cancel before finishing a new transaction, the number
	 * must be freed up again
	 * */
	public void abortCurrentTransaction() {
		init();
		// default to some trans type and any contact
		setTransactionListIndex(0);
		getCurrentTransaction().setContact(
				client.getContacts().getContacts().elementAt(0));
	}

	public void setAsCurrentTransaction(int transID) {
		appTransaction currentlyRetrieved = getByID(transID);
		if (currentlyRetrieved == null) {
			throw new IllegalArgumentException(
					Messages.getString("transactionRelated.transactions_transactionLoadError") + transID); //$NON-NLS-1$
		}
		int type = currentlyRetrieved.getTypeID();
		/*
		 * we load the transaction in the current transaction type, not in the
		 * transaction type of the loaded transaction -- the current transaction
		 * is used with currentTransactionType. If necessary, the
		 * transactiontype can be adjusted later with
		 * transactions.setTransactionType(xxx);
		 */
		setTransactionListIndex(client.getTransactions()
				.getInstanceIndexForTypeID(type));
		setInstanceByTypeID(type, currentlyRetrieved);

	}

	public appTransaction getForNumber(String number) {
		for (appTransaction trans : getTransactions()) {
			if (trans.getNumber().equals(number)) {
				return trans;
			}
		}
		return null;
	}

	public Integer getIDForNumber(String number) {
		appTransaction trans = getForNumber(number);
		if (trans == null) {
			return null;
		}
		return trans.getID();
	}

	public HashMap<Integer, String> getUnbalancedInvoices() {
		HashMap<Integer, String> res = null;
		res = instances[getInstanceIndexForTypeID(invoice.getType())]
				.getTodoItems(); //$NON-NLS-1$
		return res;
	}

	public HashMap<Integer, String> getUnbalancedCreditNotes() {
		HashMap<Integer, String> res = null;
		res = instances[getInstanceIndexForTypeID(creditnote.getType())]
				.getTodoItems(); //$NON-NLS-1$
		return res;
	}

	public void signalChange(appTransaction trans) {
		getTransactionTypesFromDB();

	}

	public appTransaction getForID(Integer referredTransactionID) {
		for (appTransaction trans : getTransactions()) {
			if (trans.getID() == referredTransactionID) {
				return trans;
			}
		}
		return null;
	}

	public void add(appTransaction newTransaction) {
		allTransactions.add(newTransaction);

	}


}
