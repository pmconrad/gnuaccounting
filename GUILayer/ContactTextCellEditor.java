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

import appLayer.client;
import appLayer.contact;

public class ContactTextCellEditor extends TextCellEditor {
	private openableContentProposalAdapter contentProposalAdapter;
	private contact originalContact = null;
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
					client.getContacts().getStringArray(false));
			proposalProvider.setFiltering(true);
			// enable content assist on the cell editor's text widget
			contentProposalAdapter = new openableContentProposalAdapter(text,
					new TextContentAdapter(), proposalProvider, null, null);
			char[] autoProps = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray(); //$NON-NLS-1$

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

	public ContactTextCellEditor() {
		super();
	}

	public ContactTextCellEditor(Table t) {
		super(t);
	}

	@Override
	protected Object doGetValue() {
		
		contact selected = client.getContacts().getContactByName(
				(String) super.doGetValue());
		if (selected == null) {
			selected = originalContact;
		}
		if (selected == null) {
			selected = client.getContacts().getContacts().get(0);
		}
		return selected;

	}

	@Override
	protected void doSetValue(Object value) {
		originalContact = (contact) value;
		if (originalContact != null) {
			super.doSetValue(originalContact.getName());
		}
	}

}
