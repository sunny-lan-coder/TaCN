package tk.sunnylan.tacn.tst;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.transitions.hamburger.HamburgerSlideCloseTransition;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tk.sunnylan.tacn.gui.taui1.Util;

public class JFoenixTest extends Application {

	public static void main(String[] args) {
		Util.antiAlias();
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		VBox root = new VBox();
		JFXButton btn = new JFXButton("Click me!");
		JFXButton btn2 = new JFXButton("Click me2");
		btn.getStyleClass().add("button-raised");
		btn2.getStyleClass().add("button-raised");
		JFXHamburger h1 = new JFXHamburger();
		FadeTransition fadeTrans = new FadeTransition(Duration.millis(100), btn);
		// fadeTrans.setAutoReverse(true);
		fadeTrans.setFromValue(1.0);
		fadeTrans.setToValue(0.1);
		HamburgerSlideCloseTransition burgerTask = new HamburgerSlideCloseTransition(h1);
		burgerTask.setRate(-1);
		fadeTrans.setRate(-1);
		btn2.opacityProperty().bind(btn.opacityProperty().multiply(-1).add(1.1));
		h1.addEventHandler(MouseEvent.MOUSE_PRESSED, (e) -> {
			burgerTask.setRate(burgerTask.getRate() * -1);
			fadeTrans.setRate(fadeTrans.getRate() * -1);
			burgerTask.play();
			fadeTrans.play();
		});

		root.getChildren().add(h1);
		root.getChildren().add(btn);
		root.getChildren().add(btn2);
		root.setPadding(new Insets(50, 50, 50, 50));
		root.setSpacing(50);
		Scene myScene = new Scene(root, 640, 480);

		myScene.getStylesheets().add("res/tmp/style.css");
		primaryStage.setScene(myScene);
		primaryStage.show();
	}

}
