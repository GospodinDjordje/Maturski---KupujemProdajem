package com.gospodindjole.maturski

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp

//Povezivanje sa Supabase bazom
val supabase = createSupabaseClient(
    supabaseUrl = "https://jenfebqrfencenvwkhva.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImplbmZlYnFyZmVuY2VudndraHZhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzQ2MTcyNTAsImV4cCI6MjA5MDE5MzI1MH0.2wnd9cWv_6XEKz_G4gjF6jkJ6-ENQbg00GVoKzNlWFY"
) {
    httpEngine = OkHttp.create()

    install(Postgrest)
    install(Auth)
    install(Storage)
    install(Realtime)
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)


        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        val user = supabase.auth.currentUserOrNull()

        val HomeFragment = Home()
        val PorukeFragment = Poruke()
        val NoviOglasFragment = Novi_oglas()

        PostaviFragment(HomeFragment)

        //Menjanje fragmenata
        bottomNavigationView.setOnNavigationItemSelectedListener{
            when(it.itemId){
                R.id.main -> PostaviFragment(HomeFragment)
                R.id.poruke -> PostaviFragment(PorukeFragment)
                R.id.novi_oglas -> PostaviFragment(NoviOglasFragment)
                R.id.profil -> {
                    val user = supabase.auth.currentUserOrNull()

                    if (user != null) {
                        PostaviFragment(Profil())
                    } else {
                        PostaviFragment(Login())
                    }
                }
            }
            true
        }
    }

    //Funkcija za menjanje fragmenata
    private fun PostaviFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }
}
