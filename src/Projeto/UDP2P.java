package Projeto;

import java.io.*;
import static java.lang.System.in;
import java.net.*;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
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
    private static InetAddress aHost;
    private static DatagramSocket aSocket;
    private static int serverPort = 6666;
    private static ArrayList<String> Peers;

     public static void main(String[] args) throws IOException {
      //startServer();
      startCliente("127.0.0.1");
    }//main
  
    public static void startCliente(String ip_dest) throws UnknownHostException{
    aHost = InetAddress.getByName(ip_dest); 
    (new Thread() {
        @Override
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String data = " ";
            apelido = JOptionPane.showInputDialog("Digite seu Apelido");
            
            data = "!!!CONECTADO!!!"+apelido;
            try {
                aSocket = new DatagramSocket(); //cria um socket datagrama
                
                   // byte[] m = args[0].getBytes(); // transforma a mensagem em bytes
                    byte[] m = data.getBytes(); // transforma a mensagem em bytes 

                    

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
                    data = new String(reply.getData(),reply.getOffset(),reply.getLength());
                    
                    if(data.contains("FILES")){
                        System.out.println(data);
                        sendQtdeNameFiles(data,request,"Shared/");
                    }               
                    
                    while(Menu() != 0){
               
                    }
                    
                    /* cria um pacote datagrama */
                    data = "!!!SAIR!!!"+apelido;
                    request = new DatagramPacket(data.getBytes(),  data.length(), aHost, serverPort);
                    
                    /* envia o pacote */
                    aSocket.send(request);
                    
                    /* libera o socket */
                    aSocket.close();
                    
            } catch (SocketException ex) {
                Logger.getLogger(UDP2P.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(UDP2P.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }).start();//Thread
    }//metodo
   
    public static int Menu() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("---MENU---:");
        System.out.println("1: Procurar Arquivo.");
        System.out.println("OFF 2: Criar Novo Arquivo Compartilhado.");
        System.out.println("3: Baixar Arquivo.");
        System.out.println("0: Sair.");
        int op = Integer.valueOf(reader.readLine());
        if(op == 1){
            searchFile();            
        }
        else if(op == 3){
            downFile();            
        }
        return op;
        
    }
    
    private static void searchFile() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Digite sua busca: ");
        String data = "!!!SEARCHFILE!!!"+reader.readLine();
        byte[] buffer = new byte[1000];// cria um buffer vazio para receber datagramas 

        DatagramPacket msg = new DatagramPacket(data.getBytes(), data.length(), aHost, serverPort); // cria um pacote com os dados
        aSocket.send(msg); // envia o pacote
        
        
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);	
        // aguarda datagramas 
        aSocket.receive(reply);
        data = new String(reply.getData(),reply.getOffset(),reply.getLength());
        int fim = Integer.parseInt(data);
        
        for (int i = 0; i < fim; i++) {
            buffer = new byte[1000];
            reply = new DatagramPacket(buffer, buffer.length);	
            // aguarda datagramas 
            aSocket.receive(reply);
            data = new String(reply.getData(),reply.getOffset(),reply.getLength());
            System.out.println(data);
            
        }
    }
    
    public static void sendQtdeNameFiles(String data, DatagramPacket request, String pastaPredef) throws IOException{
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
  
    private static void downFile() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Digite o nome do Arquivo: ");
        String fileDown = reader.readLine();
        int filesize = 0;
        int partes = 0;
        String data = "!!!DOWNFILE!!!"+fileDown+".";
        byte[] buffer = new byte[1000];// cria um buffer vazio para receber datagramas 

        DatagramPacket msg = new DatagramPacket(data.getBytes(), data.length(), aHost, serverPort); // cria um pacote com os dados
        aSocket.send(msg); // envia o pacote
        
        
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);	
        // aguarda datagramas 
        aSocket.receive(reply);
        data = new String(reply.getData(),reply.getOffset(),reply.getLength());
        int fim = Integer.parseInt(data);

        Peers = new ArrayList<String>();
        for (int i = 0; i < fim; i++) {
            buffer = new byte[1000];
            reply = new DatagramPacket(buffer, buffer.length);	
            // aguarda datagramas 
            aSocket.receive(reply);
            data = new String(reply.getData(),reply.getOffset(),reply.getLength());
            Peers.add(data);
        }
        
        System.out.println("PEERS");
        for(String string : Peers){
            System.out.println(string);
        }
        
        reply = new DatagramPacket(buffer, buffer.length);	
        // aguarda datagramas 
        aSocket.receive(reply);
        data = new String(reply.getData(),reply.getOffset(),reply.getLength());
        filesize = Integer.parseInt(data);
        partes = (int) Math.ceil((filesize/128.0));
        
        //partes 
        ArrayList<Packet> packets = new ArrayList<Packet>(partes);
        
        ByteArrayInputStream in;
        ObjectInputStream is;
        for (int i = 0; i < partes; i++) {
            reply = new DatagramPacket(buffer, buffer.length);	
            // aguarda datagramas 
            aSocket.receive(reply);          
            in = new ByteArrayInputStream(reply.getData());
            is = new ObjectInputStream(in);
            try {
                Packet packet = (Packet) is.readObject();
                packets.add(packet);
            } catch (ClassNotFoundException e) {
            e.printStackTrace();
            }
        }
        File file = new File("Download/"+fileDown+".temp");
        FileOutputStream fos = new FileOutputStream(file);
        
        for (Packet packet : packets) {
            fos.write(packet.getBytes());
        }
        File fileName = new File("Download/"+packets.get(0).getFileName());
        file.renameTo(fileName);
        fos.close();

    }
    
    
    /*public static void startServer() {
    (new Thread() {
        @Override
        public void run() {
            int port = 6666;
            DatagramSocket aSocket = null;
            try{
                System.out.println(port);
                aSocket = new DatagramSocket(port); // cria um socket datagrama em uma porta especifica

                while(true){
                    byte[] buffer = new byte[1000]; // cria um buffer para receber requisicoes

                    // cria um pacote vazio 
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    aSocket.receive(request);  // aguarda a chegada de datagramas
                    // imprime e envia o datagrama de volta ao cliente 
                    String data = new String(request.getData(),request.getOffset(),request.getLength());
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
    }//metodo	     */
}//class