Outputs{
	var <>outputsList;
	var <>playbackOut;
	classvar <>bus;
	classvar <>instance;
	*new { 
		if(Outputs.instance==nil, {
			Outputs.instance=super.new.init();
		}); //singleton;
		^Outputs.instance;
	}

	init { 
		outputsList=[];
	}
	*addSpeakerSystem{
		arg outs, angles;
		var outputs;
		outputs=Outputs.new();
		outputs.outputsList = outputs.outputsList.add([outs, angles]);
	}
	*test{
		thisProcess.interpreter.q.postln;
	}
	*getOutAt{
		arg index=0;
		var outputs;
		outputs=Outputs.new();
		^outputs.outputsList[index][0];
		
	}
	*getAnglesAt{
		arg index=0;
		var outputs;
		outputs=Outputs.new();
		^outputs.outputsList[index][1];
	}
	*getNumChannelsAt{
		arg index=0;
		var outputs;
		outputs=Outputs.new();
		^outputs.outputsList[index][0].size;
	}
	*setPlaybackOut{
		arg outs=0;
		var playbackOut, outputs;
		outputs=Outputs.new();
		outputs.playbackOut = outs;
	}
	*getPlaybackOut{
		var outs, outputs;
		outputs=Outputs.new();
		if(outputs.playbackOut==nil, {
			outs=0;
			^outs;
		}, {
			^outputs.playbackOut;
		});
		
	}
	*setStereo{
		var outputs;
		~mainBus=Bus.audio(numChannels:2);
		["mainBus:", ~mainBus.index].postln;
		outputs=Outputs.new();
		outputs.outputsList = [[[16, 17], [-0.5pi, 0.5pi]]];
	}
	*setStereoInternal{
		var outputs;
		outputs=Outputs.new();
		outputs.outputsList = [[[0, 1], [-0.5pi, 0.5pi]]];
	}
	*setQuad{
		var outputs;
		//~mainBus=Bus.audio(numChannels:4);
		//["mainBus:", ~mainBus.index].postln;
		outputs=Outputs.new();
		outputs.outputsList = [[[16, 17, 18, 19], [ -0.25pi, 0.25pi, -0.75pi, 0.75pi]]];
	}
	
	
}