package panels;

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
        // removed setPreferredSize to allow flexible resizing
        // removed setBorder to look better fullscreen

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
                mouseX = -100;
                mouseY = -100;
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                currentTool.onMouseDragged(e, connection);
                mouseX = e.getX();
                mouseY = e.getY();
                if (currentTool instanceof EraserTool) repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
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
                if (g2d != null) {
                    clearCanvas();
                    repaint();
                }
                return;
            }

            String[] parts = command.split(";");
            if (parts.length >= 8 && parts[0].equals("DRAW") && parts[1].equals("PENCIL")) {
                Color color = Color.decode(parts[2]);
                int thickness = Integer.parseInt(parts[3]);
                int x1 = Integer.parseInt(parts[4]);
                int y1 = Integer.parseInt(parts[5]);
                int x2 = Integer.parseInt(parts[6]);
                int y2 = Integer.parseInt(parts[7]);

                // ensure g2d exists before trying to draw from network
                if (g2d == null) {
                    // force canvas creation if it doesn't exist yet
                    ensureCanvasExists(Math.max(x1, x2) + 100, Math.max(y1, y2) + 100);
                }

                if (g2d != null) {
                    g2d.setColor(color);
                    g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2d.drawLine(x1, y1, x2, y2);
                    repaint();
                }
            }
        } catch (Exception e) {
            System.out.println("error processing command: " + command);
        }
    }

    private void clearCanvas() {
        if (canvas == null) return;
        Color originalColor = g2d.getColor();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g2d.setColor(originalColor);
    }

    // checks if canvas needs to grow to fit new window size
    private void ensureCanvasExists(int width, int height) {
        if (width <= 0 || height <= 0) return;

        if (canvas == null || width > canvas.getWidth() || height > canvas.getHeight()) {
            int newWidth = Math.max(width, canvas == null ? 1 : canvas.getWidth());
            int newHeight = Math.max(height, canvas == null ? 1 : canvas.getHeight());

            BufferedImage newCanvas = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D newG = newCanvas.createGraphics();
            newG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // fill new area with white
            newG.setColor(Color.WHITE);
            newG.fillRect(0, 0, newWidth, newHeight);

            // copy old data if exists
            if (canvas != null) {
                newG.drawImage(canvas, 0, 0, null);
            }

            // update references
            this.canvas = newCanvas;
            this.g2d = this.canvas.createGraphics();
            this.g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // dynamically grow canvas if window got bigger
        ensureCanvasExists(getWidth(), getHeight());

        g.drawImage(canvas, 0, 0, null);

        if (currentTool instanceof EraserTool && mouseX != -100) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
            int thickness = currentTool.getThickness();
            g2.drawOval(mouseX - thickness / 2, mouseY - thickness / 2, thickness, thickness);
            g2.dispose();
        }
    }
}