package org.genericsystem.ir.reinforcer;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

public class Reinforcer {

	private static final double MATCHING_RATE = 0.9;
	private final List<Template> templates = new ArrayList<>();
	private final Unclassifiable unclassifiable = new Unclassifiable();

	public static void main(String[] args) {
		Reinforcer reinforcer = new Reinforcer();
		PublishSubject<AbsoluteLabels> source = PublishSubject.create();
		source.subscribe(reinforcer.getObserver());
		AbsoluteLabels labels = new AbsoluteLabels();
		labels.addAbsoluteLabel(0, 0, 10, 10, "First Label");
		labels.addAbsoluteLabel(5, 5, 15, 15, "Second Label");

		source.onNext(labels);

		// reinforcer.reinforce(new AbsoluteLabels().addAbsoluteLabel(0, 0, 10, 10, "First Label").addAbsoluteLabel(5, 5, 15, 15, "Second Label"));

	}

	public Consumer<AbsoluteLabels> getObserver() {
		return labels -> reinforce(labels);
	}

	public void reinforce(AbsoluteLabels absoluteLabels) {
		for (Template template : templates)
			if (template.getMatchRate(absoluteLabels) > MATCHING_RATE)
				template.reinforce(absoluteLabels);
		unclassifiable.reinforce(absoluteLabels);
	}

	public Unclassifiable getUnclassifiable() {
		return unclassifiable;
	}

}
