package dataLayer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import appLayer.client;
import appLayer.contact;
import appLayer.entry;
import appLayer.item;
import appLayer.product;
import appLayer.transactionRelated.appTransaction;
import appLayer.transactionRelated.offer;

public class openTransImporter extends Thread implements IRunnableWithProgress {

	private class party {
		String senderOrganisation, accountHolder, accountCode, bankCode;

		public String getSenderOrganisation() {
			return senderOrganisation;
		}

		public void setSenderOrganisation(String senderOrganisation) {
			this.senderOrganisation = senderOrganisation;
		}

		public String getAccountHolder() {
			return accountHolder;
		}

		public void setAccountHolder(String accountHolder) {
			this.accountHolder = accountHolder;
		}

		public String getAccountCode() {
			return accountCode;
		}

		public void setAccountCode(String accountCode) {
			this.accountCode = accountCode;
		}

		public String getBankCode() {
			return bankCode;
		}

		public void setBankCode(String bankCode) {
			this.bankCode = bankCode;
		}

	}

	private String filename;

	static DocumentBuilderFactory factory = null;
	static DocumentBuilder builder = null;
	static Document document = null;
	static Node rootNode = null;
	private boolean runConcluded = false;
	private Vector<entry> entriesToImport = new Vector<entry>();

	private String usage;
	private Date issueDate, dueDate;
	private BigDecimal value;
	private String number;

	private offer importedOffer;

	public openTransImporter(String filename) {
		this.filename = filename;
		String importFileContent = null;
		try {
			importFileContent = fileUtils.readFileAsString(filename);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		ByteArrayInputStream importInputStream = new ByteArrayInputStream(
				importFileContent.getBytes());

		try {
			document = builder.parse(importInputStream);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public openTransImporter() {
	}

	public void setDocument(Document doc) {
		document = doc;
	}

	/**
	 * Get the status of an order by its oder ID.
	 * 
	 * @param useorder_id
	 *            order ID
	 * @return status as string
	 */
	public String getOrderStatusById(String useorder_id) {
		for (appTransaction current : client.getTransactions()
				.getTransactions()) {
			if (current.getTypeID() == offer.getType()
					&& (((offer) current).getOrderID()
							.equalsIgnoreCase(useorder_id))) {
				return ((offer) current).getStatusString();
			}

		}
		return "";// ID not found //$NON-NLS-1$
	}

	/**
	 * Convert the payment method to a readable (and localized) text.
	 * 
	 * @param intext
	 *            order status
	 * @return payment method as readable (and localized) text
	 */
	private String getPaymentMethodText(String intext) {
		String paymentstatustext = intext;

		if (intext.equalsIgnoreCase("cod"))paymentstatustext = Messages.getString("webshopImporter.Cash_on_Delivery"); //$NON-NLS-1$ //$NON-NLS-2$
		else if (intext.equalsIgnoreCase("prepayment"))paymentstatustext = Messages.getString("webshopImporter.Prepayment"); //$NON-NLS-1$ //$NON-NLS-2$
		else if (intext.equalsIgnoreCase("creditcard"))paymentstatustext = Messages.getString("webshopImporter.Credit_Card"); //$NON-NLS-1$ //$NON-NLS-2$
		else if (intext.equalsIgnoreCase("check"))paymentstatustext = Messages.getString("webshopImporter.Check"); //$NON-NLS-1$ //$NON-NLS-2$

		return paymentstatustext;
	}

	party importParty(Node partiesSubNode) {
		party res = new party();
		NodeList partySubElements = partiesSubNode.getChildNodes();

		for (int partyIndex = 0; partyIndex < partySubElements.getLength(); partyIndex++) {
			Node partySubNode = partySubElements.item(partyIndex);

			if (partySubNode.getNodeName().equals("ADDRESS")) { //$NON-NLS-1$
				NodeList addressElements = partySubNode.getChildNodes();
				for (int addressIndex = 0; addressIndex < addressElements
						.getLength(); addressIndex++) {
					Node addressNode = addressElements.item(addressIndex);
					if (addressNode.getNodeName().equals("bmecat:NAME")) { //$NON-NLS-1$
						res.setSenderOrganisation(addressNode.getTextContent());
					}
					if (addressNode.getNodeName().equals("CONTACT_DETAILS")) { //$NON-NLS-1$
						NodeList addressDetailElements = addressNode
								.getChildNodes();
						for (int addressDetailIndex = 0; addressDetailIndex < addressDetailElements
								.getLength(); addressDetailIndex++) {
							Node addressDetailNode = addressDetailElements
									.item(addressDetailIndex);

							if (addressDetailNode.getNodeName().equals(
									"bmecat:CONTACT_NAME")) { //$NON-NLS-1$
								res.setSenderOrganisation(addressDetailNode
										.getTextContent());
							}

						}

					}
				}

			}
			if (partySubNode.getNodeName().equals("ACCOUNT")) { //$NON-NLS-1$
				NodeList accountElements = partySubNode.getChildNodes();
				for (int accountIndex = 0; accountIndex < accountElements
						.getLength(); accountIndex++) {

					Node accountNode = accountElements.item(accountIndex);
					if (accountNode.getNodeName().equals("HOLDER")) { //$NON-NLS-1$
						res.setAccountHolder(accountNode.getTextContent());
					}
					if (accountNode.getNodeName().equals("BANK_ACCOUNT")) { //$NON-NLS-1$
						res.setAccountCode(accountNode.getTextContent());
					}
					if (accountNode.getNodeName().equals("BANK_CODE")) { //$NON-NLS-1$
						res.setBankCode(accountNode.getTextContent());
					}
				}
			}

		}
		return res;
	}

	public void run(IProgressMonitor ipm) throws InvocationTargetException,
			InterruptedException {

		String importTransactionType = null;

		ipm.beginTask(
				Messages.getString("OpenTransImporter.statusReading") + filename, 100); //$NON-NLS-1$
		factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		HashMap<BigDecimal, BigDecimal> taxes = null;

		rootNode = document.getDocumentElement();
		party p = null;
		NodeList ndList = document.getElementsByTagName("INVOICE"); //$NON-NLS-1$
		if (ndList.getLength() != 0) {
			// we're parsing an invoice
			BigDecimal totalGrossValue = new BigDecimal(0);
			contact senderContact = null;

			String invoice_id = ""; //$NON-NLS-1$
			String invoice_date = ""; //$NON-NLS-1$
			String netValue = ""; //$NON-NLS-1$
			String totalAmount = ""; //$NON-NLS-1$

			for (int transactionIndex = 0; transactionIndex < ndList
					.getLength(); transactionIndex++) {
				if (isInterrupted())
					break;
				float length, processed;
				length = ndList.getLength();
				processed = transactionIndex;
				ipm.worked(Math.round(length / processed * 100));
				taxes = new HashMap<BigDecimal, BigDecimal>();
				senderContact = null; // set contact undefined in this iteration
										// until one is found
				Node transaction = ndList.item(transactionIndex);
				// if there is a attribute in the tag number:value
				NodeList transactionSubElements = transaction.getChildNodes();
				importTransactionType = "invoice"; //$NON-NLS-1$


				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
				for (int subElementIndex = 0; subElementIndex < transactionSubElements
						.getLength(); subElementIndex++) {
					yield();
					Node detailElement = transactionSubElements
							.item(subElementIndex);
					if (detailElement.getNodeName().equals("INVOICE_HEADER")) { //$NON-NLS-1$
						NodeList headerSubElements = detailElement
								.getChildNodes();
						for (int headerSubIndex = 0; headerSubIndex < headerSubElements
								.getLength(); headerSubIndex++) {
							Node headerSubNode = headerSubElements
									.item(headerSubIndex);
							if (headerSubNode.getNodeName().equals(
									"INVOICE_INFO")) { //$NON-NLS-1$
								NodeList invoiceInfoSubElements = headerSubNode
										.getChildNodes();
								for (int invoiceInfoSubIndex = 0; invoiceInfoSubIndex < invoiceInfoSubElements
										.getLength(); invoiceInfoSubIndex++) {
									Node invoiceInfoSubNode = invoiceInfoSubElements
											.item(invoiceInfoSubIndex);
									if (invoiceInfoSubNode.getNodeName()
											.equals("INVOICE_ID")) { //$NON-NLS-1$
										invoice_id = invoiceInfoSubNode
												.getTextContent();
									}
									if (invoiceInfoSubNode.getNodeName()
											.equals("INVOICE_DATE")) { //$NON-NLS-1$
										invoice_date = invoiceInfoSubNode
												.getTextContent();
										try {
											String invDate = invoice_date
													.substring(0, 10);

											setIssueDate(sdf.parse(invDate)); //$NON-NLS-1$
											setDueDate(sdf.parse(invDate));
										} catch (ParseException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} //$NON-NLS-1$

									}
									if (invoiceInfoSubNode.getNodeName()
											.equals("PARTIES")) { //$NON-NLS-1$
										NodeList partiesSubElements = invoiceInfoSubNode
												.getChildNodes();
										for (int partiesIndex = 0; partiesIndex < partiesSubElements
												.getLength(); partiesIndex++) {
											Node partiesSubNode = partiesSubElements
													.item(partiesIndex);
											if (partiesSubNode.getNodeName()
													.equals("PARTY")) { //$NON-NLS-1$

												p = importParty(partiesSubNode);
											}

										}

									}

								}

							}
						}

					}

					senderContact = client.getContacts()
							.getContactByBankDetails(p.getAccountCode(),
									p.getBankCode(), p.getAccountHolder(),
									p.getSenderOrganisation());

					if (detailElement.getNodeName().equals("INVOICE_SUMMARY")) { //$NON-NLS-1$
						NodeList summarySubElements = detailElement
								.getChildNodes();
						for (int summarySubIndex = 0; summarySubIndex < summarySubElements
								.getLength(); summarySubIndex++) {
							Node summarySubNode = summarySubElements
									.item(summarySubIndex);
							if (summarySubNode.getNodeName().equals(
									"NET_VALUE_GOODS")) { //$NON-NLS-1$
								netValue = summarySubNode.getTextContent();
							}
							if (summarySubNode.getNodeName().equals(
									"TOTAL_AMOUNT")) { //$NON-NLS-1$
								totalAmount = summarySubNode.getTextContent();
							}
						}

					}
				}

			}
			totalGrossValue = new BigDecimal(totalAmount);
			setUsage(invoice_id);

			setValue(totalGrossValue);
			runConcluded = true;
			try {
				entry e = new entry(0, getIssueDate(), getUsage(), getValue(),
						client.getAccounts().getDefaultAccount(), client
								.getAccounts().getDefaultAccount(), "", ""); //$NON-NLS-1$ //$NON-NLS-2$
				if (senderContact != null) {
					e.setContact(senderContact);
				}
				entriesToImport.add(e);
			} catch (prematureException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			totalGrossValue = new BigDecimal(totalAmount);
			// setUsage(order_id);

			setValue(totalGrossValue);
			runConcluded = true;
			try {
				entry e = new entry(0, getIssueDate(), getUsage(), getValue(),
						client.getAccounts().getDefaultAccount(), client
								.getAccounts().getDefaultAccount(), "", ""); //$NON-NLS-1$ //$NON-NLS-2$
				if (senderContact != null) {
					e.setContact(senderContact);
				}
				entriesToImport.add(e);
			} catch (prematureException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// check, if wizard page is complete

		}
		ndList = document.getElementsByTagName("ORDER"); //$NON-NLS-1$
		if (ndList.getLength() != 0) {
			// we're parsing an order
			BigDecimal totalGrossValue = new BigDecimal(0);
			contact senderContact = null;

			String order_id = ""; //$NON-NLS-1$
			String order_date = ""; //$NON-NLS-1$
			String netValue = ""; //$NON-NLS-1$
			String totalAmount = ""; //$NON-NLS-1$

			for (int transactionIndex = 0; transactionIndex < ndList
					.getLength(); transactionIndex++) {
				if (isInterrupted())
					break;
				float length, processed;
				length = ndList.getLength();
				processed = transactionIndex;
				ipm.worked(Math.round(length / processed * 100));
				taxes = new HashMap<BigDecimal, BigDecimal>();
				senderContact = null; // set contact undefined in this iteration
										// until one is found

				// get a new transaction

				/*
				 * inv = (invoice) client.getTransactions().getInstanceByTypeID(
				 * invoice.getType()); //$NON-NLS-1$
				 * 
				 * // then get all Items inv.getAllProductsAndItems().clear();
				 */

				importedOffer = new offer(client.getTransactions());
				importedOffer.setStatus(1);
				importedOffer.setType(client.getTransactions().getTypeByTypeID(
						offer.getType()));

				Node transaction = ndList.item(transactionIndex);
				// if there is a attribute in the tag number:value
				NodeList transactionSubElements = transaction.getChildNodes();
				NamedNodeMap nnm = transaction.getAttributes();
				importTransactionType = "order"; //$NON-NLS-1$

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
				for (int subElementIndex = 0; subElementIndex < transactionSubElements
						.getLength(); subElementIndex++) {
					yield();
					Node detailElement = transactionSubElements
							.item(subElementIndex);
					if (detailElement.getNodeName().equals("ORDER_HEADER")) { //$NON-NLS-1$
						NodeList headerSubElements = detailElement
								.getChildNodes();
						for (int headerSubIndex = 0; headerSubIndex < headerSubElements
								.getLength(); headerSubIndex++) {
							Node headerSubNode = headerSubElements
									.item(headerSubIndex);
							if (headerSubNode.getNodeName()
									.equals("ORDER_INFO")) { //$NON-NLS-1$
								NodeList invoiceInfoSubElements = headerSubNode
										.getChildNodes();
								for (int invoiceInfoSubIndex = 0; invoiceInfoSubIndex < invoiceInfoSubElements
										.getLength(); invoiceInfoSubIndex++) {
									Node invoiceInfoSubNode = invoiceInfoSubElements
											.item(invoiceInfoSubIndex);
									if (invoiceInfoSubNode.getNodeName()
											.equals("ORDER_ID")) { //$NON-NLS-1$
										order_id = invoiceInfoSubNode
												.getTextContent();

									}
									if (invoiceInfoSubNode.getNodeName()
											.equals("ORDER_DATE")) { //$NON-NLS-1$
										order_date = invoiceInfoSubNode
												.getTextContent();
										try {
											String invDate = order_date
													.substring(0, 10);

											setIssueDate(new Date(sdf.parse(
													invDate).getTime())); //$NON-NLS-1$
											setDueDate(new Date(sdf.parse(
													invDate).getTime()));
										} catch (ParseException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} //$NON-NLS-1$

									}
									if (invoiceInfoSubNode.getNodeName()
											.equals("PARTIES")) { //$NON-NLS-1$
										NodeList partiesSubElements = invoiceInfoSubNode
												.getChildNodes();
										for (int partiesIndex = 0; partiesIndex < partiesSubElements
												.getLength(); partiesIndex++) {
											Node partiesSubNode = partiesSubElements
													.item(partiesIndex);
											if (partiesSubNode.getNodeName()
													.equals("PARTY")) { //$NON-NLS-1$
												p = importParty(partiesSubNode);

											}
											/** party */

										}

									}

								}

							}
						}

					}

					if (detailElement.getNodeName().equals("ORDER_ITEM_LIST")) { //$NON-NLS-1$
						NodeList listSubElements = detailElement
								.getChildNodes();
						for (int listSubIndex = 0; listSubIndex < listSubElements
								.getLength(); listSubIndex++) {
							Node itemsSubNode = listSubElements
									.item(listSubIndex);
							if (itemsSubNode.getNodeName().equals("ORDER_ITEM")) { //$NON-NLS-1$
								NodeList itemSubElements = itemsSubNode
										.getChildNodes();
								item i = importedOffer.addItem();
								String lineID = "", name = "", description = "", supplierPID = "", qty = "", unit = "", price = "", total = ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$

								for (int itemSubIndex = 0; itemSubIndex < itemSubElements
										.getLength(); itemSubIndex++) {
									Node itemSubNode = itemSubElements
											.item(itemSubIndex);

									if (itemSubNode.getNodeName().equals(
											"LINE_ITEM_ID")) { //$NON-NLS-1$
										lineID = itemSubNode.getTextContent();

									}
									if (itemSubNode.getNodeName().equals(
											"PRODUCT_ID")) { //$NON-NLS-1$
										NodeList IDSubElements = itemSubNode
												.getChildNodes();
										for (int IDSubIndex = 0; IDSubIndex < IDSubElements
												.getLength(); IDSubIndex++) {
											Node IDSubNode = IDSubElements
													.item(IDSubIndex);
											if (IDSubNode.getNodeName().equals(
													"bmecat:SUPPLIER_PID")) { //$NON-NLS-1$
												supplierPID = IDSubNode
														.getTextContent();
											}
											if (IDSubNode.getNodeName().equals(
													"bmecat:DESCRIPTION_SHORT")) { //$NON-NLS-1$
												name = IDSubNode
														.getTextContent();

											}
											if (IDSubNode.getNodeName().equals(
													"bmecat:DESCRIPTION_LONG")) { //$NON-NLS-1$
												description = IDSubNode
														.getTextContent();

											}
										}

									}
									if (itemSubNode.getNodeName().equals(
											"QUANTITY")) { //$NON-NLS-1$
										qty = itemSubNode.getTextContent();
										i.setQuantity(new BigDecimal(
												itemSubNode.getTextContent()));
									}
									if (itemSubNode.getNodeName().equals(
											"bmecat:ORDER_UNIT")) { //$NON-NLS-1$
										unit = itemSubNode.getTextContent();

									}
									if (itemSubNode.getNodeName().equals(
											"PRODUCT_PRICE_FIX")) { //$NON-NLS-1$
										NodeList PriceSubElements = itemSubNode
												.getChildNodes();
										for (int priceSubIndex = 0; priceSubIndex < PriceSubElements
												.getLength(); priceSubIndex++) {
											Node PriceSubNode = PriceSubElements
													.item(priceSubIndex);
											if (PriceSubNode
													.getNodeName()
													.equals("bmecat:PRICE_AMOUNT")) { //$NON-NLS-1$
												price = PriceSubNode
														.getTextContent();

											}
										}
									}
									if (itemSubNode.getNodeName().equals(
											"PRICE_LINE_AMOUNT")) { //$NON-NLS-1$
										total = itemSubNode.getTextContent();

									}
								}
								product prod = client.getProducts()
										.getExistingProductByDetails(name,
												description,
												new BigDecimal(price));
								if (prod == null) {
									prod = new product(client.getTaxes()
											.getFirst(), name, new BigDecimal(
											price));
									prod.setDescription(description);
									prod.save();
									client.getProducts().add(prod);
								}
								i.setProduct(prod);
							}
						}

					}

					if (p != null) {
						senderContact = client.getContacts()
								.getContactByBankDetails(p.getAccountCode(),
										p.getBankCode(), p.getAccountHolder(),
										p.getSenderOrganisation());

						importedOffer.setContact(senderContact);
					}
					if (detailElement.getNodeName().equals("ORDER_SUMMARY")) { //$NON-NLS-1$
						NodeList summarySubElements = detailElement
								.getChildNodes();
						for (int summarySubIndex = 0; summarySubIndex < summarySubElements
								.getLength(); summarySubIndex++) {
							Node summarySubNode = summarySubElements
									.item(summarySubIndex);
							if (summarySubNode.getNodeName().equals(
									"NET_VALUE_GOODS")) { //$NON-NLS-1$
								netValue = summarySubNode.getTextContent();
							}
							if (summarySubNode.getNodeName().equals(
									"TOTAL_AMOUNT")) { //$NON-NLS-1$
								totalAmount = summarySubNode.getTextContent();
							}
						}

					}

				}

				for (appTransaction currentStatus : client.getTransactions()
						.getTransactions()) {
					if (currentStatus.getTypeID() == offer.getType()) {
						offer currentInv = (offer) currentStatus;
						if (currentInv.getOrderID().equalsIgnoreCase(order_id)) {
							importedOffer = currentInv;
							break;
						}
					}
				}

				// os.setContact(p.getSenderOrganisation());
				importedOffer.setOrderID(order_id);

				importedOffer.setIssueDate(issueDate);

				/*
				 * os.setPaymentmethod(paymentmethod); os.setStatus(status);
				 */
				importedOffer.save();
			}
		}

		ipm.worked(100);

	}

	public Vector<entry> getEntriesToImport() {
		return entriesToImport;
	}

	public String getUsage() throws prematureException {
		if (!runConcluded) {
			throw new prematureException(""); //$NON-NLS-1$
		}
		return usage;
	}

	private void setUsage(String usage) {
		this.usage = usage;
	}

	public BigDecimal getValue() throws prematureException {
		if (!runConcluded) {
			throw new prematureException(""); //$NON-NLS-1$
		}
		return value;
	}

	private void setValue(BigDecimal value) {
		this.value = value;
	}

	public Date getIssueDate() {
		return issueDate;
	}

	private void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	private void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public String getNumber() {
		return number;
	}

	private void setNumber(String number) {
		this.number = number;
	}

}
