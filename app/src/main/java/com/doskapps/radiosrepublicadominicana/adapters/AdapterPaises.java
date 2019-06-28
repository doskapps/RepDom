package com.doskapps.radiosrepublicadominicana.adapters;

import android.util.Log;

import com.doskapps.radiosrepublicadominicana.callbacks.CallbackPais;
import com.doskapps.radiosrepublicadominicana.models.Pais;
import com.doskapps.radiosrepublicadominicana.rests.ApiInterface;
import com.doskapps.radiosrepublicadominicana.rests.RestAdapter;

import java.util.ArrayList;

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
