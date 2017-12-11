import java.io.Serializable;

/**
 *
 * @author itsgnegrao
 */
public class Packet implements Serializable{
    private String fileName;
    private int part;
    private byte[] bytes;

    public Packet(Object o) {
        Packet pack = (Packet) o;
        part = pack.getPart();
        fileName = pack.getFileName();
        bytes = pack.getBytes();
    }
    
    public Packet(String fileNamerec, int partrec, byte[] bytesrec) {
        part = partrec;
        fileName = fileNamerec;
        bytes = bytesrec;
    }

    public String getFileName() {
        return fileName;
    }

    public int getPart() {
        return part;
    }

    public byte[] getBytes() {
        return bytes;
    }
    
    public void print(){
        //System.out.println(new String(bytes));
        System.out.println(part);
        System.out.println(fileName);
    }
 
}
