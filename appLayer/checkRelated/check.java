package appLayer.checkRelated;

/* tests a config setting */
abstract public class check {
	private String testDescription;
	protected IcheckInteractionProvider outputProvider = null;
	private checkResult tr;

	public checkResult getResult() {
		return tr;
	}

	public check(IcheckInteractionProvider outputProvider,
			String testDescription) {
		this.testDescription = testDescription;
		this.outputProvider = outputProvider;
	}

	abstract public checkResult doTest();

	public void execute() {
		tr = doTest();

		this.outputProvider
				.output(testDescription + ":" + tr.getResultString()); //$NON-NLS-1$
	}

	public String getDescription() {
		return testDescription;
	}
}

class testTest extends check {
	public testTest(IcheckInteractionProvider output, String testDescription) {
		super(output, testDescription);
	}

	public checkResult doTest() {
		return new standardCheck();
	}
}
