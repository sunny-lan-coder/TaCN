package tk.sunnylan.tacn.gui.taui1;

import java.io.IOException;
import java.util.ArrayList;

import com.jfoenix.controls.JFXButton;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;

public class SessionView extends Scene {

	public SessionViewController controller;
	public final ObservableList<SessionPage> pages;
	private final ArrayList<JFXButton> buttons;
	private final ArrayList<SessionPage> _pages;

	// AI YA CUSTY
	public static SessionView createSessionView() throws IOException {
		FXMLLoader mahLoader = new FXMLLoader(TAUI1.class.getResource("MainInterface.fxml"));
		return new SessionView(mahLoader.load(), mahLoader.getController());
	}

	private SessionView(Parent par, SessionViewController controller) {
		super(par);
		this.controller = controller;
		_pages = new ArrayList<>();
		pages = FXCollections.observableList(_pages);
		buttons = new ArrayList<>();
		pages.addListener(new ListChangeListener<SessionPage>() {

			@Override
			public void onChanged(Change<? extends SessionPage> c) {
				while (c.next()) {
					for (SessionPage page : c.getAddedSubList()) {
						addPage(page);
					}
					for (SessionPage page : c.getRemoved()) {
						removePage(page);
					}
				}
			}

		});
	}

	private void addPage(SessionPage p) {
		JFXButton btn = new JFXButton(p.pageTitle);
		btn.setMaxWidth(Double.MAX_VALUE);
		btn.setPrefHeight(40);
		btn.setFont(new Font(btn.getFont().getName(), 16));
		buttons.add(btn);
		controller.vboxLinks.getChildren().add(btn);
		btn.setOnAction(e -> {
			controller.contentPane.getChildren().clear();
			controller.contentPane.getChildren().add(p.pageContent.getRoot());
			controller.stackMenuItems.getChildren().clear();
			controller.stackMenuItems.getChildren().add(p.menu);
		});
	}

	private void removePage(SessionPage p) {
		buttons.remove(_pages.indexOf(p));
		controller.contentPane.getChildren().remove(p.pageContent);
		_pages.remove(p);
	}

	public void setSessionName(String sessionName) {
		controller.lblSessionName.setText(sessionName);
	}

}
