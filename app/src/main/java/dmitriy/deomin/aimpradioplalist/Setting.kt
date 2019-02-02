package dmitriy.deomin.aimpradioplalist

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.*
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import com.github.danielnilsson9.colorpickerview.dialog.ColorPickerDialogFragment
import dmitriy.deomin.aimpradioplalist.Main.Companion.COLOR_FON
import dmitriy.deomin.aimpradioplalist.Main.Companion.COLOR_ITEM
import dmitriy.deomin.aimpradioplalist.Main.Companion.COLOR_TEXT
import dmitriy.deomin.aimpradioplalist.Main.Companion.COLOR_TEXTcontext
import dmitriy.deomin.aimpradioplalist.Main.Companion.face
import dmitriy.deomin.aimpradioplalist.Main.Companion.save_value_int
import dmitriy.deomin.aimpradioplalist.custom.send
import dmitriy.deomin.aimpradioplalist.custom.signal
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColor


class Setting : FragmentActivity(), ColorPickerDialogFragment.ColorPickerDialogListener {

    private var DIALOG_ID: Int = 0
    private lateinit var edit_fon: Button
    private lateinit var edit_pos1: Button
    private lateinit var edit_text_color: Button
    private lateinit var edit_textcontext_color: Button
    lateinit var context: Context

    private lateinit var linerfon: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting)
        //во весь экран
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        context = this

        edit_fon = findViewById(R.id.button_edit_fon_color)
        edit_pos1 = findViewById(R.id.button_edit_color_posty)
        edit_text_color = findViewById(R.id.button_edit_color_text)
        edit_textcontext_color = findViewById(R.id.button_edit_color_textcontext)
        linerfon = findViewById(R.id.fon_setting)

        //устанавливаем шрифт
        edit_fon.typeface = face
        edit_pos1.typeface = face
        edit_text_color.typeface = face


        edit_fon.onClick {
            DIALOG_ID = 0
            val f = ColorPickerDialogFragment
                    .newInstance(DIALOG_ID, null, null, resources.getColor(R.color.fon), true)

            f.setStyle(DialogFragment.STYLE_NORMAL, R.style.LightPickerDialogTheme)
            f.show(fragmentManager, "d")
        }

        edit_pos1.onClick {
            DIALOG_ID = 1
            val f = ColorPickerDialogFragment
                    .newInstance(DIALOG_ID, null, null, resources.getColor(R.color.green), true)

            f.setStyle(DialogFragment.STYLE_NORMAL, R.style.LightPickerDialogTheme)
            f.show(fragmentManager, "d")
        }

        edit_text_color.onClick {
            DIALOG_ID = 3
            val f = ColorPickerDialogFragment
                    .newInstance(DIALOG_ID, null, null, Color.BLACK, true)

            f.setStyle(DialogFragment.STYLE_NORMAL, R.style.LightPickerDialogTheme)
            f.show(fragmentManager, "d")
        }
        edit_textcontext_color.onClick {
            DIALOG_ID = 4
            val f = ColorPickerDialogFragment
                    .newInstance(DIALOG_ID, null, null, resources.getColor(R.color.textcontext), true)

            f.setStyle(DialogFragment.STYLE_NORMAL, R.style.LightPickerDialogTheme)
            f.show(fragmentManager, "d")
        }


        ///устанавливаем цвет и шрифт и сохраняется заодно
        pererisovka_color(false)

    }

    fun pererisovka_color(update_main:Boolean=true) {

        if(update_main){
            //пошлём сигнал пусть обновится все остальное
            signal("Main_update").putExtra("signal", "update_color").send(Main.context)
        }
        linerfon.backgroundColor = COLOR_FON
        edit_fon.textColor = COLOR_TEXT
        edit_fon.backgroundColor = COLOR_FON
        edit_pos1.textColor = COLOR_TEXT
        edit_pos1.backgroundColor = COLOR_ITEM
        edit_text_color.textColor = COLOR_TEXT
        edit_textcontext_color.textColor = COLOR_TEXTcontext
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        when (dialogId) {
            0 -> {
                save_value_int("color_fon", color)
                COLOR_FON = color
                pererisovka_color()
            }

            1 -> {
                save_value_int("color_post1", color)
                COLOR_ITEM = color
                pererisovka_color()
            }

            3 -> {
                save_value_int("color_text", color)
                COLOR_TEXT = color
                pererisovka_color()
            }
            4 -> {
                save_value_int("color_textcontext", color)
                COLOR_TEXTcontext = color
                pererisovka_color()
            }
        }
    }

    override fun onDialogDismissed(dialogId: Int) {}
}
