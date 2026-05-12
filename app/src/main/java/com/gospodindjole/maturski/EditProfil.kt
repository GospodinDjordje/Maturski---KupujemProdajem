package com.gospodindjole.maturski

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

var isLozinka: Boolean = true
class EditProfil : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profil)
        //Oznacavanje svih objekata u xml fajlu
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)
        val parameter = linearLayout.layoutParams
        val trenutniKorisnikID = supabase.auth.currentUserOrNull()?.id
        val korisnik : List<Korisnik>
        val editUsername = findViewById<EditText>(R.id.editUsername)
        val editTelefon = findViewById<EditText>(R.id.editTelefon)
        val editMesto = findViewById<EditText>(R.id.editMesto)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        runBlocking {// Ucitavanje baze
            withContext(Dispatchers.IO) {
                korisnik = supabase.from("korisnici").select().decodeList<Korisnik>()
            }
        }

        for(Korisnik in korisnik){//Preuzimanje podataka iz baze i stavljanje ih u postojece objekte
            if(Korisnik.id == trenutniKorisnikID){
                val username = Korisnik.username
                val telefon = Korisnik.telefon
                val mesto = Korisnik.mesto

                editUsername.setText(username)
                editTelefon.setText(telefon)
                editMesto.setText(mesto)

                break
            }
        }

        val checkBox = findViewById<CheckBox>(R.id.checkBox)
        val lozinkaField = findViewById<EditText>(R.id.editLozinka)
        checkBox.isChecked = true

        //Sakriti polje za lozinku ako je ukljucen checkBox
        checkBox.setOnCheckedChangeListener {_, isChecked ->
            if (isChecked == false) {
                parameter.height = LinearLayout.LayoutParams.WRAP_CONTENT
                lozinkaField.isEnabled = false
                lozinkaField.visibility = View.GONE
                isLozinka = false

            } else {
                parameter.height = LinearLayout.LayoutParams.WRAP_CONTENT
                lozinkaField.isEnabled = true
                lozinkaField.visibility = View.VISIBLE
                isLozinka = true
            }
        }
    }

    //Menjanje podataka u bazi
    fun prosledi(view: View) {
        val username = findViewById<EditText>(R.id.editUsername).text.toString()
        val telefon = findViewById<EditText>(R.id.editTelefon).text.toString()
        val mesto = findViewById<EditText>(R.id.editMesto).text.toString()
        val lozinka = findViewById<EditText>(R.id.editLozinka).text.toString()
        val trenutniKorisnikID = supabase.auth.currentUserOrNull()?.id

        Log.d("KORISNIK", trenutniKorisnikID.toString())

        try {
            if (isLozinka) {
                if (username.isNotEmpty() && telefon.isNotEmpty() && mesto.isNotEmpty() && lozinka.isNotEmpty() && trenutniKorisnikID!!.isNotEmpty()) {
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            supabase.from("korisnici").update(
                                {
                                    set("username", username)
                                    set("telefon", telefon)
                                    set("mesto", mesto)
                                }
                            ) {
                                filter {
                                    eq("id", trenutniKorisnikID)
                                }
                            }

                            supabase.auth.updateUser {
                                password = lozinka
                            }
                        }
                    }

                    Toast.makeText(this@EditProfil, "Uspesno ste promenili podatke", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    if(intent.resolveActivity(packageManager) != null){
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(this, "Popunite sva polja.", Toast.LENGTH_LONG).show()
                }
            } else {
                if (username.isNotEmpty() && telefon.isNotEmpty() && mesto.isNotEmpty() && trenutniKorisnikID!!.isNotEmpty()) {
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            supabase.from("korisnici").update(
                                {
                                    set("username", username)
                                    set("telefon", telefon)
                                    set("mesto", mesto)
                                }
                            ) {
                                filter {
                                    eq("id", trenutniKorisnikID)
                                }
                            }

                        }
                    }

                    Toast.makeText(this@EditProfil, "Uspesno ste promenili podatke", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    if(intent.resolveActivity(packageManager) != null){
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(this, "Popunite sva polja.", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e("GRESKA", e.toString())
            Toast.makeText(this, "Doslo je do greske. Molimo pokusajte kasnije", Toast.LENGTH_LONG).show()
        }
    }
}