package appLayer.transactionRelated;

import java.util.HashMap;

import javax.persistence.Entity;

import appLayer.CashFlow;
import appLayer.Messages;

@Entity
public class offer extends appTransaction {
	String paymentMethod;
	int statusID = 0;
	static String[] statusArr = {
			Messages.getString("offer.processed"), Messages.getString("offer.pending"), Messages.getString("offer.processing"), Messages.getString("offer.shipped") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	String orderID;

	public offer() {
	}

	public offer(transactions parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getTransactionName() {
		return "offer"; //$NON-NLS-1$

	}

	public static int getType() {
		// due to GUILayer/newTransactionSelectTransactionDetails this code must
		// be
		// the index defined in
		// applayer/transactionRelatzed/transactions.java:init()+1
		return 5;
	}

	@Override
	public int getNumWorkflowSteps() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public HashMap<Integer, String> getTodoItems() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CashFlow getCashFlow() {
		return CashFlow.UNDEFINED;
	}

	@Override
	public boolean isVATRequiredInStep(int step) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createEntriesForWorkflowStep(int step) {
		// TODO Auto-generated method stub

	}

	public void setStatus(int newStatus) {
		statusID = newStatus;
	}

	public int getStatus() {
		return statusID;
	}

	public static String getAStatusAsString(int status) {
		return statusArr[status];
	}

	public String getStatusString() {
		return statusArr[statusID];
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getOrderID() {
		return orderID;
	}

	public boolean isPending() {
		return statusID == 1;
	}

	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}

}
