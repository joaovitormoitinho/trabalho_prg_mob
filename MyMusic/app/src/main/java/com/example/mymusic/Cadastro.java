package com.example.mymusic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Cadastro extends AppCompatActivity {

    private EditText edit_nome,edit_email,edit_senha;
    private Button bt_cadastrar;
    private View btnSelectedPhoto;
    private ImageView mImagePhoto, iconPhoto;
    private Uri mSelectedUri;

    String[] mensagens = {"Preencha todos os campos","Usuário cadastro com sucesso"};
    String usuarioID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cadastro);

        getSupportActionBar().hide();
        IniciarComponents();

        btnSelectedPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selecionar_foto();
            }
        });

        bt_cadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nome = edit_nome.getText().toString();
                String email = edit_email.getText().toString();
                String senha = edit_senha.getText().toString();

                if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()){
                    Snackbar snackbar = Snackbar.make(view,mensagens[0],Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }else{
                   CadastrarUser(view);
                }
            }
        });
    }

    private void CadastrarUser(View view){

        String email = edit_email.getText().toString();
        String senha = edit_senha.getText().toString();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    SalvarDadosUser();



                    Snackbar snackbar = Snackbar.make(view,mensagens[1],Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }else {
                    String erro;
                    try {
                        throw task.getException();

                    }catch (FirebaseAuthWeakPasswordException e) {
                        erro = "Digite uma senha de no mínino seis caracteres";
                    }catch (FirebaseAuthUserCollisionException e) {
                        erro = "Está conta já existe";
                    }catch (FirebaseAuthInvalidCredentialsException e) {
                        erro = "E-mail inválido";
                    }catch (Exception e){
                        erro = "Erro ao cadastrar o usuário";
                    }

                    Snackbar snackbar = Snackbar.make(view,erro,Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();

                }
            }
        });
    }

    private void SalvarDadosUser(){
        String filename = UUID.randomUUID().toString();
        final StorageReference reference = FirebaseStorage.getInstance().getReference("/images/" + filename);
        reference.putFile(mSelectedUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.i("teste", uri.toString());

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        String profileURL = uri.toString();
                        String nome = edit_nome.getText().toString();

                        Map<String, Object> usuarios = new HashMap<>();
                        usuarios.put("nome", nome);
                        usuarios.put("profileURL", profileURL);

                        DocumentReference documentReference = db.collection("Usuarios").document(usuarioID);
                        documentReference.set(usuarios).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("db", "Sucesso ao salvar os dados");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("db_error", "Erro ao salvar os dados");
                            }
                        });
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("teste-erro", e.getMessage(), e);
            }
        });







    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0){
            mSelectedUri = data.getData();

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mSelectedUri);
                mImagePhoto.setImageDrawable(new BitmapDrawable(bitmap));
                btnSelectedPhoto.setAlpha(0);
                iconPhoto.setAlpha(0);
            } catch (IOException e) {
            }
        }
    }

    private void selecionar_foto(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,0);
    }

    private void IniciarComponents(){

        edit_nome = findViewById(R.id.edt_nome);
        edit_email = findViewById(R.id.edt_email);
        edit_senha = findViewById(R.id.edt_senha);
        bt_cadastrar = findViewById(R.id.btn_cadastrar);
        btnSelectedPhoto = findViewById(R.id.Conteiner_user);
        mImagePhoto = findViewById(R.id.img_photo);
        iconPhoto = findViewById(R.id.icon_add_photo);
    }

}