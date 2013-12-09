GrainSynthDefs{
	//usage:
	/*
	GrainSynthDefs.loadGrainBuf("buf_grain_test", [-0.5pi, 0.5pi], 0, 1, [1, 1]).send(s);
	b = Buffer.read(s, "sounds/a11wlk01-44_1.aiff");
	a=Synth(\buf_grain_test, [\sndbuf, b, \envbufnum,  GrainEnv.rexpodec]);
	a.set(\triggerFreq, 40)
	a.set(\envbufnum, GrainEnv.expodec)
	 a.set(\envbufnum, GrainEnv.blackman)
	a.set(\envbufnum, GrainEnv.rexpodec)
	*/
	*loadGrainBuf{
		|synthDefName, speakersAzimuth, speakersElevation=0, speakerMaxDist, speakersDistances|
		var synthDef;
		synthDef = SynthDef(synthDefName, {
			arg out=0, triggerFreq=10, dur=0.1, sndbuf, rate, pos, interp=2, envbufnum = -1, gate=1, azimuth, elevation, rho, gain, wComp;
			var in, trigger, env, w, x, y, z; 
			//audio signal:
			rate = LFNoise1.kr.range(0.5, 2);
			pos = LFNoise2.kr(0.1).range(0, 1);
			trigger = Impulse.kr(triggerFreq);
			//env = EnvGen.kr(
			//		Env([0, 1, 0], [1, 1], \sin, 1),
			//		gate,
			//		levelScale: gain,
			//		doneAction: 2);
			in=GrainBuf.ar(1, trigger, dur, sndbuf, rate, pos, interp, 0, envbufnum);
			//
			azimuth = MouseX.kr(-pi, pi); //-  in radians, -pi to pi
			elevation = 0; //-  in radians, -0.5pi to +0.5pi
			rho = MouseY.kr(0.0, 4.0); //- the speaker radius (1 places sound at the radius, <1 within the radius, >1 is outside the radius) beyond the radius, a distance scaling is applied (no filtering is done).
			gain = 0.3; // - a control rate level input.
			wComp = 1; // - chooses how the W channels is scaled. If 0, a scaler of 0.707 is used. If 1, W is varied according to the scaling of the X, Y and Z channels. 1 is the default.
			//
			#w, x, y, z = BFEncode1.ar(in, azimuth, elevation, rho, gain, wComp);
			//decode for 4 channels
			Out.ar(out, BFDecode1.ar1(w, x, y, z, speakersAzimuth, speakersElevation, speakerMaxDist, speakersDistances));
		}); 
		^synthDef;
	}
	//
	/*
	GrainSynthDefs.loadCloudGrainBuf("tGrains", [-0.5pi, 0.5pi], 0, 1, [1, 1]).send(s);
	b = Buffer.read(s, "sounds/a11wlk01-44_1.aiff");
	a=Synth(\tGrains, [\sndbuf, b, \envbufnum,  GrainEnv.rexpodec]);
	{300.do{
					arg i; 
					var timeprop = (i/199.0)**3;
					var rate=exprand(0.2, 3.0-(0.006*i));
					Synth(\tGrains,[\sndbuf, b, \rate,rate,\gain, exprand(0.1, 0.9)*1, \envbufnum,  -1]  );
					rrand((timeprop*0.1).max(0.01),timeprop*0.3).wait
					}; 
				}.fork
	*/
	//TODO: still needs to fix positioning: azimuth, rho 
	*loadCloudGrainBuf{
		|synthDefName, speakersAzimuth, speakersElevation=0, speakerMaxDist, speakersDistances|
		var synthDef;
		synthDef = SynthDef(synthDefName, {
			arg out=0, sndbuf, rate=1, amp=1, interp=2, envbufnum = -1, gate=1, azimuth=0, elevation=0, rho, gain=0.3, wComp=1;
			var in, trigger, env, soundDur, dur, pos, w, x, y, z; 
			//audio signal:
			//rate = LFNoise1.kr.range(0.5, 2);
			pos = LFNoise2.kr(0.1).range(0, 1);
			//
			trigger = Impulse.ar(1, 0);
			env=Line.kr(0, 1, 1);
			//soundDur=BufDur.kr(sndbuf)/rate;
			dur=TRand.kr(0.09, 0.5, trigger);
			//pos=TRand.kr(dur, soundDur-dur, trigger);
			
			in=GrainBuf.ar(1, trigger, dur, sndbuf, rate, pos, interp, 0, envbufnum)*env;
			//
			azimuth = MouseX.kr(-pi, pi);//LFNoise1.kr(1).range(-pi, pi);//WhiteNoise.kr(0.6).range(-pi, pi); //-  in radians, -pi to pi
			elevation = 0; //-  in radians, -0.5pi to +0.5pi
			rho = MouseY.kr(0.0, 4.0); //- the speaker radius (1 places sound at the radius, <1 within the radius, >1 is outside the radius) beyond the radius, a distance scaling is applied (no filtering is done).
			//gain = 0.3; // - a control rate level input.
			//wComp = 1; // - chooses how the W channels is scaled. If 0, a scaler of 0.707 is used. If 1, W is varied according to the scaling of the X, Y and Z channels. 1 is the default.
			//
			#w, x, y, z = BFEncode1.ar(in, azimuth, elevation, rho, gain, wComp);
			//decode for 4 channels
			Out.ar(0, BFDecode1.ar1(w, x, y, z, speakersAzimuth, speakersElevation, speakerMaxDist, speakersDistances));
			FreeSelfWhenDone.kr(env);
		}); 
		^synthDef;
	}
	*loadGrainBuf1{
		|synthDefName, speakersAzimuth, speakersElevation=0, speakerMaxDist, speakersDistances|
		var synthDef;
		synthDef = SynthDef(synthDefName, {
			arg out=0, sndbuf, rate=1, amp=1, interp=2, envbufnum = -1, gate=1, azimuth=0, elevation=0, rho, gain=0.3, wComp=1;
			var in, trigger, env, soundDur, dur, pos, w, x, y, z; 
			//audio signal:
			//rate = LFNoise1.kr.range(0.5, 2);
			pos = LFNoise2.kr(0.1).range(0, 1);
			//
			trigger = Impulse.ar(1, 0);
			env=Line.kr(0, 1, 1);
			soundDur=BufDur.kr(sndbuf)/rate;
			dur=TRand.kr(0.05, 0.5, trigger);
			pos=TRand.kr(dur, soundDur-dur, trigger);
			
			in=GrainBuf.ar(1, trigger, dur, sndbuf, rate, pos, interp, 0, envbufnum)*env;
			//
			azimuth = LFNoise1.kr(1).range(-pi, pi);//WhiteNoise.kr(0.6).range(-pi, pi); //-  in radians, -pi to pi
			elevation = 0; //-  in radians, -0.5pi to +0.5pi
			rho = MouseY.kr(0.0, 4.0); //- the speaker radius (1 places sound at the radius, <1 within the radius, >1 is outside the radius) beyond the radius, a distance scaling is applied (no filtering is done).
			//gain = 0.3; // - a control rate level input.
			//wComp = 1; // - chooses how the W channels is scaled. If 0, a scaler of 0.707 is used. If 1, W is varied according to the scaling of the X, Y and Z channels. 1 is the default.
			//
			#w, x, y, z = BFEncode1.ar(in, azimuth, elevation, rho, gain, wComp);
			//decode for 4 channels
			Out.ar(0, BFDecode1.ar1(w, x, y, z, speakersAzimuth, speakersElevation, speakerMaxDist, speakersDistances));
			FreeSelfWhenDone.kr(env);
		}); 
		^synthDef;
	}
}
