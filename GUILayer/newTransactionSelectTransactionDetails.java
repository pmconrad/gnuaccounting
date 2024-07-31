package GUILayer;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalListener2;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

import GUILayer.AccountTextCellEditor.openableContentProposalAdapter;
import appLayer.client;
import appLayer.configs;
import appLayer.contact;
import appLayer.transactionRelated.appTransaction;
import appLayer.transactionRelated.transactionType;

class transactionSelectionAdapter extends SelectionAdapter {
	private ComboViewer cmbTransaction;

	public transactionSelectionAdapter(ComboViewer cmbTransaction) {
		this.cmbTransaction = cmbTransaction;
	}

	public void widgetSelected(SelectionEvent e) {
		IStructuredSelection selection = (IStructuredSelection) cmbTransaction
				.getSelection();
		transactionType selectedTrans = (transactionType) selection
				.getFirstElement();

		client.getTransactions().setTransactionListIndex(
				selectedTrans.getListIndex());
	}
}

public class newTransactionSelectTransactionDetails extends WizardPage {

	private ComboViewer cmbTransaction;
	private static ComboViewer cmbCustomer;
	private Text txtComments;
	private Button btnCheckIsTaxExempt = null;
	private DateTime dteIssue;
	private DateTime dteFrom;
	private DateTime dteTo;
	private newTransactionWizard parentWizard;
	private GregorianCalendar currentIssueDate;
	private boolean performanceDateAdjusted=false;
	
	/**
	 * Create the wizard
	 */
	public newTransactionSelectTransactionDetails(newTransactionWizard parent) {
		super(Messages
				.getString("newTransactionSelectTransactionDetails.wizardPage")); //$NON-NLS-1$
		this.parentWizard = parent;
		setTitle(Messages
				.getString("newTransactionSelectTransactionDetails.newTransaction")); //$NON-NLS-1$
		setDescription(Messages
				.getString("newTransactionSelectTransactionDetails.selectTransactionDetails")); //$NON-NLS-1$
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "selectTransaction"; //$NON-NLS-1$
	}

	public void loadFromDB(int transID) {
		appTransaction trans;
		client.getTransactions().setAsCurrentTransaction(transID);
		trans = client.getTransactions().getCurrentTransaction();
		StructuredSelection selection = new StructuredSelection(
				trans.getContact());

		cmbCustomer.setSelection(selection);
	}

	/**
	 * Create contents of the wizard
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		//
		setControl(container);

		// the values of the fields will be set using loadValuesFromConfigFile 
		GridLayoutFactory.swtDefaults().numColumns(3).margins(10, 5)
				.applyTo(container);

		final Label recipientLabel = new Label(container, SWT.NONE);
		recipientLabel.setFont(configs.getDefaultFont());
		recipientLabel.setText(Messages
				.getString("newTransactionSelectTransactionDetails.recipient")); //$NON-NLS-1$
		
		GridDataFactory.swtDefaults().hint(200, 20).grab(true, false)
		.applyTo(recipientLabel);

		cmbCustomer = new ComboViewer(container, SWT.DROP_DOWN);
		cmbCustomer.getCombo().addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent arg0) {
				onCustomerChange();
	
			}
		});
		// This wires up the userSelectedSomething method correctly
		  cmbCustomer.addSelectionChangedListener(new ISelectionChangedListener() {
		    @Override
		    public void selectionChanged(final SelectionChangedEvent event) {

		      onCustomerChange();
		    }
		  });
		cmbCustomer.getCombo().setFont(configs.getDefaultFont());
		cmbCustomer.setContentProvider(ArrayContentProvider.getInstance());
		cmbCustomer.setInput(client.getContacts().getExistingContacts());
		if (client.getTransactions().getCurrentTransaction().getContact() != null) {
			StructuredSelection selection = new StructuredSelection(client
					.getTransactions().getCurrentTransaction().getContact());
			cmbCustomer.setSelection(selection);
		} else {
			cmbCustomer.getCombo().select(0);
		}
		

	 	SimpleContentProposalProvider proposalProvider = new SimpleContentProposalProvider(cmbCustomer.getCombo().getItems());
	 	proposalProvider.setFiltering(true);
	  
		ContentProposalAdapter contentProposalAdapter = new ContentProposalAdapter(cmbCustomer.getCombo(),
				new ComboContentAdapter(), proposalProvider, null, null);
		contentProposalAdapter.addContentProposalListener(new IContentProposalListener() {
			
			@Override
			public void proposalAccepted(IContentProposal arg0) {
				contact c=client.getContacts().getContactByName(cmbCustomer.getCombo().getText());
				
				StructuredSelection selection = new StructuredSelection(c);

				cmbCustomer.setSelection(selection);
				onCustomerChange();
			}
		});
		char[] autoProps = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray(); //$NON-NLS-1$
		contentProposalAdapter.setAutoActivationCharacters(autoProps);
		contentProposalAdapter
		.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);


		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(cmbCustomer.getCombo());

		
		final Link lnkEditCustomers = new Link(container, SWT.NONE);
		lnkEditCustomers.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				contactsWindow cw = new contactsWindow();
				cw.setBlockOnOpen(true);

				cw.open();
			}
		});
		lnkEditCustomers
				.setText(Messages
						.getString("newTransactionSelectTransactionDetails.manageCustomers")); //$NON-NLS-1$
		lnkEditCustomers.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(lnkEditCustomers);


		final Label typeLabel = new Label(container, SWT.NONE);
		typeLabel.setFont(configs.getDefaultFont());
		typeLabel.setText(Messages
				.getString("newTransactionSelectTransactionDetails.type")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(typeLabel);


		

		cmbTransaction = new ComboViewer(container, SWT.READ_ONLY);
		cmbTransaction.getCombo().setFont(configs.getDefaultFont());
		cmbTransaction.setContentProvider(ArrayContentProvider.getInstance());
		cmbTransaction.setInput(client.getTransactions().getAllSelectableTypes(
				parentWizard.shallShowReferencedTypes()));
		cmbTransaction.getCombo().addSelectionListener(
				new transactionSelectionAdapter(cmbTransaction));
		cmbTransaction.setSelection(new StructuredSelection(client
				.getTransactions().getCurrentTransaction().getTypeClass()));

		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(cmbTransaction.getCombo());

		Link lnkTemplates = new Link(container, SWT.NONE);
		lnkTemplates.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				designerWindow d = new designerWindow();
				d.open();
			}
		});
		lnkTemplates
				.setText(Messages
						.getString("newTransactionSelectTransactionDetails.lnkTemplates.text_1")); //$NON-NLS-1$
		lnkTemplates.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(lnkTemplates);

		
		final Label commentLabel = new Label(container, SWT.NONE);
		commentLabel.setFont(configs.getDefaultFont());
		commentLabel.setText(Messages
				.getString("newTransactionSelectTransactionDetails.comment")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(commentLabel);

		
		txtComments = new Text(container, SWT.BORDER);

		txtComments.setFont(configs.getDefaultFont());
		txtComments.setText(client.getTransactions().getCurrentTransaction()
				.getRemarks());
		GridDataFactory.fillDefaults().hint(200, 20).span(2,1).grab(true, false)
		.applyTo(txtComments);

		

		
		final Label dateIssueLabel = new Label(container, SWT.NONE);
		dateIssueLabel.setFont(configs.getDefaultFont());
		dateIssueLabel.setText(Messages
				.getString("newTransactionSelectTransactionDetails.date")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(dateIssueLabel);
		
		dteIssue = new DateTime(container, SWT.DATE | SWT.DROP_DOWN);
		dteIssue.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GregorianCalendar newDte=new GregorianCalendar();
				newDte.set(Calendar.YEAR, dteIssue.getYear());
				newDte.set(Calendar.MONTH, dteIssue.getMonth());
				newDte.set(Calendar.DAY_OF_MONTH, dteIssue.getDay());

				GregorianCalendar fromDte=new GregorianCalendar();
				fromDte.set(Calendar.YEAR, dteFrom.getYear());
				fromDte.set(Calendar.MONTH, dteFrom.getMonth());
				fromDte.set(Calendar.DAY_OF_MONTH, dteFrom.getDay());

				GregorianCalendar toDte=new GregorianCalendar();
				toDte.set(Calendar.YEAR, dteTo.getYear());
				toDte.set(Calendar.MONTH, dteTo.getMonth());
				toDte.set(Calendar.DAY_OF_MONTH, dteTo.getDay());

				
				if ((Math.abs(currentIssueDate.getTimeInMillis()-newDte.getTimeInMillis())>3600*1000)&&(!performanceDateAdjusted)) {
					/*
					 * The current, first issue date contains the time.
					 * issue date has changed by at least 1 h (we assume that this has been done by changing the day) 
					 * but rest seems untouched --> adjust the rest
					 */
					dteFrom.setDay(dteIssue.getDay());
					dteFrom.setMonth(dteIssue.getMonth());
					dteFrom.setYear(dteIssue.getYear());

					dteTo.setDay(dteIssue.getDay());
					dteTo.setMonth(dteIssue.getMonth());
					dteTo.setYear(dteIssue.getYear());
					
				}
				currentIssueDate=newDte;
			}
		});
		dteIssue.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).span(2,1).grab(true, false)
		.applyTo(dteIssue);

		currentIssueDate = new GregorianCalendar();

		currentIssueDate.set(Calendar.YEAR, dteIssue.getYear());
		currentIssueDate.set(Calendar.MONTH, dteIssue.getMonth());
		currentIssueDate.set(Calendar.DAY_OF_MONTH, dteIssue.getDay());


		final Label dateFromLabel = new Label(container, SWT.NONE);
		dateFromLabel.setFont(configs.getDefaultFont());
		dateFromLabel.setText(Messages.getString("newTransactionSelectTransactionDetails.performedFrom")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(dateFromLabel);
		
		dteFrom = new DateTime(container, SWT.DATE | SWT.DROP_DOWN);
		dteFrom.setFont(configs.getDefaultFont());
		dteFrom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performanceDateAdjusted=true;
			}
		});
		GridDataFactory.fillDefaults().hint(200, 20).span(2,1).grab(true, false)
		.applyTo(dteFrom);

		

		final Label dateToLabel = new Label(container, SWT.NONE);
		dateToLabel.setFont(configs.getDefaultFont());
		dateToLabel.setText(Messages.getString("newTransactionSelectTransactionDetails.performedTo")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(dateToLabel);
		
		dteTo = new DateTime(container, SWT.DATE | SWT.DROP_DOWN);
		dteTo.setFont(configs.getDefaultFont());
		dteTo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performanceDateAdjusted=true;
			}
		});
		GridDataFactory.fillDefaults().hint(200, 20).span(2,1).grab(true, false)
		.applyTo(dteTo);
		

		Label lblTransNrLabel = new Label(container, SWT.NONE);
		lblTransNrLabel.setFont(configs.getDefaultFont());
		lblTransNrLabel
				.setText(Messages
						.getString("newTransactionSelectTransactionDetails.lblTransNr.text")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(lblTransNrLabel);

		final Label lblTransNrValue = new Label(container, SWT.NONE);
		lblTransNrValue.setFont(configs.getDefaultFont());
		lblTransNrValue
				.setText(Messages
						.getString("newTransactionSelectTransactionDetails.lblTbd.text")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(lblTransNrValue);

		final Link lnkTransCancel = new Link(container, SWT.NONE);
		lnkTransCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				client.getTransactions().abortCurrentTransaction();

				IStructuredSelection selection = (IStructuredSelection) cmbTransaction
						.getSelection();
				transactionType selectedTrans = (transactionType) selection
						.getFirstElement();

				client.getTransactions().setTransactionListIndex(
						selectedTrans.getTypeIndex());

				lblTransNrValue.setText(Messages
						.getString("newTransactionSelectTransactionDetails.lblTbd.text")); //$NON-NLS-1$
				lnkTransCancel.setVisible(false);
			}
		});
		lnkTransCancel.setFont(configs.getDefaultFont());
		lnkTransCancel.setText(Messages
				.getString("newTransactionSelectTransactionDetails.link.text")); //$NON-NLS-1$
		lnkTransCancel.setVisible(false);
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(lnkTransCancel);


		Link lnkNumbers = new Link(container, SWT.NONE);
		lnkNumbers.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				numbersWindow n = new numbersWindow();
				n.open();
			}
		});
		lnkNumbers
				.setText(Messages
						.getString("newTransactionSelectTransactionDetails.lnkNumbers.text_1")); //$NON-NLS-1$
		lnkNumbers.setFont(configs.getDefaultFont());
		if ((client.getTransactions().getCurrentTransaction() != null)
				&& (client.getTransactions().getCurrentTransaction().isDirty())) {
			lblTransNrValue.setText(client.getTransactions()
					.getCurrentTransaction().getNumber());
			lnkTransCancel.setVisible(true);

		}
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(lnkNumbers);
		
		if (configs.hasSalesTax()) {		
			btnCheckIsTaxExempt = new Button(container, SWT.CHECK);
			btnCheckIsTaxExempt.setText(Messages.getString("newTransactionSelectTransactionDetails.salesTaxExempt")); //$NON-NLS-1$
			btnCheckIsTaxExempt.setFont(configs.getDefaultFont());
			GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
			.applyTo(btnCheckIsTaxExempt);
		}
		
		onCustomerChange();

	}
	
	protected void onCustomerChange() {
    	IStructuredSelection selection=(IStructuredSelection)cmbCustomer.getSelection();
    	contact selectedContact = (contact) selection.getFirstElement();
    	if ((btnCheckIsTaxExempt!=null) && (selectedContact!=null)) {
        	btnCheckIsTaxExempt.setSelection(selectedContact.isTaxExempt());
    		
    	}
    	checkPageComplete();
	}

	/**
	 * is called from contact window in case a contact is e.g. added
	 */
	public static void refreshContacts() {
		if ((cmbCustomer!=null)&&(!cmbCustomer.getCombo().isDisposed())) {
			// a customer window has been opened and a customer might have been changed/deleted/created 
			IStructuredSelection selection = (IStructuredSelection) cmbCustomer
					.getSelection();
			

			cmbCustomer.setInput(client.getContacts().getExistingContacts());
			cmbCustomer.refresh();
			
			// re-select originally selected contact
			cmbCustomer.setSelection(selection);
		}
		
	}


	@Override
	public IWizardPage getNextPage() {
		if (btnCheckIsTaxExempt!=null) {
			client.getTransactions().getCurrentTransaction()
			.setIsTaxExempt(btnCheckIsTaxExempt.getSelection());
		}
		client.getTransactions().getCurrentTransaction()
				.setRemarks(txtComments.getText());
		GregorianCalendar cal = new GregorianCalendar();

		cal.set(Calendar.YEAR, dteIssue.getYear());
		cal.set(Calendar.MONTH, dteIssue.getMonth());
		cal.set(Calendar.DAY_OF_MONTH, dteIssue.getDay());

		client.getTransactions().getCurrentTransaction()
				.setIssueDate(cal.getTime());

		cal.set(Calendar.YEAR, dteFrom.getYear());
		cal.set(Calendar.MONTH, dteFrom.getMonth());
		cal.set(Calendar.DAY_OF_MONTH, dteFrom.getDay());

		client.getTransactions().getCurrentTransaction()
		.setPerformanceStart(cal.getTime());

		cal.set(Calendar.YEAR, dteTo.getYear());
		cal.set(Calendar.MONTH, dteTo.getMonth());
		cal.set(Calendar.DAY_OF_MONTH, dteTo.getDay());

		client.getTransactions().getCurrentTransaction()
		.setPerformanceEnd(cal.getTime());
		
		IStructuredSelection selection = (IStructuredSelection) cmbCustomer
				.getSelection();
		contact selectedContact = (contact) selection.getFirstElement();

		client.getTransactions().getCurrentTransaction()
				.setContact(selectedContact);

		return super.getNextPage();
	}


	public void checkPageComplete() {
		/***
		 * If somebody started entering a text in the customer combo to perform
		 * auto completion, but the text does not auto complete to a customer name,
		 * disallow form submission until a proper customer has been selected
		 */
		if (cmbCustomer.getCombo().getSelectionIndex() == -1) {
			setPageComplete(false);
			setErrorMessage(Messages.getString("newTransactionSelectTransactionDetails.pleaseSelectValidContact"));  //$NON-NLS-1$
			return;
		} else {

			setErrorMessage(null);
			setPageComplete(true);

		}

	}

}
