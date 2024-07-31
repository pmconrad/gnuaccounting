package GUILayer;

import jargs.gnu.CmdLineParser;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ag.ion.bion.officelayer.application.IOfficeApplication;
import appLayer.appUsage;
import appLayer.application;
import appLayer.client;
import appLayer.configs;
import appLayer.utils;

public class MainWindow extends ApplicationWindow {
	private Action quickStartTutorialAction;
	private Action assetsAction;
	private Action importDocumentsAction;
	private Action usersAction;
	private Action importOpenTrans;
	private Action projectHomePageAction;
	private Action sourceforgePageAction;
	private Action importTimetrackerAction;
	private Action exportVatAnnouncementAction;
	private Action settingsAction;
	private Action importFromBankAction;
	private Action accountDefinitionAction;
	private Action accountsAction;
	private Action importFromWebShopAction;
	private Text txtFldOOoPath;
	private appUsage currentUsage;
	private boolean wasVisible = false;

	static private boolean todoWindowOpen = false;

	private Action configurationAction;
	private Action numbersformatsAction;
	private Action quitAction;
	private Action taxesAction;
	private Action aboutAction;
	private Action templateDesignerAction;
	private static Button todoButton = null;
	private int numPaints = 0;
	private static globalFilterKeyListener globalFilter;
	private Display originalDisplay;

	static private webshopImportWindow webshopimportwindow = null;
	private Action manualAction;
	private Action closeCashAction;
	private Action newAction;
	private Action importContactsAction;
	private Action CSVDATEVAction;
	private Action ASCIIaction;
	private Action checkAction;
	private Action documentsAction;
	private Action gridAction;
	private Action productsAction;
	private Action contactsAction;
	private Action CSVLEXRWAREaction;
	private Action barcoderAction;

	private class quickstartDialog extends MessageDialog {
		@Override
		protected void buttonPressed(int buttonId) {
			// TODO Auto-generated method stub
			super.buttonPressed(buttonId);
			if (buttonId == 0) {
				// 0==yes
				configs.setAcknowledgedQuickstart(true);
				browserWindow bw = new browserWindow(
						"http://www.gnuaccounting.org/quickstart.php"); //$NON-NLS-1$
				bw.open();
			} else if (buttonId == 1) {
				// 1==never
				configs.setAcknowledgedQuickstart(true);
			} else {
				// 2=remind me again
				configs.setAcknowledgedQuickstart(false);

			}
			configs.writeConfiguration();
		}

		/*
		 * @Override protected void setShellStyle(int newShellStyle) { int
		 * myStyle=SWT.CLOSE | SWT.MODELESS| SWT.BORDER | SWT.TITLE;
		 * super.setShellStyle(SWT.CLOSE | SWT.MODELESS| SWT.BORDER |
		 * SWT.TITLE);
		 * 
		 * }
		 */

		public quickstartDialog(Shell parentShell, String dialogTitle,
				Image dialogTitleImage, String dialogMessage,
				int dialogImageType, String[] dialogButtonLabels,
				int defaultIndex) {
			super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
					dialogImageType, dialogButtonLabels, defaultIndex);
			setBlockOnOpen(false);
			setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
		}

	}

	/**
	 * precondition to calling this constructor (!) is having used static
	 * MainWindow.setConfigPath
	 */
	public MainWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();

	}

	protected static void setConfigPath(String configPath) {
		if (configPath == null) {
			configPath = System.getProperty("user.home") + //$NON-NLS-1$
					"/.gnuaccounting/"; //$NON-NLS-1$
		} else {
			// configpath as argument
			if (configPath.startsWith(".")) { //$NON-NLS-1$
				// relative path as argument
				String absolutePath = utils
						.makeRelativePathAbsolute(configPath);
				if (absolutePath != null) {
					configPath = absolutePath;
				}
			}
			if (!configPath.endsWith(File.separator)) {
				configPath = configPath + File.separator;
			}

		}
		client.setConfigPath(configPath);

	}

	public static String getVersionHistory() {
		return application.versionHistory;
	}

	protected Control createContents(Composite parent) {
		parent.getShell().setSize(getInitialSize());

		try {// vista might raise an exception for unsupported ICO file format
			Image image = new Image(getShell().getDisplay(),
					"gnuaccounting.ico"); //$NON-NLS-1$

			/**
			 * ImageLoader loader = new ImageLoader();
			 * loader.load(getClass().getResourceAsStream
			 * ("Idea_SWT_Animation.gif")); Canvas canvas = new
			 * Canvas(shell,SWT.NONE); image = new
			 * Image(display,loader.data[0]);
			 * */
			/*
			 * ImageLoader loader = new ImageLoader();
			 * loader.load(getClass().getResourceAsStream("gnuaccounting.ico"));
			 * image = new Image(getShell().getDisplay(),loader.data[0]);
			 */
			parent.getShell().setImage(image);
		} catch (Exception ex) {
			// ignore if no icon could be set
		}
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FormLayout());

		container.addPaintListener(new PaintListener() {
			public void paintControl(final PaintEvent arg0) {
				if (numPaints == 0) {
					onShow();
					numPaints++;
				}
			}

		});

		currentUsage = new appUsage(client.getClient());

		container.setFont(configs.getDefaultFont());
		container.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(final DisposeEvent e) {
				// application closed
				configs.disposeOfficeApplication();
				currentUsage.save();
				System.err.println(Messages.getString("MainWindow.closing") + application.getAppName()); //$NON-NLS-1$

				// Shutting down the application

				dataLayer.persistUtil.shutdown();

				System.err.println(application.getAppName()
						+ Messages.getString("MainWindow.closed")); //$NON-NLS-1$

				System.exit(0);
			}
		});

		final Button newTransactionButton = new Button(container, SWT.NONE);
		final FormData fd_newTransactionButton = new FormData();
		fd_newTransactionButton.bottom = new FormAttachment(24, 0);
		fd_newTransactionButton.top = new FormAttachment(14, 0);
		fd_newTransactionButton.left = new FormAttachment(0, 15);
		fd_newTransactionButton.right = new FormAttachment(44, 0);
		newTransactionButton.setLayoutData(fd_newTransactionButton);
		newTransactionButton.setFont(configs.getDefaultFont());
		newTransactionButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				newTransactionWizard wizard = new newTransactionWizard();
				WizardDialog dialog = new nonModalWizardDialog(wizard);
				dialog.create();
				dialog.open();

			}
		});
		newTransactionButton.setText(Messages
				.getString("MainWindow.newTransaction")); //$NON-NLS-1$

		todoButton = new Button(container, SWT.NONE);
		final FormData fd_todoButton = new FormData();
		fd_todoButton.bottom = new FormAttachment(64, 0);
		fd_todoButton.top = new FormAttachment(54, 0);
		fd_todoButton.left = new FormAttachment(0, 15);
		fd_todoButton.right = new FormAttachment(44, 0);
		todoButton.setLayoutData(fd_todoButton);
		todoButton.setFont(configs.getDefaultFont());
		todoButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				todoWindow todo = new todoWindow();
				todo.open();
			}
		});
		todoWindow.refreshToDoList();

		final Button btnBookkeeping = new Button(container, SWT.NONE);
		btnBookkeeping.setText(Messages
				.getString("MainWindow.btnBookkeeping.text")); //$NON-NLS-1$
		final FormData fd_btnBookkeeping = new FormData();
		fd_btnBookkeeping.bottom = new FormAttachment(44, 0);
		fd_btnBookkeeping.top = new FormAttachment(34, 0);
		fd_btnBookkeeping.left = new FormAttachment(54, 0);
		fd_btnBookkeeping.right = new FormAttachment(100, -19);
		btnBookkeeping.setLayoutData(fd_btnBookkeeping);
		btnBookkeeping.setFont(configs.getDefaultFont());
		btnBookkeeping.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				accountingEditWindow ae = new accountingEditWindow();
				ae.open();
			}
		});

		Label suggestionsQuestionsBugsLabel;
		suggestionsQuestionsBugsLabel = new Label(container, SWT.NONE);
		final FormData fd_suggestionsQuestionsBugsLabel = new FormData();
		fd_suggestionsQuestionsBugsLabel.right = new FormAttachment(100, -19);
		fd_suggestionsQuestionsBugsLabel.left = new FormAttachment(0, 15);
		fd_suggestionsQuestionsBugsLabel.bottom = new FormAttachment(100, -38);
		fd_suggestionsQuestionsBugsLabel.top = new FormAttachment(74, 0);
		suggestionsQuestionsBugsLabel
				.setLayoutData(fd_suggestionsQuestionsBugsLabel);
		suggestionsQuestionsBugsLabel.setFont(configs.getDefaultFont());
		suggestionsQuestionsBugsLabel.setText(Messages
				.getString("MainWindow.suggestions")); //$NON-NLS-1$

		final Label gnuaccountingLabel = new Label(container, SWT.NONE);
		final FormData fd_gnuaccountingLabel = new FormData();
		fd_gnuaccountingLabel.left = new FormAttachment(newTransactionButton,
				0, SWT.LEFT);
		fd_gnuaccountingLabel.top = new FormAttachment(0, 8);
		fd_gnuaccountingLabel.bottom = new FormAttachment(0, 25);
		gnuaccountingLabel.setLayoutData(fd_gnuaccountingLabel);
		gnuaccountingLabel.setAlignment(SWT.CENTER);
		gnuaccountingLabel.setFont(configs.getDefaultFont());
		gnuaccountingLabel
				.setText(Messages.getString("MainWindow.gnuaccounting") + application.getVersionString()); //$NON-NLS-1$

		Button btnIncoming;
		btnIncoming = new Button(container, SWT.NONE);
		btnIncoming.setText(Messages.getString("MainWindow.btnIncoming.text")); //$NON-NLS-1$
		fd_gnuaccountingLabel.right = new FormAttachment(btnIncoming, 0,
				SWT.RIGHT);
		final FormData fd_btnIncoming = new FormData();
		fd_btnIncoming.bottom = new FormAttachment(24, 0);
		fd_btnIncoming.top = new FormAttachment(14, 0);
		fd_btnIncoming.right = new FormAttachment(100, -19);
		fd_btnIncoming.left = new FormAttachment(54, 0);
		btnIncoming.setLayoutData(fd_btnIncoming);
		btnIncoming.setFont(configs.getDefaultFont());
		btnIncoming.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				incomingWizard iw = new incomingWizard();
				WizardDialog dialog = new nonModalWizardDialog(iw);
				dialog.create();
				dialog.open();

			}
		});

		Button btnDocuments;
		btnDocuments = new Button(container, SWT.NONE);
		btnDocuments
				.setText(Messages.getString("MainWindow.btnDocuments.text")); //$NON-NLS-1$
		final FormData fd_btnDocuments = new FormData();
		fd_btnDocuments.bottom = new FormAttachment(44, 0);
		fd_btnDocuments.top = new FormAttachment(34, 0);
		fd_btnDocuments.left = new FormAttachment(0, 15);
		fd_btnDocuments.right = new FormAttachment(44, 0);
		btnDocuments.setLayoutData(fd_btnDocuments);
		btnDocuments.setFont(configs.getDefaultFont());
		btnDocuments.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				documentsWindow docWin = new documentsWindow();
				docWin.open();
			}
		});
		//

		return container;
	}

	public String getTxtFldOOoPath() {
		return txtFldOOoPath.getText();
	}

	private void createActions() {

		templateDesignerAction = new Action(
				Messages.getString("MainWindow.templateDesigner")) { //$NON-NLS-1$
			public void run() {
				designerWindow designer = new designerWindow();
				designer.open();
			}
		};

		aboutAction = new Action(Messages.getString("MainWindow.about")) { //$NON-NLS-1$
			public void run() {
				aboutWindow abtWindow = new aboutWindow();
				abtWindow.open();
			}
		};

		taxesAction = new Action(Messages.getString("MainWindow.Taxes")) { //$NON-NLS-1$
			public void run() {
				taxesWindow taxWindow = new taxesWindow();
				taxWindow.open();
			}
		};

		quitAction = new Action(Messages.getString("MainWindow.Quit")) { //$NON-NLS-1$
			public void run() {
				close();
			}
		};

		numbersformatsAction = new Action(
				Messages.getString("MainWindow.numbersFormats")) { //$NON-NLS-1$
			public void run() {
				numbersWindow numWindow = new numbersWindow();
				numWindow.open();
			}
		};

		configurationAction = new Action(
				Messages.getString("MainWindow.configuration")) { //$NON-NLS-1$
			public void run() {
				configWindow cfgWindow = new configWindow();
				cfgWindow.show();
			}
		};

		accountsAction = new Action(
				Messages.getString("MainWindow.exportSpread")) { //$NON-NLS-1$
			public void run() {
				reportWizard wizard = new reportWizard(
						reportWizard.format.formatODF);
				if (wizard.isOpenable()) {
					WizardDialog dialog = new WizardDialog(getShell(), wizard);
					dialog.open();
				}
			}
		};

		accountDefinitionAction = new Action(
				Messages.getString("MainWindow.accountDefinition")) { //$NON-NLS-1$
			public void run() {
				accountDefinitionWindow adefWindow = new accountDefinitionWindow();
				adefWindow.open();
			}
		};

		importFromBankAction = new Action(
				Messages.getString("MainWindow.bankAccountImport")) { //$NON-NLS-1$
			public void run() {
				newAccountingWizard wizard = new newAccountingWizard(true);
				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				dialog.open();

			}
		};

		settingsAction = new Action(Messages.getString("MainWindow.Settings")) { //$NON-NLS-1$
			public void run() {
				settingsWindow settings = new settingsWindow();
				settings.open();
			}
		};

		exportVatAnnouncementAction = new Action(
				Messages.getString("MainWindow.exportVAT")) { //$NON-NLS-1$
			public void run() {

				VATannouncementWindow va = new VATannouncementWindow();
				if (va.isOpenable()) {
					va.open();
				}
			}
		};

		importTimetrackerAction = new Action(
				Messages.getString("MainWindow.importKarm")) { //$NON-NLS-1$
			public void run() {
				timetrackerImportWindow kiw = new timetrackerImportWindow();
				kiw.open();
			}
		};

		sourceforgePageAction = new Action(
				Messages.getString("MainWindow.sourceforgePage")) { //$NON-NLS-1$
			public void run() {
				browserWindow b = new browserWindow(
						"http://www.sf.net/projects/gnuaccounting/"); //$NON-NLS-1$
				b.open();
			}
		};

		projectHomePageAction = new Action(
				Messages.getString("MainWindow.projectHomepage")) { //$NON-NLS-1$
			public void run() {
				browserWindow b = new browserWindow(
						"http://gnuaccounting.sf.net/"); //$NON-NLS-1$
				b.open();
			}
		};

		importOpenTrans = new Action(
				Messages.getString("MainWindow.importFromOpenTrans")) { //$NON-NLS-1$
			public void run() {
				openTransImportWindow oiw = new openTransImportWindow();
				oiw.open();
			}
		};

		importFromWebShopAction = new Action(
				Messages.getString("MainWindow.importFromWebShop")) { //$NON-NLS-1$
			public void run() {
				if (webshopimportwindow == null)
					webshopimportwindow = new webshopImportWindow();
				webshopimportwindow.open();
				webshopimportwindow.syncWithWebshop();
			}
		};

		importDocumentsAction = new Action(
				Messages.getString("MainWindow.importDocuments")) { //$NON-NLS-1$
			public void run() {
				documentsWindow dw = new documentsWindow();
				dw.open();
			}
		};

		assetsAction = new Action(
				Messages.getString("MainWindow.assetActionHeading")) { //$NON-NLS-1$
			public void run() {
				assetWindow aw = new assetWindow();
				aw.open();
			}
		};
		usersAction = new Action(
				Messages.getString("MainWindow.manageUsersActionHeading")) { //$NON-NLS-1$
			public void run() {
				userWindow uw = new userWindow();
				uw.open();
			}
		};

		quickStartTutorialAction = new Action(
				Messages.getString("MainWindow.quickstartHeading")) { //$NON-NLS-1$
			public void run() {
				browserWindow bw = new browserWindow(
						"http://www.gnuaccounting.org/quickstart.php"); //$NON-NLS-1$
				bw.open();

			}
		};

		manualAction = new Action(
				Messages.getString("MainWindow.manualAction.text")) { //$NON-NLS-1$
			public void run() {
				viewerWindow v = new viewerWindow(
						getClass()
								.getResource(
										Messages.getString("MainWindow.manualFilename")).toString()); //$NON-NLS-1$
				v.setReadonly();
			
				v.open();

			}
		};
		{
			closeCashAction = new Action(
					Messages.getString("MainWindow.closeCashAction.text")) { //$NON-NLS-1$
				public void run() {
					cashWindow c = new cashWindow(); //$NON-NLS-1$
					c.open();

				}
			};
		}
		{
			newAction = new Action(
					Messages.getString("MainWindow.newAction.text")) { //$NON-NLS-1$
				public void run() {
					newTransactionWizard wizard = new newTransactionWizard();
					WizardDialog dialog = new nonModalWizardDialog(wizard);
					dialog.create();
					dialog.open();
				}
			};
		}
		{
			importContactsAction = new Action(
					Messages.getString("MainWindow.importContactsAction.text")) { //$NON-NLS-1$

				@Override
				public void run() {

					VCFWindow vcf = new VCFWindow();
					vcf.open();

				} //$NON-NLS-1$
			};
		}
		{
			CSVDATEVAction = new Action(
					Messages.getString("MainWindow.action.text")) { //$NON-NLS-1$
				public void run() {
					reportWizard rw = new reportWizard(
							reportWizard.format.formatCSVDATEV);
					if (rw.isOpenable()) {
						WizardDialog dialog = new nonModalWizardDialog(rw);
						dialog.create();
						dialog.open();
					}

				}
			};
		}
		{
			ASCIIaction = new Action(
					Messages.getString("MainWindow.ASCIIaction.text")) { //$NON-NLS-1$
				public void run() {
					reportWizard rw = new reportWizard(
							reportWizard.format.formatASCII);
					if (rw.isOpenable()) {
						WizardDialog dialog = new nonModalWizardDialog(rw);
						dialog.create();
						dialog.open();
					}
				}
			};
		}
		{
			checkAction = new Action(
					Messages.getString("MainWindow.action.text_1")) { //$NON-NLS-1$
				public void run() {
					accountBalanceWindow abw = new accountBalanceWindow();
					abw.open();
				}
			};
		}
		{
			documentsAction = new Action(
					Messages.getString("MainWindow.actionDocuments.text")) { //$NON-NLS-1$
				public void run() {
					documentsWindow dw = new documentsWindow();
					dw.open();
				}

			};
		}
		{
			gridAction = new Action(
					Messages.getString("MainWindow.gridAction.text")) { //$NON-NLS-1$
				public void run() {
					listWindow lw = new listWindow();
					lw.open();
				}
			};
		}
		{
			productsAction = new Action(
					Messages.getString("MainWindow.action.text_2")) { //$NON-NLS-1$

				public void run() {
					productsWindow prodWindow = new productsWindow();
					prodWindow.open();
				}

			};
		}
		{
			contactsAction = new Action(
					Messages.getString("MainWindow.contactsAction.text")) { //$NON-NLS-1$
				public void run() {
					contactsWindow conWindow = new contactsWindow();
					conWindow.open();
				}
			};
		}
		{
			CSVLEXRWAREaction = new Action(
					Messages.getString("MainWindow.CSVLEXRWAREaction.text")) { //$NON-NLS-1$
				public void run() {
					reportWizard rw = new reportWizard(
							reportWizard.format.formatCSVLEXWARE);
					if (rw.isOpenable()) {
						WizardDialog dialog = new nonModalWizardDialog(rw);
						dialog.create();
						dialog.open();
					}
				}

			};
		}
		{
			barcoderAction = new Action(Messages.getString("MainWindow.action.text_3")) { //$NON-NLS-1$
				public void run() {
					barcoderWindow barcoder=new barcoderWindow();
					
					barcoder.open();
				}
			};
		}
	}

	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager(
				Messages.getString("MainWindow.menu")); //$NON-NLS-1$

		final MenuManager menuManager_3 = new MenuManager(
				Messages.getString("MainWindow.file")); //$NON-NLS-1$
		menuManager.add(menuManager_3);
		menuManager_3.add(newAction);
		menuManager_3.add(barcoderAction);

		menuManager_3.add(quitAction);

		final MenuManager menuManager_4 = new MenuManager(
				Messages.getString("MainWindow.interfaces")); //$NON-NLS-1$
		menuManager.add(menuManager_4);

		menuManager_4.add(importFromBankAction);
		menuManager_4.add(importTimetrackerAction);
		menuManager_4.add(importOpenTrans);
		menuManager_4.add(importFromWebShopAction);
		menuManager_4.add(importDocumentsAction);
		menuManager_4.add(importContactsAction);
		menuManager_4.add(new Separator());
		menuManager_4.add(CSVLEXRWAREaction);
		menuManager_4.add(CSVDATEVAction);
		menuManager_4.add(ASCIIaction);
		menuManager_4.add(accountsAction);
		menuManager_4.add(exportVatAnnouncementAction);

		final MenuManager menuManager_1 = new MenuManager(
				Messages.getString("MainWindow.settings")); //$NON-NLS-1$
		menuManager.add(menuManager_1);

		menuManager_1.add(templateDesignerAction);
		menuManager_1.add(taxesAction);
		menuManager_1.add(numbersformatsAction);
		menuManager_1.add(usersAction);
		menuManager_1.add(new Separator());

		menuManager_1.add(assetsAction);
		menuManager_1.add(documentsAction);
		menuManager_1.add(accountDefinitionAction);
		menuManager_1.add(productsAction);
		menuManager_1.add(contactsAction);
		menuManager_1.add(new Separator());
		menuManager_1.add(checkAction);
		menuManager_1.add(gridAction);
		menuManager_1.add(new Separator());
		menuManager_1.add(configurationAction);
		menuManager_1.add(settingsAction);

		final MenuManager menuManager_2 = new MenuManager(
				Messages.getString("MainWindow.help")); //$NON-NLS-1$
		menuManager.add(menuManager_2);
		menuManager_2.add(sourceforgePageAction);
		menuManager_2.add(projectHomePageAction);

		menuManager_2.add(quickStartTutorialAction);
		menuManager_2.add(manualAction);

		menuManager_2.add(aboutAction);
		return menuManager;
	}

	/*
	 * protected ToolBarManager createToolBarManager(int style) { return
	 * toolBarManager; }
	 */

	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		statusLineManager.setMessage(null, ""); //$NON-NLS-1$
		return statusLineManager;
	}

	public static void main(String args[]) {
		// prepare JNI directories before any of the libraries are invoked the first time
		System.setProperty(
				IOfficeApplication.NOA_NATIVE_LIB_PATH,
				System.getProperty("user.dir") + File.separator + "libs" + File.separator + "noa"); // for NOA //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		System.setProperty("jna.library.path", System.getProperty("user.dir") + File.separator + "libs" + File.separator +"tesseract-ocr"); // for Tess4J, i.e. Tesseract  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				

		// more NOA preparations
		System.setProperty("sun.awt.xembedserver", "true"); // for gnome linux, see http://wiki.services.openoffice.org/wiki/OOoBean#nofocus //$NON-NLS-1$ //$NON-NLS-2$
		String configPath = null;
		String officePath = null;
		if (args.length > 0) {

			// configWindow.testandshow will contrsuct main class if tests
			// successful
			CmdLineParser parser = new CmdLineParser();
			CmdLineParser.Option config = parser.addStringOption('c',
					"configPath"); //$NON-NLS-1$
			CmdLineParser.Option office = parser.addStringOption('o',
					"officePath"); //$NON-NLS-1$

			try {
				parser.parse(args);
			} catch (CmdLineParser.OptionException e) {
				System.err.println(Messages.getString("MainWindow.usage")); //$NON-NLS-1$
				e.printStackTrace();
				System.exit(-1);

			}
			configPath = (String) parser.getOptionValue(config);
			officePath = (String) parser.getOptionValue(office);

			if ((configPath == null) && (officePath == null)) {
				System.err.println(Messages.getString("MainWindow.usage")); //$NON-NLS-1$
				System.exit(-1);
			}

			if (officePath != null) {
				configs.setUserDefinedOfficePath(officePath);
			}
		}
		MainWindow.setConfigPath(configPath);// if it's null setConfigPath will
												// set to default
// as of now config values are available
		new configWindow().testAndShow();
	}

	/**
	 * This is only to be called from ToDoWindow. Please invoke
	 * todoWindow.refreshToDoList to update the number of open todo items on the
	 * application start page
	 * */
	public static void updateNrTodoItems(int nr) {
		if (todoButton != null) {
			todoButton
					.setText(Messages.getString("MainWindow.toDoOpen") + Integer.toString(nr) + Messages.getString("MainWindow.toDoClose")); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(application.getAppName());
	}

	protected Point getInitialSize() {
		return new Point(410, 405);
	}

	public static void signalTodoWindowOpen(boolean isOpen) {
		todoWindowOpen = isOpen;
	}

	public static boolean isTodoWindowOpen() {
		return todoWindowOpen;
	}


	private void onShow() {
		if ((!wasVisible) && (!configs.isAcknowledgedQuickstart())) {
			wasVisible = true;// this function is fired twice at startup
			String[] buttonText = new String[] {
					Messages.getString("MainWindow.showQuickstartNow"), Messages.getString("MainWindow.showQuickstartNever"), //$NON-NLS-1$ //$NON-NLS-2$
					Messages.getString("MainWindow.showQuickstartAskAgain") }; //$NON-NLS-1$
			MessageDialog quickstartDialogQuestion;

			quickstartDialogQuestion = new quickstartDialog(
					getShell(),
					Messages.getString("MainWindow.showQuickstartCaption"), null, Messages.getString("MainWindow.showQuickstartText"), //$NON-NLS-1$ //$NON-NLS-2$
					MessageDialog.QUESTION, buttonText, 0);
			quickstartDialogQuestion.open();
		}
	}

	public void setDisplay(Display display) {
		originalDisplay = display;
		globalFilter = new globalFilterKeyListener();
		originalDisplay.addFilter(SWT.KeyDown, globalFilter);

	}

	public static globalFilterKeyListener getGlobalFilter() {
		return globalFilter;
	}
}
