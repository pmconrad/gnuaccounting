/* Interface to import order from a web shop using OBDX */

package dataLayer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import GUILayer.newTransactionWizard;
import appLayer.client;
import appLayer.configs;
import appLayer.item;
import appLayer.transactionRelated.appTransaction;
import appLayer.transactionRelated.invoice;
import appLayer.transactionRelated.offer;

public class webshopImporter extends Thread implements IRunnableWithProgress {
	// Imported OBDX content as string and DOM.
	private String importOBDXContent = null;
	private DocumentBuilderFactory factory = null;
	private DocumentBuilder builder = null;
	private Document document = null;
	private openTransImporter oi;
	// Connection result as HTML-string.
	private String runResult = ""; //$NON-NLS-1$
	// html head and foot
	private String htmlhead = ""; //$NON-NLS-1$
	private String htmlfoot = ""; //$NON-NLS-1$
	private String shippedinterval = ""; //$NON-NLS-1$

	/**
	 * Create the class. Initialize the html head and foot strings
	 */
	public webshopImporter() {
		oi = new openTransImporter();
		// Initialize the html head and foot strings with style definitions
		htmlhead = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"><style type=\"text/css\"><!--"; //$NON-NLS-1$
		htmlhead += "body {font-family:sans-serif;font-size:11px;color:#000;margin:5px;padding:0px;}"; //$NON-NLS-1$
		htmlhead += "h1 {font-family:sans-serif;font-size:15px;color:#000;margin:0px 0 5px 0;padding:0px;}"; //$NON-NLS-1$
		htmlhead += "h2 {font-family:sans-serif;font-size:14px;color:#000;margin:5px 0 2px 0;;padding:0px;}"; //$NON-NLS-1$
		htmlhead += "h3 {font-family:sans-serif;font-size:12px;color:#000;margin:2px 0 2px 0;;padding:0px;}"; //$NON-NLS-1$
		htmlhead += "th {font-family:sans-serif;font-size:11px;color:#000;margin:0px;padding:1px;}"; //$NON-NLS-1$
		htmlhead += "td {font-family:sans-serif;font-size:11px;color:#000;margin:0px;padding:1px;}"; //$NON-NLS-1$
		htmlhead += "--></style></head><body>"; //$NON-NLS-1$
		htmlfoot = "</body></html>"; //$NON-NLS-1$

	}

	/*
	 * public static void createIvoiceFromOBDXTransactionNode(Node order) {
	 * String name; String company; String street; String zip; String city;
	 * String country; String phone; String email;
	 * 
	 * String item_quantity; String item_name; String item_description; String
	 * item_total; String item_vatfactor;
	 * 
	 * newTransactionWizard wizard = new newTransactionWizard();
	 * 
	 * // get a new transaction appTransaction inv = null; inv =
	 * client.getTransactions().getInstanceByTypeID(invoice.getType());
	 * //$NON-NLS-1$
	 * 
	 * NodeList childnodes = order.getChildNodes(); NamedNodeMap attributes;
	 * 
	 * // First get all contacts. Normally there is only one for (int
	 * childnodeIndex = 0; childnodeIndex < childnodes.getLength();
	 * childnodeIndex++) { Node childnode = childnodes.item(childnodeIndex);
	 * attributes = childnode.getAttributes();
	 * 
	 * if (childnode.getNodeName().equalsIgnoreCase("contact")) { //$NON-NLS-1$
	 * name = utils.getAttributeAsString(attributes, "name"); //$NON-NLS-1$
	 * company = utils.getAttributeAsString(attributes, "company");
	 * //$NON-NLS-1$ street = utils.getAttributeAsString(attributes, "street");
	 * //$NON-NLS-1$ zip = utils.getAttributeAsString(attributes, "zip");
	 * //$NON-NLS-1$ city = utils.getAttributeAsString(attributes, "city");
	 * //$NON-NLS-1$ country = utils.getAttributeAsString(attributes,
	 * "country"); //$NON-NLS-1$ phone = utils.getAttributeAsString(attributes,
	 * "phone"); //$NON-NLS-1$ email = utils.getAttributeAsString(attributes,
	 * "email"); //$NON-NLS-1$ // delivery_name =
	 * getAttributeAsString(attributes,"delivery_name"); //$NON-NLS-1$ //
	 * delivery_company = getAttributeAsString(attributes,"delivery_company");
	 * //$NON-NLS-1$ // delivery_street =
	 * getAttributeAsString(attributes,"delivery_street"); //$NON-NLS-1$ //
	 * delivery_zip = getAttributeAsString(attributes,"delivery_zip");
	 * //$NON-NLS-1$ // delivery_city =
	 * getAttributeAsString(attributes,"delivery_city"); //$NON-NLS-1$ //
	 * delivery_country = getAttributeAsString(attributes,"delivery_country");
	 * //$NON-NLS-1$ // delivery_phone =
	 * getAttributeAsString(attributes,"delivery_phone"); //$NON-NLS-1$ //
	 * delivery_email = getAttributeAsString(attributes,"delivery_email");
	 * //$NON-NLS-1$
	 * 
	 * // use existing contact, or create new one contact cont = client
	 * .getContacts() .getContactByDetails( name, "", street, "", zip, city,
	 * country, email, phone, "", "", 0, "", "", "", 0); //$NON-NLS-1$
	 * //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	 * //$NON-NLS-7$ inv.setContact(cont); } }
	 * 
	 * // then get all Items inv.getAllProductsAndItems().clear();
	 * 
	 * for (int childnodeIndex = 0; childnodeIndex < childnodes.getLength();
	 * childnodeIndex++) { Node childnode = childnodes.item(childnodeIndex);
	 * attributes = childnode.getAttributes();
	 * 
	 * if (childnode.getNodeName().equalsIgnoreCase("item")) { //$NON-NLS-1$
	 * item_quantity = utils.getAttributeAsString(attributes, "quantity");
	 * //$NON-NLS-1$ item_name = utils.getAttributeAsString(attributes, "name");
	 * //$NON-NLS-1$ item_description = utils.getAttributeAsString(attributes,
	 * "description"); //$NON-NLS-1$ item_total =
	 * utils.getAttributeAsString(attributes, "total"); //$NON-NLS-1$
	 * item_vatfactor = utils.getAttributeAsString(attributes, "vatfactor");
	 * //$NON-NLS-1$ // item_totalgross =
	 * getAttributeAsString(attributes,"totalgross"); //$NON-NLS-1$ //
	 * item_currency = getAttributeAsString(attributes,"currency");
	 * //$NON-NLS-1$
	 * 
	 * BigDecimal vatfactor = new BigDecimal(item_vatfactor);
	 * 
	 * BigDecimal price = new BigDecimal(item_total) .divide(new
	 * BigDecimal(item_quantity)); item i = inv.addItem(); i.setQuantity(new
	 * BigDecimal(item_quantity)); i.setPrice(price);
	 * i.setRemarks(item_description);
	 * 
	 * // use existing tax, or create new one tax vat = null; vat =
	 * client.getTaxes().getSafeVATByFactor(vatfactor);
	 * 
	 * // use existing product, or create new one product prod =
	 * client.getProducts() .getExistingProductByDetails(item_name, "", price);
	 * //$NON-NLS-1$ if (prod == null) { prod = new product(vat, item_name,
	 * price); prod.save(); client.getProducts().add(prod); }
	 * i.setProduct(prod);
	 * 
	 * // check, if wizard page is complete
	 * wizard.getWizardSelect().checkPageComplete(); } }
	 * 
	 * WizardDialog dialog = new WizardDialog(null, wizard); dialog.open(); }
	 */

	/**
	 * Export the order as "transaction" node to generate an invoice.
	 * 
	 * @param useorder_id
	 *            order ID to export
	 */
	public void generateInvoiceFromOrderID(String useorder_id) {
		for (appTransaction currentStatus : client.getTransactions()
				.getTransactions()) {
			if (currentStatus.getTypeID() == offer.getType()) {
				offer current = (offer) currentStatus;

				if (current.getOrderID().equalsIgnoreCase(useorder_id)) {

					int transID = current.getID();
					client.getTransactions().setAsCurrentTransaction(transID);
					client.getTransactions().setTransactionListIndex(
							client.getTransactions().getInstanceIndexForTypeID(
									invoice.getType())); //$NON-NLS-1$
					appTransaction trans = client.getTransactions()
							.getCurrentTransaction();
					trans.setReferTo(transID);
					trans.getNewTransactionNumber();
					newTransactionWizard wizard = new newTransactionWizard();
					wizard.getWizardSelect().checkPageComplete();
					WizardDialog dialog = new WizardDialog(null, wizard);
					dialog.open();
					break;
				}

			}
		}

		// createIvoiceFromOBDXTransactionNode(transaction);
	}

	/**
	 * Generate an html error string.
	 * 
	 * @param error
	 *            Error message
	 * @param detail
	 *            Error description
	 */
	public String MessageToString(String error, String detail) {
		return htmlhead + "<h1>" + error + "</h1>" + detail + htmlfoot; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Search the table for a specified order_id and set the status of the
	 * entry.
	 * 
	 * @param table
	 *            SWT table
	 * @param order_id
	 *            order ID
	 * @param orderstatus
	 *            new order status
	 */
	public void setTableStatus(Table table, String order_id, int orderstatus) {
		TableItem ti[] = table.getItems();
		// find the table entry
		for (int i = 0; i < ti.length; i++) {
			if (ti[i].getText(0).equalsIgnoreCase(order_id)) {
				setTableItemStatus(ti[i], orderstatus);
			}
		}
	}

	/**
	 * Convert the content of an order to an HTML string, which is displayed in
	 * the SWT browser
	 * 
	 * @param useorder_id
	 *            ID of the order to convert
	 * @return Order details as HTML string.
	 */
	public String getHTMLPreview(String useorder_id) {
		String order_id;
		String date;
		String ordersstatus;
		String paymentmethod;
		String total;
		String name;
		String company;
		String street;
		String zip;
		String city;
		String country;
		String phone;
		String email;
		String delivery_name;
		String delivery_company;
		String delivery_street;
		String delivery_zip;
		String delivery_city;
		String delivery_country;
		String delivery_phone;
		String delivery_email;
		String item_quantity;
		String item_name;
		String item_description;
		String item_total;
		String item_vat;
		String item_totalgross;
		String item_currency;

		String html = ""; //$NON-NLS-1$

		for (appTransaction currentStatus : client.getTransactions()
				.getTransactions()) {
			if (currentStatus.getTypeID() == offer.getType()) {
				offer currentOff = (offer) currentStatus;
				if (currentOff.getOrderID().equalsIgnoreCase(useorder_id)) {
					order_id = currentOff.getOrderID();

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$

					date = sdf.format(currentOff.getIssueDate());
					// paymentmethod =
					// getPaymentMethodText(utils.getAttributeAsString(
					//				attributes, "paymentmethod")); //$NON-NLS-1$
					total = currentOff.getTotalString();
					ordersstatus = currentOff.getStatusString();

					html += "<h1>" + Messages.getString("webshopImporter.Order") + ": <span style='color:#c00;'>(" + currentOff.getStatusString() + ")</span></h1>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					//						html += "" + Messages.getString("webshopImporter.Date") + ": " + date + " " + Messages.getString("webshopImporter.Total") + ": " + total + " " + Messages.getString("webshopImporter.Payment_method") + ": " + paymentmethod + "<br>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$

					html += "<table width=\"100%\" ><tr><td width=\"50%\">"; //$NON-NLS-1$

					name = currentOff.getContact().getCO();
					company = currentOff.getContact().getName();
					street = currentOff.getContact().getStreet();
					zip = currentOff.getContact().getZIP();
					city = currentOff.getContact().getLocation();
					country = currentOff.getContact().getCountry();
					phone = currentOff.getContact().getPhone();
					email = currentOff.getContact().getEmail();
					delivery_name = name;
					delivery_company = company;
					delivery_street = street;
					delivery_zip = zip;
					delivery_city = city;
					delivery_country = country;
					delivery_phone = phone;
					delivery_email = email;

					/*
					 * boolean deliveryequalsinvoice = ((name
					 * .equalsIgnoreCase(delivery_name)) &&
					 * (company.equalsIgnoreCase(delivery_company)) &&
					 * (street.equalsIgnoreCase(delivery_street)) &&
					 * (zip.equalsIgnoreCase(delivery_zip)) &&
					 * (city.equalsIgnoreCase(delivery_city)) &&
					 * (country.equalsIgnoreCase(delivery_country)) &&
					 * (phone.equalsIgnoreCase(delivery_phone)) && (email
					 * .equalsIgnoreCase(delivery_email)));
					 */
					boolean deliveryequalsinvoice = true;

					html += "<table width=\"100%\" ><tr><td>"; //$NON-NLS-1$

					// check, if delivery address is equal to the invoice
					// address
					if (deliveryequalsinvoice)
						html += "<h2>" + Messages.getString("webshopImporter.Billing_and_delivery_address") + ":</h2>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					else
						html += "<h2>" + Messages.getString("webshopImporter.Billing_address") + ":</h2>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					html += "<table>"; //$NON-NLS-1$
					html += "<tr><td><b>" + Messages.getString("webshopImporter.Address") + ":</b></td><td>" + name + "</td></tr>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					html += "<tr><td>&nbsp;</td><td>" + company + "</td></tr>"; //$NON-NLS-1$ //$NON-NLS-2$
					html += "<tr><td>&nbsp;</td><td>" + street + "</td></tr>"; //$NON-NLS-1$ //$NON-NLS-2$
					html += "<tr><td>&nbsp;</td><td>" + zip + " " + city + "</td></tr>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					html += "<tr><td>&nbsp;</td><td>" + country + "</td></tr>"; //$NON-NLS-1$ //$NON-NLS-2$
					html += "<tr><td><b>" + Messages.getString("webshopImporter.Phone") + ":</b></td><td>" + phone + "</td></tr>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					html += "<tr><td><b>" + Messages.getString("webshopImporter.email") + ":</b></td><td>" + email + "</td></tr>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					html += "</table>"; //$NON-NLS-1$

					html += "</td><td width=\"10%\">&nbsp;</td><td>"; //$NON-NLS-1$

					if (!deliveryequalsinvoice) {
						html += "<h2>" + Messages.getString("webshopImporter.Delivery_address") + ":</h2>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						html += "<table>"; //$NON-NLS-1$
						html += "<tr><td><b>" + Messages.getString("webshopImporter.Address") + ":</b></td><td>" + delivery_name + "</td></tr>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						html += "<tr><td>&nbsp;</td><td>" + delivery_company + "</td></tr>"; //$NON-NLS-1$ //$NON-NLS-2$
						html += "<tr><td>&nbsp;</td><td>" + delivery_street + "</td></tr>"; //$NON-NLS-1$ //$NON-NLS-2$
						html += "<tr><td>&nbsp;</td><td>" + delivery_zip + " " + delivery_city + "</td></tr>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						html += "<tr><td>&nbsp;</td><td>" + delivery_country + "</td></tr>"; //$NON-NLS-1$ //$NON-NLS-2$
						html += "<tr><td><b>" + Messages.getString("webshopImporter.Phone") + ":</b></td><td>" + delivery_phone + "</td></tr>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						html += "<tr><td><b>" + Messages.getString("webshopImporter.email") + ":</b></td><td>" + delivery_email + "</td></tr>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						html += "</table>"; //$NON-NLS-1$

					} else
						html += "</td>"; //$NON-NLS-1$
					html += "</td></tr></table>"; //$NON-NLS-1$

					html += "</td>"; //$NON-NLS-1$

					html += "<td width=\"10%\">&nbsp;</td>"; //$NON-NLS-1$

					html += "<td width=\"40%\" align=\"left\" valign=\"top\">"; //$NON-NLS-1$
					// 2nd get all comments
					// no monnets foreseen for the time being
					/*
					 * if (childnode.getNodeName().equalsIgnoreCase("comment"))
					 * { //$NON-NLS-1$ html +=
					 * "<div style=\"margin: 0 0 20px 0;padding: 5px 5px 5px 5px;background-color:#fd0;\">"
					 * ; //$NON-NLS-1$ html += "<h3>" +
					 * utils.getAttributeAsString(attributes, "date") + "</h3>";
					 * //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					 * 
					 * for (int textChildNodeIndex = 0; textChildNodeIndex <
					 * childnode .getChildNodes().getLength();
					 * textChildNodeIndex++) { if (childnode.getChildNodes()
					 * .item(textChildNodeIndex).getNodeType() ==
					 * Node.TEXT_NODE) html += childnode.getChildNodes()
					 * .item(textChildNodeIndex) .getTextContent() + "<br>";
					 * //$NON-NLS-1$ } html += "</div>"; //$NON-NLS-1$
					 */

					html += "</td></tr></table>"; //$NON-NLS-1$

					html += "<h2>" + Messages.getString("webshopImporter.Items") + ":</h2>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					html += "<table border=\"1\" cellspacing=\"0\" cellpadding=\"2\"><thead><tr><th>" + Messages.getString("webshopImporter.Quantity") + "</th><th>" + Messages.getString("webshopImporter.Name") + "</th><th>" + Messages.getString("webshopImporter.Description") + "</th><th>" + Messages.getString("webshopImporter.Total") + "</th><th>" + Messages.getString("webshopImporter.VAT") + "</th><th>" + Messages.getString("webshopImporter.Gross_Total") + "</th></tr></thead><tbody>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$

					// 3rd get all Items
					for (item currentItem : currentOff.getItems()) {
						html += "<tr>"; //$NON-NLS-1$
						html += "<td>" + currentItem.getQuantity() + "</td>"; //$NON-NLS-1$ //$NON-NLS-2$
						html += "<td>" + currentItem.getProduct().getName() + "</td>"; //$NON-NLS-1$ //$NON-NLS-2$
						html += "<td>" + currentItem.getProduct().getDescription() + "</td>"; //$NON-NLS-1$ //$NON-NLS-2$
						html += "<td>" + currentItem.getTotal() + " Eur </td>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						html += "<td>" + currentItem.getProduct().getVAT().getFactor() + "</td>"; //$NON-NLS-1$ //$NON-NLS-2$
						html += "<td>" + currentItem.getTotalGrossString() + " Eur </td>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						html += "</tr>"; //$NON-NLS-1$

					}

					html += "</tbody></table>"; //$NON-NLS-1$
				}
			}
		}

		return htmlhead + html + htmlfoot;
	}

	/**
	 * Set the status of one table entry Order with status "shipped" are marked
	 * in gray color
	 * 
	 * @param tableitem
	 *            Table entry
	 * @param orderstatus
	 *            New order status
	 */
	public void setTableItemStatus(TableItem tableitem, int orderstatus) {
		if (orderstatus == 1) { //$NON-NLS-1$
			tableitem.setForeground(new Color(null, 0, 0, 0));
			tableitem.setImage(5, new Image(tableitem.getDisplay(), getClass()
					.getResourceAsStream("/libs/icon_order_pending.png"))); //$NON-NLS-1$
		} else if (orderstatus == 2) { //$NON-NLS-1$
			tableitem.setForeground(new Color(null, 0, 0, 0));
			tableitem.setImage(5, new Image(tableitem.getDisplay(), getClass()
					.getResourceAsStream("/libs/icon_order_processing.png"))); //$NON-NLS-1$
		} else if (orderstatus == 3) { //$NON-NLS-1$
			tableitem.setForeground(new Color(null, 200, 200, 200));
			tableitem.setImage(5, new Image(tableitem.getDisplay(), getClass()
					.getResourceAsStream("/libs/icon_order_shipped.png"))); //$NON-NLS-1$
		} else {
			tableitem.setForeground(new Color(null, 0, 0, 0));
		}
		tableitem.setText(5, offer.getAStatusAsString(orderstatus));
	}

	/**
	 * Fill the preview-table with the most important information
	 * 
	 * @param table
	 *            SWT Table
	 */
	public void fillTableWithOrderInformation(Table table) {
		String order_id;
		String date;
		String name;
		String paymentmethod;
		String total;
		int orderstatus;

		table.removeAll();

		if (document == null)
			return;

		for (appTransaction currentStatus : client.getTransactions()
				.getTransactions()) {

			if (currentStatus.getTypeID() == offer.getType()) {
				offer current = (offer) currentStatus;

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
				date = sdf.format(current.getIssueDate());
				// paymentmethod =
				// getPaymentMethodText(utils.getAttributeAsString(
				//				attributes, "paymentmethod")); //$NON-NLS-1$
				total = current.getTotalString();
				order_id = current.getOrderID();
				orderstatus = current.getStatus();

				name = "---"; //$NON-NLS-1$
				if (currentStatus.getContact() != null) {
					name = currentStatus.getContact().getName();
				}
				TableItem item = new TableItem(table, SWT.NONE);

				item.setText(0, order_id);
				item.setText(1, name);
				item.setText(2, date);
				item.setText(3, total);
				// item.setText(4, paymentmethod);
				setTableItemStatus(item, orderstatus);
			}
		}
	}

	/**
	 * Connect to the web shop and request new orders. Use a progress monitor.
	 * Send a list with unsynchronized orders.
	 * 
	 * @param table
	 *            SWT Table
	 */
	@Override
	public void run(IProgressMonitor ipm) {

		runResult = MessageToString(
				Messages.getString("webshopImporter.Error"), Messages.getString("webshopImporter.loading_the_data")); //$NON-NLS-1$ //$NON-NLS-2$

		factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		try {
			// connect to web shop
			URLConnection conn = null;
			String address = configs.getWebShopURL();
			ipm.beginTask(Messages
					.getString("webshopImporter.Connection_to_web_shop"), 100); //$NON-NLS-1$
			ipm.subTask(Messages.getString("webshopImporter.Connected_to") + address); //$NON-NLS-1$
			ipm.worked(10);
			URL url = new URL(address);
			conn = url.openConnection();
			conn.setDoOutput(true);
			conn.setConnectTimeout(4000);

			// send username , password and a list of unsynchronized orders to
			// the shop
			OutputStreamWriter writer = new OutputStreamWriter(
					conn.getOutputStream());
			ipm.worked(10);
			writer.write("username=" + configs.getWebShopUser() + "&password=" + configs.getWebShopPassword() + //$NON-NLS-1$ //$NON-NLS-2$
					"&action=getorders&getshipped=" + shippedinterval //$NON-NLS-1$
					+ "&setstate="); //$NON-NLS-1$ //$NON-NLS-2$
			writer.flush();
			String line;
			ipm.worked(10);

			// read the OBDX answer (the orders)
			importOBDXContent = ""; //$NON-NLS-1$
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			ipm.subTask(Messages.getString("webshopImporter.Loading_data")); //$NON-NLS-1$
			int iprogress;
			int worked = 30;
			double progress = worked;
			// read line by line and set the progress bar
			while (((line = reader.readLine()) != null) && (!ipm.isCanceled())) {
				System.out.println(line);
				importOBDXContent += line;

				// exponential function to 100%
				progress += (100 - progress) * 0.02;
				iprogress = (int) progress;

				if (iprogress > worked) {
					ipm.worked(iprogress - worked);
					worked = iprogress;
				}
			}

			// parse the OBDX stream
			if (!ipm.isCanceled()) {
				ByteArrayInputStream importInputStream = new ByteArrayInputStream(
						importOBDXContent.getBytes());
				document = builder.parse(importInputStream);

				oi.setDocument(document);
				try {
					oi.run(ipm);
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				NodeList ndList = document
						.getElementsByTagName("openbusinessdataexchange"); //$NON-NLS-1$

				if (ndList.getLength() != 0) {
					runResult = MessageToString(
							Messages.getString("webshopImporter.Download_complete"), ""); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					runResult = importOBDXContent;
				}

				ndList = document.getElementsByTagName("error"); //$NON-NLS-1$
				if (ndList.getLength() > 0) {
					runResult = MessageToString(
							Messages.getString("webshopImporter.Error"), ndList.item(0).getTextContent()); //$NON-NLS-1$
				}
			}
			// cancel the download
			else {
				runResult = MessageToString(
						Messages.getString("webshopImporter.Warning"), Messages.getString("webshopImporter.User_abort")); //$NON-NLS-1$ //$NON-NLS-2$

			}

			writer.close();
			reader.close();

			ipm.done();

		} catch (SAXException e) {
			runResult = importOBDXContent;
		} catch (IOException e) {
			runResult = MessageToString(
					Messages.getString("webshopImporter.Error"), Messages.getString("webshopImporter.Could_not_connect_to_web_shop")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Get the result string of the connection.
	 * 
	 * @return run result
	 */
	public String getRunResult() {
		return runResult;
	}

	/**
	 * Set the interval, how old an shipped order may be to be downloaded.
	 * 
	 * @param intervall
	 *            result
	 */
	public void setShippedInterval(String interval) {
		shippedinterval = interval;
	}

}
