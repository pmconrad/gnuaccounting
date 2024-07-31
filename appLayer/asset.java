package appLayer;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import dataLayer.DB;

@Entity
public class asset {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected int id;

	@Transient
	static String[] stati = {
			Messages.getString("asset.statusOnStock"), Messages.getString("asset.statusReady"), Messages.getString("asset.statusInUse"), Messages.getString("asset.statusLost"), Messages.getString("asset.statusSold"), Messages.getString("asset.statusDefect"), Messages.getString("asset.statusDecommissioned") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$

	protected int account_entry_id;
	protected String name, depreciationType;
	protected String remark, number, location;

	@Temporal(value = TemporalType.DATE)
	protected Date depreciationStart;
	@Temporal(value = TemporalType.DATE)
	protected Date depreciationEnd;
	@Temporal(value = TemporalType.DATE)
	protected Date removal;
	@Column(precision = 16, scale = 6)
	protected BigDecimal revenueIfSold;
	protected int status;
	protected String removalReason;

	protected int lifetime;

	@Column(precision = 16, scale = 6)
	protected BigDecimal value;
	@Column(precision = 16, scale = 6)
	protected BigDecimal deprecatedValue;
	@Temporal(value = TemporalType.DATE)
	protected Date outdated;

	public static int newAssetID = 1;

	@Transient
	protected assets parent;

	public asset() {
		setName(""); //$NON-NLS-1$
		setStatus(0);
		setValue(new BigDecimal(0));
		setDepreciationType("linear"); //$NON-NLS-1$
		GregorianCalendar cal = new GregorianCalendar();
		setNumber(""); //$NON-NLS-1$
		setRemark(""); //$NON-NLS-1$
		setLocation(""); //$NON-NLS-1$
		setDepreciationStart(cal.getTime());
		setDepreciationEnd(cal.getTime());

	}

	public static int getStatusForString(String s)
			throws elementNotFoundException {
		for (int currentKey = 0; currentKey < stati.length; currentKey++) {
			if (stati[currentKey].equals(s)) {
				return currentKey;
			}
		}
		throw new elementNotFoundException(
				Messages.getString("asset.exceptionStatusNotFound")); //$NON-NLS-1$
	}

	public static String getStringForStatus(int status)
			throws elementNotFoundException {
		if ((status < 0) || (status > stati.length)) {
			throw new elementNotFoundException(
					Messages.getString("asset.exceptionStatusNotFound")); //$NON-NLS-1$

		}
		return stati[status];
	}

	public static String[] getStati() {
		return stati;
	}

	public asset(assets parent, int id, int account_entry_id, String name,
			BigDecimal value, Date depreciationStart, Date depreciationEnd,
			String depreciationType) {
		this.parent = parent;
		this.id = id;
		this.account_entry_id = account_entry_id;
		setName(name);
		setValue(value);
		setDepreciationType(depreciationType);
		setDepreciationStart(depreciationStart);

	}

	public void setParent(assets parent) {
		this.parent = parent;
	}

	public int getID() {
		return id;
	}

	public int getAccount_entry_id() {
		return account_entry_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDepreciationType() {
		return depreciationType;
	}

	public void setDepreciationType(String depreciationType) {
		this.depreciationType = depreciationType;
	}

	public Date getDepreciationStart() {
		return depreciationStart;
	}

	public void setDepreciationStart(Date depreciationStart) {
		this.depreciationStart = depreciationStart;
	}

	public Date getDepreciationEnd() {
		return depreciationEnd;
	}

	public void setDepreciationEnd(Date depreciationEnd) {
		this.depreciationEnd = depreciationEnd;
	}

	public void save() {
		DB.getEntityManager().getTransaction().begin();
		DB.getEntityManager().persist(this);
		DB.getEntityManager().getTransaction().commit();
		if (parent != null) {
			parent.signalChange(this);
		}

	}

	public void delete() {
		if (getID() > 0) {
			Calendar c = Calendar.getInstance();
			outdated = c.getTime();
			save();
			parent.signalChange(this);
		}
	}

	@Override
	public String toString() {

		return name;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public static asset getNewAsset() {
		asset newAs = new asset();
		newAs.setName(Messages.getString("asset.newAsset")); //$NON-NLS-1$		
		return newAs;
	}

	public int getLifetime() {
		return lifetime;
	}

	public void setLifetime(int lifetime) {
		this.lifetime = lifetime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getRemark() {
		if (remark == null) {
			return "";// was missing when data was upgrade from 0.8.0 to 0.8.1 and caused a nullpointerexception #upgrade //$NON-NLS-1$
		}
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getNumber() {
		if (number == null) {
			return "";// was missing when data was upgrade from 0.8.0 to 0.8.1 and caused a nullpointerexception #upgrade //$NON-NLS-1$
		}
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getLocation() {
		if (location == null) {
			return "";// was missing when data was upgrade from 0.8.0 to 0.8.1 and caused a nullpointerexception #upgrade //$NON-NLS-1$
		}
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public boolean isPlaceholderForNewAsset() {
		return name.equals(Messages.getString("asset.newAsset")); //$NON-NLS-1$
	}

}
