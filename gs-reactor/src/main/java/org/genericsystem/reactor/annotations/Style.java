package org.genericsystem.reactor.annotations;

import org.genericsystem.reactor.modelproperties.GenericStringDefaults;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.BiConsumer;

import org.genericsystem.reactor.Tag;
import org.genericsystem.reactor.annotations.Style.FlexDirectionStyle.FlexDirectionStyleProcessor;
import org.genericsystem.reactor.annotations.Style.FlexDirectionStyle.FlexDirections;
import org.genericsystem.reactor.annotations.Style.GenericValueBackgroundColor.GenericValueBackgroundColorProcessor;
import org.genericsystem.reactor.annotations.Style.GenericValueBackgroundColor.GenericValueBackgroundColors;
import org.genericsystem.reactor.annotations.Style.KeepFlexDirection.KeepFlexDirectionProcessor;
import org.genericsystem.reactor.annotations.Style.KeepFlexDirection.KeepFlexDirections;
import org.genericsystem.reactor.annotations.Style.ReverseFlexDirection.ReverseFlexDirectionProcessor;
import org.genericsystem.reactor.annotations.Style.ReverseFlexDirection.ReverseFlexDirections;
import org.genericsystem.reactor.annotations.Style.StyleProcessor;
import org.genericsystem.reactor.annotations.Style.Styles;
import org.genericsystem.reactor.gscomponents.FlexDirection;
import org.genericsystem.reactor.gscomponents.GSDiv;
import org.genericsystem.reactor.gscomponents.GSTagImpl;
import org.genericsystem.reactor.model.StringExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Repeatable(Styles.class)
@Process(value = StyleProcessor.class, repeatable = true)
public @interface Style {
	Class<? extends GSTagImpl>[] path() default {};

	String name();

	String value();

	int[] pos() default {};

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE })
	public @interface Styles {
		Style[] value();
	}

	public static class StyleProcessor implements BiConsumer<Annotation, Tag> {

		@Override
		public void accept(Annotation annotation, Tag tag) {
			tag.addStyle(((Style) annotation).name(), ((Style) annotation).value());
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE })
	@Repeatable(FlexDirections.class)
	@Process(FlexDirectionStyleProcessor.class)
	public @interface FlexDirectionStyle {
		Class<? extends GSTagImpl>[] path() default {};

		FlexDirection value();

		int[] pos() default {};

		@Retention(RetentionPolicy.RUNTIME)
		@Target({ ElementType.TYPE })
		public @interface FlexDirections {
			FlexDirectionStyle[] value();
		}

		public static class FlexDirectionStyleProcessor implements BiConsumer<Annotation, Tag> {
			private static final Logger log = LoggerFactory.getLogger(FlexDirectionStyleProcessor.class);

			@Override
			public void accept(Annotation annotation, Tag tag) {
				if (GSDiv.class.isAssignableFrom(tag.getClass()))
					((GSDiv) tag).setDirection(((FlexDirectionStyle) annotation).value());
				else
					log.warn("Warning: FlexDirection is applicable only to GSDiv extensions.");
			}
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE })
	@Repeatable(KeepFlexDirections.class)
	@Process(KeepFlexDirectionProcessor.class)
	public @interface KeepFlexDirection {
		Class<? extends GSTagImpl>[] path() default {};

		int[] pos() default {};

		@Retention(RetentionPolicy.RUNTIME)
		@Target({ ElementType.TYPE })
		public @interface KeepFlexDirections {
			KeepFlexDirection[] value();
		}

		public static class KeepFlexDirectionProcessor implements BiConsumer<Annotation, Tag> {
			private static final Logger log = LoggerFactory.getLogger(ReverseFlexDirectionProcessor.class);

			@Override
			public void accept(Annotation annotation, Tag tag) {
				if (GSDiv.class.isAssignableFrom(tag.getClass()))
					((GSDiv) tag).keepDirection();
				else
					log.warn("Warning: KeepFlexDirection is applicable only to GSDiv extensions.");
			}
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE })
	@Repeatable(ReverseFlexDirections.class)
	@Process(ReverseFlexDirectionProcessor.class)
	public @interface ReverseFlexDirection {
		Class<? extends GSTagImpl>[] path() default {};

		int[] pos() default {};

		@Retention(RetentionPolicy.RUNTIME)
		@Target({ ElementType.TYPE })
		public @interface ReverseFlexDirections {
			ReverseFlexDirection[] value();
		}

		public static class ReverseFlexDirectionProcessor implements BiConsumer<Annotation, Tag> {
			private static final Logger log = LoggerFactory.getLogger(ReverseFlexDirectionProcessor.class);

			@Override
			public void accept(Annotation annotation, Tag tag) {
				if (GSDiv.class.isAssignableFrom(tag.getClass()))
					((GSDiv) tag).reverseDirection();
				else
					log.warn("Warning: ReverseFlexDirection is applicable only to GSDiv extensions.");
			}
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE })
	@Repeatable(GenericValueBackgroundColors.class)
	@Process(GenericValueBackgroundColorProcessor.class)
	public @interface GenericValueBackgroundColor {
		Class<? extends GSTagImpl>[] path() default {};

		String value();

		int[] pos() default {};

		@Retention(RetentionPolicy.RUNTIME)
		@Target({ ElementType.TYPE })
		public @interface GenericValueBackgroundColors {
			GenericValueBackgroundColor[] value();
		}

		public static class GenericValueBackgroundColorProcessor implements BiConsumer<Annotation, Tag> {

			@Override
			public void accept(Annotation annotation, Tag tag) {
				tag.addPrefixBinding(modelContext -> tag.addStyle(
						modelContext,
						"background-color",
						"Color".equals(StringExtractor.SIMPLE_CLASS_EXTRACTOR.apply(modelContext.getGeneric().getMeta())) ? ((GenericStringDefaults) tag).getGenericStringProperty(modelContext).getValue() : ((GenericValueBackgroundColor) annotation)
								.value()));
			}
		}
	}

}
