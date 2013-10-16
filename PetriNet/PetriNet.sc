PetriNet : Thr44PetriNet {
	var <>initPlace;
	var <>odefList;
	var <>petriGui;
	
	*new{
		arg numPlaces=nil, numTransitions=5, connectivityIndex=0.2;
		var instance;
		^super.new.init(numPlaces, numTransitions, connectivityIndex);
	}
	init{
		arg numPlaces, numTransitions, connectivityIndex;
		var builder;
		
		this.nodeList=NodeList.new();
		this.relationList=RelationList.new();
		
		this.networkSynths=0; //do i use this?
		this.transitionList=NodeList.new();
		this.placeList=NodeList.new();
		this.isPlaying=false;
		this.playingSynths=0;
		
		this.odefList=List.new();
		if(numPlaces!=nil, {
			builder=PetriNetBasicBuilder(this);
			builder.create(numPlaces, numTransitions, connectivityIndex);
		});
		^this;
			
	}
	play{
		//alerts network to allow play
		//["play"].postln;
		if(this.isPlaying==true, {
			//["not playing"].postln;
			^nil;
		});
		this.isPlaying=true;
		
		for(0, this.placeList.size()-1, {
			arg i;
			//set update to placesList??
			this.placeList.at(i).update();
			
		});
		[this.playingSynths].postln;
		if(this.playingSynths==0, {
			if(this.initPlace.isNil, {
				this.initPlace=this.placeList.at((1.0.rand*this.placeList.size).floor);
			});
			this.initPlace.addTokens(1);
		});
	}
	update{
		//"network updated".postln;
		
		if(this.petriGui!=nil, {
			this.petriGui.update(this);	
		});
		
	}
	stop{
		if(this.isPlaying==true, {
			this.isPlaying=false;
			//this.responder.remove();
		});
		
	}
	waitTime{
		arg val;
		for(0, this.transitionList.size()-1, {
			arg i;
			this.transitionList[i].waitTime(val);
		});
			
	}
	setPlacesCapacity{
		arg k=inf;
		this.placeList.do({
			arg place;
			place.k=k;	
		});	
	}
	playTransition{
		arg transition;
		var odef;
		
		odef=transition.refObj;//this.odefList[transition.id%this.odefList.size()];
		//["should play the Odef assigned to", transition.id, odef].postln;
		odef.network=this;
		odef.play;
	}
	addOdefs{
		arg odefs, transitions;
		var odefClass;
		odefClass=odefs.class;
		odefs=[odefs].flat;
		if(transitions==nil, {transitions=this.transitionList});
		transitions=[transitions].flat;
		
		for(0, odefs.size()-1, {
			arg i;
			if(this.odefList.includes(odefs[i]).not, {
				this.odefList=this.odefList.add(odefs[i]);
			});
		});	
		
		
		["this.odefList:", this.odefList, transitions].postln;
		for(0, transitions.size()-1, {
			arg i;
			var odef=this.odefList[i%this.odefList.size()].getInstance("petri"++i);
			odef.addTag("petri");
			["adding odef:", odef.key, odef.instanceParent.key].postln;
			["it contains:", this.transitionList[i].refObj].postln;
			this.transitionList[i].refObj=odef;
			odef.refObj=this.transitionList[i];
			odef.addDependant({
				arg source, status;
				if(status=='stopped', {
					source.refObj.onActionComplete();
				}, {
					["failed to validate status", status].postln;	
				});	
			});
			//this.odefList=this.odefList.add(odef);
		});
	}
	clearOdefs{
		this.odefList=List.new();
		for(0, this.transitionList.size()-1, {
			arg i;
			this.transitionList[i].refObj.refObj=nil;
			this.transitionList[i].refObj=nil;
		});
	}
	gui{
		this.petriGui=PetriNetGui.new(this);
	}
}

PetriNetGui{
	var <>gui;
	var <>pS;
	var <>petriNet;
	var <>w;
	var <>dist=50;
	var <>repulsorDist=100;
	var <>isRunningGui=false;
	
	*new{
		arg petriNet;
		^super.new.init(petriNet);
	}
	init{
		arg petriNet;
		this.update(petriNet);
		
		
	}
	update{
		arg petriNet;
		
		this.petriNet=petriNet;
		if( this.petriNet==nil, {"error1".postln});
		["||----->", this.petriNet].postln;
		if( this.petriNet.nodeList==nil, {"error2".postln});
		["   ||----->", this.petriNet.nodeList].postln;
		if( this.petriNet.relationList==nil, {"error3".postln});
		["      ||----->", this.petriNet.relationList].postln;
		
		"ps?".postln;
		if(this.pS.isNil, {this.pS=Thr44ParticleSystem.new()});
		
		this.petriNet.nodeList.do{
			arg node, i;
			[i, node].postln;
			if(node.physics.isNil, {
				node.physics=Thr44Particle.new(nil, node);
			});
		};

		
		this.petriNet.relationList.do{
			arg relation;
			this.pS.addSpring(relation.getNode0(), relation.getNode1(), 0.6, dist);
				
		};
		this.pS.fillMatrixWithRepulsors(0.4, repulsorDist);
		
		this.draw();
		
		
	}
	draw{
		var run=true, point;
		["W:", this.w].postln;
		if( (this.w==nil).not, {
			"not null".postln;
			//w.drawFunc=nil;
			//this.w.close;
			//w="a";
		});
		if(this.w==nil, {
			this.w = Window.new.front;
			this.w.dump;
			this.w.view.background_(Color.white);
			this.w.drawFunc = {
				var rect, width, height, i, j, node0, node1;
				//"drawfunc".postln;
				width=this.w.bounds.width;
				height=this.w.bounds.height;
				Pen.translate(width*0.5, height*0.4);
				
		i=0;
		j=0;
		if( this.petriNet==nil, {^this});
		//["----->", this.petriNet].postln;
		if( this.petriNet.nodeList==nil, {^this});
		//["   ----->", this.petriNet.nodeList].postln;
		if( this.petriNet.relationList==nil, {^this});
		//["      ----->", this.petriNet.relationList].postln;
		this.petriNet.relationList.do{
			arg relation;
			var x0, x1, y0, y1;
			x0=relation.node0.physics.x;
			x1=relation.node1.physics.x;
			y0=relation.node0.physics.y;
			y1=relation.node1.physics.y;
			Pen.strokeColor = Color.red;
					
			Pen.moveTo(x0@y0);
			Pen.lineTo(x1@y1);
			Pen.stroke;
			
			Pen.use{
					var ang=atan2( (y1-y0), (x1-x0) );
					Pen.fillColor = Color.red;
					Pen.translate(x1, y1);
					Pen.rotate(ang, 0, 0);
					Pen.moveTo(0@0);
					Pen.lineTo((-10)@(-2));
					Pen.lineTo((-10)@(2));
					Pen.lineTo(0@0);
					Pen.fill;
				};
			
			
			
		};
		//"wtf".postln;
		//draw mapped relations - THIS SHOULD BE IN GUIDEBUG!!
		
		/*
		i=0;
		j=0;		
		while ({ i < pS.nNodes }, {
			
			Pen.color = Color.grey;
			Pen.fillOval(Rect(i*20,i*20, 16, 16));
			
			j = 0;
			
			while ({ j < i }, {
				if(pS.forcesMatrix[i][j].class==Thr44PSpring, {
					node0=pS.forcesMatrix[i][i];
					node1=pS.forcesMatrix[j][j];
					
					Pen.color = Color.red;
					Pen.fillOval(Rect(i*20,j*20, 16, 16));

						
				}, {
					if(pS.forcesMatrix[i][j].class==Thr44PRepulsor, {
						node0=pS.forcesMatrix[i][i];
						node1=pS.forcesMatrix[j][j];
						
						Pen.color = Color.green;
						Pen.fillOval(Rect(i*20,j*20, 16, 16));
					

						
					});
				});
				j = j + 1;
			});
			i = i + 1; 
		});
		*/
		////

				this.petriNet.transitionList.do{
					arg transition;
					if( (transition.refObj.isNil.not) && (transition.refObj.isPlaying), {
						Pen.color = Color.green;
					}, {
						Pen.color = Color.black;
					});
					
					Pen.addRect(Rect(transition.physics.x-3,transition.physics.y-8, 6, 16));
					Pen.perform(\fill);
					Pen.color = Color.black;
					Pen.font = Font( "Georgia", 9 );
		Ê Ê 			Pen.stringAtPoint( "id:"+transition.id.asString(), (transition.physics.x+6)@(transition.physics.y-8) );
					
				};
				//"and here?".postln;
				this.petriNet.placeList.do{
					arg place;
					
					Pen.color = Color.grey;
					Pen.fillOval(Rect(place.physics.x-6,place.physics.y-6, 12, 12));
					Pen.color = Color.black;
					 Pen.font = Font( "Georgia", 9 );
		Ê Ê 			Pen.stringAtPoint( "id:"+place.id.asString()+"\ntokens:"+place.nTokens, (place.physics.x+8)@(place.physics.y-4) );
					//Pen.perform(\fill);
					
				};
				//"and here too?".postln;		
			}; //ends drawFunc
		}); //ends w==nill
		//"beforeRefresh".postln;
		//if(this.w=="a", {"is a".postln}, {
		this.w.refresh;
		if(isRunningGui.not, {
			{
				while { run } {
					//"fork".postln;
					//"-> cparticles".postln;
					this.pS.calculateParticles();
					//"-> end cparticles".postln;
					this.w.refresh; 
					0.05.wait;
					//"done".postln;				 
				};
			}.fork(AppClock);	
		});
		
		isRunningGui=true;
		"end draw".postln;
	}
}
PetriNetGui2{
	var <>gui;
	var <>petriNet;
	var <>w;
	
	*new{
		arg petriNet;
		^super.new.init(petriNet);
	}
	init{
		arg petriNet;
		this.update(petriNet);
		
		
	}
	update{
		arg petriNet;
		this.petriNet=petriNet;
		this.petriNet.nodeList.do{
			arg node, i;
			[i, node].postln;
			if(node.physics.isNil, {
				node.physics=Thr44Particle.new(nil, node);
			});
		};
		
		this.draw();
		
	}
	draw{
		var run=true, point;
		if(w==nil, {
			w = Window.new.front;
			w.dump;
			w.view.background_(Color.white);
			w.drawFunc = {
				var rect, width, height, i, j, node0, node1;
				width=w.bounds.width;
				height=w.bounds.height;
				Pen.translate(width*0.5, height*0.4);
				
		i=0;
		j=0;
		this.petriNet.relationList.do{
			arg relation;
			var x0, x1, y0, y1;
			x0=relation.node0.physics.x;
			x1=relation.node1.physics.x;
			y0=relation.node0.physics.y;
			y1=relation.node1.physics.y;
			Pen.strokeColor = Color.red;
					
			Pen.moveTo(x0@y0);
			Pen.lineTo(x1@y1);
			Pen.stroke;
			
			Pen.use{
					var ang=atan2( (y1-y0), (x1-x0) );
					Pen.fillColor = Color.red;
					Pen.translate(x1, y1);
					Pen.rotate(ang, 0, 0);
					Pen.moveTo(0@0);
					Pen.lineTo((-10)@(-2));
					Pen.lineTo((-10)@(2));
					Pen.lineTo(0@0);
					Pen.fill;
				};
			
			
			
		};
					//assignPositions:
					/*this.petriNet.nodeList.do{
						arg node;
					}
					*/
					//draw:
					this.petriNet.transitionList.do{
					arg transition;
					if( (transition.refObj.isNil.not) && (transition.refObj.isPlaying), {
						Pen.color = Color.green;
					}, {
						Pen.color = Color.black;
					});
					
					Pen.addRect(Rect(transition.physics.x-3,transition.physics.y-8, 6, 16));
					Pen.perform(\fill);
					Pen.color = Color.black;
					Pen.font = Font( "Georgia", 9 );
		Ê Ê 			Pen.stringAtPoint( "id:"+transition.id.asString(), (transition.physics.x+6)@(transition.physics.y-8) );
					
				};
				this.petriNet.placeList.do{
					arg place;
					
					Pen.color = Color.grey;
					Pen.fillOval(Rect(place.physics.x-6,place.physics.y-6, 12, 12));
					Pen.color = Color.black;
					 Pen.font = Font( "Georgia", 9 );
		Ê Ê 			Pen.stringAtPoint( "id:"+place.id.asString()+"\ntokens:"+place.nTokens, (place.physics.x+8)@(place.physics.y-4) );
					//Pen.perform(\fill);
					
				};
							};
		});
		w.refresh;

		{ while { run } { 
			this.pS.calculateParticles();
			w.refresh; 
			0.05.wait } }.fork(AppClock);	
	}
}

