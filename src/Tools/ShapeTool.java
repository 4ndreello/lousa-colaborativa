package Tools;

import Connection.ServerConnection;
import Panels.DrawingPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public abstract class ShapeTool implements Tool {

    protected int startX, startY;
    protected int currentX, currentY;
    protected boolean dragging = false;

    protected Color color = Color.BLACK;
    protected int thickness = 2;

    // abstract method: specific shape tools must implement their own draw logic
    // commandName: the protocol name for this shape (e.g., "RECT", "TRIANGLE")
    protected abstract String getCommandName();
    protected abstract void drawShape(Graphics2D g2d, int x, int y, int w, int h);

    @Override
    public void onMousePressed(MouseEvent e, ServerConnection connection) {
        startX = e.getX();
        startY = e.getY();
        currentX = startX;
        currentY = startY;
        dragging = true;
    }

    @Override
    public void onMouseDragged(MouseEvent e, ServerConnection connection) {
        currentX = e.getX();
        currentY = e.getY();
        // request repaint to show preview while dragging
        e.getComponent().repaint();
    }

    @Override
    public void onMouseReleased(MouseEvent e, ServerConnection connection) {
        dragging = false;
        // send final command on release
        // normalize coordinates (start should be top-left)
        int x = Math.min(startX, currentX);
        int y = Math.min(startY, currentY);
        int w = Math.abs(currentX - startX);
        int h = Math.abs(currentY - startY);

        // avoid zero-size shapes
        if (w > 0 && h > 0) {
            String hexColor = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
            // format: DRAW;SHAPE_NAME;COLOR;THICKNESS;X;Y;W;H
            String cmd = String.format("DRAW;%s;%s;%d;%d;%d;%d;%d",
                    getCommandName(), hexColor, thickness, x, y, w, h);
            connection.sendMessage(cmd);
        }
        e.getComponent().repaint();
    }

    @Override
    public void drawPreview(Graphics2D g) {
        if (dragging) {
            g.setColor(color);
            g.setStroke(new BasicStroke(thickness));
            int x = Math.min(startX, currentX);
            int y = Math.min(startY, currentY);
            int w = Math.abs(currentX - startX);
            int h = Math.abs(currentY - startY);
            drawShape(g, x, y, w, h);
        }
    }

    @Override
    public int getThickness() { return thickness; }

    @Override
    public JComponent getOptionsPanel(DrawingPanel context) {
        // default options panel for shapes (similar to pencil)
        JToolBar options = new JToolBar();
        options.setFloatable(false);
        options.add(new JLabel(" " + getCommandName().toLowerCase() + " config:  "));

        options.add(createColorButton("⬛", Color.BLACK));
        options.add(createColorButton("🟥", Color.RED));
        options.add(createColorButton("🟦", Color.BLUE));
        options.add(createColorButton("🟩", Color.GREEN.darker()));
        options.addSeparator();

        options.add(new JLabel("thickness: "));
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 20, thickness);
        slider.setPreferredSize(new Dimension(100, 35));
        slider.addChangeListener(e -> this.thickness = slider.getValue());
        options.add(slider);
        return options;
    }

    protected JButton createColorButton(String text, Color c) {
        JButton btn = new JButton(text);
        btn.setForeground(c);
        btn.addActionListener(e -> this.color = c);
        return btn;
    }
}