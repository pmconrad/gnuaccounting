package appLayer.checkRelated;

public class checkResult {
	private String resultString = null;
	private boolean result = false;
	private boolean isFatal = false;

	public checkResult(boolean result, String resultString) {
		this.result = result;
		this.resultString = resultString;
	}

	public boolean getResult() {
		return result;
	}

	public String getResultString() {
		return resultString;
	}

	public void setFatal() {
		isFatal = true;
	}

	public boolean getFatal() {
		return isFatal;
	}

}
