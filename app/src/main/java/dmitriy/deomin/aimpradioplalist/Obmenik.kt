package dmitriy.deomin.aimpradioplalist

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.kotlinpermissions.ifNotNullOrElse
import dmitriy.deomin.aimpradioplalist.custom.*
import kotlinx.android.synthetic.main.obmenik.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onLongClick
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Obmenik : Activity() {

    lateinit var ao: Adapter_obmenik

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.obmenik)
        //во весь экран
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val context: Context = this

        fon_obmenik.backgroundColor = Main.COLOR_TEXTcontext

        val find = findViewById<EditText>(R.id.editText_find_obmennik)
        find.typeface = Main.face
        find.textColor = Main.COLOR_TEXT
        find.hintTextColor = Main.COLOR_TEXTcontext

        val recikl_list = list_obmenik
        recikl_list.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        //полоса быстрой прокрутки
        val fastScroller = fast_scroller_obmenik
        //получим текущие пораметры
        val paramL = fastScroller.layoutParams
        //меняем ширину
        paramL.width = Main.SIZE_WIDCH_SCROLL
        //устанавливаем
        fastScroller.layoutParams = paramL
        fastScroller.setRecyclerView(recikl_list)
        recikl_list.setOnScrollListener(fastScroller.onScrollListener)

        val d = ArrayList<Radio>()

        Slot(context, "Obmennik").onRun { it ->
            //получим данные
            when (it.getStringExtra("update")) {

                "zaebis" -> {

                    //напишем на кнопке что происходит
                    button_menu_obmenik.text = "Загрузка.."
                    //включим анимацию
                    load_anim(true)

                    //очистим список если был
                    d.clear()

                    //-----------------получаем список из базы------------------------------------
                    val db = FirebaseFirestore.getInstance()
                    db.collection("radio_obmenik")
                            .orderBy("date")
                            .get()
                            .addOnSuccessListener { result ->

                                for (document in result) {
                                    d.add(Radio(
                                            (if(document.data["name"]!=null){document.data["name"]}else{""}) as String,
                                            (if(document.data["kat"]!=null){document.data["kat"]}else{""}) as String,
                                            (if(document.data["kbps"]!=null){document.data["kbps"]}else{""}) as String,
                                            (if(document.data["url"]!=null){document.data["url"]}else{""}) as String,
                                            (if(document.data["user_name"]!=null){document.data["user_name"]}else{""}) as String,
                                            (if(document.data["user_id"]!=null){document.data["user_id"]}else{""}) as String,
                                            (document.id))
                                    )
                                }

                                //перевернём список
                                d.reverse()

                                ao = Adapter_obmenik(d)
                                recikl_list.adapter = ao

                                //скроем или покажем полосу прокрутки и поиск
                                if (d.size > Main.SIZE_LIST_LINE) {
                                    fastScroller.visibility = View.VISIBLE

                                    find.visibility = View.VISIBLE
                                    // текст только что изменили в строке поиска
                                    find.addTextChangedListener(object : TextWatcher {
                                        override fun afterTextChanged(s: Editable) {}
                                        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                                        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                                            ao.filter.filter(text)
                                        }
                                    })
                                } else {
                                    fastScroller.visibility = View.GONE

                                    find.setText("")
                                    find.visibility = View.GONE
                                }

                                //Напишем на кнопке меню текуию инфу по базе
                                button_menu_obmenik.text = "Всего в базе:" + d.size.toString() + " станций"
                                //выключаем анимацию загрузки
                                load_anim(false)

                            }
                            .addOnFailureListener { exception ->
                                //   Log.w(TAG, "Error getting documents.", exception)
                            }
                    //-----------------------------------------------------------------------------
                }

            }
        }

        button_menu_obmenik.onClick {
            val menu_ob = DialogWindow(context, R.layout.menu_obmenika)

            //имя и ид пользователя, нужны будут для удаления своих ссылок и оображении кто добавил
            val ed_id_user = menu_ob.view().findViewById<EditText>(R.id.editText_id)
            val ed_name_user = menu_ob.view().findViewById<EditText>(R.id.editText_name)

            ed_id_user.typeface = Main.face
            ed_id_user.textColor = Main.COLOR_TEXT
            ed_id_user.hintTextColor = Main.COLOR_TEXTcontext

            ed_name_user.typeface = Main.face
            ed_name_user.textColor = Main.COLOR_TEXT
            ed_name_user.hintTextColor = Main.COLOR_TEXTcontext

            ed_id_user.setText(Main.ID_USER)
            ed_name_user.setText(Main.NAME_USER)


            val user_dannie = (menu_ob.view().findViewById<LinearLayout>(R.id.liner_user_data))
            (menu_ob.view().findViewById<Button>(R.id.visible_user_data)).onClick {
                if (user_dannie.visibility == View.VISIBLE) {
                    user_dannie.visibility = View.GONE
                } else {
                    user_dannie.visibility = View.VISIBLE
                }
            }

            (menu_ob.view().findViewById<Button>(R.id.button_save_user_dannie)).onClick {

                //id важно будем спрашивать и проверяь
                if(ed_id_user.text.toString()!=Main.ID_USER){
                    if(ed_id_user.text.toString().length<4){
                        context.toast("Id должен быть не меньше 4-х символов")
                    }else{
                        alert( "При смене Id Вы потеряете ранее добавленнные ссылки","Внимание") {
                            yesButton {
                                Main.ID_USER=ed_id_user.text.toString()
                                Main.save_value("id_user",Main.ID_USER)
                                toast("Готово") }
                            noButton {}
                        }.show()

                    }
                }
                //на имя пофиг пусть ставят любое
                Main.NAME_USER = ed_name_user.text.toString()
                Main.save_value("name_user",Main.NAME_USER)
            }


        }

        button_add_new_obmenik.onClick {

            val menu_add_new = DialogWindow(context, R.layout.add_new_url_obmenik)

            val ed_name = menu_add_new.view().findViewById<EditText>(R.id.editText_name_new)
            val ed_url = menu_add_new.view().findViewById<EditText>(R.id.editText_url_new)
            val ed_kat = menu_add_new.view().findViewById<EditText>(R.id.editText_kategoria_new)
            val ed_kbps = menu_add_new.view().findViewById<EditText>(R.id.editText_kbps_new)

            ed_name.typeface = Main.face
            ed_name.textColor = Main.COLOR_TEXT
            ed_name.hintTextColor = Main.COLOR_TEXTcontext

            ed_url.typeface = Main.face
            ed_url.textColor = Main.COLOR_TEXT
            ed_url.hintTextColor = Main.COLOR_TEXTcontext

            ed_kat.typeface = Main.face
            ed_kat.textColor = Main.COLOR_TEXT
            ed_kat.hintTextColor = Main.COLOR_TEXTcontext

            ed_kbps.typeface = Main.face
            ed_kbps.textColor = Main.COLOR_TEXT
            ed_kbps.hintTextColor = Main.COLOR_TEXTcontext




            (menu_add_new.view().findViewById<Button>(R.id.button_paste_iz_bufera_obmenik)).onClick {
                ed_url.setText(Main.getText(context))
            }

            (menu_add_new.view().findViewById<Button>(R.id.button_add)).onClick {

                if (ed_name.text.toString().isEmpty() || ed_url.text.toString().isEmpty()) {
                    context.toast("Введите данные")
                } else {

                    var kategoria = ""
                    if(ed_kat.text.isNotEmpty()){
                        kategoria = ed_kat.text.toString()
                    }
                    var kbps = ""
                    if(ed_kbps.text.isNotEmpty()){
                        kbps = ed_kbps.text.toString()
                        if(!kbps.contains("kbps")){
                            kbps += "kbps"
                        }
                    }

                    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                    val currentDate = sdf.format(Date())


                    //добавление в базу
                    val db = FirebaseFirestore.getInstance()
                    val user = hashMapOf(
                            "date" to currentDate,
                            "user_name" to Main.NAME_USER,
                            "user_id" to Main.ID_USER,
                            "kat" to kategoria,
                            "kbps" to kbps,
                            "name" to ed_name.text.toString(),
                            "url" to ed_url.text.toString()
                    )

                    // Add a new document with a generated ID
                    db.collection("radio_obmenik")
                            .add(user)
                            .addOnSuccessListener { documentReference ->
                                //  Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                                menu_add_new.close()
                                //если все пучком пошлём сигнал для обновления(пока всего плейлиста)
                                signal("Obmennik").putExtra("update", "zaebis").send(context)
                            }
                            .addOnFailureListener { e ->
                                context.toast(e.toString())
                            }
                }
            }
        }

        //пошлём сигнал для загрузки дааных п спискок
        signal("Obmennik").putExtra("update", "zaebis").send(context)

    }

    fun load_anim(b: Boolean) {
        if (b) {
            progressBar_load_obmenik.visibility = View.VISIBLE
        } else {
            progressBar_load_obmenik.visibility = View.GONE
        }
    }
}
