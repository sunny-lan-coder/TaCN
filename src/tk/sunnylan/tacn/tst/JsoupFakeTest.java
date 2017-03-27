package tk.sunnylan.tacn.tst;

import org.jsoup.nodes.Document;

import tk.sunnylan.tacn.data.Subject;
import tk.sunnylan.tacn.parse.jsoup.Parse;
import tk.sunnylan.tacn.webinterface.jsoup.TASession;

public class JsoupFakeTest {

	public static void main(String[] args) throws Exception {
		System.out.println("Logging in...");
		TASession login=new TASession("zxc", "zxc");
		System.out.println("Loading main page.");
		for(Document doc:login.subpages){
			String name=Parse.getCourseCode(doc);
			System.out.println(name);
			Subject s=new Subject(name);
			Parse.parseSubject(doc, s);
			System.out.println(s.toString());
		}
		
		
	}

}
