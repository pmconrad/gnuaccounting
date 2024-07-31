package GUILayer;

import java.math.BigDecimal;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import appLayer.application;
import appLayer.client;
import appLayer.configs;
import appLayer.utils;
import appLayer.taxRelated.tax;
import appLayer.taxRelated.taxList;

import org.eclipse.swt.widgets.Link;


public class taxesWindow extends ApplicationWindow {
	class ContentProvider implements IStructuredContentProvider {
		public ContentProvider() {
			super();
		}

		public Object[] getElements(Object inputElement) {
			return client.getTaxes().getVATs().toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class taxSelectionChangedListener implements ISelectionChangedListener {
		protected taxesWindow parent;

		public taxSelectionChangedListener(taxesWindow parent) {
			this.parent = parent;

		}

		public void selectionChanged(SelectionChangedEvent arg0) {
			parent.updateControlsFromSelection();

		}
	}
	/**
	 *  MAIN CLASS
	 */	

	// //////////////////////////////////////////////////////

	private Text txtFldDebitTax;
	private Text txtFldCreditTax;
	private Text txtFldValue;
	private Text txtFldDescription;
	private Button btnDelete = null;
	private Label lblIDValue = null;
	private taxList vats = new taxList();
	protected TableViewer tblViewerTax = null;
	private String unknownIDString = Messages
			.getString("taxesWindow.notYetAssigned0"); //$NON-NLS-1$

	public taxesWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	protected Control createContents(Composite parent) {
		parent.getShell().setSize(getInitialSize());

		Composite container = new Composite(parent, SWT.NONE);

		GridLayoutFactory.swtDefaults().numColumns(3).margins(10, 5)
		.applyTo(container);

		client.getAccounts().getAccountsFromDatabase();
		

		tblViewerTax = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		tblViewerTax
				.addSelectionChangedListener(new taxSelectionChangedListener(
						this));
		
		tblViewerTax.setContentProvider(ArrayContentProvider.getInstance());
		Table tblTax = tblViewerTax.getTable();
		tblTax.setHeaderVisible(true);
		tblTax.setFont(configs.getDefaultFont());
		
		GridDataFactory.fillDefaults().hint(200, 100).grab(true, true).span(3,1).applyTo(tblTax);
		
		TableViewerColumn tableViewerColumnStandard = new TableViewerColumn(tblViewerTax, SWT.NONE);
		tableViewerColumnStandard.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				tax currentTax=(tax) element;
				if (currentTax.isDefaultIncomingTax()) { 
					return Messages.getString("taxesWindow.isStandard"); //$NON-NLS-1$
				} else {
					return ""; //$NON-NLS-1$
				}
			  
			}

			}); 
		
		TableColumn tblclmnStandard = tableViewerColumnStandard.getColumn();
		tblclmnStandard.setWidth(25);
		tblclmnStandard.setText(Messages.getString("taxesWindow.standard")); //$NON-NLS-1$
		
		
		TableViewerColumn tableViewerColumnName = new TableViewerColumn(tblViewerTax, SWT.NONE);
		tableViewerColumnName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				tax currentTax=(tax) element;
			  return currentTax.getDescription();  
			}

			}); 
		TableColumn tblclmnName = tableViewerColumnName.getColumn();
		tblclmnName.setWidth(200);
		tblclmnName.setText(Messages.getString("taxesWindow.name")); //$NON-NLS-1$
	
		
		TableViewerColumn tableViewerColumnFactor = new TableViewerColumn(tblViewerTax, SWT.NONE);
		tableViewerColumnFactor.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				tax currentTax=(tax) element;
			  return utils.BD2String(currentTax.getValue().multiply(new BigDecimal(100)),'.');  
			}

			}); 
		TableColumn tblclmnFactor = tableViewerColumnFactor.getColumn();
		tblclmnFactor.setWidth(75);
		tblclmnFactor.setText(Messages.getString("taxesWindow.value"));  //$NON-NLS-1$

		// finally: fill the table
		tblViewerTax.setInput(client.getTaxes().getVATs());

		
		final Label idLabel = new Label(container, SWT.NONE);
		idLabel.setAlignment(SWT.RIGHT);
		idLabel.setFont(configs.getDefaultFont());
		idLabel.setText(Messages.getString("taxesWindow.id")); //$NON-NLS-1$
		GridDataFactory.swtDefaults().hint(200, 20).applyTo(idLabel);


		lblIDValue = new Label(container, SWT.NONE);
		lblIDValue.setFont(configs.getDefaultFont());
		lblIDValue.setText(unknownIDString);
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false).span(2,1).applyTo(lblIDValue);

		final Label descriptionLabel = new Label(container, SWT.NONE);
		descriptionLabel.setAlignment(SWT.RIGHT);
		descriptionLabel.setFont(configs.getDefaultFont());
		descriptionLabel.setText(Messages.getString("taxesWindow.description")); //$NON-NLS-1$
		GridDataFactory.swtDefaults().hint(200, 20).applyTo(descriptionLabel);


		txtFldDescription = new Text(container, SWT.BORDER);
		txtFldDescription.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().grab(true, false).hint(200, 20).applyTo(txtFldDescription);

		Composite blank1 = new Composite(container, SWT.NONE);
		GridDataFactory.swtDefaults().hint(1,1).applyTo(blank1);
		
		final Label valueLabel = new Label(container, SWT.NONE);
		valueLabel.setAlignment(SWT.RIGHT);
		valueLabel.setFont(configs.getDefaultFont());
		valueLabel.setText(Messages.getString("taxesWindow.value")); //$NON-NLS-1$
		GridDataFactory.swtDefaults().hint(200, 20).applyTo(valueLabel);
		
		txtFldValue = new Text(container, SWT.BORDER);
		txtFldValue.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().grab(true, false).hint(200, 20).applyTo(txtFldValue);

		final Label lblPercent = new Label(container, SWT.NONE);
		lblPercent.setText(Messages.getString("taxesWindow.lblPercent.text")); //$NON-NLS-1$ //$NON-NLS-1$
		GridDataFactory.fillDefaults().applyTo(lblPercent);

		
		
		
		Label lblCreditTaxField = new Label(container, SWT.NONE);
		lblCreditTaxField.setAlignment(SWT.RIGHT);

		lblCreditTaxField.setFont(configs.getDefaultFont());
		lblCreditTaxField.setText(Messages
				.getString("taxesWindow.lblCreditTaxField.text")); //$NON-NLS-1$
		GridDataFactory.swtDefaults().hint(200, 20).applyTo(lblCreditTaxField);


		
		txtFldCreditTax = new Text(container, SWT.BORDER);
		txtFldCreditTax.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false).applyTo(txtFldCreditTax);
		Composite blank2 = new Composite(container, SWT.NONE);
		GridDataFactory.swtDefaults().hint(1,1).applyTo(blank2);

		
		Label lblDebitTaxField = new Label(container, SWT.NONE);

		lblDebitTaxField.setFont(configs.getDefaultFont());

		lblDebitTaxField.setAlignment(SWT.RIGHT);
		lblDebitTaxField.setText(Messages
				.getString("taxesWindow.lblDebitTaxField.text")); //$NON-NLS-1$
		GridDataFactory.swtDefaults().hint(200, 20).applyTo(lblDebitTaxField);


		txtFldDebitTax = new Text(container, SWT.BORDER);
		txtFldDebitTax.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().grab(true, false).hint(200, 20).applyTo(txtFldDebitTax);
		
		Composite blank3 = new Composite(container, SWT.NONE);
		GridDataFactory.swtDefaults().hint(1,1).applyTo(blank3);

		Link linkSettings = new Link(container, SWT.NONE);
		linkSettings.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				settingsWindow sw=new settingsWindow();
				sw.open();
			}
		});
		linkSettings.setText(Messages.getString("taxesWindow.linkSettings.text")); //$NON-NLS-1$
		linkSettings.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().grab(true, false).hint(200, 30).span(2,1).applyTo(linkSettings);

		Composite blank4 = new Composite(container, SWT.NONE);
		GridDataFactory.swtDefaults().hint(1,1).applyTo(blank4);

		
		final Button btnOK = new Button(container, SWT.NONE);
		btnOK.setFont(configs.getDefaultFont());
		btnOK.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) tblViewerTax
						.getSelection();
				tax selectedTax = (tax) selection.getFirstElement();

				boolean newTaxCreated = false;
				if (selectedTax.getID() == tax.newTaxID) {
					selectedTax = tax.getNewTax();
					selectedTax.setParent(client.getTaxes());
					newTaxCreated = true;
				}
				int errorCount = 0;
				String vatValue = txtFldValue.getText();
				if (txtFldDescription.getText().equalsIgnoreCase("")) { //$NON-NLS-1$
					getStatusLineManager().setErrorMessage(
							Messages.getString("taxesWindow.VATEmptyDesc")); //$NON-NLS-1$
					errorCount++;
				}
				if (vatValue.equalsIgnoreCase("")) { //$NON-NLS-1$
					getStatusLineManager().setErrorMessage(
							Messages.getString("taxesWindow.VATEmptyValue")); //$NON-NLS-1$
					errorCount++;
				}
				
				if ((selectedTax==client.getTaxes().getEmpty())&&(!utils.String2BD(vatValue).equals(new BigDecimal(0)))) { //$NON-NLS-1$
					getStatusLineManager().setErrorMessage(
							Messages.getString("taxesWindow.emptyTax"));  //$NON-NLS-1$
					errorCount++;
				}

				if (errorCount == 0) {
					
					BigDecimal vatValueBD = utils.String2BD(vatValue);
					vatValueBD = vatValueBD.divide(new BigDecimal(100)); // percent
					vatValue = vatValueBD.toString();
					selectedTax.setValue(vatValueBD);

					String debitTaxField = "0"; //$NON-NLS-1$
					String creditTaxField = "0"; //$NON-NLS-1$
					if (txtFldCreditTax.getText().length() > 0) {
						creditTaxField = txtFldCreditTax.getText();
					}
					if (txtFldDebitTax.getText().length() > 0) {
						debitTaxField = txtFldDebitTax.getText();
					}
					selectedTax.setCreditTaxField(Integer.valueOf(creditTaxField));
					selectedTax.setDebitTaxField(Integer.valueOf(debitTaxField));
					selectedTax.setDescription(txtFldDescription.getText());
					
					selectedTax.save();
					getStatusLineManager().setErrorMessage(""); //$NON-NLS-1$
					tblViewerTax.refresh();
					todoWindow.refreshToDoList();
					if (newTaxCreated) {
						selection = new StructuredSelection(selectedTax);
						tblViewerTax.setSelection(selection);
					}

				}

			}
		});

		
		btnOK.setText(Messages.getString("taxesWindow.ok")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().grab(true, false).applyTo(btnOK);

		btnDelete = new Button(container, SWT.NONE);
		btnDelete.setFont(configs.getDefaultFont());
		btnDelete.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) tblViewerTax
						.getSelection();
				tax selectedTax = (tax) selection.getFirstElement();
				int errorCount = 0;

				if (client.getTaxes().getCount() <= 2) {
					// vat available
					getStatusLineManager().setErrorMessage(
							Messages.getString("taxesWindow.atleastoneneeded")); //$NON-NLS-1$
					errorCount++;
				}
				if (selectedTax.isDefaultIncomingTax()) {
					getStatusLineManager()
							.setErrorMessage(
									Messages.getString("taxesWindow.cantDeleteStandardVAT")); //$NON-NLS-1$
					errorCount++;
				}

				if (errorCount == 0) {
					selectedTax.delete();
					tblViewerTax.remove(selectedTax);
					tblViewerTax.refresh();
					tblViewerTax.getTable().select(0);
					getStatusLineManager().setErrorMessage(""); //$NON-NLS-1$
				}

			}
		});
		btnDelete.setEnabled(false);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(btnDelete);
		btnDelete.setText(Messages.getString("taxesWindow.delete")); //$NON-NLS-1$
		
		

		tblTax.select(0);

		updateControlsFromSelection();

		return container;
	}

	public void updateControlsFromSelection() {

		// Object[] objects=((ContentProvider)
		// listViewer.getContentProvider()).getElements(null);
		tax firstTax = vats.getFirst();
		tax emptyTax = vats.getEmpty();

		IStructuredSelection selection = (IStructuredSelection) tblViewerTax
				.getSelection();
		tax selectedTax = (tax) selection.getFirstElement();
		if (selection.isEmpty()) {
			// default select first element (new asset), e.g. if current element
			// wasd deleted
			selectedTax = firstTax;
		}
		

		if ((selectedTax == firstTax) || (selectedTax == emptyTax)) {
			btnDelete.setEnabled(false);
		} else {
			btnDelete.setEnabled(true);
		}

		if (selectedTax.getID() >= 0) {
			lblIDValue.setText(Integer.toString(selectedTax.getID()));
		} else {
			lblIDValue.setText(unknownIDString);
		}

		txtFldCreditTax.setText(Integer.toString(selectedTax
				.getCreditTaxField()));
		txtFldDebitTax
				.setText(Integer.toString(selectedTax.getDebitTaxField()));
		txtFldDescription.setText(selectedTax.getDescription());

		txtFldValue.setText(selectedTax.getValue().multiply(new BigDecimal(100)).toString());

	}

	private void createActions() {
	}

	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager(
				Messages.getString("taxesWindow.menu")); //$NON-NLS-1$
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

	public static void main(String args[]) {
		try {
			taxesWindow window = new taxesWindow();
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
	}

	protected Point getInitialSize() {
		return new Point(477, 500);
	}
}
