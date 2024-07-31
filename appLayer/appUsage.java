package appLayer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import dataLayer.DB;

@Entity
public class appUsage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int ID = -1;

	Timestamp startDate, endDate;
	int client;
	double appVersion;
	double dbVersion;

	public appUsage() {

	}

	public appUsage(int theClient) {
		this.client = theClient;
		Calendar cal = Calendar.getInstance();
		startDate = new Timestamp(cal.getTime().getTime());
		appVersion = application.getVersionDouble();
		dbVersion = DB.getDBVersionDouble();

		try {
			// Create file

			FileWriter fstream = new FileWriter(appLayer.client.getConfigPath()
					+ File.separator + "signature.dat"); //$NON-NLS-1$
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(appVersion + "|" + dbVersion); //$NON-NLS-1$
			// Close the output stream
			out.close();
		} catch (Exception e) {// Catch exception if any
			e.printStackTrace();
		}
	}

	public void save() {
		Calendar cal = Calendar.getInstance();
		endDate = new Timestamp(cal.getTime().getTime());

		DB.getEntityManager().getTransaction().begin();
		DB.getEntityManager().persist(this);
		DB.getEntityManager().getTransaction().commit();

	}

}
