package GUILayer;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import GUILayer.reportWizard.format;
import appLayer.configs;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.events.VerifyEvent;

public class reportWizardFile extends WizardPage {
	private Text txtFilename;

	/**
	 * Create the wizard.
	 */
	public reportWizardFile() {
		super(Messages.getString("reportWizardFile.pageID")); //$NON-NLS-1$
		setTitle(Messages.getString("reportWizardFile.filename")); //$NON-NLS-1$
		setDescription(Messages.getString("reportWizardFile.chooseandfinish")); //$NON-NLS-1$
	}

	public String getFilename() {
		return txtFilename.getText();
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);

		Label lblFilename = new Label(container, SWT.NONE);
		lblFilename.setAlignment(SWT.RIGHT);
		lblFilename.setBounds(60, 57, 112, 20);
		lblFilename.setText(Messages.getString("reportWizardFile.Filename")); //$NON-NLS-1$
		lblFilename.setFont(configs.getDefaultFont());

		txtFilename = new Text(container, SWT.BORDER);
		txtFilename.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent arg0) {
				checkFinish();
			}
		});
		txtFilename.setBounds(178, 51, 187, 26);

		Button btnBrowse = new Button(container, SWT.NONE);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				if (((reportWizard) getWizard()).getFormat() == format.formatASCII) {
					String allowedExtensions[] = { "*.txt" }; //$NON-NLS-1$
					dialog.setFilterExtensions(allowedExtensions);

				}
				if ((((reportWizard) getWizard()).getFormat() == format.formatCSVDATEV)||((reportWizard) getWizard()).getFormat() == format.formatCSVLEXWARE) {
					String allowedExtensions[] = { "*.csv" }; //$NON-NLS-1$
					dialog.setFilterExtensions(allowedExtensions);

				}

				String name = dialog.open();
				if (name != null) {
					txtFilename.setText(name);

					checkFinish();
				}

			}
		});
		btnBrowse.setBounds(371, 47, 90, 30);
		btnBrowse.setText(Messages.getString("reportWizardFile.Browse")); //$NON-NLS-1$
	}

	/*
	 * enables the finish button if a filename has been selected *
	 */
	protected void checkFinish() {
		if (txtFilename.getText().length() > 0) {
			((reportWizard) getWizard()).enableFinish();
			setPageComplete(true); // this will also enable finish
			getWizard().getContainer().updateButtons();

		}

	}
}
