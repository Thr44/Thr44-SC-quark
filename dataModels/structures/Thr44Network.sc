Thr44Network{
	var <type="Thr44Network";
  	var <>newNodeID=0;
  	var <>newRelationID=0;
  	var <>nodeList;//=NodeList.new();
  	var <>relationList;//=RelationList.new();


	//Constructor:
	*new{
		^super.new.init();
	}
	init{
		this.nodeList=NodeList.new();
		this.relationList=RelationList.new();
		^this;
	}
	//
	getNewNodeID{
	 	var id=this.newNodeID;
 		this.newNodeID=this.newNodeID+1;
 		^id;
	}
	getNewRelationID{
 		var id=this.newRelationID;
 		this.newRelationID=this.newRelationID+1;
 		^id;
	}
	//
	addNode{
		arg node;
		node.setNetwork(this);
		this.nodeList.addNode(node);
		//should return a bool??
		this.update();
	}
	addNodeIfNew{
		arg node;
		node.setNetwork(this);
		//node.id=getNewNodeID();
		this.nodeList.addNodeIfNew(node);//IfNew(node);
		//should return a bool??
		this.update();
	}
	removeNode{
		arg node;
  		var filteredRelations=this.relationList.getRelationsWithNode(node);
  		for(0, filteredRelations.size-1, {
	  		arg i;
    			this.removeRelation(filteredRelations[i]);
    			i=i+1;
  		});
  		node.setNetwork(nil);
  		this.nodeList.removeNode(node);
  		//should return bool?
  		this.update();
	}
	addRelation{
		arg relation;
		var added;
  		//relation.id=getNewRelationID();

 		added=this.relationList.addRelationIfNew(relation);
 		if(added==false) {^false};
		//
 		relation.getNode0().nodeList.addNodeIfNew(relation.getNode1());
 		relation.getNode0().relationList.addRelation(relation);
 		if(relation.directed==false || this.type!="Tree", {
 			relation.getNode1().nodeList.addNodeIfNew(relation.getNode0());
 			relation.getNode1().relationList.addRelation(relation);
 		});
		this.nodeList.addNodeIfNew(relation.getNode0());
		this.nodeList.addNodeIfNew(relation.getNode1());
  		//should return a bool??
  		this.update();
  		^true;
	}
	removeRelation{
		arg relation;
		this.relationList.removeRelation(relation);
		relation.getNode0().nodeList.removeNode(relation.getNode1());
		relation.getNode0().relationList.removeRelation(relation);
		relation.getNode1().nodeList.removeNode(relation.getNode0());
		relation.getNode1().relationList.removeRelation(relation);
		this.update();
	}
	getNodeWithId{
		arg id;
		^this.nodeList.getNodeWithId(id);
	}
	getRelationWithId{
		arg id;
		^this.relationList.getNodeWithId(id);
	}
	//this method is called when network changes! to be overriden!
	update{
		"update@Thr44Network".postln;
	}
	log{
		("NETWORK numNodes:"+this.nodeList.size()+" numRelations:"+this.relationList.size()).postln;
		this.nodeList.log();
	}



	addOdefs{
		arg odefs, nodes;
		var odefClass;
		odefClass=odefs.class;
		odefs=[odefs].flat;
		if(nodes==nil, {nodes=this.nodeList});
		nodes=[nodes].flat;

		for(0, odefs.size()-1, {
			arg i;
			if(this.odefList.includes(odefs[i]).not, {
				this.odefList=this.odefList.add(odefs[i]);
			});
		});


		//["this.odefList:", this.odefList, transitions].postln;
		for(0, nodes.size()-1, {
			arg i;
			var odef=this.odefList[i%this.odefList.size()].getInstance("petri"++i);
			odef.addTag("petri");
			//["adding odef:", odef.key, odef.instanceParent.key].postln;
			//["it contains:", this.nodeList[i].refObj].postln;
			nodes[i].refObj=odef;
			odef.refObj=nodes[i];
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
		for(0, this.nodeList.size()-1, {
			arg i;
			this.nodeList[i].refObj.refObj=nil;
			this.nodeList[i].refObj=nil;
		});
	}

	gui{
		this.gui=Thr44NetworkGui.new(this);
	}


}
Thr44Node{
	var <>id;
    var <>label ="";
    var <type="Node";
   var <>network;
    var <>nodeList;
    var <>relationList;
    var <>parent;
    var <>physics;
    var <>refObj; //any object to map to node
	var <>defAction;

	*new{
		arg id, label="";
		^super.new.init(id, label);
	}
	init{
		arg id, label;
		this.id=id;
		this.label=label;
		this.nodeList=NodeList.new();
		this.relationList=RelationList.new();
		this.defAction={};
		^this;
	}
	defaultAction{
		^this.defAction(this);
	}
	applyAction{
		arg func;
		this.defAction=func;
	}
	setNetwork{
		arg network;
		this.network=network;
		//this.id=network.getNewNodeID();
	}
	getLabel{
		^this.label;
	}
	getID{
		^this.id;
	}
	log{
		("	id: "+this.getID()+" label:"+this.getLabel()).postln;
	}
}

Thr44Relation : Thr44Node{
	var <>node0;
    	var <>node1;
    	var <>weight=1;
	var <type="Relation";
	var <>directed=false;

	getNode0{
		^this.node0;
	}
    	getNode1{
		^this.node1;
	}
	*new{
		arg node0, node1, id, label="";
		^super.new.init(node0, node1, id, label);
	}
	init{
		arg node0, node1, id, label;

		super.init(id, label);
		this.id=id;
		this.label=label;
		this.node0=node0;
		this.node1=node1;
		this.nodeList=NodeList.new();
		this.relationList=RelationList.new();
		^this;

	}
	log{
		("	id: "+this.getID()+" label:"+this.getLabel()+"     related nodes: "+this.node0.id+" "+this.node1.id).postln;
	}
}
NodeList : List{
	//var <>list;
	*new{
		^super.newClear(0).init();
	}
	init{
		//this.list=List.new();
		^this;
	}

	addNode{
		arg node;
		^super.add(node);
	}
	addNodeIfNew{
		arg node;
		var i=0;
		while({i < this.size}, {
			if(this[i]==node, {
				^false;
			});
			i=i+1;
		});
		this.addNode(node);
		^true;
	}
	removeNode{
		arg node;
		var i=0;
		while({i < this.size}, {
			if(this[i]==node, {
				this.removeAt(i);
				^nil;
			});
			i=i+1;
		});


	}
	getNodeWithId{
		arg id;
		var i=0;
		while({i < this.size}, {
			if(this[i].id==id, {
				^this[i];
			});
			i=i+1;
		});

	}
	/*
	size{
		^this.list.size;
	}
	at{
		arg index;
		^this.list[index];
	}

	put{
		arg index, item;
		this.list=this.list[index]=item;
	}
	clone{
		^list.copy();
	}
	*/
	log{
		if(this.size()==0, {^""});
		for(0, this.size-1, {
			arg i;
			this[i].log();
			i=i+1;
		});
	}
}
RelationList : NodeList{
	*new{
		^super.new.init();
	}
	init{
		^super.init();
	}
	getRelationsWithNode{
		arg node;
  		var filteredRelations=List.new();
  		var i=0;
  		var relation;

		while({i < this.size}, {
			relation=this[i];
			if( (relation.node0==node)||(relation.node1==node), {
      			filteredRelations.add(relation);

    			});
			i=i+1;
		});
     		^filteredRelations;
  	}
  	getDirectedRelationsWithNodes{
		arg node0, node1;
  		var filteredRelations=List.new();
  		var i=0;
  		var relation;

		while({i < this.size}, {
			relation=this[i];
			if( (relation.node0==node0) && (relation.node1==node1), {
      			filteredRelations.add(relation);

    			});
			i=i+1;
		});
     		^filteredRelations;
  	}
  	getRelationWithId{
	 	arg id;
	 	^getNodeWithId(id);
  	}
  	addRelationIfNew{
	 	arg relation;
	 	var i=0;
		while({i < this.size}, {
			if(this[i]==relation, {
				^false;
			});
			i=i+1;
		});
		this.addRelation(relation);
		^true;


  	}
	addRelation{
		arg relation;
		this.add(relation);
	}
	removeRelation{
		arg relation;
		var i=0;

		while({i < this.size}, {
			if(this[i]==relation, {
				this.removeAt(i);
				^nil;
			});
			i=i+1;
		});

	}


}





Thr44NetworkGui{
	var <>gui;
	var <>pS;
	var <>network;
	var <>w;
	var <>dist=50;
	var <>repulsorDist=100;
	var <>isRunningGui=false;

	*new{
		arg network;
		^super.new.init(network);
	}
	init{
		arg network;
		this.update(network);


	}
	update{
		arg network;

		this.network=network;
		if( this.network==nil, {"error1".postln});
		//["||----->", this.network].postln;
		if( this.network.nodeList==nil, {"error2".postln});
		//["   ||----->", this.network.nodeList].postln;
		if( this.network.relationList==nil, {"error3".postln});
		//["      ||----->", this.network.relationList].postln;

		if(this.pS.isNil, {this.pS=Thr44ParticleSystem.new()});

		this.network.nodeList.do{
			arg node, i;
			//[i, node].postln;
			if(node.physics.isNil, {
				node.physics=Thr44Particle.new(nil, node);
			});
		};


		this.network.relationList.do{
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
		if( this.network==nil, {^this});
		//["----->", this.network].postln;
		if( this.network.nodeList==nil, {^this});
		//["   ----->", this.network.nodeList].postln;
		if( this.network.relationList==nil, {^this});
		//["      ----->", this.network.relationList].postln;
		this.network.relationList.do{
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
				this.network.nodeList.do{
					arg transition;
					if( (transition.refObj.isNil.not) && (transition.refObj.isPlaying), {
						Pen.color = Color.green;
					}, {
						Pen.color = Color.black;
					});

					Pen.addRect(Rect(transition.physics.x-3,transition.physics.y-3, 6, 6));
					Pen.perform(\fill);
					Pen.color = Color.black;
					Pen.font = Font( "Georgia", 9 );
					Pen.stringAtPoint( "id:"+transition.id.asString(), (transition.physics.x+6)@(transition.physics.y-8) );

				};
				//"and here?".postln;
				/*
				this.network.placeList.do{
					arg place;

					Pen.color = Color.grey;
					Pen.fillOval(Rect(place.physics.x-6,place.physics.y-6, 12, 12));
					Pen.color = Color.black;
					Pen.font = Font( "Georgia", 9 );
					Pen.stringAtPoint( "id:"+place.id.asString()+"\ntokens:"+place.nTokens, (place.physics.x+8)@(place.physics.y-4) );
					//Pen.perform(\fill);

				};
				*/
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

