Thr44PRepulsor : Thr44PForce{
	var <>node1;
	var <>k;
	var <>l;
	*new{
		arg node0, node1, k, l;
		^super.new(node0, node1, k, l).init(node0, node1, k, l);
	}
	init{
		arg node0, node1, k, l;
		this.node0=node0;
		this.node1=node1;
		this.k=k;
		this.l=l;
		if(k==nil || l==nil, {("ERROR: k and l must be defined: k:"+k+" l:"+l).postln});
		^this;
	}
	calculate{
		var dx, dy, dist, force;
		dx=this.node0.x - this.node1.x;
		dy=this.node0.y - this.node1.y;
		//var dz:Number=node0.vector3D.z-node1.vector3D.z;
		dist = sqrt((dx * dx) + (dy * dy));//+(dz*dz))
		if(dist < this.l, {
			force=this.k * (dist - this.l)/dist;
			this.node0.ax = this.node0.ax + (force*dx);
			this.node0.ay = this.node0.ay + (force*dy);
			//node0.az-=force*dz
			this.node1.ax = this.node1.ax - (force*dx);
			this.node1.ay = this.node1.ay- (force*dy);
			//node1.az+=force*dz;

		});
	
	}
}

