package GUILayer;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.common.HybridBinarizer;


	public class BufferedImageLuminanceSource extends LuminanceSource {

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
						Messages.getString("documentsWindow.errorCrop")); //$NON-NLS-1$
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
						Messages.getString("documentsWindow.errorRow") + y); //$NON-NLS-1$
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


		public static String scanForBarcode(URI uri) {
			String docNr=""; //$NON-NLS-1$
			BufferedImage image;
			try {
				image = ImageIO.read(uri.toURL());
				if (image == null) {
//					System.err.println(uri.toString()
//							+ Messages.getString("documentsWindow.errorLoad")); //$NON-NLS-1$
					// could not open, maybe e.g. a pdf?
					return null;
				}
				try {
					LuminanceSource source;
					source = new BufferedImageLuminanceSource(image);
					BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(
							source));
					// try hard

					Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(
							DecodeHintType.class);
					hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

					Result result = new MultiFormatReader().decode(bitmap,
							hints);
					ParsedResult parsedResult = ResultParser
							.parseResult(result);

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
					docNr = result.getText();

				} catch (NotFoundException nfe) {
					/*
					 * System.out.println(uri.toString() +
					 * ": No barcode found");
					 */
				}

			} catch (IllegalArgumentException iae) {
				System.err.println(Messages
						.getString("documentsWindow.errorResource") + uri); //$NON-NLS-1$
			} catch (MalformedURLException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (IOException ey) {
				// TODO Auto-generated catch block
				ey.printStackTrace();
			}
			return docNr;
		}
		
	}
