package tk.sunnylan.tacn.tst;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.swing.JOptionPane;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class CONFIG {
	private static final String root = new File("").getAbsolutePath() + "\\";
	public static final String logpath = root + "cache\\logs\\";
	private static final Logger logger = Logger.getLogger(CONFIG.class.getName());
	// REMEMBER TO SET THIS TO FALSE!!! WILL DISABLE SSL IF SET TO TRUE
	public static final boolean USE_PROXY = false;
	public static final boolean DEBUG_MODE = false;
	private static final boolean LOG_TO_CONSOLE = false;
	public static final String PROXY_HOST = "127.0.0.1";
	public static final int PROXY_PORT = 8888;
	private static final Level LOG_LEVEL = Level.ALL;
	private static final boolean LOG_FILTER_ON = true;
	private static final String UPDATE_SITE = "http://sunnylan.tk/tyanide/version.html";
	public static final String VERSION_MAJOR = "2";
	public static final String VERSION_MINOR = "4";
	public static final String VERSION_BUILD = "0";
	public static final String CURRENT_VERSION = VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_BUILD;

	public static void initDebug() {
		try {
			Logger rootLogger = Logger.getLogger("");
			if (!LOG_TO_CONSOLE) {
				rootLogger.removeHandler(rootLogger.getHandlers()[0]);
			}
			File targetFile = new File(logpath + "\\" + "log.xml");
			File parent = targetFile.getParentFile();
			if (!parent.exists() && !parent.mkdirs()) {
				throw new IllegalStateException("Couldn't create dir: " + parent);
			}
			FileHandler fh = new FileHandler(logpath + "\\" + "log.xml");

			rootLogger.addHandler(fh);
			rootLogger.setLevel(LOG_LEVEL);
			if (LOG_FILTER_ON) {
				Filter filt = new Filter() {
					@Override
					public boolean isLoggable(LogRecord record) {
						if (record.getLevel() == Level.SEVERE)
							return true;
						if (record.getLoggerName().startsWith("tk.sunnylan.tacn"))
							return true;
						return false;
					}
				};
				fh.setFilter(filt);
			}

		} catch (SecurityException | IOException e) {
			throw new RuntimeException("Could not initialize logger");
		}

		if (CONFIG.USE_PROXY) {
			logger.info("WARNING: USING PROXY - CONNECTION MAY BE INSECURE");
			SSLUtilities.trustAllHostnames();
			SSLUtilities.trustAllHttpsCertificates();
			System.setProperty("http.proxyHost", CONFIG.PROXY_HOST); // set
																		// proxy
																		// server
			System.setProperty("http.proxyPort", CONFIG.PROXY_PORT + ""); // set
																			// proxy
																			// port
			System.setProperty("https.proxyHost", CONFIG.PROXY_HOST); // set
																		// proxy
																		// server
			System.setProperty("https.proxyPort", CONFIG.PROXY_PORT + ""); // set
																			// proxy
																			// port
		}
	}

	public static void checkUpdates() {
		logger.log(Level.INFO, "Checking for updates");
		Document d;
		try {
			d = Jsoup.connect(UPDATE_SITE).get();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not connect to update site", e);
			return;

		}
		Elements s = d.select("#version");
		if (s.size() > 0) {
			String version = s.first().text().trim();
			String[] split = version.split("\\.");
			if (split.length != 3) {
				logger.log(Level.WARNING, "Invalid version specifier", split);
				return;
			}
			if (!split[0].equals(VERSION_MAJOR) || !split[1].equals(VERSION_MINOR)) {
				try {
					Thread t = new Thread(() -> JOptionPane.showMessageDialog(null,
							"Downloading updates...please wait.", "Update", JOptionPane.INFORMATION_MESSAGE));
					t.start();
					downloadUpdates(version);
					t.interrupt();
					logger.info("Successfully downloaded updates.  Restarting...");
					try {
						Runtime.getRuntime().exec("java -jar Tyanide" + version + ".jar");
					} catch (Exception e) {
						logger.log(Level.WARNING, "Unable to start new version. reverting", e);
						return;
					}
					System.exit(0);
				} catch (IOException e) {
					logger.log(Level.WARNING, "Unable to download update", e);
				}
				return;
			} else {
				if (!split[2].equals(VERSION_BUILD)) {
					logger.log(Level.WARNING, "New build is availible");
					return;
				}
			}
			try (Stream<Path> paths = Files.walk(Paths.get(root))) {
				paths.forEach(filePath -> {
					if (Files.isRegularFile(filePath)) {
						if (filePath.getFileName().toString().startsWith("Tyanide")) {
							if (!filePath.getFileName().toString().substring("Tyanide".length())
									.equals(version + ".jar")) {
								try {
									Files.delete(filePath);
								} catch (IOException e) {
									logger.log(Level.WARNING, "Unable to delete " + filePath, e);
								}
							}
						}
					}
				});
			} catch (IOException e) {
				logger.log(Level.WARNING, "Unable to delete old version", e);
			}
		} else {
			logger.log(Level.WARNING, "Unable to parse version website");
		}
	}

	private static void downloadUpdates(String version) throws IOException {
		URL website = new URL("http://sunnylan.tk/tyanide/Tyanide.jar");
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		FileOutputStream fos = new FileOutputStream(root + "Tyanide" + version + ".jar");
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		// fos.close();
	}
}
