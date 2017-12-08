
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;


/**
 *
 * @author itsgnegrao
 */

class client_threads extends Thread{
	public String filename;

    public static String magic = "1234";
    public static String chunkDowStr = "14";
 private boolean downloadComplete=false;
	public int chunk;
    private int indx;
 public int userCount;
	public String user;
	public int index;
	public int userid;
	public client_threads(String filename,int slot,int chunkIndex, String user, int userCount){
	this.filename=filename;
	this.user=user;
	this.indx=chunkIndex;//chunarray index
	this.index = slot;//thread index
	chunk = Downloader.chunkArray[chunkIndex];
	this.userCount = userCount;
    }
	public void run(){	
		try{
String st=null;	
			//System.out.println("in client_thread :"+index);
			Downloader.slot_free[index]=false;
			Downloader.userBusy[userCount]=true;
			int server_port=Peer.SPORT;
	    int client_port=Peer.CPORT;

			int LFR=-1;//last frame received 
        		int RWS=8;//receiver window size
        		int SNTA=0;//sequence no. to be acked
       			int LAS=RWS+LFR;//last acceptable sequence no.



	    
	    DatagramSocket socket = new DatagramSocket(client_port+index+5);

// set up client socket for listening and sending 
	    
        		int buffull=0;
       			boolean alreadypresent=false;
			boolean errFlag=false;
			boolean downloadComplete=false;
			boolean sentAllPackets=false;
			DatagramPacket []PacketBuff=new DatagramPacket[RWS];
			String SeqNo="";
			int SN=-1;
			boolean flag=false;
			int serverHandlerPort=0;
			boolean closedfile=true;
			int k=0;
			while(k<RWS){
				PacketBuff[k]=null;
				k++;
			}

			byte [] buf1=new byte[1024];
			byte [] buf=new byte[1024];
	        	try {
    				socket.setSoTimeout(1000);//if receiver is down then generate an exception
			}
			catch (SocketException e) {
    				System.out.println("Error setting timeout TIMEOUT: " + e);
			}
			InetAddress dir=InetAddress.getByName(user);		
			DatagramPacket packet=null;
		        		
	    String entry=magic+" "+chunkDowStr+" "+filename+" "+chunk;
	    packet = new DatagramPacket(buf,buf.length);
	    packet.setData(entry.getBytes());
	    packet.setAddress(dir);
	    packet.setPort(server_port);//server will send data on different ports not where it is a regular listener
	    //also if it will serve many client at the same time then all will be served at different ports
	    //packet = new DatagramPacket(buf1, io, dir,server_port);
	    try{
		socket.send(packet);
	    }
	    catch(Exception err){ System.out.println(err);
	    }


	    FileOutputStream outFile;
	    outFile = new FileOutputStream(filename+"_chunk_"+(chunk)+".txt");//or filename_chunknumber.txt
            DatagramPacket ack=new DatagramPacket("0000".getBytes(),4);
	    ack.setAddress(dir);
	    int port = 0;
	    socket.setSoTimeout(2000);
	    
			byte [] buf2=new byte[1024];
			DatagramPacket packet2 = new DatagramPacket(buf2,buf2.length);
			
			int p1=0; 
			while(true){
				byte []buffer=new byte[1024];
				DatagramPacket P = new DatagramPacket(buffer,buffer.length);
				try{
					socket.receive(P);
					serverHandlerPort=P.getPort();
					Downloader.deadCount[userCount]=0;	
				}				
				catch(SocketTimeoutException te){
					socket.close();
					
					if(downloadComplete){
						Downloader.downloaded[indx]=true;
						Downloader.under_process[indx]=false;
						Downloader.userBusy[userCount]=false;
						Downloader.timeout[index]=true;				
						break;
					}
					if(Downloader.deadCount[userCount] == 3)
	           	 	Downloader.userDead[userCount] = true;
				  	  else{
					Downloader.deadCount[userCount]++;				
					//System.out.println("deadcount increased");
					}
					//System.out.println("thread terminated : "+index);
					Downloader.userBusy[userCount]=false;
					Downloader.timeout[index]=true;					
					errFlag=true;
					break;
				}

				Downloader.userBusy[userCount]=true;
				//System.out.println("received packet with sn : "+getSN(P));
				if(downloadComplete){
                                      	if(LFR/1000!=0)SeqNo=""+LFR;
                                      	else if(LFR/100!=0)SeqNo="0"+LFR;
                                       	else if(LFR/10!=0)SeqNo="00"+LFR;
                                       	else SeqNo="000"+LFR;
					byte []sn=new byte[4];
					copySN(sn,SeqNo);
                                       	ack=new DatagramPacket(sn,4,dir,serverHandlerPort);
      			       		socket.send(ack);
									
				}
				else{
					SN=getSN(P);
					if(SN>LFR && SN<=LAS){						
							PacketBuff[SN-SNTA]=P;
							int l=0;
							DatagramPacket temp=PacketBuff[l];
							while(l<RWS && temp!=null){							
								st=new String(temp.getData());
								
								if(st.charAt(4)=='@' && st.charAt(5)=='#' && st.charAt(6)=='$'){//check whether end of transfer,last packet					
									downloadComplete=true;
								//Downloader.shareChunk(filename,chunk,socket);
								Downloader.downloaded[indx]=true;
				    			Downloader.under_process[indx] = false;
								Downloader.shareChunk(filename,chunk,socket);
									//System.out.println("end packet received");								
									SNTA++;
									l++;
									break;
								}
								else{
									//copy the data part
									byte []data=new byte[temp.getLength()-4];
									int j=0;
									while(j<temp.getLength()-4){
										data[j]=temp.getData()[j+4];
										j++;
									}
									outFile.write(data,0,temp.getLength()-4);
									PacketBuff[l]=null;
									SNTA++;
									l++;					
								}
								if(l<RWS)temp=PacketBuff[l];
							}
							if(l!=0)LFR=SNTA-1;
							int l1=0;
							while(l<RWS){						
								PacketBuff[l1]=PacketBuff[l];
								l++;
								l1++;
							}
							while(l1<RWS){						
								PacketBuff[l1]=null;
								//l++;
								l1++;
							}
							p1++;							
							LAS=LFR+RWS;
							if(LFR/1000!=0)SeqNo=""+LFR;
                                                	else if(LFR/100!=0)SeqNo="0"+LFR;
                                                	else if(LFR/10!=0)SeqNo="00"+LFR;
                                                	else SeqNo="000"+LFR;
							byte []sn=new byte[4];
							copySN(sn,SeqNo);
                                               		ack=new DatagramPacket(sn,4,dir,serverHandlerPort);
							//System.out.println("send an ack with sn : "+LFR);
      			       				if(LFR!=-1)socket.send(ack);
						
					}
					else{//reject the packet
					}
				}
			}
	       	 	try{
				flag=true;
       				outFile.close();
       				socket.close();
			}
			catch (Exception err1){//Catch exception if any
 				 System.err.println("Error: " + err1.getMessage());
   			}
			
			Downloader.userBusy[userCount]=false;
			Downloader.slot_free[index]=true;
			Downloader.under_process[indx]=false;	   
	    	Downloader.allSlotsBusy = false;
	    	Downloader.allUserBusy = false;


		}
		catch(Exception sdgds){}      		
	}
	
     	public int getSN(DatagramPacket P){
                //read first 4 bytes of data and get SN
                byte []st=P.getData();
                byte []SN=new byte[4];
                SN[0]=st[0];
                SN[1]=st[1];
                SN[2]=st[2];
                SN[3]=st[3];
                String SeqN=new String(SN);
                int seq=Integer.parseInt(SeqN);
                return seq;
     	}
	public void copySN(byte[] buf,String SN){
		int i=0;
		char d=0;
		while(i<4){
			d=SN.charAt(i);
			switch (d) {
				case '0': buf[i]='0';break;
				case '1': buf[i]='1';break;
				case '2': buf[i]='2';break;
				case '3': buf[i]='3';break;
				case '4': buf[i]='4';break;
				case '5': buf[i]='5';break;
				case '6': buf[i]='6';break;
				case '7': buf[i]='7';break;
				case '8': buf[i]='8';break;
				case '9': buf[i]='9';break;
				default: buf[i]='\0';break;
			}
			i++;
		}	
	}
         public boolean probability(){
		if(Math.random()<Server.send_prob)return true;
		else return false;
	}





/*
    


   catch (Exception err1){
		System.err.println("Error: " + err1.getMessage());
	    }
	    
	//System.out.println("Set users as not busy slot is also made free");
	}
	catch(Exception e){}
	//System.out.println("finally exiting from thread");	
    }//end run

    public int getSN(DatagramPacket P){
	//read first 4 bytes of data and get SN
	byte []st=P.getData();
	byte []SN=new byte[4];
	SN[0]=st[0];
	SN[1]=st[1];
	SN[2]=st[2];
	SN[3]=st[3];
	String SeqN=new String(SN);
	int seq=Integer.parseInt(SeqN);
	return seq;
    }
    */
//deleteChunk("unfinishedDownload.txt",filename,chunk);

//deleteChunk("main.txt",filename.substring(0,j));//Delete count for thisshared file
        //this method is also used in server b/c he also do these when receive such request.
public static void setContents(File aFile, String aContents)
                                 throws FileNotFoundException, IOException {
		//System.out.println("inside setcontents method");
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

    //use buffering
                Writer output = new BufferedWriter(new FileWriter(aFile));
                try {
                        //FileWriter always assumes default encoding is OK!
                        output.write( aContents );
			//System.out.println("contents of file "+aContents+"$$");
                }
                finally {
                        output.close();
                }
        }



    private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
        	int halfbyte = (data[i] >>> 4) & 0x0F;
        	int two_halfs = 0;
        	do {
	            if ((0 <= halfbyte) && (halfbyte <= 9))
	                buf.append((char) ('0' + halfbyte));
	            else
	            	buf.append((char) ('a' + (halfbyte - 10)));
	            halfbyte = data[i] & 0x0F;
        	} while(two_halfs++ < 1);
        }
        return buf.toString();
    }
 
    public static String SHA1(String text) 
    throws NoSuchAlgorithmException, UnsupportedEncodingException  {
	MessageDigest md;
	md = MessageDigest.getInstance("SHA");
	byte[] sha1hash = new byte[40];
	md.update(text.getBytes(), 0, text.length());
	sha1hash = md.digest();
	return convertToHex(sha1hash);
    }
	public static void deleteSearchLine(String filename, String lineWithFilename){
		try{
		File file = new File(filename);
		String last = getDeletedSearchLine(file,lineWithFilename);
		setContents(file,last);
		}catch(FileNotFoundException fnfe){}
		catch(IOException ioe){}
	}//done decrementing

	public static String getDeletedSearchLine(File aFile, String lineWithFilename) {
		//System.out.println("inside deletesearchline method");
    //...checks on aFile are elided
		StringBuilder contents = new StringBuilder();
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
				if(st.nextToken().equals(lineWithFilename)){
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
	catch (IOException ex){
		ex.printStackTrace();
	}
	return contents.toString();
	}	
		

}//ends class