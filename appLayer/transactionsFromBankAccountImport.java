package appLayer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import appLayer.transactionRelated.receiptIncoming;
import dataLayer.DB;

public class transactionsFromBankAccountImport implements
		IStructuredContentProvider, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Vector<transactionFromBankAccountImport> importTransactions;
	private Vector<receiptIncoming> convertedTransactions;
	private boolean isLoaded = false;
	private boolean dirty = false;
	private account contraAccount;

	public transactionsFromBankAccountImport() {
		importTransactions = new Vector<transactionFromBankAccountImport>();
		convertedTransactions = new Vector<receiptIncoming>();
		isLoaded = false;
		try {
			contraAccount = client.getAccounts().getBankAccount();
		} catch (AccountNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public int size() {
		return convertedTransactions.size();
	}

	public void setContraAccount(account contraAccount) {
		this.contraAccount = contraAccount;
	}

	public transactionFromBankAccountImport add(String description,
			String subjectName, String subjectBank, String subjectAccount,
			String subjectBankCode, Date when, BigDecimal value) {
		if (!isLoaded) {
			load();
		}
		transactionFromBankAccountImport newTransactionFromImport = new transactionFromBankAccountImport();
		newTransactionFromImport.setDescription(description);
		newTransactionFromImport.setSubjectName(subjectName);
		newTransactionFromImport.setSubjectBank(subjectBank);
		newTransactionFromImport.setContraAccount(contraAccount);
		newTransactionFromImport.setSubjectAccount(subjectAccount);
		newTransactionFromImport.setSubjectBankCode(subjectBankCode);
		newTransactionFromImport.setWhen(when);
		newTransactionFromImport.setValue(value);
		
		for (transactionFromBankAccountImport currentTransaction : importTransactions) {
			// prevent dupes
			if (currentTransaction.equals(newTransactionFromImport)) {
				return null;
			}
		}
		
		newTransactionFromImport.save();
		importTransactions.add(newTransactionFromImport);
		convertedTransactions.add(newTransactionFromImport.getAsTransaction());
		return newTransactionFromImport;
	}

	public transactionFromBankAccountImport getForID(int id) {
		for (transactionFromBankAccountImport currentEntry : importTransactions) {
			// prevent dupes
			if (currentEntry.getID() == id) {
				return currentEntry;
			}
		}
		return null;

	}

	public void load() {
		importTransactions.clear();
		convertedTransactions.clear();
		// AND is_imported=false
		List retrievals = DB
				.getEntityManager()
				.createQuery(
						"SELECT e FROM transactionFromBankAccountImport e WHERE e.outdated IS NULL AND e.is_imported=false").getResultList(); //$NON-NLS-1$
		for (Iterator iter = retrievals.iterator(); iter.hasNext();) {
			transactionFromBankAccountImport currentlyRetrieved = (transactionFromBankAccountImport) iter
					.next();
			importTransactions.add(currentlyRetrieved);
			convertedTransactions.add(currentlyRetrieved.getAsTransaction());
		}
		isLoaded = true;

	}

	public Vector<receiptIncoming> getImportTransactions() {
		if (!isLoaded) {
			load();
		}
		Vector<receiptIncoming> res = new Vector<receiptIncoming>();
		for (transactionFromBankAccountImport importEntry : importTransactions) {
			res.add(importEntry.getAsTransaction());
		}

		return res;
	}

	public void deleteAndRemove(int importID) {
		transactionFromBankAccountImport importEntry = getForID(importID);
		if (importEntry != null) {
			importEntry.delete();
			importTransactions.remove(importEntry);
		}

	}

	@Override
	public Object[] getElements(Object arg0) {
		if (!isLoaded) {
			load();
		}

		return convertedTransactions.toArray();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

	/*
	 * The add wizard has to work with importIDs (no entry ID defined),
	 * accountingEditWindow has to work with entry IDs
	 */
	public receiptIncoming getTransactionForImportID(int importID) {
		for (receiptIncoming currentEntry : convertedTransactions) {
			if (currentEntry.getImportID() == importID) {
				return currentEntry;
			}
		}
		return null;
	}

	public void removeImportID(int importid) {
		receiptIncoming toRemove = null;
		for (receiptIncoming currentEntry : convertedTransactions) {
			if (currentEntry.getImportID() == importid) {
				toRemove = currentEntry;
				// store for later removal, if we remove it in the for loop
				// we'll get a
				// java.util.ConcurrentModificationException
				break;
			}
		}
		if (toRemove != null) {
			convertedTransactions.remove(toRemove);
		}

	}

	public void remove(receiptIncoming en) {
		convertedTransactions.remove(en);
	}

	/**
	 * if any of the entries has been changed without transferring it into a
	 * proper accounting entry
	 */
	public void setDirty() {
		dirty = true;

	}

}
