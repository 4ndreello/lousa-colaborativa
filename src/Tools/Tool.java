package Tools;

import Connection.ServerConnection;
import Panels.DrawingPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public interface Tool {
    void onMousePressed(MouseEvent e, ServerConnection connection);
    void onMouseDragged(MouseEvent e, ServerConnection connection);
    void onMouseReleased(MouseEvent e, ServerConnection connection);

    // allows the tool to draw a temporary preview on the panel (e.g. while dragging a shape)
    void drawPreview(Graphics2D g);

    JComponent getOptionsPanel(DrawingPanel context);
    int getThickness();
}