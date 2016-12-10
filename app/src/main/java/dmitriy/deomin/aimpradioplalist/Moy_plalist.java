package dmitriy.deomin.aimpradioplalist;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Moy_plalist extends Fragment implements AdapterView.OnItemLongClickListener {
    ListView listView;
    Context context;
    final String STANCIA="stancia";
    File_function file_function;



    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.my_plalist, null);
        context = container.getContext();

        listView =(ListView)v.findViewById(R.id.listvew_my_plalist);

        file_function = new File_function();

        String [] mas_radio=file_function.My_plalist();
        final ArrayList<Map<String,Object>> data = new ArrayList<Map<String,Object>>(mas_radio.length);

        Map<String,Object> m ;

        for(int i = 0;i<mas_radio.length;i++){
            m= new HashMap<String,Object>();
            m.put(STANCIA,(mas_radio[i]));
            data.add(m);
        }

        // массив имен атрибутов, из которых будут читаться данные
        String[] from = { STANCIA };
        // массив ID View-компонентов, в которые будут вставлять данные
        int[] to = { R.id.textView};

        final Adapter_vse_radio adapter_vse_radio = new Adapter_vse_radio(context,data,R.layout.delegat_vse_radio_list,from,to);
        listView.setAdapter(adapter_vse_radio);


        //Слушаем кнопки

        ((Button)v.findViewById(R.id.button_delete)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!file_function.My_plalist()[0].equals("Плейлист пуст")) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo));
                    final View content = LayoutInflater.from(context).inflate(R.layout.custon_dialog_delete_plalist, null);
                    builder.setView(content);
                    // builder.show();

                    final AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    ((Button) content.findViewById(R.id.button_dialog_delete)).setTextColor(Main.COLOR_TEXT);
                    ((Button) content.findViewById(R.id.button_dialog_delete)).setTypeface(Main.face);
                    ((Button) content.findViewById(R.id.button_dialog_delete)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Animation anim = AnimationUtils.loadAnimation(context, R.anim.myalpha);
                            v.startAnimation(anim);
                            alertDialog.dismiss();
                            Main.number_page = 2;
                            file_function.Delet_my_plalist();
                            Main.myadapter.notifyDataSetChanged();
                            Main.viewPager.setAdapter(Main.myadapter);
                            Main.viewPager.setCurrentItem(Main.number_page);
                        }

                    });
                    ((Button) content.findViewById(R.id.button_dialog_no)).setTextColor(Main.COLOR_TEXT);
                    ((Button) content.findViewById(R.id.button_dialog_no)).setTypeface(Main.face);
                    ((Button) content.findViewById(R.id.button_dialog_no)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Animation anim = AnimationUtils.loadAnimation(context, R.anim.myalpha);
                            v.startAnimation(anim);
                            alertDialog.dismiss();
                        }
                    });
                }else {
                    Main.Toast_error("Плейлист пуст");
                }
            }

        });


        ((Button)v.findViewById(R.id.open_aimp)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim = AnimationUtils.loadAnimation(context, R.anim.myalpha);
                v.startAnimation(anim);
                if(!file_function.My_plalist()[0].equals("Плейлист пуст")) {

                    //проверим есть вообще аимп
                    PackageManager pm = Main.context.getPackageManager();
                    PackageInfo pi = null;
                    try {
                        pi = pm.getPackageInfo("com.aimp.player", 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (pi != null) {
                        //откроем файл с сылкой в плеере
                        ComponentName cm = new ComponentName(
                                "com.aimp.player",
                                "com.aimp.player.views.MainActivity.MainActivity");

                        Intent intent = new Intent();
                        intent.setComponent(cm);

                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(Environment.getExternalStorageDirectory().toString()+"/aimp_radio/my_plalist.m3u"),"audio/mpegurl");
                        intent.setFlags(0x3000000);

                        startActivity(intent);
                        //иначе предложим системе открыть
                    }else {

                        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo));
                        final View content = LayoutInflater.from(context).inflate(R.layout.custom_dialog_no_aimp, null);
                        builder.setView(content);
                        final AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        Button dw_aimp = (Button)content.findViewById(R.id.button_dialog_dowload_aimp);
                        Button open_sys = (Button)content.findViewById(R.id.button_dialog_open_sistem);

                        dw_aimp.setTypeface(Main.face);
                        dw_aimp.setTextColor(Main.COLOR_TEXT);
                        dw_aimp.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Animation anim = AnimationUtils.loadAnimation(context, R.anim.myalpha);
                                v.startAnimation(anim);
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id=com.aimp.player"));
                                Main.context.startActivity(intent);
                            }
                        });
                        open_sys.setTypeface(Main.face);
                        open_sys.setTextColor(Main.COLOR_TEXT);
                        open_sys.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Animation anim = AnimationUtils.loadAnimation(context, R.anim.myalpha);
                                v.startAnimation(anim);



                                if(!file_function.My_plalist()[0].equals("Плейлист пуст")) {
                                    String send = "";

                                    for (String s : file_function.My_plalist()) {
                                        send += s + "\n";
                                    }
                                    Intent i = new Intent(android.content.Intent.ACTION_VIEW);
                                    // Uri data = Uri.parse(Environment.getExternalStorageDirectory().toString()+"/aimp_radio/my_plalist.m3u");
                                    Uri data = Uri.parse(send);
                                    i.setData(data);
                                    //проверим есть чем открыть или нет
                                    if (i.resolveActivity(Main.context.getPackageManager()) != null) {
                                        Main.context.startActivity(i);
                                    }else {
                                        Main.Toast_error("Системе не удалось ( ");
                                    }

                                }else {
                                    Main.Toast_error("Нечего отпралять, плейлист пуст");
                                }

                            }
                        });
                    }

                }else {
                    Main.Toast_error("Плёйлист пуст, добавьте хотябы одну станцию");
                }
            }
        });

        ((Button)v.findViewById(R.id.button_otpravit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim = AnimationUtils.loadAnimation(context, R.anim.myalpha);
                v.startAnimation(anim);

                if(!file_function.My_plalist()[0].equals("Плейлист пуст")) {
                    String send = "";

                    for (String s : file_function.My_plalist()) {
                        send += s + "\n";
                    }


                    final Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");

                    intent.putExtra(Intent.EXTRA_TEXT, send);
                    try {
                        startActivity(Intent.createChooser(intent, "Поделиться через"));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Main.Toast_error("Some error");
                    }
                }else {
                    Main.Toast_error("Нечего отпралять, плейлист пуст");
                }
            }
        });

        listView.setOnItemLongClickListener(this);

        return v;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final String selectedItem = parent.getItemAtPosition(position).toString(); //получаем строку



        final String[] m  = file_function.My_plalist();
        if(!m[0].equals("Плейлист пуст")) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo));
        final View content = LayoutInflater.from(context).inflate(R.layout.custom_dialog_delete_stancii, null);
        builder.setView(content);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();


            ((TextView) content.findViewById(R.id.text_voprosa_del_stncii)).setTypeface(Main.face);
            ((TextView) content.findViewById(R.id.text_voprosa_del_stncii)).setText("Точно удалить: \n" + selectedItem.substring(9, selectedItem.length() - 1) + " ?");

            ((Button) content.findViewById(R.id.button_dialog_delete)).setTextColor(Main.COLOR_TEXT);
            ((Button) content.findViewById(R.id.button_dialog_delete)).setTypeface(Main.face);
            ((Button) content.findViewById(R.id.button_dialog_delete)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Animation anim = AnimationUtils.loadAnimation(context, R.anim.myalpha);
                    v.startAnimation(anim);
                    alertDialog.dismiss();

                    ArrayList<String> masiv = new ArrayList<String>();

                    for (String s : m) {
                        masiv.add(s);
                    }

//пробуем удалить
                    if (masiv.remove(selectedItem.substring(9, selectedItem.length() - 1))) {
                        Main.number_page = 2;
                        //всё удаляем
                        file_function.Delet_my_plalist();
                        //записываем заново

                        //фильтр для нашего сигнала
                        IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction("File_created");

                        //приёмник  сигналов
                        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                if (intent.getAction().equals("File_created")) {
                                    //получим данные
                                    String s = intent.getStringExtra("update");
                                    if (s.equals("zaebis")) {
                                        //обновим старницу
                                        Main.myadapter.notifyDataSetChanged();
                                        Main.viewPager.setAdapter(Main.myadapter);
                                        Main.viewPager.setCurrentItem(Main.number_page);
                                    } else {
                                        Main.Toast_error("Ошибочка вышла тыкниете еще раз");
                                    }
                                }
                            }
                        };

                        //регистрируем приёмник
                        Main.context.registerReceiver(broadcastReceiver, intentFilter);


                        String url_link = "";
                        for (String s : masiv) {
                            url_link += s + "\n";
                        }

                        File_function file_function = new File_function();
                        file_function.Add_may_plalist_stansiy(url_link);
                    } else {
                        Toast.makeText(context, "Ошибка", Toast.LENGTH_SHORT).show();
                    }

                }

            });

            ((Button) content.findViewById(R.id.button_dialog_no)).setTextColor(Main.COLOR_TEXT);
            ((Button) content.findViewById(R.id.button_dialog_no)).setTypeface(Main.face);
            ((Button) content.findViewById(R.id.button_dialog_no)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Animation anim = AnimationUtils.loadAnimation(context, R.anim.myalpha);
                    v.startAnimation(anim);
                    alertDialog.dismiss();
                }
            });

        }else {
            Main.Toast_error("Плейлист пуст");
        }


        return true;
    }
}
