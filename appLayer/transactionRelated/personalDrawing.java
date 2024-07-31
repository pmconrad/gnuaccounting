package appLayer.transactionRelated;

import java.util.HashMap;
import java.util.Vector;

import javax.persistence.Entity;

import appLayer.AccountNotFoundException;
import appLayer.CashFlow;
import appLayer.account;
import appLayer.accountException;
import appLayer.client;
import appLayer.entry;

@Entity
public class personalDrawing extends appTransaction {
	account pendingCreditAccount = null;
	account pendingDebitAccount = null;

	account finishCreditAccount = null;
	account finishDebitAccount = null;

	public personalDrawing() {

	}

	public personalDrawing(transactions parent) {
		super(parent);
		try {
			pendingCreditAccount = client.getAccounts()
					.getPersonalDrawAccount();
			pendingDebitAccount = client.getAccounts().getBankAccount();

			finishCreditAccount = client.getAccounts().getBankAccount();
			finishDebitAccount = client.getAccounts().getPersonalDrawAccount();
		} catch (AccountNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static int getType() {
		// due to GUILayer/newTransactionSelectTransactionDetails this code must
		// be
		// the index defined in
		// applayer/transactionRelatzed/transactions.java:init()+1

		return 6;
	}

	@Override
	public String getTransactionName() {
		return "personal drawing"; //$NON-NLS-1$
	}

	@Override
	public int getNumWorkflowSteps() {
		return 1;
	}

	public HashMap<Integer, String> getTodoItems() {
		HashMap<Integer, String> result = new HashMap<Integer, String>();
		Vector<entry> candidates = pendingCreditAccount
				.getJournalForAccount(false);
		for (entry currentEntry : candidates) {
			if ((currentEntry.getCreditAccount().getID() == pendingCreditAccount
					.getID())
					&& (currentEntry.getDebitAccount().getID() == pendingDebitAccount
							.getID())) {
				result.put(currentEntry.getID(), currentEntry.getReference()); //$NON-NLS-1$ //$NON-NLS-2$

			}
		}

		return result;
	}

	@Override
	public CashFlow getCashFlow() {
		return CashFlow.SENDING;
	}

	@Override
	public boolean isVATRequiredInStep(int step) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createEntriesForWorkflowStep(int step) {
		account creditAccount = null;
		account debitAccount = null;
		creditAccount = pendingCreditAccount;
		debitAccount = pendingDebitAccount;
		try {
			creditAccount.setReferTo(getContact().getID());
		} catch (accountException e) {
			e.printStackTrace();
		}
		// create entry WITHOUT the number here, otherwise the document with the
		// number is assumed to be consumed
		entry entryToBook = new entry(
				getIssueDate(),
				getType() + " " + getNumber(), getTotalGross(), creditAccount, debitAccount, "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		entryToBook.setReferredTransaction(this);
		entryToBook.setReference(getNumber());
		entries.add(entryToBook);
	}

}
