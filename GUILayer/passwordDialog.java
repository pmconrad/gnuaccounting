package GUILayer;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import appLayer.configs;

public class passwordDialog extends Dialog {
	private static final int RESET_ID = IDialogConstants.NO_TO_ALL_ID + 1;

	private Text usernameField;

	private Text passwordField;
	private String username = ""; //$NON-NLS-1$
	private String password = ""; //$NON-NLS-1$

	public passwordDialog(Shell parentShell, String usernameDefault) {
		super(parentShell);
		username = usernameDefault;
	}

	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);

		GridLayout layout = (GridLayout) comp.getLayout();
		layout.numColumns = 2;

		Label usernameLabel = new Label(comp, SWT.RIGHT);
		usernameLabel.setText(Messages.getString("passwordDialog.username")); //$NON-NLS-1$
		usernameLabel.setFont(configs.getDefaultFont());

		usernameField = new Text(comp, SWT.SINGLE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		usernameField.setLayoutData(data);
		usernameField.setFont(configs.getDefaultFont());
		usernameField.setText(username);

		Label passwordLabel = new Label(comp, SWT.RIGHT);
		passwordLabel.setText(Messages.getString("passwordDialog.password")); //$NON-NLS-1$
		passwordLabel.setFont(configs.getDefaultFont());

		passwordField = new Text(comp, SWT.SINGLE | SWT.PASSWORD);
		data = new GridData(GridData.FILL_HORIZONTAL);
		passwordField.setLayoutData(data);
		passwordField.setFont(configs.getDefaultFont());

		if (username.length() == 0) {
			// no default username
			usernameField.setFocus();
		} else {
			passwordField.setFocus();
		}
		return comp;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		createButton(parent, RESET_ID,
				Messages.getString("passwordDialog.resetAll"), false); //$NON-NLS-1$
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == RESET_ID) {
			usernameField.setText(username);
			passwordField.setText(""); //$NON-NLS-1$
		} else {
			username = usernameField.getText();
			password = passwordField.getText();
			super.buttonPressed(buttonId);
		}
	}
}