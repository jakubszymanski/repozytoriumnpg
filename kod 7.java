/*
   This applet lets the user place colored rectangles on a canvas
   by right-clicking the canvas.  The color of the rectangle is given
   by a color palette that lies along the bottom of the applet.
   Alt-clicking (or clicking with middle mouse button) on a rectangle
   will delete it.  Shift-clicking a rectangle will move it out in
   front of all the other rectangles.  With no modifier key,
   the user can left-click and drag to move the rectangles around.
   They are restricted so that they cannot be moved entirely off
   the canvas.
   
   The main point of this applet is to demonstrate the use of a
   Vector.
   
   This file defines four classes, the applet class, SimpleDrawRects,
   and three support classes, ColoredRect, RainbowPalette, and
   RectCanvas.  All the work with rectangles is done in the 
   RectCanvas class.  The RainbowPalette class is an example of
   a custom component class.
*/


import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.*;    // (For access to the Vector class.)


public class SimpleDrawRects extends Applet {

      // The applet class simple creates a BorderLayout with
      // A RectCanvas in the Center and a RainbowPalette in
      // the South position.  The variable that refers to the
      // palette is in the canvas class.

   public void init() {
      setBackground(Color.gray);
      setLayout(new BorderLayout(2,2));
      RectCanvas canvas = new RectCanvas();
      canvas.colorInput = new RainbowPalette();
      add(canvas, BorderLayout.CENTER);
      add(canvas.colorInput, BorderLayout.SOUTH);
   }
   
   public Insets getInsets() {
      return new Insets(2,2,2,2);
   }
   
}  // end class SimpleDrawRects



class ColoredRect {
       // Represents the data for one colored rectangle.
   int x, y, width, height;   // Location and size of rect.
   Color color;               // Color of rect.
}



class RainbowPalette extends Canvas implements MouseListener {

      // A custom component that lets the user select a color.
      // The available colors are bright, saturated colors in
      // a full range of hues.  The possible colors are shown
      // in a strip.  The user clicks on this strip to select
      // a color.  The selected color is hilited with a white
      // frame.  A program that uses this component can find
      // out which color is currently selected by calling 
      // the getSelectedColor() method.
      
   /* The currently selected color is stored in the variable
      selectedColor.  The hue of this color -- a float value
      in the range 0.0F to 1.0F is stored in selectedHue.
      The value of selectedHue is used to determine where
      to draw the hilite on the palette */

   private float selectedHue = 0;
   private Color selectedColor = Color.getHSBColor(0,1,1);

   /* Some variable for an off-screen canvas.  The palette,
      without the white box that hilites the selected color,
      is stored in the off-screen canvas.  This means that it
      only has to be redrawn if the applet changes size. */

   private Image OSC;        // The off-screen canvas.
   private int widthOfOSC;   // The current width of OSC (if OSC != null).
   private int heightOfOSC;  // The current height of OSC (if OSC != null).
   
   RainbowPalette() {
         // Constructor.  Set the component to listen for mouse clicks
         // on itself.
      addMouseListener(this);
   }
   
   public Color getSelectedColor() {
         // Return the color that is currently selected in the palette.
      return selectedColor;
   }

   public Dimension getPreferredSize() {
         // Return a good size for this component. (The width
         // is chosen to allow for 256 colors in the palette.)
         // This is for use by layout managers.
      return new Dimension(264, 24);
   }
   
   public Dimension getMinimumSize() {
         // Return the smallest reasonable size for this component.
         // This is for use by layout managers.
      return new Dimension(100,16);
   }
   
   public void update(Graphics g) {
          // Redefine update() so it just calls paint() without
          // first erasing the component.
      paint(g);
   }
   
   public void paint(Graphics g) {
          // Create a new palette in an off-screen canvas if there
          // is currently no off-screen canvas or if the component
          // has changed size.  Copy the palette onto the component
          // and add a white rectangle to hilite the selected color.
      if (OSC == null || widthOfOSC != getSize().width
                          || heightOfOSC != getSize().height){
          OSC = createImage(getSize().width, getSize().width);
          widthOfOSC = getSize().width;
          heightOfOSC = getSize().height;
          Graphics OSG = OSC.getGraphics();
          OSG.setColor(Color.black);
          OSG.fillRect(0,0,widthOfOSC,heightOfOSC);
          for (int i = 0; i < widthOfOSC - 8; i++) {
             float hue = (float)i / (widthOfOSC-8);
             OSG.setColor( Color.getHSBColor(hue, 1, 1) );
             OSG.drawLine(i+4,4,i+4,heightOfOSC-5);
          }
          OSG.dispose();
      }
      g.drawImage(OSC,0,0,this);
      int x = 4 + (int)(selectedHue*(widthOfOSC-8));  // x-coord of selected color.
      g.setColor(Color.white);
      g.drawRect(x-2,3,2,heightOfOSC-7);  // Draw the hilite.
      g.drawRect(x-3,2,4,heightOfOSC-5);
   }
   
   public void mousePressed(MouseEvent evt) {
           // When the user clicks on the component, select the
           // color that the user clicked.  But make sure that
           // the selectedHue is in the legal range, 0 to 1.
       int x = evt.getX();
       selectedHue = (float)x / (getSize().width - 4);
       if (selectedHue < 0)
          selectedHue = 0;
       else if (selectedHue > 1)
          selectedHue = 1;
       selectedColor = Color.getHSBColor(selectedHue, 1, 1);
       repaint();
   }
   
   public void mouseReleased(MouseEvent evt) { }
   public void mouseClicked(MouseEvent evt) { }
   public void mouseEntered(MouseEvent evt) { }
   public void mouseExited(MouseEvent evt) { }

}  // end class RainbowPalette




class RectCanvas extends Canvas
                          implements MouseListener, MouseMotionListener {
        
        // This class is a canvas that shows some colored rectangles.
        // The user adds a rectangle by right-clicking on the canvas.
        // The user can delete a rectangle by Alt-clicking it, and can
        // move it out in front of the other rectangles by Shift-clicking
        // it.  The user can also click-and-drag rectangles to move them
        // around the canvas.

   RainbowPalette colorInput;  // The color of a rectangle is given by the
                               // selected color in this paletter.  This variable
                               // must be given a value by the applet class.
   
   private Vector rects;   // The colored rectangles are represented by objects
                           // of type ColoredRect that are stored in this Vector.
                           
   /* Variables for implementing an off-screen canvas.  This canvas holds a
      complete copy of what's on the screen.  It is redrawn every time the
      screen is repainted.  The purpose is to implement smooth dragging. */
   
   private Image OSC;   // The off-screen canvas.
   private int widthOfOSC, heightOfOSC;  // Current size of off-screen canvas.
   
   /* Variables for implementing dragging. */
   
   private boolean dragging;      // This is true when dragging is in progress.
   private ColoredRect dragRect;  // The rect that is being dragged (if dragging is true).
   private int offsetx, offsety;  // The distance from the upper left corner of the
                                  //   dragRect to the point where the user clicked
                                  //   the rect.  This offset is maintained as the
                                  //   rect is dragged.
   
   
   RectCanvas() {
         // Constructor.  The canvas listens for mouse events, and a
         // Vector is created to hold the ColoredRects.
      setBackground(Color.white);
      addMouseListener(this);
      addMouseMotionListener(this);
      rects = new Vector();
   }
   
   ColoredRect findRect(int x, int y) {
         // Find the topmost rect that contains the point (x,y).
         // Return null if no rect contains that point.
         // The rects in the Vector are considered in reverse order
         // so that if one lies on top of another, the one on top
         // is seen first and is returned.
       for (int i = rects.size() - 1;  i >= 0;  i--) {
          ColoredRect rect = (ColoredRect)rects.elementAt(i);
          if ( x >= rect.x && x < rect.x + rect.width
                      && y >= rect.y && y < rect.y + rect.height )
              return rect;  // (x,y) is inside this rect.
       }
       return null;
   }
   
   void bringToFront(ColoredRect rect) {
           // If rect != null, move it out in front of the other
           // rects by moving it to the last position in the Vector.
      if (rect != null) {
          rects.removeElement(rect);  // Remove rect from current position.
          rects.addElement(rect);     // Put rect in the Vector in last position.
          repaint();
      }
   }
   
   void deleteRect(ColoredRect rect) {
           // If rect != null, remove it from the Vector and from the screen.
      if (rect != null) {
         rects.removeElement(rect);
         repaint();
      }
   }
   
   public void update(Graphics g) {
           // Redefine update() so it doesn't erase the screen before calling paint().
      paint(g);
   }
   
   public void paint(Graphics g) {
           // Make a new OSC if necessary.  Draw the entire contents
           // of the canvas on the OSC, then copy it to the screen.

      if (OSC == null || widthOfOSC != getSize().width
                                    || heightOfOSC != getSize().height){
                   // Create a new off-screen canvas.
          OSC = null;
          OSC = createImage(getSize().width, getSize().width);
          widthOfOSC = getSize().width;
          heightOfOSC = getSize().height;
      }
      
      /* Fill the OSC with white, then draw all the rects to the OSC. */

      Graphics OSG = OSC.getGraphics();
      OSG.setColor(Color.white);
      OSG.fillRect(0,0,widthOfOSC,heightOfOSC);
      for (int i = 0; i < rects.size(); i++) {
         ColoredRect rect = (ColoredRect)rects.elementAt(i);
         OSG.setColor(rect.color);
         OSG.fillRect(rect.x, rect.y, rect.width, rect.height);
         OSG.setColor(Color.black);
         OSG.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
      }
      OSG.dispose();
      
      /* Copy the OSC to the screen. */

      g.drawImage(OSC,0,0,this);
      
   }  // end paint()
   
   public void mousePressed(MouseEvent evt) { 
            // The user clicked on the canvas.  This can have several effects...
 
      if (dragging)  // If dragging is already in progress, just return.
         return;
         
      if (evt.isMetaDown()) {
               // User right-clicked or command clicked.  Make a new
               // rectangle and add it to the canvas.  Every rectangle is
               // 60 pixels wide and 30 pixels tall.  The point where the
               // user clicked is at the center of the rectangle.  It's
               // color is the selected color in the colorInput palette.
          ColoredRect rect = new ColoredRect();
          rect.x = evt.getX() - 30;
          rect.y = evt.getY() - 15;
          rect.width = 60;
          rect.height = 30;
          rect.color = colorInput.getSelectedColor();
          rects.addElement(rect);
          repaint();
      }
      else if (evt.isShiftDown()) {
              // User shift-clicked.  More the rect that the user
              // clicked (if any) to the front.  Note that findRect()
              // might return null, but bringToFront() accounts for that.
         bringToFront( findRect( evt.getX(), evt.getY() ) );
      }
      else if (evt.isAltDown()) {
              // User alt-clicked or middle-clicked.  Delete the rect
              // that the user clicked.
         deleteRect( findRect( evt.getX(), evt.getY() ) );
      }
      else {
               // This is a simple left-click.  Start dragging the
               // rect that the user clicked (if any).
         dragRect = findRect( evt.getX(), evt.getY() );
         if (dragRect != null) {
            dragging = true;   // Begin a drag operation.
            offsetx = evt.getX() - dragRect.x;
            offsety = evt.getY() - dragRect.y;
         }
         
      }
      
   } // end mousePressed()
   
   public void mouseReleased(MouseEvent evt) { 
           // End the drag operation, if one is in progress.
      if (dragging == false)
         return;
      dragRect = null;
      dragging = false;
   }
   
   public void mouseDragged(MouseEvent evt) { 
           // Continue the drag operation if one is in progress.
           // Move the rect that is being dragged to the current
           // mouse position.  But clamp it so that it can't
           // be more than halfway off the screen.
           
      if (dragging == false)
         return;
         
      dragRect.x = evt.getX() - offsetx;  // Get new postion of rect.
      dragRect.y = evt.getY() - offsety;
      
      /* Clamp (x,y) to a permitted range, as described above. */
      
      if (dragRect.x < - dragRect.width / 2)
         dragRect.x = - dragRect.width / 2;
      else if (dragRect.x + dragRect.width/2 > getSize().width)
         dragRect.x = getSize().width - dragRect.width / 2;
      if (dragRect.y < - dragRect.height / 2)
         dragRect.y = - dragRect.height / 2;
      else if (dragRect.y + dragRect.height/2 > getSize().height)
         dragRect.y = getSize().height - dragRect.height / 2;
       
      /* Redraw the canvas, with the rect in its new position. */

      repaint();

   }  // end mouseDragged()

   public void mouseClicked(MouseEvent evt) { }
   public void mouseEntered(MouseEvent evt) { }
   public void mouseExited(MouseEvent evt) { }
   public void mouseMoved(MouseEvent evt) { }

}  // end nested class RectCanvas
