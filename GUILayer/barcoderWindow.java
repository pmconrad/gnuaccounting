package GUILayer;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import appLayer.application;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

public class barcoderWindow extends ApplicationWindow {

	/**
	 * if we start too many scan threads at once, we're out of memory, if we
	 * start too few, the processor will under-perform, so we need a central
	 * QueueManager to start around the same number of scanThreads as we have
	 * CPU cores available
	 * 
	 * @author jstaerk
	 * 
	 */
	class QueueManager {
		Vector<QueueEntry> queue = new Vector<barcoderWindow.QueueEntry>();
		private int runningThreads = 0;
		private String directory;

		public void add(QueueEntry entry) {
			queue.add(entry);
		}

		public Vector<QueueEntry> getQueue() {
			return queue;
		}

		/**
		 * This will start the processing of the scans/images
		 * 
		 * @param hasChecksum
		 * @param directory
		 */
		public void start(boolean hasChecksum, String directory) {
			this.directory=directory;
			for (QueueEntry entry : queue) {

				entry.setMod43(hasChecksum);
				entry.setDirectory(directory);

			}
			runningThreads = 0;
			startScanThreads();

		}

		/**
		 * start as many scan threads as possible:
		 * initially started - or a thread has finished and it's place is to be taken
		 * */
		private void startScanThreads() {

			if (getNumRunnables() > 0) {
				int availableCores = Runtime.getRuntime().availableProcessors();

				for (QueueEntry entry : queue) {
					if (entry.isRunnable() && (runningThreads < availableCores)) {
						entry.startScan();
						runningThreads++;
					}
				}
			}

		}

		public void clear() {
			queue.removeAllElements();
		}

		private int getNumRunnables() {
			int runnables = 0;
			for (QueueEntry entry : queue) {
				if (entry.isRunnable()) {
					runnables++;
				}
			}
/*			System.err.println("still running: " + runningThreads
 
					+ " \n runnables left: " + runnables);*/
			return runnables;
		}

		/**
		 * a thread signals it has finished
		 */
		public void signalFinish() {
			runningThreads--;

			if (runningThreads == 0) {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						String lastCodeFound=null;
						for (QueueEntry entry : queue) {
							if (entry.getStatusCode()==2) { // found
								lastCodeFound=entry.getCode();
							}
							if (cmbOrder.getText().equals(Messages.getString("Barcoder.codeOnFirst"))&&(entry.getStatusCode()==5)) { // none found //$NON-NLS-1$
								entry.rename(directory, lastCodeFound);
							} else if (entry.getStatusCode()==5) {
								
							}
						}

						MessageDialog.openInformation(getShell(), Messages.getString("Barcoder.finishedHeading"), Messages.getString("Barcoder.finishedText")); //$NON-NLS-1$ //$NON-NLS-2$
						
					}
					
				});
			} else {
				startScanThreads();
			}
		}
	}

	class PictureScanThread extends Thread {
		class Alphabet39 {
			private char codeTable[] = { '0', '1', '2', '3', '4', '5', '6',
					'7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
					'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
					'V', 'W', 'X', 'Y', 'Z', '-', '.', ' ', '$', '/', '+', '%',
					'*' };

			private int getValueForChar(char codeChar) {
				for (int codeTablePos = 0; codeTablePos < codeTable.length; codeTablePos++) {
					if (codeChar == codeTable[codeTablePos]) {
						return codeTablePos;
					}
				}
				throw new RuntimeException(Messages.getString("Barcoder.character") + codeChar //$NON-NLS-1$
						+ Messages.getString("Barcoder.undefined")); //$NON-NLS-1$

			}

			private char getCharForValue(int idx) {
				return codeTable[idx];
			}

			public boolean checkCode(String barcode, char Checksum) {
				int sum = 0;
				for (int barcodePosition = 0; barcodePosition < barcode
						.length(); barcodePosition++) {
					char charToBeChecked = barcode.charAt(barcodePosition);
					int codeValue = getValueForChar(charToBeChecked);
					sum += codeValue;
				}
				return getCharForValue(sum % 43) == Checksum;
			}

		}

		// PictureScanThread
		private String directory;

		private QueueEntry source;
		boolean isMod43 = false;

		public void setDirectory(String directory) {
			this.directory = directory;
		}

		public void setSource(QueueEntry q) {
			this.source = q;
		}


		@Override
		public void run() {

			
			File f = source.getFile(directory);

			URI uri = f.toURI();
			BufferedImage image;
			try {
		        /*RandomAccessFile raf = new RandomAccessFile(f, "r");
		        FileChannel channel = raf.getChannel();
		        ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
		        PDFFile pdffile = new PDFFile(buf);
				
		        image = (BufferedImage)pdffile.getPage(0+1).getImage(
		                (int)pdffile.getPage(0+1).getWidth(),
		                (int)pdffile.getPage(0+1).getHeight(),
		                new Rectangle((int)pdffile.getPage(0+1).getWidth(),
		                (int)pdffile.getPage(0+1).getHeight()),
		                 null, true, true);
				PDF
				 *   //load a pdf from a byte buffer
        File file = new File(pdf);
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        FileChannel channel = raf.getChannel();
        ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        PDFFile pdffile = new PDFFile(buf);
 
        // show the first page
        PDFPage page = pdffile.getPage(0);
        panel.showPage(page);
 */
				image = ImageIO.read(uri.toURL());
				if (image == null) {
					System.err.println(uri.toString()
							+ Messages.getString("Barcoder.errorImageLoad")); //$NON-NLS-1$
					return;
				}
				try {
					LuminanceSource luminance = new BufferedImageLuminanceSource(
							image);
					BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(
							luminance));
					// try hard

					Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(
							DecodeHintType.class);
					Collection<BarcodeFormat> vector = new ArrayList<BarcodeFormat>(
							8);
					vector.add(BarcodeFormat.UPC_A);
					vector.add(BarcodeFormat.UPC_E);
					vector.add(BarcodeFormat.EAN_13);
					vector.add(BarcodeFormat.EAN_8);
					vector.add(BarcodeFormat.RSS_14);
					vector.add(BarcodeFormat.RSS_EXPANDED);
					vector.add(BarcodeFormat.CODE_39);
					vector.add(BarcodeFormat.CODE_93);
					vector.add(BarcodeFormat.CODE_128);
					vector.add(BarcodeFormat.ITF);
					vector.add(BarcodeFormat.QR_CODE);
					vector.add(BarcodeFormat.DATA_MATRIX);
					vector.add(BarcodeFormat.AZTEC);
					vector.add(BarcodeFormat.PDF_417);
					vector.add(BarcodeFormat.CODABAR);
					vector.add(BarcodeFormat.MAXICODE);
					hints.put(DecodeHintType.POSSIBLE_FORMATS, vector);

					hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

					Result result = new MultiFormatReader().decode(bitmap,
							hints);
					/*
					 * ParsedResult parsedResult = ResultParser
					 * .parseResult(result);
					 */

					//

					/*
					 * System.out.println(uri.toString() + " (format: " +
					 * result.getBarcodeFormat() + ", type: " +
					 * parsedResult.getType() + "):\nRaw result:\n" +
					 * result.getText() + "\nParsed result:\n" +
					 * parsedResult.getDisplayResult());
					 * 
					 * System.out.println("Found " +
					 * result.getResultPoints().length + " result points.");
					 */

					if (!source.getFilename().contains(result.getText())) {
						String barcode = result.getText();
						boolean doRename = true;
						if (isMod43) {
							char checkSum = barcode
									.charAt(barcode.length() - 1);
							barcode = barcode
									.substring(0, barcode.length() - 1);

							Alphabet39 theAlphabet = new Alphabet39();
							doRename = theAlphabet.checkCode(barcode, checkSum);

						}
						if (doRename) {
							source.rename(f,barcode);
							source.setStatus(2, barcode);
						} else {
							source.setStatus(3, barcode);

						}

					} else {

						source.setStatus(4);

					}

				} catch (NotFoundException nfe) {
					source.setStatus(5);

				}

			} catch (IllegalArgumentException iae) {
				iae.printStackTrace();
			} catch (MalformedURLException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (IOException ey) {
				// TODO Auto-generated catch block
				ey.printStackTrace();
			}

		}


		public void setMod43(boolean enabled) {
			isMod43 = enabled;
		}

	}

	/**
	 * This class is runnable because it will update the UI itself before and
	 * after the PictureScanThread has done it's work
	 * 
	 * @author jstaerk
	 * 
	 */
	class QueueEntry implements Runnable {
		String filename, statusMessage;
		PictureScanThread scanThread = new PictureScanThread();
		private int status = 0;// 0=to be queued
		private QueueManager manager;
		private String code=null;
		private String directory=null;

		public QueueEntry(QueueManager manager) {
			scanThread.setSource(this);
			this.manager = manager;
		}

		public boolean isRunnable() {
			return status == 0;
		}

		public String getFilename() {
			return filename;
		}

		public File getFile(String directory) {
			this.directory=directory;
			File f = new File(directory + File.separator + getFilename());
			return f;
		}

		private void rename(File f, String barcode) {
			f.renameTo(new File(directory + File.separator
					+ barcode + "-" + getFilename())); //$NON-NLS-1$

		}
		private void rename(String directory, String barcode) {
			
			rename(getFile(directory), barcode);

		}

		public void setFilename(String filename) {
			this.filename = filename;
		}

		public String getStatus() {
			return statusMessage;
		}

		public int getStatusCode() {
			return status;
		}

		@Override
		public void run() {
			queueViewer.refresh();
		}

		private String getMessageForStatus(int status) {
			String[] messages = { Messages.getString("Barcoder.statusRunnable"), Messages.getString("Barcoder.statusProcessing"), Messages.getString("Barcoder.statusRecognized"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					Messages.getString("Barcoder.failedChecksum"), Messages.getString("Barcoder.alreadyProcessed"), Messages.getString("Barcoder.noBarcode") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return messages[status];
		}

		public void setStatus(int status) {
			this.status = status;
			this.statusMessage = getMessageForStatus(status);
			Display.getDefault().asyncExec(this);
			if (status > 1) {
				manager.signalFinish();
			}

		}

		public void setStatus(int status, String code) {
			this.status = status;
			this.statusMessage = getMessageForStatus(status) + " " + code; //$NON-NLS-1$
			this.code=code;
			Display.getDefault().asyncExec(this);
			if (status > 1) {
				manager.signalFinish();
			}
		}

		public String toString() {
			return filename;
		}

		public void setMod43(boolean hasChecksum) {
			scanThread.setMod43(hasChecksum);
		}

		public void setDirectory(String directory) {
			scanThread.setDirectory(directory);

		}

		public void startScan() {
			setStatus(1);
			scanThread.start();
		}

		public String getCode() {
			return code;
		}

	}

	private static final String[] FILTER_NAMES = { "JPEG (*.jpg)", "JPEG (*.JPG)", //$NON-NLS-1$ //$NON-NLS-2$
		"TIFF (*.tif)", "TIFF (*.TIF)", "PNG (*.png)" , "PNG (*.PNG)" , "PDF (*.pdf)" , "PDF (*.PDF)" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

	// These filter extensions are used to filter which files are displayed.
	private static final String[] FILTER_EXTS = { "*.jpg", "*.JPG", "*.tif", "*.TIF", "*.png", "*.PNG", "*.pdf", "*.PDF" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
	Button btnProcess;
	private Text txtDirectory;
	TableViewer queueViewer;
	QueueManager manager = new QueueManager();
	Combo cmbOrder;
	Button btnChecksum;
	/**
	 * Create the application window.
	 */
	public barcoderWindow() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	class BufferedImageLuminanceSource extends LuminanceSource {

		private final BufferedImage image;
		private final int left;
		private final int top;

		public BufferedImageLuminanceSource(BufferedImage image) {
			this(image, 0, 0, image.getWidth(), image.getHeight());
		}

		public BufferedImageLuminanceSource(BufferedImage image, int left,
				int top, int width, int height) {
			super(width, height);

			int sourceWidth = image.getWidth();
			int sourceHeight = image.getHeight();
			if (left + width > sourceWidth || top + height > sourceHeight) {
				throw new IllegalArgumentException(
						Messages.getString("Barcoder.cropError")); //$NON-NLS-1$
			}

			// The color of fully-transparent pixels is irrelevant. They are
			// often, technically, fully-transparent
			// black (0 alpha, and then 0 RGB). They are often used, of course
			// as the "white" area in a
			// barcode image. Force any such pixel to be white:
			for (int y = top; y < top + height; y++) {
				for (int x = left; x < left + width; x++) {
					if ((image.getRGB(x, y) & 0xFF000000) == 0) {
						image.setRGB(x, y, 0xFFFFFFFF); // = white
					}
				}
			}

			// Create a grayscale copy, no need to calculate the luminance
			// manually
			this.image = new BufferedImage(sourceWidth, sourceHeight,
					BufferedImage.TYPE_BYTE_GRAY);
			this.image.getGraphics().drawImage(image, 0, 0, null);
			this.left = left;
			this.top = top;
		}

		@Override
		public byte[] getRow(int y, byte[] row) {
			if (y < 0 || y >= getHeight()) {
				throw new IllegalArgumentException(
						Messages.getString("Barcoder.outsideImageError") + y); //$NON-NLS-1$
			}
			int width = getWidth();
			if (row == null || row.length < width) {
				row = new byte[width];
			}
			// The underlying raster of image consists of bytes with the
			// luminance values
			image.getRaster().getDataElements(left, top + y, width, 1, row);
			return row;
		}

		@Override
		public byte[] getMatrix() {
			int width = getWidth();
			int height = getHeight();
			int area = width * height;
			byte[] matrix = new byte[area];
			// The underlying raster of image consists of area bytes with the
			// luminance values
			image.getRaster().getDataElements(left, top, width, height, matrix);
			return matrix;
		}

		@Override
		public boolean isCropSupported() {
			return true;
		}

		@Override
		public LuminanceSource crop(int left, int top, int width, int height) {
			return new BufferedImageLuminanceSource(image, this.left + left,
					this.top + top, width, height);
		}

		/**
		 * This is always true, since the image is a gray-scale image.
		 * 
		 * @return true
		 */
		@Override
		public boolean isRotateSupported() {
			return true;
		}

		@Override
		public LuminanceSource rotateCounterClockwise() {
			// if (!isRotateSupported()) {
			// throw new IllegalStateException("Rotate not supported");
			// }
			int sourceWidth = image.getWidth();
			int sourceHeight = image.getHeight();

			// Rotate 90 degrees counterclockwise.
			AffineTransform transform = new AffineTransform(0.0, -1.0, 1.0,
					0.0, 0.0, sourceWidth);

			// Note width/height are flipped since we are rotating 90 degrees.
			BufferedImage rotatedImage = new BufferedImage(sourceHeight,
					sourceWidth, BufferedImage.TYPE_BYTE_GRAY);

			// Draw the original image into rotated, via transformation
			Graphics2D g = rotatedImage.createGraphics();
			g.drawImage(image, transform, null);
			g.dispose();

			// Maintain the cropped region, but rotate it too.
			int width = getWidth();
			return new BufferedImageLuminanceSource(rotatedImage, top,
					sourceWidth - (left + width), getHeight(), width);
		}

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


		Button btnBrowse = new Button(container, SWT.NONE);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(getShell(), SWT.MULTI);
				dlg.setFilterNames(FILTER_NAMES);
				dlg.setFilterExtensions(FILTER_EXTS);
				String fn = dlg.open();
				manager.clear();
				btnProcess.setEnabled(false);
				if (fn != null) {
					txtDirectory.setText(dlg.getFilterPath());
					// Append all the selected files. Since getFileNames()
					// returns only
					// the names, and not the path, prepend the path,
					// normalizing
					// if necessary
					StringBuffer buf = new StringBuffer();
					String[] files = dlg.getFileNames();
					for (int i = 0, n = files.length; i < n; i++) {
						buf.append(dlg.getFilterPath());

						if (buf.charAt(buf.length() - 1) != File.separatorChar) {
							buf.append(File.separatorChar);
						}
						buf.append(files[i]);
						QueueEntry q = new QueueEntry(manager);
						q.setFilename(files[i]);
						manager.add(q);
						btnProcess.setEnabled(true);

						buf.append(" "); //$NON-NLS-1$
					}
					queueViewer.refresh();

				}
			}
		});
		btnBrowse.setText(Messages.getString("Barcoder.btnBrowse")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().hint(200, 30).grab(true, false)
		.applyTo(btnBrowse);

		btnProcess = new Button(container, SWT.NONE);
		btnProcess.setEnabled(false);
		btnProcess.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				manager.start(btnChecksum.getSelection(),
						txtDirectory.getText());
			}

		});
		btnProcess.setText(Messages.getString("Barcoder.btnProcess")); //$NON-NLS-1$

		GridDataFactory.fillDefaults().hint(200, 30).grab(true, false)
		.applyTo(btnProcess);
		
		btnChecksum = new Button(container, SWT.CHECK);
		GridDataFactory.fillDefaults().hint(200, 30).grab(true, false)
		.applyTo(btnChecksum);

		btnChecksum.setText(Messages.getString("Barcoder.mod43")); //$NON-NLS-1$


		cmbOrder = new Combo(container, SWT.READ_ONLY);
		cmbOrder.add(Messages.getString("Barcoder.codeOnEvery")); //$NON-NLS-1$
		cmbOrder.add(Messages.getString("Barcoder.codeOnLast")); //$NON-NLS-1$
		cmbOrder.add(Messages.getString("Barcoder.codeOnFirst")); //$NON-NLS-1$
		cmbOrder.select(0);
		GridDataFactory.fillDefaults().hint(200, 30).grab(true, false)
		.applyTo(cmbOrder);


		txtDirectory = new Text(container, SWT.BORDER);
		txtDirectory.setText(Messages.getString("Barcoder.directory")); //$NON-NLS-1$
		txtDirectory.setEditable(false);
		GridDataFactory.fillDefaults().hint(200, 20).span(2,1).grab(true, false)
		.applyTo(txtDirectory);

		
		queueViewer = new TableViewer(container, SWT.BORDER
				| SWT.FULL_SELECTION);
		TableViewerColumn col1 = new TableViewerColumn(queueViewer, SWT.NONE);
		col1.getColumn().setWidth(300);
		col1.getColumn().setText(Messages.getString("Barcoder.lblFilename")); //$NON-NLS-1$
		col1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				QueueEntry q = (QueueEntry) element;
				return q.getFilename();
			}
		});

		TableViewerColumn col2 = new TableViewerColumn(queueViewer, SWT.NONE);
		col2.getColumn().setWidth(200);
		col2.getColumn().setText(Messages.getString("Barcoder.status")); //$NON-NLS-1$
		col2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				QueueEntry q = (QueueEntry) element;
				return q.getStatus();
			}
		});

		Table table = queueViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridDataFactory.fillDefaults().hint(200, 20).span(2,1).grab(true, true)
		.applyTo(table);

		queueViewer.setContentProvider(ArrayContentProvider.getInstance());
		queueViewer.setInput(manager.getQueue());



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
			barcoderWindow window = new barcoderWindow();
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
		return new Point(450, 600);
	}
}
