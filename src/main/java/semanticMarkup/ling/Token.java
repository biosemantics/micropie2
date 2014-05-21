package semanticMarkup.ling;

import org.apache.commons.lang3.StringUtils;

/**
 * A token represents a consecutive number of characters e.g. a word
 * @author rodenhausen
 */
public class Token {

	protected String content;

	/**
	 * @param content
	 */
	public Token(String content) {
		super();
		this.content = content;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return content;
	}
	
	@Override
	public boolean equals(Object obj) {

		if (obj == this) {
			return true;
		}

		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}

		Token myToken = (Token) obj;

		return (StringUtils.equals(this.content, myToken.getContent()));
	}
}