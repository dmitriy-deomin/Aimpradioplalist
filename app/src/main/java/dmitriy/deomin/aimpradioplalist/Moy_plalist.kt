package dmitriy.deomin.aimpradioplalist

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import dmitriy.deomin.aimpradioplalist.`fun`.m3u.create_m3u_file
import dmitriy.deomin.aimpradioplalist.`fun`.play.play_aimp
import dmitriy.deomin.aimpradioplalist.`fun`.play.play_system
import dmitriy.deomin.aimpradioplalist.`fun`.read_and_pars_m3u_file
import dmitriy.deomin.aimpradioplalist.`fun`.save_read_int
import dmitriy.deomin.aimpradioplalist.`fun`.send_email
import dmitriy.deomin.aimpradioplalist.`fun`.share_text
import dmitriy.deomin.aimpradioplalist.`fun`.windows.*
import dmitriy.deomin.aimpradioplalist.adapters.Adapter_my_list
import dmitriy.deomin.aimpradioplalist.custom.Radio
import dmitriy.deomin.aimpradioplalist.custom.Slot
import dmitriy.deomin.aimpradioplalist.custom.send
import dmitriy.deomin.aimpradioplalist.custom.signal
import kotlinx.android.synthetic.main.my_plalist.view.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onLongClick
import org.jetbrains.anko.support.v4.email
import org.jetbrains.anko.support.v4.share
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller
import java.lang.Exception


class Moy_plalist : Fragment() {

    lateinit var ad: Adapter_my_list

    companion object {
        var position_list_my_plalist = 0
        //список файлов которые загружались онлайн
        var list_move_history: ArrayList<String> = ArrayList()
        //файл который загружен сейчас
        var open_file = ""
    }

    @SuppressLint("WrongConstant", "SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.my_plalist, null)
        val context: Context = Main.context

        val find = v.findViewById<EditText>(R.id.editText_find_my_list)

        v.button_close_list.textSize = Main.SIZE_TEXT_NAME

        position_list_my_plalist = save_read_int("position_list_my_plalist")

        val recikl_list = v.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recicl_my_list)
        recikl_list.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

        //полоса быстрой прокрутки
        val fastScroller = v.findViewById<VerticalRecyclerViewFastScroller>(R.id.fast_scroller)
        //получим текущие пораметры
        val paramL = fastScroller.layoutParams
        //меняем ширину
        paramL.width = Main.SIZE_WIDCH_SCROLL
        //устанавливаем
        fastScroller.layoutParams = paramL
        fastScroller.setRecyclerView(recikl_list)
        recikl_list.setOnScrollListener(fastScroller.onScrollListener)

        val update_list = v.findViewById<Button>(R.id.button_close_list)

        //будем слушать эфир постоянно если че обновим список
        //----------------------------------------------------------------------------
        Slot(context, "Data_add").onRun { it ->
            //получим данные
            when (it.getStringExtra("update")) {
                "zaebis" -> {
                    if (!it.getStringExtra("listfile").isNullOrEmpty()) {
                        if (it.getStringExtra("listfile") == "old") {
                            //оставим все как есть если не открыт мой список
                            if (open_file != Main.MY_PLALIST) {
                                context.toast("добавлено")
                            }
                        } else {
                            //иначе обновим мой список
                            update_list.visibility = View.VISIBLE
                            open_file = it.getStringExtra("listfile")!!
                            list_move_history.add(open_file)
                        }
                    } else {
                        update_list.visibility = View.GONE
                        open_file = Main.MY_PLALIST
                    }

                    //заново все сделаем
                    val d = read_and_pars_m3u_file(open_file)
                    ad = Adapter_my_list(d)
                    recikl_list.adapter = ad
                    //---------------------------------------------------------

                    //перемотаем
                    if (position_list_my_plalist < d.size && position_list_my_plalist >= 0) {
                        recikl_list.scrollToPosition(position_list_my_plalist)
                    }

                    //скроем или покажем полосу прокрутки и поиск
                    if (d.size > Main.SIZE_LIST_LINE) {
                        fastScroller.visibility = View.VISIBLE

                        find.visibility = View.VISIBLE
                        // текст только что изменили в строке поиска
                        find.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable) {}
                            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                                ad.filter.filter(text)
                            }
                        })
                    } else {
                        fastScroller.visibility = View.GONE

                        find.setText("")
                        find.visibility = View.GONE
                    }

                    //остановим анимацию
                    signal("Main_update").putExtra("signal", "stop_anim_my_list").send(context)
                    //==================================================================
                }

                "move_back" -> {
                    //если в истории чтото есть вообще
                    if (list_move_history.isNotEmpty()) {
                        //Если в истории что то есть
                        if (list_move_history.size > 1) {
                            Log.e("ttt", list_move_history.toString())
                            //удаляем текущию открытую страницу
                            list_move_history.removeAt(list_move_history.size - 1)
                            //передаём предыдующию
                            Log.e("ttt", list_move_history.toString())
                            open_file = list_move_history.last()
                        }else{
                            if (list_move_history.size == 1) {
                                open_file = Main.MY_PLALIST
                                update_list.visibility = View.GONE
                                list_move_history.clear()
                            }
                        }
                    } else {
                        update_list.visibility = View.GONE
                        list_move_history.clear()
                        open_file = ""
                        return@onRun
                    }

                    //заново все сделаем
                    val d = read_and_pars_m3u_file(open_file)
                    ad = Adapter_my_list(d)
                    recikl_list.adapter = ad
                    //---------------------------------------------------------

                    //перемотаем
                    if (position_list_my_plalist < d.size && position_list_my_plalist >= 0) {
                        recikl_list.scrollToPosition(position_list_my_plalist)
                    }

                    //скроем или покажем полосу прокрутки и поиск
                    if (d.size > Main.SIZE_LIST_LINE) {
                        fastScroller.visibility = View.VISIBLE

                        find.visibility = View.VISIBLE
                        // текст только что изменили в строке поиска
                        find.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable) {}
                            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                                ad.filter.filter(text)
                            }
                        })
                    } else {
                        fastScroller.visibility = View.GONE

                        find.setText("")
                        find.visibility = View.GONE
                    }
                    //остановим анимацию
                    signal("Main_update").putExtra("signal", "stop_anim_my_list").send(context)
                    //==================================================================
                }
            }
        }
        //-------------------------------------------------------------------------------------

        //Слушаем кнопки
        //===========================================================================================
        //------------удалить(очистить весь плейлист)---------------------------------------------
        (v.findViewById<Button>(R.id.button_delete)).onClick {

            //если список совсем не пуст (есть пояснялка но ссылок нет)
            if (ad.raduoSearchList.isNotEmpty()) {
                //если список не пуст
                if (ad.raduoSearchList[0].name != (Main.PUSTO.replace("\n", ""))) {
                    dialog_delete_plalist(context,ad)
                } else {
                    context.toast("Плейлист пуст")
                }

            } else {
                dialog_delete_plalist_pustoy(context)
            }

        }
        //-----------------------------------------------------------------------------------------

        //-------------добавить свой поток(имя и адрес)-----------------------------------------
        (v.findViewById<Button>(R.id.button_add_url)).onClick {
            add_url_user(context)
        }
        //-----------------------------------------------------------------------------------------

        //-------------сохранить этот плейлист в отдельный файл------------------------------------
        (v.findViewById<Button>(R.id.save_v_file)).onClick {
            //если список не пуст
            if (ad.raduoSearchList[0].name != (Main.PUSTO)) {
                name_save_file(context,ad)
            } else {
                context.toast("Нечего сохранять добавьте хотябы одну станцию")
            }
        }
        //--------------------------------------------------------------------------------------

        //-------------открыть из памяти устройства или по ссылке плейлист--------------------------------------
        (v.findViewById<Button>(R.id.load_file)).onClick {
            //если в плейлисте есть чето предложим чегонибуть
            if (read_and_pars_m3u_file(Main.MY_PLALIST)[0].name != Main.PUSTO) {
                vopros_pri_otkritii_new_file(context)
            } else {
                open_load_file(context, arrayListOf(Radio(name = Main.PUSTO, url = "")))
            }
            //---------------------------------------------------------------------------------
        }
        //------------------------------------------------------------------------------------

        //-------------при долгом нажатии будем открывать папку программы-------------------
        (v.findViewById<Button>(R.id.load_file)).onLongClick {

            val fileDialog = OpenFileDialog(context)
                    .setFilter(".*\\.m3u")
                    .setStartDirectory(Main.ROOT)
                    .setEnablButton(false)
            fileDialog.show()
        }
        //-----------------------------------------------------------------------------------

        //------------открыть в плеере------------------------------------------------------
        (v.findViewById<Button>(R.id.open_aimp)).onClick {
            //если список не пуст
            if (ad.raduoSearchList[0].name != (Main.PUSTO.replace("\n", ""))) {
                val name_file = "Плэйлист с " + ad.raduoSearchList.size.toString() + " станциями"
                //когда прийдёт сигнал что сохранилось все хорошо обновим плейлист
                Slot(context, "File_created", false).onRun {
                    //получим данные
                    when (it.getStringExtra("update")) {
                        "zaebis" -> play_aimp(it.getStringExtra("name")!!, "")
                        "pizdec" -> context.longToast(it.getStringExtra("erorr")!!)
                    }
                }
                create_m3u_file(name_file, ad.raduoSearchList)
            } else {
                context.toast("Плэйлист пуст, добавьте хотябы одну станцию")
            }
        }
        //--------------------------------------------------------------------------

        //---------------открыть в системе----------------------------------------
        (v.findViewById<Button>(R.id.open_aimp)).onLongClick {
            //если список не пуст
            if (ad.raduoSearchList[0].name != (Main.PUSTO.replace("\n", ""))) {
                val name_file = "Плэйлист с " + ad.raduoSearchList.size.toString() + " станциями"
                //когда прийдёт сигнал что сохранилось все хорошо обновим плейлист
                Slot(context, "File_created", false).onRun {
                    //получим данные
                    when (it.getStringExtra("update")) {
                        "zaebis" -> play_system(it.getStringExtra("name")!!, "")
                        "pizdec" -> context.longToast(it.getStringExtra("erorr")!!)
                    }
                }
                create_m3u_file(name_file, ad.raduoSearchList)
            } else {
                context.toast("Плэйлист пуст, добавьте хотябы одну станцию")
            }
        }
        //----------------------------------------------------------------------------------

        //------------поделится---------------------------------------------------------
        (v.findViewById<Button>(R.id.button_otpravit)).onClick {

            //если список не пуст
            if (ad.raduoSearchList[0].name != (Main.PUSTO.replace("\n", ""))) {
                //приведем к норм виду
                val d = ad.raduoSearchList
                val data = ArrayList<String>()
                //запишем в строчном формате
                data.add("#EXTM3U")
                for (s in d.iterator()) {
                    if (s.url.isNotEmpty()) {
                        data.add("\n#EXTINF:-1," + s.name + " " + s.kbps + "\n" + s.url)
                    }
                }
                share_text(data.joinToString("\n"))
            } else {
                context.toast("Плэйлист пуст, добавьте хотябы одну станцию")
            }
        }
        //----------------------------------------------------------------------------------

        //-----при долгом нажатиии будем предлогать отправить мне письмом этот плейлист----
        (v.findViewById<Button>(R.id.button_otpravit)).onLongClick {
            //если список не пуст
            if (ad.raduoSearchList[0].name != (Main.PUSTO.replace("\n", ""))) {
                //приведем к норм виду
                val data = ArrayList<String>()
                val d = ad.raduoSearchList
                //запишем в строчном формате
                data.add("#EXTM3U")
                for (s in d.iterator()) {
                    if (s.url.isNotEmpty()) {
                        data.add("\n#EXTINF:-1," + s.name + " " + s.kbps + "\n" + s.url)
                    }
                }
                send_email("deomindmitriy@gmail.com", data.joinToString("\n"))
            } else {
                context.toast("Плэйлист пуст, добавьте хотябы одну станцию")
            }
        }
        //--------------------------------------------------------------------------

        //Кнопка обновления(появляется когда открывается список из ссылки)
        update_list.onClick {
            signal("Data_add").putExtra("update", "move_back").send(context)
        }
        //======================================================================================

        //пошлём сигнал для загрузки дааных п спискок
        signal("Data_add").putExtra("update", "zaebis").send(context)

        return v
    }
}