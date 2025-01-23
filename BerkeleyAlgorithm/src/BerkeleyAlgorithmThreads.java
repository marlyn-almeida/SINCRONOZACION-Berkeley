import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BerkeleyAlgorithmThreads {
    private static final int NUM_NODES = 5; // Número de nodos en la simulación.
    private static final long MAX_OFFSET = 500000; // Desviación máxima permitida en milisegundos.

    public static void main(String[] args) {
        // Aseguramos que la interfaz gráfica se ejecute en el hilo de la EDT.
        SwingUtilities.invokeLater(() -> {
            BerkeleyAlgorithmThreads app = new BerkeleyAlgorithmThreads();
            app.runAlgorithm();
        });
    }

    private void runAlgorithm() {
        // Inicializamos los nodos con relojes desincronizados.
        List<Node> nodes = initializeNodes(NUM_NODES);

        // Configuramos la ventana principal.
        JFrame frame = new JFrame("Algoritmo de Berkeley - Sincronización de Relojes");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(null);

        // Etiqueta de título.
        JLabel titleLabel = new JLabel("Sincronización de Relojes (Algoritmo de Berkeley)", SwingConstants.CENTER);
        titleLabel.setBounds(50, 10, 500, 30);
        frame.add(titleLabel);

        // Tabla para mostrar los relojes.
        String[] columnNames = {"Nodo", "Reloj Actual"};
        JTable table = new JTable(new DefaultTableModel(columnNames, 0));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(50, 50, 500, 150);
        frame.add(scrollPane);

        // Botón para sincronizar relojes.
        JButton syncButton = new JButton("Sincronizar Relojes");
        syncButton.setBounds(200, 220, 200, 30);
        frame.add(syncButton);

        // Etiqueta para mostrar el tiempo promedio después de la sincronización.
        JLabel avgLabel = new JLabel("", SwingConstants.CENTER);
        avgLabel.setBounds(50, 260, 500, 30);
        frame.add(avgLabel);

        // Referencia final necesaria para las expresiones lambda.
        final List<Node> finalNodes = nodes;

        // Inicializamos la tabla con los datos iniciales.
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        updateTable(model, finalNodes);

        // Acción del botón para sincronizar relojes.
        syncButton.addActionListener(e -> {
            // Creamos nuevos offsets aleatorios para simular la desincronización.
            List<Node> updatedNodes = initializeNodes(NUM_NODES);

            // Sincronizamos los relojes.
            synchronizeClocks(updatedNodes);

            // Actualizamos la tabla con los nuevos datos.
            updateTable(model, updatedNodes);

            // Calculamos y mostramos el tiempo promedio.
            long averageTime = updatedNodes.stream().mapToLong(Node::getTime).sum() / updatedNodes.size();
            avgLabel.setText("Tiempo promedio sincronizado: " + formatTime(averageTime));
        });

        // Mostramos la ventana.
        frame.setVisible(true);
    }

    private List<Node> initializeNodes(int numNodes) {
        // Creamos y retornamos una lista de nodos con tiempos desincronizados.
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < numNodes; i++) {
            long offset = (long) (Math.random() * (2 * MAX_OFFSET) - MAX_OFFSET); // Generamos una desviación aleatoria.
            nodes.add(new Node(i, System.currentTimeMillis() + offset));
        }
        return nodes;
    }

    private void synchronizeClocks(List<Node> nodes) {
        // Iniciamos los hilos de cada nodo para calcular tiempos.
        nodes.forEach(Thread::start);

        // Esperamos que todos los hilos terminen.
        for (Node node : nodes) {
            try {
                node.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Calculamos el tiempo promedio para sincronizar los relojes.
        long averageTime = nodes.stream().mapToLong(Node::getTime).sum() / nodes.size();

        // Ajustamos cada reloj al tiempo promedio.
        nodes.forEach(node -> node.adjustTime(averageTime));
    }

    private void updateTable(DefaultTableModel model, List<Node> nodes) {
        // Limpiamos y actualizamos la tabla con los datos actuales.
        model.setRowCount(0);
        for (Node node : nodes) {
            model.addRow(new Object[]{node.getNodeId(), formatTime(node.getTime())});
        }
    }

    private String formatTime(long timeInMillis) {
        // Damos formato a los tiempos para mostrarlos en "HH:mm:ss.SSS".
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        return sdf.format(new Date(timeInMillis));
    }
}

class Node extends Thread {
    private final int nodeId; // Identificador del nodo.
    private long time; // Tiempo del reloj del nodo.

    public Node(int nodeId, long time) {
        this.nodeId = nodeId;
        this.time = time;
    }

    public int getNodeId() {
        return nodeId;
    }

    public long getTime() {
        return time;
    }

    public void adjustTime(long averageTime) {
        // Ajustamos el tiempo del nodo al tiempo promedio.
        long adjustment = averageTime - time; // Calculamos la diferencia.
        time += adjustment; // Actualizamos el tiempo.
    }

    @Override
    public void run() {
        // Simulamos el cálculo local del nodo.
        try {
            Thread.sleep(100); // Agregamos un retraso para simular el procesamiento.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
