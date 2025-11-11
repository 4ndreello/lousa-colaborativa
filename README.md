## Documento Técnico: Frontend da Lousa Colaborativa

### 1. Visão Geral

Este documento detalha a arquitetura da aplicação cliente (frontend) da Lousa Colaborativa. O frontend é uma aplicação de desktop Java Swing que serve como interface gráfica para o servidor de backend.

A aplicação tem duas responsabilidades principais:
1.  **Capturar e Enviar:** Capturar as interações do utilizador (cliques, arrastar do rato, seleção de ferramentas), traduzi-las para o protocolo de mensagens de texto (`DRAW;...`) e enviá-las ao servidor.
2.  **Receber e Renderizar:** Ouvir continuamente o servidor e, ao receber uma mensagem de protocolo, renderizar a ação correspondente (um traço, uma forma, etc.) na tela.

A interface é projetada para ser responsiva, com a área de desenho a preencher o espaço disponível, e contextual, onde as opções de ferramentas mudam dinamicamente.

---

### 2. Arquitetura e Componentes

A arquitetura do frontend é modular e segue o **Padrão de Design Strategy** (Estratégia) para o gerenciamento de ferramentas, o que a torna altamente extensível.

* **Contexto (Strategy):** `Panels.DrawingPanel`.
* **Estratégia (Interface):** `Tools.Tool`.
* **Estratégias Concretas:** `Tools.PencilTool`, `Tools.EraserTool`, `Tools.ShapeTool`, etc.

Esta abordagem permite que o `DrawingPanel` delegue todo o comportamento de desenho para a "ferramenta" ativa, sem precisar saber como essa ferramenta funciona internamente.

#### Estrutura de Pacotes
O código está organizado nos seguintes pacotes:
* **`Connection/`**: Contém `ServerConnection.java`, responsável pela comunicação de rede TCP.
* **`Panels/`**: Contém `DrawingPanel.java`, o "canvas" onde o desenho ocorre.
* **`Tools/`**: Contém a interface `Tool` e todas as implementações concretas (Lápis, Borracha, Formas).
* **`(default package)`**: Contém `WhiteboardClient.java`, o ponto de entrada da aplicação (`main`).

---

### 3. Análise Detalhada dos Componentes

#### 3.1. `WhiteboardClient.java` (O Montador da UI)
Esta é a classe principal (`JFrame`) que monta o layout da aplicação.
* **Layout:** Utiliza um `BorderLayout`. O `DrawingPanel` é colocado no `CENTER`, permitindo que se redimensione. Um `topContainer` é colocado no `NORTH`.
* **Toolbars (Barras de Ferramentas):**
    * `mainToolbar`: Uma barra fixa com botões para selecionar ferramentas (Lápis, Borracha, Retângulo, Quadrado, Triângulo, Hexágono) e Limpar.
    * `configPanelContainer`: Uma barra dinâmica que exibe as opções da ferramenta atualmente selecionada.
* **Lógica de UI Contextual (`selectTool(Tool tool)`):** Este método é central para a arquitetura da UI. Ele é chamado sempre que um botão de ferramenta é clicado.
    1.  Define a ferramenta atual no `drawingPanel`.
    2.  Limpa o `configPanelContainer`.
    3.  Chama `tool.getOptionsPanel(drawingPanel)`, pedindo à própria ferramenta que forneça seu painel de opções customizado.
    4.  Adiciona o painel retornado ao `configPanelContainer`.
* **Sincronização de Histórico:** A conexão (`ServerConnection`) é instanciada primeiro, depois toda a UI é construída e, só no final do construtor, `connection.startListening()` é chamado. Isso previne uma "condição de corrida" (race condition), garantindo que o `drawingPanel` exista antes de qualquer mensagem do histórico ser recebida.

#### 3.2. `Connection.ServerConnection.java`
Esta classe abstrai a rede, agindo como o "telefone" para o servidor.
* **Callback:** Recebe um `Consumer<String> onMessageReceived` no construtor. Esta é a função (definida no `WhiteboardClient`) que será executada para cada mensagem recebida.
* **Thread de Escuta:** O método `startListening()` inicia uma nova `Thread` que bloqueia em `input.readLine()`. Quando uma linha (mensagem) chega, ela é passada para o callback.
* **Envio:** `sendMessage(String msg)` escreve a string no `PrintWriter`.

#### 3.3. `Panels.DrawingPanel.java` (O Canvas)
Este é o componente central da aplicação, estendendo `JPanel`.
* **Estado:** Armazena o `BufferedImage canvas` (a imagem permanente onde todos desenham) e a `Tool currentTool` (a ferramenta ativa).
* **Delegação de Eventos (Strategy Pattern):** Os `MouseListener` e `MouseMotionListener` não contêm lógica de desenho. Eles simplesmente delegam o evento para a ferramenta ativa: ex. `currentTool.onMousePressed(e, connection)`.
* **Renderização (`paintComponent`):** Este método é chamado pelo Swing sempre que o painel precisa ser redesenhado (ex: ao arrastar o rato ou receber uma mensagem).
    1.  **Canvas Permanente:** Desenha o `BufferedImage canvas` (o desenho já feito).
    2.  **Preview Temporário:** Chama `currentTool.drawPreview(g2)`. Isso permite que ferramentas de forma (como `ShapeTool`) desenhem uma pré-visualização "fantasma" enquanto o utilizador arrasta o rato, sem salvar no canvas permanente.
    3.  **Cursor Customizado:** Se a ferramenta for `EraserTool`, desenha um círculo vazio no local do rato para indicar o tamanho da borracha.
* **Receção de Comandos (`processCommand`):** Este método é o "receptor" do `ServerConnection`. Ele recebe a string de comando, faz o *parse* (divide em `;`), e usa `switch (type)` para determinar o que desenhar. Ele então modifica diretamente o `g2d` (o `Graphics` do `canvas` permanente) com métodos como `g2d.drawLine`, `g2d.drawRect`, ou o `drawPolygonShape`.
* **Design Responsivo (`ensureCanvasExists`):** Se a janela for redimensionada, este método cria uma nova `BufferedImage` maior e copia o conteúdo da imagem antiga para a nova, garantindo que o desenho não se perca.

#### 3.4. `Tools/` (A Arquitetura de Ferramentas)
* **`Tool.java` (Interface):** Define o contrato que todas as ferramentas devem seguir: métodos de rato (`onMousePressed`, `onMouseDragged`, `onMouseReleased`), um método para desenhar pré-visualizações (`drawPreview`), um método para fornecer a UI de opções (`getOptionsPanel`) e um método para obter a espessura (`getThickness`).
* **`PencilTool.java`:** Implementação do Lápis. Armazena o seu próprio estado (cor, espessura). O seu `onMouseDragged` envia *continuamente* comandos `DRAW;PENCIL;...`. O seu `getOptionsPanel` constrói e retorna uma `JToolBar` com seletores de cor e um slider de espessura.
* **`EraserTool.java`:** Herda de `PencilTool`. Reutiliza a maior parte da lógica, mas sobrescreve `onMouseDragged` para forçar a cor `#ffffff` (branco) e usar uma espessura própria. O seu `getOptionsPanel` retorna apenas um slider de tamanho.
* **`ShapeTool.java` (Classe Abstrata):** Uma classe base elegante para todas as ferramentas de formas "clica-arrasta-solta".
    * `onMousePressed`: Guarda o ponto inicial (`startX`, `startY`).
    * `onMouseDragged`: Atualiza o ponto final (`currentX`, `currentY`) e chama `e.getComponent().repaint()` para acionar o `drawPreview`.
    * `onMouseReleased`: Calcula a caixa delimitadora final (`x,y,w,h`) e envia **um único** comando `DRAW;[NOME_DA_FORMA];...`.
* **Implementações de Forma (`RectangleTool`, `SquareTool`, `TriangleTool`, `HexagonTool`):**
    * Heradam de `ShapeTool`.
    * São extremamente simples, precisando apenas de sobrescrever `getCommandName()` (ex: "RECT") e `drawShape()` (ex: `g2d.drawRect(x,y,w,h)`).
    * `SquareTool` é uma especialização de `RectangleTool` que sobrescreve `onMouseReleased` e `drawPreview` para forçar um rácio de aspeto 1:1 (quadrado).

---

### 4. Formato das Mensagens (Perspetiva do Cliente)

#### Mensagens Enviadas (para o Servidor)
* `DRAW;PENCIL;[HEX];[THICK];[X1];[Y1];[X2];[Y2]` (Enviado em `PencilTool.onMouseDragged` e `EraserTool.onMouseDragged`).
* `DRAW;RECT;[HEX];[THICK];[X];[Y];[W];[H]` (Enviado em `ShapeTool.onMouseReleased`, usado por `RectangleTool` e `SquareTool`).
* `DRAW;TRIANGLE;[HEX];[THICK];[X];[Y];[W];[H]` (Enviado em `ShapeTool.onMouseReleased`, usado por `TriangleTool`).
* `DRAW;HEXAGON;[HEX];[THICK];[X];[Y];[W];[H]` (Enviado em `ShapeTool.onMouseReleased`, usado por `HexagonTool`).
* `ACTION;CLEAR` (Enviado por `WhiteboardClient` ao clicar no botão "clear").

#### Mensagens Recebidas (do Servidor)
O `DrawingPanel.processCommand` está preparado para receber exatamente os mesmos formatos que o cliente envia (`PENCIL`, `RECT`, `TRIANGLE`, `HEXAGON`, `ACTION;CLEAR`), pois o servidor apenas retransmite as mensagens válidas que recebe.

---

### 5. Requisitos e Procedimentos

* **Requisitos:** Java JRE 21 ou superior (conforme `misc.xml`). Acesso de rede (TCP) ao endereço e porta do servidor.
* **Execução:**
    1.  Compilar todo o projeto.
    2.  Garantir que o Backend está a correr e acessível.
    3.  **Configurar Conexão:** O endereço do servidor está "hardcoded" (fixo no código) em `WhiteboardClient.java` para `localhost` e porta `12345`. É necessário alterar esta linha para conectar a um servidor na nuvem.
    4.  Executar a classe principal `WhiteboardClient`.
