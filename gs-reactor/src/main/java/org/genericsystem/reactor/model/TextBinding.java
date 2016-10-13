package org.genericsystem.reactor.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.function.BiFunction;

import org.genericsystem.common.Statics;
import org.genericsystem.reactor.Context;
import org.genericsystem.reactor.Tag;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;

public interface TextBinding extends BiFunction<Context, Tag, ObservableValue<String>> {

	public static class GENERIC_STRING implements TextBinding {
		@Override
		public ObservableValue<String> apply(Context context, Tag tag) {
			return null;
		}
	}

	public static class CACHE_LEVEL implements TextBinding {
		@Override
		public ObservableValue<String> apply(Context context, Tag tag) {
			return Bindings.convert(context.getCacheLevelObservableValue());
		}
	}

	public static class LAST_UPDATE implements TextBinding {
		@Override
		public ObservableValue<String> apply(Context context, Tag tag) {
			return Bindings.createStringBinding(() -> {
				Long tsMs = (Long) context.getTsObservableValue().getValue() / Statics.MILLI_TO_NANOSECONDS;
				Date dateMs = new Date(tsMs);
				Instant instant = Instant.ofEpochMilli(dateMs.getTime());
				LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
				return "Last update : " + ldt.format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss"));
			}, context.getTsObservableValue());
		}
	}
}
