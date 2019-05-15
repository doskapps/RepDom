package com.doskapps.interradio.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.doskapps.interradio.callbacks.CallbackPais;
import com.doskapps.interradio.models.Pais;
import com.doskapps.interradio.rests.ApiInterface;
import com.doskapps.interradio.rests.RestAdapter;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterPaises {

    private ArrayList<Pais> paises = new ArrayList<>();
    private ArrayList<String> nombrePaises = new ArrayList<>();

    public AdapterPaises() {
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<CallbackPais> callbackCall = apiInterface.getPaises();
        callbackCall.enqueue(new Callback<CallbackPais>() {
            @Override
            public void onResponse(Call<CallbackPais> call, Response<CallbackPais> response) {
                CallbackPais resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    paises = resp.posts;
                    if (resp.posts.size() == 0)
                        Log.d("Paises", "No se encontraron paises en la tabla tbl_pais");
                } else {
                    Log.e("Paises", "Error al buscar paises en la tabla tbl_pais");
                }

                for (Pais p: paises) {
                    nombrePaises.add(p.getNombre());
                }
            }

            @Override
            public void onFailure(Call<CallbackPais> call, Throwable t) {
                Log.e("Paises", "Falló la llamada el servicio para buscar paises en la tabla tbl_pais");
            }

        });
    }

    public ArrayList<String> getNombrePaises() {
        return nombrePaises;
    }

    public ArrayList<Pais> getPaises() {
        return paises;
    }
}
