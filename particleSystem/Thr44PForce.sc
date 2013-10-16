Thr44PForce{
	var <>node0;
	
	//Constructor:
	*new{
		arg node;
		^super.new.init(node);
	}
	//constructor function
	init{
		arg node;
		this.node0=node;
		^this;
	}
	calculate{
		("override this!!!!!").postln;
	}
	
}
