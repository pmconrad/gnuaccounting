package appLayer.checkRelated;

import GUILayer.configWindow;

public class configWindowDatabaseCheck implements IdatabaseCheckConductor {
	private configWindow cw = null;

	public configWindowDatabaseCheck(configWindow cw) {
		this.cw = cw;
	}

	@Override
	public String conduct() {
		return cw.connectDatabaseAndJPA();
	}

}
