package org.genericsystem.reactor.gscomponents;

import org.genericsystem.reactor.modelproperties.ComponentsDefaults;
import org.genericsystem.reactor.modelproperties.ConvertedValueDefaults;
import org.genericsystem.reactor.modelproperties.PasswordDefaults;
import org.genericsystem.reactor.modelproperties.SelectionDefaults;
import org.genericsystem.reactor.modelproperties.UserRoleDefaults;

import org.genericsystem.reactor.htmltag.HtmlButton;
import org.genericsystem.reactor.htmltag.HtmlHyperLink;
import org.genericsystem.reactor.htmltag.HtmlInputText;
import org.genericsystem.reactor.htmltag.HtmlLabel;
import org.genericsystem.reactor.htmltag.HtmlLabel.GSLabelDisplayer;
import org.genericsystem.reactor.htmltag.HtmlSpan;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.genericsystem.api.core.exceptions.RollbackException;
import org.genericsystem.common.Generic;
import org.genericsystem.reactor.Context;
import org.genericsystem.reactor.EncryptionUtils;
import org.genericsystem.reactor.annotations.Attribute;
import org.genericsystem.reactor.annotations.BindAction;
import org.genericsystem.reactor.annotations.BindText;
import org.genericsystem.reactor.annotations.Children;
import org.genericsystem.reactor.annotations.ForEach;
import org.genericsystem.reactor.annotations.Select;
import org.genericsystem.reactor.annotations.SelectModel;
import org.genericsystem.reactor.annotations.SetText;
import org.genericsystem.reactor.annotations.Style;
import org.genericsystem.reactor.annotations.Style.FlexDirectionStyle;
import org.genericsystem.reactor.annotations.Style.GenericValueBackgroundColor;
import org.genericsystem.reactor.annotations.Style.ReverseFlexDirection;
import org.genericsystem.reactor.gscomponents.GSCheckBoxWithValue.GSCheckBoxEditor;
import org.genericsystem.reactor.gscomponents.GSComposite.Content;
import org.genericsystem.reactor.gscomponents.GSComposite.Header;
import org.genericsystem.reactor.gscomponents.GSInputTextWithConversion.GSInputTextEditorWithConversion;
import org.genericsystem.reactor.gscomponents.GSInputTextWithConversion.PasswordInput;
import org.genericsystem.reactor.gscomponents.GSSelect.GSSelectWithEmptyEntry;
import org.genericsystem.reactor.gscomponents.GSSelect.InstanceCompositeSelect;
import org.genericsystem.reactor.gscomponents.InstanceEditor.AttributeContent;
import org.genericsystem.reactor.gscomponents.InstanceEditor.GSHoldersEditor;
import org.genericsystem.reactor.gscomponents.InstanceEditor.GSMultiCheckbox;
import org.genericsystem.reactor.gscomponents.InstanceEditor.GSPasswordHoldersEditor;
import org.genericsystem.reactor.gscomponents.InstanceEditor.GSValueComponentsEditor;
import org.genericsystem.reactor.gscomponents.InstancesTable.ContentRow;
import org.genericsystem.reactor.gscomponents.InstancesTable.GSHolders;
import org.genericsystem.reactor.gscomponents.InstancesTable.GSValueComponents;
import org.genericsystem.reactor.gscomponents.InstancesTable.HeaderRow;
import org.genericsystem.reactor.gscomponents.Modal.ModalWithDisplay;
import org.genericsystem.reactor.gscomponents2.GSCellDiv.GSActionLink;
import org.genericsystem.reactor.model.ContextAction.ADD_HOLDER;
import org.genericsystem.reactor.model.ContextAction.MODAL_DISPLAY_FLEX;
import org.genericsystem.reactor.model.ContextAction.REMOVE;
import org.genericsystem.reactor.model.ObservableListExtractor;
import org.genericsystem.reactor.model.ObservableListExtractor.NO_FOR_EACH;
import org.genericsystem.reactor.model.ObservableListExtractor.SUBINSTANCES_OF_LINK_COMPONENT;
import org.genericsystem.reactor.model.ObservableModelSelector.HOLDER_ADDITION_ENABLED_SELECTOR;
import org.genericsystem.reactor.model.ObservableModelSelector.REMOVABLE_HOLDER_SELECTOR;
import org.genericsystem.reactor.model.ObservableValueSelector;
import org.genericsystem.reactor.model.ObservableValueSelector.DIRECT_RELATION_SELECTOR;
import org.genericsystem.reactor.model.ObservableValueSelector.MULTICHECKBOX_SELECTOR;
import org.genericsystem.reactor.model.ObservableValueSelector.NON_MULTICHECKBOX_SELECTOR;
import org.genericsystem.reactor.model.ObservableValueSelector.PASSWORD_ATTRIBUTE_SELECTOR;
import org.genericsystem.reactor.model.ObservableValueSelector.REVERSED_RELATION_SELECTOR;
import org.genericsystem.reactor.model.ObservableValueSelector.STRICT_ATTRIBUTE_SELECTOR;
import org.genericsystem.reactor.model.ObservableValueSelector.TYPE_SELECTOR;
import org.genericsystem.security.model.User.Salt;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

@Style(name = "flex", value = "1 1 0%")
@Style(name = "overflow", value = "hidden")
@ReverseFlexDirection(path = GSComposite.class)
@FlexDirectionStyle(FlexDirection.ROW)
@Style(path = HeaderRow.class, name = "flex", value = "0.3")
@Style(path = ContentRow.class, name = "flex", value = "1")
@Children({ HeaderRow.class, ContentRow.class })
@Children(path = HeaderRow.class, value = { GSValueComponents.class, GSValueComponents.class })
@Children(path = ContentRow.class, value = { GSValueComponentsEditor.class, AttributeContent.class })
@Children(path = { ContentRow.class, GSValueComponentsEditor.class }, value = { Header.class, Content.class })
@Children(path = { ContentRow.class, Content.class }, value = { GSPasswordHoldersEditor.class, GSHoldersEditor.class, GSMultiCheckbox.class })
@ForEach(path = { HeaderRow.class, GSValueComponents.class }, pos = { 0, 1 }, value = ObservableListExtractor.ATTRIBUTES_OF_INSTANCES.class)
@ForEach(path = { GSComposite.class, GSValueComponents.class, Content.class }, value = ObservableListExtractor.OTHER_COMPONENTS_2.class)
@Select(path = { HeaderRow.class, GSValueComponents.class }, pos = { 0, 0 }, value = TYPE_SELECTOR.class)
public class InstanceEditor extends GSDiv implements SelectionDefaults {

	@FlexDirectionStyle(FlexDirection.COLUMN)
	public static class HorizontalInstanceEditor extends InstanceEditor {
	}

	@Children({ GSPasswordHoldersEditor.class, GSHoldersEditor.class, GSMultiCheckbox.class })
	@ForEach(ObservableListExtractor.ATTRIBUTES_OF_INSTANCES.class)
	@Select(path = GSPasswordHoldersEditor.class, value = PASSWORD_ATTRIBUTE_SELECTOR.class)
	@Select(path = GSHoldersEditor.class, value = NON_MULTICHECKBOX_SELECTOR.class)
	@Select(path = GSMultiCheckbox.class, value = MULTICHECKBOX_SELECTOR.class)
	public static class AttributeContent extends Content {
	}

	@Children(CheckboxLabel.class)
	@ForEach(path = CheckboxLabel.class, value = SUBINSTANCES_OF_LINK_COMPONENT.class)
	@Style(name = "flex-wrap", value = "wrap")
	@Style(name = "overflow", value = "auto")
	@Style(name = "margin-right", value = "1px")
	@Style(name = "margin-bottom", value = "1px")
	public static class GSMultiCheckbox extends GSDiv {
	}

	@Style(name = "flex", value = "1 0 auto")
	@Style(name = "justify-content", value = "center")
	@Style(name = "align-items", value = "center")
	@Style(name = "text-align", value = "center")
	@GenericValueBackgroundColor("#e5ed00")
	@Children(Checkbox.class)
	@BindText
	public static class CheckboxLabel extends org.genericsystem.reactor.htmltag.HtmlLabel {
	}

	@Style(name = "float", value = "left")
	@Style(name = "vertical-align", value = "middle")
	@Style(name = "margin", value = "4px")
	public static class Checkbox extends GSCheckBoxWithValue {

		@Override
		public void init() {
			initValueProperty(context -> context.getGenerics()[2].getLink(context.getGenerics()[1], context.getGeneric()) != null ? true : false);
			storeProperty("exists", context -> {
				ObservableValue<Boolean> exists = Bindings.createBooleanBinding(() -> context.getGenerics()[2].getObservableLink(context.getGenerics()[1], context.getGeneric()).getValue() != null ? true : false,
						context.getGenerics()[2].getObservableLink(context.getGenerics()[1], context.getGeneric()));
				exists.addListener((o, v, nva) -> {
					if (!context.isDestroyed())
						getConvertedValueProperty(context).setValue(nva);
				});
				return exists;
			});
			addConvertedValueChangeListener((context, nva) -> {
				if (Boolean.TRUE.equals(nva))
					context.getGenerics()[2].setHolder(context.getGenerics()[1], null, context.getGeneric());
				if (Boolean.FALSE.equals(nva)) {
					Generic link = context.getGenerics()[2].getLink(context.getGenerics()[1], context.getGeneric());
					if (link != null)
						link.remove();
				}
			});
		}
	}

	@Children({ ModalWithDisplay.class, HtmlHyperLink.class })
	@Children(path = { ModalWithDisplay.class, GSDiv.class }, value = { PasswordEditorContent.class, HtmlHyperLink.class })
	@SetText(path = HtmlHyperLink.class, value = "Change password")
	@BindAction(path = HtmlHyperLink.class, value = MODAL_DISPLAY_FLEX.class)
	public static class PasswordEditor extends GSDiv implements PasswordDefaults {
		@Override
		public void init() {
			addPrefixBinding(context -> {
				Property<byte[]> saltProperty = getSaltProperty(context);
				if (saltProperty.getValue() == null)
					saltProperty.setValue((byte[]) context.find(Salt.class).getInstance(context.getGeneric()).getValue());
			});
		}
	}

	@Children({ HtmlLabel.class, HtmlInputText.class, HtmlLabel.class, HtmlInputText.class, HtmlLabel.class, HtmlInputText.class, HtmlSpan.class, HtmlSpan.class, ValidateButton.class })
	@SetText(path = HtmlLabel.class, pos = 0, value = "Enter old password:")
	@SetText(path = HtmlLabel.class, pos = 1, value = "Enter new password:")
	@SetText(path = HtmlLabel.class, pos = 2, value = "Confirm password:")
	@SetText(path = HtmlSpan.class, pos = 0, value = "These passwords don’t match. Try again.")
	@SetText(path = HtmlSpan.class, pos = 1, value = "Old password incorrect.")
	@Style(path = HtmlSpan.class, name = "color", value = "darkred")
	@Style(path = HtmlSpan.class, name = "display", value = "none")
	@Attribute(path = HtmlInputText.class, pos = 0, name = "type", value = "password")
	@Attribute(path = HtmlInputText.class, pos = 1, name = "type", value = "password")
	@Attribute(path = HtmlInputText.class, pos = 2, name = "type", value = "password")
	public static class PasswordEditorContent extends GSDiv {
	}

	@SetText("OK")
	public static class ValidateButton extends HtmlButton implements PasswordDefaults, UserRoleDefaults {
		@Override
		public void init() {
			bindAction(context -> {
				HtmlInputText oldPassword = getParent().find(HtmlInputText.class);
				HtmlInputText passwordInput = getParent().find(HtmlInputText.class, 1);
				HtmlInputText confirmPassword = getParent().find(HtmlInputText.class, 2);
				HtmlSpan invalidPassword = getParent().find(HtmlSpan.class, 1);
				HtmlSpan invalidConfirmPassword = getParent().find(HtmlSpan.class);
				if (Arrays.equals((byte[]) context.getGeneric().getValue(), EncryptionUtils.getEncryptedPassword(oldPassword.getDomNodeAttributes(context).get("value"), getSaltProperty(context).getValue()))) {
					String psw1 = passwordInput.getDomNodeAttributes(context).get("value");
					String psw2 = confirmPassword.getDomNodeAttributes(context).get("value");
					if (psw1 != null && psw1.equals(psw2)) {
						invalidConfirmPassword.addStyle(context, "display", "none");
						invalidPassword.addStyle(context, "display", "none");
						context.getGeneric().updateValue(EncryptionUtils.getEncryptedPassword(psw1, getSaltProperty(context).getValue()));
					} else {
						invalidConfirmPassword.addStyle(context, "display", "none");
					}
				} else {
					invalidPassword.addStyle(context, "display", "inline");
				}
			});
		}
	}

	@Children({ HtmlLabel.class, PasswordInput.class, HtmlLabel.class, PasswordInput.class, HtmlSpan.class })
	@SetText(path = HtmlLabel.class, pos = 0, value = "Enter new password:")
	@SetText(path = HtmlLabel.class, pos = 1, value = "Confirm password:")
	@SetText(path = HtmlSpan.class, value = "These passwords don’t match. Try again.")
	@Style(path = HtmlSpan.class, name = "color", value = "darkred")
	@Style(path = HtmlSpan.class, name = "display", value = "none")
	public static class PasswordAdder extends GSDiv implements PasswordDefaults, ConvertedValueDefaults {
		@Override
		public void init() {
			createConvertedValueProperty();
			addConvertedValueChangeListener((context, nva) -> {
				if (nva != null) {
					Generic passwordHash = context.getGenerics()[1].addHolder(context.getGeneric(), nva);
					passwordHash.setHolder(context.find(Salt.class), getSaltProperty(context).getValue());
				}
			});
			find(PasswordInput.class).addConvertedValueChangeListener((context, nva) -> {
				if (nva != null && Arrays.equals((byte[]) nva, (byte[]) find(PasswordInput.class, 1).getConvertedValueProperty(context).getValue())) {
					getConvertedValueProperty(context).setValue(nva);
					find(HtmlSpan.class).addStyle(context, "display", "none");
				} else
					find(HtmlSpan.class).addStyle(context, "display", "inline");
			});
			find(PasswordInput.class, 1).addConvertedValueChangeListener((context, nva) -> {
				if (nva != null && Arrays.equals((byte[]) nva, (byte[]) find(PasswordInput.class, 0).getConvertedValueProperty(context).getValue())) {
					getConvertedValueProperty(context).setValue(nva);
					find(HtmlSpan.class).addStyle(context, "display", "none");
				} else
					find(HtmlSpan.class).addStyle(context, "display", "inline");
			});
		}
	}

	@Style(path = GSValueComponents.class, name = "flex", value = "1 0 auto")
	@Children(value = { PasswordEditor.class, PasswordAdder.class })
	@ForEach(path = PasswordEditor.class, value = ObservableListExtractor.HOLDERS.class)
	@SelectModel(path = PasswordAdder.class, value = HOLDER_ADDITION_ENABLED_SELECTOR.class)
	@FlexDirectionStyle(FlexDirection.COLUMN)
	@Style(name = "flex-wrap", value = "wrap")
	@Style(name = "overflow", value = "auto")
	public static class GSPasswordHoldersEditor extends GSDiv implements PasswordDefaults {
		@Override
		public void init() {
			createSaltProperty();
		}
	}

	@Style(path = { Header.class, GSInputTextEditorWithConversion.class }, name = "flex", value = "1")
	@Style(path = { Header.class, GSInputTextEditorWithConversion.class }, name = "width", value = "100%")
	@Children({ Header.class, Content.class, GSActionLink.class })
	@Children(path = Header.class, value = { GSInputTextEditorWithConversion.class })
	@Children(path = Content.class, value = { DirectRelationComponentEditor.class, GSLabelDisplayer.class })
	@Select(path = { Content.class, DirectRelationComponentEditor.class }, value = DIRECT_RELATION_SELECTOR.class)
	@Select(path = { Content.class, GSLabelDisplayer.class }, value = REVERSED_RELATION_SELECTOR.class)
	@SelectModel(path = GSActionLink.class, value = REMOVABLE_HOLDER_SELECTOR.class)
	@SetText(path = GSActionLink.class, value = "×")
	@BindAction(path = GSActionLink.class, value = REMOVE.class)
	public static class GSValueComponentsEditor extends GSValueComponents implements ComponentsDefaults {
	}

	@Style(path = GSValueComponents.class, name = "flex", value = "1 0 auto")
	@Children(value = { GSValueComponentsEditor.class, GSHolderAdder.class })
	@Children(path = { GSValueComponentsEditor.class, Header.class }, value = { GSInputTextEditorWithConversion.class, GSCheckBoxEditor.class })
	@Children(path = { GSHolderAdder.class, Header.class }, value = { HolderAdderInput.class, BooleanHolderAdderInput.class })
	@ForEach(path = GSHolderAdder.class, value = NO_FOR_EACH.class)
	@SelectModel(path = GSHolderAdder.class, value = HOLDER_ADDITION_ENABLED_SELECTOR.class)
	@Select(path = { GSValueComponents.class, Header.class, GSInputTextEditorWithConversion.class }, value = ObservableValueSelector.LABEL_DISPLAYER.class)
	@Select(path = { GSValueComponents.class, Header.class, GSCheckBoxEditor.class }, value = ObservableValueSelector.CHECK_BOX_DISPLAYER.class)
	@FlexDirectionStyle(FlexDirection.COLUMN)
	@Style(name = "flex-wrap", value = "wrap")
	@Style(name = "overflow", value = "auto")
	public static class GSHoldersEditor extends GSHolders {
	}

	@Style(name = "flex", value = "1")
	@Style(name = "width", value = "100%")
	public static class DirectRelationComponentEditor extends InstanceCompositeSelect {
		@Override
		public void init() {
			super.init();
			addPostfixBinding(model -> {
				Property<List<Property<Context>>> selectedComponents = getComponentsProperty(model);
				if (selectedComponents != null)
					selectedComponents.getValue().add(getSelectionProperty(model));
			});
		}
	}

	@Style(name = "flex", value = "1 0 auto")
	@Style(path = { Header.class, GSInputTextWithConversion.class }, name = "flex", value = "1")
	@Style(path = { Header.class, GSInputTextWithConversion.class }, name = "width", value = "100%")
	@Children({ Header.class, Content.class, GSActionLink.class })
	@Children(path = Header.class, value = { HolderAdderInput.class, BooleanHolderAdderInput.class })
	@Children(path = Content.class, value = ComponentAdderSelect.class)
	@Select(path = GSActionLink.class, value = STRICT_ATTRIBUTE_SELECTOR.class)
	@Select(path = Header.class, value = ObservableValueSelector.STRICT_ATTRIBUTE_SELECTOR_OR_CHECK_BOX_DISPLAYER_ATTRIBUTE.class)
	@Select(path = { Header.class, HolderAdderInput.class }, value = ObservableValueSelector.LABEL_DISPLAYER_ATTRIBUTE.class)
	@Select(path = { Header.class, BooleanHolderAdderInput.class }, value = ObservableValueSelector.CHECK_BOX_DISPLAYER_ATTRIBUTE.class)
	@SetText(path = GSActionLink.class, value = "+")
	@BindAction(path = GSActionLink.class, value = ADD_HOLDER.class)
	public static class GSHolderAdder extends GSValueComponents implements ComponentsDefaults, ConvertedValueDefaults {
		@Override
		public void init() {
			createComponentsListProperty();
			createConvertedValueProperty();
			addConvertedValueChangeListener((context, nva) -> {
				if (nva != null)
					context.getGenerics()[1].addHolder(context.getGeneric(), nva);
			});
			addPostfixBinding(model -> {
				Property<List<Property<Context>>> selectedComponents = getComponentsProperty(model);
				ChangeListener<Context> listener = (o, v, nva) -> {
					List<Generic> selectedGenerics = selectedComponents.getValue().stream().filter(obs -> obs.getValue() != null).map(obs -> obs.getValue().getGeneric()).filter(gen -> gen != null).collect(Collectors.toList());
					if (selectedGenerics.size() + 1 == model.getGeneric().getComponents().size()) {
						selectedComponents.getValue().stream().forEach(sel -> sel.setValue(null));
						try {
							model.getGenerics()[1].setHolder(model.getGeneric(), null, selectedGenerics.stream().toArray(Generic[]::new));
						} catch (RollbackException e) {
							e.printStackTrace();
						}
					}
				};
				selectedComponents.getValue().forEach(component -> component.addListener(listener));
			});
		}
	}

	public static class HolderAdderInput extends GSInputTextWithConversion {
		@Override
		public void init() {
			addConvertedValueChangeListener((context, nva) -> ((ConvertedValueDefaults) getParent().getParent()).getConvertedValueProperty(context.getParent().getParent()).setValue(nva));
		}
	}

	public static class BooleanHolderAdderInput extends GSCheckBoxWithValue {
		@Override
		public void init() {
			addConvertedValueChangeListener((context, nva) -> ((ConvertedValueDefaults) getParent().getParent()).getConvertedValueProperty(context.getParent().getParent()).setValue(nva));
		}
	}

	@Style(name = "flex", value = "1")
	@Style(name = "width", value = "100%")
	@Select(DIRECT_RELATION_SELECTOR.class)
	public static class ComponentAdderSelect extends GSSelectWithEmptyEntry {

		@Override
		public void init() {
			super.init();
			addPostfixBinding(model -> {
				Property<List<Property<Context>>> selectedComponents = getComponentsProperty(model);
				if (selectedComponents != null)
					selectedComponents.getValue().add(getSelectionProperty(model));
			});
		}
	}
}