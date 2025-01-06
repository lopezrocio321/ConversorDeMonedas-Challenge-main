package com.gusqroo.ProyectoMaven;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import okhttp3.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::run);
    }

    private static void run() {
        JFrame frame = new JFrame("Conversor de Monedas - ExchangeRate API");

        JLabel fromLabel = new JLabel("De");
        JComboBox<String> comboboxFrom;
        String[] currencys = {
                "USD - Dólar", "EUR - Euro", "MXN - Peso Mexicano",
                "CAD - Dólar Canadiense", "GBP - Libra Esterlina",
                "JPY - Yen", "CNH - Yuan", "ARS - Peso Argentino",
                "SEK - Corona Sueca"
        };
        comboboxFrom = new JComboBox<>(currencys);

        JLabel toLabel = new JLabel("A:");
        JComboBox<String> comboboxTo = new JComboBox<>(currencys);
        JLabel amountLabel = new JLabel("Monto:");
        JTextField amountTextField = new JTextField(10);
        JButton convertButton = new JButton("Convertir");

        JPanel panel = new JPanel();
        panel.add(fromLabel);
        panel.add(comboboxFrom);
        panel.add(toLabel);
        panel.add(comboboxTo);
        panel.add(amountLabel);
        panel.add(amountTextField);
        panel.add(convertButton);

        frame.setLocationRelativeTo(null);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Icon icono = new ImageIcon("src/images/rb_60000.png");

                String from = comboboxFrom.getSelectedItem() != null ? comboboxFrom.getSelectedItem().toString().substring(0, 3) : "";
                String to = comboboxTo.getSelectedItem() != null ? comboboxTo.getSelectedItem().toString().substring(0, 3) : "";
                String amount = amountTextField.getText();

                if (amount.isEmpty() || from.isEmpty() || to.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Por favor completa todos los campos", "Error", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                OkHttpClient client = new OkHttpClient();

                // URL con el nuevo endpoint de ExchangeRate API
                String apiUrl = "https://v6.exchangerate-api.com/v6/aa30ea80170b316b2607bc23/pair/" + from + "/" + to + "/" + amount;

                Request request = new Request.Builder()
                        .url(apiUrl)
                        .get()
                        .build();

                try (Response response = client.newCall(request).execute()) { // try-with-resources
                    if (!response.isSuccessful()) {
                        throw new IOException("Código inesperado: " + response);
                    }

                    String responseBody = response.body().string();
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(responseBody);

                    if (jsonNode.has("conversion_result")) {
                        double result = jsonNode.get("conversion_result").asDouble();
                        String mensaje = "El monto convertido de " + amount + " " + from + " es de " + result + " " + to;

                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(frame, mensaje, "Resultado de la Conversión", JOptionPane.INFORMATION_MESSAGE, icono)
                        );
                    } else {
                        JOptionPane.showMessageDialog(frame, "Error en la respuesta de la API: " + jsonNode.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error de conexión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
