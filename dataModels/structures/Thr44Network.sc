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
		^this;
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