package dmitriy.deomin.aimpradioplalist.custom

import android.os.Parcel
import android.os.Parcelable


data class Radio(val name: String, val kategory: String = "", val kbps: String = "",
                 val url: String, val user_name: String = "", val id_user: String = "", val id: String = "") : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(kategory)
        parcel.writeString(kbps)
        parcel.writeString(url)
        parcel.writeString(user_name)
        parcel.writeString(id_user)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Koment> {
        override fun createFromParcel(parcel: Parcel): Koment {
            return Koment(parcel)
        }

        override fun newArray(size: Int): Array<Koment?> {
            return arrayOfNulls(size)
        }
    }
}


data class Link(val kbps: String, val url: String)

data class RadioPop(val name: String,
                    val ava_url: String,
                    val link1: Link,
                    val link2: Link,
                    val link3: Link,
                    val link4: Link,
                    val link5: Link)

data class Theme(val name: String, val fon: Int, val item: Int, val text: Int, val text_context: Int, val color_selekt: Int)

data class History(val name: String, val url: String, val data_time: String, val size: String = "")

data class HistoryNav(val url:String,val kat:String="del")

data class Koment(val user_name: String = "", val user_id: String = "", val text: String = "", val data: String = "", val kom_id: String = "") : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(user_name)
        parcel.writeString(user_id)
        parcel.writeString(text)
        parcel.writeString(data)
        parcel.writeString(kom_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Koment> {
        override fun createFromParcel(parcel: Parcel): Koment {
            return Koment(parcel)
        }

        override fun newArray(size: Int): Array<Koment?> {
            return arrayOfNulls(size)
        }
    }
}

