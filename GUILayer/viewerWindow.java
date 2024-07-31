package GUILayer;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JPanel;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.desktop.GlobalCommands;
import ag.ion.bion.officelayer.desktop.IFrame;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.text.ITextDocument;
import appLayer.application;
import appLayer.configs;

public class viewerWindow extends ApplicationWindow {

	private String url;
	private boolean readOnly = false;
	protected ITextDocument textDocument = null;

	public viewerWindow(String url) {
		super(null);
		this.url = url;
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	public void setReadonly() {
		readOnly = true;
	}

	protected Control createContents(Composite parent) {
		parent.getShell().setSize(getInitialSize());

		Composite container = new Composite(parent, SWT.EMBEDDED);
		try {
			System.setProperty("sun.awt.noerasebackground", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NoSuchMethodError error) {
			System.out.println(Messages.getString("viewerWindow.bgNotErased")); //$NON-NLS-1$
		}
		container.setLayout(new FillLayout());

		final Frame frame = SWT_AWT.new_Frame(container);

		final JPanel panel = new JPanel(new BorderLayout());
		panel.setVisible(true);
		frame.add(panel, BorderLayout.CENTER);
		try {

			IOfficeApplication officeApplication = configs
					.getOfficeApplication();

			IFrame officeFrame;

			// embedd OOo Frame only if it's possible
			if (configs.isOOoEmbedded())
				officeFrame = officeApplication.getDesktopService()
						.constructNewOfficeFrame(panel);
			else
				officeFrame = null;

			// officeApplication.getDocumentService().constructNewDocument(officeFrame,
			// IDocument.WRITER, DocumentDescriptor.DEFAULT);
			DocumentDescriptor descriptor = DocumentDescriptor.DEFAULT;
			if (readOnly) {
				descriptor.setReadOnly(true);
			}
			IDocument document = officeApplication.getDocumentService()
					.loadDocument(officeFrame, this.url, descriptor);
			textDocument = (ITextDocument) document;

			frame.validate();

			if (officeFrame == null) {
				officeFrame = document.getFrame();
			}
			// Now it is time to disable two commands in the frame
			officeFrame.disableDispatch(GlobalCommands.CLOSE_DOCUMENT);
			officeFrame.disableDispatch(GlobalCommands.QUIT_APPLICATION);
			officeFrame.updateDispatches();

			/*
			 * textDocument.getViewCursorService().getViewCursor().getPageCursor(
			 * ).jumpToLastPage();
			 */

		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}

		//
		return container;
	}

	private void createActions() {
	}

	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager(
				Messages.getString("viewerWindow.menu")); //$NON-NLS-1$
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

	/*
	 * public static void main(String args[]) { try { viewerWindow window = new
	 * viewerWindow(); window.setBlockOnOpen(true); window.open();
	 * Display.getCurrent().dispose(); } catch (Exception e) {
	 * e.printStackTrace(); } }
	 */

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(application.getAppName());
		newShell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
			}
		});
	}

	@Override
	public boolean close() {
		textDocument.close();
		// TODO Auto-generated method stub
		return super.close();
	}

	protected Point getInitialSize() {
		return new Point(500, 375);
	}

}
