package com.uc.degura.view.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.uc.degura.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FishGillFragment extends Fragment {

    @BindView(R.id.btn_camera_gill)
    Button btn_camera_gill;

    @BindView(R.id.btn_gallery_gill)
    Button btn_gallery_gill;



    private static final String TAG = "FishGillFragment";

    public FishGillFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fish_gill, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

//        Bitmap fish_eye_image = getArguments().getParcelable("fish_eye");

        Uri fish_eye_uri = getArguments().getParcelable("fish_eye");

        Log.d(TAG, "Image Uri Fish Eye Debug: "+fish_eye_uri.toString());

        ActivityResultLauncher<Intent> cameraResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        Bitmap fish_gill_image = (Bitmap) data.getExtras().get("data");
                        int dimension = Math.min(fish_gill_image.getWidth(), fish_gill_image.getHeight());
                        fish_gill_image = ThumbnailUtils.extractThumbnail(fish_gill_image, dimension, dimension);

                        Log.d(TAG, "Image Bitmap Debug: "+fish_gill_image.toString());

                        Uri fish_gill_uri = saveImage(fish_gill_image, getActivity());

                        Log.d(TAG, "Image Uri Debug: "+fish_gill_uri.toString());

                        NavDirections actions;
                        actions = FishGillFragmentDirections.actionFishGillFragmentToDetectionFragment(fish_eye_uri, fish_gill_uri);
                        Navigation.findNavController(view).navigate(actions);

                    }
                }
        );

        btn_camera_gill.setOnClickListener(v -> {
            if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraResultLauncher.launch(cameraIntent);
            } else {
                getActivity().requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            }
        });

        ActivityResultLauncher<Intent> galleryResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        Uri imageUri = data.getData();
//                        Bitmap fish_gill_image = null;

//                        try {
//                            fish_gill_image = MediaStore.Images.Media.getBitmap(this.getContext().getContentResolver(), imageUri);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

                        Log.d(TAG, "Image Uri Debug: "+imageUri.toString());

                        NavDirections actions;
                        actions = FishGillFragmentDirections.actionFishGillFragmentToDetectionFragment(fish_eye_uri, imageUri);
                        Navigation.findNavController(view).navigate(actions);
                    }
                }
        );

        btn_gallery_gill.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryResultLauncher.launch(galleryIntent);
        });
    }

    private Uri saveImage(Bitmap image, Context context){
        File imagesFolder = new File(context.getCacheDir(), "deguraImages");
        Uri fish_eye_uri = null;
        try {
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, "captured_fish_gill_image.jpg");
            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();
            fish_eye_uri = FileProvider.getUriForFile(context.getApplicationContext(), "com.uc.degura"+".provider", file);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return fish_eye_uri;
    }
}