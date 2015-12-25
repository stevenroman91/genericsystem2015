package org.genericsystem.gsadmin;

import java.util.function.Function;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

import org.genericsystem.gsadmin.RowBuilder.TableCellRowBuilder;
import org.genericsystem.gsadmin.RowBuilder.TextCellFirstRowBuilder;
import org.genericsystem.gsadmin.RowBuilder.TextCellRowBuilder;
import org.genericsystem.gsadmin.Stylable.TableStyle;
import org.genericsystem.ui.Element;
import org.genericsystem.ui.components.GSHBox;
import org.genericsystem.ui.components.GSSCrollPane;
import org.genericsystem.ui.components.GSVBox;
import org.genericsystem.ui.utils.Transformation;

public abstract class TableBuilder<ITEM, COL, U, T> implements Builder {

	protected Table build(ObservableList<ITEM> items, ObservableValue<String> firstColumnString, ObservableList<COL> columns, Function<COL, ObservableValue<String>> columnExtractor, Function<ITEM, ObservableValue<String>> rowfirstColumnString,
			Function<ITEM, Function<COL, ObservableValue<T>>> rowColumnExtractor, TableStyle tableStyle) {
		return new Table(getFirstElement(firstColumnString, columns, columnExtractor, tableStyle), getElements(items, rowfirstColumnString, columns, rowColumnExtractor, tableStyle), getStyle(tableStyle));
	}

	protected ObservableValue<String> getStyle(TableStyle tableStyle) {
		return tableStyle.table;
	}

	protected ObservableValue<Row> getFirstElement(ObservableValue<String> firstColumnString, ObservableList<COL> columns, Function<COL, ObservableValue<String>> columnExtractor, TableStyle tableStyle) {
		return new SimpleObjectProperty<>(new TextCellFirstRowBuilder<COL, String>().build(firstColumnString, columns, columnExtractor, tableStyle));
	}

	protected ObservableList<Row> getElements(ObservableList<ITEM> items, Function<ITEM, ObservableValue<String>> rowfirstColumnString, ObservableList<COL> columns, Function<ITEM, Function<COL, ObservableValue<T>>> rowColumnExtractor, TableStyle tableStyle) {
		return new Transformation<Row, ITEM>(items, item -> getRowBuilder().build(rowfirstColumnString.apply(item), columns, col -> (ObservableValue) rowColumnExtractor.apply(item).apply(col), tableStyle));
	}

	@Override
	public void init(Element<?> parent) {
		GSSCrollPane scrollPane = new GSSCrollPane(parent);
		{
			GSVBox tablePanel = new GSVBox(scrollPane).setPrefWidth(800).setPrefHeight(600).setStyleClass(Table::getStyleClass);
			{
				new GSHBox(tablePanel).select(Table::getFirstElement).include(new TextCellFirstRowBuilder<>()::init).setStyleClass(Row::getStyleClass);
				new GSHBox(tablePanel).forEach(Table::getElements).include(getRowBuilder()::init).setStyleClass(Row::getStyleClass);
			}
		}
	}

	abstract RowBuilder<COL, U, T> getRowBuilder();

	public static class TextCellTableBuilder<ITEM, COL> extends TableBuilder<ITEM, COL, String, String> {

		@Override
		RowBuilder<COL, String, String> getRowBuilder() {
			return new TextCellRowBuilder<COL, String>();
		}
	}

	public static class TableCellTableBuilder<ITEM, COL> extends TableBuilder<ITEM, COL, Table, Table> {
		@Override
		RowBuilder<COL, Table, Table> getRowBuilder() {
			return new TableCellRowBuilder<COL, Table>();
		}
	}
}