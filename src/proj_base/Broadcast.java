package proj_base;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 *
 * @author itsgnegrao
 */

class Broadcast extends Thread{ //broad cast to all server with name in temp.txt
	Cubbyhole cb1;
	DatagramSocket sock;
	public Broadcast(Cubbyhole cb1, DatagramSocket sock){
	   this.cb1 = cb1;
	  this.sock=sock;
	}
	public void run(){
		while(true){
			int count =1;
			DatagramPacket pack = cb1.get();
			//System.out.println("Got a brdcst request. Packet contents are "+new String(pack.getData()));
			for(int i = 0; i < count; i++){
				try{
				File userList = new File("temp.txt");
				FileReader userlist = new FileReader(userList);
				BufferedReader br = new BufferedReader(userlist);
				// Continue to read lines while
				// there are still some left to read
				String  c;
				while((c = br.readLine()) != null){
					
					if(c.equals(InetAddress.getLocalHost().getHostName()))
						continue;
					//System.out.println(" broadcasting "+new String(pack.getData())+" to "+c);
					try{
					pack.setPort(Peer.SPORT);
					InetAddress address = InetAddress.getByName(c);
					pack.setAddress(address);
					}catch(UnknownHostException uhe){}
					try{
						sock.send(pack);
					}catch (IOException ex) {System.err.println(ex);}
				}
				//System.out.println ("Waiting for return packet inside broadcasting");
				//try {
					int timeout = 500;long time = System.currentTimeMillis();
					while((System.currentTimeMillis()-time)<timeout){}//do nothing wait
					//sock.setSoTimeout(500);

				//}
				//catch (SocketTimeoutException ste)
         			//{
         			//	System.out.println ("Timeout Occurred: Packet assumed lost");
         			//	System.out.println ("message re-attempt" + i);
				//}
				br.close();
			}catch(FileNotFoundException fnfe){System.out.println("Broadcast: "+fnfe);}
			catch(IOException ioe){System.out.println("Broadcast: "+ioe);}
			}
		}
	}
}

