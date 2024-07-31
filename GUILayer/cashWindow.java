package GUILayer;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import appLayer.application;
import appLayer.client;
import appLayer.configs;
import appLayer.inventory;
import appLayer.inventory.inventoryType;
import appLayer.inventoryItem;

public class cashWindow extends ApplicationWindow {

	private HashMap<BigDecimal, String> moneyValues = new HashMap<BigDecimal, String>();
	private HashMap<BigDecimal, Text> moneyControls = new HashMap<BigDecimal, Text>();
	private inventory currentInventory;
	private Label totalLbl;

	/**
	 * Create the application window.
	 */
	public cashWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
		// we use the string constructor of bigdecimal for two reasons
		// 1) it's the only way to reliably create values <1 Eur,
		// if we used the double constructor values like 0.1 would be
		// subject to rounding errors.
		// 2) it's localizable, so somebody could translate 500 Eur with a
		// value of 500 to 1000$ with a value of 1000.

		moneyValues
				.put(new BigDecimal(Messages.getString("cashWindow.500val")), Messages.getString("cashWindow.500denom")); //$NON-NLS-1$ //$NON-NLS-2$
		moneyValues
				.put(new BigDecimal(Messages.getString("cashWindow.200val")), Messages.getString("cashWindow.200denom")); //$NON-NLS-1$ //$NON-NLS-2$
		moneyValues
				.put(new BigDecimal(Messages.getString("cashWindow.100val")), Messages.getString("cashWindow.100denom")); //$NON-NLS-1$ //$NON-NLS-2$
		moneyValues
				.put(new BigDecimal(Messages.getString("cashWindow.50val")), Messages.getString("cashWindow.50denum")); //$NON-NLS-1$ //$NON-NLS-2$
		moneyValues
				.put(new BigDecimal(Messages.getString("cashWindow.20val")), Messages.getString("cashWindow.20denom")); //$NON-NLS-1$ //$NON-NLS-2$
		moneyValues
				.put(new BigDecimal(Messages.getString("cashWindow.10val")), Messages.getString("cashWindow.10denum")); //$NON-NLS-1$ //$NON-NLS-2$
		moneyValues
				.put(new BigDecimal(Messages.getString("cashWindow.5val")), Messages.getString("cashWindow.5denom")); //$NON-NLS-1$ //$NON-NLS-2$
		moneyValues
				.put(new BigDecimal(Messages.getString("cashWindow.2val")), Messages.getString("cashWindow.2denom")); //$NON-NLS-1$ //$NON-NLS-2$
		moneyValues
				.put(new BigDecimal(Messages.getString("cashWindow.1val")), Messages.getString("cashWindow.1denom")); //$NON-NLS-1$ //$NON-NLS-2$
		moneyValues
				.put(new BigDecimal(Messages.getString("cashWindow.dot5val")), Messages.getString("cashWindow.dot5denom")); //$NON-NLS-1$ //$NON-NLS-2$
		moneyValues
				.put(new BigDecimal(Messages.getString("cashWindow.dot2val")), Messages.getString("cashWindow.dot2denom")); //$NON-NLS-1$ //$NON-NLS-2$
		moneyValues
				.put(new BigDecimal(Messages.getString("cashWindow.dot1val")), Messages.getString("cashWindow.dot1denom")); //$NON-NLS-1$ //$NON-NLS-2$
		moneyValues
				.put(new BigDecimal(Messages.getString("cashWindow.dot05val")), Messages.getString("cashWindow.dot05denom")); //$NON-NLS-1$ //$NON-NLS-2$
		moneyValues
				.put(new BigDecimal(Messages.getString("cashWindow.dot02val")), Messages.getString("cashWindow.dot02denom")); //$NON-NLS-1$ //$NON-NLS-2$
		moneyValues
				.put(new BigDecimal(Messages.getString("cashWindow.dot01val")), Messages.getString("cashWindow.dot01denom")); //$NON-NLS-1$ //$NON-NLS-2$

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

		Map<BigDecimal, String> sortedMap = new TreeMap<BigDecimal, String>(
				moneyValues);

		for (BigDecimal value : sortedMap.keySet()) {
			Label amountLabel = new Label(container, SWT.None);
			amountLabel.setText(moneyValues.get(value));
			amountLabel.setFont(configs.getDefaultFont());

			Text amountText = new Text(container, SWT.None);
			amountText.setText(""); //$NON-NLS-1$
			amountText.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent arg0) {
					recalcTotal();
				}
			});
			amountText.addKeyListener(new KeyListener() {

				@Override
				public void keyReleased(KeyEvent arg0) {
					recalcTotal();

				}

				@Override
				public void keyPressed(KeyEvent arg0) {
					recalcTotal();

				}
			});
			amountText.setFont(configs.getDefaultFont());
			GridDataFactory.swtDefaults().grab(true, false).hint(50, 20)
					.applyTo(amountText);

			moneyControls.put(value, amountText);

		}
		totalLbl = new Label(container, SWT.NONE);
		totalLbl.setText("0"); //$NON-NLS-1$
		totalLbl.setFont(configs.getDefaultFont());
		GridDataFactory.swtDefaults().grab(true, false)
				.align(SWT.CENTER, SWT.FILL).span(2, 1).hint(100, 20)
				.applyTo(totalLbl);

		Button insertLogBtn = new Button(container, SWT.NONE);
		insertLogBtn.setText(Messages.getString("cashWindow.done")); //$NON-NLS-1$
		insertLogBtn.setFont(configs.getDefaultFont());
		insertLogBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateInventoryItems();
				currentInventory.setFinished();
				currentInventory.save();

			}

		});

		currentInventory = client.getInventories().getLastUnfinishedInventory(
				inventoryType.cash);
		if (currentInventory != null) {
			List<inventoryItem> inventoryItems = currentInventory.getItems();
			for (inventoryItem currentItem : inventoryItems) {
				moneyControls.get(currentItem.getValue()).setText(
						currentItem.getQuantity().toString());
			}
		} else {
			currentInventory = client.getInventories().startInventory(
					inventoryType.cash);
		}
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.FILL).span(2, 1)
				.applyTo(insertLogBtn);
		// GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).applyTo(b);

		return container;
	}

	protected void recalcTotal() {
		BigDecimal total = new BigDecimal(0);
		for (BigDecimal currentValue : moneyControls.keySet()) {
			String text = moneyControls.get(currentValue).getText();
			if (text.length() == 0) {
				text = "0"; //$NON-NLS-1$
			}
			BigDecimal itemQty = new BigDecimal(0);
			try {
				itemQty = new BigDecimal(text);
			} catch (NumberFormatException e) {
				moneyControls.get(currentValue).setText("0"); //$NON-NLS-1$
			}
			total = total.add(itemQty.multiply(currentValue));
		}
		totalLbl.setText(total.toString());
	}

	private void updateInventoryItems() {
		currentInventory.clear();
		currentInventory.save();
		for (BigDecimal currentValue : moneyControls.keySet()) {
			String text = moneyControls.get(currentValue).getText();
			if (text.length() == 0) {
				text = "0"; //$NON-NLS-1$
			}
			BigDecimal itemQty = new BigDecimal(text);
			inventoryItem item = currentInventory.getNewItem(
					moneyValues.get(currentValue), currentValue, itemQty);
			item.save();
			moneyControls.get(currentValue).setText(""); //$NON-NLS-1$
		}
		currentInventory.save();
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
		MenuManager menuManager = new MenuManager("menu"); //$NON-NLS-1$
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
			cashWindow window = new cashWindow();
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
		return new Point(250, 500);
	}

	@Override
	public boolean close() {
		updateInventoryItems();
		currentInventory.save();
		return super.close();
	}

}
