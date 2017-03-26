package tk.sunnylan.tacn.gui.taui1;

import tk.sunnylan.tacn.webinterface.htmlunit.TALoginClient;

public interface ILoginListener {
	public void successfulLogin(TALoginClient loggedIn);
	public void loginCancelled();
}
