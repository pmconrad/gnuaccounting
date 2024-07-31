package GUILayer;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

import appLayer.account;
import appLayer.accounts;
import appLayer.application;
import appLayer.client;
import appLayer.configs;

public class accountDefinitionWindow extends ApplicationWindow {
	class ContentProvider implements IStructuredContentProvider {
		public ContentProvider() {
			super();
			client.getAccounts().getAccountsFromDatabase();
		}

		public Object[] getElements(Object inputElement) {
			return client.getAccounts().getCurrentChart().getAccounts(true)
					.toArray();
		}

		public accounts getAllAccounts() {
			return client.getAccounts();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class accountSelectionChangedListener implements ISelectionChangedListener {
		protected accountDefinitionWindow parent;

		public accountSelectionChangedListener(accountDefinitionWindow parent) {
			this.parent = parent;

		}

		public void selectionChanged(SelectionChangedEvent arg0) {
			parent.updateControlsFromSelection();

		}
	}

	// //////////////////////////////////////////////////////

	private Combo cmbRefersTo;
	private Combo cmbType;
	private Text txtFldDescription;
	private Text txtFldCode;
	private Label lblID = null;
	private Button btnAutoVAT = null;
	private Button btnDelete = null;

	private ListViewer listViewer = null;

	public accountDefinitionWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	protected Control createContents(Composite parent) {
		parent.getShell().setSize(getInitialSize());

		Composite container = new Composite(parent, SWT.NONE);

		container.setFont(configs.getDefaultFont());
		GridLayoutFactory.swtDefaults().numColumns(2).margins(10, 5)
		.applyTo(container);

		listViewer = new ListViewer(container, SWT.V_SCROLL | SWT.H_SCROLL
				| SWT.BORDER);
		listViewer
				.addSelectionChangedListener(new accountSelectionChangedListener(
						this));

		listViewer.setContentProvider(new ContentProvider());
		listViewer.setInput(new Object());

		List list;
		list = listViewer.getList();
		list.setFont(configs.getDefaultFont());
		list.select(0);
		GridDataFactory.fillDefaults().hint(200, 200).span(2,1).grab(true, true)
		.applyTo(list);

		

		Label idLabel;
		idLabel = new Label(container, SWT.NONE);
		idLabel.setAlignment(SWT.RIGHT);
		idLabel.setFont(configs.getDefaultFont());
		idLabel.setText(Messages.getString("accountDefinitionWindow.id")); //$NON-NLS-1$
		GridDataFactory.swtDefaults().applyTo(idLabel);
		

		lblID = new Label(container, SWT.NONE);
		lblID.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(lblID);
		lblID.setText(Messages.getString("accountDefinitionWindow.label")); //$NON-NLS-1$

		final Label codeLabel = new Label(container, SWT.NONE);
		codeLabel.setAlignment(SWT.RIGHT);
		codeLabel.setFont(configs.getDefaultFont());
		codeLabel.setText(Messages.getString("accountDefinitionWindow.code")); //$NON-NLS-1$
		GridDataFactory.swtDefaults().applyTo(codeLabel);

		
		txtFldCode = new Text(container, SWT.BORDER);
		txtFldCode.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(txtFldCode);


		final Label lblDescription = new Label(container, SWT.NONE);
		lblDescription.setAlignment(SWT.RIGHT);
		lblDescription.setFont(configs.getDefaultFont());
		GridDataFactory.swtDefaults().applyTo(lblDescription);
		lblDescription.setText(Messages
				.getString("accountDefinitionWindow.description")); //$NON-NLS-1$

		txtFldDescription = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(txtFldDescription);



		final Label lblFunction = new Label(container, SWT.NONE);
		lblFunction.setAlignment(SWT.RIGHT);
		lblFunction.setFont(configs.getDefaultFont());
		GridDataFactory.swtDefaults().applyTo(lblFunction);

		lblFunction.setText(Messages
				.getString("accountDefinitionWindow.function")); //$NON-NLS-1$

		cmbType = new Combo(container, SWT.READ_ONLY);
		for (String currentType : account.getTypes()) {
			cmbType.add(currentType);
		}
		cmbType.select(0);
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(cmbType);



		final Label lblRefersTo = new Label(container, SWT.NONE);
		lblRefersTo.setAlignment(SWT.RIGHT);
		lblRefersTo.setFont(configs.getDefaultFont());
		GridDataFactory.swtDefaults().applyTo(lblRefersTo);

		lblRefersTo.setText(Messages
				.getString("accountDefinitionWindow.refersto")); //$NON-NLS-1$

		cmbRefersTo = new Combo(container, SWT.READ_ONLY);
		cmbRefersTo.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(cmbRefersTo);

		for (String currentSubaccount : account.getSubAccounts()) {
			cmbRefersTo.add(currentSubaccount);
		}
		cmbRefersTo.select(0);
		
		final Label lblAutoVAT = new Label(container, SWT.NONE);
		lblAutoVAT.setAlignment(SWT.RIGHT);
		lblAutoVAT.setFont(configs.getDefaultFont());
		GridDataFactory.swtDefaults().applyTo(lblAutoVAT);
		lblAutoVAT.setText(Messages.getString("accountDefinitionWindow.autoVAT")); //$NON-NLS-1$

		btnAutoVAT = new Button(container, SWT.CHECK);
		btnAutoVAT.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 30).grab(true, false)
		.applyTo(btnAutoVAT);
		
		
		final Button btnOK = new Button(container, SWT.NONE);
		btnOK.setFont(configs.getDefaultFont());
		btnOK.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) listViewer
						.getSelection();
				account selectedAccount = (account) selection.getFirstElement();

				boolean accountCreated = false;
				if (selectedAccount.isPlaceholderForNewAccount()) {
					try {
						selectedAccount = account.getNewAccount(
								client.getAccounts().getCurrentChart()).clone();
					} catch (CloneNotSupportedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					selectedAccount.setParent(client.getAccounts()
							.getCurrentChart());
					client.getAccounts().getCurrentChart()
							.addAccount(selectedAccount);
					accountCreated = true;
				}
				if (txtFldCode.getText().equalsIgnoreCase("")) { //$NON-NLS-1$
					getStatusLineManager()
							.setErrorMessage(
									Messages.getString("accountDefinitionWindow.accountsEmptyCode")); //$NON-NLS-1$
				} else {
					selectedAccount.update(txtFldCode.getText(),
							txtFldDescription.getText(),
							cmbType.getSelectionIndex(),
							cmbRefersTo.getSelectionIndex());
					selectedAccount.setAutoVAT(btnAutoVAT.getSelection());
					selectedAccount.save();
					getStatusLineManager().setErrorMessage(""); //$NON-NLS-1$
					listViewer.refresh();
					if (accountCreated) {
						selection = new StructuredSelection(selectedAccount);
						listViewer.setSelection(selection);
					}
					updateControlsFromSelection();

				}

			}
		});
		GridDataFactory.fillDefaults().hint(200, 30).grab(true, false)
		.applyTo(btnOK);
		btnOK.setText(Messages.getString("accountDefinitionWindow.ok")); //$NON-NLS-1$
		
		btnDelete = new Button(container, SWT.NONE);
		btnDelete.setFont(configs.getDefaultFont());
		btnDelete.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) listViewer
						.getSelection();
				account selectedAccount = (account) selection.getFirstElement();
				int errorCount = 0;
				if (((ContentProvider) listViewer.getContentProvider())
						.getAllAccounts().getNumAccounts() <= 1) {
					getStatusLineManager()
							.setErrorMessage(
									Messages.getString("accountDefinitionWindow.oneAccountNeeded"));//$NON-NLS-1$
					errorCount++;
				}
				if (errorCount == 0) {
					selectedAccount.delete();
					listViewer.remove(selectedAccount);
					client.getAccounts().getCurrentChart()
							.removeAccount(selectedAccount);
					listViewer.refresh();
					listViewer.getList().select(0);
					updateControlsFromSelection();
					getStatusLineManager().setErrorMessage(""); //$NON-NLS-1$
				}
			}
		});
		GridDataFactory.fillDefaults().hint(200, 30).grab(true, false)
		.applyTo(btnDelete);

		btnDelete.setText(Messages
				.getString("accountDefinitionWindow.delete")); //$NON-NLS-1$

		txtFldCode.setFont(configs.getDefaultFont());
		txtFldDescription.setFont(configs.getDefaultFont());
		cmbType.setFont(configs.getDefaultFont());

		updateControlsFromSelection();

		return container;
	}

	public void updateControlsFromSelection() {
		IStructuredSelection selection = (IStructuredSelection) listViewer
				.getSelection();
		account selectedAccount = (account) selection.getFirstElement();
		account defaultAccount = ((ContentProvider) listViewer
				.getContentProvider()).getAllAccounts().getDefaultAccount();
		if (selection.isEmpty()) {
			// default select first element (new asset), e.g. if current element
			// wasd deleted
			selectedAccount = defaultAccount;
		}
		if (selectedAccount.getID() == defaultAccount.getID()) {
			btnDelete.setEnabled(false);
		} else {
			btnDelete.setEnabled(true);
		}

		lblID.setText(Integer.toString(selectedAccount.getID()));
		txtFldCode.setText(selectedAccount.getCode());
		btnAutoVAT.setSelection(selectedAccount.isAutoVAT());
		
		txtFldDescription.setText(selectedAccount.getDescription());
		cmbType.select(selectedAccount.getType());
		cmbRefersTo.select(selectedAccount.getSubAccountTypesCode());

	}

	private void createActions() {
	}

	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager(
				Messages.getString("accountDefinitionWindow.menu")); //$NON-NLS-1$
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
		return new Point(500, 442);
	}
}
