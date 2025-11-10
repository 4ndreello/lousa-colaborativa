package Panels;

import Tools.EraserTool;
import Tools.PencilTool;
import Tools.Tool;
import Connection.ServerConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

public class DrawingPanel extends JPanel {

    private final ServerConnection connection;
    private BufferedImage canvas;
    private Graphics2D g2d;

    private Tool currentTool = new PencilTool();
    private int mouseX = -100;
    private int mouseY = -100;

    public DrawingPanel(ServerConnection connection) {
        this.connection = connection;
        setBackground(Color.WHITE);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                currentTool.onMousePressed(e, connection);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                currentTool.onMouseReleased(e, connection);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                mouseX = -100; mouseY = -100; repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                currentTool.onMouseDragged(e, connection);
                mouseX = e.getX(); mouseY = e.getY();
                // repaint needed for previews (shapes) and custom cursors (eraser)
                repaint();
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX(); mouseY = e.getY();
                if (currentTool instanceof EraserTool) repaint();
            }
        });
    }

    public void setCurrentTool(Tool tool) {
        this.currentTool = tool;
        if (tool instanceof EraserTool) {
            setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
                    new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank"));
        } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }
        repaint();
    }

    public void processCommand(String command) {
        try {
            if (command.startsWith("ACTION;CLEAR")) {
                if (g2d != null) { clearCanvas(); repaint(); }
                return;
            }

            String[] parts = command.split(";");
            if (parts.length < 2 || !parts[0].equals("DRAW")) return;

            String type = parts[1];
            // common parameters for most shapes
            Color color = Color.BLACK;
            int thickness = 1;
            if (parts.length > 3) {
                color = Color.decode(parts[2]);
                thickness = Integer.parseInt(parts[3]);
            }

            ensureGraphics();
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (type) {
                case "PENCIL":
                    if (parts.length >= 8) {
                        g2d.drawLine(Integer.parseInt(parts[4]), Integer.parseInt(parts[5]),
                                Integer.parseInt(parts[6]), Integer.parseInt(parts[7]));
                    }
                    break;
                case "RECT":
                    if (parts.length >= 8) {
                        g2d.drawRect(Integer.parseInt(parts[4]), Integer.parseInt(parts[5]),
                                Integer.parseInt(parts[6]), Integer.parseInt(parts[7]));
                    }
                    break;
                case "TRIANGLE":
                    if (parts.length >= 8) {
                        drawPolygonShape(parts, 3);
                    }
                    break;
                case "HEXAGON":
                    if (parts.length >= 8) {
                        drawPolygonShape(parts, 6);
                    }
                    break;
            }
            repaint();

        } catch (Exception e) {
            System.out.println("error processing command: " + command);
        }
    }

    // helper to draw polygons from bounding box received from server
    private void drawPolygonShape(String[] parts, int sides) {
        int x = Integer.parseInt(parts[4]);
        int y = Integer.parseInt(parts[5]);
        int w = Integer.parseInt(parts[6]);
        int h = Integer.parseInt(parts[7]);

        int[] xPoints = new int[sides];
        int[] yPoints = new int[sides];

        if (sides == 3) { // Triangle
            xPoints[0] = x + w / 2; yPoints[0] = y;
            xPoints[1] = x;         yPoints[1] = y + h;
            xPoints[2] = x + w;     yPoints[2] = y + h;
        } else if (sides == 6) { // Hexagon
            xPoints[0] = x + (int)(w * 0.25); yPoints[0] = y;
            xPoints[1] = x + (int)(w * 0.75); yPoints[1] = y;
            xPoints[2] = x + w;               yPoints[2] = y + h / 2;
            xPoints[3] = x + (int)(w * 0.75); yPoints[3] = y + h;
            xPoints[4] = x + (int)(w * 0.25); yPoints[4] = y + h;
            xPoints[5] = x;                   yPoints[5] = y + h / 2;
        }
        g2d.drawPolygon(xPoints, yPoints, sides);
    }

    private void clearCanvas() {
        if (canvas == null) return;
        Color originalColor = g2d.getColor();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g2d.setColor(originalColor);
    }

    private void ensureGraphics() {
        // ensures canvas exists before drawing from network commands
        ensureCanvasExists(getWidth(), getHeight());
        if (g2d == null && canvas != null) {
            g2d = canvas.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
    }

    private void ensureCanvasExists(int width, int height) {
        if (width <= 0 || height <= 0) width = 1; height = 1; // safety

        if (canvas == null || width > canvas.getWidth() || height > canvas.getHeight()) {
            int newWidth = Math.max(width, canvas == null ? 800 : canvas.getWidth());
            int newHeight = Math.max(height, canvas == null ? 600 : canvas.getHeight());

            BufferedImage newCanvas = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D newG = newCanvas.createGraphics();
            newG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            newG.setColor(Color.WHITE);
            newG.fillRect(0, 0, newWidth, newHeight);

            if (canvas != null) newG.drawImage(canvas, 0, 0, null);
            canvas = newCanvas;
            g2d = canvas.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ensureCanvasExists(getWidth(), getHeight());
        g.drawImage(canvas, 0, 0, null);

        // render tool preview (transient, not saved to canvas yet)
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        currentTool.drawPreview(g2);

        // render custom cursor if needed
        if (currentTool instanceof EraserTool && mouseX != -100) {
            g2.setColor(Color.BLACK);
            int t = currentTool.getThickness();
            g2.drawOval(mouseX - t / 2, mouseY - t / 2, t, t);
        }
        g2.dispose();
    }
}