package appLayer;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.mustangproject.ZUGFeRD.IZUGFeRDExportableProduct;

import appLayer.taxRelated.tax;
import dataLayer.DB;

@Entity
public class product implements IZUGFeRDExportableProduct {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected int id;
	protected String name;
	protected String unit = "C62"; //$NON-NLS-1$
	protected String description = new String();
	protected String barcode;
	int type;
	@Column(precision = 16, scale = 6)
	protected BigDecimal price = new BigDecimal(0);
	/*
	 * @Column(precision=16,scale=6) protected BigDecimal nominalPrice=new
	 * BigDecimal(0);
	 */
	protected tax vat;
	protected tax salesTax; /*canadian provincial VAT*/
	protected Timestamp outdated = null;
	@Transient
	protected products parent;
	private static product installationDefaultProduct = null;
	public static int newProductID = 1;

	// private items parent;
	public product() {
		

	}

	public product(products parent, int id, tax vat, String name,
			BigDecimal price) {
		this.parent = parent;
		this.id = id;
		this.name = name;
		this.price = price;
		// updateNominalPrice();
		this.vat = vat;

	}

	public product(tax vat, String name, BigDecimal price) {
		this.name = name;
		this.price = price;
		// updateNominalPrice();
		this.vat = vat;

	}

	public static product getInstallationDefault() {
		if (installationDefaultProduct == null) {
			installationDefaultProduct = new product(
					null,
					0,
					client.getTaxes().getStandardVAT(),
					Messages.getString("configs.sampleProduct"), new BigDecimal(1)); //$NON-NLS-1$
		}
		return installationDefaultProduct;
	}

	public void setParent(products parent) {
		this.parent = parent;
	}

	public int getID() {
		return id;
	}

	public void setType(int newType) {
		type=newType;
	}

	public int getType() {
		return type;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public tax getVAT() {
		return vat;
	}

	public void setSalesTax(tax theSalesTax) {
		this.salesTax=theSalesTax;
	}
	
	public tax getSalesTax() {
		if (salesTax==null) { 
			return client.getTaxes().getEmpty();
		} else {
			return salesTax;	
		}
		
	}

	/*
	 * public BigDecimal getNominalPrice() { return nominalPrice; }
	 * 
	 * public void updateNominalPrice() { this.nominalPrice=price; }
	 */

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {

		return name;
	}

	public void delete() {
		Calendar c = Calendar.getInstance();

		outdated = new Timestamp(c.getTime().getTime());
		save();
		if (parent != null) {
			parent.signalChange(this);
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOutdated(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);

		outdated = new Timestamp(c.getTime().getTime());

	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public void setTax(tax newTax) {
		this.vat = newTax;
	}

	public void save() {
		DB.getEntityManager().getTransaction().begin();
		DB.getEntityManager().persist(this);
		DB.getEntityManager().getTransaction().commit();

		if (parent != null) {
			parent.signalChange(this);
		}

	}

	public static product getNewProduct() {

		return new product(client.getTaxes().getFirst(),
				Messages.getString("product.newProduct"), new BigDecimal(1)); //$NON-NLS-1$
	}

	public String getBarcode() {
		if (barcode == null) {
			return ""; //$NON-NLS-1$
		} else {
			return barcode;
		}
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	/**
	 * test, if it is marked as deleted.
	 * 
	 * @return true, if it has been deleted
	 */
	public boolean getIsDeleted() {
		return (outdated != null);
	}

	@Override
	public BigDecimal getVATPercent() {
		return getVAT().getValue().multiply(new BigDecimal(100));
	}

}
