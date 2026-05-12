package com.gospodindjole.maturski


import android.graphics.BitmapFactory
import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

//Adapter za prikazivanje oglasa kao listi
class RecyclerAdapter(private var oglas: List<Oglas>): RecyclerView.Adapter<com.gospodindjole.maturski.RecyclerAdapter.UserViewHolder>() {
    var onItemClick : ((Oglas) -> Unit)? = null

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val oglasNaslov: TextView = itemView.findViewById(R.id.oglasNaslov)
        val oglasCena: TextView = itemView.findViewById(R.id.oglasCena)
        val idOglasa: TextView = itemView.findViewById(R.id.idOglasa)
        val slikaPreview: ImageView = itemView.findViewById(R.id.slika)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_oglas, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val Oglasov = oglas[position]
        val naslov= Oglasov.naslov
        val kategorija = Oglasov.kategorija
        val cena = Oglasov.cena
        val id = Oglasov.id_oglasa
        val kreator_id = Oglasov.id_kreatora
        val onetime = Oglasov.one_time
        val slike = Oglasov.slike


        holder.oglasNaslov.text = naslov
        holder.oglasCena.text = cena
        holder.idOglasa.text = id
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = BitmapFactory.decodeStream(URL(slike[0]).openStream())

            withContext(Dispatchers.Main) {
                holder.slikaPreview.setImageBitmap(bitmap)
            }
        }

        holder.itemView.setOnClickListener{
            onItemClick?.invoke(Oglasov)
        }
    }

    override fun getItemCount(): Int = oglas.size

    fun updateList(filteredList: List<Oglas>) {
        oglas = filteredList
        notifyDataSetChanged()
    }
}