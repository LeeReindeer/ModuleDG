package moe.leer.moduledg;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import guru.nidi.graphviz.engine.Engine;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;

/**
 * @author leer
 * Created at 12/31/19 2:03 PM
 */
public class ModuleGraphController implements ProjectComponent {

  public static final Logger LOG = Logger.getInstance(ModuleGraphController.class);

  public Project project;
  public static final String FLOWCHART_WIN_ID = "Module Graph";
  public ToolWindow graphWindow;

  private ScrollScaleImagePanel graphPanel;
  private BufferedImage image;
  private String dotStr;

  public ModuleGraphController(Project project) {
    this.project = project;
  }

  public String getDotStr() {
    return dotStr;
  }

  public BufferedImage getImage() {
    return image;
  }

  public ScrollScaleImagePanel getGraphPanel() {
    return graphPanel;
  }

  @Nullable
  public static ModuleGraphController getInstance(Project project) {
    if (project == null) {
      LOG.error("getInstance: project is null");
      return null;
    }
    ModuleGraphController pc = project.getComponent(ModuleGraphController.class);
    if (pc == null) {
      LOG.error("getInstance: getComponent() for " + project.getName() + " returns null");
    }
    return pc;
  }

  @Override
  public void projectOpened() {
    createToolWindows();
  }

  @Override
  public void projectClosed() {
    graphPanel = null;
    graphWindow = null;
    project = null;
  }

  @NotNull
  @Override
  public String getComponentName() {
    return "moduledg.ProjectComponent";
  }

  private void createToolWindows() {
    LOG.info("createToolWindows " + project.getName());
    ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
    graphPanel = new ScrollScaleImagePanel(this);

    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(graphPanel, "", false);
    content.setCloseable(false);

    graphWindow = toolWindowManager.registerToolWindow(FLOWCHART_WIN_ID, true, ToolWindowAnchor.RIGHT);
    graphWindow.getContentManager().addContent(content);
//    flowchartWindow.setIcon(Icons.getToolWindow());
  }

  public void startGenerateModuleGraphEvent(String dotStr, Engine engine) {
    this.dotStr = dotStr;
    if (graphPanel != null) {
      this.image = Graphviz.fromString(dotStr).engine(engine).render(Format.PNG).toImage();
      graphPanel.setImage(this.image);
    }
  }
}
