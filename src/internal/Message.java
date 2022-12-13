package internal;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * <p>Message base class
 * @author Sergey Gutnikov
 * @version 1.0
 */

@XmlRootElement(name="message")
public abstract class Message extends MessageXml implements Serializable {

    public static class Data implements Serializable {

		private static final long serialVersionUID = 1L;

		protected byte id;

		@XmlAttribute
		public byte getID() {
			return id;
		}
		public void setID(byte id) {
			assert( Protocol.validID( id )== true );
			this.id = id;
		}
		public Data() {
		}

		public String toString() {
			return "" + id;
		}
    }

	private static final long serialVersionUID = 1L;

	protected abstract Data getData();

	public byte getID() {
		return getData().getID();
	}

	protected Message() {
	}

	protected void setup( byte id ) {
		getData().setID(id);
	}

	public String toString() {
		return getData().toString();
	}
}
