package tk.sunnylan.tacn.gui.taui1;

import tk.sunnylan.tacn.webinterface.jsoup.TASession;

public interface ILoginListener {
	public void successfulLogin(TASession loggedIn);
	public void loginCancelled();
}
