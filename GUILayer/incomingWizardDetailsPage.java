package GUILayer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.kapott.hbci.manager.HBCIUtils;

import uk.co.mmscomputing.device.scanner.Scanner;
import uk.co.mmscomputing.device.scanner.ScannerIOException;
import uk.co.mmscomputing.device.scanner.ScannerIOMetadata;
import uk.co.mmscomputing.device.scanner.ScannerListener;
import appLayer.AccountNotFoundException;
import appLayer.client;
import appLayer.configs;
import appLayer.contact;
import appLayer.document;
import appLayer.payment;
import appLayer.products;
import appLayer.utils;
import appLayer.taxRelated.tax;
import appLayer.transactionRelated.invoice;
import appLayer.transactionRelated.receiptIncoming;
import dataLayer.HBCI;

public class incomingWizardDetailsPage extends WizardPage {
	private Text txtAmount;
	private Text txtPurpose;
	private Text txtReference;
	private Text txtAccount;
	private Text txtHolder;
	private Text txtBankName;
	private Text txtBankCode;
	private Label lblBankName;
	private Label lblAccount;
	private Label lblHolder;
	private Label lblBankCode;
	private Button chkBalance;
	private ComboViewer cmbVATViewer;
	private ComboViewer cmbSenderViewer;
	private Combo cmbType;
	private DateTime dteSend;
	private document transactionDocument = null;
	private Scanner scanner = null;

	/**
	 * Create the wizard.
	 */
	public incomingWizardDetailsPage() {
		super("incomingWizard"); //$NON-NLS-1$
		setTitle(Messages
				.getString("incomingWizardDetailsPage.titleNewIncomingTransaction")); //$NON-NLS-1$
		setDescription(Messages
				.getString("incomingWizardDetailsPage.invoicedetails")); //$NON-NLS-1$

	}

	protected void checkPageComplete() {
		setPageComplete(txtAmount.getText().length() > 0);
	}

	protected void checkTransferPossible() {
		boolean isPossible = false;
		if ((txtAmount.getText().length() > 0)
				&& (txtPurpose.getText().length() > 0)
				&& (txtBankName.getText().length() > 0)) {
			if ((configs.getBankCode().length() > 0)
					&& (configs.getAccountCode().length() > 0)) {
				isPossible = true;

			} else {
				String baloonText;

				if (configs.getCtAPI().length() == 0) {
					baloonText = Messages
							.getString("incomingWizardDetailsPage.bankTransferChipcard"); //$NON-NLS-1$

				} else {
					baloonText = Messages
							.getString("incomingWizardDetailsPage.bankTransferDetails"); //$NON-NLS-1$

				}

				ToolTip tlpWhyNoTransfer = new ToolTip(getShell(), SWT.BALLOON
						| SWT.ICON_INFORMATION);

				tlpWhyNoTransfer.setText(baloonText);
				tlpWhyNoTransfer.setLocation(chkBalance.getLocation().x
						+ getShell().getLocation().x + 20,
						chkBalance.getLocation().y + getShell().getLocation().y
								+ 140);
				tlpWhyNoTransfer.setVisible(true);
				isPossible = false;

			}

		}

		txtPurpose.setEnabled(isPossible);
		chkBalance.setEnabled(isPossible);
		checkPageComplete();
	}

	protected void checkBankData() {
		Color black = new Color(getShell().getDisplay(), 0x00, 0x00, 0x00);
		Color red = new Color(getShell().getDisplay(), 0x80, 0x00, 0x00);
		// we need to invoke HBCI.getInstance otherwise the static HBCIUtils
		// functions won't be able to
		// connect to their ressources (like localized strings)
		HBCI.getInstance(getShell());
		lblBankCode.setForeground(black);
		lblAccount.setForeground(black);

		if ((txtBankCode.getText().length() > 0)
				|| (txtAccount.getText().length() > 0)) {

			lblBankCode.setForeground(red);
			lblAccount.setForeground(red);
		}
		updateBank();
		checkTransferPossible();
	}

	private void updateBank() {
		Color green = new Color(getShell().getDisplay(), 0x00, 0x80, 0x00);
		String bankName;
		HBCI.getInstance(getShell());
		if ((txtBankCode.getText().length() > 0)
				&& (txtAccount.getText().length() > 0)) {

			bankName = HBCIUtils.getNameForBLZ(txtBankCode.getText());
			if (bankName.length() != 0) {
				lblBankCode.setForeground(green);
				// if bank code is not correct, account code can neither be
				// checked nor be correct
				if (HBCIUtils.checkAccountCRC(txtBankCode.getText(),
						txtAccount.getText())) {
					lblAccount.setForeground(green);
				}
				txtBankName.setText(bankName);
			}
		} else {
			txtBankName.setText(""); //$NON-NLS-1$

		}

	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		client.getTransactions().setCurrentTransactionByTypeID(
				receiptIncoming.getType());

		if (visible) {
			txtReference.setText(client.getTransactions()
					.getCurrentTransaction().getNextNumberString());

			txtPurpose.setText(Messages
					.getString("incomingWizardDetailsPage.purposeTemplate") //$NON-NLS-1$
					+ client.getTransactions().getCurrentTransaction()
							.getNextNumberString());

		}
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);

		Label lblRecipient = new Label(container, SWT.NONE);
		lblRecipient.setAlignment(SWT.RIGHT);
		lblRecipient.setBounds(0, 16, 120, 20);
		lblRecipient.setText(Messages
				.getString("incomingWizardDetailsPage.sender")); //$NON-NLS-1$
		lblRecipient.setFont(configs.getDefaultFont());

		Label lblAmount = new Label(container, SWT.NONE);
		lblAmount.setAlignment(SWT.RIGHT);
		lblAmount.setBounds(0, 122, 120, 20);
		lblAmount.setText(Messages
				.getString("incomingWizardDetailsPage.amount")); //$NON-NLS-1$
		lblAmount.setFont(configs.getDefaultFont());

		txtAmount = new Text(container, SWT.BORDER);
		txtAmount.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				updateDocumentNumberValue();
			}
		});
		txtAmount.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				checkTransferPossible();
			}
		});
		txtAmount.setBounds(135, 117, 78, 26);
		txtAmount.setFont(configs.getDefaultFont());

		txtPurpose = new Text(container, SWT.BORDER);
		txtPurpose.setEnabled(false);
		txtPurpose.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				checkTransferPossible();
			}
		});
		txtPurpose.setBounds(135, 213, 457, 26);
		txtPurpose.setFont(configs.getDefaultFont());

		Label lblPurpose = new Label(container, SWT.NONE);
		lblPurpose.setAlignment(SWT.RIGHT);
		lblPurpose.setBounds(10, 221, 110, 20);
		lblPurpose.setText(Messages
				.getString("incomingWizardDetailsPage.purpose")); //$NON-NLS-1$
		lblPurpose.setFont(configs.getDefaultFont());

		Label lblReference = new Label(container, SWT.RIGHT);
		lblReference.setBounds(0, 91, 120, 20);
		lblReference.setText(Messages
				.getString("incomingWizardDetailsPage.reference")); //$NON-NLS-1$
		lblReference.setFont(configs.getDefaultFont());

		txtReference = new Text(container, SWT.BORDER);
		txtReference.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				updateDocumentNumberValue();
			}
		});
		txtReference.setBounds(135, 83, 78, 26);
		txtReference.setFont(configs.getDefaultFont());

		lblAccount = new Label(container, SWT.RIGHT);
		lblAccount.setBounds(0, 266, 120, 20);
		lblAccount.setText(Messages
				.getString("incomingWizardDetailsPage.account")); //$NON-NLS-1$
		lblAccount.setFont(configs.getDefaultFont());

		lblHolder = new Label(container, SWT.RIGHT);
		lblHolder.setBounds(0, 313, 120, 20);
		lblHolder.setText(Messages
				.getString("incomingWizardDetailsPage.holder")); //$NON-NLS-1$
		lblHolder.setFont(configs.getDefaultFont());

		Label lblVat = new Label(container, SWT.NONE);
		lblVat.setAlignment(SWT.RIGHT);
		lblVat.setBounds(235, 119, 173, 20);
		lblVat.setText(Messages.getString("incomingWizardDetailsPage.VAT")); //$NON-NLS-1$
		lblVat.setFont(configs.getDefaultFont());

		lblBankName = new Label(container, SWT.NONE);
		lblBankName.setAlignment(SWT.RIGHT);
		lblBankName.setBounds(0, 391, 129, 20);
		lblBankName.setText(Messages
				.getString("incomingWizardDetailsPage.bankName")); //$NON-NLS-1$
		lblBankName.setFont(configs.getDefaultFont());

		chkBalance = new Button(container, SWT.CHECK);
		chkBalance.setEnabled(false);
		chkBalance.setBounds(135, 417, 213, 20);
		chkBalance.setText(Messages
				.getString("incomingWizardDetailsPage.Balance")); //$NON-NLS-1$
		chkBalance.setFont(configs.getDefaultFont());

		Link lnkManageContacts = new Link(container, SWT.NONE);
		lnkManageContacts.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				contactsWindow cw = new contactsWindow();
				cw.open();
			}
		});
		lnkManageContacts.setBounds(365, 14, 130, 20);
		lnkManageContacts.setFont(configs.getDefaultFont());
		lnkManageContacts.setText(Messages
				.getString("incomingWizardDetailsPage.manageContacts")); //$NON-NLS-1$

		cmbSenderViewer = new ComboViewer(container, SWT.READ_ONLY);
		Combo cmbSender = cmbSenderViewer.getCombo();
		cmbSender.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				IStructuredSelection selection = (IStructuredSelection) cmbSenderViewer
						.getSelection();
				contact selectedContact = (contact) selection.getFirstElement();
				if (selectedContact != null) {
					if ((selectedContact.getBankAccount() != null)
							&& (selectedContact.getAccountholder() != null)) {
						txtAccount.setText(selectedContact.getBankAccount());
						txtHolder.setText(selectedContact.getAccountholder());
						txtBankCode.setText(selectedContact.getBankCode());
						checkBankData();

					}
				}
			}
		});
		cmbSender.setFont(configs.getDefaultFont());
		cmbSender.setBounds(131, 10, 217, 27);
		cmbSenderViewer.setContentProvider(ArrayContentProvider.getInstance());
		cmbSenderViewer.setInput(client.getContacts().getExistingContacts());

		cmbVATViewer = new ComboViewer(container, SWT.READ_ONLY);
		cmbVATViewer.setContentProvider(ArrayContentProvider.getInstance());
		cmbVATViewer.setInput(client.getTaxes().getExistingVATArray());

		Combo cmbVAT = cmbVATViewer.getCombo();
		cmbVATViewer.setSelection(new StructuredSelection(client.getTaxes()
				.getStandardVAT()));

		cmbVAT.setFont(configs.getDefaultFont());
		cmbVAT.setBounds(432, 115, 160, 27);

		txtAccount = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		txtAccount.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				MessageDialog.open(
						SWT.ERROR,
						getShell(),
						Messages.getString("incomingWizardDetailsPage.impossible"), //$NON-NLS-1$
						Messages.getString("incomingWizardDetailsPage.useContactMng"), //$NON-NLS-1$
						SWT.NONE);

			}
		});
		txtAccount.setBounds(135, 258, 213, 26);
		txtAccount.setFont(configs.getDefaultFont());

		txtHolder = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		txtHolder.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				MessageDialog.open(
						SWT.ERROR,
						getShell(),
						Messages.getString("incomingWizardDetailsPage.impossible"), //$NON-NLS-1$
						Messages.getString("incomingWizardDetailsPage.useContactMng"), //$NON-NLS-1$
						SWT.NONE);

			}
		});
		txtHolder.setBounds(135, 307, 213, 26);
		txtHolder.setFont(configs.getDefaultFont());

		txtBankName = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		txtBankName.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				MessageDialog.open(
						SWT.ERROR,
						getShell(),
						Messages.getString("incomingWizardDetailsPage.impossible"), //$NON-NLS-1$
						Messages.getString("incomingWizardDetailsPage.useContactMng"), //$NON-NLS-1$
						SWT.NONE);
			}
		});
		txtBankName.setBounds(135, 385, 213, 26);
		txtBankName.setFont(configs.getDefaultFont());

		txtBankCode = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		txtBankCode.setBounds(135, 350, 213, 26);
		txtBankCode.setFont(configs.getDefaultFont());

		lblBankCode = new Label(container, SWT.NONE);
		lblBankCode.setAlignment(SWT.RIGHT);
		lblBankCode.setBounds(0, 355, 129, 20);
		lblBankCode.setText(Messages
				.getString("incomingWizardDetailsPage.bankCode")); //$NON-NLS-1$
		lblBankCode.setFont(configs.getDefaultFont());

		Label dateLabel = new Label(container, SWT.NONE);
		dateLabel.setAlignment(SWT.RIGHT);
		dateLabel.setBounds(0, 50, 120, 20);
		dateLabel.setText(Messages
				.getString("incomingWizardDetailsPage.lblDate.text")); //$NON-NLS-1$
		dateLabel.setFont(configs.getDefaultFont());

		dteSend = new DateTime(container, SWT.BORDER);
		dteSend.setBounds(135, 42, 149, 28);
		dteSend.setFont(configs.getDefaultFont());

		Label lblDocument = new Label(container, SWT.NONE);
		lblDocument.setAlignment(SWT.RIGHT);
		lblDocument.setBounds(10, 159, 110, 20);
		lblDocument.setText(Messages
				.getString("incomingWizardDetailsPage.lblDocument.text")); //$NON-NLS-1$
		lblDocument.setFont(configs.getDefaultFont());

		Button btnDocBrowse = new Button(container, SWT.NONE);
		btnDocBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				String allowedExtensions[] = {
						"*.pdf", "*.jpg", "*.jpeg", "*.png" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				dialog.setFilterExtensions(allowedExtensions);
				String name = dialog.open();
				if (name != null) {

					String fileName = dialog.getFilterPath() + File.separator
							+ dialog.getFileName();

					getShell()
							.setCursor(
									new Cursor(getShell().getDisplay(),
											SWT.CURSOR_WAIT));

					transactionDocument=new document();
					transactionDocument.setImportFilename(fileName, getShell());
					getShell().setCursor(
							new Cursor(getShell().getDisplay(),
									SWT.CURSOR_ARROW));

					transactionDocument.parseMetadata();

					if (transactionDocument.getMetaAmount() != null) {
						/* no bezahlcode but meta data? Possibly doctag... */
						txtAmount.setText(utils.currencyFormat(
								transactionDocument.getMetaAmount(), '.'));
					}
					if (transactionDocument.getMetaPurpose() != null) {
						txtPurpose.setText(transactionDocument.getMetaPurpose());
					}
					if (transactionDocument.getMetaIBAN() != null) {
						txtAccount.setText(transactionDocument.getMetaIBAN());
					}
					if (transactionDocument.getMetaBIC() != null) {
						txtBankCode.setText(transactionDocument.getMetaBIC());
					}
					if (transactionDocument.getMetaHolder() != null) {
						txtHolder.setText(transactionDocument.getMetaHolder());
					}
					if (transactionDocument.getMetaBankName() != null) {
						txtBankName.setText(transactionDocument.getMetaBankName());
					}
					updateBank();
					checkPageComplete();

				}
			}

		});
		btnDocBrowse.setBounds(135, 149, 90, 30);
		btnDocBrowse.setText(Messages
				.getString("incomingWizardDetailsPage.btnBrowseButton.text")); //$NON-NLS-1$
		btnDocBrowse.setFont(configs.getDefaultFont());

		Button btnDocScan = new Button(container, SWT.NONE);
		btnDocScan.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					scanner.acquire();

				} catch (ScannerIOException ex) {
					ex.printStackTrace();
				}

			}
		});
		btnDocScan.setBounds(231, 149, 90, 30);
		btnDocScan.setText(Messages
				.getString("incomingWizardDetailsPage.btnScanButton.text")); //$NON-NLS-1$
		btnDocScan.setFont(configs.getDefaultFont());

		cmbType = new Combo(container, SWT.READ_ONLY);
		cmbType.setBounds(432, 83, 160, 28);
		cmbType.setFont(configs.getDefaultFont());
		cmbType.setItems(products.types);

		// now we add the services we can consume, but not provide
		cmbType.add(Messages
				.getString("incomingWizardDetailsPage.officialEntertainment")); //$NON-NLS-1$
		cmbType.add(Messages.getString("incomingWizardDetailsPage.telecommunications"));  //$NON-NLS-1$
		// done

		cmbType.select(0);// default select: services

		Label lblType = new Label(container, SWT.NONE);
		lblType.setAlignment(SWT.RIGHT);
		lblType.setBounds(338, 91, 70, 20);
		lblType.setText(Messages
				.getString("incomingWizardDetailsPage.lblNewLabel.text")); //$NON-NLS-1$
		lblType.setFont(configs.getDefaultFont());
		transactionDocument=new document();

		transactionDocument = client.getDocuments().getNewDocument(
				client.getTransactions().getCurrentTransaction()
						.getFilenamePDF());
		transactionDocument.setOriginalFilename(client.getTransactions()
				.getCurrentTransaction().getFilenameODT());
		updateDocumentNumberValue();

		try {
			scanner = Scanner.getDevice();
		} catch (UnsatisfiedLinkError e1) {
			// no scan support installed,e.g. Linux w/o sane
		}
		String scannerName = configs.getScannerName();

		if (scanner != null) {// if at least one scanner, more precisely SANE or
								// TWAIN driver is installed
			if ((scannerName != null) && (scannerName.length() > 0)) {
				try {
					scanner.select(scannerName);
				} catch (ScannerIOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			scanner.addListener(new ScannerListener() {
				public void update(ScannerIOMetadata.Type type,
						ScannerIOMetadata metadata) {
					if (ScannerIOMetadata.ACQUIRED.equals(type)) {
						/*
						 * make reference copy here to avoid race condition --
						 * as seen in uk.co.mmscomputing.application.imageviewer
						 */
						final BufferedImage scannedImage = metadata.getImage();
						/*
						 * obviously we need a separate thread to save the files
						 * (also stolen from imageviewer application, also
						 * available on
						 * http://www.mms-computing.co.uk/uk/co/mmscomputing
						 * /application/imageviewer/index.php) otherwise when
						 * scanning from ADF(automated document feed) only the
						 * first page is saved and the rest is feeded and
						 * ejected w/o being scanned or transmitted.
						 */
						scanSavePicThread sspt = new scanSavePicThread(
								scannedImage, getShell(), new IscanProgress() {

									@Override
									public void onComplete(String filename,
											String docNr) {
										transactionDocument
												.setImportFilename(filename, getShell());
									}
								});
						/*
						 * as we want to access the gui while the thread is
						 * running (to update the list of documents) we start
						 * the thread not via sspt.run but via
						 * getShell().getDisplay().syncExec(sspt); synchronously
						 * to the main GUI thread
						 */
						getShell().getDisplay().asyncExec(sspt);
					}
				}
			});
		} else {
			// no scanner found
		}

		checkPageComplete();
	}

	private void updateDocumentNumberValue() {

		transactionDocument.setNumber(txtReference.getText());

		transactionDocument.setValue(utils.String2BD(txtAmount.getText()));
	}

	public void book() {
		IStructuredSelection taxSelection = (IStructuredSelection) cmbVATViewer
				.getSelection();
		tax selectedTAX = (tax) taxSelection.getFirstElement();
		if (client.getTransactions().getCurrentTransaction().getDocument() != null) {
			client.getTransactions().getCurrentTransaction().getDocument()
					.setValue(utils.String2BD(txtAmount.getText()));
		}

		IStructuredSelection conSelection = (IStructuredSelection) cmbSenderViewer
				.getSelection();
		contact selectedContact = (contact) conSelection.getFirstElement();
		if (selectedContact != null) {
			client.getTransactions().getCurrentTransaction()
					.setContact(selectedContact);
		}

		GregorianCalendar cal = new GregorianCalendar();
		cal.set(dteSend.getYear(), dteSend.getMonth(), dteSend.getDay());
		client.getTransactions().setCurrentTransactionByTypeID(receiptIncoming.getType());
		receiptIncoming ri = (receiptIncoming) client.getTransactions()
				.getCurrentTransaction();
		ri.setIssueDate(cal.getTime());

		ri.setPurpose(cmbType.getSelectionIndex());
		ri.setDefaultReference(txtReference.getText());

		ri.setVAT(selectedTAX);
		try {
			ri.setDefaultDebitAccount(client.getAccounts().getPayableAccount());
			ri.setDefaultCreditAccount(client.getAccounts().getBankAccount());
		} catch (AccountNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ri.setGrossValue(utils.String2BD(txtAmount.getText()));
		ri.setPaymentPurpose(txtPurpose.getText());

		client.getTransactions().getCurrentTransaction().save();

		if (chkBalance.getSelection()) {
			payment p = new payment(getShell());
			p.perform(client.getTransactions().getCurrentTransaction());

		}
		client.getTransactions().getCurrentTransaction()
				.getNewTransactionNumber();

		client.getTransactions().getCurrentTransaction()
				.setDocument(transactionDocument);

		client.getTransactions().finishCurrentTransaction(false);

		/*
		 * assume when the user clicks "new transaction" he does not
		 * neccessarily want to create another incoming one:
		 */
		client.getTransactions().setCurrentTransactionByTypeID(
				invoice.getType());

	}

	public void processDocument() {
		if ((transactionDocument!=null)&&(transactionDocument.getImportFilename()!=null)) {
			transactionDocument.copyOver();
			transactionDocument.save();
			
		}
		
	}
}
