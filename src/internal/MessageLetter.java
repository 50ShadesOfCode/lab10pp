package internal;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * <p>MessageLetter class
 * @author Sergey Gutnikov
 * @version 1.0
 */
@XmlRootElement
public class MessageLetter extends Message implements Serializable {

	private static final long serialVersionUID = 1L;

	public String usrNic;
	public String txt;

	public Message.Data data = new Message.Data();
	protected Message.Data getData() {
		return data;
	}

	public MessageLetter() {
		//super.setup( Protocol.CMD_LETTER );
	}

	public MessageLetter( String usrNic, String txt ) {
		super.setup( Protocol.CMD_LETTER );
		this.usrNic = usrNic;
		this.txt = txt;
	}

	public String toString() {
		return super.toString() + ", " + usrNic + ", " + txt;
	}
}
