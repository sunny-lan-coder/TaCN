package tk.sunnylan.tacn.gui.taui1;

import com.jfoenix.controls.JFXTreeTableView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import tk.sunnylan.tacn.gui.taui1.SummaryView.WeightWrapper;

public class SummaryViewController {

    @FXML
    public JFXTreeTableView<WeightWrapper> tableWeights;

    @FXML
    public Label lblAverage;

}
