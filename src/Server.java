
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

/**
 *
 * @author itsgnegrao
 */


class Server extends Thread {
	private static final String magic = "1234";
	private final static int newUserStr = 0;//making them integer
	private static String newUserRepStr =      "1";
	private final static int beaconStr =          2;
	private static String beaconRepStr =       "3";
	private final static int updateStr =          4;
	private static String updateboth =         "5";
	private static String updateUsers =        "6";
	private static String updateMain =         "7";
	private static String allreadyUpdated =    "8";
	private final static int sendUserStr =        9;
	private final static int sendMainStr =        10;
	private final static int fileReqStr =         11;
	private static String YfileReqRepStr =     "12";
	private static String NfileReqRepStr =     "13";
	private final static int chunkDowStr =        14;
	private final static int fileUploadStr =      15;
	private final static int newfileUploadStr =   16;
	private static String newfileUploadRepStr= "17";
	private static String fileUploadRepStr =   "19";
	private final static int fileDeleteStr =      22;
	private static String fileDeleteRepStr =   "23";
	private final static int fileDownloadedAck = 24;
	public static String updatemasterChunk =         "26";
	public final static int sendMasterStr  =   28;
	public final static int max_slots = 4;
	public static double send_prob = 1;
	public static boolean[] free_slot = new boolean[max_slots]; 
	private static File users = new File("users.txt");
	private static File main = new File("main.txt");
	private static File master = new File("masterChunk.txt");
	private static DatagramPacket pack;
	private static DatagramSocket sock;
	
	public Server(){
	}

	public void run() {
		for(int free = 0; free < max_slots; free++){
			free_slot[free] = true;
		}
		try{
			sock = new DatagramSocket(Peer.SPORT);
			sock.setSoTimeout(100000);
		}
		catch (SocketException se) {System.err.println(se);}
		while(true){
			byte[] receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {	
				sock.setSoTimeout(100000);
				sock.receive(receivePacket);
          		}
      			catch (SocketException se) {System.err.println(se);}
          		catch(IOException e){ }
			String ans = new String(receivePacket.getData());
			int i=0;
			ans = ans.trim();
			StringTokenizer st = new StringTokenizer(ans);
			String array[] = new String[5];
			byte[] buf = new byte[1024];
			while (st.hasMoreTokens()) {
				array[i] = st.nextToken();
				//System.out.println("value in arrays are:"+array[i]);
				i++;
			}
			String str = array[1];
			if(array[0]==null)
				continue;
			if(array[0].equals(magic)){
				//System.out.println("switching on int value:"+array[1]+"$$");
				int sw = Integer.parseInt(str);
				switch(sw){
					case newUserStr:
						//System.out.println("server got a newuser");
						String qw = receivePacket.getAddress().getHostName().toString();
						if(!searchLine("users.txt",qw));
							append("users.txt",qw);
						String temp = array[0] + " " + newUserRepStr;
						buf = temp.getBytes();
						pack=new DatagramPacket(buf,buf.length,receivePacket.getAddress(), receivePacket.getPort());
						try{
							sock.send(pack);
						}catch(IOException ioe){}
						break;
					case beaconStr:
						//System.out.println("Server:got a beacon from "+receivePacket.getAddress());
						temp = array[0] + " " + beaconRepStr ;
						buf = temp.getBytes();
						pack = new DatagramPacket(buf,buf.length, receivePacket.getAddress(), receivePacket.getPort());
						try{
							sock.send(pack);
						}catch(IOException ioe){}
						break;
					case updateStr:
						//System.out.println("server got a compare update:"+array[2].substring(0,13)+"k");
						long updatedusers =Long.parseLong(array[2].substring(0,8)); 
						long updatedmain =Long.parseLong(array[3].substring(0,8)); 
						//long updatedmaster =Long.parseLong(array[4].substring(0,8)); 
						//System.out.println("his updated time of users:"+updatedusers+"main:"+updatedmain);
						long myUpdateusers =Long.parseLong((Long.toString(users.lastModified())).substring(0,8)); 
						long myUpdatemain =Long.parseLong((Long.toString(main.lastModified())).substring(0,8));
						//long myUpdatemaster =Long.parseLong((Long.toString(master.lastModified())).substring(0,8));
						//System.out.println("my updated time of master:"+myUpdatemaster+"master:"+myUpdatemaster);
						if(myUpdateusers > updatedusers){
							if(myUpdatemain > updatedmain)
								temp = array[0] + " " + updateboth;
							else
								temp = array[0] + " " + updateUsers;
						}
						else if(myUpdatemain > updatedmain)
							temp = array[0] + " " + updateMain;
						else
							temp = array[0] + " " + allreadyUpdated;
						buf = temp.getBytes();
						pack = new DatagramPacket(buf,buf.length, receivePacket.getAddress(), receivePacket.getPort());
						try{
							sock.send(pack);
						}catch(IOException ioe){}
						break;
					case sendMainStr:
						//System.out.println("Server:got a sendmain request");
						sendfile(sock,"main.txt",receivePacket.getAddress());
						break;
					case sendUserStr:
						//System.out.println("Server:got a send user.txt request");
						sendfile(sock,"users.txt", receivePacket.getAddress());
						break;
					case sendMasterStr:
						//System.out.println("Server:got a sendmasterChunk request");
						sendfile(sock,"masterChunk.txt",receivePacket.getAddress());
						break;
					case fileReqStr:
						System.out.println("in the corect case at least with filename "+array[2]+"$");
						String line = RetLineSearch("shared.txt",array[2]);//return null or that line
						String tempLine = "";
						if(line == null)
							temp = array[0] + " " + NfileReqRepStr;
						else{
						String[] result = line.split("\\s");
						for (int x=0; x<result.length - 1; x++){
							//System.out.println(result[x]);
							tempLine+= result[x]+" ";
						}
						temp = array[0] + " " + YfileReqRepStr + " " + tempLine;
						}
						
						buf = temp.getBytes();
						pack = new DatagramPacket(buf,buf.length,receivePacket.getAddress(), receivePacket.getPort());
						try{
							sock.send(pack);
						}catch(IOException ioe){}
						break;
					case chunkDowStr://array[2] must have filename and array[3] have chunk number
						String filename = array[2];
						int chunkNumber = Integer.parseInt(array[3]);
						for(int free = 0;free < max_slots; free++){
						if(free_slot[free]){
						free_slot[free] = false;
						ServerReqHandler s = new ServerReqHandler(receivePacket.getAddress(),receivePacket.getPort(), filename, chunkNumber, free);
						s.start();//it runs on different port so not interfere with server other works
						break;
						}
						}
						break;
					case newfileUploadStr:
						append("main.txt",array[2] + " " + 1);
						temp = array[0] + " " + newfileUploadRepStr;
						buf = temp.getBytes();
						pack = new DatagramPacket(buf,buf.length,receivePacket.getAddress(),receivePacket.getPort());
						try{
						sock.send(pack);
						downloadmasterChunk(sock,receivePacket.getAddress());
						}catch(IOException ioe){}
						break;
					case fileUploadStr:
						if(searchLine("main.txt",(array[2] + " " +array[3])))
							incrementCount("main.txt",array[2]);
						temp = array[0] + " " + fileUploadRepStr;
						buf = temp.getBytes();
						pack = new DatagramPacket(buf,buf.length,receivePacket.getAddress(), receivePacket.getPort());
						try{
							sock.send(pack);
						}catch(IOException ioe){}
						break;
					case fileDeleteStr:
						if(searchLine("main.txt",(array[2] + " " + array[3])))
							decrementCount("main.txt", array[2]);
						temp = array[0] + " " + fileDeleteRepStr;
						buf = temp.getBytes();
						pack = new DatagramPacket(buf,buf.length,receivePacket.getAddress(), receivePacket.getPort());
						try{
							sock.send(pack);
						}catch(IOException ioe){};
						break;
					case fileDownloadedAck:
						//System.out.println("file "+array[2]+" is recvd by "+receivePacket.getAddress());
				}//end switch
			}//end if
		}//end while(true)
	}//end run
	public static String RetLineSearch(String f, String toSearch){
		try{
		File file = new File(f);
		FileReader in = new FileReader(file);
		BufferedReader br = new BufferedReader(in);
		// Continue to read lines while
		// there are still some left to read
		String  c,d;
		while ((c=br.readLine()) !=null)
		{
			StringTokenizer st = new StringTokenizer(c);
			while ((st.hasMoreTokens())){
				d = st.nextToken();
				if(d.equals(toSearch))
					return c;
			}
		}}catch(FileNotFoundException fnf){System.out.println("fille"+fnf);}
		catch( IOException ioe){System.out.println("error"+ioe);}
		return null;
	}

	public static void sendfile(DatagramSocket sock,String filename, InetAddress addr){
        	byte bytes[] = new byte[1600];
		try{
		File inputFile = new File(filename);
		InputStream in = new FileInputStream(inputFile);
		int io;
		while ((io = in.read(bytes, 0, 1600)) > 0){
			DatagramPacket packet;
			if(io < 1600){
				byte newbuf[] = new byte[io];
				int i = 0;
				while(i < io){
					newbuf[i] = bytes[i];
					i++;
				}
				//System.out.println ("Sending data " + io +  " bytes to "+addr);
				packet = new DatagramPacket(newbuf,io, addr,Peer.CPORT);
			}
			else
				packet = new DatagramPacket(bytes,io,addr,Peer.CPORT);
			boolean lost = true;
			byte[] receiveData = new byte[1600];
			int i=0;
			while(lost==true)
			{
				try{
					//System.out.print("Server:In sendfile. Now sending "+new String(bytes));
					sock.send(packet);
				}
				catch (IOException ex){ System.err.println(ex);}
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try {
					sock.setSoTimeout(5000);
					sock.receive(receivePacket);
					InetAddress returnIPAddress = receivePacket.getAddress();  // get ipddress
					//int port = receivePacket.getPort();  // get port number
					//System.out.println ("Server:Waiting for return packet in sendfile method from host at: " + returnIPAddress);
					lost = false;
				}
				catch (SocketTimeoutException ste){
					System.out.println ("Timeout Occurred: Packet assumed lost");
					//System.out.println ("Server: message "+" re-attempt no." + i);
					lost = true;
					i++;
					if(i==5){	
						String set="010";
						pack.setData(set.getBytes());
						pack.setPort(Peer.CPORT);
					return;
					}
				}//end catch
				catch (UnknownHostException ex) { System.err.println(ex);}
				catch (SocketException se) {
					System.err.println(se);
				}
				catch (IOException e) {
					System.err.println("Packet " + i + " failed with " + e);
				}
			}//end while
		}//end while
		bytes = "@#$".getBytes();
		DatagramPacket packet = new DatagramPacket(bytes, bytes.length, addr,Peer.CPORT);
		try{
		//System.out.print("Server:In sendfile. Now sending "+new String(bytes));
			sock.send(packet);
		}
		catch (IOException ex){ System.err.println(ex);}
		}catch(IOException ioe){}
	}//end method sendfile

        public static boolean downloadmasterChunk(DatagramSocket sock, InetAddress addr){
                //first send download request   
                int count =0;//send file download request 4 times
                boolean notStarted = true, flag = false;
                String hdr;
                while((count < 4)&&(notStarted)){
                        count++;
                        try{
                                byte[] buf = new byte[1600];
                                byte[] receiveData = new byte[1600];
                                File fp1 = new File("masterChunk.txt");
                                FileOutputStream fp = new FileOutputStream("masterChunk.txt");      
                                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);  // 5. create datagram packet for incoming         datagram
                              //size of file
                                int size = 0;
                                while(size<5){//max downloadable size is 10 kb
                                        size++;
                                        System.out.println("file size"+fp1.length());
                                        sock.setSoTimeout(1000);
                                        try {//you may also check whether it is correct file packet or something else
                                            sock.receive(receivePacket);  
                                            InetAddress returnIPAddress = receivePacket.getAddress();
                                                int port = receivePacket.getPort();  // get port number
                                                //System.out.println ("From server at: " + returnIPAddress +  ":" + port);
                                        //System.out.println("Message: " + modifiedSentence);
						String d = new String(receivePacket.getData());
						d = d.trim();
                                                String see = new String(receivePacket.getData());
                                                see = see.trim();
                                                //System.out.println("inside the packet"+see); 
                                                //System.out.println(" downloading file   \n");
                                                notStarted = false;
                                                String ackn="ack";
                                                byte[] buff = new byte[1600];
                                                buff = ackn.getBytes();
                                                DatagramPacket pkt = new DatagramPacket(buff, buff.length, returnIPAddress,port);
                                                sock.send(pkt);
                                                //System.out.println("ack sent \n");
                                                if(see.charAt(0)=='@' && see.charAt(1)=='#' && see.charAt(2)=='$'){  //last packet	       
         		        			flag =true;
         						break;
         					}
						else
							 fp.write(d.getBytes());
                                        }
                                catch (SocketTimeoutException ste)
                                {
                                        System.out.println ("Timeout Occurred: Packet assumed lost");}
                        }//end while 
                        fp.close();
                }//end try
                catch (UnknownHostException ex) { System.err.println(ex);}
                catch (IOException ex) {System.err.println(ex);}
                catch (Exception e){System.err.println("File input error");}

                }//end while
		if(flag){
				byte[] buf2 = new byte[1600];
				buf2 = (Peer.magic +" "+Peer.fileDownloadedAck+" "+"masterChunk.txt").getBytes();

				try{
				//InetAddress addr = InetAddress.getByName(address);
				DatagramPacket ackpack = new DatagramPacket(buf2, buf2.length, addr, Peer.CPORT);
				sock.send(ackpack);
				} catch(Exception e){
					System.out.println(e);
				}
		}
                return flag;
        }//end method



	public static boolean searchLine(String file, String query){
		try{
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				// Print the content on the console
				if(strLine.equals(query)){
					in.close();
					//System.out.print("dunno");
					return true;
				}
				//System.out.print(strLine);
			}
			//Close the input stream
			in.close();
		}catch (Exception e){System.err.println("Error: " + e.getMessage());}
		return false;
	}//end method

	public static void append(String filename, String toAppend){
		try{
			File file = new File(filename);
			String last = getAppendedContents(file,toAppend);
			//System.out.println("inside append server calling set contents");
			setContents(file,last);
		}catch(FileNotFoundException fnfe){}
		catch(IOException ioe){}
	}//done appending

	public static String getAppendedContents(File aFile, String toAppend) {
		StringBuilder contents = new StringBuilder();
		try {
			BufferedReader input =  new BufferedReader(new FileReader(aFile));
			try {
				String line = null;
				while (( line = input.readLine()) != null){
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
				contents.append(toAppend);
				contents.append(System.getProperty("line.separator"));
			}
			finally {
				input.close();
			}
		}
		catch (IOException ex){ex.printStackTrace();}
		return contents.toString();
	}
	
	public static void setContents(File aFile, String aContents)throws FileNotFoundException, IOException {
		if (aFile == null) {
			throw new IllegalArgumentException("File should not be null.");
		}
		if (!aFile.exists()) {
			throw new FileNotFoundException ("File does not exist: " + aFile);
		}
		if (!aFile.isFile()) {
			throw new IllegalArgumentException("Should not be a directory: " + aFile);
		}
		if (!aFile.canWrite()) {
			throw new IllegalArgumentException("File cannot be written: " + aFile);
		}
		Writer output = new BufferedWriter(new FileWriter(aFile));
		try {
			output.write( aContents );
		}
		finally {
			output.close();
		}
	}//end method
	public static void incrementCount(String filename, String toIncrement){
		try{
		File file = new File(filename);
		String last = getIncrementedContents(file,toIncrement);
		setContents(file,last);
		}catch(FileNotFoundException fnfe){}
		catch(IOException ioe){}
	}//done incrementing

	public static String getIncrementedContents(File aFile, String toIncrement) {
		StringBuilder contents = new StringBuilder();
		int initialCount, finalCount;
		try {
		BufferedReader input =  new BufferedReader(new FileReader(aFile));
			try {
				String line = null; //not declared within while loop
				while (( line = input.readLine()) != null){
					StringTokenizer st = new StringTokenizer(line);
					if(st.nextToken().equals(toIncrement)){
						initialCount = Integer.valueOf( st.nextToken()).intValue();
						finalCount = initialCount + 1;
						line = toIncrement + " " + finalCount;
					}
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			}
			finally {
				input.close();
			}
		}
		catch (IOException ex){ex.printStackTrace();}
		return contents.toString();
	}	
	
	public static void decrementCount(String filename,String toDecrement){
		try{
			File file = new File(filename);
			String last = getDecrementedContents(file,toDecrement);
			setContents(file,last);
		}catch(FileNotFoundException fnfe){}
		catch(IOException ioe){}
	}//done decrementing
	
	public static String getDecrementedContents(File aFile, String toDecrement) {
		StringBuilder contents = new StringBuilder();
		int initialCount, finalCount;
		boolean toAdd = true;
		try {
		//use buffering, reading one line at a time
		//FileReader always assumes default encoding is OK!
			BufferedReader input =  new BufferedReader(new FileReader(aFile));
			try {
				String line = null; //not declared within while loop
				while (( line = input.readLine()) != null){
					StringTokenizer st = new StringTokenizer(line);
					toAdd = true;
					if(st.nextToken().equals(toDecrement)){
						initialCount = Integer.valueOf( st.nextToken()).intValue();
						finalCount = initialCount - 1;
						if(finalCount > 0)
							line = toDecrement + " " + finalCount;
						else
							toAdd = false;
					}
					if(toAdd){
						contents.append(line);
						contents.append(System.getProperty("line.separator"));
					}
				}
			}
			finally {
				input.close();
			}
		}
		catch (IOException ex){ex.printStackTrace();}
		return contents.toString();
	}
}//end server thread