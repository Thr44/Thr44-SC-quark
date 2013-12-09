BufferSynthDefs{
	//usage:
	/*
	BufferSynthDefs.loadRecBuf();
	~recSynth=Synth(\recBuffer, [\in, soundIn, \bufnum, bufnum]);
	
	*/
	*loadRecBuf{
		arg server = Server.default;
		var synthDef;
		synthDef = SynthDef(\recBuffer, {
			arg in=0, bufnum=0;
			RecordBuf.ar(In.ar(in), bufnum);
		});
		if(server==nil, {
			server=Server.default;
		});
		synthDef.send(server);
		//^synthDef;
	}
	//TODO: enable stereo OR mono
	*loadInputRecBuf{
		arg server = Server.default;
		var synthDef;
		synthDef = SynthDef(\recInputBuffer, {
			arg in=0, bufnum=0;
			RecordBuf.ar(SoundIn.ar(in, 1), bufnum);
		});
		if(server==nil, {
			server=Server.default;
		});
		synthDef.send(server);
		//^synthDef;
	}
	*loadInputRecBufStereo{
		arg server = Server.default;
		var synthDef;
		synthDef = SynthDef(\recInputBufferStereo, {
			arg bufnum=0;
			RecordBuf.ar(Mix.new([SoundIn.ar(0, 1), SoundIn.ar(1, 1)]), bufnum);
		});
		if(server==nil, {
			server=Server.default;
		});
		synthDef.send(server);
		//^synthDef;
	}
	/* TODO: */
	*loadPlayBuf{
		arg server = Server.default, output=Outputs.getPlaybackOut();
		var synthDef;
		synthDef = SynthDef(\playBuffer, {
			arg out=output, index=1003, rate=1, trigger=1, startPos=0, loop=0, gate=1, size;
			var dur=size/44100;
			var enve = Env.new([0, 0.1, 0.1, 0], [0.2*dur, 0.6*dur, 0.2*dur], 'sine');//var enve = Env.new([0, 0.1, 0.1, 0], [dur, 0.1, 1], 'linear', 2);
			//FreeSelfWhenDone.kr(Line.kr(1, 1, 10));
			Out.ar(out,
				Pan2.ar(PlayBuf.ar(1,index, BufRateScale.kr(index)*rate, trigger, startPos, 0)) * EnvGen.kr(enve, gate, doneAction:2)
			);
		});
		synthDef.send(server);
	}
	*loadPlaybackBuf{
		arg server = Server.default, output=Outputs.getPlaybackOut();
		var synthDef;
		synthDef = SynthDef(\playbackBuffer, {
			arg out=output, index=1003, rate=1, trigger=1, startPos=0, loop=0, gate=1;
			var dur=BufDur.kr(index);
			var enve = Env.new([0, 0.1, 0.1, 0], [1, dur, 1], 'welch', 2);
			Out.ar(out,
				PlayBuf.ar(1,index, BufRateScale.kr(index)*rate, trigger, startPos, 0) * EnvGen.kr(enve, gate, doneAction: 2)
			);
		});
		synthDef.send(server);
	}
}