package Nimbuz;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

import static javafx.application.Platform.exit;

public class Controller {

    private static final String TERMINATE = "Exit";
    static String name = "None";
    static volatile boolean finished = false;
    private int port = Integer.parseInt("3000");
    private MulticastSocket socket;
    private InetAddress group;
    private boolean isConnected = false;

    @FXML
    public TextArea chatScreen;

    @FXML
    private TextField textField;

    @FXML
    private Label Status;

    @FXML
    private TextField clientName;

    @FXML
    private TextField localhost;

    @FXML
    private TextField portNumber;

    public Controller() { }

    @FXML
    private void connect() {
        try {
            chatScreen.setText("");
            String portAddress = portNumber.getText();
            if(portAddress == null){
                portAddress = "3000";
            }
            port =  Integer.parseInt(portAddress);
            String localHost = localhost.getText();
            if(localHost == null){
                localHost = "224.0.0.0";
            }
            name = clientName.getText();

            group = InetAddress.getByName(localHost);
            socket = new MulticastSocket(port);
            socket.setTimeToLive(0);
            socket.joinGroup(group);
            isConnected = true;
            Thread t = new Thread(new ReadThread(chatScreen, socket, group, port));
            t.start();
            Status.setText("Connected");

            canEdit(false);

        } catch (Exception e) {
            Status.setText("Something Went Wrong. Try Again");
        }
    }

    @FXML
    private void sendMessage() throws IOException {
        textField.setEditable(true);
        String message = textField.getText();
        if (message.equalsIgnoreCase(TERMINATE)) {
            closeConnection();
        }
        String messageBytes = name + ": " + message + "\n";
        String displayMessage = "You: " + message + "\n";
        chatScreen.appendText(displayMessage);
        byte[] buffer = messageBytes.getBytes();
        DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, group, port);
        socket.send(datagram);
        textField.setText("");
    }

    @FXML
    private void exitNimbuz() throws IOException {
        closeConnection();
        exit();
    }

    private void closeConnection() throws IOException {
        if (isConnected) {
            finished = true;
            socket.leaveGroup(group);
            socket.close();
        }
            exit();
    }

    @FXML
    private void disConnect() throws IOException {
        if (isConnected) {
            finished = true;
            socket.leaveGroup(group);
            socket.close();
            isConnected = false;
            Status.setText("Disconnected");
        } else {
            Status.setText("Not Connected to any Chats!");
        }
        canEdit(true);
    }
        private void canEdit(boolean possible){
            textField.setEditable(!possible);
            localhost.setEditable(possible);
            portNumber.setEditable(possible);
            clientName.setEditable(possible);
        }

}

class ReadThread implements Runnable {

      @FXML
      private TextArea chatScreen;

      private MulticastSocket socket;
      private InetAddress group;
      private int port;
      private static final int MAX_LEN = 1000;

      ReadThread(TextArea chatScreen, MulticastSocket socket, InetAddress group, int port) {
          this.chatScreen = chatScreen;
          this.socket = socket;
          this.group = group;
          this.port = port;

      }

      @Override
      public void run() {
          while(!Controller.finished) {
              byte[] buffer = new byte[ReadThread.MAX_LEN];
              DatagramPacket datagram = new DatagramPacket(buffer,buffer.length,group,port);
              String message;
              try
              {
                  socket.receive(datagram);
                  message = new String(buffer,0,datagram.getLength(), StandardCharsets.UTF_8);
                  if(!message.startsWith(Controller.name)) {
                        updateChatScreen(message);
                  }
              } catch (IOException e) {
                  chatScreen.appendText("Socket Closed");
              }
          }
      }

    private void updateChatScreen(String message) {
        chatScreen.appendText(message);
    }
}