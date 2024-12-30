package application;

import java.util.LinkedList;

class Queue {
    private LinkedList<Vertex> list;

    public Queue() {
        list = new LinkedList<>();
    }

    public void enqueue(Vertex vertex) {
        list.add(vertex);
        
        //Bubble Sort
        for(int k = 0; k < list.size(); k++) {
        	for(int i = 0; i < list.size() - 1; i++) {
        		if(list.get(i).getArticles().size() < list.get(i + 1).getArticles().size()) {
        			Vertex temp;        			
        			temp = list.get(i);
        			list.set(i, list.get(i + 1));
        			list.set(i + 1, temp);
        		}
        	}
        }
    }

    public Vertex dequeue() {
        if (isEmpty()) {
            return null;
        }
        return list.removeFirst();
    }

    public Vertex peek() {
        if (isEmpty()) {
            return null;
        }
        return list.getFirst();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int size() {
        return list.size();
    }

    public String printQueue() {
    	String sonuc = "Kuyruk oluşturuldu:\n";
        for (Vertex v : list) {
            sonuc += v.getauthorName() + " (" + v.getArticles().size() + " articles)\n";
        }
        return sonuc;
    }
}