package tk.sunnylan.tacn.gui.taui1;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.jfoenix.controls.JFXButton;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import tk.sunnylan.tacn.data.Subject;
import tk.sunnylan.tacn.parse.Parse;
import tk.sunnylan.tacn.parse.SubjectChange;
import tk.sunnylan.tacn.parse.Update;
import tk.sunnylan.tacn.tst.CONFIG;
import tk.sunnylan.tacn.webinterface.TALoginClient;
import tk.sunnylan.tacn.webinterface.TASession;

public class TAUI1 extends Application {

	final String savepath = new File("").getAbsolutePath() + "\\cache\\";
	String curruser;

	public static void main(String[] args) {
		// proxy test:
		if (CONFIG.USE_PROXY) {
			System.setProperty("http.proxyHost", "127.0.0.1");
			System.setProperty("https.proxyHost", "127.0.0.1");
			System.setProperty("http.proxyPort", "8080");
			System.setProperty("https.proxyPort", "8080");
		}
		launch(args);
	}

	TASession session;
	HashMap<String, Subject> subjects;
	HashMap<String, SubjectView> sviews;
	SessionView sessView;
	Stage primaryStage;
	Scene loginScene;
	Thread refreshthread;

	@Override
	public void start(Stage primaryStage) throws Exception {

		subjects = new HashMap<>();
		sviews = new HashMap<>();

		this.primaryStage = primaryStage;
		FXMLLoader mahLoader = new FXMLLoader(TAUI1.class.getResource("LoginScreen.fxml"));
		Pane mainPane = mahLoader.load();
		loginScene = new Scene(mainPane, 1280, 720);
		LoginController loginController = mahLoader.getController();

		mahLoader = new FXMLLoader(TAUI1.class.getResource("LoadingScreen.fxml"));
		mainPane = mahLoader.load();
		Scene loadingScene = new Scene(mainPane, 1280, 720);

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

		try {
			sessView = SessionView.createSessionView();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

		loadCache();
		JFXButton syncBtn = new JFXButton("Login");
		syncBtn.setPrefWidth(Double.MAX_VALUE);
		syncBtn.setOnAction(e -> {
			loginController.loginListener = new ILoginListener() {

				@Override
				public void successfulLogin(TALoginClient loggedIn) {
					setScene(loadingScene);

					sessView.setSessionName(loggedIn.user);
					new Thread(() -> {
						session = new TASession(loggedIn);
						try {
							refreshthread = new Thread(() -> {
								refreshLoop();
							});
							refreshthread.start();
							Platform.runLater(() -> {
								setScene(sessView);
								syncBtn.setText("Logout");
							});
						} catch (Exception e) {
							e.printStackTrace();
							Platform.runLater(() -> setScene(loginScene));
						}
					}).start();

				}

				@Override
				public void loginCancelled() {
					setScene(sessView);
				}

			};

			primaryStage.setScene(loginScene);
			if (refreshthread != null)
				refreshthread.interrupt();
			if (session != null)
				syncBtn.setText("Logout");

		});
		sessView.controller.globalMenu.getChildren().add(syncBtn);

		JFXButton saveBtn = new JFXButton("Save");
		saveBtn.setPrefWidth(Double.MAX_VALUE);
		saveBtn.setOnAction(e -> {
			saveCache();
		});
		sessView.controller.globalMenu.getChildren().add(saveBtn);

		primaryStage.setScene(sessView);
		primaryStage.setTitle("Tyanide");
		primaryStage.setWidth(1280);
		primaryStage.setHeight(720);
		initNotifications();
		primaryStage.show();
	}

	private void saveCache() {
		for (String coursecode : subjects.keySet()) {
			try {
				PrintWriter writer = new PrintWriter(
						savepath + tk.sunnylan.tacn.parse.Util.sanitizeFileName(coursecode) + ".xml", "UTF-8");
				writer.print("<isis>");
				writer.print(subjects.get(coursecode).toString());
				writer.print("</isis>");
				writer.close();
			} catch (FileNotFoundException | UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
	}

	private void refreshLoop() {
		while (true) {

			try {
				if (session != null) {
					final Update u = getUpdates();

					if (u.updates.size() > 0 || u.additions.size() > 0) {
						Platform.runLater(() -> {
							try {
								refresh(u);

							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						});
						if (icon != null)
							icon.displayMessage(Util.summarizeUpdatesShort(u), Util.summarizeUpdates(u),
									MessageType.INFO);
					}
				}
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e2) {
				return;
			}
		}

	}

	private TrayIcon icon;

	public void initNotifications() throws IOException, AWTException {
		SystemTray tray;
		if (SystemTray.isSupported())
			tray = SystemTray.getSystemTray();
		else {
			System.err.println("System tray not supported, not using notifications");

			return;
		}
		icon = new TrayIcon(ImageIO.read(TAUI1.class.getResource("/res/img/ico.png")), "Tyanide");
		icon.addActionListener((e) -> Platform.runLater(() -> primaryStage.show()));
		tray.add(icon);
	}

	private void loadCache() throws ParserConfigurationException, IOException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		try (Stream<Path> paths = Files.walk(Paths.get(savepath))) {
			paths.forEach(filePath -> {
				if (Files.isRegularFile(filePath)) {
					Document doc;
					try {
						doc = builder.parse(filePath.toString());
						Subject s = new Subject((Element) doc.getChildNodes().item(0));
						subjects.put(s.courseCode, s);

						SubjectView sv = SubjectView.getNewSubjectView(s);
						sviews.put(s.courseCode, sv);
						sessView.pages.add(new SessionPage(s.courseCode, sv, sv.toggleSummary));
					} catch (SAXException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
	}

	private Update getUpdates() throws Exception {
		Update u = new Update();
		session.refresh();
		for (HtmlPage p : session.subpages) {
			String coursecode = Parse.getCourseCode(p);
			Subject s = subjects.get(coursecode);
			if (s == null) {
				Subject x = new Subject(coursecode);
				SubjectChange z = Parse.parseSubject(p, x);
				if (z.changes.size() > 0)
					u.additions.put(coursecode, z);
				s = x;
			} else {
				SubjectChange z = Parse.parseSubject(p, s);
				if (z.changes.size() > 0)
					u.updates.put(coursecode, z);
			}
			subjects.put(coursecode, s);
		}
		return u;
	}

	public void refresh(Update u) throws Exception {
		for (String updatedC : u.updates.keySet()) {
			sviews.get(updatedC).refresh();
		}
		for (String added : u.additions.keySet()) {
			Platform.runLater(() -> {

				try {

					SubjectView sv = SubjectView.getNewSubjectView(subjects.get(added));

					sviews.put(added, sv);

					sessView.pages.add(new SessionPage(added, sv, sv.toggleSummary));

				} catch (IOException e) {

					e.printStackTrace();

				}

			});
		}
	}

	private void setScene(Scene s) {

		double prevH2 = primaryStage.getHeight();
		double prevW2 = primaryStage.getWidth();
		primaryStage.setScene(s);
		primaryStage.setWidth(prevW2);
		primaryStage.setHeight(prevH2);
	}
}
