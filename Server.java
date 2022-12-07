import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public static void main(String[] args) throws IOException {
        
        final int port = 3000;

        ServerSocket serverSocket = new ServerSocket(port);
        Server server = new Server(serverSocket);
        System.out.println("Server started at port " + port);
        server.startServer();

    }

    public void startServer() {
        
        try {
            while (!serverSocket.isClosed()){

                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected");
                ClientHandler clientHandler = new ClientHandler(socket);

                // thread to listen to clients
                Thread thread = new Thread(clientHandler);
                thread.start();
            }

        } catch (IOException e) {
            
        }
    }
    
    public void closeServerSocket() {

        try {
            if (serverSocket != null){
                serverSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}