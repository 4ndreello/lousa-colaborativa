package Tools;

import Connection.ServerConnection;
import Panels.DrawingPanel;

import javax.swing.JComponent;
import java.awt.event.MouseEvent;

public interface Tool {
    // handles mouse press events
    void onMousePressed(MouseEvent e, ServerConnection connection);

    // handles mouse drag events (actual drawing often happens here)
    void onMouseDragged(MouseEvent e, ServerConnection connection);

    // handles mouse release events
    void onMouseReleased(MouseEvent e, ServerConnection connection);

    // provides the ui configuration panel for this specific tool
    JComponent getOptionsPanel(DrawingPanel context);

    // returns the current thickness of the tool (useful for cursor rendering)
    int getThickness();
}