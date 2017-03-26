package tk.sunnylan.tacn.tst;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import tk.sunnylan.tacn.parse.htmlunit.Parse;
import tk.sunnylan.tacn.webinterface.htmlunit.TALoginClient;
import tk.sunnylan.tacn.webinterface.htmlunit.TASession;

public class TestWeb {

	public static void main(String[] args) throws Exception {
		System.out.println("Logging in...");
		TALoginClient login=new TALoginClient("insert student id", "insert password");
		System.out.println("Loading main page.");
		TASession mahNav=new TASession(login);
		mahNav.refresh();
		System.out.println("Done:");
		for(HtmlPage p:mahNav.subpages){
			System.out.println(Parse.getCourseCode(p));
		}
	}

}
