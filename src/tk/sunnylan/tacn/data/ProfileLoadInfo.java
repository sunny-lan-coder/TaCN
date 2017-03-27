package tk.sunnylan.tacn.data;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ProfileLoadInfo {
	public String cachepath;
	public String profileName;
	public String username, password;
	public boolean isCached;
	public boolean hasUsername;
	public boolean hasPassword;
	public boolean isSynced;

	public ProfileLoadInfo(String profileName, boolean synced) {
		this.profileName = profileName;
		this.isSynced=synced;
	}

	public ProfileLoadInfo(Element docroot) {
		NodeList l = docroot.getElementsByTagName("name");
		profileName = l.item(0).getTextContent().trim();
		l = docroot.getElementsByTagName("cachepath");
		if (l.getLength() > 0) {
			cachepath = l.item(0).getTextContent();
			isCached = true;
		}
		l = docroot.getElementsByTagName("username");
		if (l.getLength() > 0) {
			username = l.item(0).getTextContent();
			hasUsername = true;
		}
		l = docroot.getElementsByTagName("password");
		if (l.getLength() > 0) {
			password = l.item(0).getTextContent();
			hasPassword = true;
		}
		l = docroot.getElementsByTagName("synced");
		if (l.getLength() > 0) {
			isSynced = true;
		}
	}

	@Override
	public String toString() {
		String res = "";
		res += "<name>" + profileName + "</name>";
		if (isCached)
			res += "<cachepath>" + cachepath + "</cachepath>";
		if (hasUsername)
			res += "<username>" + username + "</username>";
		if (hasPassword)
			res += "<password>" + password + "</password>";
		if (isSynced)
			res += "<synced/>";

		return res;
	}
}
