package appLayer;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import appLayer.transactionRelated.receiptIncoming;
import dataLayer.DB;

@Entity
public class transactionFromBankAccountImport {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	// scale 2, i.e. 2 digits after the comma, are sufficient for imported
	// entries
	// and if the scale were different the "equals" function would not work
	// properly because
	// bigdecimal 2.00!=bigdecimal 2.000. This would mean that after
	// gnuaccounting is restarted
	// and the same entries are imported, equals would not fire and dupes would
	// be created
	@Column(precision = 16, scale = 2)
	private BigDecimal value;
	private String description;
	private String subjectName, subjectBank, subjectAccount, subjectBankCode;
	private account contraAccount = null;
	@Temporal(value = TemporalType.DATE)
	private Date dateEffective;
	private boolean is_imported = false;
	private Timestamp outdated = null;
	private String reference;

	public transactionFromBankAccountImport() {
		outdated = null;

	}

	public void setContraAccount(account contraAccount) {
		this.contraAccount = contraAccount;
	}

	public int getID() {
		return id;
	}

	public BigDecimal getValue() {
		return value;
	}

	public String getDescription() {
		return description;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public String getSubjectBank() {
		return subjectBank;
	}

	public String getSubjectAccount() {
		return subjectAccount;
	}

	public String getSubjectBankCode() {
		return subjectBankCode;
	}

	public Date getDateEffective() {
		return dateEffective;
	}

	public boolean is_imported() {
		return is_imported;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public void setSubjectBank(String subjectBank) {
		this.subjectBank = subjectBank;
	}

	public void setSubjectAccount(String subjectAccount) {
		this.subjectAccount = subjectAccount;
	}

	public void setSubjectBankCode(String subjectBankCode) {
		this.subjectBankCode = subjectBankCode;
	}

	public void setWhen(Date when) {
		this.dateEffective = when;
	}

	public void setImported() {
		this.is_imported = true;
	}

	public boolean equals(transactionFromBankAccountImport toCompare) {
		return ((toCompare.getValue().equals(getValue()))
				&& (toCompare.getDescription().equals(getDescription()))
				&& (toCompare.getSubjectName().equals(getSubjectName()))
				&& (toCompare.getSubjectBank().equals(getSubjectBank()))
				&& (toCompare.getSubjectAccount().equals(getSubjectAccount()))
				&& (toCompare.getSubjectBankCode().equals(getSubjectBankCode()))
				&& (toCompare.getDateEffective().equals(getDateEffective())) && (toCompare
					.is_imported() == is_imported()));

	}

	public void save() {

		DB.getEntityManager().getTransaction().begin();
		DB.getEntityManager().persist(this);
		DB.getEntityManager().getTransaction().commit();

	}

	public void delete() {
		Calendar c = Calendar.getInstance();

		outdated = new Timestamp(c.getTime().getTime());

		save();
	}

	/**
	 * the contraaccount will be THE, or at least A bank account
	 * */
	public receiptIncoming getAsTransaction() {
		receiptIncoming ri = new receiptIncoming();
		if (contraAccount == null) {
			try {
				contraAccount = client.getAccounts().getBankAccount();
			} catch (AccountNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		account creditAccount = null;
		account debitAccount = null;
		BigDecimal value = getValue();
		if (value.compareTo(new BigDecimal(0)) > 0) {
			debitAccount = contraAccount;
		} else {
			creditAccount = contraAccount;
		}
		String subjectName = getSubjectName();
		String description;
		if (subjectName.length() > 0) {
			description = subjectName + ":" + getDescription(); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			description = getDescription(); //$NON-NLS-1$
		}

		ri.setType(client.getTransactions().getTypeByTypeID(
				receiptIncoming.getType()));
		ri.setVAT(client.getTaxes().getEmpty());
		ri.setIssueDate(getDateEffective());
		ri.setDefaultDescription(description);
		ri.setDefaultCreditAccount(creditAccount);
		ri.setDefaultDebitAccount(debitAccount);

		ri.estimateCashFlow();

		String docNr = client.getDocuments().recognizeDocumentNumberInString(
				description);
		if (docNr != null) {
			ri.setDefaultReference(docNr);
		}
		ri.setContact(client.getContacts().getContactByBankDetails(
				subjectAccount, subjectBankCode, subjectName, subjectName)); //$NON-NLS-1$
		ri.setGrossValue(getValue().abs()); //$NON-NLS-1$
		ri.setImportID(getID()); //$NON-NLS-1$
		// ri.setDefaultReference(getReference());

		return ri;
	}
	/*
	 * public void setReference(String reference) { this.reference = reference;
	 * }
	 * 
	 * private String getReference() {
	 * 
	 * return reference; }
	 */

}
