package application;

import java.util.ArrayList;

public class Vertex{
	private String authorName;
	private Boolean isMainAuthor;
	private ArrayList<String> articles = new ArrayList<>();
	private Integer authorID;
	private String orcID;
	private ArrayList<Edge> edges = new ArrayList<>() ;
	private ArrayList<Vertex> coAuthors = new ArrayList<>();
	
	public Vertex() {
		this.orcID = "";
	};
	
	public Vertex(String authorName, Integer authorID, boolean isMainAuthor){
		this.authorName = authorName;
		this.authorID = authorID;
		this.edges = new ArrayList<Edge>();
		this.coAuthors = new ArrayList<Vertex>();
		this.articles = new ArrayList<String>();
		this.isMainAuthor = isMainAuthor;
	}
	
	public void addEdge(Vertex endV, Integer weight) {
		this.edges.add(new Edge(this, endV, weight));
	}
	
	public String getauthorName() {
		return this.authorName;
	}
	
	public Boolean getIsMainAuthor() {
		return isMainAuthor;
	}
	
	public String getorcID() {
		return this.orcID;
	}
	
	public Integer getauthorID() {
		return this.authorID;
	}
	
	public ArrayList<Edge> getEdges(){
		return this.edges;
	}
	
	public ArrayList<Vertex> getCoAuthors(){
		return this.coAuthors;
	}
	
	public ArrayList<String> getArticles(){
		return this.articles;
	}
	
	public void addArticle(String article) {
		articles.add(article);
	}
	
	public void addCoauthor(Vertex coAuthor) {
		coAuthors.add(coAuthor);
	}
	
	@Override
	public String toString() {
		return this.authorName;
	}
	
	///Opsiyonel
	
	public void print() {
		String message = "";
		
		if (this.edges.size() == 0) {
			System.out.println(this.authorName + " -->");
			return;
		}
		
		for(int i = 0; i < this.edges.size(); i++) {
			if (i == 0) {
				message += this.edges.get(i).getStartV().authorName + " -->  ";
			}

			message += this.edges.get(i).getEndV().authorName;
 
			message += " (" + this.edges.get(i).getWeight() + ")";
			
			if (i != this.edges.size() - 1) {
				message += ", ";
			}
		}
		System.out.println(message);
	}													
}
