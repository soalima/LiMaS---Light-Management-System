package com.proyecto.limas11;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LuzFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_luz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String dirBlue = ((AlmacenGlobal) getActivity().getApplication()).getDirBluetooth();
        if (dirBlue != null) {
            Intent i = new Intent(getActivity(), activity_comunicacion.class);
            i.putExtra("Direccion_Bluethoot", (dirBlue));
            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            getActivity().startActivityIfNeeded(i, 0);
        }
    }
}
