package usp.syntax;

import java.util.*;
import usp.util.*;

public class Sentence {
	// zero -> dummy root token
	
	public ArrayList<Token> tokens_=new ArrayList<Token>();
		
	// idx
	public Map<Integer,Pair<String,Integer>> tkn_par_=new HashMap<Integer,Pair<String,Integer>>();
		// par: id,lbl
	public Map<Integer,Set<Pair<String,Integer>>> tkn_children_
		=new HashMap<Integer,Set<Pair<String,Integer>>>();
}
