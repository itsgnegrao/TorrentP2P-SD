import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 *
 * @author itsgnegrao
 */
public class Peer {
    String nick;
    int port;
    InetAddress ip;
    ArrayList<Integer> parts;

    Peer(String string) throws UnknownHostException {
        parts = new ArrayList<>();
        String[] formatstr;
        formatstr = string .split("\\s+");
        nick = formatstr[0];
        ip = InetAddress.getByName(formatstr[1].replace("/", ""));
        port = Integer.parseInt(formatstr[2]);
    }

    public String getNick() {
        return nick;
    }

    public int getPort() {
        return port;
    }

    public InetAddress getIp() {
        return ip;
    }

    public ArrayList<Integer> getParts() {
        return parts;
    }
    
    public void addPart(int part){
        parts.add(part);
    }
    
}
