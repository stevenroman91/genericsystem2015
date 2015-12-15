package org.genericsystem.ui.components;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.genericsystem.ui.Element;

public class GSTableColumn<T> extends Element<TableColumn> {

	public GSTableColumn(Element parent, String columnTitle, Function<T, String> stringConverter) {
		super(parent, TableColumn.class, TableView<T>::getColumns);
		setText(columnTitle);
		setCellValueFactory(features -> new SimpleObjectProperty<>(stringConverter.apply(features.getValue())));
	}

	public GSTableColumn(Element parent, Function<T, ObservableValue<String>> columnTitleObservable, Function<T, String> stringConverter) {
		super(parent, TableColumn.class, TableView<T>::getColumns);
		setObservableText(columnTitleObservable);
		setCellValueFactory(features -> new SimpleObjectProperty<>(stringConverter.apply(features.getValue())));
	}

	public GSTableColumn<T> setCellValueFactory(Callback<CellDataFeatures<T, String>, ObservableValue<String>> valueFactory) {
		super.addBoot(TableColumn::cellValueFactoryProperty, valueFactory);
		return this;
	}

	public GSTableColumn<T> setPrefWidth(Number prefWidth) {
		super.addBoot(TableColumn::prefWidthProperty, prefWidth);
		return this;
	}

	public GSTableColumn<T> setObservableText(Function<T, ObservableValue<String>> columnTitleObservable) {
		addBinding(TableColumn::textProperty, columnTitleObservable);
		return this;
	}

	public GSTableColumn<T> setText(String columnTitle) {
		addBoot(TableColumn::textProperty, columnTitle);
		return this;
	}

	public static class GSTableColumnAction<T> extends GSTableColumn<T> {
		private Callback<TableColumn<T, String>, TableCell<T, String>> callbackDelete;

		public GSTableColumnAction(Element parent, String columnTitle, Function<T, String> stringConverter, Consumer<T> action) {
			super(parent, columnTitle, stringConverter);
			callbackDelete = col -> new DeleteButtonCell(action);
			super.addBoot(TableColumn::cellFactoryProperty, callbackDelete);
		}

		@Override
		public GSTableColumnAction<T> setPrefWidth(Number prefWidth) {
			return (GSTableColumnAction<T>) super.setPrefWidth(prefWidth);
		}

		public class DeleteButtonCell extends TableCell<T, String> {
			private final Button cellButton = new Button();

			private final Consumer<T> consumer;

			public DeleteButtonCell(Consumer<T> consumer) {
				setEditable(true);
				cellButton.setMaxWidth(200);
				cellButton.setAlignment(Pos.BASELINE_CENTER);
				this.consumer = consumer;
			}

			public DeleteButtonCell() {
				setEditable(true);
				cellButton.setMaxWidth(200);
				cellButton.setAlignment(Pos.BASELINE_CENTER);
				this.consumer = e -> {};
			}

			@Override
			protected void updateItem(String t, boolean empty) {
				super.updateItem(t, empty);
				if (empty || t == null) {
					cellButton.setText(null);
					setGraphic(null);
				} else {
					cellButton.setText("Delete");
					setGraphic(cellButton);
					cellButton.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							Alert alert = new Alert(AlertType.CONFIRMATION);
							alert.setTitle("Confirmation Dialog");
							alert.setHeaderText("Confirmation is required");
							alert.setContentText("Are you sure you want to delete : " + t + " ?");

							Optional<ButtonType> result = alert.showAndWait();
							if (result.get() == ButtonType.OK) {
								consumer.accept((T) getTableRow().getItem());
							}
						}
					});

				}
			}
		}
	}

}
