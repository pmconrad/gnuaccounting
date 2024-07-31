package appLayer;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.mustangproject.ZUGFeRD.IZUGFeRDExportableContact;

import dataLayer.DB;

@Entity
public class contact implements IZUGFeRDExportableContact{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id = -1;
	String name, co, additionalAddressLine, street, zip, location, country,
			email, phone, fax, VATID, bankaccount, bankcode, accountholder, bic, iban, SEPAmandate;

	boolean taxExempt;
	int role, paymentmethod /**0=invoice, 1=debit*/;
	private Timestamp outdated = null;
	@Transient
	private static contact installationDefault = null;
	public static int newContactID = 1;
	private String vcfId;
	@Temporal(value = TemporalType.DATE)
	private Date lastVcfChange;

	public contact() {
		prepare();
	}

	public contact(int id) {
		prepare();
		this.id = id;
	}

	public contact(String name, String co, String street,
			String additionalAddress, String zip, String location,
			String country, String email, String phone, String fax,
			String VATID, int role, String bankaccount, String bankcode,
			String accountholder, String bic, String iban, int paymentmethod) {
		client.getClient();
		this.name = name;
		this.co = co;
		this.street = street;
		this.additionalAddressLine = additionalAddress;
		this.zip = zip;
		this.location = location;
		this.country = country;
		this.email = email;
		this.phone = phone;
		this.fax = fax;
		this.VATID = VATID;
		this.role = role;
		this.bankaccount = bankaccount;
		this.bankcode = bankcode;
		this.accountholder = accountholder;
		this.bic = bic;
		this.iban = iban;
		this.paymentmethod = paymentmethod;
	}

	public contact(String name) {// also the constructor for the installation
									// default contact
		prepare();
		this.name = name;
	}

	private void prepare() {
		this.co = ""; //$NON-NLS-1$
		this.street = ""; //$NON-NLS-1$
		this.zip = ""; //$NON-NLS-1$
		this.location = ""; //$NON-NLS-1$
		this.country = ""; //$NON-NLS-1$
		this.email = ""; //$NON-NLS-1$
		this.phone = ""; //$NON-NLS-1$
		this.fax = ""; //$NON-NLS-1$
		this.VATID = ""; //$NON-NLS-1$
		this.bankaccount = ""; //$NON-NLS-1$
		this.bankcode = ""; //$NON-NLS-1$
		this.accountholder = ""; //$NON-NLS-1$
		this.additionalAddressLine = ""; //$NON-NLS-1$
		this.bic = ""; //$NON-NLS-1$
		this.iban = ""; //$NON-NLS-1$
		this.SEPAmandate = ""; //$NON-NLS-1$
	}

	public static contact getInstallationDefault() {
		if (installationDefault == null) {
			installationDefault = new contact();
			installationDefault.setName(Messages
					.getString("configs.sampleCustomer")); //$NON-NLS-1$

		}
		return installationDefault;
	}

	public String getNumber() {
		return Integer.toString(id);
	}

	public String getName() {
		return name;
	}

	public String getStreet() {
		return street;
	}

	public String getZIP() {
		return zip;
	}

	public String getLocation() {
		return location;
	}

	public String getCountry() {
		return country;
	}

	public String getCO() {
		return co;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public String getFax() {
		return fax;
	}

	public String getVATID() {
		return VATID;
	}

	public String getAdditionalAddressLine() {
		return additionalAddressLine;
	}

	public int getID() {
		return id;
	}

	public int getRole() {
		return role;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public void setZIP(String zip) {
		this.zip = zip;
	}

	public void setAdditionalAddressLine(String line2) {
		this.additionalAddressLine = line2;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setCO(String co) {
		this.co = co;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public void setRole(int role) {
		this.role = role;
	}

	public void setVATID(String VATID) {
		this.VATID = VATID;
	}

	@Override
	public String toString() {

		return name;
	}

	public void setOutdated(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);

		outdated = new Timestamp(c.getTime().getTime());

	}

	public void delete() {
		Calendar c = Calendar.getInstance();

		outdated = new Timestamp(c.getTime().getTime());
		save();

		client.getContacts().signalChange(this);
	}

	public static contact getNewContact() {
		return new contact(Messages.getString("contact.newContact")); //$NON-NLS-1$
	}

	public void save() {
		boolean added = false;
		if (id == -1) {
			added = true;
		}
		DB.getEntityManager().getTransaction().begin();
		DB.getEntityManager().persist(this);
		DB.getEntityManager().getTransaction().commit();

		if (added) {
			client.getContacts().signalAdd(this);
		} else {
			client.getContacts().signalChange(this);
		}

	}

	public String getBankAccount() {
		return bankaccount;
	}

	public void setBankaccount(String bankaccount) {
		this.bankaccount = bankaccount;
	}

	public String getBankCode() {
		return bankcode;
	}

	public void setBankcode(String bankcode) {
		this.bankcode = bankcode;
	}

	public String getAccountholder() {
		return accountholder;
	}

	public void setAccountholder(String accountholder) {
		this.accountholder = accountholder;
	}

	public int getPaymentmethod() {
		return paymentmethod;
	}

	public void setPaymentmethod(int paymentmethod) {
		this.paymentmethod = paymentmethod;
	}

	/**
	 * test, if it is marked as deleted.
	 * 
	 * @return true, if it has been deleted
	 */
	public boolean getIsDeleted() {
		return (outdated != null);
	}

	public Date getLastVcfChange() {
		return lastVcfChange;
	}

	public void setLastVcfChange(Date lastVcfChange) {
		this.lastVcfChange = lastVcfChange;
	}

	public String getVcfID() {
		return vcfId;
	}

	public void setVcfId(String vcfId) {
		this.vcfId = vcfId;
	}


	public String getBIC() {
		return bic;
	}

	public void setBIC(String bic) {
		this.bic = bic;
	}


	public String getIBAN() {
		return iban;
	}

	public void setIBAN(String iban) {
		this.iban = iban;
	}

	public String getSEPAmandate() {
		if (SEPAmandate==null) {
			// 0.8.5 upgrade related
			return "";			 //$NON-NLS-1$
		} else {
			return SEPAmandate;
		}
	}

	public void setSEPAmandate(String SEPAmandate) {
		this.SEPAmandate = SEPAmandate;
	}

	public void setTaxExempt(boolean selection) {
		this.taxExempt=selection;
	}

	public boolean isTaxExempt() {
		return taxExempt;
	}
}
