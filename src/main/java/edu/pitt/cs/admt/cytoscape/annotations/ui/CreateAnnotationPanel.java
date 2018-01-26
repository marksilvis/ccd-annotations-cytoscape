package edu.pitt.cs.admt.cytoscape.annotations.ui;

import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegate;
import edu.pitt.cs.admt.cytoscape.annotations.db.StorageDelegateFactory;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.Annotation;
import edu.pitt.cs.admt.cytoscape.annotations.db.entity.AnnotationValueType;
import edu.pitt.cs.admt.cytoscape.annotations.task.CreateAnnotationTaskFactory;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class CreateAnnotationPanel extends JPanel implements Serializable {

  private static final long serialVersionUID = 7883464890620284415L;
  private static final String NEW_ANNOTATION_NAME = "New...";
  private final JLabel title = new JLabel("Create new CCD Annotation", SwingConstants.CENTER);
  private final JLabel nameLabel = new JLabel("Annotation name");
  private final JComboBox<String> nameSelector = new JComboBox<>();
  private final JLabel newNameLabel = new JLabel("New annotation name");
  private final JTextField newNameField = new JTextField();
  private final JLabel descriptionLabel = new JLabel("Annotation description");
  private final JTextArea descriptionText = new JTextArea();
  private final JLabel valueTypeLabel = new JLabel("Annotation value type");
  private final JComboBox<String> valueTypeSelector = new JComboBox<>(
      new DefaultComboBoxModel<>(
          new Vector<>(Arrays.asList(new String[]{"boolean", "char", "float", "int", "string"}))
      )
  );
  private final JLabel valueLabel = new JLabel("Annotation value");
  private final JComboBox<String> valueBoolField = new JComboBox<>(
      new DefaultComboBoxModel<>(
          new Vector(Arrays.asList(new String[]{"true", "false"}))
      )
  );
  private final JTextField valueField = new JTextField();
  private final JButton createButton = new JButton("Create");
  private Map<String, Annotation> annotations = new HashMap<>();
  private Long networkSUID = null;

  public CreateAnnotationPanel(
      final TaskManager taskManager,
      final CreateAnnotationTaskFactory createAnnotationTaskFactory) {
    // panel settings
    setBorder(new EmptyBorder(10, 10, 10, 10));
    setPreferredSize(new Dimension(300, 800));
    setMaximumSize(new Dimension(400, 1000));

    // actions
    createButton.addActionListener((ActionEvent e) -> {
      String name;
      if (nameSelector.getSelectedIndex() == 0) {
        name = newNameField.getText();
      } else {
        name = (String) nameSelector.getSelectedItem();
      }
      AnnotationValueType type = AnnotationValueType.parse((String)this.valueTypeSelector.getSelectedItem());
      TaskIterator taskIterator = new TaskIterator();
      taskIterator.append(createAnnotationTaskFactory
        .createOnSelected(name)
        .setAnnotationDescription(descriptionText.getText())
        .setAnnotationValue(valueField.getText())
        .setAnnotationValueType(type)
        .enableDatabaseUpdate()
        .createTaskIterator());
      taskIterator.append(new Task() {
        @Override
        public void run(TaskMonitor taskMonitor) throws Exception {
          refresh();
        }

        @Override
        public void cancel() { }
      });
      taskIterator.append(new Task() {
        @Override
        public void run(TaskMonitor taskMonitor) throws Exception {
          nameSelector.setSelectedItem(name);
        }
        @Override
        public void cancel() { }
      });
      taskManager.execute(taskIterator);
      newNameField.setText("");
    });

    nameSelector.addActionListener((ActionEvent e) -> {
      String selected = (String) nameSelector.getSelectedItem();
      if (selected.equals(NEW_ANNOTATION_NAME)) {
        newNameLabel.setVisible(true);
        newNameField.setVisible(true);
        descriptionText.setText("");
        descriptionText.setEditable(true);
        valueTypeSelector.setSelectedIndex(0);
        valueTypeSelector.setEnabled(true);
        valueField.setText("");
      } else {
        newNameLabel.setVisible(false);
        newNameField.setVisible(false);
        Annotation annotation = annotations.get(selected);
        descriptionText.setText(annotation.getDescription());
        descriptionText.setEditable(false);
        valueTypeSelector.setSelectedItem(annotation.getType().toString().toLowerCase());
        valueTypeSelector.setEnabled(false);
        valueField.setText("");
      }
    });


    // Components
    add(title);
    // name
    add(nameLabel);
    add(nameSelector);
    newNameLabel.setVisible(true);
    newNameField.setVisible(true);
    newNameField.setPreferredSize(new Dimension(200, 20));
    add(newNameLabel);
    add(newNameField);
    // description
    descriptionText.setLineWrap(true);
    descriptionText.setRows(3);
    descriptionText.setPreferredSize(new Dimension(200, 40));
    add(descriptionLabel);
    add(descriptionText);
    // value type
    add(valueTypeLabel);
    add(valueTypeSelector);
    // value
    valueField.setPreferredSize(new Dimension(100, 20));
    add(valueLabel);
    add(valueField);
    add(createButton);

    setVisible(true);
  }

  private void updateView() {
    List<String> names = new ArrayList<>(this.annotations.keySet());
    Collections.sort(names);
    Vector<String> nameVec = new Vector<>(names);
    nameVec.insertElementAt(NEW_ANNOTATION_NAME, 0);
    this.nameSelector.setModel(new DefaultComboBoxModel<>(nameVec));
    revalidate();
  }

  public void refresh() {
    Optional<StorageDelegate> storageDelegateOptional = StorageDelegateFactory.getDelegate(this.networkSUID);
    if (storageDelegateOptional.isPresent()) {
      StorageDelegate delegate = storageDelegateOptional.get();
      try {
        this.setAnnotations(delegate.getAllAnnotations());
      } catch(SQLException e) {
        e.printStackTrace();
      }
    } else {
      System.out.println("Create panel couldn't find storage delegate");
    }
    updateView();
  }

  public void refresh(Long suid) {
    this.networkSUID = suid;
    refresh();
  }

  public void setAnnotations(Map<String, Annotation> annotations) {
    this.annotations = annotations;
  }

  public void setAnnotations(Collection<Annotation> annotations) {
    System.out.println("Setting " + annotations.size() + " annotations");
    this.annotations = annotations
        .stream()
        .collect(Collectors.toMap(Annotation::getName, Function.identity()));
  }
}