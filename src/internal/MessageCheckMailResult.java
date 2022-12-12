package internal;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * <p> MessageCheckMailResult class
 * @author Sergey Gutnikov
 * @version 1.0
 */
@XmlRootElement
public class MessageCheckMailResult extends MessageResult implements
		Serializable {

	private static final long serialVersionUID = 1L;

	public String[] letters = null;

	public MessageResult.Data data = new MessageResult.Data();

	protected MessageResult.Data getData() {
		return data;
	}

	public MessageCheckMailResult() {
		//super.setup( Protocol.CMD_CHECK_MAIL );
	}

	public MessageCheckMailResult( String errorMessage ) { //Error
		super.setup( Protocol.CMD_CHECK_MAIL, errorMessage );
	}

	public MessageCheckMailResult( String[] letters ) { // No errors
		super.setup( Protocol.CMD_CHECK_MAIL );
		this.letters = letters;
	}

	public String toString() {
		String res = super.toString();
		if (letters != null)
			for (String str : letters)
				res += ", " + str;
		return res;
	}
}
