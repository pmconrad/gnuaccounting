package appLayer.taxRelated;

import java.util.Vector;

import appLayer.Messages;

public class states {
	private Vector<state> states = new Vector<state>();

	/**
	 * source for formats and tax codes:
	 * http://www.felfri.de/winston/schnittstellen.htm
	 */
	public states() {
		state currentState = new state(
				Messages.getString("states.notapplicable"), "", "0"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		states.add(currentState);
		currentState = new state(
				Messages.getString("states.bawue"), "FFBBB/UUUUP", "28"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		states.add(currentState);
		currentState = new state(
				Messages.getString("states.bayern"), "FFF/BBB/UUUUP", "91"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		currentState.addTaxCode("92"); //$NON-NLS-1$
		states.add(currentState);
		currentState = new state(
				Messages.getString("states.berlin"), "BBB/UUUUP", "11"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		states.add(currentState);
		currentState = new state(
				Messages.getString("states.brandenburg"), "FFF/BBB/UUUUP", "30"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		states.add(currentState);
		currentState = new state(
				Messages.getString("states.bremen"), "FFBBB UUUUP", "24"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		states.add(currentState);
		currentState = new state(
				Messages.getString("states.hamburg"), "FF/BBB/UUUUP", "22"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		states.add(currentState);
		currentState = new state(
				Messages.getString("states.hessen"), "0FF BBB UUUUP", "26"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		states.add(currentState);
		currentState = new state(
				Messages.getString("states.meckpom"), "FFF/BBB/UUUUP", "40"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		states.add(currentState);
		currentState = new state(
				Messages.getString("states.niedersachsen"), "FF/BBB/UUUUP", "23"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		states.add(currentState);
		currentState = new state(
				Messages.getString("states.nrw"), "FFF/BBBB/UUUP", "51"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		currentState.addTaxCode("52"); //$NON-NLS-1$
		currentState.addTaxCode("53"); //$NON-NLS-1$
		states.add(currentState);
		currentState = new state(
				Messages.getString("states.rlp"), "FF/BBB/UUUU/P", "27"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		states.add(currentState);
		currentState = new state(
				Messages.getString("states.saarland"), "FFF/BBB/UUUUP", "10"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		states.add(currentState);
		currentState = new state(
				Messages.getString("states.sachsen"), "FFF/BBB/UUUUP", "32"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		states.add(currentState);
		currentState = new state(
				Messages.getString("states.sachsenanhalt"), "FFF/BBB/UUUUP", "31"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		states.add(currentState);
		currentState = new state(
				Messages.getString("states.schleswig"), "FFBBB UUUUP", "21"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		states.add(currentState);
		currentState = new state(
				Messages.getString("states.thueringen"), "FFF/BBB/UUUUP", "41"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		states.add(currentState);
	}

	public state[] getStates() {
		state[] res = new state[states.size()];
		for (int i = 0; i < states.size(); i++) {
			res[i] = states.get(i);
		}
		return res;
	}

}
