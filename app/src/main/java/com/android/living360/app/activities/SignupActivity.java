package com.android.living360.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonRegister;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignin;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        buttonRegister = (Button) findViewById(R.id.buttonRegister);

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);


        textViewSignin = (TextView) findViewById(R.id.textViewSignin);

        progressDialog = new ProgressDialog(this);

        buttonRegister.setOnClickListener(this);
        textViewSignin.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if (v == buttonRegister) {
            registerUser();
        }

        if (v == textViewSignin) {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void registerUser() {

        String email = editTextEmail.getText().toString();
        final String password = editTextPassword.getText().toString();

        if(!isPasswordValid(password)) {
            Toast.makeText(SignupActivity.this, "Password has to be longer than 6 characters", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(email)) {
            //email is empty
            Toast.makeText(SignupActivity.this, "Please enter an email", Toast.LENGTH_SHORT).show();
            //Stops function from executing further
            return;
        }

        if (TextUtils.isEmpty(password)) {
            //password is empty
            Toast.makeText(SignupActivity.this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Registering user...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            //User is successfully registered and logged in
                            //start profile activity here
                            Toast.makeText(SignupActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                            onAuthSuccess(task.getResult().getUser());
                        } else if(!isPasswordValid(password)) {
                            Toast.makeText(SignupActivity.this, "Password must be longer than 6 characters", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignupActivity.this, "Could not register...", Toast.LENGTH_SHORT).show();
                        }

                        progressDialog.dismiss();
                    }
                });


    }

    private boolean isPasswordValid(String password) {
        if (password != null && password.length() > 6) {
            return true;
        } else {
            return false;
        }
    }

    private void onAuthSuccess(FirebaseUser user) {
        String username = user.getEmail();

        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail());

        // Go to MainActivity
        startActivity(new Intent(SignupActivity.this, HomeActivity.class));
        finish();
    }

    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);

        database.child("users").child(userId).setValue(user);
    }
}
