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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/cacheHandler/CacheObject.java,v $
// $RCSfile: CacheObject.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.util.cacheHandler;

public class CacheObject {

  public Object obj = null;
  public int cachedTime = 0;
  public String id = null;
  
  /** New object, set the local clock to zero
   */
  public CacheObject(String identifier, Object cachedObject){
      id = identifier;
      obj = cachedObject;
  }

  public boolean match(String queryID){
    return (queryID.equals(id));
  }

  public boolean older(int time){
    return (cachedTime < time);
  }
}

