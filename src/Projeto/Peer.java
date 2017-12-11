package Projeto;

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
        this.parts = new ArrayList<>();
        String[] formatstr;
        formatstr = string .split("\\s+");
        this.nick = formatstr[0];
        this.ip = InetAddress.getByName(formatstr[1]);
        this.port = Integer.parseInt(formatstr[2]);
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
  
    public void addPart(int part){
        this.parts.add(part);
    }
    
}
