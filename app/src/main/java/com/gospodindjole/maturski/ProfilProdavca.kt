package com.gospodindjole.maturski

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ProfilProdavca : AppCompatActivity() {

    //Id koji je poslat pri otvaranju profila iz oglasa
    var ids : String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profil_prodavca)

        // Ucitavanje layouta
        val oglas :List<Oglas>
        val korisnici : List<Korisnik>
        var recyclerView = findViewById<RecyclerView>(R.id.oglasiLicni)

        var id = intent.getStringExtra("vlasnikOglasa")
        ids =id

        //Uzimanje samo podataka kojima se slaze id kreatora, toest id korisnikaa
        runBlocking {
            withContext(Dispatchers.IO) {
                oglas = supabase.from("oglasi").select{
                    filter {
                        eq("id_kreatora",id!!)
                    }
                }.decodeList<Oglas>()
                korisnici = supabase.from("korisnici").select {
                    filter { eq("id",id!!) }
                }.decodeList()
            }
        }

        val sviOglasi = oglas.toList()

        //Definisanje svih promenjivih
        var username : String = ""
        var mesto : String=""
        var starost : String = ""
        var telefon : String = ""
        var ocene : String = ""
        var pozitivnih : String = ""
        var negativnih : String= ""
        var sablon : String= "dd.MM.yyyy" // Sablon za formatiranje datuma
        val reformator = DateTimeFormatter.ofPattern(sablon) // Reformator datuma

        //Definisanje svih objekata
        val Username = findViewById<TextView>(R.id.imeProfila)
        val Mesto = findViewById<TextView>(R.id.Mesto)
        val Starost = findViewById<TextView>(R.id.Starost)
        val Telefon = findViewById<TextView>(R.id.Telefon)
        val Ocene = findViewById<Button>(R.id.Ocene)

        //Ucitavanje vrednosti iz baze
        for(korisnik in korisnici){
            if(korisnik.id == id){
                username= korisnik.username
                mesto = "Mesto: "+korisnik.mesto
                val datum = LocalDate.parse(korisnik.starost_naloga)
                starost = "Clan od: "+ datum.format(reformator)
                telefon = korisnik.telefon.toString()
                pozitivnih = korisnik.pozitivnih_ocena.toString()
                negativnih = korisnik.negativnih_ocena.toString()
                ocene = pozitivnih+"-"+negativnih
            }
        }

        val obojeneOcene = SpannableString(ocene)
        //Pozitivne ocene (Zelene)
        obojeneOcene.setSpan(
            ForegroundColorSpan(Color.GREEN),
            0,
            pozitivnih.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        //Negativne ocene (Crvene)
        obojeneOcene.setSpan(
            ForegroundColorSpan(Color.RED),
            pozitivnih.length+1,
            ocene.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        //Postavljanje teksta
        Username.setText(username)
        Mesto.setText(mesto)
        Starost.setText(starost)
        Telefon.setText(telefon)
        Ocene.setText(obojeneOcene)

        //Ucitavanje oglasa
        var adapter = RecyclerAdapter(oglas)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter =adapter

        //Namestanje Search bara da radi
        var searchView = findViewById<SearchView>(R.id.pretrazivacSvojihOglasa)

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d("SEARCH", "Kuca: $newText")
                val text = newText.orEmpty()

                val filteredList =if (text.isEmpty()){
                    sviOglasi
                }
                else{
                    sviOglasi.filter {
                        it.naslov.contains(text, ignoreCase = true)
                    }
                }
                adapter.updateList(filteredList)
                return true
            }
        })

        adapter.onItemClick = {
            val intent = Intent(this, PregledOglasa::class.java)
            intent.putExtra("Oglas", it.id_oglasa)
            startActivity(intent)
        }

    }

    fun otvoriOcene(view: View){
        val intent = Intent(this, Ocene::class.java)
        intent.putExtra("vlasnikOcena", ids)
        startActivity(intent)
    }
}