package com.gospodindjole.maturski

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable

data class Razgovori(
    @SerialName("id_razgovora")
    val id_razgovora:String =" ",

    @SerialName("id_prodavca")
    val id_prodavca:String =" ",

    @SerialName("id_kupca")
    val id_kupca:String =" ",

    @SerialName("id_oglasa")
    val id_oglasa:String =" ",

    @SerialName("naslov")
    val naslov:String =" "
)