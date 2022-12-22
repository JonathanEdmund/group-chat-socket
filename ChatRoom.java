import java.util.ArrayList;

public class ChatRoom {
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
        
        ArrayList<String> list = new ArrayList<>();

        for (ClientHandler participant : participants) {
            list.add(participant.clientUsername);
        }

        return list.toString();
    }
}
