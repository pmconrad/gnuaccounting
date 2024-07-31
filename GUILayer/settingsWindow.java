package GUILayer;

import java.math.BigDecimal;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.kapott.hbci.manager.HBCIUtils;

import appLayer.accounts;
import appLayer.application;
import appLayer.client;
import appLayer.configs;
import appLayer.taxRelated.IRSoffices;
import appLayer.taxRelated.states;
import appLayer.taxRelated.tax;
import appLayer.taxRelated.taxNotFoundException;
import dataLayer.HBCI;

class warningFocusAdapter extends FocusAdapter {
	boolean warned = false;
	Shell sh = null;

	public warningFocusAdapter(Shell sh) {
		warned = false;
		this.sh = sh;
	}

	public void focusGained(final FocusEvent e) {
		if (!warned) {
			MessageDialog
					.openWarning(
							sh,
							Messages.getString("settingsWindow.securityWarningCaption"), Messages.getString("settingsWindow.passwordStoredPlaintext")); //$NON-NLS-1$ //$NON-NLS-2$
			warned = true;
		}
	}
}

public class settingsWindow extends ApplicationWindow {

	private Text txtHoldername;
	private Text txtCreditorID;
	private Text txtBankaccount;
	private Text txtIBAN;
	private Text txtBIC;
	private Text txtBankcode;
	private Text txtBankname;
	private Button chkSMTPAuth;
	private Text txtSMTPemail;
	private Text txtSMTPpassword;
	private Text txtSMTPUser;
	private Text txtSMTPServer;
	private Combo cmbVATperiod;
	private Button chkVATexempt;
	private Button chkSalesTax;
	private Button chkDocTag;
	private Button chkRoundt5ct;
	private Combo cmbPeriodFrom;
	private ComboViewer cmbIRS;
	private Text txtOrganizationName;
	private Text txtOrganizationStreet;
	private Text txtOrganizationZip;
	private Text txtOrganizationLocation;
	private Text txtOrganizationCountry;

	private ComboViewer cmbState;
	private Combo cmbChart;
	private Combo cmbTaxmode;
	private Text txtTaxID;
	private Text txtVatID;
	private Label bankCodeLabel;
	private Label bankAccountLabel;
	private Label IBANLabel;
	private Label BICLabel;
	private Button chkSMTPSSL;
	private Button chkICAL;
	private Text txtWebShopURL;
	private Text txtWebShopUser;
	private Text txtWebShopPassword;
	private ComboViewer cmbViewerDefaultVAT; 
	/**
	 * Create the application window
	 */
	public settingsWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	private void checkBankDetails() {
		Color black = new Color(getShell().getDisplay(), 0x00, 0x00, 0x00);
		Color red = new Color(getShell().getDisplay(), 0x80, 0x00, 0x00);
		Color green = new Color(getShell().getDisplay(), 0x00, 0x80, 0x00);
		// we need to invoke HBCI.getInstance otherwise the static HBCIUtils
		// functions won't be able to
		// connect to their ressources (like localized strings)
		HBCI.getInstance(getShell());
		/***
		 * this is a hack to be able to hide real account data in live demos by entering "hide", then the real IBAN 
		 */
		if ((txtIBAN.getText().equals("hide")) || (txtBIC.getText().equals("hide")) || (txtHoldername.getText().equals("hide"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (txtIBAN.getText().equals("hide")) {txtBankcode.setText("");} //$NON-NLS-1$ //$NON-NLS-2$
			if (txtBIC.getText().equals("hide")) {txtBankaccount.setText("");} //$NON-NLS-1$ //$NON-NLS-2$
			if (txtHoldername.getText().equals("hide")) {txtHoldername.setText("");} //$NON-NLS-1$ //$NON-NLS-2$
			txtBankname.setEchoChar('*');
			txtIBAN.setEchoChar('*');
			txtBIC.setEchoChar('*');
			txtHoldername.setEchoChar('*');
		}

		IBANLabel.setForeground(black);
		txtBankname.setText(""); //$NON-NLS-1$
		if (txtIBAN.getText().length() > 0) {
			IBANLabel.setForeground(red);
			if (txtIBAN.getText().contains(" ")) { //$NON-NLS-1$
				txtIBAN.setText(txtIBAN.getText().replaceAll(" ", "")); //$NON-NLS-1$ //$NON-NLS-2$
				txtIBAN.setSelection(txtIBAN.getText().length());
			}
		}
		if ((txtIBAN.getText().length() > 12)
				&& (txtIBAN.getText().startsWith("DE"))) { //$NON-NLS-1$
// find out bank names for german IBANs
			String blz=txtIBAN.getText().substring(4, 12);
			
			String bankName = HBCIUtils.getNameForBLZ(blz);
			if (bankName.length() != 0) {
				txtBankname.setText(bankName);
				// if bank code is not correct, account code can neither be
				// checked nor be correct
				
			}

		}
		
		if ((txtIBAN.getText().length()>5)&&(HBCIUtils.checkIBANCRC(txtIBAN.getText()))) {
			IBANLabel.setForeground(green);
		}


	}

	/**
	 * Create contents of the application window
	 * 
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		parent.getShell().setSize(getInitialSize());

		Composite container = new Composite(parent, SWT.NONE);

		// use SWT grid layout with 1 column for the container of the tabs
		GridLayout mainGridLayout = new GridLayout();
		mainGridLayout.numColumns = 1;
		container.setLayout(mainGridLayout);
		GridData mainGridData;

		// SWT tab folder
		final TabFolder tabFolder = new TabFolder(container, SWT.NONE);
		mainGridData = new GridData();
		mainGridData.horizontalAlignment = SWT.FILL;
		mainGridData.verticalAlignment = SWT.FILL;
		mainGridData.grabExcessHorizontalSpace = true;
		mainGridData.grabExcessVerticalSpace = true;
		tabFolder.setLayoutData(mainGridData);

		final TabItem configCompanyTabItem = new TabItem(tabFolder, SWT.NONE);
		configCompanyTabItem.setText(Messages
				.getString("settingsWindow.Company")); //$NON-NLS-1$
		final Composite configCompanyComposite = new Composite(tabFolder,
				SWT.NONE);
		configCompanyTabItem.setControl(configCompanyComposite);

		final TabItem configSMTPTabItem = new TabItem(tabFolder, SWT.NONE);
		configSMTPTabItem.setText(Messages
				.getString("settingsWindow.Mail_server")); //$NON-NLS-1$
		final Composite configSMTPComposite = new Composite(tabFolder, SWT.NONE);
		configSMTPTabItem.setControl(configSMTPComposite);

		final TabItem configBankTabItem = new TabItem(tabFolder, SWT.NONE);
		configBankTabItem.setText(Messages
				.getString("settingsWindow.Bank_account")); //$NON-NLS-1$
		final Composite configBankComposite = new Composite(tabFolder, SWT.NONE);
		configBankTabItem.setControl(configBankComposite);

		final TabItem configWebShopTabItem = new TabItem(tabFolder, SWT.NONE);
		configWebShopTabItem.setText(Messages
				.getString("settingsWindow.Web_shop")); //$NON-NLS-1$
		final Composite configWebShopComposite = new Composite(tabFolder,
				SWT.NONE);
		configWebShopTabItem.setControl(configWebShopComposite);

		// use SWT grid layout with 2 columns
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.verticalSpacing = 10;
		gridLayout.marginTop = 20;
		configCompanyComposite.setLayout(gridLayout);

		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 2;
		gridLayout1.verticalSpacing = 10;
		gridLayout1.marginTop = 20;
		configSMTPComposite.setLayout(gridLayout1);

		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 2;
		gridLayout2.verticalSpacing = 10;
		gridLayout2.marginTop = 20;
		configBankComposite.setLayout(gridLayout2);

		GridLayout gridLayout3 = new GridLayout();
		gridLayout3.numColumns = 2;
		gridLayout3.verticalSpacing = 10;
		gridLayout3.marginTop = 20;
		configWebShopComposite.setLayout(gridLayout3);

		GridData gridDataLeft = new GridData();
		gridDataLeft.widthHint=220;
		GridData gridDataRight = new GridData();

		gridDataLeft.horizontalAlignment = SWT.RIGHT;

		gridDataRight.horizontalAlignment = SWT.FILL;
		gridDataRight.grabExcessHorizontalSpace = true;

		// widgets in the company tab

		final Label organizationNameLabel = new Label(configCompanyComposite,
				SWT.NONE);
		organizationNameLabel.setText(Messages
				.getString("settingsWindow.orgaName")); //$NON-NLS-1$
		organizationNameLabel.setAlignment(SWT.RIGHT);
		organizationNameLabel.setLayoutData(gridDataLeft);
		organizationNameLabel.setFont(configs.getDefaultFont());

		txtOrganizationName = new Text(configCompanyComposite, SWT.BORDER);
		txtOrganizationName.setText(configs.getOrganisationName());
		txtOrganizationName.setLayoutData(gridDataRight);
		txtOrganizationName.setFont(configs.getDefaultFont());


		final Label organizationStreetLabel = new Label(configCompanyComposite,
				SWT.NONE);
		organizationStreetLabel.setText(Messages.getString("settingsWindow.street"));  //$NON-NLS-1$
		organizationStreetLabel.setAlignment(SWT.RIGHT);
		organizationStreetLabel.setLayoutData(gridDataLeft);
		organizationStreetLabel.setFont(configs.getDefaultFont());

		txtOrganizationStreet = new Text(configCompanyComposite, SWT.BORDER);
		txtOrganizationStreet.setText(configs.getOrganisationStreet());
		txtOrganizationStreet.setLayoutData(gridDataRight);
		txtOrganizationStreet.setFont(configs.getDefaultFont());

		

		final Label organizationZipLabel = new Label(configCompanyComposite,
				SWT.NONE);
		organizationZipLabel.setText(Messages.getString("settingsWindow.ZIP"));  //$NON-NLS-1$
		organizationZipLabel.setAlignment(SWT.RIGHT);
		organizationZipLabel.setLayoutData(gridDataLeft);
		organizationZipLabel.setFont(configs.getDefaultFont());

		txtOrganizationZip = new Text(configCompanyComposite, SWT.BORDER);
		txtOrganizationZip.setText(configs.getOrganisationZip());
		txtOrganizationZip.setLayoutData(gridDataRight);
		txtOrganizationZip.setFont(configs.getDefaultFont());
		

		final Label organizationLocationLabel = new Label(configCompanyComposite,
				SWT.NONE);
		organizationLocationLabel.setText(Messages.getString("settingsWindow.city"));  //$NON-NLS-1$
		organizationLocationLabel.setAlignment(SWT.RIGHT);
		organizationLocationLabel.setLayoutData(gridDataLeft);
		organizationLocationLabel.setFont(configs.getDefaultFont());

		txtOrganizationLocation = new Text(configCompanyComposite, SWT.BORDER);
		txtOrganizationLocation.setText(configs.getOrganisationLocation());
		txtOrganizationLocation.setLayoutData(gridDataRight);
		txtOrganizationLocation.setFont(configs.getDefaultFont());

		final Label organizationCountryLabel = new Label(configCompanyComposite,
				SWT.NONE);
		organizationCountryLabel.setText(Messages.getString("settingsWindow.country"));  //$NON-NLS-1$
		organizationCountryLabel.setAlignment(SWT.RIGHT);
		organizationCountryLabel.setLayoutData(gridDataLeft);
		organizationCountryLabel.setFont(configs.getDefaultFont());

		txtOrganizationCountry = new Text(configCompanyComposite, SWT.BORDER);
		txtOrganizationCountry.setText(configs.getOrganisationCountry());
		txtOrganizationCountry.setLayoutData(gridDataRight);
		txtOrganizationCountry.setFont(configs.getDefaultFont());

		
		
		final Label taxIdLabel = new Label(configCompanyComposite, SWT.NONE);
		taxIdLabel.setText(Messages.getString("settingsWindow.taxID")); //$NON-NLS-1$
		taxIdLabel.setAlignment(SWT.RIGHT);
		taxIdLabel.setLayoutData(gridDataLeft);
		taxIdLabel.setFont(configs.getDefaultFont());

		txtTaxID = new Text(configCompanyComposite, SWT.BORDER);
		txtTaxID.setText(configs.getTaxID());
		txtTaxID.setLayoutData(gridDataRight);
		txtTaxID.setFont(configs.getDefaultFont());

		final Label vatIdLabel = new Label(configCompanyComposite, SWT.NONE);
		vatIdLabel.setText(Messages.getString("settingsWindow.VATID"));  //$NON-NLS-1$
		vatIdLabel.setAlignment(SWT.RIGHT);
		vatIdLabel.setLayoutData(gridDataLeft);
		vatIdLabel.setFont(configs.getDefaultFont());

		txtVatID = new Text(configCompanyComposite, SWT.BORDER);
		txtVatID.setText(configs.getVATID());
		txtVatID.setLayoutData(gridDataRight);
		txtVatID.setFont(configs.getDefaultFont());
		
		final Label chartLabel = new Label(configCompanyComposite, SWT.NONE);
		chartLabel
				.setText(Messages.getString("settingsWindow.accountingChart")); //$NON-NLS-1$
		chartLabel.setAlignment(SWT.RIGHT);
		chartLabel.setLayoutData(gridDataLeft);
		chartLabel.setFont(configs.getDefaultFont());

		cmbChart = new Combo(configCompanyComposite, SWT.READ_ONLY);
		cmbChart.setLayoutData(gridDataRight);
		String selectedChart = configs.getAccountChart();
		int selectedChartIdx = 0;
		int currentOptionIdx = 0;
		for (String option : accounts.getAccountChartOptions()) {
			cmbChart.add(option);
			if (option.startsWith(selectedChart)) {
				selectedChartIdx = currentOptionIdx;
			}

			currentOptionIdx++;
		}

		cmbChart.select(selectedChartIdx);
		cmbChart.setFont(configs.getDefaultFont());

		final Label taxmodeLabel = new Label(configCompanyComposite, SWT.NONE);
		taxmodeLabel.setText(Messages.getString("settingsWindow.taxMode")); //$NON-NLS-1$
		taxmodeLabel.setAlignment(SWT.RIGHT);
		taxmodeLabel.setLayoutData(gridDataLeft);
		taxmodeLabel.setFont(configs.getDefaultFont());

		cmbTaxmode = new Combo(configCompanyComposite, SWT.READ_ONLY);
		cmbTaxmode.setLayoutData(gridDataRight);
		String selectedTaxmode = configs.getTaxmode();
		int selectedModeIdx = 0;
		currentOptionIdx = 0;
		for (String option : configs.getTaxmodeOptions()) {
			cmbTaxmode.add(option);
			if (option.startsWith(selectedTaxmode)) {
				selectedModeIdx = currentOptionIdx;
			}

			currentOptionIdx++;
		}

		cmbTaxmode.select(selectedModeIdx);
		cmbTaxmode.setFont(configs.getDefaultFont());

		final Label stateLabel = new Label(configCompanyComposite, SWT.NONE);
		stateLabel.setText(Messages.getString("settingsWindow.State")); //$NON-NLS-1$
		stateLabel.setAlignment(SWT.RIGHT);
		stateLabel.setLayoutData(gridDataLeft);
		stateLabel.setFont(configs.getDefaultFont());

		cmbState = new ComboViewer(configCompanyComposite, SWT.READ_ONLY);
		cmbState.getCombo().setLayoutData(gridDataRight);
		states stateList = new states();
		cmbState.setContentProvider(ArrayContentProvider.getInstance());
		cmbState.setInput(stateList.getStates());
		cmbState.getCombo().select(configs.getStateListIDX());
		cmbState.getCombo().setFont(configs.getDefaultFont());

		final Label irsOfficeLabel = new Label(configCompanyComposite, SWT.NONE);
		irsOfficeLabel.setText(Messages.getString("settingsWindow.irsOffice")); //$NON-NLS-1$
		irsOfficeLabel.setAlignment(SWT.RIGHT);
		irsOfficeLabel.setLayoutData(gridDataLeft);
		irsOfficeLabel.setFont(configs.getDefaultFont());

		cmbIRS = new ComboViewer(configCompanyComposite, SWT.READ_ONLY);
		cmbIRS.getCombo().setLayoutData(gridDataRight);
		cmbIRS.setContentProvider(ArrayContentProvider.getInstance());
		cmbIRS.setInput(IRSoffices.getIRSOffices());
		cmbIRS.getCombo().select(configs.getTaxOfficeListIDX());
		cmbIRS.getCombo().setFont(configs.getDefaultFont());

		final Label vatAnnouncementPeriodLabel = new Label(
				configCompanyComposite, SWT.NONE);
		vatAnnouncementPeriodLabel.setText(Messages
				.getString("settingsWindow.vatAnnouncement")); //$NON-NLS-1$
		vatAnnouncementPeriodLabel.setAlignment(SWT.RIGHT);
		vatAnnouncementPeriodLabel.setLayoutData(gridDataLeft);
		vatAnnouncementPeriodLabel.setFont(configs.getDefaultFont());

		cmbVATperiod = new Combo(configCompanyComposite, SWT.READ_ONLY);
		cmbVATperiod.setLayoutData(gridDataRight);
		cmbVATperiod.add("monthly"); //$NON-NLS-1$
		cmbVATperiod.add("quarterly"); //$NON-NLS-1$
		String VATPeriod = configs.getVATPeriod();
		if (VATPeriod.equals("quarterly")) { //$NON-NLS-1$
			cmbVATperiod.select(1);
		} else {
			cmbVATperiod.select(0);
		}
		cmbVATperiod.setFont(configs.getDefaultFont());

		final Label periodFromLabel = new Label(configCompanyComposite,
				SWT.NONE);
		periodFromLabel
				.setText(Messages.getString("settingsWindow.fiscalYear")); //$NON-NLS-1$
		periodFromLabel.setAlignment(SWT.RIGHT);
		periodFromLabel.setLayoutData(gridDataLeft);
		periodFromLabel.setFont(configs.getDefaultFont());

		cmbPeriodFrom = new Combo(configCompanyComposite, SWT.READ_ONLY);
		cmbPeriodFrom.setLayoutData(gridDataRight);
		cmbPeriodFrom.add(Messages.getString("settingsWindow.none")); //$NON-NLS-1$
		int comboIndex = 0;
		cmbPeriodFrom.select(comboIndex);
		for (int currentYear = client.getEntries().getFirstYearWithEntries(); currentYear <= client
				.getEntries().getLastYearWithEntries(); currentYear++) {
			cmbPeriodFrom.add(Integer.toString(currentYear));
			comboIndex++;
			if (configs.getDisplaySince().equals(currentYear + "-01-01")) { //$NON-NLS-1$
				cmbPeriodFrom.select(comboIndex);
			}
		}

		cmbPeriodFrom.setFont(configs.getDefaultFont());
		

		final Label vatExemptLabel = new Label(configCompanyComposite,
				SWT.NONE);
		vatExemptLabel
				.setText(Messages.getString("settingsWindow.VATExemption")); //$NON-NLS-1$
		vatExemptLabel.setAlignment(SWT.RIGHT);
		vatExemptLabel.setLayoutData(gridDataLeft);
		vatExemptLabel.setFont(configs.getDefaultFont());

		chkVATexempt = new Button(configCompanyComposite, SWT.CHECK);
		chkVATexempt.setLayoutData(gridDataRight);
		chkVATexempt.setFont(configs.getDefaultFont());
		chkVATexempt.setSelection(configs.isVATexempt());
		chkVATexempt.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (chkVATexempt.getSelection()) {
					try {
						tax zero=client.getTaxes().getVATByFactor(new BigDecimal(1));
						cmbViewerDefaultVAT.setSelection(new StructuredSelection(zero));
					} catch (taxNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
								
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		final Label vatDefault = new Label(configCompanyComposite,
				SWT.NONE);
		vatDefault
				.setText(Messages.getString("settingsWindow.defaultVAT")); //$NON-NLS-1$
		vatDefault.setAlignment(SWT.RIGHT);
		vatDefault.setLayoutData(gridDataLeft);
		vatDefault.setFont(configs.getDefaultFont());

		cmbViewerDefaultVAT = new ComboViewer(configCompanyComposite, SWT.READ_ONLY);
		cmbViewerDefaultVAT.getCombo().setLayoutData(gridDataRight);
		cmbViewerDefaultVAT.getCombo().setFont(configs.getDefaultFont());
		cmbViewerDefaultVAT.setContentProvider(ArrayContentProvider.getInstance());
		cmbViewerDefaultVAT.setInput(client.getTaxes().getExistingVATArray());
		cmbViewerDefaultVAT.setSelection(new StructuredSelection(client.getTaxes().getStandardVAT()));


		final Label salesTaxLabel = new Label(configCompanyComposite,
				SWT.NONE);
		salesTaxLabel
				.setText(Messages.getString("settingsWindow.CanadianTVQ"));  //$NON-NLS-1$
		salesTaxLabel.setAlignment(SWT.RIGHT);
		salesTaxLabel.setLayoutData(gridDataLeft);
		salesTaxLabel.setFont(configs.getDefaultFont());

		chkSalesTax = new Button(configCompanyComposite, SWT.CHECK);
		chkSalesTax.setLayoutData(gridDataRight);
		chkSalesTax.setFont(configs.getDefaultFont());
		chkSalesTax.setSelection(configs.hasSalesTax());

		
		final Label doctagLabel = new Label(configCompanyComposite,
				SWT.NONE);
		doctagLabel
				.setText(Messages.getString("settingsWindow.doctagLabel")); //$NON-NLS-1$
		doctagLabel.setAlignment(SWT.RIGHT);
		doctagLabel.setLayoutData(gridDataLeft);
		doctagLabel.setFont(configs.getDefaultFont());

		chkDocTag = new Button(configCompanyComposite, SWT.CHECK);
		chkDocTag.setLayoutData(gridDataRight);
		chkDocTag.setFont(configs.getDefaultFont());
		chkDocTag.setSelection(configs.shallDocTag());
		
		final Label round5ctLabel = new Label(configCompanyComposite,
				SWT.NONE);
		round5ctLabel
				.setText(Messages.getString("settingsWindow.swissRounding")); //$NON-NLS-1$
		round5ctLabel.setAlignment(SWT.RIGHT);
		round5ctLabel.setLayoutData(gridDataLeft);
		round5ctLabel.setFont(configs.getDefaultFont());

		chkRoundt5ct = new Button(configCompanyComposite, SWT.CHECK);
		chkRoundt5ct.setLayoutData(gridDataRight);
		chkRoundt5ct.setFont(configs.getDefaultFont());
		chkRoundt5ct.setSelection(configs.shallRoundTo5ct());
		
		// widgets in the Mail Server tab

		final Label smtpServerLabel = new Label(configSMTPComposite, SWT.NONE);
		smtpServerLabel
				.setText(Messages.getString("settingsWindow.smtpServer")); //$NON-NLS-1$
		smtpServerLabel.setAlignment(SWT.RIGHT);
		smtpServerLabel.setLayoutData(gridDataLeft);
		smtpServerLabel.setFont(configs.getDefaultFont());

		txtSMTPServer = new Text(configSMTPComposite, SWT.BORDER);
		txtSMTPServer.setText(configs.getSMTPServer());
		txtSMTPServer.setLayoutData(gridDataRight);
		txtSMTPServer.setFont(configs.getDefaultFont());

		final Label emptyCellLabel1 = new Label(configSMTPComposite, SWT.NONE);
		emptyCellLabel1.setLayoutData(gridDataLeft);

		chkSMTPAuth = new Button(configSMTPComposite, SWT.CHECK);
		chkSMTPAuth.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
			}
		});
		chkSMTPAuth.setText(Messages
				.getString("settingsWindow.serverRequiresAuthentication")); //$NON-NLS-1$
		chkSMTPAuth.setSelection(configs.shallUseSMTPAuth());
		chkSMTPAuth.setLayoutData(gridDataRight);
		chkSMTPAuth.setFont(configs.getDefaultFont());

		final Label emptyCellLabel2 = new Label(configSMTPComposite, SWT.NONE);
		emptyCellLabel2.setLayoutData(gridDataLeft);

		chkSMTPSSL = new Button(configSMTPComposite, SWT.CHECK);
		chkSMTPSSL.setText(Messages
				.getString("settingsWindow.btnUseSslsmtps.text")); //$NON-NLS-1$
		chkSMTPSSL.setSelection(configs.shallUseSMTPSSL());
		chkSMTPSSL.setLayoutData(gridDataRight);
		chkSMTPSSL.setFont(configs.getDefaultFont());

		final Label smtpUserNameLabel = new Label(configSMTPComposite, SWT.NONE);
		smtpUserNameLabel.setText(Messages
				.getString("settingsWindow.smtpUsername")); //$NON-NLS-1$
		smtpUserNameLabel.setAlignment(SWT.RIGHT);
		smtpUserNameLabel.setLayoutData(gridDataLeft);
		smtpUserNameLabel.setFont(configs.getDefaultFont());

		txtSMTPUser = new Text(configSMTPComposite, SWT.BORDER);
		txtSMTPUser.setText(configs.getSMTPUsername());
		txtSMTPUser.setLayoutData(gridDataRight);
		txtSMTPUser.setFont(configs.getDefaultFont());

		final Label smtpPasswordLabel = new Label(configSMTPComposite, SWT.NONE);
		smtpPasswordLabel.setText(Messages
				.getString("settingsWindow.smtpPassword")); //$NON-NLS-1$
		smtpPasswordLabel.setAlignment(SWT.RIGHT);
		smtpPasswordLabel.setLayoutData(gridDataLeft);
		smtpPasswordLabel.setFont(configs.getDefaultFont());

		txtSMTPpassword = new Text(configSMTPComposite, SWT.BORDER
				| SWT.PASSWORD);
		txtSMTPpassword.addFocusListener(new warningFocusAdapter(getShell()));
		txtSMTPpassword.setText(configs.getSMTPPassword());
		txtSMTPpassword.setLayoutData(gridDataRight);
		txtSMTPpassword.setFont(configs.getDefaultFont());

		final Label emailSenderLabel = new Label(configSMTPComposite, SWT.NONE);
		emailSenderLabel.setText(Messages
				.getString("settingsWindow.emailAddress")); //$NON-NLS-1$
		emailSenderLabel.setAlignment(SWT.RIGHT);
		emailSenderLabel.setLayoutData(gridDataLeft);
		emailSenderLabel.setFont(configs.getDefaultFont());

		txtSMTPemail = new Text(configSMTPComposite, SWT.BORDER);
		txtSMTPemail.setText(configs.getSenderEmail());
		txtSMTPemail.setLayoutData(gridDataRight);
		txtSMTPemail.setFont(configs.getDefaultFont());

		final Label emptyCellLabel3 = new Label(configSMTPComposite, SWT.NONE);
		emptyCellLabel3.setLayoutData(gridDataLeft);

		chkICAL = new Button(configSMTPComposite, SWT.CHECK);
		chkICAL.setText(Messages
				.getString("settingsWindow.btnIcalInvitationFor.text")); //$NON-NLS-1$
		chkICAL.setSelection(configs.shallSendICAL());
		chkICAL.setLayoutData(gridDataRight);
		chkICAL.setFont(configs.getDefaultFont());

		// widgets in the Bank tab

		final Label bankNameLabel = new Label(configBankComposite, SWT.NONE);
		bankNameLabel.setText(Messages.getString("settingsWindow.bankname")); //$NON-NLS-1$
		bankNameLabel.setAlignment(SWT.RIGHT);
		bankNameLabel.setLayoutData(gridDataLeft);
		bankNameLabel.setFont(configs.getDefaultFont());

		txtBankname = new Text(configBankComposite, SWT.READ_ONLY | SWT.BORDER);
		txtBankname.addFocusListener(new FocusAdapter() {
			public void focusGained(final FocusEvent arg0) {
				getShell().setFocus();
				MessageDialog.openInformation(
						getShell(),
						Messages.getString("settingsWindow.captionBanknameAutomatic"), Messages.getString("settingsWindow.textBanknameAutomatic")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
		txtBankname.setText(configs.getBankName());
		txtBankname.setLayoutData(gridDataRight);
		txtBankname.setFont(configs.getDefaultFont());

		bankCodeLabel = new Label(configBankComposite, SWT.NONE);
		bankCodeLabel.setText(Messages.getString("settingsWindow.bankcode")); //$NON-NLS-1$
		bankCodeLabel.setAlignment(SWT.RIGHT);
		bankCodeLabel.setLayoutData(gridDataLeft);
		bankCodeLabel.setFont(configs.getDefaultFont());

		txtBankcode = new Text(configBankComposite, SWT.BORDER);
		txtBankcode.addKeyListener(new KeyAdapter() {
			public void keyReleased(final KeyEvent arg0) {
				checkBankDetails();
			}
		});
		txtBankcode.setText(configs.getBankCode());
		txtBankcode.setLayoutData(gridDataRight);
		txtBankcode.setFont(configs.getDefaultFont());

		bankAccountLabel = new Label(configBankComposite, SWT.NONE);
		bankAccountLabel
				.setText(Messages.getString("settingsWindow.accountnr")); //$NON-NLS-1$
		bankAccountLabel.setAlignment(SWT.RIGHT);
		bankAccountLabel.setLayoutData(gridDataLeft);
		bankAccountLabel.setFont(configs.getDefaultFont());

		txtBankaccount = new Text(configBankComposite, SWT.BORDER);
		txtBankaccount.addKeyListener(new KeyAdapter() {
			public void keyReleased(final KeyEvent arg0) {
				checkBankDetails();
			}
		});
		txtBankaccount.setText(configs.getAccountCode());
		txtBankaccount.setLayoutData(gridDataRight);
		txtBankaccount.setFont(configs.getDefaultFont());


		IBANLabel = new Label(configBankComposite, SWT.NONE);
		IBANLabel
				.setText(Messages.getString("settingsWindow.IBAN"));  //$NON-NLS-1$
		IBANLabel.setAlignment(SWT.RIGHT);
		IBANLabel.setLayoutData(gridDataLeft);
		IBANLabel.setFont(configs.getDefaultFont());

		txtIBAN = new Text(configBankComposite, SWT.BORDER);
		txtIBAN.addKeyListener(new KeyAdapter() {
			public void keyReleased(final KeyEvent arg0) {
				checkBankDetails();
			}
		});
		txtIBAN.setText(configs.getIBAN());
		txtIBAN.setLayoutData(gridDataRight);
		txtIBAN.setFont(configs.getDefaultFont());


		BICLabel = new Label(configBankComposite, SWT.NONE);
		BICLabel
				.setText(Messages.getString("settingsWindow.BIC")); //$NON-NLS-1$
		BICLabel.setAlignment(SWT.RIGHT);
		BICLabel.setLayoutData(gridDataLeft);
		BICLabel.setFont(configs.getDefaultFont());

		txtBIC = new Text(configBankComposite, SWT.BORDER);
		txtBIC.addKeyListener(new KeyAdapter() {
			public void keyReleased(final KeyEvent arg0) {
				checkBankDetails();
			}
		});
		txtBIC.setText(configs.getBIC());
		txtBIC.setLayoutData(gridDataRight);
		txtBIC.setFont(configs.getDefaultFont());

		final Label holderNameLabel = new Label(configBankComposite, SWT.NONE);
		holderNameLabel
				.setText(Messages.getString("settingsWindow.holdername")); //$NON-NLS-1$
		holderNameLabel.setAlignment(SWT.RIGHT);
		holderNameLabel.setLayoutData(gridDataLeft);
		holderNameLabel.setFont(configs.getDefaultFont());

		txtHoldername = new Text(configBankComposite, SWT.BORDER);
		txtHoldername.setText(configs.getHolderName());
		txtHoldername.setLayoutData(gridDataRight);
		txtHoldername.setFont(configs.getDefaultFont());

		
		final Label creditorIDLabel = new Label(configBankComposite, SWT.NONE);
		creditorIDLabel
				.setText(Messages.getString("settingsWindow.CreditorID")); //Gläubiger-Identifikationsnummer //$NON-NLS-1$
		creditorIDLabel.setAlignment(SWT.RIGHT);
		creditorIDLabel.setLayoutData(gridDataLeft);
		creditorIDLabel.setFont(configs.getDefaultFont());

		txtCreditorID = new Text(configBankComposite, SWT.BORDER);
		txtCreditorID.setText(configs.getCreditorID());
		txtCreditorID.setLayoutData(gridDataRight);
		txtCreditorID.setFont(configs.getDefaultFont());
		
		checkBankDetails();

		// widgets in the Web Shop tab

		final Label webShopURLLabel = new Label(configWebShopComposite,
				SWT.NONE);
		webShopURLLabel
				.setText(Messages.getString("settingsWindow.webShopURL")); //$NON-NLS-1$
		webShopURLLabel.setAlignment(SWT.RIGHT);
		webShopURLLabel.setLayoutData(gridDataLeft);
		webShopURLLabel.setFont(configs.getDefaultFont());

		txtWebShopURL = new Text(configWebShopComposite, SWT.BORDER);
		txtWebShopURL.setText(configs.getWebShopURL());
		txtWebShopURL.setLayoutData(gridDataRight);
		txtWebShopURL.setFont(configs.getDefaultFont());

		final Label webShopUserLabel = new Label(configWebShopComposite,
				SWT.NONE);
		webShopUserLabel.setText(Messages
				.getString("settingsWindow.webShopUser")); //$NON-NLS-1$
		webShopUserLabel.setAlignment(SWT.RIGHT);
		webShopUserLabel.setLayoutData(gridDataLeft);
		webShopUserLabel.setFont(configs.getDefaultFont());

		txtWebShopUser = new Text(configWebShopComposite, SWT.BORDER);
		txtWebShopUser.setText(configs.getWebShopUser());
		txtWebShopUser.setLayoutData(gridDataRight);
		txtWebShopUser.setFont(configs.getDefaultFont());

		final Label webShopPasswordLabel = new Label(configWebShopComposite,
				SWT.NONE);
		webShopPasswordLabel.setText(Messages
				.getString("settingsWindow.webShopPassword")); //$NON-NLS-1$
		webShopPasswordLabel.setAlignment(SWT.RIGHT);
		webShopPasswordLabel.setLayoutData(gridDataLeft);
		webShopPasswordLabel.setFont(configs.getDefaultFont());

		txtWebShopPassword = new Text(configWebShopComposite, SWT.BORDER
				| SWT.PASSWORD);
		txtWebShopPassword.setText(configs.getWebShopPassword());
		txtWebShopPassword.setLayoutData(gridDataRight);
		txtWebShopPassword.setFont(configs.getDefaultFont());

		final Button applyButton = new Button(container, SWT.NONE);
		applyButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				// if taxes change (taxesNeedAccountChanges or because a new
				// taxmode has been selected)
				// we need to reload the instances of our transaction types
				// because they have been
				// loaded on program start with the now wrong values.
				// as read operations may cause the settingsDocument to be
				// reset, conduct all reads....

				// ... before conducting any writes, i.e. configs.setXXX
				String oldAccountingChart = configs.getAccountChart();
				configs.setOrganizationName(txtOrganizationName.getText());
				configs.setOrganizationStreet(txtOrganizationStreet.getText());
				configs.setOrganizationZip(txtOrganizationZip.getText());
				configs.setOrganizationLocation(txtOrganizationLocation.getText());
				configs.setOrganizationCountry(txtOrganizationCountry.getText());
				
				configs.setTaxID(txtTaxID.getText());
				configs.setVATID(txtVatID.getText());
				String chartNameSelected = cmbChart.getText();
				if (chartNameSelected.indexOf(' ') != -1) {
					chartNameSelected = chartNameSelected.substring(0,
							chartNameSelected.indexOf(' '));
				}

				// order: 1. if change in taxmode, conduct
				// 2.
				// 3. if change in taxmode reinit accounts
				configs.setAccountChart(chartNameSelected);
				configs.setTaxmode(cmbTaxmode.getSelectionIndex());
				configs.setStateListIDX(cmbState.getCombo().getSelectionIndex());
				configs.setTaxOfficeListIDX(cmbIRS.getCombo()
						.getSelectionIndex());
				configs.setVATPeriod(cmbVATperiod.getText());
				String periodFrom = ""; //$NON-NLS-1$
				String periodTo = ""; //$NON-NLS-1$
				if (cmbPeriodFrom.getSelectionIndex() != 0) {
					periodFrom = cmbPeriodFrom.getText() + "-01-01"; //$NON-NLS-1$
					periodTo = cmbPeriodFrom.getText() + "-12-31"; //$NON-NLS-1$
				}
				configs.setDisplaySince(periodFrom);
				configs.setDisplayTo(periodTo);
				configs.setVATExempt(chkVATexempt.getSelection());
				configs.setSalesTax(chkSalesTax.getSelection());
				configs.setShallDocTag(chkDocTag.getSelection());
				configs.setShallRound5ct(chkRoundt5ct.getSelection());

				configs.setSMTPServer(txtSMTPServer.getText());
				configs.setSMTPUsername(txtSMTPUser.getText());
				configs.setSMTPPassword(txtSMTPpassword.getText());
				configs.setUseSMTPAuth(chkSMTPAuth.getSelection());
				configs.setUseSMTPSSL(chkSMTPSSL.getSelection());
				configs.setSendICAL(chkICAL.getSelection());
				configs.setEmailSender(txtSMTPemail.getText());

				configs.setAccountCode(txtBankaccount.getText());
				configs.setIBAN(txtIBAN.getText());
				configs.setBIC(txtBIC.getText());
				
				configs.setHolderName(txtHoldername.getText());
				configs.setCreditorID(txtCreditorID.getText());
				configs.setBankCode(txtBankcode.getText());
				configs.setBankName(txtBankname.getText());

				configs.setWebShopURL(txtWebShopURL.getText());
				configs.setWebShopUser(txtWebShopUser.getText());
				configs.setWebShopPassword(txtWebShopPassword.getText());


				IStructuredSelection selection = (IStructuredSelection) cmbViewerDefaultVAT
						.getSelection();
				tax selectedTax = (tax) selection.getFirstElement();
				selectedTax.setAsDefaultIncomingTax();
				
				configs.writeSettings();
				// re-read the file
				configs.readSettings();

				if (!chartNameSelected.equals(oldAccountingChart)) {
					/**
					 * Now re-create the instances for all transaction types:
					 * they contain the accounts for the workflow steps which
					 * will have changed when the accounting chart has changed
					 * */
					client.getTransactions().init();

				}

				close();

			}
		});
		applyButton.setText(Messages.getString("settingsWindow.ok")); //$NON-NLS-1$
		mainGridData = new GridData();
		mainGridData.horizontalAlignment = SWT.RIGHT;
		mainGridData.verticalAlignment = SWT.FILL;
		mainGridData.grabExcessHorizontalSpace = true;
		mainGridData.grabExcessVerticalSpace = false;
		applyButton.setLayoutData(mainGridData);

		//
		return container;
	}

	/**
	 * Create the actions
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Create the menu manager
	 * 
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager(
				Messages.getString("settingsWindow.menu")); //$NON-NLS-1$
		return menuManager;
	}

	/**
	 * Create the toolbar manager
	 * 
	 * @return the toolbar manager
	 */
	@Override
	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);
		return toolBarManager;
	}

	/**
	 * Create the status line manager
	 * 
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		statusLineManager.setMessage(null, ""); //$NON-NLS-1$
		return statusLineManager;
	}

	/**
	 * Launch the application
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			settingsWindow window = new settingsWindow();
			window.setBlockOnOpen(true);
			window.open();
			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the shell
	 * 
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(new Point(103, 100));
		super.configureShell(newShell);
		newShell.setText(application.getAppName());
	}

	/**
	 * Return the initial size of the window
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 840);
	}

}
