package org.meta1.crud;

public class Vehiculo {
    protected String marca;
    protected String modelo;
    protected int age;

    public Vehiculo(String marca, String modelo, int anio) {
        this.marca = marca;
        this.modelo = modelo;
        this.age = anio;
    }

    public void mostrarInfo() {
        System.out.println("Marca: " + marca);
        System.out.println("Modelo: " + modelo);
        System.out.println("Año: " + age);
    }
}
