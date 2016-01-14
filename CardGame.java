package cardgame;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class CardGame {
	
	static HashMap<SuitFace, BufferedImage> cards;
	static JPanel display;
	static CardImagePanel[] cardPanels;
	static int i;
		
	public static void main(String[] args) {
		
		new Thread() {
			@Override
			public void run() {
				loadCardImages();
				build();
				DealCards();	
			}								
		}.start();		
	}
	
	public static void build() {
		
		final JFrame frame = new JFrame("Solitaire Match");
		display = new JPanel();
		display.setLayout(null);
		frame.add(display);
		
		JMenuBar menuBar = new JMenuBar();
		JMenu cardsMenu = new JMenu("Cards");
		JMenuItem deal = new JMenuItem("Deal");		
		
		deal.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				new Thread() {
					@Override
					public void run() {
						DealCards();	
					}								
				}.start();
			}
		});
		
		cardsMenu.add(deal);
		
		JMenu helpMenu = new JMenu("Help");
		JMenuItem howToPlay = new JMenuItem("How To Play");
		
		howToPlay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frame, "Eliminate cards by adding the same face or suit together.\n"
						+ "Suit additions are the result of the ordinal value of Diamonds, Clubs, Hearts, Spades mod 4.\n"
						+ "Face additions are the result of the suit value mod 13.\n"
						+ "The same card added to itself results in the same card.\n"
						+ "You win when 1 card is left.", "Instructions", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		JMenuItem about = new JMenuItem("About");
		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frame, "Written for COMP 585 by Andrew Isaac", "About", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		
		helpMenu.add(howToPlay);
		helpMenu.add(about);
		menuBar.add(cardsMenu);
		menuBar.add(helpMenu);
		frame.setJMenuBar(menuBar);
				
		frame.setMinimumSize(new Dimension(1200,500));		
		frame.setVisible(true);		
	}
	
	public static void loadCardImages() {
		
		cards = new HashMap<SuitFace, BufferedImage>();
		File cardDir = new File("Cards");
		
		File[] cardFiles = cardDir.listFiles();
		
		for (i = 0; i < cardFiles.length; i++) {
			
			try {			
				
				// Tokenize file name
				StringTokenizer st = new StringTokenizer(cardFiles[i].getName(), ".");
				String cardName = st.nextToken();			
				SuitFace sf = new SuitFace(cardName);							
				BufferedImage img = ImageIO.read(cardFiles[i]);
				cards.put(sf, img);
			}
			catch (Exception e) {
				System.out.println(e);
			}
		}			
	}
	
	public static void DealCards() {
					
		try {
			SwingUtilities.invokeAndWait(
				new Runnable() {
					public void run() {						
						display.removeAll();
					}
			});
		} catch (InvocationTargetException | InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
		
		// Generates a permutation of a deck of cards and places them into 8 columns		
		int[] ordered = new int[52];
		int random;
		
		cardPanels = new CardImagePanel[52];		
		SuitFace currentSuitFace;
		
		for (i = 0; i < 52; i++) {
			
			random = (int)(Math.random() * i);			
			ordered[i] = ordered[random];
			ordered[random] = i;		
		}		
				
		for (i = 0; i < 52; i++) {
			
			currentSuitFace = new SuitFace(SuitFace.Suit.values()[ordered[i] / 13], SuitFace.Face.values()[ordered[i] % 13]);
			cardPanels[i] = new CardImagePanel(currentSuitFace, cards.get(currentSuitFace));
			
			if (i >= 8) {
				cardPanels[i].onTopOf = cardPanels[i - 8];
			}
			
			cardPanels[i].setBounds(i % 8 * 140 + 40, i / 8 * 25 + 60, 100, 145);
			
			if (i >= 44 ) {
				cardPanels[i].onTop = true;
			}		
			
			try {
				SwingUtilities.invokeAndWait(
					new Runnable() {
						public void run() {
							
							display.add(cardPanels[i]);
							display.setComponentZOrder(cardPanels[i], 0);						
							display.repaint();
					}
				});
			} catch (InvocationTargetException | InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			
			try {				
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
}

class SuitFace {

	enum Suit { Diamonds, Clubs, Hearts, Spades };
	enum Face { Two, Three, Four, Five, Six, Seven, Eight, Nine, Ten, Jack, Queen, King, Ace };

	Suit suit;
	Face face;
	
	SuitFace (String cardName) {
		setSuit(cardName);
		setFace(cardName);
	}
	
	SuitFace(Suit s, Face f) {
		suit = s;
		face = f;
	}
	
	public Suit getSuit() {
		return suit;
	}
	
	public Face getFace() {
		return face;
	}
	
	void setSuit(String cardName) {
		
		StringTokenizer st = new StringTokenizer(cardName, "_ ");		
		st.nextToken(); // face
		st.nextToken(); // of
		String s = st.nextToken(); // suit
		
		if (s.equalsIgnoreCase("clubs")) suit = Suit.Clubs;
		else if (s.equalsIgnoreCase("diamonds")) suit = Suit.Diamonds;
		else if (s.equalsIgnoreCase("hearts")) suit = Suit.Hearts;
		else if (s.equalsIgnoreCase("spades")) suit = Suit.Spades;
	}
		
	void setFace(String cardName) {
		
		StringTokenizer st = new StringTokenizer(cardName, "_ ");		
		String f = st.nextToken(); // face		
		
		if (f.equalsIgnoreCase("2") || f.equalsIgnoreCase("two")) face = Face.Two;
		else if (f.equalsIgnoreCase("3") || f.equalsIgnoreCase("three")) face = Face.Three;
		else if (f.equalsIgnoreCase("4") || f.equalsIgnoreCase("four")) face = Face.Four;
		else if (f.equalsIgnoreCase("5") || f.equalsIgnoreCase("five")) face = Face.Five;
		else if (f.equalsIgnoreCase("6") || f.equalsIgnoreCase("six")) face = Face.Six;
		else if (f.equalsIgnoreCase("7") || f.equalsIgnoreCase("seven")) face = Face.Seven;
		else if (f.equalsIgnoreCase("8") || f.equalsIgnoreCase("eight")) face = Face.Eight;
		else if (f.equalsIgnoreCase("9") || f.equalsIgnoreCase("nine")) face = Face.Nine;
		else if (f.equalsIgnoreCase("10") || f.equalsIgnoreCase("ten")) face = Face.Ten;
		else if (f.equalsIgnoreCase("jack")) face = Face.Jack;
		else if (f.equalsIgnoreCase("queen")) face = Face.Queen;
		else if (f.equalsIgnoreCase("king")) face = Face.King;
		else if (f.equalsIgnoreCase("ace")) face = Face.Ace;
		}
	
	public String toString() {
		return face + " of " + suit;
	}
	
	@Override
	public boolean equals(Object sf) {	
		
		return suit == ((SuitFace)sf).getSuit() && face == ((SuitFace)sf).getFace();
	}
	
	@Override
	public int hashCode() {
		
		return suit.ordinal() * 14 + face.ordinal();		
	}
	
	public boolean hasSameSuit(SuitFace sf) {		
		return suit == sf.suit;		
	}
	
	public boolean hasSameFace(SuitFace sf) {
		return face == sf.face;
	}
	
	public SuitFace Add(SuitFace sf) {
		
		if (face == sf.face && suit != sf.suit) {
			
			return new SuitFace(Suit.values()[(suit.ordinal() + sf.suit.ordinal()) % 4], face);
		}
		
		else if (suit == sf.suit && face != sf.face) {
			return new SuitFace(suit, Face.values()[((face.ordinal() + sf.face.ordinal() + 2) % 13)] );
		}
		
		else {
			return this;
		}
	}
}


