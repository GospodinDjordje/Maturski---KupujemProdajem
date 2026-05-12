package com.gospodindjole.maturski

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class Chat : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var id_razgovora = intent.getStringExtra("Razgovor")
        val poruke :List<Poruka>
        var recyclerView = findViewById<RecyclerView>(R.id.listaPoruka)

        //Uzimanje informacija iz baze
        runBlocking {
            withContext(Dispatchers.IO) {
                poruke = supabase.from("poruke").select {
                    filter {
                        eq("id_razgovora",id_razgovora!!)
                    }
                    order("vreme_slanja", Order.DESCENDING)
                }.decodeList<Poruka>()
            }
        }

        var adapter = RecyclerPoruka(poruke)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter =adapter


    }

    fun posaljiPoruku(view: View){
        var tekst = this.findViewById<TextView>(R.id.tekstPoruke).toString()
        val id_raz = intent.getStringExtra("Razgovor").toString()
        val trenutniKorisnikID = supabase.auth.currentUserOrNull()?.id.toString()
        var id_primaoca = ""
        val poruke :List<Poruka>

        runBlocking {
            withContext(Dispatchers.IO) {
                poruke = supabase.from("poruke").select {
                    filter {
                        eq("id_razgovora",id_raz)
                    }
                }.decodeList<Poruka>()
            }
        }
        for (poruka in poruke){
            if(poruka.id_posiljaoca==trenutniKorisnikID){
                id_primaoca=poruka.id_primaoca
            }
        }

        if (tekst.isEmpty() == false){
            val poruka = Poruka(
                id_razgovora = id_raz,
                id_posiljaoca =trenutniKorisnikID,
                id_primaoca = id_primaoca,
                tekst_poruke = tekst
            )

            poruka?.let {
                runBlocking {
                    withContext(Dispatchers.IO){
                        supabase.from("poruke").insert(it)
                    }
                }
            }

            findViewById<EditText>(R.id.tekstPoruke).text.clear()
        } else{
            Toast.makeText(this, "Unesite Poruku.", Toast.LENGTH_SHORT).show()
        }
    }
}