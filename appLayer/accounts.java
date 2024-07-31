package appLayer;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dataLayer.DB;

/**
 * this provides a list of all accounts
 * */
public class accounts implements Serializable {
	private class XMLImporter extends Thread implements IRunnableWithProgress {

		private Integer chartToImport = null;

		@Override
		public void run(IProgressMonitor ipm) throws InvocationTargetException,
				InterruptedException {

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();

			DocumentBuilder builder;
			Document document = null;

			try {
				builder = factory.newDocumentBuilder();

				InputStream accountXMLStream = getClass().getResourceAsStream(
						"/init/accounts.xml"); //$NON-NLS-1$
				if (accountXMLStream == null) {
					System.err
							.println(Messages
									.getString("accountsList.accountDefinitionFileMissing")); //$NON-NLS-1$
					return;
				}
				String code = "", description = ""; //$NON-NLS-1$ //$NON-NLS-2$
				int type = 0, subAccounts = 0;
				int id = 0;

				document = builder.parse(accountXMLStream);
				NodeList chartNodeList = document.getElementsByTagName("chart"); //$NON-NLS-1$
				ipm.beginTask(
						Messages.getString("accounts.taskLabelCreateAccountCharts"), chartNodeList.getLength() * 100); //$NON-NLS-1$
				ipm.subTask(Messages
						.getString("accounts.subtaskLabelCreatingAccounts")); //$NON-NLS-1$
				for (int chartIndex = 0; chartIndex < chartNodeList.getLength(); chartIndex++) {
					Node chartNode = chartNodeList.item(chartIndex);
					if (chartNode.getNodeType() == Node.ELEMENT_NODE) {
						NamedNodeMap chartAttributes = chartNode
								.getAttributes();
						accountChart acChart = new accountChart(chartAttributes
								.getNamedItem("name").getNodeValue(), //$NON-NLS-1$
								chartAttributes.getNamedItem("description") //$NON-NLS-1$
										.getNodeValue(), chartAttributes
										.getNamedItem("vatin").getNodeValue(), //$NON-NLS-1$
								chartAttributes.getNamedItem("vatout") //$NON-NLS-1$
										.getNodeValue(), chartAttributes
										.getNamedItem("vatonhold") //$NON-NLS-1$
										.getNodeValue());
						acChart.save();
						/*
						 * account
						 * templateAccount=account.getNewAccount(client.getAccounts
						 * ().getCurrentChart());
						 * templateAccount.setPlaceholderForNewAccount(true);
						 * acChart.addAccount(templateAccount);
						 */
						Element chartElement = (Element) chartNode;
						NodeList accountNodeList = chartElement
								.getElementsByTagName("account"); //$NON-NLS-1$
						account newA = account.getNewAccount(acChart);
						newA.setPlaceholderForNewAccount(true);
						newA.save();
						acChart.addAccount(newA);
						acChart.save();
						for (int accountIndex = 0; accountIndex < accountNodeList
								.getLength(); accountIndex++) {
							Node accountNode = accountNodeList
									.item(accountIndex);
							Boolean assetsDeductable = Boolean.valueOf(false);
							boolean autoVAT = false;
							// if there is a attribute in the tag number:value
							NamedNodeMap accountAttributes = accountNode
									.getAttributes();
							if (accountAttributes.getNamedItem("code") != null) //$NON-NLS-1$
							// if there is a attribute "code" in the tag printer
							{
								code = accountAttributes
										.getNamedItem("code").getNodeValue(); //$NON-NLS-1$
							}
							if (accountAttributes.getNamedItem("type") != null) //$NON-NLS-1$
							// if there is a attribute "type" in the tag printer
							{
								type = account
										.getTypeIDForString(accountAttributes
												.getNamedItem("type").getNodeValue()); //$NON-NLS-1$
							}

							if (accountAttributes.getNamedItem("refersTo") != null) //$NON-NLS-1$
							// if there is a attribute "type" in the tag printer
							{
								subAccounts = account
										.getSubAccountsTypeIDForString(accountAttributes
												.getNamedItem("refersTo").getNodeValue()); //$NON-NLS-1$
							}
							if (accountAttributes.getNamedItem("automatic") != null) //$NON-NLS-1$
							// if there is a attribute "type" in the tag printer
							{
								autoVAT=true;
							}

							if (accountAttributes
									.getNamedItem("assetsDeductable") != null) //$NON-NLS-1$
							{
								assetsDeductable = Boolean.valueOf(
										accountAttributes
												.getNamedItem(
														"assetsDeductable").getNodeValue()); //$NON-NLS-1$
							}

							if (accountAttributes.getNamedItem("description") != null) //$NON-NLS-1$
							// if there is a attribute "description" in the tag
							// printer
							{
								description = accountAttributes.getNamedItem(
										"description").getNodeValue(); //$NON-NLS-1$
							}

							int chart = Integer.valueOf(accountNode
									.getParentNode().getAttributes()
									.getNamedItem("id").getNodeValue()); //$NON-NLS-1$
							if ((chartToImport == null)
									|| (chartToImport == chart)) {
								account currentAccount = new account(id, code,
										description, type, subAccounts);
								if (accountIndex == 0) {
									currentAccount.startSession();
								}
								currentAccount.setParent(acChart); //$NON-NLS-1$
								if (autoVAT) {
									currentAccount.setAutoVAT(true);
								}
								currentAccount
										.setAssetsDeductable(assetsDeductable);
								id++;
								currentAccount.saveInSession();
								acChart.addAccount(currentAccount);
								if (accountIndex == accountNodeList.getLength() - 1) {
									currentAccount.endSession();
									acChart.save();
								}
							}
							// progress will be internally (only for progress
							// bar progress) represented as percent complete per
							// account chart, i.e. 0-300 for 3 acct charts
							ipm.worked(chartIndex
									* 100
									+ (accountNodeList.getLength() / (accountIndex + 1))
									* 100);// accountIndex+1 to avoid division
											// by zero
						}
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		public void setChartToImport(int chart) {
			chartToImport = chart;
		}

	}

	private static final long serialVersionUID = 1L;
	private static Vector<accountChart> accountCharts = new Vector<accountChart>();

	public accounts() {
	}

	public void getAccountsFromDatabase() {
		accountCharts.clear();
		getFromDB();

	}

	public static Vector<accountChart> getAccountCharts() {
		return accountCharts;

	}

	public static String[] getAccountChartOptions() {
		String[] al = new String[getAccountCharts().size()];
		int accIdx = 0;
		for (accountChart currentAcc : getAccountCharts()) {
			al[accIdx] = currentAcc.getName();
			accIdx++;
		}
		return al;
	}

	private void getFromDB() {
		List<accountChart> acRetrievals = DB.getEntityManager()
				.createQuery("SELECT a FROM accountChart a").getResultList(); //$NON-NLS-1$
		for (Iterator<accountChart> iter = acRetrievals.iterator(); iter
				.hasNext();) {
			accountChart currentlyRetrieved = (accountChart) iter.next();
			currentlyRetrieved.removeDeletedAccounts();
			accountCharts.add(currentlyRetrieved);
		}
	}

	private int getCurrentChartIndex() {
		String[] accountChartNames = getAccountChartOptions();

		for (int chartIndex = 0; chartIndex < accountChartNames.length; chartIndex++) {
			if (accountChartNames[chartIndex]
					.indexOf(configs.getAccountChart()) == 0) {
				return chartIndex;
			}
		}
		return -1;
	}

	public accountChart getCurrentChart() {
		int chartIdx = getCurrentChartIndex();
		if ((chartIdx < 0)
				|| (chartIdx > accountCharts.size() || (accountCharts.size() == 0))) {
			return null;
		}
		return accountCharts.get(chartIdx);

	}

	public boolean isEmptyPeriod() {
		return false;
		// getEntriesFromDatabase();
		// return journal.isEmpty();
	}

	public account getDefaultAccount() {
		// will return "new account"
		return (account) accountCharts.get(getCurrentChartIndex())
				.getAccounts().get(0);
	}

	public account getFirstSelectableAccount() {
		// will return the first account in the selected account chart
		return (account) accountCharts.get(getCurrentChartIndex())
				.getAccounts().get(1);
	}

	public account getCashAccount() throws AccountNotFoundException {
		if (configs.getAccountChart().equals("SKR04")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("1600"); //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("SKR03")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("1000"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("Jes")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("700"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else {
			return getCurrentChart().getAccountForCode("1.2.3"); //$NON-NLS-1$
		}
	}

	public account getBankAccount() throws AccountNotFoundException {
		if (configs.getAccountChart().equals("SKR04")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("1800"); //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("SKR03")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("1200"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("Jes")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("710"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else {
			return getCurrentChart().getAccountForCode("1.2.1"); //$NON-NLS-1$
		}
	}

	public account getOfficialEntertainmentAccount() throws AccountNotFoundException {
		if (configs.getAccountChart().equals("SKR04")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("6640"); //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("SKR03")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("4650"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("Jes")) { //$NON-NLS-1$
			throw new AccountNotFoundException(Messages.getString("accounts.accountNotDefined")); //$NON-NLS-1$
//			return getCurrentChart().getAccountForCode("710"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else {
			throw new AccountNotFoundException(Messages.getString("accounts.accountNotDefined")); //$NON-NLS-1$
//			return getCurrentChart().getAccountForCode("1.2.1"); //$NON-NLS-1$
		}
	}
	
	public account getNonDeductibleExpensesAccount() throws AccountNotFoundException {
		if (configs.getAccountChart().equals("SKR04")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("6645"); //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("SKR03")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("4655"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("Jes")) { //$NON-NLS-1$
			throw new AccountNotFoundException(Messages.getString("accounts.accountNotDefined")); //$NON-NLS-1$
//			return getCurrentChart().getAccountForCode("710"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else {
			throw new AccountNotFoundException(Messages.getString("accounts.accountNotDefined")); //$NON-NLS-1$
//			return getCurrentChart().getAccountForCode("1.2.1"); //$NON-NLS-1$
		}
		
	}
	
	public account getReceivablesAccount() throws AccountNotFoundException {
		if (configs.getAccountChart().equals("SKR04")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("1200"); //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("SKR03")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("1400"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("Jes")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("720"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else {
			return getCurrentChart().getAccountForCode("1.1"); //$NON-NLS-1$
		}
	}

	public account getPersonalDrawAccount() throws AccountNotFoundException {
		if (configs.getAccountChart().equals("SKR04")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("2100"); //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("SKR03")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("1800"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("Jes")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("610"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else {
			return getCurrentChart().getAccountForCode("2.3"); //$NON-NLS-1$
		}

	}

	public account getLiablilitiesAccount() throws AccountNotFoundException {
		if (configs.getAccountChart().equals("SKR04")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("3300"); //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("SKR03")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("1600"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("Jes")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("1600"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else {
			return getCurrentChart().getAccountForCode("730"); //$NON-NLS-1$
		}
	}

	public account getPayableAccount() throws AccountNotFoundException {
		if (configs.getAccountChart().equals("SKR04")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("5900"); //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("SKR03")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("3100"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("Jes")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("120"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else {
			return getCurrentChart().getAccountForCode("3.14"); //$NON-NLS-1$
		}
	}

	public account getRevenuesAccount() throws AccountNotFoundException {
		if (configs.getAccountChart().equals("SKR04")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("4400"); //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("SKR03")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("8400"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("Jes")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("500"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else {
			return getCurrentChart().getAccountForCode("4.2"); //$NON-NLS-1$
		}
	}

	public account getInputTaxAccount() throws AccountNotFoundException {
		if (configs.getAccountChart().equals("SKR04")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("1406"); //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("SKR03")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("1576"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("Jes")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("498"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else {
			return getCurrentChart().getAccountForCode("3.23.10"); //$NON-NLS-1$
		}
	}

	public account getUndueVatAccount() throws AccountNotFoundException {
		if (configs.getAccountChart().equals("SKR04")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("3810"); //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("SKR03")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("1766"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("Jes")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("997"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else {
			return getCurrentChart().getAccountForCode("3.23.11"); //$NON-NLS-1$
		}
	}

	public account getTurnoverTaxAccount() throws AccountNotFoundException {
		if (configs.getAccountChart().equals("SKR04")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("3806"); //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("SKR03")) { //$NON-NLS-1$
			// sollbesteuerung
			return getCurrentChart().getAccountForCode("1776"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("Jes")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("998"); //$NON-NLS-1$
		} else {
			return getCurrentChart().getAccountForCode("3.23.9"); //$NON-NLS-1$
		}
	}

	public account getGoodsAccount()  throws AccountNotFoundException {
		if (configs.getAccountChart().equals("SKR04")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("5400"); //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("SKR03")) { //$NON-NLS-1$
			// sollbesteuerung
			return getCurrentChart().getAccountForCode("3400"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("Jes")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode(""); //$NON-NLS-1$
		} else {
			return getCurrentChart().getAccountForCode(""); //$NON-NLS-1$
		}
	}

	public account getProductsAccount()  throws AccountNotFoundException {
		if (configs.getAccountChart().equals("SKR04")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("3806"); //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("SKR03")) { //$NON-NLS-1$
			// sollbesteuerung
			return getCurrentChart().getAccountForCode("1776"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("Jes")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("998"); //$NON-NLS-1$
		} else {
			return getCurrentChart().getAccountForCode("3.23.9"); //$NON-NLS-1$
		}
	}

	public account getRawMaterialsAccount()  throws AccountNotFoundException {
		if (configs.getAccountChart().equals("SKR04")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("3806"); //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("SKR03")) { //$NON-NLS-1$
			// sollbesteuerung
			return getCurrentChart().getAccountForCode("1776"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("Jes")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("998"); //$NON-NLS-1$
		} else {
			return getCurrentChart().getAccountForCode("3.23.9"); //$NON-NLS-1$
		}
	}

	public account getUnfinishedManufacturesAccount()  throws AccountNotFoundException {
		if (configs.getAccountChart().equals("SKR04")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("3806"); //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("SKR03")) { //$NON-NLS-1$
			// sollbesteuerung
			return getCurrentChart().getAccountForCode("1776"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("Jes")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("998"); //$NON-NLS-1$
		} else {
			return getCurrentChart().getAccountForCode("3.23.9"); //$NON-NLS-1$
		}
	}



	public account getYearClosingAccount() throws AccountNotFoundException {
		if (configs.getAccountChart().equals("SKR04")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("9000"); //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("SKR03")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("9000"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else if (configs.getAccountChart().equals("Jes")) { //$NON-NLS-1$
			return getCurrentChart().getAccountForCode("900"); // this is hard-coded SKR03 //$NON-NLS-1$
		} else {
			return getCurrentChart().getAccountForCode("2.1"); //$NON-NLS-1$
		}
	}

	public String[] getStringArray(boolean includeNewAccount) {
		
		List<account> accts = accountCharts.get(getCurrentChartIndex())
				.getAccounts(includeNewAccount);
		String[] accountsStringArray = new String[accts.size()];
		for (int accountIndex = 0; accountIndex < accts.size(); accountIndex++) {
			accountsStringArray[accountIndex] = accts.get(accountIndex)
					.toString();
		}
		return accountsStringArray;
	}

	public List<account> getList(boolean includeNewAccount) {
		if (!includeNewAccount) {
			return accountCharts
					.get(getCurrentChartIndex())
					.getAccounts()
					.subList(
							1,
							accountCharts.get(getCurrentChartIndex())
									.getAccounts().size());
		} else {
			return accountCharts.get(getCurrentChartIndex()).getAccounts();
		}

	}

	public account getAccountByFullString(String codeAndName) {
		List<account> accts = accountCharts.get(getCurrentChartIndex()).getAccounts();
		for (int accountIndex = 0; accountIndex < accts.size(); accountIndex++) {
			account a = (account) accts.get(accountIndex);
			if (a.toString().equalsIgnoreCase(codeAndName)) {
				return a;
			}
		}
		return null;

	}

	public account getAccountByCode(String code) {
		List<account> accts = accountCharts.get(getCurrentChartIndex()).getAccounts();
		for (int accountIndex = 0; accountIndex < accts.size(); accountIndex++) {
			account a = (account) accts.get(accountIndex);
			if (a.getCode().equalsIgnoreCase(code)) {
				return a;
			}
		}
		return null;

	}

	public int getChartSize(int chart) {
		return accountCharts.get(chart).getAccounts().size();
	}

	public int getChartSize() {
		return accountCharts.get(getCurrentChartIndex()).getAccounts().size();
	}

	public boolean empty() {
		return accountCharts.size() == 0;
	}

	public boolean empty(int chart) {
		if (accountCharts.size() == 0) {
			return true;
		}
		return ((accountCharts.size() < chart) || (accountCharts.get(0)
				.getAccounts().size() == 0));
	}

	public int getNumCharts() {
		// the accounts are saved in an array of vectors of accounts -- the
		// vectors are the contents of the accounting chart
		// --> return the length of the array
		return accountCharts.size();
	}

	public void importAccountingChartFromXML(Shell sh, int chart) {
		XMLImporter xmli = new XMLImporter();
		xmli.setChartToImport(chart);
		xmli.run();

		try {
			new ProgressMonitorDialog(sh).run(true, true, xmli);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * this will crete the account charts, create the <new> accounts in those
	 * charts, read the account definition and persist the accounts
	 * */
	public void importAccountsFromXML(Shell sh) {
		XMLImporter xmli = new XMLImporter();

		try {
			new ProgressMonitorDialog(sh).run(true, true, xmli);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public int getNumAccounts() {
		int num = 0;
		for (accountChart currentAccounts : accountCharts) {
			num += currentAccounts.getAccounts().size();
		}
		return num;
	}

}
