package GUILayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import appLayer.AccountNotFoundException;
import appLayer.account;
import appLayer.client;
import appLayer.configs;
import dataLayer.CSVImporter;
import dataLayer.HBCIImporter;
import dataLayer.hibiscusImporter;
import dataLayer.moneyplexImporter;
import dataLayer.starmoneyImporter;

public class newAccountingWizardImport extends WizardPage {

	private Text txtFldStarmoneyFilename;
	private DateTime dteHBCIFrom;
	private DateTime dteHBCITo;
	private ComboViewer cmbToAccountViewer = null;

	protected newAccountingWizard parentWizard;

	private Text txtFldMoneyplexFilename;
	private Text txtFldHibiscusFilename;
	private Text txtFldCSVFilename;

	/**
	 * Create the wizard
	 */
	public newAccountingWizardImport(newAccountingWizard parentWizard) {
		super(Messages.getString("newAccountingWizardImport.bookkeeping")); //$NON-NLS-1$
		this.parentWizard = parentWizard;
		setTitle(Messages.getString("newAccountingWizardImport.importBank")); //$NON-NLS-1$
		setDescription(Messages
				.getString("newAccountingWizardImport.selectExportFile")); //$NON-NLS-1$
	}

	public account getSelectedContraAccount() {
		IStructuredSelection isel = (IStructuredSelection) cmbToAccountViewer
				.getSelection();
		return (account) isel.getFirstElement();
	}

	public void performHibiscusImport(String filename)
			throws FileNotFoundException {

		hibiscusImporter hibi = new hibiscusImporter(filename);
		hibi.run();

		try {
			new ProgressMonitorDialog(getShell()).run(true, true, hibi);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void performCSVImport(String filename) throws FileNotFoundException {
		CSVImporter ci = new CSVImporter(filename);
		ci.run();

		try {
			new ProgressMonitorDialog(getShell()).run(true, true, ci);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void performMoneyplexImport(String filename)
			throws FileNotFoundException {

		moneyplexImporter mpi = new moneyplexImporter(filename);
		mpi.run();

		try {
			new ProgressMonitorDialog(getShell()).run(true, true, mpi);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void performStarmoneyImport(String filename)
			throws FileNotFoundException {

		starmoneyImporter smi = new starmoneyImporter(filename);

		try {
			new ProgressMonitorDialog(getShell()).run(true, true, smi);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void performHBCIImport() {

		if (!new File(configs.getCtAPI()).exists()) {
			MessageDialog
					.openError(
							getShell(),
							Messages.getString("newAccountingWizardImport.missingConfig"), Messages.getString("newAccountingWizardImport.specifyChipcardDriver")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			Calendar dateInCalendar = Calendar.getInstance();
			dateInCalendar.set(Calendar.YEAR, dteHBCIFrom.getYear());
			dateInCalendar.set(Calendar.MONTH, dteHBCIFrom.getMonth());
			dateInCalendar.set(Calendar.DAY_OF_MONTH, dteHBCIFrom.getDay());

			Date from = dateInCalendar.getTime();

			dateInCalendar.set(Calendar.YEAR, dteHBCITo.getYear());
			dateInCalendar.set(Calendar.MONTH, dteHBCITo.getMonth());
			dateInCalendar.set(Calendar.DAY_OF_MONTH, dteHBCITo.getDay());

			Date to = dateInCalendar.getTime();
			if (from.getTime() == to.getTime()) {
				MessageDialog
						.openError(
								getShell(),
								Messages.getString("newAccountingWizardImport.wrongoption"), Messages.getString("newAccountingWizardImport.timeframeAtLeastOneDay")); //$NON-NLS-1$ //$NON-NLS-2$

			} else {
				HBCIImporter hbcii = new HBCIImporter(getShell(), from, to);

				try {
					new ProgressMonitorDialog(getShell())
							.run(true, true, hbcii);
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				getWizard().getContainer().showPage(getNextPage());

			}

		}

	}

	public String getMoneyplexImportFilename() {
		return txtFldMoneyplexFilename.getText();
	}

	public String getStarmoneyImportFilename() {
		return txtFldStarmoneyFilename.getText();
	}

	public String getHibiscusImportFilename() {
		return txtFldHibiscusFilename.getText();
	}

	public String getCSVImportFilename() {
		return txtFldCSVFilename.getText();
	}

	/**
	 * Create contents of the wizard
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		container.setFont(configs.getDefaultFont());
		//
		setControl(container);

		txtFldMoneyplexFilename = new Text(container, SWT.BORDER);
		txtFldMoneyplexFilename.setFont(configs.getDefaultFont());
		txtFldMoneyplexFilename.setBounds(191, 10, 176, 25);

		final Button btnBrowseMoneyplex = new Button(container, SWT.NONE);
		btnBrowseMoneyplex.setFont(configs.getDefaultFont());
		btnBrowseMoneyplex.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {

				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				String allowedExtensions[] = { "*.xml" }; //$NON-NLS-1$
				dialog.setFilterExtensions(allowedExtensions);
				String name = dialog.open();
				if (name != null) {
					txtFldMoneyplexFilename.setText(name);
				}
			}
		});
		btnBrowseMoneyplex.setText(Messages
				.getString("newAccountingWizardImport.browse")); //$NON-NLS-1$
		btnBrowseMoneyplex.setBounds(382, 10, 89, 24);

		final Label lblMoneyplexXML = new Label(container, SWT.NONE);
		lblMoneyplexXML.setFont(configs.getDefaultFont());
		lblMoneyplexXML.setText(Messages
				.getString("newAccountingWizardImport.MoneyplexXML")); //$NON-NLS-1$
		lblMoneyplexXML.setBounds(10, 18, 176, 17);

		final Label lblStarmoneyTXT = new Label(container, SWT.NONE);
		lblStarmoneyTXT.setFont(configs.getDefaultFont());
		lblStarmoneyTXT.setBounds(10, 90, 175, 17);
		lblStarmoneyTXT.setText(Messages
				.getString("newAccountingWizardImport.orStarMoneyTXT")); //$NON-NLS-1$

		txtFldStarmoneyFilename = new Text(container, SWT.BORDER);
		txtFldStarmoneyFilename.setFont(configs.getDefaultFont());
		txtFldStarmoneyFilename.setBounds(191, 82, 176, 25);

		final Button btnBrowseStarmoney = new Button(container, SWT.NONE);
		btnBrowseStarmoney.setFont(configs.getDefaultFont());
		btnBrowseStarmoney.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				String allowedExtensions[] = { "*.txt" }; //$NON-NLS-1$
				dialog.setFilterExtensions(allowedExtensions);
				String name = dialog.open();
				if (name != null) {
					txtFldStarmoneyFilename.setText(name);
				}

			}
		});
		btnBrowseStarmoney.setBounds(382, 82, 89, 24);
		btnBrowseStarmoney.setText(Messages
				.getString("newAccountingWizardImport.browse")); //$NON-NLS-1$

		final Button btnImport = new Button(container, SWT.NONE);
		btnImport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				performHBCIImport();
			}
		});
		btnImport.setText("import"); //$NON-NLS-1$
		btnImport.setFont(configs.getDefaultFont());
		btnImport.setBounds(382, 162, 88, 25);

		final Label orHbciImportLabel = new Label(container, SWT.NONE);
		orHbciImportLabel.setText(Messages
				.getString("newAccountingWizardImport.hbciImportAlternative")); //$NON-NLS-1$
		orHbciImportLabel.setBounds(10, 143, 176, 17);
		orHbciImportLabel.setFont(configs.getDefaultFont());

		dteHBCITo = new DateTime(container, SWT.DATE | SWT.DROP_DOWN);
		dteHBCITo.setBounds(252, 160, 115, 27);

		dteHBCIFrom = new DateTime(container, SWT.DATE | SWT.DROP_DOWN);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		dteHBCIFrom.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
				cal.get(Calendar.DAY_OF_MONTH));
		dteHBCIFrom.setBounds(94, 160, 115, 27);

		final Label lblFrom = new Label(container, SWT.NONE);
		lblFrom.setText(Messages
				.getString("newAccountingWizardImport.periodFrom")); //$NON-NLS-1$
		lblFrom.setBounds(20, 166, 66, 15);
		lblFrom.setFont(configs.getDefaultFont());

		final Label lblTo = new Label(container, SWT.NONE);
		lblTo.setText(Messages.getString("newAccountingWizardImport.periodTo")); //$NON-NLS-1$
		lblTo.setBounds(215, 166, 30, 15);
		lblTo.setFont(configs.getDefaultFont());

		txtFldHibiscusFilename = new Text(container, SWT.BORDER);
		txtFldHibiscusFilename.setBounds(191, 113, 176, 25);

		Button btnBrowseHibiscus = new Button(container, SWT.NONE);
		btnBrowseHibiscus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				String allowedExtensions[] = { "*.xml" }; //$NON-NLS-1$
				dialog.setFilterExtensions(allowedExtensions);
				String name = dialog.open();
				if (name != null) {
					txtFldHibiscusFilename.setText(name);
				}

			}

		});
		btnBrowseHibiscus.setBounds(382, 112, 89, 25);
		btnBrowseHibiscus.setText(Messages
				.getString("newAccountingWizardImport.btnBrowse.text")); //$NON-NLS-1$
		btnBrowseHibiscus.setFont(configs.getDefaultFont());

		Label lblHibiscus = new Label(container, SWT.NONE);
		lblHibiscus.setBounds(10, 122, 176, 15);
		lblHibiscus.setText(Messages
				.getString("newAccountingWizardImport.lblHibiscus.text")); //$NON-NLS-1$
		lblHibiscus.setFont(configs.getDefaultFont());

		Label lblCSVtxt = new Label(container, SWT.NONE);
		lblCSVtxt.setBounds(10, 53, 176, 20);
		lblCSVtxt.setFont(configs.getDefaultFont());
		lblCSVtxt.setText(Messages
				.getString("newAccountingWizardImport.lblCSV.text")); //$NON-NLS-1$

		txtFldCSVFilename = new Text(container, SWT.BORDER);
		txtFldCSVFilename.setText(""); //$NON-NLS-1$
		txtFldCSVFilename.setBounds(191, 50, 176, 26);
		txtFldCSVFilename.setFont(configs.getDefaultFont());

		Button btnBrowseCSV = new Button(container, SWT.NONE);
		btnBrowseCSV.setFont(configs.getDefaultFont());
		btnBrowseCSV.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				String allowedExtensions[] = { "*.csv" }; //$NON-NLS-1$
				dialog.setFilterExtensions(allowedExtensions);
				String name = dialog.open();
				if (name != null) {
					txtFldCSVFilename.setText(name);
				}

			}
		});
		btnBrowseCSV.setBounds(382, 46, 90, 30);
		btnBrowseCSV.setText(Messages
				.getString("newAccountingWizardImport.btnBrowse.text_1")); //$NON-NLS-1$

		Label lblToAccount = new Label(container, SWT.NONE);
		lblToAccount.setBounds(10, 207, 176, 20);
		lblToAccount.setText(Messages
				.getString("newAccountingWizardImport.lblToAccount.text")); //$NON-NLS-1$
		lblToAccount.setFont(configs.getDefaultFont());

		cmbToAccountViewer = new ComboViewer(container, SWT.READ_ONLY);
		Combo cmbToAccount = cmbToAccountViewer.getCombo();

		cmbToAccount.setBounds(191, 199, 280, 27);
		cmbToAccount.setFont(configs.getDefaultFont());
		cmbToAccountViewer.setContentProvider(ArrayContentProvider
				.getInstance());
		cmbToAccountViewer.setInput(client.getAccounts().getList(false));

		try {
			cmbToAccountViewer.setSelection(new StructuredSelection(client
					.getAccounts().getBankAccount()));
		} catch (AccountNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
}