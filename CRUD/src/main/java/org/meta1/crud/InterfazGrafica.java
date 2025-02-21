package org.meta1.crud;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.Optional;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class InterfazGrafica extends Application {
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/m1.2_analisis";
    private static final String DATABASE_USER = "root";
    private static final String DATABASE_PASSWORD = "guadalupe";

    private ManejadorPersonas personaHandler;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        // Inicializamos la base de datos y el manejador de personas
        BaseDeDatos baseDeDatos = new BaseDeDatos();
        personaHandler = new ManejadorPersonas(baseDeDatos);

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
        TextInputDialog nombreDialog = new TextInputDialog();
        nombreDialog.setTitle("Registrar Persona");
        nombreDialog.setHeaderText("Ingrese el nombre de la persona:");
        Optional<String> nombre = nombreDialog.showAndWait();

        if (nombre.isPresent() && !nombre.get().isEmpty()) {
            TextInputDialog direccionDialog = new TextInputDialog();
            direccionDialog.setTitle("Registrar Dirección");
            direccionDialog.setHeaderText("Ingrese la dirección de la persona:");
            Optional<String> direccion = direccionDialog.showAndWait();

            if (direccion.isPresent() && !direccion.get().isEmpty()) {
                TextInputDialog telefonoDialog = new TextInputDialog();
                telefonoDialog.setTitle("Registrar Teléfonos");
                telefonoDialog.setHeaderText("Ingrese los teléfonos de la persona (separados por coma):");
                Optional<String> telefonos = telefonoDialog.showAndWait();

                TextInputDialog vehiculoDialog = new TextInputDialog();
                vehiculoDialog.setTitle("Registrar Vehículos");
                vehiculoDialog.setHeaderText("Ingrese los vehículos de la persona (separados por coma):");
                Optional<String> vehiculos = vehiculoDialog.showAndWait();

                if (telefonos.isPresent() && vehiculos.isPresent()) {
                    personaHandler.guardarPersona(nombre.get(), direccion.get(), telefonos.get(), vehiculos.get());
                    mostrarAlerta("Éxito", "Persona registrada correctamente.", Alert.AlertType.INFORMATION);
                }
            }
        }
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
                        personaHandler.actualizarPersona(Integer.parseInt(id), nuevoNombre.get(), nuevaDireccion.get());
                        mostrarAlerta("Éxito", "Datos actualizados.", Alert.AlertType.INFORMATION);
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
            try {
                personaHandler.eliminarPersona(Integer.parseInt(id));
                mostrarAlerta("Éxito", "Persona eliminada.", Alert.AlertType.INFORMATION);
            } catch (RuntimeException e) {
                mostrarAlerta("Error", "Error al eliminar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.show();
    }
}
