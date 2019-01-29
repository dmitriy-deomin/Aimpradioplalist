package dmitriy.deomin.aimpradioplalist

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import dmitriy.deomin.aimpradioplalist.custom.*
import kotlinx.android.synthetic.main.vse_radio.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.hintTextColor
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onLongClick
import org.jetbrains.anko.support.v4.toast
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller

import java.util.ArrayList


class Vse_radio : Fragment() {

    internal lateinit var context: Context
    lateinit var find: EditText

    companion object {
        var Numeracia: Int = 1
        var Poisk_ima_url: Int = 1
    }

    @SuppressLint("WrongConstant")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.vse_radio, null)


        context = Main.context

        find = v.findViewById(R.id.editText_find)
        find.typeface = Main.face
        find.hintTextColor = Main.COLOR_TEXTcontext


        Numeracia = if (Main.save_read_int("setting_numer") == 1) {
            1
        } else {
            0
        }
        Poisk_ima_url = if (Main.save_read_int("setting_poisk") == 1) {
            1
        } else {
            0
        }


        val ganrlist = listOf("-Музыка-", "-Юмор-", "-Разговорное-", "-Детское-", " -Аудиокниги-", "-Саундтреки-", "-Дискография-")

        val recikl_vse_list = v.findViewById<RecyclerView>(R.id.recicl_vse_radio)
        recikl_vse_list.layoutManager = LinearLayoutManager(context)

        //полоса быстрой прокрутки
        val fastScroller: VerticalRecyclerViewFastScroller = v.findViewById(R.id.fast_scroller)
        fastScroller.setRecyclerView(recikl_vse_list)
        recikl_vse_list.setOnScrollListener(fastScroller.onScrollListener)

        //адаптеру будем слать список классов Radio
        val data = ArrayList<Radio>()

        GlobalScope.launch {

            //получаем список радио >1000 штук
            val mas_radio = resources.getStringArray(R.array.vse_radio)

            for (i in mas_radio.indices) {
                val m = mas_radio[i].split("\n")
                var name = m[0]

                //---kbps----------------------------------
                var kbps = ""
                if (name.contains("kbps")) {
                    kbps = name.substring((name.length - 7), name.length)
                    name = name.substring(0, (name.length - 7))
                }
                //-------------------------------------------------------

                //---mono----------------------------------------
                if (name.contains("mono")) {
                    kbps = "mono " + kbps
                    name = name.replace("mono", "")
                }
                //------------------------------------------

                //--ganr--------------------------------------------------
                var ganr = ""
                for (g in ganrlist) {
                    if (name.contains(g)) {
                        name = name.replace(g, "")
                        ganr = g.replace("-", "")
                    }
                }
                //-----------------------------------------------------


                data.add(Radio(name, ganr, kbps, m[1]))
            }

            //пошлём сигнал в маин чтобы отключил показ прогресс бара
            //он нам пошлёт в обратку сигнал "update_vse_radio"
            signal("Main_update")
                    .putExtra("signal", "load_good_vse_radio")
                    .send(context)

        }


        //когда все распарсится и в маине отключится показ прогрессбара прилетит  сигнал
        // и запустит этот слот
        //------------------------------------------------------------------------------
        Slot(context, "update_vse_radio", false).onRun {

            val adapter_vse_list = Adapter_vse_list(data)
            recikl_vse_list.adapter = adapter_vse_list

            //пролистываем до нужного элемента
            if (Main.cho_nagimali_poslednee > 0 && data.size > Main.cho_nagimali_poslednee) {
                recikl_vse_list.scrollToPosition(Main.cho_nagimali_poslednee)
            }

            // текст только что изменили в строке поиска
            find.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                    adapter_vse_list.filter.filter(text)
                }
            })
        }
        //---------------------------------------------------------------


        //при первой загрузке будем ставить текст на кнопке , потом при смене будем менять тамже
        val t = if (Main.save_read("button_text_filter1").isNotEmpty()) {
            Main.save_read("button_text_filter1")
        } else {
            "Дискография"
        }
        v.kod_diskografii.text = t

        //при клике будем вставлять в строку поиска для отфильтровки
        v.kod_diskografii.onClick {

            if (find.text.toString() == v.kod_diskografii.text) {
                find.setText("")
            } else {
                //включаем поиск по всему
                Poisk_ima_url = 1
                Main.save_value_int("setting_poisk", 1)
                //ищем
                find.setText(v.kod_diskografii.text)
            }
        }
        //при долгом нажатиии будем предлогать изменить текст
        v.kod_diskografii.onLongClick {

            val vntvrf = DialogWindow(context, R.layout.vvod_new_text_vse_radio_filtr)

            val e_t = vntvrf.view().findViewById<EditText>(R.id.new_text_filter_editText)
            e_t.typeface = Main.face
            e_t.setTextColor(Main.COLOR_TEXT)
            e_t.setText(if (Main.save_read("button_text_filter1").isNotEmpty()) {
                Main.save_read("button_text_filter1")
            } else {
                "Дискография"
            })

            (vntvrf.view().findViewById<Button>(R.id.new_text_filter_button)).onClick {

                if (e_t.text.toString().isNotEmpty()) {
                    Main.save_value("button_text_filter1", e_t.text.toString())
                    v.kod_diskografii.text = Main.save_read("button_text_filter1")
                } else {
                    toast("Значения нет, восстановим по умолчанию")
                    Main.save_value("button_text_filter1", "Дискография")
                    v.kod_diskografii.text = Main.save_read("button_text_filter1")
                }
                vntvrf.close()
            }
        }

        v.kod_32bit.onClick {

            if (find.text.toString() == (v.kod_32bit.text)) {
                find.setText("")
            } else {
                //включаем поиск по всему
                Poisk_ima_url = 1
                Main.save_value_int("setting_poisk", 1)
                //ищем
                find.setText(v.kod_32bit.text)
            }
        }
        v.kod_64bit.onClick {

            if (find.text.toString() == (v.kod_64bit.text)) {
                find.setText("")
            } else {
                //включаем поиск по всему
                Poisk_ima_url = 1
                Main.save_value_int("setting_poisk", 1)
                //ищем
                find.setText(v.kod_64bit.text)
            }
        }
        v.kod_96bit.onClick {

            if (find.text.toString() == (v.kod_96bit.text)) {
                find.setText("")
            } else {
                //включаем поиск по всему
                Poisk_ima_url = 1
                Main.save_value_int("setting_poisk", 1)
                //ищем
                find.setText(v.kod_96bit.text)
            }
        }
        v.kod_128bit.onClick {

            if (find.text.toString() == (v.kod_128bit.text)) {
                find.setText("")
            } else {
                //включаем поиск по всему
                Poisk_ima_url = 1
                Main.save_value_int("setting_poisk", 1)
                //ищем
                find.setText(v.kod_128bit.text)
            }
        }
        v.kod_256bit.onClick {

            if (find.text.toString() == (v.kod_256bit.text)) {
                find.setText("")
            } else {
                //включаем поиск по всему
                Poisk_ima_url = 1
                Main.save_value_int("setting_poisk", 1)
                //ищем
                find.setText(v.kod_256bit.text)
            }
        }

        val setting = v.findViewById<Button>(R.id.button_settig_vse_radio)
        setting.onClick {

            val svr = DialogWindow(context, R.layout.setting_vse_radio)

            val num = svr.view().findViewById<Button>(R.id.button_seting_number)
            val pouisk = svr.view().findViewById<Button>(R.id.button_poisk)

            if (Numeracia == 1) {
                num.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                num.setTypeface(Main.face, Typeface.BOLD)
            } else {
                num.paintFlags = 0
                num.typeface = Main.face
            }

            if (Poisk_ima_url == 1) {
                pouisk.text = "Поиск по всему"
            } else {
                pouisk.text = "Поиск по имени"
            }

            num.onClick {

                if (Main.save_read_int("setting_numer") == 0) {
                    Main.save_value_int("setting_numer", 1)
                    Numeracia = 1
                    num.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                    num.setTypeface(Main.face, Typeface.BOLD)
                    recikl_vse_list.adapter!!.notifyDataSetChanged()
                } else {
                    Main.save_value_int("setting_numer", 0)
                    Numeracia = 0
                    num.paintFlags = 0
                    num.typeface = Main.face
                    recikl_vse_list.adapter!!.notifyDataSetChanged()
                }
            }

            pouisk.onClick {

                if (Main.save_read_int("setting_poisk") == 1) {
                    Poisk_ima_url = 0
                    Main.save_value_int("setting_poisk", 0)
                    pouisk.text = "Поиск по имени"
                    recikl_vse_list.adapter!!.notifyDataSetChanged()
                } else {
                    Poisk_ima_url = 1
                    Main.save_value_int("setting_poisk", 1)
                    pouisk.text = "Поиск по всему"
                    recikl_vse_list.adapter!!.notifyDataSetChanged()
                }
            }
        }
        return v
    }

}