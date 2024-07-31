package appLayer;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import dataLayer.DB;

/**
 * Products are all available goods, that is all services, trade goods and
 * products
 */
public class products {
	private Vector<product> vecProducts = new Vector<product>();
	private static HashMap<String, String> unitCodes = new HashMap<String, String>();
	public static String types[]={Messages.getString("products.services"),Messages.getString("products.goods"),Messages.getString("products.products"),Messages.getString("products.raw"),Messages.getString("products.unfinished")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

	public products() {
		// codes from UNECE recommendation 20 to match BMEcat for opentrans
		unitCodes.put("C62", Messages.getString("products.unitPiece")); //$NON-NLS-1$ //$NON-NLS-2$
		unitCodes.put("HUR", Messages.getString("products.unitHour")); //$NON-NLS-1$ //$NON-NLS-2$
		unitCodes.put("DAY", Messages.getString("products.unitDay")); //$NON-NLS-1$ //$NON-NLS-2$
		unitCodes.put("KGM", Messages.getString("products.unitKG")); //$NON-NLS-1$ //$NON-NLS-2$
		unitCodes.put("LTR", Messages.getString("products.unitLitre")); //$NON-NLS-1$ //$NON-NLS-2$
		unitCodes.put("MTR", Messages.getString("products.unitMetre")); //$NON-NLS-1$ //$NON-NLS-2$
		unitCodes.put("MTK", Messages.getString("products.unitSquareMetre")); //$NON-NLS-1$ //$NON-NLS-2$

		unitCodes.put("HAR", Messages.getString("products.hectare"));  //$NON-NLS-1$ //$NON-NLS-2$
		unitCodes.put("KTM", Messages.getString("products.kilometre"));  //$NON-NLS-1$ //$NON-NLS-2$
		unitCodes.put("KWH", Messages.getString("products.kilowatthour"));  //$NON-NLS-1$ //$NON-NLS-2$
		unitCodes.put("LS", Messages.getString("products.lumpsum"));  //$NON-NLS-1$ //$NON-NLS-2$
		unitCodes.put("MIN", Messages.getString("products.minute"));  //$NON-NLS-1$ //$NON-NLS-2$
		unitCodes.put("MMK", Messages.getString("products.squaremillimetre"));  //$NON-NLS-1$ //$NON-NLS-2$
		unitCodes.put("MMT", Messages.getString("products.millimetre"));  //$NON-NLS-1$ //$NON-NLS-2$
		unitCodes.put("MTQ", Messages.getString("products.cubicmetre"));  //$NON-NLS-1$ //$NON-NLS-2$
		unitCodes.put("NAR", Messages.getString("products.numberArticles"));  //$NON-NLS-1$ //$NON-NLS-2$
		unitCodes.put("NPR", Messages.getString("products.numberPairs"));  //$NON-NLS-1$ //$NON-NLS-2$
		unitCodes.put("P1", Messages.getString("products.percent"));  //$NON-NLS-1$ //$NON-NLS-2$
		unitCodes.put("SET", Messages.getString("products.set"));  //$NON-NLS-1$ //$NON-NLS-2$
		unitCodes.put("TNE", Messages.getString("products.mettricton"));  //$NON-NLS-1$ //$NON-NLS-2$
		unitCodes.put("WEE", Messages.getString("products.week"));  //$NON-NLS-1$ //$NON-NLS-2$
	
	}

	public void getProductsFromDB() {
		vecProducts.clear();
		List retrievals = DB
				.getEntityManager()
				.createQuery("SELECT p FROM product p WHERE p.outdated IS NULL").getResultList(); //$NON-NLS-1$
		for (Iterator iter = retrievals.iterator(); iter.hasNext();) {
			product currentlyRetrieved = (product) iter.next();
			currentlyRetrieved.setParent(this);
			// currentlyRetrieved.updateNominalPrice();
			vecProducts.add(currentlyRetrieved);
		}
		sort();

	}

	public String getUnitName(String code) {
		return unitCodes.get(code);
	}

	public String getUnitCode(String name) {
		for (String currentCode : unitCodes.keySet()) {
			if (unitCodes.get(currentCode).equals(name)) {
				return currentCode;
			}
		}
		return null;
	}

	public String[] getUnitNames() {
		String[] res = new String[unitCodes.size()];
		int codeIndex = 0;
		for (String unitcode : unitCodes.values()) {
			res[codeIndex] = unitcode;
			codeIndex++;
		}
		return res;
	}

	public HashMap<String, String> getUnitCodes() {
		return unitCodes;
	}

	/**
	 * sort by name (new product at top) and build string array
	 * */
	private void sort() {
		if (vecProducts.size() > 0) {
			Collections.sort(vecProducts, new Comparator<product>() {
				public int compare(product s1, product s2) {
					// "new product" always at top

					if (s1.getID() == product.newProductID)
						return -1;
					if (s2.getID() == product.newProductID)
						return +1;
					// otherwise sort by name
					return s1.getName().compareToIgnoreCase(s2.getName());
				}
			});
		}
	}

	public boolean containsInstallationDefault() {
		for (product currentProduct : vecProducts) {
			if (currentProduct.getName().equals(
					product.getInstallationDefault().getName())) {
				return true;
			}
		}
		return false;
	}

	public Vector<product> getProducts() {
		if (vecProducts.size() == 0) {
			getProductsFromDB();
		}
		return vecProducts;
	}

	public Vector<product> getProductsWithoutNew() {
		if (vecProducts.size() == 0) {
			getProductsFromDB();
		}
		return new Vector(vecProducts.subList(1, vecProducts.size()));
	}

	public product getDefaultProduct() {
		return (product) vecProducts.elementAt(1);
	}

	public product getProduct(int id) {
		for (product p : vecProducts) {
			if (p.id == id) {
				return p;
			}

		}
		return null;

	}

	public void signalChange(product changed) {
		refresh();
	}

	public void refresh() {
		getProductsFromDB();

	}

	public int getCount() {
		return vecProducts.size();
	}

	public void add(product selectedProduct) {
		vecProducts.add(selectedProduct);

	}

	public product getExistingProductByDetails(String name, String description,
			BigDecimal price) {
		if (name.length() == 0) {
			return null;
		}

		for (product currentProduct : vecProducts) {
			if (currentProduct.getName().equals(name)
					&& currentProduct.getDescription().equals(description)
					&& currentProduct.getPrice().equals(price)
					&& !currentProduct.getIsDeleted()) {
				return currentProduct;
			}
		}

		return null;
	}

	public product findFirstProductByEAN(String ean) {
		if (ean.length() == 0) {
			return null;
		}

		for (product currentProduct : vecProducts) {
			if (currentProduct.getBarcode().equals(ean)
					&& !currentProduct.getIsDeleted()) {
				return currentProduct;
			}
		}

		return null;
	}

}
