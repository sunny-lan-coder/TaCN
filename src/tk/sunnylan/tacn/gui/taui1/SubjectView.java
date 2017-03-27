package tk.sunnylan.tacn.gui.taui1;

import static tk.sunnylan.tacn.parse.htmlunit.Util.convertMarkToString;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.io.RuntimeIOException;

import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.cells.editors.TextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTreeTableCell;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
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
import tk.sunnylan.tacn.parse.htmlunit.Parse;

public class SubjectView extends Scene {
	private static Logger logger = Logger.getLogger(SubjectView.class.getName());

	public final ObservableList<AssignmentWrapper> assignments;
	private SummaryView sum;
	private Subject subject;
	private SubjectViewController controller;

	public static SubjectView getNewSubjectView(Subject subject) throws IOException {
		FXMLLoader mahLoader = new FXMLLoader(TAUI1.class.getResource("SubjectView.fxml"));
		try {
			return new SubjectView(mahLoader.load(), mahLoader.getController(), subject);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to load subject view", e);
			throw e;
		}
	}

	public SubjectView(Parent root, SubjectViewController controller, Subject subject) {
		super(root);
		this.controller = controller;
		try {
			sum = SummaryView.getNewScene(subject);
		} catch (IOException e1) {
			logger.log(Level.SEVERE, "Unable to initialize UI component:sum", e1);
			throw new RuntimeIOException(e1);
		}

		controller.summaryPane.getChildren().add(sum.getRoot());
		assignments = FXCollections.observableArrayList();
		initMarkTable(controller, subject);
	}

	private void initMarkTable(SubjectViewController controller, Subject subject) {
		this.subject = subject;
		JFXTreeTableColumn<AssignmentWrapper, Integer> timecodeColumn = new JFXTreeTableColumn<>("#");

		timecodeColumn.setCellValueFactory((CellDataFeatures<AssignmentWrapper, Integer> param) -> {
			if (timecodeColumn.validateValue(param))
				return new SimpleIntegerProperty(param.getValue().getValue().a.timeCode).asObject();
			else
				return timecodeColumn.getComputedValue(param);
		});
		timecodeColumn.setPrefWidth(50);
		timecodeColumn.setSortable(true);
		controller.tableMarks.getColumns().add(timecodeColumn);
		controller.tableMarks.getSortOrder().add(timecodeColumn);

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

				if (t.getNewValue().isEmpty()) {
					wr.a.clearMark(section);
				} else if (!wr.a.containsMark(section))
				
						wr.a.addMark(new Mark(0, 0, 0), section);
					
				if (!Parse.parseMark(wr.a.getMark(section), t.getNewValue())) {
					if (!Parse.parseMark(wr.a.getMark(section), t.getOldValue())) {
						wr.a.clearMark(section);
					}
				}
				refresh();
			});

			sectionColumn.setPrefWidth(150);
			sectionColumn.getStyleClass().add("centercol");
			controller.tableMarks.getColumns().add(sectionColumn);
		}

		refresh();
	}

	public void refresh() {
		assignments.removeAll(assignments);
		Iterator<Entry<String, Assignment>> f = subject.getAssignmentIterator();
		Entry<String, Assignment> entry;
		while (f.hasNext()) {
			entry = f.next();
			assignments.add(new AssignmentWrapper(entry.getValue(), entry.getKey()));
		}
		SortedList<AssignmentWrapper> sa = new SortedList<>(assignments);
		sa.setComparator((AssignmentWrapper a1, AssignmentWrapper a2) -> {
			return -Integer.compare(a1.a.timeCode, a2.a.timeCode);
		});

		TreeItem<AssignmentWrapper> rootItem = new RecursiveTreeItem<AssignmentWrapper>(sa,
				RecursiveTreeObject::getChildren);
		controller.tableMarks.setRoot(rootItem);
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
