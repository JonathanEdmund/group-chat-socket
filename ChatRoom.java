import java.util.ArrayList;

public class ChatRoom {
    // list of groups
    public static ArrayList<ChatRoom> chatroom = new ArrayList<>();
    
    public String groupName;
    public ArrayList<ClientHandler> participants = new ArrayList<>();

    public ChatRoom(String groupName) {
        this.groupName = groupName;
    }

    // method to broadcast message to group
    public void broadcast() {

    }
}
