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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/terrain/ProfileStateMachine.java,v $
// $RCSfile: ProfileStateMachine.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.terrain;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import com.bbn.openmap.layer.util.stateMachine.*;

class ProfileStateMachine extends StateMachine {

    public ProfileGenerator pg;
    public static final int TOOL_DO_NOTHING = 0;
    public static final int TOOL_DRAW = 1;
    public static final int TOOL_DEFINED = 2;
    public static final int TOOL_VIEW = 3;

    public ProfileStateMachine(ProfileGenerator generator){
	pg = generator;
	State[] profileStates = init();
	setStates(profileStates);

	// set reset state
	setResetState(TOOL_DO_NOTHING);
	reset();
	setMapMouseListenerResponses(true);
    }


    protected State[] init(){
	State[] pStates = new State[4];

	pStates[TOOL_DO_NOTHING] = new ProfileDoNothingState(pg);
	pStates[TOOL_DRAW] = new ProfileDrawState(pg);
	pStates[TOOL_DEFINED] = new ProfileDefinedState(pg);
	pStates[TOOL_VIEW] = new ProfileViewState(pg);

	return pStates;
    }
}










