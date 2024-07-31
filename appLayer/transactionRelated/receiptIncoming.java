package appLayer.transactionRelated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

import javax.persistence.Entity;

import appLayer.AccountNotFoundException;
import appLayer.CashFlow;
import appLayer.account;
import appLayer.client;
import appLayer.configs;
import appLayer.entry;
import appLayer.utils;
import appLayer.taxRelated.tax;

@Entity
public class receiptIncoming extends appTransaction {

	private int purpose = 0;// consists of: product types+official entertainment

	public receiptIncoming(transactions parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	public receiptIncoming() {
	}

	@Override
	public String getTransactionName() {
		return Messages.receiptIncoming_transactionName;

	}

	/**
	 * returns the transaction type
	 * 
	 * @return
	 */
	public static int getType() {
		// due to GUILayer/newTransactionSelectTransactionDetails this code must
		// be
		// the index defined in
		// applayer/transactionRelatzed/transactions.java:init()+1
		return 9;
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
	public boolean isVATRequiredInStep(int step) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * usually, incoming invoices relate to services which will create 1-2
	 * entries (net and VAT). If the receipt is for business lunch, however, up
	 * to 3 entries have to be created: net, vat and the not reimbursable part
	 * of the invoice
	 * */
	public void setPurpose(int newPurpose) {
		purpose = newPurpose;
	}

	@Override
	public void createEntriesForWorkflowStep(int step) {
		/**
		 * this function translates a single tax rate (e.g. via "quick"
		 * selection in a table with a combo box of VATs) to the more detailed
		 * and more reliable representation for multiple VAT rates via
		 * addVATAmount/setVATAmount and getVATAmountForTax
		 * */
		prepareVATamounts();
		BigDecimal netValue = getGrossValue();
		for (tax currentTax : client.getTaxes().getVATArray()) {
			BigDecimal VATamount = getVATAmountForTax(currentTax);
//			BigDecimal VATamount = getNetAmountForTax(currentTax);//getVATAmountForTax(currentTax);

			/*
			 * dont do a split booking if a refer to transaction number is set,
			 * because then the VAT has already been booked on creation.
			 */

			estimateCashFlow();
			if ((VATamount!=null)&&(VATamount.compareTo(new BigDecimal(0)) != 0)) {
				entry entryToBook;
				try {

					account credit = client.getAccounts()
							.getTurnoverTaxAccount();
					if (!configs.isTaxmodeIssue()) { //$NON-NLS-1$
						// tax on payment = istbesteuerung

						/**
						 * In this case (tax on payment = istbesteuerung) we
						 * need to balance a second entry to mark the VAT due,
						 * i.e. balance 1766 Ust nicht fällig against 1776 USt
						 * 19%
						 * */
						credit = client.getAccounts().getUndueVatAccount();
					}
					account debit = defaultDebitAccount;

					if (getCashFlow() == CashFlow.SENDING) {
						// we're not getting an invoice, we're paying one
						debit = client.getAccounts().getInputTaxAccount();
						credit = defaultCreditAccount;
					}

					entryToBook = new entry(
							getIssueDate(),
							getTypeName() + " " + getNumber(), VATamount, credit, debit, "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					entryToBook.setReferredTransaction(this);
					entryToBook.setReference(getNumber());
										
					entries.add(entryToBook);
					netValue = netValue.subtract(VATamount);
				} catch (AccountNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

				// this one we only need when... setValue(getValue()-vatAmount);
			}

		}

		// net amount
		// ordinary entry
		if (purpose == 0) {// services
			entry entryToBook = new entry(
					getIssueDate(),
					getTypeName() + " " + getNumber(), netValue, defaultCreditAccount, defaultDebitAccount, "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			entryToBook.setReferredTransaction(this);
			entryToBook.setReference(getNumber());
			entries.add(entryToBook);
		} else if (purpose == 1) {// goods
			entry entryToBook;
			try {
				entryToBook = new entry(getIssueDate(), getTypeName() + " " //$NON-NLS-1$
						+ getNumber(), netValue, defaultCreditAccount, client
						.getAccounts().getGoodsAccount(), "", ""); //$NON-NLS-1$ //$NON-NLS-2$
				entryToBook.setReferredTransaction(this);
				entryToBook.setReference(getNumber());
				entries.add(entryToBook);
			} catch (AccountNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else if (purpose == 2) {// "Products"
			try {
				entry entryToBook = new entry(
						getIssueDate(),
						getTypeName() + " " + getNumber(), netValue, defaultCreditAccount, client.getAccounts().getProductsAccount(), "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				entryToBook.setReferredTransaction(this);
				entryToBook.setReference(getNumber());
				entries.add(entryToBook);
			} catch (AccountNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else if (purpose == 3) {// ,"Raw materials and supplies"
			try {
				entry entryToBook = new entry(
						getIssueDate(),
						getTypeName() + " " + getNumber(), netValue, defaultCreditAccount, client.getAccounts().getRawMaterialsAccount(), "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				entryToBook.setReferredTransaction(this);
				entryToBook.setReference(getNumber());
				entries.add(entryToBook);
			} catch (AccountNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else if (purpose == 4) {// ,"unfinished manufactures"
			try {
				entry entryToBook = new entry(
						getIssueDate(),
						getTypeName() + " " + getNumber(), netValue, defaultCreditAccount, client.getAccounts().getUnfinishedManufacturesAccount(), "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				entryToBook.setReferredTransaction(this);
				entryToBook.setReference(getNumber());
				entries.add(entryToBook);
			} catch (AccountNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else if (purpose == 5) {// official entertainment
			// expenses by german law
			/**
			 * split booking: VAT reimbursable, 70% net reimbursable 30%
			 * non-imbursable net value
			 * */

			account officialEntertainment;
			BigDecimal seventyPercent = utils.round(
					netValue.multiply(new BigDecimal("0.7")), 2); //$NON-NLS-1$
			BigDecimal thirtyPercent = utils.round(
					netValue.subtract(seventyPercent), 2);
			try {
				officialEntertainment = client.getAccounts()
						.getOfficialEntertainmentAccount();
				entry entryToBook = new entry(
						getIssueDate(),
						getTypeName() + " " + getNumber(), seventyPercent, defaultCreditAccount, officialEntertainment, "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				entryToBook.setReferredTransaction(this);
				entryToBook.setReference(getNumber());
				entries.add(entryToBook);
			} catch (AccountNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			account nonDeductibleBusinessExpenses;
			try {
				nonDeductibleBusinessExpenses = client.getAccounts()
						.getNonDeductibleExpensesAccount();
				entry entryToBook = new entry(
						getIssueDate(),
						getTypeName() + " " + getNumber(), thirtyPercent, defaultCreditAccount, nonDeductibleBusinessExpenses, "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				entryToBook.setReferredTransaction(this);
				entryToBook.setReference(getNumber());
				entries.add(entryToBook);
			} catch (AccountNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public boolean hasEmptyReference() {
		return getDefaultReference().isEmpty();
	}

	public void setGoods() {
		// TODO Auto-generated method stub

	}

	@Override
	public BigDecimal getTotal() {
		if (getVAT()==null) return getTotalGross();
		
 		return getTotalGross().divide(getVAT().getFactor(), 2,
				RoundingMode.HALF_UP);
		
	}

}
