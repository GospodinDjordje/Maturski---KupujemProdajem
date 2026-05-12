package com.gospodindjole.maturski

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView

import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.TopAppBar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class Novi_oglas : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val selectedImages = mutableListOf<Uri>()
    private lateinit var slikeAdapter: AdapterSlika
    private var id_slike = generisiSlikaId()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_novi_oglas, container, false)

        val kategorije = resources.getStringArray(R.array.Kategorije)
        val dropdown = rootView.findViewById<Spinner>(R.id.Kategorija)
        val prosledi = rootView.findViewById<Button>(R.id.napraviOglasDugme)
        recyclerView = rootView.findViewById<RecyclerView>(R.id.recyclerViewSlike)

        recyclerView.layoutManager = GridLayoutManager(requireContext(),4)

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.Kategorije,
            android.R.layout.simple_spinner_item
        ).also {adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            dropdown.adapter = adapter
        }

        slikeAdapter = AdapterSlika(selectedImages){
            openGallery()
        }
        recyclerView.adapter = slikeAdapter

        if(savedInstanceState != null){
            val savedUris = savedInstanceState.getParcelableArrayList<Uri>("selected_images")
            savedUris?.let {
                selectedImages.addAll(it)
                slikeAdapter.notifyDataSetChanged()
            }
        }

        prosledi.setOnClickListener {
            lifecycleScope.launch { uploadSlike()
                val imageUris = selectedImages
                val naslov = rootView.findViewById<EditText>(R.id.naslovOglasa).text.toString()
                val kategorija = dropdown.selectedItem.toString()
                val opis = rootView.findViewById<EditText>(R.id.opisOglasa).text.toString()
                val cena = rootView.findViewById<EditText>(R.id.cenaOglasa).text.toString()
                val id_kreatora = supabase.auth.currentUserOrNull()?.id
                val one_time = rootView.findViewById<CheckBox>(R.id.oneTime).isChecked

                val trenutniIdSlike = id_slike
                val linkSlike = "https://jenfebqrfencenvwkhva.supabase.co/storage/v1/object/public/slike/uploads/"

                var slike= mutableListOf<String>()
                var brojSlika = imageUris.size

                for(i in 0 until  brojSlika){
                    slike.add($"${linkSlike}${trenutniIdSlike}-${i}.jpg")
                }


                if (id_kreatora != null){

                    if (naslov.isEmpty() == false && kategorija.isEmpty() == false && opis.isEmpty() == false && cena.isEmpty() == false){

                        if(slike[1].isEmpty() == false){

                            val oglas =  Oglas(
                                naslov = naslov,
                                kategorija = kategorija,
                                opis = opis,
                                cena = cena,
                                id_kreatora = id_kreatora,
                                one_time = one_time,
                                slike = slike
                            )

                            oglas?.let {
                                runBlocking {
                                    withContext(Dispatchers.IO){
                                        supabase.from("oglasi").insert(it)
                                    }
                                }
                            }

                            rootView.findViewById<EditText>(R.id.naslovOglasa).text.clear()
                            rootView.findViewById<EditText>(R.id.opisOglasa).text.clear()
                            rootView.findViewById<EditText>(R.id.cenaOglasa).text.clear()


                            id_slike = generisiSlikaId()
                            val intent = Intent(context, MainActivity::class.java)
                            startActivity(intent)

                        }else{
                            Toast.makeText(context, "Postavite bar jednu sliku.", Toast.LENGTH_SHORT).show()
                        }

                    }else{
                        Toast.makeText(context, "Popunite sva polja.", Toast.LENGTH_SHORT).show()
                    }

                } else{
                    Toast.makeText(context, "Ulogujte se da bi postavili oglas.", Toast.LENGTH_SHORT).show()
                }
            }

        }
        return rootView
    }


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openGallery()
        }
    }


    private val pickImagesLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedImages.addAll(uris)
            slikeAdapter.notifyDataSetChanged()
        }
    }

    private fun openGallery() {
        pickImagesLauncher.launch("image/*")
    }

    private fun generisiSlikaId(): String{
        val allowedChars = ('A' .. 'Z') + ('a' .. 'z') + ('0' .. '9')
        val duzina = 15
        return (1 .. duzina)
            .map { allowedChars.random() }
            .joinToString("")
    }

    public suspend fun uploadSlike(){
        val bucket = supabase.storage.from("slike")
        val imageUris = selectedImages
        val trenutniIdSlike = id_slike
        for(i in imageUris.indices){
            val slika = imageUris[i]
            val inputStream =context?.contentResolver?.openInputStream(slika)
            val bytes = inputStream!!.readBytes()

            val fileName = trenutniIdSlike+"-"+i+".jpg"
            val path = "uploads/"+fileName
            bucket.upload(path = path, data = bytes)
        }
    }


    private inner class AdapterSlika(
        private val imageUris: MutableList<Uri>,
        private val dodajClick: () -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        private val TIP_SLIKA = 0
        private val TIP_DUGME = 1

        override fun getItemCount(): Int {
            return if (imageUris.size >= 8) {
                imageUris.size
            } else {
                imageUris.size + 1
            }
        }

        override fun getItemViewType(position: Int): Int{
            return if (position == imageUris.size) TIP_DUGME else TIP_SLIKA
        }

        inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivImage: ImageView = itemView.findViewById(R.id.ivImage)
            val dugmeSkloni: ImageView = itemView.findViewById(R.id.dugmeIzbrisi)
        }

        inner class DodajViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            val dodajSliku: ImageView = itemView.findViewById(R.id.dodajSliku)
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if(viewType == TIP_SLIKA){
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_slika, parent, false)
                ImageViewHolder(view)
            }else{
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dodaj_sliku, parent,false)
                DodajViewHolder(view)
            }
        }


        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(holder is ImageViewHolder){
                val uri = imageUris[position]
                holder.ivImage.setImageURI(uri)

                holder.dugmeSkloni.setOnClickListener {
                    val poz = holder.adapterPosition
                    if(poz != RecyclerView.NO_POSITION){
                        skloniSliku(poz)
                    }
                }
            }else if (holder is DodajViewHolder){
                holder.dodajSliku.setOnClickListener {
                    dodajClick()
                }
            }

        }

        private fun skloniSliku(pozicija: Int) {
            imageUris.removeAt(pozicija)
            notifyDataSetChanged()
        }


    }

}

