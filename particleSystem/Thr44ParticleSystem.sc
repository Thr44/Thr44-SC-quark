Thr44ParticleSystem{
	var <>forcesMatrix;
	var <>nNodes=0;
	var <>damp = 0.09;
	var k = 0.07;
	var w;
	//Constructor:
	*new{
		^super.new.init();
	}
	//constructor function
	init{
		forcesMatrix=[];
		^this;
	}
	//Private:
	createPhysicsNode{
		arg object;
		var pNode;
		if(nNodes>0, {
			for(0, this.nNodes-1, {
				arg i;
				if(forcesMatrix[i][i].object==object, {
					^forcesMatrix[i][i];	
				});
			});
		});
		pNode=Thr44Particle.new(nil, object);
		//[pNode].postln;
		//[object.physics].postln;
		object.physics=pNode;
		^pNode;
			
	}
	//private:
	addPhysicsNode{
			arg object;
			var nodePhysicsRequest, nodePhysics, index, node;
			if(object.class==Thr44Particle, {
				node=object;
			}, {
				node=this.createPhysicsNode(object);	
			});
			nodePhysicsRequest=this.requestNodePhysics(node);
			nodePhysics=nodePhysicsRequest[0];
				if(nodePhysics==nil, {
					index=nodePhysicsRequest[1];
					nodePhysics=node;//Particle344.new(index);//NodePhysics(node, index);
					nodePhysics.index=index;
					if(forcesMatrix.size<(index+1), {
						forcesMatrix=forcesMatrix.extend((index+1), []);
						forcesMatrix[index]=forcesMatrix[index].extend(index+1);
						//["FMI:", forcesMatrix[index]].postln;
					});
					if(forcesMatrix[index][index] == nil, {
						forcesMatrix[index]=forcesMatrix[index].extend((index+1), []);
						//("...3.1b"+forcesMatrix[index]).postln;
						//forcesMatrix[index][index]=nodePhysics;
						//forcesMatrix[index]=forcesMatrix[index].add(nodePhysics);
					});
					forcesMatrix[index][index]=nodePhysics;
					if((index+1)>nNodes, {
						nNodes=nNodes+1;
					});
					//traceForcesMatrix()
				});
				//("nodePhysics:"+nodePhysics).postln;
				^nodePhysics; 
		}
	//private:
	requestNodePhysics{
		arg node;
		var i, index, array;
		i=(nNodes - 1);
		index=nNodes;
		if(node == nil, {}, {
			if(i >= 0, {
				while ({ i >= 0 }, {
					if(forcesMatrix[i][i]==nil, {
						index=i;
					}, {
						if(node == forcesMatrix[i][i], {
							^[forcesMatrix[i][i]];
						});
					});
					i = i - 1;
				});
			});
		});
		
		/*
		if(forcesMatrix[i]==nil, {
			//check this:
			forcesMatrix=forcesMatrix.add([]);
			//forcesMatrix[i]=[];
		});
		*/
		array=[nil, index];
		^array;
	}
	
	//Forces assignment:
	addSpring{
		arg node0, node1, k, l;
		this.addForce(node0, node1, Thr44PSpring, k, l);
	}
	addRepulsor{
		arg node0, node1, k, l;
		this.addForce(node0, node1, Thr44PRepulsor, k, l);
	}
	addForce{
		arg ... args;
		var array, newArray, nodeP0, nodeP1, forceType;
		var pNode0, pNode1, force, aPhysics;
		array=args;

		nodeP0=array[0]; 
		nodeP1=array[1]; 
		forceType=array[2];
		newArray=array.copyRange(3, array.size()-1);
		/*TODO verificar o validador de existencia de particulas*/
		
		pNode0=this.addPhysicsNode(nodeP0);
		pNode1=this.addPhysicsNode(nodeP1);

		//pNode0=nodeP0;
		//pNode1=nodeP1;
		pNode0.x=rrand(0, 0.001);
		pNode0.y=rrand(0, 0.001);
		pNode1.x=rrand(0, 0.001);
		pNode1.y=rrand(0, 0.001);
		//
		//["indexes:", pNode0.index,pNode0.object, "---", pNode1.index, pNode1.object].postln;
		if(pNode0.index < pNode1.index, {
			aPhysics = [pNode1, pNode0];
			},{
				aPhysics=[pNode0, pNode1];
			});
		//["a:::", aPhysics].postln;
		//[aPhysics[0], aPhysics[1]].postln;
		//["f:::", forceType].postln;
		force = forceType.new(aPhysics[0], aPhysics[1], *newArray);
		//["ADDING:", force, nodeP0.id, nodeP1.id, "..", pNode0, pNode1].postln;
		if(this.forcesMatrix[aPhysics[0].index] == nil, {
			this.forcesMatrix[aPhysics[0].index]=[];
			this.forcesMatrix[aPhysics[0].index]=this.forcesMatrix[aPhysics[0].index].extend(aPhysics[0].index+1, nil);
			//"ok0".postln;

		});
		//"ok1".postln;
		this.forcesMatrix[aPhysics[0].index][aPhysics[1].index] = force;
		//"ok2".postln;
	}
	fillMatrixWithRepulsors{
		arg k, l;
		var i, j, force;
		i=0;
		while ({ i < nNodes }, {
			j = 0;//i + 1;
			
			while ({ j < i }, {
				//this.forcesMatrix[i]=this.forcesMatrix[i].extend(i, nil);//extending the array
				//["checking!! force at:", i, j, forcesMatrix[i][j]].postln;
				if(forcesMatrix[i][j]==nil, {
					//["needs force at:", i, j, forcesMatrix[i][j]].postln;
					//assign force!
					//["contains:", forcesMatrix[i][i], forcesMatrix[j][j]].postln;
					force=Thr44PRepulsor(this.forcesMatrix[i][i], this.forcesMatrix[j][j], k, l);
					
					/*[this.forcesMatrix[i], "!"].postln;
					[this.forcesMatrix[i][i].x, this.forcesMatrix[i][i].y, "!"].postln;
					[this.forcesMatrix[j][j].x, this.forcesMatrix[j][j].y, "!"].postln;
					*/
					this.forcesMatrix[i][j] = force;
				});
				j = j + 1;
			});
			i = i + 1; 
		});
		//"done fill!".postln;
	}
	fillMatrixWithForces{
		arg forceType, k, l;
		var i, j, force;
		i=0;
		while ({ i < nNodes }, {
			j = 0;//i + 1;
			
			while ({ j < i }, {
				//this.forcesMatrix[i]=this.forcesMatrix[i].extend(i, nil);//extending the array
				//["checking!! force at:", i, j, forcesMatrix[i][j]].postln;
				if(forcesMatrix[i][j]==nil, {
					//["needs force at:", i, j, forcesMatrix[i][j]].postln;
					//assign force!
					//["contains:", forcesMatrix[i][i], forcesMatrix[j][j]].postln;
					force=forceType(this.forcesMatrix[i][i], this.forcesMatrix[j][j], k, l);
					
					/*[this.forcesMatrix[i], "!"].postln;
					[this.forcesMatrix[i][i].x, this.forcesMatrix[i][i].y, "!"].postln;
					[this.forcesMatrix[j][j].x, this.forcesMatrix[j][j].y, "!"].postln;
					*/
					this.forcesMatrix[i][j] = force;
				});
				j = j + 1;
			});
			i = i + 1; 
		});
		//"done fill!".postln;
	}
	removeForceFromNodes{
		arg node0, node1;//):int {
		var i, pnode0, pnode1, pnode0req, pnode1req;
		pnode0req = this.requestNodePhysics.(node0);
		pnode0 = pnode0req[0];
		pnode1req = this.requestNodePhysics.(node1);
		pnode1=pnode1req[0];
		if(pnode0.index<pnode1.index, {
			forcesMatrix[pnode1.index][pnode0.index]=nil;
		}, {
			forcesMatrix[pnode0.index][pnode1.index]=nil;
		});
		^ -1;
	}
	calculateParticles{
		var i, j;
		//
		i = 0;
		while ({ i < nNodes }, {
			if(forcesMatrix[i][i]!=nil && forcesMatrix[i][i]!="", {
				forcesMatrix[i][i].ax = 0;
				forcesMatrix[i][i].ay = 0;
				forcesMatrix[i][i].az = 0;
			}, {
				"there's nills".postln;	
			});
			i = i + 1; 
		});
		//
		i=0;
		while ({ i < nNodes }, {
			j = 0;
			while ({ j < i }, {
				if(forcesMatrix[i][j].class.superclass==Thr44PForce, {
					forcesMatrix[i][j].calculate();
				});
				j = j + 1;
			});
			i = i + 1; 
		});
		//
		i = 0;
		while ({ i < nNodes }, {
			if(forcesMatrix[i][i]!=nil && forcesMatrix[i][i]!="", {
				//("node ASSIGN:"+forcesMatrix[i][i]).postln;	
				this.assignPositions(forcesMatrix[i][i]);
			});
			i = i + 1; 
		});	
	}
	//
	assignPositions{
		arg node;
		//("node to assignPos:"+node).postln;
		node.vx = node.vx + node.ax;
		node.vy = node.vy + node.ay;
		node.vz = node.vz + node.az;
		node.vx = node.vx * this.damp;
		node.vy = node.vy * this.damp;
		//node.vz = node.vz * this.damp;
		//
		node.x = node.x - node.vx;
		node.y = node.y - node.vy;
		//[node.x, node.y].postln;
		//node.z = node.z - node.vz;
		//
		//[node.x, node.y, node.z].postln;
			
	}
	//Rect(forcesMatrix[i][i].x*400, forcesMatrix[i][i].y*400, 8, 8)
	gui{
		var run=true;
		if(w==nil, {
			w = Window.new.front;
			w.dump;
			w.view.background_(Color.white);
			w.drawHook = {
				nNodes.do{
					arg i; 
					// set the Color
					Pen.color = Color.black;
					Pen.addRect(Rect(forcesMatrix[i][i].x+200, forcesMatrix[i][i].y+200, 8, 8));
					Pen.perform(\fill);
				};
			};
		});
		w.refresh;
		//	("started").postln;
		{ while { run } { 
			this.calculateParticles();
			w.refresh; 
			0.04.wait } }.fork(AppClock);
	}
}