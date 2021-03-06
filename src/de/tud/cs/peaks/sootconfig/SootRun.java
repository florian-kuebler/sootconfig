package de.tud.cs.peaks.sootconfig;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

public class SootRun {
	private final FluentOptions o;
	private final AnalysisTarget t;

	public SootRun(FluentOptions o, AnalysisTarget t) {
		super();
		this.o = o;
		this.t = t;
	}

	public SootResult perform() {
		SootResult result = new SootResult();
		result.totalSootRuntime().start();

		// System.out.println(t.toString());
		// System.out.println(o.toString());

		ByteArrayOutputStream out = configureSoot();

		loadClasses(result);

		setEntryPoints(result);

		runSootPacks(result);

		result.totalSootRuntime().stop();

		result.setCompleteOutput(out.toString());
		result.setSootGloabel(G.v());

		return result;
	}

	private void runSootPacks(SootResult result) {
		result.packRuntime().start();
		PackManager.v().runPacks();
		result.packRuntime().stop();
	}

	private void setEntryPoints(SootResult result) {
		result.entryPointCalculationRuntime().start();
		List<SootMethod> entryPoints = this.t.getEntryPointCalculator().calculateEntryPoints(Scene.v());
		if (entryPoints != null && entryPoints.size() > 0)
			Scene.v().setEntryPoints(entryPoints);
		result.entryPointCalculationRuntime().stop();
	}

	private void loadClasses(SootResult result) {
		result.classLoadRuntime().start();
		Scene.v().loadNecessaryClasses();
		result.classLoadRuntime().stop();
	}

	private ByteArrayOutputStream configureSoot() {
		G.reset();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		G.v().out = new PrintStream(out);

		this.o.applyTo(G.v().soot_options_Options());
		this.t.applyTo(G.v().soot_options_Options());
		Scene.v().addBasicClass("antlr.ASTNULLType", SootClass.HIERARCHY);
		Scene.v().addBasicClass("com.fasterxml.classmate.members.ResolvedField", SootClass.HIERARCHY);
		Scene.v().addBasicClass("com.fasterxml.classmate.members.ResolvedMethod", SootClass.HIERARCHY);

		return out;
	}

}
