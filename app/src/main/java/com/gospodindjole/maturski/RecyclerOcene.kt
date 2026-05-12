package com.gospodindjole.maturski

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter


//Adapter za prikazivanje ocene kao listu
class RecyclerOcene(private var ocena: List<Ocena>): RecyclerView.Adapter<com.gospodindjole.maturski.RecyclerOcene.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ocenaNaslov: TextView = itemView.findViewById(R.id.naslovOcenjenog)
        val imePostavljaca: TextView = itemView.findViewById(R.id.imePostavljaca)
        val opis: TextView = itemView.findViewById(R.id.opisOcene)
        val Slika: ImageView = itemView.findViewById(R.id.pozneg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ocena, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val Ocenica = ocena[position]
        val naslov= Ocenica.naslov
        val opis = Ocenica.opis
        val pozitivna = Ocenica.pozitivna
        val datumpre = Ocenica.created_at
        val datum = LocalDate.parse(datumpre)
        var id = Ocenica.id_posiljaoca

        var sablon : String= "dd.MM.yyyy" // Sablon za formatiranje datuma
        val reformator = DateTimeFormatter.ofPattern(sablon) // Reformator datuma
        val korisnici : List<Korisnik>

        runBlocking {
            withContext(Dispatchers.IO) {
                korisnici = supabase.from("korisnici").select {
                    filter { eq("id",id) }
                }.decodeList()
            }
        }

        var ime = ""
        for(korisnik in korisnici){
            if(korisnik.id == id){
                ime = $"${korisnik.username} - ${datum.format(reformator)}"
            }
        }

        holder.imePostavljaca.text = ime
        holder.ocenaNaslov.text = naslov
        holder.opis.text = opis
        if(pozitivna == true) holder.Slika.setColorFilter(R.color.md_theme_secondaryContainer)
        else if (pozitivna==false) holder.Slika.setColorFilter(R.color.red)
    }

    override fun getItemCount(): Int = ocena.size

    fun updateList(filteredList: List<Ocena>) {
        ocena = filteredList
        notifyDataSetChanged()
    }
}