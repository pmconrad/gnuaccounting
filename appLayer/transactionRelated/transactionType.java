package appLayer.transactionRelated;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import dataLayer.DB;

@Entity
public class transactionType {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int typeID;
	private int typeIndex, typePeriod;
	@Transient
	private int listIndex;
	private String typeName, typePrefix, typeFormat;
	private boolean allowUnreferencedCreation = true; /*
													 * some types, e.g. reminder
													 * or cancellation, can only
													 * be created if they refer
													 * to an existing
													 * transaction
													 */

	public transactionType() {

	}

	public transactionType(int typeIndex, int typePeriod, String typeName,
			String typePrefix, String typeFormat) {
		this.typeIndex = typeIndex;
		this.typePeriod = typePeriod;
		this.typeName = typeName;
		this.typePrefix = typePrefix;
		this.typeFormat = typeFormat;
	}

	public String toString() {
		return typeName;
	}

	public int getTypeID() {
		return typeID;
	}

	/**
	 * gettypeIndex is the number from which the transaction numbers will start,
	 * e.g. if it is 235 the next transaction will be 236. This has nothing to
	 * do with getType and getTypeID which get the identification number of the
	 * type (refactor here)
	 * 
	 */

	public int getTypeIndex() {
		return typeIndex;
	}

	public int getTypePeriod() {
		return typePeriod;
	}

	public String getTypeName() {
		return typeName;
	}

	public String getTypePrefix() {
		return typePrefix;
	}

	public String getTypeFormat() {
		return typeFormat;
	}

	public void setTypeIndex(int typeIndex) {
		this.typeIndex = typeIndex;
	}

	public void setTypePeriod(int typePeriod) {
		this.typePeriod = typePeriod;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public void setTypePrefix(String typePrefix) {
		this.typePrefix = typePrefix;
	}

	public static int getNumTypes() {
		List retrievals = DB.getEntityManager()
				.createQuery("SELECT t FROM transactionType t").getResultList(); //$NON-NLS-1$
		return retrievals.size();
	}

	public void setTypeFormat(String typeFormat) {
		this.typeFormat = typeFormat;
	}

	public void save() {

		DB.getEntityManager().getTransaction().begin();
		DB.getEntityManager().persist(this);
		DB.getEntityManager().getTransaction().commit();

	}

	/**
	 * some types, e.g. reminder or cancellation, can only be created if they
	 * refer to an existing transaction
	 */
	public boolean isAllowUnreferencedCreation() {
		return allowUnreferencedCreation;
	}

	public void setAllowUnreferencedCreation(boolean allowUnreferencedCreation) {
		this.allowUnreferencedCreation = allowUnreferencedCreation;
	}

	public void setListIndex(int listIndex) {
		this.listIndex = listIndex;
	}

	public int getListIndex() {
		return listIndex;
	}

}
