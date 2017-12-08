
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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


class ServerReqHandler extends Thread{
	public InetAddress dir;
	public static int port;
	public int chunkIndex;
	public String filename;
	public static int SPORT = 2364;
	public static String magic  = "1234";
	public static String chunkRepStr = "27";
	private int index;
	private int client_port;
	public ServerReqHandler(InetAddress address, int client_port, String filename, int chunkIndex, int index){
		this.dir=address;
		this.chunkIndex=chunkIndex;
		this.filename = filename;
		this.index=index;
		this.port = client_port;
	}
	public void run(){
		Server.free_slot[index] = false;
		try{
                  DatagramSocket socket = new DatagramSocket(Peer.SPORT+index+10);
                  try {
    					socket.setSoTimeout(100);
				}
				catch (SocketException e) {
    				System.out.println("Error setting timeout TIMEOUT: " + e);
				}
                  String date = DateUtils.now();
                  File log = new File("server.log");
                  log.createNewFile();
                  BufferedWriter out = new BufferedWriter(new FileWriter("server.log",true));
                        out.write("*****************"+date+"*****************");
			byte[] waste = new byte[1024*1024];
			byte[] buf = new byte[1024];
			byte[] buf4=new byte[1020];
			byte[] buf5 = new byte[20];
			byte []sn=new byte[4];
			byte[] buf2 = new byte[1020];
			//
			int error=0;
			int SWS=8;									//initial sliding window
			int SNTS=0;
			//int RWS=5;
			int LAR=-1;
			int LFS=-1;
			long[]timer=new long[SWS];
				DatagramPacket []packet1=new DatagramPacket[SWS];
				byte [] []BUFF=new byte[SWS] [];				
				long timeout=1;
				int SN=0;
				String SeqNo="";
				boolean sentAllPackets=false;
				boolean transferComplete=false;
				int endSN=9999;
				int count=0;
				boolean endu=true;
				int ssthresh=64;
				timer=new long[1034];
				packet1=new DatagramPacket[1034];
				BUFF=new byte[1034] [];
				boolean [] packetsent=new boolean[1034];
				int k=0;
				while(k<1034){
					packet1[k]=null;
					packetsent[k]=false;
					k++;
				}
				SWS=1;
				boolean slowStart=true;
				boolean  congestionAvoidance=false;
				long RTT=10;
				long timePassed=System.currentTimeMillis();
				double alfa=0.9;
				int dup=0;
				//
				boolean errFlag=false;			
				int io=0; 
			
			k=0;

			File f = new File(filename);
                        FileInputStream fstream;
                        DataInputStream in;
                        if(f.exists()){
                        fstream = new FileInputStream(filename);
                        in = new DataInputStream(fstream);
                        int dummy = chunkIndex;
                        while(dummy!=0){//updated the piointer for getting the required chunk
                                io = in.read(waste,0,1024*1024);
                                dummy--;
                        }
                        }
                        else{
                                fstream = new FileInputStream(filename+"_chunk_"+chunkIndex+".txt");
                                in = new DataInputStream(fstream);
                        }
			io = in.read(buf2,0,1020);//data
			k=0;
			boolean flagEnd = false;

				while(!transferComplete){



					int i=0;
					while(i<SWS){						//see for timeout of some packet already send
						if(BUFF[i]!=null && System.currentTimeMillis()-timer[i]>timeout){
							if(SWS/2>2)ssthresh=SWS/2;
							else ssthresh=2;
							SWS=1;						//restart slow start
							slowStart=true;
							congestionAvoidance=false;
							byte []Y=new byte[1024];
							int in1=0;
							while(in1<BUFF[i].length){
								Y[in1]=BUFF[i][in1];
								in1++;
							}
							DatagramPacket X=new DatagramPacket(Y,BUFF[i].length,dir,port);
							System.out.println("resend packet : "+getSN(X));

							if(probability())socket.send(X);	//resend packet[i];
							timer[i]=System.currentTimeMillis();//update timer entry;
						}
						i++;
					}
					while(LFS-LAR>=0 && LFS-LAR<SWS){
						if(io!=-1 && k<1028){							
							if(SNTS/1000!=0)SeqNo=""+SNTS;
							else if(SNTS/100!=0)SeqNo="0"+SNTS;
							else if(SNTS/10!=0)SeqNo="00"+SNTS;
							else SeqNo="000"+SNTS;


							copySN(sn,SeqNo);						
							copy(sn,buf2,buf);
							byte[] sendbuf=new byte[1024];
							int in1=0;
							while(in1<1024){
								sendbuf[in1]=buf[in1];
								in1++;
							}
							BUFF[LFS-LAR]=sendbuf;
	              					DatagramPacket packet = new DatagramPacket(buf, io+4,dir,port);
							System.out.println("send packet with sn : "+SNTS);
							if(probability())socket.send(packet);							
							k++;
							timer[LFS-LAR]=System.currentTimeMillis();
							packet1[LFS-LAR]=packet;
							if(k==1028){
								io = in.read(buf4,0,16);
							}
							else{
		        					io = in.read(buf2,0,1020);
							}
						}
						else if(io!=-1 && k==1028){
							if(SNTS/1000!=0)SeqNo=""+SNTS;
							else if(SNTS/100!=0)SeqNo="0"+SNTS;
							else if(SNTS/10!=0)SeqNo="00"+SNTS;
							else SeqNo="000"+SNTS;
							copySN(sn,SeqNo);
							buf5[0]=sn[0];
							buf5[1]=sn[1];
							buf5[2]=sn[2];
							buf5[3]=sn[3];
							int l=4;
							while(l<20){
								buf5[l]=buf4[l-4];
								l++;
							}
							if(io<16){
								DatagramPacket packet = new DatagramPacket(buf5, io+4,dir,port);
		           					if(probability())socket.send(packet);
								//System.out.println("send packet with sn : "+SNTS);
								k++;
								timer[LFS-LAR]=System.currentTimeMillis();
								packet1[LFS-LAR]=packet;
								byte[] sendbuf=new byte[io+4];
								int in1=0;
								while(in1<io+4){
									sendbuf[in1]=buf5[in1];
									in1++;
								}
								BUFF[LFS-LAR]=sendbuf;
							}
							else{
								DatagramPacket packet = new DatagramPacket(buf5, 20,dir,port);
		           					if(probability())socket.send(packet);
								//System.out.println("send packet with sn : "+SNTS);
								k++;
								timer[LFS-LAR]=System.currentTimeMillis();
								packet1[LFS-LAR]=packet;
								byte[] sendbuf=new byte[20];
								int in1=0;
								while(in1<20){
									sendbuf[in1]=buf5[in1];
									in1++;
								}
								BUFF[LFS-LAR]=sendbuf;
							}							
						}
						else{
							
	        					byte[] buf1 = new byte[1024];
							if(SNTS/1000!=0)SeqNo=""+SNTS;
							else if(SNTS/100!=0)SeqNo="0"+SNTS;
							else if(SNTS/10!=0)SeqNo="00"+SNTS;
							else SeqNo="000"+SNTS;
							copySN(buf1,SeqNo);
					buf1[4]='@';
					buf1[5]='#';
					buf1[6]='$';
					buf1[7]='\0';
	        					DatagramPacket packet = new DatagramPacket(buf1,1024, dir,port);
    							if(probability())socket.send(packet);
							//System.out.println("send packet with sn : "+SNTS);
							timer[LFS-LAR]=System.currentTimeMillis();
							packet1[LFS-LAR]=packet;
							byte[] sendbuf=new byte[20];
							int in1=0;
							while(in1<20){
								sendbuf[in1]=buf1[in1];
								in1++;
							}
							BUFF[LFS-LAR]=sendbuf;			
	       						in.close();
							if(endu){
								endu=false;
								endSN=SNTS;
							}
							sentAllPackets=true;
						}
						LFS=LFS+1;
						SNTS=SNTS+1;					
					}
					byte []buf3=new byte[1024];
					DatagramPacket ACK=new DatagramPacket(buf3,1024);
					try{
						socket.receive(ACK);						
					}
					catch(SocketTimeoutException se){						
						errFlag=true;
						error++;
						if(error<1000)continue;
						else break;	
					}
					SN=getSN(ACK);
					//System.out.println("got an ack with sn : "+SN);					
					if(SN==endSN) {
						transferComplete = true;
						//System.out.println("transfer complete");					
						break;
					}
					count=0;
					if(SN >LAR){								//assuming cumulative acking
						RTT=(long)(alfa*(double)RTT+(1.0-alfa)*(double)(System.currentTimeMillis()-timer[SN-LAR-1]));
						//System.out.println("RTT of this packet : "+(int)(System.currentTimeMillis()-timer[SN-LAR-1]));
						//System.out.println("exponential RTT is : "+RTT);
						if(dup>=3){SWS=ssthresh;				//deflating the window that is increased for fastRetransmit
						System.out.println("current SWS is " +SWS);
						}
						else {
							if(slowStart){SWS++;				//increase window size each time you receive an ack for slow start
							System.out.println("current SWS is " +SWS);
							}
							else{							//and for congestion avoidance at RTT
								if(System.currentTimeMillis()-timePassed>RTT){
									SWS++;
									System.out.println("current SWS is " +SWS);
									timePassed=System.currentTimeMillis();
								}
							}
						}
						dup=0;
						if(SWS>ssthresh && !congestionAvoidance){//move to congestion avoidance
							slowStart=false;
							congestionAvoidance=true;
							System.out.println("went into congestion avoidance");
						}
						count=SN-LAR;
					}
					else{
						dup++;
						//drop the ack
					}
					int oldLAR=LAR;
					LAR=LAR+count;
					if(dup>=3){								//fast retransmit inspite of timeout
															//retransmit the lost packet
						byte []Y=new byte[1024];
						int in1=0;
						while(in1<BUFF[0].length){
							Y[in1]=BUFF[0][in1];
							in1++;
						}
						DatagramPacket X=new DatagramPacket(Y,BUFF[0].length,dir,port);
						System.out.println("FastRetransmit packet : "+getSN(X));
						if(probability())socket.send(X);
						timer[0]=System.currentTimeMillis();
						if(dup==3){							//update the window
							if(SWS/2>2)ssthresh=SWS/2;
							else ssthresh=2;
							SWS=SWS+3;						//inflates the window by 3 since 3 dup acks
						}
						else{ SWS=SWS+1;					//for each next inflate the window by 1
							System.out.println("current SWS is " +SWS);
						}
					}
					if(count>0){
															//update packet[] and timer[] i.e. shift the remaining entries
						int j=0;
						//System.out.println("count : "+count);
						while(count<SNTS-oldLAR){
							BUFF[j]=BUFF[count];
							packet1[j]=packet1[count];
							timer[j]=timer[count];
							j++;
							count++;
						}
						while(j<SNTS-oldLAR){
							packet1[j]=null;
							BUFF[j]=null;
							j++;
						}						
						count=0;
					}
					if(transferComplete){
						//System.out.println("transfer complete");
						break;
					}
				}											//max seq has upper bound of  1033 so bytes 0 to 3 of data are reserved
															//for SN since for each chunk we have sliding window implementation
															//and chunk has at max 1029 packets
		       		in.close();
		        	//socket.close();



			in.close();
			socket.close();
			System.out.println("Transfer of chunk No."+chunkIndex+" of file "+filename+" to "+dir.getHostName()+" is complete.");
			Server.free_slot[index] = true;
			
		}
		catch (SocketException ex) {
			ex.printStackTrace();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		catch (Exception ex) {
			//out.write("exception in server sender thread "+ex);
			ex.printStackTrace();
		}
		//out.write("Completely exiting this thread");
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
	public void copyLast(byte[] sn,byte[] buf2,byte[]buf,int io){
		buf[0]=sn[0];
		buf[1]=sn[1];
		buf[2]=sn[2];
		buf[3]=sn[3];
		int i=4;
		while(i<io+4){
			buf[i]=buf2[i-4];
			i++;
		}
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
	public void copy(byte[] sn,byte[] buf2,byte[]buf){
		buf[0]=sn[0];
		buf[1]=sn[1];
		buf[2]=sn[2];
		buf[3]=sn[3];
		int i=4;
		while(i<1024){
			buf[i]=buf2[i-4];
			i++;
		}
	}
	
	public void copy(byte[] buf,String SN){
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
		if(Math.random()<Server.send_prob){
			return true;
		}
		else return false;
	}
	
	
	public static String retLastToken(String f, String toSearch){
		try{
			File file = new File(f);
			FileReader in = new FileReader(file);
			BufferedReader br = new BufferedReader(in);
			String  c,d;
			while ((c=br.readLine()) !=null)
			{
				StringTokenizer st = new StringTokenizer(c);
				while ((st.hasMoreTokens())){
					d = st.nextToken();
					if(d.equals(toSearch)){
						while(st.hasMoreTokens()){
							d = st.nextToken();
						}
						return d;
					}
				}
			}
		}
		catch(FileNotFoundException fnf){System.out.println("fille"+fnf);}
		catch( IOException ioe){System.out.println("error"+ioe);}
		return null;
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

	public static String SHA1(String text)throws NoSuchAlgorithmException, UnsupportedEncodingException  {
		MessageDigest md;
		md = MessageDigest.getInstance("SHA");
		byte[] sha1hash = new byte[40];
		md.update(text.getBytes(), 0, text.length());
		sha1hash = md.digest();
		return convertToHex(sha1hash);
	}
}