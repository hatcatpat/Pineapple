//========================================================================
Pineapple {

	var num_lines = 16;
	var lpb = 4;
	var <>proxyspace, <>samples, <>soloed, <bpm, <>lim;

	*new {
		^super.new.init
	}

	init {
		arg folder = "all";
		var cmd_period_func;

		proxyspace = ProxySpace.push(Server.default, "pineapple");
		this.bpm_(120);
		soloed = [];
		this.initClock;
		this.initSamples(folder);

		SynthDef(\lim, {
			arg in=0, out=0, lim=((-6).dbamp), dur=0.01;
			ReplaceOut.ar(out, Limiter.ar(In.ar(in,2), lim, 0.01))
		}).add;

		cmd_period_func = {
			lim = Synth.after(Server.default.defaultGroup, \lim, [\lim,(-6).dbamp]);
		};

		Server.default.doWhenBooted({ cmd_period_func.value; });

		CmdPeriod.add({ {cmd_period_func.value}.defer(0.01) });
	}

	bpm_ {|b|
		proxyspace[\tempo] =  { (b / 60) };
		bpm = b;
		("set bpm:" ++ bpm).postln;
	}

	initClock {
		proxyspace[\t] = { Impulse.kr(proxyspace[\tempo]) };
		proxyspace[\c_reset] = { 0 };
		proxyspace[\c] = { PulseCount.kr(proxyspace[\t], proxyspace[\c_reset]) };
		31.do({|i|
			var ii = i + 2;
			proxyspace[("t"++ii).asSymbol] = { PulseDivider.kr(proxyspace[\t],ii) };
		});
		("clock initialized").postln;
	}

	initSamples {|samples_folder|
		samples_folder = "/home/pat/Music/Samples/" ++ samples_folder;
		samples = Dictionary.new;
		PathName(samples_folder).entries.do({|f|
			if(f.isFolder){
				var d = Dictionary.new;
				("found folder:" ++ f.folderName).postln;
				f.entries.do({|ff,i|
					if(ff.extension == "wav"){
						d.add(ff.fileNameWithoutExtension.asSymbol -> Buffer.read(Server.default, ff.fullPath));
						(
							" i:" ++ i
							++ " f:" ++ ff.fileNameWithoutExtension
						).postln;
					}
				});
				samples.add(f.folderName.asSymbol -> d);
			}{
				if(f.extension == "wav"){
					samples.add(f.fileNameWithoutExtension.asSymbol -> [
						Buffer.read(Server.default, f.fullPath)
					]);
					("loaded file:" ++ f.fileNameWithoutExtension).postln;
				}
			}
		});

		"samples loaded".postln;
	}

	gui { ^proxyspace.gui }

	remove {
		arg ... args;
		args.do({|k| proxyspace.removeAt[k] });
		("removed:" ++ args).postln;
	}

	solo {
		arg ... args;

		args.do({|k|
			if(soloed.includes(k)){
				proxyspace[k].resume;
			};
		});

		soloed = [];

		proxyspace.arProxyNames.do({|a|
			if(args.includes(a).not){
				proxyspace[a].pause;
				soloed = soloed.add(a);
			}
		});

		("soloed:" ++ args).postln;
	}

	unsolo {
		soloed.do({|k|
			if(proxyspace.arProxyNames.includes(k)){
				proxyspace[k].resume;
			}
		});

		soloed = [];

		("unsoloed all").postln;
	}

	play {
		arg ... args;
		args.do({|k|
			proxyspace[k].play
		});

		("played:" ++ args).postln;
	}

	stop {
		arg ... args;
		args.do({|k|
			proxyspace[k].stop
		});

		("stopped:" ++ args).postln;
	}

}

//========================================================================
// Select but the "which" argument wraps around the array
WSelect {

	*ar {|which, array|
		^(Select.ar(which % array.size, array))
	}

	*kr {|which, array|
		^(Select.kr(which % array.size, array))
	}

}

//========================================================================
// Returns 1 when input modulo m == 0
Every {
	*action {|t, m| ^(t % m <= 0) }
	*kr {|t, m| ^Every.action(t,m) }
	*ar {|t, m| ^Every.action(t,m) }
}

//========================================================================
// Returns 1 when input modulo a < b
WhenMod {
	*action {|t, a, b| ^((t % a) <= b) }
	*kr {|t, a, b| ^Every.action(t,a,b) }
	*ar {|t, a, b| ^Every.action(t,a,b) }
}

//========================================================================
// Euclidean algoritmn (requires Bjorklund quark)
Euclid {
	*ar {|t, a, b| ^WSelect.ar(t, Bjorklund(a,b)) }
	*kr {|t, a, b| ^WSelect.kr(t, Bjorklund(a,b)) }
}

//========================================================================
Div {
	*new {|t,d| ^(t/d).floor }
}