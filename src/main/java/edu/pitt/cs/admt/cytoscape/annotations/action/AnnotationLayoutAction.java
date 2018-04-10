package edu.pitt.cs.admt.cytoscape.annotations.action;

import static edu.pitt.cs.admt.cytoscape.annotations.task.AnnotationLayoutTask.CreateAnnotationLayoutTask;

import java.awt.event.ActionEvent;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.work.TaskManager;

/**
 * @author Mark Silvis (marksilvis@pitt.edu)
 */
public class AnnotationLayoutAction extends AbstractCyAction {

  private static final long serialVersionUID = 4125398496302851531L;

  private final CyApplicationManager applicationManager;
  private final AnnotationManager annotationManager;
  private final TaskManager taskManager;

  public AnnotationLayoutAction(final CyApplicationManager applicationManager, final AnnotationManager annotationManager, final TaskManager taskManager) {
    super("Annotation Relayout");
    this.setPreferredMenu("Layout");

    this.applicationManager = applicationManager;
    this.annotationManager = annotationManager;
    this.taskManager = taskManager;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    taskManager.execute(CreateAnnotationLayoutTask(this.annotationManager, this.applicationManager.getCurrentNetworkView()).toTaskIterator());
  }
}
