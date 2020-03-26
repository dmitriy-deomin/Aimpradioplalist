package dmitriy.deomin.aimpradioplalist.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import dmitriy.deomin.aimpradioplalist.*
import dmitriy.deomin.aimpradioplalist.custom.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onLongClick
import dmitriy.deomin.aimpradioplalist.`fun`.*
import dmitriy.deomin.aimpradioplalist.`fun`.file.long_name_resize
import dmitriy.deomin.aimpradioplalist.`fun`.m3u.download_i_open_m3u_file
import dmitriy.deomin.aimpradioplalist.`fun`.play.play_aimp
import dmitriy.deomin.aimpradioplalist.`fun`.play.play_system


class Adapter_online_palist(val data: ArrayList<Radio>) : androidx.recyclerview.widget.RecyclerView.Adapter<Adapter_online_palist.ViewHolder>(), Filterable {

    private lateinit var context: Context
    var raduoSearchList: ArrayList<Radio> = data

    override fun getFilter(): Filter {


        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {

                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    this@Adapter_online_palist.raduoSearchList = data
                } else {
                    val filteredList = ArrayList<Radio>()
                    for (row in data) {
                        if (row.name.replace("<List>", "").toLowerCase().contains(charString.toLowerCase())
                                || row.url.toLowerCase().contains(charString.toLowerCase())
                                || row.kbps.toLowerCase().contains(charString.toLowerCase())
                                || row.kategory.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row)
                        }
                    }
                    this@Adapter_online_palist.raduoSearchList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = this@Adapter_online_palist.raduoSearchList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                if (filterResults.values != null) {
                    this@Adapter_online_palist.raduoSearchList = filterResults.values as ArrayList<Radio>
                    notifyDataSetChanged()
                }else{
                    notifyDataSetChanged()
                }
            }
        }
    }


    class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val name_radio = itemView.findViewById<TextView>(R.id.name_radio)
        val nomer_radio = itemView.findViewById<TextView>(R.id.nomer_radio)
        val url_radio = itemView.findViewById<TextView>(R.id.user_name_info)
        val fon = itemView.findViewById<androidx.cardview.widget.CardView>(R.id.fon_item_radio)
        val kbps = itemView.findViewById<TextView>(R.id.kbps_radio)
        val ganr = itemView.findViewById<TextView>(R.id.ganr_radio)
        val liner_kbps = itemView.findViewById<LinearLayout>(R.id.liner_kbps)
        val liner_ganr = itemView.findViewById<LinearLayout>(R.id.liner_ganr)
        val liner_url = itemView.findViewById<LinearLayout>(R.id.liner_url)
        val liner_fon = itemView.findViewById<LinearLayout>(R.id.liner_fon)


        //------------------------------------------------------------------------------------
        // коментарии ,лайки, инфо
        val liner_user = itemView.findViewById<LinearLayout>(R.id.liner_user_add_info)
        val user_name = itemView.findViewById<TextView>(R.id.user_name)
        //
        val liner_reiting = itemView.findViewById<LinearLayout>(R.id.liner_reiting)
        val btn_koment = itemView.findViewById<Button>(R.id.button_komenty)
        val btn_like = itemView.findViewById<Button>(R.id.button_like)
        //
        val liner_text_komentov = itemView.findViewById<LinearLayout>(R.id.liner_text_komentov)
        val btn_add_koment = itemView.findViewById<Button>(R.id.btn_add_new_koment)
        val btn_update_koment = itemView.findViewById<Button>(R.id.button_updete_obmenik)
        val text_komentov = itemView.findViewById<TextView>(R.id.text_komentov)
        //-----------------------------------------------------------------------------------
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v = LayoutInflater.from(p0.context).inflate(R.layout.item_list_online_plalist, p0, false)
        context = p0.context
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return this.raduoSearchList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {


        //заполним данными(тут в логах бывает падает - обращение к несуществующему элементу)
        //поэтому будем проверять чтобы общее количество было больше текушего номера
        val radio: Radio = if (this.raduoSearchList.size > p1) {
            this.raduoSearchList[p1]
        } else {
            //иначе вернём пустой элемент(дальше будут проверки и он не отобразится)
            Radio("", "", "", "")
        }


        var name_text = radio.name.replace("<List>", "")
        //добавим переносы для более читабельного вида где это нужно
        if (name_text.contains(".Автор:")) {
            name_text = name_text.replace(".Автор:", ".\nАвтор:")
        }
        if (name_text.contains(".Авторы:")) {
            name_text = name_text.replace(".Авторы:", ".\nАвторы:")
        }
        if (name_text.contains(".Читает:")) {
            name_text = name_text.replace(".Читает:", ".\nЧитает:")
        }
        if (name_text.contains(".Читают:")) {
            name_text = name_text.replace(".Читают:", ".\nЧитают:")
        }
        if (name_text.contains(".Длительность:")) {
            name_text = name_text.replace(".Длительность:", ".\nДлительность:")
        }
        //название будем делать жирным где эт надо
        if (name_text.contains("Автор:") || name_text.contains("Авторы:") || name_text.contains("Длительность:")) {
            p0.name_radio.text = Bold_text(name_text)
        } else {
            p0.name_radio.text = name_text
        }


        if (radio.url.isNotEmpty()) {
            p0.liner_url.visibility = View.VISIBLE
            p0.url_radio.text = radio.url
        } else {
            p0.liner_url.visibility = View.GONE
        }

        //нумерация списка
        if (Vse_radio.Numeracia == 1) {
            p0.nomer_radio.text = (p1 + 1).toString() + ". "
        } else {
            p0.nomer_radio.text = ""
        }
        //kbps
        if (radio.kbps.isNotEmpty()) {
            p0.liner_kbps.visibility = View.VISIBLE
            p0.kbps.text = radio.kbps
        } else {
            p0.liner_kbps.visibility = View.GONE
        }
        //ganr
        if (radio.kategory.isNotEmpty()) {
            p0.liner_ganr.visibility = View.VISIBLE
            p0.ganr.text = radio.kategory
        } else {
            p0.liner_ganr.visibility = View.GONE
        }

        //если есть сохранёная позиция поменяем у неё цвет
        if (Online_plalist.position_list_online_palist > 0 && Online_plalist.position_list_online_palist == p1) {
            p0.name_radio.textColor = Main.COLOR_SELEKT
            p0.url_radio.textColor = Main.COLOR_SELEKT
            p0.nomer_radio.textColor = Main.COLOR_SELEKT
            p0.kbps.textColor = Main.COLOR_SELEKT
            p0.ganr.textColor = Main.COLOR_SELEKT
            p0.text_komentov.textColor = Main.COLOR_SELEKT
        } else {
            p0.name_radio.textColor = Main.COLOR_TEXT
            p0.url_radio.textColor = Main.COLOR_TEXT
            p0.nomer_radio.textColor = Main.COLOR_TEXT
            p0.kbps.textColor = Main.COLOR_TEXT
            p0.ganr.textColor = Main.COLOR_TEXT
            p0.text_komentov.textColor = Main.COLOR_TEXT
        }



        p0.btn_like.visibility = View.GONE

        //покажем понель пока коментарии откроем
        p0.liner_reiting.visibility = View.VISIBLE

        //-----------коментарии и лайки-----------------------------------------
        //так как вся эта хуйня лежит не в базе у неё нет ид , будем брать урл
        var id = radio.url.replace("/", "")
        if (id.isEmpty()) {
            id = "pustoy_plalist"
        }


        GlobalScope.launch {
            Slot(context, "load_koment").onRun {
                if (it.getStringExtra("id") == id) {
                    val data = it.getParcelableArrayListExtra<Koment>("data")
                    p0.btn_koment.text = "Коментарии: " + (if (data.size > 0) {
                        data.size
                    } else {
                        0
                    })
                    //обнулим количество коментов и заново запишем
                    p0.text_komentov.text = ""
                    var t = ""
                    for (kom in data.iterator()) {
                        t = t + "\n" + (if (kom.user_name.isEmpty()) {
                            "no_name"
                        } else {
                            kom.user_name
                        }) + ": " + kom.text
                    }
                    p0.text_komentov.text = t
                }
            }
        }


        p0.btn_koment.onClick {
            if (p0.liner_text_komentov.visibility == View.GONE) {
                p0.liner_text_komentov.visibility = View.VISIBLE
            } else {
                p0.liner_text_komentov.visibility = View.GONE
            }
        }
        p0.btn_add_koment.onClick {
            //добавление коментариев
            //-------------------------------------------------------------------------------
            val add_kom = DialogWindow(context, R.layout.add_koment)
            val ed = add_kom.view().findViewById<EditText>(R.id.ed_add_kom)
            ed.typeface = Main.face
            ed.textColor = Main.COLOR_TEXT
            ed.hintTextColor = Main.COLOR_TEXTcontext
            add_kom.view().findViewById<Button>(R.id.btn_ad_kom).onClick {

                if (ed.text.toString().isEmpty()) {
                    context.toast("введите текст")
                } else {
                    Slot(context, "add_koment", false).onRun {
                        if (it.getStringExtra("update") == "zaebis") {
                            add_kom.close()
                            load_koment(id)
                        } else {
                            context.toast("ошибка")
                        }
                    }
                    add_koment(id, ed.text.toString())
                }
            }
            //-------------------------------------------------------------------------------------

        }
        p0.btn_update_koment.onClick {
            //обновить текуший список коментов
            load_koment(id)
        }

        GlobalScope.launch {
            //Загрузим в начале просто количество коментов
            load_koment(id)
        }

        //-----------------------------------------------------------------------


        if (Online_plalist.list_selekt.contains(p1)) {
            p0.fon.backgroundColor = Main.COLOR_SELEKT
        } else {
            p0.fon.backgroundColor = Main.COLOR_ITEM
        }

        p0.itemView.setOnLongClickListener {
            // TODO Auto-generated method stub
            p0.fon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.myscale))
            signal("Online_plalist_Adapter").putExtra("signal", "visible").send(context)
            true
        }

        //обработка нажатий
        p0.itemView.onClick {
            p0.fon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.myscale))

            p0.name_radio.textColor = Main.COLOR_SELEKT
            p0.url_radio.textColor = Main.COLOR_SELEKT
            p0.nomer_radio.textColor = Main.COLOR_SELEKT
            p0.kbps.textColor = Main.COLOR_SELEKT
            p0.ganr.textColor = Main.COLOR_SELEKT
            p0.text_komentov.textColor = Main.COLOR_SELEKT

            if (Online_plalist.visible_selekt) {

                if (Online_plalist.list_selekt.contains(p1)) {
                    Online_plalist.list_selekt.remove(p1)
                    p0.fon.backgroundColor = Main.COLOR_ITEM
                } else {
                    Online_plalist.list_selekt.add(p1)
                    p0.fon.backgroundColor = Main.COLOR_SELEKT
                }

            } else {
                //сохраняем позицию текушею списка
                signal("save_pozitions").putExtra("pos", p1.toString()).send(context)

                val mvr = DialogWindow(context, R.layout.menu_vse_radio)

                val add_pls = mvr.view().findViewById<Button>(R.id.button_add_plalist)
                val open_aimp = mvr.view().findViewById<Button>(R.id.button_open_aimp)
                val open_custom = mvr.view().findViewById<Button>(R.id.open_custom_plaer)
                val loadlist = mvr.view().findViewById<Button>(R.id.button_load_list)
                val share = mvr.view().findViewById<Button>(R.id.button_cshre)

                val name = radio.name.replace("<List>", "")
                //Имя и урл выбраной станции , при клике будем копировать урл в буфер
                val text_name_i_url = mvr.view().findViewById<TextView>(R.id.textView_vse_radio)
                text_name_i_url.text = name + "\n" + radio.url

                text_name_i_url.onClick{
                    text_name_i_url.startAnimation(AnimationUtils.loadAnimation(context, R.anim.myalpha))
                    putText_сlipboard(radio.url, context)
                    context.toast("url скопирован в буфер")
                }

                open_aimp.onLongClick {
                    play_system(long_name_resize(name), radio.url)
                }

                open_aimp.onClick {
                    if (save_read("categoria") == "3") {
                        play_system(long_name_resize(name), radio.url)
                    } else {
                        play_aimp(long_name_resize(name), radio.url)
                    }
                    mvr.close()
                }

                open_custom.onClick {
                    if(isValidURL(radio.url)){
                        Play_audio(radio.name, radio.url)
                    }else{
                        context.toast("Возможно ссылка битая, нельзя открыть")
                    }
                    mvr.close()
                }


                add_pls.onClick {

                    //если текуший элемент список ссылок
                    if (radio.name.contains("<List>")) {
                        mvr.close()
                        val dw = DialogWindow(context, R.layout.dialog_delete_stancii)
                        val dw_start = dw.view().findViewById<Button>(R.id.button_dialog_delete)
                        val dw_no = dw.view().findViewById<Button>(R.id.button_dialog_no)
                        val dw_logo = dw.view().findViewById<TextView>(R.id.text_voprosa_del_stncii)

                        dw_logo.text = "Текущая ссылка содержит список плейлистов(или еще ссылок).\n Все равно добавить ?"
                        dw_start.text = "Да"
                        dw_no.text = "Нет"

                        dw_start.onClick {
                            add_myplalist(radio.name, radio.url)
                            dw.close()
                        }
                        dw_no.onClick {
                            dw.close()
                        }

                    } else {
                        add_myplalist(radio.name, radio.url)
                        mvr.close()
                    }

                }

                share.onClick {
                    //сосавим строчку как в m3u вайле
                    context.share(radio.name + "\n" + radio.url)
                }
                share.onLongClick {
                    context.email("deomindmitriy@gmail.com", "aimp_radio_plalist", radio.name + "\n" + radio.url)
                }

                //если текуший элемент список ссылок
                if (radio.name.contains("<List>")) {
                    //скроем кнопки открытия в плеере
                    open_aimp.visibility = View.GONE
                    //покажем кнопку загрузки списка
                    loadlist.visibility = View.VISIBLE
                } else {
                    //иначе покажем
                    open_aimp.visibility = View.VISIBLE
                    //скроем кнопку загрузки списка
                    loadlist.visibility = View.GONE
                }

                //загрузить список
                loadlist.onClick {
                    //закрываем основное окошко
                    mvr.close()
                    download_i_open_m3u_file(radio.url, "anim_online_plalist")
                }
            }
        }

    }
}