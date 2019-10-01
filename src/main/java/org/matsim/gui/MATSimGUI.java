/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.gui;

import org.matsim.run.RunLosAngelesScenario;
import org.matsim.run.gui.Gui;

public class MATSimGUI {

	public static void main(String[] args) {
		Gui.show("MATSim GUI from example project", RunLosAngelesScenario.class);
	}

	//	The jar file is generated by "mvn package".  Note that the pom.xml refers at some point to this class here. 

}
