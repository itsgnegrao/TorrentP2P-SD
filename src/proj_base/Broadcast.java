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
 * 
 * Essa classe é reponsavel por fazer o broadcast ( DE ALGOOOOOOOOO) para todos os usuarios de temp.txt? POQRUQ?
 * 
 */

class Broadcast extends Thread{ //broadcast to all server with name in temp.txt
    
	Cubbyhole cb1; //Classe instanciada que faz acordo copm as threads e comunicação
	DatagramSocket sock; // Datagrama Socket biblioteca importada
        
	public Broadcast(Cubbyhole cb1, DatagramSocket sock){
	   this.cb1 = cb1;
	   this.sock=sock;
	}
	public void run(){
		while(true){
			int count =1;
			DatagramPacket pack = cb1.get(); // CHAMA método de Cubbyhole get usado para comunicação entre as threads.. 
                        //como um acordo.. WAIT..
                        
			//System.out.println("Got a brdcst request. Packet contents are "+new String(pack.getData()));
			for(int i = 0; i < count; i++){
                            try{
				File userList = new File("temp.txt");//lista de usuarios contidos em temp.txt
				FileReader userlist = new FileReader(userList);
				BufferedReader br = new BufferedReader(userlist);
                                
                                //Continua a Ler as linhas no while
				//ainda há alguns para ler
                                
				String  c;
				
                                while((c = br.readLine()) != null){
					
					if(c.equals(InetAddress.getLocalHost().getHostName()))
						continue;
					//System.out.println(" broadcasting "+new String(pack.getData())+" to "+c);
					try{
                                            pack.setPort(Peer.SPORT); //Cria um novo Datagrama baseado na porta do peer
                                            InetAddress address = InetAddress.getByName(c);
                                            pack.setAddress(address); //pega o endereço pelo nome contido no temp.txt e seta ao novo Datagrama
					}
                                        catch(UnknownHostException uhe){}
					try{
                                            sock.send(pack);
					}
                                        catch (IOException ex) {
                                            System.err.println(ex);
                                        }
				}
                                
				//System.out.println ("Waiting for return packet inside broadcasting");
                                
				int timeout = 500;long time = System.currentTimeMillis();
                                
				while((System.currentTimeMillis()-time)<timeout){}//não espeara nada

				br.close();
                            }
                            catch(FileNotFoundException fnfe){
                                System.out.println("Broadcast: "+fnfe);
                            }
                            catch(IOException ioe){
                                System.out.println("Broadcast: "+ioe);
                            }
			}
		}
	}
}

