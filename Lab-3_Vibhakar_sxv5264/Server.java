//Name : Sejal Vibhakar
//ID : 1001765264
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.file.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

//Server class 
public class Server {

  public ArrayList<Handler> handlers;
  private ArrayList<String> connectedClients;
  private JTextArea ta;
  public ArrayList<String> logMsg;
  String[] availableDirs = { "A", "B", "C" };
  ArrayList<String> createList;
  //UndoHandler undoHandler = new UndoHandler();
  //UndoManager undoManager = new UndoManager();
  
  public Server() {
      handlers = new ArrayList<>();
      connectedClients = new ArrayList<>();
      logMsg = new ArrayList<>();
      //UndoAction undoAction = null;

  }
  /*public String log() {
		// TODO Auto-generated method stub
		return null;
	}*/
  
  // Reference: https://www.guru99.com/java-swing-gui.html
  private void runGUI() {
      JFrame frame = new JFrame("Server Frame");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setSize(500, 500);

      JMenuBar mb = new JMenuBar();
      JMenu m1 = new JMenu("File");
      mb.add(m1);
      JMenuItem m11 = new JMenuItem("Exit");
      m1.add(m11);
      m11.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
              // Exit the code
              System.exit(0);
          }
      });

      JPanel panel = new JPanel(); // the panel is not visible in output
      JButton startButton = new JButton("Start");
      startButton.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
              // running logic on separate thread than GUI
              new Thread(new Runnable() {
                  @Override
                  public void run() {
                      Server.this.start();
                  }
              }).start();

              Server.this.log("Server started on port 8080.");
          }
      });
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

  // Reference: https://www.javamex.com/tutorials/threads/invokelater.shtml
  private synchronized void log(String logMessage) {
      Runnable r = new Runnable() {
          @Override
          public void run() {
              Server.this.ta.append(logMessage + "\n");
          }
      };
      logMsg.add(logMessage);
      SwingUtilities.invokeLater(r);
  }

  public static void main(String[] args) {
      new Server().runGUI();
  }

  // Checks if username is unique or not
  public Boolean registerUsername(String username) {
      if (connectedClients.contains(username))
          return false;
      connectedClients.add(username);
      return true;
  }

  // Houses the main logic for handling client commands.
  @SuppressWarnings({ "unused", "resource" })
	public void start() {

      try {
          Thread[] tarr = new Thread[10];
          // Bind Server to port
          ServerSocket ss = new ServerSocket(8080);
          while (true) {
              Socket s = ss.accept();
              // Spawn new thread to handle client connection.
              Handler h = new Handler(s, this);
              handlers.add(h);
              Thread t = new Thread(h);
              t.start();
          }
      } catch (Exception e) {
          e.printStackTrace();
      }
  }

  // Class to handle every client in separate thread.
  class Handler implements Runnable {
      SocketService<Command> conn;
      private String username;
      private Boolean isReady;
      private Server server;

      public Handler(Socket s, Server sv) {
          conn = new SocketService<>(s);
          server = sv;
      }

      public String getUsername() {
          return username;
      }

      public Boolean isReady() {
          return isReady;
      }
      public void deleteDir(File dir) {
          File[] files = dir.listFiles();
          if(files != null) {
              for (final File file : files) {
                  deleteDir(file);
              }
          }
          dir.delete();
      }
      
      // Function to handle commands for directory creation and other operations.
      @SuppressWarnings("null")
	public void handleCommand(Command cmd) {
          try {
        	  String baseDir = this.username;
              String dirName;
              int i = 0;
          	  //String c;
              //ArrayList<String> msg = new ArrayList<>();
  	          switch (cmd.getCommand()) {
  	          	case "SYNCDIR":
  	          		dirName = cmd.getArgs();
                    cmd.setResults(DirectoryHelper.getSyncData(dirName));
                    cmd.setCommand("SYNCDATA " + dirName);
                    conn.sendMessage(cmd);
                    break;
                    
                case "CREATE": {
                    dirName = cmd.getArgs();
                    if (dirName.matches("[^a-zA-Z0-9\\.\\-]"))
                        throw new Exception("Invalid arguments");

                    Files.createDirectories(Paths.get(baseDir, dirName));
                    //String con = "/Users/sejalvibhakar/eclipse-workspace/Lab - 3 try/".concat(baseDir).concat(dirName);
                    //Path p = null;
                    //p.resolve(con); 
                    //String pp = p.toString();
                    //createList.add(con);
                    //System.out.println(con);
                    //System.out.println(createList);
                    cmd.setCommand("Successfully created dir.");
                    conn.sendMessage(cmd);
                    return;
                }
                case "RENAME":
                case "MOVE": {
                    String dir1, dir2;
                    dir1 = cmd.getArgs().split(",")[0];
                    dir2 = cmd.getArgs().split(",")[1];
                    if (dir1.matches("[^a-zA-Z0-9\\.\\-]") || dir2.matches("[^a-zA-Z0-9\\.\\-]"))
                        throw new Exception("Invalid arguments");
                    Files.move(Paths.get(baseDir, dir1), Paths.get(baseDir, dir2),
                            StandardCopyOption.REPLACE_EXISTING);
                    cmd.setCommand("Successfully moved/renamed dir.");
                    conn.sendMessage(cmd);
                    return;
                }
                case "DELETE": {
                    dirName = cmd.getArgs();
                    Files.delete(Paths.get(baseDir, dirName));
                    if (dirName.matches("[^a-zA-Z0-9\\.\\-]"))
                        throw new Exception("Invalid arguments");
                    cmd.setCommand("Successfully deleted dir.");
                    conn.sendMessage(cmd);
                    return;
                }
                case "LIST": {
                    dirName = cmd.getArgs();
                    if (dirName.matches("[^a-zA-Z0-9\\.\\-]"))
                        throw new Exception("Invalid arguments");

                    List<String> res = Files.list(Paths.get(baseDir, dirName)).map(x -> x.toString())
                            .collect(Collectors.toList());
                    cmd.setResults(res);
                    conn.sendMessage(cmd);
                    break;
            	}
  	          	case "UNDOSYNCDIR":{
					dirName = cmd.getArgs();
					cmd.setResults(DirectoryHelper.getSyncData(dirName));
					cmd.setCommand("SYNCDATA " + dirName);
					conn.sendMessage(cmd);
					break;
  	          	}
  	          	case "UNDODELETE": {
  	          		dirName = cmd.getArgs();
  	          		Files.createDirectories(Paths.get(baseDir, dirName));
  	          		if (dirName.matches("[^a-zA-Z0-9\\.\\-]"))
  	          			throw new Exception("Invalid arguments");
  	          		Server.this.ta.selectAll();
  	          		Server.this.ta.replaceSelection("");
				    for(i=0;i<logMsg.size();i++) {
  	          			//c = logMsg.get(i);
  	          			if((logMsg.contains("DELETE") && logMsg.contains("UNDODELETE")) || logMsg.contains("RENAME") || 
  	          					logMsg.contains("MOVE") && (logMsg.contains("UNDORENAME") || logMsg.contains("UNDOMOVE"))) {
  	  	          					logMsg.remove(i);
  	  	          			}
  		          		}
  	  	          		for(i=1;i<=logMsg.size();i++) {
  	          				Handler.this.server.log(logMsg.get(i) + "\n");
  	          			}
  	          		cmd.setCommand("Successfully created dir.");
  	          		conn.sendMessage(cmd);
	          		return;
				}
  	          	case "UNDORENAME":
  	          	case "UNDOMOVE": {
  	          		String dir1, dir2;
  	          		dir1 = cmd.getArgs().split(",")[0];
  	          		dir2 = cmd.getArgs().split(",")[1];
  	          		if (dir1.matches("[^a-zA-Z0-9\\.\\-]") || dir2.matches("[^a-zA-Z0-9\\.\\-]"))
  	          			throw new Exception("Invalid arguments");
  	          		Files.move(Paths.get(baseDir, dir2), Paths.get(baseDir, dir1),StandardCopyOption.REPLACE_EXISTING);
  	          		Server.this.ta.selectAll();
  	          		Server.this.ta.replaceSelection("");
  	          		for(i=0;i<logMsg.size();i++) {
  	          			//c = logMsg.get(i);
  	          			if((logMsg.contains("RENAME") || logMsg.contains("MOVE")) && (logMsg.contains("UNDORENAME") || logMsg.contains("UNDOMOVE"))) {
  	          					logMsg.remove(i);
  	          			}
	          		}
  	          		for(i=1;i<=logMsg.size();i++) {
          				Handler.this.server.log(logMsg.get(i) + "\n");
          			}
  	          		cmd.setCommand("Rename / Move Undo done!");
	          		conn.sendMessage(cmd);
  	          		break;
			  }
			  case "UNDOCREATE": {
				  dirName = cmd.getArgs();
                  Files.delete(Paths.get(baseDir, dirName));
                  if (dirName.matches("[^a-zA-Z0-9\\.\\-]"))
                      throw new Exception("Invalid arguments");
				  Server.this.ta.selectAll();
				  Server.this.ta.replaceSelection("");
				  for(i=0;i<logMsg.size();i++) {
	          			//c = logMsg.get(i);
	          			if(logMsg.contains("CREATE") && logMsg.contains("UNDOCREATE")) {
	          					logMsg.remove(i);
	          			}
	          			//Server.this.ta.append(logMsg.get(i) + "\n");
	          		}
	          		for(i=0;i<logMsg.size();i++) {
	          			Handler.this.server.log(logMsg.get(i) + "\n");
	          		}
	          		cmd.setCommand("Successfully deleted dir.");
					conn.sendMessage(cmd);
	          		return;
			  }
  	        }
          } catch (Exception ex) {
              //ex.printStackTrace();
              System.out.println("Command Execution failed.");
              Handler.this.server.log("Command Execution failed: " + ex.getMessage());
          }
      }

      @Override
      public void run() {
          try {
              while (true) {
                  Command incomingCommand = null;
                  if ((incomingCommand = conn.getMessage()) != null) {
                      // Handle command
                      if (incomingCommand.getCommand().equals("Username")) {

                          // If username is unique, then go ahead, create a home directory
                          if (this.server.registerUsername(incomingCommand.getArgs())) {
                              this.username = incomingCommand.getArgs();
                              Files.createDirectories(Paths.get(username));
                              isReady = true;
                              incomingCommand.setCommand("Connection Accepted");
                              Handler.this.server.log("Client connected: " + this.username);

                          } else {
                              // Reject username
                              incomingCommand.setCommand("Duplicate Username");

                              Handler.this.server.log("Duplicate username tried connecting.");
                          }
                          conn.sendMessage(incomingCommand);
                          conn.sendMessage(new Command("Directories on Server", String.join(", ", availableDirs)));
                      } else {
                          System.out.println(incomingCommand);}
                      	Handler.this.server.log("Command received from " + this.username + " : " + incomingCommand.toString());
                      	handleCommand(incomingCommand);
                  	}

                  Thread.sleep(500);
                  //Handler.this.server.

              }
          } catch (Exception ex) {
              ex.printStackTrace();
              System.out.println("Exception occurred in main Server thread");
              Handler.this.server.log(Handler.this.username + " exited.");
              connectedClients.remove(Handler.this.username);
              if(Handler.this.server.connectedClients.isEmpty())
            	  Handler.this.server.log("No clients are connected.");
              else
            	  Handler.this.server.log(Handler.this.server.connectedClients + " still connected.");
              return;
          }
      }
  }
}