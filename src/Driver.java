import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
/**
 * This class examines Fitts's law by providing the user with 10 targets
 * of different widths that have to be clicked as fast as possible. According to 
 * Fitts's Law, the time required to quickly move to a target area is a function of the ratio
 * between the distance to the target and the width of the target (Wikipedia). After completing
 * the task the user is presented with a line chart of time taken to click a target vs the index
 * of difficulty of the target (= log base 2 of (target_distance/target_width +1)).
 * @author Nicolae Turcan
 *
 */
public class Driver extends Application{
	// List that stores the time taken to click each target
	ArrayList<Long> times = new ArrayList<Long>();
	/* List that stores the times taken to click each target relative to 
 	the previous one (time taken from clicking one target to clicking another one) */
	ArrayList<Long> relativeTimes = new ArrayList<Long>();
	// Lists for the coordinates of the targets
	ArrayList<Double> xCoordinates = new ArrayList<Double>();
	ArrayList<Double> yCoordinates = new ArrayList<Double>();
	// List that will store the distances between targets
	ArrayList<Double> distances = new ArrayList<Double>();
	// List that will store the widths of each target
	ArrayList<Double> widths = new ArrayList<Double>();
	// List that will store the difficulty indices for each target 
	ArrayList<Double> indexOfDifficulty = new ArrayList<Double>();
	// Number of failures
	int failures = 0;
	// Apparent width of each target, will be added to the widths list
	double apparentWidth = 0;
	// Number of successful target clicks
	int trials = 0;
	public static void main(String[] args) {
		launch(args);
	}
	/**
	 * Method that displays the instructions, targets, and the final results
	 *  in the form of a line chart
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		// Button that triggers the beginning of the test
		Button btn = new Button("Click to start");
		// Button size
		btn.setLayoutX(400);
		btn.setLayoutY(300);
		btn.setPrefWidth(200);
		btn.setPrefHeight(100);
		Pane root = new Pane();
		root.getChildren().add(btn);
		// Sets sizes of the scene
		Scene scene = new Scene(root, 1000, 700);
		// If the target was missed (user clicked outside the target box)
		scene.setOnMousePressed(new EventHandler<MouseEvent>(){

			@Override
			public void handle(MouseEvent event) {
				// Prints to the console when the user doesn't click the target area
				System.out.println("clicked outside");
				// Increments the number of failures
				failures++;
			}
		});
		primaryStage.setScene(scene);
		primaryStage.setTitle("Fitts's Law Experiment");
		// Shows the stage
		primaryStage.show();

		// Alert box instructing the user on what to do
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Information Dialog");
		alert.setHeaderText(null);
		alert.setContentText("After clicking start, you will be presented with 10"
				+ " targets that you will have to click as fast as possible. Then,"
				+ " based on your performance, your accuracy will be displayed along with a graph.");
		// Shows alert and waits for user input
		alert.showAndWait();
		// If target was clicked
		btn.setOnMouseClicked(new EventHandler<MouseEvent>(){

			@Override
			public void handle(MouseEvent event) {
				// If there are trials left
				if (trials != 10){
					trials++;
					// Updates the target text
					btn.setText("Target " + trials);
					// Adds the time taken to click the target to the times ArrayList
					times.add(System.currentTimeMillis());
					if (times.size() > 1){
						/* Calculates the time for clicking each target after the first target by subtracting the time taken for
				   		the previous target from present time and puts it in the relativeTimes ArrayList */
						relativeTimes.add(times.get(times.size()-1) - times.get(times.size()-2));
					}
					// Adds x and y coordinates of the target
					xCoordinates.add(event.getSceneX());
					yCoordinates.add(event.getSceneY());
					// Sets a random location for the next target within some bounds
					btn.setLayoutX(ThreadLocalRandom.current().nextInt(1, 800));
					btn.setLayoutY(ThreadLocalRandom.current().nextInt(1, 630));
					// Height of the button
					btn.setPrefHeight(50);
					// Removes the target
					root.getChildren().remove(btn);
					// Sets a random width to the next target within some bounds
					apparentWidth = ThreadLocalRandom.current().nextInt(70, 200);
					// Prints out the width of the target
					System.out.println(apparentWidth);
					btn.setPrefWidth(apparentWidth);
					// Adds the width of the target to the array
					widths.add(apparentWidth);
					root.getChildren().add(btn);
				}
				// If all trials completed
				else{
					// Add the time and the coordinates of the last target
					times.add(System.currentTimeMillis());
					relativeTimes.add(times.get(times.size()-1) - times.get(times.size()-2));
					xCoordinates.add(event.getSceneX());
					yCoordinates.add(event.getSceneY());
					// Remove the last button
					root.getChildren().remove(btn);
					// Calculates the distances between each target and puts them into the distances ArrayList
					for (int i=1; i<xCoordinates.size();  i++){
						distances.add(Math.sqrt(Math.pow(xCoordinates.get(i)-xCoordinates.get(i-1), 2) +Math.pow(yCoordinates.get(i)-yCoordinates.get(i-1), 2)));
					}
					// Calculates the indexOfDifficulty using the formula indexOfDifficulty = log base 2 of (distance/width +1)
					for (int i=0; i<distances.size(); i++){
						indexOfDifficulty.add(Math.log(distances.get(i)/widths.get(i)+1)/Math.log(2));
						// Prints out the difficulty index
						System.out.println(indexOfDifficulty.get(i));
					}
					// Sets the x axis that will represent the index of Difficulty
					NumberAxis xAxis = new NumberAxis();
					xAxis.setLabel("Index of Difficulty");
					// Sets they y axis that will represent the time taken to click each target
					NumberAxis yAxis = new NumberAxis();
					yAxis.setLabel("Time");

					// Creates a line chart with x and y axes
					LineChart<Number, Number> line = new LineChart<Number, Number>(xAxis, yAxis);
					line.setTitle("Line Chart");
					XYChart.Series<Number, Number> data = new XYChart.Series<>();
					// Supplies data to the axes
					for (int i=0; i<relativeTimes.size(); i++){
						data.getData().add(new XYChart.Data<Number, Number>(indexOfDifficulty.get(i), relativeTimes.get(i)));
					}
					// Creates a line chart
					line.getData().add(data);
					// Prepares the stage for the line chart
					primaryStage.setWidth(600);
					primaryStage.setHeight(450);
					root.getChildren().add(line);
					primaryStage.setScene(scene);
					// Shows the chart
					primaryStage.show();
					// Notifies the user about the accuracy percentage on the test
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Information Dialog");
					alert.setHeaderText(null);
					// Calculates the accuracy percentage
					float a = (float)10/(10+failures)*100;
					System.out.println(a);
					alert.setContentText("Your accuracy was: " + a + "%");
					// Displays the alert
					alert.showAndWait();
				}
			}
		});
	}
}
