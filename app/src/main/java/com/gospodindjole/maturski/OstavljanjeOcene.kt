package com.gospodindjole.maturski

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ButtonColors
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class OstavljanjeOcene : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ostavljanje_ocene)
        var idPrimaoca = intent.getStringExtra("primaocOcena")
        var trenutniKorisnikID = supabase.auth.currentUserOrNull()?.id
        var naslov = intent.getStringExtra("naslovOglasa")
        val naslovView = findViewById<TextView>(R.id.naslovOcene)
        naslovView.text = naslov
        val dugme = findViewById<Button>(R.id.OstaviOcenu)

        var pozitivnihOcena: Int =0
        var negativnihOcena: Int =0

        dugme.setOnClickListener {
            val opisOcene = findViewById<TextView>(R.id.opisOcena).text.toString()
            val pozitivna = findViewById<CheckBox>(R.id.Zadovoljni).isChecked
            val finalniNaslov = naslovView.text.toString()

            if(finalniNaslov.isEmpty() == false && opisOcene.isEmpty() == false){
                lifecycleScope.launch {
                    var ocena = Ocena(
                        id_primaoca = idPrimaoca,
                        id_posiljaoca = trenutniKorisnikID,
                        opis = opisOcene,
                        pozitivna = pozitivna,
                        naslov = naslov
                    )
                    ocena?.let {
                        runBlocking {
                            withContext(Dispatchers.IO){
                                supabase.from("ocene").insert(it)
                                val korisnik = supabase.from("korisnici").select{
                                    filter { eq("id", idPrimaoca!!) }
                                }.decodeSingle<Korisnik>()

                                if(pozitivna == true){
                                    val response = supabase.from("korisnici").update({
                                        set("pozitivnih_ocena", korisnik.pozitivnih_ocena + 1)
                                    }){
                                        filter {
                                            eq("id", idPrimaoca!!)
                                        }
                                        select()
                                    }
                                }
                                else{
                                    supabase.from("korisnici").update({
                                        set("negativnih_ocena", korisnik.negativnih_ocena  + 1)
                                    }){
                                        filter {
                                            eq("id", idPrimaoca!!)
                                        }
                                        select()
                                    }
                                }
                            }
                        }
                    }

                    val intent = Intent(this@OstavljanjeOcene, MainActivity::class.java)
                    startActivity(intent)
                }
            }
            else {
                Toast.makeText(this, "Napisite naslov i opis.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}