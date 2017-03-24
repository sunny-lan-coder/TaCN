package tk.sunnylan.tacn.gui.taui1;

import java.io.IOException;

import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.cells.editors.TextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTreeTableCell;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import tk.sunnylan.tacn.analysis.SubjectSummary;
import tk.sunnylan.tacn.data.Subject;

public class SummaryView extends Scene {

	private SummaryViewController controller;
	private SubjectSummary sum;
	private ObservableList<WeightWrapper> weights;
	private Subject s;

	public static SummaryView getNewScene(Subject s) throws IOException {
		FXMLLoader mahLoader = new FXMLLoader(TAUI1.class.getResource("SummaryView.fxml"));
		return new SummaryView(mahLoader.load(), mahLoader.getController(), s);
	}

	public void refresh() {
		weights.removeAll(weights);
		for (String section : s.weights.keySet()) {
			weights.add(new WeightWrapper(section, s.weights.get(section)));
		}

		for (String section : s.sections) {
			if (!s.weights.containsKey(section))
				weights.add(new WeightWrapper(section, s.weights.get(section)));
		}

		sum.refresh();
		if (sum.average != null)
			controller.lblAverage.setText(String.format("Average:%n%.1f%%", sum.average));
		else
			controller.lblAverage.setText("Average:\n" + "N/A");
	}

	private SummaryView(Parent root, SummaryViewController controller, Subject s) {
		super(root);
		this.s = s;
		this.controller = controller;
		sum = new SubjectSummary(s);
		sum.refresh();

		JFXTreeTableColumn<WeightWrapper, String> colSectionName = new JFXTreeTableColumn<>("Section");
		colSectionName.setCellValueFactory((CellDataFeatures<WeightWrapper, String> param) -> {
			if (colSectionName.validateValue(param)) {
				return param.getValue().getValue().section;
			}
			return colSectionName.getComputedValue(param);
		});
		colSectionName.setPrefWidth(150);

		JFXTreeTableColumn<WeightWrapper, String> colWeight = new JFXTreeTableColumn<>("Weight");
		colWeight.setCellValueFactory((CellDataFeatures<WeightWrapper, String> param) -> {
			if (colWeight.validateValue(param) && param.getValue().getValue().weight != null) {
				return new SimpleStringProperty(String.format("%.0f%%", param.getValue().getValue().weight));
			}
			return colWeight.getComputedValue(param);
		});

		weights = FXCollections.observableArrayList();
		colWeight.setCellFactory(
				((TreeTableColumn<WeightWrapper, String> param) -> new GenericEditableTreeTableCell<WeightWrapper, String>(
						new TextFieldEditorBuilder())));
		colWeight.setOnEditCommit((CellEditEvent<WeightWrapper, String> t) -> {
			Double val;
			try {
				val = Double.parseDouble(t.getNewValue());
			} catch (NumberFormatException e) {
				return;
			}
			WeightWrapper wr = ((WeightWrapper) t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow())
					.getValue());
			wr.weight = val;
			s.weights.put(wr.section.getValue(), wr.weight);
			refresh();
		});
		colWeight.setPrefWidth(100);
		JFXTreeTableColumn<WeightWrapper, String> colAverage = new JFXTreeTableColumn<>("Average");
		colAverage.setCellValueFactory((CellDataFeatures<WeightWrapper, String> param) -> {
			if (colAverage.validateValue(param)) {
				Double avg = sum.averages.get(param.getValue().getValue().section.getValue());
				if (avg != null) {
					return new SimpleStringProperty(String.format("%.1f%%", avg));
				}
			}
			return colAverage.getComputedValue(param);
		});
		colAverage.setPrefWidth(100);

		controller.tableWeights.getColumns().add(colSectionName);
		controller.tableWeights.getColumns().add(colWeight);
		controller.tableWeights.getColumns().add(colAverage);
		refresh();
		TreeItem<WeightWrapper> rootItem = new RecursiveTreeItem<WeightWrapper>(weights,
				RecursiveTreeObject::getChildren);

		controller.tableWeights.setRoot(rootItem);
	}

	static class WeightWrapper extends RecursiveTreeObject<WeightWrapper> {
		public final StringProperty section;
		public Double weight;

		public WeightWrapper(String section, Double weight) {
			this.section = new SimpleStringProperty(section);
			this.weight = weight;
		}
	}
}
