// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/Debug.java,v $
// $RCSfile: Debug.java,v $
// $Revision: 1.6 $
// $Date: 2004/12/08 01:10:45 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util;

import java.applet.Applet;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

/**
 * An abstract class that presents a static interface for debugging
 * output. It also provides a way to direct output into a log. There
 * are two types of output - the regular output information, and the
 * error information, and they can be handled separately. There are
 * two differences between the two - the error file only gets created
 * if there is an error, and the error messages have a header and a
 * tail surrounding the messages, making them easier to spot. If the
 * output and error file are set to be the same (setLogFile()), then
 * that file is created automatically, regardless of anything being
 * put into it.
 * <p>
 * Debugging output is turned on or off by system properties for
 * applications, or parameters for applets.
 * <p>
 * A programmer can use code like the following:
 * <p>
 * <code><pre>
 * if (Debug.debugging(&quot;foo&quot;)) {
 *     System.out.println(&quot;Got &quot; + nbytes + &quot; bytes of data.&quot;);
 * }
 * </pre></code>
 * <p>
 * The message gets printed when the application is run with
 * <code>-Ddebug.foo</code> or when the applet gets run with:
 * <p>
 * <code>&lt;param name=debug.foo value=&gt;</code>
 * <p>
 * The special token <code>debug.all</code> turns on all debugging
 * for both applets and applications.
 * 
 * @author Tom Mitchell (tmitchell@bbn.com)
 * @author $Author: dietrick $
 * @version $Revision: 1.6 $, $Date: 2004/12/08 01:10:45 $
 * @deprecated use slf4j directly instead of this class
 */
@Deprecated
public abstract class Debug {

    public static String ERROR_HEADER = "\n*** ERROR ***";
    public static String ERROR_TAIL = "*************";

    /**
     * Don't allow construction, all methods are static.
     */
    private Debug() {}

    /**
     * Globally enable or disable debugging.
     */
    public static final boolean On = true;
    /**
     * The flag for whether the output stream should still be notified
     * if logging output.
     */
    protected static boolean notifyOut = true;
    /**
     * The flag for whether the err stream should still be notified if
     * logging errors.
     */
    protected static boolean notifyErr = true;
    /**
     * Flag to have the errors appended to the error log.
     */
    protected static boolean errorAppend = false;
    /**
     * Flag to indicate whether all debug messages should get printed.
     * This is shorthand for defining all the debug symbols.
     */
    public static boolean debugAll = false;
    /**
     * The user specified flag to indicate all debugging is on.
     * Default is "all".
     */
    public static String debugAllToken = "all";

    private static final Map<String, Boolean> dbgTable = new ConcurrentHashMap<>();
    private static String debugTokenHeader = "debug.";

    /**
     * Initialize debugging for the given applet. Applets must pass an
     * array of parameters because the applet Parameters list cannot
     * be accessed in whole, only queried. The parameters list looks
     * something like this:
     * <p>
     * 
     * <pre><code>
     * String[] debugTokens = { &quot;debug.debug&quot;, // com.bbn.openmap.Debug
     *         &quot;debug.openmap&quot;, // com.bbn.openmap.client.OpenMap
     *         &quot;debug.mappanel&quot;, // com.bbn.openmap.awt.MapPanel
     *         &quot;debug.awt&quot;, // com.bbn.openmap.awt.*
     *         &quot;debug.map&quot;, // com.bbn.openmap.Map
     *         &quot;debug.layer&quot;, // com.bbn.openmap.Layer
     *         &quot;debug.proj&quot;, // com.bbn.openmap.proj.*
     *         &quot;debug.spec&quot;, // com.bbn.openmap.spec.*
     *         &quot;debug.env&quot; // com.bbn.openmap.Environment
     * };
     * </code></pre>
     * 
     * @param applet The applet
     * @param parameters The debugging flags to look for in the
     *        applet's parameters list
     */
    public static void init(Applet applet, String[] parameters) {
        if (applet == null) {
            // handle a SecurityException in case we are an applet
            // but no applet was passed as an argument.
            try {
                init(System.getProperties());
            } catch (SecurityException e) {
            }
        } else if (parameters != null) {
            try {
                for (int i = 0; i < parameters.length; i++) {
                    String pname = parameters[i];
                    if (pname.startsWith(debugTokenHeader)
                            && (applet.getParameter(parameters[i]) != null)) {
                        String token = pname.substring(debugTokenHeader.length());
                        dbgTable.put(token, Boolean.TRUE);
                    }
                }
                // look for special debug.all token!
                if (applet.getParameter(debugTokenHeader + debugAllToken) != null) {
                    dbgTable.put(debugAllToken, Boolean.TRUE);
                }
            } catch (NullPointerException npe) {
            }
        }

        Debug.postInit();
    }

    /**
     * Initialize debugging for an application. Debugging symbols are
     * detected in the given properties list, and must have the form
     * "debug.X", where X is a debug token used in the application.
     * 
     * @param p A properties list, usually System.getProperties()
     */
    public static void init(Properties p) {
        Enumeration e = p.propertyNames();
        while (e.hasMoreElements()) {
            String name = e.nextElement().toString();
            if (name.startsWith(debugTokenHeader)) {
                String token = name.substring(debugTokenHeader.length());
                dbgTable.put(token, Boolean.TRUE);
            }
        }
        Debug.postInit();
    }

    /**
     * Initialize debugging from the system properties.
     */
    public static void init() {
        Properties p;
        try {
            p = System.getProperties();
        } catch (java.security.AccessControlException ace) {
            p = new Properties();
        }

        init(p);
    }

    /**
     * Common inits, regardless of applet or application.
     */
    private static void postInit() {
        debugAll = dbgTable.containsKey(debugAllToken);
    }

    /**
     * Indicates if the debugging for the named token is on or off.
     * 
     * @param token a candidate token
     * @return true if debugging is on, false otherwise.
     */
    public static boolean debugging(String token) {
        return Debug.On && (debugAll || dbgTable.containsKey(token));
    }

    /**
     * Installs a new debug token
     * 
     * @param dbgToken token name
     */
    public static void put(String dbgToken) {
        dbgTable.put(dbgToken, Boolean.TRUE);
    }

    /**
     * Rremoves a debug token
     * 
     * @param dbgToken token name
     */
    public static void remove(String dbgToken) {
        dbgTable.remove(dbgToken);
    }

    /**
     * Prints <code>message</code> if <code>dbgToken</code>
     * debugging is on. NOTE, WARNING!: this is a potentially
     * expensive method if you pass a message String composed of many
     * concatenated pairs. For example, like: <br>
     * `onceStr+" "+uponStr+" a "+timeStr+", "+ ... +"\nThe end."'
     * <br>
     * Instead you should do: <code><pre>
     * 
     *   if (Debug.debugging(dbgToken)) {
     *       Debug.output(onceStr+&quot; &quot;+uponStr+&quot; a &quot;+timeStr+&quot;, &quot;+ ... +&quot;\nThe end.&quot;);
     *   }
     *  
     * </pre></code>
     * 
     * @param dbgToken a token to be tested by debugging()
     * @param message a message to be printed
     */
    public static void message(String dbgToken, String message) {
        if (Debug.On && Debug.debugging(dbgToken)) {
            Debug.output(message);
        }
    }

    /**
     * Provide a file to log output. This can be in conjunction with
     * the output stream, or instead of it. Will overwrite the file, if
     * it exists.
     * 
     * @param file the file to use for the error log.
     * @param alsoToOutStream true if the out stream should still
     *        provide output, in addition to logging the output.
     */
    public static void directOutput(File file, boolean alsoToOutStream) {
        throw new UnsupportedOperationException("not supported");
    }

    /**
     * Provide a file to log output. This can be in conjunction with
     * the output stream, or instead of it.
     * 
     * @param filename the file to use for the error log.
     * @param append if true, log the output at the end of the file,
     *        instead of the beginning.
     * @param alsoToOutStream true if the out stream should still
     *        provide output, in addition to logging the output.
     */
    public static void directOutput(String filename, boolean append,
                                    boolean alsoToOutStream) {
        throw new UnsupportedOperationException("not supported");
    }

    /**
     * Provide a DataOutputStream to log output. This can be in
     * conjunction with the output stream, or instead of it.
     * 
     * @param os the OutputStream that's handling outputlogging.
     * @param alsoToOutStream true if the out stream should still
     *        provide output, in addition to logging the output.
     */
    public static void directOutput(OutputStream os, boolean alsoToOutStream) {
        throw new UnsupportedOperationException("not supported");
    }

    /**
     * Sets the error output stream to the named stream.
     * 
     * @param err the desired error output stream
     */
    public static void setErrorStream(PrintStream err) {
    }

    /**
     * Accessor for the current error output stream.
     * 
     * @return the current error output stream.
     */
    public static PrintStream getErrorStream() {
        throw new UnsupportedOperationException("not supported");
    }

    /**
     * Provide a file to log errors. This can be in conjunction with
     * the errorstream, or instead of it.
     * 
     * @param file the file to use for the error log.
     * @param alsoToErrStream true if the err stream should still
     *        provide output, in addition to logging the errors.
     */
    public static void directErrors(File file, boolean alsoToErrStream) {
        throw new UnsupportedOperationException("not supported");
    }

    /**
     * Provide a file to log errors. This can be in conjunction with
     * the errorstream, or instead of it.
     * 
     * @param filename the file to use for the error log.
     * @param append if true, log the output at the end of the file,
     *        instead of the beginning.
     * @param alsoToErrStream true if the err stream should still
     *        provide output, in addition to logging the errors.
     */
    public static void directErrors(String filename, boolean append,
                                    boolean alsoToErrStream) {
        throw new UnsupportedOperationException("not supported");
    }

    /**
     * Provide a DataOutputStream to log errors. This can be in
     * conjunction with the errorstream, or instead of it.
     * 
     * @param os the DataOutputStream handling error logging.
     * @param alsoToErrStream true if the err stream should still
     *        provide output, in addition to logging the errors.
     */
    public static void directErrors(OutputStream os, boolean alsoToErrStream) {
        throw new UnsupportedOperationException("not supported");
    }

    /**
     * Handle error messages, buy writing them to an error log, if
     * that has been set up, and/or to the error stream, if requested.
     * The special thing about error output is that the error is
     * framed with a header and a tail, hopefully to make it easier
     * for someone to spot in the log.
     * 
     * @param errorString the string to write as an error.
     */
    public static void error(String errorString) {
        LoggerFactory.getLogger(Debug.class).error(errorString);
    }

    /**
     * Handle output messages, buy writing them to an output log, if
     * that has been set up, and/or to the output stream, if
     * requested.
     * 
     * @param outputString the string to write as output.
     */
    public static void output(String outputString) {
        LoggerFactory.getLogger(Debug.class).info(outputString);
    }

    /**
     * Provide a file to log output. This can be in conjunction with
     * the streams, or instead of them. This basically sets the output
     * log and the error log to be the same thing.
     * 
     * @param file the file to use for the error log.
     * @param alsoToStreams true if the streams should still provide
     *        output, in addition to logging the output.
     */
    public static void setLog(File file, boolean alsoToStreams) {
        throw new UnsupportedOperationException("not supported");
    }

    /**
     * Provide an output stream to log output. This can be in
     * conjunction with the streams, or instead of them. This
     * basically sets the output log and the error log to be the same
     * thing.
     * 
     * @param logStream the output stream for output.
     * @param alsoToStreams true if the streams should still provide
     *        output, in addition to logging the output.
     */
    public static void setLog(OutputStream logStream, boolean alsoToStreams) {
        throw new UnsupportedOperationException("not supported");
    }
}