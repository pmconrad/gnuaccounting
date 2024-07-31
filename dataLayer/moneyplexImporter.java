package dataLayer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import GUILayer.Messages;
import appLayer.client;

public class moneyplexImporter extends Thread implements IRunnableWithProgress {
	class part {
		private String description, amount;
		private boolean virginity = true;

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			virginity = false;
			this.description = description.replace("@", " "); //$NON-NLS-1$ //$NON-NLS-2$
		}

		public String getAmount() {
			if (amount == null) {
				// e.g. in moneyplex XML, the tag "BETRAG" (amount) is missing
				// for the monthly payment of accounts which are free of charge
				return ""; //$NON-NLS-1$
			} else {
				return amount;
			}
		}

		public void setAmount(String amount) {
			virginity = false;
			this.amount = amount;
		}

		public boolean isEmpty() {
			return virginity;
		}

	}

	private String filename = null;
	static DocumentBuilderFactory factory = null;
	static DocumentBuilder builder = null;
	static Document document = null;
	static Node rootNode = null;

	public moneyplexImporter(String filename) {
		this.filename = filename;

	}

	public void run(IProgressMonitor ipm) {
		ipm.beginTask(
				Messages.getString("newAccountingWizardImport.reading") + filename, 100); //$NON-NLS-1$
		factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String importFileContent = null;
		try {
			importFileContent = fileUtils.readFileAsString(filename);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		/*
		 * the moneyplex XML lacks an XML header which we need to add for the
		 * parser to know that it's ANSI encoded... plus we add a little
		 * UTF8-BOM
		 */
		byte[] bomArr = { -17, -69, -65 };// this is the standard UTF-8
											// intel-endian Byte Order Mark
											// (BOM) in signed int notation
		String bom = new String(bomArr);
		importFileContent = bom
				+ "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + importFileContent; //$NON-NLS-1$

		ByteArrayInputStream importInputStream = new ByteArrayInputStream(
				importFileContent.getBytes());
		ipm.worked(100);

		try {
			document = builder.parse(importInputStream);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		rootNode = document.getDocumentElement();
		NodeList ndList = document.getElementsByTagName("BUCHUNG"); //$NON-NLS-1$

		ipm.beginTask(
				Messages.getString("newAccountingWizardImport.importing") + filename, ndList.getLength()); //$NON-NLS-1$
		for (int bookingIndex = 0; bookingIndex < ndList.getLength(); bookingIndex++) {
			if (isInterrupted())
				break;
			ipm.worked(bookingIndex);
			Node booking = ndList.item(bookingIndex);
			// if there is a attribute in the tag number:value
			NodeList bookingDetails = booking.getChildNodes();

			importEntriesFromChildNodes(bookingDetails);

		}

	}

	private void importEntriesFromChildNodes(NodeList bookingDetails) {
		String date = ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// recipient
		String name = "", bank = "", bankcode = "", account = ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		Vector<part> parts = new Vector<part>();
		part currentPart = new part();// there could be split bookings, so
										// handle every entry as split with at
										// least one part...
		for (int detailIndex = 0; detailIndex < bookingDetails.getLength(); detailIndex++) {
			yield();

			Node detailElement = bookingDetails.item(detailIndex);
			if (detailElement.getNodeName() == "DATUM") { //$NON-NLS-1$
				date = detailElement.getTextContent();
			}
			if (detailElement.getNodeName() == "SPLITT") { //$NON-NLS-1 //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-1$
				for (int splitPartIndex = 0; splitPartIndex < detailElement
						.getChildNodes().getLength(); splitPartIndex++) {
					Node splitElement = detailElement.getChildNodes().item(
							splitPartIndex);
					if (splitElement.getNodeName() == "PART") { //$NON-NLS-1$
						for (int partPartIndex = 0; partPartIndex < splitElement
								.getChildNodes().getLength(); partPartIndex++) {
							Node partElement = splitElement.getChildNodes()
									.item(partPartIndex);
							if (partElement.getNodeName() == "ZWECK") { //$NON-NLS-1$
								currentPart.setDescription(partElement
										.getTextContent());
							} else if (partElement.getNodeName() == "BETRAG") { //$NON-NLS-1$
								currentPart.setAmount(partElement
										.getTextContent());
							}
						}
						if (!currentPart.isEmpty()) {
							parts.add(currentPart);// PART only exists in SPLITT
													// but we don't need to know
													// about split
						}
						currentPart = new part();

					}

				}
			}
			if (detailElement.getNodeName() == "PART") { //$NON-NLS-1$
				if (!currentPart.isEmpty()) {
					parts.add(currentPart);// PART only exists in SPLITT but we
											// don't need to know about split
				}
				currentPart = new part();
			}

			// else if (detailElement.getNodeName()=="VALUTA") {
			// valuta=detailElement.getTextContent();
			// }
			else if (detailElement.getNodeName() == "ZWECK") { //$NON-NLS-1$
				currentPart.setDescription(detailElement.getTextContent());
			} else if (detailElement.getNodeName() == "BETRAG") { //$NON-NLS-1$
				currentPart.setAmount(detailElement.getTextContent());
			}
			// else if (detailElement.getNodeName()=="WAEHRUNG") {
			// currency=detailElement.getTextContent();
			// }
			else if (detailElement.getNodeName() == "EMPFAENGER") { //$NON-NLS-1$
				// recipient
				NodeList recipientDetails = detailElement.getChildNodes();
				name = "";bank = "";bankcode = "";account = ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

				for (int recipientDetailIndex = 0; recipientDetailIndex < recipientDetails
						.getLength(); recipientDetailIndex++) {
					Node recipientDetailElement = recipientDetails
							.item(recipientDetailIndex);
					if (recipientDetailElement.getNodeName() == "NAME") { //$NON-NLS-1$
						name = recipientDetailElement.getTextContent();
					} else if (recipientDetailElement.getNodeName() == "BANKNAME") { //$NON-NLS-1$
						bank = recipientDetailElement.getTextContent();
					} else if (recipientDetailElement.getNodeName() == "BLZ") { //$NON-NLS-1$
						bankcode = recipientDetailElement.getTextContent();
					} else if (recipientDetailElement.getNodeName() == "KONTONR") { //$NON-NLS-1$
						account = recipientDetailElement.getTextContent();
					}

				}
			}

		}
		if (!currentPart.isEmpty()) {
			parts.add(currentPart);
		}

		String day = date.substring(0, 2);
		String month = date.substring(3, 5);
		String year = date.substring(6, 8);

		date = "20" + year + "-" + month + "-" + day; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (part listedPart : parts) {
			/*
			 * the amount of a booking can be missing if it's e.g. just a
			 * message from the bank
			 */
			if (listedPart.getAmount() != "") { //$NON-NLS-1$
				String description = listedPart.getDescription();
				description = description.replaceAll("\\'", "\\\\'"); //$NON-NLS-1$ //$NON-NLS-2$

				String amount = listedPart.getAmount();
				amount = amount.replace(',', '.');
				SimpleDateFormat ISO = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
				Date when;
				try {
					when = ISO.parse(date);
					client.getImportQueue().add(description, name, bank,
							account, bankcode, when, new BigDecimal(amount));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				yield();

			}

		}
	}

}
