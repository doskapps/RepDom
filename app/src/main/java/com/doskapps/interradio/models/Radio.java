package com.doskapps.interradio.models;

import java.io.Serializable;

public class Radio implements Serializable {

    private int id;
    public String radio_id = "";
    public String radio_name = "";
    public String genere_name = "";
    public String category_name = "";
    public String radio_image = "";
    public String radio_url = "";

    public Radio() {
    }

    public Radio(String radio_id) {
        this.radio_id = radio_id;
    }

    public Radio(String radio_id, String radio_name, String genere_name, String category_name, String radio_image, String radio_url) {
        this.radio_id = radio_id;
        this.radio_name = radio_name;
        this.genere_name = genere_name;
        this.category_name = category_name;
        this.radio_image = radio_image;
        this.radio_url = radio_url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRadio_id() {
        return radio_id;
    }

    public void setRadio_id(String radio_id) {
        this.radio_id = radio_id;
    }

    public String getRadio_name() {
        return radio_name;
    }

    public void setRadio_name(String radio_name) {
        this.radio_name = radio_name;
    }

    public String getGenere_name() {
        return genere_name;
    }

    public void setGenere_name(String genere_name) {
        this.genere_name = genere_name;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getRadio_image() {
        return radio_image;
    }

    public void setRadio_image(String radio_image) {
        this.radio_image = radio_image;
    }

    public String getRadio_url() {
        return radio_url;
    }

    public void setRadio_url(String radio_url) {
        this.radio_url = radio_url;
    }

}
