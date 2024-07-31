package appLayer;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import ag.ion.bion.officelayer.text.ITextDocumentImage;
import ag.ion.bion.officelayer.text.ITextField;
import ag.ion.bion.officelayer.text.ITextRange;
import ag.ion.bion.officelayer.text.TextException;
import ag.ion.noa.graphic.GraphicInfo;
import appLayer.taxRelated.tax;
import appLayer.transactionRelated.appTransaction;

import com.sun.star.container.XNamed;
import com.sun.star.uno.UnoRuntime;

/**
 * Manager for placeholders.
 * 
 * @author Markus Kr�ger
 * @version $Revision: 11615 $
 * @date 11.01.2008
 */
public class placeholderManager {

	/** The character in OpenOffice.org for the start of a placeholder **/
	public static final String START_PLACEHOLDER = "<"; //$NON-NLS-1$
	/** The character in OpenOffice.org for the end of a placeholder **/
	public static final String END_PLACEHOLDER = ">"; //$NON-NLS-1$
	/** The string defining the start of the items block **/
	public static final String ITEMS_START_PLACEHOLDER = "items"; //$NON-NLS-1$
	/** The string defining the end of the items block **/
	public static final String ITEMS_END_PLACEHOLDER = "/items"; //$NON-NLS-1$
	/** The prefix of an item placeholder **/
	public static final String ITEM_PLACEHOLDER = "item:"; //$NON-NLS-1$
	/** The string defining the start of the items block **/
	public static final String TAXES_START_PLACEHOLDER = "taxes"; //$NON-NLS-1$
	/** The string defining the end of the items block **/
	public static final String TAXES_END_PLACEHOLDER = "/taxes"; //$NON-NLS-1$
	public static final String PAYMENT_START_PLACEHOLDER = "payment"; //$NON-NLS-1$
	public static final String PAYMENT_END_PLACEHOLDER = "/payment"; //$NON-NLS-1$
	/** The prefix of an item placeholder **/
	public static final String TAX_PLACEHOLDER = "tax:"; //$NON-NLS-1$
	/** The prefix for a placeholder for the own, sending party **/
	public static final String SENDER_PLACEHOLDER = "sender:"; //$NON-NLS-1$
	/** The prefix of an customer placeholder **/
	public static final String CUSTOMER_PLACEHOLDER = "customer:"; //$NON-NLS-1$
	/** The prefix of an transaction placeholder **/
	public static final String TRANSACTION_PLACEHOLDER = "transaction:"; //$NON-NLS-1$
	/** The prefix of an VAT placeholder **/
	public static final String VAT_PLACEHOLDER = "vat:"; //$NON-NLS-1$
	private DateFormat df = DateFormat.getDateInstance();
	private item currentItem;

	private tax currentTax;
	private appTransaction currentTransaction; 
	private Shell theShell;
	private ITextRange paymentDeductionEnd = null;
	private ITextRange paymentInvoiceEnd = null;
	private ITextRange paymentDeductionStart = null;
	private ITextRange paymentInvoiceStart = null;
	private int itemIndex;

	// ----------------------------------------------------------------------------
	/**
	 * String placeholderNames[] = { ITEM_PLACEHOLDER + "name", //$NON-NLS-1$
	 * ITEM_PLACEHOLDER + "description", //$NON-NLS-1$ ITEM_PLACEHOLDER +
	 * "quantity", //$NON-NLS-1$ ITEM_PLACEHOLDER + "unit", //$NON-NLS-1$
	 * ITEM_PLACEHOLDER + "remarks", //$NON-NLS-1$ ITEM_PLACEHOLDER + "price",
	 * //$NON-NLS-1$ ITEM_PLACEHOLDER + "pricegross", //$NON-NLS-1$
	 * ITEM_PLACEHOLDER + "total", //$NON-NLS-1$ ITEM_PLACEHOLDER +
	 * "totalgross", //$NON-NLS-1$ ITEM_PLACEHOLDER + "vat", //$NON-NLS-1$
	 * TAX_PLACEHOLDER + "name", TAX_PLACEHOLDER + "amount", TAX_PLACEHOLDER +
	 * "net", CUSTOMER_PLACEHOLDER + "number", //$NON-NLS-1$
	 * CUSTOMER_PLACEHOLDER + "name", //$NON-NLS-1$ CUSTOMER_PLACEHOLDER + "co",
	 * //$NON-NLS-1$ CUSTOMER_PLACEHOLDER + "additional", //$NON-NLS-1$
	 * CUSTOMER_PLACEHOLDER + "street", //$NON-NLS-1$ CUSTOMER_PLACEHOLDER +
	 * "zip", //$NON-NLS-1$ CUSTOMER_PLACEHOLDER + "location", //$NON-NLS-1$
	 * CUSTOMER_PLACEHOLDER + "country", //$NON-NLS-1$ CUSTOMER_PLACEHOLDER +
	 * "email", //$NON-NLS-1$ CUSTOMER_PLACEHOLDER + "phone", //$NON-NLS-1$
	 * CUSTOMER_PLACEHOLDER + "fax", //$NON-NLS-1$ CUSTOMER_PLACEHOLDER +
	 * "vatid", //$NON-NLS-1$ CUSTOMER_PLACEHOLDER + "bic", //$NON-NLS-1$
	 * CUSTOMER_PLACEHOLDER + "iban", //$NON-NLS-1$ CUSTOMER_PLACEHOLDER +
	 * "mandate", //$NON-NLS-1$ SENDER_PLACEHOLDER + "name", SENDER_PLACEHOLDER
	 * + "street", SENDER_PLACEHOLDER + "zip", SENDER_PLACEHOLDER + "location",
	 * SENDER_PLACEHOLDER + "country", SENDER_PLACEHOLDER + "bic",
	 * SENDER_PLACEHOLDER + "iban", SENDER_PLACEHOLDER + "creditorid",
	 * TRANSACTION_PLACEHOLDER + "number", //$NON-NLS-1$ TRANSACTION_PLACEHOLDER
	 * + "duedate", //$NON-NLS-1$ TRANSACTION_PLACEHOLDER + "date",
	 * //$NON-NLS-1$ TRANSACTION_PLACEHOLDER + "remarks", //$NON-NLS-1$
	 * TRANSACTION_PLACEHOLDER + "total", //$NON-NLS-1$ TRANSACTION_PLACEHOLDER
	 * + "totalgross", //$NON-NLS-1$ TRANSACTION_PLACEHOLDER + "bezahlcode",
	 * //$NON-NLS-1$ TRANSACTION_PLACEHOLDER + "doctag", //$NON-NLS-1$
	 * TRANSACTION_PLACEHOLDER + "performedfrom", //$NON-NLS-1$
	 * TRANSACTION_PLACEHOLDER + "performedto", //$NON-NLS-1$
	 * TRANSACTION_PLACEHOLDER + "performanceperiod", //$NON-NLS-1$
	 * VAT_PLACEHOLDER + "total", //$NON-NLS-1$ VAT_PLACEHOLDER + "list" };
	 * //$NON-NLS-1$
	 */


	public void setShell(Shell currentShell) {
		this.theShell = currentShell;
	}

	public void setItem(item currentItem, int index) {
		this.currentItem = currentItem;
		this.itemIndex = index;
	}

	public void setTax(tax currentTax) {
		this.currentTax = currentTax;
	}

	public void setTransaction(appTransaction currentTransaction) {
		this.currentTransaction = currentTransaction;
	}

	
	/***
	 * Formats a date and potentially applies a arguments like period-add
	 * 
	 * @param toDisplay
	 * @param placeholder
	 * @return String with formatted date
	 */
	private String getDateString(Date toDisplay, String placeholder) {
		Calendar c = Calendar.getInstance();
		String pattern = ".*period-add=\"(\\d+)\\s*(day|days|weekdays|workingdays)\".*"; //digit whitespace? day(also matches ...days) //$NON-NLS-1$
		String periodAmount = placeholder.replaceAll(pattern, "$1"); //$NON-NLS-1$
		String periodType = placeholder.replaceAll(pattern, "$2"); //$NON-NLS-1$

		if (!periodAmount.equals(placeholder)) {
			// found a period add
			c.setTime(toDisplay);
			
			if (periodType.equals("day") || periodType.equals("days")) { //$NON-NLS-1$ //$NON-NLS-2$
				c.add(Calendar.DATE, Integer.parseInt(periodAmount));
			} else if (periodType.equals("weekdays")) { //$NON-NLS-1$
				int consumed=0;
				while (consumed<Integer.parseInt(periodAmount))  {
					c.add(Calendar.DATE, 1);
					if (utils.isWeekDay(c)) {
						consumed++;
					}
				}
			} else if (periodType.equals("workingdays")) { //$NON-NLS-1$
				int consumed=0;
				while (consumed<Integer.parseInt(periodAmount))  {
					c.add(Calendar.DATE, 1);
					if (utils.isWorkingDay(c)) {
						consumed++;
					}
				}
			}
			toDisplay = c.getTime();
		}
		return df.format(toDisplay);
	}
	
	/***
	 * 
	 * @param placeholder
	 * @return the replacement (String) for the placeholder
	 */
	public String getPlaceholderValue(ITextField placeholder) {
		java.util.Date due = currentTransaction.getDueDate();
		java.util.Date issueDate = currentTransaction.getIssueDate();// new
																		// java.util.Date();
		String itemName = new String(""); //$NON-NLS-1$
		String itemEAN = new String(""); //$NON-NLS-1$
		String itemDescription = new String(""); //$NON-NLS-1$
		String itemQuantity = new String(""); //$NON-NLS-1$
		String itemUnit = new String(""); //$NON-NLS-1$
		String itemRemarks = new String(""); //$NON-NLS-1$
		String itemPrice = new String(""); //$NON-NLS-1$
		String itemPriceGross = new String(""); //$NON-NLS-1$
		String itemTotal = new String(""); //$NON-NLS-1$

		String itemTotalGross = new String(""); //$NON-NLS-1$
		String itemVATdescription = new String(""); //$NON-NLS-1$
		String itemSalesTaxdescription = new String(""); //$NON-NLS-1$

		String taxName = new String(""); //$NON-NLS-1$
		String taxAmount = new String(""); //$NON-NLS-1$
		String taxNet = new String(""); //$NON-NLS-1$

		if (currentItem != null) {

			itemName = currentItem.getProduct().getName();
			itemEAN = currentItem.getProduct().getBarcode();
			itemDescription = currentItem.getProduct().getDescription();
			itemQuantity = currentItem.getQuantity().toString();
			itemUnit = client.getProducts().getUnitName(
					currentItem.getProduct().getUnit());
			itemRemarks = currentItem.getRemarks();
			itemPrice = currentItem.getPriceString();
			itemPriceGross = currentItem.getPriceGrossString();
			itemTotal = currentItem.getTotalString();
			itemTotalGross = currentItem.getTotalGrossString();
			itemVATdescription = currentItem.getProduct().getVAT()
					.getDescription();
			itemSalesTaxdescription = currentItem.getProduct().getSalesTax()
					.getDescription();
		}
		if (currentTax != null) {
			taxName = currentTax.getDescription();
			taxAmount = utils.currencyFormat(currentTransaction
					.getVATAmountForTax(currentTax));
			taxNet = utils.currencyFormat(currentTransaction
					.getNetAmountForTax(currentTax));

		}
		
		String performancePeriod = df.format(currentTransaction
				.getPerformanceStart());
		if (currentTransaction.getPerformanceStart().compareTo(
				currentTransaction.getPerformanceEnd()) != 0) {
			performancePeriod = performancePeriod.concat(" - ").concat( //$NON-NLS-1$
					df.format(currentTransaction.getPerformanceEnd()));
		}

		if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER + ITEM_PLACEHOLDER
				+ "name")) { //$NON-NLS-1$
			return itemName;
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER + ITEM_PLACEHOLDER
				+ "index")) { //$NON-NLS-1$
			return Integer.toString(itemIndex);
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER + ITEM_PLACEHOLDER
				+ "ean")) { //$NON-NLS-1$
			return itemEAN;
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER + ITEM_PLACEHOLDER
				+ "description")) { //$NON-NLS-1$
			return itemDescription;
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER + ITEM_PLACEHOLDER
				+ "quantity")) { //$NON-NLS-1$
			return itemQuantity;
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER + ITEM_PLACEHOLDER
				+ "unit")) { //$NON-NLS-1$
			return itemUnit;
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER + ITEM_PLACEHOLDER
				+ "remarks")) { //$NON-NLS-1$
			return itemRemarks;
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER + ITEM_PLACEHOLDER
				+ "pricegross")) { //$NON-NLS-1$
			return itemPriceGross;
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER + ITEM_PLACEHOLDER
				+ "price")) { //$NON-NLS-1$
			return itemPrice;
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER + ITEM_PLACEHOLDER
				+ "totalgross")) { //$NON-NLS-1$
			return itemTotalGross;
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER + ITEM_PLACEHOLDER
				+ "total")) { //$NON-NLS-1$
			return itemTotal;
		}  else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER + ITEM_PLACEHOLDER
				+ "vat")) { //$NON-NLS-1$
			return itemVATdescription;
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER + ITEM_PLACEHOLDER
				+ "salestax")) { //$NON-NLS-1$
			return itemSalesTaxdescription;
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER + TAX_PLACEHOLDER
				+ "name")) { //$NON-NLS-1$

			return taxName;
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER + TAX_PLACEHOLDER
				+ "amount")) { //$NON-NLS-1$
			return taxAmount;
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER + TAX_PLACEHOLDER
				+ "net")) { //$NON-NLS-1$
			return taxNet;
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ CUSTOMER_PLACEHOLDER + "number")) { //$NON-NLS-1$
			return currentTransaction.getContact().getNumber();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ CUSTOMER_PLACEHOLDER + "name")) { //$NON-NLS-1$
			return currentTransaction.getContact().getName();
		}  else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ CUSTOMER_PLACEHOLDER + "additional")) { //$NON-NLS-1$
			return currentTransaction.getContact().getAdditionalAddressLine();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ CUSTOMER_PLACEHOLDER + "street")) { //$NON-NLS-1$
			return currentTransaction.getContact().getStreet();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ CUSTOMER_PLACEHOLDER + "zip")) { //$NON-NLS-1$
			return currentTransaction.getContact().getZIP();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ CUSTOMER_PLACEHOLDER + "location")) { //$NON-NLS-1$
			return currentTransaction.getContact().getLocation();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ CUSTOMER_PLACEHOLDER + "country")) { //$NON-NLS-1$
			return currentTransaction.getContact().getCountry();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ CUSTOMER_PLACEHOLDER + "co")) { //$NON-NLS-1$
			// replace startswith CO after the more specific COUNTRY
			return currentTransaction.getContact().getCO();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ CUSTOMER_PLACEHOLDER + "email")) { //$NON-NLS-1$
			return currentTransaction.getContact().getEmail();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ CUSTOMER_PLACEHOLDER + "phone")) { //$NON-NLS-1$
			return currentTransaction.getContact().getPhone();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ CUSTOMER_PLACEHOLDER + "fax")) { //$NON-NLS-1$
			return currentTransaction.getContact().getFax();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ CUSTOMER_PLACEHOLDER + "vatid")) { //$NON-NLS-1$
			return currentTransaction.getContact().getVATID();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ CUSTOMER_PLACEHOLDER + "bic")) { //$NON-NLS-1$
			return currentTransaction.getContact().getBIC();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ CUSTOMER_PLACEHOLDER + "iban")) { //$NON-NLS-1$
			return currentTransaction.getContact().getIBAN();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ CUSTOMER_PLACEHOLDER + "mandate")) { //$NON-NLS-1$
			return currentTransaction.getContact().getSEPAmandate();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ SENDER_PLACEHOLDER + "name")) { //$NON-NLS-1$
			return configs.getOrganisationName();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ SENDER_PLACEHOLDER + "street")) { //$NON-NLS-1$
			return configs.getOrganisationStreet();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ SENDER_PLACEHOLDER + "zip")) { //$NON-NLS-1$
			return configs.getOrganisationZip();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ SENDER_PLACEHOLDER + "location")) { //$NON-NLS-1$
			return configs.getOrganisationLocation();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ SENDER_PLACEHOLDER + "country")) { //$NON-NLS-1$
			return configs.getOrganisationCountry();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ SENDER_PLACEHOLDER + "bic")) { //$NON-NLS-1$
			return configs.getBIC();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ SENDER_PLACEHOLDER + "iban")) { //$NON-NLS-1$
			return configs.getIBAN();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ SENDER_PLACEHOLDER + "creditorid")) { //$NON-NLS-1$
			return configs.getCreditorID();
		}  else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ SENDER_PLACEHOLDER + "vatid")) { //$NON-NLS-1$
			return configs.getVATID();
		}  else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ SENDER_PLACEHOLDER + "bankname")) { //$NON-NLS-1$
			return configs.getBankName();
		}  else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ SENDER_PLACEHOLDER + "holder")) { //$NON-NLS-1$
			return configs.getHolderName();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ TRANSACTION_PLACEHOLDER + "number")) { //$NON-NLS-1$
			return currentTransaction.getNumber();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ TRANSACTION_PLACEHOLDER + "duedate")) { //$NON-NLS-1$
			return getDateString(due, placeholder.getDisplayText());
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ TRANSACTION_PLACEHOLDER + "date")) { //$NON-NLS-1$
			// Extract the text between the two title elements
			return getDateString(issueDate, placeholder.getDisplayText());
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ TRANSACTION_PLACEHOLDER + "remarks")) { //$NON-NLS-1$
			return currentTransaction.getRemarks();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ TRANSACTION_PLACEHOLDER + "totalgross")) { //$NON-NLS-1$
			// we need to check for start w/ "totalgross" before checking start w/ "total" only
			return currentTransaction.getTotalGrossString();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ TRANSACTION_PLACEHOLDER + "total")) { //$NON-NLS-1$
			return currentTransaction.getTotalString();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ TRANSACTION_PLACEHOLDER + "bezahlcode")) { //$NON-NLS-1$

			GraphicInfo bezahlcode = client.getTransactions()
					.getCurrentTransaction().getBezahlcodeQRCode();
			if (bezahlcode == null) {
				MessageDialog
						.openError(
								theShell,
								Messages.getString("placeholderManager.bezahlcodeErrorHeadline"), Messages.getString("placeholderManager.bezahlcodeErrorText")); //$NON-NLS-1$ //$NON-NLS-2$
			} else {

				ITextDocumentImage textDocumentImage;
				try {
					textDocumentImage = placeholder
							.getTextDocument().getTextService()
							.getTextContentService()
							.constructNewImage(bezahlcode);
					placeholder.getTextDocument().getTextService()
					.getTextContentService()
					.insertTextContent(placeholder.getTextRange(), textDocumentImage);
					// set name
					XNamed xNamed = (XNamed) UnoRuntime.queryInterface(
							XNamed.class, textDocumentImage.getXTextContent());
					xNamed.setName(Messages.getString("placeholderManager.bezahlcodeImageTitle"));  //$NON-NLS-1$
				} catch (TextException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			return ""; //$NON-NLS-1$
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ TRANSACTION_PLACEHOLDER + "doctag")) { //$NON-NLS-1$

			GraphicInfo doctag = client.getTransactions()
					.getCurrentTransaction().getDocTagQRCode();

			ITextDocumentImage textDocumentImage;
			try {
				textDocumentImage = placeholder
						.getTextDocument().getTextService()
						.getTextContentService().constructNewImage(doctag);
				placeholder.getTextDocument().getTextService()
						.getTextContentService()
						.insertTextContent(placeholder.getTextRange(), textDocumentImage);

				// set name
				XNamed xNamed = (XNamed) UnoRuntime.queryInterface(
						XNamed.class, textDocumentImage.getXTextContent());
				xNamed.setName("doctag QR Code");  //$NON-NLS-1$
			} catch (TextException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return ""; //$NON-NLS-1$
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ TRANSACTION_PLACEHOLDER + "performedfrom")) { //$NON-NLS-1$
			return getDateString(currentTransaction.getPerformanceStart(), placeholder.getDisplayText());
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ TRANSACTION_PLACEHOLDER + "performedto")) { //$NON-NLS-1$
			return getDateString(currentTransaction.getPerformanceEnd(), placeholder.getDisplayText());
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER
				+ TRANSACTION_PLACEHOLDER + "performanceperiod")) { //$NON-NLS-1$
			return performancePeriod;
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER + VAT_PLACEHOLDER
				+ "total")) { //$NON-NLS-1$
			return currentTransaction.getVatTotal();
		} else if (placeholder.getDisplayText().startsWith(START_PLACEHOLDER + VAT_PLACEHOLDER
				+ "list")) { //$NON-NLS-1$
			return currentTransaction.getVatList();
		} else if (placeholder.getDisplayText().startsWith(placeholderManager.START_PLACEHOLDER
				+ placeholderManager.PAYMENT_START_PLACEHOLDER+"-deduction")) { //$NON-NLS-1$
			paymentDeductionStart = placeholder.getTextRange(); // store, might be start of deletion later
			return ""; //$NON-NLS-1$
		} else if (placeholder.getDisplayText().startsWith(placeholderManager.START_PLACEHOLDER
				+ placeholderManager.PAYMENT_END_PLACEHOLDER+"-deduction")) { //$NON-NLS-1$
				paymentDeductionEnd = placeholder.getTextRange();// store, might be end of deletion later
				return ""; //$NON-NLS-1$
		} else if (placeholder.getDisplayText().startsWith(placeholderManager.START_PLACEHOLDER
				+ placeholderManager.PAYMENT_START_PLACEHOLDER+"-invoice")) { //$NON-NLS-1$
			paymentInvoiceStart = placeholder.getTextRange(); // store, might be start of deletion later
			return ""; //$NON-NLS-1$
		} else if (placeholder.getDisplayText().startsWith(placeholderManager.START_PLACEHOLDER
				+ placeholderManager.PAYMENT_END_PLACEHOLDER+"-invoice")) { //$NON-NLS-1$
				paymentInvoiceEnd = placeholder.getTextRange();// store, might be end of deletion later
				return ""; //$NON-NLS-1$
		}

		// throw new IllegalArgumentException();
		return Messages.getString("placeholderManager.placeholder") + placeholder.getDisplayText() + Messages.getString("placeholderManager.notfound"); //$NON-NLS-1$ //$NON-NLS-2$

	}

	public ITextRange getDeductionEnd() {
		return paymentDeductionEnd;
	}

	public ITextRange getDeductionStart() {
		return paymentDeductionStart;
	}


	public ITextRange getInvoiceEnd() {
		return paymentInvoiceEnd;
	}

	public ITextRange getInvoiceStart() {
		return paymentInvoiceStart;
	}

}
