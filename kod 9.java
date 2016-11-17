
/*
   This applet is a simple card game.  The user sees a card and
   tries to predict whether the next card will be higher or 
   lower.  Aces are the lowest-valued cards.  If the user makes
   three correct predictions, the user wins.  If not, the
   user loses.
   
   This version of the game uses images of the cards, stored
   in a file smallcards.gif.  Each card is 40 pixels wide and
   60 pixels high.  Other than that, the applet is the same as
   the original HighLowGUI.
   
   The programming of this applet assumes that the applet is
   set up to be about 266 pixels wide and about 150 pixels high.
   That width is just big enough to show 4 cards, with spacing and
   borders.  The height is probably a little bigger than necessary,
   to allow for variations in the size of buttons from one platform
   to another.
   
   This file defines two classes, the applet class HighLowGUI2
   and a class HighLowCanvas2 which is used in the applet.
   (This is slightly bad style, but is OK since the HighLowCanvas
   class is not public and is not used outside this file.)
*/

import java.awt.*;
import java.awt.event.*;
import java.applet.*;

public class HighLowGUI2 extends Applet {

   public void init() {
   
         // The init() method lays out the applet using a BorderLayout.
         // A HighLowCanvas occupies the CENTER position of the layout.
         // On the bottom is a panel that holds three buttons.  The
         // HighLowCanvas object listens for ActionEvents from the buttons
         // and does all the real work of the program.
         
      Image cardpics;  // This will be an image that contains pictures
                       // of all the playing cards.  It is loaded here and
                       // passed to the constructor for the canvas.  It's
                       // done this way because only applets have a convenient
                       // method for loading images.
                       
      cardpics = getImage(getCodeBase(), "smallcards.gif");

      setBackground( new Color(130,50,40) );
      setLayout( new BorderLayout(3,3) );

      
      HighLowCanvas2 board = new HighLowCanvas2(cardpics);
      add(board, BorderLayout.CENTER);
      
      Panel buttonPanel = new Panel();
      buttonPanel.setBackground( new Color(220,200,180) );
      add(buttonPanel, BorderLayout.SOUTH);
      
      Button higher = new Button( "Higher" );
      higher.addActionListener(board);
      higher.setBackground(Color.lightGray);
      buttonPanel.add(higher);
      
      Button lower = new Button( "Lower" );
      lower.addActionListener(board);
      lower.setBackground(Color.lightGray);
      buttonPanel.add(lower);
      
      Button newGame = new Button( "New Game" );
      newGame.addActionListener(board);
      newGame.setBackground(Color.lightGray);
      buttonPanel.add(newGame);
      
   }  // end init()
   
   public Insets getInsets() {
         // Specify how much space to leave between the edges of
         // the applet and the components it contains.  The background
         // color shows through in this border.
      return new Insets(3,3,3,3);
   }

} // end class HighLowGUI


class HighLowCanvas2 extends Canvas implements ActionListener {

      // A class that displays the card game and does all the work
      // of keeping track of the state and responding to user events.
      
   Image cardImages;  // An image that contains the cards.  Each card is 40-by-60
                      //    pixels.  The cards are arranged in 4 rows and 13 columns,
                      //    according to suit and value.  The order of the suits
                      //    is clubs, hearts, spades, diamonds.  The order of the
                      //    values puts ace at the beginning.

   Deck deck;       // A deck of cards to be used in the game.
   Hand hand;       // The cards that have been dealt.
   String message;  // A message drawn on the canvas, which changes
                    //    to reflect the state of the game.
                    
   boolean gameInProgress;  // Set to true when a game begins and to false
                            //   when the game ends.
   
   Font bigFont;      // Font that will be used to display the message.
   Font smallFont;    // Font that will be used to draw the cards.
   

   HighLowCanvas2(Image cardpics) {
         // Constructor.  Creates fonts and starts the first game.
         // The parameter is the Image containing pictures af all the cards.
      cardImages = cardpics;
      setBackground( new Color(0,120,0) );
      setForeground( Color.green );
      smallFont = new Font("SansSerif", Font.PLAIN, 12);
      bigFont = new Font("Serif", Font.BOLD, 14);
      doNewGame();
   }
   

   public void actionPerformed(ActionEvent evt) {
          // Respond when the user clicks on a button by calling
          // the appropriate procedure.  Note that the canvas is
          // registered as a listener in the HighLowGUI class.
      String command = evt.getActionCommand();
      if (command.equals("Higher"))
         doHigher();
      else if (command.equals("Lower"))
         doLower();
      else if (command.equals("New Game"))
         doNewGame();
   }
   

   void doHigher() {
            // Called by actionPerformmed() when user clicks "Higher" button.
            // Check the user's prediction.  Game ends if user guessed
            // wrong or if the user has made three correct predictions.
      if (gameInProgress == false) {
            // If the game has ended, it was an error to click "Higher",
            // So set up an error message and abort processing.
         message = "Use \"New Game\" to begin a game!";
         repaint();
         return;
      }
      hand.addCard( deck.dealCard() );     // Deal a card to the hand.
      int cardCt = hand.getCardCount();
      Card thisCard = hand.getCard( cardCt - 1 );  // Card just dealt.
      Card prevCard = hand.getCard( cardCt - 2 );  // The previous card.
      if ( thisCard.getValue() < prevCard.getValue() ) {
         gameInProgress = false;
         message = "Too bad! You lose.";
      }
      else if ( thisCard.getValue() == prevCard.getValue() ) {
         gameInProgress = false;
         message = "Too bad! You lose on ties.";
      }
      else if ( cardCt == 4) {
         gameInProgress = false;
         message = "You win!";
      }
      else {
         message = "Right!  Try for " + cardCt + ".";
      }
      repaint();
   }
   

   void doLower() {
            // Called by actionPerformmed() when user clicks "Lower" button.
            // Check the user's prediction.  Game ends if user guessed
            // wrong or if the user has made three correct predictions.
      if (gameInProgress == false) {
            // If the game has ended, it was an error to click "Lower",
            // So set up an error message and abort processing.
         message = "Use \"New Game\" to begin a game!";
         repaint();
         return;
      }
      hand.addCard( deck.dealCard() );     // Deal a card to the hand.
      int cardCt = hand.getCardCount();
      Card thisCard = hand.getCard( cardCt - 1 );  // Card just dealt.
      Card prevCard = hand.getCard( cardCt - 2 );  // The previous card.
      if ( thisCard.getValue() > prevCard.getValue() ) {
         gameInProgress = false;
         message = "Too bad! You lose.";
      }
      else if ( thisCard.getValue() == prevCard.getValue() ) {
         gameInProgress = false;
         message = "Too bad! You lose on ties.";
      }
      else if ( cardCt == 4) {
         gameInProgress = false;
         message = "You win!";
      }
      else {
         message = "Right!  Try for " + cardCt + ".";
      }
      repaint();
   }
   

   void doNewGame() {
          // Called by the constructor, and called by actionPerformed() if
          // the use clicks the "New Game" button.  Start a new game.
      if (gameInProgress) {
              // If the current game is not over, it is an error to try
              // to start a new game.
         message = "Finish this game first!";
         repaint();
         return;
      }
      deck = new Deck();   // Create the deck and hand to use for this game.
      hand = new Hand();
      deck.shuffle();
      hand.addCard( deck.dealCard() );  // Deal the first card into the hand.
      message = "Is the next card higher or lower?";
      gameInProgress = true;
      repaint();
   }

   
   public void paint(Graphics g) {
         // The paint method shows the message at the bottom of the
         // canvas, and it draws all of the dealt cards spread out
         // across the canvas.  If the game is in progress, an
         // extra card is dealt representing the card to be dealt next.
      g.setFont(bigFont);
      g.drawString(message, 10, getSize().height - 10);
      g.setFont(smallFont);
      int cardCt = hand.getCardCount();
      for (int i = 0; i < cardCt; i++)
         drawCard(g, hand.getCard(i), 20 + i * 60, 10);
      if (gameInProgress)
         drawCard(g, null, 20 + cardCt * 60, 10);
   }
   

   void drawCard(Graphics g, Card card, int x, int y) {
           // Draws a card as a 40 by 60 rectangle with
           // upper left corner at (x,y).  The card is drawn
           // in the graphics context g.  If card is null, then
           // a face-down card is drawn. The cards are taken
           // from an image file, smallcards.gif.
      if (card == null) {  
             // Draw a face-down card
         g.setColor(Color.blue);
         g.fillRect(x,y,40,60);
         g.setColor(Color.white);
         g.drawRect(x+3,y+3,33,53);
         g.drawRect(x+4,y+4,31,51);
      }
      else {
         int row = 0;
         switch (card.getSuit()) {
            case Card.CLUBS:    row = 0;  break;
            case Card.HEARTS:   row = 1;  break;
            case Card.SPADES:   row = 2;  break;
            case Card.DIAMONDS: row = 3;  break;
         }
         int sx, sy;  // coords of upper left corner in the source image.
         sx = 40*(card.getValue() - 1);
         sy = 60*row;
         g.drawImage(cardImages, x, y, x+40, y+60,
                                 sx, sy, sx+40, sy+60, this);
         System.out.println(card.toString());
      }
   }


} // end class HighLowCanvas
