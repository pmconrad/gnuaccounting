package appLayer.taxRelated;

public class IRSoffice {
	private String id, name;

	public IRSoffice(String id, String name) {
		this.id = id;
		this.name = name;

	}

	public String getName() {
		return name;
	}

	public String toString() {
		return getName();
	}

	public String getID() {
		return id;
	}

}
