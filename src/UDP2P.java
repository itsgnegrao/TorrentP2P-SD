import java.io.*;
import static java.lang.System.in;
import java.net.*;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import static sun.misc.GThreadHelper.lock;

/**
 *
 * @author itsgnegrao
 */
public class UDP2P {   
    private boolean run = true;
    private static String apelido;
    private static InetAddress aHost;
    private static DatagramSocket aSocket;
    private static int serverPort = 6667;
    private static String serverIp = "127.0.0.1";
    private static ArrayList<String> Peers;
    private static File caminhoComp = new File("Shared/");
    private static File[] qtdFiles = caminhoComp.listFiles();
    private static Thread client;
    

    public static void main(String[] args) throws IOException {
      startServer();
      startCliente(serverIp);//Endereço do Servidor.
    }//main
  
    public static void startCliente(String ip_dest) throws UnknownHostException{
    aHost = InetAddress.getByName(ip_dest); 
    (client = new Thread() {
        @Override
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String data = " ";
            try {
                apelido = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ex) {
                Logger.getLogger(UDP2P.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            data = "!!!CONECTADO!!!"+apelido;
            try {
                aSocket = new DatagramSocket(); //cria um socket datagrama
                
                   // byte[] m = args[0].getBytes(); // transforma a mensagem em bytes
                    byte[] m = data.getBytes(); // transforma a mensagem em bytes 

                    

                    /* cria um pacote datagrama */
                    DatagramPacket request = new DatagramPacket(m,  data.length(), aHost, serverPort);

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
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(UDP2P.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(UDP2P.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }).start();//Thread
    }//metodo
   
    public static int Menu() throws IOException, ClassNotFoundException, UnknownHostException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int op = 0;
        System.out.println("---MENU---:");
        System.out.println("1: Procurar Arquivo.");
        System.out.println("2: Criar Novo Arquivo Compartilhado.");
        System.out.println("3: Baixar Arquivo.");
        System.out.println("4: Restart.");
        System.out.println("0: Sair.");
        op = Integer.valueOf(reader.readLine());

        if(op == 1){
            searchFile();            
        }
        else if(op == 2){
            newFile();            
        }
        else if(op == 3){
            downFile();            
        }
        else if(op == 4){
            restart();          
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
  
    private static void downFile() throws IOException, ClassNotFoundException {
        
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
        ArrayList<Peer> Peers = new ArrayList<>();
       
        for (int i = 0; i < fim; i++) {
            buffer = new byte[1000];
            reply = new DatagramPacket(buffer, buffer.length);	
            // aguarda datagramas 
            aSocket.receive(reply);
            Peer peer = new Peer(new String(reply.getData(),reply.getOffset(),reply.getLength()));
            Peers.add(peer);
        }
      
        
        buffer = new byte[1000];
        reply = new DatagramPacket(buffer, buffer.length);	
        // aguarda datagramas 
        aSocket.receive(reply);
        data = new String(reply.getData(),reply.getOffset(),reply.getLength());
        filesize = Integer.parseInt(data);
        partes = (int) Math.ceil((filesize/128.0));
        
        
        for (int i = 0, j = 0; i < partes; i++,j++) {
            if(j > Peers.size()-1) j=0;
            Peers.get(j).addPart(i+1);
        }
        
        ArrayList<Integer> PeerParts;
        for (Peer peer : Peers) {
            PeerParts = peer.getParts();
            
            data = "!!!PARTS!!!";
            msg = new DatagramPacket(data.getBytes(), data.length(), peer.getIp(), 6666); // cria um pacote com os dados
            aSocket.send(msg); // envia o pacote
            
            data = fileDown;
            msg = new DatagramPacket(data.getBytes(), data.length(), peer.getIp(), 6666); // cria um pacote com os dados
            aSocket.send(msg); // envia o pacote
                             
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            os.writeObject(PeerParts);
            byte[] list = outputStream.toByteArray();
            msg = new DatagramPacket(list, list.length, peer.getIp(), 6666);
            aSocket.send(msg);
        }
        
        //partes 
        ArrayList<Packet> packets = new ArrayList<Packet>();
        
        for (int i = 0; i < partes; i++) {
            DatagramPacket part = new DatagramPacket(buffer, buffer.length);	
            // aguarda datagramas 33
            aSocket.receive(part);          
            ByteArrayInputStream in = new ByteArrayInputStream(part.getData());
            ObjectInputStream is = new ObjectInputStream(in);
            Object o = is.readObject();
            Packet packet = new Packet(o);
            packets.add(packet);
        }
                
        if(packets.size() == partes){
            File file = new File("Download/"+fileDown+".temp");
            FileOutputStream fos = new FileOutputStream(file);
            Collections.sort (packets, new ComparePacks());
            for (int i = 0; i < partes; i++) {
                Packet packet = packets.get(i);
                fos.write(packet.getBytes());
            }
            File fileName = new File("Download/"+packets.get(0).getFileName());
            file.renameTo(fileName);
            File fileNameShared = new File("Shared/"+packets.get(0).getFileName());
            CopiaArquivo.copyFile(fileName, fileNameShared);
            fos.close();
            restart();
        }
        else{
            System.out.println("FALHA DE RETRANSMISSAO NÃO IMPLEMENTADA AINDA.");
        }
        

    }
    
    private static void newFile() throws UnknownHostException, IOException, InterruptedException {
        UIManager.put("swing.boldMetal", Boolean.FALSE); 
        
        JFrame frame = new JFrame("FileChooser");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        FileChooser fileChooser = new FileChooser(caminhoComp);

        //Add content to the window.
        frame.add(fileChooser);

        //Display the window.
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setSize(500, 200);
        frame.setVisible(true);
       
    }
    
    private static void restart() throws IOException{
        /* cria um pacote datagrama */
        String data = "!!!SAIR!!!"+apelido;
        DatagramPacket request = new DatagramPacket(data.getBytes(),  data.length(), aHost, serverPort);
        /* envia o pacote */
        aSocket.send(request);
        /* libera o socket */
        aSocket.close();
        startCliente(apelido);
    }

    public static void startServer() {
    (new Thread() {
        int port = 6666;
        DatagramSocket aSocket = null;
        File diretorioPadrao = caminhoComp;
       
        @Override
        public void run() {
           
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
                    if(data.contains("!!!PARTS!!!")){
                        enviaFiles();                        
                    }
                    
                    
                } //while
            }catch (SocketException e){
               System.out.println("Socket Server: " + e.getMessage());
            }catch (IOException e) {
               System.out.println("IO Server: " + e.getMessage());
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(UDP2P.class.getName()).log(Level.SEVERE, null, ex);
            } //catch  
        }

        private void enviaFiles() throws IOException, ClassNotFoundException {
            byte[] buffer = new byte[1024];
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(request);  // aguarda a chegada de datagramas
            String data = new String(request.getData(),request.getOffset(),request.getLength());
            String fileDown = data;

            File[] arquivos = diretorioPadrao.listFiles();

            for(int i = 0; i < arquivos.length; i++){
               if(arquivos[i].getName().toUpperCase().contains(fileDown.toUpperCase())){
                    fileDown = arquivos[i].getName();
               }
            }//for

            ArrayList<Integer> parts;
            DatagramPacket part = new DatagramPacket(buffer, buffer.length);	
            aSocket.receive(part);          
            ByteArrayInputStream in = new ByteArrayInputStream(part.getData());
            ObjectInputStream is = new ObjectInputStream(in);
            parts = (ArrayList < Integer >) is.readObject();

            ArrayList<Packet> packets = new ArrayList<>();

            FileInputStream outToClient = new FileInputStream(diretorioPadrao.getName()+"/"+fileDown);
            byte[] buff = new byte[128];

            int count;
            int i = 1;
            while ((count=outToClient.read(buff)) > 0) {
                Packet pack = new Packet(fileDown, i, buff);
                if(parts.contains(i)){
                    packets.add(pack);
                }
                i = i+1;
                buff = new byte[128];
            }

            for (Packet packet : packets) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ObjectOutputStream os = new ObjectOutputStream(outputStream);
                os.writeObject(packet);
                byte[] pack = outputStream.toByteArray();
                DatagramPacket reply = new DatagramPacket(pack, pack.length, request.getAddress(), request.getPort());
                aSocket.send(reply);
            }      
        }
        
    }).start();//Thread
    }//metodo	     
}//class