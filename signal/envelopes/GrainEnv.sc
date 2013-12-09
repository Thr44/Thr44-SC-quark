GrainEnv{
	//@see Signal: usage of welch, hanning, hamming, etc..
	//TODO: more windows at: http://en.wikipedia.org/wiki/Window_function

	classvar <>blackmanEnv;
	classvar <>exponEnv;
	classvar <>expodecEnv;
	classvar <>rexpodecEnv;
	classvar <>rexpodec2Env;
	classvar <>gaussianEnv; 
	classvar <>sinEnv; 
	classvar <>welchEnv;
	classvar <>hanningEnv;
	classvar <>sinc8Env; // important for color
	classvar <>sinc16Env;
	classvar <>sinc32Env;
	classvar <>rectEnv;
	classvar <>pulseEnv;
	classvar grainEnvArray;
	classvar grainEnvNamesArray;
	
	classvar <>loaded=false;
	
	
	//
	*loadGrainEnvs{
		arg size=4096;
		if(GrainEnv.loaded == false, {
			GrainEnv.loadBlackman(size:size);
			GrainEnv.loadExpon(size:size);
			GrainEnv.loadExpodec(size:size);
			GrainEnv.loadGaussian(size:size);
			GrainEnv.loadSin(size:size);
			GrainEnv.loadRexpodec(size:size);
			GrainEnv.loadWelch(size:size);
			GrainEnv.loadHanning(size:size);
			GrainEnv.loadSinc8(size:size);
			GrainEnv.loadSinc16(size:size);
			GrainEnv.loadSinc32(size:size);
			GrainEnv.loadRect(size:size);
			GrainEnv.loadPulse(size:size);
			GrainEnv.loaded = true;
			grainEnvArray=[blackmanEnv, exponEnv, expodecEnv, rexpodecEnv, rexpodec2Env, gaussianEnv, sinEnv, welchEnv, hanningEnv, sinc8Env, sinc16Env, sinc32Env, rectEnv, pulseEnv];
			grainEnvNamesArray=["blackmanEnv", "exponEnv", "expodecEnv", "rexpodecEnv", rexpodec2Env, "gaussianEnv", "sinEnv", "welchEnv", "hanningEnv", "sinc8Env", "sinc16Env", "sinc32Env", "rectEnv", "pulseEnv"];
		});
		
		
	}
	*getGrainEnvArray{
		^grainEnvArray;
	}
	*getGrainEnvNameFromBuffer{
		arg buffer;
		grainEnvArray.size.do({
			arg i;
			if(buffer == grainEnvArray[i], {
				^("GrainEnv."++grainEnvNamesArray[i]);	
			});
		});	
		^nil;
	}
	*loadBlackman{
		arg size=4096;
		var sig, alpha, a0, a1, a2, s;
		s=Server.default;
		alpha = 0.9;
		~sig = Signal.newClear(size);
		//
		a0 = 0.5*(1-alpha);
		a1 = 0.5;
		a2 = alpha*0.5;
		//
		~sig.waveFill( { 
					arg x, i;
					a0 - (a1*cos( (2*pi*x)/(size-1) ) ) + (a2*cos( (4*pi*x)/(size-1) ) )
				}, 0, size );

		GrainEnv.blackmanEnv = Buffer.sendCollection(s, ~sig, 1, action: {
			|buf|
			GrainEnv.blackmanEnv=buf;
			"blackmanEnv created".postln;
		});
	}
	*loadExpon{
		arg size=4096;
		var env, s;
		s=Server.default;
		//
		env = Env([0, 1, 0], [0.5, 0.5], [8, -8, 8]);
		GrainEnv.exponEnv = Buffer.sendCollection(s, env.discretize(size), 1, action: {
			|buf|
			GrainEnv.exponEnv=buf;
			"exponEnv created".postln;
		});
	}
	*loadExpodec{
		arg size=4096;
		var env, s, buffer;
		s=Server.default;
		//
		env = Env.perc(0.1, 0.9, 1, -4);
		//env = Env([0, 1, 0.01], [0.05, 1],[0, 'exponential']);//Env([0, 1, 0.01], [0.01, 1],'exponential');
		buffer = Buffer.sendCollection(s, env.discretize(size), 1, action: {
			|buf|
			GrainEnv.expodecEnv=buf;
			"expodecEnv created".postln;
		});
	}
	*loadGaussian{
		arg size=4096;
		var array,sigma, mu, s;
		s=Server.default;
		sigma= 0.1; //this is the std dev, 99% of curve within 3 std dev

		mu=0.5;

		array=Array.fill(size,{
			arg i; 
			exp(((i/(size-1)-mu)**2).neg/(2*sigma*sigma))
		}) * (1/(sigma*(2pi**0.5)*4));
		//
		GrainEnv.gaussianEnv = Buffer.sendCollection(s, array, 1, 0, action: {
			|buf|
			GrainEnv.gaussianEnv=buf;
			"gaussianEnv created".postln;
		});
	}
	*loadSin{
		arg size=4096;
		var array, s;
		s=Server.default;
		array=Array.fill(size,{
			arg i; 
			(i/size*2*pi).sin;
		});
		//
		GrainEnv.sinEnv = Buffer.sendCollection(s, array, 1, 0, action: {
			|buf|
			GrainEnv.sinEnv=buf;
			"sinEnv created".postln;
		});
	}
	*loadRexpodec{
		arg size=4096;
		var env, s;
		s=Server.default;
		//
		env = Env.perc(1, 0.1, 0.90, 4);
		//env = Env([0.001, 1, 0.001], [0.99, 0.01],['exponential', 0]);//Env([0.001, 1], [1],'exponential');
		GrainEnv.rexpodecEnv = Buffer.sendCollection(s, env.discretize(size), 1, action: {
			|buf|
			GrainEnv.rexpodecEnv=buf;
			"rexpondecEnv created".postln;
		});
	}

	*loadWelch{
		arg size=4096;
		var sig, alpha, a0, a1, a2, s;
		s=Server.default;
		alpha = 0.9;
		sig = Signal.welchWindow(size);
		GrainEnv.welchEnv = Buffer.sendCollection(s, sig, 1, action: {
			|buf|
			GrainEnv.welchEnv=buf;
			"welchEnv created".postln;
		});
	}
	*loadHanning{
		arg size=4096;
		var sig, alpha, a0, a1, a2, s;
		s=Server.default;
		alpha = 0.9;
		~sig = Signal.hanningWindow(size);
		GrainEnv.hanningEnv = Buffer.sendCollection(s, ~sig, 1, action: {
			|buf|
			GrainEnv.hanningEnv=buf;
			"hanningEnv created".postln;
		});
		/*var env, s;
		s=Server.default;
		//
		env = Env.sine(1,1);
		GrainEnv.hanningEnv = Buffer.sendCollection(s, env.discretize, 1, action: {
			|buf|
			GrainEnv.hanningEnv=buf;
			"hanningEnv created".postln;
		});
		*/
	}
	*loadSinc8{
		arg size=4096;
		var sig, s;
		s=Server.default;
		sig = Signal.newClear(size);
		//
		sig.waveFill( { 
					arg x, i;
					x=x-(size*0.5);
					if(x==0, {
						1;	
					}, {
						( (8*2*pi*x)/(size) ).sin / ( (8*2*pi*x)/(size) )
					});
					
				}, 0, size );
				
		GrainEnv.sinc8Env = Buffer.sendCollection(s, sig, 1, action: {
			|buf|
			GrainEnv.sinc8Env=buf;
			"sinc8Env created".postln;
		});
	}
	*loadSinc16{
		arg size=4096;
		var sig, s;
		s=Server.default;
		sig = Signal.newClear(size);
		//
		sig.waveFill( { 
					arg x, i;
					x=x-(size*0.5);
					if(x==0, {
						1;	
					}, {
						( (16*2*pi*x)/(size) ).sin / ( (16*2*pi*x)/(size) )
					});
					
				}, 0, size );
				
		GrainEnv.sinc16Env = Buffer.sendCollection(s, sig, 1, action: {
			|buf|
			GrainEnv.sinc16Env=buf;
			"sinc16Env created".postln;
		});
	}
	*loadSinc32{
		arg size=4096;
		var sig, s;
		s=Server.default;
		sig = Signal.newClear(size);
		//
		sig.waveFill( { 
					arg x, i;
					x=x-(size*0.5);
					if(x==0, {
						1;	
					}, {
						( (32*2*pi*x)/(size) ).sin / ( (32*2*pi*x)/(size) )
					});
					
				}, 0, size );
				
		GrainEnv.sinc32Env = Buffer.sendCollection(s, sig, 1, action: {
			|buf|
			GrainEnv.sinc32Env=buf;
			"sinc32Env created".postln;
		});
	}
	*loadPulse{
		arg size=4096;
		var sig, s;
		s=Server.default;
		sig = Signal.newClear(size);
		//
		sig.waveFill( { 
					arg x, n, i;
					x=x+size;
					n=1;
					forBy(1, 400, 2, {
						|j| 
						n=n+ ((x*j/size*pi).sin / j);
					});
					1-n;
					
				}, 0, size );
		
		

		GrainEnv.pulseEnv = Buffer.sendCollection(s, sig, 1, action: {
			|buf|
			GrainEnv.pulseEnv=buf;
			"pulseEnv created".postln;
		});
	}

	*loadRect{
		arg size=4096;
		var env, s;
		s=Server.default;
		//
		env = Env.new([0, 1, 1, 0], [0, 1, 0],[-4, 0, 4]);
		//Env.new([0, 1, 1, 0], [0.05, 0.9, 0.05],[-4, 0, 4]);
		
		GrainEnv.rectEnv = Buffer.sendCollection(s, env.discretize(size), 1, action: {
			|buf|
			GrainEnv.rectEnv=buf;
			"rectEnv created".postln;
		});
	}

}
