package com.irmadevelop.authentication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class WelcomeActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "WelcomeActivity";
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleApiClient googleApiClient;

    private TextView tvUserDetail;
    private Button   btnSignOut;
    private ImageView imvPhoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        tvUserDetail=(TextView) findViewById(R.id.tvUserDetail);
        btnSignOut=(Button) findViewById(R.id.btnSingOut);
        imvPhoto=(ImageView) findViewById(R.id.imvPhoto);

        inicialize();

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //cuando alguien de clic al boton sign out se debe cerrar la sesion
                signOut();
            }
        });

    }

    private void signOut(){
        firebaseAuth.signOut();

        if (Auth.GoogleSignInApi != null){

            Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()){
                        //aqui se finaliza la ejecucion de la presente actividad, y se manda ejecuta el main
                        //que es la actividad inicial.
                        Intent i = new Intent(WelcomeActivity.this , MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                    else{
                        //No salir permanecer, quedarse y mostrar un mensaje
                        Toast.makeText(WelcomeActivity.this, "Error in Google Sign Out", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (LoginManager.getInstance() !=null){
            LoginManager.getInstance().logOut();
        }


    }

    private void inicialize(){
        //detecta que algo paso en la sesion entonces muestra los datos del usuario
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    tvUserDetail.setText("IDUser: " + firebaseUser.getUid() + " Email: " + firebaseUser.getEmail() + "Url: " + firebaseUser.getPhotoUrl());
                    Picasso.get().load(firebaseUser.getPhotoUrl()).into(imvPhoto);
                } else {
                    Log.w(TAG, "onAuthStateChanged - signed_out");
                }
            }
        };

        //Inicializacion de Google Account
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }


    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
