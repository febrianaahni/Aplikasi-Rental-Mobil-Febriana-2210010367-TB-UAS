package views;

import controllers.DatabaseHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.sql.*;

public class RentalMobilApp extends JFrame {
    private JPanel mainPanel;
    private JTextField fieldNama;
    private JComboBox<String> comboKategori;
    private JTextField fieldHarga;
    private JComboBox<String> comboLokasi;
    private JButton btnTambah;
    private JTable tabelMobil;
    private DefaultTableModel model;
    private JButton btnLaporanKategori;
    private JButton btnLaporanPendapatan;

    public RentalMobilApp() {
        initComponents();
        setContentPane(mainPanel);
        setTitle("Rental Mobil");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Setup tabel
        String[] kolom = {"ID", "Nama Mobil", "Kategori", "Harga/Hari", "Lokasi", "Edit", "Hapus"};
        model = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5 || column == 6; // Hanya kolom Edit dan Hapus yang bisa diklik
            }
        };
        tabelMobil.setModel(model);

        tabelMobil.getColumn("Edit").setCellRenderer(new ButtonRenderer("Edit"));
        tabelMobil.getColumn("Hapus").setCellRenderer(new ButtonRenderer("Hapus"));
        tabelMobil.getColumn("Edit").setCellEditor(new ButtonEditor(new JCheckBox(), "Edit"));
        tabelMobil.getColumn("Hapus").setCellEditor(new ButtonEditor(new JCheckBox(), "Hapus"));

        loadKategori();
        loadLokasi();
        loadData();

        btnTambah.addActionListener(e -> tambahMobil());
    }

    private void loadKategori() {
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nama_kategori FROM kategori")) {

            comboKategori.removeAllItems();
            while (rs.next()) {
                comboKategori.addItem(rs.getString("nama_kategori"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat kategori!");
        }
    }

    private void loadLokasi() {
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nama_lokasi FROM lokasi")) {

            comboLokasi.removeAllItems();
            while (rs.next()) {
                comboLokasi.addItem(rs.getString("nama_lokasi"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat lokasi!");
        }
    }

    private void loadData() {
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM mobil")) {

            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nama_mobil"),
                        rs.getString("kategori"),
                        rs.getInt("harga_per_hari"),
                        rs.getString("lokasi"),
                        "Edit",
                        "Hapus"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data mobil!");
        }
    }

    private void tambahMobil() {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO mobil (nama_mobil, kategori, harga_per_hari, lokasi) VALUES (?, ?, ?, ?)")) {

            stmt.setString(1, fieldNama.getText());
            stmt.setString(2, (String) comboKategori.getSelectedItem());
            stmt.setInt(3, Integer.parseInt(fieldHarga.getText()));
            stmt.setString(4, (String) comboLokasi.getSelectedItem());

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Mobil berhasil ditambahkan!");
            loadData();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menambahkan mobil!");
        }
    }

    private void editMobil(int id) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM mobil WHERE id = ?")) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Data lama
                String namaLama = rs.getString("nama_mobil");
                String kategoriLama = rs.getString("kategori");
                int hargaLama = rs.getInt("harga_per_hari");
                String lokasiLama = rs.getString("lokasi");

                // Form dialog untuk edit data
                JTextField fieldEditNama = new JTextField(namaLama, 20);
                JComboBox<String> comboEditKategori = new JComboBox<>();
                JComboBox<String> comboEditLokasi = new JComboBox<>();
                JTextField fieldEditHarga = new JTextField(String.valueOf(hargaLama), 10);

                // Load data kategori dan lokasi ke combo box
                loadComboBox(comboEditKategori, "SELECT nama_kategori FROM kategori", kategoriLama);
                loadComboBox(comboEditLokasi, "SELECT nama_lokasi FROM lokasi", lokasiLama);

                JPanel panelEdit = new JPanel();
                panelEdit.setLayout(new BoxLayout(panelEdit, BoxLayout.Y_AXIS));
                panelEdit.add(new JLabel("Nama Mobil:"));
                panelEdit.add(fieldEditNama);
                panelEdit.add(new JLabel("Kategori:"));
                panelEdit.add(comboEditKategori);
                panelEdit.add(new JLabel("Harga/Hari:"));
                panelEdit.add(fieldEditHarga);
                panelEdit.add(new JLabel("Lokasi:"));
                panelEdit.add(comboEditLokasi);

                int result = JOptionPane.showConfirmDialog(this, panelEdit, "Edit Mobil",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    // Update data
                    try (PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE mobil SET nama_mobil = ?, kategori = ?, harga_per_hari = ?, lokasi = ? WHERE id = ?")) {

                        updateStmt.setString(1, fieldEditNama.getText());
                        updateStmt.setString(2, (String) comboEditKategori.getSelectedItem());
                        updateStmt.setInt(3, Integer.parseInt(fieldEditHarga.getText()));
                        updateStmt.setString(4, (String) comboEditLokasi.getSelectedItem());
                        updateStmt.setInt(5, id);

                        updateStmt.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Data berhasil diperbarui!");
                        loadData();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memperbarui data!");
        }
    }

    private void hapusMobil(int id) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM mobil WHERE id = ?")) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Mobil berhasil dihapus!");
            loadData();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menghapus data!");
        }
    }

    private void loadComboBox(JComboBox<String> comboBox, String query, String selectedItem) {
        comboBox.removeAllItems();
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                comboBox.addItem(rs.getString(1));
            }
            comboBox.setSelectedItem(selectedItem);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data ke combo box!");
        }
    }

    private void initComponents() {
        mainPanel = new JPanel();
        fieldNama = new JTextField(20);
        comboKategori = new JComboBox<>();
        fieldHarga = new JTextField(10);
        comboLokasi = new JComboBox<>();
        btnTambah = new JButton("Tambah");
        tabelMobil = new JTable();
        btnLaporanKategori = new JButton("Laporan Kategori");
        btnLaporanPendapatan = new JButton("Laporan Pendapatan");

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(new JLabel("Nama Mobil:"));
        mainPanel.add(fieldNama);
        mainPanel.add(new JLabel("Kategori:"));
        mainPanel.add(comboKategori);
        mainPanel.add(new JLabel("Harga/Hari:"));
        mainPanel.add(fieldHarga);
        mainPanel.add(new JLabel("Lokasi:"));
        mainPanel.add(comboLokasi);
        mainPanel.add(btnTambah);
        mainPanel.add(btnLaporanKategori);
        mainPanel.add(btnLaporanPendapatan);
        mainPanel.add(new JScrollPane(tabelMobil));

        btnLaporanKategori.addActionListener(e -> laporanKategori());
        btnLaporanPendapatan.addActionListener(e -> laporanPendapatan());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RentalMobilApp().setVisible(true));
    }
    
    private void laporanKategori() {
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT kategori, COUNT(*) AS jumlah_mobil " +
                     "FROM mobil GROUP BY kategori")) {

            DefaultTableModel laporanModel = new DefaultTableModel(new String[]{"Kategori", "Jumlah Mobil"}, 0);
            while (rs.next()) {
                laporanModel.addRow(new Object[]{
                        rs.getString("kategori"),
                        rs.getInt("jumlah_mobil")
                });
            }

            JTable tabelLaporan = new JTable(laporanModel);
            JOptionPane.showMessageDialog(this, new JScrollPane(tabelLaporan), "Laporan Kategori", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat laporan kategori!");
        }
    }
    
    private void laporanPendapatan() {
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT m.nama_mobil, SUM(t.harga_per_hari) AS total_pendapatan " +
                     "FROM transaksi t " +
                     "INNER JOIN mobil m ON t.mobil_id = m.id " +
                     "GROUP BY m.nama_mobil")) {

            DefaultTableModel laporanModel = new DefaultTableModel(new String[]{"Nama Mobil", "Total Pendapatan"}, 0);
            while (rs.next()) {
                laporanModel.addRow(new Object[]{
                        rs.getString("nama_mobil"),
                        rs.getInt("total_pendapatan")
                });
            }

            JTable tabelLaporan = new JTable(laporanModel);
            JOptionPane.showMessageDialog(this, new JScrollPane(tabelLaporan), "Laporan Pendapatan", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat laporan pendapatan!");
        }
    }




    // Renderer untuk tombol
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer(String label) {
            setText(label);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // Editor untuk tombol
    class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private String label;
        private int id;

        public ButtonEditor(JCheckBox checkBox, String label) {
            super(checkBox);
            button = new JButton(label);
            button.addActionListener(e -> {
                int row = tabelMobil.getSelectedRow();
                id = (int) tabelMobil.getValueAt(row, 0);

                if (label.equals("Edit")) {
                    editMobil(id);
                } else if (label.equals("Hapus")) {
                    hapusMobil(id);
                }
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (String) value;
            button.setText(label);
            return button;
        }
    }
}
