package application;

import java.util.*;

public class Graph {
    private ArrayList<Vertex> vertices = new ArrayList<>();
    private ArrayList<String> authors = new ArrayList<>();
    private ArrayList<String> orcIDs = new ArrayList<>();
    
    public Graph() {
    	this.vertices = new ArrayList<Vertex>();
    	this.authors = new ArrayList<String>();
    	this.orcIDs = new ArrayList<String>();
    }
    
    public Vertex addVertex(String authorName, Integer authorID, boolean isMainAuthor) {
    	Vertex newVertex = new Vertex(authorName, authorID, isMainAuthor);
    	this.vertices.add(newVertex);
    	return newVertex;
    }
    
    public void addAuthor(String authorName, String orcID) {
    	this.authors.add(authorName);
    	this.orcIDs.add(orcID);
    }
    
    public void addEdge(Vertex vertex1, Vertex vertex2, Integer weight) {
    	vertex1.addEdge(vertex2, weight);
    	vertex2.addEdge(vertex1, weight);
    }
    
    public ArrayList<Vertex> getVertices() {
    	return this.vertices;
    }
    
    public ArrayList<String> getAuthors(){
    	return this.authors;
    }
    
    public ArrayList<String> getOrcIDs(){
    	return this.orcIDs;
    }

    
    public void print() {
    	for(Vertex v : this.vertices)
    		v.print();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String author : authors) {
            sb.append(author.toString()).append("\n");
        }
        return sb.toString();
    }    
}