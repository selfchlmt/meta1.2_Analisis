package org.meta1.crud;

import java.sql.*;

import java.sql.*;

public class BaseDeDatos {

    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/m1.2_analisis";
    private static final String DATABASE_USER = "root";
    private static final String DATABASE_PASSWORD = "guadalupe";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
    }

    public void guardarPersona(String nombre, String direccion, String telefonos, String vehiculos) {
        try (Connection connection = getConnection()) {
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
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar persona: " + e.getMessage(), e);
        }
    }

    public void actualizarPersona(int id, String nuevoNombre, String nuevaDireccion) throws SQLException {
        String query = "UPDATE persona SET nombre = ?, direccion = ? WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, nuevoNombre);
            preparedStatement.setString(2, nuevaDireccion);
            preparedStatement.setInt(3, id);
            preparedStatement.executeUpdate();
        }
    }

    public void eliminarPersona(int id) throws SQLException {
        String deleteTelefono = "DELETE FROM telefono WHERE ID_cel = ?";
        String deleteVehiculo = "DELETE FROM vehiculo WHERE ID_vehiculo = ?";
        String deletePersona = "DELETE FROM persona WHERE ID = ?";

        try (Connection connection = getConnection();
             PreparedStatement telefonoStmt = connection.prepareStatement(deleteTelefono);
             PreparedStatement vehiculoStmt = connection.prepareStatement(deleteVehiculo);
             PreparedStatement personaStmt = connection.prepareStatement(deletePersona)) {

            telefonoStmt.setInt(1, id);
            telefonoStmt.executeUpdate();
            vehiculoStmt.setInt(1, id);
            vehiculoStmt.executeUpdate();
            personaStmt.setInt(1, id);
            personaStmt.executeUpdate();
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
}

