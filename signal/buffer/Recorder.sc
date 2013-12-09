//Recorder CLASS:


/*
TODO:
set stereo for inputs record
fix buttons for inputs on GUI / create Inputs Class!
*/

Recorder {
	var >bufferList, startTime, endTime, recSynthList;
	var b, recSynth, <playSynth, s, length;
	var bufferSnapShotTask, snapshotActive=false, snapshotSize, <>bufS1, <>bufS2, <recS1, <recS2, startRecS1, startRecS2, timerS1, timerS2, enableSBuffer, startSTime, currSBuffer, bufferS, snapshotList, snapSSize=2, snapSMaxSize=2, snapSSizeText=2, snapSMaxSizeText=2, snapButtonEnable, snapMaxSlider, snapSizeSlider;
	//gui:

	var <>win, name="Buffer Container", point;
	var bufferListNames, <>activeIdList;
	var gList, recButton, delButton, playButton, input0Button, input1Button, input2Button, inputStereoButton, input20Button;
	var hasGUI=false;
	var <>currInput=0;
	
	classvar <>instance;
	//Constructor:
	*new{ 
		if(Recorder.instance==nil, {
			Recorder.instance=super.new.init();
			~recorder=Recorder.instance;
		}); //singleton;
		^Recorder.instance;
	}
	init{
		("Recorder by mcardoso@344server.org").postln;
		s=Server.default;
		bufferList=BufferList.new();
		recSynthList=[];
		activeIdList=0;
		//b=Buffer.alloc(s, 44100*30, 1, bufnum:1003);
		//snapshotList=[];
		//
		Routine{
			BufferSynthDefs.loadRecBuf();
			BufferSynthDefs.loadInputRecBuf(); 
			BufferSynthDefs.loadInputRecBufStereo();
			BufferSynthDefs.loadPlayBuf();
			BufferSynthDefs.loadPlaybackBuf();
			s.sync;
		}.value;
		^this;
	}
	startRec{
		arg in=8;
		var startTime, buffer;
		//resize recSynthList to add synth in correct slot:
		if(recSynthList.size<in, {
			while({recSynthList.size<=in}, {recSynthList=recSynthList.add(nil)});
		});
		if(recSynthList[in]!=nil, {
			("[!] RECORDER: already recording bus"+in++"!").postln;
			^nil;
		});
		//
		startTime=Main.elapsedTime; //find the timestamp
		buffer=Buffer.alloc(s, 44100*30);
		recSynth=Synth(\recBuffer, [\in, in, \bufnum, buffer.bufnum], Server.default, \addToTail);
		recSynthList[in]=[startTime, buffer, recSynth];
		/*if(hasGUI, {
			{recButton.value_(1)}.defer;
		});*/
	}
	stopRec{
		arg in=8,fName="Buffer "+(bufferList.getSize());
		var resultBuffer;
		if(recSynthList[in]==nil, {
			("[!] RECORDER: not recording this bus"+In++"!").postln;			^nil;
		});
		endTime=Main.elapsedTime;
		length=endTime-recSynthList[in][0];
		//free recorder:
		recSynthList[in][2].free;
		//copy data to final buffer:
		resultBuffer=Buffer.alloc(s, 44100*length, 1);
		recSynthList[in][1].copyData(resultBuffer, 0, 0, 44100*length);
		recSynthList[in][1].free;
		recSynthList[in]=nil;
		//
		bufferList.addBuffer(fName, resultBuffer);
		//
		/*
		if(activeIdList==bufferList.size, {
		
		this.updateGUI();
		
		if(hasGUI, {
			//gList.items=bufferListNames;
			{recButton.value_(0)}.defer;
			activeIdList = activeIdList + 1;
			gList.value_(activeIdList);
		}, {
			activeIdList = activeIdList + 1;
		});
		*/
		
	}
}