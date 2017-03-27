package tk.sunnylan.tacn.webinterface;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

import org.apache.commons.logging.LogFactory;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;

public class Util {
	public static void shutUp(WebClient webClient) {
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");

		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
		java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

		webClient.setIncorrectnessListener(new IncorrectnessListener() {

			@Override
			public void notify(String arg0, Object arg1) {

			}
		});
		webClient.setCssErrorHandler(new ErrorHandler() {

			@Override
			public void warning(CSSParseException exception) throws CSSException {

			}

			@Override
			public void fatalError(CSSParseException exception) throws CSSException {

			}

			@Override
			public void error(CSSParseException exception) throws CSSException {

			}
		});
		webClient.setJavaScriptErrorListener(new JavaScriptErrorListener() {
			@Override
			public void scriptException(HtmlPage page, ScriptException scriptException) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void timeoutError(HtmlPage page, long allowedTime, long executionTime) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void malformedScriptURL(HtmlPage page, String url, MalformedURLException malformedURLException) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void loadScriptError(HtmlPage page, URL scriptUrl, Exception exception) {
				// TODO Auto-generated method stub
				
			}

		});
		webClient.setHTMLParserListener(new HTMLParserListener() {

			@Override
			public void error(String arg0, URL arg1, String arg2, int arg3, int arg4, String arg5) {

			}

			@Override
			public void warning(String arg0, URL arg1, String arg2, int arg3, int arg4, String arg5) {

			}

		});

	}
}
