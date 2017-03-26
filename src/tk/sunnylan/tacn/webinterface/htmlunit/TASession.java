package tk.sunnylan.tacn.webinterface.htmlunit;

import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

public class TASession {
	static final String KEYWORD = "Course Name";
	static final int LINK_COL = 2;
	private TALoginClient client;
	public ArrayList<HtmlPage> subpages;

	public TASession(TALoginClient client) {
		this.client = client;
	}

	public void refresh() throws Exception {
		client.refresh();
		DomNodeList<DomElement> tables = client.page.getElementsByTagName("table");
		HtmlTable table = null;
		for (DomElement elem : tables) {
			if (elem.getTextContent().contains(KEYWORD)) {
				table = (HtmlTable) elem;
				break;
			}
		}
		
		if (table == null) {
			throw new Exception("Could not find table");
		}
		subpages = new ArrayList<>();
		for (HtmlTableRow row : table.getRows()) {
			List<HtmlTableCell> cells = row.getCells();
			if (cells.size() > LINK_COL) {
				HtmlTableCell cell = row.getCell(LINK_COL);
				DomNodeList<HtmlElement> sub = cell.getElementsByTagName("a");
				for (HtmlElement e : sub) {
					subpages.add(((HtmlAnchor) e).click());
				}
			}
		}
	}
}