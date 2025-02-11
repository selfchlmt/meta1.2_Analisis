package org.meta1.crud;

public class VehiculoMaritimo extends Vehiculo {
    private double eslora; // Longitud en metros

    public VehiculoMaritimo(String marca, String modelo, int anio, double eslora) {
        super(marca, modelo, anio);
        this.eslora = eslora;
    }

    @Override
    public void mostrarInfo() {
        super.mostrarInfo();
        System.out.println("Eslora: " + eslora + " metros");
    }
}
