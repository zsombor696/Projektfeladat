import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class App extends JFrame {

    private static final String FILE_NAME = "yacht_koltsegek_2024.csv";

    private DefaultTableModel tableModel;
    private JTable table;

    private JTextField yachtNameField;
    private JTextField dateField;
    private JComboBox<String> categoryCombo;
    private JTextField amountField;
    private JTextField noteField;

    private int nextId = 1;  // automatikus ID

    private final String[] categories = {"Maintenance", "Repairs", "Insurance", "Docking Fees", "Other"};

    public App() {
        super("Oceanic Dreams Yacht Költség Adminisztráció");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // Táblázat model és létrehozás
        String[] columns = {"ID", "Yacht Name", "Date", "Category", "Amount (€)", "Note"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false; // csak olvasható
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // Új adat felvételi panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 6, 10, 10));
        yachtNameField = new JTextField();
        dateField = new JTextField("yyyy-MM-dd");
        categoryCombo = new JComboBox<>(categories);
        amountField = new JTextField();
        noteField = new JTextField();

        inputPanel.add(new JLabel("Yacht Name"));
        inputPanel.add(new JLabel("Date (yyyy-MM-dd)"));
        inputPanel.add(new JLabel("Category"));
        inputPanel.add(new JLabel("Amount (€)"));
        inputPanel.add(new JLabel("Note"));
        inputPanel.add(new JLabel("")); // üres hely a gombnak

        inputPanel.add(yachtNameField);
        inputPanel.add(dateField);
        inputPanel.add(categoryCombo);
        inputPanel.add(amountField);
        inputPanel.add(noteField);

        JButton addButton = new JButton("Hozzáadás");
        inputPanel.add(addButton);

        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        // Adatok betöltése
        loadData();

        // Hozzáadás gomb eseménykezelő
        addButton.addActionListener(e -> addNewRecord());

        setVisible(true);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        Path path = Paths.get(FILE_NAME);
        if (!Files.exists(path)) {
            // ha nincs fájl, létrehoz egy üres fejlécet
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write("id;yachtname;datum;kategoria;osszeg;megjegyzes\n");
            } catch (IOException ex) {
                showError("Hiba a fájl létrehozásakor: " + ex.getMessage());
            }
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // fejléc, kihagyjuk
            int maxId = 0;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";", -1);
                if (parts.length < 6) continue;

                int id = Integer.parseInt(parts[0]);
                if (id > maxId) maxId = id;

                String yachtName = parts[1];
                String date = parts[2];
                String category = parts[3];
                String amount = parts[4];
                String note = parts[5];

                tableModel.addRow(new Object[]{id, yachtName, date, category, amount, note});
            }
            nextId = maxId + 1;

        } catch (IOException | NumberFormatException ex) {
            showError("Hiba az adatok betöltésekor: " + ex.getMessage());
        }
    }

    private void addNewRecord() {
        String yachtName = yachtNameField.getText().trim();
        String dateStr = dateField.getText().trim();
        String category = (String) categoryCombo.getSelectedItem();
        String amountStr = amountField.getText().trim();
        String note = noteField.getText().trim();

        // Egyszerű validáció
        if (yachtName.isEmpty() || dateStr.isEmpty() || amountStr.isEmpty()) {
            showError("Kérlek töltsd ki a kötelező mezőket (Yacht Name, Date, Amount)!");
            return;
        }

        // Dátum ellenőrzés
        if (!isValidDate(dateStr)) {
            showError("A dátum formátuma nem megfelelő! (yyyy-MM-dd)");
            return;
        }

        // Összeg ellenőrzés
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError("Az összegnek pozitív számnak kell lennie.");
            return;
        }

        // Új sor hozzáadása a modellhez
        Object[] row = {nextId, yachtName, dateStr, category, String.format("%.2f", amount), note};
        tableModel.addRow(row);

        // CSV fájl frissítése
        writeAllDataToFile();

        nextId++;

        // Űrlap törlése
        yachtNameField.setText("");
        dateField.setText("yyyy-MM-dd");
        categoryCombo.setSelectedIndex(0);
        amountField.setText("");
        noteField.setText("");
    }

    private void writeAllDataToFile() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_NAME), StandardCharsets.UTF_8)) {
            // Fejléc
            writer.write("id;yachtname;datum;kategoria;osszeg;megjegyzes\n");

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    sb.append(tableModel.getValueAt(i, j));
                    if (j < tableModel.getColumnCount() - 1) {
                        sb.append(";");
                    }
                }
                sb.append("\n");
                writer.write(sb.toString());
            }
        } catch (IOException ex) {
            showError("Hiba a fájl mentésekor: " + ex.getMessage());
        }
    }

    private boolean isValidDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Hiba", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::new);
    }
}
