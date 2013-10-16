Thr44Place : Thr44Node{
    var <type="Place";
	var <directed=true;
	var <>inputNodeList;
	var <>inputRelationList;
    var <>weight=1;
    var <>nTokens=0;
    var <>k=inf;
    //
	
   	*new{
		arg id, label="";
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
		^this;
		
	}
	//method to ad tokens to place
	addTokens{
		arg n;
		var hasTokens=false;
		if( this.nTokens<this.k, {
			this.nTokens=min(this.nTokens+n, this.k);
			
			for(0, this.relationList.size()-1, {
	  			arg i;
	  			var relation, transition;
	  			relation=this.relationList[i];
	  			if(relation.weight<=this.nTokens){
		  			transition=relation.getNode1();
	  				transition.update();
	  			};
    				i=i+1;
  			});
		});
	}
	//method to remove tokens from place
	removeTokens{
		arg n;
		this.nTokens=max(this.nTokens-n, 0);
			
	}
}