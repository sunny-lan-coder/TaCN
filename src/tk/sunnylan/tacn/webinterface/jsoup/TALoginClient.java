package tk.sunnylan.tacn.webinterface.jsoup;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class TALoginClient {
	private static final String TA_URL = "https://ta.yrdsb.ca/";
	private static final String STUDENT_ID_COOKIE = "student_id";
	private static final String SESSION_TOKEN_COOKIE = "session_token";
	private static final String USER_INPUT = "username";
	private static final String PASS_INPUT = "password";
	private static final String ERR_INVALID = "3";
	private static final String LOGIN_ACCEPTOR = "index.php";
	private static final String HOMEPAGE_FILE = "listReports.php";
	private static final String ERROR_KEYWORD = "?error_message=";
	public String user;
	public String pass;
	private String session_token = "";
	private String student_id = "";
	public Document page;

	public TALoginClient(String user, String pass) throws Exception {
		this.user = user;
		this.pass = pass;

		refresh();
	}

	public void refresh() throws Exception {
		Response r = mask(Jsoup.connect(TA_URL + LOGIN_ACCEPTOR).followRedirects(true)
				.cookie(STUDENT_ID_COOKIE, student_id).cookie(SESSION_TOKEN_COOKIE, session_token)).execute();
		if (r.url().toString().contains(HOMEPAGE_FILE)) {
			page = r.parse();
			return;
		}
		r = mask(Jsoup.connect(r.url().toString()).followRedirects(true)
				.data("subject_id", "0", USER_INPUT, user, PASS_INPUT, pass, "submit", "Login").method(Method.POST))
						.execute();
		if (r.url().toString().contains(HOMEPAGE_FILE)) {
			page = r.parse();
			return;
		}
		// determine error
		String loc = r.header("location");
		if (loc.contains(ERROR_KEYWORD)) {
			String errId = loc.substring(loc.indexOf(ERROR_KEYWORD));
			if (errId.equals(ERR_INVALID))
				throw new Exception("Invalid login");
			throw new Exception("Unidentified exception - error code " + errId);
		}
		throw new Exception("Unable to get page - unknown reason");
	}

	private Connection mask(Connection c) {
		return c.header("Host", "ta.yrdsb.ca")
				.header("Connection", "keep-alive")
//				.header("Content-Length", ""+c.request().requestBody().length())
				.header("Cache-Control", "max-age=0")
				.header("Origin", "https://ta.yrdsb.ca")
				.header("Upgrade-Insecure-Requests", "1")
				.header("User-Agent", "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.48 Safari/537.36")
				.header("Content-Type", "application/x-www-form-urlencoded")
				.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
				.referrer("https://ta.yrdsb.ca/yrdsb/")
				.header("Accept-Encoding", "gzip, deflate, br")
				.header("Accept-Language", "en-US,en;q=0.8");
	}
}
