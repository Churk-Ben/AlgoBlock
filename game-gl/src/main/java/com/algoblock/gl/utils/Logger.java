package com.algoblock.gl.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A versatile logger for the game engine.
 * Supports log levels, thread tracking, timestamping, and ANSI colors.
 */
public class Logger {
    public enum Level {
        TRACE(0, "\u001B[36m"), // Cyan
        DEBUG(1, "\u001B[34m"), // Blue
        INFO(2, "\u001B[32m"), // Green
        WARN(3, "\u001B[33m"), // Yellow
        ERROR(4, "\u001B[31m"); // Red

        private final int value;
        private final String colorCode;

        Level(int value, String colorCode) {
            this.value = value;
            this.colorCode = colorCode;
        }
    }

    private static final String RESET_COLOR = "\u001B[0m";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static Level currentLevel = Level.DEBUG;
    private static String defaultTag = "AlgoBlock";

    // File logging configurations
    private static boolean enableFileLog = false;
    private static String logFilePath = "algoblock.log";
    private static PrintWriter fileWriter = null;

    public static void setLevel(Level level) {
        currentLevel = level;
    }

    public static void setDefaultTag(String tag) {
        defaultTag = tag;
    }

    /**
     * Enable or disable file logging.
     * 
     * @param enable true to start writing logs to file, false to stop.
     */
    public static synchronized void setEnableFileLog(boolean enable) {
        enableFileLog = enable;
        if (enable && fileWriter == null) {
            initFileWriter();
        } else if (!enable && fileWriter != null) {
            closeFileWriter();
        }
    }

    /**
     * Set the path for the log file.
     * 
     * @param path The relative or absolute path of the log file.
     */
    public static synchronized void setLogFilePath(String path) {
        logFilePath = path;
        if (enableFileLog) {
            closeFileWriter();
            initFileWriter();
        }
    }

    private static void initFileWriter() {
        try {
            // Append mode with auto-flush
            fileWriter = new PrintWriter(new FileWriter(logFilePath, true), true);
        } catch (IOException e) {
            System.err.println("Failed to initialize file logger: " + e.getMessage());
            enableFileLog = false;
        }
    }

    private static void closeFileWriter() {
        if (fileWriter != null) {
            fileWriter.close();
            fileWriter = null;
        }
    }

    private static void log(Level level, String tag, String format, Object... args) {
        if (level.value >= currentLevel.value) {
            String time = LocalDateTime.now().format(TIME_FORMATTER);
            String threadName = Thread.currentThread().getName();
            String message = args.length > 0 ? String.format(format, args) : format;

            // Console output with color
            System.out.printf("%s[%s] [%s] [%s] [%s] %s%s%n",
                    level.colorCode, time, threadName, level.name(), tag, message, RESET_COLOR);

            // File output without color
            if (enableFileLog && fileWriter != null) {
                fileWriter.printf("[%s] [%s] [%s] [%s] %s%n",
                        time, threadName, level.name(), tag, message);
            }
        }
    }

    private static void log(Level level, String tag, Throwable t, String format, Object... args) {
        if (level.value >= currentLevel.value) {
            log(level, tag, format, args);
            t.printStackTrace(System.out);

            if (enableFileLog && fileWriter != null) {
                t.printStackTrace(fileWriter);
            }
        }
    }

    // --- TRACE ---
    public static void trace(String format, Object... args) {
        log(Level.TRACE, defaultTag, format, args);
    }

    public static void trace(String tag, String format, Object... args) {
        log(Level.TRACE, tag, format, args);
    }

    // --- DEBUG ---
    public static void debug(String format, Object... args) {
        log(Level.DEBUG, defaultTag, format, args);
    }

    public static void debug(String tag, String format, Object... args) {
        log(Level.DEBUG, tag, format, args);
    }

    // --- INFO ---
    public static void info(String format, Object... args) {
        log(Level.INFO, defaultTag, format, args);
    }

    public static void info(String tag, String format, Object... args) {
        log(Level.INFO, tag, format, args);
    }

    // --- WARN ---
    public static void warn(String format, Object... args) {
        log(Level.WARN, defaultTag, format, args);
    }

    public static void warn(String tag, String format, Object... args) {
        log(Level.WARN, tag, format, args);
    }

    public static void warn(String tag, Throwable t, String format, Object... args) {
        log(Level.WARN, tag, t, format, args);
    }

    // --- ERROR ---
    public static void error(String format, Object... args) {
        log(Level.ERROR, defaultTag, format, args);
    }

    public static void error(String tag, String format, Object... args) {
        log(Level.ERROR, tag, format, args);
    }

    public static void error(String tag, Throwable t, String format, Object... args) {
        log(Level.ERROR, tag, t, format, args);
    }
}
