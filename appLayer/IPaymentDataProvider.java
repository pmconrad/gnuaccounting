package appLayer;

import java.math.BigDecimal;

public interface IPaymentDataProvider {
	public CashFlow getCashFlow();

	public String getBankAccount();

	public String getBankCode();

	public String getBankAccountHolder();

	public String getPaymentPurpose();

	public BigDecimal getPaymentAmount();

	public void setContact(contact c);

	public contact getContact();

}
