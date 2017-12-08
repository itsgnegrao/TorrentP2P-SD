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

public class UDPServer{
    public static void main(String args[]){
    	DatagramSocket aSocket = null;
        File tempuser = new File("LogServer/tempuser.txt");
                
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
                    novoUser(apelido,request,tempuser);
                    getFiles(apelido, aSocket, request.getAddress(), request.getPort());
                }
                else if (data.contains("!!!SAIR!!!")){
                    String apelido = data.replace("!!!SAIR!!!", "");
                    removeUser(apelido, tempuser);
                }

            } //while
        }catch (SocketException e){
	   System.out.println("Socket: " + e.getMessage());
        }catch (IOException e) {
	   System.out.println("IO: " + e.getMessage());
        } //catch
    } //main
    
    public static void getFiles(String apelido, DatagramSocket aSocket, InetAddress adress, int port) throws IOException{
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
        System.out.println("QTDE de arquivos: "+fim);

        //fazer for para recebr os arquivo..
        
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

    private static void novoUser(String apelido, DatagramPacket request, File tempuser) throws IOException {
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
    
    
}//class
