package appLayer;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import appLayer.taxRelated.taxList;
import appLayer.transactionRelated.transactions;

/*
 * This refers to the client of the application.
 * Customers are "contacts"
 * */
public class client {
	private static int clientNr = 0;
	static private DecimalFormat formatter = new DecimalFormat("0000"); //$NON-NLS-1$
	static private transactions transactionController = null;
	static private taxList allTaxes = null;
	static private contacts allContacts = null;
	static private accounts allAccounts = null;
	static private entries allEntries = null;
	static private products allProducts = null;
	static private assets allAssets = null;
	static private inventories allInventories = null;
	static private transactionsFromBankAccountImport importQueue = null;

	static private documents documents = null;
	static private String configPath = null;
	static private String dataPath = null;
	static private String globalDataPath = null;

	static private final String clientPath = formatter.format(clientNr) + "/"; //$NON-NLS-1$

	public static String getClientPath() {
		return clientPath;
	}

	public static void setConfigPath(String configPath) {
		client.configPath = configPath;

		globalDataPath = configPath;
		dataPath = configPath + getClientPath();

	}

	public static String getConfigPath() {
		return configPath;
	}

	public static String getGlobalDataPath() {
		return globalDataPath;
	}

	public static String getDataPath() {
		return dataPath;
	}

	public static int getClient() {
		return clientNr;
	}

	public static String getConfigFilename() {
		return getConfigPath() + "config.xml"; //$NON-NLS-1$
	}

	public static String getSettingsFilename() {
		return getDataPath() + "settings.xml"; //$NON-NLS-1$
	}

	public static String getInterfaceDir() {
		return getDataPath() + "interface/"; //$NON-NLS-1$
	}

	public static String getInterfaceProcessedDir() {
		return getInterfaceDir() + "processed/"; //$NON-NLS-1$
	}

	public static String getDocumentPath() {
		return getDataPath() + "documents/"; //$NON-NLS-1$
	}

	/**
	 * this will create the document path if it's not yet there
	 * */
	public static void prepareDocumentsPath() {
		if (!new File(getDocumentPath()).exists()) {
			new File(getDocumentPath()).mkdirs();
		}
	}

	public static String getNextScanFilename() {
		prepareDocumentsPath();
		SimpleDateFormat sdf = new SimpleDateFormat(
				Messages.getString("client.dateFormatInFilename")); //$NON-NLS-1$
		int scanIndex = 0;
		String currentFilename;
		do {
			scanIndex++;
			currentFilename = Messages
					.getString("client.scanFilenameFormatPrefix") + sdf.format(new Date()) + scanIndex + ".png"; //$NON-NLS-1$ //$NON-NLS-2$

		} while (new File(getDocumentPath() + currentFilename).exists());
		return getDocumentPath() + currentFilename;
	}

	public static transactions getTransactions() {
		if (transactionController == null) {
			transactionController = new transactions();
		}
		return transactionController;
	}

	public static documents getDocuments() {
		if (documents == null) {
			documents = new documents();
		}
		return documents;
	}

	public static transactionsFromBankAccountImport getImportQueue() {
		if (importQueue == null) {
			importQueue = new transactionsFromBankAccountImport();
			importQueue.load();
		}
		return importQueue;
	}

	public static taxList getTaxes() {
		if (allTaxes == null) {
			allTaxes = new taxList();
			allTaxes.getTaxesFromDatabase();
		}
		return allTaxes;
	}
	

	public static accounts getAccounts() {
		if (allAccounts == null) {
			allAccounts = new accounts();
			allAccounts.getAccountsFromDatabase();
		}
		return allAccounts;

	}

	public static inventories getInventories() {
		if (allInventories == null) {
			allInventories = new inventories();
		}
		return allInventories;
	}

	public static entries getEntries() {
		if (allEntries == null) {
			allEntries = new entries();
		}
		return allEntries;

	}

	public static assets getAssets() {
		if (allAssets == null) {
			allAssets = new assets();
			allAssets.getAssetsFromDB();

		}
		return allAssets;
	}

	public static contacts getContacts() {
		if (allContacts == null) {
			allContacts = new contacts();
			allContacts.getContactsFromDB();
		}
		return allContacts;
	}

	public static products getProducts() {
		if (allProducts == null) {
			allProducts = new products();
			allProducts.getProductsFromDB();
		}
		return allProducts;
	}

}
