import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientHandler implements Runnable {
    
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>(); // belongs to the class (not each objects)

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    public ClientHandler(Socket socket){
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("SERVER: " + clientUsername + " has joined the chat!");
        } catch (Exception e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()){
            try {
                messageFromClient = bufferedReader.readLine();
                
                // split message from command (">")
                String[] message = messageFromClient.split(">", 2);

                // split commmands
                String[] command  = message[0].split(" ");

                System.out.println(Arrays.toString(command) + " " + message[1]);
                
                // merge command and message into an arraylist
                ArrayList<String> commands = new ArrayList<>();
                for(int i = 0; i < command.length; i++) {
                    commands.add(command[i]);
                }

                if (message.length > 1) commands.add(message[1]); 

                // controller (call methods based on client commands)
                controller(commands);

                // broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void directMessage(String senderName, String receiverName, String messageToSend) throws IOException {

        // find receiver in the client list
        System.out.println("directMessage reached");
        System.out.println(clientHandlers.size());
        System.out.println(messageToSend);
        
        for (int i = 0; i < clientHandlers.size(); i++) {
            try {
                ClientHandler clientHandler = clientHandlers.get(i);
                System.out.println(receiverName + " = " + clientHandler.clientUsername);
                if (!clientHandler.clientUsername.equals(receiverName)) {
                    if (i == clientHandlers.size()-1) {
                        // this.clientHandler.bufferedWriter.write("Receiver not found!")   
                        System.out.println("Receiver not found!"); 
                        break;
                    }
                    continue;
                }
                System.out.println("Found");
                clientHandler.bufferedWriter.write("/dm " + senderName + " " + receiverName + ">" + messageToSend);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
                break;
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void controller(ArrayList<String> commands) {
        String command = commands.get(0);

        switch (command) {
            case "/reg":
                // register user
                break;
            case "/lsuser":
                // list all users
                break;
            case "/rmu":
                // remove user
                break;
            case "/mkgroup":
                // create group
                break;
            case "/join":
                // join group
                break;
            case "/exit":
                // exit group
                break;
            case "/lsgroup":
                // list groups
                break;
            case "/dm":
                // direct message
                try {
                    String senderName = commands.get(1);
                    String receiverName = commands.get(2);
                    String message = commands.get(3);
                    directMessage(senderName, receiverName, message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "/gm":
                // group message
                break;
            case "/reqlsuser":
                // gm method
                break;
            case "/reqlsgroup":
                // gm method
                break;
            default:
                // commmand not found 
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " has left the chat!");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
