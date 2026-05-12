package com.gospodindjole.maturski

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable

data class Ocena(
    @SerialName("id_primaoca")
    val id_primaoca:String =" ",

    @SerialName("id_posiljaoca")
    val id_posiljaoca:String =" ",

    @SerialName("opis")
    val opis:String =" ",

    @SerialName("pozitivna")
    val pozitivna: Boolean,

    @SerialName("naslov")
    val naslov:String =" ",

    @SerialName("created_at")
    val created_at:String =" "
)