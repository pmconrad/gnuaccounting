package appLayer.transactionRelated;

import java.util.HashMap;

import javax.persistence.Entity;

import appLayer.CashFlow;
import appLayer.Messages;
import appLayer.account;
import appLayer.client;
import appLayer.entry;

@Entity
public class cancelation extends appTransaction {
	public cancelation() {
	}

	public cancelation(transactions parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getTransactionName() {
		return "cancelation"; //$NON-NLS-1$

	}

	public static int getType() {
		// due to GUILayer/newTransactionSelectTransactionDetails this code must
		// be
		// the index defined in
		// applayer/transactionRelatzed/transactions.java:init()+1
		return 3;
	}

	@Override
	public int getNumWorkflowSteps() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public HashMap<Integer, String> getTodoItems() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CashFlow getCashFlow() {
		return CashFlow.UNDEFINED;
	}

	@Override
	public boolean isVATRequiredInStep(int step) {
		return false;
	}

	@Override
	public void createEntriesForWorkflowStep(int step) {
		for (entry originalEntry : loadEntriesFromTransID(getRefersTo())) {
			/*
			 * we book a cancelation by booking the debit of the original
			 * transactions on the credit side and the credit of the original
			 * transactions on the debit side...
			 */
			account creditAccount = originalEntry.getDebitAccount();
			account debitAccount = originalEntry.getCreditAccount();

			String number = getNumber();
			String dateIssue = getIssueDate().toString();
			String dateDue = getDueDate().toString();

			// create entry WITHOUT the number here, otherwise the document with
			// the number is assumed to be consumed
			entry entryToBook = new entry(
					getIssueDate(),
					Messages.getString("cancelation.cancelationOf") + number + " (" + dateIssue + Messages.getString("cancelation.due") + dateDue + ")", originalEntry.getValue(), creditAccount, debitAccount, "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			entryToBook.setReferredTransaction(this);
			entryToBook.setReference(getNumber());
			entries.add(entryToBook);
		}
		if (getRefersTo() != -1) {
			// getRefersTo==-1 should not happen at all because it means that
			// somebody writes a
			// cancellation without referring to an original
			client.getTransactions().getByID(getRefersTo()).advanceWorkflow();
		}
	}

}
