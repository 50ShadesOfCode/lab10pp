package internal;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * <p> MessageCheckMail class
 * @author Sergey Gutnikov
 * @version 1.0
 */
@XmlRootElement
public class MessageCheckMail extends Message implements Serializable {

	private static final long serialVersionUID = 1L;

	public Message.Data data = new Message.Data();
	
	protected Message.Data getData() {
		return data;
	}
	
	public MessageCheckMail() {
		super.setup( Protocol.CMD_CHECK_MAIL );
	}
}
