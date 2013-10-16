Thr44PetriNet : Thr44Network{
	var <>networkSynths;
	var <>numTransitions;
	var <>transitionList;
  	var <>placeList;
  	var <>isPlaying;
  	var <>playingSynths;

	*new{
		^super.new.init();	
	}
	init{
		this.nodeList=NodeList.new();
		this.relationList=RelationList.new();
		
		this.networkSynths=0; //do i use this?
		this.transitionList=NodeList.new();
		this.placeList=NodeList.new();
		this.isPlaying=false;
		this.playingSynths=0;
		^this;
	}
	addTransition{
		arg transition;
		transition.setNetwork(this);
		//node.id=getNewNodeID();
		this.transitionList.addNode(transition);
		this.nodeList.addNode(transition);
		//should return a bool??
		this.update();
	}
	removeTransition{
		arg transition;
  		var filteredArcs=this.relationList.getRelationsWithNode(transition);
    		for(0, filteredArcs.size-1, {
	  		arg i;
    			this.removeArc(filteredArcs[i]);  
    			i=i+1;
  		});
  		
  		transition.setNetwork(nil);
  		this.nodeList.removeNode(transition);
  		this.transitionList.removeNode(transition);
  		this.update();
	}
	getTransitionFromId{
		arg id;
		for(0, transitionList.size()-1, {
	  		arg i;
	  		if(transitionList[i].id==id){
				^transitionList[i];	
	  		};
    			i=i+1;
  		});
  		^nil;
	}
	playTransition{
		arg transition;
		"should be implemented in final class!".postln;	
	}
	addPlace{
		arg place;
		place.setNetwork(this);
		//node.id=getNewNodeID();
		this.placeList.addNode(place);
		this.nodeList.addNode(place);
		//should return a bool??
		this.update();
		
	}
	
	removePlace{
		arg place;
  		var filteredArcs=this.relationList.getRelationsWithNode(place);
    		for(0, filteredArcs.size-1, {
	  		arg i;
    			this.removeRelation(filteredArcs[i]);  
    			i=i+1;
  		});
  		
  		place.setNetwork(nil);
  		this.placeList.removeNode(place);
  		this.nodeList.removeNode(place);
  		this.update();
	}
	getPlaceFromId{
		arg id;
		for(0, placeList.size()-1, {
	  		arg i;
	  		if(placeList[i].id==id){
				^placeList[i];	
	  		};
    			i=i+1;
  		});
  		^nil;
	}
	addArc{
		arg arc;
		var added;

 		added=this.relationList.addRelationIfNew(arc);
 		if(added==false) {
	 		^false;
	 	};
 		if(arc.getNode0().type=="Place", {
			this.placeList.addNodeIfNew(arc.getNode0());
			this.transitionList.addNodeIfNew(arc.getNode1());
		}, {
			this.transitionList.addNodeIfNew(arc.getNode0());
			this.placeList.addNodeIfNew(arc.getNode1());
		});
		this.nodeList.addNodeIfNew(arc.getNode0());
		this.nodeList.addNodeIfNew(arc.getNode1());
		//fill nodes with related references:
 		arc.getNode0().nodeList.addNodeIfNew(arc.getNode1());
 		arc.getNode0().relationList.addRelation(arc);
 		arc.getNode1().inputNodeList.addNodeIfNew(arc.getNode0());
 		arc.getNode1().inputRelationList.addRelation(arc);
				
  		//should return a bool??
  		this.update();
  		^true;
	}
	removeArc{
		arg arc;
		this.relationList.removeRelation(arc);
		//should only remove arc:
		/*
		if(arc.getNode0().type=="Place", {
			this.placeList.removeNode(arc.getNode0());
			this.transitionList.removeNode(arc.getNode1());
		}, {
			this.transitionList.removeNode(arc.getNode0());
			this.placeList.removeNode(arc.getNode1());
		});
		*/
		arc.getNode0().nodeList.removeNode(arc.getNode1());
		arc.getNode0().relationList.removeRelation(arc);
		arc.getNode1().inputNodeList.removeNode(arc.getNode0());
		arc.getNode1().inputRelationList.removeRelation(arc);
		this.update();
		
	}
	//FIX THIS:
	getMinConnectivity{
		^(this.placeList.size()+this.transitionList.size()-1);	
	}
	getMaxConnectivity{
		^(this.placeList.size()*this.transitionList.size()*2);	
	}
	getConnectivity{
		var relations = this.relationList.size();
		if(relations==0, {^ 0});
		^ relations/this.getMaxConnectivity();
			
	}
	//
	update{
		"update@Thr44PetriNet".postln;
	}
	log{
		("PETRINET numTransitions:"+this.transitionList.size+" numPlaces:"+this.placeList.size+" numTransitions: "+this.transitionList.size+"numRelations:"+this.relationList.size()).postln;
		"transitions: ".postln;
		this.transitionList.log();
		"places: ".postln;
		this.placeList.log();
		"arcs: ".postln;
		this.relationList.log();
	}
	
}
