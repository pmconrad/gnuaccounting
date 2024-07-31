package GUILayer;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalListener2;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;

import appLayer.account;
import appLayer.client;

public class AccountTextCellEditor extends TextCellEditor {
	private openableContentProposalAdapter contentProposalAdapter;
	private account originalAccount = null;
	private boolean popupOpen = false; // true when popup is open

	@Override
	protected void focusLost() {
		if (!popupOpen) {
			// Focus lost deactivates the cell editor.
			// This must not happen if focus lost was caused by activating
			// the completion proposal popup.
			super.focusLost();
		}
	}

	// avoid focus issues:
	@Override
	protected boolean dependsOnExternalFocusListener() {
		return false;
	}// even more focus issues closed at
		// http://javafind.appspot.com/model?id=318036

	class openableContentProposalAdapter extends ContentProposalAdapter {

		public openableContentProposalAdapter(Control control,
				IControlContentAdapter controlContentAdapter,
				IContentProposalProvider proposalProvider, KeyStroke keyStroke,
				char[] autoActivationCharacters) {
			super(control, controlContentAdapter, proposalProvider, keyStroke,
					autoActivationCharacters);
		}

		public void openProposalPopup() {
			super.openProposalPopup();
		}

		public void closeProposalPopup() {
			super.closeProposalPopup();
		}

	}

	@Override
	public void activate() {

		if (contentProposalAdapter == null) {
			// auswahl von ausgewaehltem kto
			SimpleContentProposalProvider proposalProvider = new SimpleContentProposalProvider(
					client.getAccounts().getStringArray(false));
			proposalProvider.setFiltering(true);
			// enable content assist on the cell editor's text widget
			contentProposalAdapter = new openableContentProposalAdapter(text,
					new TextContentAdapter(), proposalProvider, null, null);
			char[] autoProps = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
					'9' };
			contentProposalAdapter.setAutoActivationCharacters(autoProps);
			contentProposalAdapter
					.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
			contentProposalAdapter
					.addContentProposalListener(new IContentProposalListener2() {

						@Override
						public void proposalPopupOpened(
								ContentProposalAdapter arg0) {
							popupOpen = true;
						}

						@Override
						public void proposalPopupClosed(
								ContentProposalAdapter arg0) {
							popupOpen = false;
						}
					});
		} else {
			contentProposalAdapter.setEnabled(true);

		}
		super.activate();
		// contentProposalAdapter.openProposalPopup();

	}

	public AccountTextCellEditor() {
		super();
	}

	public AccountTextCellEditor(Table t) {
		super(t);
	}

	@Override
	protected Object doGetValue() {
		account selected = client.getAccounts().getAccountByFullString(
				(String) super.doGetValue());
		if (selected == null) {
			selected = originalAccount;
		}
		if (selected == null) {
			selected = client.getAccounts().getFirstSelectableAccount();
		}
		return selected;

	}

	@Override
	protected void doSetValue(Object value) {
		// account selected = client.getAccounts().getAccountByFullString(
		// (String) value);
		originalAccount = (account) value;
		if (originalAccount != null) {
			super.doSetValue(originalAccount.getAsString());
		}
	}

}
