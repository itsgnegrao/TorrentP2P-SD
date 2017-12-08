package proj_base;


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
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.StringTokenizer;
/**
 *
 * @author itsgnegrao
 */

class Downloader extends Thread{

	public String[] user;
	public String  filename;
	//private String  tempfilename;
	public int num_users=0;
	public static int max_slots=4;
	public static int totalChunks;
	private int chunkRem;
	public static String magic = "1234";
	public static int chunkArray[];
	public static boolean [] timeout=new boolean[max_slots];	
	public static boolean [] slot_free=new boolean[max_slots];
	public static boolean [] under_process;
	public static boolean [] thisSession;
	public static boolean [] downloaded;
	public static boolean [] userBusy;
	public static boolean [] userDead;
	public static final int MAX_CHUNKS = 103;    // Max size which can be downloaded is 100MB. Change accordingly
	public static boolean allUserBusy = false;
	public static boolean allSlotsBusy = false;
	public static client_threads [] clt=new client_threads[max_slots];
	public static long TIMEOUT = 5000;
	//public static  int debug[];
	public static int deadCount[];
	public Downloader(String filename, int i){
		this.filename=filename;
		totalChunks=i;
	}
	public void run(){
		num_users = 0;
		int i = 0;
		int count = 0; //no. of chunks to downloaded in this session
		System.out.println("In class Downloader for file "+filename);
		//int chunkRem=intelligentParser(filename+"_temp.txt");  //chunkREm = total chunks in file
		File f = new File("unfinishedDownload.txt");
		StringTokenizer st;
		String c;
		try{
		FileReader in = new FileReader(f);
		BufferedReader br = new BufferedReader(in);
		c = br.readLine();
		st = new StringTokenizer(c);
		while(c !=null){
			//System.out.println("inside downloader while loop line in unfinished download is " + c);
			if(st.nextToken().equals(filename)){
				//System.out.println(" file name found break");
				break;
			}
			c = br.readLine();
			if(c != null)
			st = new StringTokenizer(c);
		}
		//System.out.println("line in unfinished is "+c);
		st.nextToken();
		//initializing
		//st.nextToken();
		while(st.hasMoreTokens()){
			count++;
			st.nextToken();
		}
		under_process = new boolean[count];
		//chunkBusy = new boolean[count];
		downloaded = new boolean[count];
		thisSession = new boolean[count];
		//debug = new int[count];
		chunkArray = new int[count];
		int rarest[] = new int[count]; //sorted array of chunks
		while(i<count){
			under_process[i] = false;
			thisSession[i] = true;
			downloaded[i] = false;
			under_process[i] = false;
			//debug[i] = 0;
			rarest[i] = 0;
			i++;
		}
		StringTokenizer st1 = new StringTokenizer(c);
		//System.out.println("line" +c);
		st1.nextToken();
		st1.nextToken();
		//System.out.println("The chunkarray has "); 
		i=0;
		while(st1.hasMoreTokens()){
			//System.out.println(" i is "+i);
			chunkArray[i] = Integer.parseInt(st1.nextToken());
			System.out.print(chunkArray[i]+" ");
			i++;
		}
		//System.out.println("out side");
		File fil = new File(filename+"_temp.txt");
		FileReader fr  = new FileReader(fil);
		BufferedReader bfd = new BufferedReader(fr);
		while((c = bfd.readLine())!=null){
			StringTokenizer sti = new StringTokenizer(c);
			if(sti.nextToken().equals(InetAddress.getLocalHost().getHostName()))
				continue;
			num_users++; // to count total users 
		}
		userBusy = new boolean[num_users];
		userDead = new boolean[num_users];
		deadCount = new int [num_users];
		for(int p = 0;p<num_users;p++){
			userBusy[p]=false;
			userDead[p] = false;
			deadCount[p] = 0;
		}
		//System.out.println("\n numusers = "+num_users+" users has some part");
		String line;
		fr  = new FileReader(fil);
		bfd = new BufferedReader(fr);
		while((line = bfd.readLine())!=null){
			st = new StringTokenizer(line);
			//System.out.println("line is "+line+"$");
			if(st.nextToken().equals(InetAddress.getLocalHost().getHostName()))
				continue;
			st.nextToken();
			System.out.println("Total chunks of "+filename +" are "+st.nextToken());
			while(st.hasMoreTokens()){
				String tmp = st.nextToken();
				for(int k =0;k < count;k++){
					//System.out.println("in ");
					if(Integer.parseInt(tmp) == chunkArray[k]){
						rarest[k]++;
						break;
					}
				}
			}
		}
		for(int j = 1; j< count;j++){
			for(int k = j; k > 0; k--){
				if(rarest[k] < rarest[k-1]){
					int temp2 = chunkArray[k-1];
					chunkArray[k-1] = chunkArray[k];
					chunkArray[k] = temp2;
					int temp = rarest[k-1];
					rarest[k-1] = rarest[k];
					rarest[k] = temp;
				}
				else
					break;
			}
		}

		System.out.println("In downloader thread num users"+num_users);
		//System.out.println("In downloader thread totalChunks"+totalChunks);
		//chunkBusy=new boolean[num_users];
		//int next_chunk_to_be=0,next_user_to_be=0,j=0;
		//under_process=new boolean[totalChunks];
		for(i=0;i<max_slots;i++){
			timeout[i]=false;
			slot_free[i]=true;
		}
		chunkRem = count;
		//System.out.println("chunkrem: "+chunkRem);
		int cnt = 0;
		//while there are still chunks to download
		//=> either all chunks (count) are not downlaoded or some chunks can't be downloaded in this session or there are some under process chunks
		while(thrRChunksToDow(count)){
		//while(chunkRem>0){
			for(int k = 0; k < chunkArray.length;k++){
				allSlotsBusy = true;
				for(int y = 0; y < max_slots; y++){
					if(slot_free[y]){
						allSlotsBusy = false;
						break;
					}
				}
				allUserBusy = true;
				for(int p = 0;p<num_users;p++){
					if(!userBusy[p]){
						allUserBusy = false;
						break;
					}
				}

				while(allUserBusy) ;
				while(allSlotsBusy);
				long time;// = System.currentTimeMillis();
				//while((System.currentTimeMillis()-time)<250) ;
				if(!(downloaded[k])&&!(under_process[k])&&(thisSession[k])){
					//System.out.println("calling for chunk " +chunkArray[k]);
					under_process[k] = true;
					cnt++;
					searchHostsWithChunk(filename,k);
					/*time = System.currentTimeMillis();
						while((System.currentTimeMillis()-time)<100);*/
				}
        	}//ends big for
			
		}//ends while
		//System.out.println("called searchHostsWithChunk "+cnt+" number of times. chunkrem is "+chunkRem);
		//System.out.println("printing debug array:");
		/*for(int q=0;q<count;q++)
			System.out.print("debug["+q+"]="+debug[q]);
		System.out.println();*/
		for(int q = 0; q< count; q++){
			if(downloaded[q]){
				String toDelete = ""+chunkArray[q];
				deleteChunk(filename, toDelete);
			}
		}
		System.out.println("Downloading of this file in this session is complete");
		File tempFile = new File(filename+"_temp.txt");
		tempFile.delete(); //uncomment later
		}//ends try 1
		catch(Exception e){System.out.println(e);}
                
	}//ends run
public static void searchHostsWithChunk(String filename,int chunkIndex){
		//System.out.println("In searchHostsWithChunk for filename "+filename+" and chunkIndex "+chunkIndex);
		int userCount = 0, usersWithThisChunk=0, deadCount=0;boolean flag = false;
                try{
                        FileReader in = new FileReader(filename+"_temp.txt");
                        BufferedReader br = new BufferedReader(in);
			String c = ""; int i=0,tok = 0;
			//System.out.println("in srchhost2");
			while((c=br.readLine())!=null && !c.equals("")){
			//System.out.println(c+"printed");
			StringTokenizer st = new StringTokenizer(c);
			//st.nextToken();st.nextToken();st.nextToken();
				String d[] = new String[MAX_CHUNKS];
				while(st.hasMoreTokens()){
					//System.out.println("the user no. "+userCount+" is not busy checking whether he has chunk or not");
					String user = st.nextToken();
					if(user.equals(InetAddress.getLocalHost().getHostName()))
				continue;
					d[i] = user;  i++;
					//System.out.println("d["+i+"]="+d[i-1]);
					if(i>3){
						tok = Integer.parseInt(d[i-1]);
						if(tok==chunkArray[chunkIndex]){
						usersWithThisChunk++;
						if(!(userDead[userCount])){
							flag = true;
							if(!(userBusy[userCount])){
							//start chunk no. k download from d[0] for file in d[1]
							    for(int j=0;j<max_slots;j++)
								if(slot_free[j]){
								System.out.println("Downloading chunk No."+chunkArray[chunkIndex]+" from "+d[0]);
								//under_process[chunkIndex] = true;
								//if(!downloaded[chunkIndex]){
								clt[j] = new client_threads(filename,j,chunkIndex,d[0],userCount);
								userBusy[userCount] = true;
								slot_free[j]=false;
								clt[j].start();
								//}
								//break;
								return;
								}
								/*else if(j==max_slots){
									System.out.println("all slots are busy");
									long time = System.currentTimeMillis();
									while(System.currentTimeMillis()-time<TIMEOUT/2);
									searchHostsWithChunk(filename, chunkIndex);
								}*/
								}//allocated a thread to download the chunk
							    }
							else{
								//System.out.println("user is dead");
								deadCount++;
								}
							break;
							}
						//else if(tok>chunkIndex) break; //the chunks need to be sorted
				}// end st has more token
				
			}
			userCount++; i = 0;//System.out.println("usercount "+userCount);
		}in.close();
		if((!flag)&&(usersWithThisChunk==deadCount)||(usersWithThisChunk == 0)){
			thisSession[chunkIndex]=false;
			System.out.println("this chunk can't be downloaded in this session"+chunkArray[chunkIndex]);
			
		}
		if(flag){
			under_process[chunkIndex] = false;
		}
		}
		catch(Exception e){System.out.println("in searchHostsWithChunk:"+e );}
		/*if(userCount==num_users){
			//chunkBusy[chunkIndex] = true;
			System.out.println("finished hr");
		}*/
		under_process[chunkIndex] = false;
	
	}
	
	public static boolean thrRChunksToDow(int count){
		int i;
		for(i = 0; i < count; i++){
			if(!(downloaded[i])&&thisSession[i]){
				return true;
			}
		}
		//System.out.println("download chunk index: "+i);
		return false;
	
	
	}
    public static void deleteChunk(String filename, String toDelete){
	//System.out.println("inside delete chunk deleteing chunk: "+toDelete);
	File file = new File("unfinishedDownload.txt");
	String last = getDeletedChunk(filename,toDelete, file);
	try{
		//System.out.println("inside delete chunk calling setcontents");
	    setContents(file,last);
	}
	//catch(FileNotFoundException fnfe){ System.out.println(fnfe); }IOException
	catch(IOException fnfe){ System.out.println(fnfe); }
	//System.out.println("Done deleteing chunk: "+toDelete);
    }//done Deleteing
    
        public static String getDeletedChunk(String filename, String toDelete, File file) {
    //...checks on aFile are elided
	        //File file = new File("unfinishedDownload.txt");
                StringBuilder contents = new StringBuilder();
                int count = 0;

            try {
                //use buffering, reading one line at a time
                //FileReader always assumes default encoding is OK!
                BufferedReader br =  new BufferedReader(new FileReader(file));
                //try {
		String  c,d,line=filename, totalChunks;
		while ((c=br.readLine()) !=null)
		{
			StringTokenizer st = new StringTokenizer(c);
			d = st.nextToken();
			if(d.equals(filename)){
				//System.out.println("find line in which chunk has to deleted line is "+line);
				totalChunks = st.nextToken();
				line+=" "+totalChunks; //total chunks wrote
				while ((st.hasMoreTokens())){
					//System.out.println("line is "+line +"to delete is "+toDelete);
	                                if(!((d=st.nextToken()).equals(toDelete))){
	                                		count++;
                                        	line+=" "+d;
						//System.out.println("chunk not to delete is "+d);
					}
				}
				//System.out.println("final line is "+line);
				if(!(count==0)){
				contents.append(line);
                                contents.append(System.getProperty("line.separator"));
				}
                if(count == 0){
			//System.out.println("calling reassembly&&&&&&&&&&&&&&");
                	reassembly(filename,totalChunks);
			}
			}
			else{
				contents.append(c);
	                        contents.append(System.getProperty("line.separator"));
			}
		}
	      }catch(FileNotFoundException fnf){System.out.println("fille"+fnf);}
	       catch( IOException ioe){System.out.println("error"+ioe);}    
	return contents.toString();
	}



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
        
    public static void reassembly(String filename, String totalChunks){
    System.out.println("All chunks downloaded. Now Reassembling the file...");
    	int totalchunks = Integer.parseInt(totalChunks);
    	byte [] bytes = new byte [1024*1024];
	byte []bytes2;
    	int io = -1;
    	 try {
	        File file = new File(filename);
	        if(file.exists())
	        	file.delete();
			file.createNewFile();
		FileOutputStream fp = new FileOutputStream(file,true);
		deleteSearchLine("unfinishedDownload.txt", filename);
		int off = 0; 
    	for(int i = 0; i<totalchunks;i++){
    		
		File temp = new File(filename+"_chunk_"+i+".txt");
		//File tempSha =  new File(filename+"_"+i+"_sha.txt");
    	FileInputStream fstream = new FileInputStream(filename+"_chunk_"+i+".txt");
    	DataInputStream in = new DataInputStream(fstream);
		io = in.read(bytes,0,1024*1024);
			
			if(io!= -1){
				bytes2 = new byte[io];
				for(int a=0;a<io;a++)
					bytes2[a] = bytes[a];
				
				//System.out.println("io is "+io);
				fp.write(bytes2);
	    		}
		//System.out.println("in for offset is "+off);
	    	off = off + io;
		temp.delete();
	//System.out.println("offset final is "+off);
    	}
	fp.close();
	System.out.println("Reassembled Successfully");
	}catch(Exception e){}//System.out.println("in reasembly"+e);}
    }
	public static synchronized void shareChunk(String filename,int chunk, DatagramSocket socket) throws IOException{
	File file =  new File("shared.txt");
	System.out.println("Sharing chunk No. "+chunk);
	BufferedReader in =  new BufferedReader(new FileReader(file));
	String line = "", toWrite="";
	boolean first = true;
	StringTokenizer st;
	boolean nothing = true;
	while((line=in.readLine())!=null&&!(line.equals(""))){
		nothing = false;
		st = new StringTokenizer(line);
		if(filename.equals(st.nextToken())){
			String temp="";
			String more = "";
			first = false;
			boolean flag = false;
			boolean flag1 = false;
			line = filename + " " + st.nextToken();
			temp = st.nextToken();
			while(st.hasMoreTokens()){
				if(chunk  > Integer.parseInt(temp)){
					line += " " + temp;
				}
				else if(chunk == Integer.parseInt(temp));
				else{
					flag = true;
					line += " " + chunk;
					break;
				}
				temp = st.nextToken();
				if(filename.equals(temp)){
					flag1 = true;
					break;
				}
			}
			if(flag){
				line += " " + temp;
				while(st.hasMoreTokens()){
					line  += " " + st.nextToken();
				}
			}
			else{
				line += " " + chunk;
				line += " " +filename;
			}
		}
		if(toWrite.equals(""))
			toWrite = line;
		else
			toWrite +="\n" + line;
	}
	if(first){
		if(!nothing)
		toWrite += "\n"+filename +" "+totalChunks+" "+chunk+" "+filename;
		else
		toWrite += filename +" "+totalChunks+" "+chunk+" "+filename;
		String Line = Client.CmpltLine("main.txt",filename);
		Client.incrementCount("main.txt",filename);
		String increment=Peer.magic + " "+Peer.fileUploadStr+" "+Line;//00 is used for file request
		byte smallMsg[] = new byte[1024];
		DatagramPacket smallPack = new DatagramPacket(smallMsg, smallMsg.length);
		smallPack.setData(increment.getBytes());
		Client.copy("users.txt","temp.txt");
		System.out.println("Broadcasting sharing of "+filename);
		Cubbyhole cb = new Cubbyhole();
		Broadcast broadcast = new Broadcast(cb, socket);
		broadcast.start();
		cb.put(smallPack);
	}
	setContents(file, toWrite);
	in.close();
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
		
		//	decrement(lineWithFilename.substring(0,i),"main.txt");
		//if the count become 0 delete that entry completely






}//ends controller thread