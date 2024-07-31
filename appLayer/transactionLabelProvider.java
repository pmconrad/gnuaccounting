package appLayer;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import appLayer.transactionRelated.appTransaction;

public class transactionLabelProvider extends LabelProvider implements
		ITableLabelProvider, ITableColorProvider {
	private Display display = null;

	public transactionLabelProvider(Display display) {
		this.display = display;
	}

	public Image getColumnImage(Object arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getColumnText(Object arg0, int arg1) {
		appTransaction t = (appTransaction) arg0;
		return t.getColumnString(arg1);
	}

	public Color getBackground(Object obj, int index) {
		return display.getSystemColor(SWT.COLOR_WHITE);
	}

	public Color getForeground(Object arg0, int arg1) {
		Color color = display.getSystemColor(SWT.COLOR_BLACK);
		appTransaction currentTransaction = (appTransaction) arg0;

		// if
		// ((currentEntry.getDebitAccount()==client.getAccounts().getBankAccount())||(currentEntry.getDebitAccount()==client.getAccounts().getStandardVATDebitAccount()))
		// {
		if (currentTransaction.getCashFlow() == CashFlow.SENDING) {
			color = display.getSystemColor(SWT.COLOR_RED);
		}
		return color;
	}
}