BufferList{
	var <>bufferList;
	var win, point, activeIdList, gList, <>hasGUI=false;
	classvar <>instance;
	*new{
		//singleton:
		if(BufferList.instance==nil, {
			BufferList.instance=super.new.init();
		});
		^BufferList.instance;
	}
	init{
		bufferList=List[];
	}
	getSize{
		^bufferList.size;
	}
	getBuffersWithKeyword{
		arg keyw;
		var results=List.new;
		bufferList.do({
			arg elem, i;
			var hasKey=false, keywords;
			keywords=elem[3];
			keywords.do({
				arg keyword, j;
				if(keyword==keyw, {
					hasKey=true;
					"has key".postln;
				});
			});
			if(hasKey==true, {
				results.add(bufferList[i]);
			});
		});
		^results;
	}
	getBuffersWithName{
		arg name;
		var results=List.new;
		bufferList.do({
			arg elem, i;
			var bname;
			bname=elem[2];
			if(bname==name, {
				results.add(bufferList[i]);
			});
		});
		^results;
	}
	addBuffer{
		arg name="new Buffer"+this.getSize(), buffer, keywords=[];
		bufferList=bufferList.add([buffer.bufnum, buffer, name, keywords]);
		["!!", bufferList].postln;
		if(hasGUI, {this.updateGUI()});
	}
	clearAll{
		bufferList=List[];
		if(hasGUI, {this.updateGUI()});
	}
	removeBufferAt{
		arg index=(bufferList.size - 1).max(0);
		bufferList.removeAt(index);
		this.updateGUI();
	}
	getSelectedBuffer{
		^bufferList[activeIdList][1];
	}
	getBufferAt{
		arg index=(bufferList.size - 1).max(0);
		["ind:", index].postln;
		bufferList[index].postln;
		bufferList[index][1].postln;
		^bufferList[index][1];
	}
	gui{
		var arr, name="BufferList";
		{
			if(point.isNil, point=Point.new(318, 581));
			win = GUI.window.new(name, Rect(point.x, point.y, 260, 275), resizable:false);
			//list of elements:
			gList = GUI.listView.new(win, Rect(10,15, 240, 240))
				//.background_(Color.new255(155, 205, 155, 60))
				.hiliteColor_(Color.new255(100, 100, 100)) //Color.new255(155, 205, 155)
				.selectedStringColor_(Color.black);
			gList.action = {
				arg n;
				activeIdList=n.value;
				["activeIdList:", activeIdList].postln;
			};
			hasGUI=true;
			win.front;
			win.onClose_({ hasGUI=false });

		}.defer;
		hasGUI=true;
		this.updateGUI();
	}
	updateGUI{
		var bufferListNames=[];
		if(hasGUI, {
			bufferList.size.do{
				arg i;
				bufferListNames=bufferListNames.add(bufferList[i][2]);
			};
		//bufferListNames=bufferListNames.add("create new Buffer...");
		//postln("bufferListNames:"+bufferListNames);
			{gList.items=bufferListNames}.defer;
		});
	}
}