package internal;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * <p>MessageContextResult class
 * @author Sergey Gutnikov
 * @version 1.0
 */
@XmlRootElement
public class MessageContextResult extends MessageResult implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public MessageResult.Data data = new MessageResult.Data(); 
	protected MessageResult.Data getData() {
		return data;
	}
	
	public MessageContextResult( String errorMessage ) { // Error
		super.setup( Protocol.CMD_CONTEXT, errorMessage );
	}
	
	public MessageContextResult() { // No error
		super.setup( Protocol.CMD_CONTEXT );
	}
}
