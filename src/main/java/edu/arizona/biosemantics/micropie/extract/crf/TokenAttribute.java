package edu.arizona.biosemantics.micropie.extract.crf;


/**
 * types of token attributes
 * @author maojin
 *
 */
public enum TokenAttribute {
	TokenType,//type_of_token,
	Length,//the length in character
	POS,  //POS_TAG, _,
	Typographic, //typographic
	pPunct,  //presence of punctuation
	pDigit,  //presence of digits
	pCap,  //presence of capitals
	PhraseBIO, //phrase BIO format
	isHead, //is head of the noun phrase
	HeadId, //Head_Token_ID, 
	DepRole, //dependent_role
	subVerb, // the lemma of the verb for which the token acts as a subject
	objVerb, //the lemmas of verbs for which the token acts as an object
	modNoun,//the lemmas of nouns for which the token acts as modifiers
	modifier,//the lemmas of modifiers of that token.
	prepNoun, //the lemma of the noun for which the token acts as a preposition
	preposition, //the preposition of the token
	GeniaLabel, //Genia Tagger Label
	wordSense, //extended word sense
	NCBIID,//mapping to NCBI ontology, its class ID
	TaxonRank, //Taxon Hierachy, genus, species, no rank
	TaxonType, //Plan, Baceria....
	ISInSpecies, //whether it's in species results
	ISInLinnaerus, //whether it's in linnareus results
	InOntoBiotope, //presence in OntoBiotope
	InNCBITaxonomy,//presence in NCBITaxonomy
	InNonBacteria, //presence in NCBI NonBacteria
	CocoaType,//the entity type by Cocoas
	BrownCluster,//brown cluster
	wordEmbedCluster, //word embedding cluster
	NER	//answer
}
