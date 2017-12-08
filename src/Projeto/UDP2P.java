package Projeto;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author itsgnegrao
 */
public class UDP2P {
  private boolean run = true;
  private static String apelido;
    
   public static void main(String[] args) throws IOException {
    startServer();
    //startCliente(args[0]); //args[0] ip de destino
    startCliente("127.0.0.1");
  }//main
  
   public static void startCliente(String ip_dest) throws UnknownHostException{
    InetAddress aHost = InetAddress.getByName(ip_dest); 
    (new Thread() {
        @Override
        public void run() {
            DatagramSocket aSocket = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String data = " ";
            apelido = JOptionPane.showInputDialog("Digite seu Apelido");
            
            data = "!!!CONECTADO!!!"+apelido;
            try {
                aSocket = new DatagramSocket(); //cria um socket datagrama
                
                   // byte[] m = args[0].getBytes(); // transforma a mensagem em bytes
                    byte[] m = data.getBytes(); // transforma a mensagem em bytes 

                    /* armazena o IP do destino */
                    //InetAddress aHost = InetAddress.getByName(args[1]); 
                    int serverPort = 6666; // porta do servidor

                    /* cria um pacote datagrama */
                    DatagramPacket request =
                    new DatagramPacket(m,  data.length(), aHost, serverPort);
                    //new DatagramPacket(m,  args[0].length(), aHost, serverPort);

                    /* envia o pacote */
                    aSocket.send(request);
                    
                    
                    /* cria um buffer vazio para receber datagramas */
                    byte[] buffer = new byte[1000];
                   
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);	

                    /* aguarda datagramas */
                    aSocket.receive(reply);
                    data = new String(reply.getData());
                    if(data.contains("FILES")){
                        System.out.println(data);
                        sendQtdeNameFiles(data,aSocket,request,"Shared/");
                    }
                    System.out.println("Resposta: " + new String(reply.getData()));

                    /* libera o socket */
                    aSocket.close();	
                    
            } catch (SocketException ex) {
                Logger.getLogger(UDP2P.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(UDP2P.class.getName()).log(Level.SEVERE, null, ex);
            }

            
            
            while(true){
                try {
                    System.out.println(apelido+": ");
                    data = apelido +": "+reader.readLine();
                    aSocket = new DatagramSocket(); //cria um socket datagrama

                   // byte[] m = args[0].getBytes(); // transforma a mensagem em bytes
                    byte[] m = data.getBytes(); // transforma a mensagem em bytes 

                    /* armazena o IP do destino */
                    //InetAddress aHost = InetAddress.getByName(args[1]); 
                    int serverPort = 6666; // porta do servidor

                    /* cria um pacote datagrama */
                    DatagramPacket request =
                    new DatagramPacket(m,  data.length(), aHost, serverPort);
                    //new DatagramPacket(m,  args[0].length(), aHost, serverPort);

                    /* envia o pacote */
                    aSocket.send(request);			                        

                    /* cria um buffer vazio para receber datagramas */
                    byte[] buffer = new byte[1000];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);	

                    /* aguarda datagramas */
                    aSocket.receive(reply);
                    //System.out.println("Resposta: " + new String(reply.getData()));

                    /* libera o socket */
                    aSocket.close();	
                } catch (SocketException e){
                    System.out.println("Socket: " + e.getMessage());
                }catch (IOException e){
                    System.out.println("IO: " + e.getMessage());
                } //catch       
            }
        }
    }).start();//Thread
    }//metodo


  public static void startServer() {
    (new Thread() {
        @Override
        public void run() {
            int port = 6667;
            DatagramSocket aSocket = null;
            try{
                System.out.println(port);
                aSocket = new DatagramSocket(port); // cria um socket datagrama em uma porta especifica

                while(true){
                    byte[] buffer = new byte[1000]; // cria um buffer para receber requisicoes

                    /* cria um pacote vazio */
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    aSocket.receive(request);  // aguarda a chegada de datagramas
                    /* imprime e envia o datagrama de volta ao cliente */
                    String data = new String(request.getData());
                    System.out.println(data);
                    if(data.equals("FILES")){
                        sendQtdeNameFiles(data, aSocket, request,"Shared/");
                    }
                    DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(), request.getAddress(), request.getPort()); // cria um pacote com os dados
                    System.out.println(apelido+": ");
                    aSocket.send(reply); // envia o pacote
                } //while
            }catch (SocketException e){
               System.out.println("Socket Server: " + e.getMessage());
            }catch (IOException e) {
               System.out.println("IO Server: " + e.getMessage());
            } //catch  
        }
    }).start();//Thread
    }//metodo	     
  
   public static void sendQtdeNameFiles(String data, DatagramSocket aSocket, DatagramPacket request, String pastaPredef) throws IOException{
       File f = null;
       File[] list;
       
       if(data.contains("FILES")){
           
            f = new File(pastaPredef);
            list = f.listFiles();
           
            data = String.valueOf(list.length);

            DatagramPacket reply = new DatagramPacket(data.getBytes(), data.length(), request.getAddress(), request.getPort()); // cria um pacote com os dados
            aSocket.send(reply); // envia o pacote
            System.out.println(data);
            
                    
            for (File file : list) {
                data = file.getName();
                reply = new DatagramPacket(data.getBytes(), data.length(), request.getAddress(), request.getPort()); // cria um pacote com os dados
                aSocket.send(reply); // envia o pacote
                
                data =  String.valueOf(file.length());
                reply = new DatagramPacket(data.getBytes(), data.length(), request.getAddress(), request.getPort()); // cria um pacote com os dados
                aSocket.send(reply); // envia o pacote       
                 
            }

        }

    }
  
}//class