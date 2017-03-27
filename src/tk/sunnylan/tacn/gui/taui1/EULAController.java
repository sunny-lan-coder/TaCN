package tk.sunnylan.tacn.gui.taui1;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class EULAController {

    @FXML
    private Label lblConditions;

    @FXML
    public JFXButton btnAccept;
    
    @FXML
    public void initialize(){
    	p("Terms and Conditions:");
    	p("By clicking 'I Accept', you agree to the following Terms and conditions\n");
    }
    
    private void p(String s){
    	lblConditions.setText(lblConditions.getText()+s+"\n");
    }

}
