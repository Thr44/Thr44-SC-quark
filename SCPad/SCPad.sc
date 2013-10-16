SCPad{
	var <>outgoingRoutine, <>incomingResponder, <>isPlaying=false, <>rate=1, <>prevState, <>iPadNetAddress, <>scNetAddr;
	/*
		isPlaying - alert if SCPad cycle is running
		rate - update iPad refresh rate
		iPadNetAddress - iPad's OSC address
	*/
	
	//TODO: implement singleton!
	
	*new { 
		arg ip="127.0.0.1", port=12345;
		^super.new.init(ip, port);
	}
	*loadSpecs{
		//required specs:
		ControlSpec.specs[\rate] = ControlSpec(-2, 2, \linear, 0.01, 1, units: "Hz");
		ControlSpec.specs[\density] = ControlSpec(0.2, 20, \exp, 0.01, 1, units: "Hz");
		ControlSpec.specs[\gain] = ControlSpec(0.0, 1.0, \amp, 0.001, 1, units: "");
		ControlSpec.specs[\pan] = ControlSpec(-1, 1, \linear, 0.001, 1, units: "");
		ControlSpec.specs[\azimuth] = ControlSpec(-pi, pi, \linear, 0.001, 1, units: "");
		ControlSpec.specs[\rho] = ControlSpec(0, 8, \linear, 0.001, 1, units: "");
		ControlSpec.specs[\grainSize] = ControlSpec(0.05, 1, \linear, 0.01, 1, units: "ms");	
	}
	*defaultControls{
		var controls=[];
		controls=controls.add(["Slider", [\gain]]);
		//controls=controls.add(["circularSelectSlider", [\bufnum]]);
		controls=controls.add(["circularSelectSlider", [\sndbuf]]);		controls=controls.add(["circularRange", [\grainSize]]);
		controls=controls.add(["scatterXY", [\rate, \density, \play]]);
		controls=controls.add(["scatterRadial", [\azimuth, \rho, \spatialCtrMode]]);
		controls=controls.add(["scatterRadial", [\azimuth, \rho]]);
		controls=controls.add(["scatterRadial", [\pan, \spatialCtrMode]]);
		controls=controls.add(["scatterRadial", [\pan]]);
		controls=controls.add(["Play", [\play]]);
		^controls;
	}
	init{
		arg ip="127.0.0.1", port=12345;
		SCPad.loadSpecs();
		this.iPadNetAddress=NetAddr.new(ip, port);
		this.outgoingRoutine=Task({
			inf.do({
				this.updateAll();
				(1/rate).wait;
			});	
		});
		//responder listening to local server at IP 57120. defined by default as nil:
		this.incomingResponder=OSCresponderNode.new(nil, \scPad, { 
			arg time, resp, msg, str; 
			var def, dict;
			if(msg.isNil, {}, {
				//msg.postln;
				//~msg=msg;
				def=Odef(msg[1].asSymbol);
		
				if(msg[2]==\play, {
			
					if(msg[3]==1, {
						["playing:", msg[2], msg[3]].postln;
						def.play;
					},{
						["stopping:", msg[2], msg[3]].postln;
						def.stop;
					});
				},{
					//~json=msg[2];
					//["1:", msg[2].class].postln;
					//["2:", String(msg[2])].postln;	
					dict=msg[2].asString.jsonToDict;
					//dict.postln;
					//~json=dict;
					dict.keys.do({
						arg item;
						var data, res, spec;
						spec=item.asSymbol.asSpec;
						data = dict.at(item);
						res = data;
						//converts association ref to data:
						if(def.argAssociations[item.asSymbol].isNil.not, {
							data=def.argAssociations[item.asSymbol];
							//["C:", data.class, data.class==Array].postln;
							if(data.class==Array, {
								res=[];
								data.do({
									arg val, i;
									if(dict.at(item)[i]==1, {
										res=res.add(val);
									});
								});
								//["DATA_", res].postln;
							});
														//res=data;//.wchoose(dict.at(item)[0]);
							//def.set(item, item.asSymbol.asSpec.map(dict.at(item)[0]));
							
						});
						
						//if spec, maps data:
						if(spec.isNil, {
							def.set(item, res);
						}, {
							if(res.class==Array, {
								res.do({
									arg resVal, i;
									res[i]=item.asSymbol.asSpec.map(res[i]);
								});
							}, {
								res= item.asSymbol.asSpec.map(res);
							});
						});
						
						//res= item.asSymbol.asSpec.map(dict.at(item)[0]);
						
						//["RES:"; res].postln;
						def.set(item, res);//item.asSymbol.asSpec.map(dict.at(item)[0]));
						
					});
				});
			});
		});
		this.play();
		^this;
		
	}
	play{
		if(this.isPlaying.not, {
			
			this.outgoingRoutine.play;
			this.incomingResponder.add;
			this.isPlaying=true;
		});
	}
	stop{
		if(this.isPlaying, {
			this.outgoingRoutine.stop;
			//this.outgoingRoutine.reset;
			this.incomingResponder.remove;
			this.isPlaying=false;
		});
	}
	
	updateAll{
		var currState, i, debug;
		if( rrand(0.0, 1.0)>0.5, {
			debug=true;
		});
		if(this.prevState.isNil, {
			this.prevState=IdentityDictionary();//this.createState();
			//should create elements here
			//"created prevState".postln;
			^nil;		
		});

		//Odef.all.postln;
		//if firstTime running:
		
		/*
		if(this.prevState.isNil, {
			this.prevState=[];//this.createState();
			//should create elements here
			^nil;		
		});
		*/
		currState=this.createState();
		if(debug==true,{ 
			//["III:", currState.size].postln;
			//[this.prevState.asCompileString(), currState.asCompileString()].postln;
		});
		if(this.prevState.asCompileString()!=currState.asCompileString, {
			//check difs for objects to be created or modified:
			currState.do({
				
			});
			if(debug==true,{ 
				//[currState.size, "---", this.prevState.size].postln;
			});
						//remove from iPad check:

			if(currState.size!=this.prevState.size, {
				prevState.pairsDo({
					arg key, value;
					if(currState.at(key).isNil, {
						this.removeDefWithKey(key);	
					});
					//["prevstate", key, value].postln;
				});
				currState.pairsDo({
					arg key, value;
					if(prevState.at(key).isNil, {
						this.addDef(Odef(key) );
					});
					//["currState", key, value].postln;
				});
				/*i=0;
				while({currState.size<this.prevState.size}, {
					if( currState[i]==nil, {
						//alert iPad to remove i element;
						["remove:", this.prevState[i][0]].postln;
						this.removeDefWithKey(this.prevState[i][0].asSymbol);
						this.prevState=this.prevState.removeAt(i);
					}, {
						if(currState[i]!=nil && currState[i][0]!=this.prevState[i][0], {
							//alert iPad to remove i element;
							["remove2:", this.prevState[i][0]].postln;
							this.removeDefWithKey(this.prevState[i][0]);
							this.prevState=this.prevState.removeAt(i);
						
						},{
							i=i+1;
						});
					});
				});*/
			});

			//add to iPad check:
			/*
			if(currState.size>this.prevState.size, {
				i=(this.prevState.size-1).max(0);
				while({currState.size>this.prevState.size}, {
					if( this.prevState[i]==nil, {
						//alert iPad to add i element;
						["add:", currState[i][0]].postln;
						this.addDef(Odef( currState[i][0].asSymbol) );
						this.prevState=this.prevState.add(currState[i]);
					}, {
						if(this.prevState[i]!=nil && currState[i][0]!=this.prevState[i][0], {
							//alert iPad to add i element;
							["add2:", currState[i][0]].postln;
							currState=currState.add(this.prevState[i]);
						},{
							i=i+1;
						});
					});
				});
			});
			*/
			//TODO: update params
			
			this.prevState=currState;
		});
	}
	
	assignControls{
		arg oDef;
		var argNames, controls, controlArgs, defaultControls, filterArgs, filteredArgs;

		argNames=oDef.argNames.copy; //a
		controls=oDef.controls; //b


		filterArgs=["play".asSymbol]; //h

		defaultControls=SCPad.defaultControls().copy;

		controlArgs=[]; //d
		controls.do({
			arg val;
			controlArgs=controlArgs++val[1].copy;
			//controlArgs.postln;
		});
		//remove used args:
		controlArgs.do({
			arg elem;
			var index;
			index=argNames.indexOf(elem);
			if(index!=nil, {
				argNames.removeAt(index);
				//argNames.postln;
			});
		});
	
		//CREATE TAGS LIST
	
	
		filterArgs=filterArgs++controlArgs;
	
		filteredArgs=[]; //g
		defaultControls.do({
			arg val;
			var elems=val[1].copy;
			//["--", elems].postln;
			if(elems.size()>0, {
				//elems.postln;
				filteredArgs=filteredArgs.add(elems.copy);
			});
		});
	
		//CLEAN PLAY and USED
		filteredArgs.do({
			arg val, i;
			filterArgs.do({
				arg filtered, j;
				var index;
				if(val!=nil, {
					index=val.indexOf(filtered);
					//index.postln;
					if(index!=nil, {val.removeAt(index)});
					if(val.size()==0, {filteredArgs[i]=nil});
					//["-->", val].postln;
				});
			});
		});
	
		filteredArgs.do({
			arg val, i;
			var  hasAll, argsDeleteList;
			hasAll=false;
			if(val!=nil, {hasAll=true});
			//[":::", val, hasAll].postln;
			val.do({
				arg elem;
				var elemIndex;
				elemIndex=argNames.indexOf(elem);
				if(elemIndex==nil, {
					hasAll=false;	
				});
			});
			if(hasAll==true, {
				
				["should add Control:", defaultControls[i]].postln;
				oDef.addControl(*defaultControls[i].copy);
				argsDeleteList=filteredArgs[i];
				argsDeleteList.do({
					arg argElem, k;
					var argIndex=argNames.indexOf(argElem);
					if(argIndex!=nil, {
						argNames.removeAt(argIndex);
					});	
				});

				filteredArgs[i]=nil;
			});
		});	
	}
	addDef{
		arg def; //a Odef
		var data, controlMethod, ch0, ch1, ch2, controls, availableArgs, hasPlayControl;
		controlMethod=def.controlMethod;
		
		//attempt to set control method:
		if(controlMethod.isNil, {
			ch0=def.envir.newFunc.findRegexp("(.do)"); //check .do
			ch1=def.envir.newFunc.findRegexp("(doneAction:2)"); //check inf.do
			ch2=def.envir.newFunc.findRegexp("(inf.do)"); //check inf.do

			controlMethod=\select;
			if(ch2.size>0, {
				controlMethod=\select;
			}, {
				if( ((ch0.size==0) && (ch1.size>0)), {
					controlMethod=\trig;
				}, {
					if(ch0.size>0, {
						controlMethod=\trig;
					});
				});
			});
			
		});
		//attempt to set controls:
		this.assignControls(def);
		
		
		// adds play control::
		hasPlayControl=false;
		if(controlMethod==\select, {
			def.controls.do({
				arg control;
				if(control[0]=="Play", {hasPlayControl=true});
			});
			if(hasPlayControl==false, {
				def.addControl("Play", [\play]);
			});
		});
		
		//["set control method to:", controlMethod].postln;
		data="<xml>
<soundObject name='"++def.key.asString++"' action='add' cType='"++controlMethod.asString++"' status='"++def.isPlaying()++"'>
<controls>\n";
		
		
		def.controls.do({
			arg val, i;
			var controlData, j, argData, argName, aSpec;
			//[">>>", val].postln;
			controlData="<control type='"++val[0]++"'>\n";
			j=0;
			//["size:", val[1], val[1].size].postln;
			while ({ j < val[1].size }, {
				argName=val[1][j];
				controlData=controlData++"\n<param name='"++argName++"'>";
				//"-----".postln;
				//["def:", def].postln;
				//["name:", argName].postln;
	
				//["arg:", def.envir[argName.asSymbol]].postln;
				//["ass:", def.argAssociations[argName.asSymbol]].postln;
				if(def.argAssociations[argName.asSymbol].isNil, {
					//"normal:".postln; 
					argData=def.envir[argName.asSymbol];
				}, {
					//"association!".postln;
					argData=def.argAssociations[argName.asSymbol].size;
					
				});
				//["ARG DATA:", argData, argData.class].postln;
				//fix this:
				aSpec=argName.asSymbol.asSpec;
				if(aSpec!=nil, {
					if(argData.class==Array, {
						argData.do({
							arg val, i;
							argData[i]=aSpec.unmap(val);
								
						});
					}, {
						argData=aSpec.unmap(argData);
					});
				});
				//["ARG DATA:", argData].postln;
				if(argData.class==Array, {
					argData.do({
						arg val, i;
						controlData=controlData++"\n<value>"++val++"</value>";
					});
				}, {
					controlData=controlData++"\n<value>"++argData++"</value>";
				});
				controlData=controlData++"\n</param>";
				j = j + 1; 
			});
			/*
			while ({ j < val[1].size }, {
				controlData=controlData++"\n<param name='"++val[1][j]++"'>";
				val[1][j+1].size.do({
					arg paramVal, k;
					controlData=controlData++"\n<value>"++paramVal++"</value>";
				});
				controlData=controlData++"\n</param>";
				j = j + 2; 
			});
			*/
			controlData=controlData++"\n</control>";
			data=data+controlData;
		});
					/*
					<control name='gain' type='circularSlider'>
						<value>0.2</value>
					</control>
					<control name='density_other' type='scatterXY'>
						<value>0</value>
						<value>1</value>
					</control>
					<control name='spacial' type='scatterRadial'>
						<value>0</value>
						<value>1</value>
					</control>
					<control name='grain' type='circularSelectSlider'>
						<value>5</value>
					</control>
					*/
		data=data+"</controls>\n</soundObject>\n</xml>";
		["sending to iPad:", data].postln;
		this.iPadNetAddress.sendMsg("/soundObjects", data);
		
	}
	removeDef{
		arg def; //a Odef
		this.removeDefWithKey(def.key.asString);

	}
	removeAllDefs{
		Odef.all.do({
			arg val, i;
			this.removeDef(val);	
		});	
	}
	removeDefWithKey{
		arg key; //a Odef name
		var data;
		//["called remove", key].postln;
		data="<xml><soundObject name='"++key++"' action='remove' cType='select' /></xml>";
		["send remove:", data].postln;
		this.iPadNetAddress.sendMsg("/soundObjects", data);
	}

	createState{
		var state = IdentityDictionary();
		Odef.all.do({
			arg val, i;
			if(val.tags.includes("petri").not, {
				state.put(val.key.copy, val.envir.copy);
			});
		});
		^state;
	}
	/*
	test{
		Odef.all.postln;
		
		~oscSend=OSC.getNetAddress;
		~oscSend.sendMsg("/soundObjects", ~paramMsg.());
		~paramMsg={"<xml>
			<soundObject name='" ++ ~name ++ "' action='add' cType='select'>
				<params>
					<control name='density' type='circularRange'>
						<value>0</value>
						<value>1</value>
					</control>
					<control name='grain' type='circularRange'>
						<value>0</value>
						<value>1</value>
					</control>
					<control name='volume' type='circularRange'>
						<value>0.1</value>
					</control>
				</params>
			</soundObject>
		</xml>"};
	}
	*/
}