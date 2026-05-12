package com.gospodindjole.maturski

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class Login : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Ucitavanje layouta
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        //Listener za dugme koje otvara register prozor
        val otvaranjeRegistratora = view?.findViewById<View>(R.id.button)
        otvaranjeRegistratora?.setOnClickListener { openSignUp() }

        //Listener za dugme za loginovanje
        val Loginovanje = view?.findViewById<View>(R.id.loginButton)
        Loginovanje?.setOnClickListener {  login() }

        return view
    }

    //Samo loginovanje
    suspend fun signIn(username: String, passwd: String) {
        supabase.auth.signInWith(Email) {
            email = username
            password = passwd
        }
    }

    //povezivanje svih polja, njihovih tekstova i proveravanje sa bazom
    fun login() {
        val emailEdit = view?.findViewById<TextInputEditText>(R.id.email)
        val passwordEdit = view?.findViewById<TextInputEditText>(R.id.password)

        var emails = emailEdit?.text.toString()
        var password = passwordEdit?.text.toString()

        emails = emails.trim(' ')
        password = password.trim(' ')

        Log.d("EMAIL", emails)
        Log.d("LOZINKA", password)

        try {
            runBlocking {
                withContext(Dispatchers.IO) {
                    signIn(emails, password)
                }
            }

            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)

        } catch (e: AuthRestException) {
            val textView = view?.findViewById<TextView>(R.id.textView2)
            textView?.text = "Pogresni podaci"
        }
    }

    //Otvaranje Register prozora
    fun openSignUp(){
        val intent = Intent(requireContext(), Registracija::class.java)
        startActivity(intent)
    }


}