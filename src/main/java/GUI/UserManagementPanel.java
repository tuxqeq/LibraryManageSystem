package GUI;

import LibraryEntities.User;
import dao.Dao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * A panel for managing {@link User} entities (adding, editing, deleting).
 */
public class UserManagementPanel extends JPanel {

    /**
     * DAO for User entities.
     */
    private final Dao<User> userDao;

    /**
     * Table to display users.
     */
    private JTable userTable;

    /**
     * Constructs a new panel for managing users.
     *
     * @param userDao The DAO for user operations.
     */
    public UserManagementPanel(Dao<User> userDao) {
        this.userDao = userDao;
        setLayout(new BorderLayout());
        initComponents();
    }

    /**
     * Initializes the user table and action buttons.
     */
    private void initComponents() {
        userTable = new JTable(new DefaultTableModel(new Object[]{"ID", "Name", "Email", "Phone", "Address"}, 0));
        add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel userActions = new JPanel(new FlowLayout());
        JButton addUserButton = new JButton("Add User");
        JButton editUserButton = new JButton("Edit User");
        JButton deleteUserButton = new JButton("Delete User");

        userActions.add(addUserButton);
        userActions.add(editUserButton);
        userActions.add(deleteUserButton);
        add(userActions, BorderLayout.SOUTH);

        addUserButton.addActionListener(e -> addUser());
        editUserButton.addActionListener(e -> editUser());
        deleteUserButton.addActionListener(e -> deleteUser());
    }

    /**
     * Loads all User entities into the table.
     */
    public void loadUsers() {
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        model.setRowCount(0);
        List<User> users = userDao.findAll();
        for (User user : users) {
            model.addRow(new Object[]{
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getAddress()
            });
        }
    }

    /**
     * Opens a dialog to add a new User.
     */
    private void addUser() {
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField addressField = new JTextField();

        Object[] fields = {
                "Name:", nameField,
                "Email:", emailField,
                "Phone:", phoneField,
                "Address:", addressField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Add User", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            User newUser = new User(
                    nameField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    addressField.getText());
            userDao.create(newUser);
            loadUsers();
        }
    }

    /**
     * Opens a dialog to edit the selected User's fields.
     */
    private void editUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to edit.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Long userId = Long.valueOf(userTable.getValueAt(selectedRow, 0).toString());
        User user = userDao.findById(userId);

        JTextField nameField = new JTextField(user.getName());
        JTextField emailField = new JTextField(user.getEmail());
        JTextField phoneField = new JTextField(user.getPhoneNumber());
        JTextField addressField = new JTextField(user.getAddress());

        Object[] fields = {
                "Name:", nameField,
                "Email:", emailField,
                "Phone:", phoneField,
                "Address:", addressField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Edit User", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            user.setName(nameField.getText());
            user.setEmail(emailField.getText());
            user.setPhoneNumber(phoneField.getText());
            user.setAddress(addressField.getText());
            userDao.update(user);
            loadUsers();
        }
    }

    /**
     * Deletes the currently selected User.
     */
    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to delete.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Long userId = Long.valueOf(userTable.getValueAt(selectedRow, 0).toString());
        try{
            userDao.delete(userId);
        }catch (Exception e){
            JOptionPane.showMessageDialog(this,
                    "Cannot delete user with active borrowings.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        loadUsers();
    }
}