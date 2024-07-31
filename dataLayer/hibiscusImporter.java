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

public class hibiscusImporter extends Thread implements IRunnableWithProgress {
	class part {
		private String description = "", amount; //$NON-NLS-1$
		private boolean virginity = true;

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			virginity = false;
			this.description = description;
		}

		public String getAmount() {
			return amount;
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

	public hibiscusImporter(String filename) {
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
		importFileContent = bom + importFileContent; //$NON-NLS-1$

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
		NodeList ndList = document.getElementsByTagName("object"); //$NON-NLS-1$

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
			Node detailElement = bookingDetails.item(detailIndex);
			if (detailElement.getNodeName() == "datum") { //$NON-NLS-1$
				date = detailElement.getTextContent().substring(6, 10)
						+ "-" + detailElement.getTextContent().substring(3, 5) + "-" + detailElement.getTextContent().substring(0, 2); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// else if (detailElement.getNodeName()=="VALUTA") {
			// valuta=detailElement.getTextContent();
			// }
			if ((detailElement.getNodeName() == "zweck") || (detailElement.getNodeName() == "zweck2") || (detailElement.getNodeName() == "zweck3")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				currentPart.setDescription(currentPart.getDescription()
						+ detailElement.getTextContent());
			} else if (detailElement.getNodeName() == "betrag") { //$NON-NLS-1$
				currentPart.setAmount(detailElement.getTextContent());
			}
			// else if (detailElement.getNodeName()=="WAEHRUNG") {
			// currency=detailElement.getTextContent();
			// }
			else if (detailElement.getNodeName() == "empfaenger_name") { //$NON-NLS-1$
				name = detailElement.getTextContent();
			} else if (detailElement.getNodeName() == "empfaenger_konto") { //$NON-NLS-1$
				account = detailElement.getTextContent();

			} else if (detailElement.getNodeName() == "empfaenger_blz") { //$NON-NLS-1$
				bankcode = detailElement.getTextContent();

			}

		}
		if (!currentPart.isEmpty()) {
			parts.add(currentPart);
		}

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


			}

		}
	}

}
