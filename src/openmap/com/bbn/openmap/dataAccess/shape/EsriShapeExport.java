/*
 *  File: EsriShapeExport.java
 *  OptiMetrics, Inc.
 *  2107 Laurel Bush Road - Suite 209
 *  Bel Air, MD 21015
 *  (410)569 - 6081
 */
package com.bbn.openmap.dataAccess.shape;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.MalformedURLException;
import java.util.*;
import javax.swing.*;

import com.bbn.openmap.Environment;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.dataAccess.shape.output.*;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.FileUtils;
import com.bbn.openmap.util.PropUtils;

/**
 * Provides methods for saving OMGraphicLists as ShapeFiles.  This
 * code was originally submitted by Karl Stuempfle of OptiMetrics, and
 * I modified it a little to add a user interface to modify the DBF
 * files if the user wants to.<P> 
 *
 * Since Shape files can only hold one type of graphic, this class
 * will create one to three different lists as needed, for points,
 * lines and polygons. <P>
 *
 * If the OMGraphicList's AppObject holds a DbfTableModel, it will be
 * used for the shape file database file.
 */
public class EsriShapeExport implements ShapeConstants, OMGraphicConstants {

    /**
     * The source graphics to write to a shape file.
     */
    protected OMGraphicList graphicList = null;
    
    /**
     * The optional DbfTableModel that describes properties for the
     * OMGraphics.  This should be set in the AppObject of the
     * OMGraphicList.
     */
    protected DbfTableModel masterDBF = null;

    /**
     * The projection needed to convert other OMGraphicTypes to
     * polygons.
     */
    protected Projection projection;

    /**
     * The path where the shape files should be written.
     */
    protected String filePath;

    /**
     * Gets set automatically if Debug.debugging("shape");
     */
    protected boolean DEBUG = false;

    /**
     * A list of ESEInterface classes, holding information for
     * different type ESRIGraphicLists created from the OMGraphicList.
     */
    protected ArrayList eseInterfaces = new ArrayList();

    /**
     * Flag for whether the DBF file should be written when the
     * OMGraphicList is exported to a .shp/.shx file.  The .dbf file
     * will be created if set to true, and this is true by default.
     */
    protected boolean writeDBF = true;

    /**
     * Flad to note whether, if a DbfTableModel is set, to add the
     * rendering information (DrawingAttributes contents) about the
     * OMGraphics to the contents of the DbfTableModel.  False by
     * default.  Doesn't do anything yet.
     */
    protected boolean dbfHasRenderingInfo = false;

    /**
     * Create an EsriShapeExport object.
     * @param list the OMGraphicList to export.
     * @param proj the Projection of the map, needed to convert some
     * OMGraphic types to OMPolys.
     * @param pathToFile the file path of the shape file to save to.
     * If null, the user will be queried.  If not null, the files will
     * be saved without any GUI confirmation.
     */
    public EsriShapeExport(OMGraphicList list, 
                           Projection proj, 
                           String pathToFile) {

        setGraphicList(list);
        projection = proj;
        filePath = pathToFile;
        DEBUG = Debug.debugging("shape");
    }

    /**
     * Create an EsriShapeExport object.
     * @param list the EsriGraphicList to export.
     * @param dbf the DbfTableModel holding the attributes for the list objects.
     * @param pathToFile the file path of the shape file to save to.
     * If null, the user will be queried.  If not null, the files will
     * be saved without any GUI confirmation.
     */
    public EsriShapeExport(EsriGraphicList list, 
                           DbfTableModel dbf,
                           String pathToFile) {

        setGraphicList(list);
        setMasterDBF(dbf);
        filePath = pathToFile;
        DEBUG = Debug.debugging("shape");
    }

    /**
     * Set the OMGraphicList to use for export.  If the AppObject in
     * the OMGraphicList holds a DbfTableModel, it will be used in the
     * export.
     */
    public void setGraphicList(OMGraphicList list) {
        graphicList = list;

        if (list != null) {
            Object obj = list.getAppObject();
            if (obj instanceof DbfTableModel) {
                masterDBF = (DbfTableModel) obj;
                Debug.message("shape", "Setting master DBF in ESE");
            }
        }
    }

    public OMGraphicList getGraphicList() {
        return graphicList;
    }

    public void setProjection(Projection proj) {
        projection = proj;
    }

    public Projection getProjection() {
        return projection;
    }

    public void setFilePath(String pathToFile) {
        filePath = pathToFile;
    }

    public String getFilePath() {
        return filePath;
    }

    protected EsriPolygonList polyList = null;
    protected EsriPolylineList lineList = null;
    protected EsriPointList pointList = null;

    /**
     * Return the polygon list, create it if needed.
     */
    protected EsriPolygonList getPolyList() {
        if (polyList == null) {
            polyList = new EsriPolygonList();
            polyList.setTable(getMasterDBFHeaderClone());
        }
        return polyList;
    }

    /**
     * Return the line list, create it if needed.
     */
    protected EsriPolylineList getLineList() {
        if (lineList == null) {
            lineList = new EsriPolylineList();
            lineList.setTable(getMasterDBFHeaderClone());
        }
        return lineList;
    }

    /**
     * Return the point list, create it if needed.  If the masterDBF
     * object exists, then a new one is created, which matching
     * structure, and put in the AppObject of the new list that is
     * returned.  If there isn't a masterDBF object, then the
     * AppObject is set to null, and a default one will be created.
     */
    protected EsriPointList getPointList() {
        if (pointList == null) {
            pointList = new EsriPointList();
            pointList.setTable(getMasterDBFHeaderClone());
        }
        return pointList;
    }

    /**
     * Add a graphic to the list, and add the record to the list's
     * DbfTableModel if both exist.
     */
    protected void addGraphic(EsriGraphicList egl,
                              OMGraphic graphic, 
                              ArrayList record) {
        egl.add(graphic);
        DbfTableModel dtm = egl.getTable();
        if (dtm != null && record != null) {
            dtm.addRecord(record);
        }
    }

    /** Scoping method to call addGraphic with the right list. */
    protected void addPolygon(OMGraphic graphic, ArrayList record) {
        addGraphic(getPolyList(), graphic, record);
    }

    /** Scoping method to call addGraphic with the right list. */
    protected void addLine(OMGraphic graphic, ArrayList record) {
        addGraphic(getLineList(), graphic, record);
    }

    /** Scoping method to call addGraphic with the right list. */
    protected void addPoint(OMGraphic graphic, ArrayList record) {
        addGraphic(getPointList(), graphic, record);
    }

    /**
     * Set the DbfTableModel representing the dbf file for the main
     * OMGraphicList.  Can also be passed to this object as an
     * appObject within the top level OMGraphicList.
     */
    public void setMasterDBF(DbfTableModel dbf) {
        masterDBF = dbf;
    }

    /**
     * Get the DbfTableModel representing the dbf file for the main
     * OMGraphicList.
     */
    public DbfTableModel getMasterDBF() {
        return masterDBF;
    }

    /**
     * Set whether the DBF file should be written when the
     * OMGraphicList is exported to a .shp/.shx file.  The .dbf file
     * will be created if set to true, and this is true by default.
     */
    public void setWriteDBF(boolean value) {
        writeDBF = value;
    }

    /**
     * Get whether the DBF file should be written when the
     * OMGraphicList is exported to a .shp/.shx file.
     */
    public boolean getWriteDBF() {
        return writeDBF;
    }

    /**
     * Get whether the DBF file should have the DrawingAttributes
     * information added to the DbfTableModel if it isn't already
     * there.
     */
    public void setDBFHasRenderingInfo(boolean value) {
        dbfHasRenderingInfo = value;
    }

    /**
     * Get whether the DBF file should have the DrawingAttributes
     * information added to the DbfTableModel if it isn't already
     * there.
     */
    public boolean getDBFHasRenderingInfo() {
        return dbfHasRenderingInfo;
    }

    /**
     * If the OMGraphicList has a DbfTableModel in its AppObject slot,
     * a new DbfTableModel is created that has the same structure.
     * @return DbfTableModel that matches the structure that is in the
     * OMGraphicList AppObject.
     */
    protected DbfTableModel getMasterDBFHeaderClone() {
        if (masterDBF != null) {
            return masterDBF.headerClone();
        }
        return null;
    }

    /**
     * Gets the DbfTableModel record at the index.  Used when the
     * OMGraphicList contents are being split up into different type
     * EsriGraphicLists, and the records are being split into
     * different tables, too.
     */
    protected ArrayList getMasterDBFRecord(int index) {
        try {
            if (masterDBF != null) {
                return (ArrayList)masterDBF.getRecord(index);
            }
        } catch (IndexOutOfBoundsException ioobe) {
        }
        return null;
    }

    /**
     * Separates the graphics from the OMGraphicList into Polygon,
     * Polyline and Point lists, then passes the desired shape lists
     * to their respective export functions to be added to an
     * EsriLayer of the same type and prepared for export.  OMGraphics
     * that are on sublists within the top-level OMGraphicList will be
     * simply written to the appropriate list at the top level of that
     * list.  They will be handled as multi-part geometries.<p>
     *
     * Separating the graphics into the three types is necessary due
     * to shape file specification limitations which will only allow
     * shape files to be of one type.<P>
     *
     * For OMGraphicLists that are actually EsriGraphicLists, this
     * export method will be redirected to a different method that
     * will handle sub-OMGraphicLists as multi-part geometries.<P>
     *
     * If you want to write out multi-part geometries and have a
     * regular OMGraphicList, you have to convert them to
     * EsriGraphicLists first (and OMGraphics to EsriGraphics), which
     * forces you to group shapes into like types (points, polylines
     * and polygons).
     */
    public void export() {
        OMGraphicList list = getGraphicList();
        if (list == null) {
            Debug.error("EsriShapeExport: no graphic list to export!");
            return;
        }

        export(list, null, true);
    }

    /**
     * A counter for graphics that are not RENDERTYPE_LATLON.
     */
    int badGraphics;

    /**
     * This method is intended to allow embedded OMGraphicLists to be
     * handled. The record should be set if the list is an embedded
     * list, reusing a record from the top level interation.  Set the
     * record to null at the top level iteration to cause the method
     * to fetch the record from the masterDBF, if it exists.
     * @param list the OMGraphicList to write
     * @param record the record for the current list, used if the list
     * is actually a multipart geometry for the overall list.  May be
     * null anyway, though.
     * @deprecated use export(OMGraphicList, ArrayList, boolean) instead.
     * @see #export(OMGraphicList list, ArrayList record, boolean writeFiles)
     */
    protected void export(OMGraphicList list, ArrayList record) {
        export(list, record, true);
    }

    /**
     * This method is intended to allow embedded OMGraphicLists to be
     * handled. The record should be set if the list is an embedded
     * list, reusing a record from the top level interation.  Set the
     * record to null at the top level iteration to cause the method
     * to fetch the record from the masterDBF, if it exists.  If the
     * list is an EsriGraphicList, then the export for
     * EsriGraphicLists will be called.  The DbfTableModel for the
     * list should be stored in the appObject member variable of the
     * EsriGraphicList.
     *
     * @param list the OMGraphicList to write.
     * @param masterRecord the record for the current list, used if the list
     * is actually a multipart geometry for the overall list.  May be
     * null anyway, though.
     * @param writeFiles Flag to note when this method is being called
     * iteratively, which is when record is not null.  If it is
     * iterative, then the writing of the files should not be
     * performed on this round of the method call.
     */
    protected void export(OMGraphicList list, ArrayList masterRecord, boolean writeFiles) {
        badGraphics = 0;

        if (list == null) {
            return;
        } else if (list instanceof EsriGraphicList) {
            export((EsriGraphicList)list);
            return;
        }

        int dbfIndex = 0;

        //parse the graphic list and fill the individual lists with
        //the appropriate shape types
        Iterator it = list.iterator();
        while (it.hasNext()) {
            OMGraphic dtlGraphic = (OMGraphic) it.next();
            // Reset the record to the master flag record, which will
            // cause a new record to be read for the top level list
            // contents, but duplicate the current masterRecord for
            // iterative contents.
            ArrayList record = masterRecord;

            if (record == null) {
                record = getMasterDBFRecord(dbfIndex++);
            }

            // If we have an OMGraphicList, interate through that one
            // as well.  We're not handling multi-part geometries yet.
            if (dtlGraphic instanceof OMGraphicList) {
                if (DEBUG) Debug.output("ESE: handling OMGraphicList");
                export((OMGraphicList)dtlGraphic, record, false);
                continue;
            }

            //check to be sure the graphic is rendered in LAT/LON
            if (dtlGraphic.getRenderType() != RENDERTYPE_LATLON) {
                badGraphics++;
                continue;
            }

            if (dtlGraphic instanceof OMPoly) {
                OMPoly omPoly = (OMPoly)dtlGraphic;
                //verify that this instance of OMPoly is a polygon
                if (isPolygon(omPoly)) {
                    if (DEBUG) Debug.output("ESE: handling OMPoly polygon");
                    addPolygon(dtlGraphic, record);
                }
                //if it is not it must be a polyline and therefore
                //added to the line list
                else {
                    if (DEBUG) Debug.output("ESE: handling OMPoly line");
                    addLine(dtlGraphic, record);
                }
            }
            //(end)if (dtlGraphic instanceof OMPoly)
            //add all other fully enclosed graphics to the polyList
            else if (dtlGraphic instanceof OMRect) {
                if (DEBUG) Debug.output("ESE: handling OMRect");
                addPolygon((OMGraphic)EsriPolygonList.convert((OMRect)dtlGraphic), record);
            } else if (dtlGraphic instanceof OMCircle) {
                if (DEBUG) Debug.output("ESE: handling OMCircle");
                addPolygon((OMGraphic)EsriPolygonList.convert((OMCircle)dtlGraphic, projection), record);

            } else if (dtlGraphic instanceof OMRangeRings) {
                if (DEBUG) Debug.output("ESE: handling OMRangeRings");
                export(EsriPolygonList.convert((OMRangeRings)dtlGraphic, projection), record, false);

            }

            //add lines to the lineList
            else if (dtlGraphic instanceof OMLine) {
                if (DEBUG) Debug.output("ESE: handling OMLine");
                addLine((OMGraphic)EsriPolylineList.convert((OMLine)dtlGraphic), record);
            }
            //add points to the pointList
            else if (dtlGraphic instanceof OMPoint) {
                if (DEBUG) Debug.output("ESE: handling OMPoint");
                addPoint(dtlGraphic, record);
            }
        }
        //(end)for (int i = 0; i < dtlGraphicList.size(); i++)

        if (badGraphics > 0) {
            // Information popup provider, it's OK that this gets dropped.
            DrawingToolRenderException.notifyUserOfNonLatLonGraphics(badGraphics);
        }

        if (!writeFiles) {
            // Punch the stack back up so that the initial call will
            // write the files.
            return;
        }

        boolean needConfirmation = false;
        //call the file chooser if no path is given
        if (filePath == null) {
            filePath = getFilePathFromUser();
            needConfirmation = true;
        }

        if (DEBUG) Debug.output("ESE: writing files...");

        boolean needTypeSuffix = false;

        //(end)if (filePath == null) call the appropriate methods to
        //set up the shape files of their respective types
        if (polyList != null) {
            eseInterfaces.add(new ESEInterface(polyList, filePath, null));
            needTypeSuffix = true;
        }

        if (lineList != null) {
            eseInterfaces.add(new ESEInterface(lineList, filePath, (needTypeSuffix?"Lines":null)));
            needTypeSuffix = true;
        }

        if (pointList != null) {
            eseInterfaces.add(new ESEInterface(pointList, filePath, (needTypeSuffix?"Pts":null)));
        }

        if (needConfirmation) {
            showGUI();
        } else {
            writeFiles();
        }
    }

    /**
     * Writes out EsriGraphicLists as shape files, assumes that the
     * DbfTableModel representing the attribute data for the list
     * objects is stored in the appObject member variable of the
     * EsriGraphicList.  This export handles multi-part geometries,
     * because it's assumed that the sorting of the graphic types have
     * been handled and that any sub-lists are meant to be multi-part
     * geometries.  If the filePath hasn't been set in the
     * EsriShapeExport class, the user will be asked to provide it.
     */
    protected void export(EsriGraphicList egList) {
        Object obj = egList.getAppObject();
        if (obj == null) {
            egList.setAppObject(getMasterDBF());
        }

        eseInterfaces.add(new ESEInterface(egList, filePath, null));
        writeFiles();
    }

    /**
     * The the Iterator of ESEIterators.
     */
    protected Iterator getInterfaces() {
        return eseInterfaces.iterator();
    }

    /**
     * Just write the files from the ESEInterfaces.
     */
    protected void writeFiles() {
        Iterator it = getInterfaces();
        while (it.hasNext()) {
            ((ESEInterface)it.next()).write();
        }
    }

    protected JFrame frame = null;

    /**
     * Show the GUI for saving the Shape files.
     */
    public void showGUI() {

        if (frame == null) {
            frame = new JFrame("Saving Shape Files");
        
            frame.getContentPane().add(getGUI(), BorderLayout.CENTER);
//          frame.setSize(400, 300);
            frame.pack();
        }

        frame.setVisible(true);
    }

    /**
     * Hide the Frame holding the GUI.
     */
    public void hideGUI() {
        if (frame != null) {
            frame.setVisible(false);
        }
    }

    /**
     * Create the GUI for managing the different ESEIterators.
     */
    public Component getGUI() {

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel interfacePanel = new JPanel();
        interfacePanel.setLayout(new GridLayout(0, 1));

        Iterator interfaces = getInterfaces();
        int count = 0;
        while (interfaces.hasNext()) {
            interfacePanel.add(((ESEInterface)interfaces.next()).getGUI());
            count++;
        }
        panel.add(interfacePanel, BorderLayout.CENTER);

        if (count > 1) {
            JLabel notification = new JLabel("  " + count + " Shape file sets needed:");
            panel.add(notification, BorderLayout.NORTH);
        }

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    writeFiles();
                    hideGUI();
                }
            });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    hideGUI();
                }
            });
        JPanel controlPanel = new JPanel();
        controlPanel.add(saveButton);
        controlPanel.add(cancelButton);
        panel.add(controlPanel, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Prepares and returns a 7 column DbfTableModel to accept input
     * for columns of TYPE_CHARACTER. <br> <br> The default model used
     * holds most of the DrawingAttributes of the OMGraphics.
     *
     *
     * @param list the EsriGraphicList to create a DbfTableModel from.
     * @return The completed DbfTableModel.
     */
    public DbfTableModel createDefaultModel(EsriGraphicList list) {
        if (DEBUG) Debug.output("ESE: creating DbfTableModel");

        DbfTableModel _model = new DbfTableModel(7);
        //Setup table structure
        //column 0
        //The first parameter, 0, respresents the first column
        _model.setLength(0, (byte)50);
        _model.setColumnName(0, SHAPE_DBF_DESCRIPTION);
        _model.setType(0, (byte)DbfTableModel.TYPE_CHARACTER);
        _model.setDecimalCount(0, (byte)0);
        //column 1
        //The first parameter, 1, respresents the second column
        _model.setLength(1, (byte)10);
        _model.setColumnName(1, SHAPE_DBF_LINECOLOR);
        _model.setType(1, (byte)DbfTableModel.TYPE_CHARACTER);
        _model.setDecimalCount(1, (byte)0);
        //column2
        //The first parameter, 2, respresents the third column
        _model.setLength(2, (byte)10);
        _model.setColumnName(2, SHAPE_DBF_FILLCOLOR);
        _model.setType(2, (byte)DbfTableModel.TYPE_CHARACTER);
        _model.setDecimalCount(2, (byte)0);
        //column3
        //The first parameter, 3, respresents the fourth column
        _model.setLength(3, (byte)10);
        _model.setColumnName(3, SHAPE_DBF_SELECTCOLOR);
        _model.setType(3, (byte)DbfTableModel.TYPE_CHARACTER);
        _model.setDecimalCount(3, (byte)0);
        //column4
        //The first parameter, 4, respresents the fifth column
        _model.setLength(4, (byte)4);
        _model.setColumnName(4, SHAPE_DBF_LINEWIDTH);
        _model.setType(4, (byte)DbfTableModel.TYPE_NUMERIC);
        _model.setDecimalCount(4, (byte)0);
        //column5
        //The first parameter, 5, respresents the sixth column
        _model.setLength(5, (byte)20);
        _model.setColumnName(5, SHAPE_DBF_DASHPATTERN);
        _model.setType(5, (byte)DbfTableModel.TYPE_CHARACTER);
        _model.setDecimalCount(5, (byte)0);
        //column6
        //The first parameter, 6, respresents the seventh column
        _model.setLength(6, (byte)10);
        _model.setColumnName(6, SHAPE_DBF_DASHPHASE);
        _model.setType(6, (byte)DbfTableModel.TYPE_NUMERIC);
        _model.setDecimalCount(6, (byte)4);

        // At a later time, more stroke parameters can be addded, like
        // dash phase, end cap, line joins, and dash pattern.

        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            OMGraphic omg = (OMGraphic)iterator.next();

            ArrayList record = new ArrayList();

            // Description
            Object obj = omg.getAppObject();
            if (obj instanceof String) {
                record.add(obj);
            } else {
                record.add("");
            }

            record.add(ColorFactory.getHexColorString(omg.getLineColor()));
            record.add(ColorFactory.getHexColorString(omg.getFillColor()));
            record.add(ColorFactory.getHexColorString(omg.getSelectColor()));
            BasicStroke bs = (BasicStroke)omg.getStroke();
            record.add(new Double(bs.getLineWidth()));
            String dp = BasicStrokeEditor.dashArrayToString(bs.getDashArray());
            if (dp == BasicStrokeEditor.NONE) {
                dp = "";
            }
            record.add(dp);
            record.add(new Double(bs.getDashPhase()));
            _model.addRecord(record);
            if (DEBUG) Debug.output("ESE: adding record: " + record);
        }

        return _model;
    }

    /**
     * Takes an OMPoly as the parameter and checks whether
     * or not it is a polygon or polyline. <br>
     * <br>
     * This method incorporates the OMPoly.isPolygon()
     * method which returns true if the fill color is not
     * clear, but also checks the first set and last set of
     * lat/lon points of the float[] defined by
     * OMPoly.getLatLonArray(). Returns true for a polygon
     * and false for a polyline.
     *
     * @param omPoly  the OMPoly object to be verified
     * @return        The polygon value
     */
    public static boolean isPolygon(OMPoly omPoly) {
        boolean isPolygon = false;
        //get the array of lat/lon points
        float[] points = omPoly.getLatLonArray();
        int i = points.length;

        //compare the first and last set of points, equal points
        //verifies a polygon.
        if (points[0] == points[i - 2] && 
            points[1] == points[i - 1]) {

            isPolygon = true;
        }
        //check OMPoly's definition of a polygon
        if (omPoly.isPolygon()) {
            isPolygon = true;
        }

        return isPolygon;
    }

    /**
     * Generic error handling, puts up an error window.
     */
    protected void handleException(Exception e) {
        //System.out.println(e);
        StringBuffer sb = new StringBuffer("ShapeFile Export Error:");
        sb.append("\nProblem with creating the shapefile set.");
        sb.append("\n" + e.toString());
        
        JOptionPane.showMessageDialog(null, sb.toString(), "ESRI Shape Export to File", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    /**
     * Fetches a file path from the user, via a JFileChooser.  Returns
     * null if the user cancels.
     * @see com.bbn.openmap.util.FileUtils.getFilePathFromUser
     */
    public String getFilePathFromUser() {
        return FileUtils.getFilePathFromUser("Select Name for Shape File Set...");
    }

    /**
     * The main function is a test, reads in a Shape file (with the
     * .shx and .dbf files) and writes them back out.
     */
    public static void main(String[] argv) {
        Debug.init();
        boolean toUpper = true;

        ArgParser ap = new ArgParser("EsriShapeExport");
        ap.add("shp", "A URL to a shape file (.shp).", 1);

        if (argv.length < 1) {
            ap.bail("", true);
        }

        ap.parse(argv);

        String[] files = ap.getArgValues("shp");
        if (files != null && files[0] != null) {
            String shp = files[0];
            String shx = null;
            String dbf = null;

            try {
                shx = shp.substring(0, shp.lastIndexOf('.') + 1) + PARAM_SHX;
                dbf = shp.substring(0, shp.lastIndexOf('.') + 1) + PARAM_DBF;

                DbfTableModel model = 
                    DbfTableModel.getDbfTableModel(PropUtils.getResourceOrFileOrURL(null, dbf));

                EsriGraphicList list = 
                    EsriGraphicList.getEsriGraphicList(PropUtils.getResourceOrFileOrURL(null, shp), 
                                                       PropUtils.getResourceOrFileOrURL(null, shx), 
                                                       null, null);

                Debug.output(list.getDescription());

                EsriShapeExport ese = new EsriShapeExport(list, model, null);
                ese.export();

            } catch (MalformedURLException murle) {
                Debug.error("EsriShapeExport: Malformed URL Exception\n" + murle.getMessage());
            } catch (NullPointerException npe) {
                Debug.error("EsriShapeExport: Path to shape file isn't good enough to find .dbf file and .shx file.");
            } catch (Exception exception) {
                Debug.error("EsriShapeExport: Exception\n" + exception.getMessage());
                exception.printStackTrace();
            }

        } else {
            ap.bail("Need a path to a Shape file (.shp)", true);
        }
        System.exit(0);
    }

    /**
     * A helper class to manage a specific instance of a
     * EsriGraphicList, it's data model, etc.  Provides a GUI to
     * display and change the name of the file, and the DbfTableModel
     * GUI, and also writes the files out.
     */
    public class ESEInterface {

        protected EsriGraphicList list;
        protected DbfTableModel model;
        protected String suffix;
        protected String filePath;

        File shpFile = null;
        File shxFile = null;
        File dbfFile = null;

        protected JTextField filePathField;

        public ESEInterface(EsriGraphicList eglist, 
                            String filePathString,
                            String fileNameSuffix) {
            list = eglist;
            filePath = filePathString;

            model = eglist.getTable();

            if (model == null) {
                model = createDefaultModel(list);
            }
            model.setWritable(true);

            suffix = (fileNameSuffix==null?"":fileNameSuffix);
        }

        public Component getGUI() {
            JPanel panel = new JPanel();

            int type = list.getType();
            String sectionTitle;
            switch (type) {
            case (SHAPE_TYPE_POINT):
                sectionTitle = "Point Shape File";
                break;
            case (SHAPE_TYPE_POLYLINE):
                sectionTitle = "Line Shape File";
                break;
            case (SHAPE_TYPE_POLYGON):
                sectionTitle = "Polygon Shape File";
                break;
            default:
                sectionTitle = "Shape File";
            }

            panel.setBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), sectionTitle));
            
            panel.setLayout(new GridLayout(0, 1));
            JPanel pathPanel = new JPanel();
            filePathField = new JTextField(20);
            filePathField.setText(filePath + suffix);
            JButton filePathChooserLauncher = new JButton("Change Path");
            filePathChooserLauncher.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        setFilePath(getFilePathFromUser());
                    }
                });

            panel.add(filePathField);

            JButton editDBFButton = new JButton("Edit the Attribute File...");
            editDBFButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        model.showGUI(getFilePath() + " Attributes", DbfTableModel.DONE_MASK | DbfTableModel.MODIFY_COLUMN_MASK);
                    }
                });

            pathPanel.add(editDBFButton);
            pathPanel.add(filePathChooserLauncher);

            panel.add(pathPanel);

            return panel;
        }

        protected void setFilePath(String path) {
            filePath = path;
        }

        public void write() {

            if (filePathField != null) {
                filePath = filePathField.getText();
            } 

            if (filePath == null) {
                filePath = getFilePathFromUser();
                if (filePath == null) {
                    return;
                }
            }

            shpFile = new File(filePath + ".shp");
            shxFile = new File(filePath + ".shx");
            dbfFile = new File(filePath + ".dbf");

            try {
            
                //create an esriGraphicList and export it to the shapefile set
                if (DEBUG) Debug.output("ESE writing: " + 
                                        list.size() + 
                                        " elements");
                
                ShpOutputStream pos = 
                    new ShpOutputStream(new FileOutputStream(shpFile));
                int[][] indexData = pos.writeGeometry(list);

                ShxOutputStream xos = 
                    new ShxOutputStream(new FileOutputStream(shxFile));
                xos.writeIndex(indexData, list.getType());

                if (getWriteDBF()) {
                    DbfOutputStream dos = 
                        new DbfOutputStream(new FileOutputStream(dbfFile));
                    dos.writeModel(model);
                }

            } catch (Exception e) {
                handleException(e);
            }
        }

    }
}

