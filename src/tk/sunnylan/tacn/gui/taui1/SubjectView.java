package tk.sunnylan.tacn.gui.taui1;

import static tk.sunnylan.tacn.parse.Util.convertMarkToString;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import com.jfoenix.controls.JFXToggleButton;
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
import tk.sunnylan.tacn.data.Assignment;
import tk.sunnylan.tacn.data.Mark;
import tk.sunnylan.tacn.data.Subject;
import tk.sunnylan.tacn.parse.Parse;

public class SubjectView extends Scene {

	public final ObservableList<AssignmentWrapper> assignments;
	public final JFXToggleButton toggleSummary;
	private SummaryView sum;
	private Subject subject;

	public static SubjectView getNewSubjectView(Subject subject) throws IOException {
		FXMLLoader mahLoader = new FXMLLoader(TAUI1.class.getResource("SubjectView.fxml"));
		return new SubjectView(mahLoader.load(), mahLoader.getController(), subject);
	}

	public SubjectView(Parent root, SubjectViewController controller, Subject subject) {
		super(root);

		try {
			sum = SummaryView.getNewScene(subject);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		controller.summaryPane.getChildren().add(sum.getRoot());
		assignments = FXCollections.observableArrayList();
		initMarkTable(controller, subject);
		toggleSummary = new JFXToggleButton();
		toggleSummary.setText("Show summary");
		toggleSummary.setOnAction((e) -> {
			controller.summaryPane.getChildren().clear();
			if (toggleSummary.isSelected()) {
				controller.summaryPane.getChildren().add(sum.getRoot());
			}
		});
		toggleSummary.setSelected(true);
	}

	private void initMarkTable(SubjectViewController controller, Subject subject) {
		this.subject = subject;
		JFXTreeTableColumn<AssignmentWrapper, String> assignmentColumn = new JFXTreeTableColumn<>("Assignment");

		assignmentColumn.setCellValueFactory((CellDataFeatures<AssignmentWrapper, String> param) -> {
			if (assignmentColumn.validateValue(param))
				return param.getValue().getValue().name;
			else
				return assignmentColumn.getComputedValue(param);
		});
		assignmentColumn.setPrefWidth(200);
		controller.tableMarks.getColumns().add(assignmentColumn);

		for (String section : subject.sections) {
			JFXTreeTableColumn<AssignmentWrapper, String> sectionColumn = new JFXTreeTableColumn<>(section);

			sectionColumn.setCellValueFactory((CellDataFeatures<AssignmentWrapper, String> param) -> {
				if (sectionColumn.validateValue(param)) {
					Assignment a = param.getValue().getValue().a;
					if (a.containsMark(section)) {
						Mark m = a.getMark(section);
						return new SimpleStringProperty(convertMarkToString(m));
					}
				}

				return sectionColumn.getComputedValue(param);
			});
			sectionColumn.setCellFactory(
					((TreeTableColumn<AssignmentWrapper, String> param) -> new GenericEditableTreeTableCell<AssignmentWrapper, String>(
							new TextFieldEditorBuilder())));
			sectionColumn.setOnEditCommit((CellEditEvent<AssignmentWrapper, String> t) -> {
				AssignmentWrapper wr = ((AssignmentWrapper) t.getTreeTableView()
						.getTreeItem(t.getTreeTablePosition().getRow()).getValue());
				if (!Parse.parseMark(wr.a.getMark(section), t.getNewValue())) {
					if (!Parse.parseMark(wr.a.getMark(section), t.getOldValue())) {
						wr.a.clearMark(section);
					}
				}
				sum.refresh();
			});

			sectionColumn.setPrefWidth(150);
			sectionColumn.getStyleClass().add("centercol");
			controller.tableMarks.getColumns().add(sectionColumn);
		}
		refresh();

		TreeItem<AssignmentWrapper> rootItem = new RecursiveTreeItem<AssignmentWrapper>(assignments,
				RecursiveTreeObject::getChildren);
		controller.tableMarks.setRoot(rootItem);

	}

	public void refresh() {
		assignments.removeAll(assignments);
		Iterator<Entry<String, Assignment>> f = subject.getAssignmentIterator();
		Entry<String, Assignment> entry;
		while (f.hasNext()) {
			entry = f.next();
			assignments.add(new AssignmentWrapper(entry.getValue(), entry.getKey()));
		}
		sum.refresh();
	}

	static class AssignmentWrapper extends RecursiveTreeObject<AssignmentWrapper> {

		public final StringProperty name;
		public final Assignment a;

		public AssignmentWrapper(Assignment a, String name) {
			this.a = a;
			this.name = new SimpleStringProperty(name);
		}
	}
}
