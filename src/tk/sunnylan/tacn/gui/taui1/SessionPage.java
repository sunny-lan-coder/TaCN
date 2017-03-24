package tk.sunnylan.tacn.gui.taui1;

import javafx.scene.Node;
import javafx.scene.Scene;

public class SessionPage {
	public final String pageTitle;
	public final Scene pageContent;
	public final Node menu;

	public SessionPage(String pageTitle, Scene pageContent, Node menu) {
		this.pageTitle = pageTitle;
		this.pageContent = pageContent;
		this.menu = menu;
	}

	public SessionPage(String pageTitle, Scene pageContent) {
		this(pageTitle, pageContent, null);
	}
}
