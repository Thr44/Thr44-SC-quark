Thr44Transition : Thr44Node{
	var <type="Transition";
	var <directed=true;
	var <>inputNodeList;
	var <>inputRelationList;
	var >waitTimeValue;
	
   	*new{
		arg id, label;
		^super.new.init(id, label);
	}
	init{
		arg id, label;
		this.inputNodeList=NodeList.new();
		this.inputRelationList=RelationList.new();
		this.id=id;
		this.label=label;
		this.nodeList=NodeList.new();
		this.relationList=RelationList.new();	
		this.waitTimeValue=0;
		^this;
		
	}
	//update called when tokens are added to input places!
	update{
		var toPlay=network.isPlaying, relation;
		var i=0;
		
		//should validate if odef is playing:
		if(this.refObj.isPlaying, {
			^this;	
		});
		
		//validates if every input place has enough tokens!!
		while({ (i<this.inputRelationList.size()) && (toPlay) }, {
			relation=this.inputRelationList[i];
			if(relation.weight>relation.getNode0().nTokens){
				toPlay=false;
			};
			i = i + 1;
		});
		//if input places have tokens remove them and doAction:
		["transition requests play to network:", toPlay].postln;
		if(toPlay==true){
			for(0, this.inputRelationList.size()-1, {
	  			arg i;
	  			var relation=this.inputRelationList[i];
	  			var place=relation.getNode0();
	  			place.removeTokens(relation.weight);
    				i=i+1;
  			});
  			this.doAction();
		};
		
	}
	waitTime{
		arg val;
		this.waitTimeValue=	val;
	}
	//method that calls transition action
	doAction{
		this.network.playingSynths=this.network.playingSynths+1;
		this.network.playTransition(this);
	}
	//called when action is complete
	onActionComplete{
		var i=0;
		{
			waitTimeValue.value.wait;
			this.network.playingSynths=this.network.playingSynths-1;
			
			//dispatches tokens to related places
			for(0, this.relationList.size()-1, {
				var relation=this.relationList[i];
				relation.getNode1().addTokens(relation.weight);
				i=i+1;
			});
			this.update();
		}.fork;
	}
}