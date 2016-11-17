
/* This applet supports two-way chatting between users.  It
   implements a client for the ConnectionBroker server,
   which can set up a relayed connection between a pair of such
   clients.  There are really two functions:  The user can register
   with the server as a client who is willing to accept a connection,
   or the user can make a connection with one of the clients who
   is waiting on the server.  The applet retrieves and displays
   a list of waiting clients when it starts.  There is a Refresh
   button that the user can click to refresh this list (since the
   list of waiting clients can change from time to time).  The
   user connects with one of the clients on the list by clicking
   it and then clicking a Connect button.  Finally, there is 
   a register button that will add the user to the list and
   then wait for a connection.  There is an input box where the
   user can enter a name or other information to be displayed
   in other users' client lists.  The use can be a party to
   multiple connections simultaneously.  A separater window
   opens for each connection that can be used to send and retrieve
   messages.  (Note that there is nothing to stop a user from
   chatting with him or herself.)
      An applet parameter named "server" can be used to specify the
   name or IP number of the server computer.  If none is specified,
   the computer from which the applet was loaded is used.  Another
   applet parameter, named "port", can be used to specify the
   port on which the server is listening.  If none is specified,
   then the port given by the constant DEFAULT_PORT is used.
      This class can also be run as a standalone application.
   In that case, the server must be specified as the first
   command-line parameter.  The port, if differnent from the
   DEFAULT_PORT, can be specified as the second parameter.
*/

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.applet.Applet;
import java.util.Vector;

public class BrokeredChat extends Applet
                implements Runnable, ActionListener, ItemListener {
                
   public static void main(String[] args) {
        // Run the applet as a standalone program in its own window,
        // using information about the server from the command line.
      if (args.length == 0) {
         System.out.println("Usage:  java BrokeredChat <server> [<port>]");
         return;
      }
      BrokeredChat applet = new BrokeredChat();
      applet.computer = args[0];   // Set these values so the applet
      applet.port = DEFAULT_PORT;  //    won't do it when it starts.
      if (args.length > 1) {
         try {
            applet.port = Integer.parseInt(args[1]);
         }
         catch (NumberFormatException e) {
         }
      }
      applet.init();
      Frame window = new Frame("BrokeredChat");
      window.add(applet, BorderLayout.CENTER);
      window.setSize(450,350);
      window.addWindowListener(
            new WindowAdapter() {
               public void windowClosing(WindowEvent evt) {
                  System.exit(0);
               }
            }
        );
      window.show();
      applet.start();
   }

   /* Command chars that are used for communicating with the
      server.  See BrokeredChat.java for more info. */

   static final char REGISTER = '[';          // Commands that the
   static final char CONNECT = '=';           //    applet can send
   static final char SEND_CLIENT_LIST = ':';  //    to the server.

   static final char NOT_AVAILABLE = '!';     // Responses from the
   static final char CONNECTED = '.';         //    server.
   static final char CLIENT_INFO = '>';
   static final char END_CLIENT_INFO = '<';
   

   static final int DEFAULT_PORT = 3030;  // Listening port on server,
                                          //   if another is not given.
   
   Label display;         // Displays infomation to the user.
   List clients;          // List of available clients from the server.
   Button connectButton;  // For connecting to one of the waiting clients.
   Button refreshButton;  // For refreshing the list of clients.
   Button registerButton; // For registering with the server.
   TextField infoInput;   // Contains info sent to server to appear
                          //   as a description of this client, when
                          //   the client registers with the server.
   
   Thread runner;         // A thread that fetches the list of
                          //   available clients from the server.
   Vector clientStrings;  // Client descriptions received from
                          //   server.  Each description is a
                          //   ID number, followed by a space, followed
                          //   by the client's info.
                          
   String computer;  // Computer where server runs.
   int port;         // Port on which server listens.
   

   public void init() {
          // Set up the user interface for the applet.
   
      setBackground(Color.gray);
      setLayout(new BorderLayout(10,10));
      Panel top = new Panel();
      top.setLayout(new BorderLayout(2,2));
      add(top, BorderLayout.CENTER);
      Panel bottom = new Panel();
      bottom.setLayout(new GridLayout(2,1,2,2));
      add(bottom, BorderLayout.SOUTH);
      
      display = new Label("Available Connections:");
      display.setBackground(Color.white);
      top.add(display, BorderLayout.NORTH);
      
      clients = new List();
      clients.setBackground(Color.white);
      clients.addItemListener(this);
      top.add(clients, BorderLayout.CENTER);
      
      Panel bts = new Panel();
      top.add(bts, BorderLayout.SOUTH);
      bts.setLayout(new GridLayout(1,2,2,2));
      connectButton = new Button("Connect");
      connectButton.setBackground(Color.lightGray);
      connectButton.setEnabled(false);
      connectButton.addActionListener(this);
      bts.add(connectButton);
      refreshButton = new Button("Refresh list");
      refreshButton.setBackground(Color.lightGray);
      refreshButton.setEnabled(false);
      refreshButton.addActionListener(this);
      bts.add(refreshButton);
      
      registerButton = new Button("Register me with this info:");
      registerButton.setBackground(Color.lightGray);
      registerButton.addActionListener(this);
      bottom.add(registerButton);
      
      infoInput = new TextField();
      infoInput.setBackground(Color.white);
      bottom.add(infoInput);
      
   }  // end init();
   

   public Insets getInsets() {
        // Specify a border around the edges of the applet.
      return new Insets(3,3,3,3);
   }
   

   public void start() {
         // When the applet starts (or restarts), start a
         // thread to get the list of available users from
         // the server.  First, get the computer name and
         // listening portif that has not already been done.
      if (computer == null) {
         try {
            String portStr = getParameter("port");
            if (portStr == null)
               port = DEFAULT_PORT;
            else {
               try {
                  port = Integer.parseInt(portStr);
               }
               catch (NumberFormatException e) {
                  port = DEFAULT_PORT;
               }
            }
            computer = getParameter("server");
            if (computer == null) {
                  // Use the computer from which the applet was loaded.
               computer = getCodeBase().getHost();
            }
         }
         catch (Exception e) {
               // As a last resort, try to use the computer
               // on which the applet is running.
            computer = "127.0.0.1";
            port = DEFAULT_PORT;
         }
      }
      doRefresh();
   }
   

   public void itemStateChanged(ItemEvent evt) {
        // When an item is selected/deselected, set the enabled
        // state of the Connect button so it can only be used
        // when an item is selected in the list of available clients.
      synchronized(clients) {
         connectButton.setEnabled( clients.getSelectedItem() != null );
      }
   }
   

   public void actionPerformed(ActionEvent evt) {
         // Respond when the user clicks one of the buttons.
      Object source = evt.getSource();
      if (source == connectButton) {
         doConnect();
      }
      else if (source == refreshButton) {
         doRefresh();
      }
      else if (source == registerButton) {
         doRegister();
      }
   }
   

   void doRefresh() {
        // Start a thread to get the list of available clients
        // from the server (unless one is already running).
        // (Possibly I should just do it here, rather than
        // starting a thread.  Better yet, I should use other
        // threadsfor the doConnect() and doRegister() commands.)
      if (runner != null && runner.isAlive())
         return;
      runner = new Thread(this);
      runner.start();
   }   
   

   void doConnect() {
         // Respond to a click on the Connect button by requesting
         // a connection to the selected client.  If the connection
         // succeeds, open a window for chatting with that client.
         // Also, refresh the list of available clients, since
         // the client to whom we've just connected should be
         // gone.
      String info, ID;  // Data from the selected client string.
      synchronized(clients) {
         int clientIndex = clients.getSelectedIndex();
         if (clientIndex < 0)
            return;
         info = (String)clientStrings.elementAt(clientIndex);
      }
      int spacePos = info.indexOf(" ");
      ID = info;
      if (spacePos > 0) {
         ID = ID.substring(0,spacePos);
         info = info.substring(spacePos+1);
      }
      try {
         Socket connection = new Socket(computer,port);
         TextReader in = new TextReader(connection.getInputStream());
         PrintWriter out = new PrintWriter(connection.getOutputStream());
         out.println(CONNECT + ID);  // Request the connection.
         out.flush();
         if (out.checkError())
            throw new IOException("Can't send command.");
         String answer = in.getln();  // Read the server's response.
         if (answer.length() == 0 || answer.charAt(0) != CONNECTED)
            throw new IOException("Can't connect.");
         new ConnectionWindow(info, connection);
         display.setText("Available Connections:");
         doRefresh();
      }
      catch (Exception e) {
            // If an error occurs, display it.
         display.setText(e.toString());
      }
   }  // End doConnect()
   

   void doRegister() {
        // Called when the user clicks the Register button.
        // Register with the server, sending the info from
        // the infoInput box.  If the registration succeeds,
        // open a window.  This window will display the
        // message "Waiting for connection" and will wait
        // until someone out on the net asks to connect to
        // this user.  After that, the window can be used for
        // communication.  It can be closed at any time to
        // cancel the registration.  Also, if the registration
        // completes, the list of available clients is refreshed.
        // It should show this user.
      try {
         String info = infoInput.getText().trim();
         Socket connection = new Socket(computer,port);
         PrintWriter out = new PrintWriter(connection.getOutputStream());
         out.println(REGISTER + info);
         out.flush();
         if (out.checkError())
            throw new IOException("Can't send info.");
         if (info.length() == 0)
            new ConnectionWindow("Connection",connection,true);
         else
            new ConnectionWindow("Connection (" + info 
                                            + ")", connection,true);
         infoInput.setText("");
         doRefresh();
      }
      catch (Exception e) {
         display.setText(e.toString());
      }
   } // end doRegister)_
   
   
   void makeClientList(Vector clientInfo) {
        // This is called by the run() method to put the
        // data the list of available clients that it has
        // received into the List on the screen.
      synchronized(clients) {
         clientStrings = clientInfo;
         for (int i = 0; i < clientInfo.size(); i++) {
            String info = (String)clientInfo.elementAt(i);
            int spacePos = info.indexOf(" ");
            if (spacePos > 0)
               info = info.substring(spacePos+1);
            clients.add(info);
         }
         display.setText("Available Connections:");
      }
   }
   

   public void run() {
        // Gets the list of available clients from the server.
        // The current list on the screen is deleted and replaced
        // with the retrieved list.  The refresh button is disabled
        // while the list is being retrieved.
      refreshButton.setEnabled(false);
      clients.removeAll();
      connectButton.setEnabled(false);
      try {
         Socket connection = new Socket(computer,port);
         TextReader in = new TextReader(connection.getInputStream());
         PrintWriter out = new PrintWriter(connection.getOutputStream());
         out.println("" + SEND_CLIENT_LIST);  // Request the client list.
         out.flush();
         if (out.checkError())
            throw new IOException("Can't send command.");
         Vector clientInfo = new Vector();
         while (true) {
               // Get the responses and put them in the clientInfo vector.
            String line = in.getln();
            if (line.length() > 0) {
               if (line.charAt(0) == END_CLIENT_INFO)
                  break;
               else if (line.charAt(0) == CLIENT_INFO)
                  clientInfo.addElement( line.substring(1) );
               else
                  throw new IOException("Bad data received.");
            }
         }
         if (clientInfo.size() == 0)
            display.setText("No connections available!");
         else
            makeClientList(clientInfo);
      }
      catch (Exception e) {
         display.setText(e.toString());
      }
      finally {
         refreshButton.setEnabled(true);
      }
   } // end run()
   

}  // end class BrokeredChat
