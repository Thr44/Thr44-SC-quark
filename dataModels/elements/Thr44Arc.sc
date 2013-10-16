Thr44Arc : Thr44Relation{
	var <type="Arc";
	
	*new{
		arg node0, node1, id, label;
		^super.new.init(node0, node1, id, label);
	}
	init{
		arg node0, node1, id, label;
		this.directed=true;
		super.init(node0, node1, id, label); 
		^this;
		
	}
}