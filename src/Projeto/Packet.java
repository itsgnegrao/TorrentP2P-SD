package Projeto;

import com.sun.org.apache.xml.internal.utils.SerializableLocatorImpl;
import java.io.Serializable;

/**
 *
 * @author itsgnegrao
 */
public class Packet implements Serializable{
    private final  String fileName;
    private final  int part;
    private final  byte[] bytes;

    public Packet(String fileName, int part, byte[] bytes) {
        this.part = part;
        this.fileName = fileName;
        this.bytes = bytes;
    }

    public String getFileName() {
        return this.fileName;
    }

    public int getPart() {
        return this.part;
    }

    public byte[] getBytes() {
        return this.bytes;
    }
 
}
