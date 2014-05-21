package semanticMarkup.core;

public class Treatment {

	private String description;
	private String fileName;
	
	public Treatment(String fn, String des){
		this.fileName = fn;
		this.description=des;
	}
	
	public String getDescription() {
		return this.description;
	}

	public String getFileName() {
		return this.fileName;
	}

}
