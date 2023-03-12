package com.uc.degura.view.splash;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uc.degura.R;

public class SplashFragment extends Fragment {

    private static int splashtime = 2500;
    private static final String TAG = "SplashFragment";

    public SplashFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            NavDirections action;
//            if (helper.getAccessToken().isEmpty()){
//                action = SplashFragmentDirections.actionSplashFragmentToLoginFragment();
//            } else{
//                action = SplashFragmentDirections.actionSplashFragmentToBerandaFragment();
//            }
//            Navigation.findNavController(view).navigate(action);
//        }, splashtime);
    }
}