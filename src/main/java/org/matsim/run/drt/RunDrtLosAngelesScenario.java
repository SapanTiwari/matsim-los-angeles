/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2020 by the members listed in the COPYING,        *
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

package org.matsim.run.drt;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.av.robotaxi.fares.drt.DrtFareModule;
import org.matsim.contrib.av.robotaxi.fares.drt.DrtFaresConfigGroup;
import org.matsim.contrib.drt.routing.DrtRoute;
import org.matsim.contrib.drt.routing.DrtRouteFactory;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtConfigs;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtModule;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.contrib.dvrp.run.DvrpQSimComponents;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.Config;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.population.routes.RouteFactories;
import org.matsim.core.router.AnalysisMainModeIdentifier;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.drtSpeedUp.DrtSpeedUpConfigGroup;
import org.matsim.drtSpeedUp.DrtSpeedUpModule;
import org.matsim.run.LosAngelesIntermodalPtDrtRouterAnalysisModeIdentifier;
import org.matsim.run.LosAngelesIntermodalPtDrtRouterModeIdentifier;
import org.matsim.run.RunLosAngelesScenario;

/**
 * This class starts a simulation run with DRT.
 * 
 *  - The input DRT vehicles file specifies the number of vehicles and the vehicle capacity (a vehicle capacity of 1 means there is no ride-sharing).
 * 	- The DRT service area is set based on an input shape file.
 * 	- Initial plans are not modified.
 * 
 * @author ikaddoura
 */

public final class RunDrtLosAngelesScenario {

	private static final Logger log = Logger.getLogger(RunDrtLosAngelesScenario.class);

	public static void main(String[] args) throws CommandLine.ConfigurationException {
		
		for (String arg : args) {
			log.info( arg );
		}
		
		if ( args.length==0 ) {
			args = new String[] {"./scenarios/los-angeles-v1.0/input/drt/los-angeles-drt-v1.0-0.1pct.config.xml"}  ;
		}
		
		Config config = prepareConfig( args ) ;
		Scenario scenario = prepareScenario( config ) ;
		Controler controler = prepareControler( scenario ) ;
		controler.run() ;
		
		// run analysis
		RunLosAngelesScenario.runAnalysis(config);
	}
	
	public static Controler prepareControler( Scenario scenario ) {

		Controler controler = RunLosAngelesScenario.prepareControler( scenario ) ;
		
		// Add drt and dvrp modules
		controler.addOverridingModule(new MultiModeDrtModule());
		controler.addOverridingModule(new DvrpModule());
		controler.configureQSimComponents(DvrpQSimComponents.activateAllModes(MultiModeDrtConfigGroup.get(controler.getConfig())));
		
		// Add drt-specific fare module
		controler.addOverridingModule(new DrtFareModule());
				
		// Add the drt-speed-up module
		controler.addOverridingModule(new DrtSpeedUpModule());
				
		controler.addOverridingModule(new AbstractModule() {
			
			@Override
			public void install() {
				// use a main mode identifier which knows how to handle intermodal trips generated by the used sbb pt raptor router
				// the SwissRailRaptor already binds its IntermodalAwareRouterModeIdentifier, however drt obviuosly replaces it
				// with its own implementation
				// So we need our own main mode indentifier which replaces both :-(
				bind(MainModeIdentifier.class).to(LosAngelesIntermodalPtDrtRouterModeIdentifier.class);
				bind(AnalysisMainModeIdentifier.class).to(LosAngelesIntermodalPtDrtRouterAnalysisModeIdentifier.class);
			}
		});
						
		return controler;
	}
	
	public static Scenario prepareScenario( Config config ) {

		Scenario scenario = RunLosAngelesScenario.prepareScenario( config );

		// required by drt module
		RouteFactories routeFactories = scenario.getPopulation().getFactory().getRouteFactories();
		routeFactories.setRouteFactory(DrtRoute.class, new DrtRouteFactory());

		for (DrtConfigGroup drtCfg : MultiModeDrtConfigGroup.get(config).getModalElements()) {
			
			String drtServiceAreaShapeFile = drtCfg.getDrtServiceAreaShapeFile();
			if (drtServiceAreaShapeFile != null && !drtServiceAreaShapeFile.equals("") && !drtServiceAreaShapeFile.equals("null")) {				
				// Currently all transit stops (within the drt service area) can be used as an intermodal transfer point (drt -> pt or pt -> drt).
				// TODO: To speed up computation times, we can also exclude certain transit stops (e.g. less relevant bus stops)
			}
		}
		
		return scenario;
	}

	public static Config prepareConfig(String [] args) {
		Config config = RunLosAngelesScenario.prepareConfig(args, new MultiModeDrtConfigGroup(), new DvrpConfigGroup(), new DrtFaresConfigGroup(), new DrtSpeedUpConfigGroup()  ) ;

		DrtConfigs.adjustMultiModeDrtConfig(MultiModeDrtConfigGroup.get(config), config.planCalcScore(), config.plansCalcRoute());
		DrtSpeedUpModule.addTeleportedDrtMode(config);

		return config ;
	}

}

