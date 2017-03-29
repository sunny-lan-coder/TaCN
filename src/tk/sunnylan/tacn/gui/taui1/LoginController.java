package tk.sunnylan.tacn.gui.taui1;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import tk.sunnylan.tacn.webinterface.jsoup.TASession;

public class LoginController {
	private static Logger logger=Logger.getLogger(LoginController.class.getName());

	@FXML
	public JFXTextField txtStudentID;

	@FXML
	private JFXPasswordField txtPassword;

	@FXML
	private Label lblInvalid;

	@FXML
	private JFXButton btnLogin;

	@FXML
	private JFXSpinner loadingSpinner;

	public ILoginListener loginListener;
	

    @FXML
    private Hyperlink linkCancel;

	@FXML
	public void initialize() {
		btnLogin.setDisable(true);
		loadingSpinner.setOpacity(0);
		lblInvalid.setOpacity(0);
		txtStudentID.textProperty().addListener((s) -> txtChanged());
		txtPassword.textProperty().addListener((s) -> txtChanged());
	}

	@FXML
	void btnLoginClicked(ActionEvent event) {
		btnLogin.setOpacity(0);
		loadingSpinner.setOpacity(1);
		setDisableAll(true);
		new Thread(() -> {

			try {
				TASession client = new TASession(txtStudentID.getText(), txtPassword.getText());
				if (client != null && loginListener != null) {
					Platform.runLater(() -> {
						lblInvalid.setText("");
						lblInvalid.setOpacity(0);
						txtPassword.clear();
						loginListener.successfulLogin(client);
					});
				}
			} catch (Exception e) {
				Platform.runLater(() -> {
					lblInvalid.setText(e.getMessage());
					txtPassword.clear();
					lblInvalid.setOpacity(1);
				});
				logger.log(Level.WARNING, "Login failed", e);
			}
			Platform.runLater(() -> {
				btnLogin.setOpacity(1);
				loadingSpinner.setOpacity(0);
				setDisableAll(false);
			});

		}).start();

	}

	private boolean d = true;

	void txtChanged() {
		btnLogin.setDisable(d && txtPassword.getText().isEmpty() || txtStudentID.getText().isEmpty());
	}

	void setDisableAll(boolean val) {
		btnLogin.setDisable(val);
		txtPassword.setDisable(val);
		txtStudentID.setDisable(val);
		linkCancel.setDisable(val);
		d = !val;
	}

	@FXML
	void keyPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER && !btnLogin.isDisabled())
			btnLogin.fire();
	}

	@FXML
	void linkCancelClicked(ActionEvent event) {
		txtPassword.clear();
		loginListener.loginCancelled();
	}
	
	public void clearCreds(){
		txtPassword.clear();
		txtStudentID.clear();
	}
}
