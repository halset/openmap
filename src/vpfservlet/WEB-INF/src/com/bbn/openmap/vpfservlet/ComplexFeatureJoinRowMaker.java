// **********************************************************************
// <copyright>
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// </copyright>
// **********************************************************************
// $Source: /cvs/distapps/openmap/src/vpfservlet/WEB-INF/src/com/bbn/openmap/vpfservlet/ComplexFeatureJoinRowMaker.java,v $
// $Revision: 1.1 $ $Date: 2004/01/25 20:04:45 $ $Author: wjeuerle $
// **********************************************************************
package com.bbn.openmap.vpfservlet;

import java.io.File;
import java.util.*;
import javax.servlet.http.*;

import com.bbn.openmap.layer.util.html.*;
import com.bbn.openmap.layer.vpf.*;
import com.bbn.openmap.io.FormatException;

/**
 * A RowMaker that performs the join in a feature index table.
 */
public class ComplexFeatureJoinRowMaker extends PlainRowMaker {
    /** a list reused to load primitive rows */
    final List primRow = new ArrayList();
    /** a list reused to load feature rows */
    final List featureRow = new ArrayList();

    /** the array of feature tables, each index is lazy-initialized */
    final DcwRecordFile[] featureTables;

    /**
     * Construct a rowmaker for a complex feature join.
     * @param drf the table
     * @throws FormatException some error was encountered
     */
    public ComplexFeatureJoinRowMaker(DcwRecordFile drf) throws FormatException {
	featureTables = getTables(drf);
    }

    public void addToRow(TableRowElement row, List l) {
	try {
	    boolean color1 = false;
	    int i = 0;
	    for (Iterator li = l.iterator(); li.hasNext(); ) {
		Object o = li.next();
		DcwRecordFile featureTable = featureTables[i++];
		if ((featureTable != null) &&
		    featureTable.getRow(featureRow, VPFUtil.objectToInt(o))) {
		    color1 = !color1;
		    for (Iterator fi = featureRow.iterator(); fi.hasNext(); ) {
			row.addElement(new TableDataElement(color1 ? "CLASS=JoinColumn" : "CLASS=Join2Column",
							    fi.next().toString()));
		    }
		} else {
		    row.addElement(o.toString());
		}
	    }
	} catch (FormatException fe) {
	    row.addElement(fe.toString());
	}
    }

    public DcwRecordFile[] getTables(DcwRecordFile drf) throws FormatException {
	DcwRecordFile[] retval = new DcwRecordFile[drf.getColumnCount()];
	File dirPath = new File(drf.getTableFile()).getParentFile();
	File fcsfile = new File(dirPath, "fcs");
	if (!fcsfile.canRead()) {
	    fcsfile = new File(dirPath, "fcs.");
	}
	DcwRecordFile fcs = new DcwRecordFile(fcsfile.toString());
	List l = new ArrayList(fcs.getColumnCount());
	String tableName = drf.getTableName();

	int table1Column = fcs.whatColumn("table1");
	int table1_keyColumn = fcs.whatColumn("table1_key");
	int table2Column = fcs.whatColumn("table2");
	int table2_keyColumn = fcs.whatColumn("table2_key");

	while (fcs.parseRow(l)) {
	    String table1 = (String)l.get(table1Column);
	    String table1_key = (String)l.get(table1_keyColumn);
	    String table2 = (String)l.get(table2Column);
	    String table2_key = (String)l.get(table2_keyColumn);
	    if (table1.equalsIgnoreCase(tableName) &&
		table2_key.equalsIgnoreCase("id")) {
		int indexCol = drf.whatColumn(table1_key);
		retval[indexCol] = new DcwRecordFile(dirPath + File.separator + table2);
	    }
	}

	fcs.close();
	return retval;
    }

    public void close() {
	for (int i = 0; i < featureTables.length; i++) {
	    DcwRecordFile drf = featureTables[i];
	    if (drf != null) {
		drf.close();
	    }
	}
    }
}

