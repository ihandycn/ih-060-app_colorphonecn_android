package colorphone.acb.com.libscreencard.gif;

/**
 * Central list of files the Security writes to the application data directory.
 * <p>
 * To add a new Security file, create a String constant referring to the filename, and add it to
 * ALL_FILES, as shown below.
 */
public class SecurityFiles {

    public static final String REAL_TIME_PROTECTION_PREFS = "com.fasttrack.security.real_time.protection.prefs"; // Main process
    public static final String SECURITY_PROTECTION_PREFS = "com.fasttrack.security.security.protection.prefs"; // Main process

}
