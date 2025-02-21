package org.meta1.crud;

public class CamionCarga extends Vehiculo {
    private double capacidadCarga; // en toneladas

    public CamionCarga(String marca, String modelo, int anio, double capacidadCarga) {
        super(marca, modelo, anio);
        this.capacidadCarga = capacidadCarga;
    }

    @Override
    public void mostrarInfo() {
        super.mostrarInfo();
        System.out.println("Capacidad de carga: " + capacidadCarga + " toneladas");
    }
}
