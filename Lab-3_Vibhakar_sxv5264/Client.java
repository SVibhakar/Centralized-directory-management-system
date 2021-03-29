//Name : Sejal Vibhakar
//ID : 1001765264
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Client class
public class Client {

    private JTextArea ta;
    private SocketService<Command> svc;
    private JTextField input;
    private ArrayList<String> syncDirectories = new ArrayList<>();
    private HashMap<String, List<String>> results = new HashMap<>();

    // Reference: https://www.javamex.com/tutorials/threads/invokelater.shtml
    private synchronized void log(String logMessage) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Client.this.ta.append(logMessage + "\n");
            }
        };
        SwingUtilities.invokeLater(r);
    }

    // Reference: https://www.guru99.com/java-swing-gui.html
    private void runGUI() {
        JFrame frame = new JFrame("Client Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(750, 500);

        JMenuBar mb = new JMenuBar();
        JMenu m1 = new JMenu("File");
        mb.add(m1);
        JMenuItem m11 = new JMenuItem("Exit");
        m1.add(m11);
        m11.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        JPanel panel = new JPanel(); // the panel is not visible in output
        input = new JTextField();
        input.setColumns(45);

        JButton startButton = new JButton("Send");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = input.getText();
                try {

                    if (text.startsWith("SYNCDIR")) {
                        syncDirectories.add(text.split(" ")[1]);
                    } else if (text.startsWith("DESYNCDIR")) {
                        syncDirectories.remove(text.split(" ")[1]);
                    } else
                        // Accept user input and then send the command to server.
                        svc.sendMessage(new Command(text.split(" ")[0], text.split(" ")[1]));
                } catch (Exception e1) {
                    Client.this.log(e1.getMessage());
                }
            }
        });
        panel.add(input);
        panel.add(startButton);

        // Text Area at the Center
        ta = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(ta);

        // Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        frame.getContentPane().add(BorderLayout.NORTH, mb);
        frame.getContentPane().add(BorderLayout.CENTER, scrollPane);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        Client c = new Client();
        // running logic on separate thread than GUI
        new Thread(new Runnable() {
            @Override
            public void run() {
                c.start();
            }
        }).start();

        c.runGUI();
    }

    // Houses the main client logic.
    public void start() {
        String uname;
        try {
            Socket s = new Socket("localhost", 8080);
            Command msg;
            svc = new SocketService<>(s);
            while (true) {
                uname = JOptionPane.showInputDialog(null, "Enter username: ");
                if (uname.matches("^[A-Za-z0-9_-]*$")){//"[^a-zA-Z0-9\\.\\-]")) {
	                msg = new Command("Username", uname);
	                //System.out.println(msg + "   " +uname);
	                svc.sendMessage(msg);}
	            else
	            	//System.out.println("Invalid username, pleasse connect again");
	            	System.exit(0);
                while ((msg = svc.getMessage()) == null) {

                }

                if (msg.getCommand().equals("Duplicate Username"))
                    System.out.println("Duplicate username, try again");
                else
                    break;
            }

            Client.this.log("Connected to server.");

            while (true) {

                Command incomingMsg = null;
                // If there is any response from server, print it.

                for (String dir : syncDirectories) {
                    svc.sendMessage(new Command("SYNCDIR", dir));
                }

                if ((incomingMsg = svc.getMessage()) != null) {
                    if (incomingMsg.getCommand().startsWith("SYNCDATA")) {
                        String incomingDir = incomingMsg.getCommand().split(" ")[1];
                        if (!results.containsKey(incomingDir)) {
                            results.put(incomingDir, incomingMsg.getResults());
                            DirectoryHelper.createSyncDir("client_" + uname + "/" + incomingDir,
                                    incomingMsg.getResults());
                        } else {
                            if (!results.get(incomingDir).equals(incomingMsg.getResults()))
                                DirectoryHelper.createSyncDir("client_" + uname + "/" + incomingDir,
                                        incomingMsg.getResults());
                        }
                    } else if (incomingMsg.getResults() != null && incomingMsg.getResults().size() > 0) {
                        for (String text : incomingMsg.getResults()) {
                            Client.this.log(text);
                        }
                    } else {
                        Client.this.log("Reply: " + incomingMsg.getCommand());
                        if (incomingMsg.getArgs() != null)
                            Client.this.log("Reply: " + incomingMsg.getArgs());
                    }
                }
                Thread.sleep(1500);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
