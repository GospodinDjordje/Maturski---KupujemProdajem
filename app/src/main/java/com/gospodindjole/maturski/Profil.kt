package com.gospodindjole.maturski

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Profil : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Ucitavanje layouta
        val view = inflater.inflate(R.layout.fragment_profil, container, false)
        val oglas :List<Oglas>
        val korisnici : List<Korisnik>
        var recyclerView = view.findViewById<RecyclerView>(R.id.oglasiLicni)
        val trenutniKorisnikID = supabase.auth.currentUserOrNull()?.id


        //Uzimanje samo podataka kojima se slaze id kreatora, toest id korisnikaa
        runBlocking {
            withContext(Dispatchers.IO) {
                oglas = supabase.from("oglasi").select{
                    filter {
                        eq("id_kreatora",trenutniKorisnikID!!)
                    }
                }.decodeList<Oglas>()
                korisnici = supabase.from("korisnici").select {
                    filter { eq("id",trenutniKorisnikID!!) }
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
        val Username = view.findViewById<TextView>(R.id.imeProfila)
        val Mesto = view.findViewById<TextView>(R.id.Mesto)
        val Starost = view.findViewById<TextView>(R.id.Starost)
        val Telefon = view.findViewById<TextView>(R.id.Telefon)
        val Ocene = view.findViewById<Button>(R.id.Ocene)

        //Ucitavanje vrednosti iz baze
        for(korisnik in korisnici){
            if(korisnik.id == trenutniKorisnikID){
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
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter =adapter

        //Namestanje Search bara da radi
        var searchView = view.findViewById<SearchView>(R.id.pretrazivacSvojihOglasa)

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

        //Listeneri za dugmice
        val Logautovanje = view.findViewById<View>(R.id.unlog)
        Logautovanje?.setOnClickListener { logOut() }

        val Editovanje = view.findViewById<View>(R.id.editProfile)
        Editovanje?.setOnClickListener { otvoriEdit() }

        val vidiOcene = view.findViewById<Button>(R.id.Ocene)
        vidiOcene?.setOnClickListener { otvoriOcene() }

        adapter.onItemClick = {
            val intent = Intent(requireContext(), PregledOglasa::class.java)
            intent.putExtra("Oglas", it.id_oglasa)
            startActivity(intent)
        }

        return view
    }

    fun logOut() {
        //Logout iz naloga i vracanje na Main fragment
            runBlocking {
                withContext(Dispatchers.IO) {
                    com.gospodindjole.maturski.supabase.auth.signOut()
                }
            }
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
    }
    fun otvoriEdit(){ //Otvaranje EditProfile activity
        val intent = Intent(requireContext(), EditProfil::class.java)
        startActivity(intent)
    }
    fun otvoriOcene(){//Otvaranje Ocene activity
        val intent = Intent(requireContext(), Ocene::class.java)
        val trenutniKorisnikID = supabase.auth.currentUserOrNull()?.id
        intent.putExtra("vlasnikOcena", trenutniKorisnikID)
        startActivity(intent)
    }
}