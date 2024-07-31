package GUILayer;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JPanel;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;

import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.desktop.DesktopException;
import ag.ion.bion.officelayer.desktop.IFrame;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.DocumentException;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.text.ITextCursor;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextField;
import ag.ion.bion.officelayer.text.ITextRange;
import ag.ion.bion.officelayer.text.ITextTableCell;
import ag.ion.bion.officelayer.text.IViewCursor;
import ag.ion.noa.NOAException;
import ag.ion.noa.frame.IDispatch;
import appLayer.client;
import appLayer.configs;
import appLayer.contact;
import appLayer.itemTableList;
import appLayer.itemTextList;
import appLayer.placeholderManager;
import appLayer.transactionRelated.appTransaction;

import com.sun.star.datatransfer.XTransferable;
import com.sun.star.datatransfer.XTransferableSupplier;
import com.sun.star.frame.XController;
import com.sun.star.uno.UnoRuntime;

public class newTransactionWizardPreview extends WizardPage {

	private newTransactionWizard parentWizard;
	private IFrame officeFrame = null;
	private JPanel officePanel = null;
	private boolean isInititalized = false;

	/**/
	public newTransactionWizardPreview(newTransactionWizard parent) {
		super(Messages.getString("newTransactionWizardPreview.transaction")); //$NON-NLS-1$
		parentWizard = parent;
		setTitle(Messages
				.getString("newTransactionWizardPreview.newTransaction")); //$NON-NLS-1$
		setDescription(Messages.getString("newTransactionWizardPreview.windowDescription")); //$NON-NLS-1$
	}

	public void onActivate() {
		// process();
	}

	@Override
	public String getName() {
		return Messages.getString("newTransactionWizardPreview.preview"); //$NON-NLS-1$
	}

	public void createControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NO_BACKGROUND
				| SWT.EMBEDDED);
		try {
			System.setProperty("sun.awt.noerasebackground", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NoSuchMethodError error) {
			System.out
					.println(Messages
							.getString("newTransactionWizardPreview.backgroundnotErased")); //$NON-NLS-1$
		}
		setControl(container);

		final Frame frame = SWT_AWT.new_Frame(container);
		frame.setLayout(new BorderLayout());
		officePanel = new JPanel(new BorderLayout());
		frame.add(officePanel, BorderLayout.CENTER);
	}

	public void insertHeader() {
		String transactionPrefix = client.getTransactions()
				.getCurrentTransaction().getTypePrefix();
		// clear text (maybe preview was filled out before, switched back a page
		// and now flips back on preview page again)
		String url = "file:///" + client.getDataPath() + //$NON-NLS-1$
				transactionPrefix + "template1.odt"; //$NON-NLS-1$
		try {
			parentWizard.setDocument((ITextDocument)
			configs.getOfficeApplication().getDocumentService().loadDocument(officeFrame, url));
		} catch (OfficeApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void process() {
		// now the header is loaded, do the processing
		insertHeader();
		//		newTransactionSelectTransactionDetails transactionSelection = (newTransactionSelectTransactionDetails)this.parentWizard.getPage("selectTransaction"); //$NON-NLS-1$
		// int intCustomerID = transactionSelection.getSelectedCustomerIndex();
		// client.getTransactions().getCurrentTransaction().bindToCustomer(intCustomerID);
		placeholderManager placeholderReplacements = new placeholderManager();
		placeholderReplacements.setTransaction(client.getTransactions()
				.getCurrentTransaction());
		// check if table
		ITextField[] placeholderFields = null;
		boolean startIsInTable = false;
		try {
			placeholderFields = parentWizard.getDocument()
					.getTextFieldService().getPlaceholderFields();
			for (int i = 0; i < placeholderFields.length; i++) {
				if (placeholderFields[i].getDisplayText().equals(
						placeholderManager.START_PLACEHOLDER
								+ placeholderManager.ITEMS_START_PLACEHOLDER
								+ placeholderManager.END_PLACEHOLDER)) {
					ITextRange textRangeStart = placeholderFields[i]
							.getTextRange();
					ITextTableCell startCell = textRangeStart.getCell();
					startIsInTable = startCell != null;
				}
			}

			// process table
			if (startIsInTable) {
				new itemTableList(getShell(), parentWizard.getDocument(),
						client.getTransactions().getCurrentTransaction(),
						placeholderReplacements, placeholderFields).process();
			}
			// process text
			else {
				new itemTextList(getShell(), parentWizard.getDocument(), client
						.getTransactions().getCurrentTransaction(),
						placeholderReplacements, placeholderFields).process();
			}

			doReplaceItemless(placeholderReplacements, client.getTransactions()
					.getCurrentTransaction());

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		if (configs.shallPrintPoweredBy()) {
			ITextCursor textCursor = parentWizard.getDocument()
					.getTextService().getCursorService().getTextCursor();
			textCursor.gotoEnd(true);
			textCursor
					.getEnd()
					.setText(
							Messages.getString("newTransactionWizardPreview.poweredByGnuaccounting")); //$NON-NLS-1$
			/*
			 * IParagraph paragraph = null; try { paragraph =
			 * parentWizard.getDocument().getTextService()
			 * .getTextContentService().constructNewParagraph();
			 * parentWizard.getDocument().getTextService()
			 * .getTextContentService() .insertTextContent(textCursor.getEnd(),
			 * paragraph); paragraph .setParagraphText(Messages
			 * .getString("newTransactionWizardPreview.poweredByGnuaccounting"
			 * )); //$NON-NLS-1$
			 * 
			 * } catch (TextException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); }
			 */

		}
	}

	public void initOOo() {
		if (!isInititalized) {
			IOfficeApplication officeApplication = configs
					.getOfficeApplication();
			ITextDocument textDoc = null;
			try {
				// embedd OOo Frame only if it's possible
				if (configs.isOOoEmbedded())
					officeFrame = officeApplication.getDesktopService()
							.constructNewOfficeFrame(officePanel);
				else
					officeFrame = null;
				textDoc = (ITextDocument) officeApplication
						.getDocumentService().constructNewDocument(officeFrame,
								IDocument.WRITER, DocumentDescriptor.DEFAULT);
				if (officeFrame == null) {
					officeFrame = textDoc.getFrame();
				}
				parentWizard.setDocument(textDoc);
			} catch (DesktopException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NOAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OfficeApplicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			parentWizard.setOfficeFrame(officeFrame);
			officePanel.setVisible(true);
			officePanel.validate();
			isInititalized = true;
		}
	}

	@Override
	public void setVisible(boolean isVisible) {
		if (isVisible) {
			initOOo();
			process();
		}
		super.setVisible(isVisible);
	}

	/*deb  
	private void appendDocument(String url) {
		


			      
		  //load source
		  IDocument documentTemplate;
		try {
			documentTemplate = configs.getOfficeApplication().getDocumentService().loadDocument(url,
			      DocumentDescriptor.DEFAULT_HIDDEN);
		      ITextDocument textDocumentTemplate = (ITextDocument) documentTemplate;
		      //marks the text in the document to be used as source
		      IViewCursor templateViewCursor = textDocumentTemplate.getViewCursorService().getViewCursor();
		      templateViewCursor.getPageCursor().jumpToEndOfPage();
		      ITextRange end = templateViewCursor.getTextCursorFromEnd().getEnd();
		      templateViewCursor.getPageCursor().jumpToStartOfPage();
		      templateViewCursor.goToRange(end, true);

		      //load target
		      IDocumentDescriptor descriptor = DocumentDescriptor.DEFAULT;
		      descriptor.setAsTemplate(true);
		      IDocument document;

			      //go to end of target document
			      IViewCursor viewCursor = parentWizard.getDocument().getViewCursorService().getViewCursor();
			      IPageCursor pageCursor = viewCursor.getPageCursor();     
			      pageCursor.jumpToEndOfPage();

			      //maybe insert a page break before
			      ITextCursor textCursor = viewCursor.getTextCursorFromEnd();
			      textCursor.insertPageBreak();

			      //copy from soruce to target
			      copy(textDocumentTemplate, parentWizard.getDocument());
//					parentWizard.getDocument().getViewCursorService().getViewCursor()
//							.getTextCursorFromStart().insertDocument(url);

		} catch (OfficeApplicationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	

	  private static void copy(ITextDocument sourceDoc, ITextDocument targetDoc) throws Exception {
	    // the controllers 
	    XController xController_sourceDoc = sourceDoc.getXTextDocument().getCurrentController();
	    XController xController_targetDoc = targetDoc.getXTextDocument().getCurrentController();
	    // getting the data supplier of our source doc 
	    XTransferableSupplier xTransferableSupplier_sourceDoc = (XTransferableSupplier) UnoRuntime.queryInterface(XTransferableSupplier.class,
	        xController_sourceDoc);
	    // saving the selected contents 
	    XTransferable xTransferable = xTransferableSupplier_sourceDoc.getTransferable();
	    // getting the data supplier of our target doc 
	    XTransferableSupplier xTransferableSupplier_targetDoc = (XTransferableSupplier) UnoRuntime.queryInterface(XTransferableSupplier.class,
	        xController_targetDoc);
	    // inserting the source document there 
	    xTransferableSupplier_targetDoc.insertTransferable(xTransferable);
	  }

	/**
	 * Replaces the placeholder not mentioned in the <items>..</items> area,
	 * i.e. usually customer, tax and transaction placeholders (and bezahlcode)
	 * */
	private void doReplaceItemless(placeholderManager placeholderReplacements,
			appTransaction currentTransaction) throws Throwable {
		ITextField[] placeholderFields = parentWizard.getDocument()
				.getTextFieldService().getPlaceholderFields();
		placeholderReplacements.setShell(getShell());
		for (int i = 0; i < placeholderFields.length; i++) {
			ITextField placeholder = placeholderFields[i];
			ITextRange range = placeholder.getTextRange();

			String value = placeholderReplacements
					.getPlaceholderValue(placeholder);

			range.setText(value);

		}

		contact recipient = client.getTransactions().getCurrentTransaction()
				.getRecipient();

		boolean isValidDeduction = (recipient.getPaymentmethod() == 1)
				&& (recipient.getIBAN().length() > 0)
				&& (configs.getCreditorID().length() > 0)
				&& (configs.getIBAN().length() > 0)
				&& (recipient.getSEPAmandate().length() > 0);
		// if no deduction, cut (=delete) from payment-deduction.../payment-deduction
		if (!isValidDeduction) {

			if ((placeholderReplacements.getDeductionStart() != null)
					&& (placeholderReplacements.getDeductionEnd() != null)) {
				placeholderReplacements.getDeductionStart().setText(""); //$NON-NLS-1$
				placeholderReplacements.getDeductionEnd().setText(""); //$NON-NLS-1$
				IViewCursor viewCursor = parentWizard.getDocument()
						.getViewCursorService().getViewCursor();

				viewCursor.goToRange(
						placeholderReplacements.getDeductionStart(), false);

				// now start marking (true) to the second occurence
				viewCursor.goToRange(placeholderReplacements.getDeductionEnd(),
						true);
				IDispatch cut = parentWizard.getDocument().getFrame()
						.getDispatch(".uno:Cut"); //$NON-NLS-1$
				cut.dispatch();
			}
		}
		else {
// cut invoice part
			if ((placeholderReplacements.getInvoiceStart() != null)
					&& (placeholderReplacements.getInvoiceEnd() != null)) {
				placeholderReplacements.getInvoiceStart().setText(""); //$NON-NLS-1$
				placeholderReplacements.getInvoiceEnd().setText(""); //$NON-NLS-1$
				IViewCursor viewCursor = parentWizard.getDocument()
						.getViewCursorService().getViewCursor();

				viewCursor.goToRange(
						placeholderReplacements.getInvoiceStart(), false);

				// now start marking (true) to the second occurence
				viewCursor.goToRange(placeholderReplacements.getInvoiceEnd(),
						true);
				IDispatch cut = parentWizard.getDocument().getFrame()
						.getDispatch(".uno:Cut"); //$NON-NLS-1$
				cut.dispatch();
			}
			
		}

	}

}
