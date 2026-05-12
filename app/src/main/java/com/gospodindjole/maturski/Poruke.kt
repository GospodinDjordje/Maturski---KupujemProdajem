package com.gospodindjole.maturski

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class Poruke : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_poruke, container, false)
        val razgovor :List<Razgovori>
        var recyclerView = rootView.findViewById<RecyclerView>(R.id.razgovori)

        //Uzimanje informacija iz baze
        runBlocking {
            withContext(Dispatchers.IO) {
                razgovor = supabase.from("razgovori").select().decodeList<Razgovori>()
            }
        }

        //Pakovanje razgovora u recyclerView
        //val sviRazgovori = razgovor.toList()
        var adapter = RecyclerChatovi(razgovor)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter =adapter

        var searchView = rootView.findViewById<SearchView>(R.id.pretrazivacRazgovora)

        //Uporedjivanje naslova sa search barom
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d("SEARCH", "Kuca: $newText")
                val text = newText.orEmpty()

                val filteredList =if (text.isEmpty()){
                    razgovor
                }
                else{
                    razgovor.filter {
                        it.naslov.contains(text, ignoreCase = true)
                    }
                }
                adapter.updateList(filteredList)
                return true
            }
        })

        adapter.onItemClick = {
            val intent = Intent(requireContext(), Chat::class.java)
            intent.putExtra("Razgovor", it.id_razgovora)
            startActivity(intent)
        }

        return rootView
    }
}