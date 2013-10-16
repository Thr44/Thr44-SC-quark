Thr44Particle {
	var <>ax=0;
	var <>ay=0;
	var <>az=0;
	var <>vx=0;
	var <>vy=0;
	var <>vz=0;
	var <>x=0;
	var <>y=0;
	var <>z=0;
	var <>index;
	var <>action;
	var <>object;
	
	//Constructor:
	*new{ 
		arg index, object;
		^super.new.init(index, object);
	}
	//constructor function
	init{
		arg index, object;
		this.index=index;
		this.object=object;
		this.action={};
		^this;
	}
	run{
		action.value;
	}
	
}