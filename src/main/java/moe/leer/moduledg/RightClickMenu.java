package moe.leer.moduledg;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.ui.JBColor;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Copy from https://github.com/antlr/intellij-plugin-v4/blob/master/src/main/java/org/antlr/intellij/plugin/preview/ParseTreeContextualMenu.java
 */
public class RightClickMenu {
  static void showPopupMenu(ModuleGraphController controller, MouseEvent event) {
    JPopupMenu menu = new JPopupMenu();

    menu.add(createExportMenuItem(controller.getGraphPanel(), controller.getImage(), "Export to image (white background)", false));
//    menu.add(createExportMenuItem(imagePanel, image, "Export to image (transparent background)", true));
    menu.add(createDotStringMenuItem(controller.getDotStr(), "Export to DOT"));

    menu.show(controller.getGraphPanel(), event.getX(), event.getY());
  }

  private static JMenuItem createExportMenuItem(JPanel imagePanel, BufferedImage image, String label, boolean useTransparentBackground) {
    JMenuItem item = new JMenuItem(label);
    boolean isMacNativeSaveDialog = SystemInfo.isMac && Registry.is("ide.mac.native.save.dialog");

    item.addActionListener(event -> {
      String[] extensions = useTransparentBackground ? new String[]{"png", "svg"} : new String[]{"png", "jpg", "svg"};
      FileSaverDescriptor descriptor = new FileSaverDescriptor("Export Image to", "Choose the destination file", extensions);
      FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, (Project) null);

      String fileName = "graph" + (isMacNativeSaveDialog ? ".png" : "");
      VirtualFileWrapper vf = dialog.save(null, fileName);

      if (vf == null) {
        return;
      }

      File file = vf.getFile();
      String imageFormat = FileUtilRt.getExtension(file.getName());
      if (StringUtils.isBlank(imageFormat)) {
        imageFormat = "png";
      }

      if ("svg".equals(imageFormat)) {
        exportToSvg(imagePanel, image, file, useTransparentBackground);
      } else {
        exportToImage(imagePanel, image, file, useTransparentBackground, imageFormat);
      }
    });

    return item;
  }

  private static JMenuItem createDotStringMenuItem(String dot, String label) {
    JMenuItem item = new JMenuItem(label);
    item.addActionListener(event -> {
      Messages.showInfoMessage(dot, "DOT");
    });
    return item;
  }

  private static void exportToImage(JPanel imagePanel, BufferedImage image, File file, boolean useTransparentBackground, String imageFormat) {
//    int imageType = useTransparentBackground ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
//    BufferedImage bi = UIUtil.createImage(imagePanel.getWidth(), imagePanel.getHeight(), imageType);
//    Graphics graphics = bi.getGraphics();

//    if (!useTransparentBackground) {
//      graphics.setColor(JBColor.WHITE);
//      graphics.fillRect(0, 0, imagePanel.getWidth(), imagePanel.getHeight());
//    }
//    imagePanel.paint(graphics);

    try {
      if (!ImageIO.write(image, imageFormat, file)) {
        Notification notification = new Notification(
            "CodeFlow export",
            "Error while exporting parse tree to file " + file.getAbsolutePath(),
            "unknown format '" + imageFormat + "'?",
            NotificationType.WARNING
        );
        Notifications.Bus.notify(notification);
      }
    } catch (IOException e) {
      Logger.getInstance(RightClickMenu.class)
          .error("Error while exporting flowchart to file " + file.getAbsolutePath(), e);
    }
  }

  private static void exportToSvg(JPanel imagePanel, BufferedImage image, File file, boolean useTransparentBackground) {
    DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
    Document document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
    SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

    if (!useTransparentBackground) {
      svgGenerator.setColor(JBColor.WHITE);
//      svgGenerator.fillRect(0, 0, imagePanel.getWidth(), imagePanel.getHeight());
      svgGenerator.fillRect(0, 0, image.getWidth(), image.getHeight());
    }
    svgGenerator.drawImage(image, null, null);
//    imagePanel.paint(svgGenerator);
    try {
      svgGenerator.stream(file.getAbsolutePath(), true);
    } catch (SVGGraphics2DIOException e) {
      Logger.getInstance(RightClickMenu.class)
          .error("Error while exporting flowchart to SVG file " + file.getAbsolutePath(), e);
    }
  }

}
