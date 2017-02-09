package edu.arizona.biosemantics.micropie.io.xml;

public enum FileTypeEnum {	
	/*TAXON_DESCRIPTION("Taxon Description"),
	GLOSSARY("Glossary"),
	EULER("Euler");
    private String displayName;
    private FileType(String displayName) {
        this.displayName = displayName;
    }
    public String displayName() { 
    	return displayName; 
    }
    @Override 
    public String toString() { 
    	return displayName; 
    }*/
	
	TAXON_DESCRIPTION("Text Capture Input", true),
	MARKED_UP_TAXON_DESCRIPTION("Text Capture Output/Matrix Generation Input", true),
	MATRIX("Matrix Generation Output", true), 
	PLAIN_TEXT("Plain Text", true),
	DIRECTORY("Directory", false);
	
    private String displayName;
    private boolean viewable;

    private FileTypeEnum(String displayName, boolean viewable) {
        this.displayName = displayName;
        this.viewable = viewable;
    }

    public String displayName() { 
    	return displayName; 
    }
    
    public boolean isViewable() {
    	return viewable;
    }
    
    public static FileTypeEnum getEnum(String displayName) {
        if(displayName == null)
            throw new IllegalArgumentException();
        for(FileTypeEnum v : values())
            if(displayName.equals(v.displayName())) return v;
        throw new IllegalArgumentException();
    }	


}
