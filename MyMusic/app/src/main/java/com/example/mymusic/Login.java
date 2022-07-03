package com.example.mymusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.ktx.Firebase;

public class Login extends AppCompatActivity {

    private TextView text_cadastro;
    private EditText edit_email,edit_senha;
    private Button btn_entrar;
    private ProgressBar progressBar;
    String[] mensagens = {"Preencha todos os campos"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();
        IniciarComponentes();

        text_cadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Login.this, Cadastro.class);
                startActivity(intent);
            }
        });

        btn_entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = edit_email.getText().toString();
                String senha = edit_senha.getText().toString();

                if (email.isEmpty() || senha.isEmpty()){
                    Snackbar snackbar = Snackbar.make(view,mensagens[0],Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }else {
                    AutenticarUser(view);
                }
            }
        });
    }

    private void AutenticarUser(View view){

        String email = edit_email.getText().toString();
        String senha = edit_senha.getText().toString();

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    progressBar.setVisibility(View.VISIBLE);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            TelaPrincipal();
                        }
                    }, 2000);
                }else {
                    String erro;
                    try {
                        throw task.getException();
                    }catch (Exception e){
                        erro = "Erro ao fazer login";
                    }
                    Snackbar snackbar = Snackbar.make(view,erro,Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser userAtual = FirebaseAuth.getInstance().getCurrentUser();

        if (userAtual != null) {
            TelaPrincipal();
        }
    }

    private void TelaPrincipal(){
        Intent intent = new Intent(Login.this,Tela_Principal.class);
        startActivity(intent);
        finish();
    }



    private void IniciarComponentes(){

        text_cadastro = findViewById(R.id.text_cadastro);
        edit_email = findViewById(R.id.edt_text_email);
        edit_senha = findViewById(R.id.edt_text_senha);
        btn_entrar = findViewById(R.id.btn_entrar);
        progressBar = findViewById(R.id.barra_progresso);
    }
}