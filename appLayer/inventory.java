package appLayer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import dataLayer.DB;

@Entity
public class inventory implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static enum inventoryType {
		cash;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private appUser created;
	@Temporal(value = TemporalType.DATE)
	private Date outdated = null;

	private inventoryType type;
	@Transient
	inventories parent;
	@OneToMany(mappedBy = "parent")
	private List<inventoryItem> items = new ArrayList<inventoryItem>();
	private boolean finished;

	public inventory() {

	}

	public void setParent(inventories parent) {
		this.parent = parent;
	}

	public void setType(inventoryType type) {
		this.type = type;
	}

	public inventoryItem getNewItem(String name, BigDecimal value,
			BigDecimal quantity) {
		inventoryItem i = new inventoryItem();
		i.setParent(this);
		i.setName(name);
		i.setValue(value);
		i.setQuantity(quantity);
		i.setCreated(application.getUsers().getActiveUser());
		items.add(i);
		return i;
	}

	public void save() {
		DB.getEntityManager().getTransaction().begin();
		DB.getEntityManager().persist(this);
		DB.getEntityManager().getTransaction().commit();
	}

	public void setFinished() {
		this.finished = true;
	}

	public boolean isFinished() {
		return this.finished;
	}

	public void setCreated(appUser creator) {
		this.created = creator;
	}

	public boolean wasCreatedBy(appUser toCompareWith) {
		return created.getUsername().equalsIgnoreCase(
				toCompareWith.getUsername());
	}

	public List<inventoryItem> getItems() {
		return items;
	}

	public void clear() {
		items.clear();
	}

}
