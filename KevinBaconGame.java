import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class KevinBaconGame {
	public final int width = 600, height = 800; // these are the parameters that deterine the size of the screen
	private JTextArea displayArea, typeArea; // two main regions for typing and displaying text
	boolean entered = false; // parameter to make sure that the text box is not empty
	String text = ""; // keeps track of what the user inputted
	int fontsize = 20; // the size of the displaying font
	ArrayList<String> displayText = new ArrayList<String>(); // arraylist of everything that should be on the text display
	int textSpace = 25; // the space between each line of text
	int numberOfChar = 600/10; // the number of characters that each line can hold
	// all these stuff are used to keep track of info
	LabeledGraph<String, String> graph = new LabeledGraph<String, String>();
	HashMap<String, String> actorsMap = new HashMap<String, String>();
	HashMap<String, String> moviesMap = new HashMap<String, String>();
	HashMap<String, String> map = new HashMap<String, String>();
	
	public KevinBaconGame() {
		JFrame frame = new JFrame();
		frame.setSize(width, height);
		paintString("Welcome to the Kevin Bacon Game! Start typing in the text box and click the buttons to begin!");
		paintString("If more than one input is required, you should separate it by a comma!"); // this is gonna be on the screen at the very start
		// initializing a bunch of stuff
		JPanel canvas = new JPanel() {};
		canvas.setPreferredSize(new Dimension(width, width));
		JPanel userArea = new JPanel();
		userArea.setPreferredSize(new Dimension(width, height-width));
		
		displayArea = new JTextArea() {
			public void paint(Graphics g) {
				// draying the text display
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, width, 3*height/4);
			    Font tr = new Font("Lucida Bright", Font.PLAIN, fontsize);
			    g.setFont(tr);
			    // draws everything that is in the text arraylist
				for(int i = 0; i < displayText.size(); i++) {
					g.setColor(Color.BLACK);
					g.drawString(displayText.get(i), textSpace/5, textSpace*(i+1));
				}
			}
		};
		// add a scroller so you can scroll down
		JScrollPane scroller = new JScrollPane(displayArea);
		
		// some more setups
		displayArea.setEditable(false);
		displayArea.setBackground(new Color(247, 241, 225));
		displayArea.setPreferredSize(new Dimension(width, width));
		typeArea = new JTextArea();
		typeArea.setEditable(true);
		typeArea.setBackground(Color.WHITE);
		typeArea.setPreferredSize(new Dimension(width,height/4));
		// listens for any actions in the type area
		typeArea.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == '\n') {
					write();
				}
			}
			public void keyPressed(KeyEvent e) {}

			public void keyReleased(KeyEvent e) {}
		});
		
		// detects for actions and runs the program with the input
		// this will also output on the user's screen
		JButton distanceButton = new JButton("Distance Between");
		distanceButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				write();
				if(entered) {
					String[] t = text.split(",");
					for(int i = 0; i < t.length; i++) {
						t[i] = t[i].trim();
					}
					int bfs = BFS(t[0], t[1]);
					if(bfs != -1) {
						paintString(t[0] + " and " + t[1] + " are connected by " + bfs + " movies.");
					}
					frame.getContentPane().repaint();
				}
			}
		});
		
		// same as above but another function
		JButton bestFriendButton = new JButton("Find His/Her Best Friend");
		bestFriendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				write();
				if(entered) {
					String fbf = findBestFriend(text);
					if(!fbf.isEmpty()) {
						paintString(text + " is best friends with " + fbf + ".");
					}
					frame.getContentPane().repaint();
				}
			}
		});
		
		// samething as above
		JButton withinDistance = new JButton("Find # of People Within Distance");
		withinDistance.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				write();
				if(entered) {
					String[] t = text.split(",");
					for(int i = 0; i < t.length; i++) {
						t[i] = t[i].trim();
					}
					int wd = 0;
					try {
						wd = withInDistance(t[0], Integer.parseInt(t[1]));
					}
					// we need to do a this because the program might have an error because the user did not input a number following a comma
					catch(NumberFormatException n) {
						paintString("Please input a valid number following the actor with a comma!");
						wd = -1;
					}
					// the program should never return -1 if it is running properly, but will run the following output if there is no problems
					if(wd != -1) {
						paintString(t[0] + " has " + wd + " actors within " + " a distance of " + t[1] + " actors.");
					}
					frame.getContentPane().repaint(); // paints the new stuff
				}
			}
		});
		
		// similar to the previous ones
		JButton averageConnectivity = new JButton("Average Connectivity");
		averageConnectivity.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				write();
				if(entered) {
					double d = averageConnectivity(text);
					if(d != 0) { // again, average connectivity should enver be 0, so it will run the following if there is no error
						paintString(text + " has an average connectivity of "+ d + ".");
					}
					frame.getContentPane().repaint(); // paints the new text on the screen
				}
			}
			
		});
		
		BoxLayout boxlayout = new BoxLayout(canvas, BoxLayout.Y_AXIS); // makes sure that the layout builds from the top to bottom
		canvas.setLayout(boxlayout);
		
		// adding stuff to stuff so it will be visible
		frame.setSize(600, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		canvas.add(scroller);
		canvas.add(userArea);
		userArea.add(bestFriendButton);
		userArea.add(distanceButton);
		userArea.add(averageConnectivity);		
		userArea.add(withinDistance);
		userArea.add(typeArea);
		frame.add(canvas);
		canvas.setFocusable(true);
		frame.setVisible(true);
		frame.setBackground(new Color(250, 246, 207)); 
	}
	
	// this just gets the user input from the text box
	public void write() {
		if (!typeArea.getText().trim().equals("")) { // only works when the box is not empty
			text = typeArea.getText().trim(); // gets rid of spaces
			entered = true; // make sure its not empty
		}
		typeArea.setText(""); // empties the text box
	}
	
	// this function will add the stuff that needs to be displayed in to display text in a way 
	// that fits within one line of the screen
	// we can simply put in a string that we want to print and it will get it into the right format
	// i could probably do a recursive here but that requires too much brain power
	public void paintString(String str) {
		int lines = str.length()/numberOfChar; // finds the number of lines that the input will require (-1 most likely)
		int start = 0;
		for(int i = 0; i < lines; i++) { 
			if(numberOfChar*(i+1) < str.length()) {
				start = findLastSpace(str, numberOfChar*(i+1)); // since the end of the line might be between a word, so we pass the string and the current location to another function
				displayText.add(str.substring(numberOfChar*i, start)); // add that piece of text to substring
			}
		}
		// if the input string had more than one line, then that means we need to skip one character to start the next line with a letter rather than a space
		// i could be just running one more location in the findlastspace but why not use a if statement
		if(lines > 0 && str.length()%numberOfChar != 0) {
			displayText.add(str.substring(start+1));
		}
		// happens if there is only one line worth of input
		else {
			displayText.add(str.substring(start));
		}
	}
	
	// literally find the last occurrence of a space in the input and returns it
	public int findLastSpace(String str, int currLoc) {
		for(int i = currLoc; i >0; i--) {
			if(str.charAt(i) == ' ') {
				return i;
			}
		}
		return currLoc;
	}
	
	// this function reads in the files
	public void MapGeneration(String actorsFile, String moviesFile, String actorsMoviesMap) {
		BufferedReader reader;
		try {
			String line = "";
			String[] arr;
			reader = new BufferedReader(new FileReader(actorsFile));
			while((line =reader.readLine()) != null) {
				arr = line.split("~"); // since the index and actor name is separated by a '~' we split it
				actorsMap.put(arr[0], arr[1]); // maps the index to the name i
				graph.addVertex(arr[1]); // add it to a part of our graph
			}
			//System.out.println(actorsMap.size()); // this can check the number actors in map
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			// similar to the previous reader, we spliting it, putting in a map, and add it to a grpah
			reader = new BufferedReader(new FileReader(moviesFile));
			String line = "";
			String[] arr;
			while((line =reader.readLine()) != null) {
				arr = line.split("~");
				moviesMap.put(arr[0], arr[1]);
			}
			//System.out.println(moviesMap.size()); // checks the number of movies
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			// similar to the previous two
			reader = new BufferedReader(new FileReader(actorsMoviesMap));
			String[] arr = reader.readLine().split("~");
			String[] pre = {"",""};
			ArrayList<String[]> arrayList = new ArrayList<String[]>();
			String line = "";
			int connections = 0;
			while((line =reader.readLine()) != null) {
				arr = line.split("~");
				// we are essentially finding all of the connections from the same movie and connecting the current instance with all previous instances of the movie
				if(arr[0].equals(pre[0])) { // if the movie did not change from before
					for(int i = 0; i < arrayList.size(); i++) { // for all of the previous actors in the map that is in the same movie, connect them
						graph.connect(actorsMap.get(arr[1]), actorsMap.get(arrayList.get(i)[1]), moviesMap.get(arr[0]));
						connections++; // used to test the number of connections
					}
					arrayList.add(arr); // add the new actor (its arr) into this arrayList
				}
				else { // if we are at a new movie, we will make a new arrayList, and add the new actor's arr to it
					arrayList = new ArrayList<String[]>();
					arrayList.add(arr);
				}
				pre = arr; // set the newest movie to the current arr
			}
			//System.out.println(connections); // use this to check the number of connections
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	// this function is able to fine the average connectivity of a actor, which is the average of distances to all other actors
	public double averageConnectivity(String actor) {
		try {
			return graph.averageConnectivity(actor); // runs the averageConnectivity in the LabeledGraph
		}
		catch(NullPointerException e){ // returns string if there is a problem with user input
			paintString(actor + " does not exist, please enter a new actor.");
			String out = findSimilarActor(actor); // runs to find an actor that has a similar name with the user input
			if(!out.isEmpty()) // if there is no similar actor, just don't print
			paintString("You might be looking for: " + out);
		}
		return 0; // the function will return 0 when it did not run properly, so this tells the print functions not to print
	}
	
	// finds the number of actors within a distance of a actor
	public int withInDistance(String actor, int distance) {
		try {
			return graph.findWithinDistance(actor, distance); // runs the function in LabeledGraph
		}
		catch(NullPointerException e) {
			// similar to the last function
			paintString(actor + " does not exist, please enter a new actor with an integer separated by a comma");
			String out = findSimilarActor(actor);
			if(!out.isEmpty()) { // same thing as the previous function
				paintString("You might be looking for: " + out);
			}
		}
		return -1;
	}
	
	public String findBestFriend(String actor) {
		try {
			ArrayList output = graph.findBestFriend(actor); // this will store all of the friends
			String out = "";
			
			// get the output into a string that looks ok
			for(int i = 0; i < output.size()-1; i++) {
				out += output.get(i);
				out += ", "; 
			}
			// with the end that has an and before it
			if(output.size() > 1) {
				out += " and " + output.get(output.size()-1);
			}
			else {
				out += output.get(output.size()-1);
			}
			return out;
		}
		catch(NullPointerException e) {
			// if this does not work, then find similar actor
			paintString(actor + " does not exist.");
			String out= findSimilarActor(actor);
			if(!out.isEmpty()) {
				paintString("You might be looking for: " + out);
			}
		}
		return ""; // tells the program that it did not run
	}
	
	// similar to the previous functions, runs BFS in labeledGraph
	public int BFS(String actor1, String actor2){
		try {
			return graph.findPath(actor1, actor2);
		}
		catch(NullPointerException e) {
			// same thing as the previous function just print something when the actor 
			paintString(actor1 + " or " + actor2 + " does not exist.");
			String out = findSimilarActor(actor1);
			out += findSimilarActor(actor2);
			if(!out.isEmpty()) {
				paintString("You might be looking for: " + out);
			}
		}
		return -1; // tells program that the actors did not exist
	}
	
	// finds actors that has the two 
	public String findSimilarActor(String actor) {
		String output = "";
		for(String actors:actorsMap.keySet()) {
			String[] arr = actorsMap.get(actors).split(" ");
			String[] a = actor.split(" ");
			if(a.length > 1) { // makes sure that the user input is at least 2 words
				if(arr.length > 1 && arr[0].length() > 1 && arr[1].length() > 1) {
					// find other actors that have the same two beginning letters as the input
					if(a[0].substring(0,2).equals(arr[0].substring(0,2)) && a[1].substring(0,2).equals(arr[1].substring(0,2))) {
						output += actorsMap.get(actors) + "  "; 
					}
				}
			}
			else {
				paintString("Your input needs to be at least two different words!");
			}
		}
		return output; 
	}
	
	public static void main(String[] args) {
		KevinBaconGame run = new KevinBaconGame();
		run.MapGeneration("actors.txt", "movies.txt", "movie-actors.txt"); // generates the map
	}

}
