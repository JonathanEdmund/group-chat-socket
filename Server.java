import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) throws IOException {
        final int port = 3000;
        ArrayList<Client> clientList = new ArrayList<>();
        try (ServerSocket serverSocket = new  ServerSocket(port)) {
            System.out.println("Server running on port " + port);
            Thread connection = new Thread(new Runnable() {
                Socket socket;
                @Override
                public void run() {
                    while(true){
                        try {
                            socket = serverSocket.accept();
                            Client c = new Client(socket);
                            clientList.add(c);
                            System.out.println(c.name + " has joined.");
                            
                            Thread listen = new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    while(true) {
                                        String command = c.read();
                                        if (command.charAt(0) == '/')
                                        {
                                            System.out.println("This is a command");
                                        }
                                        else 
                                        {
                                            System.out.println(c.name + " : " + command);
                                        }
                                    }
                                }
                            });

                            listen.run();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }   
                    }
                }
                
            });
            connection.run();
        }
    }
  
}
