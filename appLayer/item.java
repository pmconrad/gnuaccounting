package appLayer;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Vector;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.mustangproject.ZUGFeRD.IZUGFeRDAllowanceCharge;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableItem;

import appLayer.transactionRelated.appTransaction;
import dataLayer.DB;

@Entity
public class item implements IZUGFeRDExportableItem {

	// private int productIndex = -1;
	private String remarks = new String();
	private appTransaction parent;
	@Column(precision = 16, scale = 6)
	private BigDecimal quantity = new BigDecimal(1);
	@Column(precision = 16, scale = 6)
	private BigDecimal price = new BigDecimal(0);
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private static String[] columnNames = {
			Messages.getString("item.quantity"), Messages.getString("item.article"), Messages.getString("item.description"), Messages.getString("item.price"), Messages.getString("item.total") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	@Transient
	private int idOnPage;
	@Transient
	private Vector<IItemListener> itemListeners = new Vector<IItemListener>();
	private product currentProduct = null;

	@Deprecated
	/**
	 * use appTransaction's factory addItem
	 * */
	public item() {

	}

	public void prepare() {
		setProduct(client.getProducts().getDefaultProduct());

	}

	public String getOTString(int lineID) {

		String prodNameXML = utils.quoteForXML(getProduct().getName());
		String prodDescrXML = utils.quoteForXML(getProduct().getDescription());// <item
																				// quantity='"+getQuantity()+"'
																				// name='"+prodNameXML+"'
																				// currency='EUR'
																				// price='"+getPrice()+"'
																				// description='"+descrXML+"'
																				// total='"+getTotal()+"'
																				// totalgross='"+getTotalGross()+"'
																				// vatfactor='"+getProduct().getVAT().getFactor()+"'>
		BigDecimal vatAmount = getTotalGross().subtract(getTotal());
		return "\n\t\t<INVOICE_ITEM>\n" + //$NON-NLS-1$
				"\t\t\t<LINE_ITEM_ID>" //$NON-NLS-1$
				+ lineID
				+ "</LINE_ITEM_ID>\n" + //$NON-NLS-1$ //$NON-NLS-2$
				"\t\t\t<PRODUCT_ID>\n" //$NON-NLS-1$
				+ //$NON-NLS-1$
				"\t\t\t\t<bmecat:SUPPLIER_PID>" //$NON-NLS-1$
				+ getID()
				+ "</bmecat:SUPPLIER_PID>\n" + //$NON-NLS-1$ //$NON-NLS-2$
				"\t\t\t\t<bmecat:DESCRIPTION_SHORT>" //$NON-NLS-1$
				+ prodNameXML
				+ "</bmecat:DESCRIPTION_SHORT>\n" + //$NON-NLS-1$ //$NON-NLS-2$
				"\t\t\t\t<bmecat:DESCRIPTION_LONG>" //$NON-NLS-1$
				+ prodDescrXML
				+ "</bmecat:DESCRIPTION_LONG>\n" + //$NON-NLS-1$ //$NON-NLS-2$
				"\t\t\t</PRODUCT_ID>\n" //$NON-NLS-1$
				+ //$NON-NLS-1$
				"\t\t\t<QUANTITY>" //$NON-NLS-1$
				+ getQuantity()
				+ "</QUANTITY>\n" + //$NON-NLS-1$ //$NON-NLS-2$
				"\t\t\t<bmecat:ORDER_UNIT>" //$NON-NLS-1$
				+ getProduct().getUnit()
				+ "</bmecat:ORDER_UNIT>\n" + //$NON-NLS-1$ //$NON-NLS-2$
				"\t\t\t<PRODUCT_PRICE_FIX>\n" //$NON-NLS-1$
				+ //$NON-NLS-1$
				"\t\t\t<bmecat:PRICE_AMOUNT>" //$NON-NLS-1$
				+ getPrice()
				+ "</bmecat:PRICE_AMOUNT>\n" + //$NON-NLS-1$ //$NON-NLS-2$
				"\t\t\t<TAX_DETAILS_FIX>\n" //$NON-NLS-1$
				+ //$NON-NLS-1$
				"\t\t\t\t<bmecat:TAX_CATEGORY>standard_rate</bmecat:TAX_CATEGORY>\n" //$NON-NLS-1$
				+ //$NON-NLS-1$
				"\t\t\t\t<bmecat:TAX_TYPE>vat</bmecat:TAX_TYPE>\n" //$NON-NLS-1$
				+ //$NON-NLS-1$
				"\t\t\t\t<bmecat:TAX>" + getProduct().getVAT().getFactor() //$NON-NLS-1$
				+ "</bmecat:TAX>\n" + //$NON-NLS-1$ //$NON-NLS-2$
				"\t\t\t\t<TAX_AMOUNT>" + vatAmount + "</TAX_AMOUNT>\n" + //$NON-NLS-1$ //$NON-NLS-2$
				"\t\t\t</TAX_DETAILS_FIX>\n" //$NON-NLS-1$
				+ //$NON-NLS-1$
				"\t\t\t</PRODUCT_PRICE_FIX>\n" //$NON-NLS-1$
				+ //$NON-NLS-1$
				"\t\t\t<PRICE_LINE_AMOUNT>" + getTotalGross() //$NON-NLS-1$
				+ "</PRICE_LINE_AMOUNT>\n" + //$NON-NLS-1$ //$NON-NLS-2$
				"\t\t</INVOICE_ITEM>\n"; //$NON-NLS-1$
	}


	public String getDocTagString(int lineID) {
		BigDecimal vatAmount = getTotalGross().subtract(getTotal());
		return "\n\t" //$NON-NLS-1$
				+ "{" //$NON-NLS-1$
				+ "\"title\": \""+getProduct().getName()+"\"," //$NON-NLS-1$ //$NON-NLS-2$
				+ "\"description\": \""+getProduct().getDescription()+"\"," //$NON-NLS-1$ //$NON-NLS-2$
				+ "\"unit\": \""+getProduct().getUnit()+"\"," //$NON-NLS-1$ //$NON-NLS-2$
				+ "\"quantity\": "+getQuantity()+"," //$NON-NLS-1$ //$NON-NLS-2$
				+ "\"unit_price\": {" //$NON-NLS-1$
				+ "\"net\": "+utils.currencyFormat(getPrice(),'.')+"," //$NON-NLS-1$ //$NON-NLS-2$
				+ "\"gross\": "+utils.currencyFormat(getPriceGross(),'.')+"" //$NON-NLS-1$ //$NON-NLS-2$
				+ "}," //$NON-NLS-1$
				+ "\"total\": {" //$NON-NLS-1$
				+ "\"net\": "+utils.currencyFormat(getTotal(), '.')+"," //$NON-NLS-1$ //$NON-NLS-2$
				+ "\"gross\": "+utils.currencyFormat(getTotalGross(), '.')+"" //$NON-NLS-1$ //$NON-NLS-2$
				+ "}," //$NON-NLS-1$
				+ "\"taxes\": [" //$NON-NLS-1$
				+ "{" //$NON-NLS-1$
				+ "\"name\": \"MwSt\"," //$NON-NLS-1$
				+ "\"rate\": "+utils.round(getProduct().getVAT().getValue().multiply(new BigDecimal(100)),1) +"," //$NON-NLS-1$ //$NON-NLS-2$
				+ "\"amount\": "+vatAmount+"" //$NON-NLS-1$ //$NON-NLS-2$
				+ "}" //$NON-NLS-1$
				+ "]" //$NON-NLS-1$
				+ "}"; //$NON-NLS-1$
			
	}

	public boolean isEmpty() {
		return currentProduct == null
				|| currentProduct == client.getProducts().getDefaultProduct();
	}

	public void addListener(IItemListener listener) {
		itemListeners.add(listener);
	}

	public static String[] getColumnNames() {
		return columnNames;
	}

	public int getColumnType(int colIndex) {
		int res = 1;
		switch (colIndex) {
		case 1:
			res = 2;
			break;// article, combo with products
		}
		return res;
	}

	public void removeItem() {
		parent.removeItem(this);
	}

	public Object getColumn(int colIndex) {
		Object res = null;
		switch (colIndex) {
		case 0:
			res = getQuantity().toString();
			break;
		case 1:
			res = currentProduct;
			break;
		case 2:
			res = remarks;
			break;
		case 3:
			res = getPrice().toString();
			break;
		case 4:
			res = getTotal().toString();
			break;

		}
		return res;

	}

	public void setColumn(int colIndex, Object value) {
		switch (colIndex) {
		case 0:
			setQuantity(utils.String2BD((String) value));
			break;
		case 1:
			if (value instanceof product) {
				setProduct((product) value);
				
			} else {
				product p=client.getProducts().findFirstProductByEAN((String) value);
				if (p!=null) {
					setProduct(p);
				}
			}
			 
			break;
		case 2:
			remarks = (String) value;
			break;
		case 3:
			price = utils.String2BD((String) value);
			break;
		case 4:
			;
			break; // total column is not editable

		}
		// qty, article, or price change should recalculate total.
		// We don't need to recalc because getColumn anyway returns qty*price

	}

	public String getColumnString(int colIndex) {

		// we start with string-only editors
		if (colIndex == 1) {
			if (currentProduct == null) {
				return ""; //$NON-NLS-1$
			}
			return currentProduct.getName();

		} else {
			return (String) getColumn(colIndex);

		}

	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getRemarks() {
		if (this.remarks == null) {
			return new String(""); //$NON-NLS-1$
		} else {
			return this.remarks;
		}
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getQuantity() {
		return this.quantity;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public BigDecimal getPrice() {
		return this.price;
	}

	public String getPriceString() {
		NumberFormat form = NumberFormat.getCurrencyInstance();
		return form.format(getPrice());
	}

	/*
	 * public BigDecimal getNominalPrice() { if (currentProduct==null) { return
	 * new BigDecimal(0); } else { return currentProduct.getNominalPrice(); } }
	 */

	public void setProduct(product currentProduct) {
		// this.productIndex=productIndex;
		// set item name
		this.currentProduct = currentProduct;
		setPrice(currentProduct.getPrice());
		for (IItemListener currentListener : itemListeners) {
			currentListener.onProductChange(currentProduct);
		}

	}

	/*
	 * loads a product with the specified id from the database
	 */
	public void setProduct(int id) {
		currentProduct = client.getProducts().getProduct(id);

	}

	public BigDecimal getTotal() {
		return quantity.multiply(price);
	}

	public String getTotalString() {
		NumberFormat form = NumberFormat.getCurrencyInstance();
		return form.format(getTotal());
	}

	public BigDecimal getPriceGross() {
		BigDecimal multiplicant=new BigDecimal(1).add(currentProduct.getVAT().getValue());
		if (configs.hasSalesTax()&&!parent.isTaxExempt()) {
			multiplicant=multiplicant.add(currentProduct.getSalesTax().getValue());
		}
		
		return price.multiply(multiplicant);
	}

	public String getPriceGrossString() {
		NumberFormat form = NumberFormat.getCurrencyInstance();
		return form.format(getPriceGross());
	}

	public product getProduct() {

		return currentProduct;
	}

	public BigDecimal getTotalGross() {
		if (currentProduct == null) { 
			return new BigDecimal(0);
		} else {
			return quantity.multiply(getPriceGross()); // remove when BD migration complete

		}

	}

	public String getTotalGrossString() {
		NumberFormat form = NumberFormat.getCurrencyInstance();
		return form.format(getTotalGross());
	}

	public void save() {
		DB.getEntityManager().getTransaction().begin();
		DB.getEntityManager().persist(this);
		DB.getEntityManager().getTransaction().commit();

	}

	public int getID() {
		return id;
	}

	public void setIDonPage(int idOnPage) {
		this.idOnPage = idOnPage;
	}

	public int getIDonPage() {
		return idOnPage;
	}

	public void setParent(appTransaction parent) {
		this.parent = parent;
	}

	public appTransaction getParent() {
		return parent;
	}

	/**
	 * This is rather a copy than a clone functionality and could be required by
	 * an interface like Icopyable
	 */
	public void cloneFrom(item source) {
		setQuantity(source.getQuantity());
		setProduct(source.getProduct());
		setPrice(source.getPrice());
		setRemarks(source.getRemarks());
	}

	@Override
	public IZUGFeRDAllowanceCharge[] getItemAllowances() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IZUGFeRDAllowanceCharge[] getItemCharges() {
		// TODO Auto-generated method stub
		return null;
	}

}
