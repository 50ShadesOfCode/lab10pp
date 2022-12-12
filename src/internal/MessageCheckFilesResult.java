package internal;

import java.io.Serializable;

public class MessageCheckFilesResult extends MessageResult implements
        Serializable {

    private static final long serialVersionUID = 1L;

    public MessageResult.Data data = new MessageResult.Data();

    public CustomFile[] files = null;

    public MessageCheckFilesResult( String errorMessage ) { //Error
        super.setup( Protocol.CMD_CHECK_FILES, errorMessage );
    }

    public MessageCheckFilesResult( CustomFile[] files ) { // No errors
        super.setup( Protocol.CMD_CHECK_FILES);
        this.files = files;
    }

    @Override
    protected MessageResult.Data getData() {
        return data;
    }
}
