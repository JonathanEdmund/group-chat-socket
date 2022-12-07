import java.util.ArrayList;

public class ChatRoom {
    // list of groups
    // public static ArrayList<ChatRoom> chatroom = new ArrayList<>();
    public String roomName;
    public ArrayList<ClientHandler> participants = new ArrayList<>();

    public ChatRoom(String roomName) {
        this.roomName = roomName;
    }

    // method to broadcast message to group
    public void broadcast(String sender, String message) {
        for (ClientHandler clientHandler : participants) {
            if (!clientHandler.clientUsername.equals(sender)) {
                clientHandler.write("/gm" + " " + sender + " " + roomName + ">" + message);
            }
        }
    }

    public String getParticipantList() {
        String list = "";

        for (ClientHandler participant : participants) {
            list += participant.clientUsername + " ";
        }

        return list;
    }
}
