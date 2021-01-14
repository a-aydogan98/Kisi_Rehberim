package com.abdullah34tr.kisirehberim;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class AddUpdateRecordActivity extends AppCompatActivity {

    private CircularImageView profileIv;
    private EditText nameEt, phoneEt, emailEt, dobEt, bioEt;
    private FloatingActionButton saveBtn;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;

    private static final int IMAGE_PICK_CAMERA_CODE = 102;
    private static final int IMAGE_PICK_GALLERY_CODE = 103;

    private String[] cameraPermissions;
    private String[] storagePermissions;

    private Uri imageUri;
    private String id, name, phone, email, dob, bio, addedTime, updatedTime;
    private boolean isEditMode = false;

    private MyDbHelper dbHelper;

    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_update_record);

        actionBar = getSupportActionBar();

        actionBar.setTitle("Yeni Kişi Ekle");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        profileIv = findViewById(R.id.profileIv);
        nameEt = findViewById(R.id.nameEt);
        phoneEt = findViewById(R.id.phoneEt);
        emailEt = findViewById(R.id.emailEt);
        dobEt = findViewById(R.id.dobEt);
        bioEt = findViewById(R.id.bioEt);
        saveBtn = findViewById(R.id.saveBtn);

        Intent intent = getIntent();

        isEditMode = intent.getBooleanExtra("isEditMode", false);

        if(isEditMode) {

            actionBar.setTitle("Kişi Bilgisini Düzenle");

            id = intent.getStringExtra("ID");
            name = intent.getStringExtra("NAME");
            phone = intent.getStringExtra("PHONE");
            email = intent.getStringExtra("EMAIL");
            dob = intent.getStringExtra("DOB");
            bio = intent.getStringExtra("BIO");
            imageUri = Uri.parse(intent.getStringExtra("IMAGE"));
            addedTime = intent.getStringExtra("ADDED_TIME");
            updatedTime = intent.getStringExtra("UPDATED_TIME");

            nameEt.setText(name);
            phoneEt.setText(phone);
            emailEt.setText(email);
            dobEt.setText(dob);
            bioEt.setText(bio);

            if(imageUri.toString().equals("null")) {

                profileIv.setImageResource(R.drawable.ic_person_black);
            }

            else {

                profileIv.setImageURI(imageUri);
            }
        }

        else {

            actionBar.setTitle("Yeni Kişi Ekle");
        }

        dbHelper = new MyDbHelper(this);

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        profileIv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                imagePickDialog();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                inputData();
            }
        });
    }

    private void inputData() {

        name = "" + nameEt.getText().toString().trim();
        phone = "" + phoneEt.getText().toString().trim();
        email = "" + emailEt.getText().toString().trim();
        dob = "" + dobEt.getText().toString().trim();
        bio = "" + bioEt.getText().toString().trim();

        if(isEditMode) {

            String timestamp = "" + System.currentTimeMillis();

            dbHelper.updateRecord(
                    "" + id,
                    "" + name,
                    "" + imageUri,
                    "" + bio,
                    "" + phone,
                    "" + email,
                    "" + dob,
                    "" + addedTime,
                    "" + timestamp
            );

            Toast.makeText(this, "Kişi bilgileri güncellendi.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(AddUpdateRecordActivity.this, MainActivity.class);
            startActivity(intent);

            finish();
        }

        else {

            String timestamp = "" + System.currentTimeMillis();

            long id = dbHelper.insertRecord(
                    "" + name,
                    "" + imageUri,
                    "" + bio,
                    "" + phone,
                    "" + email,
                    "" + dob,
                    "" + timestamp,
                    "" + timestamp
            );

            Toast.makeText(this, "Kayıt eklendi. ID : " + id, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(AddUpdateRecordActivity.this, MainActivity.class);
            startActivity(intent);

            finish();
        }
    }

    private void imagePickDialog() {

        String[] options = {"Kamera", "Galeri"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Şu Kaynaktan Bir Resim Seç");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(i == 0) {

                    if(!checkCameraPermissions()) {

                        requestCameraPermission();
                    }

                    else {

                        pickFromCamera();
                    }
                }

                else if(i == 1) {

                    if(!checkStoragePermission()) {

                        requestStoragePermission();
                    }

                    else {

                        pickFromGallery();
                    }
                }
            }
        });

        builder.create().show();
    }

    private void pickFromGallery() {

        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Resim Başlığı");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Resim Açıklaması");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent camereIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camereIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(camereIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission() {

        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStoragePermission() {

        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermissions() {

        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission() {

        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void copyFileOrDirectory(String srcDir, String desDir) {

        try {

            File src = new File(srcDir);
            File des = new File(desDir, src.getName());

            if(src.isDirectory()) {

                String[] files = src.list();
                int filesLength = files.length;

                for(String file : files) {

                    String src1 = new File(src, file).getPath();
                    String des1 = des.getPath();

                    copyFileOrDirectory(src1, des1);
                }
            }

            else {

                copyFile(src, des);
            }
        }

        catch (Exception e) {

            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void copyFile(File srcDir, File desDir) throws IOException {

        if(!desDir.getParentFile().exists()) {

            desDir.mkdirs();
        }

        if(!desDir.exists()) {

            desDir.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {

            source = new FileInputStream(srcDir).getChannel();
            destination = new FileOutputStream(desDir).getChannel();
            destination.transferFrom(source, 0, source.size());

            imageUri = Uri.parse(desDir.getPath());
            Log.d("ImagePath", "copyFile : " + imageUri);
        }

        catch (Exception e) {

            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        finally {

            if(source != null) {

                source.close();
            }

            if(destination != null) {

                destination.close();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();

        return super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case CAMERA_REQUEST_CODE: {

                if(grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted && storageAccepted) {

                        pickFromCamera();
                    }

                    else {
                        Toast.makeText(this,
                                "Kameraya erişim izni ve depolama alanına erişim izni gereklidir.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            break;

            case STORAGE_REQUEST_CODE: {

                if(grantResults.length > 0) {

                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(storageAccepted) {

                        pickFromGallery();
                    }

                    else {

                        Toast.makeText(this, "Depolama alanına erişim izni gereklidir.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode == RESULT_OK) {

            if(requestCode == IMAGE_PICK_GALLERY_CODE) {

                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);
            }

            else if(requestCode == IMAGE_PICK_CAMERA_CODE) {

                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this);
            }

            else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if(resultCode == RESULT_OK) {

                    Uri resultUri = result.getUri();
                    imageUri = resultUri;

                    profileIv.setImageURI(resultUri);

                    copyFileOrDirectory("" + imageUri.getPath(), "" + getDir("SQLiteRecordImages",
                            MODE_PRIVATE));
                }

                else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                    Exception error = result.getError();

                    Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}