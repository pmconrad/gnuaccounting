package GUILayer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Vector;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import uk.co.mmscomputing.device.scanner.Scanner;
import uk.co.mmscomputing.device.scanner.ScannerIOException;
import uk.co.mmscomputing.device.scanner.ScannerIOMetadata;
import uk.co.mmscomputing.device.scanner.ScannerListener;
import appLayer.application;
import appLayer.client;
import appLayer.configs;
import appLayer.document;

public class documentsWindow extends ApplicationWindow {

	private Table table_1;
	Button btnDelete;

	private class documentsList {
		public documentsList() {
		}
	}

	private class documentCellModifier implements ICellModifier {

		public boolean canModify(Object arg0, String arg1) {
			document doc = (document) arg0;
			return doc.getChangeValueForColumnName(arg1) != null;
		}

		public Object getValue(Object arg0, String arg1) {
			document doc = (document) arg0;
			return doc.getChangeValueForColumnName(arg1);
		}

		public void modify(Object arg0, String arg1, Object arg2) {
			TableItem ti = (TableItem) arg0;
			document doc = (document) ti.getData();
			doc.setChangedValueForColumnName(arg1, arg2);
			tableViewer.refresh();
		}

	}

	/**
	 * MAIN CLASS
	 * **/

	private Scanner scanner = null;
	private TableViewer tableViewer;
	private Vector<document> deleteVector = new Vector<document>();

	/**
	 * Create the application window
	 */
	public documentsWindow() {
		super(null);
		createActions();

		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	private void addToDeleteQueue(document doc) {
		deleteVector.add(doc);
		btnDelete.setText(Integer.toString(deleteVector.size()));
		client.getDocuments().remove(doc);
		tableViewer.refresh();
	}

	/**
	 * Create contents of the application window
	 * 
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		parent.getShell().setSize(getInitialSize());
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FormLayout());

		final Button btnFiles = new Button(container, SWT.NONE);
		FormData fd_btnFiles = new FormData();
		fd_btnFiles.right = new FormAttachment(100, -110);
		fd_btnFiles.top = new FormAttachment(0, 10);
		fd_btnFiles.left = new FormAttachment(100, -220);
		btnFiles.setLayoutData(fd_btnFiles);
		btnFiles.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN
						| SWT.MULTI);
				String errorResult = ""; //$NON-NLS-1$
				String allowedExtensions[] = {
						"*.pdf", "*.jpg", "*.jpeg", "*.png" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				dialog.setFilterExtensions(allowedExtensions);
				String name = dialog.open();
				if (name != null) {
					String[] files = dialog.getFileNames();

					for (String file : files) {
						StringBuffer buf = new StringBuffer();
						buf.append(dialog.getFilterPath());
						if (buf.charAt(buf.length() - 1) != File.separatorChar) {
							buf.append(File.separatorChar);
						}
						buf.append(file);
						document d = client.getDocuments().getNewDocument(
								buf.toString());
						d.setNumber(""); //$NON-NLS-1$

						String recognizedNumber = client.getDocuments()
								.recognizeDocumentNumberInString(file);
						if (recognizedNumber != null) {
							d.setNumber(recognizedNumber);
						}

						String fileError = d.copyOver();
						if (fileError != null) {
							errorResult += fileError + "\n"; //$NON-NLS-1$
						} else {
							d.save();
						}
					}
				}
				tableViewer.refresh();
				if (errorResult.length() > 0) {
					
					MessageDialog.openError(
							getShell(),
							Messages.getString("documentsWindow.ImportErrorDialogHeading"), errorResult); //$NON-NLS-1$
				}
			}
		});
		btnFiles.setText(Messages.getString("documentsWindow.importButton")); //$NON-NLS-1$
		btnFiles.setFont(configs.getDefaultFont());
		final Button btnScan = new Button(container, SWT.NONE);
		FormData fd_btnScan = new FormData();
		fd_btnScan.right = new FormAttachment(100, -10);
		fd_btnScan.top = new FormAttachment(0, 10);
		fd_btnScan.left = new FormAttachment(100, -100);
		btnScan.setLayoutData(fd_btnScan);
		btnScan.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent arg0) {
				try {

					scanner.acquire();

				} catch (ScannerIOException ex) {
					ex.printStackTrace();
				}

			}
		});
		btnScan.setText(Messages.getString("documentsWindow.ScanButtonCaption")); //$NON-NLS-1$
		btnScan.setFont(configs.getDefaultFont());

		client.getDocuments().getDocumentsFromDatabase();
		documentLabelProvider dlp = new documentLabelProvider(getShell()
				.getDisplay());

		final ScrolledComposite scrolledComposite = new ScrolledComposite(
				container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		FormData fd_scrolledComposite = new FormData();
		fd_scrolledComposite.bottom = new FormAttachment(100, -70);
		fd_scrolledComposite.right = new FormAttachment(100, -10);
		fd_scrolledComposite.top = new FormAttachment(0, 45);
		fd_scrolledComposite.left = new FormAttachment(0);
		scrolledComposite.setLayoutData(fd_scrolledComposite);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);

		tableViewer = new TableViewer(scrolledComposite, SWT.FULL_SELECTION
				| SWT.MULTI | SWT.BORDER);
		// tableViewer.setSize(220, 136);
		table_1 = tableViewer.getTable();
		table_1.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				Rectangle clientArea = table_1.getClientArea();
				Point pt = new Point(event.x, event.y);
				int index = table_1.getTopIndex();
				while (index < table_1.getItemCount()) {
					boolean visible = false;
					TableItem item = table_1.getItem(index);
					for (int i = 0; i < table_1.getColumnCount(); i++) {
						Rectangle rect = item.getBounds(i);
						if ((rect.contains(pt)) && (i == 5)) { // only react on
																// click on
																// action column
																// (#5)
							document d = (document) item.getData();
							if (d != null) {
								if ((d.getOriginalFilename() != null)
										&& (d.getOriginalFilename().length() > 0)) {
									viewerWindow vw = new viewerWindow(
											"file:///" + d.getOriginalFilename()); //$NON-NLS-1$
									vw.open();
								} else {
									documentViewerWindow dvw = new documentViewerWindow();
									dvw.open();
									dvw.loadImage(d.getImportFilename());

								}
							}
						}
						if (!visible && rect.intersects(clientArea)) {
							visible = true;
						}
					}
					if (!visible)
						return;
					index++;
				}
			}
		});

		CellEditor[] editors = new CellEditor[6];
		editors[0] = new TextCellEditor(table_1);
		editors[1] = new DateCellEditor(table_1);
		editors[2] = new TextCellEditor(table_1);
		editors[3] = new TextCellEditor(table_1);
		editors[4] = new TextCellEditor(table_1);
		editors[5] = new TextCellEditor(table_1);

		tableViewer.setCellEditors(editors);

		tableViewer.setColumnProperties(dlp.getColumnNames());

		tableViewer.setCellModifier(new documentCellModifier());
		tableViewer.setContentProvider(client.getDocuments());
		tableViewer.setLabelProvider(dlp);

		scrolledComposite.setContent(table_1);
		tableViewer.setCellModifier(new documentCellModifier());
		table_1.setLinesVisible(true);
		table_1.setHeaderVisible(true);

		final TableColumn newColumnTableColumn = new TableColumn(table_1,
				SWT.NONE);
		newColumnTableColumn.setWidth(100);
		//		newColumnTableColumn.setText(Messages.getString("documentsWindow.colHeadingDocument")); //$NON-NLS-1$
		newColumnTableColumn.setText(documentLabelProvider.getCaption(0)); //$NON-NLS-1$

		final TableColumn newColumnTableColumn_1 = new TableColumn(table_1,
				SWT.NONE);
		newColumnTableColumn_1.setWidth(100);
		//		newColumnTableColumn_1.setText(Messages.getString("documentsWindow.colHeadingDate")); //$NON-NLS-1$
		newColumnTableColumn_1.setText(documentLabelProvider.getCaption(1)); //$NON-NLS-1$

		final TableColumn newColumnTableColumn_2 = new TableColumn(table_1,
				SWT.NONE);
		newColumnTableColumn_2.setWidth(100);
		//		newColumnTableColumn_2.setText(Messages.getString("documentsWindow.colHeadingNumber")); //$NON-NLS-1$
		newColumnTableColumn_2.setText(documentLabelProvider.getCaption(2)); //$NON-NLS-1$

		final TableColumn newColumnTableColumn_3 = new TableColumn(table_1,
				SWT.NONE);
		newColumnTableColumn_3.setWidth(100);
		//		newColumnTableColumn_3.setText(Messages.getString("documentsWindow.colHeadingSubject")); //$NON-NLS-1$
		newColumnTableColumn_3.setText(documentLabelProvider.getCaption(3)); //$NON-NLS-1$

		final TableColumn newColumnTableColumn_4 = new TableColumn(table_1,
				SWT.NONE);
		newColumnTableColumn_4.setWidth(100);
		//		newColumnTableColumn_4.setText(Messages.getString("documentsWindow.colHeadingValue")); //$NON-NLS-1$
		newColumnTableColumn_4.setText(documentLabelProvider.getCaption(4)); //$NON-NLS-1$
		final TableColumn newColumnTableColumn_5 = new TableColumn(table_1,
				SWT.NONE);
		newColumnTableColumn_5.setWidth(100);
		newColumnTableColumn_5.setText(documentLabelProvider.getCaption(5)); //$NON-NLS-1$
		tableViewer.setInput(new documentsList());

		final Button btnAdd = new Button(container, SWT.NONE);
		FormData fd_btnAdd = new FormData();
		fd_btnAdd.right = new FormAttachment(100, -230);
		fd_btnAdd.top = new FormAttachment(0, 10);
		fd_btnAdd.left = new FormAttachment(100, -300);
		btnAdd.setLayoutData(fd_btnAdd);
		btnAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent arg0) {
				document d = client.getDocuments().getNewDocument(""); //$NON-NLS-1$
				d.save();
				tableViewer.refresh();

			}
		});
		btnAdd.setText(Messages.getString("documentsWindow.addButtonCaption")); //$NON-NLS-1$
		btnAdd.setFont(configs.getDefaultFont());

		final DragSource tableDragSource = new DragSource(table_1,
				DND.DROP_MOVE);
		tableDragSource.addDragListener(new DragSourceAdapter() {
			public void dragSetData(final DragSourceEvent event) {
				TableItem[] ti = table_1.getSelection();
				String data = new String();
				for (int i = 0; i < table_1.getSelectionCount(); i++) {
					document currentDocument = (document) ti[i].getData();
					data += "document://" + currentDocument.getID() + ","; //$NON-NLS-1$ //$NON-NLS-2$

				}
				event.data = data;

			}

			public void dragStart(final DragSourceEvent event) {
				if (table_1.getSelectionCount() > 0) {
					event.doit = true;
				}

			}
		});
		tableDragSource
				.setTransfer(new Transfer[] { TextTransfer.getInstance() });

		final Group tableItemDropGroup = new Group(container, SWT.NONE);
		FormData fd_tableItemDropGroup = new FormData();
		fd_tableItemDropGroup.bottom = new FormAttachment(100, -5);
		fd_tableItemDropGroup.right = new FormAttachment(100, -10);
		fd_tableItemDropGroup.top = new FormAttachment(100, -60);
		fd_tableItemDropGroup.left = new FormAttachment(100, -170);
		tableItemDropGroup.setLayoutData(fd_tableItemDropGroup);
		tableItemDropGroup.setBackground(new Color(getShell().getDisplay(),
				191, 191, 191));

		tableItemDropGroup.setText(Messages
				.getString("documentsWindow.dropzone")); //$NON-NLS-1$
		tableItemDropGroup.setFont(configs.getDefaultFont());

		btnDelete = new Button(tableItemDropGroup, SWT.NONE);
		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) tableViewer
						.getSelection();
				for (Object currentObject : selection.toArray()) {
					document currentDocument = (document) currentObject;
					addToDeleteQueue(currentDocument);
				}

			}

		});
		btnDelete.setBounds(35, 24, 89, 26);
		btnDelete.setImage(new Image(getShell().getDisplay(), getClass()
				.getResourceAsStream(
						"/libs/sarxos_Simple_Folder_Documents_small.png"))); //$NON-NLS-1$
		btnDelete.setText(Messages.getString("documentsWindow.deleteButton")); //$NON-NLS-1$
		btnDelete.setFont(configs.getDefaultFont());

		final DropTarget btnDeleteDropTarget = new DropTarget(btnDelete,
				DND.DROP_MOVE);
		btnDeleteDropTarget.setTransfer(new Transfer[] { TextTransfer
				.getInstance() });
		btnDeleteDropTarget.addDropListener(new DropTargetAdapter() {
			public void drop(final DropTargetEvent event) {

				String theEntries = (String) event.data;
				String[] elems = theEntries.split(","); //$NON-NLS-1$
				for (String elem : elems) {
					String prefix = "document://"; //$NON-NLS-1$
					int prefixLen = prefix.length();
					int documentID = Integer.valueOf(elem.substring(prefixLen));
					document doc = client.getDocuments().getDocumentForID(
							documentID);
					addToDeleteQueue(doc);
				}
				tableViewer.getTable().removeAll();
				tableViewer.refresh();
			}

		});

		try {
			scanner = Scanner.getDevice();
		} catch (UnsatisfiedLinkError e1) {
			// no scan support installed,e.g. Linux w/o sane
		}

		String scannerName = configs.getScannerName();

		if (scanner != null) {// if at least one scanner, more precisely SANE or
								// TWAIN driver is installed
			if ((scannerName != null) && (scannerName.length() > 0)) {
				try {
					scanner.select(scannerName);
				} catch (ScannerIOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			scanner.addListener(new ScannerListener() {
				public void update(ScannerIOMetadata.Type type,
						ScannerIOMetadata metadata) {
					if (ScannerIOMetadata.ACQUIRED.equals(type)) {
						/*
						 * make reference copy here to avoid race condition --
						 * as seen in uk.co.mmscomputing.application.imageviewer
						 */
						final BufferedImage scannedImage = metadata.getImage();
						/*
						 * obviously we need a separate thread to save the files
						 * (also stolen from imageviewer application, also
						 * available on
						 * http://www.mms-computing.co.uk/uk/co/mmscomputing
						 * /application/imageviewer/index.php) otherwise when
						 * scanning from ADF(automated document feed) only the
						 * first page is saved and the rest is feeded and
						 * ejected w/o being scanned or transmitted.
						 */
						scanSavePicThread sspt = new scanSavePicThread(
								scannedImage, getParentShell(),
								new IscanProgress() {

									@Override
									public void onComplete(String filename,
											String docNr) {

										// create a document
										document scanned = client
												.getDocuments().getNewDocument(
														filename);
										scanned.setNumber(docNr);
										scanned.save();

										tableViewer.getTable().removeAll();
										tableViewer.refresh();

									}
								});
						/*
						 * as we want to access the gui while the thread is
						 * running (to update the list of documents) we start
						 * the thread not via sspt.run but via
						 * getShell().getDisplay().syncExec(sspt); synchronously
						 * to the main GUI thread
						 */
						getShell().getDisplay().asyncExec(sspt);
					}
				}
			});
		} else {
			// no scanner found
		}

		//
		return container;
	}

	/**
	 * Create the actions
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Create the menu manager
	 * 
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager("menu"); //$NON-NLS-1$
		return menuManager;
	}

	/**
	 * Create the toolbar manager
	 * 
	 * @return the toolbar manager
	 */
	@Override
	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);
		return toolBarManager;
	}

	/**
	 * Create the status line manager
	 * 
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		statusLineManager.setMessage(null, ""); //$NON-NLS-1$
		return statusLineManager;
	}

	/**
	 * Launch the application
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			documentsWindow window = new documentsWindow();
			window.setBlockOnOpen(true);
			window.open();
			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the shell
	 * 
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText(application.getAppName());
	}

	/**
	 * Return the initial size of the window
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(621, 537);
	}

	@Override
	public boolean close() {
		for (document toBeDeleted : deleteVector) {
			toBeDeleted.delete();
		}
		return super.close();
	}
}
