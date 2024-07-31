package appLayer.transactionRelated;

import java.util.HashMap;

import javax.persistence.Entity;

import appLayer.AccountNotFoundException;
import appLayer.CashFlow;
import appLayer.account;
import appLayer.accountException;
import appLayer.client;
import appLayer.entry;

@Entity
public class creditnote extends appTransaction {
	account pendingCreditAccount = null;
	account pendingDebitAccount = null;

	account finishCreditAccount = null;
	account finishDebitAccount = null;

	public creditnote() {

	}

	public creditnote(transactions parent) {
		super(parent);
	}

	private void prepareEntryCreation() {

		try {
			pendingCreditAccount = client.getAccounts().getRevenuesAccount();
			pendingDebitAccount = client.getAccounts().getBankAccount();

			finishCreditAccount = client.getAccounts().getBankAccount();
			finishDebitAccount = client.getAccounts().getRevenuesAccount();
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
		return 2;
	}

	@Override
	public String getTransactionName() {
		return "creditnote"; //$NON-NLS-1$
	}

	@Override
	public int getNumWorkflowSteps() {
		return 2;
	}

	public HashMap<Integer, String> getTodoItems() {
		HashMap<Integer, String> result = new HashMap<Integer, String>();
		for (appTransaction currentTransaction : client.getTransactions()
				.getTransactions()) {
			if ((currentTransaction.getTypeID() == getType())
					&& (currentTransaction.getWorkflowStep() == 2)) {
				result.put(currentTransaction.getID(),
						currentTransaction.getNumber()); //$NON-NLS-1$ //$NON-NLS-2$

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
		prepareEntryCreation();
		account creditAccount = pendingCreditAccount;
		account debitAccount = pendingDebitAccount;
		try {
			creditAccount.setReferTo(getContact().getID());
		} catch (accountException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// create entry WITHOUT the number here, otherwise the document with the
		// number is assumed to be consumed
		entry entryToBook = new entry(
				getIssueDate(),
				getType() + " " + getNumber(), getTotalGross(), creditAccount, debitAccount, "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		entryToBook.setReferredTransaction(this);
		entryToBook.setReference(getNumber());

	}

}
