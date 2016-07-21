package org.genericsystem.reactor.flex;

import org.genericsystem.reactor.Tag;
import org.genericsystem.reactor.html.HtmlButton;
import org.genericsystem.reactor.html.HtmlH1;
import org.genericsystem.reactor.html.HtmlSection;
import org.genericsystem.reactor.model.GenericModel;

public class GenericSection extends HtmlSection<GenericModel> {
	private final FlexDirection direction;

	public GenericSection(Tag<?> parent, FlexDirection direction) {
		super(parent);
		this.direction = direction;
		addStyle("display", "flex");
		addStyle("flex-direction", direction.toString());
		addStyle("flex-wrap", "nowrap");
	}

	public FlexDirection getDirection() {
		return direction;
	}

	public FlexDirection getReverseDirection() {
		return getDirection().reverse();
	}

	@Override
	protected HtmlDomNode createNode(String parentId) {
		return new HtmlDomNode(parentId);
	}

	// TODO remove or improve this
	@Deprecated
	public static class H1FlexElement extends GenericSection {
		public H1FlexElement(Tag<?> parent, String title) {
			this(parent, FlexDirection.ROW, title);
		}

		public H1FlexElement(Tag<?> parent) {
			this(parent, FlexDirection.ROW);
		}

		public H1FlexElement(Tag<?> parent, FlexDirection flexDirection, String title) {
			super(parent, flexDirection);
			addStyle("justify-content", "center");
			addStyle("background-color", "#ffa500");
			new HtmlH1<>(this).setText(title);
		}

		public H1FlexElement(Tag<?> parent, FlexDirection flexDirection) {
			super(parent, flexDirection);
			addStyle("justify-content", "center");
			addStyle("background-color", "#ffa500");
			new HtmlH1<GenericModel>(this).bindText(GenericModel::getString);
		}
	};

	public static class TransactionMonitor extends GenericSection {

		public TransactionMonitor(Tag<?> parent) {
			this(parent, FlexDirection.ROW);
		}

		public TransactionMonitor(Tag<?> parent, FlexDirection direction) {
			super(parent, direction);
			addStyle("justify-content", "space-around");
			addStyle("padding", "10px");
			new HtmlButton<GenericModel>(this) {
				{
					setText("Save");
					bindAction(GenericModel::flush);
				}
			};
			new HtmlButton<GenericModel>(this) {
				{
					setText("Cancel");
					bindAction(GenericModel::cancel);
				}
			};
			new HtmlButton<GenericModel>(this) {
				{
					setText("Collect");
					bindAction(model -> System.gc());
				}
			};
		}

	}
}