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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
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
import tk.sunnylan.tacn.webinterface.jsoup.TASession;

public class TAUI1 extends Application {
	private static Logger logger = Logger.getLogger(TAUI1.class.getName());

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
	private Scene selectionScreen;

	public boolean keepopen = false;

	@Override
	public void start(Stage primaryStage) throws Exception {

		logger.info("Initializing UI...");
		primaryStage.setTitle("Tyanide " + CONFIG.CURRENT_VERSION);
		primaryStage.setHeight(180);
		primaryStage.setWidth(240);
		primaryStage.setMaximized(true);
		this.primaryStage = primaryStage;
		FXMLLoader mahLoader;
		Pane mainPane = new StackPane(new JFXSpinner());
		loadingScene = new Scene(mainPane);
		logger.info("  Scene 1 loaded");

		primaryStage.setScene(loadingScene);
		primaryStage.show();
		mahLoader = new FXMLLoader(TAUI1.class.getResource("LoginScreen.fxml"));
		mainPane = mahLoader.load();
		loginScene = new Scene(mainPane);
		loginController = mahLoader.getController();
		logger.info("  Scene 2 loaded");
		mahLoader = new FXMLLoader(TAUI1.class.getResource("ProfileSelectionScreen.fxml"));
		selectionScreen = new Scene(mahLoader.load());
		selectionController = mahLoader.getController();
		logger.info("  Scene 3 loaded");
		selectionController.btnLoginNew.setOnAction(e -> {

			loginController.loginListener = new ILoginListener() {

				@Override
				public void successfulLogin(TASession loggedIn) {

					String name = loggedIn.user + " - temporary session";
					if (profiles.containsKey(name))
						name += " - ID: " + Util.genRandomProfileName(3);
					while (profiles.containsKey(name))
						name += Util.genRandomProfileName(3);
					ProfileLoadInfo p = new ProfileLoadInfo(name, false);
					p.password = loggedIn.pass;
					p.username = loggedIn.user;
					SessionView view;
					try {
						view = SessionView.createSessionView(p, TAUI1.this);
					} catch (IOException e) {
						loggedIn.logout();
						logger.log(Level.SEVERE, "Unable to load view:SessionView", e);
						setScene(selectionScreen);
						return;
					}
					view.tasession = loggedIn;
					view.trySync();
					setScene(view);

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
		selectionController.profileLinks.getChildren().add(empty);

		Platform.setImplicitExit(false);

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				logger.info("Close request for primary stage");
				if (CONFIG.DEBUG_MODE) {
					logger.info("Debug mode - exiting");
					System.exit(0);
				} else if (keepopen) {
					logger.info("keepopen is set - simply hiding main window");
					event.consume();
					primaryStage.hide();
				} else {
					logger.info("keepopen isn't set - closing main window");
					System.exit(0);
				}
			}
		});

		icon = new TrayIcon(ImageIO.read(TAUI1.class.getResource("/res/img/ico.png")), "Tyanide Sync");
		icon.addActionListener((e) -> Platform.runLater(() -> primaryStage.show()));
		logger.info("UI loaded");

		try {
			loadProfiles();
			logger.info("Profiles loaded");
		} catch (IOException | ParserConfigurationException | SAXException e1) {
			logger.log(Level.SEVERE, "Unable to load profiles", e1);
		}

		primaryStage.setScene(selectionScreen);
		logger.info("Done.");
	}

	private void loadProfiles() throws IOException, ParserConfigurationException, SAXException {
		logger.info("Loading profiles");
		profiles = new HashMap<>();
		profileLinks = new HashMap<>();
		if (profileLinks.size() > 0)
			selectionController.profileLinks.getChildren().clear();
		if (!Files.exists(Paths.get(cachepath + "profiles.xml"))) {
			logger.info("Cache does not exist, creating new cache");
			saveProfiles();
			return;
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document doc = builder.parse(cachepath + "profiles.xml");
		NodeList l = doc.getDocumentElement().getElementsByTagName("profile");

		for (int i = 0; i < l.getLength(); i++) {
			Element e = (Element) l.item(i);
			ProfileLoadInfo p = new ProfileLoadInfo(e);
			profiles.put(p.profileName, p);
			reloadProfileLink(p.profileName);
		}
	}

	private HashMap<String, Hyperlink> profileLinks;

	public void reloadProfileLink(String profileName) {
		if (profileLinks.containsKey(profileName)) {
			profileLinks.get(profileName).setText(profiles.get(profileName).profileName);
			profileLinks.put(profiles.get(profileName).profileName, profileLinks.get(profileName));
			profileLinks.remove(profileName);
			return;
		}
		Hyperlink lnk = new Hyperlink(profileName);
		lnk.setOnAction(e -> {
			SessionView view;
			try {
				view = SessionView.createSessionView(profiles.get(profileName), this);
			} catch (IOException e1) {
				logger.log(Level.SEVERE, "Unable to load view:SessionView", e);
				return;
			}

			setScene(view);
		});
		profileLinks.put(profileName, lnk);
		selectionController.profileLinks.getChildren().add(lnk);
		selectionController.profileLinks.getChildren().remove(empty);
	}

	public void saveProfiles() {
		logger.info("Saving profiles");
		PrintWriter writer;
		try {
			writer = new PrintWriter(cachepath + "profiles.xml");
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "Cache file not found", e);
			return;
		}
		writer.write("<isis>");
		for (Entry<String, ProfileLoadInfo> entry : profiles.entrySet()) {
			writer.write("<profile>");
			writer.write(entry.getValue().toString());
			writer.write("</profile>");
		}
		writer.write("</isis>");
		writer.close();
	}

	public void showSelectionScene() {
		setScene(selectionScreen);
	}

	public void openLoginPage(ILoginListener listener, Scene ret, String... user) {

		if (user.length == 1)
			loginController.txtStudentID.setText(user[0]);
		loginController.loginListener = new ILoginListener() {

			@Override
			public void successfulLogin(TASession loggedIn) {
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
		if (lastScene != null) {
			setScene(lastScene);
			lastScene = null;
		}
	}

	private void setScene(Scene s) {
		double prevH2 = primaryStage.getHeight();
		double prevW2 = primaryStage.getWidth();
		primaryStage.setScene(s);
		primaryStage.setWidth(prevW2);
		primaryStage.setHeight(prevH2);
	}

	public void addProfile(ProfileLoadInfo p) {
		logger.info("Adding profile " + p.profileName);
		profiles.put(p.profileName, p);
		reloadProfileLink(p.profileName);

		selectionController.profileLinks.getChildren().remove(empty);
	}

	public void removeProfile(ProfileLoadInfo p) {
		logger.info("Removing profile " + p.profileName);
		selectionController.profileLinks.getChildren().remove(profileLinks.get(p.profileName));
		profileLinks.remove(p.profileName);
		profiles.remove(p.profileName);
		if (profiles.size() == 0)
			selectionController.profileLinks.getChildren().setAll(empty);
	}

	public static void main(String[] args) {
		CONFIG.initDebug();
		try {
			Class.forName("com.sun.javafx.runtime.VersionInfo");
			logger.info("JavaFX version: " + com.sun.javafx.runtime.VersionInfo.getRuntimeVersion());
		} catch (ClassNotFoundException e) {
			logger.log(Level.WARNING, "Unable to get JavaFX version", e);
		}
		CONFIG.checkUpdates();
		try {
			logger.info("Launching Tyanide...");
			launch(args);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Unhandled exception", e);
			JOptionPane.showMessageDialog(null, "An error occured in Tyanide. Please send the contents of "
					+ CONFIG.logpath + " to sunny.lan.coder@gmail.com. Thank you!", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
