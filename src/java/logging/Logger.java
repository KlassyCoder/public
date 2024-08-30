import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Utility class for logging messages with different levels of severity. Provides functionalities to handle logging
 * level configuration and message formatting.
 */
public class Logger {

    /**
     * Log entry formatter
     */
    static final private SimpleDateFormat _logEntryFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Stores the logging level
     */
    static final private ThreadLocal<String> _logLevel = new ThreadLocal<>();

    /**
     * Stores the default log level
     */
    static public String defaultLogLevel;

    /**
     * Inits the log level with default value if not provided
     */
    static public void init() {
        init(null);
    }

    /**
     * Inits the log level
     *
     * @param logLevel The log level to be set; if null, the default log level is used.
     * @throws RuntimeException if the provided log level is invalid.
     */
    static public void init(String logLevel) {
        if (logLevel == null) {
            logLevel = defaultLogLevel;
        }

        ArrayList<String> validLogLevels = new ArrayList<>();
        validLogLevels.add(LogLevel.DEBUG);
        validLogLevels.add(LogLevel.INFO);
        validLogLevels.add(LogLevel.WARN);
        validLogLevels.add(LogLevel.ERROR);

        logLevel = logLevel.toUpperCase();

        if (validLogLevels.contains(logLevel)) {
            _logLevel.set(logLevel);
        } else {
            throw new RuntimeException("Invalid log level: " + logLevel);
        }
    }

    /**
     * Log levels
     */
    static public class LogLevel {
        static public String DEBUG = "DEBUG";
        static public String INFO = "INFO";
        static public String WARN = "WARN";
        static public String ERROR = "ERROR";
    }

    /**
     * Writes out a debug message
     *
     * @param message The message to be logged.
     */
    static public void debug(String message) {
        _write(LogLevel.DEBUG, message);
    }

    /**
     * Writes out an info message
     *
     * @param message The message to be logged.
     */
    static public void info(String message) {
        _write(LogLevel.INFO, message);
    }

    /**
     * Writes out a warning message
     *
     * @param message The message to be logged.
     */
    static public void warning(String message) {
        _write(LogLevel.WARN, message);
    }

    /**
     * Writes out an error message
     *
     * @param message The message to be logged.
     */
    static public void error(String message) {
        _write(LogLevel.ERROR, message);
    }

    /**
     * Writes out a message
     *
     * @param level   The level of the log.
     * @param message The message to be logged.
     */
    static private void _write(String level, String message) {
        String minLogLevel = _logLevel.get();

        // if there's no min log level, init in this thread
        if ((minLogLevel == null) && (defaultLogLevel != null)) {
            init();
            minLogLevel = _logLevel.get();
        }

        if (minLogLevel != null) {
            if (minLogLevel.equals(LogLevel.INFO)) {
                if (level.equals(LogLevel.DEBUG)) {
                    return;
                }
            }

            if (minLogLevel.equals(LogLevel.WARN)) {
                if (level.equals(LogLevel.DEBUG) ||
                        level.equals(LogLevel.INFO)) {
                    return;
                }
            }

            if (minLogLevel.equals(LogLevel.ERROR)) {
                if (level.equals(LogLevel.DEBUG) ||
                        level.equals(LogLevel.INFO) ||
                        level.equals(LogLevel.WARN)) {
                    return;
                }
            }
        }

        String threadName = Thread.currentThread().getName();
        String stackTraceText = _callerStackTrace();
        String dateText = _logEntryFormat.format(new Date());

        StringBuilder builder = new StringBuilder();
        builder.append(dateText).append(" ");
        builder.append(threadName).append(" ");
        builder.append(minLogLevel == null ? "*" : "");
        builder.append(level).append(" ");
        builder.append(stackTraceText).append(" ");
        builder.append(message);

        System.out.println(builder);
    }

    /**
     * Returns the caller stack trace
     *
     * @return The caller stack trace information.
     */
    static private String _callerStackTrace() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();

        String loggerClassName = Logger.class.getName();
        boolean startLooking = false;
        String caller = null;

        for (StackTraceElement element : elements) {
            String className = element.getClassName();

            if (startLooking && !className.startsWith(loggerClassName)) {
                String[] classNameParts = className.split("\\.");
                String simpleClassName = classNameParts[classNameParts.length - 1];
                String methodName = element.getMethodName();
                int lineNumber = element.getLineNumber();

                caller = "[" + simpleClassName + "." + methodName + "()]:" + lineNumber;
                break;
            }

            if (className.startsWith(loggerClassName)) {
                startLooking = true;
            }
        }

        return caller;
    }


}
