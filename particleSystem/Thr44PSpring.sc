Thr44PSpring : Thr44PForce{
	var <>node1;
	var <>k;
	var <>length;
	*new{
		arg node0, node1, k, length;
		^super.new(node0, node1, k, length).init(node0, node1, k, length);
	}
	init{
		arg node0, node1, k, length;
		this.node0=node0;
		this.node1=node1;
		this.k=k;
		this.length=length;
		if(k==nil || length==nil, {("ERROR: k and l must be defined: k:"+k+" length:"+length).postln});
		^this;
	}
	calculate{
		var dx, dy, dist, force;
		dx=this.node1.x - this.node0.x;
		dy=this.node1.y - this.node0.y;
		//var dz:Number=node0.vector3D.z-node1.vector3D.z;
		dist=(sqrt((dx * dx) + (dy * dy)));//+(dz*dz))
		force=this.k*(dist - this.length)/dist;
		/*
		if(dist>(this.length * 2), {
			["dist:", dist, "\nforce:", force].postln;	
		});
		*/
		this.node0.ax = this.node0.ax - (force*dx);
		this.node0.ay = this.node0.ay - (force*dy);
		//node0.az-=force*dz
		this.node1.ax = this.node1.ax + (force*dx);
		this.node1.ay = this.node1.ay + (force*dy);
		//node1.az+=force*dz;
	
	}
}

