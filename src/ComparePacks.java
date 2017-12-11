import java.util.Comparator;

/**
 *
 * @author itsgnegrao
 */
public class ComparePacks implements Comparator<Packet>{
    public int compare(Packet p1, Packet p2) {
        if (p1.getPart()< p2.getPart()) return -1;
        else if (p1.getPart()> p2.getPart()) return +1;
        else return 0;
    }   
}
