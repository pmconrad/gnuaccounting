package GUILayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolTip;

import ag.ion.bion.officelayer.document.DocumentException;
import ag.ion.bion.officelayer.filter.PDFFilter;
import ag.ion.bion.officelayer.filter.PDFFilterProperties;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.noa.text.TextRangeSelection;
import appLayer.client;
import appLayer.configs;

public class newTransactionWizardPrintAndSave extends WizardPage {
	private newTransactionWizard parentWizard;
	private Button chkPrint = null;
	private Button chkEmail = null;
	private Button chkBalanced = null;
	private Combo cmbCopies = null;
	private Label lblCopies = null;

	public int getNumPrints() {
		if (!chkPrint.getSelection()) {
			return 0;
		} else {
			return Integer.valueOf(cmbCopies.getText());
		}
	}

	public boolean isEmailChecked() {
		return chkEmail.getSelection();
	}

	public boolean isBalancedChecked() {
		return chkBalanced.getSelection();
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "printAndSave"; //$NON-NLS-1$
		// return super.getName();
	}

	public newTransactionWizardPrintAndSave(newTransactionWizard parent) {
		super(Messages
				.getString("newTransactionWizardPrintAndSave.transaction")); //$NON-NLS-1$
		parentWizard = parent;
		setTitle(Messages
				.getString("newTransactionWizardPrintAndSave.newTransaction")); //$NON-NLS-1$
		setDescription(Messages
				.getString("newTransactionWizardPrintAndSave.additionalActions")); //$NON-NLS-1$
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		container.setLayout(new FormLayout());
		//
		setControl(container);

		chkPrint = new Button(container, SWT.CHECK);
		chkPrint.setFont(configs.getDefaultFont());
		chkPrint.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (chkPrint.getSelection()) {
					cmbCopies.setEnabled(true);
					lblCopies.setEnabled(true);
				} else {
					cmbCopies.setEnabled(false);
					lblCopies.setEnabled(false);

				}

			}
		});
		final FormData formData = new FormData();
		formData.top = new FormAttachment(0, 15);
		formData.left = new FormAttachment(0, 20);
		chkPrint.setLayoutData(formData);
		chkPrint.setText(Messages
				.getString("newTransactionWizardPrintAndSave.print")); //$NON-NLS-1$

		chkEmail = new Button(container, SWT.CHECK);
		chkEmail.setFont(configs.getDefaultFont());
		final FormData fd_chkEmail = new FormData();
		fd_chkEmail.top = new FormAttachment(chkPrint, 5, SWT.BOTTOM);
		fd_chkEmail.left = new FormAttachment(chkPrint, 0, SWT.LEFT);
		chkEmail.setText(Messages
				.getString("newTransactionWizardPrintAndSave.sendEmail")); //$NON-NLS-1$
		chkEmail.setLayoutData(fd_chkEmail);

		chkBalanced = new Button(container, SWT.CHECK);
		chkBalanced.setFont(configs.getDefaultFont());
		FormData fd_chkBalanced = new FormData();
		fd_chkBalanced.top = new FormAttachment(chkEmail, 6);
		fd_chkBalanced.left = new FormAttachment(chkPrint, 0, SWT.LEFT);
		chkBalanced.setLayoutData(fd_chkBalanced);
		chkBalanced
				.setText(Messages
						.getString("newTransactionWizardPrintAndSave.btnPaidInCash.text")); //$NON-NLS-1$

		cmbCopies = new Combo(container, SWT.READ_ONLY);
		FormData fd_cmbCopies = new FormData();
		fd_cmbCopies.bottom = new FormAttachment(chkPrint, 0, SWT.BOTTOM);
		fd_cmbCopies.left = new FormAttachment(chkPrint, 21);
		cmbCopies.setLayoutData(fd_cmbCopies);
		cmbCopies.setFont(configs.getDefaultFont());
		cmbCopies.add(Messages
				.getString("newTransactionWizardPrintAndSave.NumCopiesOne")); //$NON-NLS-1$
		cmbCopies.add(Messages
				.getString("newTransactionWizardPrintAndSave.NumCopiesTwo")); //$NON-NLS-1$
		cmbCopies.add(Messages
				.getString("newTransactionWizardPrintAndSave.NumCopiesThree")); //$NON-NLS-1$
		cmbCopies.add(Messages
				.getString("newTransactionWizardPrintAndSave.NumCopiesFour")); //$NON-NLS-1$
		cmbCopies.add(Messages
				.getString("newTransactionWizardPrintAndSave.NumCopiesFive")); //$NON-NLS-1$
		cmbCopies.setText(Messages
				.getString("newTransactionWizardPrintAndSave.NumCopiesOne")); //$NON-NLS-1$
		cmbCopies.setEnabled(false);

		lblCopies = new Label(container, SWT.NONE);
		FormData fd_lblCopies = new FormData();
		fd_lblCopies.top = new FormAttachment(chkPrint, 0, SWT.TOP);
		fd_lblCopies.left = new FormAttachment(cmbCopies, 24);
		lblCopies.setLayoutData(fd_lblCopies);
		lblCopies
				.setText(Messages
						.getString("newTransactionWizardPrintAndSave.lblNumCopies.text")); //$NON-NLS-1$
		lblCopies.setFont(configs.getDefaultFont());
		lblCopies.setEnabled(false);
	}

	@Override
	public IWizardPage getNextPage() {
		try {
			
			parentWizard
					.getDocument()
					.getPersistenceService()
					.store("file:///" + client.getTransactions().getCurrentTransaction().getFilenameODT()); //$NON-NLS-1$
			PDFFilter pdfFilter = PDFFilter.FILTER;
			PDFFilterProperties pdfFilterProperties = pdfFilter.getPDFFilterProperties();
			pdfFilterProperties.setPdfVersion(1);
			/* this will export as PDF/A-1-a which is a subformat of PDF 1.4...
			 * and which we will later upgrade to PDF A-3-a to store Zugferd*/
			parentWizard
					.getDocument()
					.getPersistenceService()
					.export("file:///" + client.getTransactions().getCurrentTransaction().getFilenamePDF(), pdfFilter); //$NON-NLS-1$
			
			if (client.getTransactions().getCurrentTransaction()
					.getWorkflowStep() == 1) {
				// step 1= just created the invoice (step 2 could e.g. be just balanced or cancelled the invoice)
				client.getTransactions().getCurrentTransaction().export();
			}

		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (configs.getGPGPath().length() > 0) {
			try {
				Process p = Runtime
						.getRuntime()
						.exec(configs.getGPGPath()
								+ System.getProperty("file.separator") + "gpg -s " + client.getTransactions().getCurrentTransaction().getFilenamePDF()); //$NON-NLS-1$ //$NON-NLS-2$
				String line;

				BufferedReader input = new BufferedReader(
						new InputStreamReader(p.getInputStream()));
				while ((line = input.readLine()) != null) {
					System.out.println(line);
				}
				input.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return super.getNextPage();
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			try {
				// set selection somewhere outside of the table to make table
				// toolbar disapear if selection was put to table by user
				ITextDocument document = parentWizard.getDocument();
				if (document != null)
					document.setSelection(new TextRangeSelection(document
							.getTextService().getText().getTextCursorService()
							.getTextCursor().getStart()));
			} catch (Exception e) {
				// TODO: handle exception
			}

			parentWizard.setCanFinish(true);

			ToolTip tlpWhyNoMail = new ToolTip(getShell(), SWT.BALLOON
					| SWT.ICON_INFORMATION);
			String whyNoMail = ""; //$NON-NLS-1$
			if (configs.getSMTPServer().length() == 0) {

				whyNoMail += Messages
						.getString("newTransactionWizardPrintAndSave.warningNoSMTPServerConfigured") + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (configs.getSenderEmail().length() == 0) {
				whyNoMail += Messages
						.getString("newTransactionWizardPrintAndSave.warningNoSenderMailConfigured") + "\n"; //$NON-NLS-1$ //$NON-NLS-2$

			}
			if ((client.getTransactions().getCurrentTransaction().getContact()
					.getEmail() == null)
					|| (client.getTransactions().getCurrentTransaction()
							.getContact().getEmail().length() == 0)) {
				whyNoMail += Messages
						.getString("newTransactionWizardPrintAndSave.noticeNoRecipientEmailSet") + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
			}

			if (whyNoMail.length() > 0) {
				tlpWhyNoMail
						.setText(Messages
								.getString("newTransactionWizardPrintAndSave.cantSendMailBalloonHeading")); //$NON-NLS-1$
				tlpWhyNoMail.setMessage(whyNoMail);
				chkEmail.setEnabled(false);

				tlpWhyNoMail.setLocation(chkEmail.getLocation().x
						+ getShell().getLocation().x + 20,
						chkEmail.getLocation().y + getShell().getLocation().y
								+ 109);
				tlpWhyNoMail.setVisible(true);
			}

		}
		super.setVisible(visible);
	}
}
