package internal;

import java.io.Serializable;

public class CustomFile implements Serializable {

    public String name;

    public byte[] fileArray;

    public CustomFile(String name, byte[] fileArray){
        this.name = name;
        this.fileArray = fileArray;
    }

}
