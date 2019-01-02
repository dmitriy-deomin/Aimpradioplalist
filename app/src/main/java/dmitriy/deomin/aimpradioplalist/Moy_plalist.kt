package dmitriy.deomin.aimpradioplalist

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import java.util.ArrayList
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onLongClick
import org.jetbrains.anko.support.v4.email
import org.jetbrains.anko.support.v4.share
import org.jetbrains.anko.textColor
import org.jetbrains.anko.toast
import java.io.File


class Moy_plalist : Fragment() {


    lateinit var file_function: File_function

    @SuppressLint("WrongConstant")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.my_plalist, null)
        val context: Context = container!!.context

        val recikl_list = v.findViewById<RecyclerView>(R.id.recicl_my_list)
        recikl_list.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)

        file_function = File_function()

        //прочитали файл Main.MY_PLALIST и получили список строк , каждая строка содержит имя и адрес станции
        //или получили Main.PUSTO если ам нет нечего
        val mas_radio = file_function.My_plalist(Main.MY_PLALIST)

        //адаптеру будем слать список классов Radio
        val data = ArrayList<Radio>()

        for (i in mas_radio.indices) {
            val m = mas_radio[i].split("\n")
            data.add(Radio(m[0], m[1]))
        }

        val adapter_vse_radio = Adapter_my_list(data)
        recikl_list.adapter = adapter_vse_radio


        //Слушаем кнопки
        (v.findViewById<View>(R.id.button_delete) as Button).setOnClickListener { v ->
            v.startAnimation(AnimationUtils.loadAnimation(context, R.anim.myalpha))
            if (file_function.My_plalist(Main.MY_PLALIST)[0] != Main.PUSTO) {

                val builder = AlertDialog.Builder(ContextThemeWrapper(context, android.R.style.Theme_Holo))
                val content = LayoutInflater.from(context).inflate(R.layout.custon_dialog_delete_plalist, null)
                builder.setView(content)

                val alertDialog = builder.create()
                alertDialog.show()

                val b_d_D = content.findViewById<View>(R.id.button_dialog_delete) as Button
                b_d_D.setTextColor(Main.COLOR_TEXT)
                b_d_D.typeface = Main.face
                b_d_D.onClick {
                    b_d_D.startAnimation(AnimationUtils.loadAnimation(context, R.anim.myalpha))
                    alertDialog.dismiss()
                    Main.number_page = 2
                    file_function.Delet_my_plalist()
                    Main.myadapter.notifyDataSetChanged()
                    Main.viewPager.adapter = Main.myadapter
                    Main.viewPager.currentItem = Main.number_page
                }
                val b_d_N = content.findViewById<View>(R.id.button_dialog_no) as Button
                b_d_N.setTextColor(Main.COLOR_TEXT)
                b_d_N.typeface = Main.face
                b_d_N.onClick {
                    b_d_N.startAnimation(AnimationUtils.loadAnimation(context, R.anim.myalpha))
                    alertDialog.dismiss()
                }
            } else {
                context.toast("Плейлист пуст")
            }
        }

        (v.findViewById<View>(R.id.button_add_url) as Button).setOnClickListener { v ->
            v.startAnimation(AnimationUtils.loadAnimation(context, R.anim.myalpha))

            val builder = AlertDialog.Builder(ContextThemeWrapper(context, android.R.style.Theme_Holo))
            val content = LayoutInflater.from(context).inflate(R.layout.add_url_user, null)
            builder.setView(content)
            val alertDialog = builder.create()
            alertDialog.show()

            val logo = content.findViewById<View>(R.id.textView_logo_add) as TextView
            logo.typeface = Main.face
            logo.textColor = Main.COLOR_TEXT

            val edit = content.findViewById<View>(R.id.editText_add_url) as EditText
            edit.typeface = Main.face
            edit.textColor = Main.COLOR_TEXT

            val edit_name = content.findViewById<View>(R.id.editText_add_url_name) as EditText
            edit_name.typeface = Main.face
            edit_name.textColor = Main.COLOR_TEXT

            val paste = content.findViewById<View>(R.id.button_paste_url_add) as Button
            paste.typeface = Main.face
            paste.textColor = Main.COLOR_TEXT
            paste.onClick {
                paste.startAnimation(AnimationUtils.loadAnimation(context, R.anim.myalpha))
                edit.setText(getText(context))
            }

            val add = content.findViewById<View>(R.id.button_add_url) as Button
            add.typeface = Main.face
            add.textColor = Main.COLOR_TEXT
            add.onClick {
                add.startAnimation(AnimationUtils.loadAnimation(context, R.anim.myalpha))

                //проверим на пустоту
                if (edit.text.toString().length > 7) {

                    //проверим есть ли в начале ссылки http:// или "https://" - ато от неё много чего зависит
                    if (edit.text.toString().substring(0, 7) == "http://" || edit.text.toString().substring(0, 8) == "https://") {
                        Main.number_page = 2

                        //фильтр для нашего сигнала
                        val intentFilter = IntentFilter()
                        intentFilter.addAction("File_created")

                        //приёмник  сигналов
                        val broadcastReceiver = object : BroadcastReceiver() {
                            override fun onReceive(c: Context, intent: Intent) {
                                if (intent.action == "File_created") {
                                    //получим данные
                                    val s = intent.getStringExtra("update")
                                    if (s == "zaebis") {
                                        alertDialog.cancel()
                                        //обновим старницу
                                        Main.myadapter.notifyDataSetChanged()
                                        Main.viewPager.adapter = Main.myadapter
                                        Main.viewPager.currentItem = Main.number_page
                                    } else {
                                        context.toast(context.getString(R.string.error))
                                    }
                                    //попробуем уничтожить слушителя
                                    context.unregisterReceiver(this)
                                }
                            }
                        }

                        //регистрируем приёмник
                        context.registerReceiver(broadcastReceiver, intentFilter)


                        //делаем
                        val file_function = File_function()
                        file_function.Add_may_plalist_stansiy(edit.text.toString(), edit_name.text.toString())

                        alertDialog.cancel()
                    } else {
                        edit.setText("http://" + edit.text.toString())
                        context.toast("В начале ссылки потока должна быть http://, добавил , повторите :)")
                    }

                } else {
                    context.toast("Нечего добавлять")
                }
            }
        }

        //будем предлогать сохранить этот плейлист в отдельный файл
        val svf = v.findViewById<Button>(R.id.save_v_file)
        svf.onClick {
            svf.startAnimation(AnimationUtils.loadAnimation(context, R.anim.myalpha))
            //прочитаем плейлист весь с закорючками
            val data: String = file_function.read(Main.MY_PLALIST)

            if (data.length < 7) {
                context.toast("Нечего сохранять добавьте хотябы одну станцию")
            } else {

                //покажем оконо в котором нужно будет ввести имя
                val builder = AlertDialog.Builder(ContextThemeWrapper(context, android.R.style.Theme_Holo))
                val content = LayoutInflater.from(context).inflate(R.layout.name_save_file, null)
                builder.setView(content)
                val alertDialog = builder.create()
                alertDialog.show()

                val vvedite_name = content.findViewById<View>(R.id.textView_vvedite_name) as TextView
                vvedite_name.typeface = Main.face
                vvedite_name.textColor = Main.COLOR_TEXT

                val name = content.findViewById<View>(R.id.edit_new_name) as EditText
                name.typeface = Main.face
                name.textColor = Main.COLOR_TEXT

                val save_buttten = content.findViewById<View>(R.id.button_save) as Button
                save_buttten.typeface = Main.face
                save_buttten.textColor = Main.COLOR_TEXT
                save_buttten.onClick {
                    save_buttten.startAnimation(AnimationUtils.loadAnimation(context, R.anim.myalpha))

                    //----
                    if (name.text.toString().length < 1) {
                        //пока покажем это потом будум генерерить свои если не захотят вводить
                        context.toast("Введите имя")
                    } else {
                        //нужно чтоб после обновления открылась таже вкладка
                        Main.number_page = 2

                        //когда прийдёт сигнал что сохранилось все хорошо обновим плейлист

                        //приёмник  сигналов
                        // фильтр для приёмника
                        val intentFilter = IntentFilter()
                        intentFilter.addAction("File_created")

                        //
                        val broadcastReceiver = object : BroadcastReceiver() {
                            override fun onReceive(c: Context, intent: Intent) {
                                if (intent.action == "File_created") {
                                    //получим данные
                                    val s = intent.getStringExtra("update")
                                    if (s == "zaebis") {

                                        //и обновим старницу
                                        Main.myadapter.notifyDataSetChanged()
                                        Main.viewPager.adapter = Main.myadapter
                                        Main.viewPager.currentItem = Main.number_page

                                        //тут злоябучий выскакивает глюк
                                        context.toast(Main.rnd_ok())
                                    } else {
                                        context.toast(context.getString(R.string.error))
                                    }
                                    //попробуем уничтожить слушителя
                                    context.unregisterReceiver(this)
                                }
                            }
                        }
                        //регистрируем приёмник
                        context.registerReceiver(broadcastReceiver, intentFilter)

                        //сохраним  временый файл ссылку и ждём сигналы
                        val file_function = File_function()
                        file_function.Save_temp_file(name.text.toString() + ".m3u", data)

                        //закроем окошко
                        alertDialog.cancel()
                    }
                }

                //----
            }
        }

        val lf = v.findViewById<Button>(R.id.load_file)
        lf.onClick {
            lf.startAnimation(AnimationUtils.loadAnimation(context, R.anim.myalpha))

            //если в плейлисте есть чето предложим чегонибуть
            if (file_function.My_plalist(Main.MY_PLALIST)[0] != Main.PUSTO) {

                val builder = AlertDialog.Builder(ContextThemeWrapper(context, android.R.style.Theme_Holo))
                val content = LayoutInflater.from(context).inflate(R.layout.vopros_pri_otkritii_new_file, null)
                builder.setView(content)
                val alertDialog = builder.create()
                alertDialog.show()

                (content.findViewById<View>(R.id.textView_vopros_pro_old_file) as TextView).typeface = Main.face
                (content.findViewById<View>(R.id.textView_vopros_pro_old_file) as TextView).setTextColor(Main.COLOR_TEXT)


                //затираем старое
                val del = content.findViewById<View>(R.id.button_dell_old_plalist) as Button
                del.typeface = Main.face
                del.setTextColor(Main.COLOR_TEXT)
                del.onClick {
                    del.startAnimation(AnimationUtils.loadAnimation(context, R.anim.myalpha))

                    //отправим с пустым старым текстом , старое затрётся
                    open_load_file(context, "")
                    //закрываем окошко
                    alertDialog.cancel()
                }


                //добавляем к старому если есть дубликаты пропустим их
                val add = content.findViewById<View>(R.id.button_add_old_plalist) as Button
                add.typeface = Main.face
                add.setTextColor(Main.COLOR_TEXT)
                add.onClick {
                    add.startAnimation(AnimationUtils.loadAnimation(context, R.anim.myalpha))

                    //прочтём текущий со всеми закорючками и отправим для добавления
                    val old_text: String = file_function.read(Main.MY_PLALIST)

                    //после выбора файла он прочётся и добавится к старым данным
                    open_load_file(context, old_text)
                    //закрываем окошко
                    alertDialog.cancel()

                }


            } else {
                open_load_file(context, "")
            }
        }
        val op = v.findViewById<View>(R.id.open_aimp)
        op.setOnClickListener {
            op.startAnimation(AnimationUtils.loadAnimation(context, R.anim.myalpha))
            if (file_function.My_plalist(Main.MY_PLALIST)[0] != Main.PUSTO) {


                //проверим есть ли аимп
                if (Main.install_app("com.aimp.player")) {
                    //откроем файл с сылкой в плеере
                    val cm = ComponentName(
                            "com.aimp.player",
                            "com.aimp.player.views.MainActivity.MainActivity")

                    val intent = Intent()
                    intent.component = cm

                    intent.action = Intent.ACTION_VIEW
                    intent.setDataAndType(Uri.parse("file://" + Environment.getExternalStorageDirectory().toString() + "/aimp_radio/my_plalist.m3u"), "audio/mpegurl")
                    intent.flags = 0x3000000

                    startActivity(intent)

                } else {
                    Main.setup_aimp("",
                            "file://" + Environment.getExternalStorageDirectory().toString() + "/aimp_radio/my_plalist.m3u")
                }

            } else {
                context.toast("Плэйлист пуст, добавьте хотябы одну станцию")
            }
        }

        val bt_send = v.findViewById<Button>(R.id.button_otpravit)
        bt_send.onClick {
            bt_send.startAnimation(AnimationUtils.loadAnimation(context, R.anim.myalpha))

            if (file_function.My_plalist(Main.MY_PLALIST)[0] != Main.PUSTO) {
                var send = ""

                for (s in file_function.My_plalist(Main.MY_PLALIST)) {
                    send += s + "\n"
                }

                share(send)
            } else {
                context.toast("Нечего отпралять, плейлист пуст")
            }
        }
        //при долгом нажатиии будем предлогать отправить мне письмом этот плейлист
        bt_send.onLongClick {
            bt_send.startAnimation(AnimationUtils.loadAnimation(context, R.anim.myalpha))

            if (file_function.My_plalist(Main.MY_PLALIST)[0] != Main.PUSTO) {
                var send = ""

                for (s in file_function.My_plalist(Main.MY_PLALIST)) {
                    send += s + "\n"
                }
                email("deomindmitriy@gmail.com", "aimp_radio_plalist", send)
            } else {
                context.toast("Нечего отпралять, плейлист пуст")
            }
        }

        return v
    }

    private fun open_load_file(context: Context, str_old: String) {
        //если плейлист пуст откроем окно выбора загрузки файла(память или ссылка)
        val builder = AlertDialog.Builder(ContextThemeWrapper(context, android.R.style.Theme_Holo))
        val content = LayoutInflater.from(context).inflate(R.layout.load_file, null)
        builder.setView(content)
        val alertDialog = builder.create()
        alertDialog.show()


        var file_m3u_custom: String

        //при выборе из памяти устройства
        val add_fs = content.findViewById<View>(R.id.load_fs) as Button
        add_fs.typeface = Main.face
        add_fs.textColor = Main.COLOR_TEXT
        add_fs.setOnClickListener { vie ->
            vie.startAnimation(AnimationUtils.loadAnimation(context, R.anim.myalpha))

            //посмотрим есть старый пусть
            val old_dir = Main.save_read("startdir")
            val startdir: String
            startdir = if (old_dir.length > 2) {
                old_dir
            } else {
                Environment.getExternalStorageDirectory().path
            }

            //----
            val fileDialog = OpenFileDialog(context)
                    .setFilter(".*\\.m3u")
                    .setStartDirectory(startdir)
                    .setOpenDialogListener {
                        if (it != null) {
                            file_m3u_custom = it
                            alertDialog.cancel()

                            //сохраним путь ,потом тамж и откроем
                            Main.save_value("startdir", File(file_m3u_custom).parent)

                            //проверим на наличие файла и будем действовать дальше
                            var str = file_function.read(file_m3u_custom)
                            //если файл есть и он не пустой зальём его в список по умолчанию
                            if (str.length > 1) {
                                //нужно чтоб после обновления открылась таже вкладка
                                Main.number_page = 2

                                //когда прийдёт сигнал что все хорошо обновим плейлист

                                //приёмник  сигналов
                                // фильтр для приёмника
                                val intentFilter = IntentFilter()
                                intentFilter.addAction("File_created")

                                //
                                val broadcastReceiver = object : BroadcastReceiver() {
                                    override fun onReceive(c: Context, intent: Intent) {
                                        if (intent.action == "File_created") {
                                            //получим данные
                                            val s = intent.getStringExtra("update")
                                            if (s == "zaebis") {
                                                //обновим старницу
                                                Main.myadapter.notifyDataSetChanged()
                                                Main.viewPager.adapter = Main.myadapter
                                                Main.viewPager.currentItem = Main.number_page
                                            } else {
                                                context.toast(context.getString(R.string.error))
                                                //Изменим текущию вкладку при обновлении что тутж остаться
                                                Main.number_page = 2
                                                //запросим разрешения
                                                Main.EbuchieRazreshenia()
                                            }
                                            //попробуем уничтожить слушителя
                                            context.unregisterReceiver(this)
                                        }
                                    }
                                }

                                //регистрируем приёмник
                                context.registerReceiver(broadcastReceiver, intentFilter)

                                //если в параметрах данные были поставим их вначале
                                if (str_old.length > 7) {
                                    //заменим тег #EXTM3U если есть в начале файла на перенос строки
                                    str = str.replace("#EXTM3U", "\n")

                                    //если в новых данных есть старые удалим их
                                    val cikl_data = str_old.replace("#EXTM3U", "").split("#EXTINF:-1,")
                                    for (i in cikl_data.iterator()) {
                                        //читаем старые данные и если они есть в новых удаляем
                                        str = str.replace("#EXTINF:-1,$i", "")
                                    }

                                    //поехали , сохраняем  и ждём сигналы
                                    file_function.SaveFile(Main.MY_PLALIST, str_old + str)
                                } else {
                                    //поехали , сохраняем  и ждём сигналы
                                    file_function.SaveFile(Main.MY_PLALIST, str)
                                }

                            } else {
                                context.toast("Файл: $file_m3u_custom пуст")
                            }

                        }
                    }

            fileDialog.show()
            //----
        }
    }

    //чтение из буфера
    private fun getText(c: Context): String {
        val clipboardManager: ClipboardManager = c.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val data = clipboardManager.primaryClip
        val item = data.getItemAt(0)
        return item.text.toString()
    }
}
