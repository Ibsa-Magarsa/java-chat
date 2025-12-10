import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

public class ChatClient extends JFrame {
    JTextArea chat;
    JTextField input;
    PrintWriter out;
    BufferedReader in;

    public ChatClient(){
        setTitle("Chat Client");
        setSize(600,400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        chat = new JTextArea(); chat.setEditable(false);
        add(new JScrollPane(chat), BorderLayout.CENTER);

        input = new JTextField();
        input.addActionListener(e -> send());
        add(input, BorderLayout.SOUTH);

        setVisible(true);
        connect();
    }

    void connect(){
        try {
            String host = JOptionPane.showInputDialog(this, "Host:", "127.0.0.1");
            String name = JOptionPane.showInputDialog(this, "Your name:", "User");
            Socket s = new Socket(host,12345);
            out = new PrintWriter(s.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out.println(name);

            new Thread(() -> {
                try {
                    String line;
                    while((line = in.readLine()) != null){
                        chat.append(line + "\n");
                    }
                }catch(Exception ignored){}
            }).start();
        } catch(Exception e){
            chat.append("Connection failed: " + e.getMessage());
        }
    }

    void send(){
        String text = input.getText().trim();
        if(text.isEmpty()) return;
        out.println(text);
        input.setText("");
    }

    public static void main(String[] args){
        new ChatClient();
    }
}
