package tk.sunnylan.tacn.gui.taui1;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
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
	private static Logger logger = Logger.getLogger(SessionView.class.getName());

	private SessionViewController controller;
	public final ObservableMap<String, SessionPage> pages;
	private final HashMap<String, JFXButton> buttons;
	private final HashMap<String, SessionPage> _pages;
	private HashMap<String, Subject> subjects;
	private HashMap<String, SubjectView> sviews;
	private Thread refreshthread;
	private TAUI1 context;
	private ProfileLoadInfo profile;
	private JFXButton lastClicked;
	public TASession tasession;

	private StackPane emptySubjectsPane;

	// AI YA CUSTY
	public static SessionView createSessionView(ProfileLoadInfo p, TAUI1 context) throws IOException {
		FXMLLoader mahLoader = new FXMLLoader(TAUI1.class.getResource("MainInterface.fxml"));

		SessionView view;
		try {
			view = new SessionView(p, mahLoader.load(), mahLoader.getController(), context);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to load FXML file", e);
			throw e;
		}

		return view;
	}

	private SessionView(ProfileLoadInfo profile, Parent par, SessionViewController controller, TAUI1 context) {
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
			trySync();
		}
	}

	private void initUI() {
		// this.getStylesheets().add("res/css/button.css");
		controller.txtSessionName.setText(profile.profileName);
		controller.txtSessionName.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				if (tk.sunnylan.tacn.parse.htmlunit.Util.sanitizeFileName(controller.txtSessionName.getText())
						.isEmpty()) {
					controller.txtSessionName.setText(profile.profileName);
					return;
				}

				String oldProfileName = profile.profileName;
				profile.profileName = controller.txtSessionName.getText();
				controller.txtSessionName.setPrefWidth(controller.txtSessionName.getText().length() * 7);
				if (profile.isCached) {
					context.reloadProfileLink(oldProfileName);
					context.saveProfiles();
				}
				this.getRoot().requestFocus();
			}

		});
		controller.txtSessionName.setPrefWidth(controller.txtSessionName.getText().length() * 7);
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
				trySync();
			}
			this.getRoot().requestFocus();
		});

		controller.radioStoreOffline.setOnAction(e -> {

			if (!controller.radioStoreOffline.isSelected())
				disableCache();
			else
				enableCache();

			this.getRoot().requestFocus();
		});

		controller.lnkCloseSession.setOnAction(e -> exit());

		controller.radioStoreCreds.setOnAction(e -> {
			if (controller.radioStoreCreds.isSelected())
				setSaveCredentials();
			else
				unsetSaveCredentials();
			this.getRoot().requestFocus();
		});

		if (profile.password == null || profile.username == null) {
			controller.radioStoreCreds.setDisable(true);
		}

		controller.contentPane.getChildren().add(new Label("No subject selected"));
		this.getRoot().requestFocus();
	}

	private void setSaveCredentials() {
		// if(profile.hasPassword && profile.hasUsername)
		// return;
		if (profile.password == null || profile.username == null) {
			return;
		}
		profile.hasPassword = profile.hasUsername = true;

		context.saveProfiles();

		controller.radioStoreCreds.setDisable(false);
		controller.radioStoreCreds.setSelected(true);
	}

	private void unsetSaveCredentials() {
		// if(!(profile.hasPassword && profile.hasUsername))
		profile.hasPassword = profile.hasUsername = false;
		context.saveProfiles();
		controller.radioStoreCreds.setSelected(false);
	}

	private SystemTray tray;

	private void initNotifications() {
		if (SystemTray.isSupported())
			tray = SystemTray.getSystemTray();
		else {
			logger.log(Level.WARNING, "System tray not supported, not using notifications");
			return;
		}

		try {
			tray.add(context.icon);
			context.keepopen = true;
		} catch (AWTException e) {
			logger.log(Level.WARNING, "Unable to init tray notifications", e);
		}
	}

	private void endNotifications() {
		if (tray != null)
			tray.remove(context.icon);
		context.keepopen = false;
	}

	private void enableCache() {
		logger.info("Enabling cache");
		if (!profile.isCached) {
			// generate cache path
			profile.isCached = true;
			profile.cachepath = tk.sunnylan.tacn.parse.htmlunit.Util.sanitizeFileName(profile.profileName) + "\\";
			while (Files.exists(Paths.get(context.cachepath + profile.cachepath)))
				profile.cachepath += Util.genRandomProfileName(10);
			saveCache();
			context.addProfile(profile);
		}

		context.saveProfiles();
		loadCache();

		if (profile.isCached && profile.password != null && profile.username != null)
			controller.radioStoreCreds.setDisable(false);
		controller.radioStoreOffline.setSelected(true);
	}

	private void disableCache() {
		logger.info("Disabling cache");
		if (profile.isCached) {
			Util.removeDirectory(new File(context.cachepath + profile.cachepath));
			profile.isCached = false;
			context.removeProfile(profile);
		}
		context.saveProfiles();

		controller.radioStoreCreds.setDisable(true);
		controller.radioStoreCreds.setSelected(false);
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
			logger.log(Level.SEVERE, "Unable to write to cache", e1);
		}
		context.saveProfiles();
	}

	private void loadCache() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			return;
		}

		try (Stream<Path> paths = Files.walk(Paths.get(context.cachepath + profile.cachepath))) {
			paths.forEach(filePath -> {
				if (Files.isRegularFile(filePath)) {
					Document doc;
					try {
						doc = builder.parse(filePath.toString());
					} catch (SAXException | IOException e) {
						logger.log(Level.SEVERE, "Unable to read/parse a subject cache file", e);
						return;
					}
					Subject s = new Subject((Element) doc.getChildNodes().item(0));
					subjects.put(s.courseCode, s);

					SubjectView sv;
					try {
						sv = SubjectView.getNewSubjectView(s);
					} catch (IOException e) {
						return;
					}
					sviews.put(s.courseCode, sv);
					SessionPage mahPage = new SessionPage(s.courseCode, sv, new Pane());

					updatePage(s.courseCode, mahPage);
				}
			});
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to walk cache directory", e);
			return;
		}
	}

	public synchronized void trySync() {
		controller.radioSync.setDisable(true);
		if (tasession != null) {
			profile.username = tasession.user;
			profile.password = tasession.pass;
			if (profile.isCached)
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
					logger.log(Level.WARNING, "Unable to log in", e);
					stopSync();
				}
				Platform.runLater(() -> {
					if (profile.isCached)
						controller.radioStoreCreds.setDisable(false);
					context.hideLoadingScreen();
				});
			}).start();
			return;
		}

		ILoginListener l = new ILoginListener() {

			@Override
			public void successfulLogin(TASession loggedIn) {
				tasession = loggedIn;

				profile.username = tasession.user;
				profile.password = tasession.pass;
				if (profile.isCached)
					controller.radioStoreCreds.setDisable(false);
				startSync();
			}

			@Override
			public void loginCancelled() {
				stopSync();
			}

		};

		Platform.runLater(() -> {
			if (profile.hasUsername)
				context.openLoginPage(l, this, profile.username);
			else
				context.openLoginPage(l, this);
		});
	}

	private synchronized void startSync() {
		profile.isSynced = true;

		initNotifications();

		context.saveProfiles();

		refreshthread = new Thread(() -> refreshLoop());
		refreshthread.start();

		Platform.runLater(() -> {
			controller.radioSync.setDisable(false);
			controller.radioSync.setSelected(true);
			this.getRoot().requestFocus();
		});

	}

	private synchronized void stopSync() {
		profile.isSynced = false;
		context.saveProfiles();

		if (refreshthread != null)
			refreshthread.interrupt();
		Platform.runLater(() -> {
			controller.radioSync.setDisable(false);
			controller.radioSync.setSelected(false);
			this.getRoot().requestFocus();
		});
		if (tasession != null)

			tasession.logout();

		endNotifications();
	}

	private void refreshLoop() {
		while (true) {
			if (tasession != null) {
				try {
					final Update u = getUpdates();
					if (u.updates.size() > 0 || u.additions.size() > 0) {
						Platform.runLater(() -> {
							refresh(u);
						});
						if (context.icon != null) {
							context.icon.displayMessage(Util.summarizeUpdatesShort(u), Util.summarizeUpdates(u),
									MessageType.INFO);
						}
						if (controller.radioStoreOffline.isSelected()) {
							saveCache();
						}
					}
				} catch (RuntimeException ex) {
					logger.log(Level.WARNING, "Unhandled runtime exception occured in refresh thread", ex);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Unable to get updates", e);
				}
			}

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e2) {
				return;
			}
		}

	}

	private void exit() {
		if (profile.isSynced) {
			if (refreshthread != null)
				refreshthread.interrupt();
			endNotifications();
		}

		if (profile.isCached)
			saveCache();

		if (tasession != null)
			tasession.logout();

		context.showSelectionScene();
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

	private void refresh(Update u) {
		for (String updatedC : u.updates.keySet()) {
			sviews.get(updatedC).refresh();
		}
		for (String added : u.additions.keySet()) {
			Platform.runLater(() -> {
				SubjectView sv;
				try {
					sv = SubjectView.getNewSubjectView(subjects.get(added));
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Could not lad subject view", e);
					return;
				}
				sviews.put(added, sv);

				updatePage(added, new SessionPage(added, sv, new Pane()));

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
		btn.getStylesheets().add("res/css/button.css");
		controller.vboxLinks.getChildren().add(btn);
		_pages.put(p.pageTitle, p);
		btn.setOnAction(e -> {
			if (lastClicked != null) {
				lastClicked.getStyleClass().remove("jbtn-selected");
				lastClicked.getStyleClass().add("jbtn-deselected");
			}
			controller.contentPane.getChildren().setAll(p.pageContent.getRoot());
			controller.stackMenuItems.getChildren().setAll(p.menu);
			btn.getStyleClass().remove("jbtn-deselected");
			btn.getStyleClass().add("jbtn-selected");
			lastClicked = btn;
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
