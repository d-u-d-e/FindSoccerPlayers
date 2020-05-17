package com.es.findsoccerplayers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    private TextView selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = findViewById(R.id.reg_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText nameEd = findViewById(R.id.reg_name);
        final EditText surnameEd = findViewById(R.id.reg_surname);
        final EditText emailEd = findViewById(R.id.reg_email);
        final EditText emailConfEd = findViewById(R.id.reg_emailConf);
        final EditText passEd = findViewById(R.id.reg_pass);
        final EditText passConfEd = findViewById(R.id.reg_passConf);
        Button registerBtn = findViewById(R.id.reg_registerBtn);
        final ProgressBar progressBar = findViewById(R.id.reg_progressBar);
        selectedDate = findViewById(R.id.reg_selectDate);

        final FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings  =  new FirebaseFirestoreSettings.Builder().build();
        database.setFirestoreSettings(settings);

        selectedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEd.getText().toString();
                String surname = surnameEd.getText().toString();
                String email = emailEd.getText().toString();
                String emailConf = emailConfEd.getText().toString();
                String pass = passEd.getText().toString();
                String passConf = passConfEd.getText().toString();

                //check if fields are completed without input errors

                String missingField = getString(R.string.field_missing);

                if(selectedDate.getText().toString().equals(getString(R.string.act_register_select_date)))
                    Toast.makeText(RegisterActivity.this, R.string.select_birth_date, Toast.LENGTH_SHORT).show();

                if(TextUtils.isEmpty(name))
                    nameEd.setError(missingField);

                if(TextUtils.isEmpty(surname))
                    surnameEd.setError(missingField);

                if(TextUtils.isEmpty(email))
                    emailEd.setError(missingField);

                if(TextUtils.isEmpty(emailConf))
                    emailConfEd.setError(missingField);

                if(TextUtils.isEmpty(pass))
                    passEd.setError(missingField);

                if(TextUtils.isEmpty(passConf))
                    passConfEd.setError(missingField);

                if(pass.length() < 6)
                    passEd.setError(getString(R.string.pass_too_short));

                if(!email.equals(emailConf))
                    emailEd.setError(getString(R.string.email_not_equal));

                if(!pass.equals(passConf))
                    passEd.setError(getString(R.string.pass_not_equal));

                if (emailEd.getError() != null || passEd.getError() != null ||
                        emailConfEd.getError() != null || passConfEd.getError() != null )
                    return;

                progressBar.setVisibility(View.VISIBLE);

                //everything is fine, create an account
                auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        progressBar.setVisibility(View.INVISIBLE);
                        if(task.isSuccessful()){
                            Utils.showSuccessLoginToast(RegisterActivity.this);
                            //back to login
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            finish();
                        }else
                            Utils.showErrorToast(RegisterActivity.this, task.getException());
                    }
                });
            }
        });
    }

    /**
     * Shows date picker dialog after user hits select date text view
     */
    private void showDatePickerDialog(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    /**
     * sets the date after the user picked it
     */
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        month += 1;
        selectedDate.setTextColor(getResources().getColor(R.color.black));
        selectedDate.setText(dayOfMonth + "/" + month + "/" + year);
    }

    /**
     * hitting back will return to Login Activity
     */
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, LoginActivity.class));
        super.onBackPressed();
    }
}