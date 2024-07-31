package appLayer.transactionRelated;

import java.util.HashMap;

import javax.persistence.Entity;

import appLayer.CashFlow;

@Entity
public class reminder extends appTransaction {

	public reminder(transactions parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	public reminder() {
	}

	@Override
	public String getTransactionName() {

		return "reminder"; //$NON-NLS-1$
	}

	public static int getType() {
		// due to GUILayer/newTransactionSelectTransactionDetails this code must
		// be
		// the index defined in
		// applayer/transactionRelatzed/transactions.java:init()+1
		return 4;
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

}
