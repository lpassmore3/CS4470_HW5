import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.HTMLEditorKit;
import java.io.IOException;
import java.util.ArrayList;

public class Main implements ActionListener, HyperlinkListener {
    // Values for sizing
    static int startSizeX = 1300;
    static int startSizeY = 700;
    static int frameMinX = 1000;
    static int frameMinY = 600;
    static int tabPaneMinX = 400;
    static int rightSideMinX = 500;
    static int addressMinY = 200;

    static JFrame frame;

    // ArrayList to hold the urls for the history setting
    static ArrayList<String> urls = new ArrayList<String>();
    static int urlIndex = 0;
    static JEditorPane browser;
    static String homePage = "https://en.wikipedia.org";
    JTextField urlField;

    // Static variables for the address book
    static int oldSelectionIndex = 0;
    static int currSelectionIndex = 0;
    static JTable addressTable;
    static String selectedContact = "";
    static JTextArea selectedTextArea;
    static Object[][] addressData = {
        {"Martin", "Mertens", "378-555-2389"},
        {"Minerva", "Mertens", "157-555-4825"},
        {"Susan", "Strong", "289-555-1177"},
        {"Simon", "Petrikov", "163-555-2146"},
        {"Randy", "Butternubs", "291-555-8886"},
        {"Hunson", "Abadeer", "626-555-4377"},
        {"Marshall", "Lee", "626-555-2658"},
        {"Davey", "Johnson", "120-555-6682"},
        {"James", "Baxter", "998-555-5452"},
        {"Tree", "Trunks", "677-555-3309"},
        {"Choose", "Goose", "322-555-7777"},
        {"Peace", "Master", "882-555-2001"},
        {"Bonnibel", "Bubblegum", "231-555-8806"},
        {"Marceline", "Abadeer", "788-555-0015"},
        {"Tiny", "Manticore", "232-555-1008"},
        {"Magic", "Man", "638-555-0028"}
    };

    // Radio buttons to control the ink tool for the content side
    static JRadioButton freeInkButton;
    static JRadioButton rectInkButton;
    static JRadioButton ovalInkButton;
    static JRadioButton textInkButton;

    // Radio buttons to control the ink tool for the content side
    static JRadioButton blackInkButton;
    static JRadioButton redInkButton;
    static JRadioButton greenInkButton;
    static JRadioButton blueInkButton;

    // The JLabel for the staus bar at the bottom of the application
    static JLabel statusBar;

    // The JPanel that holds the MyPages
    static JPanel rightSide;

    // Holds the array of MyPages for the User Content side
    ArrayList<MyPage> myPages = new ArrayList<MyPage>();
    int myPagesIndex = 0;


    public Main() {
        // Setting up the JFrame
        frame = new JFrame();
        frame.setSize(startSizeX, startSizeY);
        frame.setMinimumSize(new Dimension(frameMinX, frameMinY));
        frame.setTitle("Developing the First Swing Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Setting up the JPanel to hold the JSplitPane and JLabel
        JPanel mainPanel = new JPanel(new BorderLayout());


        // Setting up the JTabbedPane for the left side of the SplitPane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setMinimumSize(new Dimension(tabPaneMinX, frameMinY));

        // Tab 1 is a JEditorPane for a web browser
        browser = new JEditorPane();
        browser.setEditable(false);
        browser.addHyperlinkListener(this);
        JScrollPane browserScroll = new JScrollPane(browser);
        //tab1.setPreferredSize(new Dimension((startSizeX / 2) / 4, startSizeY / 10));

        // Set the page to show URL text
        if (homePage != null) {
            try {
                browser.setPage(homePage);
                //browserScroll.getVerticalScrollBar().setValue(0);
            } catch (IOException e) {
                System.err.println("Attempted to read a bad URL: " + homePage);
            }
        }

        // Setting up the history setting for the web browser
        urlIndex = 0;
        urls.add(homePage);

        // The top of the web browser with buttons and url box
        JPanel browserBar = new JPanel(new BorderLayout());
        JPanel browserButtons = new JPanel(new BorderLayout());
        browserBar.add(browserButtons, BorderLayout.LINE_START);

        JButton backButton = new JButton("<");
        browserButtons.add(backButton, BorderLayout.LINE_START);
        backButton.setActionCommand("back");
        backButton.addActionListener(this);

        JButton forwardButton = new JButton(">");
        browserButtons.add(forwardButton, BorderLayout.CENTER);
        forwardButton.setActionCommand("forward");
        forwardButton.addActionListener(this);

        JButton homeButton = new JButton("Home");
        browserButtons.add(homeButton, BorderLayout.LINE_END);
        homeButton.setActionCommand("home");
        homeButton.addActionListener(this);

        urlField = new JTextField(homePage);
        browserBar.add(urlField, BorderLayout.CENTER);
        urlField.setActionCommand("url");
        urlField.addActionListener(this);


        JPanel tab1 = new JPanel(new BorderLayout());
        tab1.add(browserScroll, BorderLayout.CENTER);
        tab1.add(browserBar, BorderLayout.PAGE_START);
        tabbedPane.addTab("Web Browser", tab1);


        // The top of Tab 2 is a scrollable table
        String[] columnNames = {"First Name", "Last Name", "Phone Number"};
        addressTable = new JTable(addressData, columnNames);
        addressTable.setRowHeight(50);
        // Make table scrollable
        JScrollPane addressScroll = new JScrollPane(addressTable);
        addressScroll.setMinimumSize(new Dimension(tabPaneMinX, addressMinY));
        //addressScroll.setPreferredSize(new Dimension(300, 300));
        //addressTable.setFillsViewportHeight(true);

        //Set variables for updating selected text area
        oldSelectionIndex = addressTable.getSelectionModel().getLeadSelectionIndex();
        currSelectionIndex = addressTable.getSelectionModel().getLeadSelectionIndex();

        // The bottom of Tab 2 is a text area that shows the selected info
        selectedTextArea = new JTextArea(selectedContact);
        // Make text area scrollable
        JScrollPane selectedTextScroll = new JScrollPane(selectedTextArea);
        selectedTextScroll.setMinimumSize(new Dimension(tabPaneMinX, addressMinY));
        //selectedTextScroll.setPreferredSize(new Dimension(300, 120));


        // Add the table and the text area to the tab
        // The SplitPane for the entire application
        JSplitPane tab2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            addressScroll, selectedTextScroll);

        // tab2.add(addressScroll, BorderLayout.CENTER);
        // tab2.add(selectedTextScroll, BorderLayout.PAGE_END);
        tabbedPane.addTab("Address Book", tab2);



        // Right side of the application
        rightSide = new JPanel(new BorderLayout());
        rightSide.setMinimumSize(new Dimension(rightSideMinX, frameMinY));

        // The page where the user will be able to edit
        //JPanel userContent = new JPanel();
        MyPage userContent = new MyPage();
        myPages.add(userContent);
        rightSide.add(userContent, BorderLayout.CENTER);

        // All the buttons for the right side
        JPanel contentButtons = new JPanel(new BorderLayout());
        rightSide.add(contentButtons, BorderLayout.PAGE_END);

        // The buttons to control the page feature
        JPanel pageButtons = new JPanel(new GridLayout(1, 4, 10, 10));
        contentButtons.add(pageButtons, BorderLayout.PAGE_START);

        JButton newPageButton = new JButton("New Page");
        pageButtons.add(newPageButton);
        newPageButton.setActionCommand("new page");
        newPageButton.addActionListener(this);

        JButton deletePageButton = new JButton("Delete Page");
        pageButtons.add(deletePageButton);
        deletePageButton.setActionCommand("delete page");
        deletePageButton.addActionListener(this);

        JButton pageBackwardButton = new JButton("< Page Backward");
        pageButtons.add(pageBackwardButton);
        pageBackwardButton.setActionCommand("page backward");
        pageBackwardButton.addActionListener(this);

        JButton pageForwardButton = new JButton("Page Forward >");
        pageButtons.add(pageForwardButton);
        pageForwardButton.setActionCommand("page forward");
        pageForwardButton.addActionListener(this);


        // The buttons to control ink input on user content pages
        JPanel inkToolButtonsContainer = new JPanel(new GridLayout(1, 4, 10, 10));
        inkToolButtonsContainer.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        JPanel inkColorButtonsContainer = new JPanel(new GridLayout(1, 4, 10, 10));
        inkColorButtonsContainer.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        JPanel inkButtonsContainer = new JPanel(new GridLayout(2, 1, 5, 5));
        inkButtonsContainer.add(inkToolButtonsContainer);
        inkButtonsContainer.add(inkColorButtonsContainer);
        contentButtons.add(inkButtonsContainer, BorderLayout.PAGE_END);

        // Ink tool buttons
        freeInkButton = new JRadioButton("Free-Form Ink");
        inkToolButtonsContainer.add(freeInkButton);
        freeInkButton.setActionCommand("free ink");
        freeInkButton.addActionListener(this);

        rectInkButton = new JRadioButton("Rectangle");
        inkToolButtonsContainer.add(rectInkButton);
        rectInkButton.setActionCommand("rectangle");
        rectInkButton.addActionListener(this);

        ovalInkButton = new JRadioButton("Oval");
        inkToolButtonsContainer.add(ovalInkButton);
        ovalInkButton.setActionCommand("oval");
        ovalInkButton.addActionListener(this);

        textInkButton = new JRadioButton("Text");
        inkToolButtonsContainer.add(textInkButton);
        textInkButton.setActionCommand("text ink");
        textInkButton.addActionListener(this);

        // Ink color buttons
        blackInkButton = new JRadioButton("Black");
        inkColorButtonsContainer.add(blackInkButton);
        blackInkButton.setSelected(true);
        blackInkButton.setActionCommand("black ink");
        blackInkButton.addActionListener(this);

        redInkButton = new JRadioButton("Red");
        inkColorButtonsContainer.add(redInkButton);
        redInkButton.setActionCommand("red ink");
        redInkButton.addActionListener(this);

        greenInkButton = new JRadioButton("Green");
        inkColorButtonsContainer.add(greenInkButton);
        greenInkButton.setActionCommand("green ink");
        greenInkButton.addActionListener(this);

        blueInkButton = new JRadioButton("Blue");
        inkColorButtonsContainer.add(blueInkButton);
        blueInkButton.setActionCommand("blue ink");
        blueInkButton.addActionListener(this);


        // The SplitPane for the entire application
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            tabbedPane, rightSide);
        splitPane.setDividerLocation(startSizeX / 2);
        splitPane.setResizeWeight(0.5);
        //splitPane.setOneTouchExpandable(true);

        // The status bar at the bottom of the application
        statusBar = new JLabel();
        statusBar.setText(" ");

        // Adding the JSplitPane and the JLabel to the panel
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(statusBar, BorderLayout.PAGE_END);

        // Adding the JPanel to the frame
        frame.getContentPane().add(mainPanel);
    }

    public static void main(String[] args) {
        Main main = new Main();

        frame.setVisible(true);

        // Loop to update
        Boolean updateSelectedText = true;
        while (true) {
            // Update text area in address tab
            oldSelectionIndex = currSelectionIndex;
            currSelectionIndex = addressTable.getSelectionModel().getLeadSelectionIndex();
            System.out.print("");
            if (currSelectionIndex >= 0 && currSelectionIndex != oldSelectionIndex) {
                selectedContact = String.format("%s %s, %s",
                    addressData[currSelectionIndex][0],
                    addressData[currSelectionIndex][1],
                    addressData[currSelectionIndex][2]);
                updateSelectedText = true;
                //selectedTextArea.insert(selectedContact, 0);
            }
            if (updateSelectedText) {
                selectedTextArea.setText(null);
                selectedTextArea.setText(selectedContact);
                updateSelectedText = false;
            }
            ArrayList<MyPage> pages = main.getMyPages();
            int pageIndex = main.getMyPagesIndex();
            String gesture = pages.get(pageIndex).getPageGesture();
            if (gesture == "NULL_GESTURE") {
                statusBar.setText("Gesture not recognized.");
                pages.get(pageIndex).clearPageGesture();
            } else if (gesture == "PIGTAIL") {
                statusBar.setText("PIGTAIL gesture recognized. Deleting ink.");
                pages.get(pageIndex).clearPageGesture();
            } else if (gesture == "LOOP") {
                statusBar.setText("LOOP gesture recognized. Ink selected.");
                pages.get(pageIndex).clearPageGesture();
            } else if (gesture == "RIGHT_ANGLE" || gesture == "FINISHED_PAGE_FORWARD") {
                pages.get(pageIndex).clearPageGesture();
                freeInkButton.setSelected(false);
                rectInkButton.setSelected(false);
                ovalInkButton.setSelected(false);
                textInkButton.setSelected(false);
                if (pageIndex < pages.size() - 1) {
                    if (gesture == "RIGHT_ANGLE") {
                        pages.get(pageIndex + 1).animateBackwardPageTurn("gesture");
                    } else if (gesture == "FINISHED_PAGE_FORWARD") {
                        try {
                            Thread.currentThread().sleep(700);
                        } catch (InterruptedException ie) {
                            // ... Error message...
                        }
                    }
                    BorderLayout layout = (BorderLayout) rightSide.getLayout();
                    rightSide.remove(layout.getLayoutComponent(BorderLayout.CENTER));
                    main.setMyPagesIndex(pageIndex + 1);
                    pageIndex++;
                    rightSide.add(pages.get(pageIndex), BorderLayout.CENTER);
                    statusBar.setText("RIGHT_ANGLE gesture recognized. Showing forward page. Viewing page " + Integer.toString(pageIndex + 1) + " of " + Integer.toString(pages.size()) + ".");
                } else {
                    statusBar.setText("RIGHT_ANGLE gesture recognized. No forward page. Showing same page " + Integer.toString(pageIndex + 1) + " of " + Integer.toString(pages.size()) + ".");
                }
                pages.get(pageIndex).repaint();
                pages.get(pageIndex).setTool("");
            } else if (gesture == "LEFT_ANGLE" || gesture == "FINISHED_PAGE_BACK") {
                pages.get(pageIndex).clearPageGesture();
                freeInkButton.setSelected(false);
                rectInkButton.setSelected(false);
                ovalInkButton.setSelected(false);
                textInkButton.setSelected(false);
                if (pageIndex > 0) {
                    if (gesture == "LEFT_ANGLE") {
                        pages.get(pageIndex - 1).animateForwardPageTurn("gesture");
                    } else if (gesture == "FINISHED_PAGE_BACK") {
                        try {
                            Thread.currentThread().sleep(700);
                        } catch (InterruptedException ie) {
                            // ... Error message...
                        }
                    }
                    BorderLayout layout = (BorderLayout) rightSide.getLayout();
                    rightSide.remove(layout.getLayoutComponent(BorderLayout.CENTER));
                    main.setMyPagesIndex(pageIndex - 1);
                    pageIndex--;
                    rightSide.add(pages.get(pageIndex), BorderLayout.CENTER);
                    statusBar.setText("LEFT_ANGLE gesture recognized. Showing backward page. Viewing page " + Integer.toString(pageIndex + 1) + " of " + Integer.toString(pages.size()) + ".");
                } else {
                    statusBar.setText("LEFT_ANGLE gesture recognized. No backward page. Showing same page " + Integer.toString(pageIndex + 1) + " of " + Integer.toString(pages.size()) + ".");
                }
                pages.get(pageIndex).repaint();
                pages.get(pageIndex).setTool("");
            }


            // Update sizing
            // Rectangle frameBounds = frame.getBounds();
            // double frameHeight = frameBounds.getHeight();
            // double frameWidth = frameBounds.getWidth();
            // System.out.printf("Width: " + Double.toString(frameWidth) + ", Height: " + Double.toString(frameHeight) + "\n");
        }

    }

    // Returns the list of pages
    public ArrayList<MyPage> getMyPages() {
        return this.myPages;
    }

    // Returns the index of the current page
    public int getMyPagesIndex() {
        return this.myPagesIndex;
    }

    // Sets the index of the current page
    public void setMyPagesIndex(int i) {
        this.myPagesIndex = i;
    }

    // Handles button events
    public void actionPerformed(ActionEvent e) {
        if ("back".equals(e.getActionCommand())) { // Back bottom pressed
            if (urlIndex - 1 >= 0) {
                urlIndex = urlIndex - 1;
                String backPage = urls.get(urlIndex);
                //System.out.println(backPage);
                try {
                    browser.setPage(backPage);
                } catch (IOException excep) {
                    System.err.println("Attempted to read a bad URL: " + backPage);
                }
                urlField.setText(backPage);
                statusBar.setText("Back button pressed. Previous webpage shown.");
            }
        } else if ("forward".equals(e.getActionCommand())) { // Forward button pressed
            int urlSize = urls.size();
            if (urlIndex + 1 <= urlSize - 1) {
                urlIndex = urlIndex + 1;
                String forwardPage = urls.get(urlIndex);
                //System.out.println(forwardPage);
                try {
                    browser.setPage(forwardPage);
                } catch (IOException excep) {
                    System.err.println("Attempted to read a bad URL: " + forwardPage);
                }
                urlField.setText(forwardPage);
                statusBar.setText("Forward button pressed. Next webpage shown.");

            }
        } else if ("home".equals(e.getActionCommand())) { // Home button pressed
            //System.out.println(homePage);
            try {
                browser.setPage(homePage);
            } catch (IOException excep) {
                System.err.println("Attempted to read a bad URL: " + homePage);
            }
            urlField.setText(homePage);
            statusBar.setText("Home button pressed. Home webpage shown.");
        } else if ("url".equals(e.getActionCommand())) { // URL bar updated
            try {
                String newURL = urlField.getText();
                browser.setPage(newURL);
                urls.add(newURL);
                urlIndex = urlIndex + 1;
                statusBar.setText("Viewing new webpage: " + newURL);
            } catch (IOException excep) {
                System.err.println("Attempted to read a bad URL: " + homePage);
            }
        } else if ("free ink".equals(e.getActionCommand())) { // Free-form ink radio button
            //freeInkButton.setSelected(!freeInkButton.isSelected());
            rectInkButton.setSelected(false);
            ovalInkButton.setSelected(false);
            textInkButton.setSelected(false);
            myPages.get(myPagesIndex).setTool("stroke");
            //System.out.println(myPages.get(myPagesIndex).getDisplayList().size());
            statusBar.setText("Free-Form Ink Tool selected.");
        } else if ("rectangle".equals(e.getActionCommand())) { // Rectangle ink radio button
            //freeInkButton.setSelected(!freeInkButton.isSelected());
            freeInkButton.setSelected(false);
            ovalInkButton.setSelected(false);
            textInkButton.setSelected(false);
            myPages.get(myPagesIndex).setTool("rect");
            statusBar.setText("Rectangle Tool selected.");
        } else if ("oval".equals(e.getActionCommand())) { // Oval ink radio button
            //freeInkButton.setSelected(!freeInkButton.isSelected());
            freeInkButton.setSelected(false);
            rectInkButton.setSelected(false);
            textInkButton.setSelected(false);
            myPages.get(myPagesIndex).setTool("oval");
            statusBar.setText("Oval Tool selected.");
        } else if ("text ink".equals(e.getActionCommand())) { // Text ink radio button
            //freeInkButton.setSelected(!freeInkButton.isSelected());
            freeInkButton.setSelected(false);
            rectInkButton.setSelected(false);
            ovalInkButton.setSelected(false);
            myPages.get(myPagesIndex).setTool("text");
            statusBar.setText("Text Tool selected.");
        } else if ("black ink".equals(e.getActionCommand())) { // Black ink color radio button
            redInkButton.setSelected(false);
            greenInkButton.setSelected(false);
            blueInkButton.setSelected(false);
            myPages.get(myPagesIndex).setInkColor("black");
            statusBar.setText("Black ink color selected.");
        } else if ("red ink".equals(e.getActionCommand())) { // Red ink color radio button
            blackInkButton.setSelected(false);
            greenInkButton.setSelected(false);
            blueInkButton.setSelected(false);
            myPages.get(myPagesIndex).setInkColor("red");
            statusBar.setText("Red ink color selected.");
        } else if ("green ink".equals(e.getActionCommand())) { // Green ink color radio button
            blackInkButton.setSelected(false);
            redInkButton.setSelected(false);
            blueInkButton.setSelected(false);
            myPages.get(myPagesIndex).setInkColor("green");
            statusBar.setText("Green ink color selected.");
        } else if ("blue ink".equals(e.getActionCommand())) { // Blue ink color radio button
            blackInkButton.setSelected(false);
            redInkButton.setSelected(false);
            greenInkButton.setSelected(false);
            myPages.get(myPagesIndex).setInkColor("blue");
            statusBar.setText("Blue ink color selected.");
        } else if ("new page".equals(e.getActionCommand())) { // New Page button
            freeInkButton.setSelected(false);
            rectInkButton.setSelected(false);
            ovalInkButton.setSelected(false);
            textInkButton.setSelected(false);
            MyPage newPage = new MyPage();
            myPages.add(myPagesIndex + 1, newPage);
            myPagesIndex++;
            if (myPagesIndex > 0) {
                newPage.setBackwardPage(myPages.get(myPagesIndex - 1));
                myPages.get(myPagesIndex - 1).setForwardPage(newPage);
            }
            if (myPagesIndex < myPages.size() - 1) {
                newPage.setForwardPage(myPages.get(myPagesIndex + 1));
                myPages.get(myPagesIndex + 1).setBackwardPage(newPage);
            }
            BorderLayout layout = (BorderLayout) rightSide.getLayout();
            rightSide.remove(layout.getLayoutComponent(BorderLayout.CENTER));
            rightSide.add(newPage, BorderLayout.CENTER);
            myPages.get(myPagesIndex).setTool("");
            statusBar.setText("New page created. Viewing page " + Integer.toString(myPagesIndex + 1) + " of " + Integer.toString(myPages.size()) + ".");
            //System.out.printf("Curr index: %d, Page size: %d", myPagesIndex, myPages.size());
        } else if ("delete page".equals(e.getActionCommand())) { // Delete Page button
            freeInkButton.setSelected(false);
            rectInkButton.setSelected(false);
            ovalInkButton.setSelected(false);
            textInkButton.setSelected(false);
            myPages.remove(myPagesIndex);
            BorderLayout layout = (BorderLayout) rightSide.getLayout();
            rightSide.remove(layout.getLayoutComponent(BorderLayout.CENTER));
            if (myPages.size() <= 0) {
                MyPage newPage = new MyPage();
                myPages.add(newPage);
                myPagesIndex = 0;
                rightSide.add(newPage, BorderLayout.CENTER);
            } else if (myPagesIndex == 0) {
                rightSide.add(myPages.get(myPagesIndex), BorderLayout.CENTER);
                myPages.get(myPagesIndex).setBackwardPage(null);
            } else {
                myPagesIndex--;
                rightSide.add(myPages.get(myPagesIndex), BorderLayout.CENTER);
                myPages.get(myPagesIndex).setBackwardPage(myPages.get(myPagesIndex - 1));
                myPages.get(myPagesIndex - 1).setForwardPage(myPages.get(myPagesIndex));
            }
            myPages.get(myPagesIndex).repaint();
            myPages.get(myPagesIndex).setTool("");
            statusBar.setText("Page deleted. Viewing page " + Integer.toString(myPagesIndex + 1) + " of " + Integer.toString(myPages.size()) + ".");
            //System.out.printf("Curr index: %d, Page size: %d", myPagesIndex, myPages.size());
        } else if ("page backward".equals(e.getActionCommand())) { // Page Backward button
            freeInkButton.setSelected(false);
            rectInkButton.setSelected(false);
            ovalInkButton.setSelected(false);
            textInkButton.setSelected(false);
            if (myPagesIndex > 0) {
                myPages.get(myPagesIndex - 1).animateForwardPageTurn("button");
                BorderLayout layout = (BorderLayout) rightSide.getLayout();
                rightSide.remove(layout.getLayoutComponent(BorderLayout.CENTER));
                myPagesIndex--;
                rightSide.add(myPages.get(myPagesIndex), BorderLayout.CENTER);
                statusBar.setText("Showing backward page. Viewing page " + Integer.toString(myPagesIndex + 1) + " of " + Integer.toString(myPages.size()) + ".");
            } else {
                statusBar.setText("No backward page. Showing same page " + Integer.toString(myPagesIndex + 1) + " of " + Integer.toString(myPages.size()) + ".");
            }
            myPages.get(myPagesIndex).revalidate();
            myPages.get(myPagesIndex).repaint();
            myPages.get(myPagesIndex).setTool("");
            //System.out.printf("Curr index: %d, Page size: %d", myPagesIndex, myPages.size());
        } else if ("page forward".equals(e.getActionCommand())) { // Page Forward button
            freeInkButton.setSelected(false);
            rectInkButton.setSelected(false);
            ovalInkButton.setSelected(false);
            textInkButton.setSelected(false);
            if (myPagesIndex < myPages.size() + 1) {
                myPages.get(myPagesIndex + 1).animateBackwardPageTurn("button");
                BorderLayout layout = (BorderLayout) rightSide.getLayout();
                rightSide.remove(layout.getLayoutComponent(BorderLayout.CENTER));
                myPagesIndex++;
                rightSide.add(myPages.get(myPagesIndex), BorderLayout.CENTER);
                statusBar.setText("Showing forward page. Viewing page " + Integer.toString(myPagesIndex + 1) + " of " + Integer.toString(myPages.size()) + ".");
            } else {
                statusBar.setText("No forward page. Showing same page " + Integer.toString(myPagesIndex + 1) + " of " + Integer.toString(myPages.size()) + ".");
            }
            myPages.get(myPagesIndex).revalidate();
            myPages.get(myPagesIndex).repaint();
            myPages.get(myPagesIndex).setTool("");
            //System.out.printf("Curr index: %d, Page size: %d", myPagesIndex, myPages.size());
        }
    }

    // Makes hyperlinks in browser clickable
    public void hyperlinkUpdate(HyperlinkEvent he) {
        HyperlinkEvent.EventType type = he.getEventType();
        if (type == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                browser.setPage(he.getURL());
                String newURL = he.getURL().toString();
                urls.add(newURL);
                urlIndex = urlIndex + 1;
                urlField.setText(newURL);
                statusBar.setText("Hyperlink clicked. Viewing " + newURL);
            } catch (IOException excep) {
                System.err.println("Error caught when trying to access hyperlink.");
            }
        }
    }

}