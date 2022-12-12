package internal;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * <p>MessageUser class: Get userNics list
 * @author Sergey Gutnikov
 * @version 1.0
 */
@XmlRootElement
public class MessageUser extends Message implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public Message.Data data = new Message.Data();
	protected Message.Data getData() {
		return data;
	}

	public MessageUser() {
		super.setup( Protocol.CMD_USER );
	}
}