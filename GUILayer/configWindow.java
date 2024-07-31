package GUILayer;

import java.awt.print.PrinterJob;
import java.io.File;
import java.util.Vector;

import javax.print.PrintService;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import uk.co.mmscomputing.device.scanner.Scanner;
import uk.co.mmscomputing.device.scanner.ScannerIOException;
import appLayer.appUser;
import appLayer.application;
import appLayer.client;
import appLayer.configs;
import appLayer.checkRelated.DerbyLockCheck;
import appLayer.checkRelated.OOoCheck;
import appLayer.checkRelated.check;
import appLayer.checkRelated.checkResult;
import appLayer.checkRelated.configWindowDatabaseCheck;
import appLayer.checkRelated.configWindowTestUI;
import appLayer.checkRelated.databaseConnectivityCheck;
import appLayer.checkRelated.databaseDriverNameCheck;
import appLayer.checkRelated.dirCheck;
import appLayer.checkRelated.templateCheck;
import appLayer.checkRelated.winstonCheck;
import dataLayer.persistUtil;

public class configWindow extends ApplicationWindow {

	private Text txtFlddatabaseserver;
	private Text txtFlddatabasedriverfilename;
	private Text txtFlddatabasetype;
	private Text txtFlddatabasedriver;
	private Text txtflddatabasepassword;
	private Text txtFlddatabaseuser;
	private Text txtFlddatabasename;
	private Button chkUseExternalDB;
	private Text txtFldGPGPath;
	private Text txtFldTessLangPath;
	private Text txtFldCTAPI;
	private Text txtFldWinstonPath;
	private Text txtFldOOoPath;
	private List lstPrinters;
	private Button chkPowered = null;
	private Button chkUseChipcardPinpad = null;
	private Button chkOCR = null;
	private Combo cmbScanner;
	private Combo cmbOCRlang;
	private Button btnBrowseWinston;
	private Button btnBrowseTessLang; 

	public List listStatus;
	private TabFolder tabFolder = null;
	private Vector<check> testCases = new Vector<check>();

	private boolean waitForCorrectConfigBeforeMainWindow = true;
	private Composite parent;

	public configWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	/*
	 * public static Shell getShell() { return getShell(); }
	 */
	private void saveConfig() {
		persistUtil.shutdown();
		String[] printerSelection = null;
		printerSelection = lstPrinters.getSelection();
		if (printerSelection.length > 0) {

			configs.setPrinterName(printerSelection[0]);
		}
		configs.setDatabaseName(txtFlddatabasename.getText());
		configs.setDatabaseDriverName(txtFlddatabasedriver.getText());
		configs.setDatabaseDriverFileName(txtFlddatabasedriverfilename
				.getText());
		configs.setDatabasePassword(txtflddatabasepassword.getText());
		configs.setDatabaseServer(txtFlddatabaseserver.getText());
		configs.setDatabaseUser(txtFlddatabaseuser.getText());
		configs.setDatabaseType(txtFlddatabasetype.getText());
		configs.setOOoPath(txtFldOOoPath.getText());
		configs.setPrintPoweredBy(chkPowered.getSelection());
		// configs.setStartWithCommunity(chkCommunity.getSelection());
		configs.setUseExternalDB(chkUseExternalDB.getSelection());

		configs.setWinstonPath(txtFldWinstonPath.getText());
		configs.setGPGPath(txtFldGPGPath.getText());
		configs.setCtAPI(txtFldCTAPI.getText());
		configs.setUseCardReaderPINpad(chkUseChipcardPinpad.getSelection());
		configs.setScannerName(cmbScanner.getText());
		configs.setShallOCR(chkOCR.getSelection());
		configs.setOCRlang(cmbOCRlang.getText());
		configs.setOCRlangPath(txtFldTessLangPath.getText());

		// write the file
		configs.writeConfiguration();
		// re-read the file
		configs.readConfiguration();
		test();
	}

	public Composite getParent() {
		return parent;
	}

	private void updateOCREnabled() {
		cmbOCRlang.setEnabled(chkOCR.getSelection());
		txtFldTessLangPath.setEnabled(chkOCR.getSelection());
		btnBrowseTessLang.setEnabled(chkOCR.getSelection());
	}

	protected Control createContents(Composite parent) {
		/*
		 * "configs" should not be used in this method, outsource to
		 * selectValuesFromConfigFile Reason: this method is invoked when the
		 * class is constructed, at program start, which is BEFORE the main
		 * method will set the location of the config file which Exception:
		 * Event handlers like the save button event handlers will already have
		 * the correct location
		 */
		this.parent = parent;
		parent.getShell().setSize(getInitialSize());

		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());

		tabFolder = new TabFolder(container, SWT.NONE);

		final TabItem configTabItem = new TabItem(tabFolder, SWT.NONE);
		configTabItem.setText(Messages.getString("configWindow.config")); //$NON-NLS-1$
		final Composite compConfig = new Composite(tabFolder, SWT.NONE);
		configTabItem.setControl(compConfig);

		final TabItem dbTabItem = new TabItem(tabFolder, SWT.NONE);
		dbTabItem
				.setText(Messages.getString("configWindow.databaseTabCaption")); //$NON-NLS-1$

		final Composite compDB = new Composite(tabFolder, SWT.NONE);
		dbTabItem.setControl(compDB);

		chkUseExternalDB = new Button(compDB, SWT.CHECK);
		chkUseExternalDB.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (chkUseExternalDB.getSelection()) {
					boolean sampleValues = MessageDialog.openQuestion(
							getShell(),
							Messages.getString("configWindow.mysqlMessageCaption"), Messages.getString("configWindow.mysqlMessageText")); //$NON-NLS-1$ //$NON-NLS-2$
					if (sampleValues) {
						txtFlddatabasedriver.setText("com.mysql.jdbc.Driver"); //$NON-NLS-1$
						txtFlddatabasedriverfilename.setText(System
								.getProperty("user.dir") + System.getProperty("file.separator") + "libs" + System.getProperty("file.separator") + "persistence" + System.getProperty("file.separator") + "mysql-connector-java-5.1.6-bin.jar"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
						txtFlddatabasename.setText("gnuaccounting"); //$NON-NLS-1$
						txtFlddatabaseserver.setText("localhost"); //$NON-NLS-1$
						txtFlddatabasetype.setText("mysql"); //$NON-NLS-1$
						txtFlddatabaseuser.setText("gnuaccounting"); //$NON-NLS-1$
					}
				} else {
					txtFlddatabasedriver.setText(""); //$NON-NLS-1$
					txtFlddatabasedriverfilename.setText(""); //$NON-NLS-1$
					txtFlddatabasename.setText(""); //$NON-NLS-1$
					txtFlddatabaseserver.setText(""); //$NON-NLS-1$
					txtFlddatabasetype.setText(""); //$NON-NLS-1$
					txtFlddatabaseuser.setText(""); //$NON-NLS-1$

				}
			}
		});
		chkUseExternalDB.setBounds(31, 5, 332, 32);
		chkUseExternalDB.setFont(configs.getDefaultFont());
		chkUseExternalDB.setText(Messages
				.getString("configWindow.useExternalDB")); //$NON-NLS-1$

		txtFlddatabasename = new Text(compDB, SWT.BORDER);
		txtFlddatabasename.setBounds(166, 103, 168, 22);
		txtFlddatabasename.setFont(configs.getDefaultFont());

		txtFlddatabaseuser = new Text(compDB, SWT.BORDER);
		txtFlddatabaseuser.setBounds(166, 130, 168, 22);
		txtFlddatabaseuser.setFont(configs.getDefaultFont());

		txtflddatabasepassword = new Text(compDB, SWT.BORDER);
		txtflddatabasepassword.setBounds(166, 157, 168, 22);
		txtflddatabasepassword.setFont(configs.getDefaultFont());
		txtflddatabasepassword.setEchoChar('*');

		txtFlddatabasedriver = new Text(compDB, SWT.BORDER);
		txtFlddatabasedriver.setBounds(166, 183, 168, 22);
		txtFlddatabasedriver.setFont(configs.getDefaultFont());

		final Label dbTypeLabel = new Label(compDB, SWT.NONE);
		dbTypeLabel.setAlignment(SWT.RIGHT);
		dbTypeLabel.setBounds(0, 45, 160, 22);
		dbTypeLabel.setFont(configs.getDefaultFont());
		dbTypeLabel.setText(Messages.getString("configWindow.dbType")); //$NON-NLS-1$

		final Label dbPasswordLabel = new Label(compDB, SWT.NONE);
		dbPasswordLabel.setAlignment(SWT.RIGHT);
		dbPasswordLabel.setBounds(0, 160, 160, 22);
		dbPasswordLabel.setFont(configs.getDefaultFont());
		dbPasswordLabel.setText(Messages.getString("configWindow.dbPassword")); //$NON-NLS-1$

		final Label jdbcClassNameLabel = new Label(compDB, SWT.NONE);
		jdbcClassNameLabel.setAlignment(SWT.RIGHT);
		jdbcClassNameLabel.setBounds(0, 186, 160, 22);
		jdbcClassNameLabel.setFont(configs.getDefaultFont());
		jdbcClassNameLabel.setText(Messages
				.getString("configWindow.jdbcClassName")); //$NON-NLS-1$

		final Label jarFileNameLabel = new Label(compDB, SWT.NONE);
		jarFileNameLabel.setAlignment(SWT.RIGHT);
		jarFileNameLabel.setBounds(0, 216, 160, 22);
		jarFileNameLabel.setFont(configs.getDefaultFont());
		jarFileNameLabel
				.setText(Messages.getString("configWindow.jarFileName")); //$NON-NLS-1$

		final Button button = new Button(compDB, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				String allowedExtensions[] = { "*.jar" }; //$NON-NLS-1$
				dialog.setFilterExtensions(allowedExtensions);
				String name = dialog.open();
				if (name != null) {
					txtFlddatabasedriverfilename.setText(name);
				}
			}
		});
		button.setBounds(304, 209, 30, 22);
		button.setFont(configs.getDefaultFont());
		button.setText(Messages
				.getString("configWindow.smallBrowseButtonCaption")); //$NON-NLS-1$

		final Label dbUserLabel = new Label(compDB, SWT.NONE);
		dbUserLabel.setAlignment(SWT.RIGHT);
		dbUserLabel.setBounds(0, 133, 160, 22);
		dbUserLabel.setFont(configs.getDefaultFont());
		dbUserLabel.setText(Messages.getString("configWindow.dbUser")); //$NON-NLS-1$

		final Label dbDatabaseLabel = new Label(compDB, SWT.NONE);
		dbDatabaseLabel.setAlignment(SWT.RIGHT);
		dbDatabaseLabel.setBounds(0, 106, 160, 22);
		dbDatabaseLabel.setFont(configs.getDefaultFont());
		dbDatabaseLabel.setText(Messages.getString("configWindow.dbName")); //$NON-NLS-1$

		txtFlddatabasetype = new Text(compDB, SWT.BORDER);
		txtFlddatabasetype.setBounds(166, 43, 168, 22);
		txtFlddatabasetype.setFont(configs.getDefaultFont());

		txtFlddatabasedriverfilename = new Text(compDB, SWT.BORDER);
		txtFlddatabasedriverfilename.setBounds(166, 211, 132, 22);
		txtFlddatabasedriverfilename.setFont(configs.getDefaultFont());

		txtFlddatabaseserver = new Text(compDB, SWT.BORDER);
		txtFlddatabaseserver.setBounds(166, 73, 168, 22);
		txtFlddatabaseserver.setFont(configs.getDefaultFont());

		final Label dbHostLabel = new Label(compDB, SWT.NONE);
		dbHostLabel.setAlignment(SWT.RIGHT);
		dbHostLabel.setBounds(0, 78, 160, 22);
		dbHostLabel.setFont(configs.getDefaultFont());
		dbHostLabel.setText(Messages.getString("configWindow.dbHost")); //$NON-NLS-1$

		final Button btnSaveConfig_1 = new Button(compDB, SWT.NONE);
		btnSaveConfig_1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				saveConfig();
			}
		});
		btnSaveConfig_1.setBounds(10, 324, 101, 29);
		btnSaveConfig_1.setFont(configs.getDefaultFont());
		btnSaveConfig_1.setText(Messages
				.getString("configWindow.saveConfigButtonCaption")); //$NON-NLS-1$

		final TabItem testTabItem = new TabItem(tabFolder, SWT.NONE);
		testTabItem.setText(Messages.getString("configWindow.test")); //$NON-NLS-1$
		final Composite compTest = new Composite(tabFolder, SWT.NONE);
		compTest.setLayout(new FillLayout());
		testTabItem.setControl(compTest);

		listStatus = new List(compTest, SWT.V_SCROLL | SWT.BORDER);

		Label defaultPrinterLabel;

		Label lblOOOPath;


		Button btnSaveConfig;

		// //////////////// config tab

		// the values of the fields will be set using loadValuesFromConfigFile 
		GridLayoutFactory.swtDefaults().numColumns(3).margins(10, 5)
				.applyTo(compConfig);

		lblOOOPath = new Label(compConfig, SWT.NONE);
		lblOOOPath.setAlignment(SWT.RIGHT);
		lblOOOPath.setFont(configs.getDefaultFont());
		lblOOOPath.setText(Messages.getString("configWindow.ooopath")); //$NON-NLS-1$
		GridDataFactory.swtDefaults().applyTo(lblOOOPath);

		txtFldOOoPath = new Text(compConfig, SWT.BORDER);
		txtFldOOoPath.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
				.applyTo(txtFldOOoPath);

		final Button btnBrowseOOO = new Button(compConfig, SWT.NONE);
		btnBrowseOOO.setFont(configs.getDefaultFont());
		btnBrowseOOO.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {

				DirectoryDialog dialog = new DirectoryDialog(getShell(),
						SWT.OPEN);
				/*
				 * String allowedExtensions[]={"*.jar"};
				 * dialog.setFilterExtensions(allowedExtensions);
				 */
				String name = dialog.open();
				if (name != null) {
					txtFldOOoPath.setText(name);
				}
			}
		});
		btnBrowseOOO.setText(Messages.getString("configWindow.browse")); //$NON-NLS-1$
		GridDataFactory.swtDefaults().applyTo(btnBrowseOOO);

		final Label winstonLabel = new Label(compConfig, SWT.NONE);
		winstonLabel.setAlignment(SWT.RIGHT);
		winstonLabel.setFont(configs.getDefaultFont());
		winstonLabel.setText(Messages.getString("configWindow.winstonoutbox")); //$NON-NLS-1$
		GridDataFactory.swtDefaults().applyTo(winstonLabel);

		txtFldWinstonPath = new Text(compConfig, SWT.BORDER);
		txtFldWinstonPath.setFont(configs.getDefaultFont());
		txtFldWinstonPath.setText(configs.getWinstonPath());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
				.applyTo(txtFldWinstonPath);

		btnBrowseWinston = new Button(compConfig, SWT.NONE);
		btnBrowseWinston.setFont(configs.getDefaultFont());

		btnBrowseWinston.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell(),
						SWT.OPEN);
				/*
				 * String allowedExtensions[]={"*.jar"};
				 * dialog.setFilterExtensions(allowedExtensions);
				 */
				String name = dialog.open();
				if (name != null) {
					if (!name.endsWith(File.separator)) {
						name = name + File.separator;
					}
					if (!name.endsWith("Ausgang" + File.separator)) { //$NON-NLS-1$
						name = name + "Ausgang" + File.separator; //$NON-NLS-1$
					}
					txtFldWinstonPath.setText(name);
				}
			}
		});
		btnBrowseWinston.setText(Messages.getString("configWindow.browse")); //$NON-NLS-1$
		GridDataFactory.swtDefaults().grab(true, false)
				.applyTo(btnBrowseWinston);

		final Label chipcardDriverLabel = new Label(compConfig, SWT.NONE);
		chipcardDriverLabel.setAlignment(SWT.RIGHT);
		chipcardDriverLabel.setText(Messages
				.getString("configWindow.chipcarddriverapi")); //$NON-NLS-1$
		chipcardDriverLabel.setFont(configs.getDefaultFont());
		GridDataFactory.swtDefaults().grab(true, false)
				.applyTo(chipcardDriverLabel);

		txtFldCTAPI = new Text(compConfig, SWT.BORDER);
		txtFldCTAPI.setFont(configs.getDefaultFont());
		txtFldCTAPI.setText(configs.getCtAPI());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
				.applyTo(txtFldCTAPI);

		final Button btnBrowseCTAPI = new Button(compConfig, SWT.NONE);
		btnBrowseCTAPI.setFont(configs.getDefaultFont());
		GridDataFactory.swtDefaults().grab(true, false).applyTo(btnBrowseCTAPI);

		btnBrowseCTAPI.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				String allowedExtensions[] = { "*.so", "*.dll" }; //$NON-NLS-1$ //$NON-NLS-2$
				dialog.setFilterExtensions(allowedExtensions);
				String name = dialog.open();
				if (name != null) {
					txtFldCTAPI.setText(name);
				}
			}
		});
		btnBrowseCTAPI.setText(Messages.getString("configWindow.browseButton")); //$NON-NLS-1$

		chkUseChipcardPinpad = new Button(compConfig, SWT.CHECK);
		chkUseChipcardPinpad.setText(Messages
				.getString("configWindow.useChipardPinpad")); //$NON-NLS-1$
		chkUseChipcardPinpad.setFont(configs.getDefaultFont());
		GridDataFactory.swtDefaults().grab(true, false).span(3, 1)
				.applyTo(chkUseChipcardPinpad);

		final Label lblGPGPath = new Label(compConfig, SWT.NONE);
		lblGPGPath.setAlignment(SWT.RIGHT);
		lblGPGPath.setText(Messages.getString("configWindow.GPGPath")); //$NON-NLS-1$
		lblGPGPath.setFont(configs.getDefaultFont());
		GridDataFactory.swtDefaults().grab(true, false).applyTo(lblGPGPath);

		txtFldGPGPath = new Text(compConfig, SWT.BORDER);
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
				.applyTo(txtFldGPGPath);

		final Button btnBrowseGPG = new Button(compConfig, SWT.NONE);
		btnBrowseGPG.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent arg0) {
				DirectoryDialog dialog = new DirectoryDialog(getShell(),
						SWT.OPEN);
				/*
				 * String allowedExtensions[]={"*.jar"};
				 * dialog.setFilterExtensions(allowedExtensions);
				 */
				String name = dialog.open();
				if (name != null) {
					txtFldGPGPath.setText(name);
				}

			}
		});
		btnBrowseGPG.setFont(configs.getDefaultFont());
		btnBrowseGPG.setText(Messages.getString("configWindow.browse")); //$NON-NLS-1$
		GridDataFactory.swtDefaults().grab(true, false).applyTo(btnBrowseGPG);

		defaultPrinterLabel = new Label(compConfig, SWT.NONE);
		defaultPrinterLabel.setAlignment(SWT.RIGHT);
		defaultPrinterLabel.setFont(configs.getDefaultFont());
		defaultPrinterLabel.setText(Messages
				.getString("configWindow.defaultPrinter")); //$NON-NLS-1$
		GridDataFactory.swtDefaults().grab(true, false)
				.applyTo(defaultPrinterLabel);

		lstPrinters = new List(compConfig, SWT.V_SCROLL | SWT.H_SCROLL
				| SWT.BORDER);
		lstPrinters.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().grab(true, false).hint(200, 100)
				.applyTo(lstPrinters);
		
		/* .span(2,1) does not look nice in this case (brakes the indent) so lets create an empty cell using a composite
		*/
		Composite dummy=new Composite(compConfig, SWT.NONE);
		GridDataFactory.swtDefaults().hint(0,0).applyTo(dummy);

		Label lblScanner = new Label(compConfig, SWT.NONE);
		lblScanner.setAlignment(SWT.RIGHT);
		lblScanner.setText(Messages.getString("configWindow.lblScanner.text")); //$NON-NLS-1$

		lblScanner.setFont(configs.getDefaultFont());
		GridDataFactory.swtDefaults().grab(true, false).applyTo(lblScanner);

		ComboViewer cmbViewerScanner = new ComboViewer(compConfig,
				SWT.READ_ONLY);
		cmbViewerScanner.setContentProvider(ArrayContentProvider.getInstance());
		cmbScanner = cmbViewerScanner.getCombo();
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
				.applyTo(cmbScanner);

		cmbScanner.setFont(configs.getDefaultFont());
		try {
			Scanner scanner = Scanner.getDevice();
			if (scanner != null) {
				cmbViewerScanner.setInput(scanner.getDeviceNames());
			}
		} catch (UnsatisfiedLinkError e1) {
			// no scan support installed,e.g. Linux w/o sane
		} catch (ScannerIOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		chkOCR = new Button(compConfig, SWT.CHECK);
		chkOCR.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateOCREnabled();
				
			}
		});
		chkOCR.setText(Messages.getString("configWindow.useOCR")); //$NON-NLS-1$
		chkOCR.setFont(configs.getDefaultFont());
		GridDataFactory.swtDefaults().grab(true, false).span(3, 1)
				.applyTo(chkOCR);

		Label lblOCRLang = new Label(compConfig, SWT.RIGHT);
		lblOCRLang.setText(Messages.getString("configWindow.ocrLanguage")); //$NON-NLS-1$
		lblOCRLang.setFont(configs.getDefaultFont());

		GridDataFactory.swtDefaults().grab(true, false).applyTo(lblOCRLang);

		cmbOCRlang = new Combo(compConfig, SWT.READ_ONLY);
		cmbOCRlang.setFont(configs.getDefaultFont());
		// Tesseract recognizes text in the following languages
		// if the according data is installed
		cmbOCRlang.add("afr"); //$NON-NLS-1$
		cmbOCRlang.add("ara"); //$NON-NLS-1$
		cmbOCRlang.add("aze"); //$NON-NLS-1$
		cmbOCRlang.add("bel"); //$NON-NLS-1$
		cmbOCRlang.add("ben"); //$NON-NLS-1$
		cmbOCRlang.add("bul"); //$NON-NLS-1$
		cmbOCRlang.add("cat"); //$NON-NLS-1$
		cmbOCRlang.add("ces"); //$NON-NLS-1$
		cmbOCRlang.add("chi"); //$NON-NLS-1$
		cmbOCRlang.add("chr"); //$NON-NLS-1$
		cmbOCRlang.add("dan"); //$NON-NLS-1$
		cmbOCRlang.add("deu"); //$NON-NLS-1$
		cmbOCRlang.add("ell"); //$NON-NLS-1$
		cmbOCRlang.add("eng"); //$NON-NLS-1$
		cmbOCRlang.add("enm"); //$NON-NLS-1$
		cmbOCRlang.add("epo"); //$NON-NLS-1$
		cmbOCRlang.add("equ"); //$NON-NLS-1$
		cmbOCRlang.add("est"); //$NON-NLS-1$
		cmbOCRlang.add("eus"); //$NON-NLS-1$
		cmbOCRlang.add("fin"); //$NON-NLS-1$
		cmbOCRlang.add("fra"); //$NON-NLS-1$
		cmbOCRlang.add("frk"); //$NON-NLS-1$
		cmbOCRlang.add("frm"); //$NON-NLS-1$
		cmbOCRlang.add("glg"); //$NON-NLS-1$
		cmbOCRlang.add("grc"); //$NON-NLS-1$
		cmbOCRlang.add("heb"); //$NON-NLS-1$
		cmbOCRlang.add("heb"); //$NON-NLS-1$
		cmbOCRlang.add("heb"); //$NON-NLS-1$
		cmbOCRlang.add("hin"); //$NON-NLS-1$
		cmbOCRlang.add("hrv"); //$NON-NLS-1$
		cmbOCRlang.add("hun"); //$NON-NLS-1$
		cmbOCRlang.add("ind"); //$NON-NLS-1$
		cmbOCRlang.add("isl"); //$NON-NLS-1$
		cmbOCRlang.add("ita"); //$NON-NLS-1$
		cmbOCRlang.add("jpn"); //$NON-NLS-1$
		cmbOCRlang.add("kan"); //$NON-NLS-1$
		cmbOCRlang.add("kor"); //$NON-NLS-1$
		cmbOCRlang.add("lav"); //$NON-NLS-1$
		cmbOCRlang.add("lit"); //$NON-NLS-1$
		cmbOCRlang.add("mal"); //$NON-NLS-1$
		cmbOCRlang.add("mkd"); //$NON-NLS-1$
		cmbOCRlang.add("mlt"); //$NON-NLS-1$
		cmbOCRlang.add("msa"); //$NON-NLS-1$
		cmbOCRlang.add("nld"); //$NON-NLS-1$
		cmbOCRlang.add("nor"); //$NON-NLS-1$
		cmbOCRlang.add("osd"); //$NON-NLS-1$
		cmbOCRlang.add("pol"); //$NON-NLS-1$
		cmbOCRlang.add("por"); //$NON-NLS-1$
		cmbOCRlang.add("ron"); //$NON-NLS-1$
		cmbOCRlang.add("rus"); //$NON-NLS-1$
		cmbOCRlang.add("slk"); //$NON-NLS-1$
		cmbOCRlang.add("slv"); //$NON-NLS-1$
		cmbOCRlang.add("spa"); //$NON-NLS-1$
		cmbOCRlang.add("sqi"); //$NON-NLS-1$
		cmbOCRlang.add("srp"); //$NON-NLS-1$
		cmbOCRlang.add("swa"); //$NON-NLS-1$
		cmbOCRlang.add("swe"); //$NON-NLS-1$
		cmbOCRlang.add("tam"); //$NON-NLS-1$
		cmbOCRlang.add("tel"); //$NON-NLS-1$
		cmbOCRlang.add("tgl"); //$NON-NLS-1$
		cmbOCRlang.add("tha"); //$NON-NLS-1$
		cmbOCRlang.add("tur"); //$NON-NLS-1$
		cmbOCRlang.add("ukr"); //$NON-NLS-1$
		cmbOCRlang.add("vie"); //$NON-NLS-1$
		cmbOCRlang.setEnabled(false);

		GridDataFactory.fillDefaults().grab(true, false).hint(200, 20)
				.applyTo(cmbOCRlang);
		/* .span(2,1) does not look nice in this case (brakes the indent) so lets create an empty cell using a composite
		*/
		Composite dummy2=new Composite(compConfig, SWT.NONE);
		GridDataFactory.swtDefaults().hint(0,0).applyTo(dummy2);

		Label lblOCRLangPath = new Label(compConfig, SWT.RIGHT);
		lblOCRLangPath.setText(Messages.getString("configWindow.OCRlanguagePath")); //$NON-NLS-1$
		lblOCRLangPath.setFont(configs.getDefaultFont());

		GridDataFactory.swtDefaults().grab(true, false).applyTo(lblOCRLangPath);

		txtFldTessLangPath = new Text(compConfig, SWT.BORDER);
		txtFldTessLangPath.setFont(configs.getDefaultFont());

		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
				.applyTo(txtFldTessLangPath);

		btnBrowseTessLang = new Button(compConfig, SWT.NONE);
		btnBrowseTessLang.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent arg0) {
				DirectoryDialog dialog = new DirectoryDialog(getShell(),
						SWT.OPEN);
				/*
				 * String allowedExtensions[]={"*.jar"};
				 * dialog.setFilterExtensions(allowedExtensions);
				 */
				String name = dialog.open();
				if (name != null) {
					txtFldTessLangPath.setText(name);
				}

			}
		});
		btnBrowseTessLang.setFont(configs.getDefaultFont());
		btnBrowseTessLang.setText(Messages.getString("configWindow.browse")); //$NON-NLS-1$
		GridDataFactory.swtDefaults().grab(true, false)
				.applyTo(btnBrowseTessLang);

		chkPowered = new Button(compConfig, SWT.CHECK);
		chkPowered.setFont(configs.getDefaultFont());
		chkPowered.setText(Messages.getString("configWindow.printPoweredBy")); //$NON-NLS-1$
		GridDataFactory.swtDefaults().grab(true, false).span(3, 1)
				.applyTo(chkPowered);

		btnSaveConfig = new Button(compConfig, SWT.NONE);
		btnSaveConfig.setFont(configs.getDefaultFont());
		btnSaveConfig.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				saveConfig();
			}

		});
		btnSaveConfig.setText(Messages.getString("configWindow.save")); //$NON-NLS-1$
		GridDataFactory.swtDefaults().grab(true, false).span(3, 1)
				.applyTo(btnSaveConfig);

		// now the app is loaded and almost completely there
		return container;
	}

	public boolean test() {
		listStatus.removeAll();
		tabFolder.setSelection(2);

		configWindowTestUI cwtu = new configWindowTestUI(this);
		configWindowDatabaseCheck cwdt = new configWindowDatabaseCheck(this);

		testCases.add(new dirCheck(cwtu, Messages
				.getString("configWindow.checkDir") + client.getDataPath() + //$NON-NLS-1$
				Messages.getString("configWindow.isthere"))); //$NON-NLS-1$

		testCases.add(new databaseDriverNameCheck(cwtu, Messages
				.getString("configWindow.checkconfigisthere"))); //$NON-NLS-1$
		testCases.add(new DerbyLockCheck(cwtu, Messages
				.getString("configWindow.ensureDerbyLock"))); //$NON-NLS-1$
		testCases.add(new databaseConnectivityCheck(cwtu, Messages
				.getString("configWindow.testDBConnectivity"), cwdt)); //$NON-NLS-1$
		testCases.add(new templateCheck(cwtu, Messages
				.getString("configWindow.checkTemplates"))); //$NON-NLS-1$
		testCases.add(new OOoCheck(cwtu, Messages
				.getString("configWindow.checkOOODir"))); //$NON-NLS-1$
		testCases.add(new winstonCheck(cwtu, Messages
				.getString("configWindow.checkWinston"))); //$NON-NLS-1$

		boolean alreadyDead = false;
		for (int testIndex = 0; testIndex < testCases.size(); testIndex++) {
			if (!alreadyDead) {
				check currentTest = (check) testCases.elementAt(testIndex);
				currentTest.execute();
				checkResult tr = currentTest.getResult();
				if (tr.getFatal()) {
					tabFolder.setSelection(0);
					MessageDialog
							.openError(
									getShell(),
									Messages.getString("configWindow.Error"), currentTest.getDescription() + ":" + tr.getResultString()); //$NON-NLS-1$ //$NON-NLS-2$
					alreadyDead = true;
				}
			}
		}

		if (!alreadyDead) {
			waitForCorrectConfigBeforeMainWindow = false;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method invokes the test screen, then the username/password dialog if
	 * multiuser is enabled, then creates the main screen
	 * */
	public void testAndShow() {
		show();
		test();
		Display display = getShell().getDisplay();

		while (waitForCorrectConfigBeforeMainWindow) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		close();

		String username = "Administrator"; //$NON-NLS-1$
		String password = appUser.emptyPassword;

		while (!application.getUsers().authenticate(username, password)) {
			passwordDialog pwd = new passwordDialog(getShell(), "Administrator"); //$NON-NLS-1$
			pwd.setBlockOnOpen(true);
			int res = pwd.open();
			if (res == 1) {
				System.exit(0);
			}
			username = pwd.getUsername();
			password = pwd.getPassword();

		}

		try {
			MainWindow window = new MainWindow();
			window.setBlockOnOpen(true);
			window.setDisplay(display);
			window.open();

			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void loadValuesFromConfigFile() {
		String selectedPrinter = configs.getPrinterName();
		lstPrinters.removeAll();
		PrintService[] pss = PrinterJob.lookupPrintServices();

		int itemIndex = 0;
		for (int printerIndex = 0; printerIndex < pss.length; printerIndex++) {
			String currentSystemPrinter = pss[printerIndex].getName();
			lstPrinters.add(currentSystemPrinter);
			if (currentSystemPrinter.equals(selectedPrinter)) {
				lstPrinters.select(itemIndex);
			}

			itemIndex++;
		}
		chkPowered.setSelection(configs.shallPrintPoweredBy());
		String oooPath = ""; //$NON-NLS-1$
		try {
			oooPath = configs.getOOoPath();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (oooPath == null) {
			oooPath = new String(""); //$NON-NLS-1$
		}
		txtFldOOoPath.setText(oooPath);
		txtFldCTAPI.setText(configs.getCtAPI());
		chkUseChipcardPinpad.setSelection(configs.shallUseCardReaderPINPad());
		txtFldGPGPath.setText(configs.getGPGPath());

		chkOCR.setSelection(configs.shallOCR());
		updateOCREnabled();
		cmbOCRlang.setText(configs.getOCRlang());
		txtFldTessLangPath.setText(configs.getOCRlangPath());

		if (!configs.getDatabaseDriverFileName().equals("")) { //$NON-NLS-1$
			// not using build-in HSQLDB
			chkUseExternalDB.setSelection(true);
			txtFlddatabasedriver.setText(configs.getDatabaseDriverName());
			txtFlddatabasedriverfilename.setText(configs
					.getDatabaseDriverFileName());
			txtFlddatabasename.setText(configs.getDatabaseName());
			txtFlddatabaseserver.setText(configs.getDatabaseServer());
			txtFlddatabasetype.setText(configs.getDatabaseType());
			txtFlddatabaseuser.setText(configs.getDatabaseUser());
			txtflddatabasepassword.setText(configs.getDatabasePassword());

		}

	}

	public String connectDatabaseAndJPA() {

		//

		// JPA
		try {
			persistUtil.connect(getShell());
		} catch (Exception ex) {
			return "\n" + ex.toString(); //$NON-NLS-1$
		}

		return null;

	}

	private void createActions() {
	}

	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager(
				Messages.getString("configWindow.menu")); //$NON-NLS-1$
		return menuManager;
	}

	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);
		return toolBarManager;
	}

	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		statusLineManager.setMessage(null, ""); //$NON-NLS-1$
		return statusLineManager;
	}

	public static void main(String args[]) {
		try {
			configWindow window = new configWindow();
			window.setBlockOnOpen(true);
			window.open();
			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(application.getAppName());
	}

	/**
	 * use this instead of open()! It will load the values from the config file
	 * */
	public void show() {
		setBlockOnOpen(false);
		open();
		loadValuesFromConfigFile();
	}

	protected Point getInitialSize() {
		return new Point(700, 600);
	}
}