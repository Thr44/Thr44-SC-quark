Odef{
	classvar <>defaultServer, <>all, <>nodeWatcher;
	var <>envir, <>type, <>key, <>obj, <>objectType="", <>result, <>registerWatcher=false, <>instanceCopies, <>instanceParent, <>refObj, <>argAssociations, <>controlMethod=nil, <>controls;
	var <>network, <>nodeToRefObjMapping, <>tags;
	*initClass {
		all = ();
		//nodeWatcher=OdefWatcher.new(Server.default);
	}
	*clearAll{
		all.pairsDo {
			arg key, value;
			all.at(key).clear;


		}
	}
	*new {
		arg key, obj, registerWatcher=true;
		var currDef=all.at(key);
		if(currDef.isNil, {
			if(obj.isNil, { //only creates object if it contains an obj
				^nil;
			});
			//returns new Odef:
			^super.new.init(key, obj, registerWatcher);
		}, {
			if(obj.isNil.not, { //modifies obj of existing key
				currDef.valueObject(obj);
				//copies obj to its dependants:
				currDef.instanceCopies.do({
					arg item, i;
					Odef(item.key, currDef.obj);
					//["changing:", i, item].postln;
				});
			});
			//updates registerWatcher state:
			currDef.registerWatcher=registerWatcher;

			//returns current object with new obj assigned:
			^currDef;
		});

	}
	init{
		arg key, obj, registerWatcher=true;
		this.envir=();
		this.key=key;
		this.controls=[];
		this.instanceCopies=IdentityDictionary.new;
		this.argAssociations=IdentityDictionary.new;
		this.nodeToRefObjMapping=IdentityDictionary.new;
		this.tags=[];

		all.put(key, this);
		this.valueObject(obj);
		this.registerWatcher=registerWatcher;
		^this;
	}
	clone{
		arg name;
		^Odef(name.asSymbol, this.obj);
	}
	//returns a new instance of thrDef, but dependant of parent Odef.
	//if original Odef function object is altered, it is copied to its descendants:
	getInstance{
		arg name;
		var instance=this.clone(name);
		instance.instanceParent=this;
		this.instanceCopies.put(name, instance);
		^instance;
	}
	play{
		"play odef".postln;
		envir.use({
			var depFunc;
			if( objectType=="streamEvent", {
				result=envir.func.play();
				if(this.registerWatcher==true, {
						nodeToRefObjMapping.put(result, this.refObj);
						depFunc={
							arg obj, status;
							var id, transition;
							//["--", status, obj, nodeToRefObjMapping, nodeToRefObjMapping.at(obj)].postln;
							if(status=='stopped', {
								nodeToRefObjMapping.removeAt(obj);
								result.removeDependant(depFunc);
								obj.reset;
								this.changed(status);
							});

						};
						result.addDependant(depFunc);

				});
			}, {



				if(objectType=="soundFunction", {
					result=envir.func.play(args:this.args());
					if(this.registerWatcher==true, {
						nodeToRefObjMapping.put(result.nodeID, this.refObj);
						OdefWatcher.register(result,  this, true);
					});
				}, {
					if(objectType=="synthFunction", {
						if(this.isPlaying(), {
							"is Playing�".postln;
							this.stop();
						});
						"SYNTH FUNCTION".postln;
					envir.func.value;//play(args:this.args());
					result=envir.result;
					result.set(args:this.args());
					["synth result:", result].postln;
					if(this.registerWatcher==true, {
						nodeToRefObjMapping.put(result.nodeID, this.refObj);
						OdefWatcher.register(result,  this, true);
					});
				}, {
					^envir.func.value;
				});
				});



			});
		});
	}
	//this method validates if object is playing:
	isPlaying{
		if(((objectType=="soundFunction") ||(objectType=="synthFunction")) && result.isNil.not, {
			^result.isPlaying;
		});
		if( (objectType=="streamEvent") && (result.isNil.not), {
			^result.isPlaying;
		});
		//TODO: return for other types of objects
		^false;
	}
	stop{
		"stop odef".postln;
		if( (objectType=="synthFunction"), {
			if(this.isPlaying(), {
				["?????", result].postln;
				result.free;
				result=nil;
			});
		}, {
			if( (objectType=="soundFunction"), {
			if(this.isPlaying(), {
				["?????", result].postln;
				result.free;
				result=nil;
			});
		}, {
			["other?", objectType].postln;
			envir.func.stop;
		});
		});

	}
	clear{
		if(this.isPlaying(), {
			this.stop;
		});
		all.removeAt(this.key);
	}
	set{
		arg ... args;
		"odef set".postln;
		args.pairsDo {
			arg key, value;
			this.envir[key]=value;
			["SET:", key, value].postln;
		};
		if( ( ((objectType=="soundFunction") || (objectType=="synthFunction"))  && this.isPlaying()), {
				result.set(*args);
			});
	}
	args{
		var argNames=obj.def.argNames;
		var argsArray=Array.new();
		if(argNames.size()==0, {
			^nil;
		});
		for(0, argNames.size()-1, {
			arg i;
			var argSymbol;
			argSymbol=argNames[i];
			argsArray=argsArray.add(argSymbol);
			argsArray=argsArray.add(this.envir.at(argSymbol));
			i=i+1;
		});
		^argsArray;
	}
	argNames{
		^obj.def.argNames;
	}
	argValues{
		var argNames=obj.def.argNames;
		var argsArray=Array.new();
		for(0, argNames.size()-1, {
			arg i;
			var argSymbol;
			argSymbol=argNames[i];
			argsArray=argsArray.add(this.envir.at(argSymbol));
			i=i+1;
		});
		^argsArray;
	}
	addTag{
		arg tag;
		this.tags=this.tags.add(tag);
	}
	//
	interpretObject{
		var string=this.obj.asCompileString;
		var array, exp, cindex, argsElement, res, ns, synthRes;
		//"@interpretObject".postln;
		//general:
		array=[string];
		argsElement=string;

		//detect if it is a streamEvent:
		objectType="function";
		exp=string.findRegexp("wait");
		if(exp.size>0, {
			//objectType="streamEvent";
			exp=string.findRegexp(".do"); //1.do{} should also be detected!
			if(exp.size>0, {
				objectType="streamEvent";
			});
		});
		if( (objectType=="streamEvent").not, {
			//"detect if it is a sound function:".postln;
			exp=string.findRegexp("[.]ar[(]");
			//["EXP SIZE:", exp, exp.size].postln;
			if(exp.size>0, {
				objectType="soundFunction";

			});
			//detect if it is a Synth:

			exp=string.findRegexp("Synth");
			if(exp.size>0, {
				objectType="synthFunction";
			});
		});


		//applies transfotmation to function:

		//case normal function or stream or synth:
		if( ((objectType=="soundFunction").not) && ((objectType=="synthFunction").not)&& ((objectType=="function").not), {
			res="";

			//remove args from object:
			argsElement=argsElement.split(${);
			exp=argsElement[1].findRegexp("(arg.*.;)");
			if(exp.size>0, {
				res=exp[0][1];
			}, {
				exp=argsElement[1].findRegexp("(|.*.|)");
				if(exp.size>0, {
					res=exp[0][1];
				}, {
					res=exp;//mmc;
				});
			});

			argsElement[1]=argsElement[1].replace(res, "");
			array[0]=argsElement.join("{");
		});
		if( objectType=="synthFunction", {
			res="";
			//remove args from object:
			argsElement=argsElement.split(${);
			exp=argsElement[1].findRegexp("(arg.*.;)");
			if(exp.size>0, {
				res=exp[0][1];
			}, {
				exp=argsElement[1].findRegexp("(|.*.|)");
				if(exp.size>0, {
					res=exp[0][1];
				}, {
					res=exp;//mmc
				});
			});
			exp=res.findRegexp("(Synth.*.;)");
			if(exp.size>0, {
				res=exp[0][1];
			});
			"{currentEnvironment.result="++res++"}".postln;
			^"{currentEnvironment.result="++res++"}";
		});
		if( objectType=="function", {
			res="";
			//remove args from object:
			argsElement=argsElement.split(${);
			exp=argsElement[1].findRegexp("(arg.*.;)");
			if(exp.size>0, {
				argsElement=argsElement[1].split($;);
				res=argsElement[1];

			}, {
				exp=argsElement[1].findRegexp("(|.*.|)");
				if(exp.size>0, {
					argsElement=argsElement[1].split($|);
					res=argsElement[2];
					^"{"++res;
				}, {
					res=exp;//mmc
				});
			});
			^"{"++res++"}";
		});
		^array.join("");

	}
	copyArgsToEnvir{
		var funcString=obj.asCompileString, argArray, hasArgs=false, exp, argumentsArray;

		argumentsArray=funcString.split(${);
		//["ARGS?", argumentsArray].postln;
		if(argumentsArray.size>1, { //starts here
		//["using:", argumentsArray[1]].postln;
		argArray=argumentsArray[1];

		argArray=argumentsArray[1].split($|);
		if(argArray.size>2, {
			argArray[0]=argArray[0]++"|"++argArray[1]++"|";
			argArray[1]="";
			hasArgs=true;
		}, {
			argArray=argumentsArray[1].split($;);
			if(argArray.size>1, {

				exp=argArray[0].findRegexp("arg");
				if(exp.size>0, {

					argArray[0]=argArray[0]++";";
					argArray[1]="";
					hasArgs=true;
				});
			});

		});//ends here
		});
		//["hasArgs?", hasArgs].postln;
		if(hasArgs, {
			obj.def.argNames.do({
				arg val, i;
				argArray[1]=argArray[1]++"\ncurrentEnvironment."++val++"="++val++";";
			});
			funcString="{"++argArray[0]++argArray[1]++"}";
			//["RESULT:", funcString].postln;
			envir.use({funcString.interpret.value;});
			obj.def.argNames.do({
				arg val, i;
				if(envir[val.asSymbol].class == Association, {
					this.argAssociations.put(val, envir.at(val).value);
					envir[val.asSymbol]=envir.at(val).key;
				});
			});
		});
	}
	valueObject{
		arg obj;
		var newFunc;

		//depends on type of object:
		if(this.isPlaying(), {
			//this.stop;
		});

		this.obj=obj;
		this.type="func";

		//************Odef interpreter:

		//copies args to envir:
		this.copyArgsToEnvir();
		//interpret:
		newFunc=this.interpretObject();

		//replaces function args to envir vars:

		if((objectType!="soundFunction"), {
			//["�� not soundFunction"].postln;
			obj.def.argNames.do({
				arg val, i;
				//[i, val].postln;
				//["'''", newFunc].postln;
				//newFunc=newFunc.replace("\\"++val.asString;);

				newFunc=newFunc.replace(val.asString, "currentEnvironment."++val.asString);
				newFunc=newFunc.replace( "\\currentEnvironment.", "\\");
			});

		});
		//interprets new func:

		envir.newFunc=newFunc;

		envir.use({
			var nf;
			if(objectType=="streamEvent", {
				//["as stream"].postln;
				currentEnvironment.func=Task(currentEnvironment.newFunc.interpret);
				^nil;
			});
			if(objectType=="soundFunction", {
				nf="{"++currentEnvironment.newFunc++"}";
				currentEnvironment.func=nf.interpret;
				^nil;
			});
			currentEnvironment.func=currentEnvironment.newFunc.interpret;
		});
		//************
	}
	/*********************** NODEWATCHER *********************/
	dispatchGo{
		arg node;
		"node started playing".postln;
	}
	dispatchEnd{
		arg node;
		"node stopped".postln;
		nodeToRefObjMapping.removeAt(node.nodeID);
		this.changed('stopped');
	}

	/*********************** SCPAD *********************/
	addControl{
		arg ctype, params;
		controls=controls.add([ctype, params]);
	}
	setControlMethod{
		arg method;
		this.controlMethod=method;

	}
}


OdefWatcher : BasicNodeWatcher {

	classvar <>all;

	var <thrDefs;

	*initClass {
		all = IdentityDictionary.new();
		CmdPeriod.add(this);
	}

	*cmdPeriod { all.do { arg item; item.clear } }


	*newFrom { arg server;
		var res;
		res = all.at(server.name);
		if(res.isNil, {
			res = this.new(server);
			res.start;
			all.put(server.name, res)
		});
		^res
	}

	*register { arg node, thrDef, assumePlaying=false; //added thrDef
		var watcher;
		watcher = this.newFrom(node.server);
		watcher.register(node, thrDef, assumePlaying);
	}

	*unregister { arg node;
		var watcher;
		watcher = this.newFrom(node.server);
		watcher.unregister(node);
	} //should create unregisterOdef??


	cmds { ^#["/n_go", "/n_end", "/n_off", "/n_on"] }

	respond { arg method, msg;
		var node, group;
		node = nodes.at(msg.at(1));
		if(node.notNil, {
				group = nodes.at(msg.at(2));
				this.performList(method, node, group)
		})
	}

	clear {
		nodes.do({ arg node;
			node.isPlaying = false;
			node.isRunning = false;
			node.changed(\n_end);
		});
		nodes = IdentityDictionary.new;
		thrDefs = IdentityDictionary.new;
	}

	register { arg node, thrDef,  assumePlaying=false;
		//["register:", node, thrDef, assumePlaying].postln;
		if(server.serverRunning.not) { nodes.removeAll; thrDefs.removeAll; ^this };
		if(isWatching) {
			if(assumePlaying and: { nodes.at(node.nodeID).isNil }) { node.isPlaying = true };
			nodes.put(node.nodeID, node);
			thrDefs.put(node.nodeID, thrDef);
		};
	}

	unregister { arg node;
		nodes.removeAt(node.nodeID);
		thrDefs.removeAt(node.nodeID);
	}


	//////////////private implementation//////////////

	n_go {
		arg node;
		var thrDef;
		thrDef=thrDefs.at(node.nodeID);
		//"n_go".postln;

		node.isPlaying = true;
		node.isRunning = true;
		node.changed(\n_go);  // notify all the node's dependents of the change

		if(thrDef.isNil.not, {thrDef.dispatchGo(node)});
	}

	n_end {
		arg node;
		var thrDef;
		thrDef=thrDefs.at(node.nodeID);

		this.unregister(node);
		["STOPPED n_end!"].postln;
		node.isPlaying = false;
		node.isRunning = false;
		node.changed(\n_end);

		if(thrDef.isNil.not, {thrDef.dispatchEnd(node)});
	}

	n_off { arg node;
		["STOPPED n_off!"].postln;
		node.isRunning = false;
		node.changed(\n_off);
		//"n_off".postln;
	}

	n_on { arg node;
		node.isRunning = true;
		node.changed(\n_on);
		//"n_on".postln;
	}

}
