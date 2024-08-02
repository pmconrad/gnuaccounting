package dataLayer;

import java.io.File;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.swt.widgets.Shell;

import appLayer.AccountNotFoundException;
import appLayer.appUser;
import appLayer.application;
import appLayer.asset;
import appLayer.client;
import appLayer.configs;
import appLayer.contact;
import appLayer.product;
import appLayer.taxRelated.tax;
import appLayer.transactionRelated.transactionType;

public class persistUtil {
	private static final String PERSISTENCE_UNIT_NAME = "jpa"; //$NON-NLS-1$
	private static EntityManagerFactory factory;
	private static boolean DBUpgradePerformed = false;

	/**
	 * This variable will store if the user upgrades to a new version until the
	 * restart of the software as of which it can be detected via the
	 * MAX(DBVERSION) of the appUsage table.
	 * 
	 * I.e. it stores if a DB upgrade has been conducted since the software has
	 * been started because the DB is checked on software start -- and if the
	 * users clicks save in the setup window.
	 */

	public static void connect(Shell sh) {
		// JDBC
		Driver driver = null;
		Properties properties = null;

		String jdbcURLDB = configs.getJDBCURL();
		String jarFile = configs.getDatabaseDriverFileName();
		if ((jarFile != null) && (!jarFile.equals(""))) { //$NON-NLS-1$
			try {
				File jarFileObj = new File(configs.getDatabaseDriverFileName());

				URL url;
				try {
					url = new URL("file://" + jarFileObj.getPath()); //$NON-NLS-1$
					// Sonderzeichen (z.B. Leerzeichen) werden codiert
					url = jarFileObj.toURI().toURL();
					URLClassLoader classLoader = new URLClassLoader(new URL[] { url },
							persistUtil.class.getClassLoader());
					driver = (Driver) Class.forName(configs.getDatabaseDriverName(), true, classLoader).newInstance();
					DriverManager.registerDriver(new DelegatingDriver(driver)); // register
																				// using
																				// the
																				// Delegating
																				// Driver

					DriverManager.getDriver(configs.getJDBCURL()); // checks
																	// that the
																	// driver is
																	// found
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				properties = new Properties();
				properties.put("password", configs.getDatabasePassword()); //$NON-NLS-1$
				properties.put("user", configs.getDatabaseUser()); //$NON-NLS-1$
				properties.put("prompt", "false"); //$NON-NLS-1$ //$NON-NLS-2$
				if (configs.getDatabaseType().equals("mysql")) { //$NON-NLS-1$

					/*
					 * In MySQL, we need to switch off data truncation warnings,
					 * because up to at least the MySQL JDB Connector 5.1.6 one
					 * otherwise CAN NOT store floating numbers via JDBC in
					 * MySQL, even something like
					 * "INSERT INTO account_entries(`value`) VALUES ('12.0')";
					 * would raise a SQLExecption (data truncation) even if
					 * value is float(6,5) or decimal(6,5), see
					 * http://www.ahristov
					 * .com/tutorial/Blog/MySQL%2Band%2BData%2
					 * BTruncation%2B%3A%2BA%2Bdescent%2Binto%2BIEEE%2Bhell.html
					 */
					properties.put("jdbcCompliantTruncation", "false"); //$NON-NLS-1$ //$NON-NLS-2$

				}

			} catch (java.lang.ClassNotFoundException e) {
				e.printStackTrace();
			} catch (NoClassDefFoundError e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				Class.forName(configs.getDatabaseDriverName());
				/*
				 * the next line would not be needed for connectivity because
				 * class.forName registers with drivermanager anyway but this
				 * way we can always use testConnect(driver,properties,jdbcurl)
				 */
				driver = DriverManager.getDriver(jdbcURLDB);
				properties = new Properties();
				properties.put("password", configs.getDatabasePassword()); //$NON-NLS-1$ //$NON-NLS-2$
				properties.put("user", configs.getDatabaseUser()); //$NON-NLS-1$ //$NON-NLS-2$
				properties.put("prompt", "false"); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		try {
			Connection con = driver.connect(configs.getJDBCURL(), properties);
			DB.setConnection(con);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		boolean tablesCorrectlyInitialized = false;// if theres any prev version
		ResultSet rs;
		Statement stmt = null;
		int numVers = 0;
		// at least the tables are
		// there,
		try {
			// Get a statement from the connection
			stmt = DB.getConnection().createStatement();

			// otherwise leave initial init to JPA
			if (configs.getDatabaseType().equals("derby")) { //$NON-NLS-1$
				rs = stmt.executeQuery("SELECT COUNT(*) FROM sys.SYSTABLES WHERE TABLENAME = 'APPUSAGE'"); //$NON-NLS-1$
			} else { // $NON-NLS-1$

				rs = stmt.executeQuery(
						"SELECT COUNT(*) FROM information_schema.tables WHERE table_type = 'BASE TABLE' AND table_schema NOT IN ('pg_catalog', 'information_schema') AND table_name='appusage'"); //$NON-NLS-1$

			}
			// Loop through the result set
			rs.next();

			tablesCorrectlyInitialized = rs.getInt(1) > 0;
			rs.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if ((tablesCorrectlyInitialized) && (!DBUpgradePerformed)) {
			try {
				// Execute the query
				rs = stmt.executeQuery("SELECT MAX(DBVERSION) FROM appusage"); //$NON-NLS-1$

				// Loop through the result set
				rs.next();

				double maxDBVer = rs.getDouble(1);
				rs.close();

				if (maxDBVer > 0) { // if maxDBVer==0.0 it is the initial, first
									// setup of Gnuaccounting, not only the
									// first setup of this version

					if (maxDBVer < 0.81) { // a previous version's DB exists
											// which
											// should be 0.8.0, otherwise the
											// table name would be different
						DBUpgradePerformed = true;

						// DB needs updating
						if (configs.getDatabaseType().equals("derby")) { //$NON-NLS-1$
							stmt = DB.getConnection().createStatement();
							stmt.executeUpdate("ALTER TABLE account DROP OPENINGBALANCE"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE asset DROP DEPRECIATIONEND"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE contact DROP CLIENTID"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE entry DROP ADVANCESTRANSACTIONWORKFLOW"); //$NON-NLS-1$

							stmt.executeUpdate("ALTER TABLE asset ADD COLUMN NEW_COLUMN DECIMAL( 16, 6 )"); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE asset SET NEW_COLUMN=VALUE"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE asset DROP COLUMN VALUE"); //$NON-NLS-1$
							stmt.executeUpdate("RENAME COLUMN asset.NEW_COLUMN TO VALUE"); //$NON-NLS-1$

							stmt.executeUpdate("ALTER TABLE asset ADD DEPRECATEDVALUE DECIMAL( 16, 6 ) "); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE asset ADD LIFETIME INT"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE asset ADD REMOVAL DATE"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE asset ADD REMOVALREASON VARCHAR( 255 ) "); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE asset ADD REVENUEIFSOLD DECIMAL( 16, 6 )"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE asset ADD STATUS INT"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE asset ADD LOCATION VARCHAR( 255 ) "); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE asset ADD NUMBER VARCHAR( 255 ) "); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE asset ADD REMARK VARCHAR( 255 ) "); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE contact ADD LASTVCFCHANGE DATE "); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE contact ADD VCFID VARCHAR( 255 )"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE transactionfrombankaccountimport ADD REFERENCE VARCHAR( 255 ) "); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE apptransaction ADD TRANSDOCUMENT_ID INT "); //$NON-NLS-1$
							stmt.executeUpdate(
									"CREATE INDEX TRANSDOCUMENT_ID ON  apptransaction ( TRANSDOCUMENT_ID ) "); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE apptransaction ADD FOREIGN KEY ( TRANSDOCUMENT_ID ) REFERENCES document ( ID )"); //$NON-NLS-1$
						} else if (configs.getDatabaseType().equals("mysql")) { //$NON-NLS-1$
							stmt = DB.getConnection().createStatement();
							stmt.executeUpdate("ALTER TABLE `account` DROP `OPENINGBALANCE`"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE `asset` DROP `DEPRECIATIONEND`"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE `contact` DROP `CLIENTID`"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE `entry` DROP `ADVANCESTRANSACTIONWORKFLOW`"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `asset` CHANGE `VALUE` `VALUE` DECIMAL( 16, 6 ) NULL DEFAULT NULL"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `asset` ADD `DEPRECATEDVALUE` DECIMAL( 16, 6 ) NULL AFTER `ACCOUNT_ENTRY_ID`"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `asset` ADD `LIFETIME` INT( 11 ) NULL AFTER `DEPRECIATIONTYPE`"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE `asset` ADD `REMOVAL` DATE NULL AFTER `OUTDATED`"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `asset` ADD `REMOVALREASON` VARCHAR( 255 ) NULL AFTER `REMOVAL`"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `asset` ADD `REVENUEIFSOLD` DECIMAL( 16, 6 ) NULL AFTER `REMOVALREASON`"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `asset` ADD `STATUS` INT( 11 ) NULL AFTER `REVENUEIFSOLD` "); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `asset` ADD `LOCATION` VARCHAR( 255 ) NULL AFTER `LIFETIME`"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE `asset` ADD `NUMBER` VARCHAR( 255 ) NULL AFTER `NAME`"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE `asset` ADD `REMARK` VARCHAR( 255 ) NULL AFTER `OUTDATED`"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE `contact` ADD `LASTVCFCHANGE` DATE NULL AFTER `FAX`"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE `contact` ADD `VCFID` VARCHAR( 255 ) NULL AFTER `STREET`"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `transactionfrombankaccountimport` ADD `REFERENCE` VARCHAR( 255 ) NULL AFTER `OUTDATED`"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `apptransaction` ADD `TRANSDOCUMENT_ID` INT( 11 ) NULL AFTER `RECIPIENT_ID`"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE `apptransaction` ADD INDEX ( `TRANSDOCUMENT_ID` )"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `apptransaction` ADD FOREIGN KEY ( `TRANSDOCUMENT_ID` ) REFERENCES `document` (`ID`)"); //$NON-NLS-1$

						}
					}
					if (maxDBVer < 0.82) { // upgrading from
											// DB 0.8.1 to 0.8.2
						DBUpgradePerformed = true;
						stmt = DB.getConnection().createStatement();
						rs = stmt.executeQuery("SELECT MIN(ACCOUNTID) FROM account WHERE CODE='1200'"); //$NON-NLS-1$
						// there will be 2 account IDs 1200, one for SKR03 and
						// one for SKR04.
						// take the one for SKR04 (should be the smaller ID) and
						// hope the user does not have SKR04 selected
						// Loop through the result set.
						// client.getAccounts().getBankAccount() will not yet
						// work w/o session persistence.
						rs.next();
						int ID = rs.getInt(1);
						rs.close();

						if (configs.getDatabaseType().equals("derby")) { //$NON-NLS-1$

							stmt.executeUpdate(
									"ALTER TABLE transactionfrombankaccountimport ADD CONTRAACCOUNT_ACCOUNTID INT "); //$NON-NLS-1$
							stmt.executeUpdate(
									"UPDATE transactionfrombankaccountimport SET CONTRAACCOUNT_ACCOUNTID=" + ID);// 291 //$NON-NLS-1$
																													// in
																													// my
																													// case
																													// is
																													// 1200
																													// bank
																													// in
																													// SKR03

							stmt.executeUpdate("ALTER TABLE asset ADD COLUMN NEW_COLUMN DECIMAL( 16, 6 )"); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE asset SET NEW_COLUMN=VALUE"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE asset DROP COLUMN VALUE"); //$NON-NLS-1$
							stmt.executeUpdate("RENAME COLUMN asset.NEW_COLUMN TO VALUE"); //$NON-NLS-1$

							stmt.executeUpdate("ALTER TABLE asset ADD COLUMN NEW_COLUMN DECIMAL( 16, 6 )"); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE asset SET NEW_COLUMN=DEPRECATEDVALUE"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE asset DROP COLUMN DEPRECATEDVALUE"); //$NON-NLS-1$
							stmt.executeUpdate("RENAME COLUMN asset.NEW_COLUMN TO DEPRECATEDVALUE"); //$NON-NLS-1$

							stmt.executeUpdate("ALTER TABLE asset ADD COLUMN NEW_COLUMN DECIMAL( 16, 6 )"); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE asset SET NEW_COLUMN=REVENUEIFSOLD"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE asset DROP COLUMN REVENUEIFSOLD"); //$NON-NLS-1$
							stmt.executeUpdate("RENAME COLUMN asset.NEW_COLUMN TO REVENUEIFSOLD"); //$NON-NLS-1$

						} else if (configs.getDatabaseType().equals("mysql")) { //$NON-NLS-1$
							stmt = DB.getConnection().createStatement();
							stmt.executeUpdate(
									"ALTER TABLE `transactionfrombankaccountimport` ADD `CONTRAACCOUNT_ACCOUNTID` INT( 11 ) NULL AFTER `VALUE`"); //$NON-NLS-1$
							stmt.executeUpdate(
									"UPDATE transactionfrombankaccountimport SET CONTRAACCOUNT_ACCOUNTID=" + ID);// ...1200 //$NON-NLS-1$
																													// bank
																													// in
																													// SKR03

							stmt.executeUpdate(
									"ALTER TABLE `asset` CHANGE `VALUE` `VALUE` DECIMAL( 16, 6 ) NULL DEFAULT NULL"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `asset` CHANGE `DEPRECATEDVALUE` `DEPRECATEDVALUE` DECIMAL( 16, 6 ) NULL DEFAULT NULL"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `asset` CHANGE `REVENUEIFSOLD` `REVENUEIFSOLD` DECIMAL( 16, 6 ) NULL DEFAULT NULL"); //$NON-NLS-1$
						}

					}
					if (maxDBVer < 0.83) {
						if (maxDBVer < DB.getDBVersionDouble()) { // upgrading
																	// from
							// DB 0.8.2->0.8.3
							DBUpgradePerformed = true;

							if (configs.getDatabaseType().equals("derby")) { //$NON-NLS-1$

								stmt.executeUpdate("ALTER TABLE document ADD METADATA VARCHAR ( 255 ) "); //$NON-NLS-1$
								stmt.executeUpdate("ALTER TABLE document ADD BEZAHLCODEACCOUNT VARCHAR ( 255 ) "); //$NON-NLS-1$
								stmt.executeUpdate("ALTER TABLE document ADD BEZAHLCODEBANKID VARCHAR ( 255 ) "); //$NON-NLS-1$
								stmt.executeUpdate("ALTER TABLE document ADD BEZAHLCODEHOLDER VARCHAR ( 255 ) "); //$NON-NLS-1$
								stmt.executeUpdate("ALTER TABLE document ADD BEZAHLCODEPURPOSE VARCHAR ( 255 ) "); //$NON-NLS-1$

								stmt.executeUpdate("ALTER TABLE document ADD BEZAHLCODEAMOUNT DECIMAL ( 16,6 ) "); //$NON-NLS-1$
								stmt.executeUpdate("ALTER TABLE document ADD BEZAHLCODEPARSED INT "); //$NON-NLS-1$

							} else if (configs.getDatabaseType().equals("mysql")) { //$NON-NLS-1$
								stmt = DB.getConnection().createStatement();
								stmt.executeUpdate("ALTER TABLE document ADD METADATA VARCHAR ( 255 ) NULL "); //$NON-NLS-1$
								stmt.executeUpdate("ALTER TABLE document ADD BEZAHLCODEACCOUNT VARCHAR ( 255 ) NULL "); //$NON-NLS-1$
								stmt.executeUpdate("ALTER TABLE document ADD BEZAHLCODEBANKID VARCHAR ( 255 ) NULL "); //$NON-NLS-1$
								stmt.executeUpdate("ALTER TABLE document ADD BEZAHLCODEHOLDER VARCHAR ( 255 ) NULL "); //$NON-NLS-1$
								stmt.executeUpdate("ALTER TABLE document ADD BEZAHLCODEPURPOSE VARCHAR ( 255 ) NULL "); //$NON-NLS-1$

								stmt.executeUpdate("ALTER TABLE document ADD BEZAHLCODEAMOUNT DECIMAL ( 16,6 ) NULL "); //$NON-NLS-1$
								stmt.executeUpdate(
										"ALTER TABLE document ADD BEZAHLCODEPARSED INT ( 1 ) NULL DEFAULT '0'"); //$NON-NLS-1$

							}

						}

					}
					if (maxDBVer < 0.84) { // upgrading from
						// DB 0.8.3
						DBUpgradePerformed = true;

						if (configs.getDatabaseType().equals("derby")) { //$NON-NLS-1$
							/*
							 * 
							 * CREATE TABLE IF NOT EXISTS `DOCUMENT` (
							 * 
							 * - `BEZAHLCODEAMOUNT` decimal(16,6) DEFAULT NULL,
							 * +`METAAMOUNT` decimal(16,6) DEFAULT NULL,
							 * 
							 * 
							 * -`BEZAHLCODEPURPOSE` varchar(255) DEFAULT NULL, +
							 * `METAPURPOSE` varchar(255) DEFAULT NULL,
							 * 
							 * - `METADATA` varchar(255) DEFAULT NULL,
							 * +`METADATA` longtext,
							 */

							stmt.executeUpdate("ALTER TABLE apptransaction ADD PURPOSE int"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE contact ADD BIC VARCHAR ( 255 )"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE contact ADD IBAN VARCHAR ( 255 )"); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE contact SET BIC='', IBAN=''"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE product ADD TYPE int"); //$NON-NLS-1$

							stmt.executeUpdate("RENAME COLUMN document.BEZAHLCODEAMOUNT TO METAAMOUNT"); //$NON-NLS-1$
							stmt.executeUpdate("RENAME COLUMN document.BEZAHLCODEPURPOSE TO METAPURPOSE"); //$NON-NLS-1$

							// change type (enlarge) Metadata (from varchar
							// (255) to longtext)
							stmt.executeUpdate("ALTER TABLE document ADD COLUMN NEW_COLUMN CLOB"); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE document SET NEW_COLUMN=METADATA"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE document DROP COLUMN METADATA"); //$NON-NLS-1$
							stmt.executeUpdate("RENAME COLUMN document.NEW_COLUMN TO METADATA"); //$NON-NLS-1$

						} else if (configs.getDatabaseType().equals("mysql")) { //$NON-NLS-1$
							stmt = DB.getConnection().createStatement();

							stmt.executeUpdate("ALTER TABLE apptransaction ADD `PURPOSE` int(11) DEFAULT NULL"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE contact ADD BIC VARCHAR ( 255 ) NULL "); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE contact ADD IBAN VARCHAR ( 255 ) NULL "); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE contact SET BIC='', IBAN=''"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE product ADD `TYPE` int(11) DEFAULT NULL"); //$NON-NLS-1$

							stmt.executeUpdate(
									"ALTER TABLE `document` CHANGE `BEZAHLCODEAMOUNT` `METAAMOUNT` DECIMAL( 16, 6 ) NULL DEFAULT NULL"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `document` CHANGE `BEZAHLPURPOSE` `METAPURPOSE` VARCHAR(255) NULL DEFAULT NULL"); //$NON-NLS-1$

							// change type (enlarge) Metadata (from varchar
							// (255) to longtext)
							stmt.executeUpdate(
									"ALTER TABLE `document` CHANGE `METADATA` `METADATA` LONGTEXT NULL DEFAULT NULL"); //$NON-NLS-1$

						}

					}
					if (maxDBVer < 0.85) { // upgrading from
						if (configs.getDatabaseType().equals("derby")) { //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE asset ADD DEPRECIATIONEND DATE"); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE asset SET DEPRECIATIONEND=DEPRECIATIONSTART"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE contact ADD SEPAMANDATE VARCHAR ( 255 )  "); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE contact SET SEPAMANDATE=''"); //$NON-NLS-1$

							stmt.executeUpdate("ALTER TABLE document ADD HOCR CLOB "); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE document ADD METABANKNAME VARCHAR ( 255 )  "); //$NON-NLS-1$
							stmt.executeUpdate("RENAME COLUMN document.BEZAHLCODEACCOUNT TO METAIBAN"); //$NON-NLS-1$
							stmt.executeUpdate("RENAME COLUMN document.BEZAHLCODEBANKID TO METABIC"); //$NON-NLS-1$
							stmt.executeUpdate("RENAME COLUMN document.BEZAHLCODEHOLDER TO METAHOLDER"); //$NON-NLS-1$

						} else if (configs.getDatabaseType().equals("mysql")) { //$NON-NLS-1$
							stmt = DB.getConnection().createStatement();

							stmt.executeUpdate("ALTER TABLE `asset` ADD `DEPRECIATIONEND` DATE NULL AFTER `OUTDATED`"); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE `asset` SET DEPRECIATIONEND=DEPRECIATIONSTART"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE contact ADD SEPAMANDATE VARCHAR ( 255 ) NULL "); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE contact SET SEPAMANDATE=''"); //$NON-NLS-1$

							stmt.executeUpdate("ALTER TABLE `document` ADD HOCR LONGTEXT NULL "); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `document` ADD `METABANKNAME` VARCHAR(255) NULL DEFAULT NULL"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `document` CHANGE `BEZAHLCODEACCOUNT` `METAIBAN` VARCHAR(255) NULL DEFAULT NULL"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `document` CHANGE `BEZAHLCODEBANKID` `METABIC` VARCHAR(255) NULL DEFAULT NULL"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `document` CHANGE `BEZAHLCODEHOLDER` `METAHOLDER` VARCHAR(255) NULL DEFAULT NULL"); //$NON-NLS-1$

						}

					}

					if (maxDBVer < 0.86) { // upgrading from 0.8.6
						if (configs.getDatabaseType().equals("derby")) { //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE apptransaction ADD COLUMN PERFORMANCESTART DATE"); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE apptransaction SET PERFORMANCESTART=ISSUEDATE"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE apptransaction ADD COLUMN PERFORMANCEEND DATE"); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE apptransaction SET PERFORMANCEEND=ISSUEDATE"); //$NON-NLS-1$

						} else if (configs.getDatabaseType().equals("mysql")) { //$NON-NLS-1$
							stmt = DB.getConnection().createStatement();

							stmt.executeUpdate(
									"ALTER TABLE `apptransaction` ADD `PERFORMANCESTART` DATE NULL AFTER `OUTDATED`"); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE `apptransaction` SET PERFORMANCESTART=ISSUEDATE"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `apptransaction` ADD `PERFORMANCEEND` DATE NULL AFTER `OUTDATED`"); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE `apptransaction` SET PERFORMANCEEND=ISSUEDATE"); //$NON-NLS-1$

						}

					}
					if (maxDBVer < 0.87) { // upgrading from 0.8.6
						if (configs.getDatabaseType().equals("derby")) { //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE product ADD COLUMN TVQ_ID INT"); //$NON-NLS-1$
						} else if (configs.getDatabaseType().equals("mysql")) { //$NON-NLS-1$
							stmt = DB.getConnection().createStatement();
							stmt.executeUpdate("ALTER TABLE `PRODUCT` ADD `TVQ_ID` int(11) NULL AFTER `UNIT`"); //$NON-NLS-1$
						}
					}
					if (maxDBVer < 0.88) { // upgrading from 0.8.7
						if (configs.getDatabaseType().equals("derby")) { //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE apptransaction ADD COLUMN NEW_COLUMN CLOB"); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE apptransaction SET NEW_COLUMN=DEFAULTDESCRIPTION"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE apptransaction DROP COLUMN DEFAULTDESCRIPTION"); //$NON-NLS-1$
							stmt.executeUpdate("RENAME COLUMN apptransaction.NEW_COLUMN TO DEFAULTDESCRIPTION"); //$NON-NLS-1$

							stmt.executeUpdate("ALTER TABLE apptransaction ADD COLUMN NEW_COLUMN CLOB"); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE apptransaction SET NEW_COLUMN=DEFAULTREFERENCE"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE apptransaction DROP COLUMN DEFAULTREFERENCE"); //$NON-NLS-1$
							stmt.executeUpdate("RENAME COLUMN apptransaction.NEW_COLUMN TO DEFAULTREFERENCE"); //$NON-NLS-1$

							stmt.executeUpdate("ALTER TABLE apptransaction ADD COLUMN NEW_COLUMN CLOB"); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE apptransaction SET NEW_COLUMN=DEFAULTCOMMENT"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE apptransaction DROP COLUMN DEFAULTCOMMENT"); //$NON-NLS-1$
							stmt.executeUpdate("RENAME COLUMN apptransaction.NEW_COLUMN TO DEFAULTCOMMENT"); //$NON-NLS-1$

							stmt.executeUpdate("ALTER TABLE apptransaction ADD COLUMN NEW_COLUMN CLOB"); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE apptransaction SET NEW_COLUMN=REMARKS"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE apptransaction DROP COLUMN REMARKS"); //$NON-NLS-1$
							stmt.executeUpdate("RENAME COLUMN apptransaction.NEW_COLUMN TO REMARKS"); //$NON-NLS-1$

							stmt.executeUpdate("ALTER TABLE apptransaction ADD COLUMN NEW_COLUMN CLOB"); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE apptransaction SET NEW_COLUMN=PAYMENTPURPOSE"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE apptransaction DROP COLUMN PAYMENTPURPOSE"); //$NON-NLS-1$
							stmt.executeUpdate("RENAME COLUMN apptransaction.NEW_COLUMN TO PAYMENTPURPOSE"); //$NON-NLS-1$
						} else if (configs.getDatabaseType().equals("mysql")) { //$NON-NLS-1$
							stmt = DB.getConnection().createStatement();
							stmt.executeUpdate(
									"ALTER TABLE `apptransaction` CHANGE `DEFAULTDESCRIPTION` `DEFAULTDESCRIPTION` LONGTEXT NULL DEFAULT NULL"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `apptransaction` CHANGE `DEFAULTREFERENCE` `DEFAULTREFERENCE` LONGTEXT NULL DEFAULT NULL"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `apptransaction` CHANGE `DEFAULTCOMMENT` `DEFAULTCOMMENT` LONGTEXT NULL DEFAULT NULL"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `apptransaction` CHANGE `REMARKS` `REMARKS` LONGTEXT NULL DEFAULT NULL"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `apptransaction` CHANGE `PAYMENTPURPOSE` `PAYMENTPURPOSE` LONGTEXT NULL DEFAULT NULL"); //$NON-NLS-1$
						}
					}

					if (maxDBVer < DB.getDBVersionDouble()) { // upgrading from
																// 0.8.8
						/*
						 * if (configs.getDatabaseType().equals("derby")) {
						 * //$NON-NLS-1$ stmt.
						 * executeUpdate("ALTER TABLE apptransaction ADD ISTAXEXEMPT SMALLINT"
						 * ); //$NON-NLS-1$ stmt.
						 * executeUpdate("UPDATE apptransaction SET ISTAXEXEMPT=0"
						 * ); //$NON-NLS-1$ stmt.
						 * executeUpdate("RENAME COLUMN product.TVQ_ID TO SALESTAX_ID"
						 * ); //$NON-NLS-1$
						 * 
						 * stmt.
						 * executeUpdate("ALTER TABLE contact ADD TAXEXEMPT SMALLINT"
						 * ); //$NON-NLS-1$
						 * stmt.executeUpdate("UPDATE contact SET TAXEXEMPT=0");
						 * //$NON-NLS-1$
						 * 
						 * } else if (configs.getDatabaseType().equals("mysql"))
						 * { //$NON-NLS-1$ stmt =
						 * DB.getConnection().createStatement(); stmt.
						 * executeUpdate("ALTER TABLE `APPTRANSACTION` ADD `ISTAXEXEMPT` tinyint(1) NULL DEFAULT '0' AFTER `IMPORT_ITEM_ID`"
						 * ); //$NON-NLS-1$ stmt.
						 * executeUpdate("ALTER TABLE `contact` ADD `TAXEXEMPT` tinyint(1) NULL DEFAULT '0' AFTER `STREET`"
						 * ); //$NON-NLS-1$ stmt.
						 * executeUpdate("ALTER TABLE `product` CHANGE `TVQ_ID` `SALESTAX_ID` INT ( 11 ) NULL DEFAULT NULL"
						 * ); //$NON-NLS-1$ }
						 */
						if (configs.getDatabaseType().equals("derby")) { //$NON-NLS-1$
							stmt.executeUpdate("DELETE FROM appuser"); //$NON-NLS-1$

							// stmt.executeUpdate("ALTER TABLE appuser ALTER SALT SET DATA TYPE VARCHAR(255)"); raises a  The type of a column may not be changed. Exceptioon
							// --> drop old col, create new one 
							stmt.executeUpdate("ALTER TABLE appuser DROP COLUMN SALT"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE appuser ADD COLUMN SALT VARCHAR(255)"); //$NON-NLS-1$

							stmt.executeUpdate("ALTER TABLE appuser DROP COLUMN PASSWORDHASH"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE appuser ADD COLUMN PASSWORDHASH VARCHAR(255)"); //$NON-NLS-1$
							stmt.executeUpdate("ALTER TABLE account ADD ISAUTOVATACCOUNT INT"); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE account SET ISAUTOVATACCOUNT=0"); //$NON-NLS-1$
							stmt.executeUpdate("UPDATE account SET ISAUTOVATACCOUNT=1 WHERE CODE IN ('2401', '2405', '3110', '3120', '3300', '3400', '3420', '3425', '3440', '3550', '3731', '3734', '3736', '3750', '3760', '3780', '3790', '8100', '8110', '8120', '8125', '8130', '8135', '8150', '8300', '8310', '8315', '8400', '8591', '8595', '8710', '8720', '8725', '8726', '8731', '8736', '8750', '8760', '8780', '8790', '8801', '8807', '8808', '8820', '8827', '8828', '8910', '8915', '8920', '8921', '8922', '8925', '8930', '8935', '8940', '8945')"); //$NON-NLS-1$

						} else if (configs.getDatabaseType().equals("mysql")) { //$NON-NLS-1$
							stmt = DB.getConnection().createStatement();
							stmt.executeUpdate("DELETE FROM `APPUSER`"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `APPUSER` CHANGE `SALT` `SALT` VARCHAR ( 255 ) NULL DEFAULT NULL"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `APPUSER` CHANGE `PASSWORDHASH` `PASSWORDHASH` VARCHAR ( 255 ) NULL DEFAULT NULL"); //$NON-NLS-1$
							stmt.executeUpdate(
									"ALTER TABLE `account` ADD ISAUTOVATACCOUNT INT ( 1 ) NULL DEFAULT '0'"); //$NON-NLS-1$
							stmt.executeUpdate(
									"UPDATE `account` SET ISAUTOVATACCOUNT=1 WHERE CODE IN ('2401', '2405', '3110', '3120', '3300', '3400', '3420', '3425', '3440', '3550', '3731', '3734', '3736', '3750', '3760', '3780', '3790', '8100', '8110', '8120', '8125', '8130', '8135', '8150', '8300', '8310', '8315', '8400', '8591', '8595', '8710', '8720', '8725', '8726', '8731', '8736', '8750', '8760', '8780', '8790', '8801', '8807', '8808', '8820', '8827', '8828', '8910', '8915', '8920', '8921', '8922', '8925', '8930', '8935', '8940', '8945')"); //$NON-NLS-1$
						}

					}

				}

				// Close the result set, statement
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} // if anyPreviousVersionExists
			// JPA/derby
		HashMap<String, Object> configOverrides = new HashMap<String, Object>();

		configOverrides.put("javax.persistence.jdbc.driver", configs.getDatabaseDriverName()); //$NON-NLS-1$
		configOverrides.put("javax.persistence.jdbc.user", configs.getDatabaseUser()); //$NON-NLS-1$
		configOverrides.put("javax.persistence.jdbc.password", configs.getDatabasePassword()); //$NON-NLS-1$
		configOverrides.put("eclipselink.ddl-generation", "create-tables"); //$NON-NLS-1$ //$NON-NLS-2$
		configOverrides.put("eclipselink.ddl-generation.output-mode", "database"); //$NON-NLS-1$ //$NON-NLS-2$

		// eclipselink.logging.level OFF, SEVERE, finer, finest, all
		configOverrides.put("eclipselink.logging.level", "SEVERE"); //$NON-NLS-1$ //$NON-NLS-2$

		configOverrides.put("javax.persistence.jdbc.url", configs.getJDBCURL()); //$NON-NLS-1$
		// We create the SchemaUpdate thanks to the configs

		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, configOverrides);
		DB.setEntityManager(factory.createEntityManager());

		try {

			ensureDBContainsDefaults(sh);
			// do this in any case

			client.getTransactions().init(); // load the just stored transaction
												// types so that their internal
												// IDs are updated

		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}

	}

	public static void ensureDBContainsDefaults(Shell sh) throws AccountNotFoundException {
		// initial installation, create default values
		if (client.getAccounts().empty()) {
			client.getAccounts().importAccountsFromXML(sh);
		}

		if (transactionType.getNumTypes() == 0) {
			transactionType invoice = new transactionType(0, 21, Messages.getString("persistUtil.Invoice"), //$NON-NLS-1$
					Messages.getString("persistUtil.InvoicePrefix"), Messages.getString("persistUtil.InvoiceFormat")); //$NON-NLS-1$ //$NON-NLS-2$
			invoice.save();
			transactionType creditnote = new transactionType(0, 0, Messages.getString("persistUtil.CreditNote"), //$NON-NLS-1$
					Messages.getString("persistUtil.CreditNotePrefix"), //$NON-NLS-1$
					Messages.getString("persistUtil.CreditNoteFormat")); //$NON-NLS-1$
			creditnote.save();
			transactionType cancellation = new transactionType(0, 0, Messages.getString("persistUtil.Cancellation"), //$NON-NLS-1$
					Messages.getString("persistUtil.CancellationPrefix"), //$NON-NLS-1$
					Messages.getString("persistUtil.CancellationFormat")); //$NON-NLS-1$
			cancellation.setAllowUnreferencedCreation(false);
			cancellation.save();
			transactionType reminder = new transactionType(0, 0, Messages.getString("persistUtil.Reminder"), //$NON-NLS-1$
					Messages.getString("persistUtil.ReminderPrefix"), Messages.getString("persistUtil.ReminderFormat")); //$NON-NLS-1$ //$NON-NLS-2$
			cancellation.setAllowUnreferencedCreation(false);
			reminder.save();
			transactionType offer = new transactionType(0, 0, Messages.getString("persistUtil.Offer"), //$NON-NLS-1$
					Messages.getString("persistUtil.OfferPrefix"), Messages.getString("persistUtil.OfferFormat")); //$NON-NLS-1$ //$NON-NLS-2$
			offer.save();
			transactionType personalDrawing = new transactionType(0, 0,
					Messages.getString("persistUtil.PersonalDrawing"), //$NON-NLS-1$
					Messages.getString("persistUtil.PersonalDrawingPrefix"), //$NON-NLS-1$
					Messages.getString("persistUtil.PersonalDrawingFormat")); //$NON-NLS-1$
			personalDrawing.save();
			transactionType receipt = new transactionType(0, 0, Messages.getString("persistUtil.Receipt"), //$NON-NLS-1$
					Messages.getString("persistUtil.ReceiptPrefix"), Messages.getString("persistUtil.ReceiptFormat")); //$NON-NLS-1$ //$NON-NLS-2$
			receipt.save();
			transactionType shippingTicket = new transactionType(0, 0, Messages.getString("persistUtil.ShippingTicket"), //$NON-NLS-1$
					Messages.getString("persistUtil.ShippingTicketPrefix"), //$NON-NLS-1$
					Messages.getString("persistUtil.ShippingTicketFormat")); //$NON-NLS-1$
			shippingTicket.save();
			transactionType receiptIncoming = new transactionType(0, 0,
					Messages.getString("persistUtil.incomingReceipt"), //$NON-NLS-1$
					Messages.getString("persistUtil.incomingReceiptPrefix"), //$NON-NLS-1$
					Messages.getString("persistUtil.incomingReceiptFormat")); //$NON-NLS-1$
			receiptIncoming.save();
		}
		if (transactionType.getNumTypes() == 9) {
			/**
			 * gnuaccounting <= 0.8.8
			 */
			transactionType salarySlip = new transactionType(0, 0,
					Messages.getString("persistUtil.salarySlip"), //$NON-NLS-1$
					Messages.getString("persistUtil.salarySlipPrefix"),  //$NON-NLS-1$
					Messages.getString("persistUtil.salarySlipFormat"));  //$NON-NLS-1$
			salarySlip.save();
			
		}
			
		// and back to the installation defaults
		if (client.getTaxes().getVATArray().length == 0) {
			tax newVat = tax.getNewTax();
			newVat.save();

			tax noVat = new tax(Messages.getString("persistUtil.emptyVATname"), new BigDecimal(0)); //$NON-NLS-1$
			noVat.silentSave();

			tax reducedVat = new tax(client.getTaxes(), 0, Messages.getString("persistUtil.reducedVATname"), //$NON-NLS-1$
					new BigDecimal("0.07")); //$NON-NLS-1$

			reducedVat.setCreditTaxField(66);
			reducedVat.setDebitTaxField(81);
			reducedVat.silentSave();

			tax fullVat = new tax(client.getTaxes(), 0, Messages.getString("persistUtil.fullVATname"), //$NON-NLS-1$
					new BigDecimal("0.19")); //$NON-NLS-1$
			fullVat.setCreditTaxField(66);
			fullVat.setDebitTaxField(81);
			fullVat.setAsDefaultIncomingTax();
			fullVat.save();

		}
		client.getContacts().getContactsFromDB();
		if (client.getContacts().getCount() == 0) {
			contact newC = contact.getNewContact();
			newC.save();
			contact defC = contact.getInstallationDefault();
			defC.save();
			client.getContacts().getContactsFromDB();
		}
		client.getProducts().getProductsFromDB();
		if (client.getProducts().getCount() == 0) {
			product newP = product.getNewProduct();
			newP.save();
			product defP = product.getInstallationDefault();
			defP.save();
		}
		if (client.getAssets().getAssets(true).size() == 0) {
			asset newAs = asset.getNewAsset();
			newAs.save();
			client.getAssets().getAssetsFromDB();
		}

		if (application.getUsers().getCount() == 0) {
			appUser defaultUser = appUser.getDefaultUser();
			defaultUser.save();
			appUser newUser = appUser.getNewUser();
			newUser.save();
		}

		client.getAccounts().getAccountsFromDatabase();
		client.getAssets().getAssetsFromDB();
		client.getContacts().getContactsFromDB();
		client.getDocuments().getDocumentsFromDatabase();
		client.getEntries().getEntriesFromDatabase();
		client.getInventories().getInventoriesFromDB();
		client.getTaxes().getTaxesFromDatabase();
		client.getTransactions().getTransactionTypesFromDB();

	}

	public static void shutdown() {
		// close JPA
		EntityManager mgr = DB.getEntityManager();
		if (mgr != null) { mgr.close(); }
		// now close JDBC
		try {
			Connection conn = DB.getConnection();
			if (conn == null) { conn.close(); }
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}