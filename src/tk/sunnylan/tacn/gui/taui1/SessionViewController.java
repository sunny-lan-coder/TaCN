package tk.sunnylan.tacn.gui.taui1;

import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.transitions.hamburger.HamburgerBackArrowBasicTransition;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SessionViewController {

	@FXML
	public JFXDrawer courseDrawer;

	@FXML
	public JFXHamburger hamOpenMenu;

	@FXML
	public StackPane contentPane;

    @FXML
    public JFXTextField txtSessionName;

	@FXML
	public VBox vboxLinks;

	@FXML
	private VBox vboxDrawer;

	@FXML
	public StackPane stackMenuItems;

	@FXML
	public JFXRadioButton radioSync;

	@FXML
	public JFXRadioButton radioStoreCreds;

	@FXML
	public JFXRadioButton radioStoreOffline;

	private HamburgerBackArrowBasicTransition burgerTask;

	@FXML
	public void initialize() {
		courseDrawer.setSidePane(vboxDrawer);
		burgerTask = new HamburgerBackArrowBasicTransition(hamOpenMenu);
		burgerTask.setRate(-2);
		Util.getDrawerTransition(courseDrawer).setRate(2);
		courseDrawer.open();
//		System.out.println("controller initalize");
	}

	@FXML
	void handleHamburgerClick(MouseEvent event) {
		if (event.getEventType() == MouseEvent.MOUSE_PRESSED)
			burgerTask.setRate(burgerTask.getRate() * -1);
		burgerTask.play();
		if (courseDrawer.isShown()) {
			courseDrawer.close();
		} else {
			courseDrawer.open();
		}
	}
}
