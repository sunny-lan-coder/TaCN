package tk.sunnylan.tacn.gui.taui1;

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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.jfoenix.controls.JFXButton;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import tk.sunnylan.tacn.data.Subject;
import tk.sunnylan.tacn.parse.Parse;
import tk.sunnylan.tacn.webinterface.TALoginClient;
import tk.sunnylan.tacn.webinterface.TASession;

public class TAUI1 extends Application {

	String curruser;

	public static void main(String[] args) {
		launch(args);
	}

	TASession session;
	HashMap<String, Subject> subjects;
	HashMap<String, SubjectView> sviews;
	SessionView sessView;
	Stage primaryStage;
	Scene loginScene;

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		FXMLLoader mahLoader = new FXMLLoader(TAUI1.class.getResource("LoginScreen.fxml"));
		Pane mainPane = mahLoader.load();
		loginScene = new Scene(mainPane, 1280, 720);
		LoginController loginController = mahLoader.getController();

		mahLoader = new FXMLLoader(TAUI1.class.getResource("LoadingScreen.fxml"));
		mainPane = mahLoader.load();
		Scene loadingScene = new Scene(mainPane, 1280, 720);

		sessView = SessionView.createSessionView();

		subjects = new HashMap<>();
		sviews = new HashMap<>();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		final String savepath = new File("").getAbsolutePath() + "\\cache\\";

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

		JFXButton syncBtn = new JFXButton("Sync");
		syncBtn.setPrefWidth(Double.MAX_VALUE);
		syncBtn.setOnAction(e -> {
			if (session == null) {
				loginController.loginListener = new ILoginListener() {

					@Override
					public void successfulLogin(TALoginClient loggedIn) {
						setScene(loadingScene);

						sessView.setSessionName(loggedIn.user);
						new Thread(() -> {
							session = new TASession(loggedIn);
							refresh();
						}).start();

					}
				};
				primaryStage.setScene(loginScene);
			} else {
				refresh();
			}
		});
		sessView.controller.globalMenu.getChildren().add(syncBtn);

		JFXButton saveBtn = new JFXButton("Save");
		saveBtn.setPrefWidth(Double.MAX_VALUE);
		saveBtn.setOnAction(e -> {
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
		});
		sessView.controller.globalMenu.getChildren().add(saveBtn);

		primaryStage.setScene(sessView);
		primaryStage.setWidth(1280);
		primaryStage.setHeight(720);
		primaryStage.show();
	}

	public void refresh() {
		try {
			session.refresh();
			for (HtmlPage p : session.subpages) {
				String coursecode = Parse.getCourseCode(p);
				Subject s = subjects.get(coursecode);
				if (s == null) {
					Subject x = new Subject(coursecode);
					Parse.parseSubject(p, x);
					Platform.runLater(() -> {
						try {
							SubjectView sv = SubjectView.getNewSubjectView(x);
							sviews.put(coursecode, sv);
							sessView.pages.add(new SessionPage(coursecode, sv, sv.toggleSummary));
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
					s = x;
				} else {
					Parse.parseSubject(p, s);
					sviews.get(coursecode).refresh();
				}
				subjects.put(coursecode, s);

			}

			Platform.runLater(() -> setScene(sessView));
		} catch (Exception e) {
			e.printStackTrace();

			Platform.runLater(() -> setScene(loginScene));
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
