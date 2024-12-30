package application;

import javafx.application.Application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class GraphVisualizer extends Application {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    
    @Override
    public void start(Stage primaryStage) {
                      
        String filePath = "files/dataset.xlsx";
        
        Graph graph = new Graph();
        
        ArrayList<Vertex> mainAuthorsList = new ArrayList<>();
        HashMap<Vertex, double[]> vertexPositions = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            int id = 0;
            
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                                              
                String orcid = getCellValue(row.getCell(0));
                String name = getCellValue(row.getCell(3));
                String article = getCellValue(row.getCell(5));
                
                Vertex v1 = new Vertex();
                
                if(graph.getOrcIDs().contains(orcid)) {
                	int authorIndex1 = graph.getOrcIDs().indexOf(orcid);
                	v1 = graph.getVertices().get(authorIndex1);
                	v1.addArticle(article);
                }
                else {
                	v1 = graph.addVertex(name, id, true);
                	graph.addAuthor(name, orcid);
                	id += 1;
                	mainAuthorsList.add(v1);
                	v1.addArticle(article);
                }                               
            }
            
            System.out.println("OrcID ler: " + graph.getOrcIDs());
            
        for(Row row : sheet) {
        	if (row.getRowNum() == 0) continue;
        	
        	String orcid = getCellValue(row.getCell(0));
            String name = getCellValue(row.getCell(3));
            String article = getCellValue(row.getCell(5));
            String coAuthors = getCellValue(row.getCell(4));
            
            Vertex v1 = graph.getVertices().get(graph.getOrcIDs().indexOf(orcid));                                   
        	
        	String[] authorsArray = coAuthors.substring(1, coAuthors.length() - 1).split(","); // Remove the brackets
            
            
            for (String author : authorsArray) {
                String cleanedAuthor = author.trim().replace("'", "");
                
                Vertex v2;
                if (!cleanedAuthor.equals(name)) {
                	if (graph.getAuthors().contains(cleanedAuthor)) {
                        
                        int coAuthorIndex = graph.getAuthors().indexOf(cleanedAuthor);
                        v2 = graph.getVertices().get(coAuthorIndex);
                    } else {
                        v2 = graph.addVertex(cleanedAuthor, id, false);
                        graph.addAuthor(cleanedAuthor, "");
                        v2.addArticle(article);
                        id += 1;
                    }
                	if(!v1.getCoAuthors().contains(v2)) {
                		v1.addCoauthor(v2);
                		v2.addCoauthor(v1);
                		graph.addEdge(v1, v2, 1);
                		graph.addEdge(v2, v1, 1);
                	}
                }
            }
        	
        }
        } catch (FileNotFoundException e) {        	
        	System.err.println("Dosya bulunamadı:");
        	e.printStackTrace();
        } catch (IOException e) {
        	System.err.println("Dosya okuma hatası tespit edildi:");
            e.printStackTrace();
        }        
        
        Double articleCount = 0.0;
        
        for(Vertex vertice : graph.getVertices()){
        	articleCount += vertice.getArticles().size();
        }
        
        double averageArticleCount = articleCount / graph.getVertices().size();
        
        Pane pane = new Pane();
        
        drawGraph(pane, graph, averageArticleCount, vertexPositions);
        
        Group group = new Group(pane);
        
        
        //Zoom in-out özelliği
        group.setOnScroll(event -> {
    	    if (event.getDeltaY() == 0) return;

    	    double scaleFactor = (event.getDeltaY() > 0) ? 1.1 : 0.9;

    	    group.setScaleX(group.getScaleX() * scaleFactor);
    	    group.setScaleY(group.getScaleY() * scaleFactor);

    	    event.consume();
    	});
        
        //Kaydırma özelliği
        final double[] mouseAnchorX = new double[1];
        final double[] mouseAnchorY = new double[1];
        final double[] initialTranslateX = new double[1];
        final double[] initialTranslateY = new double[1];

        group.setOnMousePressed(event -> {
            mouseAnchorX[0] = event.getSceneX();
            mouseAnchorY[0] = event.getSceneY();
            initialTranslateX[0] = group.getTranslateX();
            initialTranslateY[0] = group.getTranslateY();
        });

        group.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - mouseAnchorX[0];
            double deltaY = event.getSceneY() - mouseAnchorY[0];

            group.setTranslateX(initialTranslateX[0] + deltaX);
            group.setTranslateY(initialTranslateY[0] + deltaY);
        });

        Scene scene = new Scene(group, WINDOW_WIDTH, WINDOW_HEIGHT);
        
        //R ye basınca grafın ortasına atma
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.R) {
                group.setScaleX(1.0);
                group.setScaleY(1.0);
                group.setTranslateX(-50000);
                group.setTranslateY(-50000);
            }
        });
        primaryStage.setMaximized(true);
        primaryStage.setTitle("Graph Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        Stage stage = new Stage();
        
        BorderPane layout = new BorderPane();

        TextArea outputArea = new TextArea("Çıktı Ekranı");
        outputArea.setPrefWidth(600);
        outputArea.setEditable(false);
        layout.setLeft(outputArea);

        
        VBox buttonBox = new VBox(10);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: black;");
        buttonBox.setPrefWidth(150);
        
        Button button1 = new Button("1. ISTER");
        button1.setPrefSize(130, 100);
        button1.setOnAction(event ->{
        	//kisaYolBulma(graph);
        });
        
        Button button2 = new Button("2. ISTER");
        button2.setPrefSize(130, 100);
        button2.setOnAction(event ->{
        	Stage inputStage = new Stage();
        	inputStage.setTitle("Yazar ID si girin");
        	inputStage.setResizable(false);
        	
        	TextField inputField = new TextField();
        	inputField.setMaxWidth(100);
        	
        	Button okButton = new Button("Tamam");
        	
        	okButton.setOnAction(e ->{
        		int alinanID = -1;
        		try {
					alinanID = Integer.parseInt(inputField.getText());
				} catch (NumberFormatException e1) {
					outputArea.setText("Lütfen geçerli bir değer girin");
				}
    			if(alinanID >= graph.getVertices().size() || alinanID < 0) {
    				outputArea.setText("Lütfen geçerli bir değer girin");  				   				
    			}
    			else {

    	            Vertex mainAuthor = graph.getVertices().get(alinanID);
    	            
    	            Queue queue = new Queue();
    	            
    	            queue.enqueue(mainAuthor);
    	            
    	            for (Vertex vertex : mainAuthor.getCoAuthors()) {
    	                queue.enqueue(vertex);
    	            }   	            
    	            outputArea.setText(queue.printQueue());
    			}
        	});
        	VBox layout1 = new VBox(10, inputField, okButton);
            layout1.setAlignment(Pos.CENTER);
            
            Scene scene1 = new Scene(layout1, 300, 100);

            inputStage.setScene(scene1);
            inputStage.show();
        });
        
        Button button3 = new Button("3. ISTER");
        button3.setPrefSize(130, 100);
        button3.setOnAction(event ->{
        	//bstOlusturma(graph);
        });
        
        Button button4 = new Button("4. ISTER");
        button4.setPrefSize(130, 100);
        button4.setOnAction(event ->{
        	//yazarYollari(graph);
        });
        
        Button button5 = new Button("5. ISTER");
        button5.setPrefSize(130, 100);
        button5.setOnAction(event ->{
        	Stage inputStage = new Stage();
        	inputStage.setTitle("Yazar ID si girin");
        	inputStage.setResizable(false);
        	
        	TextField inputField = new TextField();
        	inputField.setMaxWidth(100);
        	
        	Button okButton = new Button("Tamam");
        	
        	okButton.setOnAction(e ->{
        		int alinanID = -1;
        		try {
					alinanID = Integer.parseInt(inputField.getText());
				} catch (NumberFormatException e1) {
					outputArea.setText("Lütfen geçerli bir değer girin");
				}
    			if(alinanID >= graph.getVertices().size() || alinanID < 0) {
    				outputArea.setText("Lütfen geçerli bir değer girin");  				   				
    			}
    			else {
    				Vertex vv = graph.getVertices().get(alinanID);
        			outputArea.setText(vv.getauthorName() + " adlı yazarın işbirlikçi yazar sayısı: " + vv.getCoAuthors().size());
    			}
        	});
        	VBox layout1 = new VBox(10, inputField, okButton);
            layout1.setAlignment(Pos.CENTER);

            Scene scene1 = new Scene(layout1, 300, 100);

            inputStage.setScene(scene1);
            inputStage.show();
        });
        
        Button button6 = new Button("6. ISTER");
        button6.setPrefSize(130, 100);
        button6.setOnAction(event ->{
        	outputArea.setText(maxCoauthorBulma(graph));
        	
        });
        
        Button button7 = new Button("7. ISTER");
        button7.setPrefSize(130, 100);
        button7.setOnAction(event ->{
        	Stage inputStage = new Stage();
        	inputStage.setTitle("Yazar ID si girin");
        	inputStage.setResizable(false);
        	
        	TextField inputField = new TextField();
        	inputField.setMaxWidth(100);
        	
        	Button okButton = new Button("Tamam");
        	Button temizleButton = new Button("Grafı temizle");
        	
        	okButton.setOnAction(e ->{
        		int alinanID = Integer.parseInt(inputField.getText());
    			
    			Vertex start = graph.getVertices().get(alinanID);
    			
    			ArrayList<Vertex> visitedVertices = new ArrayList<>();
    		    ArrayList<Vertex> currentPath = new ArrayList<>();
    		    ArrayList<Vertex> longestPath = new ArrayList<>();

    		    visitedVertices.add(start);
    		    yazarEnUzunYol(start, visitedVertices, currentPath, longestPath);

    		    String sonuc = "En uzun yol:\n";
    		    for (Vertex v : longestPath) {
    		        sonuc += v.getauthorName() +"("+ v.getauthorID() + ")"+ "\n";
    		    }
    		    sonuc += "Yol uzunluğu: " + longestPath.size();
    		    
    		    outputArea.setText(sonuc);
    		    
    		    pane.getChildren().clear();
    		    drawGraph(pane, graph, averageArticleCount, vertexPositions);
    		    highlightPath(pane, longestPath, vertexPositions);
        	});
        	
        	temizleButton.setOnAction(e ->{
        		pane.getChildren().clear();
        		drawGraph(pane, graph, averageArticleCount, vertexPositions);
        	});
        	
        	VBox layout1 = new VBox(10, inputField, okButton, temizleButton);
            layout1.setAlignment(Pos.CENTER);

            Scene scene1 = new Scene(layout1, 300, 100);

            inputStage.setScene(scene1);
            inputStage.show();
        });
        
        
        buttonBox.getChildren().addAll(button1,button2,button3,button4,button5,button6,button7);

        layout.setRight(buttonBox);
        
        Scene scene1 = new Scene(layout, 800, 600);
        stage.setTitle("Buton penceresi");
        stage.setScene(scene1);
        stage.show();
    }
    
    public void highlightPath(Pane pane, List<Vertex> longestPath, HashMap<Vertex, double[]> vertexPositions) {
        
        for (int i = 0; i < longestPath.size() - 1; i++) {
            Vertex start = longestPath.get(i);
            Vertex end = longestPath.get(i + 1);

            double[] startPos = vertexPositions.get(start);
            double[] endPos = vertexPositions.get(end);
            
            Line line = new Line(startPos[0], startPos[1], endPos[0], endPos[1]);
            line.setStroke(Color.RED);
            line.setStrokeWidth(10.0);
            pane.getChildren().add(line);
        }

        for (Vertex vertex : longestPath) {
            double[] pos = vertexPositions.get(vertex);
            Circle circle = new Circle(pos[0], pos[1], 15);
            circle.setFill(Color.YELLOW);
            circle.setStroke(Color.RED);
            circle.setStrokeWidth(2);
            pane.getChildren().add(circle);
        }
    }

    
    private String maxCoauthorBulma(Graph graph){
    	Vertex v3 = new Vertex();
        int maxCoAuthorCount = 0;

        for (Vertex vertex : graph.getVertices()) {
            if (vertex.getCoAuthors().size() > maxCoAuthorCount) {
                maxCoAuthorCount = vertex.getCoAuthors().size();
                v3 = vertex;
            }
        }
        
        return "En fazla işbirlikçi yazara sahip yazar: " + v3.getauthorName() + "(ID:" + v3.getauthorID() + ")\n"
        		+ "İşbirlikçi yazar sayısı: " + maxCoAuthorCount;
        
    }
            
    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        return cell.getStringCellValue();
    }

    private void drawGraph(Pane pane, Graph graph, Double averagePapers, HashMap<Vertex, double[]> vertexPositions) {
        ArrayList<Vertex> vertices = new ArrayList<>();

        for (Vertex vertex : graph.getVertices()) {
            if (vertex.getIsMainAuthor()) {
                vertices.add(vertex);
            }
        }
        
        HashSet<Vertex> drawnVertices = new HashSet<>();
        
        int vertexCount = vertices.size();
        int gridSize = (int) Math.ceil(Math.sqrt(vertexCount));
        int spacing = 4700;
        int startX = 200;
        int startY = 200;

        double[] xPositions = new double[vertexCount];
        double[] yPositions = new double[vertexCount];

        HashMap<Vertex, double[]> authorPositions = new HashMap<>();
                                
        for (int i = 0; i < vertexCount; i++) {
        	
            int row = (i / gridSize) * 2;
            int col = (i % gridSize) * 2;

            double x = startX + col * spacing;
            double y = startY + row * spacing;
                     
            Vertex vertex = vertices.get(i);
            vertexPositions.put(vertex, new double[]{x, y});

            xPositions[i] = x;
            yPositions[i] = y;

            Vertex mainAuthor = vertices.get(i);            

            drawnVertices.add(mainAuthor);
                
            authorPositions.put(mainAuthor, new double[] {x, y});
        }
        
        for (int i = 0; i < vertexCount; i++) {
        	int row = (i / gridSize) * 2;
            int col = (i % gridSize) * 2;

            double x = startX + col * spacing;
            double y = startY + row * spacing;
                     
            Vertex vertex = vertices.get(i);
            vertexPositions.put(vertex, new double[]{x, y});

            xPositions[i] = x;
            yPositions[i] = y;

            Vertex mainAuthor = vertices.get(i);
                     
            drawCoAuthors(vertexPositions, graph, pane, mainAuthor, x, y, 4500, authorPositions, drawnVertices, averagePapers);
        }
        
        for (int i = 0; i < vertexCount; i++) {
        	
        	int row = (i / gridSize) * 2;
            int col = (i % gridSize) * 2;

            double x = startX + col * spacing;
            double y = startY + row * spacing;
                     
            Vertex vertex = vertices.get(i);
            vertexPositions.put(vertex, new double[]{x, y});

            xPositions[i] = x;
            yPositions[i] = y;

            Vertex mainAuthor = vertices.get(i);
                     
            int paperCount = mainAuthor.getArticles().size();
            double size = (paperCount > averagePapers * 1.2) ? 500 : (paperCount < averagePapers * 0.8) ? 250 : 375;
            Color color = (paperCount > averagePapers * 1.2) ? Color.DARKGREEN : (paperCount < averagePapers * 0.8) ? Color.LIGHTGREEN : Color.GREEN;
        	
        	Circle mainCircle = new Circle(x, y, size, color);
            mainCircle.setOnMouseClicked(event -> showAuthorDetails(graph, mainAuthor));
            pane.getChildren().add(mainCircle);

            Text mainText = new Text(x - 175, y, mainAuthor.getauthorName() + "\nID:" + mainAuthor.getauthorID());
            mainText.setStyle("-fx-font-family: Serif; -fx-font-size: "+size / 5+";");
            pane.getChildren().add(mainText);
        }
    }
    
    private void drawCoAuthors(HashMap<Vertex, double[]> vertexPositions, Graph graph, Pane pane, Vertex mainAuthor, double centerX, double centerY, double radius, HashMap<Vertex, double[]> authorPositions, HashSet<Vertex> drawnVertices, Double averagePapers) {
        ArrayList<Vertex> coAuthors = mainAuthor.getCoAuthors();

        int coAuthorCount = coAuthors.size();
        if (coAuthorCount == 0) return;

        double angleIncrement = 2 * Math.PI / coAuthorCount;
        
        List<Line> linesToDraw = new ArrayList<>();
        List<Circle> nodesToDraw = new ArrayList<>();
        List<Text> textToDraw = new ArrayList<>();

        for (int i = 0; i < coAuthorCount; i++) {
            Vertex coAuthor = coAuthors.get(i);
            
            if (drawnVertices.contains(coAuthor)) {
            	double[] existingPosition = authorPositions.get(coAuthor);
            	if (existingPosition != null) {
            		System.out.println("Çalıştı!");
	                Line edgeLine = new Line(centerX, centerY, existingPosition[0], existingPosition[1]);
	                edgeLine.setStroke(Color.GRAY);
	                edgeLine.setStrokeWidth(10.0);
	                linesToDraw.add(edgeLine);
	                continue;
            	}continue;
            }

            int paperCount = coAuthor.getArticles().size();
            double size = (paperCount > averagePapers * 1.2) ? 425 : (paperCount < averagePapers * 0.8) ? 187 : 300;
            Color color = (paperCount > averagePapers * 1.2) ? Color.DARKBLUE : (paperCount < averagePapers * 0.8) ? Color.LIGHTBLUE : Color.BLUE;

            double angle = i * angleIncrement;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            
            Line edgeLine = new Line(centerX, centerY, x, y);
            edgeLine.setStroke(Color.GRAY);
            edgeLine.setStrokeWidth(10.0);
            
            linesToDraw.add(edgeLine);

            authorPositions.put(coAuthor, new double[]{x, y});
            Circle coAuthorCircle = new Circle(x, y, size, color);
            coAuthorCircle.setOnMouseClicked(event -> showAuthorDetails(graph, coAuthor));
            
            nodesToDraw.add(coAuthorCircle);

            Text coAuthorText = new Text(x - 75, y, coAuthor.getauthorName() + "\nID:"+coAuthor.getauthorID());
            coAuthorText.setStyle("-fx-font-family: Serif; -fx-font-size: "+size / 5 +";");
            textToDraw.add(coAuthorText);
            
            drawnVertices.add(coAuthor);
        }
        
        vertexPositions.putAll(authorPositions);
             
        pane.getChildren().addAll(linesToDraw);               

        pane.getChildren().addAll(nodesToDraw);
        
        pane.getChildren().addAll(textToDraw);
    }
    
    private void showAuthorDetails(Graph graph, Vertex vertex) {
    	Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Yazar detayları");
        dialog.setHeaderText("İsim:"+vertex.getauthorName()+"\nYazarın orcID: "+graph.getOrcIDs().get(vertex.getauthorID())+"\nYazarın dahil olduğu makaleler:");

        TextArea textArea = new TextArea(String.join("\n", vertex.getArticles()));
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefWidth(1200);
        textArea.setPrefHeight(300);

        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }
    
    public static void yazarEnUzunYol(Vertex start, ArrayList<Vertex> visitedVertices, 
    	ArrayList<Vertex> currentPath, ArrayList<Vertex> longestPath) {
    	
		currentPath.add(start);
		
		
		if (currentPath.size() > longestPath.size()) {
			longestPath.clear();
			longestPath.addAll(currentPath);
		}
		
		//DFS araması
		for (Edge e : start.getEdges()) {
		Vertex neighbor = e.getEndV();
		
		if (!visitedVertices.contains(neighbor)) {
			visitedVertices.add(neighbor);
			yazarEnUzunYol(neighbor, visitedVertices, currentPath, longestPath);
			visitedVertices.remove(neighbor);
			}
		}
		currentPath.remove(currentPath.size() - 1);
    }

    
    public static void main(String[] args) {
        launch(args);
    }
}