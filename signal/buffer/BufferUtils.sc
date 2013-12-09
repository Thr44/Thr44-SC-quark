BufferUtils{
	*toMono{
		arg buffer;
		var r, monoBuf;
		r=Routine.new({
			if(buffer.numChannels>1, {
				buffer.loadToFloatArray(action: { 
					arg array;
					var fmArray = array; 
					monoBuf = Buffer.loadCollection(Server.default, fmArray.unlace(2).sum*0.5);
					^monoBuf;
				});
			}, {
				^buffer
			});
		}).value;
	}
	*writeToFile{
		arg buffer, name;
		var file="~/"++name.standardizePath, format="aiff", encoding="int16";
		buffer.write(file, format, encoding, -1, 0, false);
	}
	
	*playBuffer{
		arg out=Outputs.getOutAt(0), buffer=BufferList.new().getBuffer(), rate=1, loop=0, server=Server.default;
		var r, synth;
		Routine{
			BufferSynthDefs.loadPlayBuf();
			BufferSynthDefs.loadPlaybackBuf();
			server.sync;
		}.value;
		
		["WHAT REC?", buffer].postln;
		r=Routine{
			var p, f;
			//buffer.updateInfo();
			f=buffer.numFrames;
			if(rate<0, {p=buffer.numFrames}, {p=0});
			synth=Synth(\playBuffer, [\out, out, \index, buffer.bufnum, \rate, rate, \startPos, p, \loop, loop, \size, f]);
		}.play;
	}
	*previewBuffer{
		arg out=Outputs.getPlaybackOut(), buffer=BufferList.new().getBuffer(), rate=1, loop=0;
		var server=Server.default, r, synth;
		Routine{
			BufferSynthDefs.loadPlayBuf();
			BufferSynthDefs.loadPlaybackBuf();
			server.sync;
		}.value;
		r=Routine{
			var p, f;
			//buffer.updateInfo();
			f=buffer.numFrames;
			if(rate<0, {p=buffer.numFrames}, {p=0});
			synth=Synth(\playBuffer, [\out, out, \index, buffer.bufnum, \rate, rate, \startPos, p, \loop, loop]);
		}.play;
	}

}