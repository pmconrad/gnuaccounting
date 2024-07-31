package appLayer.taxRelated;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.persistence.Transient;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;

import appLayer.AccountNotFoundException;
import appLayer.Messages;
import appLayer.client;
import appLayer.utils;
import dataLayer.DB;

public class taxList implements Serializable {

	private static final long serialVersionUID = 1L;
	private static Vector<tax> VATs = new Vector<tax>();
	private String emptyName = Messages.getString("taxList.newVAT"); //$NON-NLS-1$
	private String[] taxesStringArray = null;
	// this is the tax which will be applied to incoming invoices=outgoing
	// payments
	// when booked as "standard" from the import queue
	// in germany usually "full VAT"=19%, except if a company is small enough
	// and opted out VAT-->0%
	@Transient
	private tax defaultIncomingTax = null;

	public void getTaxesFromDatabase() {
		VATs.removeAllElements();

		List<tax> retrievals = DB
				.getEntityManager()
				.createQuery("SELECT t FROM tax t WHERE t.outdated IS NULL").getResultList(); //$NON-NLS-1$
		for (Iterator<tax> iter = retrievals.iterator(); iter.hasNext();) {
			tax currentlyRetrieved = iter.next();
			currentlyRetrieved.setParent(this);
			try {
				if (currentlyRetrieved.getCreditTaxField() != 0) {

					client.getAccounts()
							.getInputTaxAccount()
							.setTaxField(currentlyRetrieved.getCreditTaxField());
				}
				if (currentlyRetrieved.getDebitTaxField() != 0) {
					client.getAccounts().getTurnoverTaxAccount()
							.setTaxField(currentlyRetrieved.getDebitTaxField());
				}
			} catch (AccountNotFoundException ex) {
				ex.printStackTrace();
			}
			if (currentlyRetrieved.hasStandardIncomingAttribute()) {
				defaultIncomingTax = currentlyRetrieved;
			}

			VATs.add(currentlyRetrieved);
		}
		int taxesToOmit = 1;
		if (VATs.size() > taxesToOmit) {
			// at the very first start of the software, no taxes are present
			taxesStringArray = new String[VATs.size() - taxesToOmit];
			// strange: tempVec.toArray() does not work
			for (int taxIndex = taxesToOmit; taxIndex < VATs.size(); taxIndex++) {
				tax currentTax = (tax) VATs.get(taxIndex);
				int listIndex = taxIndex - taxesToOmit;
				currentTax.bindToList(listIndex);
				taxesStringArray[listIndex] = currentTax.getDescription();

			}
			if (defaultIncomingTax == null) {
				try {
					defaultIncomingTax = getVATByFactor(new BigDecimal("1.19")); //$NON-NLS-1$
				} catch (taxNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public tax getVATforFieldID(int fieldID) {
		for (tax currentVAT : getVATs()) {
			if ((currentVAT.getCreditTaxField() == fieldID)
					|| (currentVAT.getDebitTaxField() == fieldID)) {
				return currentVAT;
			}
		}
		return null;
	}

	public tax getVATAtListIndex(int listIndex) throws taxNotFoundException {
		for (tax currentVAT : getVATs()) {
			if (currentVAT.getIDinList() == listIndex) {
				return currentVAT;
			}
		}
		throw new taxNotFoundException();
	}

	public void setEmptyElementName(String emptyName) {
		this.emptyName = emptyName;
	}

	public Vector<tax> getVATs() {
		return VATs;
	}

	public tax[] getVATArray() {
		tax[] res = new tax[VATs.size()];

		for (int i = 0; i < VATs.size(); i++) {
			res[i] = VATs.get(i);
		}
		return res;
	}

	/**
	 * without "new VAT"
	 * */
	public tax[] getExistingVATArray() {
		tax[] res = new tax[VATs.size() - 1];

		for (int i = 1; i < VATs.size(); i++) {
			res[i - 1] = VATs.get(i);
		}
		return res;
	}

	public tax getVATByID(int ID) throws taxNotFoundException {
		for (tax currentVAT : getVATs()) {
			if (currentVAT.getID() == ID) {
				return currentVAT;
			}
		}
		throw new taxNotFoundException();
	}

	public tax getFirst() {
		if (VATs.size() > 1) {
			return VATs.get(0);// get the first tax which should be new VAT
		} else {
			return tax.getNewTax();
		}
	}

	/**
	 * returns a 0% VAT
	 * */
	public tax getEmpty() {
		try {
			return getVATByFactor(new BigDecimal(1));
		} catch (taxNotFoundException e) {
			// can not happen because 0-tax may not be deleted or changed
			e.printStackTrace();
			return null;
		}
	}

	public String[] getStringArray() {
		return taxesStringArray;
	}

	public void signalChange(tax changed) {
		getTaxesFromDatabase();
	}

	/**
	 * returns the VAT for the factor
	 * */
	public tax getVATByFactor(BigDecimal vatFactorKey)
			throws taxNotFoundException {
		for (tax currentVAT : getExistingVATArray()) {

			if (currentVAT.getFactor().compareTo(vatFactorKey) == 0) {
				return currentVAT;
			}
		}
		throw new taxNotFoundException();
	}

	/**
	 * returns the VAT for the factor, will create a new VAT if the factor is
	 * unknown
	 * */
	public tax getSafeVATByFactor(BigDecimal vatFactorKey) {
		for (tax currentVAT : getVATs()) {
			if ((currentVAT.getFactor().compareTo(vatFactorKey) == 0)
					&& !currentVAT.getIsDeleted()) {
				return currentVAT;
			}
		}
		tax t = null;
		// new taxes are generated with value (e.g. 0.07) not factor (e.g.
		// 1.07), this is the reason for vatFactorKey-1.0d
		t = new tax(
				this,
				-1,
				Messages.getString("taxList.ImportedNewTaxHeading"), vatFactorKey.subtract(new BigDecimal(1))); //$NON-NLS-1$
		t.save();
		VATs.add(t);
		return t;

	}

	public int getCount() {
		return VATs.size();
	}

	public void setDefaultIncomingTax(tax theTax) {
		if (defaultIncomingTax != null) {
			defaultIncomingTax.disableStandardIncoming();
		}
		defaultIncomingTax = theTax;

	}

	public tax getStandardVAT() {
		return defaultIncomingTax;
	}

}
