package com.gospodindjole.maturski

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable

data class Poruka(
    @SerialName("id_poruke")
    val id_poruke:String? = null,

    @SerialName("id_razgovora")
    val id_razgovora:String =" ",

    @SerialName("id_posiljaoca")
    val id_posiljaoca:String =" ",

    @SerialName("id_primaoca")
    val id_primaoca:String =" ",

    @SerialName("tekst_poruke")
    val tekst_poruke:String =" ",

    @SerialName("vreme_slanja")
    val vreme_slanja: String? = null
)