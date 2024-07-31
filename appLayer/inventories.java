package appLayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.OneToMany;

import appLayer.inventory.inventoryType;
import dataLayer.DB;

public class inventories implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@OneToMany(mappedBy = "parent")
	private List<inventory> inventoriesList = new ArrayList<inventory>();

	public inventory startInventory(inventoryType type) {
		inventory i = new inventory();
		i.setType(type);
		i.setCreated(application.getUsers().getActiveUser());
		i.setParent(this);
		inventoriesList.add(i);
		return i;
	}

	public void getInventoriesFromDB() {
		inventoriesList.clear();

		List retrievals = DB
				.getEntityManager()
				.createQuery(
						"SELECT i FROM inventory i WHERE i.outdated IS NULL").getResultList(); //$NON-NLS-1$   
		for (Iterator iter = retrievals.iterator(); iter.hasNext();) {
			inventory currentlyRetrieved = (inventory) iter.next();
			inventoriesList.add(currentlyRetrieved);
		}
	}

	public inventory getLastUnfinishedInventory(inventoryType cash) {

		getInventoriesFromDB();
		for (inventory currentInventory : inventoriesList) {
			if (!currentInventory.isFinished()
					&& currentInventory.wasCreatedBy(application.getUsers()
							.getActiveUser())) {
				return currentInventory;
			}
		}
		return null;
	}

}
