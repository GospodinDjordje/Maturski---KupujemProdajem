package com.gospodindjole.maturski

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PregledOglasa : AppCompatActivity() {


    //var id_oglasa : String = ""
    var vlasnikId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pregled_oglasa)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val Naslov = this.findViewById<TextView>(R.id.nazivOglasa)
        val Cena = this.findViewById<TextView>(R.id.cenaOglas)
        val Jednokratna = this.findViewById<TextView>(R.id.jednokratanOglas)
        val Opis = this.findViewById<TextView>(R.id.opisPrikazanogOglasa)
        val imeProfila = this.findViewById<TextView>(R.id.imeProfilaOglas)
        val Mesto = this.findViewById<TextView>(R.id.MestoOglas)
        val Starost = this.findViewById<TextView>(R.id.StarostOglas)
        val Ocenice = this.findViewById<Button>(R.id.OceneOglasenje)
        val brojSlike = this.findViewById<TextView>(R.id.brojSlike)

        val Pager = this.findViewById<ViewPager2>(R.id.viewPagerMain)

        val oglas :List<Oglas>
        val korisnici : List<Korisnik>
        var id_oglasa = intent.getStringExtra("Oglas").toString()
        val trenutniKorisnikID = supabase.auth.currentUserOrNull()?.id.toString()

        runBlocking {
            withContext(Dispatchers.IO) {
                oglas = supabase.from("oglasi").select{
                    filter {
                        eq("id_oglasa",id_oglasa!!)
                    }
                }.decodeList<Oglas>()
                korisnici = supabase.from("korisnici").select().decodeList()
            }
        }

        //Definisanje svih promenjivih
        var username : String = ""
        var mesto : String=""
        var starost : String = ""
        var ocene : String = ""
        var pozitivnih : String = ""
        var negativnih : String= ""
        var sablon : String= "dd.MM.yyyy" // Sablon za formatiranje datuma
        val reformator = DateTimeFormatter.ofPattern(sablon) // Reformator datuma

        var naslov : String =""
        var cena : String =""
        var jednokratna : Boolean = false
        var opis : String = ""
        var slike :List<String> = emptyList()

        for(oglasi in oglas){
            if(oglasi.id_oglasa==id_oglasa){
                vlasnikId=oglasi.id_kreatora
                naslov=oglasi.naslov
                cena=oglasi.cena
                jednokratna=oglasi.one_time
                opis=oglasi.opis
                slike=oglasi.slike
            }
        }
        for(korisnik in korisnici){
            if(korisnik.id == vlasnikId){
                username= korisnik.username
                mesto = "Mesto: "+korisnik.mesto
                val datum = LocalDate.parse(korisnik.starost_naloga)
                starost = "Clan od: "+ datum.format(reformator)
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

        //Postavljanje slika
        val ScrollAdapter = ScrollSlikaAdapter(slike)
        Pager.adapter = ScrollAdapter

        Pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                brojSlike.text=$"${position+1}/${slike.size}"
            }
        })

        //Postavljanje teksta
        imeProfila.setText(username)
        Mesto.setText(mesto)
        Starost.setText(starost)
        Ocenice.setText(obojeneOcene)

        Naslov.setText(naslov)
        Cena.setText(cena)
        Opis.setText(opis)
        if(jednokratna == true){
            Jednokratna.setText("Jednokratan Oglas!")
        }
        else{
            Jednokratna.setText("Nije jednokratan!")
        }

        val pokreniRazgovor = findViewById<ImageView>(R.id.pokreniRazgovorDugme)
        pokreniRazgovor?.setOnClickListener {
            var id_prodavca = vlasnikId
            var id_kupca = trenutniKorisnikID
            var id_razgovora = ""
            if(id_prodavca != id_kupca){

                val razgovori :List<Razgovori>
                runBlocking {
                    withContext(Dispatchers.IO) {
                        razgovori = supabase.from("razgovori").select().decodeList<Razgovori>()
                    }
                }

                for(razgovor in razgovori){
                    if(razgovor.id_oglasa == id_oglasa && razgovor.id_kupca == id_kupca){
                        id_razgovora = razgovor.id_razgovora

                    }
                }
                if(id_razgovora == ""){
                    id_razgovora=generisiIdRazgovora()
                    val razgovor = Razgovori(
                        id_razgovora = id_razgovora,
                        id_prodavca = id_prodavca,
                        id_kupca = id_kupca,
                        id_oglasa = id_oglasa,
                        naslov = naslov
                    )
                    razgovor?.let {
                        runBlocking {
                            withContext(Dispatchers.IO){
                                supabase.from("razgovori").insert(it)
                            }
                        }
                    }
                }

                val intent = Intent(this, Chat::class.java)
                intent.putExtra("Razgovor", id_razgovora)
                startActivity(intent)

            }else{
                Toast.makeText(this, "Ne mozete poslati poruku sebi", Toast.LENGTH_LONG).show()
            }

        }

    }
    fun otvoriProdavca(view: View){
        val intent = Intent(this, ProfilProdavca::class.java)
        intent.putExtra("vlasnikOglasa", vlasnikId)
        startActivity(intent)
    }
    fun otvoriOceneOglas(view: View){
        val intent = Intent(this, Ocene::class.java)
        intent.putExtra("vlasnikOcena", vlasnikId)
        startActivity(intent)
    }
    private fun generisiIdRazgovora(): String{
        val allowedChars = ('A' .. 'Z') + ('a' .. 'z') + ('0' .. '9')
        val duzina = 15
        return (1 .. duzina)
            .map { allowedChars.random() }
            .joinToString("")
    }

}