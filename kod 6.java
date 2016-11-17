
/*
   The class MessageDialog represents a modal dialog that displays a message
   to the user along with one, two, or three buttons.  The dialog is displayed
   by the constructor.  The user must close the dialog by clicking on one
   of the buttons.  After the dialog has been closed by the user, the program
   can determine which button the user clicked by calling the getUserAction()
   method.  This method returns the name of the button that was clicked.
   Note that there are several constructors specifying from 0 to 3 button
   names.  If no button name is specified, then there will be a single 
   button named "OK".  Note also that a non-null parent Frame must
   be speciifed for the dialog in the constructor.
*/


import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

public class MessageDialog extends Dialog implements ActionListener {

   private String buttonClicked;  // The label of the button that the user
                                  // clicked to close this dialog.  The value
                                  // is null while the dialog is open.

   public String getUserAction() {
         // Can be called after the dialog has closed to find out which
         // button the user has clicked on.  The name of the button,
         // as specified in the constuctor.
      return buttonClicked;
   }
   
   public MessageDialog(Frame parent, String message) {
         // Make a dialog to display the message with an "OK" button.
         // Parent must be non-null and it is the parent frame of the dialog.
      this(parent,message,"OK",null,null);
   }
   
   public MessageDialog(Frame parent, String message, String button) {
         // Make a dialog to display the message with one button.
         // the button parameter specifies the name of the button.
         // (If button == null, then the name of the button will be "OK".)
         // Parent must be non-null and it is the parent frame of the dialog.
      this(parent,message,button,null,null);
   }
   
   public MessageDialog(Frame parent, String message, String button1, String button2) {
         // Make a dialog to display the message with one or two buttons
         // with the specified names button1 and button2.  If button1 is null,
         // the name "OK" is used.  If button2 is null, it is ignored.
         // Parent must be non-null and it is the parent frame of the dialog.
      this(parent,message,button1,button2,null);
   }
   
   public MessageDialog(Frame parent, String message, String button1, String button2, String button3) {
         // Make a dialog to display the message with one, two, or three buttons
         // with the specified names button1, button2 and button3.  If button1 is null,
         // the name "OK" is used.  If button2 or button3 is null, it is ignored.
         // Parent must be non-null and it is the parent frame of the dialog.
      super(parent, null,true);  // modal dialog with no title
      setBackground(Color.white);
      add("Center", new MessageCanvas(message));  // The message display canvas.
      Panel buttonBar = new Panel();  // A panel to hold the buttons.
      buttonBar.setLayout(new FlowLayout(FlowLayout.RIGHT,10,10));
      Button b1 = new Button( (button1 == null)? "OK" : button1 );
      b1.addActionListener(this);  // Button events are sent to this dialog.
      buttonBar.add(b1);
      if (button2 != null) {
         Button b2 = new Button(button2);
         b2.addActionListener(this);
         buttonBar.add(b2);
      }
      if (button3 != null) {
         Button b3 = new Button(button3);
         b3.addActionListener(this);
         buttonBar.add(b3);
      }
      add("South",buttonBar);
      pack();  // Resize the window to its preferred size.
               // Then move the window to a point 50 pixles over and 30 pixels
               // down from teh location of its parent frame.
      setLocation(parent.getLocation().x+50,parent.getLocation().y+30);
      show();  // make the dialog visible.
   }
   
   public Insets getInsets() {
        // Allow some space around the edges of the frame.
        // Note that a window might already uses insets to allow
        // space for the title bar and border, so it is necessary
        // to obtain a copy of the default insets and add to that.
        // (The documentation says that you aren't supposed to
        // modify the object returned by super.getInsets, so I
        // make a copy of it and modify the copy.)
      Insets ins = (Insets)super.getInsets().clone();
      ins.left += 5;
      ins.right += 5;
      ins.bottom += 12;
      ins.top += 5;
      return ins;
   }

   public void actionPerformed(ActionEvent evt) {
         // Respond when the user clicks on one of the buttons by saving the
         // name of the button that was clicked and closing the window.
      buttonClicked = evt.getActionCommand();
      dispose();
   }
   
   
   // The nested class MessageCanvas displays the message passed
   // to it in the constructor.  Unless the message is very short,
   // it will be broken into multiple lines.

   private static class MessageCanvas extends Canvas {
    
      private String message;  // A copy of the message
      
      // The following data is computed in makeStringList()
      
      private Vector messageStrings;  // The message broken up into lines.
      private int messageWidth;       // The width in pixels of the message display.
      private int messageHeight;      // The height in pixels of the message display.
      private Font font;              // The font that will be used to display the message.
      private int lineHeight;         // The height of one line in that font.
      private int fontAscent;         // The font ascent of the font (disance from the
                                      //   baseline to the top of a tall character.)
   
      MessageCanvas(String message) {
           // Constructor: store the message.
         if (message == null)
            this.message = "";  // this.message can't be null.
         else 
            this.message = message;
      }
   
      public Dimension getPreferredSize() {
            // Return the message size, as determined by makeStringList(), allowing
            // space for a border around the message.
         if (messageStrings == null)
            makeStringList();
         return new Dimension(messageWidth + 20, messageHeight + 17);
      }
      
      public void paint(Graphics g) {
            // Display the message using data stored in instance variables.
         if (messageStrings == null)
            makeStringList();
         int y = (getSize().height - messageHeight)/2 + fontAscent;
         if (y < fontAscent)
            y = fontAscent;
         int x = (getSize().width - messageWidth)/2;
         if (x < 0)
            x = 0;
         g.setFont(font);
         for (int i = 0; i < messageStrings.size(); i++) {
            g.drawString( (String)messageStrings.elementAt(i), x, y);
            y += lineHeight;
         }
      }
      
      private void makeStringList() {
             // Compute all the instance variables necessary for displaying
             // the message.  If the total width of the message in pixels
             // would be more than 280, break it up into several lines.
         messageStrings = new Vector();
         font = new Font("Dialog", Font.PLAIN, 12);
         FontMetrics fm = getFontMetrics(font);
         lineHeight = fm.getHeight() + 3;
         fontAscent = fm.getAscent();
         int totalWidth = fm.stringWidth(message);
         if (totalWidth <= 280) {
            messageStrings.addElement(message);
            messageWidth = 280;
            messageHeight = lineHeight;
         }
         else {
            if (totalWidth > 1800)
               messageWidth = Math.min(500, totalWidth/6);
            else
               messageWidth = 300;
            int actualWidth = 0;
            String line = "    ";
            String word = "";
            message += " ";   // this forces word == "" after the following for loop ends.
            for (int i = 0; i < message.length(); i++) {
               if (message.charAt(i) == ' ') {
                  if (fm.stringWidth(line + word) > messageWidth + 8) {
                      messageStrings.addElement(line);
                      actualWidth = Math.max(actualWidth,fm.stringWidth(line));
                      line = "";
                  }
                  line += word;
                  if (line.length() > 0)
                     line += ' ';
                  word = "";
               }
               else {
                  word += message.charAt(i);
               }
            }
            if (line.length() > 0) {
                messageStrings.addElement(line);
                actualWidth = Math.max(actualWidth, fm.stringWidth(line));
                   
            }
            messageHeight = lineHeight*messageStrings.size() - fm.getLeading();
            messageWidth = Math.max(280,actualWidth);
         }
      }
   
   }  // end nested class Message Canvas


}  // end class MessageDialog