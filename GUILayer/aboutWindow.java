package GUILayer;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import appLayer.application;
import appLayer.configs;
import dataLayer.DB;

public class aboutWindow extends ApplicationWindow {
	private StyledText FreeSoftwareStyledText;
	private StyledText licenseStyledText;
	private StyledText versionHistoryStyledText;
	protected int imageX;
	protected int imageY;
	private boolean handCursorActive;
	private Cursor defaultCursor;
	private Cursor handCursor;

	public aboutWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();

	}

	public int inWhichClickableArea(int x, int y) {
		if ((x > (imageX + 156)) && (y > (imageY + 35)) && (x < (imageX + 229))
				&& (y < (imageY + 49))) {
			return 1;
		} else if ((x > (imageX + 24)) && (y > (imageY + 67))
				&& (x < (imageX + 160)) && (y < (imageY + 82))) {
			return 2;
		} else {
			return 0;
		}

	}

	protected Control createContents(Composite parent) {
		parent.getShell().setSize(getInitialSize());

		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());

		defaultCursor = getShell().getCursor();
		handCursor = new Cursor(getShell().getDisplay(), SWT.CURSOR_HAND);

		container.setFont(configs.getDefaultFont());

		final TabFolder tabFolder = new TabFolder(container, SWT.NONE);

		final TabItem tabAbout = new TabItem(tabFolder, SWT.NONE);
		tabAbout.setText(Messages.getString("aboutWindow.tabAboutCaption")); //$NON-NLS-1$

		Display display = getShell().getDisplay();
		ImageLoader loader = new ImageLoader();
		final ImageData[] imageData = loader.load(getClass()
				.getResourceAsStream("/libs/gnu_herd_banner_01.png")); //$NON-NLS-1$
		final Image image = new Image(display, imageData[0]);

		final Label gnuaccountingLabel = new Label(tabFolder, SWT.CENTER);
		gnuaccountingLabel.addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(final MouseEvent arg0) {
				if (inWhichClickableArea(arg0.x, arg0.y) != 0) {
					handCursorActive = true;
					getShell().setCursor(handCursor);
				} else if (handCursorActive) {
					handCursorActive = false;
					getShell().setCursor(defaultCursor);
				}
			}
		});
		gnuaccountingLabel.addMouseListener(new MouseAdapter() {
			public void mouseDown(final MouseEvent e) {
				// if left-clicked on the image
				if (e.button == 1) {

					if (inWhichClickableArea(e.x, e.y) == 1) {
						tabFolder.setSelection(3);
					} else if (inWhichClickableArea(e.x, e.y) == 2) {
						tabFolder.setSelection(3);
					}

				}

			}
		});
		gnuaccountingLabel.addPaintListener(new PaintListener() {

			public void paintControl(final PaintEvent arg0) {
				imageX = (arg0.gc.getClipping().width / 2)
						- (imageData[0].width / 2);
				imageY = 60;
				arg0.gc.drawImage(image, imageX, imageY);
			}
		});
		gnuaccountingLabel.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_WHITE));
		gnuaccountingLabel.setFont(configs.getDefaultFont());
		gnuaccountingLabel
				.setText(Messages.getString("aboutWindow.gnuaccountingName") + application.getVersionString() + Messages.getString("aboutWindow.dbVersion") + DB.getDBVersionString() + Messages.getString("aboutWindow.gnuaccountingURL")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		//
		tabAbout.setControl(gnuaccountingLabel);

		final TabItem tabLicense = new TabItem(tabFolder, SWT.NONE);
		tabLicense.setText(Messages.getString("aboutWindow.tabLicenseCaption")); //$NON-NLS-1$

		licenseStyledText = new StyledText(tabFolder, SWT.WRAP | SWT.V_SCROLL
				| SWT.READ_ONLY | SWT.BORDER);
		licenseStyledText.setFont(configs.getDefaultFont());
		licenseStyledText.setText(Messages.getString("aboutWindow.license")); //$NON-NLS-1$

		tabLicense.setControl(licenseStyledText);

		final TabItem tabHistory = new TabItem(tabFolder, SWT.NONE);
		tabHistory.setText(Messages.getString("aboutWindow.tabHistoryCaption")); //$NON-NLS-1$

		final TabItem tabFree = new TabItem(tabFolder, SWT.NONE);
		tabFree.setText(Messages
				.getString("aboutWindow.tabFreeSoftwareCaption")); //$NON-NLS-1$

		FreeSoftwareStyledText = new StyledText(tabFolder, SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.BORDER);
		FreeSoftwareStyledText.setText(Messages
				.getString("aboutWindow.freeSoftwareUsedRecommended")); //$NON-NLS-1$
		tabFree.setControl(FreeSoftwareStyledText);

		versionHistoryStyledText = new StyledText(tabFolder, SWT.WRAP
				| SWT.V_SCROLL | SWT.READ_ONLY | SWT.BORDER);
		versionHistoryStyledText.setFont(configs.getDefaultFont());
		versionHistoryStyledText.setText(MainWindow.getVersionHistory());
		tabHistory.setControl(versionHistoryStyledText);
		//
		return container;
	}

	private void createActions() {
	}

	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager(
				Messages.getString("aboutWindow.menu")); //$NON-NLS-1$
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
			aboutWindow window = new aboutWindow();
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
		return new Point(500, 375);
	}

}
