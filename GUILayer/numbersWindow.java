package GUILayer;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import appLayer.application;
import appLayer.client;
import appLayer.configs;
import appLayer.transactionRelated.transactionType;
import appLayer.transactionRelated.transactions;

public class numbersWindow extends ApplicationWindow {

	class ContentProvider implements IStructuredContentProvider {
		protected transactions allTransactions = client.getTransactions();

		public ContentProvider() {
			super();
		}

		public Object[] getElements(Object inputElement) {
			return allTransactions.getAllTypes();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class transactionSelectionChangedListener implements
			ISelectionChangedListener {
		protected numbersWindow parent;

		public transactionSelectionChangedListener(numbersWindow parent) {
			this.parent = parent;

		}

		public void selectionChanged(SelectionChangedEvent arg0) {
			parent.updateControlsFromSelection();

		}
	}

	private ListViewer listViewer = null;
	private Text txtFldPeriod;
	private Text txtFldFormat;
	private Text txtFldIndex;
	private Text txtFldPrefix;
	private Text txtFldName;
	private Label lblID = null;
	private String formerPrefix;

	public numbersWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	protected Control createContents(Composite parent) {
		parent.getShell().setSize(getInitialSize());
		Composite container = new Composite(parent, SWT.NONE);

		container.setFont(configs.getDefaultFont());
		GridLayoutFactory.swtDefaults().numColumns(2).margins(10, 5)
		.applyTo(container);


		listViewer = new ListViewer(container, SWT.V_SCROLL | SWT.BORDER);
		listViewer
				.addSelectionChangedListener(new transactionSelectionChangedListener(
						this));

		listViewer.setContentProvider(new ContentProvider());
		listViewer.setInput(new Object());

		List list = listViewer.getList();
		list.setFont(configs.getDefaultFont());
		list.select(0);
		
		GridDataFactory.fillDefaults().hint(200, 120).span(2,1).grab(true, true)
		.applyTo(list);
		
		
		
		final Label idLabel = new Label(container, SWT.RIGHT);
		idLabel.setFont(configs.getDefaultFont());
		idLabel.setText(Messages.getString("numbersWindow.id")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(idLabel);


		lblID = new Label(container, SWT.NONE);
		lblID.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(lblID);
		lblID.setText(Messages.getString("numbersWindow.label")); //$NON-NLS-1$
		
		
		final Label nameLabel = new Label(container, SWT.RIGHT);
		nameLabel.setFont(configs.getDefaultFont());
		nameLabel.setText(Messages.getString("numbersWindow.name")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(nameLabel);

		txtFldName = new Text(container, SWT.BORDER);
		txtFldName.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(txtFldName);

		
		final Label prefixLabel = new Label(container, SWT.RIGHT);
		prefixLabel.setFont(configs.getDefaultFont());
		prefixLabel.setText(Messages.getString("numbersWindow.prefix")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(prefixLabel);

		
		

		txtFldPrefix = new Text(container, SWT.BORDER);
		txtFldPrefix.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(txtFldPrefix);
		
		
		
		final Label indexLabel = new Label(container, SWT.RIGHT);
		indexLabel.setFont(configs.getDefaultFont());
		indexLabel.setText(Messages.getString("numbersWindow.index")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(indexLabel);

		txtFldIndex = new Text(container, SWT.BORDER);
		txtFldIndex.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(txtFldIndex);
		
		final Label formatLabel = new Label(container, SWT.RIGHT);
		formatLabel.setFont(configs.getDefaultFont());
		formatLabel.setText(Messages.getString("numbersWindow.format")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(formatLabel);


		txtFldFormat = new Text(container, SWT.BORDER);
		txtFldFormat.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(txtFldFormat);
		
		final Label periodLabel = new Label(container, SWT.RIGHT);
		periodLabel.setFont(configs.getDefaultFont());
		periodLabel.setText(Messages.getString("numbersWindow.period")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(periodLabel);

		txtFldPeriod = new Text(container, SWT.BORDER);
		txtFldPeriod.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(txtFldPeriod);

		
		

		final Button okButton = new Button(container, SWT.NONE);
		okButton.setFont(configs.getDefaultFont());
		okButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int errorCount = 0;
				if (txtFldName.getText().equalsIgnoreCase("")) { //$NON-NLS-1$
					getStatusLineManager().setErrorMessage(
							Messages.getString("numbersWindow.emptyName")); //$NON-NLS-1$
					errorCount++;
				}
				if (txtFldPrefix.getText().equalsIgnoreCase("")) { //$NON-NLS-1$
					getStatusLineManager().setErrorMessage(
							Messages.getString("numbersWindow.emptyPrefix")); //$NON-NLS-1$
					errorCount++;
				}
				if (txtFldIndex.getText().equalsIgnoreCase("")) { //$NON-NLS-1$
					getStatusLineManager().setErrorMessage(
							Messages.getString("numbersWindow.emptyIndex")); //$NON-NLS-1$
					errorCount++;
				}
				if (txtFldFormat.getText().equalsIgnoreCase("")) { //$NON-NLS-1$
					getStatusLineManager().setErrorMessage(
							Messages.getString("numbersWindow.emptyFormat")); //$NON-NLS-1$
					errorCount++;
				}
				if (txtFldPeriod.getText().equalsIgnoreCase("")) { //$NON-NLS-1$
					getStatusLineManager().setErrorMessage(
							Messages.getString("numbersWindow.emptyPeriod")); //$NON-NLS-1$
					errorCount++;
				}

				IStructuredSelection selection = (IStructuredSelection) listViewer
						.getSelection();
				transactionType selectedTransaction = (transactionType) selection
						.getFirstElement();
				if (selection.isEmpty()) {
					// default select first element (new asset), e.g. if current
					// element wasd deleted
					selectedTransaction = client.getTransactions()
							.getDefaultTransactionType();
				}

				if (errorCount == 0) {
					selectedTransaction.setTypeName(txtFldName.getText());
					if (!formerPrefix.equals(txtFldPrefix.getText())) {
						try {
							renameTemplate();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					// renameTemplate might cancel the operation so
					// setTypePrefix has to be called AFTER renameTemplate
					selectedTransaction.setTypePrefix(txtFldPrefix.getText());
					selectedTransaction.setTypeFormat(txtFldFormat.getText());
					selectedTransaction.setTypeIndex(Integer
							.valueOf(txtFldIndex.getText()));
					selectedTransaction.setTypePeriod(Integer
							.valueOf(txtFldPeriod.getText()));
					selectedTransaction.save();
					getStatusLineManager().setErrorMessage(""); //$NON-NLS-1$

					listViewer.refresh();
				}

			}

			/**
			 * when the prefix of a template is changed, this function will
			 * adjust (rename) the file name of the template file
			 * */
			private void renameTemplate() throws IOException {
				File oldTemplate = new File(client.getDataPath() + formerPrefix
						+ "template1.odt"); //$NON-NLS-1$
				File newTemplate = new File(client.getDataPath()
						+ txtFldPrefix.getText() + "template1.odt"); //$NON-NLS-1$
				boolean cancelled = false;
				if (newTemplate.exists()) {
					MessageDialog.openError(
							getShell(),
							Messages.getString("numbersWindow.templatePrefixAlreadyExistsHeading"), Messages.getString("numbersWindow.templatePrefixAlreadyExistsMessage")); //$NON-NLS-1$ //$NON-NLS-2$
					cancelled = true;
					newTemplate.delete();
				}
				if (!cancelled) {
					if (!oldTemplate.renameTo(newTemplate)) {
						throw new IOException(
								Messages.getString("numbersWindow.couldNotRenameExceptionPart1") + oldTemplate.getName() + Messages.getString("numbersWindow.couldNotRenameExceptionPart2") + newTemplate.getName()); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} else {
					txtFldPrefix.setText(formerPrefix);
				}
			}
		});
		okButton.setText(Messages.getString("numbersWindow.ok")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(100, 30).span(2,1).grab(true, false)
		.applyTo(okButton);

		
		
		updateControlsFromSelection();
		//
		return container;
	}

	public void updateControlsFromSelection() {
		IStructuredSelection selection = (IStructuredSelection) listViewer
				.getSelection();
		transactionType selectedTransaction = (transactionType) selection
				.getFirstElement();
		if (selection.isEmpty()) {
			// default select first element (new asset), e.g. if current element
			// wasd deleted
			selectedTransaction = client.getTransactions()
					.getDefaultTransactionType();
		}
		lblID.setText(Integer.toString(selectedTransaction.getTypeID()));
		txtFldName.setText(selectedTransaction.getTypeName());
		txtFldPrefix.setText(selectedTransaction.getTypePrefix());
		formerPrefix = selectedTransaction.getTypePrefix();
		txtFldFormat.setText(selectedTransaction.getTypeFormat());
		txtFldIndex
				.setText(Integer.toString(selectedTransaction.getTypeIndex()));
		txtFldPeriod.setText(Integer.toString(selectedTransaction
				.getTypePeriod()));

	}

	private void createActions() {
	}

	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager(
				Messages.getString("numbersWindow.menu")); //$NON-NLS-1$
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
			numbersWindow window = new numbersWindow();
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
		return new Point(249, 501);
	}

}
