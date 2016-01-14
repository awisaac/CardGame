package cardgame;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;

public class CardImagePanel extends JPanel implements DragGestureListener, DragSourceListener {
		
	SuitFace suitFace;
	BufferedImage img;
	DragSource ds;
	boolean dragging;
	public boolean onTop;
	public CardImagePanel onTopOf;
	
	public CardImagePanel(SuitFace sf, BufferedImage i) {
		
		suitFace = sf;	
		img = i;
		dragging = false;
		onTop = false;
		
        ds = new DragSource();
        ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
        ds.addDragSourceListener(this);
        
        TransferHandler dnd = new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                if (!support.isDrop()) {
                    return false;
                }
                //only Strings
                if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }

                Transferable transferable = support.getTransferable();
                
                try {                    
                    String testName = (String)transferable.getTransferData(DataFlavor.stringFlavor);
                    SuitFace testSF = new SuitFace(testName);
                    if (testSF.hasSameSuit(suitFace) || testSF.hasSameFace(suitFace)) {
                    	suitFace = suitFace.Add(testSF);
                    	img = CardGame.cards.get(suitFace);                    	
                    	repaint();
                    	return true;
                    }
                    
                    else {
                    	return false;
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }                
            }
        };

        setTransferHandler(dnd);        
	}
	
	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (!dragging) {
        	g.drawImage(img, 0, 0, 100, 145, null); 
        }
    }
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(img.getWidth(), img.getHeight());
	}
	
	@Override
	public String toString() {
		return suitFace.face + " of " + suitFace.suit; 
	}
	
	@Override
	public void dragEnter(DragSourceDragEvent dsde) {}

	@Override
	public void dragOver(DragSourceDragEvent dsde) {}

	@Override
	public void dropActionChanged(DragSourceDragEvent dsde) {}

	@Override
	public void dragExit(DragSourceEvent dse) {}

	@Override
	public void dragDropEnd(DragSourceDropEvent dsde) {
		
		if (dsde.getDropSuccess()) {
			CardGame.display.remove(this);
			
			if (onTopOf != null) {
				onTopOf.onTop = true;
			}
			CardGame.display.repaint();

			if (CardGame.display.getComponents().length == 1) {
				JOptionPane.showMessageDialog(CardGame.display, "You win!");
			}			
		}
		else {
			dragging = false;
			CardGame.display.add(this);
			CardGame.display.setComponentZOrder(this, 0);
			CardGame.display.repaint();
		}
	}

	@Override
	public void dragGestureRecognized(DragGestureEvent dge) {
		
        Transferable transferable = new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{DataFlavor.stringFlavor};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                if (!isDataFlavorSupported(flavor)) {
                    return false;
                }
                return true;
            }

            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                return suitFace.toString();
            }           
        };
        
        if (onTop) {        
	        Image dragImg = img.getScaledInstance(100, 145, java.awt.Image.SCALE_DEFAULT);
	        dragging = true;
	        CardGame.display.remove(this);
	        CardGame.display.repaint();        
	        dge.startDrag(null, dragImg, dge.getDragOrigin(), transferable, this);
        }
	}
}
