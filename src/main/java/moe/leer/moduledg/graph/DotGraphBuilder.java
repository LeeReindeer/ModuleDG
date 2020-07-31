package moe.leer.moduledg.graph;

/**
 * @author leer
 * Created at 7/31/20 8:37 PM
 */
public class DotGraphBuilder extends AbstractTextBasedGraphBuilder {
  @Override
  protected String getHeader() {
    return "digraph { \n";
  }

  @Override
  protected String getFooter() {
    return "}\n";
  }

  @Override
  protected String getNodeDefinition(String name) {
    return String.format("\"%s\";\n", name);
  }

  @Override
  protected String getEdge(String sourceName, String destName) {
    return String.format("\"%s\" -> \"%s\";\n", sourceName, destName);
  }
}
