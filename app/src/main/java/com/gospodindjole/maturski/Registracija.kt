package com.gospodindjole.maturski

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class Registracija : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registracija)

    }

    fun registerUser(view: View) {
        //Definisanje svih objekata
        val imeKorisnika = findViewById<EditText>(R.id.imeKorisnika).text.toString()
        val prezimeKorisnika = findViewById<EditText>(R.id.prezimeKorisnika).text.toString()
        var email = findViewById<EditText>(R.id.editTextEmailAddress).text.toString()
        var password = findViewById<EditText>(R.id.password).text.toString()
        val usernameKorisnika = findViewById<EditText>(R.id.UsernameKorisnika).text.toString()
        val telefonKorisnika = findViewById<EditText>(R.id.telefonKorisnika).text.toString()
        val mestoKorisnika = findViewById<EditText>(R.id.mestoKorisnika).text.toString()

        email = email.trim(' ')
        password = password.trim(' ')

        Log.d("MAIL", email)
        Log.d("LOZINKA", password)

        if (email.isEmpty() == false && password.isEmpty() == false && imeKorisnika.isEmpty() == false && prezimeKorisnika.isEmpty() == false && usernameKorisnika.isEmpty() == false && telefonKorisnika.isEmpty() == false && mestoKorisnika.isEmpty() == false)
        {
            if (password.length >= 6)
            {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        signUp(email, password)
                    }
                }

                val id_korisnika = supabase.auth.currentUserOrNull()?.id
                val email_korisnika = supabase.auth.currentUserOrNull()?.email

                if (id_korisnika == null) {
                    Log.e("AUTH", "User je null!")
                    return
                }
                //Uploadovanje podataka na bazu
                val korisnik: Korisnik? = id_korisnika?.let {
                    email_korisnika?.let {
                        Korisnik(
                            id = id_korisnika,
                            ime = imeKorisnika,
                            prezime = prezimeKorisnika,
                            email = email_korisnika,
                            username = usernameKorisnika,
                            telefon = telefonKorisnika,
                            mesto = mestoKorisnika
                        )
                    }
                }

                korisnik?.let {//Dodavanje podataka o korisniku u bazu
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            supabase.from("korisnici").insert(it)
                        }
                    }
                }

                val intent = Intent(this, MainActivity::class.java)
                if(intent.resolveActivity(packageManager) != null){
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this, "Lozinka mora da ima najmanje 6 karaktera", Toast.LENGTH_LONG).show()
            }

        } else {
            Toast.makeText(this, "Popunite sva polja.", Toast.LENGTH_LONG).show()
        }


    }

    //Auth procedura za registraciju korisnika na Supabase
    suspend fun signUp(mail: String, passwd: String) {
        supabase.auth.signUpWith(Email) {
            email = mail
            password = passwd
        }
    }
}