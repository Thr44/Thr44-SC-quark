a=RealVector[2, 3]

a.polar(1)

20*a

a.norm/400
a.angle(RealVector[1, 0])


/* EXAMPLE:
(
a=Odef(\test, {
	arg num;
	num.postlln;
});
b=Thr44Boids.new;
b.applyAction={
	|boid|
    var rho, azimuth;
rho=
	//["----->", boid.x, boid.y, odef].postln;
};
50.do({
	b.addOdef(a);
});
b.gui;

	b.applyAction(2,3)

	b.maxSpeed=25

	b.
b.v=RealVector[10, 10];
b.angleSpan=1.2;
b.dist=60;
b.attractionForce=1;
b.attract=true;
b.visibilityAngle=pi;
b.width=800;
b.height=800;



	b.removeOdefs()

	b.calculateBoids()
)
c=b.boidsList[10].boid
	c.run(b.boidsList).pos[0]
b.boidsList.do({
	arg boid;
	boid.postln;
	//boid=b.boidsList[i];
	boid.angle=10.0.rand2;
});

	*/
a=1/(3/0)

Odef(\noise, {
	arg rho=0.3, azimuth=0;
	#w, x, y, z = BFEncode1.ar(Saw.ar(rrand(400,3200), mul:LFDNoise3.ar(0.5)),azimuth, 0, rho, 0.3);
	//decode for 4 channels
	BFDecode1.ar(w, x, y, z, [-0.25pi, 0.25pi, 0.75pi, 1.25pi], 0);
});


b=Thr44Boids.new;
v=RealVector[1, 0];
b.applyAction={
	|boid, odef|
    var rho, azimuth;
	rho=boid.pos.norm/400;
	if(rho.isNaN, {boid.pos=RealVector[1, 0]});
	["--->", boid.pos, rho].postln;
	azimuth=boid.pos.angle(v);
	azimuth.postln;
	//
	//odef.set([\rho, rho, \azimuth, azimuth])
};
10.do({
	|i|
	var odef=Odef(\noise).clone("noise"++i);
	b.addOdef(odef);
	//odef.play;
});
b.gui;

