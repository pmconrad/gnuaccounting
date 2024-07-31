package appLayer.taxRelated;

import java.util.Vector;

import appLayer.configs;

public class state {
	private String name;
	private String taxCodeFormat;

	private Vector<String> taxCodes = new Vector<String>();

	state(String name, String taxCodeFormat, String firstTaxCode) {
		this.name = name;
		this.taxCodeFormat = taxCodeFormat;
		addTaxCode(firstTaxCode);

	}

	void addTaxCode(String additionalTaxCode) {
		taxCodes.add(additionalTaxCode);

	}

	public String toString() {
		return getName();
	}

	public String getOrderNumber(IRSoffice localTaxOffice) {
		String orderNumber = localTaxOffice.getID()
				+ "0" + getTaxIDWithoutFirstSlice(); //$NON-NLS-1$
		return orderNumber;
	}

	private String getTaxIDWithoutFirstSlice() {
		String taxID = configs.getTaxID();
		int offset = taxID.indexOf("/") + 1; //$NON-NLS-1$
		if (offset < 0) {
			offset = taxID.indexOf(" ") + 1; //$NON-NLS-1$
		}

		String taxIDPart = taxID.substring(offset);
		String cleanStr = new String();
		for (int i = 0; i < taxIDPart.length(); i++) {
			if ((taxIDPart.charAt(i) != '/') && (taxIDPart.charAt(i) != ' ')) {
				cleanStr += taxIDPart.charAt(i);
			}

		}

		return cleanStr;
	}

	public String getName() {
		return name;
	}

	public String getTaxCodeFormat() {
		return taxCodeFormat;
	}

}
