package appLayer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ag.ion.bion.officelayer.OSHelper;
import ag.ion.bion.officelayer.application.IApplicationAssistant;
import ag.ion.bion.officelayer.application.ILazyApplicationInfo;
import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.application.OfficeApplicationRuntime;
import ag.ion.bion.officelayer.internal.application.ApplicationAssistant;
import dataLayer.DB;
import dataLayer.fileUtils;

public class configs {

	static DocumentBuilderFactory factory = null;
	static DocumentBuilder builder = null;
	static Document configurationDocument = null;
	static Document settingsDocument = null;

	private static String printerName = null;
	private static String scannerName = null;
	private static String OOoPathName = null;
	private static String WinstonPathName = null;
	private static String GPGPathName = null;

	private static String databaseName = null;
	private static String databaseDriverName = null;
	private static String databaseDriverFileName = null;
	private static String databasePassword = null;
	private static String databaseServer = null;
	private static String databaseUser = null;
	private static String databaseType = null;

	private static Boolean printPoweredBy = null;
	private static Boolean useExternalDB = null;

	// *settings*//

	private static String organisationName = null;
	private static String organisationStreet = null;
	private static String organisationZip = null;
	private static String organisationLocation = null;
	private static String organisationCountry = null;
	private static String taxID = null;
	private static String vatID = null;
	private static Integer stateListIDX = null;
	private static Integer taxOfficeListIDX = null;
	private static String VATPeriod = null;
	private static String senderEmail = null;
	private static String SMTPServer = null;
	private static String SMTPUsername = null;
	private static String SMTPPassword = null;
	private static Boolean useSMTPAuth = null;
	private static Boolean sendICAL = null;
	private static Boolean useSMTPSSL = null;
	private static String accountChart = null;
	private static Integer taxModeID = null;
	private static Boolean isVATexempt = null;
	private static Boolean shallDocTag = null;
	private static Boolean shallOCR = null;
	private static Boolean shallRoundTo5ct = null;
	private static String OCRlang = null;
	private static Boolean hasSalesTax = null;

	// *rest*//
	private static IOfficeApplication officeApplication = null;
	/**
	 * @var this will be the path with which the officeapplication is created so
	 *      that the factory method can check whether it has changed
	 */
	private static String officeApplicationPath = null;
	private static Font defaultFont = null;
	private static String accountCode = null;
	private static String IBAN = null;
	private static String BIC = null;
	private static String bankName = null;
	private static String holderName = null;
	private static String creditorID = null;
	private static String bankCode = null;
	private static String ctAPI = null;
	private static Boolean useCardReaderPINpad = null;

	// settings to import OBDX data from an web shop
	private static String webshopURL = null;
	private static String webshopUser = null;
	private static String webshopPassword = null;

	// the following dialogs have been acknowledged
	private static Boolean ack_quickstart = null;
	private static Boolean ack_dragndrop = null;
	private static boolean createdConfigFile = false;
	private static String displaySince = null;
	private static String displayTo = null;
	private static String userDefinedOfficePath = null;

	// should you need to adjust the taxModeOptions[] please ensure
	// configs::isTaxmodeIssue is still correct
	private static String taxModeOptions[] = {
			Messages.getString("configs.taxmodeIssue"), Messages.getString("configs.taxmodePayment") }; //$NON-NLS-1$ //$NON-NLS-2$
	private static String OCRlangPath;

	public static void setUserDefinedOfficePath(String path) {
		if (path.startsWith(".")) { //$NON-NLS-1$
			// relative path as argument
			String absolutePath = utils.makeRelativePathAbsolute(path);
			if (absolutePath != null) {
				path = absolutePath;
			}

		}

		userDefinedOfficePath = path;
	}

	public static Font getDefaultFont() {
		if (defaultFont == null) {

			// one could check the boolean return value of the following
			// functions to see if the fonts get loaded

			Display.getCurrent().loadFont("./libs/fonts/Vera.ttf"); //$NON-NLS-1$
			Display.getCurrent().loadFont("./libs/fonts/Vera.ttf"); //$NON-NLS-1$
			Display.getCurrent().loadFont("./libs/fonts/VeraBd.ttf"); //$NON-NLS-1$
			Display.getCurrent().loadFont("./libs/fonts/VeraBl.ttf"); //$NON-NLS-1$
			Display.getCurrent().loadFont("./libs/fonts/Veralt.ttf"); //$NON-NLS-1$
			Display.getCurrent().loadFont("./libs/fonts/Vera.ttf"); //$NON-NLS-1$

			/*
			 * System.err.println((l1?"l1":"!l1")+"**"+(l2?"l2":"!l2")+"**"+(l3?"l3"
			 * :"!l3")+"**"+(l4?"l4":"!l4")+"**"+(l5?"l5":"!l5")); FontData[]
			 * fontData = Display.getCurrent().getFontList(null, true); FontData
			 * FD2L=null; for (FontData currentFont : fontData) { if
			 * (currentFont
			 * .getName().equals("Bitstream Vera Sans")&&(currentFont
			 * .getStyle()==0)) {
			 * System.err.println(currentFont.getName()+" xx "
			 * +currentFont.getStyle()); FD2L=currentFont;
			 * 
			 * } }
			 */
			defaultFont = new Font(Display.getCurrent(),
					"Bitstream Vera Sans", 10, SWT.NORMAL); //$NON-NLS-1$
			//			defaultFont = new Font(Display.getCurrent(), FD2L); //$NON-NLS-1$

		}
		return defaultFont;

	}

	public static void readConfiguration() {
		try {

			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			File configFile = new File(client.getConfigFilename());
			if (!configFile.exists()) {
				File configPath = new File(client.getConfigPath());
				if (!configPath.exists()) {
					configPath.mkdir();

				}

				FileWriter fw = new FileWriter(configFile);
				fw.write("<config></config>"); //$NON-NLS-1$
				fw.close();
				createdConfigFile = true;
			}

			ByteArrayInputStream configInputStream = new ByteArrayInputStream(
					fileUtils.readFileAsString(client.getConfigFilename())
							.getBytes());
			configurationDocument = builder.parse(configInputStream);
			readPrinterName();
			readDocumentAquisitionSettings();
			readChipcardSettings();
			readDatabaseCredentials();
			readMiscSettings();
			readAcknowledgements();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void readDocumentAquisitionSettings() throws DOMException {

		// get list of nodes for tag number:value (per se, the tag is value, but
		// java has a strage kind to handle namespaces...)
		NodeList ndList = configurationDocument.getElementsByTagName("scanner"); //$NON-NLS-1$
		for (int i = 0; i < ndList.getLength(); i++) {
			Node n = ndList.item(i);
			if (n.hasAttributes())
			// if there is a attribute in the tag number:value
			{
				NamedNodeMap nmm = n.getAttributes();
				if (nmm.getNamedItem("name") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag printer
				{
					scannerName = nmm.getNamedItem("name").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("ocr-lang") != null) //$NON-NLS-1$
				{
					OCRlang = nmm.getNamedItem("ocr-lang").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("ocr-language-path") != null) //$NON-NLS-1$
				{
					OCRlangPath = nmm
							.getNamedItem("ocr-language-path").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("ocr-enabled") != null) //$NON-NLS-1$
				{
					shallOCR = Boolean.valueOf(nmm
							.getNamedItem("ocr-enabled").getNodeValue()); //$NON-NLS-1$

				}

			}
		}

	}

	public static String getJDBCURL() {
		String hostDelim = "//"; //$NON-NLS-1$
		String nameDelim = "/"; //$NON-NLS-1$
		if (configs.getDatabaseType().equals("derby")) { //$NON-NLS-1$
			hostDelim = ""; //$NON-NLS-1$
			nameDelim = ""; //$NON-NLS-1$
		}
		String jdbcURLDBRMS = "jdbc:" + configs.getDatabaseType() + ":" + hostDelim + configs.getDatabaseServer() + nameDelim; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String jdbcURLDB = jdbcURLDBRMS + configs.getDatabaseName();

		if (configs.getDatabaseType().equals("derby")) { //$NON-NLS-1$
			jdbcURLDB += ";create=true"; //$NON-NLS-1$

		}

		if (configs.getDatabaseType().equals("hsqldb:file")) { //$NON-NLS-1$
			jdbcURLDB += ";shutdown=true"; //$NON-NLS-1$
		}

		return jdbcURLDB;

	}

	public static void readSettings() {
		try {
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			File settingsFile = new File(client.getSettingsFilename());
			if (!settingsFile.exists()) {
				File dataPath = new File(client.getDataPath());
				if (!dataPath.exists()) {
					dataPath.mkdir();

				}

				FileWriter fw = new FileWriter(settingsFile);
				fw.write("<settings></settings>"); //$NON-NLS-1$
				fw.close();

			}

			ByteArrayInputStream settingsInputStream = new ByteArrayInputStream(
					fileUtils.readFileAsString(client.getSettingsFilename())
							.getBytes());
			settingsDocument = builder.parse(settingsInputStream);
			readClientSettings();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void readPrinterName() throws DOMException {
		// get list of nodes for tag number:value (per se, the tag is value, but
		// java has a strage kind to handle namespaces...)
		NodeList ndList = configurationDocument.getElementsByTagName("printer"); //$NON-NLS-1$
		for (int i = 0; i < ndList.getLength(); i++) {
			Node n = ndList.item(i);
			if (n.hasAttributes())
			// if there is a attribute in the tag number:value
			{
				NamedNodeMap nmm = n.getAttributes();
				if (nmm.getNamedItem("name") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag printer
				{
					printerName = nmm.getNamedItem("name").getNodeValue(); //$NON-NLS-1$
				}
			}
		}
	}

	private static void readChipcardSettings() throws DOMException {
		NodeList ndList = configurationDocument
				.getElementsByTagName("chipcard"); //$NON-NLS-1$
		for (int i = 0; i < ndList.getLength(); i++) {
			Node n = ndList.item(i);
			if (n.hasAttributes())
			// if there is a attribute in the tag number:value
			{
				NamedNodeMap nmm = n.getAttributes();
				if (nmm.getNamedItem("readerpinpad") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag printer
				{
					String usePadStringValue;
					usePadStringValue = nmm
							.getNamedItem("readerpinpad").getNodeValue(); //$NON-NLS-1$
					useCardReaderPINpad = Boolean.valueOf(usePadStringValue); // if
					// that
					// String
					// is
					// "true",
					// the
					// boolean
					// will
					// also
					// be
					// true
				}
				if (nmm.getNamedItem("ctapi") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag printer
				{
					ctAPI = nmm.getNamedItem("ctapi").getNodeValue(); //$NON-NLS-1$
				}

			}
		}
	}

	private static void readClientSettings() throws DOMException {
		// get list of nodes for tag number:value (per se, the tag is value, but
		// java has a strage kind to handle namespaces...)
		NodeList ndList = settingsDocument.getElementsByTagName("organization"); //$NON-NLS-1$
		for (int i = 0; i < ndList.getLength(); i++) {
			Node n = ndList.item(i);
			if (n.hasAttributes())
			// if there is a attribute in the tag number:value
			{
				NamedNodeMap nmm = n.getAttributes();
				if (nmm.getNamedItem("name") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag organization
				{
					organisationName = nmm.getNamedItem("name").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("street") != null) //$NON-NLS-1$
				{
					organisationStreet = nmm
							.getNamedItem("street").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("zip") != null) //$NON-NLS-1$
				{
					organisationZip = nmm.getNamedItem("zip").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("location") != null) //$NON-NLS-1$
				{
					organisationLocation = nmm
							.getNamedItem("location").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("country") != null) //$NON-NLS-1$
				{
					organisationCountry = nmm
							.getNamedItem("country").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("stateListIDX") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag organization
				{
					stateListIDX = Integer.valueOf(nmm.getNamedItem(
							"stateListIDX").getNodeValue()); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("taxOfficeListIDX") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag organization
				{
					taxOfficeListIDX = Integer.valueOf(nmm.getNamedItem(
							"taxOfficeListIDX").getNodeValue()); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("taxID") != null) //$NON-NLS-1$
				// if there is a attribute "taxID" in the tag organization of
				// the settings
				{
					taxID = nmm.getNamedItem("taxID").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("vatID") != null) //$NON-NLS-1$
				// if there is a attribute "vatID" in the tag organization of
				// the settings
				{
					vatID = nmm.getNamedItem("vatID").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("VATperiod") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag organization
				{
					VATPeriod = nmm.getNamedItem("VATperiod").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("accountChart") != null) //$NON-NLS-1$
				{
					accountChart = nmm
							.getNamedItem("accountChart").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("taxMode") != null) //$NON-NLS-1$
				{
					taxModeID = Integer.valueOf(nmm
							.getNamedItem("taxMode").getNodeValue()); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("vatExempt") != null) //$NON-NLS-1$
				{
					isVATexempt = Boolean.valueOf(nmm
							.getNamedItem("vatExempt").getNodeValue()); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("round5ct") != null) //$NON-NLS-1$
				{
					shallRoundTo5ct = Boolean.valueOf(nmm
							.getNamedItem("round5ct").getNodeValue()); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("salesTax") != null) //$NON-NLS-1$
				{
					hasSalesTax = Boolean.valueOf(nmm
							.getNamedItem("salesTax").getNodeValue()); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("docTag") != null) //$NON-NLS-1$
				{
					shallDocTag = Boolean.valueOf(nmm
							.getNamedItem("docTag").getNodeValue()); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("displaySince") != null) //$NON-NLS-1$
				{
					displaySince = nmm
							.getNamedItem("displaySince").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("displayTo") != null) //$NON-NLS-1$
				{
					displayTo = nmm.getNamedItem("displayTo").getNodeValue(); //$NON-NLS-1$
				}
			}
		}
		ndList = settingsDocument.getElementsByTagName("email"); //$NON-NLS-1$
		for (int i = 0; i < ndList.getLength(); i++) {
			Node n = ndList.item(i);
			if (n.hasAttributes())
			// if there is a attribute in the tag number:value
			{
				NamedNodeMap nmm = n.getAttributes();
				if (nmm.getNamedItem("sender") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag organization
				{
					senderEmail = nmm.getNamedItem("sender").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("server") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag organization
				{
					SMTPServer = nmm.getNamedItem("server").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("user") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag organization
				{
					SMTPUsername = nmm.getNamedItem("user").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("password") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag organization
				{
					SMTPPassword = nmm.getNamedItem("password").getNodeValue(); //$NON-NLS-1$
				}

				if (nmm.getNamedItem("auth") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag organization
				{
					String useSMTPAuthString = nmm
							.getNamedItem("auth").getNodeValue(); //$NON-NLS-1$
					useSMTPAuth = Boolean.valueOf(useSMTPAuthString); // if that
					// String is
					// "true",
					// the
					// boolean
					// will also
					// be true
				}
				if (nmm.getNamedItem("ssl") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag organization
				{
					String useSMTPSSLString = nmm
							.getNamedItem("ssl").getNodeValue(); //$NON-NLS-1$
					useSMTPSSL = Boolean.valueOf(useSMTPSSLString); // if that
					// String is
					// "true",
					// the
					// boolean
					// will also
					// be true
				}
				if (nmm.getNamedItem("ical") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag organization
				{
					String sendICALString = nmm
							.getNamedItem("ical").getNodeValue(); //$NON-NLS-1$
					sendICAL = Boolean.valueOf(sendICALString); // if that
					// String is
					// "true",
					// the
					// boolean
					// will also
					// be true
				}

			}
		}

		ndList = settingsDocument.getElementsByTagName("accounts"); //$NON-NLS-1$
		for (int i = 0; i < ndList.getLength(); i++) {
			Node n = ndList.item(i);
			if (n.hasAttributes())
			// if there is a attribute in the tag number:value
			{
				NamedNodeMap nmm = n.getAttributes();
				if (nmm.getNamedItem("iban") != null) //$NON-NLS-1$
				{
					IBAN = nmm.getNamedItem("iban").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("bic") != null) //$NON-NLS-1$
				{
					BIC = nmm.getNamedItem("bic").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("accountcode") != null) //$NON-NLS-1$
				{
					accountCode = nmm
							.getNamedItem("accountcode").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("bankcode") != null) //$NON-NLS-1$
				{
					bankCode = nmm.getNamedItem("bankcode").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("bankname") != null) //$NON-NLS-1$
				{
					bankName = nmm.getNamedItem("bankname").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("holdername") != null) //$NON-NLS-1$
				{
					holderName = nmm.getNamedItem("holdername").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("creditorid") != null) //$NON-NLS-1$
				{
					creditorID = nmm.getNamedItem("creditorid").getNodeValue(); //$NON-NLS-1$
				}

			}
		}

		ndList = settingsDocument.getElementsByTagName("webshop"); //$NON-NLS-1$
		for (int i = 0; i < ndList.getLength(); i++) {
			Node n = ndList.item(i);
			if (n.hasAttributes())
			// if there is a attribute in the tag number:value
			{
				NamedNodeMap nmm = n.getAttributes();
				if (nmm.getNamedItem("url") != null) //$NON-NLS-1$
				{
					webshopURL = nmm.getNamedItem("url").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("user") != null) //$NON-NLS-1$
				{
					webshopUser = nmm.getNamedItem("user").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("password") != null) //$NON-NLS-1$
				{
					webshopPassword = nmm
							.getNamedItem("password").getNodeValue(); //$NON-NLS-1$
				}

			}
		}

	}

	private static void readDatabaseCredentials() throws DOMException {
		// get list of nodes for tag number:value (per se, the tag is value, but
		// java has a strage kind to handle namespaces...)
		useExternalDB = Boolean.valueOf(false);
		NodeList ndList = configurationDocument
				.getElementsByTagName("database"); //$NON-NLS-1$
		for (int i = 0; i < ndList.getLength(); i++) {
			Node n = ndList.item(i);
			if (n.hasAttributes()) {
				NamedNodeMap nmm = n.getAttributes();

				if (nmm.getNamedItem("useExternal") != null) //$NON-NLS-1$
				{

					String useExternalDBString = nmm
							.getNamedItem("useExternal").getNodeValue(); //$NON-NLS-1$
					useExternalDB = Boolean.valueOf(useExternalDBString); // if that
					// String
					// is
					// "true",
					// the
					// boolean
					// will
					// also
					// be
					// true
				}

				if (nmm.getNamedItem("database") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag printer
				{
					databaseName = nmm.getNamedItem("database").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("driver") != null) //$NON-NLS-1$
				// if there is a attribute "driver" in the tag database
				{
					databaseDriverName = nmm.getNamedItem("driver"). //$NON-NLS-1$
							getNodeValue();
				}

				if (nmm.getNamedItem("file") != null) //$NON-NLS-1$
				// if there is a attribute "file" in the tag database
				{
					databaseDriverFileName = nmm.getNamedItem("file"). //$NON-NLS-1$
							getNodeValue();
				}

				if (nmm.getNamedItem("password") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag printer
				{
					databasePassword = nmm.getNamedItem("password"). //$NON-NLS-1$
							getNodeValue();
				}
				if (nmm.getNamedItem("server") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag printer
				{
					databaseServer = nmm.getNamedItem("server").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("user") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag printer
				{
					databaseUser = nmm.getNamedItem("user").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("type") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag printer
				{
					databaseType = nmm.getNamedItem("type").getNodeValue(); //$NON-NLS-1$
				}

			}
		}
		if (!useExternalDB.booleanValue()) {
			// db defaults
			// "jdbc:derby:"+client.getGlobalDataPath()+"derby;create=true;shutdown=true"
			databaseType = "derby"; //$NON-NLS-1$
			databaseName = client.getGlobalDataPath()
					+ "derby" + DB.getDBVersionString(); //$NON-NLS-1$ //used to be hibernate
			databaseDriverName = "org.apache.derby.jdbc.EmbeddedDriver"; //$NON-NLS-1$
			databaseUser = "test"; //$NON-NLS-1$
			databasePassword = "test"; //$NON-NLS-1$
			/*
			 * databaseType = "hsqldb:file";//"hsqldb:file" //$NON-NLS-1$
			 * databaseName = "hsqldb"; //$NON-NLS-1$ //used to be hibernate
			 * databaseDriverName = "org.hsqldb.jdbcDriver"; //$NON-NLS-1$
			 * databaseDriverFileName = ""; // not needed to load jar, jar
			 * already in classpath //$NON-NLS-1$ databaseServer =
			 * client.getGlobalDataPath().substring(0,
			 * client.getGlobalDataPath().length() - 1); // cut off /, //
			 * otherwise it // will be there // twice databaseUser = "sa";
			 * //$NON-NLS-1$ databasePassword = ""; //$NON-NLS-1$
			 */
			// jdbc:derby:encryptedDB;create=true;dataEncryption=true;
			// bootPassword=DBpassword
		}

	}

	private static void readMiscSettings() throws DOMException {
		// get list of nodes for tag number:value (per se, the tag is value, but
		// java has a strage kind to handle namespaces...)
		printPoweredBy = Boolean.valueOf(true); // default

		WinstonPathName = ""; //$NON-NLS-1$
		GPGPathName = ""; //$NON-NLS-1$
		NodeList ndList = configurationDocument.getElementsByTagName("misc"); //$NON-NLS-1$
		for (int i = 0; i < ndList.getLength(); i++) {
			Node n = ndList.item(i);
			if (n.hasAttributes()) {
				NamedNodeMap nmm = n.getAttributes();
				if (nmm.getNamedItem("printPoweredBy") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag printer
				{
					String printPoweredByString = nmm.getNamedItem(
							"printPoweredBy").getNodeValue(); //$NON-NLS-1$
					printPoweredBy = Boolean.valueOf(printPoweredByString); // if
					// that
					// String
					// is
					// "true",
					// the
					// boolean
					// will
					// also
					// be
					// true
				}
				if (nmm.getNamedItem("openoffice") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag printer
				{
					OOoPathName = nmm.getNamedItem("openoffice").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("gpg") != null) //$NON-NLS-1$
				{
					GPGPathName = nmm.getNamedItem("gpg").getNodeValue(); //$NON-NLS-1$
				}
				if (nmm.getNamedItem("winston") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag printer
				{
					WinstonPathName = nmm
							.getNamedItem("winston").getNodeValue(); //$NON-NLS-1$
				}

			}
		}
	}

	private static void readAcknowledgements() throws DOMException {
		NodeList ndList = configurationDocument
				.getElementsByTagName("acknowledged"); //$NON-NLS-1$
		for (int i = 0; i < ndList.getLength(); i++) {
			Node n = ndList.item(i);
			if (n.hasAttributes()) {
				NamedNodeMap nmm = n.getAttributes();
				if (nmm.getNamedItem("quickstart") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag printer
				{
					String quickstartString = nmm
							.getNamedItem("quickstart").getNodeValue(); //$NON-NLS-1$
					ack_quickstart = Boolean.valueOf(quickstartString);
					/* if that String is "true", the boolean will also be true */
				}
				if (nmm.getNamedItem("dragndrop") != null) //$NON-NLS-1$
				// if there is a attribute "name" in the tag printer
				{
					String dragndropString = nmm
							.getNamedItem("dragndrop").getNodeValue(); //$NON-NLS-1$
					ack_dragndrop = Boolean.valueOf(dragndropString);
					/* if that String is "true", the boolean will also be true */
				}

			}
		}
	}

	public static String getPrinterName() {
		if (printerName == null) {
			readConfiguration();
		}
		return printerName;
	}

	public static String getScannerName() {
		if (scannerName == null) {
			readConfiguration();
		}
		return scannerName;
	}

	public static ILazyApplicationInfo getOfficeInfo() {
		IApplicationAssistant applicationAssistant;
		ILazyApplicationInfo appInfo = null, LappInfo = null, OappInfo = null;
		try {

			applicationAssistant = new ApplicationAssistant(
					System.getProperty(IOfficeApplication.NOA_NATIVE_LIB_PATH));
			if ((userDefinedOfficePath != null)&&(userDefinedOfficePath.length()>0)) {
				appInfo = applicationAssistant.findLocalApplicationInfo(userDefinedOfficePath);
			} else {
				LappInfo = applicationAssistant
						.getLatestLocalLibreOfficeApplication();
				OappInfo = applicationAssistant
						.getLatestLocalOpenOfficeOrgApplication();
				appInfo = OappInfo;// take the latest openoffice installation
									// unless libreoffice is also installed in a
									// higher version
				if (appInfo == null) {// no OOo installed
					appInfo = LappInfo;
				}
				if ((LappInfo != null) && (OappInfo != null)) {
					if (LappInfo.getMajorVersion() > OappInfo.getMajorVersion()) {
						appInfo = LappInfo;
					}
					if (LappInfo.getMajorVersion() == OappInfo
							.getMajorVersion()) {
						if (LappInfo.getMinorVersion() > OappInfo
								.getMinorVersion()) {
							appInfo = LappInfo;
						}
					}
				}
			}
		} catch (OfficeApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return appInfo;
	}

	public static String getOOoPath() throws OfficeApplicationException {
		if (userDefinedOfficePath != null) {
			return userDefinedOfficePath;
		}
		if (OOoPathName == null) {
			readConfiguration();
		}
		if (OOoPathName == null) {
			OOoPathName = ""; //$NON-NLS-1$
		}
		if (OOoPathName != null) { // don't use an else here because
			// readConfiguation might very well change
			// the OOoPathName
			if (OOoPathName.length() == 0) { // config file read but no path
				// found, start guessing
				ILazyApplicationInfo appInfo = configs.getOfficeInfo();
				if (appInfo != null) {
					OOoPathName = appInfo.getHome();
				} else {
					OOoPathName = null;
				}
			}

		}
		return OOoPathName;

	}

	public static String getGPGPath() {
		if (GPGPathName == null) {
			readConfiguration();
		}
		return GPGPathName;
	}

	public static String getWinstonPath() {
		if (WinstonPathName == null) {
			readConfiguration();
		}
		if (WinstonPathName == null) {
			return ""; //$NON-NLS-1$
		}
		return WinstonPathName;
	}

	public static String getDatabaseName() {
		if (databaseName == null) {
			readConfiguration();
		}
		if (databaseName == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return databaseName;
	}

	public static String getDatabaseDriverName() {
		if (databaseDriverName == null) {
			readConfiguration();
		}
		if (databaseDriverName == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return databaseDriverName;
	}

	public static String getDatabaseDriverFileName() {
		if (databaseDriverFileName == null) {
			readConfiguration();
		}
		if (databaseDriverFileName == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return databaseDriverFileName;
	}

	public static String getDatabasePassword() {
		if (databasePassword == null) {
			readConfiguration();
		}
		if (databasePassword == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return databasePassword;
	}

	public static String getDatabaseServer() {
		if (databaseServer == null) {
			readConfiguration();
		}
		if (databaseServer == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return databaseServer;
	}

	public static String getDatabaseUser() {
		if (databaseUser == null) {
			readConfiguration();
		}
		if (databaseUser == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return databaseUser;
	}

	public static String getDatabaseType() {
		if (databaseType == null) {
			readConfiguration();
		}
		if (databaseType == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return databaseType;
	}

	public static String getOrganisationName() {
		if (organisationName == null) {
			readSettings();
		}
		if (organisationName == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return organisationName;
	}

	public static Integer getStateListIDX() {
		if (stateListIDX == null) {
			readSettings();
		}
		if (stateListIDX == null) { // no configuration there
			return -1;
		}
		return stateListIDX;
	}

	public static Integer getTaxOfficeListIDX() {
		if (taxOfficeListIDX == null) {
			readSettings();
		}
		if (taxOfficeListIDX == null) { // no configuration there
			return -1;
		}
		return taxOfficeListIDX;
	}

	public static String getTaxID() {
		if (taxID == null) {
			readSettings();
		}
		if (taxID == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return taxID;
	}

	public static String getVATID() {
		if (vatID == null) {
			readSettings();
		}
		if (vatID == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return vatID;
	}

	public static String getDisplaySince() {
		if (displaySince == null) {
			readSettings();
		}
		if (displaySince == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return displaySince;
	}

	public static String getDisplayTo() {
		if (displayTo == null) {
			readSettings();
		}
		if (displayTo == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return displayTo;
	}

	public static String getVATPeriod() {
		if (VATPeriod == null) {
			readSettings();
		}
		if (VATPeriod == null) { // no configuration there
			return "monthly"; // default //$NON-NLS-1$
		}
		return VATPeriod;
	}

	public static String getAccountChart() {
		if (accountChart == null) {
			readSettings();
		}
		if (accountChart == null) { // no configuration there
			return Messages.getString("configs.defaultAccountingChart"); // default  //$NON-NLS-1$
		}
		return accountChart;
	}

	public static boolean isVATexempt() {
		if (isVATexempt == null) {
			readSettings();
		}
		if (isVATexempt == null) { // no configuration there
			return false; // default
		}
		return isVATexempt.booleanValue();
	}

	public static boolean shallDocTag() {
		if (shallDocTag == null) {
			readSettings();
		}
		if (shallDocTag == null) { // no configuration there
			return false; // default
		}
		return shallDocTag.booleanValue();
	}

	public static String getSenderEmail() {
		if (senderEmail == null) {
			readSettings();
		}
		if (senderEmail == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return senderEmail;
	}

	public static boolean shallOCR() {
		if (shallOCR == null) {
			readConfiguration();
		}
		if (shallOCR == null) { // no configuration there
			return false; // default
		}
		return shallOCR.booleanValue();
	}

	public static String getOCRlang() {
		if (OCRlang == null) {
			readConfiguration();
		}
		if (OCRlang == null) { // no configuration there, return default
			return "eng"; //$NON-NLS-1$
		}
		return OCRlang;
	}

	public static String getOCRlangPath() {
		if (OCRlangPath == null) {
			readConfiguration();
		}
		if (OCRlangPath == null) { // no configuration there, return default
			return ""; //$NON-NLS-1$
		}
		return OCRlangPath;
	}

	public static String getSMTPServer() {
		if (SMTPServer == null) {
			readSettings();
		}
		if (SMTPServer == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return SMTPServer;
	}

	public static String getSMTPUsername() {
		if (SMTPUsername == null) {
			readSettings();
		}
		if (SMTPUsername == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return SMTPUsername;
	}

	public static String getSMTPPassword() {
		if (SMTPPassword == null) {
			readSettings();
		}
		if (SMTPPassword == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return SMTPPassword;
	}

	public static String getAccountCode() {
		if (accountCode == null) {
			readSettings();
		}
		if (accountCode == null) { // no configuration there
			return "";//$NON-NLS-1$
		}
		return accountCode;

	}

	public static String getIBAN() {
		if (IBAN == null) {
			readSettings();
		}
		if (IBAN == null) { // no configuration there
			return "";//$NON-NLS-1$
		}
		return IBAN;

	}

	public static String getBIC() {
		if (BIC == null) {
			readSettings();
		}
		if (BIC == null) { // no configuration there
			return "";//$NON-NLS-1$
		}
		return BIC;

	}

	public static String getBankName() {
		if (bankName == null) {
			readSettings();
		}
		if (bankName == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return bankName;

	}

	public static String getHolderName() {
		if (holderName == null) {
			readSettings();
		}
		if (holderName == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return holderName;

	}

	public static String getBankCode() {
		if (bankCode == null) {
			readSettings();
		}
		if (bankCode == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return bankCode;

	}

	public static String getWebShopURL() {
		if (webshopURL == null) {
			readSettings();
		}
		if (webshopURL == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return webshopURL;

	}

	public static String getWebShopUser() {
		if (webshopUser == null) {
			readSettings();
		}
		if (webshopUser == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return webshopUser;

	}

	public static String getWebShopPassword() {
		if (webshopPassword == null) {
			readSettings();
		}
		if (webshopPassword == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return webshopPassword;

	}

	public static boolean shallUseSMTPAuth() {
		if (useSMTPAuth == null) {
			readConfiguration();
		}
		if (useSMTPAuth == null) { // no configuration there
			return false; // default
		}
		return useSMTPAuth.booleanValue();
	}

	public static boolean shallUseSMTPSSL() {
		if (useSMTPSSL == null) {
			readConfiguration();
		}
		if (useSMTPSSL == null) { // no configuration there
			return false; // default
		}
		return useSMTPSSL.booleanValue();
	}

	public static boolean shallSendICAL() {
		if (sendICAL == null) {
			readConfiguration();
		}
		if (sendICAL == null) { // no configuration there
			return false; // default
		}
		return sendICAL.booleanValue();
	}

	public static String getCtAPI() {
		if (ctAPI == null) {
			readConfiguration();
		}
		if (ctAPI == null) { // no configuration there
			return ""; // default //$NON-NLS-1$
		}
		return ctAPI;
	}

	public static boolean shallUseCardReaderPINPad() {
		if (useCardReaderPINpad == null) {
			readConfiguration();
		}
		if (useCardReaderPINpad == null) { // no configuration there
			return false; // default
		}
		return useCardReaderPINpad.booleanValue();
	}

	public static boolean isAcknowledgedQuickstart() {
		if (ack_quickstart == null) {
			readConfiguration();
		}
		if (ack_quickstart == null) { // no configuration there
			return false; // default
		}
		return ack_quickstart.booleanValue();
	}

	public static boolean isAcknowledgedDragndrop() {
		if (ack_dragndrop == null) {
			readConfiguration();
		}
		if (ack_dragndrop == null) { // no configuration there
			return false; // default
		}
		return ack_dragndrop.booleanValue();
	}

	public static IOfficeApplication getOfficeApplication() {
		IOfficeApplication newOfficeApplication = null;
		try {
			String savedOfficeApplicationPath = configs.getOOoPath();
			if ((officeApplication != null)
					&& (savedOfficeApplicationPath
							.equals(officeApplicationPath))) {
				if (!officeApplication.isActive()) {
					try {
						officeApplication.activate();
						officeApplication.getDesktopService()
								.activateTerminationPrevention();
					} catch (OfficeApplicationException e) {
						e.printStackTrace();
					}
				}
				return officeApplication;
			}
			officeApplicationPath = savedOfficeApplicationPath;
			if (officeApplicationPath == null
					|| officeApplicationPath.length() == 0) {
				return null;
			}
			HashMap configuration = new HashMap();
			configuration.put(IOfficeApplication.APPLICATION_HOME_KEY,
					officeApplicationPath);
			String[] oooArgs = new String[] { "--nofirststartwizard", //$NON-NLS-1$
					"--norestore", "--nolockcheck", "-nocrashreport", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"--nodefault", "--nologo" }; //$NON-NLS-1$ //$NON-NLS-2$
			configuration.put(IOfficeApplication.APPLICATION_ARGUMENTS_KEY,
					oooArgs);
			configuration.put(IOfficeApplication.APPLICATION_TYPE_KEY,
					IOfficeApplication.LOCAL_APPLICATION);
			newOfficeApplication = OfficeApplicationRuntime
					.getApplication(configuration);
			newOfficeApplication.setConfiguration(configuration);
			newOfficeApplication.activate();
			newOfficeApplication.getDesktopService()
					.activateTerminationPrevention();
		} catch (OfficeApplicationException e) {
			e.printStackTrace();
			try {
				IOfficeApplication officeApp = configs.getOfficeApplication();
				if (officeApp != null) {
					if (officeApp.isActive()) {
						officeApp.deactivate();
					}
				}
			} catch (OfficeApplicationException e1) {
				e1.printStackTrace();
			}

		}
		officeApplication = newOfficeApplication;
		return officeApplication;
	}

	public static void disposeOfficeApplication() {
		// do not use getOfficeApplication, as this starts an office app
		// although it should be shutdown
		if (officeApplication != null) {
			try {
				officeApplication.deactivate();
				officeApplication.dispose();

			} catch (OfficeApplicationException e) {
				e.printStackTrace();
			} // this is really necessary
			officeApplication = null;
			System.err.println(Messages.getString("configs.officeDeactivated")); //$NON-NLS-1$
		}

	}

	public static boolean isOOoEmbedded() {
		// OOo for Mac does not support embedded Frames.
		return !OSHelper.IS_MAC;
	}

	public static boolean shallPrintPoweredBy() {
		if (printPoweredBy == null) {
			readConfiguration();
		}
		if (printPoweredBy == null) { // no configuration there
			return true; // default
		}

		return printPoweredBy.booleanValue();
	}

	public static boolean shallUseExternalDB() {
		if (useExternalDB == null) {
			readConfiguration();
		}
		if (useExternalDB == null) { // no configuration there
			return false; // default
		}
		return useExternalDB.booleanValue();
	}

	public static void setNodeAttribute(Document doc, String node,
			String attribute, String value) {
		if (value == null) {
			return;
		}
		NodeList ndList = doc.getElementsByTagName(node);
		if (ndList.getLength() == 0) {
			Node newNode = doc.createElement(node);
			doc.getDocumentElement().appendChild(newNode);
			
			ndList = doc.getElementsByTagName(node);

		}
		for (int nodeIndex = 0; nodeIndex < ndList.getLength(); nodeIndex++) {
			Node currentNode = ndList.item(nodeIndex);
			if ((!currentNode.hasAttributes() || (currentNode.getAttributes()
					.getNamedItem(attribute) == null))) {

				// Attr newAttr = doc.createAttribute(attribute);

				Element currentElement = (Element) currentNode;
				currentElement.setAttribute(attribute, value);
				

			} else {
				currentNode.getAttributes().getNamedItem(attribute)
						.setNodeValue(value);
			}

		}

	}

	public static void setPrinterName(String printerName) {
		setNodeAttribute(configurationDocument, "printer", "name", printerName); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setScannerName(String scannerName) {
		setNodeAttribute(configurationDocument, "scanner", "name", scannerName); //$NON-NLS-1$ //$NON-NLS-2$

	}

	public static void setDatabaseName(String databasename) {
		setNodeAttribute(configurationDocument,
				"database", "database", databasename); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setDatabaseDriverName(String databasedrivername) {
		setNodeAttribute(configurationDocument,
				"database", "driver", databasedrivername); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setDatabaseDriverFileName(String databasedriverfilename) {
		setNodeAttribute(configurationDocument,
				"database", "file", databasedriverfilename); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setDatabasePassword(String password) {
		setNodeAttribute(configurationDocument,
				"database", "password", password); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setDatabaseServer(String server) {
		setNodeAttribute(configurationDocument, "database", "server", server); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setDatabaseUser(String user) {
		setNodeAttribute(configurationDocument, "database", "user", user); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setDatabaseType(String type) {
		setNodeAttribute(configurationDocument, "database", "type", type); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setOOoPath(String path) {
		setNodeAttribute(configurationDocument, "misc", "openoffice", path); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setWinstonPath(String path) {
		setNodeAttribute(configurationDocument, "misc", "winston", path); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setOCRlang(String lang) {
		setNodeAttribute(configurationDocument, "scanner", "ocr-lang", lang); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setGPGPath(String path) {
		setNodeAttribute(configurationDocument, "misc", "gpg", path); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setPrintPoweredBy(boolean print) {
		String printStringValue;
		if (print) {
			printStringValue = "true"; //$NON-NLS-1$
		} else {
			printStringValue = "false"; //$NON-NLS-1$
		}
		setNodeAttribute(configurationDocument,
				"misc", "printPoweredBy", printStringValue); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setStartWithCommunity(boolean start) {
		String startStringValue;
		if (start) {
			startStringValue = "true"; //$NON-NLS-1$
		} else {
			startStringValue = "false"; //$NON-NLS-1$
		}
		setNodeAttribute(configurationDocument,
				"misc", "startWithCommunity", startStringValue); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setUseExternalDB(boolean externalDB) {
		String externalDBStringValue;
		if (externalDB) {
			externalDBStringValue = "true"; //$NON-NLS-1$
		} else {
			externalDBStringValue = "false"; //$NON-NLS-1$
		}
		setNodeAttribute(configurationDocument,
				"database", "useExternal", externalDBStringValue); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setOrganizationName(String orgaName) {
		setNodeAttribute(settingsDocument, "organization", "name", orgaName); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setTaxmode(int mode) {
		setNodeAttribute(settingsDocument,
				"organization", "taxMode", Integer.toString(mode)); //$NON-NLS-1$ //$NON-NLS-2$
		taxModeID = mode;
	}

	public static void setVATExempt(boolean isExempt) {
		String exemptStr = "false"; //$NON-NLS-1$
		if (isExempt) {
			exemptStr = "true"; //$NON-NLS-1$
		}
		setNodeAttribute(settingsDocument,
				"organization", "vatExempt", exemptStr); //$NON-NLS-1$ //$NON-NLS-2$
		isVATexempt = isExempt;
	}

	public static void setShallDocTag(boolean setIt) {
		String docTagStr = "false"; //$NON-NLS-1$
		if (setIt) {
			docTagStr = "true"; //$NON-NLS-1$
		}
		setNodeAttribute(settingsDocument, "organization", "docTag", docTagStr); //$NON-NLS-1$ //$NON-NLS-2$
		shallDocTag = setIt;
	}

	public static void setShallRound5ct(boolean setIt) {
		String round5ctStr = "false"; //$NON-NLS-1$
		if (setIt) {
			round5ctStr = "true"; //$NON-NLS-1$
		}
		setNodeAttribute(settingsDocument, "organization", "round5ct", round5ctStr); //$NON-NLS-1$ //$NON-NLS-2$
		shallRoundTo5ct  = setIt;
	}

	public static void setShallOCR(boolean setIt) {
		String OCRStr = "false"; //$NON-NLS-1$
		if (setIt) {
			OCRStr = "true"; //$NON-NLS-1$
		}
		setNodeAttribute(configurationDocument,
				"scanner", "ocr-enabled", OCRStr); //$NON-NLS-1$ //$NON-NLS-2$
		shallOCR = setIt;
	}

	public static void setDisplaySince(String since) {
		setNodeAttribute(settingsDocument,
				"organization", "displaySince", since); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setDisplayTo(String to) {
		setNodeAttribute(settingsDocument, "organization", "displayTo", to); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setTaxID(String taxID) {
		setNodeAttribute(settingsDocument, "organization", "taxID", taxID); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setVATID(String vatID) {
		setNodeAttribute(settingsDocument, "organization", "vatID", vatID); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setStateListIDX(Integer stateListIDX) {
		setNodeAttribute(settingsDocument,
				"organization", "stateListIDX", Integer.toString(stateListIDX)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setTaxOfficeListIDX(Integer taxOfficeListIDX) {
		setNodeAttribute(
				settingsDocument,
				"organization", "taxOfficeListIDX", Integer.toString(taxOfficeListIDX)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setVATPeriod(String VATPeriod) {
		setNodeAttribute(settingsDocument,
				"organization", "VATperiod", VATPeriod); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setAccountChart(String accChart) {
		setNodeAttribute(settingsDocument,
				"organization", "accountChart", accChart); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setEmailSender(String email) {
		setNodeAttribute(settingsDocument, "email", "sender", email); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setSMTPServer(String server) {
		setNodeAttribute(settingsDocument, "email", "server", server); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setSMTPUsername(String user) {
		setNodeAttribute(settingsDocument, "email", "user", user); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setSMTPPassword(String password) {
		setNodeAttribute(settingsDocument, "email", "password", password); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setUseSMTPAuth(boolean SMTPAuth) {
		String SMTPAuthStringValue;
		if (SMTPAuth) {
			SMTPAuthStringValue = "true"; //$NON-NLS-1$
		} else {
			SMTPAuthStringValue = "false"; //$NON-NLS-1$
		}
		setNodeAttribute(settingsDocument, "email", "auth", SMTPAuthStringValue); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setUseSMTPSSL(boolean SMTPSSL) {
		String SMTPSSLStringValue;
		if (SMTPSSL) {
			SMTPSSLStringValue = "true"; //$NON-NLS-1$
		} else {
			SMTPSSLStringValue = "false"; //$NON-NLS-1$
		}
		setNodeAttribute(settingsDocument, "email", "ssl", SMTPSSLStringValue); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setSendICAL(boolean ICAL) {
		String ICALStringValue;
		if (ICAL) {
			ICALStringValue = "true"; //$NON-NLS-1$
		} else {
			ICALStringValue = "false"; //$NON-NLS-1$
		}
		setNodeAttribute(settingsDocument, "email", "ical", ICALStringValue); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setCtAPI(String ctapi) {
		setNodeAttribute(configurationDocument, "chipcard", "ctapi", ctapi); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setUseCardReaderPINpad(boolean usePad) {
		String usePadStringValue;
		if (usePad) {
			usePadStringValue = "true"; //$NON-NLS-1$
		} else {
			usePadStringValue = "false"; //$NON-NLS-1$
		}

		setNodeAttribute(configurationDocument,
				"chipcard", "readerpinpad", usePadStringValue); //$NON-NLS-1$ //$NON-NLS-2$
	}

	// account code is the bank account number,accountchart is the short name of
	// the accounting chart (kontenrahmen), e.g. SKR03
	public static void setAccountCode(String accountcode) {
		setNodeAttribute(settingsDocument,
				"accounts", "accountcode", accountcode); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setIBAN(String IBAN) {
		setNodeAttribute(settingsDocument, "accounts", "iban", IBAN); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setBIC(String BIC) {
		setNodeAttribute(settingsDocument, "accounts", "bic", BIC); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setBankName(String bankname) {
		setNodeAttribute(settingsDocument, "accounts", "bankname", bankname); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setHolderName(String holdername) {
		setNodeAttribute(settingsDocument, "accounts", "holdername", holdername); //$NON-NLS-1$ //$NON-NLS-2$

	}

	public static void setBankCode(String bankcode) {
		setNodeAttribute(settingsDocument, "accounts", "bankcode", bankcode); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setWebShopURL(String url) {
		if (!url.contains("://")) { //$NON-NLS-1$
			url = "http://" + url; //$NON-NLS-1$
		}
		if (!url.contains("opentrans.php")) { //$NON-NLS-1$
			url = url + "opentrans.php"; //$NON-NLS-1$
		}
		setNodeAttribute(settingsDocument, "webshop", "url", url); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setWebShopUser(String user) {
		setNodeAttribute(settingsDocument, "webshop", "user", user); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setWebShopPassword(String password) {
		setNodeAttribute(settingsDocument, "webshop", "password", password); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static void setAcknowledgedQuickstart(boolean isAcknowledged) {
		setNodeAttribute(configurationDocument,
				"acknowledged", "quickstart", isAcknowledged ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	public static void setAcknowledgedDragndrop(boolean isAcknowledged) {
		setNodeAttribute(configurationDocument,
				"acknowledged", "dragndrop", isAcknowledged ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	public static void writeConfiguration() {
		try {
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			DOMSource source = new DOMSource(configurationDocument);
			FileOutputStream os = new FileOutputStream(new File(
					client.getConfigFilename()));
			StreamResult result = new StreamResult(os);
			transformer.transform(source, result);
		} catch (TransformerException ex) {
			ex.printStackTrace();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (TransformerFactoryConfigurationError ex) {
			ex.printStackTrace();
		}
	}

	public static void writeSettings() {
		try {
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			DOMSource source = new DOMSource(settingsDocument);
			FileOutputStream os = new FileOutputStream(new File(
					client.getSettingsFilename()));
			StreamResult result = new StreamResult(os);

			transformer.transform(source, result);
			os.flush();
			os.close();
			
		} catch (TransformerException ex) {
			ex.printStackTrace();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (TransformerFactoryConfigurationError ex) {
			ex.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean hasCreatedConfigFile() {
		return createdConfigFile;
	}

	public static int getTaxmodeID() {
		if (taxModeID == null) {
			readSettings();
		}
		if (taxModeID == null) { // no configuration there
			return 0;// default taxmode SKR03
		}
		return taxModeID;

	}

	public static String getTaxmode() {
		return getTaxmodeOptions()[getTaxmodeID()];
	}

	public static String[] getTaxmodeOptions() {
		return taxModeOptions;
	}

	public static int getTaxmodeIssueID() {
		return 0;
	}

	public static boolean isTaxmodeIssue() {
		return getTaxmodeID() == getTaxmodeIssueID();
	}

	public static void setCreditorID(String creditorID) {
		setNodeAttribute(settingsDocument, "accounts", "creditorid", creditorID); //$NON-NLS-1$ //$NON-NLS-2$

	}

	public static String getCreditorID() {
		if (creditorID == null) {
			readSettings();
		}
		if (creditorID == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return creditorID;
	}

	public static void setOCRlangPath(String path) {
		setNodeAttribute(configurationDocument,
				"scanner", "ocr-language-path", path); //$NON-NLS-1$ //$NON-NLS-2$

	}

	public static String getOrganisationStreet() {
		if (organisationStreet == null) {
			readSettings();
		}
		if (organisationStreet == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return organisationStreet;
	}

	public static String getOrganisationZip() {
		if (organisationZip == null) {
			readSettings();
		}
		if (organisationZip == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return organisationZip;
	}

	public static String getOrganisationLocation() {
		if (organisationLocation == null) {
			readSettings();
		}
		if (organisationLocation == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return organisationLocation;
	}

	public static String getOrganisationCountry() {
		if (organisationCountry == null) {
			readSettings();
		}
		if (organisationCountry == null) { // no configuration there
			return ""; //$NON-NLS-1$
		}
		return organisationCountry;
	}

	public static void setOrganizationStreet(String text) {
		setNodeAttribute(settingsDocument, "organization", "street", text); //$NON-NLS-1$ //$NON-NLS-2$

	}

	public static void setOrganizationZip(String text) {
		setNodeAttribute(settingsDocument, "organization", "zip", text); //$NON-NLS-1$ //$NON-NLS-2$

	}

	public static void setOrganizationLocation(String text) {
		setNodeAttribute(settingsDocument, "organization", "location", text); //$NON-NLS-1$ //$NON-NLS-2$

	}

	public static void setOrganizationCountry(String text) {
		setNodeAttribute(settingsDocument, "organization", "country", text); //$NON-NLS-1$ //$NON-NLS-2$

	}

	public static void setSalesTax(boolean selection) {
		String salesTaxStr = "false"; //$NON-NLS-1$
		if (selection) {
			salesTaxStr = "true"; //$NON-NLS-1$
		}
		setNodeAttribute(settingsDocument,
				"organization", "salesTax", salesTaxStr); //$NON-NLS-1$ //$NON-NLS-2$
		hasSalesTax = selection;
	}

	public static boolean hasSalesTax() {
		if (hasSalesTax == null) {
			readSettings();
		}
		if (hasSalesTax == null) { // no configuration there
			return false; // default
		}
		return hasSalesTax.booleanValue();
	}

	public static boolean shallRoundTo5ct() {
		if (shallRoundTo5ct == null) {
			readSettings();
		}
		if (shallRoundTo5ct == null) { // no configuration there
			return false; // default
		}
		return shallRoundTo5ct.booleanValue();
	}

}
