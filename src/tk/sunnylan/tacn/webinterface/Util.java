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
	public static void shutUp(WebClient webClient){
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

	    java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF); 
	    java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);


	    webClient.setIncorrectnessListener(new IncorrectnessListener() {

	        @Override
	        public void notify(String arg0, Object arg1) {
	            // TODO Auto-generated method stub

	        }
	    });
	    webClient.setCssErrorHandler(new ErrorHandler() {

	        @Override
	        public void warning(CSSParseException exception) throws CSSException {
	            // TODO Auto-generated method stub

	        }

	        @Override
	        public void fatalError(CSSParseException exception) throws CSSException {
	            // TODO Auto-generated method stub

	        }

	        @Override
	        public void error(CSSParseException exception) throws CSSException {
	            // TODO Auto-generated method stub

	        }
	    });
	    webClient.setJavaScriptErrorListener(new JavaScriptErrorListener() {

			@Override
			public void loadScriptError(HtmlPage arg0, URL arg1, Exception arg2) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void malformedScriptURL(HtmlPage arg0, String arg1, MalformedURLException arg2) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void scriptException(HtmlPage arg0, ScriptException arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void timeoutError(HtmlPage arg0, long arg1, long arg2) {
				// TODO Auto-generated method stub
				
			}

	    });
	    webClient.setHTMLParserListener(new HTMLParserListener() {

			@Override
			public void error(String arg0, URL arg1, String arg2, int arg3, int arg4, String arg5) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void warning(String arg0, URL arg1, String arg2, int arg3, int arg4, String arg5) {
				// TODO Auto-generated method stub
				
			}

	       
	    });

	}
}