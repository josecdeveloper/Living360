package com.android.living360.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.living360.R;
import com.android.living360.app.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "LoginActivity";

    private Button buttonSignin;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignup;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonSignin = (Button) findViewById(R.id.buttonLogin);
        textViewSignup = (TextView) findViewById(R.id.textViewSignup);

        progressDialog = new ProgressDialog(this);

        buttonSignin.setOnClickListener(this);
        textViewSignup.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (firebaseAuth.getCurrentUser() != null) {
            //Send them to their profile
            finish();
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        }

    }

    @Override
    public void onClick(View view) {
        if (view == buttonSignin) {
            userLogin();
        }

        if (view == textViewSignup) {
            finish();
            startActivity(new Intent(this, SignupActivity.class));
        }
    }

    private void userLogin() {
        Log.d(TAG, "userLogin");
        String email = editTextEmail.getText().toString();
        final String password = editTextPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            //email is empty
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show();
            //Stops function from executing further
            return;
        }

        if (TextUtils.isEmpty(password)) {
            //password is empty
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Login in...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                            onAuthSuccess(task.getResult().getUser());
                        } else {
                            Toast.makeText(LoginActivity.this, "You have entered wrong credentials", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void onAuthSuccess(FirebaseUser user) {
        String username = user.getEmail();

        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail());

        // Go to MainActivity
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }

    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);

        database.child("users").child(userId).setValue(user);
    }
}
