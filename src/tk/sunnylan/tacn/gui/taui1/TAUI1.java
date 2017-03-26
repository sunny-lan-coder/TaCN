package tk.sunnylan.tacn.gui.taui1;

import java.awt.TrayIcon;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jfoenix.controls.JFXSpinner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import tk.sunnylan.tacn.data.ProfileLoadInfo;
import tk.sunnylan.tacn.tst.CONFIG;
import tk.sunnylan.tacn.webinterface.htmlunit.TALoginClient;

public class TAUI1 extends Application {

	public final String cachepath = new File("").getAbsolutePath() + "\\cache\\";

	private Stage primaryStage;
	private Scene loginScene;
	private LoginController loginController;
	private Scene loadingScene;
	private ProfileSelectionController selectionController;

	public TrayIcon icon;

	private Scene lastScene;

	private HashMap<String, ProfileLoadInfo> profiles;

	private Label empty;

	@Override
	public void start(Stage primaryStage) throws Exception {
		System.out.println("Initializing UI...");
		primaryStage.setTitle("Tyanide");
		primaryStage.setWidth(1280);
		primaryStage.setHeight(720);
		this.primaryStage = primaryStage;
		FXMLLoader mahLoader ;
		Pane mainPane = new StackPane(new JFXSpinner());
		loadingScene = new Scene(mainPane, 1280, 720);
		System.out.println("  Scene 1 loaded");
		
		mahLoader = new FXMLLoader(TAUI1.class.getResource("LoginScreen.fxml"));
		mainPane = mahLoader.load();
		loginScene = new Scene(mainPane, 1280, 720);
		loginController = mahLoader.getController();
		System.out.println("  Scene 2 loaded");

		primaryStage.setScene(loadingScene);
		primaryStage.show();
		mahLoader = new FXMLLoader(TAUI1.class.getResource("ProfileSelectionScreen.fxml"));
		Scene selectionScreen = new Scene(mahLoader.load());
		selectionController = mahLoader.getController();
		System.out.println("  Scene 3 loaded");
		selectionController.btnLoginNew.setOnAction(e -> {

			loginController.loginListener = new ILoginListener() {

				@Override
				public void successfulLogin(TALoginClient loggedIn) {
					SessionView view;
					try {
						ProfileLoadInfo p = new ProfileLoadInfo(loggedIn.user + " - new session", false);
						p.password = loggedIn.pass;
						p.username = loggedIn.user;
						view = SessionView.createSessionView(p, TAUI1.this);

						setScene(view);
						view.loggedIn = loggedIn;
						view.startSession();
					} catch (IOException | ParserConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				@Override
				public void loginCancelled() {
					setScene(selectionScreen);
				}
			};
			setScene(loginScene);

		});

		empty = new Label("No profiles");
		empty.setWrapText(true);
		empty.setPadding(new Insets(20, 0, 20, 0));

		Platform.setImplicitExit(false);

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				if (CONFIG.DEBUG_MODE) {
					System.exit(0);
				} else {
					event.consume();
					primaryStage.hide();
				}
			}
		});
		
		icon = new TrayIcon(ImageIO.read(TAUI1.class.getResource("/res/img/ico.png")), "Tyanide Sync");
		icon.addActionListener((e) -> Platform.runLater(() -> primaryStage.show()));
		System.out.println("UI loaded");
		
		loadProfiles();
		System.out.println("Profiles loaded");
		
		primaryStage.setScene(selectionScreen);

		System.out.println("Done.");
	}

	private void loadProfiles() throws IOException, ParserConfigurationException, SAXException {
		profiles = new HashMap<>();
		selectionController.profileLinks.getChildren().clear();
		if (!Files.exists(Paths.get(cachepath + "profiles.xml"))) {
			saveProfiles();
			selectionController.profileLinks.getChildren().add(empty);
			return;
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document doc = builder.parse(cachepath + "profiles.xml");
		NodeList l = doc.getDocumentElement().getElementsByTagName("profile");

		selectionController.profileLinks.getChildren().remove(empty);
		for (int i = 0; i < l.getLength(); i++) {
			Element e = (Element) l.item(i);
			ProfileLoadInfo p = new ProfileLoadInfo(e);
			profiles.put(p.profileName, p);
			initProfileLink(p.profileName);
		}
	}

	private void initProfileLink(String profileName) {
		Hyperlink lnk = new Hyperlink(profileName);
		lnk.setOnAction(e -> {
			try {
				SessionView view = SessionView.createSessionView(profiles.get(profileName), this);
				setScene(view);
			} catch (IOException | ParserConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		});
		selectionController.profileLinks.getChildren().add(lnk);
	}

	public void saveProfiles() throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(cachepath + "profiles.xml");
		writer.write("<isis>");
		for (Entry<String, ProfileLoadInfo> entry : profiles.entrySet()) {
			writer.write("<profile>");
			writer.write(entry.getValue().toString());
			writer.write("</profile>");
		}
		writer.write("</isis>");
		writer.close();
	}

	public void openLoginPage(ILoginListener listener, Scene ret, String... user) {

		if (user.length == 1)
			loginController.txtStudentID.setText(user[0]);
		loginController.loginListener = new ILoginListener() {

			@Override
			public void successfulLogin(TALoginClient loggedIn) {
				setScene(loadingScene);
				listener.successfulLogin(loggedIn);
				loginController.loginListener = null;
				setScene(ret);
			}

			@Override
			public void loginCancelled() {
				listener.loginCancelled();
				loginController.loginListener = null;
				setScene(ret);
			}
		};
		setScene(loginScene);
	}

	public void showLoadingScreen(Scene ret) {
		lastScene = ret;
		setScene(loadingScene);
	}

	public void hideLoadingScreen() {
		if (lastScene != null)
			setScene(lastScene);
	}

	private void setScene(Scene s) {

		double prevH2 = primaryStage.getHeight();
		double prevW2 = primaryStage.getWidth();
		primaryStage.setScene(s);
		primaryStage.setWidth(prevW2);
		primaryStage.setHeight(prevH2);
	}

	public static void main(String[] args) {
		System.out.println("launching...");
		launch(args);
	}
}
