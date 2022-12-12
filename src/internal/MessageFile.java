package internal;

import java.io.Serializable;

public class MessageFile extends Message implements Serializable{
    private static final long serialVersionUID = 1L;

    public Message.Data data = new Message.Data();

    public String usrNic;
    public String absolutePath;

    public CustomFile file;

    public MessageFile(String usrNic, String txt, CustomFile file) {

        super.setup( Protocol.CMD_IMAGE );
        this.usrNic = usrNic;
        this.absolutePath = txt;
        this.file = file;
    }

    @Override
    protected Message.Data getData() {
        return data;
    }
}
