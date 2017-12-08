
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
import java.io.OutputStream;
import java.io.PrintWriter;
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
class Client extends Thread {
	private static String hdr;
	private static DatagramPacket pack;
	private static DatagramSocket sock;
	private static String reply;
	private static byte[] smallMsg = new byte[1024];
	private static byte[] avgMsg = new byte[1024];
	private static DatagramPacket smallPack = new DatagramPacket(smallMsg, smallMsg.length);
	private static DatagramPacket avgPack = new DatagramPacket(avgMsg, avgMsg.length);
	private boolean newPeer = false;
	boolean  updatedUserlist = false;
	boolean updatedMain = false;
	boolean updatedMasterChunk = false;
	File main = new File( "main.txt" );
	File users = new File( "users.txt" );
	File temp = new File("temp.txt");
	File master = new File("masterChunk.txt");
	static Cubbyhole cb1;
	public Client(){
	}
	public void run() {
		try{
			sock = new DatagramSocket(Peer.CPORT);
		}
		catch (SocketException se){System.err.println(se);}
		cb1 = new Cubbyhole();
		Broadcast broadcast = new Broadcast(cb1, sock);
		broadcast.start();
		if(!users.exists()){
			newPeer = true;
			String onlineUserId = ShellUtils.getStringFromShell("**Please enter an online user ip ");
			System.out.println("Starting Sending Requests...");
			//System.out.println("Downloading user.txt and main.txt: \n");
			updatedUserlist = downloadFile(sock, "users.txt",onlineUserId);
			updatedMain = downloadFile(sock, "main.txt",onlineUserId);
			updatedMasterChunk = downloadFile(sock, "masterChunk.txt",onlineUserId);
			try{
				append(InetAddress.getLocalHost().getHostName().toString(),"users.txt");
			}catch(UnknownHostException uhe){}
			try{
				File file = new File("unfinishedDownload.txt");
				file.createNewFile();
				file = new File("shared.txt");
				file.createNewFile();
			}catch(IOException ioe){System.out.println("files can't be created inside new user");}
		}
		//System.out.println("************Checking who is online :**************");
		try{
			String myid = InetAddress.getLocalHost().getHostName();
			System.out.println("I am "+myid);
		}catch(UnknownHostException uhe){}
		if(!newPeer)
			hdr =Peer.magic +" "+Peer.beaconStr+" ";
		else//new peer
			hdr = Peer.magic +" "+Peer.newUserStr+" ";
			DatagramPacket beacon = new DatagramPacket(smallMsg, smallMsg.length);
			beacon.setData(hdr.getBytes());
		//first copy all user in temp.txt
		try{
			copy("users.txt","temp.txt");
		}catch(IOException ioe){}
		long time=System.currentTimeMillis();
		long timeout = 2000;
		pack = new DatagramPacket(smallMsg, smallMsg.length);
		int onlinecount=1;
		cb1.put(beacon);
		// broadcast thread broadcast from temp after every 100ms so it will wait for 500ms
		System.out.println("The following users are online:");
		while((System.currentTimeMillis()-time)<timeout){
			try{
				sock.setSoTimeout(500);//minimize it
				sock.receive(pack);
				reply = new String(pack.getData());
				if(reply.substring(0,4).equals(Peer.magic)){
					if((reply.substring(5,6).equals(Peer.beaconRepStr))||(reply.substring(5,6).equals(Peer.newUserRepStr))){
						System.out.println((onlinecount)+". "+(pack.getAddress().getHostName()));
						onlinecount++;
						deleteLine("temp.txt", pack.getAddress().getHostName().toString());
					}
					else{}//not a beacon msg
					//System.out.println("Client:some wrong msg check for solution "+reply);
				}
				else{}
					//System.out.println("Client:Recieved non-magical reply "+reply);
			}catch(SocketTimeoutException ste){}
			catch(IOException ioe){}
		}
		//System.out.println("******checking who is online is done :)*******");
		if((!updatedUserlist)&&(!updatedMain)&&(!newPeer)){
			//System.out.println("updated main.txt and user.txt\n\n");
			hdr = Peer.magic +" "+Peer.updateStr+" "+ users.lastModified() + " " +main.lastModified();//+ " " +master.lastModified();
			DatagramPacket updateReq = new DatagramPacket(smallMsg, smallMsg.length);
			updateReq.setData(hdr.getBytes());
			String line;
			try{
				FileReader userslist = new FileReader(users);
				BufferedReader br = new BufferedReader(userslist);
				while((line = br.readLine()) != null){
					if(searchLine("temp.txt", line)){}//do nothing read next line
					else{
						if((updatedUserlist)&&(updatedMain))
							break;//updated
						try{
							updateReq.setAddress(InetAddress.getByName(line));
						}catch(UnknownHostException uhe){}
						updateReq.setPort(Peer.SPORT);
						sock.send(updateReq);
						//System.out.println("Sent update request\n\n");
						try {
							sock.setSoTimeout(1000);//minimize it
							//System.out.println("waiting for reply");
							sock.receive(avgPack); 
							reply = new String(avgPack.getData());
							if(reply.substring(0,4).equals(Peer.magic)){//valid msg
								if(reply.substring(5,6).equals(Peer.allreadyUpdated)){
									//System.out.println("users.txt & main.txt are allready updated\n");
									updatedUserlist =true;
									updatedMain = true;
									//updatedMasterChunk = downloadFile(sock, "masterChunk.txt",line);
								}
								else if(reply.substring(5,6).equals(Peer.updateUsers)){
									//System.out.println("Only users.txt needs to be updated\n");
									updatedMain = true;
									updatedUserlist = downloadFile(sock, "users.txt",line);
								}
								else if(reply.substring(5,6).equals(Peer.updateMain)){
									//System.out.println("only main.txt and master.txt needs update\n");
									updatedUserlist = true;
									updatedMain = downloadFile(sock, "main.txt",line);
									updatedMasterChunk = downloadFile(sock, "masterChunk.txt",line);
								}
								else if(reply.substring(5,6).equals(Peer.updateboth)){
									//System.out.println("Your both files needs update\n");
									updatedUserlist = downloadFile( sock, "users.txt",line);
									updatedMain = downloadFile(sock, "main.txt",line);
									updatedMasterChunk = downloadFile(sock, "masterChunk.txt",line);
								}
							}
						}//end try
						catch (SocketTimeoutException ste){
							System.out.println ("Timeout Occurred");
						}
					}//end else
				}//end while
			}catch(FileNotFoundException fnf){}
			catch(IOException ioe){}
		}
		//System.out.println("Total number of onlineuser : "+onlinecount);
		//if((updatedUserlist)&&(updatedMain))
			System.out.println("\nSuccessfully connected to the group");
		/*else
			System.out.println("Not able to update both files due to some reasons");*/
		while(true){
			int options = ShellUtils.getIntFromShell("\nPlease enter your options:\n \t1 for Downloading a file from updated file list\n \t2 for uploading/sharing a file\n \t3 for deleting a file so removing that file from your shared list\n \t4 download unfinished files \n \t5 To unjoin the group.\nEnter your choice -> ");
			if(options == 1){
				String filename = ShellUtils.getStringFromShell("Please enter the filename/part of filename to start download: ");
				String result;
				result = grep("main.txt", filename);
				//assuming user can do any bad thing
				// so check again in main whether file is shared or not check in main.txt
				if(result.equals("-1")){//user mistakenly entered 0
				}
				else{//download file
					// chk whether user has entered correctly
					System.out.println(" starting download of "+result+"...");
					if(search("main.txt",result)){
						filename = result;
						File f = new File(filename);
						if(f.exists()){
							String answer = ShellUtils.getStringFromShell("File with filename "+filename+" all ready exists.\nWant to overwrite ?(yes/no): ");
							while(!(answer.equals("yes")||answer.equals("no")))
								answer = ShellUtils.getStringFromShell("Please enter yes/no.->");
							if(answer.equals("no")){
								continue;
							}
						}

						String finl = Peer.magic + " "+Peer.fileReqStr+" "+filename;
						smallPack.setData(finl.getBytes());
						try{
						copy("users.txt","temp.txt");
						}catch(IOException ioe){}
						time=System.currentTimeMillis();
						timeout = 5000;
						//String tempFile = filename+"_temp.txt";
						boolean now = false;
						//System.out.println("brdcsting");
						cb1.put(smallPack);//done broadcst 
						//now listen
						int filecount = 0;
						String array[] = new String[105];
						while((System.currentTimeMillis()-time)<timeout){//if user is not putting anything else to download try downloading these if they are not complete yet
							try{
							byte[] newbuf = new byte[1024];
							DatagramPacket pack1 = new DatagramPacket(newbuf,newbuf.length);
							sock.receive(pack1); //they will send magic filename filesize totalchunks chunks_I_have
							reply = new String(pack1.getData());
							//System.out.println("Client:got a packet from "+pack.getAddress().getHostName()+" Saying "+reply);

							int i = 0;
							StringTokenizer st = new StringTokenizer(reply);
							while (st.hasMoreTokens()) {
								array[i] = st.nextToken();
								i++;
							}
							if(array[0].equals(Peer.magic)){
    								//String filesize = array[3];
								String totalchunks = array[3];
								if(array[1].equals(Peer.YfileReqRepStr)){
									System.out.println(pack1.getAddress().getHostName()+" user has the file "+filename);
									if(filecount == 0){//first reply construct basic file
										//append filename filesize total number of chunks count of chunks at top
										    try {
										        File file = new File(filename+"_temp.txt");
										       	if(file.exists())
										       		file.delete();
    											file.createNewFile();

											//append it to unfinished download
											//do construction from there only
											String appendStr=filename + " " + totalchunks;
											//System.out.println("Client: Appending "+appendStr+" to "+filename+"_temp.txt");
											int Totalchunks = Integer.valueOf( totalchunks ).intValue();
											for(int k=0; k< Totalchunks;k++)
												appendStr = appendStr + " " +k;
											append(appendStr,"unfinishedDownload.txt");
											//append(appendStr,filename+"_temp.txt");
											filecount++;
											}
										    catch (IOException e) {
											System.out.println("could not create "+filename+"_temp.txt");
										    }
										}
										
										filecount++;
										deleteLine("temp.txt", pack1.getAddress().getHostName().toString() );
										reply = pack1.getAddress().getHostName() + " " + reply.substring(8,reply.length());
										now = true;
										reply=reply.trim();
										//System.out.println("appending to _temp.txt "+reply);
										append(reply,filename+"_temp.txt");
										//pw.println(reply);//"Adding a line..."
										//creating lists in file so that it can be used finally to download
										//or else append(reply,filename+"_temp.txt");
									
								}
								else{//he does not have file
									deleteLine("temp.txt", pack1.getAddress().getHostName().toString());
									//System.out.println(pack.getAddress().getHostName()+" has replied he has not the file saying "+reply);
								}
							}
							else{}
								//System.out.println("Recieve non magical reply from"+reply);
   						}catch(SocketTimeoutException ste){}
						catch(IOException ioe){}
						}//end creating filelist
					//downloadFile(sock,filename,"csews6.cse.iitk.ac.in");
						if(now)//temporary file list has been created download using that file
							downloadFrom(filename,Integer.parseInt(array[3]));//,Integer.parseInt(array[3]));
						else{//no user with that file is online currently so try latter
							//append((filename + " 0"),"unfinishedDownload.txt");
							System.out.println("No user with this file is online currently. \nAppended in unfinishedDownload.txt");
						}

					}//end if
				}
			}//end if(options) 
			
			else if(options==2){//uploading file
				String filename = ShellUtils.getStringFromShell("Please enter the file name  to upload: ");
				int j=0;
				filename = filename.trim();
				File f = new File(filename);
				if(!f.exists()){
					filename = ShellUtils.getStringFromShell("This file does not exist.\nPlease re-enter the file name: ");
					f = new File(filename);
					if(!f.exists()){
						System.out.println("Even this file does not exist");
					continue;
					}
				}
				filename = filename + " " + filename;
				while(filename.charAt(j) != ' ')j++;
				boolean AllreadyThr = search("shared.txt",filename.substring(0,j));
				if(AllreadyThr)
					System.out.println("OOPS!! This file is already shared by you");
				else{
					appendSharedFile(filename, "shared.txt");//first get file size and number of chunks increment it in filename then write it to shared.txt
					AllreadyThr = search("main.txt", filename.substring(0,j));
					if(AllreadyThr){
						String Line = CmpltLine("main.txt",filename.substring(0,j));
						incrementCount("main.txt",filename.substring(0,j));
						String increment=Peer.magic + " "+Peer.fileUploadStr+" "+Line;//00 is used for file request
						smallPack.setData(increment.getBytes());
						try{
						copy("users.txt","temp.txt");
						time=System.currentTimeMillis();
						cb1.put(smallPack);//done broadcst
						while((System.currentTimeMillis()-time)<timeout){
							sock.receive(pack); 
							reply = new String(pack.getData());
							if(reply.substring(0,4).equals(Peer.magic)){
								if(reply.substring(5,7).equals(Peer.fileUploadRepStr)){
									deleteLine("temp.txt", pack.getAddress().getHostName().toString() );
								}
								else{}//not a beacon msg 
									//System.out.println("some wrong msg check for solution"+reply);
							}
								//System.out.println("Recieve msg fr om some other type not from our sources"+reply);
						}
						}catch(IOException ioe){}
						//catch(SocketTimeoutException ste){}
					}
					else{//it's a new share
						append((filename.substring(0,j)+" 1"),"main.txt");
						makeChunks(filename.substring(0,j));
						//again broad cast it to all so that the can also append it
						String insert = Peer.magic + " "+Peer.newfileUploadStr+" "+filename;//00 is used for file request
						smallPack.setData(insert.getBytes());
						try{
						copy("users.txt","temp.txt"); //?????????????
						time=System.currentTimeMillis();
						cb1.put(smallPack);//done broadcst
						while((System.currentTimeMillis()-time)<timeout){
							sock.receive(pack); 
							reply = new String(pack.getData());
							if(reply.substring(0,4).equals(Peer.magic)){
								if(reply.substring(5,7).equals(Peer.newfileUploadRepStr)){
									//System.out.println(pack.getAddress().getHostName()+" user has inserted this new file shared by you in his main.txt \n");
									deleteLine("temp.txt", pack.getAddress().getHostName().toString());
									sendmasterChunk(sock,pack.getAddress());
								}
							else{}
								//System.out.println("some wrong msg check for solution"+reply);
							}
								//System.out.println("Recieve msg fr om some other type not from our sources"+reply);
						}
						}catch(IOException ioe){}
						//catch(SocketTimeoutException ste){}
					}

				}//end else
				System.out.println("The file is succesfully uploaded");
			}//end options 

			else if(options==3){//deleting a file from your sharing
				String filename = ShellUtils.getStringFromShell("Please enter the filename or part of filename which you want to delete ");
				String result;// = new String[2];
				result=grep("shared.txt",filename);
				// chk it in shared .txt if not there print it 
				//else delete that entry and send again a packet to all user about informing this

				if(result.equals("-1")){//users do not want to delete these files
				}
				else {//delete that file
					String Filename = result;
					//String lineWithFilename = RetLineSearch("shared.txt",Filename);
					//System.out.println(" filename "+result);
					//search in shared.txt for its entry
					boolean isThr = search( "shared.txt",Filename);
					if(isThr){
						deleteSearchLine("shared.txt",Filename);
						//while(lineWithFilename.charAt(i)!=' ')i++;
						String Line = CmpltLine("main.txt",Filename);//returns the line with this filename	
						//System.out.println("Line in main is "+Line);
						decrementCount(Filename,"main.txt");
						//if the count become 0 delete that entry completely
						//broadcst it to all
						String finl=Peer.magic + " "+Peer.fileDeleteStr+" "+Line;//00 is used for file request
						smallPack.setData(finl.getBytes());
						try{
						copy("users.txt","temp.txt");
						}catch(IOException ioe){}
						time=System.currentTimeMillis();
						timeout = 5000;
						cb1.put(smallPack);//done broadcst 
						//now listen

						while((System.currentTimeMillis()-time)<timeout){
						try{
							sock.receive(pack); 
						reply = new String(pack.getData());
						if(reply.substring(0,4).equals(Peer.magic)){
	 						if(reply.substring(5,7).equals(Peer.fileDeleteRepStr)){
								//System.out.println(pack.getAddress().getHostName()+" user has decremented in his main.txt");
								deleteLine("temp.txt", pack.getAddress().getHostName().toString());
								}
								else{}//not a beacon msg 
									//System.out.println("some wrong msg check for solution"+reply);
							}

						}catch(IOException ioe){}
  						}

					}
					else
						System.out.println("sorry the file is not shared by you so you can't remove it from sharing it \n");
				}
			}//end option 

	    else if(options==4){
		try{
		    File inputFile = new File("unfinishedDownload.txt");
		    FileReader in = new FileReader(inputFile);
		    BufferedReader br = new BufferedReader(in);
		    String  c = "";
		    c=br.readLine();
		   while(c != null && (!c.equals(""))){
		    int i=0;
		    while(c.charAt(i)!=' ')i++;
		    String filename = c.substring(0,i);
		    String finl=Peer.magic + " "+Peer.fileReqStr+" "+filename;
		    smallPack.setData(finl.getBytes());
		    copy("users.txt","temp.txt");
		    time=System.currentTimeMillis();
		    timeout = 5000;//more or less check
		    boolean now = false;
		    cb1.put(smallPack);//done broadcst 
		    //now listen
		    int filecount = 0;
		    //String array[] = new String[4];
		    String totalchunks = "-1";
		    while((System.currentTimeMillis()-time)<timeout){
		    	try{
		    	byte[] newbuf = new byte[1024];
				DatagramPacket pack1 = new DatagramPacket(newbuf,newbuf.length);
			    sock.receive(pack1); //they will send magic filename filesize totalchunks chunks_I_have
			    reply = new String(pack1.getData());
			    //System.out.println("Client:got a packet from "+pack.getAddress().getHostName()+" Saying "+reply);
			    i = 0;
			    StringTokenizer st = new StringTokenizer(reply);
			    while (i<3) {
				st.nextToken();
				i++;
			    }
			    totalchunks = st.nextToken();
			    String array2[] = new String[Integer.parseInt(totalchunks)+5];
			    i=0;
			    st = new StringTokenizer(reply);
			    while (st.hasMoreTokens()) {
				array2[i] = st.nextToken();
				i++;
			    }
			    if(array2[0].equals(Peer.magic)){
				if(array2[2].equals(filename)){
				    System.out.println(pack1.getAddress().getHostName()+" user has the file "+filename);
				    if(filecount == 0){//first reply construct basic file
					//append filename filesize total number of chunks count of chunks at top
					try {
					    File file = new File(filename+"_temp.txt");
					    file.createNewFile();
					}
					catch (IOException e) {
					    System.out.println("could not create "+filename+"_temp.txt");
					}
				    }
				    filecount++;
				    deleteLine("temp.txt", pack1.getAddress().getHostName().toString() );
				    reply = pack1.getAddress().getHostName() + " " + reply.substring(8,reply.length());
				    now = true;
				    reply=reply.trim();
				    append(reply,filename+"_temp.txt");
				}
				else{//he does not have file
				    deleteLine("temp.txt", pack1.getAddress().getHostName().toString());
				    //System.out.println(pack.getAddress().getHostName()+" has replied he has not the file saying "+reply);
				}
			    }
			    
			}catch(SocketTimeoutException ste){}
			catch(IOException ioe){}
		    }//end while
		    if(now){
			if(totalchunks.equals("-1")){
			    System.out.println("Got an invalid data");
			    continue;
			}
			else{
			    downloadFrom(filename,Integer.parseInt(totalchunks));
			}
		    }
		    else{//no user with that file is online currently so try latter
			System.out.println("Again no user with this file is online. Try later");
		    }
		    if((c = br.readLine())!= null){
			String answer = ShellUtils.getStringFromShell("Wants to download another unfinished file?(yes/no) ->");
			if(answer.equals("no"))
			    break;
		    }
		}//end while(c != null)
		}//end try
		catch (Exception e){
		    System.out.println("Exception in option 4 "+e);
		}
	    }//ends option 4

			else if(options==5){
				System.out.println("Thanks for using the application.");
				File fileToDel = new File("users.txt");
				fileToDel.delete();
				fileToDel = new File("main.txt");
				fileToDel.delete();
				fileToDel = new File("masterChunks.txt");
				fileToDel.delete();
				fileToDel = new File("unfinishedDownload.txt");
				fileToDel.delete();
				fileToDel = new File("temp.txt");
				fileToDel.delete();
				fileToDel = new File("shared.txt");
				fileToDel.delete();
				System.exit(0);
			}//ends option 5
		}//end true loop
	}//end run
//methods used by client


        public static boolean downloadFile(DatagramSocket sock, String filename , String address){
                //first send download request   
                int count =0;//send file download request 4 times
                boolean notStarted = true, flag = false;
                String hdr;
                while((count < 4)&&(notStarted)){
                        count++;
                        try{
                                InetAddress addr = InetAddress.getByName(address);
                                if(filename.equals("users.txt"))
                                        hdr = Peer.magic +" "+Peer.sendUserStr;
                                else  if(filename.equals("main.txt"))
                                        hdr = Peer.magic + " "+Peer.sendMainStr;
				else
                                        hdr = Peer.magic + " "+Peer.sendMasterStr;
                                byte[] buf = new byte[1600];
                                
                                buf = hdr.getBytes();
                                DatagramPacket packet = new DatagramPacket(buf, buf.length, addr, Peer.SPORT);
                                sock.send(packet);
                                File fp1 = new File(filename);
                                FileOutputStream fp = new FileOutputStream(filename);      
                              //size of file
                                //System.out.println("file request is sent everything ok now downloading the file "+filename);
                                while(true){
                                        sock.setSoTimeout(1000);
                                        try {//you may also check whether it is correct file packet or something else
                                        		byte[] receiveData = new byte[1600];
                                        		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);  // 5. create datagram packet for incoming         datagram
                                                sock.receive(receivePacket);  
                                        //String modifiedSentence = new String(receivePacket.getData()); // 7. retrieve the data from buffer
                                                InetAddress returnIPAddress =  receivePacket.getAddress();  // get ipddress
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
                                                DatagramPacket pkt = new DatagramPacket(buff, buff.length, returnIPAddress, port);
                                                sock.send(pkt);
                                                //System.out.println("ACK sent \n");
						if(see.charAt(0)=='@' && see.charAt(1)=='#' && see.charAt(2)=='$'){  //last packet	       
         		        			flag =true;
         						break;
         					}
						else
							 fp.write(d.getBytes());
                                        }//end try
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
				buf2 = (Peer.magic +" "+Peer.fileDownloadedAck+" "+filename).getBytes();

				try{
				InetAddress addr = InetAddress.getByName(address);
				DatagramPacket ackpack = new DatagramPacket(buf2, buf2.length, addr, Peer.SPORT);
				sock.send(ackpack);
				} catch(Exception e){
					System.out.println(e);
				}
		}
                return flag;
        }//end method


public static void sendmasterChunk(DatagramSocket sock, InetAddress addr){
		//System.out.println("inside send file method for file ");
        	byte bytes[] = new byte[1600];
		try{
		File inputFile = new File("masterChunk.txt");
		InputStream in = new FileInputStream(inputFile);
		int io;
		while ((io = in.read(bytes, 0, 1600)) > 0){
			DatagramPacket packet;
			//System.out.println("hr1");
			if(io < 1600){
					//System.out.println("hr2");
					byte byte2[] = new byte[io];
					int j = 0;
					while(j < io){
						byte2[j] = bytes[j];j++;
					}
					packet = new DatagramPacket(byte2,io, addr,Peer.SPORT);
			}
			else
				packet = new DatagramPacket(bytes, bytes.length, addr,Peer.SPORT);
			boolean lost = true;
			byte[] receiveData = new byte[1600];
			int i=0;
			while(lost==true)
			{
				try{
					//System.out.print("Client:In sendfile. Now sending ");
					sock.send(packet);
				}
				catch (IOException ex){ System.err.println(ex);}
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try {
					sock.setSoTimeout(5000);
					sock.receive(receivePacket);
					InetAddress returnIPAddress = receivePacket.getAddress();  // get ipddress
					//int port = receivePacket.getPort();  // get port number
					lost = false;
				}
				catch (SocketTimeoutException ste){
					System.out.println ("Timeout Occurred: Packet assumed lost");
					System.out.println ("Server: message "+" re-attempt no." + i);
					lost = true;
					i++;
					if(i==5){	
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
		DatagramPacket pac = new DatagramPacket(bytes, bytes.length, addr,Peer.SPORT);
		try{
			//System.out.print("Client:In sendfile. Now sending "+new String(bytes));
			sock.send(pac);
		}
		catch (IOException ex){ System.err.println(ex);}
		}catch(IOException ioe){}
	}//end method filerequest
		


//String Line = CmpltLine("main.txt",filename.substring(0,j));//returns the line with this filename	
	public static String CmpltLine(String filename, String part){
		try{
		File f = new File(filename);
		FileReader in = new FileReader(f);
		BufferedReader br = new BufferedReader(in);
		String  c;
		while ((c=br.readLine()) !=null)
		{
			if(c.indexOf(part) != -1)
				return c;
		}
		}catch(FileNotFoundException fnfe){}
		catch(IOException ioe){}
		return "error";
	}


	public static String grep(String filename, String part){
		String reply= "-1";
		String[] d = new String[20];
		int temp;
		try{
		File f = new File(filename);
		FileReader in = new FileReader(f);
		BufferedReader br = new BufferedReader(in);
		// Continue to read lines while
		// there are still some left to read
		String  c;int count = 1; int choice = 0;
		while ((c=br.readLine()) !=null)
		{
			if(c.indexOf(part) != -1){
				StringTokenizer st = new StringTokenizer(c);
				d[count] = st.nextToken();
				System.out.println(count+" " +d[count]);
				count++;
			}
		}
		if(count == 0){
			reply = "-1";
			System.out.println("\t\tno match found try again\t\t");
			return "-1";
		}
		else{
			choice = ShellUtils.getIntFromShell("Please enter the file number to select the file or enter 0 to gack to main menu ->");
			reply.trim();
			if(choice<=count&&choice>0)
				reply = d[choice];
			else
				reply = "-1";//filename is saved
		}
		}catch(FileNotFoundException fnf){System.out.println("fille"+fnf);}
		catch( IOException ioe){System.out.println("error"+ioe);}
		return reply;
	}



		public void makeChunks(String newfilename){
                        int mb  = 1024*1024;
                        byte[] waste = new byte[mb];
                        byte[] waste2 = new byte[mb];
                        try{
                        FileInputStream fstream = new FileInputStream(newfilename);
                        DataInputStream in = new DataInputStream(fstream);
                        //int dummy = chunkIndex; 
                        String temp, strToAppend, sha;
                        int chunkNo = 0;
			int io = in.read(waste,0,mb);
                        while(io>0){//updated the piointer for getting the required chunk
                                temp = new String(waste);
                                if(io < mb){
                                        for(int i=0;i<io;i++)
                                                waste2[i] = waste[i];
                                        temp = new String(waste2);
                                }
                                sha = client_threads.SHA1(temp);
                                strToAppend = newfilename+" "+chunkNo+" "+sha;
                                append(strToAppend, "masterChunk.txt");
                                chunkNo++;
				io = in.read(waste,0,mb);
                        }
			//System.out.println("\t\tExiting makeChunk\t");
                        }
                        catch (Exception e ){System.out.println(e);}
        }
        


	
	public void deleteLine(String file, String lineToRemove) {
	//System.out.println("inside delete line method line to delete is : "+lineToRemove);
    	try {
			File inFile = new File(file);
			if (!inFile.isFile()) {
        		System.out.println("Parameter is not an existing file");
        		return;
      		}
      //Construct the new file that will later be renamed to the original filename.
      		File tempFile = new File(inFile.getAbsolutePath() + ".tmp");
      		BufferedReader br = new BufferedReader(new FileReader(file));
      		PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
      		String line = null;
      		//Read from the original file and write to the new
      		//unless content matches data to be removed.
      		while ((line = br.readLine()) != null) { 
			//System.out.println("Current line is : "+line);//for debugging
       			if (!line.trim().equals(lineToRemove)) {
				//System.out.println("line has been copied so not deleted: ");//for debugging
          			pw.println(line);
          			pw.flush();
        		}
      		}
      		pw.close();
      		br.close();
      		//Delete the original file
      		if (!inFile.delete()) {
        		System.out.println("Could not delete file");
        		return;
      		}
      		//Rename the new file to the filename the original file had.
      		if (!tempFile.renameTo(inFile))
        		System.out.println("Could not rename file");
    	}
    	catch (FileNotFoundException ex) {
      		ex.printStackTrace();
    	}
    	catch (IOException ex) {
			ex.printStackTrace();
    	}
	} //ends method

	public static void copy(String fileSrc, String fileDst) throws IOException {
	//System.out.println("Copying file: src:"+fileSrc+" to dst:"+fileDst);
	try{
	File src = new File(fileSrc);
	File dst = new File(fileDst);
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
    
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
            out.write(buf, 0, len);
        in.close();
        out.close();
	}catch(FileNotFoundException fnfe){}
	
    }
 
	public static boolean searchLine(String file, String query){
		try{
    	// Open the file that is the first 
    	// command line parameter
    	FileInputStream fstream = new FileInputStream(file);
    	// Get the object of DataInputStream
    	DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
    	String strLine;
    	//Read File Line By Line
    	while ((strLine = br.readLine()) != null)   {
      		// Print the content on the console
			if(strLine.equals(query)){
				in.close();
				return true;
			}
      		//System.out.print(strLine);
    	}
    	//Close the input stream
    	in.close();
    }catch (Exception e){//Catch exception if any
		System.err.println("Error: " + e.getMessage());}
	return false;
	}
	
	public static boolean search(String f, String toSearch){
		try{
		File file = new File(f);
		FileReader in = new FileReader(file);
		BufferedReader br = new BufferedReader(in);
		// Continue to read lines while
		// there are still some left to read
		String  c;
		while ((c=br.readLine()) !=null)
		{
			StringTokenizer st = new StringTokenizer(c);
			if(st.nextToken().equals(toSearch))
				return true;
		}
		}catch(FileNotFoundException fnf){System.out.println("fille"+fnf);}
		catch( IOException ioe){System.out.println("error"+ioe);}
		return false;
	}
	//append(appendStr,filename+"_temp.txt");
	public static void append(String toAppend, String filename){
		//System.out.println("inside append "+filename);
		try{
		File file = new File(filename);
		String last = getAppendedContents(file,toAppend);
		setContents(file,last);
		}catch(FileNotFoundException fnfe){}
		catch(IOException ioe){}
	}//done appending

	public static String getAppendedContents(File aFile, String toAppend) {
    //...checks on aFile are elided
		StringBuilder contents = new StringBuilder();
    
		try {
		//use buffering, reading one line at a time
		//FileReader always assumes default encoding is OK!
		BufferedReader input =  new BufferedReader(new FileReader(aFile));
		try {
			//System.out.println("in try for stringto append "+toAppend+" for file "+aFile.getName());
			String line = null; //not declared within while loop
			while (( line = input.readLine()) != null){
				//System.out.println("inside appending this line " + line + "$");
				if(line.equals("")) continue;
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
	catch (IOException ex){
		ex.printStackTrace();
	}
    
	return contents.toString();
	}	
	
	public static void setContents(File aFile, String aContents)
                                 throws FileNotFoundException, IOException {
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
		}
		finally {
			output.close();
		}
	}

	
//incrementCount("main.txt",filename.substring(0,j));//increment count for this shared file
	//this method is also used in server b/c he also do these when receive such request.
	public static void incrementCount(String filename, String toIncrement){
		try{
		File file = new File(filename);
		String last = getIncrementedContents(file,toIncrement);
		//System.out.println("calling setcontents from increment count ");
		setContents(file,last);
		}catch(FileNotFoundException fnfe){}
		catch(IOException ioe){}
	}//done incrementing

	public static String getIncrementedContents(File aFile, String toIncrement) {
    //...checks on aFile are elided
		StringBuilder contents = new StringBuilder();
		int initialCount, finalCount;
    
		try {
		//use buffering, reading one line at a time
		//FileReader always assumes default encoding is OK!
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
	catch (IOException ex){
		ex.printStackTrace();
	}
    
	return contents.toString();
	}	
	
	public static void deleteSearchLine(String filename, String lineWithFilename){
		//System.out.println("in deletesrchline");
		try{
		File file = new File(filename);
		String last = getDeletedSearchLine(file,lineWithFilename);
		setContents(file,last);
		}catch(FileNotFoundException fnfe){}
		catch(IOException ioe){}
	}//done decrementing

	public static String getDeletedSearchLine(File aFile, String lineWithFilename) {
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
						System.out.println("deleted "+lineWithFilename);
						//System.out.println("line in delete method is "+line);
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
		
		//	decrement(lineWithFilename.substring(0,i),"main.txt");
		//if the count become 0 delete that entry completely

	public static void decrementCount(String toDecrement, String filename){
		try{
		File file = new File(filename);
		String last = getDecrementedContents(file,toDecrement);
		//System.out.println("inside decrement count calling set contents ");
		setContents(file,last);
		}catch(FileNotFoundException fnfe){}
		catch(IOException ioe){}
	}//done decrementing
	

	public static String getDecrementedContents(File aFile, String toDecrement) {
    //...checks on aFile are elided
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
	catch (IOException ex){
		ex.printStackTrace();
	}
    
	return contents.toString();
	}	
	
	
	
		//appendSharedFile(filename, "shared.txt");//this method will find the size of file append line in shared.txt in its format calculating chunks and all that here filename = filename location
		
	public static void appendSharedFile(String tfilename, String filename){
		String line="";
		long file_size = 0;
		int totalChunks = 0;
		StringTokenizer st = new StringTokenizer(tfilename);
		line = st.nextToken();
		String path = st.nextToken();
    	File f = new File(path);  
		if(f.exists()){
   
   			file_size = f.length();
   			totalChunks = (int)(file_size/(1024*1024));
   			if(!(file_size %(1024*1024)==0))
   			totalChunks++;
   			line = line + " " + totalChunks;
   			int temp = 0; 
   			while(temp < totalChunks){
   				line = line + " " + temp;
   				temp++;
   			}
   			line = line + " " + path;
   			append(line, "shared.txt");
		}
	}
	
  
	//	downloadFrom(filename+"_temp.txt");//download from this file which contains information about where to get chunks
//this is to send request for chunks in least comon chunk first order

public static void downloadFrom(String filename, int totalChunks){
                Downloader downloader = new Downloader(filename,totalChunks);
                downloader.start();
        }


}//end client class
