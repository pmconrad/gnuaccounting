package appLayer;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import dataLayer.DB;

@Entity
public class inventoryItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private inventory parent;
	private String name;
	@Column(precision = 16, scale = 6)
	private BigDecimal value;
	@Column(precision = 16, scale = 6)
	private BigDecimal quantity;
	// @OneToOne
	// persistence1
	@Transient
	private appUser created;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	public inventoryItem() {

	}

	public void setParent(inventory parent) {
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getTotal() {
		return quantity.multiply(value);
	}

	public void setCreated(appUser creator) {
		this.created = creator;
	}

	public void save() {
		// parent.save();

		DB.getEntityManager().getTransaction().begin();
		DB.getEntityManager().persist(this);
		DB.getEntityManager().getTransaction().commit();
	}

}
