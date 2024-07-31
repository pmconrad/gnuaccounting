package appLayer.taxRelated;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import appLayer.Messages;
import dataLayer.DB;

@Entity
public class tax implements Cloneable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String description;
	@Column(precision = 16, scale = 6)
	private BigDecimal value;
	@Transient
	private int IDInList = -1;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int ID; // ID from DB
	private int creditTaxField = 0;
	private int debitTaxField = 0;
	private Timestamp outdated = null;
	@Transient
	taxList parent;
	private boolean isStandardIncoming = false;
	private static tax installationDefaultTax = null;
	public static int newTaxID = 1;

	/*
	 * selectedaccount is the credit account or the debit account - depending on
	 * if the user has selected (s)he has payed or received the VAT
	 */;

	public tax() {

	}

	public tax(taxList parent, int ID, String description, BigDecimal value) {
		this.parent = parent;
		this.ID = ID;
		this.value = value;
		setDescription(description);
	}

	public tax(String description, BigDecimal value) {
		this.value = value;
		setDescription(description);
	}


	public void setParent(taxList parent) {
		this.parent = parent;
	}

	public void setAsDefaultIncomingTax() {
		parent.setDefaultIncomingTax(this);
		isStandardIncoming = true;
		silentSave(); // if this is not a silent save the initial save of the settings won't work because of the signalChanges :-(
	}

	public boolean isDefaultIncomingTax() {
		return parent.getStandardVAT().getID() == getID();
	}

	public boolean hasStandardIncomingAttribute() {
		return isStandardIncoming;
	}

	public void bindToList(int IDinList) {
		this.IDInList = IDinList;
	}

	public String getDescription() {
		return description;
	}

	public int getIDinList() {
		return IDInList;
	}

	public int getID() {
		return ID;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getValue() {
		return value;
	}

	public BigDecimal getFactor() {
		return (new BigDecimal(1).add(value));
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public int getCreditTaxField() {
		return creditTaxField;
	}

	public int getDebitTaxField() {
		return debitTaxField;
	}

	public void setCreditTaxField(int creditTaxField) {
		this.creditTaxField = creditTaxField;

	}

	public void setDebitTaxField(int debitTaxField) {
		this.debitTaxField = debitTaxField;
	}

	@Override
	public String toString() {
		return getDescription();
	}

	@Override
	public tax clone() throws CloneNotSupportedException {
		return (tax) super.clone();
	}

	public void delete() {
		Calendar c = Calendar.getInstance();

		outdated = new Timestamp(c.getTime().getTime());
		save();
		if (parent != null) {
			parent.signalChange(this);
		}
	}
	public void silentSave() {
		DB.getEntityManager().getTransaction().begin();
		DB.getEntityManager().persist(this);
		DB.getEntityManager().getTransaction().commit();
		
	}
	public void save() {
		silentSave();
		if (parent != null) {
			parent.signalChange(this);
		}

	}

	public static tax getNewTax() {
		return new tax(Messages.getString("tax.newVat"), new BigDecimal(0)); //$NON-NLS-1$

	}

	/**
	 * test, if it is marked as deleted.
	 * 
	 * @return true, if it has been deleted
	 */
	public boolean getIsDeleted() {
		return (outdated != null);
	}

	public void disableStandardIncoming() {
		isStandardIncoming = false;

	}

}
