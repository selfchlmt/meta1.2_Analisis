package org.meta1.crud;

public class Bicicleta extends Vehiculo {
    private boolean tieneCambio;

    public Bicicleta(String marca, String modelo, int anio, boolean tieneCambio) {
        super(marca, modelo, anio);
        this.tieneCambio = tieneCambio;
    }

    @Override
    public void mostrarInfo() {
        super.mostrarInfo();
        System.out.println("Tiene cambio de marchas: " + (tieneCambio ? "Sí" : "No"));
    }
}
