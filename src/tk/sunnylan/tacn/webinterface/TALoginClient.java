package tk.sunnylan.tacn.webinterface;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class TALoginClient {
	static final String TA_URL = "https://ta.yrdsb.ca/";
	static final String USER_INPUT = "username";
	static final String PASS_INPUT = "password";
	static final String FORM_NAME = "loginForm";
	static final String KEYWORD = "login";
	static final String INVALID_KEYWORD = "Invalid Login";
	static final String SUBMIT_NAME = "submit";
	public String user;
	private String pass;
	private WebClient client;
	public HtmlPage page;

	public TALoginClient(String user, String pass) throws Exception {
		this.user = user;
		this.pass = pass;
		client = new WebClient();
		Util.shutUp(client);
		page = (HtmlPage) client.getPage(TA_URL);
		refresh();
	}

	public void refresh() throws Exception {
		page.refresh();
		if (page.getTitleText().contains(KEYWORD)) {
			HtmlForm form = page.getFormByName(FORM_NAME);
			form.getInputByName(USER_INPUT).type(user);
			form.getInputByName(PASS_INPUT).type(pass);
			page = form.getInputByName(SUBMIT_NAME).click();
			
			if(page.asText().contains(INVALID_KEYWORD))
			 throw new Exception("Invalid login");
		}
	}
}
