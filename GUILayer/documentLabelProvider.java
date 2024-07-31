package GUILayer;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import appLayer.document;

public class documentLabelProvider extends StyledCellLabelProvider {
	private static String[] captionCodes = {
			Messages.getString("documentLabelProvider.documentHeading"), Messages.getString("documentLabelProvider.dateHeading"), Messages.getString("documentLabelProvider.numberHeading"), Messages.getString("documentLabelProvider.subjectHeading"), Messages.getString("documentLabelProvider.valueHeading"), Messages.getString("documentLabelProvider.actionHeading") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	private final Styler fBoldStyler;
	Display display;

	public documentLabelProvider(Display theDisplay) {
		display = theDisplay;
		fBoldStyler = new Styler() {

			@Override
			public void applyStyles(TextStyle textStyle) {
				textStyle.foreground = display.getSystemColor(SWT.COLOR_BLUE);
				textStyle.underline = true;

			}
		};
	}

	public static String getCaption(int captionNr) {
		return captionCodes[captionNr];
	}

	public Image getColumnImage(Object arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getColumnText(Object arg0, int arg1) {
		document doc = (document) arg0;
		return doc.getStringValueForColumnIndex(arg0, arg1);

	}

	@Override
	public void update(ViewerCell cell) {
		document element = (document) cell.getElement();

		if (cell.getColumnIndex() != 5) {// styled text, i.e links, only in
											// column 5
			cell.setText(getColumnText(element, cell.getColumnIndex()));

		} else {
			// if
			// ((element.getOriginalFilename()!=null)&&(element.getOriginalFilename().length()>0))
			// {
			StyledString styledString = new StyledString(
					Messages.getString("documentLabelProvider.view"), fBoldStyler); //$NON-NLS-1$
			cell.setText(styledString.toString()); //$NON-NLS-1$
			cell.setStyleRanges(styledString.getStyleRanges());
			// }

		}
		super.update(cell);
	}

	public String[] getColumnNames() {
		return captionCodes;
	}

}