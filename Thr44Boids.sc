Thr44Boid{
	var <type="Thr44Boid";
  	var <>pos;
	var <>v;
	var <>a;
	var <>r=10;
	var <>maxForce=0.06;
	var <>maxSpeed=4;
	var <>w=400;
	var <>h=400;

	/* EXAMPLE:
(
a=Odef(\test, {
	arg num;
	num;
});
b=Thr44Boids.new;
20.do({
	b.addOdef(a);
});
b.gui;

	b.maxSpeed=5

b.v=0;
b.angleSpan=0.2;
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


	//Constructor:
	*new{
		arg x, y;
		^super.new.init(x, y);
	}
	init{
		arg x, y;
		this.pos=RealVector[x, y];
		this.v=RealVector[1.0.rand2, 1.0.rand2];
		this.a=RealVector[0,0];
		^this;
	}
	run{
		arg boids;
		var sep=this.separate(boids);
		var ali=this.align(boids);
		var coh=this.cohesion(boids);
		sep=sep*1.5;
		ali=ali*1.5;
		coh=coh*1.0;
		this.applyForce(sep);
		this.applyForce(ali);
		this.applyForce(coh);
	}
	applyForce{
		arg force;
		this.a=this.a+force;
	}
	updateForce{
		this.v=this.v+this.a;
		this.v=this.v.limit(this.maxSpeed);
		this.pos=this.pos+this.v;
		this.a=this.a*0;

		if(this.pos[0]<0, {
			this.pos[0]=this.w;
		});
		if(this.pos[0]>this.w, {
			this.pos[0]=0;
		});
		if(this.pos[1]<0, {
			this.pos[1]=this.h;
		});
		if(this.pos[1]>this.h, {
			this.pos[1]=0;
		});

	}
	separate{
		arg boids;
		var nBoids=boids.size();
		var desiredSep=25;
		var steer=RealVector[0, 0, 0];
		var count=0;

		nBoids.do{
			arg i;
			var other=boids[i].boid;
			var d=this.pos.dist(other.pos);
			var diff;
			if((d>0)&&(d<desiredSep), {
				diff=this.pos-other.pos;
				diff=diff.normalize();
				diff=diff/d;
				steer=steer+diff;
				count=count+1;
			});
		};
		if(count>0, {
			steer=steer/count;
		});
		if(steer.norm>0, {
			steer=steer.normalize();
			steer=steer*this.maxSpeed;
			steer=steer-this.v;
			steer=steer.limit(this.maxForce);
		});
		^steer;
	}

	align{
		arg boids;
		var nBoids=boids.size();
		var dist=8;
		var sum=RealVector[0, 0];
		var steer;
		var count=0;

		nBoids.do{
			arg i;
			var other=boids[i].boid;
			var d=this.pos.dist(other.pos);

			if((d>0)&&(d<dist), {
				sum=sum+other.v;
				count=count+1;
			});
		};
		if(count>0, {
			sum=sum/count;
			sum=sum.normalize();
			sum=sum*this.maxSpeed;
			steer=sum/this.v;
			steer=steer.limit(this.maxForce);
			^steer;
		}, {
			^RealVector[0, 0];
		});

	}

	cohesion{
		arg boids;
		var nBoids=boids.size();
		var dist=50;
		var sum=RealVector[0, 0];
		var count=0;

		nBoids.do{
			arg i;
			var other=boids[i].boid;
			var d=this.pos.dist(other.pos);
			if((d>0)&&(d<dist), {
				sum=sum+other.pos;
				count=count+1;
			});
		};
		if(count>0, {
			sum=sum/count;
			^this.seek(sum);
		}, {
			^RealVector[0, 0];
		});

	}

	seek{
		arg target;
		var desired=target/this.pos;
		var steer;
		desired=desired.normalize();
		desired=desired*this.maxSpeed;
		steer=desired/this.v;
		steer=steer.limit(this.maxForce);
		^steer;
	}

}

Thr44Boids{
	var <type="Thr44Boids";
  	var <>boidsList;//=NodeList.new();
	var <>nBoids=0;
	var <>width=400;
	var <>height=400;
	var w;
	var <>attract=true;
	var <>run=false;


	*new{
		^super.new.init();
	}
	init{
		this.boidsList=[];
		^this;
	}

	addOdef{
		arg odef;
		var x,y,angle,obj, boid;
		x=200.rand2;
		y=200.rand2;
		obj=(odef:odef, boid:Thr44Boid.new(x, y));
		this.boidsList=this.boidsList.add(obj);
		this.nBoids=this.nBoids+1;
	}
	removeOdefs{
		this.boidsList=[];
		this.nBoids=0;
	}
	calculateBoids{
		this.nBoids.do{
			arg i;
			var boid=this.boidsList[i].boid;
			boid.run(this.boidsList);
		};
		this.nBoids.do{
			arg i;
			var boid=this.boidsList[i].boid;
			boid.updateForce();
		};
	}

	gui{
		this.run=true;
		if(w==nil, {
			w = Window.new.front;
			w.dump;
			w.view.background_(Color.white);
			w.drawFunc = {
				this.nBoids.do{
					arg i;
					var boid, xf, yf, angle;
					boid=this.boidsList[i].boid;
					// set the Color
					Pen.color = Color.black;
					Pen.addRect(Rect(boid.pos[0]-4, boid.pos[1]-4, 8, 8));
					Pen.perform(\fill);
					/*Pen.color = Color.new(0, 0, 0, 0.2);
					Pen.addArc(boid.pos[0]@boid.pos[1], this.dist, 0, 2*pi);
					Pen.perform(\stroke);
					*/
					Pen.color = Color.black;
					Pen.moveTo(boid.pos[0]@boid.pos[1]);
					angle=boid.pos.angle(RealVector[1, 0]);
					xf=((20)*(cos(angle)));
					yf=((20)*(sin(angle)));
					["xf:", xf, "yf:", yf].postln;
					Pen.lineTo((boid.pos[0]+xf)@(boid.pos[1]+yf));
					Pen.perform(\stroke);

				};
			};
		});
		w.refresh;
		//	("started").postln;
		{ while { this.run } {
			this.calculateBoids();
			w.refresh;
			0.06.wait } }.fork(AppClock);
	}
}

