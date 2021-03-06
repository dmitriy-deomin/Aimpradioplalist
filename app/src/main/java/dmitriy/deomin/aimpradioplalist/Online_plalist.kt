package dmitriy.deomin.aimpradioplalist

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import dmitriy.deomin.aimpradioplalist.`fun`.*
import dmitriy.deomin.aimpradioplalist.`fun`.file.deleteAllFilesFolder
import dmitriy.deomin.aimpradioplalist.`fun`.file.getDirSize
import dmitriy.deomin.aimpradioplalist.`fun`.file.long_size_to_good_vid
import dmitriy.deomin.aimpradioplalist.`fun`.file.saveFile
import dmitriy.deomin.aimpradioplalist.`fun`.m3u.download_i_open_m3u_file
import dmitriy.deomin.aimpradioplalist.`fun`.play.play_aimp
import dmitriy.deomin.aimpradioplalist.`fun`.play.play_system
import dmitriy.deomin.aimpradioplalist.`fun`.windows.history_online_plalist
import dmitriy.deomin.aimpradioplalist.adapters.Adapter_history_online_plalist
import dmitriy.deomin.aimpradioplalist.adapters.Adapter_online_palist
import dmitriy.deomin.aimpradioplalist.custom.*
import kotlinx.android.synthetic.main.online_plalist.view.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onLongClick
import org.jetbrains.anko.support.v4.share
import org.jetbrains.anko.support.v4.startActivity
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class Online_plalist : Fragment() {

    lateinit var ad_online_palist: Adapter_online_palist
    var open_url_online_palist = ""
    private var list_history = ArrayList<HistoryNav>()


    companion object {
        var position_list_online_palist = 0
        var visible_selekt = false
        var list_selekt = ArrayList<Int>()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.online_plalist, null)
        val context: Context = Main.context

        //настройка вида----------------------------------------------------------------------------
        val find = v.findViewById<EditText>(R.id.editText_find_online_plalist)

        //Загрузим последний урл открытой страницы
        open_url_online_palist = save_read("history_last")


        //загрузим последнию позицию где тыкали
        position_list_online_palist = save_read_int(open_url_online_palist)


        val recikl_list_online = v.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recicl_online_plalist)
        recikl_list_online.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

        //полоса быстрой прокрутки
        val fastScroller = v.findViewById<VerticalRecyclerViewFastScroller>(R.id.fast_scroller_online_plalist)
        //получим текущие пораметры
        val paramL = fastScroller.layoutParams
        //меняем ширину
        paramL.width = Main.SIZE_WIDCH_SCROLL
        //устанавливаем
        fastScroller.layoutParams = paramL
        fastScroller.setRecyclerView(recikl_list_online)
        recikl_list_online.setOnScrollListener(fastScroller.onScrollListener)
        //-------------------------------------------------------------------------------------------

        //будем слушать эфир постоянно
        //----------------------------------------------------------------------------
        Slot(context, "Online_plalist").onRun { it ->
            //получим данные
            when (it.getStringExtra("update")) {
                "zaebis" -> {

                    //Посмотрим что за адресс передан
                    //-----------------------------------------------------------------------
                    if (it.getStringExtra("url") != null) {
                        open_url_online_palist = it.getStringExtra("url")!!

                        //Если список истории пуст добавим в него без проверок
                        if (list_history.isEmpty()) {
                            list_history.add(HistoryNav(open_url_online_palist, save_read("categoria")))
                        } else {
                            //если последний эелемент не совпадает с текущим переданым
                            if (list_history.last().url != open_url_online_palist) {
                                //добавим тоже в список
                                list_history.add(HistoryNav(open_url_online_palist, save_read("categoria")))
                            }
                        }
                    }
                    //-----------------------------------------------------------------------------


                    //загрузим сохнанёную позицию для этого файла(если есть)
                    position_list_online_palist = save_read_int(open_url_online_palist)

                    //получим переданные данные из сигнала если есть
                    //====================================================================================
                    val data = if (it.getParcelableArrayListExtra<Radio>("pars_data") != null) {
                        it.getParcelableArrayListExtra<Radio>("pars_data")
                    } else {
                        arrayListOf(Radio(name = Main.PUSTO, url = ""))
                    }


                    ad_online_palist = Adapter_online_palist(data!!)
                    recikl_list_online.adapter = ad_online_palist
                    //---------------------------------------------------------

                    //перемотаем
                    if (position_list_online_palist < data.size && position_list_online_palist >= 0) {
                        try {
                            recikl_list_online.scrollToPosition(position_list_online_palist)
                        } catch (e: Exception) {
                            Main.context.toast("Позиция не найдена")
                        }

                    }

                    //скроем или покажем полосу прокрутки и поиск
                    if (data.size > Main.SIZE_LIST_LINE) {
                        fastScroller.visibility = View.VISIBLE

                        find.visibility = View.VISIBLE
                        // текст только что изменили в строке поиска
                        find.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable) {}
                            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                                ad_online_palist.filter.filter(text)
                            }
                        })
                    } else {
                        fastScroller.visibility = View.GONE
                        find.setText("")
                        find.visibility = View.GONE
                    }
                    //остановим анимацию
                    signal("Main_update").putExtra("signal", "stop_anim_online_plalist").send(context)
                    //==================================================================
                }
            }
        }

        Slot(context, "save_pozitions").onRun {
            if (!it.getStringExtra("pos").isNullOrEmpty()) {
                position_list_online_palist = it.getStringExtra("pos")!!.toInt()
                save_value_int(open_url_online_palist, position_list_online_palist)
            }
        }

        //будем слушать сигналы из адаптера
        Slot(context, "Online_plalist_Adapter").onRun {

            when (it.getStringExtra("signal")) {
                "visible" -> {
                    if (v.liner_long_menu.visibility == View.VISIBLE) {
                        v.liner_long_menu.visibility = View.GONE
                        visible_selekt = false
                        list_selekt.clear()
                        ad_online_palist.notifyDataSetChanged()
                    } else {
                        v.liner_long_menu.visibility = View.VISIBLE
                        visible_selekt = true
                    }
                }
            }
        }
        //-------------------------------------------------------------------------------------

        //выделить всЁ
        v.button_selekt_all_op.onClick {
            val d = ad_online_palist.raduoSearchList
            if (d.size == list_selekt.size) {
                list_selekt.clear()
                ad_online_palist.notifyDataSetChanged()
                v.button_selekt_all_op.backgroundDrawable = resources.getDrawable(R.drawable.selektall)
            } else {
                list_selekt.clear()
                for (l in d.withIndex()) {
                    list_selekt.add(l.index)
                    ad_online_palist.notifyDataSetChanged()
                }
                v.button_selekt_all_op.backgroundDrawable = resources.getDrawable(R.drawable.un_selektall)
            }
        }

        v.button_open_aimp_op.onClick {

            if (list_selekt.size > 0) {

                val d = ad_online_palist.raduoSearchList
                val data = ArrayList<String>()
                //запишем в строчном формате
                data.add("#EXTM3U")
                for (s in d.withIndex()) {
                    if (d[s.index].url.isNotEmpty() && list_selekt.contains(s.index)) {
                        data.add("\n#EXTINF:-1," + d[s.index].name + " " + d[s.index].kbps + "\n" + d[s.index].url)
                    }
                }

                val name_file = "Плэйлист:" + d[0].name + " (" + list_selekt.size.toString() + " частей)"

                //когда прийдёт сигнал что сохранилось все хорошо обновим плейлист
                Slot(context, "File_created", false).onRun {
                    //получим данные
                    when (it.getStringExtra("update")) {
                        "zaebis" -> play_aimp(it.getStringExtra("name")!!, "")
                        "pizdec" -> Main.context.longToast(it.getStringExtra("erorr")!!)
                    }
                }
                saveFile(name_file, data.joinToString(separator = "\n"))
            } else {
                context.toast("Выберите что воспроизводить")
            }


        }

        v.button_open_aimp_op.onLongClick {
            if (list_selekt.size > 0) {

                val d = ad_online_palist.raduoSearchList
                val data = ArrayList<String>()
                //запишем в строчном формате
                data.add("#EXTM3U")
                for (s in d.withIndex()) {
                    if (d[s.index].url.isNotEmpty() && list_selekt.contains(s.index)) {
                        data.add("\n#EXTINF:-1," + d[s.index].name + " " + d[s.index].kbps + "\n" + d[s.index].url)
                    }
                }

                val name_file = "Плэйлист:" + d[0].name + " (" + list_selekt.size.toString() + " частей)"

                //когда прийдёт сигнал что сохранилось все хорошо обновим плейлист
                Slot(context, "File_created", false).onRun {
                    //получим данные
                    when (it.getStringExtra("update")) {
                        "zaebis" -> play_system(it.getStringExtra("name")!!, "")
                        "pizdec" -> Main.context.longToast(it.getStringExtra("erorr")!!)
                    }
                }
                saveFile(name_file, data.joinToString(separator = "\n"))
            } else {
                context.toast("Выберите что воспроизводить")
            }
        }

        v.button_cshre_op.onClick {
            if (list_selekt.size > 0) {
                val d = ad_online_palist.raduoSearchList
                val data = ArrayList<String>()
                //запишем в строчном формате
                data.add("#EXTM3U")
                for (s in d.withIndex()) {
                    if (d[s.index].url.isNotEmpty() && list_selekt.contains(s.index)) {
                        data.add("\n#EXTINF:-1," + d[s.index].name + " " + d[s.index].kbps + "\n" + d[s.index].url)
                    }
                }
                share_text(data.joinToString("\n"))
            } else {
                context.toast("Выберите чем поделится")
            }
        }

        v.button_add_plalist_op.onClick {

            if (list_selekt.size > 0) {
                val d = ad_online_palist.raduoSearchList
                for (s in d.withIndex()) {
                    if (d[s.index].url.isNotEmpty() && list_selekt.contains(s.index)) {
                        add_myplalist(d[s.index].name + " " + d[s.index].kbps, d[s.index].url)
                    }
                }
            } else {
                context.toast("Выберите что добавить")
            }
        }

        v.button_history_online_plalilst.setOnLongClickListener {
            v.button_history_online_plalilst.startAnimation(AnimationUtils.loadAnimation(context, R.anim.myscale))
            history_online_plalist(context, list_history, v)
            true
        }

        v.button_history_online_plalilst.onClick {

            //Если в истории что то есть
            if (list_history.size > 1) {
                //удаляем текущию открытую страницу
                list_history.removeAt(list_history.size - 1)
                //передаём предыдующию
                download_i_open_m3u_file(list_history.last().url, "anim_online_plalist")
                selekt_CATEGORIA_ONLINE_PLALIST(list_history.last().kat, v)
                save_value("categoria", list_history.last().kat)
            } else {
                //если там последняя страница списка
                if (list_history.size == 1) {
                    //передаём предыдующию
                    download_i_open_m3u_file(list_history.last().url, "anim_online_plalist")
                    selekt_CATEGORIA_ONLINE_PLALIST(list_history.last().kat, v)
                    save_value("categoria", list_history.last().kat)
                }
            }
        }

        //установим размер текста кнопкам
        v.button_open_online_plalist_radio.textSize = Main.SIZE_TEXT_ONLINE_BUTTON
        v.button_open_online_plalist_audio_book.textSize = Main.SIZE_TEXT_ONLINE_BUTTON
        v.button_open_online_plalist_tv.textSize = Main.SIZE_TEXT_ONLINE_BUTTON
        v.button_open_online_plalist_musik.textSize = Main.SIZE_TEXT_ONLINE_BUTTON
        v.button_open_online_plalist_obmennik.textSize = Main.SIZE_TEXT_ONLINE_BUTTON
        v.button_history_online_plalilst.textSize = Main.SIZE_TEXT_ONLINE_BUTTON

        //--------категории--------------------------------------------------------------
        v.button_open_online_plalist_radio.onClick {
            download_i_open_m3u_file("https://dl.dropbox.com/s/sl4x8z3yth5v1u0/Radio.m3u", "anim_online_plalist")
            save_value("categoria", "1")
            selekt_CATEGORIA_ONLINE_PLALIST("1", v)
        }
        v.button_open_online_plalist_audio_book.onClick {
            download_i_open_m3u_file("https://dl.dropbox.com/s/cd479dcdguk6cg6/Audio_book.m3u", "anim_online_plalist")
            save_value("categoria", "2")
            selekt_CATEGORIA_ONLINE_PLALIST("2", v)
        }
        v.button_open_online_plalist_tv.onClick {
            download_i_open_m3u_file("https://dl.dropbox.com/s/4m3nvh3hlx60cy7/plialist_tv.m3u", "anim_online_plalist")
            save_value("categoria", "3")
            selekt_CATEGORIA_ONLINE_PLALIST("3", v)
        }
        v.button_open_online_plalist_musik.onClick {
            download_i_open_m3u_file("https://dl.dropbox.com/s/oe9kdcksjru82by/Musik.m3u", "anim_online_plalist")
            save_value("categoria", "4")
            selekt_CATEGORIA_ONLINE_PLALIST("4", v)
        }
        v.button_open_online_plalist_obmennik.onClick {
            startActivity<Obmenik>()
        }
        //---------------------------------------------------------

        //загрузим последнию открытую страницу
        if (isValidURL(open_url_online_palist)) {
            download_i_open_m3u_file(open_url_online_palist, "anim_online_plalist")
            //Отметим категорию
            selekt_CATEGORIA_ONLINE_PLALIST(save_read("categoria"), v)
        } else {
            //иначе пустую страницу покажем
            signal("Online_plalist")
                    .putExtra("update", "zaebis")
                    .send(context)
        }
        return v
    }


    override fun onPause() {
        //сохраним последнию открытую страницу
        save_value("history_last", open_url_online_palist)
        super.onPause()
    }
}
