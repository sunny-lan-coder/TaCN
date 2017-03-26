package tk.sunnylan.tacn.gui.taui1;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map.Entry;

import com.jfoenix.controls.JFXDrawer;

import javafx.animation.Transition;
import tk.sunnylan.tacn.parse.htmlunit.ChangeType;
import tk.sunnylan.tacn.parse.htmlunit.SubjectChange;
import tk.sunnylan.tacn.parse.htmlunit.Update;

public class Util {
	public static void antiAlias() {
		System.setProperty("prism.lcdtext", "false");
		System.setProperty("prism.text", "t2k");
	}

	private static SecureRandom random = new SecureRandom();

	private static String nextSessionId() {
		return new BigInteger(130, random).toString(32);
	}

	public static String genRandomProfileName(int len) {
		return nextSessionId().substring(0, len);
	}

	public static void removeDirectory(File dir) {
		if (!dir.exists())
			return;
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			if (files != null && files.length > 0) {
				for (File aFile : files) {
					removeDirectory(aFile);
				}
			}
			dir.delete();
		} else {
			dir.delete();
		}
	}

	public static Transition getDrawerTransition(JFXDrawer drawer) {
		try {
			Class<?> c = drawer.getClass();
			Field drawerTrans = c.getDeclaredField("drawerTransition");
			drawerTrans.setAccessible(true);
			return (Transition) drawerTrans.get(drawer);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	static final int DISP_MAX_LEN = 40;

	public static String summarizeUpdates(Update u) {
		if (u.updates.size() == 1 && u.additions.size() == 0) {
			// should only loop once
			for (Entry<String, SubjectChange> s : u.updates.entrySet()) {
				return summarizeSubjectChange(s.getValue());
			}
		}
		String out = "";
		int cntleft = u.updates.size() + u.additions.size();
		for (Entry<String, SubjectChange> f : u.updates.entrySet()) {
			if (out.length() >= DISP_MAX_LEN)
				break;
			out += f.getKey();
			cntleft--;
			if (cntleft > 0)
				out += ", ";
		}
		for (Entry<String, SubjectChange> f : u.additions.entrySet()) {
			if (out.length() >= DISP_MAX_LEN)
				break;
			out += f.getKey();
			cntleft--;
			if (cntleft > 0)
				out += ", ";
		}
		if (cntleft > 0)
			out += "and " + p("other course", cntleft);
		return out;
	}

	public static String summarizeSubjectChange(SubjectChange s) {
		if (s.changes.size() == 1) {
			for (Entry<String, ChangeType> f : s.changes.entrySet()) {
				return f.getKey() + " " + f.getValue().toString();
			}
		}
		String out = "";
		int cntleft = s.changes.size();
		for (Entry<String, ChangeType> f : s.changes.entrySet()) {
			if (out.length() >= DISP_MAX_LEN)
				break;
			out += f.getKey();
			cntleft--;
			if (cntleft > 0)
				out += ", ";
		}
		if (cntleft > 0)
			out += "and " + p("other assignment", cntleft);
		return out;
	}

	public static String summarizeUpdatesShort(Update u) {
		if (u.updates.size() == 1 && u.additions.size() == 0) {
			for (Entry<String, SubjectChange> s : u.updates.entrySet()) {
				return s.getKey() + " updated";
			}
		}
		if (u.updates.size() == 0 && u.additions.size() == 1) {
			for (Entry<String, SubjectChange> s : u.additions.entrySet()) {
				return s.getKey() + " available";
			}
		}
		if (u.updates.size() > 0) {
			return "Marks updated";
		}
		if (u.additions.size() > 0) {
			return "New marks available";
		}
		return "Tyanide";
	}

	// pluralise function
	private static String p(String s, int num) {
		if (num != 1)
			s += "s";
		return num + " " + s;
	}
}
