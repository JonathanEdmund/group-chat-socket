import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>(); 
    public static ArrayList<ChatRoom> chatRooms = new ArrayList<>();

    private Socket socket;
    private BufferedReader bufferedReader;
    BufferedWriter bufferedWriter;
    String clientUsername;

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
                
                if (messageFromClient == null) throw new IOException();

                // split message from command (">")
                String[] message = messageFromClient.split(">", 2);

                // split commmands
                String[] command  = message[0].split(" ");
                
                // merge command and message into an arraylist
                ArrayList<String> commands = new ArrayList<>();
                for(int i = 0; i < command.length; i++) {
                    commands.add(command[i]);
                }

                if (message.length > 1) commands.add(message[1]); 

                // controller (call methods based on client commands)
                if (commands.isEmpty()) throw new IOException();
                controller(commands);

            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void controller(ArrayList<String> commands) {

        String command = commands.get(0);
        switch (command) {
            case "/lsuser":
            {
                // list all users
                if(!checkSyntax(1, commands)) break;
                listUsers();
                break;
            }
            case "/rmu":
                // remove user
                if(!checkSyntax(1, commands)) break;
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            
            case "/mkgroup":
            {
                // create group => /mkgroup [groupName]
                if(!checkSyntax(2, commands)) break;
                
                String groupName = commands.get(1);
                createGroup(groupName);
                break;
            }
            case "/join": 
            {
                // join group => /join [groupName]
                if(!checkSyntax(2, commands)) break;
                
                String groupName = commands.get(1);
                joinGroup(groupName, this);
                break;
            }
            case "/exit":
            {
                // exit group => /exit [groupName]
                if(!checkSyntax(2, commands)) break;
                
                String groupName = commands.get(1);
                exitGroup(groupName, this);
                break;
            }
            case "/lsgroup":
            {
                // list groups
                if(!checkSyntax(1, commands)) break;
                
                listGroups();
                break;
            }
            case "/lsparticipants":
            {
                // list participants in group => /lsparticipants [groupName] 
                if(chatRooms.isEmpty()) {
                    write("SERVER: Empty group list!"); 
                    break;
                }
                if(!checkSyntax(2, commands)) break;
                
                String groupName = commands.get(1);
                listGroupParticipants(groupName);
                break;
            }
            case "/dm":
            {
                // direct message => /dm [senderName] [receiverName]>[message]
                if(!checkSyntax(4, commands)) break;
                
                String senderName = commands.get(1);
                String receiverName = commands.get(2);
                String message = commands.get(3);
                
                directMessage(senderName, receiverName, message);
                
                break;
            }
            case "/gm":
            {
                // group message => /gm [senderName] [groupName]>[message]
                if(!checkSyntax(4, commands)) break;
                
                String senderName = commands.get(1);
                String groupName = commands.get(2);
                String message = commands.get(3);
                groupMessage(senderName, groupName, message);
                break;
            }
            default:
                // commmand not found
                write("SERVER: Unknown command!");
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

    public void directMessage(String senderName, String receiverName, String messageToSend) {

        // find receiver in the client list
        for (int i = 0; i < clientHandlers.size(); i++) {
            try {
                ClientHandler clientHandler = clientHandlers.get(i);

                // not found
                if (!clientHandler.clientUsername.equals(receiverName)) {
                    if (i == clientHandlers.size()-1) {
                        write("SERVER: Receiver not found!");
                        break;
                    }
                    continue;
                    
                }
                
                // client found
                clientHandler.bufferedWriter.write("/dm " + senderName + " " + receiverName + ">" + messageToSend);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
                break;

            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void groupMessage(String senderName, String groupName, String messageToSend) {
        // find group
        for (int i = 0; i < chatRooms.size(); i++) {
            ChatRoom chatRoom = chatRooms.get(i);

            if(!chatRoom.roomName.equals(groupName)){
                if (i == chatRooms.size()-1) {
                    // room not found
                    System.out.println("Chat room not found");
                    write("SERVER: Room not found!");
                    return;
                }
                continue;
            }

            // broadcast message to group
            chatRoom.broadcast(senderName, messageToSend);
            return;
        }

    }

    public void listUsers() {

        ArrayList<String> clientList = new ArrayList<>();

        for (ClientHandler clientHandler : clientHandlers) {
            clientList.add(clientHandler.clientUsername);
        }
        
        write(clientList.toString());
        
    }

    public void listGroups() {

        ArrayList<String> groupList = new ArrayList<>();

        for (ChatRoom chatroom : chatRooms) {
            groupList.add(chatroom.roomName);
        }

        write(groupList.toString());
    }

    public void listGroupParticipants(String groupName) {
        // find group
        for (int i = 0; i < chatRooms.size(); i++) {
            ChatRoom chatRoom = chatRooms.get(i);

            if(!chatRoom.roomName.equals(groupName)){
                if (i == chatRooms.size()-1) {
                    // room not found
                    System.out.println("chat room not found");
                    write("SERVER: Room not found!");
                    return;
                }
                continue;
            }

            // list participants
            write(chatRoom.getParticipantList());
            return;

        }
    }

    public void createGroup(String groupName) {
        // check for duplicate groupName
        for (ChatRoom chatroom : chatRooms) {
            if (chatroom.roomName.equals(groupName)) {
                write("SERVER: Group name already in use!");
                return;
            }
        }

        // create group
        ChatRoom newChatRoom = new ChatRoom(groupName);
        newChatRoom.participants.add(this);
        chatRooms.add(newChatRoom);

        // send status
        write("SERVER: Successfully created new group " + groupName+ "!");
    }

    public void joinGroup(String groupName, ClientHandler clientHandler) {
        // find chatroom
        for (int i = 0; i < chatRooms.size(); i++) {
            ChatRoom chatRoom = chatRooms.get(i);
            
            if(!chatRoom.roomName.equals(groupName)){
                if (i == chatRooms.size()-1) {
                    // room not found
                    System.out.println("Chat room not found");
                    write("SERVER: Room not found!");
                    return;
                }
                continue;
            }

            // check if client is already a participant
            if (chatRoom.participants.contains(clientHandler)) {
                write("SERVER: Client has already joined this group!");
                return;
            }
            
            // add clientHandler
            chatRoom.participants.add(clientHandler);
            write("SERVER: Successfully added " + clientUsername + " to " + groupName);
            return;
        }
    }

    public void exitGroup(String groupName, ClientHandler clientHandler) {
        // find chatroom
        for (int i = 0; i < chatRooms.size(); i++) {
            ChatRoom chatRoom = chatRooms.get(i);
            
            if(!chatRoom.roomName.equals(groupName)){
                if (i == chatRooms.size()-1) {
                    // room not found
                    System.out.println("chat room not found");
                    write("SERVER: Room not found!");
                    return;
                }
                continue;
            }

            // remove clientHandler
            chatRoom.participants.remove(clientHandler);
            write("SERVER: Successfully exit group.");
            return;
        }
    }

    public boolean checkSyntax(int fixedLength, ArrayList<String> command) {
        // checks command length
        if (command.size() != fixedLength) {
            int expectedArguments = fixedLength - 1;
            write("SERVER: Incorrect command syntax! Expecting " + expectedArguments + " argument(s).");
            return false;
        }

        return true;
    }

    public void write(String message) {
        try {
            this.bufferedWriter.write(message);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        // broadcastMessage("SERVER: " + clientUsername + " has left the chat!");
    }


    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (socket != null) {
                socket.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
