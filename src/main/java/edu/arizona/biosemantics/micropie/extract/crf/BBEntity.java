package edu.arizona.biosemantics.micropie.extract.crf;

import java.util.ArrayList;
import java.util.List;



/**
 * BB task a1 file entity
 * 
 * @author maojin
 *
 */
public class BBEntity implements Cloneable{
	private String eID;
	private String docID;
	private String name;
	private String type;
	private int start;
	private int end;
	private int start2=-1;
	private int end2=-1;
	
	private List<Token> tokens = new ArrayList();
	
	public BBEntity(String eID, String docID, String name, String type, int start, int end, int start2, int end2) {
		super();
		this.eID = eID;
		this.docID = docID;
		this.name = name;
		this.type = type;
		this.start = start;
		this.end = end;
		this.start2 = start2;
		this.end2 = end2;
	}
	
	public BBEntity(String eID, String docID, String name, String type, int start, int end) {
		this.eID = eID;
		this.docID = docID;
		this.name = name;
		this.type = type;
		this.start = start;
		this.end = end;
		this.start2 = -1;
		this.end2 = -1;
	}
	

	public BBEntity() {
	}

	public String geteID() {
		return eID;
	}
	
	public void seteID(String eID) {
		this.eID = eID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	public String getDocID() {
		return docID;
	}
	public void setDocID(String docID) {
		this.docID = docID;
	}
	
	
	public int getStart2() {
		return start2;
	}

	public void setStart2(int start2) {
		this.start2 = start2;
	}

	public int getEnd2() {
		return end2;
	}

	public void setEnd2(int end2) {
		this.end2 = end2;
	}
	
	/**
	 * add a token to the entity's token list
	 * @param token
	 */
	public void addToken(Token token){
		if(tokens.size()>0){
			tokens.add(token);
			if(token.getOffset()==this.end) this.name+=token.getText();
				else this.name+=" "+token.getText();
			
			this.end = token.getOffend();
		}else{
			this.name = token.getText();
			this.start = token.getOffset();
			this.end = token.getOffend();
			tokens.add(token);
		}
	}
	
	/**
	 * add a token to the entity's token list
	 * @param token
	 */
	public void addTokenOnly(Token token){
		tokens.add(token);
	}
	
	public void removeLastToken(){
		if(tokens.size()>0){
			tokens.remove(tokens.size()-1);
			refreshByToken();
		}
	}
	
	private void refreshByToken() {
		if(tokens.size()==0){
			this.name=null;
			this.start=-1;
			this.end=-1;
		}else{
			this.name="";
			this.start = tokens.get(0).getOffset();
			this.end =  tokens.get(0).getOffset();
			for(Token token:tokens){
				if(token.getOffset()==this.end) this.name+=token.getText();
				else this.name+=" "+token.getText();
				this.end =  token.getOffend();
			}
		}
	}

	public List<Token> getTokens() {
		return this.tokens;
	}
	
	@Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 17 + eID.hashCode();
        hash = hash * 31 + docID.hashCode();
        hash = hash * 59 + name.hashCode();
        hash = hash * 71 + start;
        return hash;
    }
	
	@Override
	public boolean equals(Object anEntity){
		if(this==anEntity) return true;
		if(name.equals(((BBEntity)anEntity).getName())
				&&type==((BBEntity)anEntity).getType()
				&&start==((BBEntity)anEntity).getStart()
				&&end==((BBEntity)anEntity).getEnd()){
			return true;
		}
		return false;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer().append(this.eID).append("\t").append(this.type).append(" ")
				.append(this.start).append(" ").append(this.end);
		if(start2!=-1) sb.append(";").append(this.start2).append(" ").append(this.end2);
		sb.append("\t").append(this.name);
		return sb.toString();
				 
	}

	public void setTokens(List<Token> phTokens) {
		this.tokens = phTokens;
	}

	public BBEntity clone(){
		try {
			return (BBEntity) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
}