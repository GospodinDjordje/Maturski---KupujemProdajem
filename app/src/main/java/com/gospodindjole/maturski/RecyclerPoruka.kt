package com.gospodindjole.maturski

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

//Adapter za prikazivanje poruka kao listu
class RecyclerPoruka(private var poruka: List<Poruka>): RecyclerView.Adapter<com.gospodindjole.maturski.RecyclerPoruka.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val primljenaPorukaOkvir: TextView = itemView.findViewById(R.id.primljenaPorukaOkvir)
        val primljenaPoruka: TextView = itemView.findViewById(R.id.primljenaPoruka)
        val vremePrimljenePoruke: TextView = itemView.findViewById(R.id.vremePrimljenePoruke)
        val poslataPorukaOkvir: TextView = itemView.findViewById(R.id.poslataPorukaOkvir)
        val poslataPoruka: TextView = itemView.findViewById(R.id.poslataPoruka)
        val vremeSlanjaPoruke: TextView = itemView.findViewById(R.id.vremeSlanjaPoruke)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poruka, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int
    ) {
        val trenutniKorisnikID = supabase.auth.currentUserOrNull()?.id.toString()
        val vreme = poruka[position].vreme_slanja
        val formatirano = vreme?.atZone(ZoneId.systemDefault())?.format(DateTimeFormatter.ofPattern("d.M.yyyy - HH:mm"))

        if(poruka[position].id_primaoca == trenutniKorisnikID) {
            holder.poslataPoruka.visibility =View.GONE
            holder.poslataPorukaOkvir.visibility = View.GONE
            holder.vremeSlanjaPoruke.visibility = View.GONE

            holder.primljenaPoruka.text = poruka[position].tekst_poruke
            holder.vremePrimljenePoruke.text = formatirano
        }
        else if(poruka[position].id_primaoca != trenutniKorisnikID){
            holder.primljenaPoruka.visibility =View.GONE
            holder.primljenaPorukaOkvir.visibility = View.GONE
            holder.vremePrimljenePoruke.visibility = View.GONE

            holder.poslataPoruka.text = poruka[position].tekst_poruke
            holder.vremeSlanjaPoruke.text = formatirano
        }
    }

    override fun getItemCount(): Int = poruka.size

    fun updateList(filteredList: List<Poruka>) {
        poruka = filteredList
        notifyDataSetChanged()
    }
}