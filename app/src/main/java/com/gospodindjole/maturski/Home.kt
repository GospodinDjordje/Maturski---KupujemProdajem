package com.gospodindjole.maturski

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class Home : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        val oglas :List<Oglas>
        var recyclerView = rootView.findViewById<RecyclerView>(R.id.oglasi)

        //Uzimanje informacija iz baze
        runBlocking {
            withContext(Dispatchers.IO) {
                oglas = supabase.from("oglasi").select().decodeList<Oglas>()
            }
        }

        //Pakovanje oglasa u recyclerView
        val sviOglasi = oglas.toList()
        var adapter = RecyclerAdapter(sviOglasi)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter =adapter

        var searchView = rootView.findViewById<SearchView>(R.id.pretrazivacOglasa)

        //Uporedjivanje naslova sa search barom
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
            val intent = Intent(requireContext(), PregledOglasa::class.java)
            intent.putExtra("Oglas", it.id_oglasa)
            startActivity(intent)
        }

        return rootView

    }
}