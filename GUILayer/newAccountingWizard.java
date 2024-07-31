package GUILayer;

import java.io.FileNotFoundException;
import java.util.Vector;
import org.eclipse.jface.wizard.Wizard;
import appLayer.AccountNotFoundException;
import appLayer.account;
import appLayer.client;
import appLayer.transactionRelated.appTransaction;
import appLayer.transactionRelated.receiptIncoming;

public class newAccountingWizard extends Wizard {
	private newAccountingWizardImport page1;
	private boolean includeImport = true;
	private newAccountingWizardAdd page2;
	private Vector<receiptIncoming> dropBook = new Vector<receiptIncoming>();
	private Vector<receiptIncoming> dropDelete = new Vector<receiptIncoming>();
	private Vector<appTransaction> dropMore = new Vector<appTransaction>();
	private Vector<receiptIncoming> dropStandard = new Vector<receiptIncoming>();

	public newAccountingWizard(boolean includeImportStep) {
		dropBook = new Vector<receiptIncoming>();
		dropDelete = new Vector<receiptIncoming>();
		dropMore = new Vector<appTransaction>();
		dropStandard = new Vector<receiptIncoming>();

		includeImport = includeImportStep;
		page1 = new newAccountingWizardImport(this);
		page2 = new newAccountingWizardAdd(this, true);

	}

	public void addPages() {
		if (includeImport) {
			addPage(page1);
		}
		addPage(page2);
	}

	public void performImport() {
		client.getImportQueue().setContraAccount(
				page1.getSelectedContraAccount());
		if (!page1.getMoneyplexImportFilename().equals("")) { //$NON-NLS-1$
			try {
				page1.performMoneyplexImport(page1.getMoneyplexImportFilename());
				page2.refreshTable();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (!page1.getStarmoneyImportFilename().equals("")) { //$NON-NLS-1$
			try {
				page1.performStarmoneyImport(page1.getStarmoneyImportFilename());
				page2.refreshTable();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (!page1.getCSVImportFilename().equals("")) { //$NON-NLS-1$
			try {
				page1.performCSVImport(page1.getCSVImportFilename());
				page2.refreshTable();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (!page1.getHibiscusImportFilename().equals("")) { //$NON-NLS-1$
			try {
				page1.performHibiscusImport(page1.getHibiscusImportFilename());
				page2.refreshTable();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public Vector<receiptIncoming> getBookVector() {
		return dropBook;
	}

	public Vector<receiptIncoming> getBookStandardVector() {
		return dropStandard;
	}

	public Vector<receiptIncoming> getDeleteVector() {
		return dropDelete;
	}

	public Vector<appTransaction> getMoreVector() {
		return dropMore;
	}

	@Override
	public boolean performFinish() {
		// book all entries in book-with-standards queue
		boolean anythingHappened = false;
		for (appTransaction theTransaction : dropStandard) {
			// all transactions are receiptIncoming at this stage, but if a
			// number is recognized the
			// type might change to invoice later
			if ((theTransaction.getTypeID() == receiptIncoming.getType())
					&& (!((receiptIncoming) theTransaction).hasEmptyReference())) {// should
																					// not
																					// be
																					// empty
																					// as
																					// that
																					// would
																					// have
																					// been
																					// rejected
																					// when
																					// dropping
				anythingHappened = true;
				account bankAccount = null;
				account payableAccount = null;
				account revenueAccount = null;
				try {
					bankAccount = page1.getSelectedContraAccount();
					payableAccount = client.getAccounts().getPayableAccount();
					revenueAccount = client.getAccounts().getRevenuesAccount();
				} catch (AccountNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if ((theTransaction.getDefaultCreditAccount()!=null)&&(theTransaction.getDefaultCreditAccount().getID() == bankAccount
						.getID())) {
					theTransaction.setDefaultDebitAccount(payableAccount);
				} else {
					theTransaction.setDefaultCreditAccount(revenueAccount);
				}

				appTransaction loadedTrans = client.getTransactions()
						.getForNumber(theTransaction.getDefaultReference());
				if (loadedTrans != null) {
					theTransaction = loadedTrans;

				} else {
					theTransaction.setVAT(client.getTaxes().getStandardVAT());
				}
				theTransaction.book();
				theTransaction.removeFromImport(true);
				todoWindow.refreshToDoList();

			}
		}
		// book all entries in book-directly queue
		for (receiptIncoming theTransaction : dropBook) {
			if (!theTransaction.hasMissingAccounts()) {// should not be empty as that would
											// have been rejected when dropping
				theTransaction.book();
				anythingHappened = true;
				theTransaction.removeFromImport(true);
			}
		}
		for (receiptIncoming theTransaction : dropDelete) {
			anythingHappened = true;
			theTransaction.removeFromImport(false);
		}
		if (dropMore.size() != 0) {
			transactionDetailWindow tdw = new transactionDetailWindow(dropMore);
			tdw.open();
		}
		if (anythingHappened) {
			client.getEntries().getEntriesFromDatabase();
		}
		accountingEditWindow.refreshAccountingEntries();
		return true;
	}

	@Override
	public boolean performCancel() {
		// reload the import queue, otherwise potential entries in the stacks
		// may not show again until software restart
		// =discard booking stacks
		client.getImportQueue().load();
		return super.performCancel();
	}

}
