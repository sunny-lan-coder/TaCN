package tk.sunnylan.tacn.tst;

import tk.sunnylan.tacn.webinterface.jsoup.TALoginClient;

public class JsoupFakeTest {

	public static void main(String[] args) throws Exception {
		System.out.println("Logging in...");
		TALoginClient login=new TALoginClient("dvbcxb", "zxcxzcv");
		System.out.println("Loading main page.");
		System.out.println(login.page.text());
	}

}
