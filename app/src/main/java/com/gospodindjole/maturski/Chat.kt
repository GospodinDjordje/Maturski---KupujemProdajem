package com.gospodindjole.maturski

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.subscribe
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import io.github.jan.supabase.realtime.decodeRecord
import kotlinx.coroutines.launch

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
        val trenutniKorisnikID = supabase.auth.currentUserOrNull()?.id
        val listaPoruka = mutableListOf<Poruka>()
        val razgovori :List<Razgovori>
        val korisnici :List<Korisnik>
        var recyclerView = findViewById<RecyclerView>(R.id.listaPoruka)
        var username = findViewById<MaterialToolbar>(R.id.toolbarChat)

        //Uzimanje informacija iz baze
        runBlocking {
            withContext(Dispatchers.IO) {
                listaPoruka.addAll(supabase.from("poruke").select {
                    filter {
                        eq("id_razgovora",id_razgovora!!)
                    }
                    order("vreme_slanja", Order.ASCENDING)
                }.decodeList<Poruka>())
                razgovori = supabase.from("razgovori").select {
                    filter {
                        eq("id_razgovora",id_razgovora!!)
                    }
                }.decodeList<Razgovori>()
                korisnici = supabase.from("korisnici").select().decodeList<Korisnik>()
            }
        }

        var ImePrimaoca : String = ""
        for (razgovor in razgovori){
            for(korisnik in korisnici){
                if(razgovor.id_kupca==trenutniKorisnikID && korisnik.id==razgovor.id_prodavca){
                    ImePrimaoca=korisnik.username
                }
                else if(razgovor.id_kupca==korisnik.id && razgovor.id_prodavca==trenutniKorisnikID){
                    ImePrimaoca=korisnik.username
                }
            }
        }

        username.title = ImePrimaoca
        var adapter = RecyclerPoruka(listaPoruka)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter =adapter
        recyclerView.scrollToPosition(adapter.itemCount-1)

        val channel = supabase.channel($"chat-${id_razgovora}")
        val changeFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public"){ table="poruke" }
        changeFlow.onEach {
            val novaPoruka = it.decodeRecord<Poruka>()

            if(novaPoruka.id_razgovora == id_razgovora){
                listaPoruka.add(novaPoruka)

                runOnUiThread {
                    adapter.notifyItemInserted(listaPoruka.size-1)
                    recyclerView.scrollToPosition(listaPoruka.size-1)
                }
            }
        }.launchIn(lifecycleScope)
        lifecycleScope.launch {
            channel.subscribe()
        }

    }

    fun posaljiPoruku(view: View){
        var tekst = this.findViewById<TextView>(R.id.tekstPoruke).text.toString()
        val id_raz = intent.getStringExtra("Razgovor").toString()
        val trenutniKorisnikID = supabase.auth.currentUserOrNull()?.id
        var id_primaoca = ""
        val razgovori :List<Razgovori>

        runBlocking {
            withContext(Dispatchers.IO) {
                razgovori = supabase.from("razgovori").select {
                    filter {
                        eq("id_razgovora",id_raz)
                    }
                }.decodeList<Razgovori>()
            }
        }
        for (razgovor in razgovori){
            if(razgovor.id_kupca==trenutniKorisnikID){
                id_primaoca=razgovor.id_prodavca
            }
            else{
                id_primaoca=razgovor.id_kupca
            }
        }
        if (trenutniKorisnikID == null) {
            Toast.makeText(this, "Niste ulogovani", Toast.LENGTH_SHORT).show()
            return
        }
        else if (tekst.isEmpty() == false){
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
    fun Nazad(view: View){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}