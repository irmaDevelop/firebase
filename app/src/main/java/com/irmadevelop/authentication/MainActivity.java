package com.irmadevelop.authentication;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "MainActivity";
    private static final int SIGN_IN_GOOGLE_CODE = 1; //por convension es los inicializa con el orden en que los crea
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleApiClient googleApiClient;

    private Button btnCreateAccount;
    private Button btnSignIn;
    private SignInButton btnSignInGoogle;

    private EditText edtEmail;
    private EditText edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCreateAccount = (Button) findViewById(R.id.btnCreateAccount);
        btnSignIn        = (Button) findViewById(R.id.btnSignIn);
        btnSignInGoogle  = (SignInButton) findViewById(R.id.btnSignInGoogle);

        edtEmail         = (EditText) findViewById(R.id.edtEmail);
        edtPassword      = (EditText) findViewById(R.id.edtPassword);

        initialize();

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount(edtEmail.getText().toString(), edtPassword.getText().toString());
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn(edtEmail.getText().toString(), edtPassword.getText().toString());
            }
        });

        btnSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, SIGN_IN_GOOGLE_CODE);
            }
        });
    }

    private void initialize(){
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    Log.w(TAG, "onAuthStateChanged - signed_in" + firebaseUser.getUid()); //LOG WARNING
                    Log.w(TAG, "onAuthStateChanged - signed_in" + firebaseUser.getEmail());
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

    private void createAccount(String email, String password){
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Create Account Success", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this, "Create Account Unsuccess", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signIn(String email, String password){
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Authentication Success", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(MainActivity.this, WelcomeActivity.class);
                    startActivity(i);
                    finish();
                }else {
                    Toast.makeText(MainActivity.this, "Authentication Unsuccess", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signInGoogleFirebase(GoogleSignInResult googleSignInResult){
        if (googleSignInResult.isSuccess()){
            AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInResult.getSignInAccount().getIdToken(),null);
            firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    //ya podemos atrapar los datos en el TASK
                    if(task.isSuccessful()){
                        Toast.makeText(MainActivity.this, "Google Authentication Success", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(MainActivity.this, WelcomeActivity.class);
                        startActivity(i);
                        finish();
                    }else {
                        Toast.makeText(MainActivity.this, "Google Authentication Unsuccess", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            Toast.makeText(MainActivity.this, "Google Sign In Unsuccess", Toast.LENGTH_SHORT).show();
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_GOOGLE_CODE){
            //en realdad en esta linea de codico se esta haciendo el sign in
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            signInGoogleFirebase(googleSignInResult);
        }
    }

    //el metodo siguiente carga automaticamente al momento de agregar el implements de la clases aunque no se va utilizar

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
