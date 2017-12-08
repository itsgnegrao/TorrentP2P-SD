/** @ChatMulticastGUI: Interface Swing para o cliente entre o Cliente e o servidor
 *
 * Universidade Tecnológica Federal do Paraná - UTFPR-CM
 * @Autor: Gabriel Negrão Silva
 * @Data: 29/09/2017
 */

package Parte4;

import java.awt.Color;
import static java.awt.event.KeyEvent.*;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextField;

public class ChatMulticastGUI extends javax.swing.JFrame {

    private static Socket socketCli; //socket do cliente
    private static String ip; //ip fornecido pela Interface
    private static int porta; // porta fornecida pela interface
    private static TCPClient client; //cliente para alocação da classe controladora
    private static ArrayList<String> reserved; //palavras de requisições reservadads
    
    
    /**
     * Cria novo formilario e inicializa os componentes
     */
    public ChatMulticastGUI() throws UnknownHostException {
        initComponents();
        
        //seta a visibilidade dos campos
        areaMsg.setEnabled(false); 
        textMsg.setEnabled(false);
        btnEnviar.setEnabled(false);
        btnSair.setEnabled(false);
        
        //inicializa a lista de palavras reservadas
        reserved = new ArrayList<>();
        
        //adiciona palavras reservadas a lista
        reserved.add("EXIT");
        reserved.add("TIME");
        reserved.add("DATA");
        reserved.add("DOWN");
        reserved.add("FILES");
    }

     //Funcao para exibir uma mensagens na tela
     public synchronized void exibeMsg(String msg){
        areaMsg.append(msg);             
        textMsg.setText("");
        textMsg.requestFocus();
     }
     
     //funcao de formatacao da string de mensagem para adicionar apelido
     //variavel 'flag' corresponde ao formato
    public synchronized String formatString(String apelido, String msg, boolean flag){
        String msg_format;
        //se flag == true formate com \n
        if(flag)  msg_format= "[ "+apelido+" ]: "+msg + "\n";
        
        //se flag == false formate sem \n
        else msg_format = "[ "+apelido+" ]: "+msg;
       
        
        textMsg.setText("");
        textMsg.requestFocus();
        
        return msg_format;
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        textApelido = new javax.swing.JTextField();
        textIP = new javax.swing.JTextField();
        textPorta = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        areaMsg = new javax.swing.JTextArea();
        textMsg = new javax.swing.JTextField();
        btnEnviar = new javax.swing.JButton();
        btnSair = new javax.swing.JButton();
        btnEntrar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chat Java Multicast");

        jLabel1.setText("IP:");

        jLabel2.setText("Porta");

        jLabel3.setText("Apelido");

        textApelido.setText("Negrao");

        textIP.setEditable(false);
        textIP.setText("127.0.0.1");

        textPorta.setText("7896");
        textPorta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textPortaActionPerformed(evt);
            }
        });

        areaMsg.setEditable(false);
        areaMsg.setColumns(20);
        areaMsg.setRows(5);
        areaMsg.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Mensagens", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        areaMsg.setRequestFocusEnabled(false);
        jScrollPane1.setViewportView(areaMsg);

        textMsg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                textMsgKeyPressed(evt);
            }
        });

        btnEnviar.setText("ENVIAR");
        btnEnviar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEnviarActionPerformed(evt);
            }
        });

        btnSair.setText("Sair do Chat");
        btnSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSairActionPerformed(evt);
            }
        });

        btnEntrar.setText("Conectar ao Chat");
        btnEntrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEntrarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(textMsg)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEnviar, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnEntrar, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnSair, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(textApelido, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(textIP, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(textPorta, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(textApelido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(textIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textPorta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textMsg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEnviar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSair)
                    .addComponent(btnEntrar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnEnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEnviarActionPerformed
        if(reserved.contains(textMsg.getText())){
            client.EnviaMsg(formatString(textApelido.getText(),textMsg.getText(),false));
        }
        else client.EnviaMsg(formatString(textApelido.getText(),textMsg.getText(),true));
    }//GEN-LAST:event_btnEnviarActionPerformed

    private void btnSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSairActionPerformed
        try {
            //seta visibilidade dos campos
            areaMsg.setEnabled(false);
            textMsg.setEnabled(false);
            textApelido.setEditable(true);
            textIP.setEditable(true);
            textPorta.setEditable(true);
            btnEntrar.setEnabled(true);
            btnSair.setEnabled(false);
            
            //esvazia area de msg
            areaMsg.setText("");
            
            //ENVIA PALAVRA EXIT PARA FINALIZAR A CONEXÃO
            client.EnviaMsg("EXIT");

        } catch (Throwable ex) {
            Logger.getLogger(ChatMulticastGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnSairActionPerformed

    private void btnEntrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEntrarActionPerformed
       
        //seta visibilidade dos campos
        areaMsg.setEnabled(true);
        textMsg.setEnabled(true);
        btnEnviar.setEnabled(true);
        btnEntrar.setEnabled(false);
        btnSair.setEnabled(true);
        textApelido.setEditable(false);
        textIP.setEditable(false);
        textPorta.setEditable(false);
        textMsg.setText("");
        textMsg.requestFocus();
        
        //Cria nova conexão cm o servidor
        ip = new String(textIP.getText());
        porta = Integer.parseInt(textPorta.getText().toString());
        client = new TCPClient(this, ip, porta);  // armazena conexão do cliente
        
        
    }//GEN-LAST:event_btnEntrarActionPerformed

    private void textMsgKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textMsgKeyPressed
        if (evt.getKeyCode() == VK_ENTER){
            btnEnviar.doClick();
        }
    }//GEN-LAST:event_textMsgKeyPressed

    private void textPortaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textPortaActionPerformed

    }//GEN-LAST:event_textPortaActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ChatMulticastGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ChatMulticastGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ChatMulticastGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ChatMulticastGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new ChatMulticastGUI().setVisible(true);
                } catch (UnknownHostException ex) {
                    Logger.getLogger(ChatMulticastGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea areaMsg;
    private javax.swing.JButton btnEntrar;
    private javax.swing.JButton btnEnviar;
    private javax.swing.JButton btnSair;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField textApelido;
    private javax.swing.JTextField textIP;
    private static javax.swing.JTextField textMsg;
    private static javax.swing.JTextField textPorta;
    // End of variables declaration//GEN-END:variables
}
