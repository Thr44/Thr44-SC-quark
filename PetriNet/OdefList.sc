OdefList : List{
	classvar <>all;
	var <>key, <>list, window, listview, textbox;
	*initClass { 
		all = ();
	}
	
	*new{
		arg key, array;
		var currList=all.at(key);
		if(array.isNil, {^currList});
		^super.newClear(0).init(key, array);
	}
	init{
		arg key, array;
		
		all.put(key, this);
		
		array.do({
			arg val;
			this.add(val);
		});
		^this;
	}
	
	add{
		arg odef;
		var i=0;
		if(odef.class==String || odef.class==Symbol, {
			odef=Odef(odef.asSymbol);		
		});
		if(odef.class!=Odef, {^this}); 
		while({i < this.size}, {
			if(this[i]==odef, {
				["Odef exists", odef].postln;
				^false;
			});
			i=i+1;
		});
		super.add(odef);
	}
	remove{
		arg odef;
		var i=0;
		if(odef.class==String || odef.class==Symbol, {
			odef=Odef(odef.asSymbol);		
		});
		if(odef.class!=Odef, {^this}); 
		while({i < this.size}, {
			if(this[i]==odef, {
				super.removeAt(i);
				^this;
			});
			i=i+1;
		});
	}	
	*write{
		arg path;	
	}
	read{
		arg path;
	}
	browse{
		var nameList=[];
		this.do({
			arg odef;
			nameList=nameList.add(odef.key.asString);	
		});
		
		window = Window.new("OdefList").front;
		
		listview = ListView(window,Rect(10,10,200,200))
			.items_(nameList)
			.background_(Color.clear)
			.hiliteColor_(Color.green(alpha:0.6))
			.action_({ arg sbs;
				[sbs.value, listview.items[sbs.value]].postln;
			});

		textbox = SCTextField.new(window,Rect(240, 10, 130, 20))
			.action_{|v| nameList.add(v.value); listview.items=nameList.array};


	}
}