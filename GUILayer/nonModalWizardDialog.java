package GUILayer;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 * we need non-modal wizard dialogs when we e.g. want to show an
 * applicationWindow. There e.g. is a link "manage customers" in the new
 * transaction dialog that opens the applicationwindow contactsWindow. If this
 * class weren't used, one could not focus on the new window, thus not change
 * any customer.
 * */
public class nonModalWizardDialog extends WizardDialog {
	nonModalWizardDialog(IWizard newWizard) {
		super(new Shell(), newWizard);
		setShellStyle(SWT.SHELL_TRIM);
	}
}
