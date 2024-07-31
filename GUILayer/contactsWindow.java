package GUILayer;

import java.util.Vector;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.kapott.hbci.manager.HBCIUtils;

import appLayer.RoleNotFoundException;
import appLayer.application;
import appLayer.client;
import appLayer.configs;
import appLayer.contact;
import appLayer.contacts;
import dataLayer.HBCI;

public class contactsWindow extends ApplicationWindow {

	class ContentProvider implements IStructuredContentProvider {
		public ContentProvider() {
			super();

		}

		public Object[] getElements(Object inputElement) {
			return client.getContacts().getContacts().toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class contactSelectionChangedListener implements ISelectionChangedListener {
		protected contactsWindow parent;

		public contactSelectionChangedListener(contactsWindow parent) {
			this.parent = parent;

		}

		public void selectionChanged(SelectionChangedEvent arg0) {
			parent.updateControlsFromSelection();

		}
	}

	// /////////////////////MAIN
	private Combo cmbPayment;
	private Text txtFldAccountHolder;
	private Text txtFldBankCode;
	private Text txtFldBankAccount;
	private Text txtFldVATID;
	private Text txtFldFax;
	private Text txtFldPhone;

	private ListViewer listViewer;
	private Text txtFldEmail;
	private ComboViewer cmbRoleViewer;
	private Text txtFldCountry;
	private Text txtFldLocation;
	private Text txtFldZip;
	private Text txtFldStreet;
	private Text txtFldCO;
	private Text txtFldName;
	private Label lblBankCode;
	private Label lblBankAccount;
	private Label lblIBAN;
	private Button btnCheckTaxExempt = null; 
	private Label lblID = null;
	private Button deleteButton = null;
	private String notYetAssignedText = Messages
			.getString("contactsWindow.notYetAssigned"); //$NON-NLS-1$
	private ContentProvider contentProvider;

	static Vector<Integer> customerIDs = new Vector<Integer>();
	private Text txtFldAddressLine2;
	private Text txtFldBIC;
	private Text txtFldIBAN;
	private Text txtFldMandate;

	public contactsWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	protected Control createContents(Composite parent) {
		parent.getShell().setSize(getInitialSize());
		Composite container = new Composite(parent, SWT.NONE);

		GridLayoutFactory.swtDefaults().numColumns(4).margins(10, 5)
		.applyTo(container);

		// row
		
		listViewer = new ListViewer(container, SWT.V_SCROLL | SWT.BORDER);
		listViewer
				.addSelectionChangedListener(new contactSelectionChangedListener(
						this));
		contentProvider = new ContentProvider();
		listViewer.setContentProvider(contentProvider);
		listViewer.setInput(new Object());
		List list = listViewer.getList();
		list.setFont(configs.getDefaultFont());

		list.select(0);

		GridDataFactory.fillDefaults().hint(400, 200).span(4,1).grab(true, true)
		.applyTo(list);

		// row
		final Label idLabel = new Label(container, SWT.NONE);
		idLabel.setAlignment(SWT.RIGHT);
		idLabel.setFont(configs.getDefaultFont());
		idLabel.setText(Messages.getString("contactsWindow.ID")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).hint(100, 20).grab(true, false)
		.applyTo(idLabel);

		lblID = new Label(container, SWT.NONE);
		lblID.setFont(configs.getDefaultFont());
		lblID.setText(notYetAssignedText); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).hint(200, 20).grab(true, false)
		.applyTo(lblID);


		final Label lblPhone = new Label(container, SWT.NONE);
		lblPhone.setFont(configs.getDefaultFont());
		lblPhone.setAlignment(SWT.RIGHT);
		lblPhone.setText(Messages.getString("contactsWindow.telephone")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).hint(200, 20).grab(true, false)
		.applyTo(lblPhone);

		
		txtFldPhone = new Text(container, SWT.BORDER);
		txtFldPhone.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).hint(200, 20).grab(true, false)
		.applyTo(txtFldPhone);

		// row

		final Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setAlignment(SWT.RIGHT);
		nameLabel.setFont(configs.getDefaultFont());
		nameLabel.setText(Messages.getString("contactsWindow.name")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(100, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(nameLabel);

		txtFldName = new Text(container, SWT.BORDER);
		txtFldName.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(txtFldName);

		Label lblFax;
		lblFax = new Label(container, SWT.NONE);
		lblFax.setFont(configs.getDefaultFont());
		lblFax.setAlignment(SWT.RIGHT);
		lblFax.setText(Messages.getString("contactsWindow.telefax")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(lblFax);

		txtFldFax = new Text(container, SWT.BORDER);
		txtFldFax.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(txtFldFax);

		// row


		final Label lblCo = new Label(container, SWT.NONE);
		lblCo.setAlignment(SWT.RIGHT);
		lblCo.setFont(configs.getDefaultFont());
		lblCo.setText(Messages.getString("contactsWindow.co")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(100, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(lblCo);

		txtFldCO = new Text(container, SWT.BORDER);
		txtFldCO.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(txtFldCO);


		final Label lblVatID = new Label(container, SWT.NONE);
		lblVatID.setAlignment(SWT.RIGHT);
		lblVatID.setText(Messages.getString("contactsWindow.vatid0")); //$NON-NLS-1$
		lblVatID.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(lblVatID);

		txtFldVATID = new Text(container, SWT.BORDER);
		txtFldVATID.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(txtFldVATID);

		// row
		
		Label lblAddressLine2 = new Label(container, SWT.NONE);
		lblAddressLine2.setAlignment(SWT.RIGHT);
		lblAddressLine2.setText(Messages.getString("contactsWindow.Line2")); //$NON-NLS-1$
		lblAddressLine2.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(100, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(lblAddressLine2);

		txtFldAddressLine2 = new Text(container, SWT.BORDER);
		txtFldAddressLine2.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(txtFldAddressLine2);

		lblBankAccount = new Label(container, SWT.NONE);
		lblBankAccount.setAlignment(SWT.RIGHT);
		lblBankAccount.setText(Messages
				.getString("contactsWindow.bankAccount")); //$NON-NLS-1$
		lblBankAccount.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(lblBankAccount);

		txtFldBankAccount = new Text(container, SWT.BORDER);
		txtFldBankAccount.addKeyListener(new KeyAdapter() {
			public void keyReleased(final KeyEvent arg0) {
				checkBankData();
			}
		});
		txtFldBankAccount.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(txtFldBankAccount);

		// row

		final Label lblStreet = new Label(container, SWT.NONE);
		lblStreet.setAlignment(SWT.RIGHT);
		lblStreet.setFont(configs.getDefaultFont());
		lblStreet.setText(Messages.getString("contactsWindow.street")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(100, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(lblStreet);

		txtFldStreet = new Text(container, SWT.BORDER);
		txtFldStreet.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(txtFldStreet);

		
		lblBankCode = new Label(container, SWT.NONE);
		lblBankCode.setAlignment(SWT.RIGHT);
		lblBankCode.setText(Messages.getString("contactsWindow.bankCode")); //$NON-NLS-1$
		lblBankCode.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(lblBankCode);

		
		txtFldBankCode = new Text(container, SWT.BORDER);
		txtFldBankCode.addKeyListener(new KeyAdapter() {
			public void keyReleased(final KeyEvent arg0) {
				checkBankData();
			}
		});
		txtFldBankCode.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(txtFldBankCode);

		// row
		final Label lblZip = new Label(container, SWT.NONE);
		lblZip.setAlignment(SWT.RIGHT);
		lblZip.setFont(configs.getDefaultFont());
		lblZip.setText(Messages.getString("contactsWindow.zip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(100, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(lblZip);

		txtFldZip = new Text(container, SWT.BORDER);
		txtFldZip.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(txtFldZip);

		Label lblAccountHolderName;
		lblAccountHolderName = new Label(container, SWT.NONE);
		lblAccountHolderName.setAlignment(SWT.RIGHT);
		lblAccountHolderName.setText(Messages
				.getString("contactsWindow.accountHolder")); //$NON-NLS-1$
		lblAccountHolderName.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(lblAccountHolderName);


		
		txtFldAccountHolder = new Text(container, SWT.BORDER);
		txtFldAccountHolder.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(txtFldAccountHolder);

		// row
		final Label lblLocation = new Label(container, SWT.NONE);
		lblLocation.setAlignment(SWT.RIGHT);
		lblLocation.setFont(configs.getDefaultFont());
		lblLocation.setText(Messages.getString("contactsWindow.location")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(100, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(lblLocation);

		txtFldLocation = new Text(container, SWT.BORDER);
		txtFldLocation.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(txtFldLocation);

		Label lblBIC = new Label(container, SWT.NONE);
		lblBIC.setAlignment(SWT.RIGHT);
		lblBIC.setFont(configs.getDefaultFont());
		lblBIC.setText(Messages.getString("contactsWindow.BICLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(lblBIC);

		txtFldBIC = new Text(container, SWT.BORDER);
		txtFldBIC.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(txtFldBIC);

		// row
		final Label lblCountry = new Label(container, SWT.NONE);
		lblCountry.setAlignment(SWT.RIGHT);
		lblCountry.setFont(configs.getDefaultFont());
		lblCountry.setText(Messages.getString("contactsWindow.country")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(100, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(lblCountry);

		txtFldCountry = new Text(container, SWT.BORDER);
		txtFldCountry.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(txtFldCountry);


		lblIBAN = new Label(container, SWT.NONE);
		lblIBAN.setAlignment(SWT.RIGHT);
		lblIBAN.setFont(configs.getDefaultFont());
		lblIBAN.setText(Messages.getString("contactsWindow.IBANLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(lblIBAN);

		txtFldIBAN = new Text(container, SWT.BORDER);
		txtFldIBAN.setFont(configs.getDefaultFont());
		txtFldIBAN.addKeyListener(new KeyAdapter() {
			public void keyReleased(final KeyEvent arg0) {
				checkBankData();
			}
		});
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(txtFldIBAN);

		// row
		final Label lblRole = new Label(container, SWT.NONE);
		lblRole.setAlignment(SWT.RIGHT);
		lblRole.setFont(configs.getDefaultFont());
		lblRole.setText(Messages.getString("contactsWindow.role")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(100, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(lblRole);

		cmbRoleViewer = new ComboViewer(container, SWT.READ_ONLY);
		cmbRoleViewer.setContentProvider(ArrayContentProvider.getInstance());
		cmbRoleViewer.setInput(appLayer.contacts.getRoles());
		cmbRoleViewer.getCombo().setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(cmbRoleViewer.getCombo());

		Label lblMandate = new Label(container, SWT.NONE);
		lblMandate.setAlignment(SWT.RIGHT);
		lblMandate.setText(Messages
				.getString("contactsWindow.lblNewLabel.text")); //$NON-NLS-1$
		lblMandate.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(lblMandate);

		txtFldMandate = new Text(container, SWT.BORDER);
		txtFldMandate.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(txtFldMandate);

		// row
		
		Label lblEmail;
		lblEmail = new Label(container, SWT.NONE);
		lblEmail.setAlignment(SWT.RIGHT);
		lblEmail.setFont(configs.getDefaultFont());
		lblEmail.setText(Messages.getString("contactsWindow.emailLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(100, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(lblEmail);

		txtFldEmail = new Text(container, SWT.BORDER);
		txtFldEmail.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(txtFldEmail);

		
		final Label lblPayment = new Label(container, SWT.NONE);
		lblPayment.setAlignment(SWT.RIGHT);
		lblPayment.setText(Messages.getString("contactsWindow.payment")); //$NON-NLS-1$
		lblPayment.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(lblPayment);

		cmbPayment = new Combo(container, SWT.READ_ONLY);
		cmbPayment.setFont(configs.getDefaultFont());
		for (String method : appLayer.contacts.getPaymentMethods()) {
			cmbPayment.add(method);
		}
		GridDataFactory.fillDefaults().hint(200, 20).align(SWT.RIGHT, SWT.CENTER).grab(true, false)
		.applyTo(cmbPayment);



		// row
	
		
		if (configs.hasSalesTax()) {
			btnCheckTaxExempt = new Button(container, SWT.CHECK);
			btnCheckTaxExempt.setText(Messages.getString("contactsWindow.salesTaxExempt")); //$NON-NLS-1$
			btnCheckTaxExempt.setFont(configs.getDefaultFont());
			GridDataFactory.fillDefaults().hint(200, 20).span(4,1).align(SWT.LEFT, SWT.CENTER).grab(true, false)
			.applyTo(btnCheckTaxExempt);
		}

		// row
		

		final Button okButton = new Button(container, SWT.NONE);
		okButton.setFont(configs.getDefaultFont());
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) listViewer
						.getSelection();
				contact selectedContact = (contact) selection.getFirstElement();

				boolean newContactCreated = false;

				if (selectedContact.getID() == contact.newContactID) {
					selectedContact = new contact();
					newContactCreated = true;
				}

				int errorCount = 0;
				if (txtFldName.getText().equalsIgnoreCase("")) { //$NON-NLS-1$
					errorCount++;
					getStatusLineManager().setErrorMessage(
							Messages.getString("contactsWindow.contactsError")); //$NON-NLS-1$
				}
				selectedContact.setName(txtFldName.getText());

				selectedContact.setStreet(txtFldStreet.getText());
				selectedContact.setZIP(txtFldZip.getText());
				selectedContact.setLocation(txtFldLocation.getText());
				selectedContact.setCountry(txtFldCountry.getText());
				selectedContact.setCO(txtFldCO.getText());
				selectedContact.setEmail(txtFldEmail.getText());
				selectedContact.setPhone(txtFldPhone.getText());
				selectedContact.setFax(txtFldFax.getText());
				selectedContact.setVATID(txtFldVATID.getText());
				if (btnCheckTaxExempt!=null) {
					selectedContact.setTaxExempt(btnCheckTaxExempt.getSelection());					
				}
				selectedContact.setSEPAmandate(txtFldMandate.getText());
				selection = (IStructuredSelection) cmbRoleViewer.getSelection();

				try {
					selectedContact.setRole(contacts
							.getRoleIDForString((String) selection
									.getFirstElement()));
				} catch (RoleNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				selectedContact.setBankaccount(txtFldBankAccount.getText());
				selectedContact.setBankcode(txtFldBankCode.getText());
				selectedContact.setAccountholder(txtFldAccountHolder.getText());
				selectedContact.setPaymentmethod(cmbPayment.getSelectionIndex());
				selectedContact
						.setAdditionalAddressLine(txtFldAddressLine2
								.getText());
				selectedContact.setBIC(txtFldBIC.getText());
				selectedContact.setIBAN(txtFldIBAN.getText());

				if (errorCount == 0) {
					selectedContact.save();
					getStatusLineManager().setErrorMessage(""); //$NON-NLS-1$
					listViewer.refresh();
					todoWindow.refreshToDoList();
					newTransactionSelectTransactionDetails.refreshContacts();
					
					if (newContactCreated) {
						selection = new StructuredSelection(selectedContact);
						listViewer.setSelection(selection);
					}
				}

			}

		});
		okButton.setText(Messages.getString("contactsWindow.ok")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 30).span(2,1).grab(true, false)
		.applyTo(okButton);

		deleteButton = new Button(container, SWT.NONE);
		deleteButton.setFont(configs.getDefaultFont());
		deleteButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) listViewer
						.getSelection();
				contact selectedContact = (contact) selection.getFirstElement();

				int errorCount = 0;
				if (client.getContacts().getContacts().size() <= 2) {
					// dont allow to delete the last contact
					getStatusLineManager().setErrorMessage(
							Messages.getString("contactsWindow.oneNeeded")); //$NON-NLS-1$
					errorCount++;
				}
				if (errorCount == 0) {
					selectedContact.delete();
					listViewer.remove(selectedContact);
					listViewer.refresh();
					listViewer.getList().select(0);
					updateControlsFromSelection();
					newTransactionSelectTransactionDetails.refreshContacts();

					getStatusLineManager().setErrorMessage(""); //$NON-NLS-1$

				}

			}
		});
		deleteButton.setText(Messages.getString("contactsWindow.delete")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 30).span(2,1).grab(true, false)
		.applyTo(deleteButton);

		
		updateControlsFromSelection();
		//
		return container;
	}

	protected void checkBankData() {
		Color black = new Color(getShell().getDisplay(), 0x00, 0x00, 0x00);
		Color red = new Color(getShell().getDisplay(), 0x80, 0x00, 0x00);
		Color green = new Color(getShell().getDisplay(), 0x00, 0x80, 0x00);
		// we need to invoke HBCI.getInstance otherwise the static HBCIUtils
		// functions won't be able to
		// connect to their ressources (like localized strings)
		HBCI.getInstance(getShell());
		lblBankCode.setForeground(black);
		lblBankAccount.setForeground(black);
		lblIBAN.setForeground(black);

		if ((txtFldBankCode.getText().length() > 0)
				|| (txtFldBankAccount.getText().length() > 0)) {

			lblBankCode.setForeground(red);
			lblBankAccount.setForeground(red);
		}

		if (txtFldIBAN.getText().length() > 0) {
			lblIBAN.setForeground(red);
			if (txtFldIBAN.getText().contains(" ")) { //$NON-NLS-1$
				txtFldIBAN.setText(txtFldIBAN.getText().replaceAll(" ", "")); //$NON-NLS-1$ //$NON-NLS-2$
				txtFldIBAN.setSelection(txtFldIBAN.getText().length());
			}
		}
		if ((txtFldBankCode.getText().length() > 0)
				&& (txtFldBankAccount.getText().length() > 0)) {
			if (HBCIUtils.getNameForBLZ(txtFldBankCode.getText()).length() != 0) {
				lblBankCode.setForeground(green);
				// if bank code is not correct, account code can neither be
				// checked nor be correct
				if (HBCIUtils.checkAccountCRC(txtFldBankCode.getText(),
						txtFldBankAccount.getText())) {
					lblBankAccount.setForeground(green);
				}
			}

		}
		if (txtFldIBAN.getText().length() > 14) {
			/*norwegian IBAN is 15 characters long*/
			if (HBCIUtils.checkIBANCRC(txtFldIBAN.getText())) {
				lblIBAN.setForeground(green);
			}
		}
	}

	public void updateControlsFromSelection() {

		Object[] objects = ((ContentProvider) listViewer.getContentProvider())
				.getElements(null);
		contact defaultContact = (contact) objects[1];

		IStructuredSelection selection = (IStructuredSelection) listViewer
				.getSelection();
		contact selectedContact = (contact) selection.getFirstElement();
		if (selection.isEmpty()) {
			// default select first element (new asset), e.g. if current element
			// was deleted
			selectedContact = defaultContact;
		}

		if ((selectedContact == defaultContact)
				|| (selectedContact.getID() == contact.newContactID)) {
			deleteButton.setEnabled(false);
		} else {
			deleteButton.setEnabled(true);
		}
		if (selectedContact.getID() > 1) {
			lblID.setText(Integer.toString(selectedContact.getID()));
		} else {
			lblID.setText(notYetAssignedText);
		}
		txtFldName.setText(selectedContact.getName());

		txtFldCO.setText(selectedContact.getCO());
		txtFldStreet.setText(selectedContact.getStreet());
		txtFldZip.setText(selectedContact.getZIP());
		txtFldLocation.setText(selectedContact.getLocation());
		txtFldCountry.setText(selectedContact.getCountry());
		txtFldPhone.setText(selectedContact.getPhone());
		txtFldFax.setText(selectedContact.getFax());
		txtFldEmail.setText(selectedContact.getEmail());
		txtFldVATID.setText(selectedContact.getVATID());
		txtFldAccountHolder.setText(selectedContact.getAccountholder());
		txtFldBankAccount.setText(selectedContact.getBankAccount());
		txtFldBankCode.setText(selectedContact.getBankCode());

		if (btnCheckTaxExempt!=null) {
			btnCheckTaxExempt.setSelection(selectedContact.isTaxExempt());			
		}
	
		txtFldAddressLine2
				.setText(selectedContact.getAdditionalAddressLine() == null ? "" : selectedContact.getAdditionalAddressLine()); //$NON-NLS-1$
		cmbRoleViewer.getCombo().select(selectedContact.getRole());
		cmbPayment.select(selectedContact.getPaymentmethod());
		txtFldIBAN.setText(selectedContact.getIBAN());
		txtFldBIC.setText(selectedContact.getBIC());
		txtFldMandate.setText(selectedContact.getSEPAmandate());
		checkBankData();
	}

	private void createActions() {
	}

	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager(
				Messages.getString("contactsWindow.menu")); //$NON-NLS-1$
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
			contactsWindow window = new contactsWindow();
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

	protected Point getInitialSize() {
		return new Point(511, 652);
	}
}
