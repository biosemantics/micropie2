package semanticMarkup.ling.learn.auxiliary;

import java.util.Set;

public class KnownTagCollection {
	
	public Set<String> nouns;
	public Set<String> organs;
	public Set<String> modifiers;
	public Set<String> boundaryWords;
	public Set<String> boundaryMarks;
	public Set<String> properNouns;
	
	public KnownTagCollection(Set<String> nouns, Set<String> organs,
			Set<String> modifiers, Set<String> boundaryWords,
			Set<String> boundaryMarks, Set<String> properNouns) {
		this.nouns = nouns;
		this.organs = organs;
		this.modifiers = modifiers;
		this.boundaryWords = boundaryWords;
		this.boundaryMarks = boundaryMarks;
		this.properNouns = properNouns;
	}

}
