package proj_base;


import java.net.DatagramPacket;

/**
 *
 * @author itsgnegrao
 */

class Cubbyhole {//used for interthread communication
    private DatagramPacket contents;
    private boolean available = false;

    public synchronized DatagramPacket get() {
        while (available == false) {
            try {
                wait();
            } catch (InterruptedException e) { }
        }
        available = false;
        notifyAll();
        return contents;
    }

    public synchronized void put(DatagramPacket value) {
        while (available == true) {
            try {
                wait();
            } catch (InterruptedException e) { }
        }
        contents = value;
        available = true;
        notifyAll();
    }
}