package internal;

import java.io.Serializable;

public class MessageFileResult extends MessageResult implements Serializable {

    private static final long serialVersionUID = 1L;

    public MessageResult.Data data = new MessageResult.Data();

    public MessageFileResult(String errorMessage ) { //Error

        super.setup( Protocol.CMD_IMAGE, errorMessage );
    }

    @Override
    protected MessageResult.Data getData() {
        return data;
    }

    public MessageFileResult() { // No errors

        super.setup( Protocol.CMD_IMAGE );
    }
}