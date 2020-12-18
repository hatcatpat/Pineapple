Pineapple {

	var <proxyspace, <samples;

	*new {
		^super.new.init;
	}

	init {
		proxyspace = ProxySpace.new(Server.default);
		currentEnvironment[\pineapple] = this;

		this.loadSamples;
	}

	loadSamples {
		samples = ();
		samples.add(\foldernames -> PathName("C:/Users/patri/Documents/SuperCollider/Samples").entries);
		for (0, samples[\foldernames].size-1,
			{
				arg i;
				samples.add(samples[\foldernames][i].folderName.asSymbol -> samples[\foldernames][i].entries.collect({
					arg sf;
					Buffer.read(Server.default,sf.fullPath);
				});
		)});
	}


}
//
//
//
//
//
TChance {

	*kr {
		arg trig, chance;
		^(
			TWChoose.kr(trig, [0,1], [1-chance,chance]) * trig
		);
	}

}
//
TSeq {
	*kr {
		arg pos, array;
		^(
			Select.kr(pos*(array.size-1), array)
		);
	}
}