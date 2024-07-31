package GUILayer;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Vector;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;

import appLayer.CashFlow;
import appLayer.account;
import appLayer.application;
import appLayer.client;
import appLayer.configs;
import appLayer.contact;
import appLayer.document;
import appLayer.payment;
import appLayer.utils;
import appLayer.taxRelated.tax;
import appLayer.taxRelated.taxNotFoundException;
import appLayer.transactionRelated.appTransaction;

public class transactionDetailWindow extends ApplicationWindow {
	// this win is invoked from the drop "More" queue of the
	private Combo cmbCashflow;
	private Combo cmbReference;
	private Table tblVAT;
	private ComboViewer cmbContact;
	private Text txtComment;
	private ComboViewer cmbDebit;
	private ComboViewer cmbCredit;
	private Text txtValue;
	private Text txtDescription;
	protected Button balanceButton = null;
	private Label lblGrossValue = null;
	private Vector<appTransaction> transactionsToProcess;
	private int entryIndex = 0;
	private Label lblEntryNum;
	private Button btnLast, btnFirst, btnPrev, btnNext;
	private DateTime dteIssue;
	private Button btnProcess;
	private int maxEntryIndex = 0;
	private HashMap<Integer, TableItem> VATIDTableItemHashmap = new HashMap<Integer, TableItem>();
	private Button chkBalance;
	private boolean autoUpdateGrossAmount = false;

	/**
	 * Create the application window
	 */
	public transactionDetailWindow(Vector<appTransaction> toProcess) {
		super(null);
		transactionsToProcess = toProcess;
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
		maxEntryIndex = transactionsToProcess.size() - 1;

	}

	@Override
	protected boolean canHandleShellCloseEvent() {
		if (!txtValue.getText().equals("0")) { //$NON-NLS-1$
			if (!MessageDialog
					.openQuestion(
							getShell(),
							Messages.getString("transactionDetailWindow.confirmCloseCaption"), Messages.getString("transactionDetailWindow.confirmCloseText"))) { //$NON-NLS-1$ //$NON-NLS-2$
				return false;
			}
		}
		return super.canHandleShellCloseEvent();
	}

	/**
	 * by default, this screen show imported transactions with fixed gross
	 * amount if a new transaction is to be entered e.g. via the new transaction
	 * button of the transactionDetailWindow, the gross amount has to be
	 * calculated from the (editable) net which can be activated here
	 * */
	public void autoUpdateGrossAmount(boolean doUpdate) {
		autoUpdateGrossAmount = doUpdate;
	}
	private void checkProcessable() {
		btnProcess.setEnabled((txtValue.getText().length()>0)&&(!cmbCredit.getSelection().isEmpty())&&(!cmbDebit.getSelection().isEmpty()));
	}

	/**
	 * Create contents of the application window
	 * 
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		parent.getShell().setSize(getInitialSize());
		ScrolledComposite scrolledContainer = new ScrolledComposite(parent,
				SWT.H_SCROLL | SWT.V_SCROLL);
		Composite container = new Composite(scrolledContainer, SWT.None);
		scrolledContainer.setContent(container);
		container.setLayout(new FormLayout());
		scrolledContainer.setExpandHorizontal(true);
		// vertically, we want scrollbars, no resize, so no
		// scrolledContainer.setExpandVertical(true); here

		int leftColWidth = 250;
		int rightMargin = -50;
		int middleMargin = 10;

		btnPrev = new Button(container, SWT.ARROW | SWT.LEFT);
		final FormData fd_btnPrev = new FormData();
		fd_btnPrev.bottom = new FormAttachment(0, 39);
		fd_btnPrev.top = new FormAttachment(0, 10);
		fd_btnPrev.right = new FormAttachment(0, 124);
		fd_btnPrev.left = new FormAttachment(0, 70);
		btnPrev.setLayoutData(fd_btnPrev);
		btnPrev.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent arg0) {
				updateTransactionFromControls();
				entryIndex--;
				updateControlsFromEntry();
			}

		});
		btnPrev.setText("button"); //$NON-NLS-1$
		btnPrev.setFont(configs.getDefaultFont());

		btnNext = new Button(container, SWT.ARROW | SWT.RIGHT);
		final FormData fd_btnNext = new FormData();
		fd_btnNext.bottom = new FormAttachment(0, 39);
		fd_btnNext.top = new FormAttachment(0, 10);
		fd_btnNext.right = new FormAttachment(100, -64);
		fd_btnNext.left = new FormAttachment(100, -118);
		btnNext.setLayoutData(fd_btnNext);
		btnNext.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent arg0) {
				updateTransactionFromControls();
				entryIndex++;
				updateControlsFromEntry();
			}
		});
		btnNext.setText("button"); //$NON-NLS-1$
		btnNext.setFont(configs.getDefaultFont());

		btnFirst = new Button(container, SWT.NONE);
		final FormData fd_btnFirst = new FormData();
		fd_btnFirst.bottom = new FormAttachment(0, 39);
		fd_btnFirst.top = new FormAttachment(0, 10);
		fd_btnFirst.right = new FormAttachment(0, 64);
		fd_btnFirst.left = new FormAttachment(0, 10);
		btnFirst.setLayoutData(fd_btnFirst);
		btnFirst.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent arg0) {
				updateTransactionFromControls();
				entryIndex = 0;
				updateControlsFromEntry();
			}
		});
		btnFirst.setText(Messages
				.getString("transactionDetailWindow.firstEntryButtonCaption")); //$NON-NLS-1$
		btnFirst.setFont(configs.getDefaultFont());

		btnLast = new Button(container, SWT.NONE);
		final FormData fd_btnLast = new FormData();
		fd_btnLast.bottom = new FormAttachment(0, 39);
		fd_btnLast.top = new FormAttachment(0, 10);
		fd_btnLast.right = new FormAttachment(100, -5);
		fd_btnLast.left = new FormAttachment(100, -59);
		btnLast.setLayoutData(fd_btnLast);
		btnLast.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent arg0) {
				updateTransactionFromControls();
				entryIndex = maxEntryIndex;
				updateControlsFromEntry();
			}
		});
		btnLast.setText(Messages
				.getString("transactionDetailWindow.lastEntryButtonCaption")); //$NON-NLS-1$
		btnLast.setFont(configs.getDefaultFont());

		lblEntryNum = new Label(container, SWT.CENTER);
		lblEntryNum.setFont(configs.getDefaultFont());
		final FormData fd_lblEntryNum = new FormData();
		fd_lblEntryNum.bottom = new FormAttachment(0, 35);
		fd_lblEntryNum.top = new FormAttachment(0, 10);

		fd_lblEntryNum.left = new FormAttachment(50, 0);

		lblEntryNum.setLayoutData(fd_lblEntryNum);

		final Label dateLabel = new Label(container, SWT.RIGHT);
		final FormData fd_dateLabel = new FormData();
		fd_dateLabel.top = new FormAttachment(0, 55);
		fd_dateLabel.right = new FormAttachment(0, leftColWidth - middleMargin);
		fd_dateLabel.left = new FormAttachment(0, 0);
		dateLabel.setLayoutData(fd_dateLabel);
		dateLabel.setText(Messages.getString("transactionDetailWindow.date")); //$NON-NLS-1$
		dateLabel.setFont(configs.getDefaultFont());

		final Label descriptionLabel = new Label(container, SWT.RIGHT);
		final FormData fd_descriptionLabel = new FormData();
		fd_descriptionLabel.top = new FormAttachment(0, 88);
		fd_descriptionLabel.right = new FormAttachment(0, leftColWidth
				- middleMargin);
		fd_descriptionLabel.left = new FormAttachment(0, 0);
		descriptionLabel.setLayoutData(fd_descriptionLabel);
		descriptionLabel.setText(Messages
				.getString("transactionDetailWindow.description")); //$NON-NLS-1$
		descriptionLabel.setFont(configs.getDefaultFont());

		Label valueLabel;
		valueLabel = new Label(container, SWT.RIGHT);
		final FormData fd_valueLabel = new FormData();
		fd_valueLabel.top = new FormAttachment(0, 126);
		fd_valueLabel.right = new FormAttachment(0, leftColWidth - middleMargin);
		fd_valueLabel.left = new FormAttachment(0, 0);
		valueLabel.setLayoutData(fd_valueLabel);
		valueLabel.setText(Messages.getString("transactionDetailWindow.value")); //$NON-NLS-1$
		valueLabel.setFont(configs.getDefaultFont());

		final Label creditAccountLabel = new Label(container, SWT.RIGHT);
		final FormData fd_creditAccountLabel = new FormData();
		fd_creditAccountLabel.right = new FormAttachment(0, leftColWidth
				- middleMargin);
		fd_creditAccountLabel.bottom = new FormAttachment(0, 238);
		fd_creditAccountLabel.left = new FormAttachment(0, 0);
		creditAccountLabel.setLayoutData(fd_creditAccountLabel);
		creditAccountLabel.setText(Messages
				.getString("transactionDetailWindow.creditAccount")); //$NON-NLS-1$
		creditAccountLabel.setFont(configs.getDefaultFont());

		Label debitAccountLabel;
		debitAccountLabel = new Label(container, SWT.RIGHT);
		final FormData fd_debitAccountLabel = new FormData();
		fd_debitAccountLabel.right = new FormAttachment(0, leftColWidth
				- middleMargin);
		fd_debitAccountLabel.left = new FormAttachment(0, 0);
		debitAccountLabel.setLayoutData(fd_debitAccountLabel);
		debitAccountLabel.setText(Messages
				.getString("transactionDetailWindow.debitAccount")); //$NON-NLS-1$
		debitAccountLabel.setFont(configs.getDefaultFont());

		Label vatLabel;
		vatLabel = new Label(container, SWT.RIGHT);
		final FormData fd_vatLabel = new FormData();
		fd_vatLabel.top = new FormAttachment(0, 296);
		fd_vatLabel.right = new FormAttachment(0, leftColWidth - middleMargin);
		fd_vatLabel.left = new FormAttachment(0, 0);
		vatLabel.setLayoutData(fd_vatLabel);
		vatLabel.setText(Messages.getString("transactionDetailWindow.VAT")); //$NON-NLS-1$
		vatLabel.setFont(configs.getDefaultFont());

		Label lblComment;
		lblComment = new Label(container, SWT.RIGHT);
		final FormData fd_lblComment = new FormData();
		fd_lblComment.top = new FormAttachment(0, 445);
		fd_lblComment.right = new FormAttachment(0, leftColWidth - middleMargin);
		fd_lblComment.left = new FormAttachment(0, 0);
		lblComment.setLayoutData(fd_lblComment);
		lblComment.setText(Messages
				.getString("transactionDetailWindow.comment")); //$NON-NLS-1$
		lblComment.setFont(configs.getDefaultFont());

		dteIssue = new DateTime(container, SWT.DATE | SWT.DROP_DOWN);
		final FormData fd_dteIssue = new FormData();
		fd_dteIssue.bottom = new FormAttachment(0, 76);
		fd_dteIssue.top = new FormAttachment(0, 49);
		fd_dteIssue.right = new FormAttachment(0, leftColWidth + 200);
		fd_dteIssue.left = new FormAttachment(0, leftColWidth);
		dteIssue.setLayoutData(fd_dteIssue);
		dteIssue.setFont(configs.getDefaultFont());

		txtDescription = new Text(container, SWT.BORDER);
		final FormData fd_txtDescription = new FormData();
		fd_txtDescription.bottom = new FormAttachment(0, 109);
		fd_txtDescription.top = new FormAttachment(0, 82);
		fd_txtDescription.right = new FormAttachment(100, rightMargin);
		fd_txtDescription.left = new FormAttachment(0, leftColWidth);
		txtDescription.setLayoutData(fd_txtDescription);
		txtDescription.setFont(configs.getDefaultFont());

		txtValue = new Text(container, SWT.BORDER);
		final FormData fd_txtValue = new FormData();
		fd_txtValue.bottom = new FormAttachment(0, 148);
		fd_txtValue.top = new FormAttachment(0, 121);
		fd_txtValue.right = new FormAttachment(0, leftColWidth + 100);
		fd_txtValue.left = new FormAttachment(0, leftColWidth);
		txtValue.setLayoutData(fd_txtValue);
		txtValue.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent arg0) {
				checkProcessable();
			}
		});
		txtValue.setFont(configs.getDefaultFont());

		cmbDebit = new ComboViewer(container, SWT.READ_ONLY);
		Combo combo = cmbDebit.getCombo();
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkProcessable();
			}

		});
		fd_debitAccountLabel.top = new FormAttachment(cmbDebit.getCombo(), 3,
				SWT.TOP);
		final FormData fd_cmbDebit = new FormData();
		fd_cmbDebit.left = new FormAttachment(0, leftColWidth);
		fd_cmbDebit.right = new FormAttachment(100, rightMargin);
		fd_cmbDebit.bottom = new FormAttachment(0, 206);
		cmbDebit.getCombo().setLayoutData(fd_cmbDebit);
		cmbDebit.getCombo().setFont(configs.getDefaultFont());
		cmbDebit.setContentProvider(ArrayContentProvider.getInstance());
		cmbDebit.setInput(client.getAccounts().getCurrentChart()
				.getAccounts(false));
		// cmbDebit.setItems(client.getAccounts().getStringArray());
		// cmbDebit.select(0);
		cmbCredit = new ComboViewer(container, SWT.READ_ONLY);
		Combo combo_1 = cmbCredit.getCombo();
		combo_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkProcessable();
			}
		});
		fd_creditAccountLabel.top = new FormAttachment(cmbCredit.getCombo(), 3,
				SWT.TOP);
		final FormData fd_cmbCredit = new FormData();
		fd_cmbCredit.top = new FormAttachment(0, 215);
		fd_cmbCredit.left = new FormAttachment(0, leftColWidth);
		fd_cmbCredit.right = new FormAttachment(100, rightMargin);
		cmbCredit.getCombo().setLayoutData(fd_cmbCredit);
		cmbCredit.getCombo().setFont(configs.getDefaultFont());
		cmbCredit.setContentProvider(ArrayContentProvider.getInstance());
		cmbCredit.setInput(client.getAccounts().getCurrentChart()
				.getAccounts(false));

		txtComment = new Text(container, SWT.BORDER);
		final FormData fd_txtComment = new FormData();
		fd_txtComment.bottom = new FormAttachment(0, 468);
		fd_txtComment.top = new FormAttachment(0, 441);
		fd_txtComment.right = new FormAttachment(100, rightMargin);
		fd_txtComment.left = new FormAttachment(0, leftColWidth);
		txtComment.setLayoutData(fd_txtComment);
		txtComment.setFont(configs.getDefaultFont());

		cmbReference = new Combo(container, SWT.NONE);
		final FormData fd_cmbReference = new FormData();
		fd_cmbReference.bottom = new FormAttachment(0, 504);
		fd_cmbReference.top = new FormAttachment(0, 480);
		fd_cmbReference.right = new FormAttachment(0, leftColWidth + 200);
		fd_cmbReference.left = new FormAttachment(0, leftColWidth);
		cmbReference.setLayoutData(fd_cmbReference);
		cmbReference.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent arg0) {
				updateContactAndCashflowFromReference();
			}
		});
		cmbReference.setFont(configs.getDefaultFont());

		Label lblContact;
		lblContact = new Label(container, SWT.RIGHT);
		final FormData fd_lblContact = new FormData();
		fd_lblContact.top = new FormAttachment(0, 520);
		fd_lblContact.right = new FormAttachment(0, leftColWidth - middleMargin);
		fd_lblContact.left = new FormAttachment(0, 0);
		lblContact.setLayoutData(fd_lblContact);
		lblContact.setText(Messages
				.getString("transactionDetailWindow.contact")); //$NON-NLS-1$
		lblContact.setFont(configs.getDefaultFont());

		cmbContact = new ComboViewer(container, SWT.READ_ONLY);
		final FormData fd_cmbContact = new FormData();
		fd_cmbContact.bottom = new FormAttachment(0, 541);
		fd_cmbContact.top = new FormAttachment(0, 517);
		fd_cmbContact.right = new FormAttachment(100, rightMargin);
		fd_cmbContact.left = new FormAttachment(0, leftColWidth);
		cmbContact.getCombo().setLayoutData(fd_cmbContact);
		cmbContact.getCombo().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent arg0) {
				checkPaymentPossible();
			}
		});
		cmbContact.getCombo().setFont(configs.getDefaultFont());
		cmbContact.setContentProvider(ArrayContentProvider.getInstance());
		cmbContact.setInput(client.getContacts().getContacts());

		chkBalance = new Button(container, SWT.CHECK);
		final FormData fd_chkBalance = new FormData();
		fd_chkBalance.bottom = new FormAttachment(0, 569);
		fd_chkBalance.top = new FormAttachment(0, 547);
		fd_chkBalance.right = new FormAttachment(100, rightMargin);
		fd_chkBalance.left = new FormAttachment(0, leftColWidth);
		chkBalance.setLayoutData(fd_chkBalance);
		chkBalance.setText(Messages
				.getString("transactionDetailWindow.balanceButton")); //$NON-NLS-1$
		chkBalance.setFont(configs.getDefaultFont());
		chkBalance.setEnabled(false);

		btnProcess = new Button(container, SWT.NONE);
		final FormData fd_btnProcess = new FormData();
		fd_btnProcess.bottom = new FormAttachment(0, 613);
		fd_btnProcess.top = new FormAttachment(0, 584);
		fd_btnProcess.right = new FormAttachment(0, leftColWidth + 200);
		fd_btnProcess.left = new FormAttachment(0, leftColWidth);
		btnProcess.setLayoutData(fd_btnProcess);
		btnProcess.setFont(configs.getDefaultFont());
		btnProcess.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent arg0) {
				appTransaction theTransaction = updateTransactionFromControls();

				theTransaction.book();
				theTransaction.removeFromImport(true);
				client.getEntries().getEntriesFromDatabase();
				accountingEditWindow.refreshAccountingEntries();

				if (chkBalance.isEnabled() && chkBalance.getSelection()) {

					IStructuredSelection selection = (IStructuredSelection) cmbContact
							.getSelection();
					contact selectedContact = (contact) selection
							.getFirstElement();
					theTransaction.setContact(selectedContact);
					theTransaction.setPaymentPurpose(cmbReference.getText());
					theTransaction.save();
					payment p = new payment(getShell());
					p.perform(theTransaction);
					chkBalance.setEnabled(false);
				}
				transactionsToProcess.remove(entryIndex);
				if ((theTransaction.getDefaultCreditAccount()
						.isAssetsDeductable())
						|| (theTransaction.getDefaultDebitAccount()
								.isAssetsDeductable())) {
					assetWindow a = new assetWindow();
					a.open();
				}
				if (transactionsToProcess.size() == 0) {
					close();
				} else {
					updateControlsFromEntry();
				}
			}
		});
		btnProcess.setText(Messages
				.getString("transactionDetailWindow.buttonDoProcessCaption")); //$NON-NLS-1$

		Label lblReference;
		lblReference = new Label(container, SWT.RIGHT);
		final FormData fd_lblReference = new FormData();
		fd_lblReference.top = new FormAttachment(0, 487);
		fd_lblReference.right = new FormAttachment(0, leftColWidth
				- middleMargin);
		fd_lblReference.left = new FormAttachment(0, 0);
		lblReference.setLayoutData(fd_lblReference);
		lblReference.setText(Messages
				.getString("transactionDetailWindow.reference")); //$NON-NLS-1$
		lblReference.setFont(configs.getDefaultFont());

		tblVAT = new Table(container, SWT.FULL_SELECTION | SWT.BORDER);
		final FormData fd_tblVAT = new FormData();
		fd_tblVAT.top = new FormAttachment(0, 290);
		fd_tblVAT.right = new FormAttachment(100, rightMargin);
		fd_tblVAT.left = new FormAttachment(0, leftColWidth);
		tblVAT.setLayoutData(fd_tblVAT);
		tblVAT.setLinesVisible(true);
		tblVAT.setHeaderVisible(true);
		final TableEditor editor = new TableEditor(tblVAT);
		// The editor must have the same size as the cell and must
		// not be any smaller than 50 pixels.
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 50;
		// editing the second column
		final int EDITABLECOLUMN = 1;

		tblVAT.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Clean up any previous editor control
				Control oldEditor = editor.getEditor();
				if (oldEditor != null)
					oldEditor.dispose();

				// Identify the selected row
				TableItem item = (TableItem) e.item;
				if (item == null)
					return;

				// The control that will be the editor must be a child of the
				// Table
				Text newEditor = new Text(tblVAT, SWT.NONE);
				newEditor.setText(item.getText(EDITABLECOLUMN));
				newEditor.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent me) {
						Text text = (Text) editor.getEditor();
						editor.getItem()
								.setText(EDITABLECOLUMN, text.getText());
						updateGrossValue();
					}
				});
				newEditor.selectAll();
				newEditor.setFocus();
				editor.setEditor(newEditor, item, EDITABLECOLUMN);
			}
		});

		final TableColumn newColumnTableColumn = new TableColumn(tblVAT,
				SWT.NONE);
		newColumnTableColumn.setWidth(100);
		newColumnTableColumn.setText(Messages
				.getString("transactionDetailWindow.vat")); //$NON-NLS-1$

		final TableColumn newColumnTableColumn_1 = new TableColumn(tblVAT,
				SWT.NONE);
		newColumnTableColumn_1.setWidth(100);
		newColumnTableColumn_1.setText(Messages
				.getString("transactionDetailWindow.amount")); //$NON-NLS-1$
		client.getDocuments().getDocumentsFromDatabase();

		final Label lblGrossValueLabel = new Label(container, SWT.RIGHT);
		final FormData fd_lblGrossValueLabel = new FormData();
		fd_lblGrossValueLabel.top = new FormAttachment(0, 159);
		fd_lblGrossValueLabel.right = new FormAttachment(0, leftColWidth
				- middleMargin);
		fd_lblGrossValueLabel.left = new FormAttachment(0, 0);
		lblGrossValueLabel.setLayoutData(fd_lblGrossValueLabel);
		lblGrossValueLabel.setFont(configs.getDefaultFont());
		lblGrossValueLabel.setText(Messages
				.getString("transactionDetailWindow.grossAmountLabel")); //$NON-NLS-1$

		lblGrossValue = new Label(container, SWT.NONE);
		fd_cmbDebit.top = new FormAttachment(lblGrossValue, 6);
		final FormData fd_lblGrossValueValue = new FormData();
		fd_lblGrossValueValue.top = new FormAttachment(0, 159);
		fd_lblGrossValueValue.right = new FormAttachment(100, rightMargin);
		fd_lblGrossValueValue.left = new FormAttachment(0, leftColWidth);
		lblGrossValue.setLayoutData(fd_lblGrossValueValue);
		lblGrossValue.setFont(configs.getDefaultFont());
		lblGrossValue.setText("Label"); //$NON-NLS-1$

		final Label lblCashflow = new Label(container, SWT.RIGHT);
		final FormData fd_lblCashflow = new FormData();
		fd_lblCashflow.top = new FormAttachment(0, 250);
		fd_lblCashflow.right = new FormAttachment(0, leftColWidth
				- middleMargin);
		fd_lblCashflow.left = new FormAttachment(0, 0);
		lblCashflow.setLayoutData(fd_lblCashflow);
		lblCashflow.setFont(configs.getDefaultFont());
		lblCashflow.setText(Messages
				.getString("transactionDetailWindow.cashFlowLabel")); //$NON-NLS-1$

		cmbCashflow = new Combo(container, SWT.READ_ONLY);
		fd_cmbCredit.bottom = new FormAttachment(cmbCashflow, -4);
		final FormData fd_cmbCashflow = new FormData();
		fd_cmbCashflow.bottom = new FormAttachment(0, 270);
		fd_cmbCashflow.top = new FormAttachment(0, 246);
		fd_cmbCashflow.right = new FormAttachment(0, leftColWidth + 200);
		fd_cmbCashflow.left = new FormAttachment(0, leftColWidth);
		cmbCashflow.setLayoutData(fd_cmbCashflow);
		cmbCashflow.add(Messages
				.getString("transactionDetailWindow.cashFlowSpendingOption")); //$NON-NLS-1$
		cmbCashflow.add(Messages
				.getString("transactionDetailWindow.cashFlowReceivingOption")); //$NON-NLS-1$
		cmbCashflow.setFont(configs.getDefaultFont());
		for (String unlinkedTransactionNumber : client.getDocuments()
				.getUnlinkedNumbers()) {
			cmbReference.add(unlinkedTransactionNumber);
		}

		for (tax currentTax : client.getTaxes().getVATArray()) {
			if (currentTax.getID() != tax.newTaxID) {
				TableItem item = new TableItem(tblVAT, SWT.NONE);
				VATIDTableItemHashmap.put(currentTax.getID(), item);
				item.setText(0,
						currentTax.getValue().multiply(new BigDecimal(100))
								+ "%"); //$NON-NLS-1$
			}

		}
		cmbCashflow.select(0);

		final Label lblPay = new Label(container, SWT.RIGHT);
		final FormData fd_lblPay = new FormData();
		fd_lblPay.top = new FormAttachment(0, 551);
		fd_lblPay.right = new FormAttachment(0, leftColWidth - middleMargin);
		fd_lblPay.left = new FormAttachment(0, 0);
		lblPay.setLayoutData(fd_lblPay);
		lblPay.setText(Messages
				.getString("transactionDetailWindow.payDebitLabel")); //$NON-NLS-1$
		lblPay.setFont(configs.getDefaultFont());

		updateControlsFromEntry();
		//
		container.setSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return scrolledContainer;
	}

	private void updateGrossValue() {
		appTransaction currentTransaction = transactionsToProcess
				.get(entryIndex);
		if (autoUpdateGrossAmount) {
			/***
			 * the net value is significant, e.g. because we update an outgoing entry from the past
			 */
			BigDecimal netValue = utils.String2BD(txtValue.getText());

			BigDecimal addition = new BigDecimal(0);
			for (tax currentTax : client.getTaxes().getVATArray()) {

				BigDecimal amount = currentTransaction
						.getVATAmountForTax(currentTax);
				if (amount != null) {

					addition = addition.add(amount);
				}
			}
			BigDecimal grossVal = netValue.add(addition);
			currentTransaction.setGrossValue(grossVal);

		} else {
			/***
			 * the gross value is significant and should not be changed, 
			 * e.g. because we update an import from the bank statement
			 */

			BigDecimal grossValue = currentTransaction.getGrossValue();
			BigDecimal deduction = new BigDecimal(0);
			// update all taxes
			for (Integer vatID : VATIDTableItemHashmap.keySet()) {
				tax currentTax;
				try {
					currentTax = client.getTaxes().getVATByID(vatID);
					BigDecimal amount = currentTransaction
							.getVATAmountForTax(currentTax);
					TableItem currentItem=VATIDTableItemHashmap.get(vatID);
					if (currentItem.getText(1).length()>0) {
						amount=utils.String2BD(currentItem.getText(1));
					}
					if (amount != null) {
						currentTransaction.setUserDetailVAT(currentTax, amount);
						deduction = deduction.add(amount);
					}
				} catch (taxNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				

			}
			BigDecimal netVal = grossValue.subtract(deduction);
			txtValue.setText(netVal.toString());

		}

	}

	/**
	 * load entry
	 * */
	private void updateControlsFromEntry() {
		if (entryIndex < 0) {
			return;
		}
		lblEntryNum
				.setText(String.format(
						Messages.getString("transactionDetailWindow.transactionHeading"), (entryIndex + 1), transactionsToProcess.size())); //$NON-NLS-1$
		if ((entryIndex == 0) && (maxEntryIndex == 0)) {
			btnFirst.setEnabled(false);
			btnPrev.setEnabled(false);
			btnNext.setEnabled(false);
			btnLast.setEnabled(false);

		} else if (entryIndex >= maxEntryIndex) {
			entryIndex = maxEntryIndex;

			if (entryIndex > 0) {
				btnFirst.setEnabled(true);
				btnPrev.setEnabled(true);
			}

			btnNext.setEnabled(false);
			btnLast.setEnabled(false);

		} else if (entryIndex <= 0) {
			entryIndex = 0;
			btnFirst.setEnabled(false);
			btnPrev.setEnabled(false);
			if (transactionsToProcess.size() > 1) {
				btnNext.setEnabled(true);
				btnLast.setEnabled(true);
			}

		} else {
			btnFirst.setEnabled(true);
			btnPrev.setEnabled(true);
			btnNext.setEnabled(true);
			btnLast.setEnabled(true);

		}
		appTransaction currentTransaction = transactionsToProcess
				.get(entryIndex);

		/*
		 * normally, vat amounts from a selectin in a table are booked via
		 * entry.book but as we already want to display the amounts (and make
		 * changes, also regarding multiple VAT amounts, possible) we already
		 * split the amounts up here and show them in a the table tblVAT
		 */
		currentTransaction.prepareVATamounts();

		cmbReference.setText(currentTransaction.getDefaultReference());
		updateContactAndCashflowFromReference();
		// now, fill the vat table with the items

		for (tax currentTax : client.getTaxes().getVATArray()) {
			TableItem tabItem = VATIDTableItemHashmap.get(currentTax.getID());
			if (tabItem != null) {
				BigDecimal amount = currentTransaction
						.getVATAmountForTax(currentTax);
				if (amount != null) {
					tabItem.setText(1, amount.toString());
				}
			}
		}

		if (currentTransaction.getCashFlow() != CashFlow.UNDEFINED) {
			if (currentTransaction.getCashFlow() == CashFlow.SENDING) {
				cmbCashflow.select(0);
			} else {
				cmbCashflow.select(1);
			}

		}
		lblGrossValue.setText(currentTransaction.getGrossValue().toString());
		txtDescription.setText(currentTransaction.getDefaultDescription());
		txtComment.setText(currentTransaction.getDefaultComment());
		StructuredSelection selection = null;
		if (currentTransaction.getDefaultDebitAccount() != null) {
			selection = new StructuredSelection(
					currentTransaction.getDefaultDebitAccount());
			cmbDebit.setSelection(selection);
		}
		if (currentTransaction.getDefaultCreditAccount() != null) {
			selection = new StructuredSelection(
					currentTransaction.getDefaultCreditAccount());
			cmbCredit.setSelection(selection);
		}
		if (currentTransaction.getContact() != null) {
			selection = new StructuredSelection(currentTransaction.getContact());
			cmbContact.setSelection(selection);
		}
		cmbReference.setText(currentTransaction.getDefaultReference());

		Calendar c = GregorianCalendar.getInstance();
		c.setTime(currentTransaction.getIssueDate());

		dteIssue.setYear(c.get(Calendar.YEAR));
		dteIssue.setMonth(c.get(Calendar.MONTH));
		dteIssue.setDay(c.get(Calendar.DAY_OF_MONTH));
		checkProcessable();
		updateGrossValue();
	}

	private void updateContactAndCashflowFromReference() {
		document docForNumber = client.getDocuments().getDocumentForNumber(
				cmbReference.getText());
		if (docForNumber != null) {
			int transID = client.getTransactions().getIDForNumber(
					cmbReference.getText());
			appTransaction transactionForNumber = client.getTransactions()
					.getByID(transID);
			contact contactForNumber = transactionForNumber.getContact();
			CashFlow cashFlowFromTransaction = transactionForNumber
					.getCashFlow();
			if (cashFlowFromTransaction == CashFlow.RECEIVING) {
				cmbCashflow.select(1);
			} else {
				cmbCashflow.select(0);
			}
			cmbCashflow.setEnabled(false);
			StructuredSelection selection = new StructuredSelection(
					contactForNumber);
			cmbContact.setSelection(selection);
			cmbContact.getCombo().setEnabled(false);
			getStatusLineManager()
					.setMessage(
							Messages.getString("transactionDetailWindow.noticeReferenceNumberFound")); //$NON-NLS-1$
		} else {
			cmbContact.getCombo().setEnabled(true);
			cmbCashflow.setEnabled(true);
			getStatusLineManager().setMessage(""); //$NON-NLS-1$

		}

		checkPaymentPossible();
	}

	private void checkPaymentPossible() {
		if (!cmbContact.getSelection().isEmpty()) {
			String whyNoPaymentPossible = ""; //$NON-NLS-1$
			IStructuredSelection selection = (IStructuredSelection) cmbContact
					.getSelection();
			contact selectedContact = (contact) selection.getFirstElement();
			if (configs.getBankCode().length() == 0) {
				whyNoPaymentPossible += Messages
						.getString("transactionDetailWindow.noPaymentPossibleBalloonOwnBankCode"); //$NON-NLS-1$
			}
			if (configs.getAccountCode().length() == 0) {
				whyNoPaymentPossible += Messages
						.getString("transactionDetailWindow.noPaymentPossibleBalloonOwnBankAccount"); //$NON-NLS-1$
			}
			if (configs.getHolderName().length() == 0) {
				whyNoPaymentPossible += Messages
						.getString("transactionDetailWindow.noPaymentPossibleBalloonOwnHolderName"); //$NON-NLS-1$

			}
			if (selectedContact.getAccountholder().length() == 0) {
				whyNoPaymentPossible += Messages
						.getString("transactionDetailWindow.noPaymentPossibleBalloonOtherAccountHolder"); //$NON-NLS-1$
			}
			if (selectedContact.getBankCode().length() == 0) {
				whyNoPaymentPossible += Messages
						.getString("transactionDetailWindow.noPaymentPossibleBalloonOtherBankCode"); //$NON-NLS-1$
			}
			if (selectedContact.getBankAccount().length() == 0) {
				whyNoPaymentPossible += Messages
						.getString("transactionDetailWindow.noPaymentPossibleBalloonOtherBankAccount"); //$NON-NLS-1$

			}
			if (whyNoPaymentPossible.length() > 0) {
				chkBalance.setEnabled(false);
				ToolTip tlpWhyNoPayment = new ToolTip(getShell(), SWT.BALLOON
						| SWT.ICON_INFORMATION);

				tlpWhyNoPayment
						.setText(Messages
								.getString("transactionDetailWindow.noPaymentPossibleBalloonHeading")); //$NON-NLS-1$
				tlpWhyNoPayment.setMessage(whyNoPaymentPossible);

				tlpWhyNoPayment.setLocation(chkBalance.getLocation().x
						+ getShell().getLocation().x + 13,
						chkBalance.getLocation().y + getShell().getLocation().y
								+ 42);
				tlpWhyNoPayment.setVisible(true);

			} else {
				chkBalance.setEnabled(true);
			}

		}
	}

	/**
	 * save entry
	 * */
	private appTransaction updateTransactionFromControls() {
		updateGrossValue();
		appTransaction currentTransaction = transactionsToProcess
				.get(entryIndex);
		currentTransaction.setDefaultDescription(txtDescription.getText());
		currentTransaction.setDefaultComment(txtComment.getText());
		currentTransaction.setDefaultReference(cmbReference.getText());

		Calendar c = GregorianCalendar.getInstance();

		c.set(Calendar.YEAR, dteIssue.getYear());
		c.set(Calendar.MONTH, dteIssue.getMonth());
		c.set(Calendar.DAY_OF_MONTH, dteIssue.getDay());
		if (cmbCashflow.getSelectionIndex() == 0) {
			currentTransaction.setCashflow(CashFlow.SENDING);
		} else {
			currentTransaction.setCashflow(CashFlow.RECEIVING);
		}

		currentTransaction.setIssueDate(c.getTime());

		ISelection selection = cmbDebit.getSelection();

		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		if (!structuredSelection.isEmpty()) {
			// get first element ...
			account firstElement = (account) structuredSelection
					.getFirstElement();
			currentTransaction.setDefaultDebitAccount(firstElement);
		}

		selection = cmbCredit.getSelection();

		structuredSelection = (IStructuredSelection) selection;
		if (!structuredSelection.isEmpty()) {
			// get first element ...
			account firstElement = (account) structuredSelection
					.getFirstElement();
			currentTransaction.setDefaultCreditAccount(firstElement);
		}

		return currentTransaction;
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
		return new Point(500, 770);
	}

}
