package appLayer;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import dataLayer.DB;

public class assets {
	Vector<asset> assets = new Vector<asset>();

	public void getAssetsFromDB() {
		assets.clear();

		List<asset> retrievals = DB
				.getEntityManager()
				.createQuery("SELECT a FROM asset a WHERE a.outdated IS NULL").getResultList(); //$NON-NLS-1$
		for (Iterator<asset> iter = retrievals.iterator(); iter.hasNext();) {
			asset currentlyRetrieved = (asset) iter.next();
			currentlyRetrieved.setParent(this);
			assets.add(currentlyRetrieved);
		}
		sort();

	}

	public String getNextNumber() {
		int highestNumber = 0;
		for (asset currentAsset : assets) {
			try {
				int currentAssetNr = 0;
				if (currentAsset.getNumber().length()>0) {
					currentAssetNr = Integer.parseInt(currentAsset.getNumber());
					if (currentAssetNr > highestNumber) {
						highestNumber = currentAssetNr;
					}
				}
			} catch (NumberFormatException nfe) {
				return ""; //$NON-NLS-1$
			}

		}
		return Integer.toString(highestNumber + 1);
	}

	public List<asset> getAssets(boolean includeNewAsset) {
		if (includeNewAsset) {
			return assets;
		} else {
			return assets.subList(1, assets.size());
		}

	}

	public void signalChange(asset changed) {
		getAssetsFromDB();
	}

	/**
	 * Sorts by ID ascending, with New Asset on top
	 * */
	private void sort() {
		int assetsToOmit = 1;

		if (assets.size() > assetsToOmit) {
			Collections.sort(assets, new Comparator<asset>() {
				public int compare(asset a1, asset a2) {
					// "new product" always at top

					if (a1.isPlaceholderForNewAsset())
						return -1;
					if (a2.isPlaceholderForNewAsset())
						return +1;
					// otherwise sort by code

					if (a1.getID() > a2.getID()) {
						return 1;
					} else {

						return -1;
					}
				}
			});

		}

	}

}
