package edu.pharmacy.desktop;

import edu.pharmacy.dao.MedicineDAO;
import edu.pharmacy.model.Medicine;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.List;

/** Small AWT utility for a quick medicine price lookup. */
public class QuickPriceChecker extends Frame {
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final TextField medicineField = new TextField(20);
    private final Button checkButton = new Button("Check");
    private final Label resultLabel = new Label("Enter a medicine name", Label.CENTER);

    public QuickPriceChecker() {
        super("Quick Price Checker");
        setLayout(new BorderLayout(10, 10));

        Panel inputPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        inputPanel.add(new Label("Medicine:"));
        inputPanel.add(medicineField);
        inputPanel.add(checkButton);

        add(inputPanel, BorderLayout.NORTH);
        add(resultLabel, BorderLayout.CENTER);

        checkButton.addActionListener(this::checkClicked);
        medicineField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    checkClicked(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "enter"));
                }
            }
        });
        medicineField.addTextListener(event -> {
            String text = medicineField.getText().trim();
            if (text.length() >= 2) {
                lookUpPrice(text);
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                dispose();
            }
        });

        setSize(420, 160);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void checkClicked(ActionEvent event) {
        lookUpPrice(medicineField.getText());
    }

    private void lookUpPrice(String rawText) {
        String query = rawText == null ? "" : rawText.trim();
        if (query.isEmpty()) {
            resultLabel.setText("Please type a medicine name.");
            return;
        }

        try {
            List<Medicine> medicines = medicineDAO.search(query);
            if (medicines.isEmpty()) {
                resultLabel.setText("No medicine found for '" + query + "'.");
                return;
            }
            Medicine medicine = medicines.get(0);
            resultLabel.setText("" + medicine.getName() + " : INR "
                    + medicine.getUnitPrice().toPlainString() + " | Stock: "
                    + medicine.getStock());
        } catch (SQLException exception) {
            resultLabel.setText("Unable to fetch price data.");
            System.err.println("Quick price lookup failed: " + exception.getMessage());
        }
    }
}
