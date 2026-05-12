package com.gospodindjole.maturski

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable

data class Oglas(
    @SerialName("id_oglasa")
    val id_oglasa:String =" ",

    @SerialName("naslov")
    val naslov:String =" ",

    @SerialName("opis")
    val opis:String =" ",

    @SerialName("kategorija")
    val kategorija:String =" ",

    @SerialName("cena")
    val cena:String =" ",

    @SerialName("id_kreatora")
    val id_kreatora:String =" ",

    @SerialName("one_time")
    val one_time: Boolean,

    @SerialName("slike")
    val slike:List<String>,
)