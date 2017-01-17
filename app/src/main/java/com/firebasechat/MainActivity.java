package com.firebasechat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity {



    public static final int SIGN_IN_REQUEST_CODE =1;


    private FloatingActionButton fab ;

    //Para popular a lista sem esforço
    private FirebaseListAdapter<ChatMessage> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkFirebaseUser();
        sendAnalytic();
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText) findViewById(R.id.input);
                //Chamando a isntancia do firebase database, e dando push e uma nova mensagem com o texto e o usuario
                FirebaseDatabase.getInstance().getReference()
                        .push()
                        .setValue(new ChatMessage(input.getText().toString(),
                                FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
                //Quando fazemos o push poder utilizar o setValue que gera uma nova Key ja que estamos inserindo um novo valor.
                //Caso contrario precisa passar a key
                input.setText("");
            }

        });

    }

private void sendAnalytic(){

    // Create an instance of FirebaseAnalytics
    FirebaseAnalytics fa = FirebaseAnalytics.getInstance(this);

    // Create a Bundle containing information about
    // the analytics event
    Bundle eventDetails = new Bundle();
    eventDetails.putString("MainActivity", "Started Main Activity");

    // Log the event
    fa.logEvent("MainActivity", eventDetails);

}

    private void checkFirebaseUser(){
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            //Caso não esteja logado, ele chama a activity do Firebase que já fará o login completo do user
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_REQUEST_CODE);
        }else{
            //User Já logado
            String displayname =FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            Toast.makeText(this, "Welcome: " +displayname, Toast.LENGTH_SHORT).show();
            displayChatMessages();
        }

    }

    private void displayChatMessages() {
        ListView listOfMessages = (ListView) findViewById(R.id.list_of_messages);
        /*Para instanciar o adapter:
        A reference to the Activity
        The class of the object you're interested in
        The layout of the list items
        A DatabaseReference object*/
        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                //Pegando as reverenciar da view e setando os atributos
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);


                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
                messageTime.setText(DateFormat.format("dd-MM-yyyy", model.getMessageTime()));

            }
        };
        listOfMessages.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_REQUEST_CODE){
            Log.i("Request Code", String.valueOf(requestCode));
            if(resultCode == RESULT_OK){
                Toast.makeText(this, "Logado com sucesso" + resultCode, Toast.LENGTH_SHORT).show();
                displayChatMessages();
            }else{
                Toast.makeText(this, "Não foi possível fazer o login", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Deslogando o usuario
        if(item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this,
                                    "You have been signed out.",
                                    Toast.LENGTH_LONG)
                                    .show();

                            // Close activity
                            finish();
                        }
                    });
        }
        return true;
    }
}
