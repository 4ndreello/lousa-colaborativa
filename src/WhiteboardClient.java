import Tools.EraserTool;
import Tools.PencilTool;
import Tools.Tool;
import Connection.ServerConnection;
import panels.DrawingPanel;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class WhiteboardClient extends JFrame {

    private ServerConnection connection;
    private DrawingPanel drawingPanel;
    private JPanel configPanelContainer;

    public WhiteboardClient() {
        super("java interactive whiteboard v6 - responsive");
        // started bigger to look good on modern screens
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            connection = new ServerConnection("localhost", 12345, msg -> {
                if (drawingPanel != null) drawingPanel.processCommand(msg);
            });
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "connection error: " + e.getMessage());
            System.exit(1);
        }

        drawingPanel = new DrawingPanel(connection);

        // === layout setup ===
        // removed the background panel. drawingPanel now sits directly in the center,
        // which makes standard borderlayout stretch it to fill available space.
        add(drawingPanel, BorderLayout.CENTER);

        JPanel topContainer = new JPanel(new BorderLayout());

        // 1. main tools toolbar
        JToolBar mainToolbar = new JToolBar();
        mainToolbar.setFloatable(false);
        mainToolbar.setBackground(new Color(230, 230, 230));

        JButton btnPencil = new JButton("✏️ pencil");
        btnPencil.addActionListener(e -> selectTool(new PencilTool()));
        mainToolbar.add(btnPencil);

        JButton btnEraser = new JButton("🧽 eraser");
        btnEraser.addActionListener(e -> selectTool(new EraserTool()));
        mainToolbar.add(btnEraser);

        mainToolbar.addSeparator();
        JButton btnClear = new JButton("🗑️ clear all");
        btnClear.addActionListener(e -> connection.sendMessage("ACTION;CLEAR"));
        mainToolbar.add(btnClear);

        // 2. dynamic config toolbar
        configPanelContainer = new JPanel(new BorderLayout());
        configPanelContainer.setBackground(new Color(245, 245, 245));

        topContainer.add(mainToolbar, BorderLayout.NORTH);
        topContainer.add(configPanelContainer, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        // initialize default tool
        selectTool(new PencilTool());
    }

    private void selectTool(Tool tool) {
        drawingPanel.setCurrentTool(tool);
        configPanelContainer.removeAll();
        configPanelContainer.add(tool.getOptionsPanel(drawingPanel), BorderLayout.CENTER);
        configPanelContainer.revalidate();
        configPanelContainer.repaint();
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new WhiteboardClient().setVisible(true));
    }
}