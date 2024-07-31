package GUILayer;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
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

import dataLayer.taskTrakerImporters;

import appLayer.application;
import appLayer.configs;

public class timetrackerImportWindow extends ApplicationWindow {
	private Text txtTaskCoachFilename;
	private Text txtKTimetrackerFilename;
	private Button importButton = null;
	private Button chkRoundOff = null;
	private Text txtGleeoFilename;

	/**
	 * Create the application window
	 */
	public timetrackerImportWindow() {
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
				if (txtKTimetrackerFilename.getText().length() > 0) {
					taskTrakerImporters.importKTimeTracker(
							txtKTimetrackerFilename.getText(),
							chkRoundOff.getSelection(), getStatusLineManager(),
							getShell());
				}
				if (txtTaskCoachFilename.getText().length() > 0) {
					taskTrakerImporters.importTaskCoach(
							txtTaskCoachFilename.getText(),
							chkRoundOff.getSelection(), getStatusLineManager(),
							getShell());
				}
				if (txtGleeoFilename.getText().length() > 0) {
					taskTrakerImporters.importGleeo(txtGleeoFilename.getText(),
							chkRoundOff.getSelection(), getStatusLineManager(),
							getShell());
				}
			}
		});
		importButton.setEnabled(false);
		importButton.setText(Messages
				.getString("timetrackerImportWindow.importButton")); //$NON-NLS-1$
		importButton.setBounds(216, 247, 54, 29);
		importButton.setFont(configs.getDefaultFont());

		final Button btnKTTBrowse = new Button(container, SWT.NONE);
		btnKTTBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				String allowedExtensions[] = { "*.csv" }; //$NON-NLS-1$
				dialog.setFilterExtensions(allowedExtensions);
				String name = dialog.open();
				if (name != null) {
					txtKTimetrackerFilename.setText(name);
					importButton.setEnabled(true);
				}
			}
		});
		btnKTTBrowse.setText(Messages
				.getString("timetrackerImportWindow.browseShortcut")); //$NON-NLS-1$
		btnKTTBrowse.setBounds(391, 85, 54, 29);
		btnKTTBrowse.setFont(configs.getDefaultFont());

		final Label fileLabel = new Label(container, SWT.NONE);
		fileLabel.setAlignment(SWT.RIGHT);
		fileLabel.setText(Messages
				.getString("timetrackerImportWindow.KtimetrackerCSV")); //$NON-NLS-1$
		fileLabel.setBounds(10, 92, 147, 17);
		fileLabel.setFont(configs.getDefaultFont());

		txtKTimetrackerFilename = new Text(container, SWT.BORDER);
		txtKTimetrackerFilename.setBounds(163, 89, 222, 25);
		txtKTimetrackerFilename.setFont(configs.getDefaultFont());

		final Label lblTaskcoach = new Label(container, SWT.NONE);
		lblTaskcoach.setAlignment(SWT.RIGHT);
		lblTaskcoach.setText(Messages
				.getString("timetrackerImportWindow.TaskCoachImportlabel")); //$NON-NLS-1$
		lblTaskcoach.setBounds(10, 128, 147, 17);
		lblTaskcoach.setFont(configs.getDefaultFont());

		txtTaskCoachFilename = new Text(container, SWT.BORDER);
		txtTaskCoachFilename.setBounds(163, 120, 222, 25);
		txtTaskCoachFilename.setFont(configs.getDefaultFont());

		final Button btnTCbrowse = new Button(container, SWT.NONE);
		btnTCbrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				String allowedExtensions[] = { "*.tsk" }; //$NON-NLS-1$
				dialog.setFilterExtensions(allowedExtensions);
				String name = dialog.open();
				if (name != null) {
					txtTaskCoachFilename.setText(name);
					importButton.setEnabled(true);
				}

			}
		});
		btnTCbrowse.setText(Messages
				.getString("timetrackerImportWindow.browseShortcut")); //$NON-NLS-1$
		btnTCbrowse.setBounds(391, 120, 54, 29);
		btnTCbrowse.setFont(configs.getDefaultFont());

		chkRoundOff = new Button(container, SWT.CHECK);
		chkRoundOff.setText(Messages
				.getString("timetrackerImportWindow.roundOff")); //$NON-NLS-1$
		chkRoundOff.setBounds(90, 190, 334, 16);
		chkRoundOff.setFont(configs.getDefaultFont());

		Label lblGleeoTimerCsv = new Label(container, SWT.NONE);
		lblGleeoTimerCsv.setAlignment(SWT.RIGHT);
		lblGleeoTimerCsv.setBounds(10, 55, 147, 20);
		lblGleeoTimerCsv.setText(Messages
				.getString("timetrackerImportWindow.lblGleeoTimerCsv.text")); //$NON-NLS-1$

		lblGleeoTimerCsv.setFont(configs.getDefaultFont());

		Button btnGleeoBrowse = new Button(container, SWT.NONE);
		btnGleeoBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				String allowedExtensions[] = { "*.csv" }; //$NON-NLS-1$
				dialog.setFilterExtensions(allowedExtensions);
				String name = dialog.open();
				if (name != null) {
					txtGleeoFilename.setText(name);
					importButton.setEnabled(true);
				}
			}
		});
		btnGleeoBrowse.setBounds(391, 49, 54, 30);
		btnGleeoBrowse.setText(Messages
				.getString("timetrackerImportWindow.browseShortcut")); //$NON-NLS-1$
		btnGleeoBrowse.setFont(configs.getDefaultFont());

		txtGleeoFilename = new Text(container, SWT.BORDER);
		txtGleeoFilename.setText(""); //$NON-NLS-1$
		txtGleeoFilename.setBounds(163, 53, 222, 26);
		txtGleeoFilename.setFont(configs.getDefaultFont());
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
		return new Point(500, 427);
	}
}
