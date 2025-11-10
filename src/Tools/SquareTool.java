package Tools;

import Connection.ServerConnection;
import java.awt.*;

public class SquareTool extends RectangleTool {
    @Override
    protected String getCommandName() {
        return "RECT"; // server treats square as a rect with equal sides
    }

    @Override
    public void drawPreview(Graphics2D g) {
        // override preview to force square aspect ratio while dragging
        if (dragging) {
            g.setColor(color);
            g.setStroke(new BasicStroke(thickness));
            int size = Math.max(Math.abs(currentX - startX), Math.abs(currentY - startY));
            // determine top-left based on drag direction
            int x = currentX < startX ? startX - size : startX;
            int y = currentY < startY ? startY - size : startY;
            drawShape(g, x, y, size, size);
        }
    }

    // override mouse released to send square coordinates
    @Override
    public void onMouseReleased(java.awt.event.MouseEvent e, ServerConnection connection) {
        dragging = false;
        int size = Math.max(Math.abs(currentX - startX), Math.abs(currentY - startY));
        int x = currentX < startX ? startX - size : startX;
        int y = currentY < startY ? startY - size : startY;

        if (size > 0) {
            String hexColor = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
            String cmd = String.format("DRAW;RECT;%s;%d;%d;%d;%d;%d",
                    hexColor, thickness, x, y, size, size);
            connection.sendMessage(cmd);
        }
        e.getComponent().repaint();
    }
}