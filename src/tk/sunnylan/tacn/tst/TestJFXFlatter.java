package tk.sunnylan.tacn.tst;

import com.guigarage.flatterfx.FlatterFX;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TestJFXFlatter extends Application {

	public static void main(String[] args) {
		tk.sunnylan.tacn.gui.taui1.Util.antiAlias();
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Stuart is powerful");

		Button btn = new Button();
		btn.setText("Go to scene 2");
		StackPane pain = new StackPane();
		pain.getChildren().add(btn);
		final Scene scene1 = new Scene(pain, 640, 480);

		Button randBtn = new Button();
		randBtn.setText("Useless");

		Button btn2 = new Button();
		btn2.setText("Go to scene 1");
		HBox pain2 = new HBox();
		pain2.getChildren().add(btn2);
		pain2.getChildren().add(randBtn);
		pain2.setPadding(new Insets(10));
		pain2.setSpacing(10);
		final Scene scene2 = new Scene(pain2, 640, 400);
		
		ProgressBar bar=new ProgressBar();
		bar.setPrefWidth(Double.MAX_VALUE);
		Slider slide=new Slider();
		slide.setPrefWidth(Double.MAX_VALUE);
		bar.progressProperty().bind(slide.valueProperty().divide(slide.getMax()));
		VBox pain3=new VBox();
		pain3.getChildren().add(bar);
		pain3.getChildren().add(slide);
		pain3.setPadding(new Insets(10));
		pain3.setSpacing(10);
		final Scene scene3=new Scene(pain3,640,480);

		scene1.getStylesheets().add("res/tmp/style.css");
		scene2.getStylesheets().add("res/tmp/style.css");
		scene3.getStylesheets().add("res/tmp/style.css");

		btn.setOnAction(e -> primaryStage.setScene(scene2));
		btn2.setOnAction(e -> primaryStage.setScene(scene1));
		randBtn.setOnAction(e -> primaryStage.setScene(scene3));
		
		primaryStage.setScene(scene1);

		primaryStage.show();
		FlatterFX.style();
	}

}
