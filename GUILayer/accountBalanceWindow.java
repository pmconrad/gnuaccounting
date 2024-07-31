package GUILayer;

import java.math.BigDecimal;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Vector;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import appLayer.account;
import appLayer.application;
import appLayer.client;
import appLayer.configs;
import appLayer.entry;
import appLayer.entryNotInThisAccountException;
import appLayer.utils;

public class accountBalanceWindow extends ApplicationWindow {
	private Text txtAccount;
	private Table table;
	private Button btnDetails;
	private Button btnDaily;
	private Button btnMonthly;

	/**
	 * Create the application window.
	 */
	public accountBalanceWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	private void updateTable() {
		account selectedAccount = client.getAccounts().getAccountByFullString(
				txtAccount.getText());
		if (selectedAccount == null)
			return;
		table.removeAll();

		Vector<entry> entries = selectedAccount.getJournalForAccount(false);
		long amountCent = 0;
		long currentEntryAmountCent = 0;
		long currentMonthAmountCent = 0;
		long currentDayAmountCent = 0;

		String processedYearMonth = ""; //$NON-NLS-1$
		String currentYearMonth = ""; //$NON-NLS-1$
		String processedDate = ""; //$NON-NLS-1$
		String currentDate = ""; //$NON-NLS-1$

		Collections.sort(entries, new Comparator<entry>() {
			public int compare(entry s1, entry s2) {
				return s1.getDate().compareTo(s2.getDate());
			}
		});
		SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyy-MM"); //$NON-NLS-1$
		SimpleDateFormat sdfDetails = new SimpleDateFormat(
				Messages.getString("accountBalanceWindow.detailDateFormat")); //$NON-NLS-1$
		entry currentEntry = null;
		for (int entryIndex = 0; entryIndex < entries.size(); entryIndex++) {
			currentEntry = entries.get(entryIndex);
			currentYearMonth = sdfMonth.format(currentEntry.getDate());
			if (processedYearMonth.length() == 0) {
				processedYearMonth = currentYearMonth;
			}
			currentDate = sdfDetails.format(currentEntry.getDate());
			if (processedDate.length() == 0) {
				processedDate = currentDate;
			}

			try {
				if (!currentEntry.increasesBalance(selectedAccount)) {
					currentEntryAmountCent = currentEntry.getValue()
							.multiply(new BigDecimal(-100)).longValue();
				} else {
					currentEntryAmountCent = currentEntry.getValue()
							.multiply(new BigDecimal(100)).longValue();
				}

			} catch (entryNotInThisAccountException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if ((btnDaily.getSelection())
					&& (!currentDate.equals(processedDate))) {
				TableItem ti = new TableItem(table, SWT.NONE);
				ti.setText(processedDate);
				ti.setText(
						1,
						utils.round(
								new BigDecimal(currentDayAmountCent)
										.divide(new BigDecimal(100)), 2)
								.toString());
				ti.setText(2, String.valueOf(utils.round(new BigDecimal(
						amountCent).divide(new BigDecimal(100)), 2)));

				processedDate = currentDate;
				currentDayAmountCent = 0;
			}

			if ((btnMonthly.getSelection())
					&& (!currentYearMonth.equals(processedYearMonth))) {
				TableItem ti = new TableItem(table, SWT.NONE);
				ti.setText(processedYearMonth);
				ti.setText(
						1,
						utils.round(
								new BigDecimal(currentMonthAmountCent)
										.divide(new BigDecimal(100)), 2)
								.toString());
				ti.setText(2, String.valueOf(utils.round(new BigDecimal(
						amountCent).divide(new BigDecimal(100)), 2)));

				processedYearMonth = currentYearMonth;
				currentMonthAmountCent = 0;
			}
			amountCent += currentEntryAmountCent;
			currentMonthAmountCent += currentEntryAmountCent;
			currentDayAmountCent += currentEntryAmountCent;

			if (btnDetails.getSelection()) {
				TableItem ti = new TableItem(table, SWT.NONE);
				ti.setText(currentDate);
				ti.setText(1,
						String.valueOf(utils.round(new BigDecimal(
								currentEntryAmountCent).divide(new BigDecimal(
								100)), 2)));
				ti.setText(2, String.valueOf(utils.round(new BigDecimal(
						amountCent).divide(new BigDecimal(100)), 2)));

			}

		}

		// last monthly...
		if (btnMonthly.getSelection()) {
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(currentYearMonth);
			ti.setText(
					1,
					utils.round(
							new BigDecimal(currentMonthAmountCent)
									.divide(new BigDecimal(100)), 2).toString());
			ti.setText(2, String.valueOf(utils.round(
					new BigDecimal(amountCent).divide(new BigDecimal(100)), 2)));
		}
		// and last daily aggregate if applicable
		if (btnDaily.getSelection()) {
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(currentDate);
			ti.setText(
					1,
					utils.round(
							new BigDecimal(currentDayAmountCent)
									.divide(new BigDecimal(100)), 2).toString());
			ti.setText(2, String.valueOf(utils.round(
					new BigDecimal(amountCent).divide(new BigDecimal(100)), 2)));
		}

	}

	/**
	 * Create contents of the application window.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		parent.getShell().setSize(getInitialSize());

		Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(3).margins(10, 5)
		.applyTo(container);


		Label lblAccount = new Label(container, SWT.NONE);
		lblAccount.setAlignment(SWT.RIGHT);
		lblAccount.setText(Messages.getString("accountBalanceWindow.account")); //$NON-NLS-1$
		lblAccount.setFont(configs.getDefaultFont());
		GridDataFactory.swtDefaults().applyTo(lblAccount);


		
		txtAccount = new Text(container, SWT.BORDER);
		txtAccount.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {

				account selectedAccount = client.getAccounts()
						.getAccountByFullString(txtAccount.getText());
				if (selectedAccount != null) {
					updateTable();
				}
			}
		});
		txtAccount.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).span(2,1).grab(true, false)
		.applyTo(txtAccount);


		new AutoCompleteField(txtAccount,
				new TextContentAdapter(), client.getAccounts().getStringArray(false));


		btnMonthly = new Button(container, SWT.RADIO);
		btnMonthly.setSelection(true);
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(btnMonthly);

		btnMonthly.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTable();
			}
		});
		btnMonthly.setText(Messages.getString("accountBalanceWindow.monthly")); //$NON-NLS-1$
		btnMonthly.setFont(configs.getDefaultFont());

		btnDaily = new Button(container, SWT.RADIO);
		btnDaily.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTable();
			}
		});
		btnDaily.setText(Messages
				.getString("accountBalanceWindow.btnRadioButton.text")); //$NON-NLS-1$
		btnDaily.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(btnDaily);

		
		btnDetails = new Button(container, SWT.RADIO);
		GridDataFactory.fillDefaults().hint(200, 20).grab(true, false)
		.applyTo(btnDetails);

		btnDetails.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTable();
			}
		});
		btnDetails.setText(Messages.getString("accountBalanceWindow.details")); //$NON-NLS-1$
		btnDetails.setFont(configs.getDefaultFont());

		table = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setFont(configs.getDefaultFont());
		GridDataFactory.fillDefaults().hint(200, 200).span(3,1).grab(true, true)
		.applyTo(table);

		final TableColumn tblclmnDate = new TableColumn(table, SWT.NONE);
		tblclmnDate.setWidth(100);
		tblclmnDate.setText(Messages.getString("accountBalanceWindow.date")); //$NON-NLS-1$
		Listener sortListener = new Listener() {
			public void handleEvent(Event e) {
				TableItem[] items = table.getItems();
				Collator collator = Collator.getInstance(Locale.getDefault());
				TableColumn sortColumn = table.getSortColumn();
				TableColumn column = (TableColumn) e.widget;
				int dir = table.getSortDirection();
				if (sortColumn == tblclmnDate) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {
					table.setSortColumn(tblclmnDate);
					dir = SWT.UP;
				}
				// sort the data based on column and direction
				int index = column == tblclmnDate ? 0 : 1;
				for (int i = 1; i < items.length; i++) {
					String value1 = items[i].getText(index);
					for (int j = 0; j < i; j++) {
						String value2 = items[j].getText(index);
						if (((dir == SWT.UP) && (collator.compare(value1,
								value2) < 0))
								|| ((dir == SWT.DOWN) && (collator.compare(
										value1, value2) > 0))) {
							String[] values = { items[i].getText(0),
									items[i].getText(1), items[i].getText(2) };
							items[i].dispose();
							TableItem item = new TableItem(table, SWT.NONE, j);
							item.setText(values);
							items = table.getItems();
							break;
						}
					}
				}
				table.setSortColumn(column);
				table.setSortDirection(dir);
			}
		};
		tblclmnDate.addListener(SWT.Selection, sortListener);
		table.setSortColumn(tblclmnDate);
		table.setSortDirection(SWT.UP);

		TableColumn tblclmnProfit = new TableColumn(table, SWT.NONE);
		tblclmnProfit.setWidth(100);
		tblclmnProfit.setText(Messages
				.getString("accountBalanceWindow.surplus")); //$NON-NLS-1$

		TableColumn tblclmnBalance = new TableColumn(table, SWT.NONE);
		tblclmnBalance.setWidth(100);
		tblclmnBalance.setText(Messages
				.getString("accountBalanceWindow.balance")); //$NON-NLS-1$


		txtAccount.forceFocus();

		return container;
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
				Messages.getString("accountBalanceWindow.menu")); //$NON-NLS-1$
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
			accountBalanceWindow window = new accountBalanceWindow();
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
		return new Point(450, 354);
	}
}
