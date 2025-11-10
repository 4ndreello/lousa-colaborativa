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

        // dynamically clamp to current component size
        Component c = e.getComponent();
        int maxWidth = c.getWidth();
        int maxHeight = c.getHeight();

        x = Math.max(0, Math.min(x, maxWidth - 1));
        y = Math.max(0, Math.min(y, maxHeight - 1));

        String hexColor = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());

        // use helper method to send command
        sendDrawCommand(lastX, lastY, x, y, hexColor, thickness, connection);

        lastX = x;
        lastY = y;
    }

    // protected helper to allow subclasses (like eraser) to easily send commands
    protected void sendDrawCommand(int x1, int y1, int x2, int y2, String hexColor, int thickness, ServerConnection connection) {
        String cmd = String.format("DRAW;PENCIL;%s;%d;%d;%d;%d;%d",
                hexColor, thickness, x1, y1, x2, y2);
        connection.sendMessage(cmd);
    }

    @Override
    public void onMouseReleased(MouseEvent e, ServerConnection connection) {
    }

    @Override
    public int getThickness() {
        return thickness;
    }

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
        slider.setPreferredSize(new Dimension(100, 20));
        slider.addChangeListener(e -> this.thickness = slider.getValue());
        options.add(slider);

        return options;
    }

    private JButton createColorButton(String text, Color c) {
        JButton btn = new JButton(text);
        btn.setForeground(c);
        btn.addActionListener(e -> this.color = c);
        return btn;
    }
}