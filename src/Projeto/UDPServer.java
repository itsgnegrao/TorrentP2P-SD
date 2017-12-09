package Projeto;

/**
 *
 * @author itsgnegrao
 */

/**
 * UDPServer: Servidor UDP
 * Descricao: Recebe um datagrama de um cliente, imprime o conteudo e retorna o mesmo
 * datagrama ao cliente
 */

import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class UDPServer{
    
    private static DatagramSocket aSocket = null;
    private static File tempuser = new File("LogServer/tempuser.txt");
    
    public static void main(String args[]){
    	              
        try{
            aSocket = new DatagramSocket(6666); // cria um socket datagrama em uma porta especifica

            while(true){
                byte[] buffer = new byte[1000]; // cria um buffer para receber requisicoes

		/* cria um pacote vazio */
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);  // aguarda a chegada de datagramas
                
                String data = new String(request.getData(), request.getOffset(), request.getLength());
                if(data.contains("!!!CONECTADO!!!")){
                    String apelido = data.replace("!!!CONECTADO!!!", "");
                    newUser(apelido,request,tempuser);
                    getFiles(apelido, aSocket, request.getAddress(), request.getPort());
                }
                else if (data.contains("!!!SAIR!!!")){
                    String apelido = data.replace("!!!SAIR!!!", "");
                    removeUser(apelido, tempuser);
                }
                else if (data.contains("!!!SEARCHFILE!!!")){
                    String file = data.replace("!!!SEARCHFILE!!!", "");
                    searchFile(file, request);
                }
                else if (data.contains("!!!DOWNFILE!!!")){
                    String file = data.replace("!!!DOWNFILE!!!", "");
                    downFile(file, request);
                }

            } //while
        }catch (SocketException e){
	   System.out.println("Socket: " + e.getMessage());
        }catch (IOException e) {
	   System.out.println("IO: " + e.getMessage());
        } //catch
    } //main
    
    private static void getFiles(String apelido, DatagramSocket aSocket, InetAddress adress, int port) throws IOException{
        FileWriter writer = new FileWriter("LogServer/FilesUsers/"+apelido+".txt", true);
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        
        String data = new String("FILES");
        DatagramPacket reply = new DatagramPacket(data.getBytes(), data.length(), adress, port);
        aSocket.send(reply);
        

        
        byte[] buffer2 = new byte[1000]; // cria um buffer para receber requisicoes
        /* cria um pacote vazio */
        DatagramPacket request = new DatagramPacket(buffer2, buffer2.length);
        /* cria um pacote vazio */
        aSocket.receive(request);  // aguarda a chegada de datagramas
        data = new String(request.getData(),request.getOffset(), request.getLength());
        int fim = Integer.parseInt(data);
        
        //recebe o nome e o tamanho em bytes de cada arquivo contido na pasta compartilhada
        for (int i = 0; i < fim ; i++) {
            buffer2 = new byte[1000]; // cria um buffer para receber requisicoes
            request = new DatagramPacket(buffer2, buffer2.length);
            aSocket.receive(request);  // aguarda a chegada de datagramas
            data = new String(request.getData(),request.getOffset(),request.getLength());
            bufferedWriter.write(data+" ");
            
            
            buffer2 = new byte[1000]; // cria um buffer para receber requisicoes
            request = new DatagramPacket(buffer2, buffer2.length);
            aSocket.receive(request);  // aguarda a chegada de datagramas
            data = new String(request.getData(),request.getOffset(),request.getLength());
            bufferedWriter.write(data);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }//método

    private static void newUser(String apelido, DatagramPacket request, File tempuser) throws IOException {
        FileWriter writer = new FileWriter(tempuser, true);
        BufferedWriter bufferedWriter = new BufferedWriter(writer);

        String adress = String.valueOf(request.getAddress());
        String port = String.valueOf(request.getPort());    
        String user = apelido+" " +adress+" " +port;
        bufferedWriter.write(user);
        bufferedWriter.newLine();
        bufferedWriter.close();
    }//mẃetodo
    
    private static void removeUser(String apelido, File tempuser) throws IOException {
        File deleteFile = new File("LogServer/FilesUsers/"+apelido+".txt");
        File tempFile = new File("tempUSERtemp.txt");

        BufferedReader reader = new BufferedReader(new FileReader(tempuser));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

        String currentLine;

        while((currentLine = reader.readLine()) != null) {
            // trim newline when comparing with lineToRemove
            String trimmedLine = currentLine.trim();
            if(trimmedLine.contains(apelido)) continue;
            writer.write(currentLine + System.getProperty("line.separator"));
        }
        writer.close(); 
        reader.close(); 
        boolean successful = tempFile.renameTo(tempuser);
        deleteFile.delete();
       
    }
    
    private static void searchFile(String str,  DatagramPacket request) throws FileNotFoundException, IOException{
        ArrayList<String> List = new ArrayList<String>();
        File diretorio = new File("LogServer/FilesUsers/");
        File[] arquivos = diretorio.listFiles();
        FileReader fr;
	BufferedReader br;
                
        for(int i = 0; i < arquivos.length; i++){
            fr = new FileReader(arquivos[i]);
            br = new BufferedReader(fr);

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                if((sCurrentLine.toUpperCase().contains(str.toUpperCase()) && !(List.contains(sCurrentLine)))){
                    List.add(sCurrentLine);
                }
            }//while
        }//for
        
        DatagramPacket reply;
        
        String qtdeEcontrada = String.valueOf(List.size());
        reply = new DatagramPacket(qtdeEcontrada.getBytes(), qtdeEcontrada.length(), request.getAddress(), request.getPort());
        aSocket.send(reply);
        String[] formatstr;
       
        for(String string : List){
            formatstr = string .split("\\s+");
            string = "Nome: "+formatstr[0]+" Tamanho: "+formatstr[1]+" Bytes";
            reply = new DatagramPacket(string.getBytes(), string.length(), request.getAddress(), request.getPort());
            aSocket.send(reply);
        }//for

    }//método
    
    private static void downFile(String str,  DatagramPacket request) throws FileNotFoundException, IOException{
        ArrayList<String> List = new ArrayList<String>();
        ArrayList<String> ListPeer = new ArrayList<String>();
        File diretorio = new File("LogServer/FilesUsers/");
        File diretorioPadrao = new File("Shared/");
        File[] arquivos = diretorio.listFiles();
        FileReader fr;
	BufferedReader br;
        String fileDown = null;
        String fileSize = null; 
        String[] formatstr;

            
        for(int i = 0; i < arquivos.length; i++){
            fr = new FileReader(arquivos[i]);
            br = new BufferedReader(fr);

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                if((sCurrentLine.toUpperCase().contains(str.toUpperCase()) && !(List.contains(sCurrentLine)))){
                    formatstr = sCurrentLine .split("\\s+");
                    fileDown = formatstr[0];
                    fileSize = formatstr[1];
                    List.add(arquivos[i].getName().replace(".txt", ""));
                }
            }//while
        }//for
       
        fr = new FileReader(tempuser);
        br = new BufferedReader(fr);
        String sCurrentLine;
            
        while ((sCurrentLine = br.readLine()) != null) {
            for(String string : List){
                if(sCurrentLine.toUpperCase().contains(string.toUpperCase()) && !(ListPeer.contains(sCurrentLine))){
                    ListPeer.add(sCurrentLine);
                }
            }//for
        }//while
        
        DatagramPacket reply;
        
        String qtdeEcontrada = String.valueOf(ListPeer.size());
        reply = new DatagramPacket(qtdeEcontrada.getBytes(), qtdeEcontrada.length(), request.getAddress(), request.getPort());
        aSocket.send(reply);
        
        for(String string : ListPeer){
            reply = new DatagramPacket(string.getBytes(), string.length(), request.getAddress(), request.getPort());
            aSocket.send(reply);
        }//for
               
        reply = new DatagramPacket(fileSize.getBytes(), fileSize.length(), request.getAddress(), request.getPort());
        aSocket.send(reply);
        
        //Daqui pra frente a implementação ocorre como se fosse e posteriormente sera no Servidor Do Cliente
        int partes = (int) Math.ceil((Integer.parseInt(fileSize)/128.0));

        ArrayList<Packet> packets = new ArrayList<Packet>(partes);
            
        FileInputStream outToClient = new FileInputStream(diretorioPadrao.getName()+"/"+fileDown);
        byte[] buffer = new byte[128];
        Packet pack;
        
        int count;
        int i = 1;
        while ((count=outToClient.read(buffer)) > 0) {
            pack = new Packet(fileDown, i, buffer);
            i = i+1;
            packets.add(pack);
        }
        
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(outputStream);
        for (Packet packet : packets) {
            System.out.println(packet.getPart());
            os.writeObject(packet);
            byte[] data = outputStream.toByteArray();
            reply = new DatagramPacket(data, data.length, request.getAddress(), request.getPort());
            aSocket.send(reply);
        }

        
    }//método
    
}//class
