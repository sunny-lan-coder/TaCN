package tk.sunnylan.tacn.tst;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import tk.sunnylan.tacn.webinterface.htmlunit.Util;

public class HtmlUnitHelloWorld {

	public static void main(String[] args) throws Exception {
		homePage();
	}
	
	static void homePage() throws Exception {
	    try (final WebClient webClient = new WebClient()) {
	    	Util.shutUp(webClient);
	        final HtmlPage page = webClient.getPage("http://ta.yrdsb.ca");
	        System.out.println( page.getTitleText());
	    }
	}

}
