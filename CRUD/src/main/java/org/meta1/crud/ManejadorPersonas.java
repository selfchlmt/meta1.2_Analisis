package org.meta1.crud;

import java.sql.SQLException;

public class ManejadorPersonas {

    private BaseDeDatos baseDeDatos;

    public ManejadorPersonas(BaseDeDatos baseDeDatos) {
        this.baseDeDatos = baseDeDatos;
    }

    public void guardarPersona(String nombre, String direccion, String telefonos, String vehiculos) {
        baseDeDatos.guardarPersona(nombre, direccion, telefonos, vehiculos);
    }

    public void actualizarPersona(int id, String nuevoNombre, String nuevaDireccion) {
        try {
            baseDeDatos.actualizarPersona(id, nuevoNombre, nuevaDireccion);
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar persona: " + e.getMessage(), e);
        }
    }

    public void eliminarPersona(int id) {
        try {
            baseDeDatos.eliminarPersona(id);
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar persona: " + e.getMessage(), e);
        }
    }
}

