package org.meta1.crud;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.Optional;

public class Gui2 extends Application {
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/m1.2_analisis";
    private static final String DATABASE_USER = "root";
    private static final String DATABASE_PASSWORD = "guadalupe";

    @Override
    public void start(Stage stage) {
        Button btnRegistrar = new Button("Registrar Persona");
        Button btnBuscar = new Button("Buscar Persona");
        Button btnActualizar = new Button("Actualizar Persona");
        Button btnEliminar = new Button("Eliminar Persona");

        btnRegistrar.setOnAction(event -> mostrarDialogoRegistro());
        btnBuscar.setOnAction(event -> buscarPersona());
        btnActualizar.setOnAction(event -> actualizarPersona());
        btnEliminar.setOnAction(event -> eliminarPersona());

        VBox layout = new VBox(10, btnRegistrar, btnBuscar, btnActualizar, btnEliminar);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Scene scene = new Scene(layout, 400, 300);

        stage.setTitle("Gestión de Personas");
        stage.setScene(scene);
        stage.show();
    }

    private void mostrarDialogoRegistro() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Registrar Persona");
        dialog.setHeaderText("Ingrese los datos de la persona");

        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre");

        TextField direccionField = new TextField();
        direccionField.setPromptText("Dirección");

        TextField telefonoField = new TextField();
        telefonoField.setPromptText("Teléfonos (separados por coma)");

        TextField vehiculoField = new TextField();
        vehiculoField.setPromptText("Vehículos (separados por coma)");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Dirección:"), 0, 1);
        grid.add(direccionField, 1, 1);
        grid.add(new Label("Teléfonos:"), 0, 2);
        grid.add(telefonoField, 1, 2);
        grid.add(new Label("Vehículos:"), 0, 3);
        grid.add(vehiculoField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new String[]{nombreField.getText(), direccionField.getText(), telefonoField.getText(), vehiculoField.getText()};
            }
            return null;
        });

        Optional<String[]> result = dialog.showAndWait();
        result.ifPresent(data -> guardarPersona(data[0], data[1], data[2], data[3]));
    }

    private void guardarPersona(String nombre, String direccion, String telefonos, String vehiculos) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD)) {
            String insertPersona = "INSERT INTO persona (nombre, direccion) VALUES (?, ?)";
            PreparedStatement personaStmt = connection.prepareStatement(insertPersona, Statement.RETURN_GENERATED_KEYS);
            personaStmt.setString(1, nombre);
            personaStmt.setString(2, direccion);
            personaStmt.executeUpdate();
            ResultSet rs = personaStmt.getGeneratedKeys();
            if (rs.next()) {
                int personaID = rs.getInt(1);
                guardarTelefonos(connection, personaID, telefonos);
                guardarVehiculos(connection, personaID, vehiculos);
                mostrarAlerta("Éxito", "Persona registrada correctamente.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al guardar en la base de datos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void guardarTelefonos(Connection connection, int personaID, String telefonos) throws SQLException {
        if (!telefonos.isEmpty()) {
            String[] telefonosArray = telefonos.split(",");
            String insertTelefono = "INSERT INTO telefono (ID_cel, numero) VALUES (?, ?)";
            PreparedStatement telefonoStmt = connection.prepareStatement(insertTelefono);
            for (String tel : telefonosArray) {
                telefonoStmt.setInt(1, personaID);
                telefonoStmt.setString(2, tel.trim());
                telefonoStmt.executeUpdate();
            }
        }
    }

    private void guardarVehiculos(Connection connection, int personaID, String vehiculos) throws SQLException {
        if (!vehiculos.isEmpty()) {
            String[] vehiculosArray = vehiculos.split(",");
            String insertVehiculo = "INSERT INTO vehiculo (ID_vehiculo, tipo) VALUES (?, ?)";
            PreparedStatement vehiculoStmt = connection.prepareStatement(insertVehiculo);
            for (String veh : vehiculosArray) {
                vehiculoStmt.setInt(1, personaID);
                vehiculoStmt.setString(2, veh.trim());
                vehiculoStmt.executeUpdate();
            }
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.show();
    }

    private void buscarPersona() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Buscar Persona");
        dialog.setHeaderText("Ingrese el ID de la persona:");
        dialog.setContentText("ID:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(idStr -> {
            try {
                int id = Integer.parseInt(idStr); // Convertir a entero
                String query = "SELECT p.ID, p.nombre, p.direccion, " +
                        "GROUP_CONCAT(DISTINCT t.numero SEPARATOR ', ') AS telefonos, " +
                        "GROUP_CONCAT(DISTINCT v.tipo SEPARATOR ', ') AS vehiculos " +
                        "FROM persona p " +
                        "LEFT JOIN telefono t ON p.ID = t.ID_cel " +
                        "LEFT JOIN vehiculo v ON p.ID = v.ID_vehiculo " +
                        "WHERE p.ID = ? " +
                        "GROUP BY p.ID, p.nombre, p.direccion";

                try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
                     PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                    preparedStatement.setInt(1, id);
                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        String resultado = "ID: " + resultSet.getInt("id") +
                                "\nNombre: " + resultSet.getString("nombre") +
                                "\nDirección: " + resultSet.getString("direccion") +
                                "\nTeléfonos: " + (resultSet.getString("telefonos") != null ? resultSet.getString("telefonos") : "Ninguno") +
                                "\nVehículos: " + (resultSet.getString("vehiculos") != null ? resultSet.getString("vehiculos") : "Ninguno");

                        mostrarAlerta("Resultado de búsqueda", resultado, Alert.AlertType.INFORMATION);
                    } else {
                        mostrarAlerta("No encontrado", "No se encontró ninguna persona con ese ID.", Alert.AlertType.WARNING);
                    }

                } catch (SQLException e) {
                    mostrarAlerta("Error", "Error en la búsqueda: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "El ID debe ser un número válido.", Alert.AlertType.ERROR);
            }
        });
    }


    private void actualizarPersona() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Actualizar Persona");
        dialog.setHeaderText("Ingrese el ID de la persona a actualizar:");
        dialog.setContentText("ID:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(id -> {
            String query = "SELECT nombre, direccion FROM persona WHERE id = ?";
            try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setInt(1, Integer.parseInt(id));
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    TextInputDialog nombreDialog = new TextInputDialog(resultSet.getString("nombre"));
                    nombreDialog.setTitle("Actualizar Nombre");
                    nombreDialog.setHeaderText("Nuevo Nombre:");
                    Optional<String> nuevoNombre = nombreDialog.showAndWait();

                    TextInputDialog direccionDialog = new TextInputDialog(resultSet.getString("direccion"));
                    direccionDialog.setTitle("Actualizar Dirección");
                    direccionDialog.setHeaderText("Nueva Dirección:");
                    Optional<String> nuevaDireccion = direccionDialog.showAndWait();

                    if (nuevoNombre.isPresent() && nuevaDireccion.isPresent()) {
                        String updateQuery = "UPDATE persona SET nombre = ?, direccion = ? WHERE id = ?";
                        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                            updateStatement.setString(1, nuevoNombre.get());
                            updateStatement.setString(2, nuevaDireccion.get());
                            updateStatement.setInt(3, Integer.parseInt(id));
                            updateStatement.executeUpdate();
                            mostrarAlerta("Éxito", "Datos actualizados.", Alert.AlertType.INFORMATION);
                        }
                    }
                } else {
                    mostrarAlerta("No encontrado", "No hay persona con ese ID.", Alert.AlertType.WARNING);
                }

            } catch (SQLException e) {
                mostrarAlerta("Error", "Error al actualizar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void eliminarPersona() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Eliminar Persona");
        dialog.setHeaderText("Ingrese el ID de la persona a eliminar:");
        dialog.setContentText("ID:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(id -> {
            try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD)) {
                String deleteTelefono = "DELETE FROM telefono WHERE id = ?";
                String deleteVehiculo = "DELETE FROM vehiculo WHERE id = ?";
                String deletePersona = "DELETE FROM persona WHERE id = ?";

                try (PreparedStatement telefonoStmt = connection.prepareStatement(deleteTelefono);
                     PreparedStatement vehiculoStmt = connection.prepareStatement(deleteVehiculo);
                     PreparedStatement personaStmt = connection.prepareStatement(deletePersona)) {

                    telefonoStmt.setInt(1, Integer.parseInt(id));
                    telefonoStmt.executeUpdate();
                    vehiculoStmt.setInt(1, Integer.parseInt(id));
                    vehiculoStmt.executeUpdate();
                    personaStmt.setInt(1, Integer.parseInt(id));
                    int rowsDeleted = personaStmt.executeUpdate();

                    if (rowsDeleted > 0) {
                        mostrarAlerta("Éxito", "Persona eliminada.", Alert.AlertType.INFORMATION);
                    } else {
                        mostrarAlerta("No encontrado", "No hay persona con ese ID.", Alert.AlertType.WARNING);
                    }
                }
            } catch (SQLException e) {
                mostrarAlerta("Error", "Error al eliminar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}