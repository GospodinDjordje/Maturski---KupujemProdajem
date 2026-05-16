package com.gospodindjole.maturski

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import android.graphics.Color
import android.widget.AdapterView
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
        var filter = rootView.findViewById<Spinner>(R.id.kategorije)
        val kategorije = mutableListOf("All")
        kategorije.addAll(resources.getStringArray(R.array.Filter))

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.Filter,
            android.R.layout.simple_spinner_item
        ).also {adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            filter.adapter = adapter
        }

        filter.post {
            val textView = filter.selectedView as TextView
            textView.setTextColor(Color.TRANSPARENT)
        }

        //Uporedjivanje naslova sa search barom
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
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

        filter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val filteredList =if (kategorije[position] == kategorije[0]){
                    sviOglasi
                }
                else{
                    sviOglasi.filter {
                        it.kategorija == kategorije[position]
                    }
                }
                adapter.updateList(filteredList)
                val textView = filter.selectedView as TextView
                textView.setTextColor(Color.TRANSPARENT)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                adapter.updateList(sviOglasi)
            }
        }

        adapter.onItemClick = {
            val intent = Intent(requireContext(), PregledOglasa::class.java)
            intent.putExtra("Oglas", it.id_oglasa)
            startActivity(intent)
        }

        return rootView

    }
}