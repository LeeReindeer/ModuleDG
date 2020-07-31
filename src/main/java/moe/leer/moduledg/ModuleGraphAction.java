package moe.leer.moduledg;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.graph.Graph;
import guru.nidi.graphviz.engine.Engine;
import moe.leer.moduledg.graph.DotGraphBuilder;

/**
 * @author leer
 * Created at 7/31/20 8:34 PM
 */
public class ModuleGraphAction extends AnAction {

  @Override
  public void actionPerformed(AnActionEvent e) {
    Project project = e.getData(PlatformDataKeys.PROJECT);
    if (project == null) {
      Messages.showMessageDialog(project, "No project loaded", "Information", Messages.getInformationIcon());
      return;
    }

    final Graph<Module> graph = ModuleManager.getInstance(project).moduleGraph();
    String dotStr = new DotGraphBuilder().buildGraph(graph);
    ModuleGraphController controller = ModuleGraphController.getInstance(e.getProject());

    if (controller != null) {
      controller.graphWindow.show(null);
      controller.startGenerateModuleGraphEvent(dotStr, Engine.CIRCO);
    } else {
      System.out.println("controller is null");
    }
  }
}
