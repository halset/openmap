// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/io/Attic/AppletDataNugget.java,v $
// $RCSfile: AppletDataNugget.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.io;

import java.io.*;
import java.net.URL;
import com.bbn.openmap.Environment;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;

/**
 * The AppletDataNugget is a hack class put together to get around the
 * problem of trying to get an applet to look inside a separate jar
 * file that contains only data files.  With an application, the
 * ClassLoader will find data files inside a jar file if the jar file
 * is in the classpath.  For an applet, though, the ClassLoader won't
 * look in a jar file unless it has a reference to a class within that
 * jar file.  That's where this class comes in.
 * <P>
 * 
 * The main function is designed to make it easy to create a
 * AppletDataNugget class to place in the jar file.  It might do that
 * for you if I can figure out how to do that.
 * <P>
 *
 * The Environment class will then look in the openmap.properties file
 * for a list of AppletDataNugget class names to provide to this class
 * at runtime.  The BinaryFile will ask this class, in a static
 * method, to look for resources based on the location of the
 * AppletDataNuggets.
 *
 * <pre>
 * #AppletDataNugget entries in properties file:
 * openmap.appletDataNuggets=AppletDataNugget_ClassName1 AppletDataNugget_ClassName2 ...
 * </pre>
 * @deprecated Not needed anymore.  BinaryFile has been modified to do the right thing.
 */
public class AppletDataNugget {

    public final static String AppletDataNuggetProperty = "openmap.appletDataNuggets";
    protected static String[] nuggetNames = null;

    private StringBuffer buffer;

    /**
     *  Create an AppletDataNugget with the class name given.  Fills
     * the StringBuffere with the class bits.
     */
    public AppletDataNugget(String className) {
	generateClass(className);
    }

    /** 
     * Given a name of a resource, provide a URL that will access
     * that resource.  This uses the AppletDataNugget names that have
     * been set in the Environment properties.
     *
     * @param name the name of the resource.  Should be a path with
     * respect to one of the AppletDataNugget locations in the data
     * jar file.
     */
    public static URL findResource(String name) {

	URL url = null;

	if (nuggetNames == null) {
	    
	    nuggetNames = LayerUtils.stringArrayFromProperties(Environment.getProperties(),
							       AppletDataNuggetProperty, " ");
	    if (Debug.debugging("datanuggets")) {
		Debug.output("Got AppletDataNuggets: " + nuggetNames);
	    }
	}
    
	if (nuggetNames != null) {
	    for (int i = 0 ; i < nuggetNames.length; i++) {
		String nugget = nuggetNames[i];

		if (Debug.debugging("datanuggets")) {
		    Debug.output("ADN: Checking the DataNugget: " + nugget);
		}
		// Check the data nugget
		Object obj = ComponentFactory.create(nugget, null);

		if (obj == null) continue;

		url = obj.getClass().getResource(name);
		
		if (Debug.debugging("datanuggets")) {
		    if (url != null) {
			Debug.output("DataNugget Worked! Got it!! -> " + url);
			break;
		    } else {
			Debug.output(" ADN: " + name + " not found via " + nugget);
		    }
		}
	    }
	}
	
	return url;

    }

    public void s(int c) { 
	buffer.append((char) c); 
    }


    /**
     * Access the StringBuffer by converting it to a String.
     */
    public String toString() {
	if (buffer != null) {
	    return buffer.toString();
	} else {
	    return null;
	}
    }

    /**
     * Generate a StringBuffer for a Java class that has the contents
     * for a given class name.
     */
    public void generateClass(String className) {
	buffer = new StringBuffer(200);
	
	s(202); // CAFEBABE
	s(254); 
	s(186); 
	s(190);
	
	s(0); // Minor version
	s(3); 
	
	s(0); // Major version
	s(45); 
	
	s(0); // Constant Pool Count
	s(10);
	
	s(10); // \n
	s(0); //  
	s(3); // 
	s(0); //  
	s(7); // 
	s(7); // 
	s(0); //  
	s(8); // 
	s(7); // 
	s(0); //  
	s(9); // 	
	s(1); // 
	s(0); //  
	s(6); // 
	s(60); // <
	s(105); // i
	s(110); // n
	s(105); // i
	s(116); // t
	s(62); // >
	s(1); // 
	s(0); //  
	s(3); // 
	s(40); // (
	s(41); // )
	s(86); // V
	s(1); // 
	s(0); //  
	s(4); // 
	s(67); // C
	s(111); // o
	s(100); // d
	s(101); // e
	s(12); // 
	s(0); //  
	s(4); // 
	s(0); //  
	s(5); // 
	s(1); // 
	s(0); //  
	
	s(className.length());		// Class name
	buffer.append(className);
	
	s(1); // 
	s(0); //  
	s(16); // 
	s(106); // j
	s(97); // a
	s(118); // v
	s(97); // a
	s(47); // /
	s(108); // l
	s(97); // a
	s(110); // n
	s(103); // g
	s(47); // /
	s(79); // O
	s(98); // b
	s(106); // j
	s(101); // e
	s(99); // c
	s(116); // t
	s(0); //  
	s(33); // !
	s(0); //  
	s(2); // 
	s(0); //  
	s(3); // 
	s(0); //  
	s(0); //  
	s(0); //  
	s(0); //  
	s(0); //  
	s(1); // 
	s(0); //  
	s(1); // 
	s(0); //  
	s(4); // 
	s(0); //  
	s(5); // 
	s(0); //  
	s(1); // 
	s(0); //  
	s(6); // 
	s(0); //  
	s(0); //  
	s(0); //  
	s(17); // 
	s(0); //  
	s(1); // 
	s(0); //  
	s(1); // 
	s(0); //  
	s(0); //  
	s(0); //  
	s(5); // 
	s(42); // *
	s(183); // ��
	s(0); //  
	s(1); // 
	s(177); // ��
	s(0); //  
	s(0); //  
	s(0); //  
	s(0); //  
	s(0); //  
	s(0); //  
    }

    /**
     * Create a AppletDataNugget, which is a package-less, empty
     * class.  Just provide the Class Name, and the class file will be
     * created.
     */
    public static void main(String[] argv) {
	if (argv.length < 1) {
	    System.out.println("Usage: java com.bbn.openmap.io.AppletDataNugget <Class Name to generate>");
	    System.out.println("  For example, DataNugget should be used to create a DataNugget.class file.");
	    System.exit(0);
	}

	for (int i = 0; i < argv.length; i++) {
	    AppletDataNugget adn = new AppletDataNugget(argv[i]);
	    try {
		File outputFile = new File(argv[i] + ".class");
		RandomAccessFile raf = new RandomAccessFile(outputFile, "rw");

		raf.writeBytes(adn.toString());
		raf.close();

		System.out.println("Created " + outputFile);

	    } catch (IOException ioe) {
		System.out.println("AppletDataNugget: can't create " +
				   argv[i] + ".class");
	    }
	}
    }
}
