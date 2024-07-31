package GUILayer;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import appLayer.appUser;
import appLayer.application;
import appLayer.configs;
import appLayer.users;

public class userWindow extends ApplicationWindow {
	private Text txtPassword;
	private Text txtUsername;
	private ListViewer listViewer;
	private Text txtPassword2;
	private Button btnDelete;
	private appUser newUserToCreateOnAdminPassword = null;

	class userSelectionChangedListener implements ISelectionChangedListener {
		protected userWindow parent;

		public userSelectionChangedListener(userWindow parent) {
			this.parent = parent;

		}

		public void selectionChanged(SelectionChangedEvent arg0) {
			parent.updateControlsFromSelection();

		}
	}

	/**
	 * Create the application window.
	 */
	public userWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	public void updateControlsFromSelection() {

		Object[] objects = ((users) listViewer.getContentProvider())
				.getElements(null);
		appUser defaultUser = (appUser) objects[0];

		IStructuredSelection selection = (IStructuredSelection) listViewer
				.getSelection();
		appUser selectedUser = (appUser) selection.getFirstElement();
		if (selection.isEmpty()) {
			// default select first element (new asset), e.g. if current element
			// wasd deleted
			btnDelete.setEnabled(false);
			selectedUser = defaultUser;
		}
		if ((selectedUser.getID() != appUser.newUserID)
				&& (selectedUser.getID() != appUser.defaultUserID)) {
			btnDelete.setEnabled(true);

		} else {
			btnDelete.setEnabled(false);
			txtUsername.setEnabled(false);
		}
		if (selectedUser.getID() != appUser.defaultUserID) {
			txtUsername.setEnabled(true);
		} else {
			txtUsername.setEnabled(false);
		}
		txtUsername.setText(selectedUser.getUsername());
		if (selectedUser.getID() == appUser.newUserID) {
			txtUsername.setText(""); //$NON-NLS-1$
			txtUsername.setFocus();
		}
		txtPassword.setText(""); //$NON-NLS-1$
		txtPassword2.setText(""); //$NON-NLS-1$

	}

	/**
	 * Create contents of the application window.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);

		GridLayoutFactory.swtDefaults().numColumns(2).margins(10, 5)
		.applyTo(container);

		listViewer = new ListViewer(container, SWT.V_SCROLL | SWT.BORDER);
		listViewer
				.addSelectionChangedListener(new userSelectionChangedListener(
						this));
		List list = listViewer.getList();
		list.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 120).span(2,1).grab(true, true)
		.applyTo(list);

		listViewer.setContentProvider(application.getUsers());
		listViewer.setInput(new Object());
		

		Label lblUsername = new Label(container, SWT.NONE);
		lblUsername.setAlignment(SWT.RIGHT);
		lblUsername.setText(Messages.getString("userWindow.username")); //$NON-NLS-1$
		lblUsername.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(lblUsername);


		txtUsername = new Text(container, SWT.BORDER);
		txtUsername.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(txtUsername);

		Label lblPassword = new Label(container, SWT.NONE);
		lblPassword.setAlignment(SWT.RIGHT);
		lblPassword.setText(Messages.getString("userWindow.password")); //$NON-NLS-1$
		lblPassword.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(lblPassword);
		
		txtPassword = new Text(container, SWT.BORDER | SWT.PASSWORD);
		txtPassword.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(txtPassword);

		

		Label lblRepeatPassword = new Label(container, SWT.NONE);
		lblRepeatPassword.setAlignment(SWT.RIGHT);
		lblRepeatPassword.setText(Messages
				.getString("userWindow.repeatPassword")); //$NON-NLS-1$
		lblRepeatPassword.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(lblRepeatPassword);

		txtPassword2 = new Text(container, SWT.BORDER | SWT.PASSWORD);
		txtPassword2.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(txtPassword2);



		
		Button btnOk = new Button(container, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) listViewer
						.getSelection();
				appUser selectedUser = (appUser) selection.getFirstElement();

				boolean newUserCreated = false;
				int errors = 0;

				if (selectedUser.getID() == appUser.newUserID) {
					selectedUser = new appUser();
					newUserCreated = true;
				}
				if (txtPassword.getText().length() > 0) {
					if (!txtPassword.getText().equals(txtPassword2.getText())) {
						MessageBox messageBox = new MessageBox(getShell(),
								SWT.ICON_ERROR | SWT.OK);
						messageBox.setText(Messages
								.getString("userWindow.passwordDontMatchLabel")); //$NON-NLS-1$
						messageBox.setMessage(Messages
								.getString("userWindow.passwordDontMatchText")); //$NON-NLS-1$
						messageBox.open();
						txtPassword.setText(""); //$NON-NLS-1$
						txtPassword2.setText(""); //$NON-NLS-1$

						return;

					}
				}
				if (txtPassword.getText().length() < 5) {
					errors++;
					getStatusLineManager().setErrorMessage(
							Messages.getString("userWindow.passwordTooShort")); //$NON-NLS-1$

				}
				if (txtUsername.getText().length() == 0) {
					errors++;
					getStatusLineManager().setErrorMessage(
							Messages.getString("userWindow.usernameEmpty")); //$NON-NLS-1$

				}
				if (txtUsername.getText().indexOf(' ') > 0) {
					errors++;
					getStatusLineManager().setErrorMessage(
							Messages.getString("userWindow.usernameNoSpaces")); //$NON-NLS-1$

				}

				if (errors == 0) {
					if ((newUserCreated)
							&& (txtPassword.getText().length() == 0)) {
						MessageBox messageBox = new MessageBox(getShell(),
								SWT.ICON_ERROR | SWT.OK);
						messageBox.setText(Messages
								.getString("userWindow.newUserPasswordRequiredLabel")); //$NON-NLS-1$
						messageBox.setMessage(Messages
								.getString("userWindow.newUserPasswordRequiredText")); //$NON-NLS-1$
						messageBox.open();
						return;
					}

					selectedUser.setUsername(txtUsername.getText());
					if (txtPassword.getText().length() > 0) {
						selectedUser.setPassword(txtPassword.getText());

					}
					if (newUserCreated) {
						if (application.getUsers().emptyAdminPassword()) {

							MessageBox messageBox = new MessageBox(getShell(),
									SWT.ICON_ERROR | SWT.OK);
							messageBox.setText(Messages
									.getString("userWindow.adminPasswordRequiredLabel")); //$NON-NLS-1$
							messageBox.setMessage(Messages
									.getString("userWindow.asminPasswordRequiredText")); //$NON-NLS-1$
							messageBox.open();

							newUserToCreateOnAdminPassword = selectedUser;
							listViewer.getList().select(0);
							updateControlsFromSelection();
							return;
						}
					}
					if ((txtUsername.getText()
							.equalsIgnoreCase("Administrator")) && (newUserToCreateOnAdminPassword != null)) { //$NON-NLS-1$
						newUserToCreateOnAdminPassword.save();
					}
					selectedUser.save();
					listViewer.refresh();

					if (newUserCreated) {
						listViewer.getList().select(
								listViewer.getList().getItemCount() - 1);
					}

				}

			}
		});
		btnOk.setText(Messages.getString("userWindow.okButtonLabel")); //$NON-NLS-1$
		btnOk.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 30).grab(true, false)
		.applyTo(btnOk);

		
		
		btnDelete = new Button(container, SWT.NONE);
		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) listViewer
						.getSelection();
				appUser selectedUser = (appUser) selection.getFirstElement();

				boolean newUserCreated = false;

				if (selectedUser.getID() != appUser.newUserID) {
					selectedUser.delete();
					listViewer.remove(selectedUser);
					listViewer.refresh();
					listViewer.getList().select(0);
					updateControlsFromSelection();
					getStatusLineManager().setErrorMessage(""); //$NON-NLS-1$
				}

			}
		});
		btnDelete.setEnabled(false);
		btnDelete.setText(Messages.getString("userWindow.delete")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 30).grab(true, false)
		.applyTo(btnDelete);

		return container;
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Create the menu manager.
	 * 
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager("menu"); //$NON-NLS-1$
		return menuManager;
	}

	/**
	 * Create the toolbar manager.
	 * 
	 * @return the toolbar manager
	 */
	@Override
	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);
		return toolBarManager;
	}

	/**
	 * Create the status line manager.
	 * 
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		return statusLineManager;
	}

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			userWindow window = new userWindow();
			window.setBlockOnOpen(true);
			window.open();
			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the shell.
	 * 
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(application.getAppName());
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 353);
	}
}
