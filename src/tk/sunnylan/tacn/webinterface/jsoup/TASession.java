package tk.sunnylan.tacn.webinterface.jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TASession {
	private static Logger logger=Logger.getLogger(TASession.class.getName());
	
	private static final String TA_URL = "https://ta.yrdsb.ca/";
	private static final String STUDENT_ID_COOKIE = "student_id";
	private static final String SESSION_TOKEN_COOKIE = "session_token";
	private static final String USER_INPUT = "username";
	private static final String PASS_INPUT = "password";
	private static final String ERR_INVALID = "3";
	private static final String LOGIN_ACCEPTOR = "index.php";
	private static final String HOMEPAGE_FILE = "listReports.php";
	private static final String ERROR_KEYWORD = "?error_message=";
	private static final String KEYWORD_TABLE = "Course Name";
	private static final String LOGOUT_PAGE = "live/students/logout.php";
	private static final int LINK_COL = 2;
	private static final int HEADER_COL = 0;
	public String user;
	public String pass;
	private String session_token = "";
	private String student_id = "";
	public ArrayList<Document> subpages;

	public TASession(String user, String pass) throws Exception {
		this.user = user;
		this.pass = pass;
		refresh();
	}

	public void refresh() throws Exception {
		Response r = Util.mask(Jsoup.connect(TA_URL + LOGIN_ACCEPTOR).followRedirects(true)
				.cookie(STUDENT_ID_COOKIE, student_id).cookie(SESSION_TOKEN_COOKIE, session_token)).execute();
		Document page;
		if (r.url().toString().contains(HOMEPAGE_FILE)) {
			page = r.parse();
		} else {
			page = login(r);
		}

		refreshSubPages(page);
	}

	private Document login(Response r) throws Exception {
		r = Util.mask(Jsoup.connect(r.url().toString()).followRedirects(true)
				.data("subject_id", "0", USER_INPUT, user, PASS_INPUT, pass, "submit", "Login").method(Method.POST))
				.execute();
		if (!r.url().toString().contains(LOGIN_ACCEPTOR)) {
			student_id = r.cookie(STUDENT_ID_COOKIE);
			session_token = r.cookie(SESSION_TOKEN_COOKIE);
			return r.parse();
		}
		// determine error
		String loc = r.header("location");
		if (loc.contains(ERROR_KEYWORD)) {
			String errId = loc.substring(loc.indexOf(ERROR_KEYWORD));
			logger.log(Level.WARNING, "Error logging in. Requesting logout.php");
			logout();
			if (errId.equals(ERR_INVALID))
				throw new Exception("Invalid login");
			throw new Exception("Unidentified exception - error code " + errId);
		}
		throw new Exception("Unable to get page - unknown reason");
	}

	private void refreshSubPages(Document curr) throws Exception {
		Elements l = curr.select("th:contains(" + KEYWORD_TABLE + ")");
		Element table = l.first().parent().parent();

		if (table == null) {
			throw new Exception("Could not find table");
		}
		subpages = new ArrayList<>();
		Elements rows = table.select("tr");
		for (int i = 0; i < rows.size(); i++) {
			if (i == HEADER_COL)
				continue;
			Elements cells = rows.get(i).select("td");
			if (cells.size() > LINK_COL) {
				Element col = cells.get(LINK_COL);
				Elements links = col.select("a");
				for (Element link : links) {
					subpages.add(Util
							.mask(Jsoup.connect(link.absUrl("href")).followRedirects(true)
									.cookie(STUDENT_ID_COOKIE, student_id).cookie(SESSION_TOKEN_COOKIE, session_token))
							.get());
				}
			}
		}
	}

	public void logout() {
		session_token = "";
		student_id = "";
		try {
			Util.mask(Jsoup.connect(TA_URL + LOGOUT_PAGE)).execute();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Unable to exit session", e);
		}
	}

}
