package Tools;
import Connection.ServerConnection;
import Panels.DrawingPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class PencilTool implements Tool {

    protected int lastX, lastY;
    private Color color = Color.BLACK;
    private int thickness = 2;

    @Override
    public void onMousePressed(MouseEvent e, ServerConnection connection) {
        lastX = e.getX();
        lastY = e.getY();
    }

    @Override
    public void onMouseDragged(MouseEvent e, ServerConnection connection) {
        int x = e.getX();
        int y = e.getY();

        // dynamic clamping
        Component c = e.getComponent();
        x = Math.max(0, Math.min(x, c.getWidth() - 1));
        y = Math.max(0, Math.min(y, c.getHeight() - 1));

        String hexColor = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        sendDrawCommand(lastX, lastY, x, y, hexColor, thickness, connection);

        lastX = x;
        lastY = y;
    }

    protected void sendDrawCommand(int x1, int y1, int x2, int y2, String hexColor, int thickness, ServerConnection connection) {
        String cmd = String.format("DRAW;PENCIL;%s;%d;%d;%d;%d;%d",
                hexColor, thickness, x1, y1, x2, y2);
        connection.sendMessage(cmd);
    }

    @Override
    public void onMouseReleased(MouseEvent e, ServerConnection connection) {}

    @Override
    public void drawPreview(Graphics2D g) {
        // pencil draws directly to server/canvas in real-time, no preview needed
    }

    @Override
    public int getThickness() { return thickness; }

    @Override
    public JComponent getOptionsPanel(DrawingPanel context) {
        JToolBar options = new JToolBar();
        options.setFloatable(false);
        options.add(new JLabel(" pencil config:  "));
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

    // getter for subclasses
    protected Color getColor() { return color; }
}