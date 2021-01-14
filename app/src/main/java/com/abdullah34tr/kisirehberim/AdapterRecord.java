package com.abdullah34tr.kisirehberim;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdapterRecord extends RecyclerView.Adapter<AdapterRecord.HolderRecord> {

    private Context context;
    private ArrayList<ModelRecord> recordsList;

    MyDbHelper dbHelper;

    public AdapterRecord(Context context, ArrayList<ModelRecord> recordsList) {

        this.context = context;
        this.recordsList = recordsList;

        dbHelper = new MyDbHelper(context);
    }

    @NonNull
    @Override
    public HolderRecord onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_record, parent, false);

        return new HolderRecord(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderRecord holder, final int position) {

        ModelRecord model = recordsList.get(position);

        final String id = model.getId();
        final String name = model.getName();
        final String image = model.getImage();
        final String bio = model.getBio();
        final String phone = model.getPhone();
        final String email = model.getEmail();
        final String dob = model.getDob();
        final String addedTime = model.getAddedTime();
        final String updatedTime = model.getUpdatedTime();

        holder.nameTv.setText(name);
        holder.phoneTv.setText(phone);
        holder.emailTv.setText(email);
        holder.dobTv.setText(dob);

        if(image.equals("null")) {

            holder.profileIv.setImageResource(R.drawable.ic_person_black);
        }

        else {

            holder.profileIv.setImageURI(Uri.parse(image));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, RecordDetailActivity.class);
                intent.putExtra("RECORD_ID", id);
                context.startActivity(intent);
            }
        });

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                showMoreDialog(
                        "" + position,
                        "" + id,
                        "" + name,
                        "" + phone,
                        "" + email,
                        "" + dob,
                        "" + bio,
                        "" + image,
                        "" + addedTime,
                        "" + updatedTime
                );
            }
        });

        Log.d("ImagePath", "onBindViewHolder : " + image);
    }

    private void showMoreDialog(String position, final String id, final String name, final String phone, final String email,
                                final String dob, final String bio, final String image, final String addedTime, final String updatedTime) {

        String[] options = {"Düzenle", "Sil"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(i == 0) {

                    Intent intent = new Intent(context, AddUpdateRecordActivity.class);

                    intent.putExtra("ID", id);
                    intent.putExtra("NAME", name);
                    intent.putExtra("PHONE", phone);
                    intent.putExtra("EMAIL", email);
                    intent.putExtra("DOB", dob);
                    intent.putExtra("BIO", bio);
                    intent.putExtra("IMAGE", image);
                    intent.putExtra("ADDED_TIME", addedTime);
                    intent.putExtra("UPDATED_TIME", updatedTime);
                    intent.putExtra("isEditMode", true);

                    context.startActivity(intent);
                }

                else if(i == 1) {

                    AlertDialog.Builder b = new AlertDialog.Builder(context);

                    b.setIcon(R.drawable.ic_warning_black);
                    b.setTitle("Kişiyi Sil");
                    b.setMessage("Bu kişi kaydını silmek istediğinizden emin misiniz?");

                    b.setPositiveButton("Evet", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dbHelper.deleteData(id);

                            Toast.makeText(context, "Kişi silindi.", Toast.LENGTH_SHORT).show();

                            ((MainActivity)context).onResume();
                        }
                    }).setNegativeButton("Hayır", null).show();
                }
            }
        });

        builder.create().show();
    }

    @Override
    public int getItemCount() {

        return recordsList.size();
    }

    class HolderRecord extends RecyclerView.ViewHolder {

        ImageView profileIv;
        TextView nameTv, phoneTv, emailTv, dobTv;
        ImageButton moreBtn;

        public HolderRecord(@NonNull View itemView) {

            super(itemView);

            profileIv = itemView.findViewById(R.id.profileIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            phoneTv = itemView.findViewById(R.id.phoneTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            dobTv = itemView.findViewById(R.id.dobTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
        }
    }
}