package appLayer.transactionRelated;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.mustangproject.ZUGFeRD.IZUGFeRDAllowanceCharge;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableTransaction;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporter;

import appLayer.AccountNotFoundException;
import appLayer.CashFlow;
import appLayer.account;
import appLayer.accountException;
import appLayer.application;
import appLayer.client;
import appLayer.configs;
import appLayer.entry;
import appLayer.item;

@Entity
public class invoice extends appTransaction implements
		IZUGFeRDExportableTransaction {
	@Transient
	account pendingCreditAccount = null;
	@Transient
	account pendingDebitAccount = null;
	@Transient
	account finishCreditAccount = null;
	@Transient
	account finishDebitAccount = null;

	public invoice() {
		super();

	}

	public void saveCurrentTransactionAsZugferd() {
		PDDocument doc;
		try {
			// automatically add Zugferd to all outgoing invoices
			ZUGFeRDExporter ze = new ZUGFeRDExporter();
			ze.ignoreA1Errors();// OpenOffice.org only exports valid A1 anyway
			ze.PDFmakeA3compliant(client.getTransactions()
					.getCurrentTransaction().getFilenamePDF(), application.getAppName(),
					System.getProperty("user.name"), true); //$NON-NLS-1$
			ze.PDFattachZugferdFile(this);

			ze.export(client.getTransactions()
					.getCurrentTransaction().getFilenamePDF());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public invoice(transactions parent) {
		super(parent);
	}

	private void prepareEntryCreation() {
		try {
			pendingDebitAccount = client.getAccounts().getReceivablesAccount();
			pendingCreditAccount = client.getAccounts().getRevenuesAccount();

			finishDebitAccount = client.getAccounts().getBankAccount();
			finishCreditAccount = client.getAccounts().getReceivablesAccount();
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
		return 1;
	}

	@Override
	public String getTransactionName() {
		return "invoice"; //$NON-NLS-1$
	}

	@Override
	public int getNumWorkflowSteps() {
		// TODO Auto-generated method stub
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
		return CashFlow.RECEIVING;
	}

	@Override
	public boolean isVATRequiredInStep(int step) {

		return step == 1;
	}

	@Override
	public void createEntriesForWorkflowStep(int step) {
		account creditAccount = null;
		account debitAccount = null;
		prepareEntryCreation();
		if (workflowStep == 1) {

			creditAccount = pendingCreditAccount;

			if (getContact() != null) {
				try {
					creditAccount.setReferTo(getContact().getID());
				} catch (accountException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			HashMap<account, BigDecimal> valuesToBook = new HashMap<account, BigDecimal>();
			/*
			 * if (getItems().size()==0) {* apparently we're importing a invoice
			 * * valuesToBook.put(defaultDebitAccount, getGrossValue());
			 * valuesToBook.put(defaultCreditAccount, getGrossValue());
			 * 
			 * }
			 */
			if (isBalanced()) {
				try {
					pendingDebitAccount = client.getAccounts().getCashAccount();
				} catch (AccountNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (isVATRequiredInStep(workflowStep)) {
				// add total of all items and add their respective vat values to
				// the hashmap valuesToBook
				for (item currentItem : getItems()) {
					BigDecimal currentVal;

					// find out which net value to book on which account
					currentVal = valuesToBook.get(pendingDebitAccount);
					if (currentVal == null) {
						currentVal = new BigDecimal(0);
					}
					currentVal = currentVal.add(currentItem.getTotal());

					currentVal = currentVal.setScale(2,
							BigDecimal.ROUND_HALF_UP); // first, round so that
														// e.g.
														// 1.189999999999999946709294817992486059665679931640625
														// becomes 1.19

					if (currentVal.compareTo(new BigDecimal(0)) > 0) {
						valuesToBook.put(pendingDebitAccount, currentVal);
					}
					account vatAccount;
					try {
						vatAccount = client.getAccounts()
								.getTurnoverTaxAccount();
						if ((!configs.isTaxmodeIssue()) && (!isBalanced())) {
							// with tax on payment, the issuer owes the VAT to
							// the IRS only after the amout has been balanced
							vatAccount = client.getAccounts()
									.getUndueVatAccount();
						}
						currentVal = valuesToBook.get(vatAccount);
						if (currentVal == null) {
							currentVal = new BigDecimal(0);
						}
						currentVal = currentVal.add(currentItem.getTotalGross()
								.subtract(currentItem.getTotal())); // get the
						// amount of VAT
						// spend
						currentVal = currentVal.setScale(2,
								BigDecimal.ROUND_HALF_UP); // first, round so
															// that e.g.
															// 1.189999999999999946709294817992486059665679931640625
															// becomes 1.19

						if (currentVal.compareTo(new BigDecimal(0)) > 0) {
							valuesToBook.put(vatAccount, currentVal);
						}
					} catch (AccountNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}// questionable
						// now do the same with the VAT amount

				}
			}

			BigDecimal grossAmount = getTotalGross();
			grossAmount = grossAmount.setScale(2, BigDecimal.ROUND_HALF_UP); // first,
																				// round
																				// so
																				// that
																				// e.g.
																				// 1.189999999999999946709294817992486059665679931640625
																				// becomes
																				// 1.19
			setGrossValue(grossAmount);

			for (account currentAccount : valuesToBook.keySet()) {
				BigDecimal toBook = valuesToBook.get(currentAccount);
				try {
					if ((currentAccount.getID() == client.getAccounts()
							.getTurnoverTaxAccount().getID())
							|| (currentAccount.getID() == client.getAccounts()
									.getUndueVatAccount().getID())) {
						// if we're booking against a VAT account which can be
						// 1776 in SKR03/tax on issue=Sollbesteuerung or 1766 in
						// SKR03/tax on payment=Istbesteuerung or their SKR04
						// pendants
						creditAccount = currentAccount;

						debitAccount = pendingDebitAccount;
					} else {
						// not booking against VAT
						creditAccount = pendingCreditAccount;
						debitAccount = pendingDebitAccount;
					}
				} catch (AccountNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// create entry WITHOUT the number here, otherwise the document
				// with the number is assumed to be consumed
				entry entryToBook = new entry(
						getIssueDate(),
						getTypeName() + " " + getNumber(), toBook, creditAccount, debitAccount, "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				entryToBook.setReferredTransaction(this);
				entryToBook.setContact(getRecipient());
				setDefaultCreditAccount(creditAccount);
				setDefaultDebitAccount(debitAccount);

				entryToBook.setReference(getNumber());
				entries.add(entryToBook);

			}
			if (isBalanced()) {
				// step 1 is issue, step 2 is payment, after every booking of a
				// transaction
				// the workflow is advanced by one - but in this case we need it
				// to jump from 0 to 2
				// so alredy advance it here
				advanceWorkflow();
			}

		} else if (workflowStep == 2) {
			// 2nd Workflow step: balancing the payment
			entry balanceEntry = new entry(
					getIssueDate(),
					getTypeName() + " " + getNumber(), getTotalGross(), finishCreditAccount, finishDebitAccount, "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			balanceEntry.setReferredTransaction(this);
			balanceEntry.setReference(getNumber());

			entries.add(balanceEntry);
			if (!configs.isTaxmodeIssue()) { //$NON-NLS-1$
				// tax on payment = istbesteuerung

				/**
				 * In this case (tax on payment = istbesteuerung) we need to
				 * balance a second entry to mark the VAT due, i.e. balance 1766
				 * Ust nicht fällig against 1776 USt 19%
				 * */
				entry taxDueEntry = new entry(
						getIssueDate(),
						getTypeName() + " " + getNumber(), getTotalGross().subtract(getTotal()), creditAccount, debitAccount, "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				taxDueEntry.setReferredTransaction(this);
				taxDueEntry.setReference(getNumber());

				try {
					taxDueEntry.setCreditAccount(client.getAccounts()
							.getUndueVatAccount()); //$NON-NLS-1$
					taxDueEntry.setDebitAccount(client.getAccounts()
							.getTurnoverTaxAccount()); //$NON-NLS-1$
				} catch (AccountNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				entries.add(taxDueEntry);

			}

		}

	}

	@Override
	public void export() {

		super.export();
		saveCurrentTransactionAsZugferd();

	}

	/**
	 * Shortcut for ZUGFeRD export
	 */
	@Override
	public String getOwnBIC() {
		return configs.getBIC();
	}

	/**
	 * Shortcut for ZUGFeRD export
	 */
	@Override
	public String getOwnBankName() {
		return configs.getBankName();
	}

	/**
	 * Shortcut for ZUGFeRD export
	 */
	@Override
	public String getOwnIBAN() {
		return configs.getIBAN();
	}

	/**
	 * Shortcut for ZUGFeRD export
	 */
	@Override
	public String getOwnTaxID() {
		return configs.getTaxID();
	}

	/**
	 * Shortcut for ZUGFeRD export
	 */
	@Override
	public String getOwnVATID() {
		return configs.getVATID();
	}

	/**
	 * Shortcut for ZUGFeRD export
	 */
	@Override
	public String getOwnOrganisationName() {
		return configs.getOrganisationName();
	}

	
	@Override
	public Date getDeliveryDate() {
		return getPerformanceStart();
	}

	@Override
	public String getOwnCountry() {
		return configs.getOrganisationCountry();
	}

	@Override
	public String getOwnLocation() {
		return configs.getOrganisationLocation();
	}

	@Override
	public String getOwnStreet() {
		return configs.getOrganisationStreet();
	}

	@Override
	public String getOwnZIP() {
		return configs.getOrganisationZip();
	}

	@Override
	public String getOwnOrganisationFullPlaintextInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCurrency() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOwnPaymentInfoText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPaymentTermDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IZUGFeRDAllowanceCharge[] getZFAllowances() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IZUGFeRDAllowanceCharge[] getZFCharges() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IZUGFeRDAllowanceCharge[] getZFLogisticsServiceCharges() {
		// TODO Auto-generated method stub
		return null;
	}

}
