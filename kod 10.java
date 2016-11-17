
/* 
    The ShapeDraw applet lets the user add small colored shapes to
    a drawing area and then drag them around.  The shapes are rectangles,
    ovals, and roundrects.  The user adds a shape to the canvas by
    clicking on a button.  The shape is added at the upper left corner
    of the canvas.  The color of the shape is given by the current
    setting of a pop-up menu.  The user can drag the shapes with the
    mouse.  Ordinarily, the shapes maintain a given back-to-front order.
    However, if the user shift-clicks on a shape, that shape will be
    brought to the front.
    
    A menu can be popped up on a shape (by right-clicking or performing
    some othe platform-dependent action).  This menu allows the user
    to change the size and color of a shape.  It is also possible to
    delete the shape and to bring it to the front.
    
    This file defines the applet class plus several other classes used
    by the applet, namely:  ShapeCanvas, Shape, RectShape, OvalShape,
    and RoundRectShape.
    
    David Eck
    July 28,  1998
*/


import java.awt.*;
import java.awt.event.*;
import java.applet.Applet;
import java.util.Vector;


public class ShapeDrawWithMenu extends Applet {
   
   public void init() {  
        // Set up the applet's GUI.  It consists of a canvas, or drawing area,
        // plus a row of controls below the canvas.  The controls include three
        // buttons which are used to add shapes to the canvas and a Choice menu
        // that is used to select the color used for a shape when it is created.
        // The canvas is set as the "listener" for these controls so that it can
        // respond to the user's actions.  (The pop-up menu is created by the canvas.)
   
      setBackground(Color.lightGray);
      
      ShapeCanvas canvas = new ShapeCanvas();  // create the canvas

      Choice colorChoice = new Choice();  // color choice menu
      colorChoice.add("Red");
      colorChoice.add("Green");
      colorChoice.add("Blue");
      colorChoice.add("Cyan");
      colorChoice.add("Magenta");
      colorChoice.add("Yellow");
      colorChoice.add("Black");
      colorChoice.add("White");
      colorChoice.addItemListener(canvas);
      
      Button rectButton = new Button("Rect");    // buttons for adding shapes
      rectButton.addActionListener(canvas);

      Button ovalButton = new Button("Oval");
      ovalButton.addActionListener(canvas);

      Button roundRectButton = new Button("RoundRect");
      roundRectButton.addActionListener(canvas);
      
      Panel bottom = new Panel();   // a Panel to hold the control buttons
      bottom.setLayout(new GridLayout(1,4,3,3));
      bottom.add(rectButton);
      bottom.add(ovalButton);
      bottom.add(roundRectButton);
      bottom.add(colorChoice);
   
      setLayout(new BorderLayout(3,3));
      add("Center",canvas);              // add canvas and controls to the applet
      add("South",bottom);
      
   }
   
   public Insets getInsets() {
        // Says how much space to leave between the edges of the applet and the
        // components in the applet.
      return new Insets(3,3,3,3);
   }
   
}  // end class ShapeDraw


class ShapeCanvas extends Canvas implements ActionListener, ItemListener,
                                            MouseListener, MouseMotionListener {
    
      // This class represents a canvas that can display colored shapes and
      // let the user drag them around.  It uses an off-screen images to 
      // make the dragging look as smooth as possible.  A pop-up menu is
      // added to the canvas that can be used to performa certain actions
      // on the shapes;

   Image offScreenCanvas = null;   // off-screen image used for double buffering
   Graphics offScreenGraphics;     // graphics context for drawing to offScreenCanvas
   Vector shapes = new Vector();   // holds a list of the shapes that are displayed on the canvas
   Color currentColor = Color.red; // current color; when a shape is created, this is its color
   

   ShapeCanvas() {
        // Constructor: set background color to white, set up listeners to respond to mouse actions,
        //              and set up the pop-up menu

      setBackground(Color.white);

      addMouseListener(this);
      addMouseMotionListener(this);

      popup = new PopupMenu();
      popup.add("Red");
      popup.add("Green");
      popup.add("Blue");
      popup.add("Cyan");
      popup.add("Magenta");
      popup.add("Yellow");
      popup.add("Black");
      popup.add("White");
      popup.addSeparator();
      popup.add("Big");
      popup.add("Medium");
      popup.add("Small");
      popup.addSeparator();
      popup.add("Delete");
      popup.add("Bring To Front");
      add(popup);
      popup.addActionListener(this);

   } // end construtor

   synchronized public void paint(Graphics g) {
        // In the paint method, everything is drawn to an off-screen canvas, and then
        // that canvas is copied onto the screen.
      makeOffScreenCanvas();
      g.drawImage(offScreenCanvas,0,0,this);
   }
   
   public void update(Graphics g) {
        // Update method is called when canvas is to be redrawn.
        // Just call the paint method.
      paint(g);
   }
   
   void makeOffScreenCanvas() {
         // Erase the off-screen canvas and redraw all the shapes in the list.
         // (First, if canvas has not yet been created, then create it.)
      if (offScreenCanvas == null) {
         offScreenCanvas = createImage(getSize().width,getSize().height);
         offScreenGraphics = offScreenCanvas.getGraphics();
      }
      offScreenGraphics.setColor(getBackground());
      offScreenGraphics.fillRect(0,0,getSize().width,getSize().height);
      int top = shapes.size();
      for (int i = 0; i < top; i++) {
         Shape s = (Shape)shapes.elementAt(i);
         s.draw(offScreenGraphics);
      }
   }   
   
   public void itemStateChanged(ItemEvent evt) {
          // This is called to respond to item events.  Such events
          // can only be sent by the color choice menu,
          // so respond by setting the current color according to
          // the selected item in that menu.
      Choice colorChoice = (Choice)evt.getItemSelectable();
      switch (colorChoice.getSelectedIndex()) {
         case 0: currentColor = Color.red;     break;
         case 1: currentColor = Color.green;   break;
         case 2: currentColor = Color.blue;    break;
         case 3: currentColor = Color.cyan;    break;
         case 4: currentColor = Color.magenta; break;
         case 5: currentColor = Color.yellow;  break;
         case 6: currentColor = Color.black;   break;
         case 7: currentColor = Color.white;   break;
      }
   }
   
   public void actionPerformed(ActionEvent evt) {
          // Called to respond to action events.  The three shape-adding
          // buttons have been set up to send action events to this canvas.
          // Respond by adding the appropriate shape to the canvas.  This
          // also be a command from a pop-up menu.
      String command = evt.getActionCommand();
      if (command.equals("Rect"))
         addShape(new RectShape());
      else if (command.equals("Oval"))
         addShape(new OvalShape());
      else if (command.equals("RoundRect"))
         addShape(new RoundRectShape());
      else
         doPopupMenuCommand(command);
   }
   
   synchronized void addShape(Shape shape) {
          // Add the shape to the canvas, and set its size/position and color.
          // The shape is added at the top-left corner, with size 50-by-30.
          // Then redraw the canvas to show the newly added shape.
      shape.setColor(currentColor);
      shape.reshape(3,3,50,30);
      shapes.addElement(shape);
      repaint();
   }
   

   // ------------ This rest of the class implements dragging and the pop-up menu ---------------------
   
   PopupMenu popup;
   
   Shape selectedShape = null;     // This is null unless a menu has been popped up on this shape.

   Shape draggedShape = null;      // This is null unless a shape has been selected for dragging.

   int prevDragX;  // During dragging, these record the x and y coordinates of the
   int prevDragY;  //    previous position of the mouse.
   
   Shape clickedShape(int x, int y) {
         // Find the frontmost shape at coordinates (x,y); return null if there is none.
      for ( int i = shapes.size() - 1; i >= 0; i-- ) {  // check shapes from front to back
         Shape s = (Shape)shapes.elementAt(i);
         if (s.containsPoint(x,y))
            return s;
      }
      return null;
   }
   
      
   void doPopupMenuCommand(String command) {
         // Handle a command from the pop-up menu.
      if (selectedShape == null)  // should be impossible
         return;
      if (command.equals("Red"))
         selectedShape.setColor(Color.red);
      else if (command.equals("Green"))
         selectedShape.setColor(Color.green);
      else if (command.equals("Blue"))
         selectedShape.setColor(Color.blue);
      else if (command.equals("Cyan"))
         selectedShape.setColor(Color.cyan);
      else if (command.equals("Magenta"))
         selectedShape.setColor(Color.magenta);
      else if (command.equals("Yellow"))
         selectedShape.setColor(Color.yellow);
      else if (command.equals("Black"))
         selectedShape.setColor(Color.black);
      else if (command.equals("White"))
         selectedShape.setColor(Color.white);
      else if (command.equals("Big"))
         selectedShape.resize(75,45);
      else if (command.equals("Medium"))
         selectedShape.resize(50,30);
      else if (command.equals("Small"))
         selectedShape.resize(25,15);
      else if (command.equals("Delete"))
         shapes.removeElement(selectedShape);
      else if (command.equals("Bring To Front")) {
         shapes.removeElement(selectedShape);
         shapes.addElement(selectedShape);
      }
      repaint();
   }

   
   synchronized public void mousePressed(MouseEvent evt) {
         // User has pressed the mouse.  Find the shape that the user has clicked on, if
         // any.  If there is a shape at the position when the mouse was clicked, then
         // start dragging it.  If the user was holding down the shift key, then bring
         // the dragged shape to the front, in front of all the other shapes.
      int x = evt.getX();  // x-coordinate of point where mouse was clicked
      int y = evt.getY();  // y-coordinate of point 
      
      if (evt.isPopupTrigger()) {            // If this is a pop-up menu event that
         selectedShape = clickedShape(x,y);  // occurred over a shape, record which shape
         if (selectedShape != null)          // it is and show the menu.
            popup.show(this,x,y);
      }
      else {
         draggedShape = clickedShape(x,y);
         if (draggedShape != null) {
            prevDragX = x;
            prevDragY = y;
            if (evt.isShiftDown()) {                 // Bring the shape to the front by moving it to
               shapes.removeElement(draggedShape);  //       the end of the list of shapes.
               shapes.addElement(draggedShape);
               repaint();  // repaint canvas to show shape in front of other shapes
            }
         }
      }
   }
   
   synchronized public void mouseDragged(MouseEvent evt) {
          // User has moved the mouse.  Move the dragged shape by the same amount.
      if (draggedShape != null) {
         int x = evt.getX();
         int y = evt.getY();
         draggedShape.moveBy(x - prevDragX, y - prevDragY);
         prevDragX = x;
         prevDragY = y;
         repaint();      // redraw canvas to show shape in new position
      }
   }
   
   synchronized public void mouseReleased(MouseEvent evt) {
          // User has released the mouse.  Move the dragged shape, then set
          // shapeBeingDragged to null to indicate that dragging is over.
          // If the shape lies completely outside the canvas, remove it
          // from the list of shapes (since there is no way to ever move
          // it back onscreen).
      int x = evt.getX();
      int y = evt.getY();
      if (draggedShape != null) {
         draggedShape.moveBy(x - prevDragX, y - prevDragY);
         if ( draggedShape.left >= getSize().width || draggedShape.top >= getSize().height ||
                 draggedShape.left + draggedShape.width < 0 ||
                 draggedShape.top + draggedShape.height < 0 ) {  // shape is off-screen
            shapes.removeElement(draggedShape);  // remove shape from list of shapes
         }
         draggedShape = null;
         repaint();
      }
      else if (evt.isPopupTrigger()) {        // If this is a pop-up menu event that
         selectedShape = clickedShape(x,y);   // occurred over a shape, record the
         if (selectedShape != null)           // shape and show the menu.
            popup.show(this,x,y);
      }      
   }
   
   public void mouseEntered(MouseEvent evt) { }   // Other methods required for MouseListener and 
   public void mouseExited(MouseEvent evt) { }    //              MouseMotionListener interfaces.
   public void mouseMoved(MouseEvent evt) { }
   public void mouseClicked(MouseEvent evt) { }
   
}  // end class ShapeCanvas



abstract class Shape {

      // A class representing shapes that can be displayed on a ShapeCanvas.
      // The subclasses of this class represent particular types of shapes.
      // When a shape is first constucted, it has height and width zero
      // and a default color of white.

   int left, top;      // Position of top left corner of rectangle that bounds this shape.
   int width, height;  // Size of the bounding rectangle.
   Color color = Color.white;  // Color of this shape.
   
   void reshape(int left, int top, int width, int height) {
         // Set the position and size of this shape.
      this.left = left;
      this.top = top;
      this.width = width;
      this.height = height;
   }
   
   void resize(int width,int height) {
         // Set the size without changing the position
      this.width = width;
      this.height = height;
   }
   
   void moveTo(int x, int y) {
          // Move upper left corner to the point (x,y)
      this.left = x;
      this.top = y;
   }
   
   void moveBy(int dx, int dy) {
          // Move the shape by dx pixels horizontally and dy pixels veritcally
          // (by changing the position of the top-left corner of the shape).
      left += dx;
      top += dy;
   }
   
   void setColor(Color color) {
          // Set the color of this shape
      this.color = color;
   }

   boolean containsPoint(int x, int y) {
         // Check whether the shape contains the point (x,y).
         // By default, this just checks whether (x,y) is inside the
         // rectangle that bounds the shape.  This method should be
         // overridden by a subclass if the default behaviour is not
         // appropriate for the subclass.
      if (x >= left && x < left+width && y >= top && y < top+height)
         return true;
      else
         return false;
   }

   abstract void draw(Graphics g);  
         // Draw the shape in the graphics context g.
         // This must be overriden in any concrete subclass.

}  // end of class Shape



class RectShape extends Shape {
      // This class represents rectangle shapes.
   void draw(Graphics g) {
      g.setColor(color);
      g.fillRect(left,top,width,height);
      g.setColor(Color.black);
      g.drawRect(left,top,width,height);
   }
}


class OvalShape extends Shape {
       // This class represents oval shapes.
   void draw(Graphics g) {
      g.setColor(color);
      g.fillOval(left,top,width,height);
      g.setColor(Color.black);
      g.drawOval(left,top,width,height);
   }
   boolean containsPoint(int x, int y) {
         // Check whether (x,y) is inside this oval, using the
         // mathematical equation of an ellipse.
      double rx = width/2.0;   // horizontal radius of ellipse
      double ry = height/2.0;  // vertical radius of ellipse 
      double cx = left + rx;   // x-coord of center of ellipse
      double cy = top + ry;    // y-coord of center of ellipse
      if ( (ry*(x-cx))*(ry*(x-cx)) + (rx*(y-cy))*(rx*(y-cy)) <= rx*rx*ry*ry )
         return true;
      else
        return false;
   }
}


class RoundRectShape extends Shape {
       // This class represents rectangle shapes with rounded corners.
       // (Note that it uses the inherited version of the 
       // containsPoint(x,y) method, even though that is not perfectly
       // accurate when (x,y) is near one of the corners.)
   void draw(Graphics g) {
      g.setColor(color);
      g.fillRoundRect(left,top,width,height,width/3,height/3);
      g.setColor(Color.black);
      g.drawRoundRect(left,top,width,height,width/3,height/3);
   }
}

