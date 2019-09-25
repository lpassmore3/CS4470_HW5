// This class is for the section of the right side of
// the Courier application that represents a page that
// can be drawn on.
//
// Author: Luke Austin Passmore, lpassmore3

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.*;

public class MyPage extends JComponent implements KeyListener, MouseListener, MouseMotionListener {

    private static final long serialVersionUID = 1L;

    // Indicates what draw tool is selected
    // "stroke" = Free-Form Ink
    // "rect" = Rectangle
    // "oval" = Oval
    // "text" = Text
    private String drawTool = "";
    private String inkColor = "black";

    // The list of things to draw
    ArrayList<Ink> displayList = new ArrayList<Ink>();

    // The Recognizer object to recognize gestures made with right-button clicks
    private Recognizer recognizer = new Recognizer();

    // The list of ink objects selected
    ArrayList<Ink> selectedList = new ArrayList<Ink>();
    ArrayList<RectangleInk> selectedHighlights = new ArrayList<RectangleInk>();
    Boolean selectionMode = false;
    int selectionMinX = 0;
    int selectionMinY = 0;
    int selectionMaxX = 0;
    int selectionMaxY = 0;
    int selectionOldX = 0;
    int selectionOldY = 0;
    int selectionCurrX = 0;
    int selectionCurrY = 0;
    RectangleInk selectionBounds = new RectangleInk(new Point(0, 0), new Point(0, 0));

    public String pageGesture = "";
    
    // References to connected pages
    //public ArrayList<MyPage> connectedPages = new ArrayList<MyPage>();
    public MyPage[] connectedPages = new MyPage[2];

    //public ArrayList<BufferedImage> offscreenImages = new ArrayList<BufferedImage>();
    public BufferedImage[] offscreenImages = new BufferedImage[3];

    // Variables used for the turning animations
    private boolean isAnimatingPageForward = false;
    private boolean isAnimatingPageBackward = false;
    private boolean isDynamicPageForward = false;
    private boolean isDynamicPageBackward = false;
    private boolean isFinishingDynamicPageBackward = false;
    private boolean isFinishingDynamicPageForward = false;
    private int frameCounter = 1;

    private int xPos = 0;

    public MyPage() {
        //super();
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public ArrayList<Ink> getDisplayList() {
        return displayList;
    }

    public String getPageGesture() {
        return this.pageGesture;
    }

    public void clearPageGesture() {
        this.pageGesture = "";
    }

    public MyPage getBackwardPage() {
        //return this.connectedPages.get(0);
        return this.connectedPages[0];
    }

    public void setBackwardPage(MyPage page) {
        //this.connectedPages.set(0, page);
        this.connectedPages[0] = page;
    }

    public MyPage getForwardPage() {
        //return this.connectedPages.get(1);
        return this.connectedPages[1];
    }

    public void setForwardPage(MyPage page) {
        //this.connectedPages.set(1, page);
        this.connectedPages[1] = page;
    }

    // The space between lines of text in sticky notes
    int leading = 5;
    // The font used in the sticky note text
    //Font font = new Font("Serif", Font.PLAIN, 24);
    // Indicates whether text in a sticky note is being created of not
    boolean editingText = false;


    @Override
    public void mousePressed(MouseEvent e) {
        int currentX = e.getX();
        int currentY = e.getY();

        xPos = currentX;

        if (SwingUtilities.isRightMouseButton(e)) {
            if (currentX > getWidth() * 0.8) {
                offscreenImages[1] = makeOffscreenImage(this);
                offscreenImages[2] = makeOffscreenImage(connectedPages[1]);
                isDynamicPageForward = true;
            } else if (currentX < getWidth() * 0.2) {
                offscreenImages[1] = makeOffscreenImage(this);
                offscreenImages[0] = makeOffscreenImage(connectedPages[0]);
                isDynamicPageBackward = true;
            } else {
                Stroke stroke = new Stroke(new Point(currentX, currentY));
                stroke.setColor("magenta");
                displayList.add(stroke);
                selectionMode = false;
                selectedList.clear();
                for (RectangleInk r : selectedHighlights) {
                    displayList.remove(r);
                }
                displayList.remove(selectionBounds);
            }
            repaint();
        } else {
            if (selectionMode) {
                Boolean selectionClicked = false;
                if (currentX >= selectionMinX && currentX <= selectionMaxX && currentY >= selectionMinY && currentY <= selectionMaxY) {
                    selectionClicked = true;
                    selectionOldX = currentX;
                    selectionCurrX = currentX;
                    selectionOldY = currentY;
                    selectionCurrY = currentY;
                }
                if (selectionClicked == false) {
                    selectionMode = false;
                    selectedList.clear();
                    for (RectangleInk r : selectedHighlights) {
                        displayList.remove(r);
                    }
                    displayList.remove(selectionBounds);
                    repaint();
                }
            } else {
                if (drawTool == "stroke") {
                    Stroke stroke = new Stroke(new Point(currentX, currentY));
                    stroke.setColor(inkColor);
                    displayList.add(stroke);
                } else if (drawTool == "rect") {
                    Point p = new Point(currentX, currentY);
                    RectangleInk rect = new RectangleInk(p, p);
                    rect.setColor(inkColor);
                    displayList.add(rect);
                } else if (drawTool == "oval") {
                    Point p = new Point(currentX, currentY);
                    OvalInk oval = new OvalInk(p, p);
                    oval.setColor(inkColor);
                    displayList.add(oval);
                } else if (drawTool == "text") {
                    Point p = new Point(currentX, currentY);
                    TextInk text = new TextInk(p, p);
                    this.setFocusable(true);
                    this.requestFocusInWindow();
                    displayList.add(text);
                }
            } 
        }       
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int currentX = e.getX();
        int currentY = e.getY();
        xPos = currentX;
        Point currPoint = new Point(currentX, currentY);
        int displayListSize = displayList.size();

        if (SwingUtilities.isRightMouseButton(e)) {
            if (isDynamicPageBackward == false && isDynamicPageForward == false) {
                Stroke stroke = (Stroke) displayList.get(displayListSize - 1);
                stroke.addToPointList(currPoint);
            }
        } else {
            if (selectionMode) {
                selectionOldX = selectionCurrX;
                selectionOldY = selectionCurrY;
                selectionCurrX = currentX;
                selectionCurrY = currentY;
                int dx = selectionCurrX - selectionOldX;
                int dy = selectionCurrY - selectionOldY;
                for (Ink s : selectedList) {
                    Ink selected = displayList.get(displayList.indexOf(s));
                    //RectangleInk selectedBox = (RectangleInk) displayList.get(displayList.indexOf(s) - 1);
                    String type = selected.getType();
                    if (type == "stroke") {
                        Stroke selectedStroke = (Stroke) selected;
                        selectedStroke.translate(dx, dy);
                    } else if (type == "rectangle") {
                        RectangleInk selectedRect = (RectangleInk) selected;
                        selectedRect.translate(dx, dy);
                    } else if (type == "oval") {
                        OvalInk selectedOval = (OvalInk) selected;
                        selectedOval.translate(dx, dy);
                    } else if (type == "text") {
                        TextInk selectedText = (TextInk) selected;
                        selectedText.translate(dx, dy);
                    }
                    //selectedBox.translate(dx, dy);
                }
                for (RectangleInk r : selectedHighlights) {
                    ((RectangleInk) displayList.get(displayList.indexOf(r))).translate(dx, dy);
                }
                RectangleInk selectionBoundsBox = (RectangleInk) displayList.get(displayList.indexOf(selectionBounds));
                selectionBoundsBox.translate(dx, dy);
            } else {
                if (drawTool == "stroke") {
                    Stroke stroke = (Stroke) displayList.get(displayListSize - 1);
                    stroke.addToPointList(currPoint);
                } else if (drawTool == "rect") {
                    RectangleInk oldRect = (RectangleInk) displayList.get(displayListSize - 1);
                    Point startPoint = oldRect.getMouseStart();
                    RectangleInk rect = new RectangleInk(startPoint, currPoint);
                    rect.setColor(inkColor);
                    displayList.remove(displayListSize - 1);
                    displayList.add(rect);
                } else if (drawTool == "oval") {
                    OvalInk oldOval = (OvalInk) displayList.get(displayListSize - 1);
                    Point startPoint = oldOval.getMouseStart();
                    OvalInk oval = new OvalInk(startPoint, currPoint);
                    oval.setColor(inkColor);
                    displayList.remove(displayListSize - 1);
                    displayList.add(oval);
                } else if (drawTool == "text") {
                    TextInk oldText = (TextInk) displayList.get(displayListSize - 1);
                    Point startPoint = oldText.getMouseStart();
                    TextInk text = new TextInk(startPoint, currPoint);
                    displayList.remove(displayListSize - 1);
                    displayList.add(text);
                }
            }
        }        
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int currentX = e.getX();
        int currentY = e.getY();
        xPos = currentX;
        Point currPoint = new Point(currentX, currentY);
        int displayListSize = displayList.size();

        if (SwingUtilities.isRightMouseButton(e)) {
            if (isDynamicPageBackward == true) {
                if (xPos > getWidth() / 2) {
                    finishDynamicBackwardPageTurn();
                    pageGesture = "FINISHED_PAGE_BACK";
                }
                isDynamicPageBackward = false;
            } else if (isDynamicPageForward == true) {
                if (xPos < getWidth() / 2) {
                    finishDynamicForwardPageTurn();
                    pageGesture = "FINISHED_PAGE_FORWARD";
                }
                isDynamicPageForward = false;
            } else {
                Stroke gestureStroke = (Stroke) displayList.get(displayListSize - 1);
                String gesture = recognizer.recognize(gestureStroke);
                displayList.remove(displayListSize - 1);
                if (gesture == "RIGHT_ANGLE") {
                    System.out.println(gesture);
                    pageGesture = gesture;
                } else if (gesture == "LEFT_ANGLE") {
                    System.out.println(gesture);
                    pageGesture = gesture;
                } else if (gesture == "PIGTAIL") {
                    System.out.println(gesture);
                    pageGesture = gesture;
                    deleteInk(gestureStroke);
                } else if (gesture == "LOOP") {
                    System.out.println(gesture);
                    pageGesture = gesture;
                    selectInk(gestureStroke);
                } else if (gesture == "NULL_GESTURE") {
                    System.out.println(gesture);
                    pageGesture = gesture;
                }
                System.out.println("");
            }
        } else {
            if (selectionMode) {
                selectionMode = false;
                selectedList.clear();
                for (RectangleInk r : selectedHighlights) {
                    displayList.remove(r);
                }
                displayList.remove(selectionBounds);
                repaint();
            } else {
                if (drawTool == "stroke") {
                    Stroke stroke = (Stroke) displayList.get(displayListSize - 1);
                    stroke.addToPointList(currPoint);
                } else if (drawTool == "rect") {
                    RectangleInk oldRect = (RectangleInk) displayList.get(displayListSize - 1);
                    Point startPoint = oldRect.getMouseStart();
                    RectangleInk rect = new RectangleInk(startPoint, currPoint);
                    rect.setColor(inkColor);
                    displayList.remove(displayListSize - 1);
                    displayList.add(rect);
                } else if (drawTool == "oval") {
                    OvalInk oldOval = (OvalInk) displayList.get(displayListSize - 1);
                    Point startPoint = oldOval.getMouseStart();
                    OvalInk oval = new OvalInk(startPoint, currPoint);
                    oval.setColor(inkColor);
                    displayList.remove(displayListSize - 1);
                    displayList.add(oval);
                } else if (drawTool == "text") {
                    TextInk oldText = (TextInk) displayList.get(displayListSize - 1);
                    Point startPoint = oldText.getMouseStart();
                    TextInk text = new TextInk(startPoint, currPoint);
                    displayList.remove(displayListSize - 1);
                    displayList.add(text);
                    editingText = true;
                }
            }
        }       
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        return;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        return;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        editingText = false;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        return;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (editingText && e.getKeyCode() == KeyEvent.VK_ENTER) {
            //System.out.println("Enter pressed");
            editingText = false;
            repaint();
        //}
        } else if (editingText && e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            TextInk textInk = (TextInk) displayList.get(displayList.size() - 1);
            if (textInk.getText().length() > 0) {
                textInk.removeChar();
            }
            repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        return;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        //System.out.println(e.getKeyChar());
        if (editingText && e.getExtendedKeyCode() != KeyEvent.VK_BACK_SPACE) {
            char c = e.getKeyChar();
            //System.out.println(e.getKeyChar());
            TextInk textInk = (TextInk) displayList.get(displayList.size() - 1);
            if (e.getKeyChar() != KeyEvent.VK_BACK_SPACE) {
                textInk.addToText(c);
            }
            // ArrayList<String> lines = textInk.getLines();
            // if (lines.size() == 0) {
            //     String line = "" + c;
            //     textInk.addLine(line);
            // } else if ()
            repaint();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // enable anti-aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (isAnimatingPageForward) {
            isAnimatingPageForward = false;

            // g2.setColor(Color.WHITE);
            // g2.fillRect(0, 0, frameCounter * 30, 30);

            // BufferedImage currPageImage = makeOffscreenImage(this);
            // BufferedImage nextPageImage = makeOffscreenImage(forwardPage);
            BufferedImage currPageImage = offscreenImages[1];
            BufferedImage nextPageImage = offscreenImages[2];

            int pageWidth = this.getWidth();
            int pageHeight = this.getHeight();
            int turnRectWidth = pageWidth / 25;
            if (frameCounter == 1) {
                turnRectWidth = turnRectWidth / 2;
            }
            int currPageWidth = turnRectWidth * (frameCounter - 1);
            int nextPageWidth = pageWidth - (turnRectWidth * frameCounter);
            int nextPageX = turnRectWidth * frameCounter;

            if (currPageWidth > 0) {
                BufferedImage currPagePortion = currPageImage.getSubimage(0, 0, currPageWidth, pageHeight);
                g2.drawImage(currPagePortion, 0, 0, this);
            }
            
            g2.setColor(Color.white);
            g2.fillRect(currPageWidth, 0, turnRectWidth, pageHeight);

            if (nextPageWidth > 0) {
                BufferedImage nextPagePortion = nextPageImage.getSubimage(nextPageX, 0, nextPageWidth, pageHeight);
                g2.drawImage(nextPagePortion, nextPageX, 0, this);
            }

        } else if (isAnimatingPageBackward) {

            isAnimatingPageBackward = false;

            // g2.setColor(Color.WHITE);
            // g2.fillRect(0, 0, frameCounter * 30, 30);

            // BufferedImage currPageImage = makeOffscreenImage(this);
            // BufferedImage nextPageImage = makeOffscreenImage(forwardPage);
            BufferedImage currPageImage = offscreenImages[1];
            BufferedImage prevPageImage = offscreenImages[0];

            int pageWidth = this.getWidth();
            int pageHeight = this.getHeight();
            int turnRectWidth = pageWidth / 25;
            if (frameCounter == 1) {
                turnRectWidth = turnRectWidth / 2;
            }
            int prevPageWidth = pageWidth - (turnRectWidth * frameCounter);
            int currPageWidth = turnRectWidth * (frameCounter - 1);
            int currPageX = pageWidth - currPageWidth;

            if (currPageWidth > 0) {
                BufferedImage currPagePortion = currPageImage.getSubimage(currPageX, 0, currPageWidth, pageHeight);
                g2.drawImage(currPagePortion, currPageX, 0, this);
            }
            
            g2.setColor(Color.white);
            g2.fillRect(pageWidth - (turnRectWidth * frameCounter), 0, turnRectWidth, pageHeight);

            if (prevPageWidth > 0) {
                BufferedImage prevPagePortion = prevPageImage.getSubimage(0, 0, prevPageWidth, pageHeight);
                g2.drawImage(prevPagePortion, 0, 0, this);
            }

        } else if (isDynamicPageBackward) { 
            System.out.println("Dynamic Page Backward");
            BufferedImage currPageImage = offscreenImages[1];
            BufferedImage prevPageImage = offscreenImages[0];

            int pageWidth = this.getWidth();
            int pageHeight = this.getHeight();
            //int turnRectWidth = pageWidth / 5;
            int turnRectWidth = xPos / 2;
            int prevPageWidth = xPos / 2;
            int currPageWidth = pageWidth - xPos;
            int currPageX = xPos;

            if (currPageWidth > 0) {
                BufferedImage currPagePortion = currPageImage.getSubimage(currPageX, 0, currPageWidth, pageHeight);
                g2.drawImage(currPagePortion, currPageX, 0, this);
            }
            
            g2.setColor(Color.white);
            g2.fillRect(xPos - turnRectWidth, 0, turnRectWidth, pageHeight);

            if (prevPageWidth > 0) {
                BufferedImage prevPagePortion = prevPageImage.getSubimage(0, 0, prevPageWidth, pageHeight);
                g2.drawImage(prevPagePortion, 0, 0, this);
            }

        } else if (isDynamicPageForward) {
            System.out.println("Dynamic Page Forward");
            BufferedImage currPageImage = offscreenImages[1];
            BufferedImage nextPageImage = offscreenImages[2];

            int pageWidth = this.getWidth();
            int pageHeight = this.getHeight();
            //int turnRectWidth = pageWidth / 5;
            int turnRectWidth = (pageWidth - xPos) / 2;
            int nextPageWidth = pageWidth - xPos - turnRectWidth;
            int nextPageX = xPos + turnRectWidth;
            int currPageWidth = xPos;
            
            if (currPageWidth > 0) {
                BufferedImage currPagePortion = currPageImage.getSubimage(0, 0, currPageWidth, pageHeight);
                g2.drawImage(currPagePortion, 0, 0, this);
            }
            
            g2.setColor(Color.white);
            g2.fillRect(xPos, 0, turnRectWidth, pageHeight);

            if (nextPageWidth > 0) {
                BufferedImage nextPagePortion = nextPageImage.getSubimage(nextPageX, 0, nextPageWidth, pageHeight);
                g2.drawImage(nextPagePortion, nextPageX, 0, this);
            }

        } else if (isFinishingDynamicPageBackward) {
            System.out.println("Finishing Dynamic Page Backward");
            BufferedImage currPageImage = offscreenImages[1];
            BufferedImage prevPageImage = offscreenImages[0];

            int mouseX = xPos / 2;
            int dMouseX = (getWidth() - mouseX) / 24;
            mouseX = mouseX + (dMouseX * frameCounter);

            int pageWidth = this.getWidth();
            int pageHeight = this.getHeight();
            //int turnRectWidth = pageWidth / 5;
            int turnRectWidth = mouseX / 2;
            int prevPageWidth = mouseX;
            int currPageWidth = pageWidth - mouseX - turnRectWidth;
            int currPageX = mouseX + turnRectWidth;

            if (currPageWidth > 0) {
                BufferedImage currPagePortion = currPageImage.getSubimage(currPageX, 0, currPageWidth, pageHeight);
                g2.drawImage(currPagePortion, currPageX, 0, this);
            }
            
            g2.setColor(Color.white);
            g2.fillRect(mouseX, 0, turnRectWidth, pageHeight);

            if (prevPageWidth > 0 && prevPageWidth <= pageWidth) {
                BufferedImage prevPagePortion = prevPageImage.getSubimage(0, 0, prevPageWidth, pageHeight);
                g2.drawImage(prevPagePortion, 0, 0, this);
            }

        } else if (isFinishingDynamicPageForward) {
            System.out.println("Finishing Dynamic Page Forward");
            BufferedImage currPageImage = offscreenImages[2];
            BufferedImage nextPageImage = offscreenImages[1];

            int mouseX = xPos;
            int rectEdgeX = getWidth() - (mouseX / 2);
            int dMouseX = rectEdgeX / 24;
            mouseX = mouseX - (dMouseX * frameCounter);

            int pageWidth = this.getWidth();
            int pageHeight = this.getHeight();
            //int turnRectWidth = pageWidth / 5;
            int turnRectWidth = xPos / 2;
            int nextPageWidth = 0;
            if (mouseX > 0) {
                nextPageWidth = mouseX;
            } else if (mouseX < 0) {
                mouseX = 0;
                turnRectWidth -= dMouseX * frameCounter;
            }
            int currPageWidth = pageWidth - mouseX - turnRectWidth;
            int currPageX = mouseX + turnRectWidth;
            if (currPageX < 0) {
                currPageX = 0;
            }
            if (currPageWidth > pageWidth) {
                currPageWidth = pageWidth;
            }

            if (currPageWidth > 0) {
                BufferedImage currPagePortion = currPageImage.getSubimage(currPageX, 0, currPageWidth, pageHeight);
                g2.drawImage(currPagePortion, currPageX, 0, this);
            }
            
            g2.setColor(Color.white);
            g2.fillRect(mouseX, 0, turnRectWidth, pageHeight);

            if (nextPageWidth > 0 && nextPageWidth <= pageWidth) {
                BufferedImage nextPagePortion = nextPageImage.getSubimage(0, 0, nextPageWidth, pageHeight);
                g2.drawImage(nextPagePortion, 0, 0, this);
            }

        } else {
            g2.setColor(Color.white);
            g2.fillRect(0, 0, this.getWidth(), this.getHeight());
            drawPaperLines(g2);
            g2.setStroke(new BasicStroke(5));

            //g2.setColor(Color.BLACK);
            // if (inkColor == "black") {
            //     g2.setColor(Color.BLACK);
            // } else if (inkColor == "red") {
            //     g2.setColor(Color.RED);
            // } else if (inkColor == "green") {
            //     g2.setColor(Color.GREEN);
            // } else if (inkColor == "blue") {
            //     g2.setColor(Color.BLUE);
            // }

            // Iterate through the displayList and draw each object
            for (Ink o : displayList) {
                String type = o.getType();
                
                if (type == "stroke") {
                    Stroke stroke = (Stroke) o;
                    ArrayList<Point> pointList = stroke.getPointList();
                    for (int n = 0; n < pointList.size() - 1; n++) {
                        Point startPoint = pointList.get(n);
                        Point endPoint = pointList.get(n + 1);
                        //g2.setColor(Color.BLACK);
                        String strokeColor = stroke.getColor();
                        if (strokeColor == "black") {
                            g2.setColor(Color.BLACK);
                        } else if (strokeColor == "red") {
                            g2.setColor(Color.RED);
                        } else if (strokeColor == "green") {
                            g2.setColor(Color.GREEN);
                        } else if (strokeColor == "blue") {
                            g2.setColor(Color.BLUE);
                        } else if (strokeColor == "magenta") {
                            g2.setColor(Color.MAGENTA);
                        }
                        g2.drawLine(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
                    }
                } else if (type == "rectangle") {
                    RectangleInk rect = (RectangleInk) o;
                    Point startPoint = rect.getMouseStart();
                    Point endPoint = rect.getMouseEnd();
                    int width = endPoint.getX() - startPoint.getX();
                    int height = endPoint.getY() - startPoint.getY();
                    //g2.setColor(Color.BLACK);
                    String rectColor = rect.getColor();
                    if (rectColor == "black") {
                        g2.setColor(Color.BLACK);
                    } else if (rectColor == "red") {
                        g2.setColor(Color.RED);
                    } else if (rectColor == "green") {
                        g2.setColor(Color.GREEN);
                    } else if (rectColor == "blue") {
                        g2.setColor(Color.BLUE);
                    } else if (rectColor == "pink") {
                        g2.setColor(Color.PINK);
                    } else if (rectColor == "cyan") {
                        g2.setColor(Color.CYAN);
                    }
                    g2.drawRect(startPoint.getX(), startPoint.getY(), width, height);
                } else if (type == "oval") {
                    OvalInk oval = (OvalInk) o;
                    Point startPoint = oval.getMouseStart();
                    Point endPoint = oval.getMouseEnd();
                    int width = endPoint.getX() - startPoint.getX();
                    int height = endPoint.getY() - startPoint.getY();
                    //g2.setColor(Color.BLACK);
                    String ovalColor = oval.getColor();
                    if (ovalColor == "black") {
                        g2.setColor(Color.BLACK);
                    } else if (ovalColor == "red") {
                        g2.setColor(Color.RED);
                    } else if (ovalColor == "green") {
                        g2.setColor(Color.GREEN);
                    } else if (ovalColor == "blue") {
                        g2.setColor(Color.BLUE);
                    }
                    g2.drawOval(startPoint.getX(), startPoint.getY(), width, height);
                } else if (type == "text") {
                    TextInk textInk = (TextInk) o;
                    Point startPoint = textInk.getMouseStart();
                    Point endPoint = textInk.getMouseEnd();
                    int width = endPoint.getX() - startPoint.getX();
                    int height = endPoint.getY() - startPoint.getY();
                    g2.setColor(Color.GRAY);
                    g2.fillRect(startPoint.getX() - (width / 10), startPoint.getY() + (height / 10), width - 1, height - 1);
                    g2.setColor(Color.BLACK);
                    g2.drawRect(startPoint.getX(), startPoint.getY(), width, height);
                    g2.setColor(Color.YELLOW);
                    g2.fillRect(startPoint.getX() + 1, startPoint.getY() + 1, width - 1, height - 1);

                    g2.setColor(Color.BLACK);
                    FontMetrics fm = g2.getFontMetrics();
                    //System.out.println(fm.getAscent());
                    //g2.drawString(textInk.getText(), startPoint.getX() + leading, startPoint.getY() + fm.getAscent() + leading);
                    //g2.drawString(textInk.getText(), startPoint.getX() + leading, startPoint.getY() + fm.getAscent() + leading);
                    String text = textInk.getText();
                    ArrayList<String> lines = new ArrayList<String>();
                    String textline = "";
                    lines.add(textline);
                    int y = startPoint.getY() + fm.getAscent() + leading;
                    int dist = fm.getDescent() + leading + fm.getAscent();
                    int lineWidth = 0;
                    for (int i = 0; i < text.length(); i++) {
                        char c = text.charAt(i);
                        if (lineWidth + fm.charWidth(c) <= width - (leading * 2)) {
                            textline += c;
                            lines.remove(lines.size() - 1);
                            lines.add(textline);
                            lineWidth += fm.charWidth(c);
                        } else {
                            textline = "" + c;
                            lines.add(textline);
                            lineWidth = fm.charWidth(c);
                        }
                    }
                    //System.out.println("Height: " + height);
                    for (int n = 0; n < lines.size(); n++) {
                        g2.setColor(Color.BLACK);
                        int newY = y + (dist * n);
                        //System.out.println("New Y: " + newY);
                        if (newY + fm.getDescent() + leading <= startPoint.getY() + height) {
                            g2.drawString(lines.get(n), startPoint.getX() + leading, newY);
                        } else {
                            editingText = false;
                            editingText = true;
                        }
                    }
                    //System.out.println("Text: " + textInk.getText() + ", " + Boolean.toString(editingText));
                } 
            }
        }
    }

    // Creates the notebook paper backgroud
    private void drawPaperLines(Graphics2D g2) {

        int height = this.getHeight();
        int width = this.getWidth();

        int blueLineLead = 15;

        // draw the blue horizontal lines
        for (int i = height / 10; i <= height; i += blueLineLead) {
            g2.setColor(Color.blue);
            g2.drawLine(0, i, width, i);
        }

        // draw the red vertical line
        g2.setColor(Color.red);
        g2.drawLine(width / 10, 0, width / 10, height);
    }

    // Sets what tool is selected
    public void setTool(String tool) {
        drawTool = tool;
    }

    // Sets what ink color is selected
    public void setInkColor(String color) {
        inkColor = color;
    }

    // Creates a list of objects on the page that are effected by a
    // gesture stroke. Must be within the gesture stroke's bounds.
    private ArrayList<Ink> checkGestureBounds(Stroke gestureStroke) {
        ArrayList<Ink> effectedObjects = new ArrayList<Ink>();
        ArrayList<Point> gesturePoints = gestureStroke.getPointList();
        int gMinX = gesturePoints.get(0).getX();
        int gMinY = gesturePoints.get(0).getY();
        int gMaxX = gesturePoints.get(0).getX();
        int gMaxY = gesturePoints.get(0).getY();
        for (int i = 0; i < gesturePoints.size(); i++) {
            int currX = gesturePoints.get(i).getX();
            int currY = gesturePoints.get(i).getY();
            if (currX < gMinX) { gMinX = currX; }
            if (currY < gMinY) { gMinY = currY; }
            if (currX > gMaxX) { gMaxX = currX; }
            if (currY > gMaxY) { gMaxY = currY; }
        }
        for (Ink o : displayList) {
            String type = o.getType();
            int iMinX = 0;
            int iMinY = 0;
            int iMaxX = 0;
            int iMaxY = 0;
            if (type == "stroke") {
                Stroke stroke = (Stroke) o;
                ArrayList<Point> strokePoints = stroke.getPointList();
                iMinX = strokePoints.get(0).getX();
                iMinY = strokePoints.get(0).getY();
                iMaxX = strokePoints.get(0).getX();
                iMaxY = strokePoints.get(0).getY();
                for (int i = 0; i < strokePoints.size(); i++) {
                    int currX = strokePoints.get(i).getX();
                    int currY = strokePoints.get(i).getY();
                    if (currX < iMinX) { iMinX = currX; }
                    if (currY < iMinY) { iMinY = currY; }
                    if (currX > iMaxX) { iMaxX = currX; }
                    if (currY > iMaxY) { iMaxY = currY; }
                }
            } else if (type == "rectangle") {
                RectangleInk rect = (RectangleInk) o;
                Point start = rect.getMouseStart();
                Point end = rect.getMouseEnd();
                if (start.getX() < end.getX()) {
                    iMinX = start.getX();
                    iMaxX = end.getX();
                } else {
                    iMinX = end.getX();
                    iMaxX = start.getX();
                }
                if (start.getY() < end.getY()) {
                    iMinY = start.getY();
                    iMaxY = end.getY();
                } else {
                    iMinY = end.getY();
                    iMaxY = start.getY();
                }
            } else if (type == "oval") {
                OvalInk oval = (OvalInk) o;
                Point start = oval.getMouseStart();
                Point end = oval.getMouseEnd();
                if (start.getX() < end.getX()) {
                    iMinX = start.getX();
                    iMaxX = end.getX();
                } else {
                    iMinX = end.getX();
                    iMaxX = start.getX();
                }
                if (start.getY() < end.getY()) {
                    iMinY = start.getY();
                    iMaxY = end.getY();
                } else {
                    iMinY = end.getY();
                    iMaxY = start.getY();
                }
            } else if (type == "text") {
                TextInk text = (TextInk) o;
                Point start = text.getMouseStart();
                Point end = text.getMouseEnd();
                if (start.getX() < end.getX()) {
                    iMinX = start.getX();
                    iMaxX = end.getX();
                } else {
                    iMinX = end.getX();
                    iMaxX = start.getX();
                }
                if (start.getY() < end.getY()) {
                    iMinY = start.getY();
                    iMaxY = end.getY();
                } else {
                    iMinY = end.getY();
                    iMaxY = start.getY();
                }
            }
            if (iMinX >= gMinX && iMaxX <= gMaxX && iMinY >= gMinY && iMaxY <= gMaxY) {
                effectedObjects.add(o);
            }
        }
        return effectedObjects;
    }

    // Deletes ink in a gesture's bounding box if the ink's
    // bounding box is completely inside the gesture's.
    private void deleteInk(Stroke gestureStroke) {
        ArrayList<Ink> deleteList = checkGestureBounds(gestureStroke);
        for (Ink d : deleteList) {
            displayList.remove(d);
        }
        repaint();
    }

    private void selectInk(Stroke gestureStroke) {
        selectedHighlights.clear();
        selectedList.clear();
        selectedList = checkGestureBounds(gestureStroke);
        ArrayList<Point> gesturePoints = gestureStroke.getPointList();
        int gMinX = gesturePoints.get(0).getX();
        int gMinY = gesturePoints.get(0).getY();
        int gMaxX = gesturePoints.get(0).getX();
        int gMaxY = gesturePoints.get(0).getY();
        for (int i = 0; i < gesturePoints.size(); i++) {
            int currX = gesturePoints.get(i).getX();
            int currY = gesturePoints.get(i).getY();
            if (currX < gMinX) { gMinX = currX; }
            if (currY < gMinY) { gMinY = currY; }
            if (currX > gMaxX) { gMaxX = currX; }
            if (currY > gMaxY) { gMaxY = currY; }
        }
        Point gStartPoint = new Point(gMinX, gMinY);
        Point gEndPoint = new Point(gMaxX, gMaxY);
        selectionBounds = new RectangleInk(gStartPoint, gEndPoint);
        selectionBounds.setColor("pink");
        displayList.add(selectionBounds);
        selectionMinX = gMinX;
        selectionMinY = gMinY;
        selectionMaxX = gMaxX;
        selectionMaxY = gMaxY;
        for (Ink s : selectedList) {
            displayList.remove(s);
            String type = s.getType();
            int minX = 0;
            int minY = 0;
            int maxX = 0;
            int maxY = 0;
            int buffer = 5;
            if (type == "stroke") {
                Stroke stroke = (Stroke) s;
                ArrayList<Point> strokePoints = stroke.getPointList();
                minX = strokePoints.get(0).getX();
                minY = strokePoints.get(0).getY();
                maxX = strokePoints.get(0).getX();
                maxY = strokePoints.get(0).getY();
                for (int i = 0; i < strokePoints.size(); i++) {
                    int currX = strokePoints.get(i).getX();
                    int currY = strokePoints.get(i).getY();
                    if (currX < minX) { minX = currX; }
                    if (currY < minY) { minY = currY; }
                    if (currX > maxX) { maxX = currX; }
                    if (currY > maxY) { maxY = currY; }
                }
            } else if (type == "rectangle") {
                RectangleInk rect = (RectangleInk) s;
                Point start = rect.getMouseStart();
                Point end = rect.getMouseEnd();
                if (start.getX() < end.getX()) {
                    minX = start.getX();
                    maxX = end.getX();
                } else {
                    minX = end.getX();
                    maxX = start.getX();
                }
                if (start.getY() < end.getY()) {
                    minY = start.getY();
                    maxY = end.getY();
                } else {
                    minY = end.getY();
                    maxY = start.getY();
                }
            } else if (type == "oval") {
                OvalInk oval = (OvalInk) s;
                Point start = oval.getMouseStart();
                Point end = oval.getMouseEnd();
                if (start.getX() < end.getX()) {
                    minX = start.getX();
                    maxX = end.getX();
                } else {
                    minX = end.getX();
                    maxX = start.getX();
                }
                if (start.getY() < end.getY()) {
                    minY = start.getY();
                    maxY = end.getY();
                } else {
                    minY = end.getY();
                    maxY = start.getY();
                }
            } else if (type == "text") {
                TextInk text = (TextInk) s;
                Point start = text.getMouseStart();
                Point end = text.getMouseEnd();
                if (start.getX() < end.getX()) {
                    minX = start.getX();
                    maxX = end.getX();
                } else {
                    minX = end.getX();
                    maxX = start.getX();
                }
                if (start.getY() < end.getY()) {
                    minY = start.getY();
                    maxY = end.getY();
                } else {
                    minY = end.getY();
                    maxY = start.getY();
                }
            }
            Point startPoint = new Point(0, 0);
            Point endPoint = new Point(0, 0);
            if (type == "text") {
                startPoint = new Point(minX - ((maxX - minX) / 10) - buffer, minY - buffer);
                endPoint = new Point(maxX + buffer, maxY + ((maxY - minY) / 10) + buffer);
            } else {
                startPoint = new Point(minX - buffer, minY - buffer);
                endPoint = new Point(maxX + buffer, maxY + buffer);
            }
            RectangleInk selectionRect = new RectangleInk(startPoint, endPoint);
            selectionRect.setColor("cyan");
            displayList.add(selectionRect);
            selectedHighlights.add(selectionRect);
            displayList.add(s);
        }
        if (selectedList.size() == 0) {
            displayList.remove(selectionBounds);
            selectionMode = false;
        } else {
            selectionMode = true;
        }
    }

    public BufferedImage makeOffscreenImage (JComponent source) {
        // Create our BufferedImage and get a Graphics object for it
        GraphicsConfiguration gfxConfig = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        BufferedImage offscreenImage = gfxConfig.createCompatibleImage(source.getWidth(), source.getHeight());
        Graphics2D offscreenGraphics = (Graphics2D) offscreenImage.getGraphics();
        
        // Tell the component to paint itself onto the image
        source.paint(offscreenGraphics);
        
        // return the image
        return offscreenImage;
    }

    // Creates the animation when the page turns forward
    public void animateForwardPageTurn(String command) {

        offscreenImages[1] = makeOffscreenImage(this);
        offscreenImages[2] = makeOffscreenImage(connectedPages[1]);

        Timer timer = new Timer(25, new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.printf("Animating forward bool: %b, Frame: %d\n", isAnimatingPageForward, frameCounter);
                if (frameCounter > 25) {
                    ((Timer)e.getSource()).stop();
                    frameCounter = 1;
                    isAnimatingPageForward = false;
                    repaint();
                } else {
                    isAnimatingPageForward = true;
                    repaint();
                    frameCounter++;
                }
            }
        });
        int delay = 0;
        if (command == "gesture") {
            delay = 100;
        } else if (command == "button") {
            delay = 100;
            timer.setInitialDelay(100);
        }
        timer.start();
        try {
            Thread.currentThread().sleep(delay);
        } catch (InterruptedException ie) {
            // ... Error message...
        }
    }

    // Creates the animation when the page turns forward
    public void animateBackwardPageTurn(String command) {

        offscreenImages[1] = makeOffscreenImage(this);
        offscreenImages[0] = makeOffscreenImage(connectedPages[0]);

        Timer timer = new Timer(25, new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.printf("Animating backward bool: %b, Frame: %d\n", isAnimatingPageBackward, frameCounter);
                if (frameCounter > 25) {
                    ((Timer)e.getSource()).stop();
                    frameCounter = 1;
                    isAnimatingPageBackward = false;
                    repaint();
                } else {
                    isAnimatingPageBackward = true;
                    repaint();
                    frameCounter++;
                }
            }
        });
        int delay = 0;
        if (command == "gesture") {
            delay = 100;
        } else if (command == "button") {
            delay = 100;
            timer.setInitialDelay(100);
        }
        timer.start();
        try {
            Thread.currentThread().sleep(delay);
        } catch (InterruptedException ie) {
            // ... Error message...
        }
    }

    // Finishes the dynamic page backward turn
    private void finishDynamicBackwardPageTurn() {

        Timer timer = new Timer(25, new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.printf("Finishing dynamic backward bool: %b, Frame: %d\n", isFinishingDynamicPageBackward, frameCounter);
                if (frameCounter > 24) {
                    ((Timer)e.getSource()).stop();
                    frameCounter = 1;
                    isFinishingDynamicPageBackward = false;
                    repaint();
                } else {
                    isFinishingDynamicPageBackward = true;
                    repaint();
                    frameCounter++;
                }
            }
        });
        int delay = 0;
        delay = 100;
        timer.setInitialDelay(100);
        timer.start();
        try {
            Thread.currentThread().sleep(delay);
        } catch (InterruptedException ie) {
            // ... Error message...
        }
    }

    // Finishes the dynamic page backward turn
    private void finishDynamicForwardPageTurn() {

        Timer timer = new Timer(25, new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.printf("Finishing dynamic backward bool: %b, Frame: %d\n", isFinishingDynamicPageBackward, frameCounter);
                if (frameCounter > 24) {
                    ((Timer)e.getSource()).stop();
                    frameCounter = 1;
                    isFinishingDynamicPageForward = false;
                    repaint();
                } else {
                    isFinishingDynamicPageForward = true;
                    repaint();
                    frameCounter++;
                }
            }
        });
        int delay = 0;
        delay = 100;
        timer.setInitialDelay(100);
        timer.start();
        try {
            Thread.currentThread().sleep(delay);
        } catch (InterruptedException ie) {
            // ... Error message...
        }
    }

}