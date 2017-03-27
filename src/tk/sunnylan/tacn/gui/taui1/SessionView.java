package tk.sunnylan.tacn.gui.taui1;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.jfoenix.controls.JFXButton;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import tk.sunnylan.tacn.data.ProfileLoadInfo;
import tk.sunnylan.tacn.data.Subject;
import tk.sunnylan.tacn.parse.SubjectChange;
import tk.sunnylan.tacn.parse.Update;
import tk.sunnylan.tacn.parse.jsoup.Parse;
import tk.sunnylan.tacn.webinterface.jsoup.TASession;

public class SessionView extends Scene {

	private SessionViewController controller;
	public final ObservableMap<String, SessionPage> pages;
	private final HashMap<String, JFXButton> buttons;
	private final HashMap<String, SessionPage> _pages;
	private HashMap<String, Subject> subjects;
	private HashMap<String, SubjectView> sviews;
	private Thread refreshthread;
	private TAUI1 context;
	private ProfileLoadInfo profile;
	public TASession tasession;

	private StackPane emptySubjectsPane;

	// AI YA CUSTY
	public static SessionView createSessionView(ProfileLoadInfo p, TAUI1 context)
			throws IOException, ParserConfigurationException {
		FXMLLoader mahLoader = new FXMLLoader(TAUI1.class.getResource("MainInterface.fxml"));

		SessionView view = new SessionView(p, mahLoader.load(), mahLoader.getController(), context);

		return view;
	}

	private SessionView(ProfileLoadInfo profile, Parent par, SessionViewController controller, TAUI1 context)
			throws ParserConfigurationException, IOException {
		super(par);
		this.context = context;
		this.profile = profile;

		subjects = new HashMap<>();
		sviews = new HashMap<>();
		this.controller = controller;
		_pages = new HashMap<>();
		pages = FXCollections.observableMap(_pages);
		buttons = new HashMap<>();
		pages.addListener(new MapChangeListener<String, SessionPage>() {

			@Override
			public void onChanged(Change<? extends String, ? extends SessionPage> c) {

				if (c.wasAdded()) {
					addPage(c.getValueAdded());
				}
				if (c.wasRemoved()) {
					removePage(c.getValueRemoved());
				}

			}

		});

		initUI();
		if (profile.isCached) {
			enableCache();
		}

		if (profile.hasPassword && profile.hasUsername) {
			setSaveCredentials();
		}
		if (profile.isSynced) {
			tryLogin();
		}
	}

	private void initUI() {
		controller.txtSessionName.setText(profile.profileName);
		controller.txtSessionName.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				if (tk.sunnylan.tacn.parse.htmlunit.Util.sanitizeFileName(controller.txtSessionName.getText()).isEmpty()) {
					controller.txtSessionName.setText(profile.profileName);
					return;
				}
				profile.profileName = controller.txtSessionName.getText();
				try {
					context.saveProfiles();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				this.getRoot().requestFocus();
			}

		});
		Label emptySubjectsLbl = new Label("No subjects loaded");
		emptySubjectsLbl.setWrapText(true);
		emptySubjectsLbl.setPadding(new Insets(10, 0, 10, 0));
		emptySubjectsLbl.setTextAlignment(TextAlignment.CENTER);
		emptySubjectsPane = new StackPane();
		emptySubjectsPane.getChildren().add(emptySubjectsLbl);
		controller.vboxLinks.getChildren().add(emptySubjectsPane);

		controller.radioSync.setOnAction(e -> {
			if (!controller.radioSync.selectedProperty().get()) {
				stopSync();
			} else {
				tryLogin();
			}
		});

		controller.radioStoreOffline.setOnAction(e -> {
			try {
				if (!controller.radioStoreOffline.isSelected())
					disableCache();
				else
					enableCache();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});

		controller.radioStoreCreds.setOnAction(e -> {
			if (controller.radioStoreCreds.isSelected())
				setSaveCredentials();
			else
				unsetSaveCredentials();
		});

		controller.contentPane.getChildren().add(new Label("No subject selected"));
		this.getRoot().requestFocus();
	}

	private void setSaveCredentials() {
		// if(profile.hasPassword && profile.hasUsername)
		// return;
		if (profile.password == null && profile.username == null)
			return;
		profile.hasPassword = profile.hasUsername = true;
		try {
			context.saveProfiles();
		} catch (FileNotFoundException e1) {
		}
		controller.radioStoreCreds.setDisable(false);
		controller.radioStoreCreds.setSelected(true);
	}

	private void unsetSaveCredentials() {
		// if(!(profile.hasPassword && profile.hasUsername))
		profile.hasPassword = profile.hasUsername = false;

		try {
			context.saveProfiles();
		} catch (FileNotFoundException e1) {
		}
		controller.radioStoreCreds.setSelected(false);
	}

	private void stopSync() {
		if (refreshthread != null)
			refreshthread.interrupt();
		endNotifications();
	}

	private SystemTray tray;

	private void initNotifications() throws AWTException {
		if (SystemTray.isSupported())
			tray = SystemTray.getSystemTray();
		else {
			System.err.println("System tray not supported, not using notifications");
			return;
		}

		tray.add(context.icon);
	}

	private void endNotifications() {
		if (tray != null)
			tray.remove(context.icon);
	}

	private void enableCache() {
		if (!profile.isCached) {
			// generate cache path
			profile.cachepath = tk.sunnylan.tacn.parse.htmlunit.Util.sanitizeFileName(profile.profileName) + "\\";
			while (Files.exists(Paths.get(context.cachepath + profile.cachepath)))
				profile.cachepath += Util.genRandomProfileName(10);
			saveCache();
			profile.isCached = true;
		}
		try {
			context.saveProfiles();
			loadCache();
		} catch (ParserConfigurationException | IOException e) {
		}
		controller.radioStoreOffline.setSelected(true);
	}

	private void disableCache() throws IOException {
		if (profile.isCached) {
			Util.removeDirectory(new File(context.cachepath + profile.cachepath));
			profile.isCached = false;
		}
		try {
			context.saveProfiles();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		controller.radioStoreOffline.setSelected(false);
	}

	private void saveCache() {
		try {
			if (!Files.isDirectory(Paths.get(context.cachepath + profile.cachepath)))
				Files.createDirectory(Paths.get(context.cachepath + profile.cachepath));
			for (String coursecode : subjects.keySet()) {

				PrintWriter writer = new PrintWriter(context.cachepath + profile.cachepath
						+ tk.sunnylan.tacn.parse.htmlunit.Util.sanitizeFileName(coursecode) + ".xml", "UTF-8");
				writer.print("<isis>");
				writer.print(subjects.get(coursecode).toString());
				writer.print("</isis>");
				writer.close();

			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void loadCache() throws ParserConfigurationException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		try (Stream<Path> paths = Files.walk(Paths.get(context.cachepath + profile.cachepath))) {
			paths.forEach(filePath -> {
				if (Files.isRegularFile(filePath)) {
					Document doc;
					try {
						doc = builder.parse(filePath.toString());
						Subject s = new Subject((Element) doc.getChildNodes().item(0));
						subjects.put(s.courseCode, s);

						SubjectView sv = SubjectView.getNewSubjectView(s);
						sviews.put(s.courseCode, sv);
						SessionPage mahPage = new SessionPage(s.courseCode, sv, new Pane());

						updatePage(s.courseCode, mahPage);

					} catch (SAXException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
	}

	private void tryLogin() {
		if (tasession != null) {
			controller.radioStoreCreds.setDisable(false);
			startSync();
			return;
		}

		if (profile.hasPassword && profile.hasUsername) {
			context.showLoadingScreen(this);
			new Thread(() -> {
				try {
					tasession = new TASession(profile.username, profile.password);
					startSync();
				} catch (Exception e) {
					e.printStackTrace();
					Platform.runLater(() -> {
						controller.radioSync.setSelected(false);
						context.hideLoadingScreen();
					});
				}
			}).start();
			return;
		}

		ILoginListener l = new ILoginListener() {

			@Override
			public void successfulLogin(TASession loggedIn) {
				SessionView.this.tasession = loggedIn;
				profile.username = loggedIn.user;
				profile.password = loggedIn.pass;
				controller.radioStoreCreds.setDisable(false);
				try {
					context.saveProfiles();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				startSync();
			}

			@Override
			public void loginCancelled() {
				controller.radioSync.setSelected(false);
			}

		};

		Platform.runLater(() -> {
			if (profile.hasUsername)
				context.openLoginPage(l, this, profile.username);
			else
				context.openLoginPage(l, this);
		});
	}

	public void startSync() {
		try {
			initNotifications();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			refreshthread = new Thread(() -> refreshLoop());
			refreshthread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Platform.runLater(() -> {
			context.hideLoadingScreen();
			Platform.runLater(() -> controller.radioSync.setSelected(true));
		});
	}

	private void refreshLoop() {
		while (true) {

			try {
				if (tasession != null) {
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
						if (context.icon != null)
							context.icon.displayMessage(Util.summarizeUpdatesShort(u), Util.summarizeUpdates(u),
									MessageType.INFO);
						if (controller.radioStoreOffline.isSelected()) {
							saveCache();
						}
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

	private Update getUpdates() throws Exception {
		Update u = new Update();
		tasession.refresh();
		for (org.jsoup.nodes.Document p : tasession.subpages) {
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

	private void refresh(Update u) throws Exception {
		for (String updatedC : u.updates.keySet()) {
			sviews.get(updatedC).refresh();
		}
		for (String added : u.additions.keySet()) {
			Platform.runLater(() -> {
				try {
					SubjectView sv = SubjectView.getNewSubjectView(subjects.get(added));
					sviews.put(added, sv);

					updatePage(added, new SessionPage(added, sv, new Pane()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
	}

	private void addPage(SessionPage p) {
		if (_pages.containsKey(p.pageTitle)) {
			updatePage(p.pageTitle, p);
			return;
		}
		controller.vboxLinks.getChildren().remove(emptySubjectsPane);
		JFXButton btn = new JFXButton(p.pageTitle);
		btn.setMaxWidth(Double.MAX_VALUE);
		btn.setPrefHeight(40);
		btn.setFont(new Font(btn.getFont().getName(), 16));
		buttons.put(p.pageTitle, btn);
		controller.vboxLinks.getChildren().add(btn);
		_pages.put(p.pageTitle, p);
		btn.setOnAction(e -> {
			controller.contentPane.getChildren().setAll(p.pageContent.getRoot());
			controller.stackMenuItems.getChildren().setAll(p.menu);
		});
	}

	private void removePage(SessionPage p) {
		buttons.remove(p.pageTitle);
		controller.contentPane.getChildren().remove(p.pageContent);
		_pages.remove(p);
	}

	private void updatePage(String pagename, SessionPage s) {
		if (!_pages.containsKey(pagename)) {
			addPage(s);
			return;
		}
		SessionPage page = _pages.get(pagename);
		buttons.get(pagename).setText(page.pageTitle);
		page.menu = s.menu;
		page.pageContent = s.pageContent;
		page.pageTitle = s.pageTitle;
	}

}
