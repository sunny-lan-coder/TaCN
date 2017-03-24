package tk.sunnylan.tacn.gui.taui1;

import com.jfoenix.controls.JFXTreeTableView;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import tk.sunnylan.tacn.gui.taui1.SubjectView.AssignmentWrapper;

public class SubjectViewController {

	@FXML
	public JFXTreeTableView<AssignmentWrapper> tableMarks;

	@FXML
	public StackPane summaryPane;

	@FXML
	public void initialize() {

	}
}
