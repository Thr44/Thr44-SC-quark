OM {
	//mcardoso@344server.org
	classvar <>basicSynthDef;
	classvar <>omSynthDef;
	//Constructor:
	*new{
		^super.new.init();
	}
	//constructor function
	init{
		("Open Music Library for supercollider").postln;
		("mcardoso@344server.org").postln;
		^this;
	}
	playSeq{
		arg noteSeq=[60], onset=[1], duration=[1], amp=[1];
		var serv;
		Routine.new{
			OM.omSynth();
			serv=Server.default;
			serv.sync;
			Pdef(\playSeq).set(\instrument, \omSynth);
			Pdef(\playSeq,
				Pbind(
					\instrument,Pfunc({ |e| e.instrument }),
					\midinote, 	Pseq(noteSeq, inf),
					\dur, 		Pseq (onset, inf),
					\envdur, 	Pseq (duration, inf),
					\amp, 		Pseq (amp, inf)
			)
		   );
		   Pdef(\playSeq).play;
	     }.play;
	}
	////
	*basicSynth{
		arg s=Server.default;
						//var r=Routine.new{
		if(OM.basicSynthDef == nil, {
			OM.basicSynthDef=SynthDef(\basicSynth,{
				arg freq=440, pan=0.0, vol=0.5, envdur=2.5, envType=0;
				var signal, envArray, env;
				env = EnvGen.ar(Env.perc(0.01, envdur), doneAction:2);
				signal = Pan2.ar(SinOsc.ar(freq), pan) * env  * vol;
				Out.ar(0, signal);
			}).send(s);
		}, {});
						//s.sync;
						//^Synth(\harmonicOsc, [\freq, 440.00001], s);
						//}.play;
	}
	*omSynth{
		arg s=Server.default;
		var r=Routine.new{
			if(OM.omSynthDef == nil, {
				OM.omSynthDef=SynthDef(\omSynth, {
					arg out=0, freq=440, envdur=1, amp=0.4, pan=0;
					var x, env;
					env = EnvGen.kr(Env.perc(0.001, envdur, amp), doneAction:2);
					x = Mix.ar([FSinOsc.ar(freq, pi/2, 0.5), Pulse.ar(freq,Rand(0.3,0.7))]);
					x = RLPF.ar(x,freq*4, rrand(0.04, 0.5));//Rand(0.04,1));
					x = Pan2.ar(x,pan);
					Out.ar(out, x*env);
			}).store;
			SynthDescLib.global.read;
			}, {});
			s.sync;

						//^Synth(\harmonicOsc, [\freq, 440.00001], s);
		}.play;
	}

	//Class method (*):
	//dx->x
	*dx2x{
		arg f=1, a=[34, 54, 34];
		var i=0, b=[f];
		while({i<a.size}, {
			b=b.add(a[i]+b[i]);
			i=i+1;
		}
		);
		^b;
	}
	*x2dx{
		arg a=[ 34, 88, 122 ];
		var i=1, c=[];
		while({i<a.size}, {
			c=c.add(a[i]-a[i-1]);
			i=i+1;
		});
		^c;
	}
	//interpolation:
	/*
	(
	var a, b, steps, r;
	a=[60, 64, 67, 78];
	b=[72, 65, 79];
	steps=4;
	curve=1.5;
	r=OM.interpolation(a, b, steps, curve);
	r.postln;
	)
	*/
	*nthrandom{
		arg a=[];
		^a[a.size.rand];
	}
	*xappend{
		//???...args.size.postln;
	}
	*omScale{
		arg intervals=[ 34, 88, 122 ], min=0, max=1;
		var res=intervals.normalize(min, max);
		^res;
	}
	*interpolation{
		arg a=[0, 1], b=[0, 1], steps=3, curve=1;
		var c, d, e, i, j, p, min;
		//a=[60, 64, 67, 78];
		//b=[72, 65, 79];
		//steps=4;
		min=min(a.size, b.size);
		c=c.add(a);
		i=0;
		while({i<min}, {
			d=d.add( (b[i]-a[i]) );
			i=i+1;
		});
		i=1;
		while({i<(steps-1)}, {
			e=[];
			j=0;
			while({j<min}, {
				p=(d[j]/(steps-i)*curve);
				d[j]=d[j]-p;
				e=e.add(p+(c[i-1][j]));
				j=j+1;
			});
			i=i+1;
			c=c.add(e);
		});
		c=c.add(b);
		^c;
	}
}