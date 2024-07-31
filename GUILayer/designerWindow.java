package GUILayer;

// example or an url for a doc: file:///C:/Users/jstaerk/.gnuaccounting/0000/CA-template1.odt

import java.awt.BorderLayout;
import java.awt.Frame;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Vector;

import javax.swing.JPanel;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.desktop.DesktopException;
import ag.ion.bion.officelayer.desktop.GlobalCommands;
import ag.ion.bion.officelayer.desktop.IFrame;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.DocumentException;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.noa.NOAException;
import ag.ion.noa.document.URLAdapter;
import ag.ion.noa.frame.IDispatchDelegate;
import ag.ion.noa.frame.ILayoutManager;
import appLayer.application;
import appLayer.client;
import appLayer.configs;
import appLayer.transactionRelated.transactionType;

public class designerWindow extends ApplicationWindow {

	Vector<String> vecPrefix = new Vector<String>();
	private String transactionPrefix = Messages
			.getString("designerWindow.invoicePrefix"); //$NON-NLS-1$
	private Button saveButton = null;
	private IFrame officeFrame1 = null;
	private ITextDocument textDocument1 = null;
	IOfficeApplication officeApplication = null;

	private Combo cmbTemplate;

	public designerWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	public void saveTemplateWithProgress() {
		final Display display = getShell().getDisplay();
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				transactionPrefix = (String) vecPrefix.elementAt(cmbTemplate
						.getSelectionIndex());
				try {
					new ProgressMonitorDialog(getShell()).run(true, false,
							new IRunnableWithProgress() {
								@Override
								public void run(IProgressMonitor progressMonitor)
										throws InvocationTargetException,
										InterruptedException {
									progressMonitor.beginTask(
											Messages.getString("designerWindow.savingTemplate"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$

									String filename = client.getDataPath()
											+ transactionPrefix
											+ "template1.odt"; //$NON-NLS-1$
									String url = null;
									try {
										url = URLAdapter.adaptURL(filename);
									} catch (MalformedURLException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									} catch (UnknownHostException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									try {
										textDocument1.getPersistenceService()
												.store(url);
									} catch (DocumentException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} //$NON-NLS-1$
									display.syncExec(new Runnable() {
										@Override
										public void run() {
											todoWindow.refreshToDoList();
										}
									});
									progressMonitor.done();
								}
							});
				} catch (Throwable throwable) {
					throwable.printStackTrace();
				}
			}
		});

	}

	private void loadTemplateWithProgress() {
		try {
			transactionPrefix = (String) vecPrefix.elementAt(cmbTemplate
					.getSelectionIndex());
			new ProgressMonitorDialog(getShell()).run(true, false,
					new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor progressMonitor)
								throws InvocationTargetException,
								InterruptedException {
							progressMonitor.beginTask(
									Messages.getString("designerWindow.loadingTemplate"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
							officeApplication = configs.getOfficeApplication();

							// setCursor(new Cursor(Cursor.WAIT_CURSOR));
							String filename = client.getDataPath()
									+ transactionPrefix + "template1.odt"; //$NON-NLS-1$
							String url = null;
							try {
								url = URLAdapter.adaptURL(filename);
							} catch (MalformedURLException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (UnknownHostException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}

							try {
								IDocument oldDoc = textDocument1;

								textDocument1 = (ITextDocument) officeApplication
										.getDocumentService().loadDocument(
												officeFrame1, url,
												DocumentDescriptor.DEFAULT);

								if (officeFrame1 == null) {
									officeFrame1 = textDocument1.getFrame();
								}

								ILayoutManager layoutManager = officeFrame1
										.getLayoutManager();

								layoutManager
										.showElement(ILayoutManager.URL_TOOLBAR_TEXTOBJECTBAR);

								// Now it is time to disable two commands in the
								// frame
								officeFrame1
										.disableDispatch(GlobalCommands.CLOSE_DOCUMENT);
								officeFrame1
										.disableDispatch(GlobalCommands.QUIT_APPLICATION);

								officeFrame1.addDispatchDelegate(
										GlobalCommands.SAVE,
										new IDispatchDelegate() {
											@Override
											public void dispatch(
													Object[] objects) {
												saveTemplateWithProgress();
											}
										});
								officeFrame1.updateDispatches();

								if (oldDoc != null && oldDoc.isOpen()) {
									oldDoc.close();
								}
							} catch (DocumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (NOAException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (OfficeApplicationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							// textDocument1.addCloseListener(new
							// OOoCloseListener());

							// saveButton.setEnabled(true);
							progressMonitor.done();
						}
					});
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}

	}

	protected Control createContents(Composite parent) {
		parent.getShell().setSize(getInitialSize());

		officeApplication = configs.getOfficeApplication();
		Composite container = new Composite(parent, SWT.NONE);
		container.setCapture(true);
		container.setDragDetect(false);
		GridLayoutFactory.swtDefaults().numColumns(3).margins(10, 5)
		.applyTo(container);
		
		

		Label lblTemplate = new Label(container, SWT.NONE);
		lblTemplate.setAlignment(SWT.RIGHT);
		lblTemplate.setFont(configs.getDefaultFont());
		lblTemplate.setText(Messages.getString("designerWindow.template"));  //$NON-NLS-1$
		GridDataFactory.swtDefaults().applyTo(lblTemplate);

		
		cmbTemplate = new Combo(container, SWT.READ_ONLY);
		cmbTemplate.setFont(configs.getDefaultFont());
		cmbTemplate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				loadTemplateWithProgress();
			}
		});


		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(cmbTemplate);

		transactionType[] types = client.getTransactions().getAllTypes();
		for (transactionType currentTransaction : types) {
			cmbTemplate.add(currentTransaction.getTypeName());
			vecPrefix.add(currentTransaction.getTypePrefix());

		}
		transactionPrefix = (String) vecPrefix.elementAt(0);

		cmbTemplate.select(0);// select first template -- invoice


		saveButton = new Button(container, SWT.NONE);
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public synchronized void widgetSelected(SelectionEvent e) {
				saveTemplateWithProgress();
			}
		});
		saveButton.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(cmbTemplate);

		saveButton.setText(Messages.getString("designerWindow.saveButton")); //$NON-NLS-1$

		final Composite composite = new Composite(container, SWT.EMBEDDED);

		composite.setLayout(new FillLayout());

		Frame frame = SWT_AWT.new_Frame(composite);

		final JPanel panel = new JPanel(new BorderLayout());
		frame.add(panel, BorderLayout.CENTER);
		panel.setVisible(true);
		try {

			// embedd OOo Frame only if it's possible
			if (configs.isOOoEmbedded()) {
				officeFrame1 = officeApplication.getDesktopService()
						.constructNewOfficeFrame(panel);

			} else {
				officeFrame1 = null;

			}

		} catch (DesktopException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (OfficeApplicationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		frame.validate();
		
		GridDataFactory.fillDefaults().span(3,1).hint(100,500).grab(true, true)
		.applyTo(composite);
		
		try {
			parent.getShell().addListener(SWT.Show, new Listener() {
				@Override
				public void handleEvent(Event event) {
					loadTemplateWithProgress();
				}
			});

			/**
			 * We obviously need to load/construct a document, the app sometimes
			 * seems to "hang" if we do it only in loadTemplates, this way we
			 * construct empty documents here and overwrite in loadTemplates
			 * (bug 1804084)
			 */

			/*
			 * IDocument
			 * document=officeApplication.getDocumentService().constructNewDocument
			 * (officeFrame1, IDocument.WRITER, DocumentDescriptor.DEFAULT);
			 * textDocument1=(ITextDocument)document;
			 * textDocument1.addDocumentListener(new DocumentAdapter(){
			 * 
			 * @Override public void onSave(IDocumentEvent documentEvent) {
			 * super.onSave(documentEvent); waitForSaveComplete=true; }
			 * 
			 * @Override public void onSaveAs(IDocumentEvent documentEvent) {
			 * super.onSaveAs(documentEvent); waitForSaveComplete=true; }
			 * 
			 * @Override public void onSaveAsDone(IDocumentEvent documentEvent)
			 * { super.onSaveAsDone(documentEvent); waitForSaveComplete=false; }
			 * 
			 * @Override public void onSaveDone(IDocumentEvent documentEvent) {
			 * super.onSaveDone(documentEvent); waitForSaveComplete=false; }
			 * 
			 * });
			 */
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
		// loadTemplates();
		return container;
	}

	@Override
	public boolean close() {
		if (textDocument1 != null && textDocument1.isOpen()) {
			try {
				new Thread() {
					public void run() {
						textDocument1.close();

					}
				}.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return super.close();
	}

	private void createActions() {
	}

	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager(
				Messages.getString("designerWindow.menu")); //$NON-NLS-1$
		return menuManager;
	}

	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);

		toolBarManager.add(new Separator());
		return toolBarManager;
	}

	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		statusLineManager.setMessage(null, ""); //$NON-NLS-1$
		return statusLineManager;
	}

	public static void main(String args[]) {
		try {
			designerWindow window = new designerWindow();
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
		newShell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {

			}
		});
	}

	protected Point getInitialSize() {
		return new Point(500, 571);
	}

}
