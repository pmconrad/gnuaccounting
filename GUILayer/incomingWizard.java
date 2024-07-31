package GUILayer;

import org.eclipse.jface.wizard.Wizard;

public class incomingWizard extends Wizard {

	private incomingWizardDetailsPage iwd = null;

	public incomingWizard() {
		iwd = new incomingWizardDetailsPage();

	}

	@Override
	public void addPages() {
		addPage(iwd);
	}

	@Override
	public boolean performFinish() {
		iwd.processDocument();
		iwd.book();
		return true;
	}

}
