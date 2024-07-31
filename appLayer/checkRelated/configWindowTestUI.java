package appLayer.checkRelated;

import org.eclipse.jface.dialogs.MessageDialog;

import GUILayer.configWindow;

public class configWindowTestUI implements IcheckInteractionProvider {

	private configWindow cw = null;

	public configWindowTestUI(configWindow cw) {
		this.cw = cw;
	}

	@Override
	public boolean confirm(String title, String message) {
		return MessageDialog.openQuestion(this.cw.getShell(), title, message);

	}

	@Override
	public void output(String s) {
		cw.listStatus.add(s);
	}

}
