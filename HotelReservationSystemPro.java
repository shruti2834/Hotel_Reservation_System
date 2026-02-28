import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class HotelReservationSystemPro extends JFrame {

    private static final String url = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String user = "root";
    private static final String password = "Your password ";

    private JTextField idField, guestField, roomField, contactField, searchField;
    private JTable table;
    private DefaultTableModel model;
    private JLabel statusLabel;
    private Connection connection;

    public HotelReservationSystemPro() {

        setTitle("🏨 Hotel Reservation Management System");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));

        // ===== MAIN PANEL =====
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 248, 255));
        add(mainPanel);

        // ===== FORM PANEL =====
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 15, 15));
        formPanel.setBorder(new TitledBorder("Reservation Details"));
        formPanel.setBackground(Color.WHITE);

        formPanel.add(new JLabel("Reservation ID:"));
        idField = new JTextField();
        formPanel.add(idField);

        formPanel.add(new JLabel("Guest Name:"));
        guestField = new JTextField();
        formPanel.add(guestField);

        formPanel.add(new JLabel("Room Number:"));
        roomField = new JTextField();
        formPanel.add(roomField);

        formPanel.add(new JLabel("Contact Number:"));
        contactField = new JTextField();
        formPanel.add(contactField);

        formPanel.add(new JLabel("Search Guest:"));
        searchField = new JTextField();
        formPanel.add(searchField);

        mainPanel.add(formPanel, BorderLayout.WEST);

        // ===== TABLE =====
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{
                "ID", "Guest", "Room", "Contact", "Date"
        });

        table = new JTable(model);
        table.setRowHeight(25);
        table.setSelectionBackground(new Color(173, 216, 230));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new TitledBorder("Reservation Records"));

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // ===== BUTTON PANEL =====
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        buttonPanel.setBackground(new Color(240, 248, 255));

        JButton addBtn = createButton("Add", new Color(40, 167, 69));
        JButton updateBtn = createButton("Update", new Color(23, 162, 184));
        JButton deleteBtn = createButton("Delete", new Color(220, 53, 69));
        JButton viewBtn = createButton("Refresh", new Color(108, 117, 125));
        JButton searchBtn = createButton("Search", new Color(255, 193, 7));
        JButton clearBtn = createButton("Clear", new Color(0, 123, 255));

        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(viewBtn);
        buttonPanel.add(searchBtn);
        buttonPanel.add(clearBtn);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // ===== STATUS BAR =====
        statusLabel = new JLabel(" Ready");
        statusLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        add(statusLabel, BorderLayout.NORTH);

        connectDatabase();
        viewReservations();

        // ===== ACTIONS =====
        addBtn.addActionListener(e -> addReservation());
        updateBtn.addActionListener(e -> updateReservation());
        deleteBtn.addActionListener(e -> deleteReservation());
        viewBtn.addActionListener(e -> viewReservations());
        searchBtn.addActionListener(e -> searchReservation());
        clearBtn.addActionListener(e -> clearFields());

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                idField.setText(model.getValueAt(row, 0).toString());
                guestField.setText(model.getValueAt(row, 1).toString());
                roomField.setText(model.getValueAt(row, 2).toString());
                contactField.setText(model.getValueAt(row, 3).toString());
            }
        });

        setVisible(true);
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    private void connectDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, password);
            statusLabel.setText(" Connected to Database");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database Connection Failed!");
        }
    }

    private void addReservation() {
        if (guestField.getText().isEmpty() || roomField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill required fields!");
            return;
        }

        try {
            String sql = "INSERT INTO reservation (guest_name, room_number, contact_number) VALUES (?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, guestField.getText());
            ps.setInt(2, Integer.parseInt(roomField.getText()));
            ps.setString(3, contactField.getText());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Reservation Added!");
            viewReservations();
            clearFields();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Adding Reservation!");
        }
    }

    private void viewReservations() {
        try {
            model.setRowCount(0);
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM reservation");
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("reservation_id"),
                        rs.getString("guest_name"),
                        rs.getInt("room_number"),
                        rs.getString("contact_number"),
                        rs.getTimestamp("reservation_date")
                });
            }
            statusLabel.setText(" Data Refreshed");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Loading Data!");
        }
    }

    private void updateReservation() {
        try {
            String sql = "UPDATE reservation SET guest_name=?, room_number=?, contact_number=? WHERE reservation_id=?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, guestField.getText());
            ps.setInt(2, Integer.parseInt(roomField.getText()));
            ps.setString(3, contactField.getText());
            ps.setInt(4, Integer.parseInt(idField.getText()));
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Updated Successfully!");
            viewReservations();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Update Failed!");
        }
    }

    private void deleteReservation() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String sql = "DELETE FROM reservation WHERE reservation_id=?";
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setInt(1, Integer.parseInt(idField.getText()));
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Deleted Successfully!");
                viewReservations();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Delete Failed!");
            }
        }
    }

    private void searchReservation() {
        try {
            model.setRowCount(0);
            String sql = "SELECT * FROM reservation WHERE guest_name LIKE ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, "%" + searchField.getText() + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("reservation_id"),
                        rs.getString("guest_name"),
                        rs.getInt("room_number"),
                        rs.getString("contact_number"),
                        rs.getTimestamp("reservation_date")
                });
            }
            statusLabel.setText(" Search Completed");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Search Failed!");
        }
    }

    private void clearFields() {
        idField.setText("");
        guestField.setText("");
        roomField.setText("");
        contactField.setText("");
        searchField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HotelReservationSystemPro::new);
    }
}
