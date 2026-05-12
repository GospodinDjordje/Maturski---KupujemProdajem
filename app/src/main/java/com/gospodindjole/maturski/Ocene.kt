package com.gospodindjole.maturski

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class Ocene : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ocene)

        var id = intent.getStringExtra("vlasnikOcena")
        val ocene :List<Ocena>
        val korisnici : List<Korisnik>
        var recyclerView = this.findViewById<RecyclerView>(R.id.ocene)

        //Uzimanje informacija iz baze
        runBlocking {
            withContext(Dispatchers.IO) {
                ocene = supabase.from("ocene").select().decodeList<Ocena>()
                korisnici = supabase.from("korisnici").select {
                    filter { eq("id",id!!) }
                }.decodeList()
            }
        }

        val imeKorisnika = this.findViewById<TextView>(R.id.ImeOcenjenog)
        for(korisnik in korisnici){
            if(korisnik.id == id){
                imeKorisnika.text = korisnik.username
            }
        }
        //Pakovanje oglasa u recyclerView
        val sveOcene = ocene.toList()
        var adapter = RecyclerOcene(sveOcene)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter =adapter

        val Dobar = this.findViewById<View>(R.id.dobar)
        Dobar?.setOnClickListener {
            val filter = sveOcene.filter { it.pozitivna == true }
            adapter.updateList(filter)
        }

        val Los = this.findViewById<View>(R.id.los)
        Los?.setOnClickListener {
            val filter = sveOcene.filter { it.pozitivna == false }
            adapter.updateList(filter)
        }
    }


}