package dataLayer;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.GV_Result.GVRKUms.UmsLine;
import org.kapott.hbci.structures.Konto;

import appLayer.client;
import appLayer.configs;

public class HBCIImporter extends Thread implements IRunnableWithProgress {
	private Date start = null;
	private Date end = null;
	Shell shell;

	public HBCIImporter(Shell sh, Date start, Date end) {
		this.start = start;
		this.end = end;
		this.shell = sh;
	}

	public void run(IProgressMonitor arg0) throws InvocationTargetException,
			InterruptedException {
		// TODO Auto-generated method stub
		HBCI hbci = HBCI.getInstance(shell);

		Konto k = new Konto(configs.getBankCode(), configs.getAccountCode());
		HBCIJob job = hbci.newJob("KUmsAll"); // nächster Auftrag ist Saldenabfrage //$NON-NLS-1$
		job.setParam("my", k); // Kontonummer für Saldenabfrage //$NON-NLS-1$
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
		job.setParam("startdate", formatter.format(start)); //$NON-NLS-1$
		job.setParam("enddate", formatter.format(end)); //$NON-NLS-1$

		job.addToQueue();

		hbci.execute("Abholen von Kontoinformationen"); //$NON-NLS-1$

		GVRKUms result = (GVRKUms) job.getJobResult();
		if (result.isOK()) {
			List<UmsLine> turnover = result.getFlatData();
			int i = 0;
			for (UmsLine umsLine : turnover) {
				String purpose = ""; //$NON-NLS-1$
				for (Object purposeLine : umsLine.usage) {
					purpose += "@" + purposeLine.toString(); //$NON-NLS-1$
				}

				if (umsLine.other.name2 == null) {
					// this can be null when the transaction comes from the bank
					// institute, e.g. annual interest rates
					umsLine.other.name2 = ""; //$NON-NLS-1$
				}

				client.getImportQueue().add(
						purpose,
						umsLine.other.name,
						umsLine.other.name2,
						umsLine.other.number,
						umsLine.other.blz,
						umsLine.valuta,
						new BigDecimal(umsLine.value.getLongValue())
								.divide(new BigDecimal(100)));


				i++;
			}

		}

	}

}
