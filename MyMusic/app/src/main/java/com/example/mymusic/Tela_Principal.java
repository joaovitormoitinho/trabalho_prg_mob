package com.example.mymusic;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class Tela_Principal extends AppCompatActivity {

    private TextView nomeUsuario;
    private ImageView logout, photo_user;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String UsuarioID;

    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private ProgressDialog loader;
    private  String key = "";
    private  String task;
    private String descricao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);

        getSupportActionBar().hide();
        iniciarComponentes();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(Tela_Principal.this,Login.class);
                startActivity(intent);
                finish();
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        loader = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        UsuarioID = mUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("Tarefas").child(UsuarioID);

        floatingActionButton.setOnClickListener((view) -> {addTask();});
    }

    @Override
    protected void onStart() {
        super.onStart();

        UsuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference documentReference = db.collection("Usuarios").document(UsuarioID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot != null){
                    nomeUsuario.setText(documentSnapshot.getString("nome"));
                    Picasso.get().load(documentSnapshot.getString("profileURL")).into(photo_user);
                }
            }
        });

        FirebaseRecyclerOptions<Model> options = new FirebaseRecyclerOptions.Builder<Model>()
                .setQuery(reference, Model.class)
                .build();

        FirebaseRecyclerAdapter<Model, myViewHolder> adapter = new FirebaseRecyclerAdapter<Model, myViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull myViewHolder holder, @SuppressLint("RecyclerView") final int position, @NonNull final Model model) {
                holder.setDate(model.getDate());
                holder.setTask(model.getTask());
                holder.setDescricao(model.getDescription());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        key = getRef(position).getKey();
                        task = model.getTask();
                        descricao = model.getDescription();

                        updateTask();
                    }
                });
            }

            @NonNull
            @Override
            public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lista,parent,false);
                return new myViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void addTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.input_file, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final EditText task = myView.findViewById(R.id.edit_tarefa);
        final EditText descricao =  myView.findViewById(R.id.edit_descricao);
        Button cancel = myView.findViewById(R.id.cancelar_btn);
        Button save = myView.findViewById(R.id.save_btn);
        String id = reference.push().getKey();
        String date = DateFormat.getDateInstance().format(new Date());

        cancel.setOnClickListener((view) -> {dialog.dismiss();});

        save.setOnClickListener((view) -> {
            String mTask = task.getText().toString().trim();
            String mDescricao = descricao.getText().toString().trim();

            if (TextUtils.isEmpty(mTask)) {
                task.setError("tarefa necessaria");
            }
            if (TextUtils.isEmpty(mDescricao)){
                descricao.setError("Descrição necessaria");
                return;
            }else{
                loader.setMessage("adicionado seus dados");
                loader.setCanceledOnTouchOutside(false);
                loader.show();

                Model model = new Model(mTask,mDescricao,id,date);
                reference.child(id).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Tela_Principal.this, "tarefa inserida com sucesso", Toast.LENGTH_SHORT).show();
                            loader.dismiss();
                        }else{
                            String error = task.getException().toString();
                            Toast.makeText(Tela_Principal.this, "Falha ao inserir tarefa"+error, Toast.LENGTH_SHORT).show();
                            loader.dismiss();
                        }
                    }
                });
            }

            dialog.dismiss();
        });

        dialog.show();
    }



    public static class myViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTask(String tarefa){
            TextView taskTextView = mView.findViewById(R.id.tarefa_item_lista);
            taskTextView.setText(tarefa);
        }

        public void setDescricao(String desc){
            TextView descTextView = mView.findViewById(R.id.descriao_item_lista);
            descTextView.setText(desc);
        }
        public void setDate(String date){
            TextView dateTextView = mView.findViewById(R.id.dataTv);
        }
    }

    private void updateTask(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.editar_data, null);
        myDialog.setView(view);

        AlertDialog dialog = myDialog.create();

        EditText mTask = view.findViewById(R.id.mEditTextTarefa);
        EditText mDescricao = view.findViewById(R.id.mEditTextDescricao);

        mTask.setText(task);
        mTask.setSelection(task.length());

        mDescricao.setText(descricao);
        mDescricao.setSelection(descricao.length());

        Button delButton = view.findViewById(R.id.delete_btn);
        Button saveButton = view.findViewById(R.id.btn_salvar);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task = mTask.getText().toString().trim();
                descricao = mDescricao.getText().toString().trim();

                String date = DateFormat.getDateInstance().format(new Date());

                Model model = new Model(task, descricao, key, date);

                reference.child(key).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(Tela_Principal.this, "Dados alterados com sucesso!", Toast.LENGTH_SHORT).show();
                        }else{
                            String err = task.getException().toString();
                            Toast.makeText(Tela_Principal.this, "Falha ao alterar os dados!"+err, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                
                dialog.dismiss();
            }
        });
        
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(Tela_Principal.this, "Tarefa deletada com sucesso!", Toast.LENGTH_SHORT).show();
                        }else {
                            String err = task.getException().toString();
                            Toast.makeText(Tela_Principal.this, "Falha ao tentar deletar a tarefa" +err, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                
                dialog.dismiss();
            }
        });

        dialog.show();

    }


    private void iniciarComponentes() {
        nomeUsuario = findViewById(R.id.text_user);
        logout = findViewById(R.id.Logout);
        photo_user = findViewById(R.id.Conteiner_user_principal);
        recyclerView = findViewById(R.id.RecyclerView);
        floatingActionButton = findViewById(R.id.button_cad_tarefas);
    }

}