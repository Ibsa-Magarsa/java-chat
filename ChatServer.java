import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private final int port;
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    public ChatServer(int port) { this.port = port; }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            Socket socket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(socket);
            clients.add(handler);
            new Thread(handler).start();
        }
    }

    private void broadcast(String msg) {
        for (ClientHandler c : clients) c.send(msg);
    }

    private void updateUsers() {
        StringBuilder sb = new StringBuilder("/users ");
        boolean first = true;
        for (ClientHandler c : clients) {
            if (!first) sb.append(",");
            sb.append(c.userName);
            first = false;
        }
        broadcast(sb.toString());
    }

    private class ClientHandler implements Runnable {
        Socket socket;
        String userName;
        BufferedReader in;
        PrintWriter out;

        ClientHandler(Socket s){ socket = s; }

        void send(String msg){ if(out!=null) out.println(msg); }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"), true);
                userName = in.readLine();
                broadcast("[" + userName + " joined]");
                updateUsers();
                String line;
                while((line = in.readLine()) != null){
                    if(line.equals("/quit")) break;
                    broadcast(userName + ": " + line);
                }
            } catch(Exception e) {}
            finally {
                clients.remove(this);
                broadcast("[" + userName + " left]");
                updateUsers();
                try{ socket.close(); } catch(Exception ex){}
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new ChatServer(12345).start();
    }
}
