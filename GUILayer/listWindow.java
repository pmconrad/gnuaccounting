package GUILayer;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import appLayer.application;
import appLayer.client;
import appLayer.configs;
import appLayer.entry;
import appLayer.utils;
import appLayer.transactionRelated.appTransaction;
import appLayer.transactionRelated.cancelation;

public class listWindow extends ApplicationWindow {

	class ViewContentProvider implements ITreeContentProvider {
		private String searchString = new String();
		private Date dateAfter = null;
		private Date dateBefore = null;
		private int totalNumMatches = 0;
		private BigDecimal totalNet = new BigDecimal(0);
		private BigDecimal totalGross = new BigDecimal(0);

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			// f return (File[]) inputElement;
			Vector<appTransaction> appTrans = (Vector<appTransaction>) inputElement;
			Vector<appTransaction> filteredTrans = new Vector<appTransaction>();
			totalNumMatches = 0;
			totalGross = new BigDecimal(0);
			totalNet = new BigDecimal(0);
			for (appTransaction currentTransaction : appTrans) {
				boolean entryMatches = true;

				if (searchString.length() > 0) {
					entryMatches = false;
					String transContact = ""; //$NON-NLS-1$
					if (currentTransaction.getContact() != null) {
						transContact = currentTransaction.getContact()
								.getName();
					}
					if ((transContact.startsWith(searchString))
							|| (currentTransaction.getNumber()
									.startsWith(searchString))) {
						entryMatches = true;
					}
				}
				if ((entryMatches) && (dateAfter != null)) {
					entryMatches = false;
					if (currentTransaction.getIssueDate().after(dateAfter)) {
						entryMatches = true;
					}

				}
				if ((entryMatches) && (dateBefore != null)) {
					entryMatches = false;
					if (currentTransaction.getIssueDate().before(dateBefore)) {
						entryMatches = true;
					}

				}

				if (entryMatches) {
					totalNumMatches++;
					totalGross = totalGross.add(currentTransaction
							.getTotalGross());
					totalNet = totalNet.add(currentTransaction.getTotal());
					filteredTrans.add(currentTransaction);
				}
			}
			if (lblSummary!=null) {
				lblSummary.setText(String.format(Messages.getString("listWindow.footerSummary"), contentprovider.getTotalNumMatches(), utils.currencyFormat(contentprovider.getTotalNet()), utils.currencyFormat(contentprovider.getTotalGross())));  //$NON-NLS-1$
			}

			return filteredTrans.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			// f File file = (File) parentElement;
			// f return file.listFiles();
			if (parentElement instanceof appTransaction) {
				return ((appTransaction) parentElement).getEntries().toArray();
			}
			Object[] o = new Object[0];
			return o;
		}

		@Override
		public Object getParent(Object element) {
			// f return ((File) element).getParentFile();
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			// f File file = (File) element;
			// f if (file.isDirectory()) {
			// f return true;
			// f }
			if (element instanceof appTransaction) {
				return !((appTransaction) element).getEntries().isEmpty();
			}
			return false;// entries would not have other children
		}

		public void setSearch(String text) {
			searchString = text;
		}

		public void setDateAfter(Date after) {
			dateAfter = after;
		}

		public void setDateBefore(Date before) {
			dateBefore = before;
		}

		public int getTotalNumMatches() {
			return totalNumMatches;
		}

		public BigDecimal getTotalNet() {
			return totalNet;
		}

		public BigDecimal getTotalGross() {
			return totalGross;
		}

	}

	class ViewLabelProvider extends StyledCellLabelProvider {

		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			switch (cell.getVisualIndex()) {

			case clmDateIndex:
				if (element instanceof appTransaction) {
					appTransaction a = (appTransaction) element;
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
					cell.setText(sdf.format(a.getIssueDate()));
				} else {
					entry e = (entry) element;
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
					cell.setText(sdf.format(e.getDate()));
				}

				break;
			case clmTypeIndex:
				if (element instanceof appTransaction) {

					appTransaction a = (appTransaction) element;
					cell.setText(a.getTransactionName());
				} else { // entry
					entry e = (entry) element;
				}

				break;
			case clmContactIndex:
				if (element instanceof appTransaction) {

					appTransaction a = (appTransaction) element;
					if (a.getContact() != null) {
						cell.setText(a.getContact().getName());
					} else {
						cell.setText(""); //$NON-NLS-1$
					}
				} else { // entry
					entry e = (entry) element;
					cell.setText(e.getDebitAccount().toString());
				}

				break;
			case clmNumberIndex:
				if (element instanceof appTransaction) {
					appTransaction a = (appTransaction) element;
					cell.setText(a.getNumber());

				} else {
					entry e = (entry) element;
					cell.setText(e.getCreditAccount().toString());
				}

				break;
			case clmValueIndex:
				if (element instanceof appTransaction) {
					appTransaction a = (appTransaction) element;
					cell.setText(a.getTotalGrossString());

				} else {
					entry e = (entry) element;
					cell.setText(utils.currencyFormat(e.getValue()));
				}

				break;
			case clmActionIndex:
				if (element instanceof appTransaction) {
					appTransaction a = (appTransaction) element;

					cell.setText(Messages
							.getString("listWindow.cancellationLinkText")); //$NON-NLS-1$

					Styler style = new Styler() {
						public void applyStyles(TextStyle textStyle) {
							textStyle.foreground = Display.getCurrent()
									.getSystemColor(SWT.COLOR_BLUE);
							textStyle.underline = true;
						}
					};
					StyledString styledString = new StyledString(
							cell.getText(), style);
					cell.setStyleRanges(styledString.getStyleRanges());

					/*
					 * 
					 * StyledString text = new StyledString(); StyleRange
					 * myStyledRange = new StyleRange(0, 6, null, Display
					 * .getCurrent().getSystemColor(SWT.COLOR_BLUE));
					 * 
					 * text.append("action", StyledString.DECORATIONS_STYLER);
					 * 
					 * StyleRange[] range = { myStyledRange };
					 * cell.setStyleRanges(range);
					 * cell.setText(text.toString());
					 */
					// super.update(cell);

				} else {
					entry e = (entry) element;
					cell.setText(""); //$NON-NLS-1$
				}

				break;
			default:
				cell.setText(Messages.getString("listWindow.unknownColError")); //$NON-NLS-1$

			}

		}

		public Image getImage(Object obj) {
			return null;
			// return PlatformUI.getWorkbench().getSharedImages()
			// .getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
		/*
		 * 
		 * @Override public String getText(Object element) { if (element
		 * instanceof appTransaction) { appTransaction a = (appTransaction)
		 * element; return a.getNumber(); } else { entry e = (entry) element;
		 * SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		 * //$NON-NLS-1$ return sdf.format(e.getDate()); }
		 * 
		 * }
		 * 
		 * @Override public Image getColumnImage(Object arg0, int arg1) { //
		 * TODO Auto-generated method stub return null; }
		 * 
		 * @Override public String getColumnText(Object arg0, int arg1) { //
		 * TODO Auto-generated method stub SimpleDateFormat sdf = new
		 * SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$ if (arg0 instanceof
		 * appTransaction) {
		 * 
		 * appTransaction a = (appTransaction) arg0; if (arg1 == 0) { // f
		 * return ((File)arg0).getName(); return sdf.format(a.getIssueDate()); }
		 * else if (arg1 == 1) { return a.getTransactionName();
		 * 
		 * } else if (arg1 == 2) { return a.getTotalGrossString();
		 * 
		 * } else if (arg1 == 3) { return a.getNumber();
		 * 
		 * } else if (arg1 == 4) { return "";
		 * 
		 * } } else { // columns for entries entry e = (entry) arg0;
		 * 
		 * if (arg1 == 0) { // f return ((File)arg0).getName(); return
		 * e.getValue().toString(); } else if (arg1 == 1) { return
		 * e.getCreditAccount().toString();
		 * 
		 * } else if (arg1 == 2) { return e.getDebitAccount().toString();
		 * 
		 * } else if (arg1 == 3) { return ""; //$NON-NLS-1$
		 * 
		 * } else if (arg1 == 4) { return ""; //$NON-NLS-1$
		 * 
		 * }
		 * 
		 * } return Messages.getString("listWindow.errUnknownCol") + arg1;
		 * //$NON-NLS-1$ }
		 */

	}

	private class treeSorter extends ViewerSorter {
		private int propertyIndex;
		// private static final int ASCENDING = 0;
		private static final int DESCENDING = 1;

		private int direction = DESCENDING;

		public treeSorter() {
			this.propertyIndex = 0;
			direction = DESCENDING;
		}

		public void setColumn(int column) {
			if (column == this.propertyIndex) {
				// Same column as last sort; toggle the direction
				direction = 1 - direction;
			} else {
				// New column; do an ascending sort
				this.propertyIndex = column;
				direction = DESCENDING;
			}
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof entry) {
				// ignore sub-entries. In this case both e1 and e2 would be an
				// entry
				return 0;
			}
			appTransaction en1 = (appTransaction) e1;
			appTransaction en2 = (appTransaction) e2;
			int rc = 0;
			switch (propertyIndex) {
			case clmDateIndex:// issue date
				rc = en1.getIssueDate().compareTo(en2.getIssueDate());
				break;
			case clmTypeIndex:// type
				rc = en1.getTypeName().compareTo(en2.getTypeName());
				break;
			case clmContactIndex:// customer
				if ((en1.getContact() == null) && (en2.getContact() != null)) {
					rc = -1;
				} else if ((en1.getContact() != null)
						&& (en2.getContact() == null)) {
					rc = +1;
				} else if ((en1.getContact() == null)
						&& (en2.getContact() == null)) {
					rc = 0;
				} else {
					rc = en1.getContact().getName()
							.compareTo(en2.getContact().getName());
				}
				break;
			case clmNumberIndex:// nr
				rc = en1.getNumber().compareTo(en2.getNumber());
				break;
			case clmValueIndex:// amount
				rc = en1.getTotalGross().compareTo(en2.getTotalGross());
				break;
			default:
				rc = 0;
			}
			// If descending order, flip the direction
			if (direction == DESCENDING) {
				rc = -rc;
			}
			return rc;
		}

	}

	// main class

	protected static final int clmDateIndex = 0;
	protected static final int clmTypeIndex = 1;
	protected static final int clmContactIndex = 2;
	protected static final int clmNumberIndex = 3;
	protected static final int clmValueIndex = 4;
	protected static final int clmActionIndex = 5;

	TreeViewer treeViewer = null;
	ViewContentProvider contentprovider = null;

	Button btnCheckAfter = null;
	DateTime dateTimeBefore = null;
	Button btnCheckBefore = null;
	DateTime dateTimeAfter = null;
	Label lblSummary = null;
	
	/**
	 * Create the application window.
	 */
	public listWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	/**
	 * Create contents of the application window.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);

		GridLayoutFactory.swtDefaults().numColumns(2).margins(10, 5)
				.applyTo(container);

		Button btnCollapse = new Button(container, SWT.NONE);
		btnCollapse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				treeViewer.collapseAll();
			}
		});
		btnCollapse.setFont(configs.getDefaultFont());
		btnCollapse.setText(Messages.getString("listWindow.buttonCollapse")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 30).grab(true, false)
				.applyTo(btnCollapse);

		Button btnExpand = new Button(container, SWT.NONE);
		btnExpand.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				treeViewer.expandAll();
			}
		});
		btnExpand.setFont(configs.getDefaultFont());
		btnExpand.setText(Messages.getString("listWindow.buttonExpand")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 30).grab(true, false)
				.applyTo(btnExpand);

		Label lblSearch = new Label(container, SWT.NONE);
		lblSearch.setFont(configs.getDefaultFont());
		lblSearch.setText(Messages.getString("listWindow.search")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
				.applyTo(lblSearch);

		final Text txtSearch = new Text(container, SWT.BORDER);
		txtSearch.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				contentprovider.setSearch(txtSearch.getText());
				treeViewer.refresh();

			}
		});
		txtSearch.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
				.applyTo(txtSearch);

		btnCheckAfter = new Button(container, SWT.CHECK);
		btnCheckAfter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				triggerDateFilters();
			}
		});
		btnCheckAfter.setText(Messages.getString("listWindow.entriesAfter")); //$NON-NLS-1$
		btnCheckAfter.setFont(configs.getDefaultFont());
		GridDataFactory.swtDefaults().applyTo(btnCheckAfter);

		dateTimeAfter = new DateTime(container, SWT.BORDER);
		dateTimeAfter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				triggerDateFilters();
			}
		});
		GridDataFactory.fillDefaults().hint(200, 30).grab(true, false)
				.applyTo(dateTimeAfter);
		dateTimeAfter.setFont(configs.getDefaultFont());

		btnCheckBefore = new Button(container, SWT.CHECK);
		btnCheckBefore.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				triggerDateFilters();
			}
		});
		btnCheckBefore.setText(Messages.getString("listWindow.entriesBefore")); //$NON-NLS-1$
		btnCheckBefore.setFont(configs.getDefaultFont());
		GridDataFactory.swtDefaults().applyTo(btnCheckBefore);

		dateTimeBefore = new DateTime(container, SWT.BORDER);
		dateTimeBefore.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				triggerDateFilters();
			}
		});
		GridDataFactory.fillDefaults().hint(200, 30).grab(true, false)
				.applyTo(dateTimeBefore);
		dateTimeBefore.setFont(configs.getDefaultFont());

		triggerDateFilters();

		treeViewer = new TreeViewer(container, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);

		final treeSorter treeSort = new treeSorter();
		treeViewer.setSorter(treeSort);

		Tree tree = treeViewer.getTree();
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent event) {
				/*
				 * IStructuredSelection selection = (IStructuredSelection)
				 * treeViewer .getSelection(); appTransaction selectedTrans =
				 * (appTransaction) selection.getFirstElement();
				 */

				TreeItem item = treeViewer.getTree().getSelection()[0];
				Point pt = new Point(event.x, event.y);
				if (item != null) {
					for (int i = 0; i < treeViewer.getTree().getColumnCount(); i++) {
						// this is the column that was clicked
						final int column = i;
						Rectangle rect = item.getBounds(i);
						if (rect.contains(pt)) {
							if (column == clmActionIndex) {
								IStructuredSelection selection = (IStructuredSelection) treeViewer
										.getSelection();

								if (selection.getFirstElement() instanceof appTransaction) {
									appTransaction selectedTrans = (appTransaction) selection
											.getFirstElement();
									// clicked on "action"
									newTransactionWizard wizard = new newTransactionWizard();
									wizard.setShowReferencedTypes(true);
									client.getTransactions()
											.setAsCurrentTransaction(
													selectedTrans.getID());

									client.getTransactions()
											.setTransactionListIndex(
													client.getTransactions()
															.getInstanceIndexForTypeID(
																	cancelation
																			.getType())); //$NON-NLS-1$
									appTransaction trans = client
											.getTransactions()
											.getCurrentTransaction();
									trans.setReferTo(selectedTrans.getID());
									String number = trans.getNumber();
									String dateIssue = trans.getIssueDate()
											.toString();
									String dateDue = trans.getDueDate()
											.toString();
									trans.setRemarks(Messages
											.getString("todoWindow.number") + number + Messages.getString("todoWindow.XOfY") + dateIssue + Messages.getString("todoWindow.dueOn") + dateDue); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
									trans.getNewTransactionNumber();

									WizardDialog dialog = new WizardDialog(
											getShell(), wizard);
									dialog.open();

								}

							}
							// Edit code

						}
					}
				}
			}
		});

		GridDataFactory.fillDefaults().hint(200, 200).span(2, 1)
				.grab(true, true).applyTo(tree);

		final TreeColumn trclmnDate = new TreeColumn(treeViewer.getTree(),
				SWT.CENTER);
		trclmnDate.setText(Messages.getString("listWindow.headingDate")); //$NON-NLS-1$
		trclmnDate.setResizable(true);
		trclmnDate.setWidth(200);

		trclmnDate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				treeSort.setColumn(clmDateIndex);
				int dir = treeViewer.getTree().getSortDirection();
				if (treeViewer.getTree().getSortColumn() == trclmnDate) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {

					dir = SWT.DOWN;
				}
				treeViewer.getTree().setSortDirection(dir);
				treeViewer.getTree().setSortColumn(trclmnDate);
				treeViewer.refresh();
			}
		});

		final TreeColumn trclmnType = new TreeColumn(tree, SWT.NONE);
		trclmnType.setWidth(100);
		trclmnType.setText(Messages.getString("listWindow.headingType")); //$NON-NLS-1$

		trclmnType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				treeSort.setColumn(clmTypeIndex);
				int dir = treeViewer.getTree().getSortDirection();
				if (treeViewer.getTree().getSortColumn() == trclmnType) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {

					dir = SWT.DOWN;
				}
				treeViewer.getTree().setSortDirection(dir);
				treeViewer.getTree().setSortColumn(trclmnType);
				treeViewer.refresh();
			}
		});

		final TreeColumn trclmnContact = new TreeColumn(tree, SWT.NONE);
		trclmnContact.setWidth(100);
		trclmnContact.setText(Messages.getString("listWindow.ContactHeadline")); //$NON-NLS-1$

		trclmnContact.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				treeSort.setColumn(clmContactIndex);
				int dir = treeViewer.getTree().getSortDirection();
				if (treeViewer.getTree().getSortColumn() == trclmnContact) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {

					dir = SWT.DOWN;
				}
				treeViewer.getTree().setSortDirection(dir);
				treeViewer.getTree().setSortColumn(trclmnContact);
				treeViewer.refresh();
			}
		});

		final TreeColumn trclmnValue = new TreeColumn(tree, SWT.NONE);
		trclmnValue.setWidth(100);
		trclmnValue.setText(Messages.getString("listWindow.headingNumber")); //$NON-NLS-1$

		final TreeColumn trclmnNumber = new TreeColumn(tree, SWT.NONE);
		trclmnNumber.setWidth(100);
		trclmnNumber.setText(Messages.getString("listWindow.Value")); //$NON-NLS-1$
		trclmnNumber.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				treeSort.setColumn(clmNumberIndex);
				int dir = treeViewer.getTree().getSortDirection();
				if (treeViewer.getTree().getSortColumn() == trclmnNumber) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {

					dir = SWT.DOWN;
				}
				treeViewer.getTree().setSortDirection(dir);
				treeViewer.getTree().setSortColumn(trclmnNumber);
				treeViewer.refresh();
			}
		});

		trclmnValue.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				treeSort.setColumn(clmValueIndex);
				int dir = treeViewer.getTree().getSortDirection();
				if (treeViewer.getTree().getSortColumn() == trclmnValue) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {

					dir = SWT.DOWN;
				}
				treeViewer.getTree().setSortDirection(dir);
				treeViewer.getTree().setSortColumn(trclmnValue);
				treeViewer.refresh();
			}
		});

		final TreeColumn trclmnAction = new TreeColumn(tree, SWT.NONE);
		trclmnAction.setWidth(100);
		trclmnAction.setText(Messages
				.getString("listWindow.actionColumnHeading")); //$NON-NLS-1$
		contentprovider = new ViewContentProvider();
		treeViewer.getTree().setLinesVisible(true);
		treeViewer.getTree().setHeaderVisible(true);
		treeViewer.setContentProvider(contentprovider);
		treeViewer.setLabelProvider(new ViewLabelProvider());

		lblSummary = new Label(container, SWT.NONE);
		lblSummary.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).span(2, 1)
				.grab(true, false).applyTo(lblSummary);

		treeViewer.setInput(client.getTransactions().getTransactions());
		treeViewer.expandAll();
		
		return container;
	}

	private void triggerDateFilters() {

		dateTimeAfter.setEnabled(btnCheckAfter.getSelection());
		if (contentprovider != null) {
			if ((btnCheckAfter.getSelection())) {
				GregorianCalendar calStart = new GregorianCalendar();

				calStart.set(Calendar.YEAR, dateTimeAfter.getYear());
				calStart.set(Calendar.MONTH, dateTimeAfter.getMonth());
				calStart.set(Calendar.DAY_OF_MONTH, dateTimeAfter.getDay());

				contentprovider.setDateAfter(calStart.getTime());
			} else {
				contentprovider.setDateAfter(null);
			}
		}
		dateTimeBefore.setEnabled(btnCheckBefore.getSelection());
		if (contentprovider != null) {
			if (btnCheckBefore.getSelection()) {
				GregorianCalendar calEnd = new GregorianCalendar();

				calEnd.set(Calendar.YEAR, dateTimeBefore.getYear());
				calEnd.set(Calendar.MONTH, dateTimeBefore.getMonth());
				calEnd.set(Calendar.DAY_OF_MONTH, dateTimeBefore.getDay());

				contentprovider.setDateBefore(calEnd.getTime());
			} else {
				contentprovider.setDateBefore(null);
			}
		}
		if (treeViewer != null) {
			treeViewer.refresh();

		}

	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Create the menu manager.
	 * 
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager(
				Messages.getString("listWindow.menu")); //$NON-NLS-1$
		return menuManager;
	}

	/**
	 * Create the toolbar manager.
	 * 
	 * @return the toolbar manager
	 */
	@Override
	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);
		return toolBarManager;
	}

	/**
	 * Create the status line manager.
	 * 
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		return statusLineManager;
	}

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			listWindow window = new listWindow();
			window.setBlockOnOpen(true);
			window.open();
			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the shell.
	 * 
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(application.getAppName());
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(628, 479);
	}
}
