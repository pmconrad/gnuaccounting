package dataLayer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import GUILayer.newTransactionWizard;
import appLayer.client;
import appLayer.item;
import appLayer.transactionRelated.appTransaction;
import appLayer.transactionRelated.invoice;

public class taskTrakerImporters {

	/**
	 * Will return rounded off to 5 minutes and 2 decimal places, e.g. 12:30 -->
	 * 12.5 8:34 --> 8.5 Minutes Decimal 5 0,08 10 0,17 15 0,25 20 0,33 25 0,42
	 * 30 0,50 35 0,58 40 0,67 45 0,75 50 0,83 55 0,92 60 1,00
	 * 
	 * */
	private static BigDecimal roundOffQuantity(long seconds) {
		int hours = (int) Math.floor(seconds / 3600);
		int minutes = (int) Math.floor((seconds % 3600) / 60);
		BigDecimal res = new BigDecimal(hours);
		if (minutes >= 55) {
			res = res.add(new BigDecimal("0.92")); //$NON-NLS-1$
		} else if (minutes >= 50) {
			res = res.add(new BigDecimal("0.83")); //$NON-NLS-1$
		} else if (minutes >= 45) {
			res = res.add(new BigDecimal("0.75")); //$NON-NLS-1$
		} else if (minutes >= 40) {
			res = res.add(new BigDecimal("0.67")); //$NON-NLS-1$
		} else if (minutes >= 35) {
			res = res.add(new BigDecimal("0.58")); //$NON-NLS-1$
		} else if (minutes >= 30) {
			res = res.add(new BigDecimal("0.50")); //$NON-NLS-1$
		} else if (minutes >= 25) {
			res = res.add(new BigDecimal("0.42")); //$NON-NLS-1$
		} else if (minutes >= 20) {
			res = res.add(new BigDecimal("0.33")); //$NON-NLS-1$
		} else if (minutes >= 15) {
			res = res.add(new BigDecimal("0.25")); //$NON-NLS-1$
		} else if (minutes >= 10) {
			res = res.add(new BigDecimal("0.17")); //$NON-NLS-1$
		} else if (minutes >= 5) {
			res = res.add(new BigDecimal("0.08")); //$NON-NLS-1$
		}

		return res;
	}

	public static void importKTimeTracker(String importFileName,
			boolean roundOff, StatusLineManager statusLine, Shell sh) {
		File importFile = new File(importFileName);
		if (!importFile.exists()) {
			statusLine.setErrorMessage(dataLayer.Messages
					.getString("fileImporters.fileNotFound")); //$NON-NLS-1$
			return;
		} else {
			statusLine.setErrorMessage(""); //$NON-NLS-1$
		}

		newTransactionWizard wizard = new newTransactionWizard();

		FileReader importReader;
		try {
			importReader = new FileReader(importFile);
			BufferedReader bufferedImportReader = new BufferedReader(
					importReader);

			appTransaction inv = null;
			inv = client.getTransactions().getInstanceByTypeID(
					invoice.getType());
			String line;
			try {
				while ((line = bufferedImportReader.readLine()) != null) {
					String[] elements = fileUtils.parseCSVLineComma(line);
					String taskName = ""; //$NON-NLS-1$
					for (String currentElem : elements) {
						if ((currentElem != null) && (currentElem.length() > 0)) {
							taskName = currentElem;
							break;
						}
					}
					item i = inv.addItem();
					i.setRemarks(taskName);
					if (roundOff) {
						i.setQuantity(roundOffQuantity(Math.round(Double
								.valueOf(elements[elements.length - 1]) * 3600d)));
					} else {
						i.setQuantity(new BigDecimal(
								elements[elements.length - 1]));
					}

				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			WizardDialog dialog = new WizardDialog(sh, wizard);
			dialog.open();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/** imports android gleeo timer files */
	public static void importGleeo(String importFileName, boolean roundOff,
			StatusLineManager statusLine, Shell sh) {
		File importFile = new File(importFileName);
		if (!importFile.exists()) {
			statusLine.setErrorMessage(dataLayer.Messages
					.getString("fileImporters.fileNotFound")); //$NON-NLS-1$
			return;
		} else {
			statusLine.setErrorMessage(""); //$NON-NLS-1$
		}

		newTransactionWizard wizard = new newTransactionWizard();

		FileReader importReader;
		try {
			importReader = new FileReader(importFile);
			BufferedReader bufferedImportReader = new BufferedReader(
					importReader);

			appTransaction inv = null;
			inv = client.getTransactions().getInstanceByTypeID(
					invoice.getType());
			String line;
			boolean firstLine = true;
			try {
				while ((line = bufferedImportReader.readLine()) != null) {
					if (firstLine) {
						firstLine = false;
						continue; // skip first line: headline
					}
					String[] elements = fileUtils.parseCSVLineComma(line);
					// elements are:
					// 0=Project,1=Task,2=Details,3=Start,4=End,5=Duration,6=Decimal
					// Duration
					item i = inv.addItem();
					String remarks = elements[0] + ":" + elements[1]; //$NON-NLS-1$
					if (elements[2] != null) {
						remarks += " " + elements[2]; //$NON-NLS-1$
					}
					i.setRemarks(remarks);
					if (roundOff) {
						i.setQuantity(roundOffQuantity(Math.round(Double
								.valueOf(elements[5]) * 3600d)));
					} else {
						i.setQuantity(new BigDecimal(elements[6]));
					}

				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			WizardDialog dialog = new WizardDialog(sh, wizard);
			dialog.open();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void importTaskCoach(String importFileName, boolean roundOff,
			StatusLineManager statusLine, Shell sh) {
		DocumentBuilderFactory factory = null;
		DocumentBuilder builder = null;
		Document taskCoachDocument = null;

		File importFile = new File(importFileName);
		if (!importFile.exists()) {
			statusLine.setErrorMessage(dataLayer.Messages
					.getString("fileImporters.fileNotFound")); //$NON-NLS-1$
			return;
		} else {
			statusLine.setErrorMessage(""); //$NON-NLS-1$
		}

		factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ByteArrayInputStream configInputStream;
		try {
			configInputStream = new ByteArrayInputStream(fileUtils
					.readFileAsString(importFileName).getBytes());
			taskCoachDocument = builder.parse(configInputStream);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		appTransaction inv = null;
		inv = client.getTransactions().getInstanceByTypeID(invoice.getType());

		NodeList ndList = taskCoachDocument.getElementsByTagName("task"); //$NON-NLS-1$
		for (int nodeIndex = 0; nodeIndex < ndList.getLength(); nodeIndex++) {
			Node taskNode = ndList.item(nodeIndex);
			if (taskNode.hasAttributes())
			// if there is a attribute in the tag number:value
			{
				NamedNodeMap nmm = taskNode.getAttributes();
				if (nmm.getNamedItem("subject") != null) //$NON-NLS-1$
				{
					String taskName = nmm
							.getNamedItem("subject").getNodeValue(); //$NON-NLS-1$

					NodeList ndEfforts = taskNode.getChildNodes();

					for (int effortIndex = 0; effortIndex < ndEfforts
							.getLength(); effortIndex++) {
						Node currentEffort = ndEfforts.item(effortIndex);
						if (currentEffort.getNodeName().equals("effort")) { //$NON-NLS-1$
							String startStr = currentEffort.getAttributes()
									.getNamedItem("start").getNodeValue(); //$NON-NLS-1$
							String endStr = startStr; // assume 0 length if end
														// is not specified (in
														// task coach usually a
														// still runing process)
							if (currentEffort.getAttributes().getNamedItem(
									"stop") != null) { //$NON-NLS-1$
								endStr = currentEffort.getAttributes()
										.getNamedItem("stop").getNodeValue(); //$NON-NLS-1$
							}
							SimpleDateFormat formatter = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
							Date startDte;
							Date endDte;
							NodeList effortChilds = currentEffort
									.getChildNodes();
							String description = null;
							for (int effortChildIndex = 0; effortChildIndex < effortChilds
									.getLength(); effortChildIndex++) {
								Node currentEffortChild = effortChilds
										.item(effortChildIndex);
								if (currentEffortChild.getNodeName().equals(
										"description")) { //$NON-NLS-1$
									description = currentEffortChild
											.getTextContent();
								}
							}

							try {
								startDte = (Date) formatter.parse(startStr);
								endDte = (Date) formatter.parse(endStr);
								long seconds = (endDte.getTime() - startDte
										.getTime()) / 1000;

								BigDecimal quantity = new BigDecimal(seconds);
								BigDecimal secsPerHour = new BigDecimal(3600);

								item i = inv.addItem();
								if (roundOff) {
									i.setQuantity(roundOffQuantity(seconds));
								} else {
									// round to 10 digits behind the comma if we
									// e.g. have 1/3rd of n hour
									i.setQuantity(quantity.divide(secsPerHour,
											10, RoundingMode.HALF_UP));
								}
								if (description != null) {
									i.setRemarks(taskName + ":" + description); //$NON-NLS-1$
								} else {
									i.setRemarks(taskName);

								}

							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		}

		newTransactionWizard wizard = new newTransactionWizard();
		WizardDialog dialog = new WizardDialog(sh, wizard);
		dialog.open();

	}

}
