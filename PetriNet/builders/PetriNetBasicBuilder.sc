PetriNetBasicBuilder{
	
	var <>petrinet;
	
	*new{
		arg petrinet;
		^super.new.init(petrinet);	
	}
	init{
		arg petrinet;
		if(petrinet.isNil, {this.petrinet=PetriNet.new()}, {this.petrinet=petrinet});
		^this;
	}
	create{
		arg numPlaces=3, numTransitions=3, connectivityIndex=0.2;
		var i, transition, arc, place, initialPlace, seqData, transitionIndex, placeIndex, maxCon, con, minCon, vertice, errors=0;
		
		//it will create a loop (for min conectivity) and then add randomly more arcs
		seqData=this.buildLoop(min(numTransitions, numPlaces));
		
		i=1;
		while ({ (i <= (numPlaces-this.petrinet.placeList.size()))}, {
			transitionIndex=rand(this.petrinet.transitionList.size());
			transition=this.petrinet.transitionList[transitionIndex];
			place=Thr44Place.new(this.petrinet.getNewNodeID());
			this.petrinet.addPlace(place);
			arc=Thr44Arc.new(transition, place, this.petrinet.getNewRelationID());
			this.petrinet.addArc(arc);
			transitionIndex=rand(this.petrinet.transitionList.size());
			transition=this.petrinet.transitionList[transitionIndex];
			arc=Thr44Arc.new(place, transition, this.petrinet.getNewRelationID());
			this.petrinet.addArc(arc);
		});
		while ({ (i <= (numTransitions-this.petrinet.transitionList.size()))}, {
			placeIndex=rand(this.petrinet.placeList.size());
			place=this.petrinet.placeList[placeIndex];
			transition=Thr44Transition.new(this.petrinet.getNewNodeID(), "");
			this.petrinet.addTransition(transition);
			arc=Thr44Arc.new(place, transition, this.petrinet.getNewRelationID());
			this.petrinet.addArc(arc);
			placeIndex=rand(this.petrinet.placeList.size());
			place=this.petrinet.placeList[placeIndex];
			arc=Thr44Arc.new(transition, place, this.petrinet.getNewRelationID());
			this.petrinet.addArc(arc);
		});
		//conectivity:
		minCon=max(this.petrinet.placeList.size(), this.petrinet.transitionList.size())*2;
		maxCon=this.petrinet.placeList.size()*this.petrinet.transitionList.size()*2;
		//FIX CON:
		con=floor( (maxCon-minCon)*connectivityIndex) + minCon;
		
		i=0;
		while({(i<(con-this.petrinet.relationList.size())) && (i>=0)}, {
			placeIndex=rand(this.petrinet.placeList.size());
			place=this.petrinet.placeList[placeIndex];
			transitionIndex=rand(this.petrinet.transitionList.size());
			transition=this.petrinet.transitionList[transitionIndex];
			
			vertice=[place, transition];
			if(rand(1.0)<0.5, {
				vertice=[transition, place];
			});
			seqData=this.petrinet.relationList.getDirectedRelationsWithNodes(vertice[0], vertice[1]);
			
			if(seqData.size()==0, {
				arc=Thr44Arc.new(vertice[0], vertice[1], this.petrinet.getNewRelationID());
				this.petrinet.addArc(arc);
				
				i=i+1;
			});
		});
	}
	
	closePaths{
		this.petrinet.placeList.do{
			arg place;
			var transitionIndex, transition, arc;
			if(place.nodeList.size()==0, {
				transitionIndex=rand(this.petrinet.transitionList.size());
				transition=this.petrinet.transitionList[transitionIndex];
				arc=Thr44Arc.new(transition, place, this.petrinet.getNewRelationID());
				this.petrinet.addArc(arc);
			});
			if(place.inputNodeList.size()==0, {
				transitionIndex=rand(this.petrinet.transitionList.size());
				transition=this.petrinet.transitionList[transitionIndex];
				arc=Thr44Arc.new(transition, place, this.petrinet.getNewRelationID());
				this.petrinet.addArc(arc);
			});
					
		};
		this.petrinet.transitionList.do{
			arg transition;
			var placeIndex, place, arc;
			if(transition.nodeList.size()==0, {
				placeIndex=rand(this.petrinet.placeList.size());
				place=this.petrinet.placeList[placeIndex];
				arc=Thr44Arc.new(place, transition, this.petrinet.getNewRelationID());
				this.petrinet.addArc(arc);
			});
			if(transition.inputNodeList.size()==0, {
				placeIndex=rand(this.petrinet.placeList.size());
				place=this.petrinet.placeList[placeIndex];
				arc=Thr44Arc.new(place, transition, this.petrinet.getNewRelationID());
				this.petrinet.addArc(arc);
			});
					
		};
	
	}
	
	//insert to initialPlace a sequence of nPlaces and nTransitions=nPlaces-1;
	//place outgoing connections will be reestablished in final place.
	insertSequence{
		arg numPlaces=2, initialPlace;
		var transition, arc, place, placeRelations, relatedTransitions, seqData; 
		place=initialPlace;
		
		placeRelations=place.relationList.copy();
		relatedTransitions=NodeList();
		for(0, placeRelations.size()-1, {
			arg i;
			relatedTransitions.add(placeRelations[i].getNode1());
			this.petrinet.removeArc(placeRelations[i]);
			i=i+1;
		});
		seqData=this.addSequence(numPlaces, initialPlace);
		
		for(0, relatedTransitions.size()-1, {
			arg i;
			arc=Thr44Arc.new(seqData.endPlace, relatedTransitions[i], this.petrinet.getNewRelationID());
			this.petrinet.addArc(arc);
			i=i+1;
		});
		^seqData;
		
	}
	//PRIVATE:
	//adds to initialPlace a sequence of nPlaces and nTransitions=nPlaces-1;
	addSequence{
		arg numPlaces=2, initialPlace;
		var transition, arc, place, seqData;
		place=initialPlace;
		seqData=();
		seqData.initialPlace=initialPlace;
		
		for(0, numPlaces-1, {
			arg i;
			
			transition=Thr44Transition.new(this.petrinet.getNewNodeID(), "");
			this.petrinet.addTransition(transition);
			arc=Thr44Arc.new(place, transition, this.petrinet.getNewRelationID());
			this.petrinet.addArc(arc);
			place=Thr44Place.new(this.petrinet.getNewNodeID());
			this.petrinet.addPlace(place);
			arc=Thr44Arc.new(transition, place, this.petrinet.getNewRelationID());
			this.petrinet.addArc(arc);
			i=i+1;
		});
		seqData.endPlace=place;
		^seqData;
		
	}
	//builds a sequence of nPlaces and nTransitions=nPlaces-1;
	buildSequence{
		arg numPlaces=2;
		var i, transition, arc, place, initialPlace, seqData; 
		
		place = Thr44Place.new(this.petrinet.getNewNodeID());
		
		initialPlace=place;
		this.petrinet.addPlace(place);
		
		seqData=this.addSequence(numPlaces-1, initialPlace);
		^seqData;
		
	}
	//builds a loop of nPlaces and nTransitions=nPlaces;
	buildLoop{
		arg numPlaces=2;
		var transition, arc, segment=this.buildSequence(numPlaces), seqData;
		////for loop:
		transition=Thr44Transition.new(petrinet.getNewNodeID(), "");
		petrinet.addTransition(transition);
	
		arc=Thr44Arc.new(segment.endPlace, transition, petrinet.getNewRelationID());
		petrinet.addArc(arc);
	
		arc=Thr44Arc.new(transition, segment.initialPlace, petrinet.getNewRelationID());
		petrinet.addArc(arc);
		//returns initialPlace;
		seqData=();
		seqData.initialPlace=segment.initialPlace;
		^seqData
		
	}
	//adds to initialPlace a loop of nPlaces and nTransitions=nPlaces;
	addLoop{
		arg numPlaces=2, initialPlace;
		var transition, arc, segment;
		segment=this.addSequence(numPlaces, initialPlace);
		////for loop:
		transition=Thr44Transition.new(petrinet.getNewNodeID(), "");
		petrinet.addTransition(transition);
		arc=Thr44Arc.new(segment.endPlace, transition, petrinet.getNewRelationID());
		petrinet.addArc(arc);
		arc=Thr44Arc.new(transition, segment.initialPlace, petrinet.getNewRelationID());
		petrinet.addArc(arc);
	}
	fork{
		arg forkSize=2, initialNode;
		var transition, place, arc, result, dataArray=[];
		result=();
		if(initialNode.class==Thr44Place, {
			forkSize.do({
				arg i;
				transition=Thr44Transition.new(petrinet.getNewNodeID(), "");
				petrinet.addTransition(transition);
				dataArray=dataArray.add(transition);
				arc=Thr44Arc.new(initialNode, transition, petrinet.getNewRelationID());
				petrinet.addArc(arc);
				
			});
			result.forkTransitions=dataArray;
			^result;
		});
		if(initialNode.class==Thr44Transition, {
			forkSize.do({
				arg i;
				place = Thr44Place.new(this.petrinet.getNewNodeID());
				petrinet.addPlace(place);
				dataArray=dataArray.add(place);
				arc=Thr44Arc.new(initialNode, place, petrinet.getNewRelationID());
				petrinet.addArc(arc);
			});
			result.forkPlaces=dataArray;
			^result;
		});
	
	}
	join{
		arg nodes;
		var nodeType, allSameType=true, newNode, arc, result=();
		nodeType=nodes[0].class;
		nodes.do({
			arg node;
			if(node.class!=nodeType, {
				"not all nodes are the type"+nodeType+"!".postln;
				^nil;	
			});
		});
		
		if(nodeType==Thr44Place, {
			newNode=Thr44Transition.new(petrinet.getNewNodeID(), "");
			petrinet.addTransition(newNode);
			result.endTransition=newNode;
			
		});
		if(nodeType==Thr44Transition, {
			newNode = Thr44Place.new(this.petrinet.getNewNodeID());
			petrinet.addPlace(newNode);
			result.endPlace=newNode;
		});
		nodes.do({
			arg node;
			arc=Thr44Arc.new(node, newNode, petrinet.getNewRelationID());
			petrinet.addArc(arc);	
		});
		^result;
			
	}

}

