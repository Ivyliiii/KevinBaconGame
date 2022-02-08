
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

// LabeledGraph given by MR.FRIEDMAN
public class LabeledGraph<E, T> {
	
	HashMap<E, Vertex> vertices; 
	
	// this class will store information and a set of edges
	public class Vertex { 
		E info;
		HashSet<Edge> edges;
		
		public Vertex(E info) {
			this.info = info;
			edges = new HashSet<Edge>();
		}
	}

	public LabeledGraph() {
		vertices = new HashMap<E, Vertex>();
	}
	
	// function that puts adds new vertex
	public void addVertex(E info) {
		vertices.put(info, new Vertex(info));
	}
	
	// input information of the two actors and the movie title and this will create an edge
	public void connect(E info1, E info2, T label) {
		Vertex v1 = vertices.get(info1);
		Vertex v2 = vertices.get(info2);
		
		if (v1 == null || v2 == null) {
			return;
		}
		
		Edge e = new Edge(label, v1, v2);
		
		v1.edges.add(e);
		v2.edges.add(e);
	}
	
	// this is a part of the BFS, where it takes in a leadsTo HashMap, the start of the vertex, and the end
	public ArrayList<Vertex> backTrace(HashMap<Vertex, Vertex> leadsTo, Vertex end, Vertex start){
		ArrayList<Vertex> path = new ArrayList<Vertex>();
		HashMap<Vertex, Vertex> map = leadsTo;
		Vertex curr = end;
		while(curr != null) { // since the start was maped to null, we know that we are at the end when we get to null
			path.add(curr); 
			curr = map.get(curr);
		}
		return path; // gives the arraylist of vertexes on the path
	}
	
	public int findPath(E s, E e) {
		ArrayList<Vertex> toVisit = new ArrayList<Vertex>();
		HashMap<Vertex, Vertex> leadsTo = new HashMap<Vertex, Vertex>();
		Vertex start = getVertex(s);
		Vertex end = getVertex(e);	
		toVisit.add(start);
		Vertex curr = toVisit.get(0); //gets the beginning vertex
		leadsTo.put(curr, null);
		while(toVisit.size() > 0) { 
			for(Edge n : curr.edges) { // runs through all of the connections of a vertex first
				if(curr == end) { //when we find it, go to backtrace
					return backTrace(leadsTo, end, start).size();
				}
				if(!leadsTo.containsKey(n.getNeighbor(curr))) { // if there is not year a path that gets to this neighboring vertex, store this path
					toVisit.add(n.getNeighbor(curr)); // we need visit this vertex later
					leadsTo.put(n.getNeighbor(curr), curr); // maps the current vertex with its new neighbor
				}
			}
			curr = toVisit.remove(0); // remove it from the tovisit

		}
		return -1;
	}
	
	// edge will contain the movie that connects two movies
	private class Edge {
		T label;
		Vertex v1, v2;
		
		public Edge(T label, Vertex v1, Vertex v2) {
			this.label = label; this.v1 = v1; this.v2 = v2;
		}
		
		// this will give the other end of an edge
		private Vertex getNeighbor(Vertex v) {
			if (v.info.equals(v1.info)) {
				return v2;
			}
			return v1;
		}
		
	}
	
	// get the vertex based on the information
	public Vertex getVertex(E info){
		return vertices.get(info);
	}
	
	// this function will find a person's best friend
	public ArrayList<E> findBestFriend(E actor) {
		Vertex a = getVertex(actor); // get the vertex based on the actor
		HashMap<Vertex, Integer> arr = new HashMap<Vertex, Integer>(); // this map maps the number of connections of actors who has been in a movie with the person we are looking at
		for(Edge e: a.edges) { // for all edges of the main actor
			if(!arr.containsKey(e.getNeighbor(a))) { // if the actor that we are looking at is new and without connections
				arr.put(e.getNeighbor(a), 1); // add it into the arr with the number of connections right now
			}
			else { // if there are already connections between the neighbors
				int freq = arr.get(e.getNeighbor(a)); // find the current number of connections
				arr.replace(e.getNeighbor(a), freq+1); // based on that, add one to it and put it into the map
			}
		}
		ArrayList<E> out = new ArrayList<E>(); // arraylist of all actors that have the most connections with the actor
		int maxTimes = 0; 
		
		for(Vertex v : arr.keySet()) { // for everything in the arraylist
			if(arr.get(v) > maxTimes) { // if it has more connections than the current max
				maxTimes = arr.get(v); // set the max as the connections this person has
				out = new ArrayList<E>(); // initializes a new arrayList
				out.add(v.info); // add this current vertex to it
			}
			else if(arr.get(v) == maxTimes) {
				out.add(v.info); // if it is equal, you should add the info into part of the output
			}
		}
		return out;
	}
	
	// this function will find the number of actors within a certain distance of the actor
	// this function will use BFS because it will always check the closest actors before going to the further ones
	public int findWithinDistance(E actor, int rounds) {
		ArrayList<HashMap<Vertex, Integer>> toVisit = new ArrayList<HashMap<Vertex, Integer>>(); // this will keep a hashMap because we also want to know the distance of the actor from the main actor
		HashMap<Vertex, Vertex> leadsTo = new HashMap<Vertex, Vertex>(); // this is just part of BFS
		Vertex v = getVertex(actor);
		int actorsFound = 0; // this will keep track of the number of actors found
		HashMap<Vertex, Integer> c = new HashMap<Vertex, Integer>(); // this is the map for the current actor
		c.put(v, 0); // the first actor is 0 distance away from itself
		toVisit.add(c); // we are going to visit it next
		boolean first = true;
		HashMap<Vertex, Integer> curr = toVisit.remove(0); // now it is part of current
		// if we see the first with a distance beyond the distance that we want, it means everything after it would be pointless to check
		while(curr.get(v) < rounds && (toVisit.size() > 0||first)) { // although the size would be zero in the first round, we should still run it
			first = false; // after the first time, first would be false
			for(Edge e:v.edges) { // run through all of the edges
				if(!leadsTo.containsKey(e.getNeighbor(v))) { // if we have not visited this vertex before
					HashMap<Vertex, Integer> temp = new HashMap<Vertex, Integer>(); 
					temp.put(e.getNeighbor(v), curr.get(v)+1); // we make a new hashmap with the new vertex mapped to a distance of +1
					toVisit.add(temp); // add it to toVist
					leadsTo.put(e.getNeighbor(v), v); // include it into leadsTo
					actorsFound++; // increment the number of actors found
				}
			}
			curr = toVisit.remove(0); // move to the next vertex
			for(Vertex ver : curr.keySet()) {
				v = ver; // since there is only one vertex in the keyset, this just gets it. There is definitely another way to find the vertex.
			}
		}
		return actorsFound;
	}
	
	// this function finds the average connectivity of an actor
	public double averageConnectivity(E actor) {
		int currentConnected = 0; 
		int previousConnected = 0;
		double currentConnectivity = 0;
		Vertex v = getVertex(actor);
		// since every actor that is connected with the actor should be within a distance of 100
		double connected  = findWithinDistance(actor, 100);
		int i = 1; 
		while(currentConnected < connected) { // when we have not run through every actor that is connected
			currentConnected = findWithinDistance(actor, i); //we fine the number of actors within a particular distance
			currentConnectivity += i*(currentConnected - previousConnected); // subtract the number of actors from the previous distance and multiply it by the distance, then divide by the number of actors to find the average
			previousConnected = currentConnected;
			i++;
		}
		// we have the average after addinging up all of the average connectivity
		return (double)currentConnectivity/connected;
	}

		
}