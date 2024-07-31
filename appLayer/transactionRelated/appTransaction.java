package appLayer.transactionRelated;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.kapott.hbci.exceptions.InvalidArgumentException;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableItem;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import GUILayer.newTransactionSelectItemDetails;
import ag.ion.noa.NOAException;
import ag.ion.noa.graphic.GraphicInfo;
import appLayer.CashFlow;
import appLayer.IPaymentDataProvider;
import appLayer.Messages;
import appLayer.account;
import appLayer.client;
import appLayer.configs;
import appLayer.contact;
import appLayer.document;
import appLayer.elementNotFoundException;
import appLayer.entry;
import appLayer.item;
import appLayer.product;
import appLayer.transactionFromBankAccountImport;
import appLayer.utils;
import appLayer.taxRelated.tax;
import appLayer.taxRelated.taxNotFoundException;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sun.star.text.HoriOrientation;
import com.sun.star.text.TextContentAnchorType;
import com.sun.star.text.VertOrientation;

import dataLayer.DB;

class pageActivator implements appLayer.IItemListener {
	private newTransactionSelectItemDetails parentPage = null;

	public pageActivator(newTransactionSelectItemDetails parentPage) {
		this.parentPage = parentPage;
	}

	public void onProductChange(product newProduct) {
		// the wizard page can not be completed with the default product
		// (<please select>)
		if (parentPage != null) {
			parentPage.checkPageComplete();
		}
	}

}

@Entity
public abstract class appTransaction implements IPaymentDataProvider,
		IStructuredContentProvider {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int transactionID = -1;

	@OneToMany(mappedBy = "referredTransaction", cascade = CascadeType.PERSIST)
	protected List<entry> entries = new ArrayList<entry>();
	@Transient
	private HashMap<Integer, BigDecimal> VATAmounts = new HashMap<Integer, BigDecimal>();
	@Transient
	private HashMap<tax, BigDecimal> VATAmountsFromDetailUserSelection = new HashMap<tax, BigDecimal>();
	private CashFlow cashFlowDirection = CashFlow.UNDEFINED;

	private tax VAT;

	private int import_item_id = -1;
	@Lob
	protected String defaultDescription;
	protected account defaultCreditAccount;
	protected account defaultDebitAccount;
	@Lob
	protected String defaultReference;
	/** @var defaultvalue is the NET value */
	@Column(precision = 16, scale = 6)
	// for transactions we only store gross values - the net value is only
	// calculated along with the VATS in prepareVATAmounts
	// e.g. transactionDetailWindow iterated over getVATAmountForTax to display
	// the net amount in the end
	// Remember: appTransactions are also created from Bank Account Import and
	// the gross value is the only thing it knows
	protected BigDecimal grossValue;
	@Lob
	protected String defaultComment;
	@Transient
	private TableViewer tableViewer = null;
	@Transient
	private int selectedItemIndex = -1;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.PERSIST)
	private List<item> items = new ArrayList<item>();
	private int itemIndex = 0;// will increase to assign IDs to new items (will
								// not decrease if entries are deleted)
	@Transient
	private newTransactionSelectItemDetails parentPage;

	contact recipient;
	String number = null;
	int precedingTransactionID = -1;
	@Temporal(value = TemporalType.DATE)
	Date dueDate;
	@Temporal(value = TemporalType.DATE)
	Date issueDate;

	/**date of delivery or performance of works*/
	@Temporal(value = TemporalType.DATE)
	private Date performanceStart;

	@Temporal(value = TemporalType.DATE)
	private Date performanceEnd;

	int workflowStep = 1;
	private boolean isTaxExempt=false;

	@Lob
	String remarks;
	String filenameODT, filenamePDF, filenameOT, filenameRelativeODT,
			filenameRelativePDF, filenameRelativeOT;
	@Transient
	transactions parent = null;
	transactionType type;
	/**
	 * @var a transaction is "dirty" if somebody has started to write data to
	 *      it, e.g. created items. it will be set Dirty by setDirty and checked
	 *      by isDirty. It is interesting if a transaction is cancelled in the
	 *      process of creation because if afterwards a new transaction is to be
	 *      created, it will be checked if it is already "dirty" and discarded
	 *      if it is
	 */
	private boolean dirty = false;

	@Lob
	private String paymentPurpose;

	@OneToOne(cascade = CascadeType.PERSIST)
	private document transDocument;

	@Transient
	// whether the transaction (typically an invoice) is to be booked on
	// receivables or directly to revenues
	private boolean isBalanced;


	public appTransaction() {
		prepare();
	}

	public appTransaction(transactions parent) {
		this.parent = parent;
		prepare();
	}

	private void prepare() {
		setDirty(false);
		recipient = null;

		Calendar cal = new GregorianCalendar();
		issueDate = cal.getTime();
		dueDate = cal.getTime();
		defaultDescription = ""; //$NON-NLS-1$
		defaultReference = ""; //$NON-NLS-1$
		defaultComment = ""; //$NON-NLS-1$
		grossValue = new BigDecimal(0); //$NON-NLS-1$
	}

	public transactions getParent() {
		return parent;
	}

	public void setType(transactionType type) {
		this.type = type;
	}

	abstract public String getTransactionName();

	/**
	 * Number of workflow steps: 0=not booked at all, 1=booked, 2=booked,
	 * afterwards shows up in todo window until balanced
	 */
	abstract public int getNumWorkflowSteps();

	abstract public boolean isVATRequiredInStep(int step);

	// consider changing this to processEntriesForWorkflowStep as it already
	// does some extra work, e.g. assigns defaultValue to the transaction
	abstract public void createEntriesForWorkflowStep(int step);

	abstract public HashMap<Integer, String> getTodoItems();

	public void bindToPage(newTransactionSelectItemDetails parentPage) {
		this.parentPage = parentPage;

		for (item currentItem : items) {
			currentItem.addListener(new pageActivator(parentPage));
		}

	}

	public void addItem(item e) {
		items.add(e);
		itemIndex++;
		e.setIDonPage(itemIndex);
		e.addListener(new pageActivator(parentPage));
	}

	public void removeItem(item e) {
		items.remove(e);
	}

	public Object[] getElements(Object arg0) {
		return items.toArray();
	}

	public void clear() {
		items.clear();
	}

	public item elementAt(int index) {
		return (item) items.get(index);
	}

	public int size() {
		return items.size();
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

	public item getItemForImportID(int itemImportID) {
		for (item currentItem : items) {
			if (currentItem.getID() == itemImportID) {
				return currentItem;
			}
		}

		return null;
	}

	public item getItemForIDonPage(int IDonPage) {
		for (item currentItem : items) {
			if (currentItem.getIDonPage() == IDonPage) {
				return currentItem;
			}
		}

		return null;
	}

	public void removeImportID(int id) {
		items.remove(getItemForImportID(id));
	}

	public void removeIDonPage(int id) {
		items.remove(getItemForIDonPage(id));
	}

	public void setSelectedItemIndex(int index) {
		selectedItemIndex = index;
	}

	
	public void export() {
		client.getTransactions().getCurrentTransaction()
				.saveCurrentTransactionAsOpenTrans();
		try {
			client.getTransactions().getCurrentTransaction()
			.saveCurrentTransactionAsDocTag();
		} catch (COSVisitorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// will only fire if doctag has been enabled in the settings
		
	}

	public void setCurrentDescription(String desc) {
		item currentItem = elementAt(selectedItemIndex);
		currentItem.setRemarks(desc);
	}

	public void setCurrentQuantity(String quantity) {
		item currentItem = elementAt(selectedItemIndex);

		if (quantity.length() > 0) {
			try {
				currentItem.setQuantity(new BigDecimal(quantity));
			} catch (NumberFormatException e) {
				System.err
						.println(Messages.getString("items.invalidcharqty") + quantity + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				// ignore text entered in number field
			}
		}

	}

	public void setCurrentPrice(String price) {
		item currentItem = elementAt(selectedItemIndex);
		BigDecimal bdPrice = new BigDecimal(0);
		if (price.length() > 0) {
			try {
				bdPrice = new BigDecimal(price);
			} catch (NumberFormatException e) {
				System.err
						.println(Messages.getString("items.invalidcharprc") + price + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				// ignore text entered in number field
			}

		}
		currentItem.setPrice(bdPrice);
	}

	public void bindTableViewer(TableViewer tv) {
		tableViewer = tv;
	}

	@Override
	public String toString() {
		return type.getTypeName();
	}

	public BigDecimal getTotal() {
		BigDecimal res = new BigDecimal(0);
		for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
			item currentItem = items.get(itemIndex);
			res = res.add(currentItem.getTotal());
		}

		return res;
	}

	public void book() {

		// createdEntries.clear();
		createEntriesForWorkflowStep(workflowStep);
		if (workflowStep == 2) {
			// workflow step 2 should be payment, mark the document as balanced
			// for historic reasons balanced is called linked
			document d = client.getDocuments()
					.getDocumentForNumber(getNumber());
			if (d != null) {
				d.setIsLinked(true);
				d.save();
			}
		}
		advanceWorkflow();
		save(); // save so that the created entries can refer to an existing ID
		for (entry currentEntry : entries) {
			if (!currentEntry.isBooked()) {
				currentEntry.book();
			}
		}
		save(); // save again:createEntriesForWorkflowStep resulted in new
				// workflow index, default value
		// update cache of entries, potentially open entry window
		client.getEntries().signalChange();
	}

	public void setIssueDate(Date newDate) {
		issueDate = newDate;
		recalcDueDate();
	}

	private void recalcDueDate() {
		Calendar dueDateCal = Calendar.getInstance();
		dueDateCal.setTime(issueDate);
		dueDateCal.add(Calendar.DAY_OF_YEAR, type.getTypePeriod());
		dueDate = dueDateCal.getTime();

	}

	/**
	 * returns the gross value of all items, or, if there are no items, the
	 * grossValue set by setGrossValue, or 0, if that had not been used
	 * (itemless transactions are usually incoming transactions).
	 * */
	public BigDecimal getTotalGross() {
		BigDecimal res = getGrossValue();
		if (res.equals(new BigDecimal(0))) {
			for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
				item currentItem = (item) items.get(itemIndex);
				if (currentItem != null) {
					res = res.add(currentItem.getTotalGross());
				}
			}
		}
		if (configs.shallRoundTo5ct()) {
			BigDecimal basis=new BigDecimal(20);
			res=res.multiply(basis).setScale(0,RoundingMode.HALF_UP).divide(basis).setScale(2,RoundingMode.HALF_UP);
			
		}

		return res;
	}

	/**
	 * if at the time of transaction creation it refers to a particular
	 * transaction E.g.: when a invoice is created from an offer, the ID of the
	 * offer will be stored with this method
	 * 
	 * */
	public void setReferTo(int transactionID) {
		precedingTransactionID = transactionID;

	}

	/**
	 * This is rather a copy than a clone functionality and could be required by
	 * an interface like Icopyable
	 */
	public void cloneFrom(appTransaction sourceTransaction) {
		List<item> sourceItems = sourceTransaction.getItems();
		setTransactionAttributes(sourceTransaction.getUnchangedNumber(),
				sourceTransaction.getFilenameODT(),
				sourceTransaction.getFilenamePDF(),
				sourceTransaction.getContact(),
				sourceTransaction.getIssueDate(),
				sourceTransaction.getDueDate(),
				sourceTransaction.getRefersTo(), sourceTransaction.getRemarks());
		List<item> newSourceItems = new ArrayList<item>(sourceItems);
		for (item currentItem : newSourceItems) {
			item targetItem = addItem();
			targetItem.cloneFrom(currentItem);
		}

	}

	public String getTotalString() {
		NumberFormat form = NumberFormat.getCurrencyInstance();
		return form.format(getTotal());
	}

	public String getTotalGrossString() {
		NumberFormat form = NumberFormat.getCurrencyInstance();
		return form.format(getTotalGross());
	}

	public static int getType() {
		return 0;
	}

	public transactionType getTypeClass() {
		return type;
	}

	// while (static)getType might return 0 in the appTransaction context of
	// e.g. an invoice, getTypeID will always return the correct result...
	public int getTypeID() {
		return type.getTypeID();
	}

	/**
	 * get next number, but don't allocate it
	 * 
	 * */
	public int getNextNumber() {
		int numericNumber = ((Integer) DB
				.getEntityManager()
				.createQuery(
						"select c.typeIndex from transactionType c where c.typeID=" + getTypeID()).getSingleResult()).intValue(); //$NON-NLS-1$
		return numericNumber + 1;

	}

	public String getNextNumberString() {
		return applyFormatToNumericNumber(getNextNumber(), type.getTypeFormat());
	}

	/**
	 * get next number into this transaction and allocate it as being used
	 * 
	 * */
	public void getNewTransactionNumber() {

		DB.getEntityManager().getTransaction().begin();
		// use getTypeID() here not just getType() because getType can't be
		// abstract, thus is implemented
		// incorrectly for the base class in which context this is executed

		type.setTypeIndex(getNextNumber());
		setNumber(applyFormatToNumericNumber(getNextNumber(),
				type.getTypeFormat()));

		String filenameRelative = client.getClientPath()
				+ filterForFilenameCharacters(getNumber());
		DB.getEntityManager().getTransaction().commit();
		type.save();// saving the transaction type (new number) should ideally
					// happen within the hibernate transaction

		filenameRelativeODT = filenameRelative + ".odt"; //$NON-NLS-1$
		filenameRelativePDF = filenameRelative + ".pdf"; //$NON-NLS-1$
		filenameRelativeOT = filenameRelative + ".xml"; //$NON-NLS-1$

		filenameODT = client.getConfigPath() + filenameRelativeODT;
		filenamePDF = client.getConfigPath() + filenameRelativePDF;
		filenameOT = client.getConfigPath() + filenameRelativeOT;

	}

	private String filterForFilenameCharacters(String number) {
		Pattern p = Pattern.compile("[^A-Za-z0-9_-]");// replace all characters //$NON-NLS-1$
														// except the ones save
														// to use as filename,
														// e.g. don't get /
		Matcher m = p.matcher(number);
		boolean result = m.find();
		// Loop through and create a new String
		// with the replacements
		StringBuffer sb = new StringBuffer(""); //$NON-NLS-1$
		while (result) {
			m.appendReplacement(sb, "_"); // replace invalid characters with //$NON-NLS-1$
											// underscore
			result = m.find();
		}
		// Add the last segment of input to
		// the new String
		m.appendTail(sb);

		return sb.toString();
	}

	private String getTwoDigitNumStrFromInt(int number) {
		DecimalFormat formatter = new DecimalFormat("00"); //$NON-NLS-1$
		return formatter.format(number);
	}

	private String applyFormatToNumericNumber(int numeric,
			String formatTagContent) {

		String format = ""; //$NON-NLS-1$
		String res = formatTagContent;
		// the user is not required to enter fully qualified xml, but the parser
		// of course requires a top element.
		// appand <format>...</format> as dummy top element
		String formatTag = "<format>" + formatTagContent + "</format>"; //$NON-NLS-1$ //$NON-NLS-2$

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			ByteArrayInputStream bais = new ByteArrayInputStream(
					formatTag.getBytes());
			Document document = builder.parse(bais);
			// get list of nodes for tag number:value (per se, the tag is value,
			// but java has a strage kind to handle namespaces...)
			NodeList ndList = document.getElementsByTagName("number:value"); //$NON-NLS-1$
			for (int i = 0; i < ndList.getLength(); i++) {
				Node n = ndList.item(i);
				if (n.hasAttributes())
				// if there is a attribute in the tag number:value
				{
					NamedNodeMap nmm = n.getAttributes();
					if (nmm.getNamedItem("format") != null) //$NON-NLS-1$
					// if there is a attribute "format" in the tag number:value
					{
						format = nmm.getNamedItem("format").getNodeValue(); //$NON-NLS-1$
						DecimalFormat formatter = new DecimalFormat(format);
						res = res.replaceAll("<number:value.*/>", //$NON-NLS-1$
								formatter.format(numeric));
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		Calendar c = Calendar.getInstance();
		c.setTime(getIssueDate());
		res = res.replaceAll("<number:value/>", Integer.toString(numeric)); //$NON-NLS-1$

		res = res.replaceAll("<number:year/>", //$NON-NLS-1$
				Integer.toString(c.get(Calendar.YEAR)));
		res = res.replaceAll("<number:month/>", //$NON-NLS-1$
				getTwoDigitNumStrFromInt(c.get(Calendar.MONTH) + 1));
		res = res.replaceAll("<number:dayofmonth/>", //$NON-NLS-1$
				getTwoDigitNumStrFromInt(c.get(Calendar.DAY_OF_MONTH)));
		if (formatTagContent == "") { //$NON-NLS-1$
			res = Integer.toString(numeric);
		}
		return res;
	}

	public String getVatTotal() {
		NumberFormat form = NumberFormat.getCurrencyInstance();
		return form.format(getTotalGross().subtract(getTotal()));
	}

	public String getVatList() {
		String res = ""; //$NON-NLS-1$
		BigDecimal totalTaxes=new BigDecimal(0);
		prepareVATamounts();
		NumberFormat form = NumberFormat.getCurrencyInstance();
		for (tax currentTax : client.getTaxes().getVATArray()) {
			BigDecimal net = getNetAmountForTax(currentTax);
			BigDecimal amount = getVATAmountForTax(currentTax);

			if (amount != null) {
				res=res+String.format(Messages.getString("appTransaction.taxListLine"),currentTax.getDescription(),form.format(net)+form.format(amount)) +"\n"; //$NON-NLS-1$ //$NON-NLS-2$
				totalTaxes=totalTaxes.add(amount);
			}
		}
		res=res+String.format(Messages.getString("appTransaction.taxListSummary"),form.format(totalTaxes))+"\n"; //$NON-NLS-1$ //$NON-NLS-2$
		
		return res;
	}

	/**
	 * this stores the items in the database and creates the open business data
	 * exchange file transaction-prefix-number.obdx in the client path
	 * 
	 * @throws InvalidArgumentException
	 * */
	public int save() {

		if (transactionID == -1) {
			client.getTransactions().add(this);

		}

		DB.getEntityManager().getTransaction().begin();
		DB.getEntityManager().persist(this);
		DB.getEntityManager().getTransaction().commit();
		// DB.getEntityManager().refresh(this);
		for (item currentItem : items) {
			// currentItem.setParent(this);
			currentItem.save();
		}
		return transactionID;
	}

	public void saveCurrentTransactionAsOpenTrans() {
		FileWriter fw = null;

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
			Calendar cal = new GregorianCalendar();

			Date lastChangedDate = cal.getTime();
			fw = new FileWriter(client.getTransactions()
					.getCurrentTransaction().getFilenameOT());
			fw.write("<?xml version='1.0' encoding='UTF-8'?>\n" + //$NON-NLS-1$
					"<!-- This is a opentrans file (http://www.opentrans.org) generated by gnuaccounting (http://www.gnuaccounting.org).  -->\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"<INVOICE version=\"2.1\" xmlns=\"http://www.opentrans.org/XMLSchema/2.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opentrans.org/XMLSchema/2.1 opentrans_2_1.xsd\" xmlns:bmecat=\"http://www.bmecat.org/bmecat/2005\" xmlns:xmime=\"http://www.w3.org/2005/05/xmlmime\">\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"<INVOICE_HEADER>\n" + //$NON-NLS-1$
					"\t<CONTROL_INFO>\n" + //$NON-NLS-1$
					"\t\t<GENERATION_DATE>" //$NON-NLS-1$
					+ sdf.format(new java.util.Date())
					+ "T00:00:00+01:00</GENERATION_DATE>\n" + //$NON-NLS-1$ //$NON-NLS-2$
					"\t</CONTROL_INFO>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t<INVOICE_INFO>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t\t<INVOICE_ID>" //$NON-NLS-1$
					+ number + "</INVOICE_ID>\n" + //$NON-NLS-1$ //$NON-NLS-2$
					"\t\t<INVOICE_DATE>" //$NON-NLS-1$
					+ getIssueDate() + "T00:00:00+01:00</INVOICE_DATE>\n" + //$NON-NLS-1$ //$NON-NLS-2$
					"\t\t<INVOICE_COVERAGE>single</INVOICE_COVERAGE>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t\t<PARTIES>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t\t\t<PARTY>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t\t\t\t<bmecat:PARTY_ID type=\"supplier_specific\">-1</bmecat:PARTY_ID>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t\t\t\t<PARTY_ROLE>document_creator</PARTY_ROLE>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t\t\t\t<ADDRESS>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t\t\t\t\t<bmecat:NAME>" //$NON-NLS-1$
					+ configs.getOrganisationName() + "</bmecat:NAME>\n" + //$NON-NLS-1$ //$NON-NLS-2$
					"\t\t\t\t\t<bmecat:EMAIL>" //$NON-NLS-1$
					+ configs.getSenderEmail() + "</bmecat:EMAIL>\n" + //$NON-NLS-1$ //$NON-NLS-2$
					"\t\t\t\t</ADDRESS>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t\t\t\t<ACCOUNT>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t\t\t\t\t<HOLDER>" //$NON-NLS-1$
					+ configs.getHolderName() + "</HOLDER>\n" + //$NON-NLS-1$ //$NON-NLS-2$
					"\t\t\t\t\t<BANK_ACCOUNT type=\"standard\">" //$NON-NLS-1$
					+ configs.getAccountCode() + "</BANK_ACCOUNT>\n" + //$NON-NLS-1$ //$NON-NLS-2$
					"\t\t\t\t\t<BANK_CODE type=\"blz\">" //$NON-NLS-1$
					+ configs.getBankCode() + "</BANK_CODE>\n" + //$NON-NLS-1$ //$NON-NLS-2$
					"\t\t\t\t\t<BANK_NAME>" //$NON-NLS-1$
					+ configs.getBankName() + "</BANK_NAME>\n" + //$NON-NLS-1$ //$NON-NLS-2$
					"\t\t\t\t\t<BANK_COUNTRY>DE</BANK_COUNTRY>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t\t\t\t</ACCOUNT>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t\t\t</PARTY>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t\t\t<PARTY>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t\t\t\t<bmecat:PARTY_ID type=\"supplier_specific\">" //$NON-NLS-1$
					+ recipient.getID() + "</bmecat:PARTY_ID>\n" + //$NON-NLS-1$ //$NON-NLS-2$
					"\t\t\t\t<PARTY_ROLE>buyer</PARTY_ROLE>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t\t\t\t<ADDRESS>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t\t\t\t\t<bmecat:NAME>" //$NON-NLS-1$
					+ recipient.getName() + "</bmecat:NAME>\n" + //$NON-NLS-1$ //$NON-NLS-2$
					"\t\t\t\t</ADDRESS>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t\t\t</PARTY>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t\t</PARTIES>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t\t<INVOICE_ISSUER_IDREF>p:INVOICE_ISSUER_IDREF</INVOICE_ISSUER_IDREF>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t\t<INVOICE_RECIPIENT_IDREF>" //$NON-NLS-1$
					+ recipient.getID() + "</INVOICE_RECIPIENT_IDREF>\n" + //$NON-NLS-1$ //$NON-NLS-2$
					"\t\t<bmecat:CURRENCY>EUR</bmecat:CURRENCY>\n" + //$NON-NLS-1$
					"\t</INVOICE_INFO>\n" + //$NON-NLS-1$
					"	" + //$NON-NLS-1$
					"</INVOICE_HEADER>\n" + //$NON-NLS-1$
					"\t<INVOICE_ITEM_LIST>"); //$NON-NLS-1$

			for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
				item currentItem = (item) items.get(itemIndex);
				fw.write(currentItem.getOTString(itemIndex + 1) + "\n"); //$NON-NLS-1$

			}
			fw.write("\t</INVOICE_ITEM_LIST>\n" + //$NON-NLS-1$
					"\t<INVOICE_SUMMARY>\n" //$NON-NLS-1$
					+ //$NON-NLS-1$
					"\t\t<TOTAL_ITEM_NUM>" //$NON-NLS-1$
					+ items.size() + "</TOTAL_ITEM_NUM>\n" + //$NON-NLS-1$ //$NON-NLS-2$
					"\t\t<NET_VALUE_GOODS>" + getTotal() //$NON-NLS-1$
					+ "</NET_VALUE_GOODS>\n" + //$NON-NLS-1$ //$NON-NLS-2$
					"\t\t<TOTAL_AMOUNT>" + getTotalGross() //$NON-NLS-1$
					+ "</TOTAL_AMOUNT>\n" + //$NON-NLS-1$ //$NON-NLS-2$
					"\t\t<TOTAL_TAX>\n" + //$NON-NLS-1$
					"\t\t\t<TAX_DETAILS_FIX/>\n" + //$NON-NLS-1$
					"\t\t</TOTAL_TAX>\n" + //$NON-NLS-1$
					"\t</INVOICE_SUMMARY>\n" + //$NON-NLS-1$
					"</INVOICE>"); //$NON-NLS-1$
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getDocTag() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
		Calendar cal = new GregorianCalendar();

		Date lastChangedDate = cal.getTime();
		StringWriter sw = new StringWriter();
		sw.write("{" + //$NON-NLS-1$

				"\"doctag_version\": \"0.9\"," + " \"doctype\": \"invoice\"," //$NON-NLS-1$ //$NON-NLS-2$
				+ "\"id\": \"org.gnuaccounting." //$NON-NLS-1$
				+ Math.round(Math.random() * Double.MAX_VALUE)/*
															 * GUID, should be
															 * difficult to have
															 * 2 times the same
															 * ID
															 */
				+ "\"," //$NON-NLS-1$
				+ "\"number\": \"" //$NON-NLS-1$
				+ number + "\"," //$NON-NLS-1$
				+ "\"date\": \"" //$NON-NLS-1$
				+ sdf.format(getIssueDate()) + "\"," //$NON-NLS-1$
				+ "\"delivery_date\": \"" //$NON-NLS-1$
				+ sdf.format(getIssueDate()) + "\"," //$NON-NLS-1$
				+ "\"due_date\": \"" //$NON-NLS-1$
				+ sdf.format(getDueDate()) + "\"," //$NON-NLS-1$
				+ "\"total\": {" //$NON-NLS-1$
				+ "        \"net\": " //$NON-NLS-1$
				+ utils.currencyFormat(getTotal(), '.') + "," //$NON-NLS-1$
				+ "\"gross\": " //$NON-NLS-1$
				+ utils.currencyFormat(getTotalGross(), '.') + "" //$NON-NLS-1$
				+ "}," //$NON-NLS-1$
				+ "\"currency\": \"EUR\"," + "\"items\": ["); //$NON-NLS-1$ //$NON-NLS-2$
		for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
			item currentItem = (item) items.get(itemIndex);
			String delimiter = ","; //$NON-NLS-1$
			if (itemIndex == items.size() - 1) {
				delimiter = ""; //$NON-NLS-1$
			}
			sw.write(currentItem.getDocTagString(itemIndex + 1) + delimiter
					+ "\n"); //$NON-NLS-1$

		}
		sw.write("\t]," + "\"taxes\": ["); //$NON-NLS-1$ //$NON-NLS-2$

		String taxes = ""; //$NON-NLS-1$
		prepareVATamounts();
		for (tax currentTax : client.getTaxes().getVATArray()) {
			BigDecimal amount = getVATAmountForTax(currentTax);
			if (amount != null) {

				taxes += "{" + "\"name\": \"MwSt\"," + "\"rate\": " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ utils.round(
								currentTax.getValue().multiply(
										new BigDecimal(100)), 1)
						+ "," + "\"amount\": " //$NON-NLS-1$ //$NON-NLS-2$
						+ utils.currencyFormat(amount, '.') + "" + "},"; //$NON-NLS-1$ //$NON-NLS-2$

			}
		}
		if (taxes.length() > 0) {
			taxes = taxes.substring(0, taxes.length() - 1);
		}

		sw.write(taxes + "    ]" + "}"); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			sw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sw.toString();
	}
	
	

	public void saveCurrentTransactionAsDocTag() throws COSVisitorException {

		try {

			PDDocument doc = PDDocument.load(client.getTransactions()
					.getCurrentTransaction().getFilenamePDF());

			PDDocumentInformation info = doc.getDocumentInformation();
			/*
			 * info.setTitle(getTypeName());
			 * 
			 * info.setCreationDate(Calendar.getInstance());
			 * info.setCreator(configs.getOrganisationName());
			 * info.setSubject(getNumber());
			 */

			/* add Doctag (www.doctag.org) only if specifically configured:
			   Zugferd does not harm but DocTag as of the current version 0.6 (2014-04-14) 
			   will cause errors in the Metadata
			 */
			if (configs.shallDocTag()) {
				/***/
				
 				PDDocumentCatalog catalog = doc.getDocumentCatalog();

				String doctagXMPAttach = "<doctag>" + getDocTag() + "<doctag>"; //$NON-NLS-1$ //$NON-NLS-2$

				// convert String into InputStream

				PDMetadata oldMetadata = catalog.getMetadata();// new
																// PDMetadata(doc,
																// newXMPData,
																// false );

				byte[] oldContentBytes = oldMetadata.getByteArray();
				String oldXMPData = new String(oldContentBytes, "UTF-8"); //$NON-NLS-1$
				String newCompleteXMP = oldXMPData + doctagXMPAttach;

				InputStream newXMPData = new ByteArrayInputStream(
						newCompleteXMP.getBytes());

				PDMetadata newMetadata = new PDMetadata(doc, newXMPData, false);
				catalog.setMetadata(newMetadata);
				doc.save(client.getTransactions().getCurrentTransaction()
						.getFilenamePDF());

			}



		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getID() {
		return transactionID;
	}

	public List<item> getItems() {
		return items;
	}

	public IZUGFeRDExportableItem[] getZFItems() {
		IZUGFeRDExportableItem[] result = new IZUGFeRDExportableItem[items.size()];
		int itemIndex=0;
		for (item currentItem : items) {
			result[itemIndex]=currentItem;
			itemIndex++;
		}
		return result;
	}

	private void setTransactionAttributes(String number, String filenameODT,
			String filenamePDF, contact theCustomer, Date issueDate,
			Date dueDate, int refersTo, String remarks) {
		// this.transactionID = insertedTransactionID;
		this.number = number;
		this.filenameODT = filenameODT;
		this.filenamePDF = filenamePDF;

		recipient = theCustomer;
		this.issueDate = issueDate;
		this.dueDate = dueDate;
		this.precedingTransactionID = refersTo;
		this.remarks = remarks;
	}

	public item addItem() {
		item i = new item();
		i.setParent(this);
		i.prepare();
		addItem(i);
		return i;

	}

	public void setContact(contact c) {
		recipient = c;
	}

	public contact getContact() {
		return recipient;
	}

	public Date getIssueDate() {
		return issueDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public int getRefersTo() {
		return precedingTransactionID;
	}

	public void setDirty(boolean isDirty) {
		dirty = isDirty;
	}

	public boolean isDirty() {
		return dirty;
	}

	/**
	 * returns a transaction number according to the format for the transaction
	 * type and creates one if not yet assigned (the new number is stored to the
	 * db)
	 * */
	public String getNumber() {
		if (defaultReference.length() > 0) {
			return defaultReference;
		}

		if (number == null) {
			getNewTransactionNumber();
		}
		return number;
	}

	private void setNumber(String number) {
		this.number = number;
	}

	/**
	 * returns the number attribute -- null if no number has yet been assigned,
	 * otherwise a new number according to the format
	 * */
	public String getUnchangedNumber() {
		return number;
	}

	public String getFilenameODT() {
		return filenameODT;
	}

	public String getFilenamePDF() {
		return filenamePDF;
	}

	public String getRelativeFilenameODT() {
		return filenameRelativeODT;
	}

	public String getRelativeFilenamePDF() {
		return filenameRelativePDF;
	}

	public String getFilenamePDFGPG() {
		if (configs.getGPGPath().length() > 0) {
			// assume there is a gpg file
			return filenamePDF + ".gpg"; //$NON-NLS-1$
		}
		return null;
	}

	public String getFilenameOT() {
		return filenameOT;
	}

	public String getRemarks() {
		if (remarks == null) {
			return ""; //$NON-NLS-1$
		} else {
			return remarks;
		}
	}

	public int getPrecedingTransactionID() {
		return precedingTransactionID;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getTypeName() {
		return type.getTypeName();
	}

	public String getTypePrefix() {
		return type.getTypePrefix();
	}

	public void saveType() {
		type.save();
		parent.signalChange(this);

	}

	public String getBankAccount() {
		return getContact().getBankAccount();
	}

	public String getBankCode() {
		return getContact().getBankCode();
	}

	public String getBankAccountHolder() {
		return getContact().getAccountholder();
	}

	public String getPaymentPurpose() {
		return paymentPurpose;
	}

	public BigDecimal getPaymentAmount() {
		return getTotalGross();
	}

	public void setPaymentPurpose(String s) {
		paymentPurpose = s;

	}

	public int getWorkflowStep() {
		return workflowStep;
	}

	public void setWorkflowStep(int workflowStep) {
		this.workflowStep = workflowStep;
	}

	public List<entry> getEntriesForCurrentWorkflowStep() {
		entries.clear();
		createEntriesForWorkflowStep(workflowStep);
		return entries;
	}

	public void advanceWorkflow() {
		workflowStep++;
		save();
	}

	public void setImportID(int import_id) {
		import_item_id = import_id;
	}

	public int getImportID() {
		return import_item_id;
	}

	public void setVAT(tax VAT) {
		this.VAT = VAT;
	}

	/**
	 * this function returns a single tax if the entry is booked in a table with
	 * a combo box of VATs.
	 * 
	 * For more details and more reliable information please iterate through
	 * every VAT and refer to getVATAmountForTax. If you would like to include
	 * the information for tabular single-VAT-rate selections please run
	 * getVATAmountForTax after addVATAmountFromQuickSelection
	 * */
	public tax getVAT() {
		return VAT;
	}

	/**
	 * this function adds a value to a potentially already existing tax rate for
	 * this entry
	 * */
	protected void addVATAmount(tax VAT, BigDecimal amount) {
		BigDecimal existingValue = VATAmounts.get(VAT.getID());
		if (existingValue == null) {
			existingValue = new BigDecimal(0);
		}
		existingValue = existingValue.add(amount);
		VATAmounts.put(VAT.getID(), existingValue);

	}

	/**
	 * this function replaces a potentially already existing tax rate for this
	 * entry
	 * *
	protected void setVATAmount(tax VAT, BigDecimal amount) {
		VATAmounts.put(VAT.getID(), amount);
	}*/

	/**
	 * Returns the amount of money for this entry and the given tax t. Returns
	 * null if no amount specified for this tax.
	 * 
	 * use addVATAmountFromQuickSelection before to ensure combo-box-selected
	 * single VAT rates are taken into account.
	 * */
	public BigDecimal getVATAmountForTax(tax t) {
		BigDecimal net=VATAmounts.get(t.getID());
		if (net==null) {
			return null;
		}
		BigDecimal gross=utils.round(net.multiply(t.getFactor()),2);
		return gross.subtract(net);
	}
	public BigDecimal getNetAmountForTax(tax t) {
		return VATAmounts.get(t.getID());
	}

	/**
	 * this function translates a single tax rate (e.g. via a 0/7/19% selection combo in
	 * bank statement import) to the more detailed and more reliable
	 * representation for multiple VAT rates via addVATAmount/setVATAmount and
	 * getVATAmountForTax
	 * */
	protected void addVATAmountFromQuickSelection() {
		if ((getVAT() != null)
				&& (getVAT().getIDinList() != client.getTaxes().getFirst()
						.getIDinList()) && (precedingTransactionID == -1)) {
			/*
			 * dont do a split booking if a refer to transaction number is set,
			 * because then the VAT has already been booked on creation.
			 */

			BigDecimal vatAmount = //calculate the net
					getGrossValue().divide(getVAT().getFactor(), 2,
							RoundingMode.HALF_UP);

			if (vatAmount.compareTo(new BigDecimal(0)) != 0) {
				vatAmount = vatAmount.setScale(2, BigDecimal.ROUND_HALF_UP); // first,
																				// round
																				// so
																				// that
																				// e.g.
																				// 1.189999999999999946709294817992486059665679931640625
																				// becomes
																				// 1.19

				addVATAmount(getVAT(), vatAmount);
			}
		}
		// prevent from being called again, e.g. this function is called in
		// entryDetailWindow
		// and later in entry.book
		// setVAT(allVATs.getDefault());

	}

	/**
	 * This will update the VAT amounts according to the items in a self-created
	 * or loaded transaction
	 * */
	protected void addVATAmountFromTransactionItems() {

		for (item currentItem : getItems()) {

			addVATAmount(currentItem.getProduct().getVAT(), currentItem.getTotal());
			if (configs.hasSalesTax()&&!isTaxExempt()) {
				addVATAmount(currentItem.getProduct().getSalesTax(), currentItem.getTotal());
			}
		}

	}
	
	public boolean isTaxExempt() {
		return isTaxExempt;
	}

	public void setIsTaxExempt(boolean b) {
		isTaxExempt=b;
		
	}


	public void setUserDetailVAT(tax VAT, BigDecimal amount) {
		VATAmountsFromDetailUserSelection.put(VAT, amount);
	}
	/***
	 * If a transaction is opened with a tabular VAT view like the "more" stack in
	 * bank statement import, it will be communicated via setUserDetailVAT and this function will add the according entries
	 * 
	 * addVATAmountFromDetailUserSelection will, if any are specified, override any previous VAT selection 
	 * (and is supposed to override quick selection) 
	 */
	protected void addVATAmountFromDetailUserSelection() {

		if (VATAmountsFromDetailUserSelection.size()>0) {
			VATAmounts.clear();
		}
		for (tax VAT : VATAmountsFromDetailUserSelection.keySet()) {
			/* in the detail user selection (table) we have the amounts of the VAT but for 
			 * addVATAmounts we need the net amount this VAT amount is applicable to,
			 * i.e. applicableAmount*(Factor-1)=VATAmount -->
			 * we have to divide by (Factor-1) 
			 */
			addVATAmount(VAT, VATAmountsFromDetailUserSelection.get(VAT).divide(VAT.getFactor().subtract(new BigDecimal(1)), 2, RoundingMode.HALF_UP));
		}

	}

	/***
	 * returns true if debit or credit account is not set (=null), or set to "new account"
	 * @return
	 */
	public boolean hasMissingAccounts() {
		return getDefaultDebitAccount()==null||getDefaultDebitAccount().isEmpty()
				|| getDefaultCreditAccount()==null || getDefaultCreditAccount().isEmpty();
	}

	public void prepareVATamounts() {
		VATAmounts.clear();

		addVATAmountFromQuickSelection();
		addVATAmountFromDetailUserSelection();
		addVATAmountFromTransactionItems();

	}

	public void setDefaultDescription(String description) {
		defaultDescription = description;
	}

	public void setDefaultCreditAccount(account creditAccount) {
		defaultCreditAccount = creditAccount;
	}

	public void setDefaultDebitAccount(account debitAccount) {
		defaultDebitAccount = debitAccount;
	}

	public void setDefaultReference(String res) {
		defaultReference = res;
	}

	public void setDefaultComment(String res) {
		defaultComment = res;
	}

	public void setGrossValue(BigDecimal abs) {
		grossValue = abs;

	}

	public String getDefaultReference() {
		return defaultReference;
	}

	public String getDefaultComment() {
		return defaultComment;
	}

	public account getDefaultDebitAccount() {
		return defaultDebitAccount;
	}

	public account getDefaultCreditAccount() {
		return defaultCreditAccount;
	}

	public BigDecimal getGrossValue() {
		return grossValue;
	}

	public String getDefaultDescription() {
		return defaultDescription;
	}

	public String getColumnString(int arg1) {
		switch (arg1) {
		case 0:
			SimpleDateFormat sdf = new SimpleDateFormat(
					Messages.getString("appTransaction.dateFormat")); //$NON-NLS-1$
			return sdf.format(getIssueDate());
		case 1:
			return getDefaultDescription();
		case 2:
			return getGrossValue().toString();
		case 3:
			if (getDefaultDebitAccount() == null) {
				return ""; //$NON-NLS-1$
			}
			return getDefaultDebitAccount().getAsString();
		case 4:
			if (getDefaultCreditAccount() == null) {
				return ""; //$NON-NLS-1$
			}
			return getDefaultCreditAccount().getAsString();
		case 5:
			return getVAT().getDescription();
		case 6:
			return getDefaultReference();
		case 7:
			return getDefaultComment();
		case 8:
			if (getContact()==null) {
				return ""; //$NON-NLS-1$
			} else {
				return getContact().getName();				
			}

		}
		return null;
	}

	public Object getColumnObject(int arg1) {
		switch (arg1) {
		case 0:
			return getIssueDate();
		case 1:
			return getDefaultDescription();
		case 2:
			return getGrossValue().toString();// as we're using a
												// textcelleditor we shall not
												// return the bigint here
		case 3:
			return getDefaultDebitAccount();
		case 4:
			return getDefaultCreditAccount();
		case 5:
			return getVAT().getIDinList();
		case 6:
			return getDefaultReference();
		case 7:
			return getDefaultComment();

		}
		return null;
	}

	public void setColumnObject(String column, Object value) {
		int idx = -1;
		try {
			idx = utils
					.findIndexOfStringInStringArray(getColumnNames(), column);
		} catch (elementNotFoundException e) {
			e.printStackTrace();
		}
		switch (idx) {
		case 0:
			setIssueDate((Date) value);
			break;
		case 1:
			setDefaultDescription((String) value);
			break;
		case 2:
			setGrossValue(new BigDecimal(((String) value).replace(",", ".")));break; //$NON-NLS-1$ //$NON-NLS-2$
		case 3:
			setDefaultDebitAccount((account) value);
			break;
		case 4:
			setDefaultCreditAccount((account) value);
			break;
		case 5:
			try {
				setVAT(client.getTaxes().getVATAtListIndex((Integer) value));
			} catch (taxNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case 6:
			setDefaultReference((String) value);
			break;
		case 7:
			setDefaultComment((String) value);
			break;
		case 8:
			setContact((contact) value);
			break;
		}

	}

	public Object getColumnObject(String colName) {
		int idx;
		try {
			idx = utils.findIndexOfStringInStringArray(getColumnNames(),
					colName);
		} catch (elementNotFoundException e) {
			return null;
		}
		return getColumnObject(idx);
	}

	public static String[] getColumnNames() {

		String[] res = {
				Messages.getString("appTransaction.dateColHeader"), Messages.getString("appTransaction.DescriptionColHeader"), Messages.getString("appTransaction.valueColHeader"), Messages.getString("appTransaction.DebitColHeader"), Messages.getString("appTransaction.CreditColHeader"), Messages.getString("appTransaction.VATColHeader"), Messages.getString("appTransaction.ReferenceColHeader"), Messages.getString("appTransaction.CommentColHeader"), Messages.getString("appTransaction.ContactColHeader") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
		return res;
	}

	public void setCashflow(CashFlow direction) {
		cashFlowDirection = direction;

	}

	public static CashFlow estimateCashFlow(account debit, account credit) {
		// try {
		// income credit account isIncomeAccount()
		// expense debit account isExpenseAccount()
		/*
		 * wenn aktivkonto im soll oder passivkonto im haben= Einnahme
		 */
		/*
		 * if ((debit.getID() == client.getAccounts().getBankAccount().getID())
		 * || (debit.getID() == client.getAccounts()
		 * .getReceivablesAccount().getID())
		 */
		/*if cash is on debit it's relatively clear in which direction the cashflow goes*/
		if ((debit != null && (debit.isBankAccount()||debit.isCashAccount()))) {
			return CashFlow.RECEIVING;
		}
		/*as well if it's on credit*/
		if ((credit != null && (credit.isBankAccount()||credit.isCashAccount()))) {
			return CashFlow.SENDING;
		}
		/****
		 * and here the guessing starts: this could be an income
		 */
		if ((debit != null && debit.isAssetAccount())
				|| (credit != null && credit.isLiabilityAccount())) {
			return CashFlow.RECEIVING;
		} else if ((debit != null && debit.isLiabilityAccount())
				|| (credit != null && credit.isAssetAccount())) {
			return CashFlow.SENDING;
		}
		/*
		 * } catch (AccountNotFoundException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); }
		 */
		return CashFlow.UNDEFINED;

	}

	public void estimateCashFlow() {
		setCashflow(estimateCashFlow(getDefaultDebitAccount(),
				getDefaultCreditAccount()));
	}

	public CashFlow getCashFlow() {
		if (cashFlowDirection == CashFlow.UNDEFINED) {
			estimateCashFlow();
		}
		return cashFlowDirection;
	}

	/**
	 * after having imported the items into the accounts, we should delete them
	 * from the import queue
	 * 
	 * @param isBooked
	 *            =false means delete the entry from the import list
	 * @seeAlso delete deletes the already imported entry
	 * */
	public void removeFromImport(boolean isBooked) {
		if (precedingTransactionID != -1) {
			appTransaction ref = client.getTransactions().getByID(
					precedingTransactionID);
			document doc = client.getDocuments().getDocumentForNumber(
					ref.getNumber());

			if (doc != null) {
				doc.setIsLinked(true);
				doc.save();
			}
		}
		if (isBooked) {
			transactionFromBankAccountImport importEntry = client
					.getImportQueue().getForID(getImportID());
			if (importEntry != null) {
				importEntry.setImported();
				importEntry.save();
			}
		}
		client.getImportQueue().deleteAndRemove(getImportID());

	}

	public contact getRecipient() {
		return recipient;
	}

	public List<entry> getEntries() {
		return entries;
	}

	public List<entry> loadEntriesFromTransID(int transID) {
		if (transID == -1) {
			// somebody writes a cancellation as a new transaction, not
			// referring to the original
			// this should not happen at all
			return new ArrayList<entry>();
		}
		return new ArrayList<entry>(client.getTransactions().getByID(transID)
				.getEntries());

	}

	public void setDocument(document d) {
		transDocument = d;
		d.setTransaction(this);
	}

	/**
	 * currently only works if document has been set before, i.e. does not
	 * persist(?)
	 * */
	public document getDocument() {
		return transDocument;
	}

	public void setBalanced(boolean balanced) {
		isBalanced = balanced;

	}

	protected boolean isBalanced() {
		return isBalanced;

	}

	public GraphicInfo getBezahlcodeQRCode() {
		GraphicInfo graphicInfo = null;
		if ((configs.getIBAN() != null)
				&& (configs.getIBAN().length() != 0)
				&& (configs.getBIC() != null)
				&& (configs.getBIC().length() != 0)
				&& (configs.getHolderName() != null)
				&& (configs.getHolderName().length() != 0)) {
			File tempFile = null;

			QRCodeWriter writer = new QRCodeWriter();
			BitMatrix bitMatrix = null;

			int qrCodeWidth = 100;
			int qrCodeHeight = 100;

			try {
				tempFile = File.createTempFile("QRcode", "bezahlcode"); //$NON-NLS-1$ //$NON-NLS-2$

				// could be supported in the future: bezahlcode
				// executiondate=ddmmyyyy
				bitMatrix = writer.encode(
						"bank://singlepaymentsepa?name=" //$NON-NLS-1$
								+ URLEncoder.encode(configs.getHolderName(),
										"UTF-8") //$NON-NLS-1$
								+ "&iban=" //$NON-NLS-1$
								+ configs.getIBAN()
								+ "&bic=" //$NON-NLS-1$
								+ configs.getBIC()
								+ "&amount=" //$NON-NLS-1$
								+ URLEncoder.encode(utils.currencyFormat(
										getTotalGross(), ','), "UTF-8") //$NON-NLS-1$
								+ "&reason=" //$NON-NLS-1$
								+ URLEncoder.encode(getNumber(), "UTF-8"), //$NON-NLS-1$
						BarcodeFormat.QR_CODE, qrCodeWidth * 3,
						qrCodeHeight * 3);
				MatrixToImageWriter.writeToFile(bitMatrix, "gif", tempFile); //$NON-NLS-1$
			} catch (WriterException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			int pixelWidth = qrCodeWidth;
			int pixelHeight = qrCodeHeight;

			// with stream
			try {
				graphicInfo = new GraphicInfo(new FileInputStream(tempFile),
						pixelWidth, true, pixelHeight, true,
						VertOrientation.TOP, HoriOrientation.LEFT,
						TextContentAnchorType.AT_PARAGRAPH);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NOAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return graphicInfo;
	}

	public GraphicInfo getDocTagQRCode() {
		GraphicInfo graphicInfo = null;
		File tempFile = null;

		QRCodeWriter writer = new QRCodeWriter();
		BitMatrix bitMatrix = null;

		int qrCodeWidth = 300;
		int qrCodeHeight = 300;

		try {
			tempFile = File.createTempFile("QRcode", "doctag"); //$NON-NLS-1$ //$NON-NLS-2$

			// could be supported in the future: bezahlcode
			// executiondate=ddmmyyyy
			bitMatrix = writer.encode(getDocTag(), BarcodeFormat.QR_CODE,
					qrCodeWidth * 3, qrCodeHeight * 3);
			MatrixToImageWriter.writeToFile(bitMatrix, "gif", tempFile); //$NON-NLS-1$
		} catch (WriterException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int pixelWidth = qrCodeWidth;
		int pixelHeight = qrCodeHeight;

		// with stream
		try {
			graphicInfo = new GraphicInfo(new FileInputStream(tempFile),
					pixelWidth, true, pixelHeight, true, VertOrientation.TOP,
					HoriOrientation.LEFT, TextContentAnchorType.AT_PARAGRAPH);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NOAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return graphicInfo;
	}

	
	/**
	 * set date of delivery or start of performed works
	 * */
	public void setPerformanceStart(Date time) {
		this.performanceStart=time;
	}

	/**
	 * set date of delivery or end of performed works
	 * */
	public void setPerformanceEnd(Date time) {
		this.performanceEnd=time;
	}
	
	public Date getPerformanceStart() {
		return performanceStart;
	}

	public Date getPerformanceEnd() {
		return performanceEnd;
	}



}
