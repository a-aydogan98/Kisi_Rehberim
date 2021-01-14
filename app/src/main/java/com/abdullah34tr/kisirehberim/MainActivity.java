package com.abdullah34tr.kisirehberim;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton addRecordBtn;
    private RecyclerView recordsRv;

    private MyDbHelper dbHelper;

    private ActionBar actionBar;

    String orderByNewest = Constants.C_ADDED_TIMESTAMP + " DESC";
    String orderByOldest = Constants.C_ADDED_TIMESTAMP + " ASC";
    String orderByTitleAsc = Constants.C_NAME + " ASC";
    String orderByTitleDesc = Constants.C_NAME + " DESC";

    String currentOrderByStatus = orderByNewest;

    private static final int STORAGE_REQUEST_CODE_EXPORT = 1;
    private static final int STORAGE_REQUEST_CODE_IMPORT = 2;
    private String[] storagePermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Bütün Kişiler");

        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        addRecordBtn = findViewById(R.id.addRecordBtn);
        recordsRv = findViewById(R.id.recordsRv);

        dbHelper = new MyDbHelper(this);

        loadRecords(orderByNewest);

        addRecordBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, AddUpdateRecordActivity.class);
                intent.putExtra("isEditMode", false);
                startActivity(intent);
            }
        });
    }

    private boolean checkStoragePermission() {

        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStoragePermissionImport() {

        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE_IMPORT);
    }

    private void requestStoragePermissionExport() {

        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE_EXPORT);
    }

    private void importCSV() {

        String filePathAndName = Environment.getExternalStorageDirectory() + "/SQLiteBackup/" + "SQLite_Backup.csv";

        File csvFile = new File(filePathAndName);

        if(csvFile.exists()) {

            try {

                CSVReader csvReader = new CSVReader(new FileReader(csvFile.getAbsoluteFile()));

                String[] nextLine;

                while ((nextLine = csvReader.readNext()) != null) {

                    String ids = nextLine[0];
                    String name = nextLine[1];
                    String image = nextLine[2];
                    String bio = nextLine[3];
                    String phone = nextLine[4];
                    String email = nextLine[5];
                    String dob = nextLine[6];
                    String addedTime = nextLine[7];
                    String updatedTime = nextLine[8];

                    long timestamp = System.currentTimeMillis();

                    long id = dbHelper.insertRecord(
                            "" + name,
                            "" + image,
                            "" + bio,
                            "" + phone,
                            "" + email,
                            "" + dob,
                            "" + addedTime,
                            "" + updatedTime
                    );
                }

                Toast.makeText(this, "Yedek geri yüklendi.", Toast.LENGTH_SHORT).show();
            }

            catch (Exception e) {

                Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        else {

            Toast.makeText(this, "Yedek bulunamadı.", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportCSV() {

        File folder = new File(Environment.getExternalStorageDirectory() + "/" + "SQLiteBackup");

        boolean isFolderCreated = false;

        if(!folder.exists()) {

            isFolderCreated = folder.mkdir();
        }

        Log.d("CSC_TAG", "exportCSV : " + isFolderCreated);

        String csvFileName = "SQLite_Backup.csv";
        String filePathAndName = folder.toString() + "/" + csvFileName;

        ArrayList<ModelRecord> recordsList = new ArrayList<>();
        recordsList.clear();
        recordsList = dbHelper.getAllRecords(orderByOldest);

        try {

            FileWriter fw = new FileWriter(filePathAndName);

            for(int i = 0; i < recordsList.size(); i++) {

                fw.append("" + recordsList.get(i).getId());
                fw.append(",");
                fw.append("" + recordsList.get(i).getName());
                fw.append(",");
                fw.append("" + recordsList.get(i).getImage());
                fw.append(",");
                fw.append("" + recordsList.get(i).getBio());
                fw.append(",");
                fw.append("" + recordsList.get(i).getPhone());
                fw.append(",");
                fw.append("" + recordsList.get(i).getEmail());
                fw.append(",");
                fw.append("" + recordsList.get(i).getDob());
                fw.append(",");
                fw.append("" + recordsList.get(i).getAddedTime());
                fw.append(",");
                fw.append("" + recordsList.get(i).getUpdatedTime());
                fw.append("\n");
            }

            fw.flush();
            fw.close();

            Toast.makeText(this, "Yedekleme dışa aktarıldı : " + filePathAndName, Toast.LENGTH_SHORT).show();
        }

        catch (Exception e) {

            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRecords(String orderBy) {

        currentOrderByStatus = orderBy;

        AdapterRecord adapterRecord = new AdapterRecord(MainActivity.this,
                dbHelper.getAllRecords(orderBy));

        recordsRv.setAdapter(adapterRecord);

        actionBar.setSubtitle("Toplam : " + dbHelper.getRecordsCount());
    }

    private void searchRecords(String query) {

        AdapterRecord adapterRecord = new AdapterRecord(MainActivity.this,
                dbHelper.searchRecords(query));

        recordsRv.setAdapter(adapterRecord);
    }

    private void sortOptionDialog() {

        String[] options = {"A-Z", "Z-A", "En Yeni", "En Eski"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sıralama Türleri");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(i == 0) {

                    loadRecords(orderByTitleAsc);
                }

                else if(i == 1) {

                    loadRecords(orderByTitleDesc);
                }

                else if(i == 2) {

                    loadRecords(orderByNewest);
                }

                else if(i == 3) {

                    loadRecords(orderByOldest);
                }
            }
        });

        builder.create().show();
    }

    @Override
    protected void onResume() {

        super.onResume();

        loadRecords(currentOrderByStatus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                searchRecords(query);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                searchRecords(newText);

                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.action_sort) {

            sortOptionDialog();
        }

        else if(id == R.id.action_delete_all) {

            AlertDialog.Builder b = new AlertDialog.Builder(this);

            b.setIcon(R.drawable.ic_warning_black);
            b.setTitle("Bütün Kişileri Sil");
            b.setMessage("Bütün kişilerin kayıtlarını silmek istediğinizden emin misiniz?");

            b.setPositiveButton("Evet", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dbHelper.deleteAllData();
                    Toast.makeText(MainActivity.this, "Bütün kişiler silindi.", Toast.LENGTH_SHORT).show();
                    onResume();
                }
            }).setNegativeButton("Hayır", null).show();
        }

        else if(id == R.id.action_backup) {

            if(checkStoragePermission()) {

                exportCSV();
            }

            else {

                requestStoragePermissionExport();
            }
        }

        else if(id == R.id.action_restore) {

            if(checkStoragePermission()) {

                importCSV();
                onResume();
            }

            else {

                requestStoragePermissionImport();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case STORAGE_REQUEST_CODE_EXPORT: {

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    exportCSV();
                }

                else {

                    Toast.makeText(this, "Depolama alanına erişim izni gereklidir.", Toast.LENGTH_SHORT).show();
                }
            }

            break;

            case STORAGE_REQUEST_CODE_IMPORT: {

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    importCSV();
                }

                else {

                    Toast.makeText(this, "Depolama alanına erişim izni gereklidir.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setIcon(R.drawable.ic_exit_black);
        builder.setTitle("Uygulamadan Çıkış");
        builder.setMessage("Uygulamadan çıkmak istediğinizden emin misiniz?");

        builder.setPositiveButton("Evet", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }).setNegativeButton("Hayır", null).show();
    }
}