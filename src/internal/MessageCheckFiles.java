package internal;

import java.io.Serializable;

public class MessageCheckFiles extends Message implements Serializable {

    private static final long serialVersionUID = 1L;

    public Message.Data data = new Message.Data();

    @Override
    protected Message.Data getData() {
        return data;
    }

    public MessageCheckFiles() {
        super.setup( Protocol.CMD_CHECK_FILES);
    }
}
