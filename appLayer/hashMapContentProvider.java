package appLayer;

import java.util.HashMap;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class hashMapContentProvider implements IStructuredContentProvider {

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object[] getElements(Object arg0) {
		HashMap input = (HashMap) arg0;
		String[] res = new String[input.size()];
		int index = 0;
		for (Object current : input.values()) {
			res[index] = (String) current;
			index++;
		}
		return res;
	}

}
