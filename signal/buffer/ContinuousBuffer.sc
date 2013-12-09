/*
TODO:
enable recording of internal buffers!

*/

ContinuousBuffer {
	classvar <>nextBuffer;
	var s;
	var bufferSnapShotTask, snapshotActive=false, snapshotSize, <>bufS1, <>bufS2, <recS1, <recS2, startRecS1, startRecS2, timerS1, timerS2, enableSBuffer, startSTime, currSBuffer, bufferS, snapshotList, snapSSize=2, snapSMaxSize=2, snapSSizeText=2, snapSMaxSizeText=2, snapButtonEnable, snapMaxSlider, snapSizeSlider, bufferID1, bufferID2;
	var <>currInput=0;
	//Constructor:
	*new{ 
		^super.new.init();
	}
	
	init{
		("ContinuousBuffer by mcardoso@344server.org").postln;
		s=Server.default;
		snapshotList=[];
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
	
	startSnapshotBuffer{	
		arg size=4, useInternalBus=false;
		var secondsSize, r, recSynthName, halfSize;
		("ContinuousBuffer startSnapshotBuffer").postln;
		/*if(hasGUI, {
			snapButtonEnable.value_(1);
		});*/
		r=Routine{
		size=size.floor;
		size=size+(size%2);
		halfSize=size/2;
		
		secondsSize = max( (size+1), 5);
		["snap:", snapshotActive].postln;
		if(snapshotActive, {
			this.stopSnapshotBuffer();
		});
		snapshotActive=true;
		snapshotSize=size*GlobalVars.audioRate;
		if(useInternalBus, {
			recSynthName=\recInputBuffer;
		}, {
			recSynthName=\recBuffer;
		});
		
		//create buffers:
		//bufferID1=ContinuousBuffer.nextBuffer;
		//ContinuousBuffer.nextBuffer=ContinuousBuffer.nextBuffer+1;
		//bufferID2=ContinuousBuffer.nextBuffer;
		//ContinuousBuffer.nextBuffer=ContinuousBuffer.nextBuffer+1;
		bufS1=Buffer.alloc(s,44100 * secondsSize ,1);//, bufnum:bufferID1);
		bufferID1=bufS1.bufnum;
		bufS2=Buffer.alloc(s,44100 * secondsSize ,1);//, bufnum:bufferID2);
		bufferID2=bufS2.bufnum;
		("starting Snapshot Buffer with size"+snapshotSize).postln;
		(["internal buffers used:", bufferID1, bufferID2]).postln;
		//task:
		s.sync;
		bufferSnapShotTask=Task{
			inf.do{
				arg i;
				//("i:"+i).postln;
				var j,startRec1, startRec2, enableBuffer;
				if(i==0, {
					recS1=Synth(recSynthName, [\in, this.currInput, \bufnum, bufferID1], s, \addToTail);
					timerS1=Main.elapsedTime;
					//enabled snapshot to work from beginning, with max possible size!
					currSBuffer=bufS1;
					enableSBuffer=true;
					startSTime=timerS1;
				});
				if(i==halfSize, {
					recS2=Synth(recSynthName, [\in, this.currInput, \bufnum, bufferID2], s, \addToTail);
					timerS2=Main.elapsedTime;
					enableSBuffer=true;
					currSBuffer=bufS1;
					startSTime=timerS1;
				});
				if(i>(size-1), {
					j=(i%size);
					//("j:"+j).postln;
					//j.postln;
					if(j==0, {
						recS1.free;
						bufS1.free;
						s.sync;
						//bufS1=Buffer.alloc(s,44100 * size ,1, bufnum:bufferID1); //fixed to dynamic buffer ids
						bufS1=Buffer.alloc(s,44100 * secondsSize ,1);//, bufnum:bufferID1);
						bufferID1=bufS1.bufnum;
						//recS1.postln;
						s.sync;
						//
						recS1=Synth(recSynthName, [\in, this.currInput, \bufnum, bufferID1], s, \addToTail);
						//recS1.postln;
						timerS1=Main.elapsedTime;
						currSBuffer=bufS2;
						startSTime=timerS2;
						
					});
					if(j==halfSize, {
						recS2.free;
						bufS2.free;
						s.sync;
						//bufS2=Buffer.alloc(s,44100 * 5 ,1, bufnum:bufferID2); //fixed to dynamic buffer ids
						bufS2=Buffer.alloc(s,44100 * secondsSize ,1);//, bufnum:bufferID1);
						bufferID2=bufS2.bufnum;
						s.sync;
						recS2=Synth(recSynthName, [\in, this.currInput, \bufnum, bufferID2], s, \addToTail);
						timerS2=Main.elapsedTime;
						currSBuffer=bufS1;
						startSTime=timerS1;

					});
				});
				1.wait;
			}
		};
		bufferSnapShotTask.play;
		}.value;
		//
		
	}
	setCurrentInput{
		arg input=8;
		["input:", input].postln;
		this.currInput=input;
		if(snapshotActive, {
			this.stopSnapshotBuffer();
			this.startSnapshotBuffer();
		});
		
		
	}
	stopSnapshotBuffer{
		/*if(hasGUI, {
			snapButtonEnable.value_(0);
		});
		*/
		bufferSnapShotTask.stop;
		bufferSnapShotTask.reset;
		recS1.free;
		recS2.free;
		bufferID1.free;
		bufferID2.free;
		snapshotActive=false;
	}
	getSnapshot{
		arg size=snapSSize;
		var endSTime, length, sampleSize, sourceStartAt;
		if(enableSBuffer, {
			endSTime=Main.elapsedTime;
			length=endSTime - startSTime;
			sampleSize=min(size, length);
			bufferS=Buffer.alloc(s, 44100*sampleSize, 1);//new buffer to the list
			//("created buffer "+bufferS.bufnum).postln;
			bufferS.zero;
			sourceStartAt = length - sampleSize;
			currSBuffer.copyData(bufferS, 0, 44100*sourceStartAt, 44100*sampleSize);
			/* manage from outside!
				snapshotList = snapshotList.add(bufferS);
			*/
			//TODO: decide how to delete buffers:
			/*
			if(snapshotList.size>50, {
				snapshotList[0].free;
				snapshotList.removeAt(0);
			});
			*/
			//("snapshotList:"+snapshotList).postln;
			/*if(bufferS==nil, {}, {
				bufferS.free;
			});
			*/
		});
		//
		//s.sync; //to test : does not work without routine...
		^bufferS;
	}
}