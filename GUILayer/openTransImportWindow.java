package GUILayer;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import dataLayer.openTransImporter;

import appLayer.application;
import appLayer.configs;

public class openTransImportWindow extends ApplicationWindow {
	private Text txtOpenTransFilename;
	private Button importButton = null;

	/**
	 * Create the application window
	 */
	public openTransImportWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
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
		importButton = new Button(container, SWT.NONE);
		importButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (txtOpenTransFilename.getText().length() > 0) {
					openTransImporter oi = new openTransImporter(
							txtOpenTransFilename.getText());
					try {
						new ProgressMonitorDialog(getShell()).run(true, true,
								oi);
					} catch (InvocationTargetException ite) {
						// TODO Auto-generated catch block
						ite.printStackTrace();
					} catch (InterruptedException ie) {
						// TODO Auto-generated catch block
						ie.printStackTrace();
					}
					try {
						oi.join();
					} catch (InterruptedException ie) {
						// TODO Auto-generated catch block
						ie.printStackTrace();
					}

					newTransactionWizard wizard = new newTransactionWizard();
					wizard.getWizardSelect().checkPageComplete();
					WizardDialog dialog = new WizardDialog(null, wizard);
					dialog.open();

					// entryDetailWindow edw=new
					// entryDetailWindow(oi.getEntriesToImport());
					// edw.open();

				}
			}
		});
		importButton.setEnabled(false);
		importButton
				.setText(Messages.getString("OpenTransImportWindow.import")); //$NON-NLS-1$
		importButton.setBounds(216, 247, 54, 29);
		importButton.setFont(configs.getDefaultFont());

		final Button btnOBDXBrowse = new Button(container, SWT.NONE);
		btnOBDXBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				String allowedExtensions[] = { "*.xml" }; //$NON-NLS-1$
				dialog.setFilterExtensions(allowedExtensions);
				String name = dialog.open();
				if (name != null) {
					txtOpenTransFilename.setText(name);
					importButton.setEnabled(true);
				}
			}
		});
		btnOBDXBrowse.setText(Messages
				.getString("OpenTransImportWindow.browseButton")); //$NON-NLS-1$
		btnOBDXBrowse.setBounds(391, 85, 54, 29);
		btnOBDXBrowse.setFont(configs.getDefaultFont());

		final Label fileLabel = new Label(container, SWT.NONE);
		fileLabel.setText(Messages
				.getString("OpenTransImportWindow.openTransFilenamePrompt")); //$NON-NLS-1$
		fileLabel.setBounds(38, 92, 119, 17);
		fileLabel.setFont(configs.getDefaultFont());

		txtOpenTransFilename = new Text(container, SWT.BORDER);
		txtOpenTransFilename.setBounds(163, 89, 222, 25);
		txtOpenTransFilename.setFont(configs.getDefaultFont());
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
		MenuManager menuManager = new MenuManager("menu"); //$NON-NLS-1$
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
	 * Configure the shell
	 * 
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(application.getAppName());
	}

	/**
	 * Return the initial size of the window
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 375);
	}

}
