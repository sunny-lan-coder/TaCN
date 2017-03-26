package tk.sunnylan.tacn.tst;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import tk.sunnylan.tacn.data.Subject;
import tk.sunnylan.tacn.parse.htmlunit.Parse;
import tk.sunnylan.tacn.webinterface.htmlunit.TALoginClient;
import tk.sunnylan.tacn.webinterface.htmlunit.TASession;

public class TestParse {

	public static void main(String[] args) throws Exception {
		System.out.println("Logging in...");
		TALoginClient login=new TALoginClient("073689168", "f82etc68");
		System.out.println("Loading main page.");
		TASession mahNav=new TASession(login);
		mahNav.refresh();
		System.out.println("Done:");
		for(HtmlPage p:mahNav.subpages){
			Subject s=new Subject(Parse.getCourseCode(p));
			Parse.parseSubject(p, s);
			System.out.println("<isis>"+s.toString()+"</isis>");
		}
		
		
	}

}
