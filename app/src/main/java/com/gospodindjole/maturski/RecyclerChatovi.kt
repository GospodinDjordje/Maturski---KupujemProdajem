package com.gospodindjole.maturski

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

//Adapter za prikazivanje razgovora kao listu
class RecyclerChatovi(private var chat: List<Razgovori>): RecyclerView.Adapter<com.gospodindjole.maturski.RecyclerChatovi.UserViewHolder>() {

    var onItemClick : ((Razgovori) -> Unit)? = null
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imeCoveka: TextView = itemView.findViewById(R.id.imeCoveka)
        val naslovOglasa : TextView = itemView.findViewById(R.id.naslovOglasaChat)
        val poslednjaPoruka: TextView = itemView.findViewById(R.id.poslednjaPoruka)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_razgovori, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int
    ) {
        val trenutniKorisnikID = supabase.auth.currentUserOrNull()?.id.toString()
        var korisnici : List<Korisnik>
        var oglasi : List<Oglas>
        var poruke : List<Poruka>
        val Korisnicic = chat[position]
        runBlocking {
            withContext(Dispatchers.IO) {
                korisnici = supabase.from("korisnici").select().decodeList<Korisnik>()
                oglasi = supabase.from("oglasi").select().decodeList<Oglas>()
                poruke = supabase.from("poruke").select{
                    filter {
                        or{
                            eq("id_posiljaoca", trenutniKorisnikID)
                            eq("id_primaoca", trenutniKorisnikID)}
                    }
                    order("vreme_slanja", Order.DESCENDING)
                }.decodeList()
            }
        }

        var ime : String = ""
        var naslov : String = ""
        var zadnja_poruka : String
        if(poruke.isEmpty()){
            zadnja_poruka = "Nema Poslatih Poruka"
        }
        else {
            zadnja_poruka = poruke[0].tekst_poruke
        }


        if(Korisnicic.id_prodavca == trenutniKorisnikID) {
            for (korisnik in korisnici){
                if(korisnik.id == Korisnicic.id_kupca){
                    ime = korisnik.username
                }
            }
        }
        else{
            for(korisnik in korisnici){
                if(korisnik.id == Korisnicic.id_prodavca){
                    ime = korisnik.username
                }
            }
        }

        for(oglas in oglasi){
            if(Korisnicic.id_oglasa == oglas.id_oglasa){
                naslov = oglas.naslov
            }
        }




        holder.imeCoveka.text = ime
        holder.naslovOglasa.text = naslov
        holder.poslednjaPoruka.text = zadnja_poruka
    }

    override fun getItemCount(): Int = chat.size

    fun updateList(filteredList: List<Razgovori>) {
        chat = filteredList
        notifyDataSetChanged()
    }
}