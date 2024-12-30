package application;

public class Edge {
    private Vertex startV;
    private Vertex endV;
    private Integer weight;

    public Edge(Vertex start, Vertex end, Integer inputWeight) {
        this.startV = start;
        this.endV = end;
        this.weight = inputWeight;
    }
    
    public Vertex getStartV() {
    	return this.startV;
    }
    
    public Vertex getEndV() {
    	return this.endV;
    }
    
    public Integer getWeight() {
    	return this.weight;
    }
    
    @Override
    public String toString() {
    	return startV.getauthorName() + " -> " + endV.getauthorName();
    }
}