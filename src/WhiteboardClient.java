import Tools.*;
import Connection.ServerConnection;
import Panels.DrawingPanel;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import javax.swing.*;
import java.awt.*;

public class WhiteboardClient extends JFrame {

    private ServerConnection connection;
    private DrawingPanel drawingPanel;
    private JPanel configPanelContainer;

    public WhiteboardClient() {
        super("java interactive whiteboard v8 - fixed history");
        setSize(1280, 800);
        // Alterado para EXIT_ON_CLOSE padrão, mas podemos melhorar isso depois para fechar conexão limpo
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. CONECTA (mas ainda não escuta)
        try {
            connection = new ServerConnection("localhost", 12345, msg -> {
                // Agora isso é seguro, pois só vai começar a ouvir depois que drawingPanel existir
                drawingPanel.processCommand(msg);
            });
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "connection error: " + e.getMessage());
            System.exit(1);
        }

        // 2. CRIA A INTERFACE
        drawingPanel = new DrawingPanel(connection);
        add(drawingPanel, BorderLayout.CENTER);

        JPanel topContainer = new JPanel(new BorderLayout());
        JToolBar mainToolbar = new JToolBar();
        mainToolbar.setFloatable(false);
        mainToolbar.setBackground(new Color(230, 230, 230));

        mainToolbar.add(createToolButton("✏️", "pencil", new PencilTool()));
        mainToolbar.add(createToolButton("🧽", "eraser", new EraserTool()));
        mainToolbar.addSeparator();
        mainToolbar.add(createToolButton("⬜", "rectangle", new RectangleTool()));
        mainToolbar.add(createToolButton("🔳", "square", new SquareTool()));
        mainToolbar.add(createToolButton("🔺", "triangle", new TriangleTool()));
        mainToolbar.add(createToolButton("⬡", "hexagon", new HexagonTool()));

        mainToolbar.addSeparator();
        JButton btnClear = new JButton("🗑️ clear");
        btnClear.addActionListener(e -> connection.sendMessage("ACTION;CLEAR"));
        mainToolbar.add(btnClear);

        configPanelContainer = new JPanel(new BorderLayout());
        configPanelContainer.setBackground(new Color(245, 245, 245));

        topContainer.add(mainToolbar, BorderLayout.NORTH);
        topContainer.add(configPanelContainer, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        selectTool(new PencilTool());

        // 3. TUDO PRONTO? AGORA SIM, COMEÇA A OUVIR O HISTÓRICO!
        connection.startListening();
    }

    private JButton createToolButton(String icon, String tooltip, Tool tool) {
        JButton btn = new JButton(icon);
        btn.setToolTipText(tooltip);
        btn.addActionListener(e -> selectTool(tool));
        return btn;
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