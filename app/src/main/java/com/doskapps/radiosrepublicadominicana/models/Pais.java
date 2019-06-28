package com.doskapps.radiosrepublicadominicana.models;

import java.io.Serializable;

public class Pais implements Serializable {

    private String locale = "";
    private String nombre = "";

    public Pais(String locale) {
        this.locale = locale;
    }

    public Pais(String locale, String nombre) {
        this.locale = locale;
        this.nombre = nombre;
    }

    public Pais() {
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
